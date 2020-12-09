package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.CartConstant;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberResponseVo;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.constant.PayConstant;
import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.entity.PaymentInfoEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WareFeignService;
import com.atguigu.gulimall.order.interceptor.LoginInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.service.PaymentInfoService;
import com.atguigu.gulimall.order.to.OrderCreateTo;
import com.atguigu.gulimall.order.to.SpuInfoTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

//import io.seata.spring.annotation.GlobalTransactional;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    private PaymentInfoService paymentInfoService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {
        MemberResponseVo memberResponseVo = LoginInterceptor.loginUser.get();
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes(); // share attributes from main thread to asynchronous threa
        CompletableFuture<Void> itemAndStockFuture = CompletableFuture.supplyAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //1. 查出所有选中购物项
            List<OrderItemVo> checkedItems = cartFeignService.getCheckedItems();
            confirmVo.setItems(checkedItems);
            return checkedItems;
        }, executor).thenAcceptAsync((items) -> {
            //4. 库存
            List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            Map<Long, Boolean> hasStockMap = wareFeignService.getSkuHasStocks(skuIds).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
            confirmVo.setStocks(hasStockMap);
        }, executor);

        //2. 查出所有收货地址
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            List<MemberAddressVo> addressByUserId = memberFeignService.getAddressByUserId(memberResponseVo.getId());
            confirmVo.setMemberAddressVos(addressByUserId);
        }, executor);

        //3. 积分
        confirmVo.setIntegration(memberResponseVo.getIntegration());

        //5. 总价自动计算
        //6. 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId(), token, 30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);
        try {
            CompletableFuture.allOf(itemAndStockFuture, addressFuture).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return confirmVo;
    }


//    @GlobalTransactional
    // (isolation = Isolation.READ_COMMITTED)
    // (propagation = Propagation.REQUIRED)
    // (propagation = Propagation.REQUIRES_NEW)
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo) {
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        responseVo.setCode(0);
        //1. 验证防重令牌
        MemberResponseVo memberResponseVo = LoginInterceptor.loginUser.get();
        String script= "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long execute = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId()), submitVo.getOrderToken());
        if (execute == 0L) {
            //1.1 防重令牌验证失败
            responseVo.setCode(1);
            return responseVo;
        }else {
            //2. 创建订单、订单项
            OrderCreateTo order =createOrderTo(memberResponseVo, submitVo);

            //3. 验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = submitVo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                //4. 保存订单
                saveOrder(order);
                //5. 锁定库存
                List<OrderItemVo> orderItemVos = order.getOrderItems().stream().map((item) -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    return orderItemVo;
                }).collect(Collectors.toList());
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                lockVo.setLocks(orderItemVos);
                // 为了
                R r = wareFeignService.orderLockStock(lockVo);
                //5.1 锁定库存成功
                if (r.getCode()==0){
//                    try {
//                        int i = 10 / 0;
//                    } catch(Exception e) {
//                        throw new RuntimeException();
//                    }


                    responseVo.setOrder(order.getOrder());
                    responseVo.setCode(0);

                    //发送消息到订单延迟队列，判断过期订单
                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order.getOrder());

                    //清除购物车记录
                    BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(CartConstant.CART_PREFIX + memberResponseVo.getId());
                    for (OrderItemEntity orderItem : order.getOrderItems()) {
                        ops.delete(orderItem.getSkuId().toString());
                    }
                    return responseVo;
                }else {
                    //5.1 锁定库存失败
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);
                }

            }else {
                //验价失败
                responseVo.setCode(2);
                return responseVo;
            }
        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        OrderEntity order_sn = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));

        return order_sn;
    }

    /**
     * 关闭过期的的订单
     * @param orderEntity
     */
    @Override
    public void closeOrder(OrderEntity orderEntity) {
        //因为消息发送过来的订单已经是很久前的了，中间可能被改动，因此要查询最新的订单
        OrderEntity newOrderEntity = this.getById(orderEntity.getId());
        //如果订单还处于新创建的状态，说明超时未支付，进行关单
        if (newOrderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            OrderEntity updateOrder = new OrderEntity();
            updateOrder.setId(newOrderEntity.getId());
            updateOrder.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(updateOrder);

            //关单后发送消息通知其他服务进行关单相关的操作，如解锁库存
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(newOrderEntity, orderTo);
            // TODO Ensure message sent out 100%, 每一个消息都可以做好日志记录（给数据库保存每一个消息的详细信息）
            // TODO 定期扫描数据库将失败的消息再发送一次
            rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other",orderTo);
        }
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        OrderEntity orderEntity = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        PayVo payVo = new PayVo();
        payVo.setOut_trade_no(orderSn);
        BigDecimal payAmount = orderEntity.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
        payVo.setTotal_amount(payAmount.toString());

        List<OrderItemEntity> orderItemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        OrderItemEntity orderItemEntity = orderItemEntities.get(0);
        payVo.setSubject(orderItemEntity.getSkuName());
        payVo.setBody(orderItemEntity.getSkuAttrsVals());
        return payVo;
    }

    @Override
    public void handlerPayResult(PayAsyncVo payAsyncVo) {
        //保存交易流水
        PaymentInfoEntity infoEntity = new PaymentInfoEntity();
        String orderSn = payAsyncVo.getOut_trade_no();
        infoEntity.setOrderSn(orderSn);
        infoEntity.setAlipayTradeNo(payAsyncVo.getTrade_no());
        infoEntity.setSubject(payAsyncVo.getSubject());
        String trade_status = payAsyncVo.getTrade_status();
        infoEntity.setPaymentStatus(trade_status);
        infoEntity.setCreateTime(new Date());
        infoEntity.setCallbackTime(payAsyncVo.getNotify_time());
        paymentInfoService.save(infoEntity);

        //判断交易状态是否成功
        if (trade_status.equals("TRADE_SUCCESS") || trade_status.equals("TRADE_FINISHED")) {
            baseMapper.updateOrderStatus(orderSn, OrderStatusEnum.PAYED.getCode(), PayConstant.ALIPAY);
        }
    }

    private void saveOrder(OrderCreateTo orderCreateTo) {
        OrderEntity order = orderCreateTo.getOrder();
        order.setCreateTime(new Date());
        order.setModifyTime(new Date());
        this.save(order);
        orderItemService.saveBatch(orderCreateTo.getOrderItems());
    }

    private OrderCreateTo createOrderTo(MemberResponseVo memberResponseVo, OrderSubmitVo submitVo) {
        //用IdWorker生成订单号
        String orderSn = IdWorker.getTimeId();
        //构建订单
        OrderEntity entity = buildOrder(memberResponseVo, submitVo,orderSn);
        //构建订单项
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);
        //计算价格
        compute(entity, orderItemEntities);
        OrderCreateTo createTo = new OrderCreateTo();
        createTo.setOrder(entity);
        createTo.setOrderItems(orderItemEntities);
        return createTo;
    }

    private OrderEntity buildOrder(MemberResponseVo memberResponseVo, OrderSubmitVo submitVo, String orderSn) {

        OrderEntity orderEntity =new OrderEntity();

        orderEntity.setOrderSn(orderSn);

        //2) 设置用户信息
        orderEntity.setMemberId(memberResponseVo.getId());
        orderEntity.setMemberUsername(memberResponseVo.getUsername());

        //3) 获取邮费和收件人信息并设置
        FareVo fareVo = wareFeignService.getFare(submitVo.getAddrId());
        BigDecimal fare = fareVo.getFare();
        orderEntity.setFreightAmount(fare);
        MemberAddressVo address = fareVo.getAddress();
        orderEntity.setReceiverName(address.getName());
        orderEntity.setReceiverPhone(address.getPhone());
        orderEntity.setReceiverPostCode(address.getPostCode());
        orderEntity.setReceiverProvince(address.getProvince());
        orderEntity.setReceiverCity(address.getCity());
        orderEntity.setReceiverRegion(address.getRegion());
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());

        //4) 设置订单相关的状态信息
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setConfirmStatus(0);
        orderEntity.setAutoConfirmDay(7);

        return orderEntity;
    }

    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> checkedItems = cartFeignService.getCheckedItems();
        List<OrderItemEntity> orderItemEntities = checkedItems.stream().map((item) -> {
            OrderItemEntity orderItemEntity = buildOrderItem(item);
            //1) 设置订单号
            orderItemEntity.setOrderSn(orderSn);
            return orderItemEntity;
        }).collect(Collectors.toList());
        return orderItemEntities;
    }

    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        Long skuId = item.getSkuId();
        //2) 设置sku相关属性
        orderItemEntity.setSkuId(skuId);
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(item.getSkuAttrValues(), ";"));
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuPrice(item.getPrice());
        orderItemEntity.setSkuQuantity(item.getCount());
        //3) 通过skuId查询spu相关属性并设置
        R r = productFeignService.getSpuBySkuId(skuId);
        if (r.getCode() == 0) {
            SpuInfoTo spuInfo = r.getData(new TypeReference<SpuInfoTo>() {
            });
            orderItemEntity.setSpuId(spuInfo.getId());
            orderItemEntity.setSpuName(spuInfo.getSpuName());
            orderItemEntity.setSpuBrand(spuInfo.getBrandName());
            orderItemEntity.setCategoryId(spuInfo.getCatalogId());
        }
        //4) 商品的优惠信息(不做)

        //5) 商品的积分成长，为价格x数量
        orderItemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());
        orderItemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());

        //6) 订单项订单价格信息
        orderItemEntity.setPromotionAmount(BigDecimal.ZERO);
        orderItemEntity.setCouponAmount(BigDecimal.ZERO);
        orderItemEntity.setIntegrationAmount(BigDecimal.ZERO);

        //7) 实际价格
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity()));
        BigDecimal realPrice = origin.subtract(orderItemEntity.getPromotionAmount())
                                     .subtract(orderItemEntity.getCouponAmount())
                                     .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(realPrice);

        return orderItemEntity;
    }

    private void compute(OrderEntity entity, List<OrderItemEntity> orderItemEntities) {
        //总价
        BigDecimal total = BigDecimal.ZERO;
        //优惠价格
        BigDecimal promotion=new BigDecimal("0.0");
        BigDecimal integration=new BigDecimal("0.0");
        BigDecimal coupon=new BigDecimal("0.0");
        //积分
        Integer integrationTotal = 0;
        Integer growthTotal = 0;

        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            total=total.add(orderItemEntity.getRealAmount());
            promotion=promotion.add(orderItemEntity.getPromotionAmount());
            integration=integration.add(orderItemEntity.getIntegrationAmount());
            coupon=coupon.add(orderItemEntity.getCouponAmount());
            integrationTotal += orderItemEntity.getGiftIntegration();
            growthTotal += orderItemEntity.getGiftGrowth();
        }

        entity.setTotalAmount(total);
        entity.setPromotionAmount(promotion);
        entity.setIntegrationAmount(integration);
        entity.setCouponAmount(coupon);
        entity.setIntegration(integrationTotal);
        entity.setGrowth(growthTotal);

        //付款价格=商品价格+运费
        entity.setPayAmount(entity.getFreightAmount().add(total));

        //设置删除状态(0-未删除，1-已删除)
        entity.setDeleteStatus(0);
    }


}
package com.atguigu.gulimall.order.config;


import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2021000116663719";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCjXTvR4JAZy0av2boZSfHT6f+QW61D4bApXlqBK8h7wWpqLV10Fj7a/cqfnp486WgKQsiXufh79COrZiYlBKj+yGe5nZeGxIg1+0g8YDooXTe86isAaCdXGVJiJctYJF2EyqeRchHFh5ac5qUMk54TwRU2uL/i1n9vtCKQUI3NvtJU6VeM4R6yiyQrQlm/79/2fzfRMiAecd604pyIE3oNehdyW0KGECXSwSCetjLNb/y3DJYR922N5HqCECQBtG3bTQ5R9/03A7Efc05LIFd4NpP12yH+2VXduKMK++PsqA+LddapWF3RdyQnx0/tWk7bMjJfIokE1iCEqVQK9ZGzAgMBAAECggEATcRuW6TJNF4+UiwVmUBILJcyH6hkvPHNCIRwFRmgoLkcTVs3J7IHTfyO3pmFvn1mF6xfDUtsbKV17Xv9IJYtVMxWBFhu76rTJM9wPb2ZMeneRMI8oj7yq1XUkABp4pW9Jx/J0UU6DIxVOG/bljcXg1PhI2GjhvqRQ72+znBqcMxnCHfHUSlZ9TIpCvrIONbH1AhETj7IAJhCKmViZ4LVWlH6Z81TAZlhB0N7HEpupEzqvUMj6e9c+Q0Pkq0bc9iIxXQqJOEzMae7oAhvDBhFwR/ci3UIaXtkyF0Aki0MxRty3W24dCQa/SpOIyiAmarW/s47WOebT7V3hG87rr2FQQKBgQDu5QgHmDAbGi6KK7GH1t2hqydrqSYZC7TwG3iONFc0tSwHltx9e/dH4cYr5RfJ3Z858YreKAH2U+FLYOTefO89s18+LLx3jiMoY4QL039nIIRyinnbFJhFIIpAoZvqTmJEfn2Ln2HtHZzorSX02Q4W7Qz2QbTnfes75TMK1yDksQKBgQCvD7g4XXStSJoPWKGdgyHk7ZWZhfn07WqjtYfRy3qFJIQuNCa7mGmwADSwGEhwmEf6/sRKGNXiByuAZoi3a/TbnxVzHhlWNSMOGCY2VcLlI3pqt8tkw1QAejVZ/9Y+XSrL+pnQbmD9VkfuZkZoAo/LeIgtZq41KzRsDMFVenSFowKBgAu4Ipe0yI9bznKqyA/A46h9q/ZlGuBZqaupvVVEjg6g4KMSrIhfX3cgRUd7+pnFjH0cM1LIpaf+32uCOlgLyvS86uwy55/50hG2KBEoc/SMLNMtpeKwdPPXL9ER3IM3Cy0PA3uo+vXV4Q+tBtKnH0DSqjMecBB3JeYPTVy3xv5RAoGAeRL4KBOKtKX31xFHhXbCJGClDK76dwkUrfDI5LJEJH1i5cmv2CLLRax+xCOnRigr8E3vWAfhNRgcYAuKFsGVcaeO4DU69MJ/YioVk5Kw3mUxL0m6ZBArssDS0VH9zNgG11iUtPECJ3Ra/2jA3QKYjPLpy+yuI5uj/+66b/HnUN0CgYBbjzXAnVjTB31cjb/JvDyaPGkvndC01mjc10wT6qspQQA9Tv91zply9VvBusahEGgvBqMIiWcKc8Puy5dofq6i2waQnTY5PdFdP1Odpq2pIj9DN5aqiiXC3XzoRUJxvkJaR0xsfXWclcYYGpojt7xUzjVGp81sJvwPGAzvM+BJrw==";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyQQceVUChTJGtF/a8SXufhSxDTKporieTq9NO7yDZSpDlAX1zVPT/nf0KWAlxq1TYappWMIYtyrOABhJyn6flNP6vuSBiM5lYsepHvYrtRHqlFiJruEkiaCgEZBKL5aCfBHYj0oqgQn9MpNV/PEH4cBYAVaiI4+VX8CBUQfeEGjgN6OkpLULZ3X0JUkmSnVvCNJ1m3PD68IIlbOfEZXJUKCqmZhzprGR5VWswjxA+g87cMwvijL4gdkSy/daG62Bz5vApcmmMkuX1k1fMWP4ajZCASVw8HD+MSLRhd8We9F97gd8CW0TavzbdR+mTS5H4yEgO8F9HRAsbkhV9yu0yQIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url;

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url = "http://member.gulimall.com/memberOrder.html";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                                                            app_id, merchant_private_key, "json",
                                                            charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_expires\":\"1m\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}

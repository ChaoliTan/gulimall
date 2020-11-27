package com.atguigu.gulimall.search.thread;

import java.util.concurrent.*;

public class ThreadTest {
    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main method start...");

//        CompletableFuture.runAsync(() -> {
//            System.out.println("Current thread: " + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("Result: " + i);
//        }, executor);

//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("Current thread: " + Thread.currentThread().getId());
//            int i = 10 / 0;
//            System.out.println("Result: " + i);
//            return i;
//        }, executor).whenComplete((result, exception) -> {
//            System.out.println("Async complete...Result is:" + result + "; Exception is " + exception);
//        }).exceptionally(throwable -> {
//            return 10;
//        });
//        R apply(T t);


//        CompletableFuture.supplyAsync(() -> {
//            System.out.println("Current thread: " + Thread.currentThread().getId());
//            int i = 10 / 0;
//            System.out.println("Result: " + i);
//            return i;
//        }, executor).thenRun(() -> {
//            System.out.println("Task 2 starting...");
//        });


//        CompletableFuture.supplyAsync(() -> {
//            System.out.println("Current thread: " + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("Result: " + i);
//            return i;
//        }, executor).thenAccept(res -> {
//            System.out.println("Task 2 starting..." + res);
//        });

//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("Current thread: " + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("Result: " + i);
//            return i;
//        }, executor).thenApply(res -> {
//            System.out.println("Task 2 starting..." + res);
//            return "Hello" + res;
//        });

//        CompletableFuture<Object> future01 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("01 Current thread: " + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("01 Result: " + i);
//            return i;
//        }, executor);
//
//        CompletableFuture<Object> future02 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("02 Current thread: " + Thread.currentThread().getId());
//            int i = 10 / 4;
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("02 Result: " + i);
//
//            return "hello";
//        }, executor);

//        future01.runAfterBothAsync(future02, () -> {
//            System.out.println("03 thread");
//        }, executor);

//        future01.thenAcceptBothAsync(future02, (f1, f2) -> {
//            System.out.println("previous res: " + f1 + "->" + f2);
//        } , executor);

//        CompletableFuture<String> future = future01.thenCombineAsync(future02, (f1, f2) -> {
//            return f1 + "? " + f2 + "-> Haha";
//        }, executor);



//        future01.runAfterEither(future02, ()->{
//            System.out.println("03 thread");
//        });

//        future01.acceptEither(future02, (res) -> {
//            System.out.println("03 thread" + res);
//        });

//        CompletableFuture<String> future = future01.applyToEither(future02, res -> {
//            System.out.println("03 thread" + res);
//            return res.toString() + "-> haha";
//        });


        CompletableFuture<Object> futureImg = CompletableFuture.supplyAsync(() -> {
            System.out.println("Product image info...");
            return "hello.jpg";
        }, executor);

        CompletableFuture<Object> futureAttr = CompletableFuture.supplyAsync(() -> {
            System.out.println("Product attr info...");
            return "256G";
        }, executor);

        CompletableFuture<Object> futureDesc = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Product description info...");

            return "apple";
        }, executor);

//        CompletableFuture<Void> allOf = CompletableFuture.allOf(futureImg, futureAttr, futureDesc);
//        allOf.get(); // wait for all finished

        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(futureImg, futureAttr, futureDesc);
        anyOf.get();

        System.out.println("main method end..." + anyOf.get());
    }

    public void thread (String[] args) {
        System.out.println("main method start...");



        /**
         * 1) 继承Thread
         *      Thread01 thread01 = new Thread01();
         *      thread01.start(); // 启动线程
         * 2）实现Runnable接口
         *         Runnable01 runnable01 = new Runnable01();
         *         new Thread(runnable01).start();
         * 3）实现Callable接口 + FutureTask (可以拿到返回结果，可以处理异常）
         *         FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
         *         new Thread(futureTask).start();
         *         Integer integer = null;
         *         try {
         *             integer = futureTask.get();
         *         } catch (InterruptedException e) {
         *             e.printStackTrace();
         *         } catch (ExecutionException e) {
         *             e.printStackTrace();
         *         }
         * 4）线程池 (Best Practice) [ExecutorService]
         *      1）创建
         *          1）Executors
         *          2) new ThreadPoolExecutor
         *      Future: 可以获取异步结果
         *
         * 区别：
         *     1，2 不能得到返回值。 3 可以获取返回值
         *     1，2，3都不能控制资源
         *     4可以控制资源，性能稳定。
         */

        /**
         * Creates a new {@code ThreadPoolExecutor} with the given initial
         * parameters.
         *
         * @param corePoolSize the number of threads to keep in the pool, even
         *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
         * @param maximumPoolSize the maximum number of threads to allow in the
         *        pool
         * @param keepAliveTime when the number of threads is greater than
         *        the core, this is the maximum time that excess idle threads
         *        will wait for new tasks before terminating.
         * @param unit the time unit for the {@code keepAliveTime} argument
         * @param workQueue the queue to use for holding tasks before they are
         *        executed.  This queue will hold only the {@code Runnable}
         *        tasks submitted by the {@code execute} method.
         * @param threadFactory the factory to use when the executor
         *        creates a new thread
         * @param handler the handler to use when execution is blocked
         *        because the thread bounds and queue capacities are reached
         * @throws IllegalArgumentException if one of the following holds:<br>
         *         {@code corePoolSize < 0}<br>
         *         {@code keepAliveTime < 0}<br>
         *         {@code maximumPoolSize <= 0}<br>
         *         {@code maximumPoolSize < corePoolSize}
         * @throws NullPointerException if {@code workQueue}
         *         or {@code threadFactory} or {@code handler} is null
         */
//        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 200, 10, TimeUnit.SECONDS, new LinkedBlockingDeque<>(100000), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
//
//        Executors.newCachedThreadPool();
//        Executors.newFixedThreadPool(5);
//        Executors.newScheduledThreadPool(5);
//        Executors.newSingleThreadExecutor();




        System.out.println("main method end...");
    }

    public static class Thread01 extends Thread {

        @Override
        public void run() {
            System.out.println("Current thread: " + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("Result: " + i);

        }
    }

    public static class Runnable01 implements Runnable {

        @Override
        public void run() {
            System.out.println("Current thread: " + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("Result: " + i);
        }
    }

    public static class Callable01 implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            System.out.println("Current thread: " + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("Result: " + i);
            return i;
        }
    }


}

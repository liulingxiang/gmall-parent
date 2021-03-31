package test5;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public class Test01 {
    public static void main(String[] args) throws InterruptedException {

        // 启动独立无返回值线程
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                System.out.println("启动一个新线程");
            }
        });

        // 启动独立有返回值线程
        CompletableFuture<Double> completableFuture1 = CompletableFuture.supplyAsync(new Supplier<Double>() {
            @Override
            public Double get() {
                Double result = 100d;
                int i = 1 / 0;
                System.out.println("启动一个新线程" + result);
                return result;
            }
        });

        // 如果线程抛异常
        completableFuture1.exceptionally(new Function<Throwable, Double>() {
            @Override
            public Double apply(Throwable throwable) {
                System.out.println("如果抛异常则一切归零");
                System.out.println(throwable);
                return 0d;
            }
        });

        Thread.sleep(1000);

        System.out.println("主线程执行");


    }

}

package test1;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class MyCyclicBarrier {
    public static void main(String[] args) {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(7,() -> {
            System.out.println("已集齐7颗龙珠，开始召唤神龙。。。");
        });

        for (int i = 1; i < 8; i++) {
            new Thread(()->{
                Random random = new Random();
                int n = random.nextInt(10);
                try {
                    Thread.sleep(n*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("收集到"+Thread.currentThread().getName()+"星龙珠");
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            },i+"").start();
        }
    }
}

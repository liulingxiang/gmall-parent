package test1;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MySemaphore {
    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(3);//模拟三个车位

        for (int i = 1; i < 7; i++) {
            new Thread(() -> {

                try {
                    semaphore.acquire();//占用资源
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("第" + Thread.currentThread().getName() + "号车进入停车位");
                Random random = new Random();
                int n = random.nextInt(10);
                try {
//                    Thread.sleep(n*1000);
                    TimeUnit.SECONDS.sleep(n);//1.8以后新的睡眠方式
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + "号车离开停车位。。。。");
                semaphore.release();//释放资源
            }, i + "").start();
        }
    }
}

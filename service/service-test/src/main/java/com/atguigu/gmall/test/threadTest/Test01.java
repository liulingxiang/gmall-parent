package com.atguigu.gmall.test.threadTest;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Test01 {
    public static void main(String[] args) {
        Cooking cooking = new Cooking();
        new Thread(()->{
            for (int i = 0; i < 10; i++) {
                cooking.one();
            }
        },"一号大师").start();
        new Thread(()->{
            for (int i = 0; i < 10; i++) {
                cooking.two();
            }
        },"二号大师").start();
        new Thread(()->{
            for (int i = 0; i < 10; i++) {
                cooking.three();
            }
        },"三号大师").start();
    }
}

class Cooking{
    Long num = 0l ;
    Long n = 1l;
    Lock lock = new ReentrantLock();
    Condition lock1 = lock.newCondition();
    Condition lock2 = lock.newCondition();
    Condition lock3 = lock.newCondition();
    public void one(){
        lock.lock();
        while (num!=0){
            try {
                lock1.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        num++;
        System.out.println(Thread.currentThread().getName()+"洗菜大师：张无忌");
        lock2.signal();
        lock.unlock();
    }
    public void two(){
        lock.lock();
        while (num!=1){
            try {
                lock2.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(Thread.currentThread().getName()+"切菜大师：杨过");
        num++;
        lock3.signal();
        lock.unlock();
    }
    public void three(){
        lock.lock();
        while (num!=2){
            try {
                lock3.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(Thread.currentThread().getName()+"炒菜大师：张三丰");
        System.out.println("第"+(n++)+"道菜已经完成---------!");
        num = 0l;
        lock1.signal();
        lock.unlock();
    }
}
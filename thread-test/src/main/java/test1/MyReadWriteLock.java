package test1;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MyReadWriteLock {

    Object myData = null;
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void write(Object date){
        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("稍等，正在写入数据。。。"+date);
            this.myData = date;
        }finally {
            writeLock.unlock();
        }
    }
    public Object read(){
        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        readLock.lock();
        try {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName()+"号同学正在读取数据......"+myData);
        }finally {
            readLock.unlock();
        }
        return myData;
    }
}
class test{
    public static void main(String[] args) {
        MyReadWriteLock myReadWriteLock = new MyReadWriteLock();
        new Thread(()->{
            myReadWriteLock.write("下课了");
            System.out.println();
        }).start();
        for (int i = 0; i < 50; i++) {
            new Thread(()->{
                myReadWriteLock.read();
            },i+"").start();
        }
    }
}

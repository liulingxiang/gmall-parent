package com.atguigu.gmall.test;

public class Num {
     Long num = 1l;
     char str = 'A';
     Long n = 0l;
    public synchronized void zero(){
        while (num!=0){
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(str);
        str++;
        num ++;
        notifyAll();
    }

    public synchronized void one(){
        n++;
        while (num!=1){
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(n);
        n++;
        System.out.println(n);
        num --;
        notifyAll();
    }
}

package com.atguigu.gmall.test;

public class Test01 {
    public static void main(String[] args) {
        Num num = new Num();

        new Thread(()->{
            for (int i = 0; i < 26; i++) {
                num.one();
            }
        },"1").start();
        new Thread(()->{
            for (int i = 0; i < 26; i++) {
                num.zero();
            }
        },"0").start();
    }
}



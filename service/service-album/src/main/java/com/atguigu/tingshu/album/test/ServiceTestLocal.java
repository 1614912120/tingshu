package com.atguigu.tingshu.album.test;

public class ServiceTestLocal {

    private static ThreadLocal<String> threadLocal = new ThreadLocal<>();

    public static void main(String[] args) {
        new Thread(()->{
            threadLocal.set("dddd");

        },"sds").start();
    }
}
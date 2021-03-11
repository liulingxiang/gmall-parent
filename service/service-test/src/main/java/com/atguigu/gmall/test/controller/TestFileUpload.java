package com.atguigu.gmall.test.controller;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

import java.io.IOException;

public class TestFileUpload {
    public static void main(String[] args) {

        String path = TestFileUpload.class.getClassLoader().getResource("tracker.conf").getPath();

        String imgUrl = "http://192.168.200.128";

        // 配置fdfsClient
        try {
            ClientGlobal.init(path);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
        // 获得trackerClient
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer clientConnection = null;
        try {
            clientConnection = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 通过tracker获得storage
        StorageClient storageClient = new StorageClient(clientConnection,null);
        // 通过storage上传文件
        String[] urls = new String[0];
        try {
            urls = storageClient.upload_file("C:/Users/Administrator/Desktop/wallpaper/1.jpg", "jpg", null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
        // 返回fileId对应的url
        for (String url : urls) {
            System.out.println("url = " + url);
            imgUrl = imgUrl + "/"+ url;
        }
        System.out.println(imgUrl);
    }
}

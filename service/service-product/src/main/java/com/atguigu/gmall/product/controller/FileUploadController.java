package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.result.Result;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/admin/product/")
@CrossOrigin
public class FileUploadController {

    @RequestMapping("fileUpload")
    public Result fileUpload(MultipartFile file){
        String path = FileUploadController.class.getClassLoader().getResource("tracker.conf").getPath();

        String imgUrl = "http://192.168.200.128:8080";

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
        String originalFilename = file.getOriginalFilename();
        String filenameExtension = StringUtils.getFilenameExtension(originalFilename);
        String[] urls = new String[0];
        try {
            urls = storageClient.upload_file(file.getBytes(), filenameExtension, null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
        // 返回fileId对应的url
        for (String url : urls) {
            imgUrl = imgUrl + "/"+ url;
        }
        return Result.ok(imgUrl);
    }
}

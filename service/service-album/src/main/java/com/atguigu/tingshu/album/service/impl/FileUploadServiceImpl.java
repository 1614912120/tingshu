package com.atguigu.tingshu.album.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.atguigu.tingshu.album.config.MinioConstantProperties;
import com.atguigu.tingshu.album.service.FileUploadService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {
    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConstantProperties props;


    @Override
    public String uploadImage(MultipartFile file) {
        try {
            BufferedImage read = ImageIO.read(file.getInputStream());
            if(read == null) {
                throw new RuntimeException("图片非法");
            }
            String floderName = DateUtil.today();
            String fileName = IdUtil.randomUUID();
            String extName = FileUtil.extName(file.getOriginalFilename());
            String objectName = "/"+floderName+"/"+fileName+"."+extName;
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(props.getBucketName()).object(objectName).stream(
                                    file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            return props.getEndpointUrl() + "/" + props.getBucketName() + objectName;
        } catch (Exception e) {
            log.error("[专辑服务]文件上传失败：{}", e);
            throw new RuntimeException(e);
        }
    }
}

package com.sky.service.impl;

import com.sky.properties.MinioOssProperties;
import com.sky.service.CommonService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * @author raoxin
 */
@Slf4j
@Service
public class CommonServiceImpl implements CommonService {
    @Autowired
    private MinioOssProperties minioOssProperties;
    @Autowired
    private MinioClient minioClient;

    public  String upload(MultipartFile file) throws Exception{
        String filename = new SimpleDateFormat("yyyyMMdd").format(new Date()) +
                "/" + UUID.randomUUID().toString() + "-" + file.getOriginalFilename();

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioOssProperties.getBucketName())
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .object(filename)
                        .contentType(file.getContentType())
                        .build());
        String url = String.join("/", minioOssProperties.getEndpoint(), minioOssProperties.getBucketName(), filename);
        log.info("文件上传到:{}", url);
        return url;
    }
}

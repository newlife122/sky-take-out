package com.sky.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author raoxin
 */
public interface CommonService {
    String upload(MultipartFile file) throws Exception;
}

package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.CommonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author raoxin
 */
@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api(tags = "通用接口")
public class CommonController {

    @Autowired
    CommonService commonService;
    @PostMapping("/upload")
    @ApiOperation(value = "文件上传")
    public Result<String> upload(MultipartFile file){
        String url = null;
        try {
            url = commonService.upload(file);
        }catch (Exception e){
            e.printStackTrace();
            log.error("文件上传失败{}",e.toString());
        }
        return Result.success(url);
    }
}

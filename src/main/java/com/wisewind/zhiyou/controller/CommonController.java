package com.wisewind.zhiyou.controller;

import com.wisewind.zhiyou.common.BaseResponse;
import com.wisewind.zhiyou.common.ErrorCode;
import com.wisewind.zhiyou.common.ResultUtils;
import com.wisewind.zhiyou.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/upload")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping
    public BaseResponse<String> upload(MultipartFile file){
        log.info("文件上传：{}",file);
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String objectName = UUID.randomUUID().toString() + extension;
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            return ResultUtils.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败：{}",e);
        }
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR);
    }
}

package com.crabdp.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.crabdp.common.Result;
import com.crabdp.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @program: crabdp
 * @description:
 * @author: snow
 * @create: 2024-08-27 13:53
 **/

@Slf4j
@RestController
@RequestMapping("upload")
public class UploadController {

    @PostMapping("/blog")
    public Result uploadImage(@RequestParam("file") MultipartFile imageFile) {

        try {
            //获取原文件名
            String fileName = imageFile.getOriginalFilename();
            // 生成新文件名
            String newFileName = createNewFileName(fileName);
            // 保存文件
            imageFile.transferTo(new File(SystemConstants.IMAGE_UPLOAD_DIR + newFileName));
            log.debug("上传文件成功，文件名：{}", newFileName);
            return Result.ok(newFileName);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败",e);
        }

    }


    private String createNewFileName(String originalFilename) {
        // 获取后缀
        String suffix = StrUtil.subAfter(originalFilename, ".", true);
        // 生成目录
        String name = UUID.randomUUID().toString();
        int hash = name.hashCode();
        int d1 = hash & 0xF;
        int d2 = (hash >> 4) & 0xF;
        // 判断目录是否存在
        File dir = new File(SystemConstants.IMAGE_UPLOAD_DIR, StrUtil.format("/blogs/{}/{}", d1, d2));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 生成文件名
        return StrUtil.format("/blogs/{}/{}/{}.{}", d1, d2, name, suffix);
    }

    @GetMapping("/blog/delete")
    public Result deleteBlogImg(@RequestParam("name") String filename) {
        File file = new File(SystemConstants.IMAGE_UPLOAD_DIR, filename);
        if (file.isDirectory()) {
            return Result.fail("错误的文件名称");
        }
        FileUtil.del(file);
        return Result.ok();

    }

}

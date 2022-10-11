package com.tcl.reggie.controller;

import com.tcl.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/*
    文件上传和下载
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     *
     * @param file 必须和网页From data 中 name一致
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        // file是一个临时文件，本次请求玩成后就会消失，需要转存到其它位置
        log.info(file.toString());

        // 原始文件名
        String originalFilename = file.getOriginalFilename();
        // 获取原始文件后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 使用UUID更改文件名，防止文件名相同覆盖

        // 判断basePath目录是否存在
        File dir = new File(basePath);
        if(!dir.exists()){
            dir.mkdirs();
        }

        String fileName = UUID.randomUUID().toString()+suffix;
        try {
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 返回文件名，用于后序的保存
        return R.success(fileName);
    }

    /**
     * 文件下载，上传到页面
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){

        try {
            // 输入流，读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

            // 输出流，将文件写回浏览器，在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            int len=0;
            byte[] bytes = new byte[1024];
            while ((len=fileInputStream.read(bytes))!=-1){
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }

            // 关闭资源
            outputStream.close();
            fileInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

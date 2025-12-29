package com.xxe.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @description: 文件上传控制器，处理Word和Excel文件上传及XXE测试
 * @author: [Author]
 * @date: [Current Date]
 */
@Controller
public class FileUploadController {

    // 允许的文件类型和扩展名
    private static final Set<String> ALLOWED_FILE_TYPES = new HashSet<>(Arrays.asList(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    ));
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "docx", "xlsx"
    ));

    /**
     * 处理文件上传请求
     * 
     * @param file 上传的文件
     * @return 处理结果
     */
    @PostMapping("/upload")
    @ResponseBody
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            return "请选择一个文件上传。";
        }

        // 获取文件信息
        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();

        // 验证文件类型
        if (!isValidFileType(filename, contentType)) {
            return "无效的文件类型。仅支持 .docx 和 .xlsx 文件。";
        }

        try {
            // 根据文件扩展名处理文件
            String result;
            if (filename.toLowerCase().endsWith(".docx")) {
                // 处理Word文档
                result = handleWordFile(file);
            } else if (filename.toLowerCase().endsWith(".xlsx")) {
                // 处理Excel文档
                result = handleExcelFile(file);
            } else {
                return "不支持的文件类型。";
            }

            return "读取" + (filename.endsWith(".docx") ? "Word" : "Excel") + "文档内容: " + result;
        } catch (Exception e) {
            return "处理文件时出错: " + e.getMessage();
        }
    }

    /**
     * 处理Word文档
     */
    private String handleWordFile(MultipartFile file) throws IOException {
        try {
            WordXxeReader reader = new WordXxeReader();
            return reader.readDocument(file.getInputStream());
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * 处理Excel文档
     */
    private String handleExcelFile(MultipartFile file) throws IOException {
        try {
            ExcelXxeReader reader = new ExcelXxeReader();
            return reader.readDocument(file.getInputStream());
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * 验证文件类型是否有效
     */
    private boolean isValidFileType(String filename, String contentType) {
        if (filename == null || contentType == null) {
            return false;
        }
        
        // 检查文件扩展名
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return false;
        }
        
        String extension = filename.substring(lastDotIndex + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension) && ALLOWED_FILE_TYPES.contains(contentType);
    }

    /**
     * 首页接口
     */
    @RequestMapping("/")
    public String index() {
        return "index";
    }
}
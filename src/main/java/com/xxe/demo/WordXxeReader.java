package com.xxe.demo;

import org.apache.poi.xwpf.usermodel.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @description: 读取Word文档文件，支持XXE漏洞测试
 * @author: KimJun (Modified for Word)
 * @date: [Current Date] // Updated date
 */
public class WordXxeReader {

    /**
     * 构造函数，初始化Word文档数据
     *
     * @param filePath Word文档路径
     * @throws IOException 如果文件读取失败
     */
    public WordXxeReader(String filePath) throws IOException {
        // 此构造函数保留以兼容原有代码
    }
    
    /**
     * 无参构造函数，用于创建实例
     */
    public WordXxeReader() {
        // 空构造函数
    }

    /**
     * 读取文档内容
     * 
     * @param inputStream 文档输入流
     * @return 读取结果
     * @throws IOException 如果读取失败
     */
    public String readDocument(InputStream inputStream) throws IOException {
        StringBuilder result = new StringBuilder();
        
        try {
            // 创建文档对象
            XWPFDocument document = null;
            
            // 直接读取文档，不区分模式
            document = new XWPFDocument(inputStream);
            
            // 读取文档内容
            if (document != null) {
                // 读取段落
                List<XWPFParagraph> paragraphs = document.getParagraphs();
                if (!paragraphs.isEmpty()) {
                    result.append("\n\n段落内容:");
                    for (int i = 0; i < paragraphs.size(); i++) {
                        String text = paragraphs.get(i).getText();
                        if (text != null && !text.isEmpty()) {
                            result.append("\n  段落").append(i + 1).append(": ").append(text);
                        }
                    }
                }
                
                // 读取表格
                List<XWPFTable> tables = document.getTables();
                if (!tables.isEmpty()) {
                    result.append("\n\n表格内容:");
                    for (int i = 0; i < tables.size(); i++) {
                        XWPFTable table = tables.get(i);
                        result.append("\n  表格").append(i + 1).append(":");
                        
                        List<XWPFTableRow> rows = table.getRows();
                        for (int j = 0; j < rows.size(); j++) {
                            XWPFTableRow row = rows.get(j);
                            List<XWPFTableCell> cells = row.getTableCells();
                            
                            StringBuilder rowText = new StringBuilder();
                            rowText.append("\n    行").append(j).append(": ");
                            
                            for (int k = 0; k < cells.size(); k++) {
                                String cellText = cells.get(k).getText();
                                rowText.append("[列").append(k).append("]").append(cellText);
                                if (k < cells.size() - 1) {
                                    rowText.append(" | ");
                                }
                            }
                            
                            result.append(rowText);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            result.append("\n读取文档时发生错误: " + e.getMessage());
        }
        
        return result.toString();
    }

    // 打印Word文档内容（包括段落和表格）
    public void readWordContent() {
        // 保留原有方法以兼容旧代码
        System.out.println("此方法已弃用，请使用readDocument方法。");
    }

    // 实现 AutoCloseable 接口的 close 方法
    public void close() throws IOException {
        // 空实现
    }

    // 测试方法
    public static void main(String[] args) {
        String wordFilePath = "/Users/chenguang/Desktop/code/xxe_docx/1.docx"; // 修改为你的 .docx 文件路径

        try (FileInputStream fis = new FileInputStream(wordFilePath)) {
            WordXxeReader reader = new WordXxeReader();
            String result = reader.readDocument(fis);
            System.out.println(result);
        } catch (IOException e) {
            System.err.println("读取 Word 文档时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
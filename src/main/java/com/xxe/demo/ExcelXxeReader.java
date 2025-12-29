package com.xxe.demo;

import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * @description: 读取Excel文档文件，支持XXE漏洞测试
 * @author: KimJun (Modified for Excel)
 * @date: [Current Date]
 */
public class ExcelXxeReader {

    /**
     * 构造函数，初始化Excel文档数据
     *
     * @param filePath Excel文档路径
     * @throws IOException 如果文件读取失败
     */
    public ExcelXxeReader(String filePath) throws IOException {
        // 此构造函数保留以兼容原有代码
    }
    
    /**
     * 无参构造函数，用于创建实例
     */
    public ExcelXxeReader() {
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
            // 创建工作簿对象
            XSSFWorkbook workbook = null;
            
            // 直接读取文档，不区分模式
            workbook = new XSSFWorkbook(inputStream);
            
            // 读取文档内容
            if (workbook != null) {
                // 获取工作表数量
                int sheetCount = workbook.getNumberOfSheets();
                result.append("\n发现工作表数量: ").append(sheetCount);
                
                // 遍历每个工作表
                for (int i = 0; i < sheetCount; i++) {
                    XSSFSheet sheet = workbook.getSheetAt(i);
                    String sheetName = sheet.getSheetName();
                    result.append("\n\n工作表 " + (i + 1) + " [" + sheetName + "]:");
                    
                    // 遍历每一行
                    Iterator<Row> rowIterator = sheet.iterator();
                    int rowNum = 0;
                    
                    while (rowIterator.hasNext()) {
                        Row row = rowIterator.next();
                        
                        // 仅读取前10行以避免数据过多
                        if (rowNum > 10) {
                            result.append("\n  ... 更多行已省略 ...");
                            break;
                        }
                        
                        StringBuilder rowText = new StringBuilder();
                        rowText.append("\n  行").append(row.getRowNum()).append(": ");
                        
                        // 获取这一行的最大列数
                        int maxCellNum = row.getLastCellNum();
                        
                        // 遍历每个单元格
                        for (int cellNum = 0; cellNum < maxCellNum; cellNum++) {
                            Cell cell = row.getCell(cellNum);
                            String cellValue = getCellValue(cell);
                            rowText.append("[列").append(cellNum).append("]").append(cellValue);
                            
                            if (cellNum < maxCellNum - 1) {
                                rowText.append(" | ");
                            }
                        }
                        
                        result.append(rowText);
                        rowNum++;
                    }
                }
            }
            
        } catch (Exception e) {
            result.append("\n读取文档时发生错误: " + e.getMessage());
        }
        
        return result.toString();
    }
    
    /**
     * 获取单元格的值，处理不同类型的单元格
     */
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "空";
        }
        
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case Cell.CELL_TYPE_BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case Cell.CELL_TYPE_FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (IllegalStateException e) {
                    return cell.getRichStringCellValue().getString();
                }
            case Cell.CELL_TYPE_BLANK:
                return "";
            default:
                return "未知类型";
        }
    }

    // 测试方法
    public static void main(String[] args) {
        String excelFilePath = "/Users/chenguang/Desktop/code/xxe_xlsx/1.xlsx"; // 修改为实际的 .xlsx 文件路径

        try (FileInputStream fis = new FileInputStream(excelFilePath)) {
            ExcelXxeReader reader = new ExcelXxeReader();
            String result = reader.readDocument(fis);
            System.out.println(result);
        } catch (IOException e) {
            System.err.println("读取 Excel 文档时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
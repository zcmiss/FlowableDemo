package com.zc.flowabledemo.utils;

import com.microsoft.schemas.office.office.CTLock;
import com.microsoft.schemas.vml.*;
import com.spire.doc.Document;
import com.spire.doc.FileFormat;
import com.spire.doc.Section;
import com.spire.doc.TextWatermark;
import com.spire.doc.documents.WatermarkLayout;
import liquibase.pro.packaged.F;
import liquibase.pro.packaged.X;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
public class WatermarkUtil {

    /**
     * word 文档格式的后缀
     */
    private static final String WORD_DOCX = "docx";
    /**
     * word字体
     */
    private static final String FONT_NAME = "宋体";
    /**
     * 字体大小
     */
    private static final String FONT_SIZE = "0.2pt";
    /**
     * 字体颜色
     */
    private static final String FONT_COLOR = "#d0d0d0";

    /**
     * 一个字平均长度，单位pt，用于：计算文本占用的长度（文本总个数*单字长度）
     */
    private static final Integer WIDTH_PER_WORD = 10;
    /**
     * 与顶部的间距
     */
    private static Integer STYLE_TOP = 0;
    /**
     * 文本旋转角度
     */
    private static final String STYLE_ROTATION = "30";


    /**
     * doc文件转换成docx格式文档
     */
    public static void docToDocxConverter(String docFilePath, String docxFilePath, String fingerText) {
        try {
            // 读取DOC文件
            InputStream inputStream = Files.newInputStream(Paths.get(docFilePath));
            HWPFDocument doc = new HWPFDocument(inputStream);
            // 创建一个新的DOCX文档
            XWPFDocument docx = new XWPFDocument();
            // 获取DOC文档的内容范围
            Range range = doc.getRange();
            // 从DOC文档中提取内容并复制到新的DOCX文档中
            for (int i = 0; i < range.numParagraphs(); i++) {
                Paragraph sourcePara = range.getParagraph(i);
                XWPFParagraph targetPara = docx.createParagraph();

                // 创建新的运行，并复制段落文本
                XWPFRun targetRun = targetPara.createRun();
                targetRun.setText(sourcePara.text());

                // 获取段落样式
                for (int j = 0; j < sourcePara.numCharacterRuns(); j++) {
                    CharacterRun sourceRun = sourcePara.getCharacterRun(j);

                    // 应用字体样式到目标字符运行
                    if (sourceRun.isBold()) {
                        targetRun.setBold(true);
                    }
                    if (sourceRun.isItalic()) {
                        targetRun.setItalic(true);
                    }
                    if (sourceRun.getUnderlineCode() != 0) {
                        targetRun.setUnderline(UnderlinePatterns.SINGLE);
                    }
                }

                // 设置段落样式
                targetPara.setAlignment(getAlignment((byte) sourcePara.getJustification()));
            }


            //保存文档
            String uuidPath = UUID.randomUUID().toString().replace("-", "") + "." + WORD_DOCX;
            // 保存新创建的DOCX文档
            OutputStream outputStream = Files.newOutputStream(Paths.get(uuidPath));
            docx.write(outputStream);

            // 关闭流
            inputStream.close();
            outputStream.close();

            //
            waterMarkDocXDocument(uuidPath, docxFilePath, fingerText);
            //boolean deleteFile = deleteFile(uuidPath);
            // log.info("文件下载转换的临时文件：{}删除：{}", uuidPath, deleteFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ParagraphAlignment getAlignment(byte justification) {
        switch (justification) {
            case 0:
                return ParagraphAlignment.LEFT;
            case 1:
                return ParagraphAlignment.CENTER;
            case 2:
                return ParagraphAlignment.RIGHT;
            case 3:
                return ParagraphAlignment.BOTH;
            default:
                return ParagraphAlignment.LEFT;
        }
    }

    /**
     * 删除文件
     *
     * @param filePath 要删除的文件路径
     * @return boolean 是否删除成功
     */
    public static boolean deleteFile(String filePath) {
        File fileToDelete = new File(filePath);
        return fileToDelete.exists() && fileToDelete.delete();
    }


    /**
     * doc文档太添加水印
     *
     * @param inPutPath  输入
     * @param putPutPath 输出路径
     * @param fingerText 水印文本
     * @date: 2024/1/25 23:42
     **/
    public static void waterMarkDocXDocument(String inPutPath, String putPutPath, String fingerText) {

        long beginTime = System.currentTimeMillis();

        try (
                OutputStream out = Files.newOutputStream(Paths.get(putPutPath));
                InputStream in = Files.newInputStream(Paths.get(inPutPath));
                OPCPackage srcPackage = OPCPackage.open(in);
                XWPFDocument doc = new XWPFDocument(srcPackage)
        ) {

            // 把整页都打上水印
            for (int lineIndex = -5; lineIndex < 20; lineIndex++) {
                STYLE_TOP = 400 * lineIndex;
                waterMarkDocXDocument(doc, fingerText);
            }
            // 输出新文档
            doc.write(out);

            log.info("添加水印成功!,一共耗时{}毫秒", System.currentTimeMillis() - beginTime);

        } catch (IOException | InvalidFormatException e) {
            throw new RuntimeException(String.valueOf(e));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 为文档添加水印
     *
     * @param doc        需要被处理的docx文档对象
     * @param fingerText 需要添加的水印文字
     */
    public static void waterMarkDocXDocument(XWPFDocument doc, String fingerText) {
        // 水印文字之间使用8个空格分隔
        fingerText = fingerText + repeatString(" ", 20);
        // 一行水印重复水印文字次数
        fingerText = repeatString(fingerText, 8);
        // 如果之前已经创建过 DEFAULT 的Header，将会复用
        XWPFHeader header = doc.createHeader(HeaderFooterType.DEFAULT);
        int size = header.getParagraphs().size();
        if (size == 0) {
            header.createParagraph();
        }
        CTP ctp = header.getParagraphArray(0).getCTP();
        byte[] rsidr = doc.getDocument().getBody().getPArray(0).getRsidR();
        byte[] rsidrDefault = doc.getDocument().getBody().getPArray(0).getRsidRDefault();
        ctp.setRsidP(rsidr);
        ctp.setRsidRDefault(rsidrDefault);
        CTPPr ppr = ctp.addNewPPr();
        ppr.addNewPStyle().setVal("Header");
        // 开始加水印
        CTR ctr = ctp.addNewR();
        CTRPr ctrpr = ctr.addNewRPr();
        ctrpr.addNewNoProof();
        CTGroup group = CTGroup.Factory.newInstance();
        CTShapetype shapeType = group.addNewShapetype();
        CTTextPath shapeTypeTextPath = shapeType.addNewTextpath();
        shapeTypeTextPath.setOn(STTrueFalse.T);
        shapeTypeTextPath.setFitshape(STTrueFalse.T);
        CTLock lock = shapeType.addNewLock();
        lock.setExt(STExt.VIEW);
        CTShape shape = group.addNewShape();
        shape.setId("PowerPlusWaterMarkObject");
        shape.setSpid("_x0000_s102");
        shape.setType("#_x0000_t136");
        // 设置形状样式（旋转，位置，相对路径等参数）
        shape.setStyle(getShapeStyle(fingerText));
        shape.setFillcolor(FONT_COLOR);
        // 字体设置为实心
        shape.setStroked(STTrueFalse.FALSE);
        // 绘制文本的路径
        CTTextPath shapeTextPath = shape.addNewTextpath();
        // 设置文本字体与大小
        shapeTextPath.setStyle("font-family:" + FONT_NAME + ";font-size:" + FONT_SIZE);
        shapeTextPath.setString(fingerText);
        CTPicture pict = ctr.addNewPict();
        pict.set(group);
    }

    /**
     * 构建Shape的样式参数
     *
     * @param fingerText 水印
     */
    private static String getShapeStyle(String fingerText) {
        StringBuilder sb = new StringBuilder();
        // 文本path绘制的定位方式
        sb.append("position: ").append("absolute");
        // 计算文本占用的长度（文本总个数*单字长度）
        sb.append(";width: ").append(fingerText.length() * WIDTH_PER_WORD).append("pt");
        // 字体高度
        sb.append(";height: ").append("20pt");
        sb.append(";z-index: ").append("-251654144");
        sb.append(";mso-wrap-edited: ").append("f");
        // 设置水印的间隔，这是一个大坑，不能用top,必须要margin-top。
        sb.append(";margin-top: ").append(STYLE_TOP);
        sb.append(";mso-position-horizontal-relative: ").append("page");
        sb.append(";mso-position-vertical-relative: ").append("page");
        sb.append(";mso-position-vertical: ").append("left");
        sb.append(";mso-position-horizontal: ").append("center");
        sb.append(";rotation: ").append(STYLE_ROTATION);
        return sb.toString();
    }

    /**
     * 将指定的字符串重复repeats次.
     */
    private static String repeatString(String pattern, int repeats) {
        StringBuilder buffer = new StringBuilder(pattern.length() * repeats);
        Stream.generate(() -> pattern).limit(repeats).forEach(buffer::append);
        return new String(buffer);
    }

    public static void main(String[] args) {
        /**
         * 你路径下没文件你就自己添加了~
         */
        final String inPath = "300_page_document.doc";
        //final String outPath = "300_page_document--水印-中移.docx";
        final String outPath = "300_page_document----水印--哒哒哒.docx";

        // 添加水印
        //waterMarkDocXDocument(inPath, outPath, "中国移动物");
        docToDocxConverter(inPath, outPath, "中国移动");
    }

}

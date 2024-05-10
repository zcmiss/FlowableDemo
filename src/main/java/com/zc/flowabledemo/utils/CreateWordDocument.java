package com.zc.flowabledemo.utils;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class CreateWordDocument {
    public static void main(String[] args) {
        try {
            // 创建一个新的Word文档
            XWPFDocument document = new XWPFDocument();

            // 设置页面大小和边距
            CTDocument1 ctDocument = document.getDocument();
            CTBody body = ctDocument.getBody();
            CTSectPr sectPr = body.addNewSectPr();
            CTPageSz pageSize = sectPr.addNewPgSz();
            // 宽度，单位：twips（1/1440英寸）
            pageSize.setW(BigInteger.valueOf(12240));
            // 高度，单位：twips（1/1440英寸）
            pageSize.setH(BigInteger.valueOf(15840));
            CTPageMar pageMar = sectPr.addNewPgMar();
            // 顶部边距，单位：twips（1/1440英寸）
            pageMar.setTop(BigInteger.valueOf(1440));
            // 底部边距，单位：twips（1/1440英寸）
            pageMar.setBottom(BigInteger.valueOf(1440));
             // 左侧边距，单位：twips（1/1440英寸）
            pageMar.setLeft(BigInteger.valueOf(1440));
             // 右侧边距，单位：twips（1/1440英寸）
            pageMar.setRight(BigInteger.valueOf(1440));

            // 添加内容，每页有一个标题和一些文本
            for (int i = 0; i < 300; i++) {
                XWPFParagraph title = document.createParagraph();
                title.setStyle("Heading1");
                XWPFRun titleRun = title.createRun();
                titleRun.setText("第 " + (i + 1) + " 页标题");

                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText("这是第 " + (i + 1) + " 页的内容。");

                // 添加分页符
                if (i < 299) {
                    XWPFParagraph pageBreak = document.createParagraph();
                    pageBreak.setPageBreak(true);
                }
            }

            // 保存文档到文件
            FileOutputStream out = new FileOutputStream("300_page_document.docx");
            document.write(out);
            out.close();

            System.out.println("Word文档创建成功！");
        } catch (IOException e) {
            System.out.println("创建Word文档时出现错误：" + e.getMessage());
        }
    }
}

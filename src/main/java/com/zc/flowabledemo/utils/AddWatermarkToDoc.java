package com.zc.flowabledemo.utils;

import com.aspose.words.*;
import com.aspose.words.Shape;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;


/**
 * @author zengc
 * @date 2024/05/10
 */
@Slf4j
public class AddWatermarkToDoc {
    public static void main(String[] args) throws Exception {
        //docDemo2();

        doxDemo1();
    }

    private static void doxDemo1() throws Exception {
        // 加载文档
        Document doc = new Document("300_page_document.doc");

        // 获取页面设置以便获取页面尺寸
        PageSetup pageSetup = doc.getFirstSection().getPageSetup();

        // 创建一个水印形状
        Shape watermark = new Shape(doc, ShapeType.TEXT_PLAIN_TEXT);

        // 设置水印属性
        watermark.getTextPath().setText("水印水印水印哟");
        watermark.getTextPath().setFontFamily("Arial");
        watermark.setWidth(300);
        watermark.setHeight(200);
        // 设置水印的旋转角度
        watermark.setRotation(-40);
        // 设置填充颜色
        watermark.getFill().setBackColor(Color.GRAY);
        watermark.setStrokeColor(Color.GRAY);
        // 确保水印不影响文本流
        watermark.setWrapType(WrapType.NONE);

        // 添加水印到每一页
        for (Section section : doc.getSections()) {
            for (Paragraph para : section.getBody().getParagraphs()) {
                Shape watermarkClone = (Shape) watermark.deepClone(true);
                para.appendChild(watermarkClone);

                // 动态设置水印位置，例如放在页面中心
                float horizontalPosition = (float) ((pageSetup.getPageWidth() - watermarkClone.getWidth()) / 2);
                float verticalPosition = (float) ((pageSetup.getPageHeight() - watermarkClone.getHeight()) / 2);

                watermarkClone.setRelativeHorizontalPosition(RelativeHorizontalPosition.PAGE);
                watermarkClone.setRelativeVerticalPosition(RelativeVerticalPosition.PAGE);
                watermarkClone.setLeft(horizontalPosition);
                watermarkClone.setTop(verticalPosition);
            }
        }

        // 保存修改后的文档
        doc.save("output.doc");
    }


    private static void docDemo2() throws Exception {
        // 加载文档
        Document doc = new Document("300_page_document.doc");

        doc.getWatermark().setText("Watermark");

        // 保存修改后的文档
        doc.save("output.doc");
    }

}

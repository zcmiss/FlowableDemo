package com.zc.flowabledemo.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * xls 水印内容
 *
 * @author zengc
 * @date 2024/05/12
 */
public class FontImageUtil {
    /**
     * 水印内容类
     *
     * @author zengc
     * @date 2024/05/12
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Watermark {
        private Boolean enable;
        private String text;
        private String color;
    }

    /**
     * 生成水印图片
     *
     * @param watermark
     * @return
     */
    public static BufferedImage createWatermarkImage(Watermark watermark) {
        if (watermark == null) {
            watermark = new Watermark();
            watermark.setEnable(true);
            watermark.setText("内部资料");
            watermark.setColor("#C5CBCF");
        } else {
            if (StringUtils.isEmpty(watermark.getText())) {
                watermark.setText("内部资料");
            }
            if (StringUtils.isEmpty(watermark.getColor())) {
                watermark.setColor("#C5CBCF");
            }
        }
        String[] textArray = watermark.getText().split("\n");
        Font font = new Font("宋体", Font.PLAIN, 20);
        Integer width = 400;
        Integer height = 200;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 背景透明 开始
        Graphics2D g = image.createGraphics();
        //		image = g.getDeviceConfiguration().createCompatibleImage(width, height);
        //		g.dispose();
        // 背景透明 结束
        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);
        // 设定画笔颜色
        g.setColor(new Color(Integer.parseInt(watermark.getColor().substring(1), 16)));
        // 设置画笔字体
        g.setFont(font);
        // 设定倾斜度
        g.shear(0.1, -0.26);
        //设置字体平滑
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int y = 50;
        for (String s : textArray) {
            // 画出字符串
            g.drawString(s, 0, y);
            y = y + font.getSize();
        }
        // 释放画笔
        g.dispose();
        return image;

    }


}

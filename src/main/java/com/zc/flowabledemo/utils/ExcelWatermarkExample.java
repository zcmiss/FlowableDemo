package com.zc.flowabledemo.utils;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ExcelWatermarkExample {
    public static void main(String[] args) {
        try {
            // 生成水印图片
            String watermarkText = "Your Watermark Text";
            String watermarkImagePath = "watermark.png";
            generateWatermark(watermarkText, watermarkImagePath);

            // 创建工作簿
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("Sheet1");

            // 将水印图片设置成水印
            setWatermark(workbook, sheet, watermarkImagePath);

            // 保存文件
            FileOutputStream out = new FileOutputStream(new File("output.xls"));
            workbook.write(out);
            out.close();
            workbook.close();

            System.out.println("Excel 文件创建成功！");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generateWatermark(String watermarkText, String outputPath) {
        try {
            // 创建一个BufferedImage对象，用于生成水印图片
            int width = 400; // 图片宽度
            int height = 200; // 图片高度
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();

            // 设置水印图片的样式（文字水印）
            g2d.setFont(new Font("Arial", Font.BOLD, 30));
            g2d.setColor(Color.BLACK);
            g2d.drawString(watermarkText, 50, 100);
            g2d.dispose();

            // 保存水印图片到文件
            File output = new File(outputPath);
            ImageIO.write(image, "png", output);
            System.out.println("水印图片生成成功：" + outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setWatermark(HSSFWorkbook workbook, HSSFSheet sheet, String watermarkImagePath) {
        try {
            // 加载水印图片
            InputStream inputStream = new FileInputStream(watermarkImagePath);
            byte[] bytes = inputStreamToBytes(inputStream);
            int pictureIndex = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);

            // 创建水印
            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
            HSSFClientAnchor anchor = new HSSFClientAnchor();
            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);

            // 设置水印图片的位置和大小
            anchor.setCol1(0); // 设置水印图片左上角的列坐标
            anchor.setRow1(0); // 设置水印图片左上角的行坐标
            anchor.setCol2(10); // 设置水印图片右下角的列坐标
            anchor.setRow2(10); // 设置水印图片右下角的行坐标

            HSSFPicture picture = patriarch.createPicture(anchor, pictureIndex);
            picture.resize(); // 调整图片大小
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] inputStreamToBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        byteArrayOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }
}

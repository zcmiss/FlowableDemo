package com.zc.flowabledemo.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.hssf.record.RecordBase;
import org.apache.poi.hssf.record.StandardRecord;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author zengc
 * @date 2024/05/12
 */
@Slf4j
public class ExileAddWatermark {

    /**
     * xls 类型添加水印
     *
     * @param inputFilePath
     * @param text
     */
    public static void xlsAddWatermark(String inputFilePath, String text) {
        FileInputStream is = null;
        FileOutputStream out = null;
        HSSFWorkbook workbook = null;
        ByteArrayOutputStream os = null;
        //水印图片
        String tarImgPath = "";
        try {
            //生成水印图片并导出字节流
            BufferedImage image = FontImageUtil.createWatermarkImage(new FontImageUtil.Watermark(true, text, null));
            File f1 = new File(inputFilePath);
            //tarImgPath = f1.getParent() + File.separator + System.nanoTime() + text + ".png";
            tarImgPath = text + ".png";
            FileOutputStream outImgStream = new FileOutputStream(tarImgPath);
            ImageIO.write(image, "png", outImgStream);

            //获取excel工作簿
            is = new FileInputStream(inputFilePath);
            workbook = new HSSFWorkbook(is);

            //获取每个Sheet表并插入水印
            // 获取图像文件的字节数据
            //PNG 不得具有透明度，否则会导致水印不可见
            byte[] data = getBackgroundBitmapData(tarImgPath);
            for (int k = 0; k < workbook.getNumberOfSheets(); k++) {
                HSSFSheet sheet1 = workbook.getSheetAt(k);

                Field _sheet = HSSFSheet.class.getDeclaredField("_sheet");
                _sheet.setAccessible(true);
                InternalSheet internalsheet = (InternalSheet) _sheet.get(sheet1);
                // get List of RecordBase
                Field _records = InternalSheet.class.getDeclaredField("_records");
                _records.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<RecordBase> records = (List<RecordBase>) _records.get(internalsheet);


                xlsAddWatermarkRecords(data, records);

//				// debug output
//				for (RecordBase r : internalsheet.getRecords()) {
//					System.out.println(r);
//				}
            }
            //生成添加水印的excel文件
            File f = new File(inputFilePath);
            //String outputFilePath = f.getParent() + File.separator + System.currentTimeMillis() + f.getName();
            String outputFilePath = f.getName();
            out = new FileOutputStream(outputFilePath);
            log.info(outputFilePath);
            outImgStream.close();
            boolean res = new File(tarImgPath).delete();
            workbook.write(out);
        } catch (Exception e) {
            log.error("excel文件添加水印异常", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    log.error("excel输入文件关闭异常", e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    log.error("excel输出文件关闭异常", e);
                }
            }
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    log.error("excel工作簿关闭异常", e);
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                    log.error("水印图片字节流关闭异常", e);
                }
            }
            boolean delete = new File(tarImgPath).delete();
        }
    }

    private static void xlsAddWatermarkRecords(byte[] data, List<RecordBase> records) {
        // 从 8220 字节的部分数据创建 BitmapRecord 和 ContinuousRecords
        BitmapRecord bitmapRecord;
        List<ContinueRecord> continueRecords = new ArrayList<>();
        int bytes;

        if (data.length > 8220) {
            bitmapRecord = new BitmapRecord(Arrays.copyOfRange(data, 0, 8220));
            bytes = 8220;
            while (bytes < data.length) {
                if ((bytes + 8220) < data.length) {
                    continueRecords.add(new ContinueRecord(Arrays.copyOfRange(data, bytes, bytes + 8220)));
                    bytes += 8220;
                } else {
                    continueRecords.add(new ContinueRecord(Arrays.copyOfRange(data, bytes, data.length)));
                    break;
                }
            }
        } else {
            bitmapRecord = new BitmapRecord(data);
        }
        // 添加PageSettingsBlock后面的记录
        int i = 0;
        for (RecordBase r : records) {
            if (r instanceof org.apache.poi.hssf.record.aggregates.PageSettingsBlock) {
                break;
            }
            i++;
        }
        records.add(++i, bitmapRecord);
        for (ContinueRecord continueRecord : continueRecords) {
            records.add(++i, continueRecord);
        }
    }


    /**
     * xlsx 类型添加水印
     *
     * @param inputFilePath 输入源文件
     * @param text          水印文本
     */
    public static void xlsxAddWatermark(String inputFilePath, String text) {
        FileInputStream is = null;
        FileOutputStream out = null;
        XSSFWorkbook workbook = null;
        ByteArrayOutputStream os = null;
        try {
            //生成水印图片并导出字节流
            BufferedImage image = FontImageUtil.createWatermarkImage(new FontImageUtil.Watermark(true, text, null));
            os = new ByteArrayOutputStream();
            ImageIO.write(image, "png", os);
            //获取excel工作簿
            is = new FileInputStream(inputFilePath);
            workbook = new XSSFWorkbook(is);
            int pictureIdx = workbook.addPicture(os.toByteArray(), Workbook.PICTURE_TYPE_PNG);
            POIXMLDocumentPart poixmlDocumentPart = workbook.getAllPictures().get(pictureIdx);
            //获取每个Sheet表并插入水印
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                XSSFSheet sheet1 = workbook.getSheetAt(i);
                PackagePartName ppn = poixmlDocumentPart.getPackagePart().getPartName();
                String relType = XSSFRelation.IMAGES.getRelation();
                //添加工作表与图片数据的关系
                PackageRelationship pr = sheet1.getPackagePart().addRelationship(ppn, TargetMode.INTERNAL, relType, null);
                //将背景图片设置为工作表
                sheet1.getCTWorksheet().addNewPicture().setId(pr.getId());
            }
            //生成添加水印的excel文件
            File f = new File(inputFilePath);
            //String outputFilePath = f.getParent() + File.separator + System.currentTimeMillis() + f.getName();
            String outputFilePath = f.getName();
            out = new FileOutputStream(outputFilePath);
            workbook.write(out);
            log.info(outputFilePath);
        } catch (Exception e) {
            log.error("excel文件添加水印异常", e);
        } finally {
            if (Objects.nonNull(is)) {
                try {
                    is.close();
                } catch (Exception e) {
                    log.error("excel输入文件关闭异常", e);
                }
            }
            if (Objects.nonNull(out)) {
                try {
                    out.close();
                } catch (Exception e) {
                    log.error("excel输出文件关闭异常", e);
                }
            }
            if (Objects.nonNull(workbook)) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    log.error("excel工作簿关闭异常", e);
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                    log.error("水印图片字节流关闭异常", e);
                }
            }
        }
    }


    /**
     * 获取背景图片数据
     *
     * @param filePath 图片路径
     * @return {@link byte[] } 图片字节数据
     * @throws Exception 异常
     */
    static byte[] getBackgroundBitmapData(String filePath) throws Exception {

        // 读取图片
        FileInputStream fio = new FileInputStream(filePath);
        BufferedImage in = ImageIO.read(fio);
        BufferedImage image = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = image.createGraphics();
        graphics.drawImage(in, null, 0, 0);
        graphics.dispose();

        // 计算行大小
        int rowSize = ((24 * image.getWidth() + 31) / 32) * 4;

        ByteArrayOutputStream output = new ByteArrayOutputStream(image.getHeight() * rowSize * 3 + 1024);

        // 将记录头放入数据中
        ByteBuffer header = ByteBuffer.allocate(8 + 12);
        header.order(ByteOrder.LITTLE_ENDIAN);

        // 未记录的 XLS 内容
        header.putShort((short) 0x09);
        header.putShort((short) 0x01);
        // 图像流大小
        header.putInt(image.getHeight() * rowSize + 12);

        // 位图核心头 (a)
        header.putInt(12);

        header.putShort((short) image.getWidth());
        // 如果自上而下书写，请使用 -height
        header.putShort((short) image.getHeight());

        //始终为 1
        header.putShort((short) 1);
        // 位数
        header.putShort((short) 24);

        output.write(header.array());

        // 自下而上输出行 (b)
        Raster raster = image.getRaster()
                .createChild(0, 0, image.getWidth(), image.getHeight(), 0, 0, new int[]{2, 1, 0});
        // 反向 BGR -> RGB (d)
        // 填充 (c)
        byte[] row = new byte[rowSize];

        for (int i = image.getHeight() - 1; i >= 0; i--) {
            row = (byte[]) raster.getDataElements(0, i, image.getWidth(), 1, row);
            output.write(row);
        }

        // 创建一个字节数组来存储整个图像的像素数据
        byte[] pixels = ((DataBufferByte) raster.getDataBuffer()).getData();

        // 将整个像素数据写入到输出流中
        output.write(pixels);
        fio.close();

        return output.toByteArray();
    }


    /**
     * 位图记录
     *
     * @author zengc
     * @date 2024/05/12
     */
    private static class BitmapRecord extends StandardRecord {
        // 数据
        byte[] data;

        // 构造方法
        BitmapRecord(byte[] data) {
            this.data = data;
        }

        // 获取数据大小
        @Override
        public int getDataSize() {
            return data.length;
        }

        // 获取记录标识
        @Override
        public short getSid() {
            return (short) 0x00E9;
        }

        // 序列化
        @Override
        public void serialize(LittleEndianOutput out) {
            out.write(data);
        }
    }

    /**
     * 继续记录
     *
     * @author zengc
     * @date 2024/05/12
     */
    static class ContinueRecord extends StandardRecord {
        // 数据
        byte[] data;

        // 构造方法
        ContinueRecord(byte[] data) {
            this.data = data;
        }

        // 获取数据大小
        @Override
        public int getDataSize() {
            return data.length;
        }

        // 获取记录标识
        @Override
        public short getSid() {
            return (short) 0x003C;
        }

        // 序列化
        @Override
        public void serialize(LittleEndianOutput out) {
            out.write(data);
        }
    }

    public static void main(String[] args) {
        xlsAddWatermark("intput.xls", "水印水印水印");
        //xlsxAddWatermark("intput.xlsx", "水印水印水印");
    }
}


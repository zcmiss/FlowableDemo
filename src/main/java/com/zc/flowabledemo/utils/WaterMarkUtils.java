package com.zc.flowabledemo.utils;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import com.spire.doc.PictureWatermark;
import com.spire.doc.TextWatermark;
import com.spire.presentation.*;
import com.spire.presentation.drawing.FillFormatType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;


/**
 * @author zengc
 */
public class WaterMarkUtils {

    /**
     * @param args
     * @throws IOException
     * @throws DocumentException
     */
    public static void main(String[] args) throws Exception {
        //String filePath = "E:\\桌面\\水印\\";
        String filePath = "";

        //1、给图片添加文字水印
        //WaterMarkUtils.setWaterMarkToImage("这是水印内容", filePath + "1.jpg", false);
//        WaterMarkUtils.img2pdf(filePath + "1.jpg" + "," + filePath + "1_水印.jpg", filePath + "1.pdf", false);
        //2、PDF添加动态水印
//        WaterMarkUtils.setWaterMarkToPdf("这是水印内容", filePath + "帆软报表.pdf", false);
        //3、PPT添加动态水印
//        WaterMarkUtils.setWaterMarkToPpt("这是水印内容", filePath + "ppt.ppt", true);
        //4、给docx、doc、wps、wpt、rtf加水印
        //WaterMarkUtils.setWaterMarkToWps("这是水印内容", filePath + "20240322.docx", true);
//        WaterMarkUtils.setWaterMarkToWps("这是水印内容", filePath + "300_page_document.docx", true);
//        WaterMarkUtils.setWaterMarkToWps2(filePath + "3.png", filePath + "docx.docx", true);
//        WaterMarkUtils.doc2pdf(filePath + "docx_水印.docx",filePath + "docx_水印222.pdf", false);
        WaterMarkUtils.setWaterMarkToWps("这是水印内容", filePath + "docx.wps", true);
//        WaterMarkUtils.setWaterMarkToWps("这是水印内容", filePath + "doc.doc", false);
//        WaterMarkUtils.setWaterMarkToWps("这是水印内容", filePath + "wpt.wpt", false);
//        WaterMarkUtils.setWaterMarkToWps("这是水印内容", filePath + "rtf.rtf", false);
        //5、给excel加水印
//        WaterMarkUtils.setWaterMarkToExcel("这是水印内容", filePath + "excel.xlsx", true);
//        WaterMarkUtils.setWaterMarkToExcel("这是水印内容", filePath + "excel.xls");
//        WaterMarkUtils.setWaterMarkToExcel("这是水印内容", filePath + "excel.et");
//        WaterMarkUtils.setWaterMarkToExcel("这是水印内容", filePath + "excel.csv");
//        WaterMarkUtils.excel2pdf("E:\\桌面\\水印\\excel2_水印.et", "E:\\桌面\\水印\\excel2_水印.pdf");

    }

    /**
     * 图片添加文字水印
     *
     * @param 水印内容
     * @param 需要加水印的附件地址
     */
    public static void setWaterMarkToImage(String 水印内容, String 需要加水印的附件地址, boolean 是否转PDF) {
        String 水印输出文件地址 = getOutputPath(需要加水印的附件地址);
        InputStream is = null;
        OutputStream os = null;
        try {
            // 1、源图片
            Image srcImg = ImageIO.read(new File(需要加水印的附件地址));
            BufferedImage buffImg = new BufferedImage(srcImg.getWidth(null), srcImg.getHeight(null), BufferedImage.TYPE_INT_RGB);
            // 2、得到画笔对象
            Graphics2D g = buffImg.createGraphics();
            // 3、设置对线段的锯齿状边缘处理
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(srcImg.getScaledInstance(srcImg.getWidth(null), srcImg.getHeight(null), Image.SCALE_SMOOTH), 0, 0, null);
            // 4、设置水印旋转
            g.rotate(Math.toRadians(45), buffImg.getWidth() / 2, buffImg.getHeight() / 2);
            // 5、设置水印文字颜色
            g.setColor(Color.blue);
            // 6、设置水印文字Font
            g.setFont(new Font("宋体", Font.BOLD, buffImg.getHeight() / 5));
            // 7、设置水印文字透明度
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.3f));
            // 8、第一参数->设置的内容，后面两个参数->文字在图片上的坐标位置(x,y)
            g.drawString(水印内容, buffImg.getWidth() / 3, buffImg.getHeight() / 4);
            // 9、释放资源
            g.dispose();
            // 10、图片后缀
            String suffix = 需要加水印的附件地址.substring(需要加水印的附件地址.lastIndexOf(".") + 1);
            // 11、生成图片
            os = new FileOutputStream(水印输出文件地址);
            ImageIO.write(buffImg, suffix, os);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != is) {
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (null != os) {
                    os.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        if (是否转PDF) {
//            img2pdf(水印输出文件地址, getPdfPath(需要加水印的附件地址), false);
//        }
    }

    /**
     * PDF添加动态文字水印
     *
     * @param 水印内容
     * @param 需要加水印的附件地址
     */
    public static void setWaterMarkToPdf(String 水印内容, String 需要加水印的附件地址, boolean 是否删除源文件) throws DocumentException, IOException {
        // 1、要输出的pdf文件
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(getOutputPath(需要加水印的附件地址))));

        PdfReader reader = new PdfReader(需要加水印的附件地址);
        PdfStamper stamper = new PdfStamper(reader, bos);
        // 2、获取总页数 +1, 下面从1开始遍历
        int total = reader.getNumberOfPages() + 1;
        // 3、使用classpath下面的字体库
        BaseFont base = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);

        JLabel label = new JLabel();
        label.setText(水印内容);
        FontMetrics metrics = label.getFontMetrics(label.getFont());
        // 4、获取水印文字的高度和宽度
        int textH = metrics.getHeight();
        int textW = metrics.stringWidth(label.getText());
        // 5、设置水印透明度
        PdfGState gs = new PdfGState();
        gs.setFillOpacity(0.3f);
        gs.setStrokeOpacity(0.3f);

        Rectangle pageSizeWithRotation;
        PdfContentByte content;
        for (int i = 1; i < total; i++) {
            // 6、在内容上方加水印
            content = stamper.getOverContent(i);
            // 7、在内容下方加水印
            // content = stamper.getUnderContent(i);
            content.saveState();
            content.setGState(gs);
            // 8、设置字体和字体大小
            content.beginText();
            content.setFontAndSize(base, 20);
            // 9、获取每一页的高度、宽度
            pageSizeWithRotation = reader.getPageSizeWithRotation(i);
            float pageHeight = pageSizeWithRotation.getHeight();
            float pageWidth = pageSizeWithRotation.getWidth();
            // 10、间隔
            int interval = -15;
            int position = 0;
            for (int height = interval + textH; height < pageHeight; height = height + textH * 5) {
                for (int width = interval + textW - position * 150; width < pageWidth + textW; width = width + textW) {
                    // 11、添加水印文字，水印文字成25度角倾斜
                    content.showTextAligned(Element.ALIGN_LEFT, 水印内容, width - textW, height - textH / 2, 45);
                }
                position++;
            }
            content.endText();
        }
        // 12、关流
        stamper.close();
        reader.close();
        if (是否删除源文件) {
            del2file(需要加水印的附件地址);
        }

    }

    /**
     * PPT添加动态文字水印
     *
     * @param 水印内容
     * @param 需要加水印的附件地址
     */
    public static void setWaterMarkToPpt(String 水印内容, String 需要加水印的附件地址, boolean 是否转PDF) throws Exception {
        // 1、加载PPT源文档
        Presentation ppt = new Presentation();
        ppt.loadFromFile(需要加水印的附件地址);
        String PPT水印文件地址 = getOutputPath(需要加水印的附件地址);
        // 2、遍历ppt每一页
        for (int p = 0; p < ppt.getSlides().size(); p++) {
            // 3、设置文本水印文本宽和高
            int width = 300;
            int height = 100;
            // 4、起始坐标
            float x = 10;
            float y = 40;
            ISlide slide = ppt.getSlides().get(p);
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    // 5、绘制文本，设置文本格式并将其添加到幻灯片
                    Rectangle2D.Double rect = new Rectangle2D.Double(x, y, width, height);
                    IAutoShape shape = slide.getShapes().appendShape(com.spire.presentation.ShapeType.RECTANGLE, rect);
                    shape.getFill().setFillType(FillFormatType.NONE);
                    shape.getShapeStyle().getLineColor().setColor(Color.white);
                    shape.setRotation(-45);
                    shape.getLocking().setSelectionProtection(true);
                    shape.getLine().setFillType(FillFormatType.NONE);
                    shape.getTextFrame().setText(水印内容);
                    shape.setShapeArrange(ShapeAlignmentEnum.ShapeArrange.SendToBack);
                    PortionEx textRange = shape.getTextFrame().getTextRange();
                    textRange.getFill().setFillType(FillFormatType.SOLID);
                    textRange.getFill().getSolidColor().setColor(new Color(238, 130, 238));
                    textRange.setFontHeight(20);
                    x += (100 + ppt.getSlideSize().getSize().getWidth() / 6);
                }
                x = 30;
                y += (100 + ppt.getSlideSize().getSize().getHeight() / 7);
            }
        }

        // 6、保存文档
        ppt.saveToFile(PPT水印文件地址, FileFormat.PPTX_2016);
        ppt.dispose();
//        if (是否转PDF) {
//            //将加水印的PPT转PDF
//            ppt2pdf(PPT水印文件地址, getPdfPath(需要加水印的附件地址), true);
//        }

    }

    /**
     * docx、doc、wps、wpt、rtf添加文字水印
     *
     * @param 水印内容
     * @throws Exception
     */
    public static void setWaterMarkToWps(String 水印内容, String 需要加水印的附件地址, boolean 是否转PDF) throws Exception {
        String 水印输出文件地址 = getOutputPath(需要加水印的附件地址);
        com.spire.doc.Document doc = new com.spire.doc.Document();
        doc.loadFromFile(需要加水印的附件地址);
        //设置水印内容
        TextWatermark txtWatermark = new TextWatermark();
        txtWatermark.setText(水印内容);
        txtWatermark.setFontSize(75);
        txtWatermark.setColor(Color.red);
        txtWatermark.setLayout(com.spire.doc.documents.WatermarkLayout.Diagonal);
        doc.getSections().get(0).getDocument().setWatermark(txtWatermark);
        doc.saveToFile(水印输出文件地址, com.spire.doc.FileFormat.Docx);
//        if (是否转PDF) {
//            // 16、转PDF
//            doc2pdf(水印输出文件地址, getPdfPath(需要加水印的附件地址), false);
//        }
    }


    public static void setWaterMarkToWps3(String 水印内容, String 需要加水印的附件地址, boolean 是否转PDF) throws Exception {
        try {
            // 读取现有的Word文档
            FileInputStream fis = new FileInputStream(需要加水印的附件地址);
            XWPFDocument document = new XWPFDocument(fis);
            fis.close();

            // 添加水印
            addWatermark(document, 水印内容);

            // 保存修改后的文档
            FileOutputStream out = new FileOutputStream("document_with_watermark.docx");
            document.write(out);
            out.close();

            System.out.println("Word文档添加水印成功！");
        } catch (IOException e) {
            System.out.println("添加水印时出现错误：" + e.getMessage());
        }
    }

    private static void addWatermark(XWPFDocument document, String watermarkText) {
            // 创建一个新的段落
        XWPFParagraph paragraph = document.createParagraph();

        // 创建一个水印文本
        XWPFRun run = paragraph.createRun();
        run.setText(watermarkText);
        run.setColor("C0C0C0"); // 设置文本颜色
        run.setFontSize(60); // 设置字体大小
        run.setFontFamily("Arial"); // 设置字体
        run.setBold(true); // 设置加粗

        // 设置段落对齐方式为居中
        paragraph.setAlignment(ParagraphAlignment.CENTER);
    }

    /**
     * docx、doc、wps、wpt、rtf添加图片水印
     *
     * @param 水印内容
     * @throws Exception
     */
    public static void setWaterMarkToWps2(String 水印内容, String 需要加水印的附件地址, boolean 是否转PDF) throws Exception {
        String 水印输出文件地址 = getOutputPath(需要加水印的附件地址);
        com.spire.doc.Document doc = new com.spire.doc.Document();
        doc.loadFromFile(需要加水印的附件地址);
        //设置水印内容
        PictureWatermark picture = new PictureWatermark();
        picture.setPicture(水印内容);
        picture.setScaling(70);
        picture.isWashout(false);
        doc.setWatermark(picture);
        doc.saveToFile(水印输出文件地址, com.spire.doc.FileFormat.Docx);
        if (是否转PDF) {
            // 16、转PDF
            doc2pdf(水印输出文件地址, getPdfPath(需要加水印的附件地址), false);
        }
    }

    /**
     * xlsx、xls、et、csv添加水印
     *
     * @param 水印内容
     * @param 需要加水印的附件地址
     * @throws Exception
     */
   /* public static void setWaterMarkToExcel(String 水印内容, String 需要加水印的附件地址, boolean 是否转PDF) throws Exception {
        String 水印文件输出的地址 = getOutputPath(需要加水印的附件地址);
        XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(需要加水印的附件地址));
        // 1、添加水印
        ByteArrayOutputStream byteArrayOutputStream = createWaterMark(水印内容);
        int pictureIdx = wb.addPicture(byteArrayOutputStream.toByteArray(), Workbook.PICTURE_TYPE_PNG);
        // 2、遍历sheet 给每个sheet 添加水印
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            XSSFSheet sheet = wb.getSheetAt(i);
            String rID = sheet.addRelation(null, XSSFRelation.IMAGES, wb
                    .getAllPictures().get(pictureIdx)).getRelationship().getId();
            sheet.getCTWorksheet().addNewPicture().setId(rID);
        }
        // 3、输出添加水印后的文件
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wb.write(bos);
        wb.close();

        byte[] content = bos.toByteArray();
        OutputStream out = null;
        out = new FileOutputStream(水印文件输出的地址);
        out.write(content);
        bos.close();
        out.close();

//        if (是否转PDF) {
//            // 4、转PDF
//            excel2pdf(水印文件输出的地址, getPdfPath(需要加水印的附件地址), true);
//        }
    }*/

    /**
     * 给sheet 加水印
     *
     * @param content 水印文字
     */
    private static ByteArrayOutputStream createWaterMark(String content) throws IOException {
        int width = 200;
        int height = 150;
        // 1、获取bufferedImage对象
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Font font = new Font("微软雅黑", Font.BOLD, 20);
        // 2、获取Graphics2d对象
        Graphics2D g2d = image.createGraphics();
        image = g2d.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        g2d.dispose();
        g2d = image.createGraphics();
        // 3、设置字体颜色和透明度，最后一个参数为透明度
        g2d.setColor(new Color(0, 0, 0, 20));
        // 4、设置字体
        g2d.setStroke(new BasicStroke(1));
        // 5、设置字体类型 加粗 大小
        g2d.setFont(font);
        // 6、设置倾斜度
        g2d.rotate(-0.5, (double) image.getWidth() / 2, (double) image.getHeight() / 2);
        FontRenderContext context = g2d.getFontRenderContext();
        Rectangle2D bounds = font.getStringBounds(content, context);
        double x = (width - bounds.getWidth()) / 2;
        double y = (height - bounds.getHeight()) / 2;
        double ascent = -bounds.getY();
        double baseY = y + ascent;
        // 7、写入水印文字原定高度过小，所以累计写水印，增加高度
        g2d.drawString(content, (int) x, (int) baseY);
        // 8、设置透明度
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        // 9、释放对象
        g2d.dispose();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // 10、输出
        ImageIO.write(image, "png", os);
        return os;
    }

    /**
     * 获取 添加水印后的文件路径
     *
     * @param 需要加水印的附件地址
     * @return
     */
    private static String getOutputPath(String 需要加水印的附件地址) {
        // 1、生成水印后的PDF路径
        String path = 需要加水印的附件地址.substring(0, 需要加水印的附件地址.lastIndexOf(".")) + "_水印.";
        // 2、文件后缀
        String suffix = 需要加水印的附件地址.substring(需要加水印的附件地址.lastIndexOf(".") + 1);
        // 3、返回添加水印后的文件路径
        return path + suffix;
    }

    /**
     * 获取PDF地址
     *
     * @param 需要加水印的附件地址
     * @return
     */
    private static String getPdfPath(String 需要加水印的附件地址) {
        return 需要加水印的附件地址.substring(0, 需要加水印的附件地址.lastIndexOf(".") + 1) + "pdf";
    }

    /**
     * img转pdf
     *
     * @param img文件路径
     * @param pdf的文件路径
     */

//    public static void img2pdf(String img文件路径, String pdf的文件路径, boolean 是否删除源文件) {
//        // 验证License 若不验证则转化出的pdf文档会有水印产生
//        if (!getLicense(2)) {
//            return;
//        }
//        //新建Pdf 文档
//        PdfDocument pdf = new PdfDocument();
//        String[] imgUrls = img文件路径.split(",");
//        for (int i = 0; i < imgUrls.length; i++) {
//            //添加一页
//            PdfPageBase page = pdf.getPages().add();
//            //加载图片
//            PdfImage image = PdfImage.fromFile(imgUrls[i]);
//            double widthFitRate = image.getPhysicalDimension().getWidth() / page.getCanvas().getClientSize().getWidth();
//            double heightFitRate = image.getPhysicalDimension().getHeight() / page.getCanvas().getClientSize().getHeight();
//            double fitRate = Math.max(widthFitRate, heightFitRate);
//            //图片大小
//            double fitWidth = image.getPhysicalDimension().getWidth() / fitRate;
//            double fitHeight = image.getPhysicalDimension().getHeight() / fitRate;
//            //绘制图片到PDF
//            page.getCanvas().drawImage(image, 0, 30, fitWidth, fitHeight);
//        }
//        //保存文档
//        pdf.saveToFile(pdf的文件路径);
//        pdf.dispose();
//
//        if (是否删除源文件) {
//            del2file(img文件路径);
//        }
//    }

    /**
     * Ppt转pdf
     *
     * @param ppt的文件路径
     * @param pdf的文件路径
     * @return
     */
    /*public static void ppt2pdf(String ppt的文件路径, String pdf的文件路径, boolean 是否删除源文件) throws Exception {
        // 验证License
        if (!getLicense(1)) {
            return;
        }
        // 输出pdf路径
        File file = new File(pdf的文件路径);
        // 输入ppt路径
        com.aspose.slides.Presentation pres = new com.aspose.slides.Presentation(ppt的文件路径);
        FileOutputStream fileOS = new FileOutputStream(file);
        pres.save(fileOS, com.aspose.slides.SaveFormat.Pdf);
        fileOS.close();
        //删除源文件
        if (是否删除源文件) {
            del2file(ppt的文件路径);
        }
    }*/

    /**
     * Word转pdf
     *
     * @param word的文件路径
     * @param pdf的文件路径
     * @return
     */
    public static void doc2pdf(String word的文件路径, String pdf的文件路径, boolean 是否删除源文件) {
        FileOutputStream os = null;
        try {
            // 新建一个空白pdf文档
            File file = new File(pdf的文件路径);
            os = new FileOutputStream(file);
            // Address是将要被转化的word文档
            com.spire.doc.Document doc = new com.spire.doc.Document(word的文件路径);
            // 保存pdf文件
            doc.saveToFile(os, com.spire.doc.FileFormat.PDF);
            // 删除源文件
            if (是否删除源文件) {
                del2file(word的文件路径);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Excel转pdf
     *
     * @param excel文件路径
     * @param pdf的文件路径
     */

//    public static void excel2pdf(String excel文件路径, String pdf的文件路径, boolean 是否删除源文件) throws Exception {
//        // 验证License 若不验证则转化出的pdf文档会有水印产生
//        if (!getLicense(2)) {
//            return;
//        }
//        //读取excel
//        com.aspose.cells.Workbook wb = new com.aspose.cells.Workbook(excel文件路径);
//        com.aspose.cells.PdfSaveOptions pdfSaveOptions = new com.aspose.cells.PdfSaveOptions();
//        pdfSaveOptions.setOnePagePerSheet(true);
//        // 遍历获取sheet数量
//        for (int i = 0; i < wb.getWorksheets().getCount(); i++) {
//            wb.getWorksheets().get(i).getHorizontalPageBreaks().clear();
//            wb.getWorksheets().get(i).getVerticalPageBreaks().clear();
//        }
//        // 导出PDF文件
//        wb.save(pdf的文件路径, pdfSaveOptions);
//        //加水印调用方法
//        //setWaterMarkToPdf("这是水印内容", pdf的文件路径, true);
//    }

    /**
     * 删除源文件
     *
     * @param 需要删除的附件地址
     */
    public static void del2file(String 需要删除的附件地址) {
        String[] imgUrls = 需要删除的附件地址.split(",");
        for (int i = 0; i < imgUrls.length; i++) {
            File file2 = new File(imgUrls[i]);
            file2.delete();
        }
    }

    /**
     * 获取license
     *
     * @return
     */
//    private static InputStream license;
//
//    private static boolean getLicense(int type) {
//        boolean result = false;
//        try {
//            // license路径
//            license = WaterMarkUtils.class.getClassLoader().getResourceAsStream("License.xml");
//            if (type == 1) {
//                com.aspose.slides.License aposeLic = new com.aspose.slides.License();
//                aposeLic.setLicense(license);
//            } else {
//                com.aspose.cells.License aposeLic = new com.aspose.cells.License();
//                aposeLic.setLicense(license);
//            }
//            result = true;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
//    }

}
 
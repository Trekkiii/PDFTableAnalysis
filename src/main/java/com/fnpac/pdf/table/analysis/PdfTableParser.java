package com.fnpac.pdf.table.analysis;

import com.fnpac.pdf.table.analysis.opencv.OpenCVExtractor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static com.fnpac.pdf.table.analysis.utils.Utils.bufferedImage2GrayscaleMat;

/**
 * Created by 刘春龙 on 2017/10/24.
 */
public class PdfTableParser {

    private static final Logger logger = Logger.getLogger(PdfTableParser.class.getName());

//    private OpenCVExtractor extractor;
    private Settings settings;

    static {
        // 加载图像处理库
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public PdfTableParser(Settings settings) {
        this.settings = settings;
//        this.extractor = new OpenCVExtractor(settings);
    }

    public PdfTableParser() {
        this(new Settings());// 使用默认配置
    }

    /**
     * 使用设置中指定的DPI渲染指定页码范围内的PDF页面，并将图像保存在指定的目录中
     * @param document PDDocument
     * @param startPage 起始页，第一页页码为1
     * @param endPage 结束页
     * @param outputDir 输出目录
     * @throws IOException
     */
    public void savePdfPagesAsPNG(PDDocument document, int startPage, int endPage, Path outputDir) throws IOException {
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        for (int page = startPage - 1; page < endPage; ++page) {// pdfbox 默认第一页页码为0
            savePdfPageAsPNG(pdfRenderer, page, outputDir);
        }
    }

    /**
     * 使用设置中指定的DPI渲染单个PDF页面，并将图像保存在指定的目录中
     * @param document PDDocument
     * @param page 页码
     * @param outputDir 输出目录
     * @throws IOException
     */
    public void savePdfPageAsPNG(PDDocument document, int page, Path outputDir) throws IOException {
        savePdfPagesAsPNG(document, page, page, outputDir);
    }

    /**
     * 使用设置中指定的DPI渲染单个PDF页面，并将图像保存在指定的目录中
     * @param renderer PDFRenderer
     * @param page 页码
     * @param outputDir 输出目录
     * @throws IOException
     */
    private void savePdfPageAsPNG(PDFRenderer renderer, int page, Path outputDir) throws IOException {
        BufferedImage bim;
        synchronized (this) {
            bim = renderer.renderImageWithDPI(page, settings.getPdfRenderingDpi(), ImageType.RGB);
        }

        Path outPath = outputDir.resolve(Paths.get("page_" + (page + 1) + ".png"));
        // out: D:\tmp\pdf_tests\page_1.png
        logger.info("out: " + outPath.toString());
        ImageIOUtil.writeImage(bim, outPath.toString(), settings.getPdfRenderingDpi());
    }

    /**
     * 将指定页码范围内的PDF页面的debug图像保存在指定的目录中。
     * @param document PDDocument
     * @param startPage 起始页，第一页页码为1
     * @param endPage 结束页
     * @param outputDir 输出目录
     * @throws IOException
     */
    public void savePdfPagesDebugImages(PDDocument document, int startPage, int endPage, Path outputDir) throws IOException {
        OpenCVExtractor debugExtractor = new OpenCVExtractor(settings);

        PDFRenderer renderer = new PDFRenderer(document);
        for (int page = startPage - 1; page < endPage; ++page) {// pdfbox 默认第一页页码为0
            Settings debugSettings = Settings.builder()
                    .setDebugImages(true)
                    .setDebugFileOutputDir(outputDir)
                    .setDebugFilename("page_" + (page + 1))
                    .build();
            debugExtractor.setSettings(debugSettings);

            BufferedImage bim;
            synchronized (this) {
                bim = renderer.renderImageWithDPI(page, debugSettings.getPdfRenderingDpi(), ImageType.RGB);
            }

            Mat mat = bufferedImage2GrayscaleMat(bim);// 需要先将图像转为灰度图
            debugExtractor.getTableBoundingRectangles(mat);
        }
    }

    /**
     * 将指定页码的PDF页面的debug图像保存在指定的目录中。
     * @param document PDDocument
     * @param page 页码
     * @param outputDir 输出目录
     * @throws IOException
     */
    public void savePdfPagesDebugImages(PDDocument document, int page, Path outputDir) throws IOException {
        savePdfPagesDebugImages(document, page, page, outputDir);
    }


}

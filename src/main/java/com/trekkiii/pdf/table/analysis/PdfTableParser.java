package com.trekkiii.pdf.table.analysis;

import com.trekkiii.pdf.table.analysis.models.ParsedTablePage;
import com.trekkiii.pdf.table.analysis.opencv.OpenCVExtractor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.trekkiii.pdf.table.analysis.utils.Utils.bufferedImage2GrayscaleMat;

/**
 * Created by 刘春龙 on 2017/10/24.
 */
public class PdfTableParser {

    private static final Logger logger = Logger.getLogger(PdfTableParser.class.getName());

    private OpenCVExtractor extractor;
    private Settings settings;

    static {
        // 加载图像处理库
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public PdfTableParser(Settings settings) {
        this.settings = settings;
        this.extractor = new OpenCVExtractor(settings);
    }

    public PdfTableParser() {
        this(new Settings());// 使用默认配置
    }

    /**
     * 使用设置中指定的DPI渲染指定页码范围内的PDF页面，并将图像保存在指定的目录中
     *
     * @param document  PDDocument
     * @param startPage 起始页，第一页页码为1
     * @param endPage   结束页
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
     *
     * @param document  PDDocument
     * @param page      页码
     * @param outputDir 输出目录
     * @throws IOException
     */
    public void savePdfPageAsPNG(PDDocument document, int page, Path outputDir) throws IOException {
        savePdfPagesAsPNG(document, page, page, outputDir);
    }

    /**
     * 使用设置中指定的DPI渲染单个PDF页面，并将图像保存在指定的目录中
     *
     * @param renderer  PDFRenderer
     * @param page      页码
     * @param outputDir 输出目录
     * @throws IOException
     */
    private void savePdfPageAsPNG(PDFRenderer renderer, int page, Path outputDir) throws IOException {
        BufferedImage bim;
        synchronized (this) {
            bim = renderer.renderImageWithDPI(page, settings.getPdfRenderingDpi(), ImageType.RGB);
        }

        Path outPath = outputDir.resolve(Paths.get("page_" + (page + 1) + ".png"));
        logger.info("out: " + outPath.toString());
        ImageIOUtil.writeImage(bim, outPath.toString(), settings.getPdfRenderingDpi());
    }

    /**
     * 将指定页码范围内的PDF页面的debug图像保存在指定的目录中。
     *
     * @param document  PDDocument
     * @param startPage 起始页，第一页页码为1
     * @param endPage   结束页
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
     *
     * @param document  PDDocument
     * @param page      页码
     * @param outputDir 输出目录
     * @throws IOException
     */
    public void savePdfPageDebugImage(PDDocument document, int page, Path outputDir) throws IOException {
        savePdfPagesDebugImages(document, page, page, outputDir);
    }

    /**
     * 解析指定页码范围内的PDF页面，并返回包含单元格文本的解析结果
     * @param document PDDocument
     * @param startPage 起始页，第一页页码为1
     * @param endPage 结束页
     * @return 包含单元格文本的解析结果
     * @throws IOException
     */
    public List<ParsedTablePage> parsePdfPages(PDDocument document, int startPage, int endPage) throws IOException {
        List<ParsedTablePage> out = new ArrayList<>();

        PDFRenderer renderer = new PDFRenderer(document);
        for (int page = startPage - 1; page < endPage; ++page) {
            BufferedImage bim;
            synchronized (this) {
                bim = renderer.renderImageWithDPI(page, settings.getPdfRenderingDpi(), ImageType.RGB);
            }
            ParsedTablePage parsedTablePage = parsePdfPage(bim, document.getPage(page), page + 1);
            out.add(parsedTablePage);
        }
        return out;
    }

    /**
     * 解析指定页码的单个PDF页面，并返回包含单元格文本的解析结果
     *
     * @param bim        图像格式的PDF页面
     * @param pdPage     PDPage格式的PDF页面
     * @param pageNumber 页码
     * @return 包含单元格文本的解析结果
     * @throws IOException
     */
    private ParsedTablePage parsePdfPage(BufferedImage bim, PDPage pdPage, int pageNumber) throws IOException {
        List<Rect> rectangles = extractor.getTableBoundingRectangles(bufferedImage2GrayscaleMat(bim));
        return parsePageByRectangles(pdPage, rectangles, pageNumber);
    }

    /**
     * 使用从{@link OpenCVExtractor}获取的{@link Rect}，逐个单元格解析PDF页面
     * @param page PDF页面
     * @param rectangles {@link OpenCVExtractor}识别的OpenCV {@link Rect}列表
     * @param pageNumber 页码
     * @return 解析的结果
     * @throws IOException
     */
    private ParsedTablePage parsePageByRectangles(PDPage page, List<Rect> rectangles, int pageNumber) throws IOException {
        List<List<Rect>> sortedRects = groupRectanglesByRow(rectangles);// 按照表格行分组

        ParsedTablePage out = new ParsedTablePage(pageNumber);

        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);

        int iRow = 0;
        int iCol = 0;
        for (List<Rect> row : sortedRects) {
            for (Rect col : row) {
                // 使用设置中指定的DPI（pdfRenderingDpi）渲染单个PDF页面，生成图像。
                // 这里需要根据比例进行相应的还原
                Rectangle r = new Rectangle(
                        (int) (col.x * settings.getDpiRatio()),
                        (int) (col.y * settings.getDpiRatio()),
                        (int) (col.width * settings.getDpiRatio()),
                        (int) (col.height * settings.getDpiRatio())
                );
                stripper.addRegion(getRegionId(iRow, iCol), r);
                iCol++;
            }
            iRow++;
            iCol = 0;
        }

        stripper.extractRegions(page);

        iRow = 0;
        iCol = 0;

        for (List<Rect> row : sortedRects) {
            List<String> rowCells = new ArrayList<>();
            for (Rect col : row) {
                String cellText = stripper.getTextForRegion(getRegionId(iRow, iCol));
                logger.info("text: " + cellText);
                rowCells.add(cellText);
                iCol++;
            }
            out.addRow(rowCells);
            iRow++;
            iCol = 0;
        }
        return out;
    }

    /**
     * 按y坐标对{@link Rect}进行分组，将它们按照表格行分组
     * @param rectangles {@link OpenCVExtractor}识别的OpenCV {@link Rect}列表
     * @return list of Rectangle lists representing table rows.
     */
    private List<List<Rect>> groupRectanglesByRow(List<Rect> rectangles) {
        List<List<Rect>> out = new ArrayList<>();

        List<Integer> rowsCoords = rectangles.stream().map(r -> r.y).distinct().collect(Collectors.toList());
        for (int rowCoords : rowsCoords) {
            List<Rect> cols = rectangles.stream().filter(r -> r.y == rowCoords).collect(Collectors.toList());
            out.add(cols);
        }
        return out;
    }

    private static String getRegionId(int row, int col) {
        return String.format("r%dc%d", row, col);
    }
}

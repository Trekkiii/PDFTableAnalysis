package com.fnpac.pdf.table.analysis.opencv;

import com.fnpac.pdf.table.analysis.Settings;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.*;

/**
 * Created by 刘春龙 on 2017/10/24.
 * 负责确定表格单元格界限框的类应该用作静态的
 */
public class OpenCVExtractor {

    private Settings settings;

    static {
        // 加载图像处理库
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public OpenCVExtractor(Settings settings) {
        this.settings = settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    /**
     * 在页面图像上应用一系列过滤器，并提取表格单元格的边界矩形(Rectangle)。
     * <p>
     * 另外在{@code settings.hasDebugImages()}为true时转储debug PNG图像
     *
     * @param inImage 灰度图像
     * @return 表示单元格边界矩形的org.opencv.core.Rect对象的列表
     */
    public List<Rect> getTableBoundingRectangles(Mat inImage) {
        List<Rect> out = new ArrayList<>();

        if (settings.hasDebugImages()) {// 输出灰度图像
            Imgcodecs.imwrite(buildDebugFilename("original_grayscaled"), inImage);
        }

        // 图像二值化
        Mat bit = binaryInvertedThreshold(inImage);
        if (settings.hasDebugImages()) {// 输出二值化图像
            Imgcodecs.imwrite(buildDebugFilename("binary_inverted_threshold"), bit);
        }

        // 查找轮廓
        List<MatOfPoint> contours = new ArrayList<>();
        if (settings.hasCannyFiltering()) {

            // 边缘检测
            Mat canny = cannyFilter(inImage);
            if (settings.hasDebugImages()) {// 输出边缘检测图像
                Imgcodecs.imwrite(buildDebugFilename("canny1"), canny);
            }

//            findContours(canny, contours, new Mat(), RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        } else {
//            findContours(bit, contours, new Mat(), RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        }

        // TODO

        return out;
    }

    /**
     * 将Binary Inverted Threshold (BIT) 应用于Mat图像。图像二值化，将灰度图转换为黑白图
     *
     * @param input Input image
     * @return 应用Binary Inverted Threshold (BIT) 的org.opencv.core.Mat image
     */
    private Mat binaryInvertedThreshold(Mat input) {
        Mat out = new Mat();
        threshold(input, out, settings.getBitThreshold(), settings.getBitMaxVal(), THRESH_BINARY_INV);
        return out;
    }

    /**
     * 将Canny边缘检测应用于Mat图像
     *
     * @param input Input image
     * @return 应用Canny边缘检测的org.opencv.core.Mat image
     */
    private Mat cannyFilter(Mat input) {
        Mat out = new Mat();
        Canny(input, out,
                settings.getCannyThreshold1(), settings.getCannyThreshold2(), settings.getCannyApertureSize(), settings.hasCannyL2Gradient());
        return out;
    }

    /**
     * 构建debug image输出路径
     *
     * @param suffix Image filename 前缀
     * @return debug image输出路径
     */
    private String buildDebugFilename(String suffix) {
        return settings.getDebugFileOutputDir().resolve(settings.getDebugFilename() + "_" + suffix + ".png").toString();
    }
}

package com.fnpac.pdf.table.analysis.opencv;

import com.fnpac.pdf.table.analysis.Settings;
import org.opencv.core.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opencv.core.Core.bitwise_xor;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
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
     * @param grayscaleMat 灰度图像
     * @return 表示单元格边界矩形的org.opencv.core.Rect对象的列表
     */
    public List<Rect> getTableBoundingRectangles(Mat grayscaleMat) {
        List<Rect> out = new ArrayList<>();

        if (settings.hasDebugImages()) {// 输出灰度图像
            imwrite(buildDebugFilename("original_grayscaled"), grayscaleMat);
        }

        /**
         * 1. 图像二值化
         */
        Mat bit = binaryInvertedThreshold(grayscaleMat);
        if (settings.hasDebugImages()) {// 输出二值化图像
            imwrite(buildDebugFilename("binary_inverted_threshold"), bit);
        }

        /**
         * 2. 查找轮廓
         */
        List<MatOfPoint> contours = new ArrayList<>();
        if (settings.hasCannyFiltering()) {

            // 2.1 基于边缘检测图像
            Mat canny = cannyFilter(grayscaleMat);
            if (settings.hasDebugImages()) {// 输出边缘检测图像
                imwrite(buildDebugFilename("canny1"), canny);
            }


            // Mat image：输入图像，必须为一个8位的二值图像
            // List<MatOfPoint> contours：用于存储轮廓的容器
            // Mat hierarchy：
            //      hiararchy参数和轮廓个数相同，每个轮廓contours[ i ]对应4个hierarchy元素hierarchy[ i ][ 0 ] ~hierarchy[ i ][ 3 ]，分别表示后一个轮廓、前一个轮廓、父轮廓、内嵌轮廓的索引编号，如果没有对应项，该值设置为负数。
            // int mode：轮廓检测的模式
            //      RETR_EXTERNAL：只提取最外层的轮廓，查找外边缘，各边缘以指针h_next相连；
            // int method：轮廓边缘的近似方法
            //      CHAIN_APPROX_SIMPLE：压缩水平方向，垂直方向，对角线方向的元素，只保留该方向的终点坐标，例如一个矩形轮廓只需4个点来保存轮廓信息；
            findContours(canny, contours, new Mat(), RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        } else {
            // 2.2 基于二值化图像
            findContours(bit, contours, new Mat(), RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        }

        /**
         * 3. 用`CV_FILLED`(填充轮廓内部)绘制轮廓，得到表格的蒙版(mask)
         */
        // Mat image：输入图像，函数将在这张图像上绘制轮廓
        // List<MatOfPoint> contours： 轮廓链表
        // int contourIdx：绘制轮廓的最大层数
        // Scalar color：颜色
        // int thickness：轮廓线的宽度，如果为`CV_FILLED`则会填充轮廓内部
        Mat contourMask = bit.clone();
        drawContours(contourMask, contours, -1, new Scalar(255, 255, 255), Core.FILLED);
        if (settings.hasDebugImages()) {
            imwrite(buildDebugFilename("contour_mask"), contourMask);
        }

        /**
         * 4. 二值图像和表格的mask异或
         */
        Mat xored = new Mat();
        bitwise_xor(bit, contourMask, xored);
        if (settings.hasDebugImages()) {
            imwrite(buildDebugFilename("xored"), xored);
        }

        /**
         * 5. 对步骤4异或的结果再次进行边缘检测 #2
         */
        List<MatOfPoint> contours2 = new ArrayList<>();
        if (settings.hasCannyFiltering()) {
            Mat canny2 = cannyFilter(xored);
            findContours(canny2, contours2, new Mat(), RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
            if (settings.hasDebugImages()) {
                imwrite(buildDebugFilename("canny2"), canny2);
            }
        } else {
            findContours(xored, contours2, new Mat(), RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        }
        /**
         * 6. 再次用`CV_FILLED`(填充轮廓内部)绘制轮廓 #2
         */
        if (settings.hasDebugImages()) {
            Mat contourMask2 = grayscaleMat.clone();
            drawContours(contourMask2, contours2, -1, new Scalar(255, 255, 255), Core.FILLED);
            imwrite(buildDebugFilename("final_contours"), contourMask2);
        }

        /**
         * 7. 计算轮廓的边界矩形或旋转矩形
         */
        for (int i = 0; i < contours2.size(); i++) {
            MatOfPoint2f approxCurve = new MatOfPoint2f();

            MatOfPoint2f contour2f = new MatOfPoint2f(contours2.get(i).toArray());
            double approxDistance = arcLength(contour2f, true) * settings.getApproxDistScaleFactor();
            approxPolyDP(contour2f, approxCurve, approxDistance, true);

            MatOfPoint points = new MatOfPoint(approxCurve.toArray());
            Rect rect = boundingRect(points);
            out.add(rect);
        }

        Collections.reverse(out);

        if (settings.hasDebugImages()) {
            int index = 0;
            for (Rect rect : out) {
                Mat outImage = grayscaleMat.clone();

                Point p1 = new Point(rect.x, rect.y);
                Point p2 = new Point(rect.x + rect.width, rect.y + rect.height);
                rectangle(outImage, p1, p2, new Scalar(0, 0, 0, 255), 3);
                imwrite(buildDebugFilename(String.format("box_%03d", index)), outImage);
                index++;
            }
        }

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

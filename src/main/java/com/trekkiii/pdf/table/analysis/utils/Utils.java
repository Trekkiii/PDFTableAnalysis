package com.trekkiii.pdf.table.analysis.utils;

import org.apache.pdfbox.io.IOUtils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by 刘春龙 on 2017/10/24.
 */
public class Utils {

    private Utils() {
    }

    /**
     * 将BufferedImage转换为灰度OpenCV Mat
     *
     * @param inImg Buffered Image
     * @return org.opencv.core.Mat
     * @throws IOException
     */
    public static Mat bufferedImage2GrayscaleMat(BufferedImage inImg) throws IOException {
        return bufferedImage2Mat(inImg, Imgcodecs.IMREAD_GRAYSCALE);
    }

    /**
     * 使用自定义标志将BufferedImage转换为OpenCV Mat
     *
     * @param inImg Buffered Image
     * @param flag  org.opencv.imgcodecs.Imgcodecs flag
     * @return org.opencv.core.Mat
     * @throws IOException
     */
    public static Mat bufferedImage2Mat(BufferedImage inImg, int flag) throws IOException {
        return inputStream2Mat(bufferedImage2InputStream(inImg), flag);
    }

    /**
     * 将BufferedImage转换为InputStream
     *
     * @param inImg Buffered Image
     * @return java.io.InputStream
     * @throws IOException
     */
    public static InputStream bufferedImage2InputStream(BufferedImage inImg) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(inImg, "png", os);
        return new ByteArrayInputStream(os.toByteArray());
    }

    /**
     * 将InputStream转换为OpenCV Mat
     *
     * @param stream java.io.InputStream
     * @param flag   org.opencv.imgcodecs.Imgcodecs flag
     * @return org.opencv.core.Mat
     * @throws IOException
     */
    public static Mat inputStream2Mat(InputStream stream, int flag) throws IOException {
        byte[] byteBuff = IOUtils.toByteArray(stream);
        return Imgcodecs.imdecode(new MatOfByte(byteBuff), flag);
    }
}

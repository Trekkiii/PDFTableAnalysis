package com.fnpac.pdf.table.analysis;

import java.nio.file.Path;

/**
 * Created by 刘春龙 on 2017/10/24.
 */
public class Settings {

    public static class SettingsBuilder {

        // --------------
        // DEFAULT VALUES
        // --------------
        // DPI SETTINGS
        // DPI 设置
        private static final int DEFAULT_PDF_DPI = 72;// Java默认dpi 72
        private int pdfRenderingDpi = 120;

        // CANNY EDGE DETECTION FLAG
        // Canny边缘检测标志（Canny：边缘检测算法）
        private boolean cannyFiltering = false;

        // BINARY INVERTED THRESHOLD SETTINGS
        //
        private double bitThreshold = 150;
        private double bitMaxVal = 255;

        // CANNY FILTER SETTINGS
        // Canny边缘检测参数
        private double cannyThreshold1 = 50;
        private double cannyThreshold2 = 200;
        private int cannyApertureSize = 3;
        private boolean cannyL2Gradient = false;

        // BOUNDING RECT PARAMS
        // 边界矩形参数
        private double approxDistScaleFactor = 0.02;

        // DEBUG IMAGES PARAMS
        // debug image 相关参数，设置输出解析过程中生成的image
        private boolean debugImages = false;
        private Path debugFileOutputDir;
        private String debugFilename;

        public SettingsBuilder setPdfRenderingDpi(int pdfRenderingDpi) {
            this.pdfRenderingDpi = pdfRenderingDpi;
            return this;
        }

        public SettingsBuilder setCannyFiltering(boolean cannyFiltering) {
            this.cannyFiltering = cannyFiltering;
            return this;
        }

        public SettingsBuilder setBitThreshold(double bitThreshold) {
            this.bitThreshold = bitThreshold;
            return this;
        }

        public SettingsBuilder setBitMaxVal(double bitMaxVal) {
            this.bitMaxVal = bitMaxVal;
            return this;
        }

        public SettingsBuilder setCannyThreshold1(double cannyThreshold1) {
            this.cannyThreshold1 = cannyThreshold1;
            return this;
        }

        public SettingsBuilder setCannyThreshold2(double cannyThreshold2) {
            this.cannyThreshold2 = cannyThreshold2;
            return this;
        }

        public SettingsBuilder setCannyApertureSize(int cannyApertureSize) {
            this.cannyApertureSize = cannyApertureSize;
            return this;
        }

        public SettingsBuilder setCannyL2Gradient(boolean cannyL2Gradient) {
            this.cannyL2Gradient = cannyL2Gradient;
            return this;
        }

        public SettingsBuilder setApproxDistScaleFactor(double approxDistScaleFactor) {
            this.approxDistScaleFactor = approxDistScaleFactor;
            return this;
        }

        public SettingsBuilder setDebugImages(boolean debugImages) {
            this.debugImages = debugImages;
            return this;
        }

        public SettingsBuilder setDebugFileOutputDir(Path debugFileOutputDir) {
            this.debugFileOutputDir = debugFileOutputDir;
            return this;
        }

        public SettingsBuilder setDebugFilename(String debugFilename) {
            this.debugFilename = debugFilename;
            return this;
        }

        /**
         * 创建Settings对象
         *
         * @return
         */
        public Settings build() {
            return new Settings(this);
        }
    }

    // DPI SETTINGS
    private int defaultPdfDpi;
    private int pdfRenderingDpi;

    // CANNY EDGE DETECTION FLAG
    private boolean cannyFiltering;

    // BINARY INVERTED THRESHOLD SETTINGS
    private double bitThreshold;
    private double bitMaxVal;

    // CANNY FILTER SETTINGS
    private double cannyThreshold1;
    private double cannyThreshold2;
    private int cannyApertureSize;
    private boolean cannyL2Gradient;

    // BOUNDING RECT PARAMS
    private double approxDistScaleFactor;

    // DEBUG IMAGES PARAMS
    private boolean debugImages;
    private Path debugFileOutputDir;
    private String debugFilename;

    /**
     * 构造方法
     *
     * @param builder
     */
    private Settings(SettingsBuilder builder) {
        this.defaultPdfDpi = SettingsBuilder.DEFAULT_PDF_DPI;
        this.pdfRenderingDpi = builder.pdfRenderingDpi;
        this.cannyFiltering = builder.cannyFiltering;
        this.bitThreshold = builder.bitThreshold;
        this.bitMaxVal = builder.bitMaxVal;
        this.cannyThreshold1 = builder.cannyThreshold1;
        this.cannyThreshold2 = builder.cannyThreshold2;
        this.cannyApertureSize = builder.cannyApertureSize;
        this.cannyL2Gradient = builder.cannyL2Gradient;
        this.approxDistScaleFactor = builder.approxDistScaleFactor;
        this.debugImages = builder.debugImages;
        this.debugFileOutputDir = builder.debugFileOutputDir;
        this.debugFilename = builder.debugFilename;
    }

    /**
     * 方式一
     */
    public Settings() {
        this(new SettingsBuilder());
    }

    /**
     * 方式二
     *
     * @return
     */
    public static SettingsBuilder builder() {
        return new SettingsBuilder();
    }

    public int getDefaultPdfDpi() {
        return defaultPdfDpi;
    }

    public int getPdfRenderingDpi() {
        return pdfRenderingDpi;
    }

    public boolean hasCannyFiltering() {
        return cannyFiltering;
    }

    public double getBitThreshold() {
        return bitThreshold;
    }

    public double getBitMaxVal() {
        return bitMaxVal;
    }

    public double getCannyThreshold1() {
        return cannyThreshold1;
    }

    public double getCannyThreshold2() {
        return cannyThreshold2;
    }

    public int getCannyApertureSize() {
        return cannyApertureSize;
    }

    public boolean hasCannyL2Gradient() {
        return cannyL2Gradient;
    }

    public double getApproxDistScaleFactor() {
        return approxDistScaleFactor;
    }

    public boolean hasDebugImages() {
        return debugImages;
    }

    public Path getDebugFileOutputDir() {
        return debugFileOutputDir;
    }

    public String getDebugFilename() {
        return debugFilename;
    }

    public double getDpiRatio() {
        return (double) defaultPdfDpi / pdfRenderingDpi;
    }
}

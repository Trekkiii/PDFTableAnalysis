package com.fnpac.pdf.table.analysis;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.testng.TestException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by 刘春龙 on 2017/10/24.
 */
public class PdfTableParserTest {

    private static final String TEST_FILENAME = "test_tables.pdf";
    // D:\tmp\pdf_tests
    private static final Path TEST_OUT_PATH = Paths.get("D:", "tmp", "pdf_tests");
    private static PDDocument PDFdoc;

    @BeforeMethod
    private void setUp() {
        try {
            ClassLoader classLoader = this.getClass().getClassLoader();
            File file = new File(classLoader.getResource(TEST_FILENAME).getFile());
            PDFdoc = PDDocument.load(file);
        } catch (IOException e) {
            e.printStackTrace();
            throw new TestException(e.getCause());
        }
    }

    @AfterMethod
    private void tearDown() {
        if (PDFdoc != null) {
            try {
                PDFdoc.close();
            } catch (IOException ioe) {
                throw new TestException(ioe);
            }
        }
    }

    @Test
    public void savePdfPagesAsPNG() throws IOException {
        PdfTableParser parser = new PdfTableParser();
        parser.savePdfPagesAsPNG(PDFdoc, 1, 3, TEST_OUT_PATH);
    }

    @Test
    public void savePdfDebugImages() throws IOException {
        PdfTableParser parser = new PdfTableParser();
        parser.savePdfPagesDebugImages(PDFdoc, 1, 3, TEST_OUT_PATH);
    }
}

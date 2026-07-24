package io.github.a5b84.cliligrane.pdfgenerator.service.templates;

import io.github.a5b84.cliligrane.common.utils.MediaTypes;
import io.github.a5b84.cliligrane.pdfgenerator.configuration.FeatureFlipping;
import io.github.a5b84.cliligrane.pdfgenerator.model.FileInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

import javax.imageio.ImageIO;

// Used for manual testing
@Disabled
public class BOPdfDocumentTemplateTest {

    private static final String WATERMARK = "  DOCUMENTS EXCLUSIVEMENT DESTINÉS À LA LOCATION IMMOBILIÈRE     ";
    private static final FeatureFlipping featureFlipping = new FeatureFlipping(true, true);
    private static final BOPdfDocumentTemplate boPdfDocumentTemplate = new BOPdfDocumentTemplate(featureFlipping, null);

    File outputfile;

    @AfterEach
    void tearDown() {
        if (outputfile != null) {
            outputfile.delete();
            outputfile = null;
        }
    }

    @DisplayName("Check if the pdf file is correctly generated in specific files")
    @Test
    public void check_render_with_special_files() throws Exception {
        InputStream is = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("secret/TestBOFile1.pdf");

        FileInputStream data = FileInputStream
                .builder()
                .mediaType(MediaTypes.APPLICATION_PDF)
                .inputStream(is)
                .build();

        File resultFile = new File("target/resultSpecial.pdf");
        resultFile.createNewFile();

        byte[] bytes = IOUtils.toByteArray(boPdfDocumentTemplate.render(Collections.singletonList(data), WATERMARK));

        FileOutputStream w = new FileOutputStream(resultFile);
        w.write(bytes);
    }


    @DisplayName("Check if the render is correctly generated from text pdf and wrong sized pdf")
    @Test
    public void check_render_with_text_pdf() throws Exception {
        InputStream is = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("bigH.pdf");

        FileInputStream data = FileInputStream
                .builder()
                .mediaType(MediaTypes.APPLICATION_PDF)
                .inputStream(is)
                .build();

        File resultFile = new File("target/resultTestPdfWrongSize.pdf");
        resultFile.createNewFile();

        byte[] bytes = IOUtils.toByteArray(boPdfDocumentTemplate.render(Collections.singletonList(data), WATERMARK));

        FileOutputStream w = new FileOutputStream(resultFile);
        w.write(bytes);
    }

    @DisplayName("Check if the render is correctly generated from all type textual pdf, pdf, image, non obfuscable pdf")
    @Test
    public void check_render_with_all_type() throws Exception {
        InputStream is = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("CNI.pdf");
        InputStream isJPG = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("CNI.jpg");
        InputStream isTextPdf = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("page1.pdf");
        InputStream isOpen = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("CNI.pdf");

        FileInputStream data = FileInputStream
                .builder()
                .mediaType(MediaTypes.APPLICATION_PDF)
                .inputStream(is)
                .build();
        FileInputStream data2 = FileInputStream
                .builder()
                .mediaType(MediaTypes.IMAGE_JPEG)
                .inputStream(isJPG)
                .build();
        FileInputStream data3 = FileInputStream
                .builder()
                .mediaType(MediaTypes.APPLICATION_PDF)
                .inputStream(isTextPdf)
                .build();
        FileInputStream data4 = FileInputStream
                .builder()
                .mediaType(MediaTypes.APPLICATION_PDF)
                .inputStream(isOpen)
                .build();

        File resultFile = new File("target/resultFullTypeTestPdf.pdf");
        resultFile.createNewFile();

        byte[] bytes = IOUtils.toByteArray(boPdfDocumentTemplate.render(Arrays.asList(data, data2, data3, data4), WATERMARK));

        FileOutputStream w = new FileOutputStream(resultFile);
        w.write(bytes);
    }

    @DisplayName("Check if the render is correctly generated from image pdf")
    @Test
    public void check_render_with_img_pdf() throws Exception {
        InputStream is = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("CNI.pdf");

        FileInputStream data = FileInputStream
                .builder()
                .mediaType(MediaTypes.APPLICATION_PDF)
                .inputStream(is)
                .build();

        File resultFile = new File("target/resultTestPdfWithJpeg.pdf");
        resultFile.createNewFile();

        byte[] bytes = IOUtils.toByteArray(boPdfDocumentTemplate.render(Collections.singletonList(data), WATERMARK));

        FileOutputStream w = new FileOutputStream(resultFile);
        w.write(bytes);
    }

    @DisplayName("Check if the render is correctly generated from jpegs")
    @Test
    public void check_render_from_jpegs() throws Exception {
        InputStream is = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("CNI.jpg");
        InputStream is2 = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("CNIHorizontale.jpg");
        InputStream is3 = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("CNISmall.jpg");

        FileInputStream data = FileInputStream
                .builder()
                .mediaType(MediaTypes.IMAGE_JPEG)
                .inputStream(is)
                .build();
        FileInputStream data2 = FileInputStream
                .builder()
                .mediaType(MediaTypes.IMAGE_JPEG)
                .inputStream(is2)
                .build();
        FileInputStream data3 = FileInputStream
                .builder()
                .mediaType(MediaTypes.IMAGE_JPEG)
                .inputStream(is3)
                .build();
        File resultFile = new File("target/resultTestJpeg.pdf");
        resultFile.createNewFile();

        byte[] bytes = IOUtils.toByteArray(boPdfDocumentTemplate.render(Arrays.asList(data, data2, data3), WATERMARK));

        FileOutputStream w = new FileOutputStream(resultFile);
        w.write(bytes);
    }

    @DisplayName("Render watermark (used mostly for developing)")
    @Test
    public void check_watermark_rendered() throws IOException {
        InputStream is = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("CNI.jpg");
        BufferedImage image = ImageIO.read(is);
        BufferedImage b = boPdfDocumentTemplate.applyWatermark(image, "watermark 2023");
        outputfile = new File("image.jpg");
        ImageIO.write(b, "jpg", outputfile);
    }

    @DisplayName("Avoid render above qrcode")
    @Test
    public void check_watermark_not_in_qrcode() throws Exception {
        InputStream is = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("qrcode-sample.pdf");

        FileInputStream data = FileInputStream
                .builder()
                .mediaType(MediaTypes.APPLICATION_PDF)
                .inputStream(is)
                .build();

        File resultFile = new File("target/resultTestPdfWithQrCode.pdf");
        resultFile.createNewFile();

        byte[] bytes = IOUtils.toByteArray(boPdfDocumentTemplate.render(Collections.singletonList(data), WATERMARK));

        FileOutputStream w = new FileOutputStream(resultFile);
        w.write(bytes);
    }
}

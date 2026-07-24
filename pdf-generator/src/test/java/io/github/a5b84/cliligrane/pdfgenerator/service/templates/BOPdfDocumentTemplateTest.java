package io.github.a5b84.cliligrane.pdfgenerator.service.templates;

import io.github.a5b84.cliligrane.common.utils.MediaTypes;
import io.github.a5b84.cliligrane.pdfgenerator.configuration.FeatureFlipping;
import io.github.a5b84.cliligrane.pdfgenerator.model.FileInputStream;
import io.github.a5b84.cliligrane.pdfgenerator.service.PdfSignatureServiceImpl;
import io.github.a5b84.cliligrane.pdfgenerator.service.interfaces.PdfSignatureService;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

// Used for manual testing
@Disabled
public class BOPdfDocumentTemplateTest {

    private static final String WATERMARK = "  DOCUMENTS EXCLUSIVEMENT DESTINÉS À LA LOCATION IMMOBILIÈRE     ";
    private static final FeatureFlipping featureFlipping = new FeatureFlipping(true, true);
    private static final PdfSignatureService pdfSignatureService = new PdfSignatureServiceImpl(false, null, null);
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final BOPdfDocumentTemplate boPdfDocumentTemplate = new BOPdfDocumentTemplate(featureFlipping, pdfSignatureService, executorService);

    private File outputfile;

    @AfterEach
    void tearDown() {
        if (outputfile != null) {
            outputfile.delete();
            outputfile = null;
        }
    }

    @AfterAll
    static void tearDownAll() {
        executorService.shutdown();
    }

    @DisplayName("Check if the pdf file is correctly generated in specific files")
    @Test
    public void check_render_with_special_files() {
        InputStream is = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("secret/TestBOFile1.pdf");

        FileInputStream data = FileInputStream
                .builder()
                .mediaType(MediaTypes.APPLICATION_PDF)
                .inputStream(is)
                .build();

        saveToFile(boPdfDocumentTemplate.renderAsync(Collections.singletonList(data), WATERMARK).join(), "target/resultSpecial.pdf");
    }


    @DisplayName("Check if the render is correctly generated from text pdf and wrong sized pdf")
    @Test
    public void check_render_with_text_pdf() {
        InputStream is = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("bigH.pdf");

        FileInputStream data = FileInputStream
                .builder()
                .mediaType(MediaTypes.APPLICATION_PDF)
                .inputStream(is)
                .build();

        saveToFile(boPdfDocumentTemplate.renderAsync(Collections.singletonList(data), WATERMARK).join(), "target/resultTestPdfWrongSize.pdf");
    }

    @DisplayName("Check if the render is correctly generated from all type textual pdf, pdf, image, non obfuscable pdf")
    @Test
    public void check_render_with_all_type() {
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

        saveToFile(boPdfDocumentTemplate.renderAsync(Arrays.asList(data, data2, data3, data4), WATERMARK).join(), "target/resultFullTypeTestPdf.pdf");
    }

    @DisplayName("Check if the render is correctly generated from image pdf")
    @Test
    public void check_render_with_img_pdf() {
        InputStream is = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("CNI.pdf");

        FileInputStream data = FileInputStream
                .builder()
                .mediaType(MediaTypes.APPLICATION_PDF)
                .inputStream(is)
                .build();

        saveToFile(boPdfDocumentTemplate.renderAsync(Collections.singletonList(data), WATERMARK).join(),  "target/resultTestPdfWithJpeg.pdf");
    }

    @DisplayName("Check if the render is correctly generated from jpegs")
    @Test
    public void check_render_from_jpegs() {
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

        saveToFile(boPdfDocumentTemplate.renderAsync(Arrays.asList(data, data2, data3), WATERMARK).join(), "target/resultTestJpeg.pdf");
    }

    @DisplayName("Render watermark (used mostly for developing)")
    @Test
    public void check_watermark_rendered() throws IOException {
        InputStream is = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("CNI.jpg");
        Assertions.assertNotNull(is);
        BufferedImage image = ImageIO.read(is);
        BufferedImage b = boPdfDocumentTemplate.applyWatermark(image, "watermark 2023");
        outputfile = new File("image.jpg");
        ImageIO.write(b, "jpg", outputfile);
    }

    @DisplayName("Avoid render above qrcode")
    @Test
    public void check_watermark_not_in_qrcode() {
        InputStream is = BOPdfDocumentTemplateTest.class.getClassLoader().getResourceAsStream("qrcode-sample.pdf");

        FileInputStream data = FileInputStream
                .builder()
                .mediaType(MediaTypes.APPLICATION_PDF)
                .inputStream(is)
                .build();

        saveToFile(boPdfDocumentTemplate.renderAsync(Collections.singletonList(data), WATERMARK).join(), "target/resultTestPdfWithQrCode.pdf");
    }

    private void saveToFile(InputStream inputStream, String path) {
        try {
            Files.copy(inputStream, Path.of(path), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

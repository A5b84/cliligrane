package io.a5b84.cliligrane;

import io.a5b84.cliligrane.common.service.interfaces.MimeTypeDetectionService;
import io.a5b84.cliligrane.pdfgenerator.model.FileInputStream;
import io.a5b84.cliligrane.pdfgenerator.service.templates.BOPdfDocumentTemplate;

import lombok.AllArgsConstructor;

import org.apache.tika.mime.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@AllArgsConstructor
public class CliWatermarkService {

    private final MimeTypeDetectionService mimeTypeDetectionService;
    private final BOPdfDocumentTemplate boPdfDocumentTemplate;

    public void processAndSave(Path inputPath, Path outputPath, String watermarkText) {
        MediaType mediaType = detectMediaType(inputPath);

        try (InputStream inputStream = Files.newInputStream(inputPath);
                InputStream watermarkedStream =
                        boPdfDocumentTemplate.render(
                                List.of(new FileInputStream(inputStream, mediaType)),
                                watermarkText)) {
            Files.copy(watermarkedStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("Could not process file at path " + inputPath, e);
        }
    }

    private MediaType detectMediaType(Path path) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            String type =
                    mimeTypeDetectionService.detect(
                            inputStream, String.valueOf(path.getFileName()));
            return MediaType.parse(type);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not detect the media type of file at path " + path, e);
        }
    }
}

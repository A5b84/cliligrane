package io.a5b84.cliligrane;

import io.a5b84.cliligrane.common.service.interfaces.MimeTypeDetectionService;
import io.a5b84.cliligrane.pdfgenerator.model.FileInputStream;
import io.a5b84.cliligrane.pdfgenerator.service.templates.BOPdfDocumentTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.tika.mime.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
public class CliWatermarkService {

    private final MimeTypeDetectionService mimeTypeDetectionService;
    private final BOPdfDocumentTemplate boPdfDocumentTemplate;

    /**
     * Watermarks the given batch of documents.
     *
     * @return whether all documents were processed successfully
     */
    public boolean processAndSaveBatch(DocumentBatch batch) {
        long startTime = System.nanoTime();
        int successfulCount = 0;
        int failedCount = 0;
        int processedCount = 0;

        for (DocumentBatch.Entry entry : batch.entries()) {
            try {
                processAndSave(entry.inputPath(), entry.outputPath(), batch.watermarkText());
                successfulCount++;
                log.info(
                        "[{}/{}] Successfully processed file at path {}, saved result to {}.",
                        processedCount + 1,
                        batch.entries().size(),
                        entry.inputPath(),
                        entry.outputPath());
            } catch (Exception e) {
                failedCount++;
                log.error(
                        "[{}/{}] Could not process file at path {}",
                        processedCount + 1,
                        batch.entries().size(),
                        entry.inputPath(),
                        e);
            }

            processedCount++;
        }

        long duration = System.nanoTime() - startTime;
        log.info(
                "Successfully watermarked {} documents in {} ms.",
                successfulCount,
                duration / TimeUnit.MILLISECONDS.toNanos(1));

        if (failedCount > 0) {
            log.error("{} documents could not be watermarked.", failedCount);
        }

        return failedCount == 0;
    }

    private void processAndSave(Path inputPath, Path outputPath, String watermarkText)
            throws IOException {
        MediaType mediaType = detectMediaType(inputPath);

        try (InputStream inputStream = Files.newInputStream(inputPath);
                InputStream watermarkedStream =
                        boPdfDocumentTemplate.render(
                                List.of(new FileInputStream(inputStream, mediaType)),
                                watermarkText)) {
            Files.copy(watermarkedStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
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

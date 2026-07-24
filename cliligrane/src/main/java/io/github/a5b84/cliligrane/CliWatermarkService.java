package io.github.a5b84.cliligrane;

import io.github.a5b84.cliligrane.common.service.interfaces.MimeTypeDetectionService;
import io.github.a5b84.cliligrane.pdfgenerator.model.FileInputStream;
import io.github.a5b84.cliligrane.pdfgenerator.service.templates.BOPdfDocumentTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.tika.mime.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Slf4j
public class CliWatermarkService {

    private final MimeTypeDetectionService mimeTypeDetectionService;
    private final BOPdfDocumentTemplate boPdfDocumentTemplate;
    private final ExecutorService executorService;

    /**
     * Watermarks the given batch of documents.
     *
     * @return whether all documents were processed successfully
     */
    public boolean processBatch(DocumentBatch batch) {
        long startTime = System.nanoTime();
        log.info("Starting processing of {} documents.", batch.entries().size());
        BatchProgress progress = new BatchProgress();

        CompletableFuture<?>[] futures =
                batch.entries().stream()
                        .map(entry -> processAsyncAndReport(batch, entry, progress))
                        .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).join();

        long duration = System.nanoTime() - startTime;
        log.info(
                "Successfully watermarked {} documents in {} ms.",
                progress.successfulCount().get(),
                duration / TimeUnit.MILLISECONDS.toNanos(1));

        int failedCount = progress.failedCount().get();
        if (failedCount > 0) {
            log.error("{} documents could not be watermarked.", failedCount);
        }

        return failedCount == 0;
    }

    private CompletableFuture<Void> processAsyncAndReport(
            DocumentBatch batch, DocumentBatch.Entry entry, BatchProgress progress) {
        return processAsync(entry.inputPath(), entry.outputPath(), batch.watermarkText())
                .handle(
                        (result, throwable) -> {
                            if (throwable == null) {
                                progress.successfulCount().incrementAndGet();
                                log.info(
                                        "[{}/{}] Successfully processed {}, saved result to {}.",
                                        progress.processedCount().incrementAndGet(),
                                        batch.entries().size(),
                                        entry.inputPath(),
                                        entry.outputPath());
                            } else {
                                progress.failedCount().incrementAndGet();
                                log.error(
                                        "[{}/{}] Could not process {}.",
                                        progress.processedCount().incrementAndGet(),
                                        batch.entries().size(),
                                        entry.inputPath(),
                                        throwable);
                            }

                            return result;
                        });
    }

    private CompletableFuture<Void> processAsync(
            Path inputPath, Path outputPath, String watermarkText) {
        InputStream inputStream;

        try {
            //noinspection resource
            inputStream = Files.newInputStream(inputPath);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }

        return CompletableFuture.supplyAsync(() -> detectMediaType(inputPath), executorService)
                .thenCompose(
                        mediaType ->
                                boPdfDocumentTemplate.renderAsync(
                                        List.of(new FileInputStream(inputStream, mediaType)),
                                        watermarkText))
                .thenAcceptAsync(
                        watermarkedStream -> {
                            try (watermarkedStream) {
                                Files.copy(
                                        watermarkedStream,
                                        outputPath,
                                        StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        executorService)
                .whenComplete(
                        (result, throwable) -> {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
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

    private record BatchProgress(
            AtomicInteger successfulCount,
            AtomicInteger failedCount,
            AtomicInteger processedCount) {
        public BatchProgress() {
            this(new AtomicInteger(), new AtomicInteger(), new AtomicInteger());
        }
    }
}

package io.github.a5b84.cliligrane;

import io.github.a5b84.cliligrane.common.service.interfaces.MimeTypeDetectionService;
import io.github.a5b84.cliligrane.pdfgenerator.model.FileInputStream;
import io.github.a5b84.cliligrane.pdfgenerator.service.templates.BOPdfDocumentTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.tika.mime.MediaType;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Slf4j
public class CliWatermarkService implements AutoCloseable {

    private final ExecutorService executorService =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final MimeTypeDetectionService mimeTypeDetectionService;
    private final BOPdfDocumentTemplate boPdfDocumentTemplate;

    /**
     * Watermarks the given batch of documents.
     *
     * @return whether all documents were processed successfully
     */
    public boolean processBatch(DocumentBatch batch) throws InterruptedException {
        long startTime = System.nanoTime();
        log.info("Starting processing of {} documents.", batch.entries().size());
        BatchProgress progress = new BatchProgress();

        List<Callable<@Nullable Void>> callables =
                batch.entries().stream()
                        .<Callable<@Nullable Void>>map(
                                entry ->
                                        () -> {
                                            processAndReport(batch, entry, progress);
                                            return null;
                                        })
                        .toList();
        executorService.invokeAll(callables);

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

    private void processAndReport(
            DocumentBatch batch, DocumentBatch.Entry entry, BatchProgress progress) {
        try {
            long start = System.nanoTime();
            process(entry.inputPath(), entry.outputPath(), batch.watermarkText());
            long duration = System.nanoTime() - start;
            progress.successfulCount().incrementAndGet();
            log.info(
                    "[{}/{}] Successfully processed {} in {} ms, saved result to {}.",
                    progress.processedCount().incrementAndGet(),
                    batch.entries().size(),
                    entry.inputPath(),
                    duration / TimeUnit.MILLISECONDS.toNanos(1),
                    entry.outputPath());
        } catch (Exception e) {
            progress.failedCount().incrementAndGet();
            log.error(
                    "[{}/{}] Could not process {}.",
                    progress.processedCount().incrementAndGet(),
                    batch.entries().size(),
                    entry.inputPath(),
                    e);
        }
    }

    private void process(Path inputPath, Path outputPath, String watermarkText) throws IOException {
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

    @Override
    public void close() {
        executorService.close();
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

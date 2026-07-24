package io.github.a5b84.cliligrane;

import io.github.a5b84.cliligrane.common.service.MimeTypeDetectionServiceImpl;
import io.github.a5b84.cliligrane.common.service.interfaces.MimeTypeDetectionService;
import io.github.a5b84.cliligrane.pdfgenerator.configuration.FeatureFlipping;
import io.github.a5b84.cliligrane.pdfgenerator.service.PdfSignatureServiceImpl;
import io.github.a5b84.cliligrane.pdfgenerator.service.interfaces.PdfSignatureService;
import io.github.a5b84.cliligrane.pdfgenerator.service.templates.BOPdfDocumentTemplate;

import lombok.Getter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Services implements AutoCloseable {

    private final ExecutorService executorService =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    @Getter private final CliWatermarkService watermarkService;

    public Services() {
        MimeTypeDetectionService mimeTypeDetectionService = new MimeTypeDetectionServiceImpl();
        FeatureFlipping featureFlipping = new FeatureFlipping(true, true);
        PdfSignatureService pdfSignatureService = new PdfSignatureServiceImpl(false, null, null);
        BOPdfDocumentTemplate boPdfDocumentTemplate = new BOPdfDocumentTemplate(featureFlipping, pdfSignatureService, executorService);
        watermarkService = new CliWatermarkService(mimeTypeDetectionService, boPdfDocumentTemplate, executorService);
    }

    @Override
    public void close() {
        executorService.close();
    }
}

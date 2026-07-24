package io.a5b84.cliligrane;

import io.a5b84.cliligrane.common.service.MimeTypeDetectionServiceImpl;
import io.a5b84.cliligrane.common.service.interfaces.MimeTypeDetectionService;
import io.a5b84.cliligrane.pdfgenerator.configuration.FeatureFlipping;
import io.a5b84.cliligrane.pdfgenerator.service.PdfSignatureServiceImpl;
import io.a5b84.cliligrane.pdfgenerator.service.interfaces.PdfSignatureService;
import io.a5b84.cliligrane.pdfgenerator.service.templates.BOPdfDocumentTemplate;

import lombok.extern.slf4j.Slf4j;

import picocli.CommandLine;

@Slf4j
public class Main {

    public static void main(String[] args) {
        int exitCode;

        try (CliWatermarkService watermarkService = createCliWatermarkService()) {
            exitCode = new CommandLine(new CliligraneCommand(watermarkService)).execute(args);
        }

        System.exit(exitCode);
    }

    private static CliWatermarkService createCliWatermarkService() {
        MimeTypeDetectionService mimeTypeDetectionService = new MimeTypeDetectionServiceImpl();
        FeatureFlipping featureFlipping = new FeatureFlipping(true, true);
        PdfSignatureService pdfSignatureService = new PdfSignatureServiceImpl(false, null, null);
        BOPdfDocumentTemplate boPdfDocumentTemplate =
                new BOPdfDocumentTemplate(featureFlipping, pdfSignatureService);
        return new CliWatermarkService(mimeTypeDetectionService, boPdfDocumentTemplate);
    }
}

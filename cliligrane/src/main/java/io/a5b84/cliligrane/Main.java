package io.a5b84.cliligrane;

import fr.dossierfacile.api.pdfgenerator.configuration.FeatureFlipping;
import fr.dossierfacile.api.pdfgenerator.service.PdfSignatureServiceImpl;
import fr.dossierfacile.api.pdfgenerator.service.interfaces.PdfSignatureService;
import fr.dossierfacile.api.pdfgenerator.service.templates.BOPdfDocumentTemplate;
import fr.dossierfacile.common.service.MimeTypeDetectionServiceImpl;
import fr.dossierfacile.common.service.interfaces.MimeTypeDetectionService;

public class Main {
    public static void main(String[] args) {
        CliParameters parameters = CliParameters.parseOrExit(args);
        CliWatermarkService cliWatermarkService = createCliWatermarkService();
        cliWatermarkService.processAndSave(
                parameters.inputPath(), parameters.outputPath(), parameters.watermarkText());
    }

    private static CliWatermarkService createCliWatermarkService() {
        MimeTypeDetectionService mimeTypeDetectionService = new MimeTypeDetectionServiceImpl();
        FeatureFlipping featureFlipping = new FeatureFlipping();
        PdfSignatureService pdfSignatureService = new PdfSignatureServiceImpl();
        BOPdfDocumentTemplate boPdfDocumentTemplate =
                new BOPdfDocumentTemplate(null, featureFlipping, pdfSignatureService);
        return new CliWatermarkService(mimeTypeDetectionService, boPdfDocumentTemplate);
    }
}

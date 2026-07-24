package io.a5b84.cliligrane.pdfgenerator.model;

import org.apache.pdfbox.pdmodel.common.PDRectangle;

public record PdfTemplateParameters(
        PDRectangle mediaBox, float compressionQuality, PageDimension maxPage) {
    public static final PdfTemplateParameters DEFAULT =
            new PdfTemplateParameters(PDRectangle.A4, 0.9f, PageDimension.A4_150_DPI);
}

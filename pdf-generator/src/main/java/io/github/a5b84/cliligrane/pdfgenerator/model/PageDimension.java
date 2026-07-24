package io.github.a5b84.cliligrane.pdfgenerator.model;

public record PageDimension(int width, int height, int dpi) {
    public static final PageDimension A4_150_DPI = new PageDimension(1240, 1754, 150);
}

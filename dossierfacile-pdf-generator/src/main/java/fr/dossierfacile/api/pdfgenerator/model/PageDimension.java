package fr.dossierfacile.api.pdfgenerator.model;

public record PageDimension(int width, int height, int dpi) {
    public static final PageDimension A4_150_DPI = new PageDimension(1240, 1754, 150);
}

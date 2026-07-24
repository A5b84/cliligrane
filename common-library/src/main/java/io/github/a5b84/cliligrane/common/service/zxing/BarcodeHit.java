package io.github.a5b84.cliligrane.common.service.zxing;

public record BarcodeHit(String format, String text, BarcodeBbox bbox) {}

package io.github.a5b84.cliligrane.pdfgenerator.model;

import lombok.Builder;

import org.apache.tika.mime.MediaType;

import java.io.InputStream;

@Builder
public record FileInputStream(InputStream inputStream, MediaType mediaType) {}

package fr.dossierfacile.common.utils;

import org.apache.tika.mime.MediaType;

public final class MediaTypes {
    private MediaTypes() {}

    public static final MediaType APPLICATION_PDF = MediaType.application("pdf");
    public static final MediaType IMAGE_JPEG = MediaType.image("jpeg");
}

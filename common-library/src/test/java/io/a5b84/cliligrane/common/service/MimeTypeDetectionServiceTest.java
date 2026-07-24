package io.a5b84.cliligrane.common.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

class MimeTypeDetectionServiceTest {

    private final MimeTypeDetectionServiceImpl service = new MimeTypeDetectionServiceImpl();

    static Stream<Arguments> knownMimeTypes() {
        return Stream.of(
                Arguments.of("doc.pdf",  "%PDF-1.4".getBytes(),                                         "application/pdf"),
                Arguments.of("img.jpg",  new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},            "image/jpeg"),
                Arguments.of("img.png",  new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A},       "image/png"),
                Arguments.of("file.exe", "MZ executable".getBytes(),                                    "application/x-dosexec")
        );
    }

    @ParameterizedTest(name = "{0} → {2}")
    @MethodSource("knownMimeTypes")
    void should_detect_mime_type_from_magic_bytes(String filename, byte[] content, String expectedMimeType) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(content);
        assertThat(service.detect(inputStream, filename)).isEqualTo(expectedMimeType);
    }

    @Test
    void should_return_octet_stream_for_unknown_content() throws IOException {
        byte[] unknownContent = new byte[]{0x00, 0x01, 0x02, 0x03};
        InputStream inputStream = new ByteArrayInputStream(unknownContent);
        assertThat(service.detect(inputStream, "unknown")).isNotBlank();
    }
}

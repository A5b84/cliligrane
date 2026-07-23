package fr.dossierfacile.common.service.interfaces;

import java.io.IOException;
import java.io.InputStream;

/**
 * OWASP File Upload — "Validate the file type, don't trust the Content-Type header".
 * <p>
 * Detects the real MIME type of a file using magic bytes (Apache Tika), independently
 * of the client-supplied Content-Type header which can be spoofed.
 */
public interface MimeTypeDetectionService {

    /**
     * Detects the MIME type of the given file by reading its content (magic bytes).
     * The original filename is used as a hint.
     * @param inputStream an input stream to read the file
     * @param name the name of the file
     * @return the detected MIME type, or {@code "application/octet-stream"} if detection is inconclusive
     * @throws IOException if the file content cannot be read
     */
    String detect(InputStream inputStream, String name) throws IOException;
}

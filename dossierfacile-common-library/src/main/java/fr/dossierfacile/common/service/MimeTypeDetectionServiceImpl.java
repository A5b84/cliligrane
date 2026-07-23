package fr.dossierfacile.common.service;

import fr.dossierfacile.common.service.interfaces.MimeTypeDetectionService;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Service
public class MimeTypeDetectionServiceImpl implements MimeTypeDetectionService {

    private static final String FALLBACK_MIME_TYPE = "application/octet-stream";
    private static final Tika TIKA = new Tika();

    @Override
    public String detect(MultipartFile file) throws IOException {
        String safePath = StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), ""));
        return detect(file.getInputStream(), safePath);
    }

    @Override
    public String detect(InputStream inputStream, String name) throws IOException {
        String detected = TIKA.detect(inputStream, name);
        return detected != null ? detected : FALLBACK_MIME_TYPE;
    }
}

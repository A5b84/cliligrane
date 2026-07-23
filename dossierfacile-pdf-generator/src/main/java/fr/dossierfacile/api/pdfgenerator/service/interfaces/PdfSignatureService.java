package fr.dossierfacile.api.pdfgenerator.service.interfaces;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface PdfSignatureService {

    void signAndSave(PDDocument document, ByteArrayOutputStream baos) throws IOException;
}

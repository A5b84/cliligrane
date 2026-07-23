package fr.dossierfacile.api.pdfgenerator.service;

import fr.dossierfacile.api.pdfgenerator.service.interfaces.PdfSignatureService;

import lombok.RequiredArgsConstructor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.CollectionStore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;

@RequiredArgsConstructor
public class PdfSignatureServiceImpl implements PdfSignatureService {

    private static final String SERVICE_NAME = "Cliligrane";

    private final boolean signatureActivation;
    private final String certificate;
    private final String privateKey;

    @Override
    public void signAndSave(PDDocument document, ByteArrayOutputStream baos) throws IOException {
        PDDocumentInformation information = new PDDocumentInformation();
        information.setCreator(SERVICE_NAME);
        information.setCreationDate(Calendar.getInstance());
        document.setDocumentInformation(information);

        if (!signatureActivation || certificate == null || privateKey == null) {
            document.save(baos);
        } else {
            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName(SERVICE_NAME);
            signature.setReason("Document filigrané par " + SERVICE_NAME);
            signature.setSignDate(Calendar.getInstance());

            document.addSignature(signature, content -> {
                try {
                    return signInputStream(loadPrivateKey(), loadCertifcateChain(), content);
                } catch (Exception e) {
                    throw new IOException("Unable to sign the document", e);
                }
            });

            document.save(baos);
        }
    }


    private Certificate[] loadCertifcateChain() throws Exception {
        byte[] certBytes = Base64.getDecoder().decode(certificate);
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        Certificate cert = certFactory.generateCertificate(new ByteArrayInputStream(certBytes));
        return new Certificate[]{cert};
    }

    private PrivateKey loadPrivateKey() throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    private byte[] signInputStream(PrivateKey privateKey, Certificate[] certificates, InputStream content) throws Exception {
        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA").build(privateKey);
        generator.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build())
                .build(contentSigner, (X509Certificate) certificates[0]));

        CollectionStore<?> certStore = new JcaCertStore(Arrays.asList(certificates));
        generator.addCertificates(certStore);

        byte[] contentBytes = content.readAllBytes();
        CMSProcessableByteArray cmsData = new CMSProcessableByteArray(contentBytes);
        CMSSignedData signedData = generator.generate(cmsData, false);

        return signedData.getEncoded();
    }
}
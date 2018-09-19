// Copyright (c) 2017 Travelex Ltd

package com.travelex.security;

import net.oauth.signature.pem.PEMReader;
import net.oauth.signature.pem.PKCS1EncodedKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

public class CertificateUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(CertificateUtils.class);

    /**
     * Converts from string to {@link PrivateKey} instance.
     * 
     * @param pem Private key content as string
     * @return Create {@link PrivateKey} instance from string
     * @throws GeneralSecurityException for exception occurred while creating {@link PrivateKey}
     *         instance from string
     * @throws IOException for exception occurred while reading pem content
     */
    public static PrivateKey loadPrivateKey(String pem)
                    throws GeneralSecurityException, IOException {

        InputStream stream = new ByteArrayInputStream(pem.getBytes("UTF-8"));

        PEMReader reader = new PEMReader(stream);

        LOGGER.info("Reading pem content");
        byte[] bytes = reader.getDerBytes();

        KeySpec keySpec;

        if (PEMReader.PRIVATE_PKCS1_MARKER.equals(reader.getBeginMarker())) {
            LOGGER.info("pem content is of type PKCS1");
            keySpec = new PKCS1EncodedKeySpec(bytes).getKeySpec();
        } else if (PEMReader.PRIVATE_PKCS8_MARKER.equals(reader.getBeginMarker())) {
            LOGGER.info("pem content is of type PKCS8");
            keySpec = new PKCS8EncodedKeySpec(bytes);
        } else {
            throw new IOException("Invalid PEM content: Unknown marker " + "for private key "
                            + reader.getBeginMarker());
        }

        KeyFactory fac = KeyFactory.getInstance("RSA");
        return fac.generatePrivate(keySpec);
    }

    /**
     * Converts from string to {@link Certificate} instance.
     * 
     * @param certsContent Certificate content as string.
     * @return {@link Certificate} instance
     * @throws CertificateException for exception occurred while creating {@link Certificate}
     *         instance from string
     * @throws UnsupportedEncodingException for exception while reading the certificate content
     *         assuming its UTF-8 encoded
     */
    public static Certificate[] createCertificateChain(List<String> certsContent)
                    throws CertificateException, UnsupportedEncodingException {
        CertificateFactory certFactory = CertificateFactory.getInstance(TlsConstants.X509);

        // Certificate string to Certificate instance
        List<Certificate> certs = new ArrayList<>();
        for (String certContent : certsContent) {
            ByteArrayInputStream certBytes =
                            new ByteArrayInputStream(certContent.getBytes("UTF-8"));
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(certBytes);
            certs.add(cert);
        }
        return certs.toArray(new Certificate[certs.size()]);
    }
}

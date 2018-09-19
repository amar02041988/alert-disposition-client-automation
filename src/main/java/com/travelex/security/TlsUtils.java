// Copyright (c) 2017 Travelex Ltd
package com.travelex.security;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.transport.http.HTTPConduit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Helper class that augments object with {@link KeyManager}s and {@link TrustManager}s.
 */
@SuppressWarnings("PMD.TooManyStaticImports")
public class TlsUtils {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TlsUtils.class);

    private final TLSClientParameters tlsParams = new TLSClientParameters();
    static final TlsUtils tlsUtils = new TlsUtils();

    /**
     * Visible For Testing.
     */
    private TlsUtils() {}


    /**
     * Setup key store
     * 
     * @param httpConduit {@link HTTPConduit} for TLS setup
     * @param keyStoreType key store type
     * @param privateKeyEntriesWithAlias private key entries having alias with its associated
     *        private key and certificate chain.
     */
    public static HTTPConduit setupKeyStore(HTTPConduit httpConduit, String keyStoreType,
                    Map<String, KeyStore.PrivateKeyEntry> privateKeyEntriesWithAlias) {
        return tlsUtils.addKeyManagers(httpConduit, keyStoreType, privateKeyEntriesWithAlias);
    }


    /**
     * Setup trust store.
     * 
     * @param httpConduit {@link HTTPConduit} for TLS setup
     * @param trustStoreType trust store type
     * @param certsWithAlias certificates with alias
     */
    public static HTTPConduit setupTrustStore(HTTPConduit httpConduit, String trustStoreType,
                    Map<String, String> certsWithAlias) {
        return tlsUtils.addTrustManagers(httpConduit, trustStoreType, certsWithAlias);
    }

    /**
     * Adds trust manager.
     * 
     * @param httpConduit {@link HTTPConduit} for TLS setup
     * @param trustStoreType trust store type
     * @param certsWithAlias certificates with alias
     * @return httpConduit {@link HTTPConduit} with {@link TrustManager} added
     */
    public HTTPConduit addTrustManagers(HTTPConduit httpConduit, String trustStoreType,
                    Map<String, String> certsWithAlias) {

        tlsParams.setTrustManagers(
                        createTrustManagers(getRandomPassword(), trustStoreType, certsWithAlias));

        // tlsParams.setDisableCNCheck(true);
        httpConduit.setTlsClientParameters(tlsParams);
        return httpConduit;
    }

    /**
     * Adds key managers.
     * 
     * @param httpConduit {@link HTTPConduit} for TLS setup
     * @param keyStoreType key store type
     * @param privateKeyEntriesWithAlias private key entries having alias with its associated
     *        private key and certificate chain.
     * @return httpConduit {@link HTTPConduit} with {@link KeyManager} added
     */
    public HTTPConduit addKeyManagers(HTTPConduit httpConduit, String keyStoreType,
                    Map<String, KeyStore.PrivateKeyEntry> privateKeyEntriesWithAlias) {
        tlsParams.setKeyManagers(createKeyManagers(keyStoreType, getRandomPassword(),
                        privateKeyEntriesWithAlias));
        httpConduit.setTlsClientParameters(tlsParams);
        return httpConduit;
    }



    /**
     * Creates key managers.
     *
     * @param keyStoreType the key store type
     * @param password the password
     * @param privateKeyEntriesWithAlias private key entries having alias with its associated
     *        private key and certificate chain.
     * @return the key manager[]
     */
    KeyManager[] createKeyManagers(String keyStoreType, char[] password,
                    Map<String, KeyStore.PrivateKeyEntry> privateKeyEntriesWithAlias) {
        String algoName = KeyManagerFactory.getDefaultAlgorithm();

        KeyManagerFactory kmf;
        try {
            kmf = KeyManagerFactory.getInstance(algoName);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        try {
            kmf.init(getKeyStore(keyStoreType, password, privateKeyEntriesWithAlias), password);
        } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return kmf.getKeyManagers();
    }

    /**
     * Gets key store.
     *
     * @param keyStoreType the key store type
     * @param password the password
     * @param privateKeyEntriesWithAlias private key entries having alias with its associated
     *        private key and certificate chain.
     * @return the key store
     */
    private KeyStore getKeyStore(String keyStoreType, char[] password,
                    Map<String, KeyStore.PrivateKeyEntry> privateKeyEntriesWithAlias) {

        KeyStore ks;
        try {
            ks = KeyStore.getInstance(keyStoreType);
        } catch (KeyStoreException e) {
            LOGGER.error("Failed to create keystore instance: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        try {
            ks.load(null, password);
            LOGGER.info("Empty keystore created");

            LOGGER.info("Start adding private key entries in keystore");
            for (Map.Entry<String, KeyStore.PrivateKeyEntry> privateKeyEntryWithAlias : privateKeyEntriesWithAlias
                            .entrySet()) {
                String alias = privateKeyEntryWithAlias.getKey();
                KeyStore.PrivateKeyEntry privateKeyEntry = privateKeyEntryWithAlias.getValue();
                ks.setKeyEntry(alias, privateKeyEntry.getPrivateKey(), password,
                                privateKeyEntry.getCertificateChain());
                LOGGER.info("Private key entry added in keystore for alias: {}", alias);
            }
        } catch (NoSuchAlgorithmException | CertificateException | IOException
                        | KeyStoreException e) {
            LOGGER.error("Failed to configure keystore: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return ks;
    }


    /**
     * Gets trust store.
     *
     * @param password the password
     * @param trustStoreType the trust store type
     * @param certsWithAlias certificates with alias
     * @return the trust store
     */
    private KeyStore getTrustStore(char[] password, String trustStoreType,
                    Map<String, String> certsWithAlias) {

        KeyStore ts;
        try {
            ts = KeyStore.getInstance(trustStoreType);
        } catch (KeyStoreException e) {
            LOGGER.error("Failed to create truststore instance: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        try {
            ts.load(null, password);
            LOGGER.info("Empty truststore created");

            // Add the certificate
            CertificateFactory cf = CertificateFactory.getInstance(TlsConstants.X509);

            LOGGER.info("Start adding certificates in Truststore");
            for (Map.Entry<String, String> certWithAlias : certsWithAlias.entrySet()) {
                String alias = certWithAlias.getKey();
                Certificate cert = cf.generateCertificate(new ByteArrayInputStream(
                                certWithAlias.getValue().getBytes(TlsConstants.ISO)));
                LOGGER.info("Generated certificate instance from content for alias: {}", alias);
                ts.setCertificateEntry(alias, cert);
                LOGGER.info("Certificate added in truststore for alias: {}", alias);
            }

        } catch (NoSuchAlgorithmException | CertificateException | IOException
                        | KeyStoreException e) {
            LOGGER.error("Failed to configure truststore: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return ts;
    }

    /**
     * Creates trust managers.
     *
     * @param password the password
     * @param trustStoreType the trust store type
     * @param clientCerts the client cert
     * @return the trust manager[]
     */
    TrustManager[] createTrustManagers(char[] password, String trustStoreType,
                    Map<String, String> certsWithAlias) {
        String algoName = TrustManagerFactory.getDefaultAlgorithm();

        TrustManagerFactory tmf;
        try {
            tmf = TrustManagerFactory.getInstance(algoName);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        try {
            tmf.init(getTrustStore(password, trustStoreType, certsWithAlias));
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }

        return tmf.getTrustManagers();
    }

    /**
     * Gets random password.
     *
     * @return random password
     */
    private char[] getRandomPassword() {
        String passChars = TlsConstants.PASSCHAR;
        StringBuilder password = new StringBuilder();
        Random rnd = new Random();
        while (password.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * passChars.length());
            password.append(passChars.charAt(index));
        }
        char[] pwdStr = password.toString().toCharArray();
        return pwdStr;

    }
}

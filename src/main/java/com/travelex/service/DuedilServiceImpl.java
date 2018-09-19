package com.travelex.service;

import com.travelex.model.AlertDetail;
import com.travelex.model.DuedilStatusResultDto;
import com.travelex.security.CertificateUtils;
import com.travelex.security.TlsConstants;
import com.travelex.security.TlsUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.Logger;

import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

public class DuedilServiceImpl implements DuedilService {

    private static final Logger LOGGER = Logger.getLogger(DuedilServiceImpl.class);
    private PollingServiceDao pollingServiceDao;
    private String baseUri;
    private String statusUri;

    public DuedilServiceImpl() {
        super();
    }

    public DuedilServiceImpl(PollingServiceDao pollingServiceDao, String baseUri,
                    String statusUri) {
        super();
        this.pollingServiceDao = pollingServiceDao;
        this.baseUri = baseUri;
        this.statusUri = statusUri;
        // webClient = initWebClient(baseUri);
    }

    public WebClient initWebClient(String baseUri) {
        WebClient webClient = WebClient.create(baseUri);
        HTTPConduit httpConduit = (HTTPConduit) WebClient.getConfig(webClient).getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setProxyServer("116.50.59.150");
        httpClientPolicy.setProxyServerPort(Integer.parseInt("8081"));
        httpConduit.setClient(httpClientPolicy);

        try {
            httpConduit = configure(httpConduit);
        } catch (Exception e) {
            LOGGER.error("Error occurred while configuring http conduit: " + e.getMessage());
            e.printStackTrace();
        }

        LOGGER.debug("Security is setup successfully !!!");

        return webClient;
    }

    public String getStatusUri() {
        return statusUri;
    }


    public void setStatusUri(String statusUri) {
        this.statusUri = statusUri;
    }

    public void checkStatus(AlertDetail alertDetail) throws Exception {
        LOGGER.info(alertDetail.toString());

        WebClient webClient = initWebClient(baseUri);
        String actualStatusUri = statusUri.replace("{alertId}", alertDetail.getAlertId());
        Response response = webClient.path(actualStatusUri).get();
        LOGGER.info("Response http status: " + response.getStatus());

        String rawResponseString = response.readEntity(String.class);
        LOGGER.info("Raw response String: " + rawResponseString);

        DuedilStatusResultDto duedilStatusResultDto = new ObjectMapper()
                        .readValue(rawResponseString, DuedilStatusResultDto.class);

        pollingServiceDao.update(alertDetail.getAlertId(), duedilStatusResultDto.getStatus());
    }


    public PollingServiceDao getPollingServiceDao() {
        return pollingServiceDao;
    }


    public void setPollingServiceDao(PollingServiceDao pollingServiceDao) {
        this.pollingServiceDao = pollingServiceDao;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public static HTTPConduit configure(HTTPConduit httpConduit) throws Exception {

        // create certificate chain
        List<String> certsContent = new ArrayList<>();
        certsContent.add(SecurityCerts.clientCert);

        Certificate[] chain = CertificateUtils.createCertificateChain(certsContent);
        LOGGER.debug("Certificate chain created successfully");

        // create private key
        PrivateKey privateKey = CertificateUtils.loadPrivateKey(SecurityCerts.clientKey);
        LOGGER.debug("Private key loaded successfully");

        // create private key entry
        PrivateKeyEntry privateKeyEntry = new PrivateKeyEntry(privateKey, chain);
        LOGGER.debug("Private key entry created successfully");

        // setup keystore
        Map<String, KeyStore.PrivateKeyEntry> privateKeyEntriesWithAlias = new HashMap<>();
        privateKeyEntriesWithAlias.put("DUEDIL_PRIVATE_KEY_ENTRY_ALIAS", privateKeyEntry);
        TlsUtils.setupKeyStore(httpConduit, TlsConstants.STORE_TYPE, privateKeyEntriesWithAlias);
        LOGGER.debug("Keystore is setup successfully");

        return httpConduit;
    }

}

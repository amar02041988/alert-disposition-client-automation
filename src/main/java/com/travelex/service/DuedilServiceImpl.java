package com.travelex.service;

import com.travelex.model.AlertDetail;
import com.travelex.model.DuedilStatusResultDto;

import org.apache.log4j.Logger;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class DuedilServiceImpl implements DuedilService {

    private static final Logger LOGGER = Logger.getLogger(DuedilServiceImpl.class);
    private PollingServiceDao pollingServiceDao;
    private RestTemplate restTemplate;
    private String baseUri;
    private String statusUri;

    public DuedilServiceImpl() {
        super();
    }

    public DuedilServiceImpl(PollingServiceDao pollingServiceDao, RestTemplate restTemplate,
                    String baseUri, String statusUri) {
        super();
        this.pollingServiceDao = pollingServiceDao;
        this.restTemplate = restTemplate;
        this.baseUri = baseUri;
        this.statusUri = statusUri;
    }


    public String getStatusUri() {
        return statusUri;
    }


    public void setStatusUri(String statusUri) {
        this.statusUri = statusUri;
    }

    public void checkStatus(AlertDetail alertDetail) {
        LOGGER.info(alertDetail.toString());

        Map<String, Object> params = new HashMap<>();
        params.put("alertId", alertDetail.getAlertId());

        DuedilStatusResultDto duedilStatusResultDto = restTemplate.getForObject(baseUri + statusUri,
                        DuedilStatusResultDto.class, params);

        System.out.println(":::::::::" + duedilStatusResultDto);
        pollingServiceDao.update(alertDetail.getAlertId(), duedilStatusResultDto.getStatus());
    }


    public PollingServiceDao getPollingServiceDao() {
        return pollingServiceDao;
    }


    public void setPollingServiceDao(PollingServiceDao pollingServiceDao) {
        this.pollingServiceDao = pollingServiceDao;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }


    public String getBaseUri() {
        return baseUri;
    }


    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }


    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

}

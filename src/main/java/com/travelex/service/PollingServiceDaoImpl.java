package com.travelex.service;

import com.travelex.model.AlertDetail;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PollingServiceDaoImpl implements PollingServiceDao {

    private JdbcTemplate jdbcTemplate;
    private AlertDetailRowMapper alertDetailRowMapper;
    private static final Logger LOGGER = Logger.getLogger(PollingServiceDaoImpl.class);

    public PollingServiceDaoImpl() {
        super();
    }

    public PollingServiceDaoImpl(JdbcTemplate jdbcTemplate,
                    AlertDetailRowMapper alertDetailRowMapper) {
        super();
        this.jdbcTemplate = jdbcTemplate;
        this.alertDetailRowMapper = alertDetailRowMapper;
    }

    @Override
    public List<AlertDetail> read() {

        List<AlertDetail> alertDetails = Optional.ofNullable(jdbcTemplate.query(
                        "select LINE_NO, ALERT_ID, STATUS, DATE_TIME from ALERT_DETAILS where STATUS='UNCONFIRMED'",
                        alertDetailRowMapper)).orElseGet(() -> new ArrayList<AlertDetail>());

        LOGGER.info("Total no. of PENDING alert ids: " + alertDetails.size());
        return alertDetails;
    }

    @Override
    public synchronized boolean update(String alertId, String status) {
        String updateQuery = "update ALERT_DETAILS set STATUS=?, DATE_TIME=? where ALERT_ID=?";
        int updateRowCount = jdbcTemplate.update(updateQuery, status,
                        new SimpleDateFormat("dd-MM-yyyy hh:mm:ss.mmm")
                                        .format(new java.util.Date()),
                        alertId);
        LOGGER.info("Updated alert id: " + alertId + " with status: " + status);
        return updateRowCount > 0 ? true : false;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }


    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public AlertDetailRowMapper getAlertDetailRowMapper() {
        return alertDetailRowMapper;
    }


    public void setAlertDetailRowMapper(AlertDetailRowMapper alertDetailRowMapper) {
        this.alertDetailRowMapper = alertDetailRowMapper;
    }

}

package com.travelex.service;

import com.travelex.model.AlertDetail;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AlertDetailRowMapper implements RowMapper<AlertDetail> {

    public AlertDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new AlertDetail(rs.getString("LINE_NO"), rs.getString("ALERT_ID"),
                        rs.getString("STATUS"), rs.getString("DATE_TIME"));
    }
}

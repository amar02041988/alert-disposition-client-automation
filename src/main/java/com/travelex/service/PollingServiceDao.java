package com.travelex.service;

import com.travelex.model.AlertDetail;

import java.util.List;

public interface PollingServiceDao {

    public List<AlertDetail> read();

    public boolean update(String alertId, String status);
}

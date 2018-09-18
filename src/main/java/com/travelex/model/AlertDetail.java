package com.travelex.model;

public class AlertDetail {

    private String lineNo;
    private String alertId;
    private String status;
    private String dateTime;

    public AlertDetail() {
        super();
    }


    public AlertDetail(String lineNo, String alertId, String status, String dateTime) {
        super();
        this.lineNo = lineNo;
        this.alertId = alertId;
        this.status = status;
        this.dateTime = dateTime;
    }


    public String getLineNo() {
        return lineNo;
    }

    public void setLineNo(String lineNo) {
        this.lineNo = lineNo;
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return "AlertDetail [lineNo=" + lineNo + ", alertId=" + alertId + ", status=" + status
                        + ", dateTime=" + dateTime + "]";
    }

}

package com.travelex.model;


public class DuedilStatusResultDto {

    private String ticketId;
    private String status;

    public DuedilStatusResultDto(String ticketId, String status) {
        super();
        this.ticketId = ticketId;
        this.status = status;
    }

    public DuedilStatusResultDto() {
        super();
        // TODO Auto-generated constructor stub
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "DuedilStatusResultDto [ticketId=" + ticketId + ", status=" + status + "]";
    }

}

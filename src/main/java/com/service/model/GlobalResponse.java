package com.service.model;

import lombok.Data;

@Data
public class GlobalResponse {
    private String message;
    private int httpStatusCode;
    private Boolean status;
    private Object body;
    public GlobalResponse(){}
    public GlobalResponse(String message, int httpStatus, Boolean status,Object body){
        this.message = message;
        this.httpStatusCode = httpStatus;
        this.status = status;
        this.body = body;
    }

    public GlobalResponse(String message, int httpStatus){
        this.message = message;
        this.httpStatusCode = httpStatus;
        this.status = false;
        this.body = null;
    }

    public Object getBody(){
        return this.body;
    }
}

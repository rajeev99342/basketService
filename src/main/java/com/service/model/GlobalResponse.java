package com.service.model;

import lombok.Data;
import org.springframework.http.HttpStatus;

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

    public static GlobalResponse getSuccess(Object body){
        return new GlobalResponse("success", HttpStatus.OK.value(), true, body);
    }

    public static GlobalResponse getSuccess(String message){
        return new GlobalResponse(message, HttpStatus.OK.value(), true, null);
    }
    public static GlobalResponse getFailure(String message){
        String messages = "Failed due to %s";
        String.format(messages, message);
        return new GlobalResponse(messages, HttpStatus.INTERNAL_SERVER_ERROR.value(), Boolean.FALSE, null);
    }

    public Object getBody(){
        return this.body;
    }
}

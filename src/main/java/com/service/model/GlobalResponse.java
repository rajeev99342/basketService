package com.service.model;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class GlobalResponse {
    private String message;
    private HttpStatus httpStatus;
    private Boolean status;
    private Object body;
    public GlobalResponse(String message, HttpStatus httpStatus, Boolean status,Object body){
        this.message = message;
        this.httpStatus = httpStatus;
        this.status = status;
        this.body = body;
    }

    public GlobalResponse(String message, HttpStatus httpStatus){
        this.message = message;
        this.httpStatus = httpStatus;
        this.status = false;
        this.body = null;
    }
}

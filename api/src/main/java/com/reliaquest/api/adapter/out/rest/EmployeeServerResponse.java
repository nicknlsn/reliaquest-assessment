package com.reliaquest.api.adapter.out.rest;

import lombok.Data;

@Data
public class EmployeeServerResponse<T> {

    private T data;
    private String status;
    private String error;
}

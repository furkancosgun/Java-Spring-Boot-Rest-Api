package com.furkancosgun.users.Exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotFoundException extends Exception {

    private int errorCode;
    private String errorMessage;
}
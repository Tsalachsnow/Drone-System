package com.musalasoft.exception;

import lombok.Data;

@Data
public class NoDataFoundException extends RuntimeException{


    public NoDataFoundException(String message){
        super(message);
    }


}

package com.converter.exceptions;

public class MaxRetriesReachedException extends RuntimeException{
    public MaxRetriesReachedException(String msg){
        super(msg);
    }
}

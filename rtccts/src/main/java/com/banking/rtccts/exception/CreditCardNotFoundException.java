package com.banking.rtccts.exception;

public class CreditCardNotFoundException extends RuntimeException{
    public CreditCardNotFoundException(String message) {
        super(message);
    }
}

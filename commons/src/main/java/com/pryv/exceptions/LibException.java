package com.pryv.exceptions;

/**
 * Created by thiebaudmodoux on 09.11.17.
 */

public class LibException extends PryvException {
    private String id;
    private String message;

    public LibException(String id, String msg) {
        this.id = id;
        this.message = msg;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getMsg() {
        return message;
    }
}

package com.pryv.exceptions;

import java.util.ArrayList;

/**
 * Created by thiebaudmodoux on 09.11.17.
 */

public class ApiException extends PryvException {

    private String id;
    private String message;
    private String data;
    private ArrayList<String> subErrors;

    public ApiException (String id, String msg, String data, ArrayList sub) {
        this.id = id;
        this.message = msg;
        this.data = data;
        this.subErrors = sub;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getMsg() {
        return message;
    }

    public String getData() {
        return data;
    }

    public ArrayList<String> getSubErrors() {
        return subErrors;
    }

}

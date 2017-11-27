package com.pryv.exceptions;

/**
 * Abstract Pryv Exception
 */
abstract class PryvException extends Exception {

    public abstract String getId();

    public abstract String getMsg();
}

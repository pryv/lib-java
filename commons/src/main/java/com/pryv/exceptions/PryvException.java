package com.pryv.exceptions;

import java.util.ArrayList;

/**
 * Created by thiebaudmodoux on 09.11.17.
 */

abstract class PryvException extends Exception {

    public abstract String getId();

    public abstract String getMsg();
}

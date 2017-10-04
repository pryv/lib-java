package com.pryv.operations;

/**
 * Created by thiebaudmodoux on 04.10.17.
 */

public class OperationFactory {
    public Operation buildOperation(String identifier) {
        switch (identifier) {
            case "get":
                return new GetOperation();
            case "create":
                return new CreateOperation();
            case "delete":
                return new DeleteOperation();
            case "update":
                return new UpdateOperation();
            default:
                return null;
        }
    }
}

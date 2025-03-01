package com.sky.exception;

/**
 * @author raoxin
 */
public class EmployeeAlreadyExistException extends BaseException{
    public EmployeeAlreadyExistException() {
        super();
    }

    public EmployeeAlreadyExistException(String msg) {
        super(msg);
    }
}

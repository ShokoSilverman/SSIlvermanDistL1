package com.example.ssilvermandistl1.Controllers;

public class MyExceptionMessage {

    private String _message;
    private Integer _httpStatus;

    public MyExceptionMessage(String _message, Integer _httpStatus, Throwable _throwable) {
        this._message = _message;
        this._httpStatus = _httpStatus;
    }

    public MyExceptionMessage(String _message, Integer _httpStatus) {
        this._message = _message;
        this._httpStatus = _httpStatus;
    }

    public String get_message() {
        return _message;
    }

    public void set_message(String _message) {
        this._message = _message;
    }

    public Integer get_httpStatus() {
        return _httpStatus;
    }

    public void set_httpStatus(Integer _httpStatus) {
        this._httpStatus = _httpStatus;
    }


}

package com.example.myreadbookapplication.model;

public class ApiResponse<T> { // Generatic de cho list<book> hoac list<category>

    private boolean success;
    private String message;
    private T data; //co the la object list<book> hoac list<category>

    public boolean isSuccess(){ return success;}
    public void setSuccess(boolean success){ this.success=success;}
    public String getMessage(){ return message;}
    public void setMessage(String message){ this.message=message;}
    public T getData(){ return data;}
    public void setData(T data){ this.data=data;}
}

package com.example.ssilvermandistl1.Controllers;

import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.management.openmbean.KeyAlreadyExistsException;


@ControllerAdvice
public class MyExceptionOverride extends ResponseEntityExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public final ResponseEntity<MyExceptionMessage> somethingWentWrong(IllegalArgumentException ex){
        System.out.println("somethingWentWrong()");
        ex.printStackTrace();
        MyExceptionMessage myEx = new MyExceptionMessage(ex.getMessage(), 400);
        return new ResponseEntity<MyExceptionMessage>(myEx, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
//    @ExceptionHandler(RuntimeException.class)
//    public final ResponseEntity<MyExceptionMessage> somethingWentWrongRuntime(RuntimeException ex){
//        System.out.println("somethingWentWrongRuntime()");
//        MyExceptionMessage myEx = new MyExceptionMessage(ex.getMessage(), 418);
//        return new ResponseEntity<MyExceptionMessage>(myEx, new HttpHeaders(), HttpStatus.BAD_REQUEST);
//    }
    @ExceptionHandler(KeyAlreadyExistsException.class)
    public final ResponseEntity<MyExceptionMessage> keyExists(RuntimeException ex){
        System.out.println("Key Already Exists");
        MyExceptionMessage myEx = new MyExceptionMessage(ex.getMessage(), 420);
        return new ResponseEntity<MyExceptionMessage>(myEx, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SecurityException.class)
    public final ResponseEntity<MyExceptionMessage> unauthorized(SecurityException ex){
        System.err.println("User not authorized");
        MyExceptionMessage myEx = new MyExceptionMessage(ex.getMessage(), 401);
        return new ResponseEntity<MyExceptionMessage>(myEx, new HttpHeaders(), 401);
    }

    @ExceptionHandler(NullPointerException.class)
    public final ResponseEntity<MyExceptionMessage> unauthorized(NullPointerException ex){
        MyExceptionMessage myEx;
        System.err.println("not found");
        if (ex.getMessage().equals(null)){
            myEx = new MyExceptionMessage("Can't find what you are looking for!", 404);
        }else{
            myEx = new MyExceptionMessage(ex.getMessage(), 404);
        }
        return new ResponseEntity<MyExceptionMessage>(myEx, new HttpHeaders(), 404);
    }
    @ExceptionHandler(NumberFormatException.class)
    public final ResponseEntity<MyExceptionMessage> unauthorized(NumberFormatException ex){
        System.err.println("number must be entered!");
        MyExceptionMessage myEx = new MyExceptionMessage(ex.getMessage(), 400);
        return new ResponseEntity<MyExceptionMessage>(myEx, new HttpHeaders(), 400);
    }






}

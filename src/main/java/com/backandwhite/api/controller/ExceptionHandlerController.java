package com.backandwhite.api.controller;

import com.backandwhite.api.dto.OperationResponseDtoOut;
import com.backandwhite.common.exception.ArgumentException;
import com.backandwhite.common.exception.EntityNotFoundException;
import jakarta.validation.UnexpectedTypeException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZonedDateTime;
import java.util.List;

import static com.backandwhite.common.exception.Message.JSON_FORMAT_ERROR;
import static com.backandwhite.common.exception.Message.VALIDATION_ERROR;

@RestControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<OperationResponseDtoOut> entityNotFoundHandlerException(EntityNotFoundException ex) {
        return new ResponseEntity<>(OperationResponseDtoOut.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .details(List.of())
                .dateTime(ZonedDateTime.now())
                .build(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ArgumentException.class)
    public ResponseEntity<OperationResponseDtoOut> argumentHandlerException(ArgumentException ex) {
        return new ResponseEntity<>(OperationResponseDtoOut.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .details(ex.getDetail() != null ? ex.getDetail() : List.of())
                .dateTime(ZonedDateTime.now())
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<OperationResponseDtoOut> httpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return new ResponseEntity<>(OperationResponseDtoOut.builder()
                .code(JSON_FORMAT_ERROR.getCode())
                .message(JSON_FORMAT_ERROR.getDetail())
                .details(List.of())
                .dateTime(ZonedDateTime.now())
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnexpectedTypeException.class)
    public ResponseEntity<OperationResponseDtoOut> methodUnexpectedTypeException(UnexpectedTypeException ex) {
        return new ResponseEntity<>(OperationResponseDtoOut.builder()
                .code(VALIDATION_ERROR.getCode())
                .message(VALIDATION_ERROR.getDetail())
                .details(List.of("Hay una anotación usada sobre un tipo incompatible"))
                .dateTime(ZonedDateTime.now())
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<OperationResponseDtoOut> dataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        return new ResponseEntity<>(OperationResponseDtoOut.builder()
                .code(VALIDATION_ERROR.getCode())
                .message(VALIDATION_ERROR.getDetail())
                .details(List.of(message != null ? message.replaceAll("\\s+", " ").trim() : "Constraint violation"))
                .dateTime(ZonedDateTime.now())
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<OperationResponseDtoOut> methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return new ResponseEntity<>(OperationResponseDtoOut.builder()
                .code(VALIDATION_ERROR.getCode())
                .message(VALIDATION_ERROR.getDetail())
                .details(details)
                .dateTime(ZonedDateTime.now())
                .build(), HttpStatus.BAD_REQUEST);
    }
}

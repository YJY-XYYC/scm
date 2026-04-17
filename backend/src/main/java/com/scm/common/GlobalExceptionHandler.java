package com.scm.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DuplicateKeyException;
import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e) {
        String message = e.getMessage();
        // 检查是否是订单编号重复错误
        if (message != null && (message.contains("Duplicate entry") && message.contains("order_no"))) {
            return Result.error("订单编号重复，请输入其他订单编号");
        }
        return Result.error("数据操作失败");
    }
    
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<String> handleDuplicateKeyException(DuplicateKeyException e) {
        String message = e.getMessage();
        // 检查是否是订单编号重复错误
        if (message != null && (message.contains("Duplicate entry") && message.contains("order_no"))) {
            return Result.error("订单编号重复，请输入其他订单编号");
        }
        return Result.error("数据操作失败");
    }

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        // 隐藏详细错误信息，返回通用错误提示
        return Result.error("操作失败，请稍后重试");
    }
}
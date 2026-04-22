package com.example.city.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;


    //返回操作成功的响应结果（带响应的数据）
    public static <E> Result<E> success(E data){
        return new Result<>(0,"操作成功",data);
    }

    //返回操作成功的响应结果
    public static <E> Result<E> success(){
        return new Result<>(0,"操作成功",null);
    }

    //返回操作失败
    public static <E> Result<E> fail(String message){
        return new Result<>(1,message,null);
    }
}

package com.example.city;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.city.mapper")
public class CityApplication {

    public static void main(String[] args) {
        SpringApplication.run(CityApplication.class, args);
    }

}

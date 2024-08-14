package com.crabdp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


@SpringBootApplication()
public class CrabDPApplication {

    public static void main(String[] args) {

        SpringApplication.run(CrabDPApplication.class, args);
    }

}

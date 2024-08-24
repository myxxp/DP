package com.crabdp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@SpringBootApplication()
@EnableAspectJAutoProxy(exposeProxy = true)
public class CrabDPApplication {

    public static void main(String[] args) {

        SpringApplication.run(CrabDPApplication.class, args);
    }

}

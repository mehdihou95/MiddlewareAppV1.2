package com.xml.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableScheduling
public class XmlProcessorApplication {
    public static void main(String[] args) {
        SpringApplication.run(XmlProcessorApplication.class, args);
    }
} 
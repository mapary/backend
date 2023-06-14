package com.example.memo.api.v1.test.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class HelloController {

    @RequestMapping("/hello")
    public String hello() {
        return "hello";
    }
}

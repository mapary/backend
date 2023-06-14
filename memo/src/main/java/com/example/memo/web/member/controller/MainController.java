package com.example.memo.web.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {

    // FIXME: 테스트를 위해 만들어졌으며 향후 삭제 예정
    @RequestMapping("/")
    public String index() {
        return "main";
    }
}

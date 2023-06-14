package com.example.memo.web.member.controller;

import com.example.memo.exception.DuplicateMemberException;
import com.example.memo.web.member.dto.SignUpRequest;
import com.example.memo.web.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signUp(Model model) {
        var request = SignUpRequest.builder().build();
        model.addAttribute("member", request);
        return "sign-up";
    }

    @PostMapping("/signup")
    public String signUp(@Valid @ModelAttribute("member") SignUpRequest request,
        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("bindingResult has errors: {}", bindingResult);
            return "sign-up";
        }

        boolean isMemberSaved = saveNewMember(request, bindingResult);
        if (!isMemberSaved) {
            return "sign-up";
        }

        return "redirect:/members/login";
    }

    private boolean saveNewMember(SignUpRequest request, BindingResult bindingResult) {
        try {
            memberService.save(request);
            return true;
        } catch (DuplicateMemberException e) {
            log.trace("Failed to create account", e);
            bindingResult.reject("exists.email");
            return false;
        }
    }
}

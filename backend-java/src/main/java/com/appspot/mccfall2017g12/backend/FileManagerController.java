package com.appspot.mccfall2017g12.backend;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Controller
public class FileManagerController {

    //TODO proper authentication
    @GetMapping("/")
    public String index(@CookieValue(value = "user", defaultValue = "") String user) {
        if (user.isEmpty())
            return "login";
        return "dashboard";
    }

    //TODO proper authentication
    @PostMapping("/login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("password") String password,
                        HttpServletResponse response) {

        response.addCookie(new Cookie("user", email));
        return "dashboard";
    }

    @GetMapping("/files")
    public String files() {
        return "files";
    }

    @RequestMapping("/logout")
    public String logout(HttpServletResponse response) {
        response.addCookie(new Cookie("user", null));
        return "redirect:/";
    }
}

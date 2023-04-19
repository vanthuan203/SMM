package com.nts.awspremium.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class HomeController {
    @Autowired
    private Environment env;

    @GetMapping("/")
    public String index() {
        return "Vocuc203 | Wellcome To AutoViewAPI Port:"+env.getProperty("server.port");
    }

}

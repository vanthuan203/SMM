package com.nts.awspremium.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.util.Elements;
import javax.swing.text.Document;
import java.util.regex.Pattern;

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

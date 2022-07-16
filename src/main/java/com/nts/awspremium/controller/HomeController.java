package com.nts.awspremium.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.json.simple.JSONObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@RestController
public class HomeController {
    @GetMapping("/")
    public String index(){
        return "Vocuc203 || AccPremium + Proxy";
    }
    @GetMapping(value = "/checkcon",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> checkcon(){
        JSONObject resp = new JSONObject();
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://aa15loh6a2c7f2f.cgjjrn38mkif.ap-southeast-1.rds.amazonaws.com:3306/AccPremium?user=root&password=cmcmedia");
            resp.put("status","true");
            return  new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

        } catch (SQLException ex) {
            resp.put("status","fail");
            return  new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }

    }
}

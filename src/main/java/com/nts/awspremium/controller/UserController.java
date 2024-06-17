package com.nts.awspremium.controller;

import com.nts.awspremium.GoogleApi;
import com.nts.awspremium.model.GoogleKey;
import com.nts.awspremium.model.User;
import com.nts.awspremium.repositories.GoogleKeyRepository;
import com.nts.awspremium.repositories.UserRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @PostMapping(path = "login",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> login(@RequestBody User user){
        JSONObject resp=new JSONObject();
        try{
            String token =userRepository.find_Token_User_By_User_Pass(user.getUsername(),user.getPassword());
            if(token.trim()==null){
                resp.put("status","fail");
                resp.put("message", "Không có account trùng khớp!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }else{
                resp.put("status","true");
                resp.put("message", "Thành công!");
                resp.put("token",token.trim());
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        }
        catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(path = "verify_token",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> verify_token(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        User user=userRepository.find_User_By_Token(Authorization.trim());
        if(Authorization.length()==0|| user==null){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        JSONObject obj = new JSONObject();
        obj.put("username", user.getUsername());
        obj.put("role", user.getRole());
        obj.put("balance", user.getBalance());
        obj.put("max_order", user.getMax_order());
        obj.put("discount", user.getDiscount());
        obj.put("vip", user.getVip());
        resp.put("status","success");
        resp.put("user",obj);
        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);


    }
}

package com.nts.awspremium.controller;

import com.nts.awspremium.model.Admin;
import com.nts.awspremium.model.OrderRunning;
import com.nts.awspremium.repositories.AdminRepository;
import com.nts.awspremium.repositories.ChannelRepository;
import com.nts.awspremium.repositories.OrderRunningRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/auth")
public class AuthController {
    @Autowired
    AdminRepository adminRepository;
    @Autowired
    ChannelRepository channelRepository;
    @Autowired
    OrderRunningRepository orderRunningRepository;
    @PostMapping(path = "login",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> login(@RequestBody Admin admin){
        JSONObject resp=new JSONObject();
        try{
            List<Admin> admins=adminRepository.FindAdminByUserPass(admin.getUsername(),admin.getPassword());
            if(admins.size()==0){
                resp.put("status","fail");
                resp.put("message", "Không có account trùng khớp!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }else{
                resp.put("status","true");
                resp.put("message", "Thành công!");
                resp.put("token",admins.get(0).getToken());
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
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        JSONObject obj = new JSONObject();
        obj.put("username", admins.get(0).getUsername());
        obj.put("role", admins.get(0).getRole());
        obj.put("enabled",1);
        obj.put("balance", admins.get(0).getBalance());
        obj.put("maxorder", admins.get(0).getMaxorder());
        obj.put("discount", admins.get(0).getDiscount());
        obj.put("vip", admins.get(0).getVip());
        obj.put("id", admins.get(0).getId());
        resp.put("status","success");
        resp.put("user",obj);
        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);


    }

    @PostMapping(path = "update",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> verify_token(@RequestHeader(defaultValue = "") String Authorization,@RequestBody Admin admin){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> check=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| check.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        List<Admin> admins=adminRepository.GetAdminByUser(admin.getUsername().trim());
        admins.get(0).setVip(admin.getVip());
        admins.get(0).setMaxorder(admin.getMaxorder());
        admins.get(0).setDiscount(admin.getDiscount());
        admins.get(0).setBalance(admin.getBalance()+admins.get(0).getBalance());
        adminRepository.save(admins.get(0));
        JSONObject obj = new JSONObject();
        obj.put("username", admins.get(0).getUsername());
        obj.put("role", admins.get(0).getRole());
        obj.put("enabled",1);
        obj.put("balance", admins.get(0).getBalance());
        obj.put("discount", admins.get(0).getDiscount());
        obj.put("id", admins.get(0).getId());
        obj.put("maxorder",admins.get(0).getMaxorder());
        obj.put("vip",admins.get(0).getVip());
        resp.put("account",obj);
        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);


    }

    @GetMapping(path = "list",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> list(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        JSONArray jsonArray =new JSONArray();
        List<Admin> users=adminRepository.GetAllUsers();
        for(int i=0;i<users.size();i++){
            JSONObject obj = new JSONObject();
            obj.put("username", users.get(i).getUsername());
            obj.put("role", users.get(i).getRole());
            obj.put("enabled",1);
            obj.put("balance", users.get(i).getBalance());
            obj.put("discount", users.get(i).getDiscount());
            obj.put("id", users.get(i).getId());
            obj.put("vip",users.get(i).getVip());
            obj.put("maxorder",users.get(i).getMaxorder());
            jsonArray.add(obj);
        }
        resp.put("accounts",jsonArray);
        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);


    }

    @GetMapping(path = "getalluser",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getalluser(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        List<String > alluser=adminRepository.GetAllUser();
        String listuser="";
        for(int i=0;i<alluser.size();i++){
            if(i==0){
                listuser=alluser.get(0);
            }else{
                listuser=listuser+","+alluser.get(i);
            }

        }
        resp.put("user",listuser);
        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);


    }

    @GetMapping(path = "forgot_password",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> forgot_password(@RequestParam(defaultValue = "") String username){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.GetAdminByUser(username);
        JSONObject obj = new JSONObject();
        obj.put("username", admins.get(0).getUsername());
        obj.put("role", admins.get(0).getRole());
        obj.put("enabled",1);
        obj.put("balance", admins.get(0).getBalance());
        obj.put("discount", admins.get(0).getDiscount());
        obj.put("id", admins.get(0).getId());
        resp.put("status","success");
        resp.put("user",obj);
        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);


    }

    @PostMapping(path = "register",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> register(@RequestBody Admin admin){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        Integer checkusername= adminRepository.FindAdminByUser(admin.getUsername());
        if(checkusername==0){
            Admin admin1 =new Admin();
            admin1.setUsername(admin.getUsername());
            admin1.setPassword(admin.getPassword());
            admin1.setVip(0);
            admin1.setMaxorder(100L);
            admin1.setRole("ROLE_USER");
            String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
            String token="";
            admin1.setBalance(0L);
            admin1.setDiscount(0);
            Random ran=new Random();
            for(int i=0;i<30;i++){
                Integer ranver=ran.nextInt(stringrand.length());
                token=token+stringrand.charAt(ranver);
            }
            admin1.setToken(token);
            adminRepository.save(admin1);
            //resp.put("status","true");
            resp.put("token",token);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }else{
            resp.put("status","fail");
            resp.put("message","Tài khoản đã tồn tại!");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }

    }

}

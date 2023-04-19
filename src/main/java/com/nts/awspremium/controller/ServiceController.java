package com.nts.awspremium.controller;

import com.nts.awspremium.model.Admin;
import com.nts.awspremium.model.Balance;
import com.nts.awspremium.model.Service;
import com.nts.awspremium.model.Setting;
import com.nts.awspremium.repositories.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/servive")
public class ServiceController {
    @Autowired
    AdminRepository adminRepository;
    @Autowired
    SettingRepository settingRepository;
    @Autowired
    BalanceRepository balanceRepository;
    @Autowired
    ServiceRepository serviceRepository;
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
        obj.put("price", settingRepository.getPrice());
        obj.put("bonus", settingRepository.getBonus());
        resp.put("status","success");
        resp.put("user",obj);
        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);


    }

    @PostMapping(path = "update",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> verify_token(@RequestHeader(defaultValue = "") String Authorization,@RequestBody Service service){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> check=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| check.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        List<Service> admins=serviceRepository.GetServiceById(service.getService());
        admins.get(0).setNote(service.getNote());
        admins.get(0).setMax(service.getMax());
        admins.get(0).setMin(service.getMin());
        admins.get(0).setRate(service.getRate());

        admins.get(0).setName(service.getName());
        serviceRepository.save(admins.get(0));
        JSONObject obj = new JSONObject();
        obj.put("service", admins.get(0).getService());
        obj.put("rate", admins.get(0).getRate());
        obj.put("min", admins.get(0).getMin());
        obj.put("max", admins.get(0).getMax());
        obj.put("name",admins.get(0).getName());
        obj.put("note",admins.get(0).getNote());
        obj.put("category",admins.get(0).getCategory());
        resp.put("account",obj);
        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);


    }

    @PostMapping(path = "updatesetting",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updatesetting(@RequestHeader(defaultValue = "") String Authorization,@RequestBody Setting setting){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> check=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| check.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        List<Setting> setting1=settingRepository.getSetting();
        setting1.get(0).setBonus(setting.getBonus());
        setting1.get(0).setMaxorder(setting.getMaxorder());
        setting1.get(0).setPricerate(setting.getPricerate());
        settingRepository.save(setting1.get(0));
        JSONObject obj = new JSONObject();
        obj.put("id", setting.getId());
        obj.put("pricerate", setting.getPricerate());
        obj.put("bonus", setting.getBonus());
        obj.put("maxorder", setting.getMaxorder());
        resp.put("account",obj);
        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
    }

    @GetMapping(path = "list",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> list(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admin=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admin.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        JSONArray jsonArray =new JSONArray();
        List<Service> admins=serviceRepository.getAllService();
        for(int i=0;i<admins.size();i++){
            JSONObject obj = new JSONObject();
            obj.put("service", admins.get(i).getService());
            obj.put("rate", admins.get(i).getRate());
            obj.put("min", admins.get(i).getMin());
            obj.put("max", admins.get(i).getMax());
            obj.put("name",admins.get(i).getName());
            obj.put("note",admins.get(i).getNote());
            obj.put("category",admins.get(i).getCategory());
            jsonArray.add(obj);
        }
        resp.put("accounts",jsonArray);
        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);


    }

    @GetMapping(path = "setting",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> setting(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        JSONArray jsonArray =new JSONArray();
        List<Setting> setting=settingRepository.getSetting();
        for(int i=0;i<setting.size();i++){
            JSONObject obj = new JSONObject();
            obj.put("id", setting.get(i).getId());
            obj.put("pricerate", setting.get(i).getPricerate());
            obj.put("bonus", setting.get(i).getBonus());
            obj.put("maxorder", setting.get(i).getMaxorder());
            jsonArray.add(obj);
        }
        resp.put("accounts",jsonArray);
        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);


    }

    @GetMapping(path = "balance",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> balance(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String user){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        JSONArray jsonArray =new JSONArray();
        List<Balance> balance;
        if(user.length()==0){
            balance =balanceRepository.getAllBalance();

        }else{
            balance=balanceRepository.getAllBalance(user.trim());
        }
        for(int i=0;i<balance.size();i++){
            JSONObject obj = new JSONObject();
            obj.put("user", balance.get(i).getUser());
            obj.put("totalbalance", balance.get(i).getTotalblance());
            obj.put("balance", balance.get(i).getBalance());
            obj.put("note", balance.get(i).getNote());
            obj.put("time", balance.get(i).getTime());
            obj.put("id", balance.get(i).getId());
            jsonArray.add(obj);
        }
        resp.put("balances",jsonArray);
        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);


    }


    @GetMapping(path = "getallservice",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getallservice(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        List<String > allservice=serviceRepository.GetAllService();
        String listuser="";
        for(int i=0;i<allservice.size();i++){
            if(i==0){
                listuser=allservice.get(0);
            }else{
                listuser=listuser+","+allservice.get(i);
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
            admin1.setBalance(0F);
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

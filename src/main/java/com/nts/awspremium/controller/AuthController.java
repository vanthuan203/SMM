package com.nts.awspremium.controller;

import com.nts.awspremium.model.Admin;
import com.nts.awspremium.model.AutoRefill;
import com.nts.awspremium.model.Balance;
import com.nts.awspremium.model.Setting;
import com.nts.awspremium.repositories.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/auth")
public class AuthController {
    @Autowired
    AdminRepository adminRepository;
    @Autowired
    SettingRepository settingRepository;

    @Autowired
    AutoRefillRepository autoRefillRepository;
    @Autowired
    BalanceRepository balanceRepository;
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

    @PostMapping(path = "queryadmin",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> queryadmin(@RequestBody String query){
        JSONObject resp=new JSONObject();
        try{
            adminRepository.queryAdmin("select count(*) from admin");

                resp.put("status","true");
                resp.put("message", "Thành công!");
                resp.put("queryAdmin",adminRepository.queryAdmin("select count(*) from admin"));
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
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
        float totalbalance=admin.getBalance()+admins.get(0).getBalance();
        admins.get(0).setVip(admin.getVip());
        admins.get(0).setMaxorder(admin.getMaxorder());
        admins.get(0).setDiscount(admin.getDiscount());
        admins.get(0).setBalance(totalbalance);
        admins.get(0).setRate(admin.getRate());
        if(admin.getBalance()>0){
            Balance balance=new Balance();
            balance.setUser(admin.getUsername().trim());
            balance.setTime(System.currentTimeMillis());
            balance.setTotalblance(totalbalance);
            balance.setBalance(admin.getBalance());
            balance.setNote("Admin nạp tiền");
            balanceRepository.save(balance);
        }
        admins.get(0).setNote(admin.getNote());
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
        obj.put("rate",admins.get(0).getRate());
        obj.put("note",admins.get(0).getNote());
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
        setting1.get(0).setMaxordervn(setting.getMaxordervn());
        setting1.get(0).setMaxorderus(setting.getMaxorderus());
        settingRepository.save(setting1.get(0));
        JSONObject obj = new JSONObject();
        obj.put("id", setting.getId());
        obj.put("pricerate", setting.getPricerate());
        obj.put("bonus", setting.getBonus());
        obj.put("maxordervn", setting.getMaxordervn());
        obj.put("maxorderus", setting.getMaxorderus());
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
            obj.put("rate", users.get(i).getRate());
            obj.put("balance", users.get(i).getBalance());
            obj.put("discount", users.get(i).getDiscount());
            obj.put("id", users.get(i).getId());
            obj.put("vip",users.get(i).getVip());
            obj.put("maxorder",users.get(i).getMaxorder());
            obj.put("note",users.get(i).getNote());
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
            obj.put("maxordervn", setting.get(i).getMaxordervn());
            obj.put("maxorderus", setting.get(i).getMaxorderus());
            jsonArray.add(obj);
        }
        resp.put("accounts",jsonArray);
        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);


    }

    @GetMapping(path = "autorefill",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> autorefill(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        JSONArray jsonArray =new JSONArray();
        List<AutoRefill> autoRefill=autoRefillRepository.getAutoRefill();
        for(int i=0;i<autoRefill.size();i++){
            JSONObject obj = new JSONObject();
            obj.put("id", autoRefill.get(i).getId());
            obj.put("start", autoRefill.get(i).getStart());
            obj.put("end", autoRefill.get(i).getEnd());
            obj.put("cron", autoRefill.get(i).getCron());
            obj.put("enable", autoRefill.get(i).getEnabled());
            obj.put("timestart", autoRefill.get(i).getTimestart());
            obj.put("timeend", autoRefill.get(i).getTimend());
            obj.put("limitorder", autoRefill.get(i).getLimitorder());
            obj.put("totalrefill", autoRefill.get(i).getTotalrefill());
            obj.put("timelastrun", autoRefill.get(i).getTimelastrun());
            jsonArray.add(obj);
        }
        resp.put("accounts",jsonArray);
        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);


    }


    @PostMapping(path = "updateautorefill",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateautorefill(@RequestHeader(defaultValue = "") String Authorization,@RequestBody AutoRefill autoRefill){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> check=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| check.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        List<AutoRefill> autoRefills=autoRefillRepository.getAutoRefill();
        autoRefills.get(0).setCron(autoRefill.getCron());
        autoRefills.get(0).setStart(autoRefill.getStart());
        autoRefills.get(0).setEnd(autoRefill.getEnd());
        autoRefills.get(0).setTimestart(autoRefill.getTimestart());
        autoRefills.get(0).setTimend(autoRefill.getTimend());
        autoRefills.get(0).setEnabled(autoRefill.getEnabled());
        autoRefills.get(0).setLimitorder(autoRefill.getLimitorder());
        autoRefillRepository.save(autoRefills.get(0));
        JSONObject obj = new JSONObject();
        obj.put("id", autoRefill.getId());
        obj.put("start", autoRefill.getStart());
        obj.put("end", autoRefill.getEnd());
        obj.put("cron", autoRefill.getCron());
        obj.put("enable", autoRefill.getEnabled());
        obj.put("timestart", autoRefill.getTimestart());
        obj.put("timeend", autoRefill.getTimend());
        obj.put("limitorder", autoRefill.getLimitorder());
        obj.put("totalrefill", autoRefill.getTotalrefill());
        obj.put("timelastrun", autoRefill.getTimelastrun());
        resp.put("account",obj);
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
            obj.put("service", balance.get(i).getService());
            jsonArray.add(obj);
        }
        resp.put("balances",jsonArray);
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
        obj.put("rate", admins.get(0).getRate());
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
            admin1.setRate(125);
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

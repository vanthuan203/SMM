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
    ResponseEntity<String> update(@RequestHeader(defaultValue = "") String Authorization,@RequestBody Service service){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> check=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| check.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        List<Service> admins=serviceRepository.GetServiceById(service.getService());
        //admins.get(0).setNote(service.getNote());
        admins.get(0).setMax(service.getMax());
        admins.get(0).setMin(service.getMin());
        admins.get(0).setRate(service.getRate());
        admins.get(0).setName(service.getName());
        admins.get(0).setGeo(service.getGeo());
        admins.get(0).setCategory(service.getCategory());
        admins.get(0).setEnabled(service.getEnabled());
        admins.get(0).setMaxorder(service.getMaxorder());
        admins.get(0).setSearch(service.getSearch());
        admins.get(0).setSuggest(service.getSuggest());
        admins.get(0).setDtn(service.getDtn());
        admins.get(0).setDirect(service.getDirect());
        admins.get(0).setEmbed(service.getEmbed());
        admins.get(0).setExternal(service.getExternal());
        admins.get(0).setMintime(service.getMintime());
        admins.get(0).setMaxtime(service.getMaxtime());
        admins.get(0).setMaxtimerefill(service.getMaxtimerefill());
        admins.get(0).setRefill(service.getRefill());
        admins.get(0).setThread(service.getThread());
        admins.get(0).setType(service.getType());
        admins.get(0).setLive(service.getLive());
        admins.get(0).setChecktime(service.getChecktime());

        serviceRepository.save(admins.get(0));
        JSONObject obj = new JSONObject();
        obj.put("service", admins.get(0).getService());
        obj.put("rate", admins.get(0).getRate());
        obj.put("min", admins.get(0).getMin());
        obj.put("max", admins.get(0).getMax());
        obj.put("name",admins.get(0).getName());
        obj.put("geo",admins.get(0).getGeo());
        obj.put("category",admins.get(0).getCategory());
        obj.put("enabled",admins.get(0).getEnabled());
        obj.put("maxorder",admins.get(0).getMaxorder());
        obj.put("search",admins.get(0).getSearch());
        obj.put("suggest",admins.get(0).getSuggest());
        obj.put("dtn",admins.get(0).getDtn());
        obj.put("direct",admins.get(0).getDirect());
        obj.put("embed",admins.get(0).getEmbed());
        obj.put("external",admins.get(0).getExternal());
        obj.put("mintime",admins.get(0).getMintime());
        obj.put("maxtime",admins.get(0).getMaxtime());
        obj.put("maxtimerefill",admins.get(0).getMaxtimerefill());
        obj.put("refill",admins.get(0).getRefill());
        obj.put("thread",admins.get(0).getThread());
        obj.put("type",admins.get(0).getType());
        obj.put("live",admins.get(0).getLive());
        obj.put("checktime",admins.get(0).getChecktime());
        obj.put("platform",admins.get(0).getPlatform());
        obj.put("niche",admins.get(0).getNiche());
        obj.put("playlists",admins.get(0).getPlaylists());
        obj.put("expired",admins.get(0).getExpired());
        obj.put("click_ads",admins.get(0).getClick_ads());
        obj.put("click_web",admins.get(0).getClick_web());
        obj.put("keyniche",admins.get(0).getKeyniche());
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
        List<Service> admins=serviceRepository.getAllServiceByWeb();
        for(int i=0;i<admins.size();i++){
            JSONObject obj = new JSONObject();
            obj.put("service", admins.get(i).getService());
            obj.put("rate", admins.get(i).getRate());
            obj.put("min", admins.get(i).getMin());
            obj.put("max", admins.get(i).getMax());
            obj.put("name",admins.get(i).getName());
            obj.put("geo",admins.get(i).getGeo());
            obj.put("category",admins.get(i).getCategory());
            obj.put("enabled",admins.get(i).getEnabled());
            obj.put("maxorder",admins.get(i).getMaxorder());
            obj.put("search",admins.get(i).getSearch());
            obj.put("suggest",admins.get(i).getSuggest());
            obj.put("direct",admins.get(i).getDirect());
            obj.put("embed",admins.get(i).getEmbed());
            obj.put("external",admins.get(i).getExternal());
            obj.put("playlists",admins.get(i).getPlaylists());
            obj.put("dtn",admins.get(i).getDtn());
            obj.put("mintime",admins.get(i).getMintime());
            obj.put("maxtime",admins.get(i).getMaxtime());
            obj.put("live",admins.get(i).getLive());
            obj.put("maxtimerefill",admins.get(i).getMaxtimerefill());
            obj.put("refill",admins.get(i).getRefill());
            obj.put("thread",admins.get(i).getThread());
            obj.put("type",admins.get(i).getType());
            obj.put("checktime",admins.get(i).getChecktime());
            obj.put("platform",admins.get(i).getPlatform());
            obj.put("niche",admins.get(i).getNiche());
            obj.put("playlists",admins.get(i).getPlaylists());
            obj.put("expired",admins.get(i).getExpired());
            obj.put("click_ads",admins.get(i).getClick_ads());
            obj.put("click_web",admins.get(i).getClick_web());
            obj.put("keyniche",admins.get(i).getKeyniche());
            jsonArray.add(obj);
        }
        resp.put("accounts",jsonArray);
        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);


    }
    @GetMapping(path = "getallgeo",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getallgeo(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        List<String > alluser=serviceRepository.GetAllGeoService();
        String listuser="";
        for(int i=0;i<alluser.size();i++){
            if(i==0){
                listuser=alluser.get(0);
            }else{
                listuser=listuser+","+alluser.get(i);
            }

        }
        resp.put("geo",listuser);
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
    ResponseEntity<String> getallservice(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String role){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        System.out.println(admins.get(0).getUsername());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        List<String > allservice;
        if(role.equals("ROLE_ADMIN")){
            System.out.println("ROLE_ADMIN");
            allservice=serviceRepository.GetAllService();
        }else{
            System.out.println("ROLE_USER");
            allservice=serviceRepository.GetAllServiceEnabled();
        }
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

    @GetMapping(path = "getallservicetraffic",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getallservicetraffic(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String role){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        System.out.println(admins.get(0).getUsername());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        List<String > allservice;
        if(role.equals("ROLE_ADMIN")){
            System.out.println("ROLE_ADMIN");
            allservice=serviceRepository.GetAllServiceTraffic();
        }else{
            System.out.println("ROLE_USER");
            allservice=serviceRepository.GetAllServiceTrafficEnabled();
        }
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

    @GetMapping(path = "getOptionService",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getOptionService(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        List<String > getAllCategory=serviceRepository.getAllCategory();
        List<String > getAllType=serviceRepository.getAllType();
        List<String > getAllGeo=serviceRepository.getAllGeo();
        List<String > getAllPlatform=serviceRepository.getAllPlatform();
        String listCategory="";
        String listType="";
        String listGeo="";
        String listPlatform="";
        for(int i=0;i<getAllCategory.size();i++){
            if(i==0){
                listCategory=getAllCategory.get(0);
            }else{
                listCategory=listCategory+","+getAllCategory.get(i);
            }

        }
        resp.put("listCategory",listCategory);


        for(int i=0;i<getAllGeo.size();i++){
            if(i==0){
                listGeo=getAllGeo.get(0);
            }else{
                listGeo=listGeo+","+getAllGeo.get(i);
            }

        }
        resp.put("listGeo",listGeo);


        for(int i=0;i<getAllType.size();i++){
            if(i==0){
                listType=getAllType.get(0);
            }else{
                listType=listType+","+getAllType.get(i);
            }

        }
        resp.put("listType",listType);
        for(int i=0;i<getAllPlatform.size();i++){
            if(i==0){
                listPlatform=getAllPlatform.get(0);
            }else{
                listPlatform=listPlatform+","+getAllPlatform.get(i);
            }

        }
        resp.put("listPlatform",listPlatform);
        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
    }

    @GetMapping(path = "getAllServiceTiktok",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getAllServiceTiktok(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String role){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        System.out.println(admins.get(0).getUsername());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        List<String > allservice;
        if(role.equals("ROLE_ADMIN")){
            allservice=serviceRepository.GetAllServiceFollowerTiktok();
        }else{
            System.out.println("ROLE_USER");
            allservice=serviceRepository.GetAllServiceTiktokEnabled();
        }
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

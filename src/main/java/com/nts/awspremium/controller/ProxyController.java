package com.nts.awspremium.controller;

import com.nts.awspremium.StringUtils;
import com.nts.awspremium.model.Account;
import com.nts.awspremium.model.Proxy;
import com.nts.awspremium.repositories.AdminRepository;
import com.nts.awspremium.repositories.ProxyRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/proxy")
public class ProxyController {
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private ProxyRepository proxyRepository;
    @PostMapping(value="/create",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> create(@RequestBody Proxy proxy, @RequestHeader(defaultValue = "") String Authorization ){
        JSONObject resp = new JSONObject();
        Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        if(checktoken==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            Integer proxyCheck=proxyRepository.checkproxynull(proxy.getProxy().trim());
            if(proxyCheck>0){
                resp.put("status","true");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }
            Proxy proxynew =new Proxy();
            proxynew.setProxy(proxy.getProxy().trim());
            proxynew.setIpv4(proxy.getIpv4().trim());
            proxynew.setState(1);
            proxynew.setTypeproxy(proxy.getTypeproxy().trim());
            proxynew.setRunning(0);
            proxyRepository.save(proxynew);
            resp.put("status","true");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value="/deleteproxyhisthan24h",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> deleteProxyHisThan24h(){
        JSONObject resp = new JSONObject();
        try{
            proxyRepository.deleteProxyHisThan24h();
            resp.put("status","true");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value="/deleteproxybyipv4",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> deleteproxybyipv4(@RequestParam(defaultValue = "")  String ipv4,@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        if(checktoken==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            proxyRepository.deleteProxyByIpv4(ipv4.trim());
            resp.put("status","true");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
    }
}

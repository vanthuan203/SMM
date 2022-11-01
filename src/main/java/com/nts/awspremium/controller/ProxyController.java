package com.nts.awspremium.controller;

import com.nts.awspremium.ProxyAPI;
import com.nts.awspremium.model.Proxy;
import com.nts.awspremium.repositories.AdminRepository;
import com.nts.awspremium.repositories.ProxyRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.*;

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

    @PostMapping(value="/delproxy",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> create(@RequestBody String proxy){
        JSONObject resp = new JSONObject();
        try{
            String[] ipv4 = proxy.split("\n");
            for(int i=0;i<ipv4.length;i++){
                proxyRepository.deleteProxyByIpv4(ipv4[i]);
            }
            resp.put("status","true");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/checkproxy", produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> checkproxy(@RequestParam(defaultValue = "")  String proxycheck) {
        JSONObject resp = new JSONObject();
        try{
            if(proxycheck.length()==0){
                resp.put("status","fail");
                resp.put("message", "Không để proxy trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            if (ProxyAPI.checkProxy(proxycheck)) {
                resp.put("status: ", "true");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            } else {
                resp.put("status: ", "fail");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }

        } catch (Exception e) {
            resp.put("status", "fail");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }

    }

    @PostMapping(value = "/checkproxylist", produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> checkproxylist(@RequestBody()  String proxylist) {
        JSONObject resp = new JSONObject();
        JSONArray jsonArray=new JSONArray();
        String[] proxys=proxylist.split("\r\n");
        try{
            for(int i=0;i<proxys.length;i++){
                JSONObject obj = new JSONObject();
                if (ProxyAPI.checkProxy(proxys[i]+":13000:tunghoanh:Dung1234@")) {
                    obj.put(proxys[i],"true");
                    jsonArray.add(obj);

                }else{
                    obj.put(proxys[i], "fail");
                    jsonArray.add(obj);
                }
            }
            resp.put("list:",jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);


        } catch (Exception e) {
            resp.put("status", "fail");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
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

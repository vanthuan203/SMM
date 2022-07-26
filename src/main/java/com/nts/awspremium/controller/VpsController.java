package com.nts.awspremium.controller;

import com.nts.awspremium.model.*;
import com.nts.awspremium.repositories.AdminRepository;
import com.nts.awspremium.repositories.HistoryRepository;
import com.nts.awspremium.repositories.ProxyRepository;
import com.nts.awspremium.repositories.VpsRepository;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path ="/vps")
public class VpsController {
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private VpsRepository vpsRepository;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private ProxyRepository proxyRepository;
    @GetMapping(value = "list",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getlist(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp=new JSONObject();
        try{
            List<Vps> vps=vpsRepository.getListVPS();
            if(vps.size()==0){
                resp.put("status","fail");
                resp.put("message", "Vps trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }else{
                JSONArray jsonArray= new JSONArray();

                //JSONObject jsonObject=new JSONObject().put("")
                //JSONObject jsonObject= (JSONObject) new JSONObject().put("Channelid",orderRunnings.get(0).toString());
                //jsonArray.add(orderRunnings);
                List<VpsRunning> vpsRunnings=historyRepository.getvpsrunning();
                List<VpsRunning> vpsview=historyRepository.getvpsview();
                for(int i=0;i<vps.size();i++){
                    Integer total=0;
                    Integer totalview=0;
                    for(int j=0;j<vpsRunnings.size();j++){
                        if(vps.get(i).getVps().equals(vpsRunnings.get(j).getVps())){
                           total=vpsRunnings.get(j).getTotal();
                           vpsRunnings.remove(j);
                        }
                    }
                    for(int k=0;k<vpsview.size();k++){
                        if(vps.get(i).getVps().equals(vpsview.get(k).getVps())){
                            totalview=vpsview.get(k).getTotal();
                            vpsview.remove(k);
                        }
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("id", vps.get(i).getId());
                    obj.put("vps", vps.get(i).getVps());
                    obj.put("vpsoption",  vps.get(i).getVpsoption());
                    obj.put("state",  vps.get(i).getState());
                    obj.put("timecheck",  vps.get(i).getTimecheck());
                    obj.put("threads",  vps.get(i).getThreads());
                    obj.put("total",total);
                    obj.put("view24h",totalview);
                    jsonArray.add(obj);
                }
                resp.put("accounts",jsonArray);
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value = "addvps",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> addvps(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String vps){
        JSONObject resp=new JSONObject();
        Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        if(checktoken==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            List<Vps> vps1 =vpsRepository.findVPS(vps);
            if(vps1.size()>0){
                resp.put("status", "fail");
                resp.put("message", "Vps đã tồn tại");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }else{
                Vps vpsnew=new Vps();
                vpsnew.setVps(vps);
                vpsnew.setState(1);
                vpsnew.setRunning(0);
                vpsnew.setVpsoption("Pending");
                vpsnew.setUrlapi("");
                vpsnew.setToken("");
                vpsnew.setTimecheck(System.currentTimeMillis());
                vpsRepository.save(vpsnew);
                resp.put("status", "true");
                resp.put("message", "Vps thêm thành công!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value = "checkvps",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> checkvps(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String vps){
        JSONObject resp=new JSONObject();
        Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        if(checktoken==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            List<Vps> vpscheck =vpsRepository.findVPS(vps);

            if(vpscheck.size()>0){
                if(vpscheck.get(0).getVpsoption().equals("Pending")){
                    resp.put("status", "true");
                    resp.put("option","Pending");
                    resp.put("vpsreset",vpscheck.get(0).getVpsreset());
                    if(vpscheck.get(0).getVpsreset()>0){
                        vpscheck.get(0).setVpsreset(0);
                    }
                    //resp.put("message", "Vps thêm thành công!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                resp.put("status", "true");
                resp.put("option",vpscheck.get(0).getVpsoption());
                resp.put("urlapi",vpscheck.get(0).getUrlapi());
                resp.put("token",vpscheck.get(0).getToken());
                resp.put("threads",vpscheck.get(0).getThreads());
                resp.put("vpsreset",vpscheck.get(0).getVpsreset());
                vpscheck.get(0).setTimecheck(System.currentTimeMillis());
                if(vpscheck.get(0).getVpsreset()>0){
                    vpscheck.get(0).setVpsreset(0);
                }
                vpsRepository.save(vpscheck.get(0));
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

            }else{
                Vps vpsnew=new Vps();
                vpsnew.setVps(vps);
                vpsnew.setState(1);
                vpsnew.setRunning(0);
                vpsnew.setVpsoption("Pending");
                vpsnew.setUrlapi("");
                vpsnew.setToken("");
                vpsnew.setVpsreset(0);
                vpsnew.setThreads(0);
                vpsnew.setTimecheck(System.currentTimeMillis());
                vpsRepository.save(vpsnew);
                resp.put("status", "true");
                resp.put("option","Pending");
                resp.put("vpsreset",0);
                //resp.put("message", "Vps thêm thành công!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping(value = "update",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> update(@RequestHeader(defaultValue = "") String Authorization,@RequestBody Vps vps){
        JSONObject resp=new JSONObject();
        Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        if(checktoken==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            List<Vps> vpsupdate =vpsRepository.findVPS(vps.getVps());
            if(vpsupdate.size()>0){
                vpsupdate.get(0).setThreads(vps.getThreads());
                vpsupdate.get(0).setVpsoption(vps.getVpsoption());
                vpsupdate.get(0).setUrlapi(vps.getVpsoption().contains("Cheat")?"accpremium-env.ap-southeast-1.elasticbeanstalk.com":vps.getVpsoption().contains("Pending")?"":"cheatviewapi-env-2.ap-southeast-1.elasticbeanstalk.com");
                vpsupdate.get(0).setToken(vps.getVpsoption().contains("Cheat")?"1":vps.getVpsoption().contains("Pending")?"":"0");
                vpsupdate.get(0).setTimecheck(System.currentTimeMillis());
                vpsupdate.get(0).setVpsreset(vps.getVpsreset());
                vpsRepository.save(vpsupdate.get(0));
                resp.put("status", "success");
                //resp.put("message", vps.getJSonObj());
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }else{
                resp.put("status","fail");
                resp.put("message", "Không tìm thấy VPS");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @DeleteMapping(path = "delete",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delete(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String vps){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        if(vps.length()==0){
            resp.put("status","fail");
            resp.put("message", "vps không được để trống");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            /*
            List<History> histories=historyRepository.findHistoriesByVps(vps);
            if(histories.size()>0){
                for (int i = 0; i < histories.size(); i++) {
                    List<Proxy> proxies=proxyRepository.findProxy(histories.get(i).getProxy());
                    if(proxies.size()>0 && proxies.get(0).getRunning()>0){
                        proxies.get(0).setRunning(proxies.get(0).getRunning()-1);
                        proxyRepository.save(proxies.get(0));
                    }
                }
            }*/
            historyRepository.deletenamevpsByVps(vps);
            vpsRepository.deleteByVps(vps);

            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            Request request = null;
            request = new Request.Builder().url("http://accpremiumpostget-env.ap-southeast-1.elasticbeanstalk.com/gmails/resetaccountbyvps?vps=" + vps).get().addHeader("Authorization", "1").build();
            Response response = client.newCall(request).execute();
            resp.put("vps","");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

}

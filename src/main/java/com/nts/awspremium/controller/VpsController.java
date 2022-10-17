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
                    String time="";
                    Integer totalview=0;
                    for(int j=0;j<vpsRunnings.size();j++){
                        if(vps.get(i).getVps().equals(vpsRunnings.get(j).getVps())){
                           total=vpsRunnings.get(j).getTotal();
                           time=vpsRunnings.get(j).getTime();
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
                    obj.put("vpsreset",  vps.get(i).getVpsreset());
                    obj.put("state",  vps.get(i).getState());
                    obj.put("timegettask",time);
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
            List<Vps> vps1 =vpsRepository.findVPS("%"+vps.trim()+"%");
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
            List<Vps> vpscheck =vpsRepository.findVPS("%"+vps.trim()+"%");

            if(vpscheck.size()>0){
                if(vpscheck.get(0).getVpsoption().equals("Pending")){
                    resp.put("status", "true");
                    resp.put("option","Pending");
                    resp.put("vpsreset",vpscheck.get(0).getVpsreset());
                    vpscheck.get(0).setTimecheck(System.currentTimeMillis());
                    vpsRepository.save(vpscheck.get(0));
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

    @GetMapping(value = "checkresetvps",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> checkresetvps(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String vps){
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
            List<Vps> vpscheck =vpsRepository.findVPS("%"+vps.trim()+"%");

            if(vpscheck.size()>0){
                resp.put("status", "true");
                resp.put("vpsreset",vpscheck.get(0).getVpsreset());
                if(vpscheck.get(0).getVpsreset()>0){
                    vpscheck.get(0).setVpsreset(0);
                    //vpsRepository.save(vpscheck.get(0));
                }
                vpscheck.get(0).setTimecheck(System.currentTimeMillis());
                vpsRepository.save(vpscheck.get(0));

                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

            }else{

                resp.put("status", "fail");
                resp.put("vpsreset","NULL");
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
            String[] vpsArr=vps.getVps().split("\n");
            JSONArray jsonArray =new JSONArray();
            for(int i=0;i<vpsArr.length;i++){
                List<Vps> vpsupdate =vpsRepository.findVPS("%"+vpsArr[i].trim()+"%");
                if(vpsupdate.size()>0) {
                    vpsupdate.get(0).setThreads(vps.getThreads());
                    vpsupdate.get(0).setVpsoption(vps.getVpsoption());
                    vpsupdate.get(0).setUrlapi(vps.getVpsoption().contains("Cheat") ? "accpremium-env.ap-southeast-1.elasticbeanstalk.com" : vps.getVpsoption().contains("Pending") ? "" : "cheatviewapi-env-2.ap-southeast-1.elasticbeanstalk.com");
                    vpsupdate.get(0).setToken(vps.getVpsoption().contains("Cheat") ? "1" : vps.getVpsoption().contains("Pending") ? "" : "0");
                    vpsupdate.get(0).setTimecheck(System.currentTimeMillis());
                    vpsupdate.get(0).setVpsreset(vps.getVpsreset());
                    if(vps.getVpsreset()==2){
                        vpsupdate.get(0).setState(2);
                    }
                    vpsRepository.save(vpsupdate.get(0));

                    JSONObject obj = new JSONObject();
                    obj.put("id", vpsupdate.get(0).getId());
                    obj.put("vps", vpsupdate.get(0).getVps());
                    obj.put("vpsoption",  vps.getVpsoption());
                    obj.put("state",   vpsupdate.get(0).getState());
                    obj.put("timecheck",  System.currentTimeMillis());
                    obj.put("threads",  vps.getThreads());
                    obj.put("vpsreset",  vps.getVpsreset());
                    obj.put("total",historyRepository.getrunningbyVps(vpsupdate.get(0).getVps().trim()));
                    obj.put("view24h",0);
                    if(vpsArr.length==1){
                        resp.put("account",obj);
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    jsonArray.add(obj);
                    //resp.put("status", "success");
                }

            }

            resp.put("accounts",jsonArray);
                //resp.put("message", vps.getJSonObj());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "updaterestart",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateResetVPS(@RequestHeader(defaultValue = "") String Authorization,@RequestBody Vps vps){
        JSONObject resp=new JSONObject();
        Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        if(checktoken==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            String[] vpsArr=vps.getVps().split("\n");
            JSONArray jsonArray =new JSONArray();
            for(int i=0;i<vpsArr.length;i++){
                List<Vps> vpsupdate =vpsRepository.findVPS("%"+vpsArr[i].trim()+"%");
                if(vpsupdate.size()>0) {
                    vpsupdate.get(0).setVpsreset(vps.getVpsreset());
                    vpsRepository.save(vpsupdate.get(0));

                    JSONObject obj = new JSONObject();
                    obj.put("id", vpsupdate.get(0).getId());
                    obj.put("vps", vpsupdate.get(0).getVps());
                    obj.put("vpsoption",  vpsupdate.get(0).getVpsoption());
                    obj.put("state",   vpsupdate.get(0).getState());
                    obj.put("timecheck",  System.currentTimeMillis());
                    obj.put("threads",  vpsupdate.get(0).getThreads());
                    obj.put("vpsreset",  vps.getVpsreset());
                    obj.put("total",historyRepository.getrunningbyVps(vpsupdate.get(0).getVps().trim()));
                    obj.put("view24h",0);
                    if(vpsArr.length==1){
                        resp.put("account",obj);
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    jsonArray.add(obj);
                    //resp.put("status", "success");
                }

            }

            resp.put("accounts",jsonArray);
            //resp.put("message", vps.getJSonObj());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/checkDelAccLocal",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> checkDelAccLocal(@RequestHeader(defaultValue = "") String Authorization,@RequestParam String vps) {
        JSONObject resp = new JSONObject();
        Integer checktoken = adminRepository.FindAdminByToken(Authorization);
        if (checktoken == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "vps không được để trống!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            resp.put("state", vpsRepository.getState("%"+vps.trim()+"%"));
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

        }catch (Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/updateStateDelAccLocal",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> UpdateStateDelAccLocal(@RequestHeader(defaultValue = "") String Authorization,@RequestParam String vps) {
        JSONObject resp = new JSONObject();
        Integer checktoken = adminRepository.FindAdminByToken(Authorization);
        if (checktoken == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "vps không được để trống!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            List<Vps> vpsList =vpsRepository.findVPS("%"+vps.trim()+"%");
            vpsList.get(0).setState(1);
            vpsRepository.save(vpsList.get(0));
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

        }catch (Exception e){
            resp.put("status", "fail");
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
            String[] vpsArr=vps.split(",");
            JSONArray jsonArray=new JSONArray();
            for(int i=0;i<vpsArr.length;i++){

                historyRepository.deletenamevpsByVps(vpsArr[i].trim());
                vpsRepository.deleteByVps(vpsArr[i].trim());

                OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
                Request request = null;
                request = new Request.Builder().url("http://accpremiumpostget-env.ap-southeast-1.elasticbeanstalk.com/gmails/resetaccountbyvps?vps=" + vpsArr[i].trim()).get().addHeader("Authorization", "1").build();
                Response response = client.newCall(request).execute();
                if(vpsArr.length==1){
                    resp.put("vps",vpsArr[i].trim());
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                JSONObject obj=new JSONObject();
                obj.put("vps",vpsArr[i].trim());
                jsonArray.add(obj);

            }
            resp.put("vps","Fdfdfd");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

}

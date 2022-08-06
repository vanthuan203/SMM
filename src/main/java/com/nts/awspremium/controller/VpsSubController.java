package com.nts.awspremium.controller;

import com.nts.awspremium.model.Admin;
import com.nts.awspremium.model.Vps;
import com.nts.awspremium.model.VpsRunning;
import com.nts.awspremium.repositories.*;
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
@RequestMapping(path ="/vpssub")
public class VpsSubController {
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private VpsRepository vpsRepository;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private ProxyRepository proxyRepository;
    @Autowired
    private AccountRepository accountRepository;
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
                List<VpsRunning> vpsRunnings=accountRepository.getvpsrunning();
                List<VpsRunning> accByVps=accountRepository.getCountAccByVps();
                List<VpsRunning> accLiveByVps=accountRepository.getCountAccLiveByVps();
                List<VpsRunning> accTaskSubByVps=accountRepository.getCountAccTaskSubByVps();
                for(int i=0;i<vps.size();i++){

                    Integer total=0;
                    Integer totalacc=0;
                    Integer totalacclive=0;
                    Integer totalacctasksub=0;
                    for(int j=0;j<vpsRunnings.size();j++){
                        if(vps.get(i).getVps().equals(vpsRunnings.get(j).getVps())){
                           total=vpsRunnings.get(j).getTotal();
                           vpsRunnings.remove(j);
                        }
                    }

                    for(int k=0;k<accByVps.size();k++){
                        if(vps.get(i).getVps().equals(accByVps.get(k).getVps())){
                            totalacc=accByVps.get(k).getTotal();
                            accByVps.remove(k);
                        }
                    }

                    for(int k=0;k<accLiveByVps.size();k++){
                        if(vps.get(i).getVps().equals(accLiveByVps.get(k).getVps())){
                            totalacclive=accLiveByVps.get(k).getTotal();
                            accLiveByVps.remove(k);
                        }
                    }
                    for(int k=0;k<accTaskSubByVps.size();k++){
                        if(vps.get(i).getVps().equals(accTaskSubByVps.get(k).getVps())){
                            totalacctasksub=accTaskSubByVps.get(k).getTotal();
                            accTaskSubByVps.remove(k);
                        }
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("id", vps.get(i).getId());
                    obj.put("vps", vps.get(i).getVps());
                    obj.put("acccount", totalacc);
                    obj.put("acccountlive", totalacclive);
                    obj.put("acccounttasksub", totalacctasksub);
                    obj.put("vpsoption",  vps.get(i).getVpsoption());
                    obj.put("state",  vps.get(i).getState());
                    obj.put("timecheck",  vps.get(i).getTimecheck());
                    obj.put("threads",  vps.get(i).getThreads());
                    obj.put("running",  vps.get(i).getRunning());
                    obj.put("total",total);
                    //obj.put("view24h",totalview);
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
    ResponseEntity<String> checkvps(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "0") Integer countuser,@RequestParam(defaultValue = "0") Integer threads,@RequestParam(defaultValue = "0") Integer numbersub,@RequestParam(defaultValue = "") String vps){
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
                //resp.put("option",vpscheck.get(0).getVpsoption());
                resp.put("countuser",vpscheck.get(0).getThreads()==0?countuser:vpscheck.get(0).getThreads());
                resp.put("numbersub",vpscheck.get(0).getState()==0?numbersub:vpscheck.get(0).getState());
                resp.put("resetacc",vpscheck.get(0).getVpsoption().equals("Yes")?1:0);
                resp.put("threads",vpscheck.get(0).getRunning()==0?threads:vpscheck.get(0).getRunning());



                vpscheck.get(0).setTimecheck(System.currentTimeMillis());
                if(vpscheck.get(0).getVpsreset()>0){
                    vpscheck.get(0).setVpsreset(0);
                }
                if(vpscheck.get(0).getThreads()==0){
                    vpscheck.get(0).setThreads(countuser);
                }
                if(vpscheck.get(0).getState()==0){
                    vpscheck.get(0).setState(numbersub);
                }
                if(vpscheck.get(0).getVpsoption().equals("Yes")){
                    vpscheck.get(0).setVpsoption("No");
                }
                if(vpscheck.get(0).getRunning()==0){
                    vpscheck.get(0).setRunning(threads);
                }
                vpsRepository.save(vpscheck.get(0));
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

            }else{
                Vps vpsnew=new Vps();
                vpsnew.setVps(vps);
                vpsnew.setState(numbersub);
                vpsnew.setRunning(threads);
                vpsnew.setVpsoption("No");
                vpsnew.setVpsreset(0);
                vpsnew.setThreads(countuser);
                vpsnew.setTimecheck(System.currentTimeMillis());
                vpsRepository.save(vpsnew);
                resp.put("status", "true");
                resp.put("countuser",countuser);
                resp.put("numbersub",numbersub);
                resp.put("resetacc",0);
                resp.put("threads",threads);
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
                vpscheck.get(0).setTimecheck(System.currentTimeMillis());
                if(vpscheck.get(0).getVpsreset()>0){
                    vpscheck.get(0).setVpsreset(0);
                }
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
                    vpsupdate.get(0).setState(vps.getState());
                    vpsupdate.get(0).setVpsreset(vps.getVpsreset());
                    vpsupdate.get(0).setRunning(vps.getRunning());
                    vpsRepository.save(vpsupdate.get(0));

                    JSONObject obj = new JSONObject();
                    obj.put("id", vpsupdate.get(0).getId());
                    obj.put("vps", vpsupdate.get(0).getVps());
                    obj.put("vpsoption",  vps.getVpsoption());
                    obj.put("state",   vpsupdate.get(0).getState());
                    obj.put("timecheck",  System.currentTimeMillis());
                    obj.put("running",vps.getRunning());
                    obj.put("threads",  vps.getThreads());
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

                accountRepository.resetAccountByVps("%"+vpsArr[i].trim()+"%");
                vpsRepository.deleteByVps(vpsArr[i].trim());


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

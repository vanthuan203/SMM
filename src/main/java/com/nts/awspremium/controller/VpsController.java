package com.nts.awspremium.controller;

import com.nts.awspremium.model.*;
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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path ="/vps")
public class VpsController {
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private VideoViewRepository viewRepository;

    @Autowired
    private VpsRepository vpsRepository;
    @Autowired
    private HistoryViewRepository historyViewRepository;
    @Autowired
    private HistoryCommentRepository historyCommentRepository;
    @Autowired
    private ProxyRepository proxyRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountChangeRepository accountChangeRepository;
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
                List<VpsRunning> vpsRunnings=historyViewRepository.getvpsrunning();
                List<VpsRunning> accByVps=accountRepository.getCountAccByVps();
                for(int i=0;i<vps.size();i++){
                    Integer total=0;
                    String time="";
                    Integer totalview=0;
                    Integer totalacc=0;
                    for(int j=0;j<vpsRunnings.size();j++){
                        if(vps.get(i).getVps().equals(vpsRunnings.get(j).getVps())){
                           total=vpsRunnings.get(j).getTotal();
                           time=vpsRunnings.get(j).getTime();
                           vpsRunnings.remove(j);
                        }
                    }
                    for(int k=0;k<accByVps.size();k++){
                        if(vps.get(i).getVps().equals(accByVps.get(k).getVps())){
                            totalacc=accByVps.get(k).getTotal();
                            accByVps.remove(k);
                        }
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("id", vps.get(i).getId());
                    obj.put("vps", vps.get(i).getVps());
                    obj.put("ipv4", vps.get(i).getIpv4());
                    obj.put("namevps", vps.get(i).getNamevps());
                    obj.put("vpsoption",  vps.get(i).getVpsoption());
                    obj.put("vpsreset",  vps.get(i).getVpsreset());
                    obj.put("state",  vps.get(i).getState());
                    obj.put("timegettask",time);
                    obj.put("timecheck",  vps.get(i).getTimecheck());
                    obj.put("get_account",  vps.get(i).getGet_account());
                    obj.put("threads",  vps.get(i).getThreads());
                    obj.put("total",total);
                    obj.put("acccount", totalacc);
                    obj.put("ext", vps.get(i).getExt());
                    obj.put("cmt", vps.get(i).getCmt());
                    obj.put("proxy", vps.get(i).getProxy());
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
            List<Vps> vps1 =vpsRepository.findVPS(vps.trim());
            if(vps1.size()>0){
                resp.put("status", "fail");
                resp.put("message", "Vps đã tồn tại");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }else{
                Vps vpsnew=new Vps();
                vpsnew.setVps(vps);
                vpsnew.setState(1);
                vpsnew.setRunning(0);
                vpsnew.setChangefinger(0);
                vpsnew.setVpsoption("Pending");
                vpsnew.setUrlapi("");
                vpsnew.setCmt(1);
                vpsnew.setProxy(1);
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
    ResponseEntity<String> checkvps(@RequestParam(defaultValue = "") String vps){
        JSONObject resp=new JSONObject();
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{

            List<Vps> vpscheck =vpsRepository.findVPS(vps.trim());

            if(vpscheck.size()>0){
                if(vpscheck.get(0).getVpsoption().equals("Pending")){
                    resp.put("status", "true");
                    resp.put("option","Pending");
                    resp.put("vpsreset",vpscheck.get(0).getVpsreset());
                    if(vpscheck.get(0).getVpsreset()>0){
                        vpscheck.get(0).setVpsreset(0);
                    }
                    vpscheck.get(0).setTimecheck(System.currentTimeMillis());
                    vpsRepository.save(vpscheck.get(0));
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                resp.put("option",vpscheck.get(0).getVpsoption());
                resp.put("cmt",vpscheck.get(0).getCmt());
                resp.put("threads",vpscheck.get(0).getThreads());
                resp.put("vpsreset",vpscheck.get(0).getVpsreset());
                resp.put("get_account",vpscheck.get(0).getGet_account());
                resp.put("proxy",vpscheck.get(0).getProxy());
                resp.put("ext",vpscheck.get(0).getExt());
                if(vpscheck.get(0).getVpsreset()>0){
                    vpscheck.get(0).setVpsreset(0);
                }
                TimeZone timeZone = TimeZone.getTimeZone("GMT+7");
                Calendar calendar = Calendar.getInstance(timeZone);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                vpscheck.get(0).setTimecheck(System.currentTimeMillis());
                vpscheck.get(0).setTimereset(day);
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
                vpsnew.setExt(1);
                vpsnew.setCmt(1);
                vpsnew.setProxy(1);
                vpsnew.setGet_account(1);
                vpsnew.setChangefinger(0);
                vpsnew.setTimecheck(System.currentTimeMillis());
                vpsnew.setTimeresettool(System.currentTimeMillis());
                vpsRepository.save(vpsnew);
                resp.put("status", "true");
                resp.put("option","Pending");
                resp.put("vpsreset",2);
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
    ResponseEntity<String> checkresetvps(@RequestParam(defaultValue = "") String vps){
        JSONObject resp=new JSONObject();
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            List<Vps> vpscheck =vpsRepository.findVPS(vps.trim());

            if(vpscheck.size()>0){
                vpscheck.get(0).setTimecheck(System.currentTimeMillis());
                vpsRepository.save(vpscheck.get(0));
                resp.put("option",vpscheck.get(0).getVpsoption());
                resp.put("status", "true");
                resp.put("vpsreset",vpscheck.get(0).getVpsreset());

                resp.put("threads",vpscheck.get(0).getThreads());
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


    @GetMapping(value = "checkresetvpspython",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> checkresetvpspython(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String vps){
        JSONObject resp=new JSONObject();
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            List<Vps> vpscheck =vpsRepository.findVPS(vps.trim());

            if(vpscheck.size()>0){
                resp.put("status", "true");
                Long min=(System.currentTimeMillis()-vpscheck.get(0).getTimecheck())/1000/60;
                System.out.println(min);
                if(min>=5){
                    resp.put("vpsreset",1);
                }else{
                    resp.put("vpsreset",0);
                }
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
    @GetMapping(value = "setResetBasDaily",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> setResetBasDaily(){
        JSONObject resp=new JSONObject();
        try{
            vpsRepository.resetVPSDaily();
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value = "resetBasDailyByCron",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> resetBasDailyByCron(@RequestParam(defaultValue = "0") Integer limit){
        JSONObject resp=new JSONObject();
        try{
            vpsRepository.resetBasDailyByCron(System.currentTimeMillis(),limit);
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
        @GetMapping(value = "resetBasByCron",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> resetBasByCron(@RequestParam(defaultValue = "0") Integer limit){
        JSONObject resp=new JSONObject();
        try{
            if(vpsRepository.checkResetVPSNext()>0){
                resp.put("status", "watting");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            vpsRepository.resetBasByCron(System.currentTimeMillis(),limit-vpsRepository.checkResetVPSNext());
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "resetBasNoCheckByCron",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> resetBasNoCheckByCron(@RequestParam(defaultValue = "0") Integer limit){
        JSONObject resp=new JSONObject();
        try{
            vpsRepository.resetBasByCron(System.currentTimeMillis(),limit);
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "changer_vn",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> changer_vn(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp=new JSONObject();
        Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        if(checktoken==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            List<AccountChange> accountChanges=accountChangeRepository.getGeoChangerVN();
            if(accountChanges.size()==0){
                TimeZone timeZone = TimeZone.getTimeZone("GMT+7");
                Calendar calendar = Calendar.getInstance(timeZone);
                int month =1+ calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                if(accountChangeRepository.checkRunningChanger("vietnam"+day+"."+month)>0){
                    resp.put("status", -1);
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                AccountChange accountChange=new AccountChange();
                accountChange.setTime(0L);
                accountChange.setGeo("vn");
                accountChange.setNote("");
                accountChange.setPriority(0);
                accountChange.setRunning(0);
                accountChange.setName("vietnam"+day+"."+month);
                accountChangeRepository.save(accountChange);
                accountChanges=accountChangeRepository.getGeoChangerVN();
            }
            Integer check=vpsRepository.changer_account_vn(accountChanges.get(0).getName().trim());
            if(check>0){
                accountChanges.get(0).setRunning(1);
                accountChanges.get(0).setTime(System.currentTimeMillis());
                accountChangeRepository.save(accountChanges.get(0));
            }
            resp.put("status", check);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "changer_us",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> changer_us(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp=new JSONObject();
        Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        if(checktoken==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            List<AccountChange> accountChanges=accountChangeRepository.getGeoChangerUS();
            if(accountChanges.size()==0){
                TimeZone timeZone = TimeZone.getTimeZone("GMT+7");
                Calendar calendar = Calendar.getInstance(timeZone);
                int month =1+ calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                if(accountChangeRepository.checkRunningChanger("hoaky"+day+"."+month)>0){
                    resp.put("status", -1);
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                AccountChange accountChange=new AccountChange();
                accountChange.setTime(0L);
                accountChange.setGeo("us");
                accountChange.setNote("");
                accountChange.setPriority(0);
                accountChange.setRunning(0);
                accountChange.setName("hoaky"+day+"."+month);
                accountChangeRepository.save(accountChange);
                accountChanges=accountChangeRepository.getGeoChangerUS();
            }
            Integer check=vpsRepository.changer_account_us(accountChanges.get(0).getName().trim());
            if(check>0){
                accountChanges.get(0).setRunning(1);
                accountChanges.get(0).setTime(System.currentTimeMillis());
                accountChangeRepository.save(accountChanges.get(0));
            }
            resp.put("status", check);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "resetAll",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> resetAll(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp=new JSONObject();
        Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        if(checktoken==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            vpsRepository.resetTimeResetTool();
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "resetVPSByName",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> resetVPSByName(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String vps){
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
            vpsRepository.updateRestartVpsByName(System.currentTimeMillis(),vps.trim());
            resp.put("status", "true");
            resp.put("message", "Restart "+vps.trim());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
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
                List<Vps> vpsupdate =vpsRepository.findVPS(vpsArr[i].trim());
                if(vpsupdate.size()>0) {
                    vpsupdate.get(0).setThreads(vps.getThreads());
                    vpsupdate.get(0).setExt(vps.getExt());
                    vpsupdate.get(0).setVpsoption(vps.getVpsoption());
                    vpsupdate.get(0).setCmt(vps.getCmt());
                    vpsupdate.get(0).setProxy(vps.getProxy());
                    //vpsupdate.get(0).setUrlapi(vps.getVpsoption().contains("Cheat") ? "accpremium-env.ap-southeast-1.elasticbeanstalk.com" : vps.getVpsoption().contains("Pending") ? "" : "cheatviewapi-env-2.ap-southeast-1.elasticbeanstalk.com");
                    //vpsupdate.get(0).setToken(vps.getVpsoption().contains("Cheat") ? "1" : vps.getVpsoption().contains("Pending") ? "" : "0");
                    //vpsupdate.get(0).setTimecheck(System.currentTimeMillis());
                    vpsupdate.get(0).setVpsreset(vps.getVpsreset());
                    vpsupdate.get(0).setGet_account(vps.getGet_account());
                    if(vps.getVpsreset()>0){
                        vpsupdate.get(0).setTimeresettool(System.currentTimeMillis());
                    }
                    vpsRepository.save(vpsupdate.get(0));

                    JSONObject obj = new JSONObject();
                    obj.put("id", vpsupdate.get(0).getId());
                    obj.put("vps", vpsupdate.get(0).getVps());
                    obj.put("vpsoption",  vps.getVpsoption());
                    obj.put("state",   vpsupdate.get(0).getState());
                    obj.put("timecheck",  vps.getTimecheck());
                    obj.put("threads",  vps.getThreads());
                    obj.put("vpsreset",  vps.getVpsreset());
                    obj.put("get_account",  vps.getGet_account());
                    obj.put("ext",  vps.getExt());
                    obj.put("cmt",  vps.getCmt());
                    obj.put("proxy",  vps.getProxy());
                    obj.put("total",historyViewRepository.getrunningbyVps(vpsupdate.get(0).getVps().trim()));
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
                List<Vps> vpsupdate =vpsRepository.findVPS(vpsArr[i].trim());
                if(vpsupdate.size()>0) {
                    if(vps.getVpsreset()>0){
                        vpsupdate.get(0).setTimeresettool(System.currentTimeMillis());
                    }
                    vpsupdate.get(0).setVpsreset(vps.getVpsreset());
                    vpsupdate.get(0).setGet_account(vps.getGet_account());
                    vpsupdate.get(0).setVpsoption(vps.getVpsoption());
                    vpsupdate.get(0).setProxy(vps.getProxy());
                    vpsupdate.get(0).setCmt(vps.getCmt());
                    vpsupdate.get(0).setExt(vps.getExt());
                    vpsRepository.save(vpsupdate.get(0));

                    JSONObject obj = new JSONObject();
                    obj.put("id", vpsupdate.get(0).getId());
                    obj.put("vps", vpsupdate.get(0).getVps());
                    obj.put("vpsoption",  vpsupdate.get(0).getVpsoption());
                    obj.put("state",   vpsupdate.get(0).getState());
                    obj.put("timecheck",  System.currentTimeMillis());
                    obj.put("threads",  vpsupdate.get(0).getThreads());
                    obj.put("vpsreset",  vps.getVpsreset());
                    obj.put("get_account",  vps.getGet_account());
                    obj.put("ext",  vps.getExt());
                    obj.put("proxy",  vps.getProxy());
                    obj.put("cmt",  vps.getCmt());
                    obj.put("total",historyViewRepository.getrunningbyVps(vpsupdate.get(0).getVps().trim()));
                    obj.put("view24h",0);
                    if(vpsArr.length==1){
                        resp.put("accounts",obj);
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
            resp.put("state", vpsRepository.getState(vps.trim()+"%"));
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
            List<Vps> vpsList =vpsRepository.findVPS(vps.trim());
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
    @GetMapping(value="/resetrunningaccbyvps",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> resetrunningaccbyvps(@RequestParam String vps){
        JSONObject resp = new JSONObject();
        try{
            String[] vpslist = vps.split(",");
            for(int i=0;i<vpslist.length;i++){
                accountRepository.updateRunningByVPs(vpslist[i].trim());
            }
            resp.put("status","true");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","true");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value="/resetrunningacccmtbyvps",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> resetrunningacccmtbyvps(@RequestParam String vps){
        JSONObject resp = new JSONObject();
        try{
            String[] vpslist = vps.split(",");
            for(int i=0;i<vpslist.length;i++){
                accountRepository.updateRunningAccCmtByVPs(vpslist[i].trim());
            }
            resp.put("status","true");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","true");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
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
            String[] vpsArr=vps.split(",");
            JSONArray jsonArray=new JSONArray();
            for(int i=0;i<vpsArr.length;i++){
                vpsRepository.deleteByVps(vpsArr[i].trim());
                accountRepository.resetAccountByVps(vpsArr[i].trim());
                historyViewRepository.resetHistoryViewByVps(vpsArr[i].trim());
                historyCommentRepository.resetThreadViewByVps(vpsArr[i].trim());
                proxyRepository.updaterunningByVps(vpsArr[i].trim());
                if(vpsArr.length==1){
                    resp.put("vps",vpsArr[i].trim());
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                JSONObject obj=new JSONObject();
                obj.put("vps",vpsArr[i].trim());
                jsonArray.add(obj);

            }
            resp.put("vps","Oke");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "resettoolbyhistimecheckcron",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> resettoolbyhistimecheckcron(){
        JSONObject resp=new JSONObject();
        try{
            vpsRepository.resetVPSByHisTimecheck(System.currentTimeMillis());
            //accountRepository.resetAccountSubByTimecheck();
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

}

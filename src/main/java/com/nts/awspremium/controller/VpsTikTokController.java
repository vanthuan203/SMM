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
import java.util.concurrent.TimeUnit;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path ="/vps_tiktok")
public class VpsTikTokController {
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private VpsRepository vpsRepository;
    @Autowired
    private AccountTikTokRepository accountTikTokRepository;
    @Autowired
    private AccountRegTikTokRepository accountRegTikTokRepository;
    @Autowired
    private HistoryTiktokRepository historyTiktokRepository;

    @Autowired
    private Proxy_IPV4_TikTokRepository proxyIpv4TikTokRepository;
    @GetMapping(value = "listVPS",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> listVPS(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp=new JSONObject();
        try{
            List<Vps> vps=vpsRepository.getListVPSTikTok();
            if(vps.size()==0){
                resp.put("status","fail");
                resp.put("message", "Vps trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }else{
                JSONArray jsonArray= new JSONArray();
                List<VpsRunning> vpsRunnings=historyTiktokRepository.getvpsrunning();
                List<VpsRunning> accByVps=accountTikTokRepository.getCountAccByVps();
                List<VpsRunning> accLiveByVps=accountTikTokRepository.getCountAccLiveByVps();
                for(int i=0;i<vps.size();i++){
                    Integer total=0;
                    Integer totalacc=0;
                    Integer totalacclive=0;
                    Integer total_device=accountTikTokRepository.countDevicebyVPS(vps.get(i).getVps());
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
                    Long time_get_task= historyTiktokRepository.getTimeGetByVPS(vps.get(i).getVps());
                    JSONObject obj = new JSONObject();
                    obj.put("id", vps.get(i).getId());
                    obj.put("vps", vps.get(i).getVps());
                    obj.put("acccount", totalacc);
                    obj.put("acccountlive", totalacclive);
                    obj.put("vpsoption",  vps.get(i).getVpsoption());
                    obj.put("vpsreset",  vps.get(i).getVpsreset());
                    obj.put("state",  vps.get(i).getState());
                    obj.put("timegettask", time_get_task==null?0:time_get_task);
                    obj.put("timecheck",  vps.get(i).getTimecheck());
                    obj.put("running",  vps.get(i).getRunning());
                    obj.put("follower", historyTiktokRepository.getHistoryFollowerTikTokByVPS(vps.get(i).getVps()));
                    obj.put("total",total);
                    obj.put("total_device",total_device);
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

    @GetMapping(value = "getListDevicesByVPS",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getListDevicesByVPS(@RequestParam(defaultValue = "") String vps){
        JSONObject resp=new JSONObject();
        try{
            if(vps.length()==0){
                resp.put("status","fail");
                resp.put("message", "Vps trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }else{
                JSONArray jsonArray= new JSONArray();
                List<VpsRunning> vpsRunnings=historyTiktokRepository.getDeviceRunningByVPS(vps.trim());
                List<DeviceRunning> accByDeviceByVps=accountTikTokRepository.getCountAccByDeviceByVps(vps.trim());
                List<DeviceRunning> accLiveByDeviceByVps=accountTikTokRepository.getCountAccLiveByDeviceByVps(vps.trim());
                for(int i=0;i<accByDeviceByVps.size();i++){
                    Integer total=0;
                    Integer total_acc_live=0;
                    Long time=0L;
                    for(int j=0;j<vpsRunnings.size();j++){
                        if(accByDeviceByVps.get(i).getDevice_id().equals(vpsRunnings.get(j).getVps())){
                            total=vpsRunnings.get(j).getTotal();
                            time=vpsRunnings.get(j).getTimeget();
                            vpsRunnings.remove(j);
                        }
                    }
                    for(int j=0;j<accLiveByDeviceByVps.size();j++){
                        if(accByDeviceByVps.get(i).getDevice_id().equals(accLiveByDeviceByVps.get(j).getDevice_id())){
                            total_acc_live=accLiveByDeviceByVps.get(j).getTotal();
                            break;
                        }
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("vps", accByDeviceByVps.get(i).getVps());
                    obj.put("device_id", accByDeviceByVps.get(i).getDevice_id());
                    obj.put("time_add", accByDeviceByVps.get(i).getTime_add());
                    obj.put("acccount", accByDeviceByVps.get(i).getTotal());
                    obj.put("acccountlive", total_acc_live);
                    obj.put("state", 1);
                    obj.put("timegettask",time);
                    obj.put("follower",historyTiktokRepository.getHistoryFollowerTikTokByDeviceId(accByDeviceByVps.get(i).getDevice_id()));
                    obj.put("running", total);
                    //obj.put("view24h",totalview);
                    jsonArray.add(obj);
                }
                resp.put("devices",jsonArray);
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
    ResponseEntity<String> checkvps(@RequestParam(defaultValue = "0") Integer countuser,@RequestParam(defaultValue = "0") Integer threads,@RequestParam(defaultValue = "0") Integer numbersub,@RequestParam(defaultValue = "") String vps){
        JSONObject resp=new JSONObject();
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            //proxyRepository.updaterunningByVps(vps.trim()+"%");
            List<Vps> vpscheck =vpsRepository.findVPS(vps.trim());
            Integer resetSub=0;

            Date date=new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, 1); //minus number would decrement the days

            if(vpscheck.size()>0){
                System.out.println(vpscheck.size());
                if(date.getDate()>vpscheck.get(0).getDayreset() && date.getMonth()==cal.getTime().getMonth()){
                    //System.out.println(date.getDate());
                    resetSub=1;
                    //vpscheck.get(0).setDayreset(cal.getTime().getDate());
                }else if(date.getHours()>=vpscheck.get(0).getTimereset() && date.getDate()==vpscheck.get(0).getDayreset()){
                    resetSub=1;
                    //System.out.println("2");
                    //vpscheck.get(0).setDayreset(cal.getTime().getDate());
                }
                resp.put("status", "true");
                //resp.put("option",vpscheck.get(0).getVpsoption());
                resp.put("countuser",vpscheck.get(0).getThreads()==0?countuser:vpscheck.get(0).getThreads());
                resp.put("numbersub",vpscheck.get(0).getState()==0?numbersub:vpscheck.get(0).getState());
                resp.put("numberlive",vpscheck.get(0).getLive()==0?numbersub:vpscheck.get(0).getLive());
                resp.put("resetacc",vpscheck.get(0).getVpsoption().equals("Yes")?1:0);
                resp.put("threads",vpscheck.get(0).getRunning()==0?threads:vpscheck.get(0).getRunning());
                resp.put("timeresetsub",resetSub);
                resp.put("option",vpscheck.get(0).getVpsoption().indexOf("Pending")>=0?"Pending":vpscheck.get(0).getVpsoption());


                if(vpscheck.get(0).getVpsreset()>0){
                    vpscheck.get(0).setVpsreset(0);
                }
                vpsRepository.save(vpscheck.get(0));
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

            }else{
                Integer gettime= vpsRepository.findTimeIdMax();
                Vps vpsnew=new Vps();
                vpsnew.setVps(vps);
                vpsnew.setState(numbersub);
                vpsnew.setRunning(threads);
                vpsnew.setVpsoption("Sub_Pending");
                vpsnew.setVpsreset(0);
                vpsnew.setTimereset(gettime==23?0:(gettime+1));
                vpsnew.setDayreset(cal.getTime().getDate());
                vpsnew.setThreads(countuser);
                vpsnew.setTimecheck(System.currentTimeMillis());
                vpsRepository.save(vpsnew);
                resp.put("status", "true");
                resp.put("countuser",countuser);
                resp.put("numbersub",numbersub);
                resp.put("resetacc",0);
                resp.put("threads",threads);
                resp.put("timeresetsub",resetSub);
                resp.put("option","Pending");
                //resp.put("message", "Vps thêm thành công!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(value = "timeresetsub",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> timeresetsub(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String vps){
        JSONObject resp=new JSONObject();
        Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        if(checktoken==0){
            resp.put("status","fail");
            resp.put("message", "Token expired ");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            List<Vps> vpscheck =vpsRepository.findVPS(vps.trim());
            Date date=new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, 1); //minus number would decrement the days

            if(vpscheck.size()>0){
                if(date.getDate()>vpscheck.get(0).getDayreset() && date.getMonth()==cal.getTime().getMonth() ){
                    if(date.getDate()>vpscheck.get(0).getDayreset()-1){
                        vpscheck.get(0).setDayreset(date.getDate());
                    }else{
                        vpscheck.get(0).setDayreset(cal.getTime().getDate());
                    }
                }else if(date.getHours()>=vpscheck.get(0).getTimereset() && date.getDate()==vpscheck.get(0).getDayreset()){
                    vpscheck.get(0).setDayreset(cal.getTime().getDate());
                }
                resp.put("status", "true");
                vpsRepository.save(vpscheck.get(0));
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

            }else{
                resp.put("status", "fail");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(value = "test",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> test(){
        JSONObject resp=new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        try{

            Date date=new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.HOUR, 20); //minus number would decrement the days
            resp.put("status",cal.getTime().getHours());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
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
            List<Vps> vpscheck =vpsRepository.findVPS(vps.trim());

            if(vpscheck.size()>0){

                resp.put("status", "true");
                resp.put("vpsreset",vpscheck.get(0).getVpsreset());
                if(vpscheck.get(0).getVpsreset()>0){
                    vpscheck.get(0).setVpsreset(0);
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
    //cron tab
    @GetMapping(value = "resetvpsbytimecheck",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> resetvpsbytimecheck(){
        JSONObject resp=new JSONObject();
        try{
            vpsRepository.resetVPSByTimecheck(System.currentTimeMillis());
            //accountRepository.resetAccountSubByTimecheck();
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "resetvpsandunrartoolbytimecheck",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> resetvpsandunrartoolbytimecheck(){
        JSONObject resp=new JSONObject();
        try{
            vpsRepository.resetVPSAndUnrarToolByTimecheck();
            //accountRepository.resetAccountSubByTimecheck();
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(value = "checkvpsdie",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> checkvpsdie(){
        JSONObject resp=new JSONObject();
        try{
            String vpsdie="";
            List<Vps> vpsList =vpsRepository.findVPSDie();
            if(vpsList.size()>0){
                for(int i=0;i<vpsList.size();i++){
                    if(i==0){
                        vpsdie=vpsList.get(i).getVps().substring(vpsList.get(i).getVps().indexOf("-")+1,vpsList.get(i).getVps().length());
                    }else{
                        vpsdie=vpsdie+","+ vpsList.get(i).getVps().substring(vpsList.get(i).getVps().indexOf("-")+1,vpsList.get(i).getVps().length());
                    }

                }
                resp.put("status", vpsdie);

                OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                Request request = null;

                request = new Request.Builder().url("https://maker.ifttt.com/trigger/vps_warning/with/key/eh3Ut1_iinzl4yCeH5-BC2d21WpaAKdzXTWzVfXurdc?value1=" + vpsdie+"&value2="+vpsList.size()).get().build();

                Response response = client.newCall(request).execute();
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }else{

                resp.put("status", "true");
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
                List<Vps> vpsupdate =vpsRepository.findVPS(vpsArr[i].trim());
                if(vpsupdate.size()>0) {

                    vpsupdate.get(0).setVpsoption(vps.getVpsoption());
                    vpsupdate.get(0).setVpsreset(vps.getVpsreset());
                    vpsRepository.save(vpsupdate.get(0));
                    Long time_get_task= historyTiktokRepository.getTimeGetByVPS(vpsupdate.get(0).getVps());
                    JSONObject obj = new JSONObject();
                    obj.put("id", vpsupdate.get(0).getId());
                    obj.put("vps",vpsupdate.get(0).getVps());
                    obj.put("acccount", 0);
                    obj.put("acccountlive", 0);
                    obj.put("vpsoption",  vpsupdate.get(0).getVpsoption());
                    obj.put("vpsreset", vpsupdate.get(0).getVpsreset());
                    obj.put("state",  vpsupdate.get(0).getState());
                    obj.put("timegettask", time_get_task==null?0:time_get_task);
                    obj.put("timecheck", vpsupdate.get(0).getTimecheck());
                    obj.put("running",  vpsupdate.get(0).getRunning());
                    obj.put("total",0);

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


    @PostMapping(value = "updatenamevps",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updatenamevps(@RequestHeader(defaultValue = "") String Authorization,@RequestBody Vps vps){
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
                    vpsupdate.get(0).setVpsoption(vps.getVpsoption());
                    vpsupdate.get(0).setState(vps.getState());
                    vpsupdate.get(0).setVpsreset(vps.getVpsreset());
                    vpsupdate.get(0).setRunning(vps.getRunning());
                    vpsRepository.save(vpsupdate.get(0));

                    JSONObject obj = new JSONObject();
                    obj.put("id", vpsupdate.get(0).getId());
                    obj.put("vps", vpsupdate.get(0).getVps());
                    obj.put("vpsoption",  vps.getVpsoption());
                    obj.put("vpsreset",  vps.getVpsreset());
                    obj.put("state",   vpsupdate.get(0).getState());
                    obj.put("timecheck",  System.currentTimeMillis());
                    obj.put("running",vps.getRunning());
                    obj.put("threads",vps.getThreads());
                    obj.put("timereset",  vpsupdate.get(0).getTimereset());
                    obj.put("dayreset",  vpsupdate.get(0).getDayreset());
                    obj.put("total",0);
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

    @PostMapping(value = "updatethread",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updatethread(@RequestHeader(defaultValue = "") String Authorization,@RequestBody Vps vps){
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
                    vpsupdate.get(0).setRunning(vps.getRunning());
                    vpsupdate.get(0).setVpsreset(vps.getVpsreset());
                    vpsRepository.save(vpsupdate.get(0));

                    JSONObject obj = new JSONObject();
                    obj.put("id", vpsupdate.get(0).getId());
                    obj.put("vps", vpsupdate.get(0).getVps());
                    obj.put("vpsoption",  vpsupdate.get(0).getVpsoption());
                    obj.put("vpsreset",  vpsupdate.get(0).getVpsreset());
                    obj.put("state",   vpsupdate.get(0).getState());
                    obj.put("timecheck",  System.currentTimeMillis());
                    obj.put("running",vps.getRunning());
                    obj.put("threads",  vpsupdate.get(0).getThreads());
                    obj.put("timereset",  vpsupdate.get(0).getTimereset());
                    obj.put("dayreset",  vpsupdate.get(0).getDayreset());
                    obj.put("total",0);
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

    @PostMapping(value = "updatesub",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updatesub(@RequestHeader(defaultValue = "") String Authorization,@RequestBody Vps vps){
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
                    vpsupdate.get(0).setState(vps.getState());
                    vpsupdate.get(0).setVpsreset(vps.getVpsreset());
                    vpsRepository.save(vpsupdate.get(0));

                    JSONObject obj = new JSONObject();
                    obj.put("id", vpsupdate.get(0).getId());
                    obj.put("vps", vpsupdate.get(0).getVps());
                    obj.put("vpsoption",  vpsupdate.get(0).getVpsoption());
                    obj.put("vpsreset",  vps.getVpsreset());
                    obj.put("state",   vps.getState());
                    obj.put("timecheck",  System.currentTimeMillis());
                    obj.put("running",vpsupdate.get(0).getRunning());
                    obj.put("threads",  vpsupdate.get(0).getThreads());
                    obj.put("timereset",  vpsupdate.get(0).getTimereset());
                    obj.put("dayreset",  vpsupdate.get(0).getDayreset());
                    obj.put("total",0);
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

    @PostMapping(value = "updateuser",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateuser(@RequestHeader(defaultValue = "") String Authorization,@RequestBody Vps vps){
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
                    vpsupdate.get(0).setVpsreset(vps.getVpsreset());
                    vpsRepository.save(vpsupdate.get(0));

                    JSONObject obj = new JSONObject();
                    obj.put("id", vpsupdate.get(0).getId());
                    obj.put("vps", vpsupdate.get(0).getVps());
                    obj.put("vpsoption",  vpsupdate.get(0).getVpsoption());
                    obj.put("vpsreset",  vps.getVpsreset());
                    obj.put("state",   vpsupdate.get(0).getState());
                    obj.put("timecheck",  System.currentTimeMillis());
                    obj.put("running",vpsupdate.get(0).getRunning());
                    obj.put("threads",  vps.getThreads());
                    obj.put("timereset",  vpsupdate.get(0).getTimereset());
                    obj.put("dayreset",  vpsupdate.get(0).getDayreset());
                    obj.put("total",0);
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
    @GetMapping(value="/resetrunningaccbyvps",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> resetrunningaccbyvps(@RequestParam String vps){
        JSONObject resp = new JSONObject();
        try{
            String[] vpslist = vps.split(",");
            for(int i=0;i<vpslist.length;i++){
                accountTikTokRepository.updateRunningByVPs(vpslist[i].trim());
            }
            resp.put("status","true");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","true");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping(value = "updaterestart",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updaterestart(@RequestHeader(defaultValue = "") String Authorization,@RequestBody Vps vps){
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
                    vpsupdate.get(0).setVpsreset(vps.getVpsreset());
                    vpsRepository.save(vpsupdate.get(0));
                    Long time_get_task= historyTiktokRepository.getTimeGetByVPS(vpsupdate.get(0).getVps());
                    JSONObject obj = new JSONObject();
                    obj.put("id", vpsupdate.get(0).getId());
                    obj.put("vps", vpsupdate.get(0).getVps());
                    obj.put("vpsoption",   vpsupdate.get(0).getVpsoption());
                    obj.put("vpsreset",  vps.getVpsreset());
                    obj.put("state",    vpsupdate.get(0).getState());
                    obj.put("timecheck",  System.currentTimeMillis());
                    obj.put("running",vpsupdate.get(0).getRunning());
                    obj.put("threads",  vpsupdate.get(0).getThreads());
                    obj.put("timereset",  vpsupdate.get(0).getTimereset());
                    obj.put("timegettask", time_get_task==null?0:time_get_task);
                    obj.put("dayreset",  vpsupdate.get(0).getDayreset());
                    obj.put("total",0);
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
                accountTikTokRepository.deleteAccountTiktokByVps(vpsArr[i].trim());
                List<String> stringList=accountRegTikTokRepository.getUsernameRegByVps(vpsArr[i].trim());
                for(int j=0;j<stringList.size();j++){
                    AccountRegTiktok accountRegTiktok=accountRegTikTokRepository.checkUsername(stringList.get(i));
                    if(accountRegTiktok!=null){
                        proxyIpv4TikTokRepository.resetProxyByProxyId(accountRegTiktok.getProxy());
                        accountRegTiktok.setVps("");
                        accountRegTiktok.setDevice_id("");
                        accountRegTiktok.setRunning(0);
                        accountRegTiktok.setProxy("");
                        accountRegTikTokRepository.save(accountRegTiktok);
                    }
                }
                historyTiktokRepository.deleteAllByVPS(vpsArr[i].trim());
                vpsRepository.deleteByVps(vpsArr[i].trim());

                JSONObject obj=new JSONObject();
                obj.put("vps",vpsArr[i].trim());
                jsonArray.add(obj);

            }
            resp.put("vps","");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

}

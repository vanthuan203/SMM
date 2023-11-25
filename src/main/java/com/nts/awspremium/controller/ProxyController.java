package com.nts.awspremium.controller;

import com.nts.awspremium.ProxyAPI;
import com.nts.awspremium.model.CheckProsetListTrue;
import com.nts.awspremium.model.IpV4;
import com.nts.awspremium.model.Proxy;
import com.nts.awspremium.model.ProxyLive;
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

import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/proxy")
public class ProxyController {
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private ProxyRepository proxyRepository;

    @Autowired
    private ProxyLiveRepository proxyLiveRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private IpV4Repository ipV4Repository;
    @Autowired
    private CheckProsetListTrue checkProsetListTrue;
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
            proxynew.setGeo(proxy.getGeo().trim());
            proxynew.setTimeget(System.currentTimeMillis());
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
    ResponseEntity<String> delproxy(@RequestBody String proxy){
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
    @GetMapping(value="/delipv4",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> delipv4(@RequestParam String ipv4){
        JSONObject resp = new JSONObject();
        try{
            String[] ipv4list = ipv4.split(",");
            System.out.println(ipv4list.length);
            for(int i=0;i<ipv4list.length;i++){
                proxyRepository.deleteProxyByIpv4(ipv4list[i].trim());
                ipV4Repository.DeleteIPv4(ipv4list[i].trim());
            }
            resp.put("status","true");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","true");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value="/addipv4",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> addipv4(@RequestParam String ipv4){
        JSONObject resp = new JSONObject();
        try{
            String[] ipv4list = ipv4.split(",");
            for(int i=0;i<ipv4list.length;i++){
                if(ipV4Repository.checkIpv4(ipv4list[i].trim())==0){
                    IpV4 ipV4=new IpV4();
                    ipV4.setState(1);
                    ipV4.setIpv4(ipv4list[i].trim());
                    ipV4.setNumcheck(0);
                    ipV4.setTimecheck(0L);
                    ipV4Repository.save(ipV4);
                }
            }
            resp.put("status","true");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","true");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value="/pendingproxy",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> pendingproxy(@RequestBody String proxy){
        JSONObject resp = new JSONObject();
        try{
            List<String> list_ipv4=proxyRepository.getIpv4ProxyBuff();
            System.out.println(list_ipv4);

            for(int i=0;i<list_ipv4.size();i++){
                proxyRepository.updatepending(list_ipv4.get(i));
            }
            resp.put("status","true");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value="/list_v4",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> list_v4(){
        JSONObject resp = new JSONObject();
        try{
            List<String> list_ipv4=proxyRepository.getListProxyV4();

            JSONArray jsonArray=new JSONArray();
            for(int i=0;i<list_ipv4.size();i++){
                JSONObject obj = new JSONObject();
                obj.put("ipv4",list_ipv4.get(i).split(",")[0]);
                obj.put("totalport",list_ipv4.get(i).split(",")[1]);
                obj.put("timecheck",list_ipv4.get(i).split(",")[2]);
                obj.put("state",list_ipv4.get(i).split(",")[3]);
                obj.put("geo",list_ipv4.get(i).split(",")[4]);
                obj.put("numcheck",list_ipv4.get(i).split(",")[5]);
                obj.put("typeproxy",list_ipv4.get(i).split(",")[6]);
                jsonArray.add(obj);
            }
            resp.put("proxies",jsonArray);
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
    @GetMapping(value = "/checkproxyvpssubpending", produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> checkproxyvpssubpending(@RequestParam(defaultValue = "")  String vps) {
        JSONObject resp = new JSONObject();
        try{
            if(vps.length()==0){
                resp.put("status","fail");
                resp.put("message", "Không để vps trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            Integer checkv1= ipV4Repository.checkIpv4ByVps(20,"%"+vps.trim()+"%");
            Integer checkv2= ipV4Repository.checkIpv4ByVps(30,"%"+vps.trim()+"%");
            if(checkv1!=12 || checkv2!=3) {
                for (int i = 0; i < 12 - checkv1; i++) {
                    ipV4Repository.updateIpv4byVps(vps.trim(), 20);
                }
                for (int i = 0; i < 3 - checkv1; i++) {
                    ipV4Repository.updateIpv4byVps(vps.trim(), 30);
                }
            }
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }

    }
    @GetMapping(value = "/checkproxyvpssub", produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> checkproxyvps(@RequestParam(defaultValue = "")  String vps) {
        JSONObject resp = new JSONObject();
        try{
            if(vps.length()==0){
                resp.put("status","fail");
                resp.put("message", "Không để vps trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            Integer checkv1= ipV4Repository.checkIpv4ByVps("%"+vps.trim()+"%");
            if(checkv1!=12 ) {
                for (int i = 0; i < 12 - checkv1; i++) {
                    ipV4Repository.updateIpv4byVps(vps.trim(), 20);
                }
                for (int i = 0; i < 3 - checkv1; i++) {
                    ipV4Repository.updateIpv4byVps(vps.trim(), 30);
                }
            }
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }

    }
//get proxy sub old
    @GetMapping(value = "/getproxysubpending", produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> getproxysubpending(@RequestParam(defaultValue = "")  String username,@RequestParam(defaultValue = "") String vps,@RequestParam(defaultValue = "") String proxyfail) {
        JSONObject resp = new JSONObject();
        try{
            if(vps.length()==0){
                resp.put("status","fail");
                resp.put("message", "Không để vps trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            if(username.length()==0){
                resp.put("status","fail");
                resp.put("message", "Không để username trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }

            Date date=new Date();
            int minutes=date.getMinutes();
            /*
            if(minutes>=56 || minutes==0){
                resp.put("status","fail");
                resp.put("message","Đợi get lại proxy!" );
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            */
            Long id=accountRepository.findIdByUsername(username);
            String proxy =accountRepository.findProxyByIdSub(id);
            String[] proxysub={"",""};
            if(proxy.length()>0){
                proxysub[0]= ipV4Repository.getIpv4ByVps(20,"%"+vps.trim()+"%");
                ipV4Repository.updateUserCountByIpv4(proxysub[0]);
                proxysub[1]= ipV4Repository.getIpv4ByVps(30,"%"+vps.trim()+"%");
                ipV4Repository.updateUserCountByIpv4(proxysub[1]);
                accountRepository.updateProxyById(proxysub[0],proxysub[1],id);
            }else{
               proxysub=proxy.split(",");
            }
            System.out.println(ipV4Repository.checkProxyLive("%"+proxysub[0]+"%"));
            List<Proxy> proxyGet=null;
            if(proxyfail.length()!=0){
                if((1<=minutes && minutes<16) || (21<=minutes && minutes<36) || (41<=minutes &&minutes<56)){
                    proxyGet=proxyRepository.getProxySubT1();
                }else{
                    proxyGet=proxyRepository.getProxySubT2();
                }
            }else{
                if(ipV4Repository.checkProxyLive("%"+proxysub[0]+"%")!=0 && ((1<=minutes && minutes<16) || (21<=minutes && minutes<36) || (41<=minutes &&minutes<56))){
                    proxyGet=proxyRepository.getProxySubByIpv4T1("%"+proxysub[0]+"%");
                }else if(proxyGet==null && (ipV4Repository.checkProxyLive("%"+proxysub[1]+"%")!=0 && (26>minutes || minutes>=31))){
                    proxyGet=proxyRepository.getProxySubByIpv4T2("%"+proxysub[1]+"%");
                }else if(proxyGet==null){
                    if((1<=minutes && minutes<16) || (21<=minutes && minutes<36) || (41<=minutes &&minutes<56)){
                        proxyGet=proxyRepository.getProxySubT1();
                    }else{
                        proxyGet=proxyRepository.getProxySubT2();
                    }
                }
            }
            if(proxyGet==null){
                resp.put("status","fail");
                resp.put("message","Hết proxy khả dụng!" );
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            proxyGet.get(0).setTimeget(System.currentTimeMillis());
            proxyRepository.save(proxyGet.get(0));
            resp.put("status","true");
            resp.put("proxy",proxyGet.get(0).getProxy());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

        } catch (Exception e) {
            resp.put("status", e);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping(value = "/getrpoxybyusersub", produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> getrpoxybyusersub(@RequestParam(defaultValue = "")  String username,@RequestParam(defaultValue = "") String vps,@RequestParam(defaultValue = "") String proxyfail) {
        JSONObject resp = new JSONObject();
        try{
            if(vps.length()==0){
                resp.put("status","fail");
                resp.put("message", "Không để vps trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            if(username.length()==0){
                resp.put("status","fail");
                resp.put("message", "Không để username trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
/*
            Long id=accountRepository.findIdByUsername(username);
            String v4 =accountRepository.CheckProxyByIdSub(id);
            if(v4.length()<5 || v4==null){
                v4= ipV4Repository.getIpv4ByVps();
                ipV4Repository.updateUserCountByIpv4(v4);
                accountRepository.updateProxyById(v4,id);
            }

            if(proxyfail.length()!=0){
                proxyGet=proxyRepository.getProxySubT1();
            }else{
                proxyGet=proxyRepository.getProxySubByIpv4T1(v4);
                if(proxyGet.size()==0){
                    proxyGet=proxyRepository.getProxySubT1();
                }
            }

 */
            if(proxyfail.length()!=0){
                Integer proxyId= proxyRepository.getIdByProxyFalse(proxyfail.trim(),vps);
                //System.out.println(proxyId);
                if(proxyId!=null){
                    proxyRepository.updaterunningProxyByVps(proxyId);
                }
            }
            List<Proxy> proxyGet=null;
            proxyGet=proxyRepository.getProxyAccSub();
            if(proxyGet.size()==0){
                resp.put("status","fail");
                resp.put("message","Hết proxy khả dụng!" );
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            Random random =new Random();
            Thread.sleep(500+random.nextInt(500));
            if(proxyRepository.getRunningProxyById(proxyGet.get(0).getId())==1){
                resp.put("status","fail");
                resp.put("message","Proxy đã được sử dụng! Thử lại" );
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            proxyRepository.updateProxyGet(vps,System.currentTimeMillis(),proxyGet.get(0).getId());
            resp.put("status","true");
            resp.put("proxy",proxyGet.get(0).getProxy());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

        } catch (Exception e) {
        resp.put("status", e.getStackTrace()[0].getLineNumber());
        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
    }

    }


    @GetMapping(value = "/getproxysub", produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> getproxysub(@RequestParam(defaultValue = "")  String username,@RequestParam(defaultValue = "") String vps,@RequestParam(defaultValue = "") String proxyfail,
                                       @RequestParam(defaultValue = "vn") String geo) {
        JSONObject resp = new JSONObject();
        try{
            Random random =new Random();
            int index=0;
            ////////////////////////////////
            if(vps.length()==0){
                resp.put("status","fail");
                resp.put("message", "Không để vps trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            if(username.length()==0){
                resp.put("status","fail");
                resp.put("message", "Không để username trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            if(geo.length()==0){
                resp.put("status","fail");
                resp.put("message", "Không để Geo trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            if(checkProsetListTrue.getValue()>=30){
                resp.put("status","fail");
                resp.put("message", "Đợi proxy...");
                Thread.sleep(1000+random.nextInt(1000));
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            if(proxyfail.length()!=0){
                Integer proxyId= proxyRepository.getIdByProxyFalse(proxyfail.trim(),vps);
                //System.out.println(proxyId);
                if(proxyId!=null){
                    proxyRepository.updaterunningProxyByVps(proxyId);
                }
            }
            while (checkProsetListTrue.getValue()>=30&&index<=6){
                Thread.sleep(500);
                index++;
                if(index==6){
                    resp.put("status","fail");
                    resp.put("message", "Đợi proxy...");
                    Thread.sleep(1000+random.nextInt(1000));
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            }
            List<Proxy> proxyGet=null;
            if(geo.trim().equals("live")){
                proxyGet=proxyRepository.getProxyByGeoNoCheckTime();
            }else{
                proxyGet=proxyRepository.getProxyByGeo(geo.trim());
            }
            if(proxyGet.size()==0){
                resp.put("status","fail");
                resp.put("message","Hết proxy khả dụng!" );
                Thread.sleep(1000+random.nextInt(1000));
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            Thread.sleep(500+random.nextInt(500));
            if(proxyRepository.getRunningProxyById(proxyGet.get(0).getId())==1){
                resp.put("status","fail");
                resp.put("message","Proxy đã được sử dụng! Thử lại" );
                Thread.sleep(1000+random.nextInt(1000));
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            proxyRepository.updateProxyGet(vps,System.currentTimeMillis(),proxyGet.get(0).getId());
            resp.put("status","true");
            resp.put("proxy",proxyGet.get(0).getProxy());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

        } catch (Exception e) {
            resp.put("status", e.getStackTrace()[0].getLineNumber());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }

    }
    @GetMapping(value = "/getproxyliveOFF", produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> getproxyliveOFF(@RequestParam(defaultValue = "") String vps,@RequestParam(defaultValue = "") String proxyfail,
                                        @RequestParam(defaultValue = "vn") String geo) {
        JSONObject resp = new JSONObject();
        try{
            Random random =new Random();
            if(vps.length()==0){
                resp.put("status","fail");
                resp.put("message", "Không để vps trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }

            if(geo.length()==0){
                resp.put("status","fail");
                resp.put("message", "Không để Geo trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            if(proxyfail.length()!=0){
                Integer proxyId= proxyLiveRepository.getIdByProxyLiveFalse(proxyfail.trim(),vps);
                //System.out.println(proxyId);
                if(proxyId!=null){
                    proxyLiveRepository.updaterunningProxyLiveByVps(proxyId);
                }
            }
            List<ProxyLive> proxyGet=null;
            proxyGet=proxyLiveRepository.getProxyLive(geo.trim());
            if(proxyGet.size()==0){
                resp.put("status","fail");
                resp.put("message","Hết proxy khả dụng!" );
                Thread.sleep(1000+random.nextInt(1000));
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            proxyLiveRepository.updateProxyLiveGet(vps,System.currentTimeMillis(),proxyGet.get(0).getId());
            resp.put("status","true");
            resp.put("proxy",proxyGet.get(0).getProxy());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

        } catch (Exception e) {
            resp.put("status", e.getStackTrace()[0].getLineNumber());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }

    }
    @GetMapping(value = "/getproxylive", produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> getproxylive(@RequestParam(defaultValue = "") String vps,
                                       @RequestParam(defaultValue = "vn") String geo) {
        JSONObject resp = new JSONObject();
        try{
            Random random =new Random();
            if(vps.length()==0){
                resp.put("status","fail");
                resp.put("message", "Không để vps trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }

            if(geo.length()==0){
                resp.put("status","fail");
                resp.put("message", "Không để Geo trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            List<Proxy> proxyGet=null;
            proxyGet=proxyRepository.getProxyNotRunningAndLive(geo.trim());
            if(proxyGet.size()==0){
                resp.put("status","fail");
                resp.put("message","Hết proxy khả dụng!" );
                Thread.sleep(1000+random.nextInt(1000));
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            String[] proxy=proxyGet.get(0).getProxy().split(":");
            resp.put("status","true");
            resp.put("proxy",proxy[0] + ":" + proxy[1] + ":1:1");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

        } catch (Exception e) {
            resp.put("status", e.getStackTrace()[0].getLineNumber());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping(value = "/resetrunningproxy", produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> resetrunningproxy(@RequestParam(defaultValue = "") String vps,@RequestParam(defaultValue = "") String proxy) {
        JSONObject resp = new JSONObject();
        try{
            if(vps.length()==0){
                resp.put("status","fail");
                resp.put("message", "Không để vps trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            if(proxy.length()==0){
                resp.put("status","fail");
                resp.put("message", "Không để proxy trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            Integer proxyId= proxyRepository.getIdByProxyLive(proxy.trim(),vps.trim());
            if(proxyId!=null){
                proxyRepository.updaterunningProxyLiveByVps(proxyId);
            }
            resp.put("status","true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", e.getStackTrace()[0].getLineNumber());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping(value = "/resetrunningproxylive", produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> resetrunningproxylive(@RequestParam(defaultValue = "") String vps,@RequestParam(defaultValue = "") String proxy) {
        JSONObject resp = new JSONObject();
        try{
            if(vps.length()==0){
                resp.put("status","fail");
                resp.put("message", "Không để vps trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            if(proxy.length()==0){
                resp.put("status","fail");
                resp.put("message", "Không để proxy trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            Integer proxyId= proxyLiveRepository.getIdByProxyLive(proxy.trim(),vps.trim());
            if(proxyId!=null){
                proxyLiveRepository.updaterunningProxyLiveByVps(proxyId);
            }
            resp.put("status","true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", e.getStackTrace()[0].getLineNumber());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping(value = "/resetproxyByVps", produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> resetproxyByVps(@RequestParam(defaultValue = "") String vps) {
        JSONObject resp = new JSONObject();
        try{
            if(vps.length()==0){
                resp.put("status","fail");
                resp.put("message", "Không để vps trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            proxyRepository.updaterunningByVps(vps.trim());
            resp.put("status","true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", e.getStackTrace()[0].getLineNumber());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping(value = "/resetproxyLiveByVps", produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> resetproxyLiveByVps(@RequestParam(defaultValue = "") String vps) {
        JSONObject resp = new JSONObject();
        try{
            if(vps.length()==0){
                resp.put("status","fail");
                resp.put("message", "Không để vps trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            proxyLiveRepository.updaterunningByVps(vps.trim());
            resp.put("status","true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", e.getStackTrace()[0].getLineNumber());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping(value = "/resetproxyLiveByCron", produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> resetproxyByCron() {
        JSONObject resp = new JSONObject();
        try{
            proxyLiveRepository.ResetProxyThan4h();
            resp.put("status","true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", e.getStackTrace()[0].getLineNumber());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }

    }
/*
    @GetMapping(value = "/checkproxylist", produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> checkproxylist(@RequestParam(defaultValue = "1") Integer cron) {
        List<String> proxys= ipV4Repository.getListIpv4(cron);
        JSONObject resp = new JSONObject();
        //System.out.println(proxys);
        //String[] proxys=proxylist.split("\r\n");
        try{
            String list_check="";
            Integer sum_error=0;
            for(int i=0;i<proxys.size();i++){
                JSONObject obj = new JSONObject();
                Random ran=new Random();
                Integer ranproxy=ran.nextInt(199)+13000;
                System.out.println(ranproxy);
                if (ProxyAPI.checkProxy(proxys.get(i)+":"+ranproxy.toString()+":tunghoanh:Dung1234@")) {
                    ipV4Repository.updateIpv4Ok(System.currentTimeMillis(),"%"+proxys.get(i)+"%");
                }else{
                    List<IpV4> stateAndCheck = ipV4Repository.getStateByIpv4("%"+proxys.get(i)+"%");
                    if (stateAndCheck.get(0).getNumcheck()>=4){
                        list_check=list_check+","+proxys.get(i);
                        sum_error++;
                    }
                    ipV4Repository.updateIpv4Error(System.currentTimeMillis(),"%"+proxys.get(i)+"%");


                }
            }
            if(list_check.length()>0){

                OkHttpClient clientchannel = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                Request requestchannel = null;

                requestchannel = new Request.Builder().url("https://maker.ifttt.com/trigger/noti_proxy_error/with/key/fwentilQadKm7AhNm53u4BCM8OF7BYmOOO0JgPE8qoS?value1="+list_check+"&value2="+sum_error.toString()+"&value3=check_ae_nhe").get().build();

                Response responsechannel = clientchannel.newCall(requestchannel).execute();
            }

            resp.put("list:",list_check);
            resp.put("sum:",sum_error);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);


        } catch (Exception e) {
            resp.put("status",e);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }

    }


 */
    @GetMapping(value = "/checkproxylistphim", produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> checkproxylistphim(@RequestParam(defaultValue = "1") Integer cron) {
        List<String> proxys= ipV4Repository.getListIpv4(cron);
        JSONObject resp = new JSONObject();
        //System.out.println(proxys);
        //String[] proxys=proxylist.split("\r\n");
        try{
            String list_check="";
            Integer sum_error=0;
            for(int i=0;i<proxys.size();i++){
                JSONObject obj = new JSONObject();
                Random ran=new Random();
                Integer ranproxy=ran.nextInt(199)+50000;
                //System.out.println(ranproxy);
                if (ProxyAPI.checkProxy(proxys.get(i)+":"+ranproxy.toString()+":proxy:789789")) {
                    ipV4Repository.updateIpv4Ok(System.currentTimeMillis(),"%"+proxys.get(i)+"%");
                }else{
                    List<IpV4> stateAndCheck = ipV4Repository.getStateByIpv4("%"+proxys.get(i)+"%");
                    if (stateAndCheck.get(0).getNumcheck()>=4){
                        list_check=list_check+","+proxys.get(i);
                        sum_error++;
                    }
                    ipV4Repository.updateIpv4Error(System.currentTimeMillis(),"%"+proxys.get(i)+"%");


                }
            }
            if(list_check.length()>0){

                OkHttpClient clientchannel = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                Request requestchannel = null;

                requestchannel = new Request.Builder().url("https://maker.ifttt.com/trigger/noti_proxy_phim_error/with/key/fwentilQadKm7AhNm53u4BCM8OF7BYmOOO0JgPE8qoS?value1="+list_check+"&value2="+sum_error.toString()+"&value3=check_ae_nhe").get().build();

                Response responsechannel = clientchannel.newCall(requestchannel).execute();
            }

            resp.put("list:",list_check);
            resp.put("sum:",sum_error);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);


        } catch (Exception e) {
            resp.put("status",e);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }

    }

    @GetMapping(value = "/checkproxylistbuffh", produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> checkproxylistbuffh(@RequestParam(defaultValue = "1") Integer cron) {
        List<String> proxys= ipV4Repository.getListIpv4(cron);
        JSONObject resp = new JSONObject();
        //System.out.println(proxys);
        //String[] proxys=proxylist.split("\r\n");
        try{
            String list_check="";
            Integer sum_error=0;
            for(int i=0;i<proxys.size();i++){
                JSONObject obj = new JSONObject();
                Random ran=new Random();
                Integer ranproxy=ran.nextInt(199)+50000;
                //System.out.println(ranproxy);
                if (ProxyAPI.checkProxy(proxys.get(i)+":"+ranproxy.toString()+":proxy:789789")) {
                    ipV4Repository.updateIpv4Ok(System.currentTimeMillis(),proxys.get(i)+"%");
                    Integer checkState=proxyRepository.checkState(0,proxys.get(i)+"%");
                    if(checkState>0){
                        proxyRepository.updateState(1,proxys.get(i)+"%");
                    }
                }else{
                    Integer checkState=proxyRepository.checkState(1,proxys.get(i)+"%");
                    if(checkState>0){
                        proxyRepository.updateState(0,proxys.get(i)+"%");
                    }
                    proxyRepository.updateState(0,proxys.get(i)+"%");
                    List<IpV4> stateAndCheck = ipV4Repository.getStateByIpv4(proxys.get(i)+"%");
                    if (stateAndCheck.get(0).getNumcheck()>=4){
                        list_check=list_check+","+proxys.get(i);
                        sum_error++;
                    }
                    ipV4Repository.updateIpv4Error(System.currentTimeMillis(),proxys.get(i)+"%");


                }
            }
            if(list_check.length()>0){

                OkHttpClient clientchannel = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                Request requestchannel = null;

                requestchannel = new Request.Builder().url("https://maker.ifttt.com/trigger/noti_proxy_error/with/key/fwentilQadKm7AhNm53u4BCM8OF7BYmOOO0JgPE8qoS?value1="+list_check+"&value2="+sum_error.toString()+"&value3=check_ae_nhe").get().build();

                Response responsechannel = clientchannel.newCall(requestchannel).execute();
            }

            resp.put("list:",list_check);
            resp.put("sum:",sum_error);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);


        } catch (Exception e) {
            resp.put("status",e);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }

    }


    @GetMapping(value = "/checkproxylist", produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> checkproxylist(@RequestParam(defaultValue = "1") Integer cron) {
        List<String> proxys= ipV4Repository.getListIpv4(cron);
        JSONObject resp = new JSONObject();
        //System.out.println(proxys);
        //String[] proxys=proxylist.split("\r\n");
        try{
            String list_check="";
            Integer sum_error=0;
            for(int i=0;i<proxys.size();i++){
                JSONObject obj = new JSONObject();
                Random ran=new Random();
                Integer ranproxy=ran.nextInt(99)+13000;
                //System.out.println(proxys.get(i)+":"+ranproxy.toString()+":tunghoanh:Dung1234@");
                if (ProxyAPI.checkProxy(proxys.get(i)+":"+ranproxy.toString()+":doanchinh:Chinhchu123@")) {
                    ipV4Repository.updateIpv4Ok(System.currentTimeMillis(),proxys.get(i));
                    Integer checkState=proxyRepository.checkState(0,proxys.get(i));
                    if(checkState>0){
                        proxyRepository.updateState(1,proxys.get(i));
                    }
                }else{
                    Integer checkState=proxyRepository.checkState(1,proxys.get(i));
                    if(checkState>0){
                        proxyRepository.updateState(0,proxys.get(i));
                    }
                    //proxyRepository.updateState(0,proxys.get(i));
                    List<IpV4> stateAndCheck = ipV4Repository.getStateByIpv4(proxys.get(i));
                    if (stateAndCheck.get(0).getNumcheck()>=4){
                        list_check=list_check+","+proxys.get(i);
                        sum_error++;
                    }
                    ipV4Repository.updateIpv4Error(System.currentTimeMillis(),proxys.get(i));
                }
            }
            if(list_check.length()>0){

                OkHttpClient clientchannel = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                Request requestchannel = null;

                requestchannel = new Request.Builder().url("https://maker.ifttt.com/trigger/noti_proxy_buffh_error/with/key/fwentilQadKm7AhNm53u4BCM8OF7BYmOOO0JgPE8qoS?value1="+list_check+"&value2="+sum_error.toString()+"&value3=check_ae_nhe").get().build();

                Response responsechannel = clientchannel.newCall(requestchannel).execute();
            }

            resp.put("list:",list_check);
            resp.put("sum:",sum_error);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);


        } catch (Exception e) {
            resp.put("status",e);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }

    }

    @GetMapping(value = "/checkproxyviewlist", produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> checkproxyviewlist(@RequestParam(defaultValue = "1") Integer cron) {
        List<String> proxys= ipV4Repository.getListIpv4(cron);
        JSONObject resp = new JSONObject();
        //System.out.println(proxys);
        //String[] proxys=proxylist.split("\r\n");
        try{
            String list_check="";
            Integer sum_error=0;
            for(int i=0;i<proxys.size();i++){
                JSONObject obj = new JSONObject();
                Random ran=new Random();
                Integer ranproxy=ran.nextInt(300)+13000;
                //System.out.println(proxys.get(i)+":"+ranproxy.toString()+":tunghoanh:Dung1234@");
                if (ProxyAPI.checkProxy(proxys.get(i)+":"+ranproxy.toString()+":doanchinh:Chinhchu123@")) {
                    ipV4Repository.updateIpv4Ok(System.currentTimeMillis(),proxys.get(i));
                    Integer checkState=proxyRepository.checkState(0,proxys.get(i));
                    if(checkState>0){
                        proxyRepository.updateState(1,proxys.get(i));
                    }
                }else{
                    Integer checkState=proxyRepository.checkState(1,proxys.get(i));
                    if(checkState>0){
                        proxyRepository.updateState(0,proxys.get(i));
                    }
                    //proxyRepository.updateState(0,proxys.get(i));
                    List<IpV4> stateAndCheck = ipV4Repository.getStateByIpv4(proxys.get(i));
                    if (stateAndCheck.get(0).getNumcheck()>=4){
                        list_check=list_check+","+proxys.get(i);
                        sum_error++;
                    }
                    ipV4Repository.updateIpv4Error(System.currentTimeMillis(),proxys.get(i));
                }
            }
            resp.put("list:",list_check);
            resp.put("sum:",sum_error);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);


        } catch (Exception e) {
            resp.put("status",e);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }

    }



    @GetMapping(value = "/addproxusub", produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> addproxusub() {
        List<String> proxys= ipV4Repository.getListIpv4(20);
        JSONObject resp = new JSONObject();
        JSONArray jsonArray=new JSONArray();
        System.out.println(proxys);
        //String[] proxys=proxylist.split("\r\n");
        try{
            while (true){
                List<Long> listId=accountRepository.getAccountByLimit(proxys.size());
                if(listId.size()==0){
                    break;
                }
                for (int i = 0; i < proxys.size(); i++) {
                    accountRepository.updateProxyAccount(proxys.get(i),listId.get(i));
                }
            }
            resp.put("list:","Ok");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }

    }


    @GetMapping(value = "/addcron", produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> addcron() {
        List<String> id_ipv4= ipV4Repository.getIdByIpv4();
        JSONObject resp = new JSONObject();
        try{
            Integer cron_num=0;
            for(int i=0;i<id_ipv4.size();i++){
                if(i%15==0){
                    cron_num=cron_num+1;
                }
                ipV4Repository.updatecronIpv4(cron_num, Long.parseLong(id_ipv4.get(i)) );
            }
            resp.put("num",cron_num);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }

    }

    @GetMapping(value = "/clearvpsinipv4", produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> clearvpsinipv4() {
        List<IpV4> ipv4= ipV4Repository.getListIpv4();
        JSONObject resp = new JSONObject();
        JSONArray jsonArray=new JSONArray();
        try{
            Integer cron_num=0;
            for(int i=0;i<ipv4.size();i++){
                String[] listvps=ipv4.get(i).getVps().split(",");
                System.out.println(listvps);
                String list_vps_new="";
                Integer num_vps=0;
                JSONObject obj = new JSONObject();
                for(int j=0;j<listvps.length;j++){
                    if(listvps[j].indexOf("WS")>=0){
                        list_vps_new=list_vps_new+","+listvps[j];
                        //System.out.println(list_vps_new);
                        num_vps++;
                    }
                }
                System.out.println(ipv4.get(i).getIpv4()+"-"+list_vps_new);
                ipV4Repository.updateVPSIpv4(list_vps_new,num_vps,ipv4.get(i).getId());
                obj.put(list_vps_new,num_vps);
                jsonArray.add(obj);
            }
            resp.put("status", jsonArray);
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

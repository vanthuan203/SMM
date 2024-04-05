package com.nts.awspremium.controller;

import com.nts.awspremium.ProxyAPI;
import com.nts.awspremium.model.*;
import com.nts.awspremium.model.Proxy;
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
    private ProxySubRepository proxySubRepository;
    @Autowired
    private Proxy_IPV4_TikTokRepository proxyLiveRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private IpV4Repository ipV4Repository;
    @Autowired
    private CheckProsetListTrue checkProsetListTrue;
    @Autowired
    private AuthenIPv4Repository authenIPv4Repository;
    @Autowired
    private Socks_IPV4Repository socksIpv4Repository;
    @Autowired
    private ProxySettingRepository proxySettingRepository;

    @Autowired
    private Proxy_IPV4_TikTokRepository proxyIpv4TikTokRepository;

    @GetMapping(value="/list_authen",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> list_authen(){
        JSONObject resp = new JSONObject();
        try{
            List<String> list_ipv4=authenIPv4Repository.getListAuthen();

            JSONArray jsonArray=new JSONArray();
            for(int i=0;i<list_ipv4.size();i++){
                JSONObject obj = new JSONObject();
                obj.put("ipv4",list_ipv4.get(i).split(",")[0]);
                obj.put("timecheck",list_ipv4.get(i).split(",")[1]);
                obj.put("lockmode",list_ipv4.get(i).split(",")[2]);
                jsonArray.add(obj);
            }
            resp.put("authens",jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value="/list_sock",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> list_sock(){
        JSONObject resp = new JSONObject();
        try{
            List<Socks_IPV4> list_ipv4=socksIpv4Repository.getListSock();

            JSONArray jsonArray=new JSONArray();
            for(int i=0;i<list_ipv4.size();i++){
                JSONObject obj = new JSONObject();
                obj.put("ip",list_ipv4.get(i).getIp());
                obj.put("ipv4",list_ipv4.get(i).getIpv4());
                obj.put("ipv4_old",list_ipv4.get(i).getIpv4_old());
                obj.put("auth",list_ipv4.get(i).getAuth());
                obj.put("timeupdate",list_ipv4.get(i).getTimeupdate());
                jsonArray.add(obj);
            }
            resp.put("socks",jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value="/proxysetting",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> proxysetting(){
        JSONObject resp = new JSONObject();
        try{
            List<ProxySetting> list_ipv4=proxySettingRepository.getProxySetting();

            JSONArray jsonArray=new JSONArray();
            for(int i=0;i<list_ipv4.size();i++){
                JSONObject obj = new JSONObject();
                obj.put("id",list_ipv4.get(i).getId());
                obj.put("option_proxy",list_ipv4.get(i).getOption_proxy());
                obj.put("total_port",list_ipv4.get(i).getTotal_port());
                obj.put("total_sock_port",list_ipv4.get(i).getTotal_sock_port());
                obj.put("username",list_ipv4.get(i).getUsername());
                obj.put("password",list_ipv4.get(i).getPassword());
                obj.put("cron",list_ipv4.get(i).getCron());
                obj.put("timeupdate",list_ipv4.get(i).getTimeupdate());
                jsonArray.add(obj);
            }
            resp.put("proxysetting",jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(path = "updateProxySetting",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateProxySetting(@RequestHeader(defaultValue = "") String Authorization,@RequestBody ProxySetting proxySetting){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> check=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| check.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        ProxySetting proxySetting1=proxySettingRepository.getProxySettingById(proxySetting.getId());
        proxySetting1.setCron(proxySetting.getCron());
        proxySetting1.setPassword(proxySetting.getPassword());
        proxySetting1.setUsername(proxySetting.getUsername());
        proxySetting1.setTotal_port(proxySetting.getTotal_port());
        proxySetting1.setTotal_sock_port(proxySetting.getTotal_sock_port());
        proxySetting1.setTimeupdate(System.currentTimeMillis());
        proxySettingRepository.save(proxySetting1);
        JSONObject obj = new JSONObject();
        obj.put("id",proxySetting1.getId());
        obj.put("option_proxy",proxySetting1.getOption_proxy());
        obj.put("total_port",proxySetting1.getTotal_port());
        obj.put("total_sock_port",proxySetting1.getTotal_sock_port());
        obj.put("username",proxySetting1.getUsername());
        obj.put("password",proxySetting1.getPassword());
        obj.put("cron",proxySetting1.getCron());
        obj.put("timeupdate",proxySetting1.getTimeupdate());
        resp.put("proxysetting",obj);
        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
    }
    @GetMapping(value="/checkAuthenIPv4",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> checkAuthenIPv4(@RequestParam String ipv4,@RequestParam(defaultValue = "0") Integer lock){
        JSONObject resp = new JSONObject();
        try{
            if(authenIPv4Repository.CheckIPv4Exist(ipv4.trim())>0){
                resp.put("status","fail");
                resp.put("message","already exist!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }else{
                AuthenIPv4 authenIPv4=new AuthenIPv4();
                authenIPv4.setIpv4(ipv4.trim());
                authenIPv4.setTimecheck(System.currentTimeMillis());
                authenIPv4.setTimeadd(System.currentTimeMillis());
                if(lock==1){
                    authenIPv4.setLockmode(1);
                }else{
                    authenIPv4.setLockmode(0);
                }
                authenIPv4Repository.save(authenIPv4);
                resp.put("status","true");
                resp.put("message","add authentication successfully!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }
        }catch (Exception e){
            resp.put("status","true");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value="/auth_ips")
    String auth_ips(){
        try{
           List<String> list_ipv4=authenIPv4Repository.getAuthen();
           String authen="";
           for(int i=0;i<list_ipv4.size();i++){
               if(i==0){
                   authen=list_ipv4.get(i);
               }else{
                   authen=authen+","+list_ipv4.get(i);
               }
           }
           return authen;
        }catch (Exception e){
            return "";
        }
    }

    @GetMapping(value="/delauthen",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> delauthen(@RequestParam String ipv4){
        JSONObject resp = new JSONObject();
        try{
            String[] ipv4list = ipv4.split(",");
            for(int i=0;i<ipv4list.length;i++){
                authenIPv4Repository.deleteAuthenByIPV4(ipv4list[i].trim());
            }
            resp.put("status","true");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","true");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(value="/addauthen",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> addauthen(@RequestParam String ipv4){
        JSONObject resp = new JSONObject();
        try{
            String[] ipv4list = ipv4.split(",");
            for(int i=0;i<ipv4list.length;i++){
                checkAuthenIPv4(ipv4list[i].trim(),1);
            }
            resp.put("status","true");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","true");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
    }

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

    @PostMapping(value="/up_state")
    ResponseEntity<String> up_state(@RequestBody JSONObject jsonObject){
        JSONObject resp = new JSONObject();
        try{
            //auth_ips
            List<Socks_IPV4> socksIpv4s=socksIpv4Repository.getIPSocksByIp(jsonObject.get("host").toString());
            if(socksIpv4s.size()==0){
                Socks_IPV4 socksIpv4 =new Socks_IPV4();
                socksIpv4.setIp(jsonObject.get("host").toString());
                socksIpv4.setIpv4("");
                socksIpv4.setIpv4_old("");
                socksIpv4.setTimeupdate(System.currentTimeMillis());
                socksIpv4.setAuth(jsonObject.get("auth_ips").toString());
                socksIpv4Repository.save(socksIpv4);
            }else{
                socksIpv4s.get(0).setTimeupdate(System.currentTimeMillis());
                socksIpv4s.get(0).setAuth(jsonObject.get("auth_ips").toString());
                socksIpv4Repository.save(socksIpv4s.get(0));
            }
            resp.put("status","true");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
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

    @GetMapping(value="/proxySetting")
    ResponseEntity<String> proxySetting(@RequestParam(defaultValue = "") String host){
        JSONObject resp = new JSONObject();
        if(host.length()==0){
            resp.put("status","fail");
            resp.put("message", "Không để IP trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            List<IpV4> ipV4=ipV4Repository.getStateByIpv4(host.trim());
            /*
            if(ipV4.size()>0){
                ipV4.get(0).setTimecheck(System.currentTimeMillis());
                ipV4.get(0).setState(1);
                ipV4Repository.save(ipV4.get(0));
            }
             */
            ProxySetting proxySetting=proxySettingRepository.getProxySettingByOption(host.trim());
            if(proxySetting==null){
                proxySetting=proxySettingRepository.getProxySettingById();
            }
            resp.put("username",proxySetting.getUsername());
            resp.put("password",proxySetting.getPassword());
            resp.put("total_port",proxySetting.getTotal_port());
            resp.put("total_sock_port",proxySetting.getTotal_sock_port());
            resp.put("update_ip_version",proxySetting.getUpdate_ip_version());
            resp.put("update_ip_url",proxySetting.getUpdate_ip_url());
            resp.put("create_version",proxySetting.getCreate_version());
            resp.put("create_url",proxySetting.getCreate_url());
            resp.put("crontab_version",proxySetting.getCrontab_version());
            resp.put("crontab_url",proxySetting.getCrontab_url());
            resp.put("cron",proxySetting.getCron());
            List<String> list_ipv4=authenIPv4Repository.getAuthen();
            String authen="";
            for(int i=0;i<list_ipv4.size();i++){
                if(i==0){
                    authen=list_ipv4.get(i);
                }else{
                    authen=authen+","+list_ipv4.get(i);
                }
            }
            resp.put("auth_ips",authen);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value="/checkIPV4")
    ResponseEntity<String> checkIPV4(@RequestParam(defaultValue = "") String host,@RequestParam(defaultValue = "") String ipv4){
        JSONObject resp = new JSONObject();
        if(host.length()==0){
            resp.put("status","fail");
            resp.put("message", "Không để IP trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if(ipv4.length()==0){
            resp.put("status","fail");
            resp.put("message", "Không để IPV4 trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            List<Socks_IPV4> socksIpv4s=socksIpv4Repository.getIPSocksByIp(host.trim());
            if(socksIpv4s.size()==0){
                resp.put("status","true");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }else{
                if(socksIpv4s.get(0).getIpv4().equals(ipv4.trim())){
                    resp.put("status","true");
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                }else{
                    socksIpv4s.get(0).setIpv4_old(socksIpv4s.get(0).getIpv4()+(socksIpv4s.get(0).getIpv4_old().length()==0?"":(","+socksIpv4s.get(0).getIpv4_old())));
                    socksIpv4s.get(0).setIpv4(ipv4.trim());
                    socksIpv4s.get(0).setTimeupdate(System.currentTimeMillis());
                    socksIpv4Repository.save(socksIpv4s.get(0));
                }
            }
            resp.put("status","true");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(value="/addipv4",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> addipv4(@RequestParam String ipv4,@RequestParam(defaultValue = "") String option_setting){
        JSONObject resp = new JSONObject();
        if(option_setting.length()==0){
            resp.put("status","fail");
            resp.put("message", "Không để option_setting trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            String[] ipv4list = ipv4.split(",");
            for(int i=0;i<ipv4list.length;i++){
                IpV4 ipV4_Check=ipV4Repository.getIpv4(ipv4list[i].trim());
                if(ipV4_Check==null){
                    IpV4 ipV4=new IpV4();
                    ipV4.setState(1);
                    ipV4.setIpv4(ipv4list[i].trim());
                    ipV4.setNumcheck(0);
                    ipV4.setTimecheck(0L);
                    ipV4.setOption_setting(option_setting.trim());
                    ipV4Repository.save(ipV4);
                }else{
                    ipV4_Check.setOption_setting(option_setting.trim());
                    ipV4Repository.save(ipV4_Check);
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

    @GetMapping(value="/tiktok",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> tiktok(@RequestHeader(defaultValue = "") String Authorization) {
        JSONObject resp = new JSONObject();
        Integer checktoken = adminRepository.FindAdminByToken(Authorization);
        if (checktoken == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            String proxy=proxyIpv4TikTokRepository.getProxyRandTikTok();
            if(proxy!=null){
                resp.put("status","true");
                resp.put("proxy",proxy);
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }else{
                resp.put("proxy","fail");
                resp.put("message", "Hết proxy khả dụng");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }
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
            List<IpV4> list_ipv4=ipV4Repository.getListV4_NEW();

            JSONArray jsonArray=new JSONArray();
            for(int i=0;i<list_ipv4.size();i++){
                JSONObject obj = new JSONObject();
                obj.put("ipv4",list_ipv4.get(i).getIpv4());
                obj.put("totalport",0);
                obj.put("option_setting",list_ipv4.get(i).getOption_setting());
                obj.put("timecheck",list_ipv4.get(i).getTimecheck());
                obj.put("state",list_ipv4.get(i).getState());
                obj.put("geo","");
                obj.put("numcheck",list_ipv4.get(i).getNumcheck());
                obj.put("typeproxy","");
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
                Integer ranproxy=ran.nextInt(150)+13000;
                if (ProxyAPI.checkProxy(proxys.get(i)+":"+ranproxy.toString()+":tunghoanh:Dung1234@")) {
                    ipV4Repository.updateIpv4Ok(System.currentTimeMillis(),proxys.get(i));

                }else{
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
                Integer ranproxy=ran.nextInt(100)+13000;
                String[] proxy={""};
                try{
                    proxy=proxyRepository.getProxyByIpv4(proxys.get(i)).split(":");
                }catch (Exception e){
                    try{
                        proxy=proxySubRepository.getProxyByIpv4(proxys.get(i)).split(":");
                    }catch (Exception f){
                        proxy= new String[]{"1", "1", "1", "1"};
                    }
                }
                String userpass=":doanchinh:Chinhchu123@";
                if(!proxy[2].equals("1")){
                    userpass=":"+proxy[2]+":"+proxy[3];
                }
                System.out.println(userpass);
                if (ProxyAPI.checkProxy(proxys.get(i)+":"+ranproxy.toString()+userpass)) {
                    ipV4Repository.updateIpv4Ok(System.currentTimeMillis(),proxys.get(i));

                }else{
                    List<IpV4> stateAndCheck = ipV4Repository.getStateByIpv4(proxys.get(i));
                    if (stateAndCheck.get(0).getNumcheck()>=0){
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

    void checkproxyMain(@RequestParam(defaultValue = "1") Integer cron) {
        List<String> proxys= ipV4Repository.getListIpv4(cron);

        try{
            for(int i=0;i<proxys.size();i++){
                Random ran=new Random();
                Integer ranproxy=ran.nextInt(100)+13000;
                String[] proxysetting=proxySettingRepository.getUserPassByHost(proxys.get(i).trim()).split(",");
                if (ProxyAPI.checkProxy(proxys.get(i)+":"+ranproxy.toString()+":"+proxysetting[0]+":"+proxysetting[1])) {
                    ipV4Repository.updateIpv4Ok(System.currentTimeMillis(),proxys.get(i));

                }else{
                    ipV4Repository.updateIpv4Error(System.currentTimeMillis(),proxys.get(i));
                }
            }
            if(proxys.size()==0){
                //System.out.println("Thread "+cron+" Watting 60s...");
                Thread.sleep(60000);
            }



        } catch (Exception e) {

        }

    }





    @GetMapping(value = "/addcron", produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> addcron() {
        List<String> id_ipv4= ipV4Repository.getIdByIpv4();
        JSONObject resp = new JSONObject();
        try{
            Integer cron_num=0;
            for(int i=0;i<id_ipv4.size();i++){
                if(i%45==0){
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

}

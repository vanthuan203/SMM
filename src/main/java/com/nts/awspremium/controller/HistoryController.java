package com.nts.awspremium.controller;

import com.nts.awspremium.StringUtils;
import com.nts.awspremium.model.*;
import com.nts.awspremium.repositories.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/history")
public class HistoryController {
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private ChannelRepository channelRepository;
    @Autowired
    private ProxyRepository proxyRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private VideoRepository videoRepository;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private HistorySumRepository historySumRepository;
    @Autowired
    private ProxyHistoryRepository proxyHistoryRepository;
    @Autowired
    private IpV4Repository ipV4Repository;
    @GetMapping(value = "get",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String username, @RequestParam(defaultValue = "") String vps,@RequestParam(defaultValue = "0") Long endtrial){
        JSONObject resp=new JSONObject();
        Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        if(checktoken==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        if (username.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (endtrial== 0) {
            resp.put("status", "fail");
            resp.put("message", "endtrial không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{

            List<History> histories=historyRepository.get(username);
            List<Video> videos;
            if(histories.size()==0){
                History history=new History();
                history.setId(System.currentTimeMillis());
                history.setUsername(username);
                history.setListvideo("");
                history.setProxy("");
                history.setRunning(1);
                history.setVps(vps);
                history.setEndtrial(endtrial);
                history.setTimeget(System.currentTimeMillis());
                //buff video + cheat view thunog
                videos=videoRepository.getvideobuff("");
                if(videos.size()==0){
                    videos=videoRepository.getvideo("");
                }
                if(videos.size()>0){
                    history.setChannelid(videos.get(0).getChannelid());
                }
                historyRepository.save(history);
            }else{
                if(!histories.get(0).getVps().trim().equals(vps.trim()) && (histories.get(0).getVps().length()!=0)){
                    resp.put("status","fail");
                    resp.put("fail", "nouser");
                    resp.put("message", "Tài khoản đã được vps khác sử dụng!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                histories.get(0).setRunning(1);
                histories.get(0).setVps(vps);

                histories.get(0).setEndtrial(endtrial);
                histories.get(0).setTimeget(System.currentTimeMillis());
                videos=videoRepository.getvideobuff(histories.get(0).getListvideo());
                if(videos.size()==0){
                    videos=videoRepository.getvideo(histories.get(0).getListvideo());
                }
                if(videos.size()>0){
                    histories.get(0).setChannelid(videos.get(0).getChannelid());
                }
                historyRepository.save(histories.get(0));
            }
            List<History> historyendtrial=historyRepository.checkEndTrial(username);
            if(historyendtrial.size()==0){
                if(histories.size()>0){
                    histories.get(0).setRunning(0);
                    histories.get(0).setVps("");
                    historyRepository.save(histories.get(0));
                }
                resp.put("status", "fail");
                resp.put("fail", "trial");
                resp.put("message", "Hết hạn endtrial!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            if(videos.size()==0){
                resp.put("status","fail");
                resp.put("fail","video");
                resp.put("message", "Không còn video để view!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }else{
                //List<Account> account=accountRepository.findAccountByUsername(username);
                List<Channel> channels=channelRepository.getChannelById(videos.get(0).getChannelid());
                List<Proxy> proxy=null;
                String ref = "";
                boolean isPremium = false;
                if (new Random().nextInt(100) < channels.get(0).getPremiumrate()) {
                    isPremium = true;
                }
                boolean isMobile = false;
                if (new Random().nextInt(100) < channels.get(0).getMobilerate()) {
                    isMobile = true;
                }
                List<String> arrRefs = new ArrayList<>();
                for (int i = 0; i < channels.get(0).getHomerate(); i++) {
                    arrRefs.add("home");
                }
                for (int i = 0; i < channels.get(0).getSearchrate(); i++) {
                    arrRefs.add("search");
                }
                for (int i = 0; i < channels.get(0).getSuggestrate(); i++) {
                    arrRefs.add("suggest");
                }
                for (int i = 0; i < channels.get(0).getDirectrate(); i++) {
                    arrRefs.add("direct");
                }
                ref = arrRefs.get(new Random().nextInt(arrRefs.size()));
                try{
                    if(histories.size()==0){
                        //proxy=proxyRepository.getProxyTimeGetNull();
                        //if(proxy.size()==0){
                          proxy=proxyRepository.getProxy();
                        //}
                    }else{
                        if(histories.get(0).getProxy().length()==0 || histories.get(0).getProxy()==null){
                            //proxy=proxyRepository.getProxyTimeGetNull();
                            //if(proxy.size()==0){
                                proxy=proxyRepository.getProxy();
                            //}
                        }else{
                            //proxy=proxyRepository.getProxyTimeGetNull(StringUtils.getProxyhost(histories.get(0).getProxy()));
                            //if(proxy.size()==0){
                                proxy=proxyRepository.getProxy(StringUtils.getProxyhost(histories.get(0).getProxy()));
                            //}
                        }
                    }
                    if(histories.size()==0){
                        histories=historyRepository.get(username);
                    }
                    histories.get(0).setProxy(proxy.get(0).getProxy());
                    historyRepository.save(histories.get(0));


                    proxy.get(0).setTimeget(System.currentTimeMillis());
                    proxy.get(0).setRunning(proxy.get(0).getRunning()+1);
                    proxyRepository.save(proxy.get(0));

                    ProxyHistory proxyHistory =new ProxyHistory();
                    proxyHistory.setId(System.currentTimeMillis());
                    proxyHistory.setProxy(proxy.get(0).getProxy());
                    proxyHistory.setState(1);
                    proxyHistory.setIpv4(proxy.get(0).getIpv4());
                    proxyHistoryRepository.save(proxyHistory);

                    IpV4 ipV4=new IpV4();
                    ipV4.setId(System.currentTimeMillis());
                    ipV4.setIpv4(proxy.get(0).getIpv4());
                    ipV4.setState(1);
                    ipV4Repository.save(ipV4);


                    HistorySum historySum=new HistorySum();
                    historySum.setId(System.currentTimeMillis());
                    historySum.setUsername(username);
                    historySum.setVideoid(videos.get(0).getVideoid());
                    historySum.setChannelid(videos.get(0).getChannelid());
                    historySum.setVps(vps);
                    historySum.setProxy(proxy.get(0).getProxy());
                    historySumRepository.save(historySum);

                }catch (Exception e){
                    resp.put("status","fail1");
                    resp.put("fail","proxy");
                    resp.put("message",e.getMessage());
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
                }

                resp.put("ref", ref);
                resp.put("channel_id", channels.get(0).getChannelid());
                //resp.put("title", channels.get(0).getTitle());
                resp.put("isPremium", isPremium ? 1 : 0);
                resp.put("isMobile", isMobile ? "Mobile" : "PC");
                resp.put("status", "true");
                resp.put("video_id",videos.get(0).getVideoid());
                resp.put("video_title", videos.get(0).getTitle());
                //resp.put("username", username);
                resp.put("proxy", proxy.get(0).getProxy());
                resp.put("video_duration", videos.get(0).getDuration());
                //resp.put("password", account.get(0).getPassword());
                //resp.put("recover", account.get(0).getRecover());
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }

        }catch (Exception e){
            resp.put("status","fail");
            resp.put("fail","sum");
            resp.put("message",e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/updatevideoid",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updatevideoid(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String username,
                                  @RequestParam(defaultValue = "") String videoid){
        JSONObject resp=new JSONObject();
        Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        if(checktoken==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        if (username.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (videoid.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "videoid không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            List<History> histories =historyRepository.get(username);
            if(histories.size()==0){
                resp.put("status", "fail");
                resp.put("message", "Không tìm thấy username!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            else{
                //histories.get(0).setProxy(proxy);
                //histories.get(0).setRunning(0);
                if(histories.get(0).getListvideo().length()==0){
                    histories.get(0).setListvideo(videoid);
                }else{
                    histories.get(0).setListvideo(histories.get(0).getListvideo()+","+videoid);
                }
                historyRepository.save(histories.get(0));
                resp.put("status", "true");
                resp.put("message", "Update hitory thành công!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        }catch (Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(value = "/update",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> update(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String username,
                                  @RequestParam(defaultValue = "") String videoid,@RequestParam(defaultValue = "") String proxy){
        JSONObject resp=new JSONObject();
        Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        if(checktoken==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        if (username.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (videoid.length() == 0) {
            List<History> histories =historyRepository.get(username);
            if(histories.size()==0){
                resp.put("status", "fail");
                resp.put("message", "Không tìm thấy username!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            else{
                histories.get(0).setRunning(0);
                historyRepository.save(histories.get(0));
                resp.put("status", "true");
                resp.put("message", "Update hitory thành công!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        }
        if (proxy.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Proxy không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            List<History> histories =historyRepository.get(username);
            if(histories.size()==0){
                resp.put("status", "fail");
                resp.put("message", "Không tìm thấy username!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            else{
                //histories.get(0).setProxy(proxy);
                histories.get(0).setRunning(0);
                historyRepository.save(histories.get(0));
                List<Proxy> proxys=proxyRepository.findProxy(proxy);
                if(proxys.get(0).getRunning()>=1){
                    proxys.get(0).setRunning(proxys.get(0).getRunning()-1);
                    proxyRepository.save(proxys.get(0));
                }
                resp.put("status", "true");
                resp.put("message", "Update hitory thành công!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        }catch (Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value = "delthreadbyusername",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delthreadbyusername(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String username,@RequestParam(defaultValue = "") String videoid){
        JSONObject resp=new JSONObject();
        Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        if(checktoken==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        if (username.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (videoid.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "videoid không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            List<History> histories =historyRepository.get(username);
            List<Proxy> proxys=proxyRepository.findProxy(histories.get(0).getProxy());
            if(proxys.get(0).getRunning()>=1){
                proxys.get(0).setRunning(proxys.get(0).getRunning()-1);
                proxyRepository.save(proxys.get(0));
            }
            historyRepository.resetThreadByUsername(username);
            historySumRepository.DelHistoryError(username,videoid);
            resp.put("status", "true");
            resp.put("message", "Update running thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch(Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(value = "delnamebyvps",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delnamebyvps(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String vps){
        JSONObject resp=new JSONObject();
        Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        if(checktoken==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            List<History> histories=historyRepository.findHistoriesByVps(vps);
            if(histories.size()>0){
                for (int i = 0; i < histories.size(); i++) {
                    List<Proxy> proxies=proxyRepository.findProxy(histories.get(i).getProxy());
                    if(proxies.size()>0 && proxies.get(0).getRunning()>0){
                        proxies.get(0).setRunning(proxies.get(0).getRunning()-1);
                        proxyRepository.save(proxies.get(0));
                    }
                }
            }
            historyRepository.deletenamevpsByVps(vps);
            resp.put("status", "true");
            resp.put("message", "Update running thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch(Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(path = "vpsrunning",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> vpsrunning(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            List<VpsRunning> vpsRunnings=historyRepository.getvpsrunning();

            //String a=orderRunnings.toString();
            JSONArray jsonArray= new JSONArray();

            //JSONObject jsonObject=new JSONObject().put("")
            //JSONObject jsonObject= (JSONObject) new JSONObject().put("Channelid",orderRunnings.get(0).toString());
            //jsonArray.add(orderRunnings);
            Integer sum_total=0;
            for(int i=0;i<vpsRunnings.size();i++){
                sum_total=sum_total+vpsRunnings.get(i).getTotal();
                JSONObject obj = new JSONObject();
                obj.put("vps", vpsRunnings.get(i).getVps());
                obj.put("total", vpsRunnings.get(i).getTotal());
                obj.put("time",  vpsRunnings.get(i).getTime());
                jsonArray.add(obj);
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");


            resp.put("computers",jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
}

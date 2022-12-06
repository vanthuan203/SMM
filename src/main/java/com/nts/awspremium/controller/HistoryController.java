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

import static java.lang.Thread.sleep;

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
    private HistoryViewRepository historyViewRepository;
    @Autowired
    private ProxyHistoryRepository proxyHistoryRepository;
    @Autowired
    private IpV4Repository ipV4Repository;
    @GetMapping(value = "get",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String username, @RequestParam(defaultValue = "") String vps,@RequestParam(defaultValue = "0") Long endtrial,@RequestParam(defaultValue = "0") Integer test){
        JSONObject resp=new JSONObject();
        if(!Authorization.equals("1")){
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
        try{
            Long  historieId=historyRepository.getId(username);
            List<Video> videos;
            if(historieId==null){
                History history=new History();
                history.setId(System.currentTimeMillis());
                history.setUsername(username);
                history.setListvideo("");
                history.setProxy("");
                history.setRunning(0);
                history.setVps(vps);
                history.setTimeget(System.currentTimeMillis());
                if(test==2){
                    videos=videoRepository.getvideobuff("");
                }else{
                    videos=videoRepository.getvideo("");
                }
                if(videos.size()>0){
                    history.setChannelid(videos.get(0).getChannelid());
                }else{
                    historyRepository.save(history);
                    resp.put("status", "fail");
                    resp.put("fail", "video");
                    resp.put("message", "Không còn video để view!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                List<Proxy> proxy = null;
                proxy = proxyRepository.getProxyUpdate();
                if (proxy == null) {
                    history.setProxy("");
                    historyRepository.save(history);
                    resp.put("status", "fail");
                    resp.put("message", "Không còn proxy để sử dụng!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                } else {
                    history.setProxy(proxy.get(0).getProxy().trim());
                }
                historyRepository.save(history);
                proxy.get(0).setTimeget(System.currentTimeMillis());
                //proxy.get(0).setRunning(proxy.get(0).getRunning()+1);
                proxyRepository.save(proxy.get(0));

                //resp.put("ref", ref);
                resp.put("channel_id", videos.get(0).getChannelid());
                //resp.put("title", channels.get(0).getTitle());
                //resp.put("isPremium", isPremium ? 1 : 0);
                //resp.put("isMobile", isMobile ? "Mobile" : "PC");
                resp.put("status", "true");
                resp.put("video_id", videos.get(0).getVideoid());
                resp.put("video_title", videos.get(0).getTitle());
                //resp.put("username", username);
                resp.put("proxy", proxy.get(0).getProxy());
                resp.put("video_duration", videos.get(0).getDuration());
                //resp.put("password", account.get(0).getPassword());
                //resp.put("recover", account.get(0).getRecover());
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }else{
                List<History> histories=historyRepository.getHistoriesById(historieId);
                histories.get(0).setRunning(0);
                histories.get(0).setVps(vps);
                histories.get(0).setTimeget(System.currentTimeMillis());
                if(test==2){
                    videos=videoRepository.getvideobuff(histories.get(0).getListvideo());
                }else{
                    videos=videoRepository.getvideo(histories.get(0).getListvideo());
                }
                if(videos.size()>0){
                    histories.get(0).setChannelid(videos.get(0).getChannelid());
                }else{
                    historyRepository.save(histories.get(0));
                    resp.put("status", "fail");
                    resp.put("fail", "video");
                    resp.put("message", "Không còn video để view!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                List<Proxy> proxy = null;
                if (histories.get(0).getProxy().length() == 0 || histories.get(0).getProxy() == null) {
                    proxy = proxyRepository.getProxyUpdate();
                } else {
                    proxy = proxyRepository.getProxyUpdate(StringUtils.getProxyhost(histories.get(0).getProxy()));
                }
                if (proxy == null){
                    histories.get(0).setProxy("");
                    historyRepository.save(histories.get(0));
                    resp.put("status", "fail");
                    resp.put("message", "Không còn proxy để sử dụng!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }else{
                    histories.get(0).setProxy(proxy.get(0).getProxy().trim());
                }
                historyRepository.save(histories.get(0));
                proxy.get(0).setTimeget(System.currentTimeMillis());
                //proxy.get(0).setRunning(proxy.get(0).getRunning()+1);
                proxyRepository.save(proxy.get(0));
                //resp.put("ref", ref);
                resp.put("channel_id", videos.get(0).getChannelid());
                //resp.put("title", channels.get(0).getTitle());
                //resp.put("isPremium", isPremium ? 1 : 0);
                //resp.put("isMobile", isMobile ? "Mobile" : "PC");
                resp.put("status", "true");
                resp.put("video_id", videos.get(0).getVideoid());
                resp.put("video_title", videos.get(0).getTitle());
                //resp.put("username", username);
                resp.put("proxy", proxy.get(0).getProxy());
                resp.put("video_duration", videos.get(0).getDuration());
                //resp.put("password", account.get(0).getPassword());
                //resp.put("recover", account.get(0).getRecover());
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
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
        if(!Authorization.equals("1")){
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

                HistoryView historyView = new HistoryView();
                historyView.setVideoid(videoid.trim());
                historyView.setTime(System.currentTimeMillis());
                try {
                    historyViewRepository.save(historyView);
                } catch (Exception e) {
                    try {
                        historyViewRepository.save(historyView);
                    } catch (Exception f) {
                    }
                }
            //Thread.sleep((long)(Math.random() * 20000));
            Long  historieId=historyRepository.getId(username);
            if(historieId==null){
                resp.put("status", "fail");
                resp.put("message", "Không tìm thấy username!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            else{
                List<History> histories =historyRepository.getHistoriesById(historieId);
                if(histories.get(0).getListvideo().length()==0){
                    histories.get(0).setListvideo(videoid);
                }else{
                    histories.get(0).setListvideo(histories.get(0).getListvideo()+","+videoid);
                }
                histories.get(0).setRunning(1);
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
                                  @RequestParam(defaultValue = "") String videoid,@RequestParam(defaultValue = "") String proxy,@RequestParam(defaultValue = "") String channelid,@RequestParam(defaultValue = "0") Integer duration){
        JSONObject resp=new JSONObject();
        if(!Authorization.equals("1")){
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

            Long  historieId=historyRepository.getId(username);
            if(historieId==null){
                resp.put("status", "fail");
                resp.put("message", "Không tìm thấy username!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            else{
                List<History> histories =historyRepository.getHistoriesById(historieId);
                //histories.get(0).setProxy(proxy);
                histories.get(0).setRunning(0);
                histories.get(0).setVps("");
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
    @GetMapping(value = "delthreadbyusername",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delthreadbyusername(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String username,@RequestParam(defaultValue = "") String videoid){
        JSONObject resp=new JSONObject();
        if(!Authorization.equals("1")){
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
            Long  historieId=historyRepository.getId(username);
            historyRepository.resetThreadById(historieId);
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
        if(!Authorization.equals("1")){
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
            List<Long> ListId=historyRepository.getHistoryIdbyVps(vps.trim());
            if(ListId.size()!=0){
                for(int i=0;i<ListId.size();i++){
                    //Mai nho sua lai
                    historyRepository.resetThreadById(ListId.get(i));
                }
                resp.put("status", "true");
                resp.put("message", "Update running thành công!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            //historyRepository.deletenamevpsByVps(ListId);
            //historyRepository.deletenamevpsByVps(ListId);
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
    @GetMapping(path = "deleteviewthan24h",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> deleteAllViewThan24h(){
        JSONObject resp = new JSONObject();
        try{
            historyRepository.deleteAllViewThan24h();
            resp.put("status","true");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
}

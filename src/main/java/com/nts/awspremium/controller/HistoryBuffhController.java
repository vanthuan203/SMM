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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/historybuffh")
public class HistoryBuffhController {
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
    private VideoBuffhRepository videoBuffhRepository;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private HistoryViewRepository historyViewRepository;
    @Autowired
    private HistorySumRepository historySumRepository;
    @Autowired
    private ProxyHistoryRepository proxyHistoryRepository;
    @Autowired
    private IpV4Repository ipV4Repository;
    @GetMapping(value = "get",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String vps,@RequestParam(defaultValue = "0") Integer test){
        JSONObject resp=new JSONObject();
        if(!Authorization.equals("1")){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        Random ran=new Random();
            try{
                Long  historieId=historyRepository.getIdAccBuffNoCheckTime24h(vps.trim()+"%");
                if(historieId==null){
                    resp.put("status", "fail");
                    resp.put("message", "Không còn user phù hợp !");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }else{
                    List<VideoBuffh> videos=null;
                    List<History> histories=historyRepository.getHistoriesById(historieId);
                    //histories.get(0).setVps(vps);
                    histories.get(0).setTimeget(System.currentTimeMillis());
                    if(test==1){
                        videos=videoBuffhRepository.getvideobuffhVer2NoCheckTime24h(histories.get(0).getListvideo(),2);
                    }else if(test==2){
                        videos=videoBuffhRepository.getvideobuffhVer2NoCheckTime24h(histories.get(0).getListvideo(),3);
                    }else{
                        videos=videoBuffhRepository.getvideobuffhVer2NoCheckTime24h(histories.get(0).getListvideo(),1);
                    }
                    //videos=videoRepository.getvideo(histories.get(0).getListvideo());
                    if(videos.size()>0){
                        histories.get(0).setVideoid(videos.get(0).getVideoid());
                        histories.get(0).setChannelid(videos.get(0).getChannelid());
                    }else{
                        historyRepository.save(histories.get(0));
                        resp.put("status", "fail");
                        resp.put("username",histories.get(0).getUsername());
                        resp.put("fail", "video");
                        resp.put("message", "Không còn video để view!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }

                    //add thong tin channel
                    List<Channel> channels=channelRepository.getChannelById(videos.get(0).getChannelid());

                    List<Proxy> proxy = null;
                    if (histories.get(0).getProxy().length() == 0 || histories.get(0).getProxy() == null) {
                        proxy=proxyRepository.getProxyBuffByUsername(histories.get(0).getUsername().trim());
                    } else {
                        proxy=proxyRepository.getProxyBuffByIpv4ByUsername(histories.get(0).getUsername().trim(),StringUtils.getProxyhost(histories.get(0).getProxy()));
                    }
                    if (proxy.size()==0){
                        histories.get(0).setProxy("");
                        historyRepository.save(histories.get(0));
                        resp.put("status", "fail");
                        resp.put("message", "Không còn proxy để sử dụng!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }else{
                        histories.get(0).setProxy(proxy.get(0).getProxy().trim());
                    }
                    histories.get(0).setRunning(1);
                    historyRepository.save(histories.get(0));
                    proxy.get(0).setTimeget(System.currentTimeMillis());
                    try{
                        ProxyHistory proxyHistory=new ProxyHistory();
                        proxyHistory.setId(System.currentTimeMillis());
                        proxyHistory.setIpv4(proxy.get(0).getIpv4());
                        proxyHistoryRepository.save(proxyHistory);
                    }catch (Exception e){

                    }
                    proxy.get(0).setRunning(1);
                    proxyRepository.save(proxy.get(0));
                    //resp.put("ref", ref);
                    resp.put("channel_id", videos.get(0).getChannelid());
                    //resp.put("title", channels.get(0).getTitle());
                    //resp.put("isPremium", isPremium ? 1 : 0);
                    //resp.put("isMobile", isMobile ? "Mobile" : "PC");
                    resp.put("status", "true");
                    resp.put("video_id", videos.get(0).getVideoid());
                    resp.put("video_title", videos.get(0).getVideotitle());
                    resp.put("geo",histories.get(0).getGeo());
                    resp.put("username", histories.get(0).getUsername());
                    resp.put("proxy", proxy.get(0).getProxy());
                    if(videos.get(0).getDuration()<3600){
                        if(videos.get(0).getDuration()>1920){
                            resp.put("video_duration", 1850+ran.nextInt(60));
                        }else{
                            resp.put("video_duration", videos.get(0).getDuration());
                        }
                    }else if(videos.get(0).getDuration()<7200){
                        if(videos.get(0).getDuration()>3780){
                            resp.put("video_duration", 3710+ran.nextInt(60));
                        }else{
                            resp.put("video_duration", videos.get(0).getDuration());
                        }
                    }else{
                        if(videos.get(0).getDuration()>7350){
                            resp.put("video_duration", 7280+ran.nextInt(60));
                        }else{
                            resp.put("video_duration", videos.get(0).getDuration());
                        }
                    }
                    //resp.put("video_duration", videos.get(0).getDuration());
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
                                  @RequestParam(defaultValue = "") String videoid,@RequestParam(defaultValue = "") String channelid,@RequestParam(defaultValue = "0") Integer duration){
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
/*
                HistoryView historyView = new HistoryView();
                historyView.setVideoid(videoid.trim());
                historyView.setUsername(username.trim());
                historyView.setTime(System.currentTimeMillis());
                historyView.setChannelid(channelid);
                historyView.setDuration(duration);
                try {
                    historyViewRepository.save(historyView);
                } catch (Exception e) {
                    try {
                        historyViewRepository.save(historyView);
                    } catch (Exception f) {
                    }
                }

 */
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
                //histories.get(0).setRunning(1);
                historyRepository.save(histories.get(0));
                resp.put("status", "true");
                resp.put("message", "Update videoid vào history thành công!");
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
                                  @RequestParam(defaultValue = "") String videoid,@RequestParam(defaultValue = "") String channelid,@RequestParam(defaultValue = "0") Integer duration){
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
            resp.put("message", "Videoid không để trống");
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
                proxyRepository.updaterunning(histories.get(0).getProxy());
                //histories.get(0).setProxy(proxy);
                //histories.get(0).setRunning(0);
                //histories.get(0).setVideoid("");
                //histories.get(0).setVps("");
                //historyRepository.save(histories.get(0));
                Integer check_duration= historyRepository.checkDurationBuffhByTimecheck(username.trim(),(long)(duration));
                if(check_duration>0){
                    HistorySum historySum = new HistorySum();
                    historySum.setVideoid(videoid.trim());
                    historySum.setUsername(username);
                    historySum.setTime(System.currentTimeMillis());
                    historySum.setChannelid(channelid);
                    historySum.setDuration(duration);
                    try {
                        historySumRepository.save(historySum);
                    } catch (Exception e) {
                        try {
                            historySumRepository.save(historySum);
                        } catch (Exception f) {
                        }
                    }
                    resp.put("status", "true");
                    resp.put("message", "Update duration thành công!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                //historyViewRepository.updateduration(duration,username,videoid);
                resp.put("status", "fail");
                resp.put("message", "Không update duration !");
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
            historyRepository.resetThreadBuffhById(historieId);
            //historyViewRepository.deleteHistoryView(username,videoid);
            resp.put("status", "true");
            resp.put("message", "Update running thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch(Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(value = "gettimebuff7day",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> gettimebuff7day(@RequestParam(defaultValue = "") String user){
        JSONObject resp=new JSONObject();
        try{
            List<String> time7day;
            if(user.length()==0){
                time7day=historySumRepository.Gettimebuff7day();
            }else{
                time7day=historySumRepository.Gettimebuff7day(user.trim());
            }

            JSONArray jsonArray=new JSONArray();
            Integer maxtime=0;
            Integer maxview=0;

            for(int i=0;i<time7day.size();i++){
                //System.out.println(time7day.get(i).split(",")[1]);
                if(maxtime<Integer.parseInt(time7day.get(i).split(",")[1])){
                    maxtime=Integer.parseInt(time7day.get(i).split(",")[1]);
                }
                if(maxview<Integer.parseInt(time7day.get(i).split(",")[2])){
                    maxview=Integer.parseInt(time7day.get(i).split(",")[2]);
                }
            }
            for(int i=0;i<time7day.size();i++){
                JSONObject obj=new JSONObject();
                obj.put("date", time7day.get(i).split(",")[0]);
                obj.put("time", time7day.get(i).split(",")[1]);
                obj.put("view", time7day.get(i).split(",")[2]);
                obj.put("maxtime", maxtime.toString());
                obj.put("maxview",maxview.toString());

                jsonArray.add(obj);
            }
            resp.put("time7day", jsonArray);

            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch(Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "delthreadcron",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delthreadcron(){
        JSONObject resp=new JSONObject();
        try{
            historyRepository.resetThreadcron();
            //historyViewRepository.deleteHistoryView(username,videoid);
            resp.put("status", "true");
            resp.put("message", "Reset thread error thành công!");
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
            historyRepository.resetThreadBuffhByVps(vps.trim()+"%");
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

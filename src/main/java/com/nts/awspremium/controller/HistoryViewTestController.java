package com.nts.awspremium.controller;

import com.nts.awspremium.TikTokApi;
import com.nts.awspremium.model.*;
import com.nts.awspremium.model.Proxy;
import com.nts.awspremium.repositories.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.security.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/historylive")
public class HistoryViewTestController {
    @Autowired
    private ProxyVNTrue proxyVNTrue;
    @Autowired
    private ProxyUSTrue proxyUSTrue;
    @Autowired
    private ProxyKRTrue proxyKRTrue;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private VideoViewRepository videoViewRepository;

    @Autowired
    private DataCommentRepository dataCommentRepository;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private HistoryViewRepository historyViewRepository;

    @Autowired
    private HistoryCommentRepository historyCommentRepository;
    @Autowired
    private DataOrderRepository dataOrderRepository;
    @Autowired
    private HistoryViewSumRepository historyViewSumRepository;
    @Autowired
    private OrderTrue orderTrue;

    @Autowired
    private ProxyRepository proxyRepository;

    @Autowired
    private ServiceRepository serviceRepository;


    @GetMapping(value = "get", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get(@RequestParam(defaultValue = "") String vps, @RequestParam(defaultValue = "vn") String geo) {
        JSONObject resp = new JSONObject();
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }

        Random ran = new Random();
        try {
            Thread.sleep(ran.nextInt(1000));
            Long historieId = historyViewRepository.getAccToLive(vps.trim());
            if(historieId==null){
                resp.put("status", "fail");
                resp.put("fail", "user");
                resp.put("message", "Không còn user để view!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            List<VideoView> videos = null;
            List<HistoryView> histories = historyViewRepository.getHistoriesById(historieId);
            if (geo.trim().equals("vn")) {
                videos = videoViewRepository.getvideoPreVer2VNTEST(orderTrue.getValue());
            } else {
                videos = videoViewRepository.getvideoPreVer2USTEST(orderTrue.getValue());
            }
            if (videos.size() > 0) {
                histories.get(0).setTimeget(System.currentTimeMillis());
                histories.get(0).setVideoid(videos.get(0).getVideoid());
                histories.get(0).setOrderid(videos.get(0).getOrderid());
                histories.get(0).setChannelid(videos.get(0).getChannelid());
            } else {
                historyViewRepository.save(histories.get(0));
                resp.put("status", "fail");
                resp.put("username", histories.get(0).getUsername());
                resp.put("fail", "video");
                resp.put("message", "Không còn video để view!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            List<Proxy> proxyGet=null;
            proxyGet = proxyRepository.getProxyNotRunningAndLive(histories.get(0).getGeo());
            String[] proxy;
            if(proxyGet.size()==0){
                historyViewRepository.save(histories.get(0));
                resp.put("status","fail");
                resp.put("username", histories.get(0).getUsername());
                resp.put("fail", "proxy");
                resp.put("message","Hết proxy khả dụng!" );
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }else {
                proxy = proxyGet.get(0).getProxy().split(":");
            }
            proxyRepository.updateProxyLiveGet(vps,System.currentTimeMillis(),proxyGet.get(0).getId());
            resp.put("proxy",proxy[0] + ":" + proxy[1] + ":1:1");

            Service service = serviceRepository.getServiceNoCheckEnabled(videos.get(0).getService());

            histories.get(0).setTimeget(System.currentTimeMillis());
            histories.get(0).setRunning(1);
            historyViewRepository.save(histories.get(0));
            if(videos.get(0).getTimestart()>=System.currentTimeMillis()){
                List<Long> arrTime = new ArrayList<>();
                for (int i = 0; i < 20; i++) {
                    arrTime.add(System.currentTimeMillis());
                }
                for (int i = 0; i < 15; i++) {
                    arrTime.add(TimeUnit.MINUTES.toMillis((ran.nextInt((int)(service.getMaxtime()*0.1))) +videos.get(0).getTimestart()));
                }
                for (int i = 0; i < 25; i++) {
                    arrTime.add(videos.get(0).getTimestart()+ TimeUnit.MINUTES.toMillis((long)(service.getMaxtime()*0.1))+TimeUnit.MINUTES.toMillis(ran.nextInt((int)(service.getMaxtime()*0.4))));
                }
                for (int i = 0; i < 40; i++) {
                    arrTime.add(videos.get(0).getTimestart()+ TimeUnit.MINUTES.toMillis((long)(service.getMaxtime()*0.4))+TimeUnit.MINUTES.toMillis(ran.nextInt((int)(service.getMaxtime()*0.6))));
                }

                resp.put("time_start", arrTime.get(ran.nextInt(arrTime.size())));
            }else{
                resp.put("time_start", System.currentTimeMillis());
            }
            resp.put("channel_id", videos.get(0).getChannelid());
            resp.put("status", "true");
            resp.put("time_end", (videos.get(0).getTimestart()+ TimeUnit.MINUTES.toMillis((long)(service.getMaxtime()*1.5))));
            resp.put("video_id", videos.get(0).getVideoid());
            resp.put("video_title", videos.get(0).getVideotitle());
            resp.put("geo", "live");
            resp.put("username", histories.get(0).getUsername());
            resp.put("like", "fail");
            resp.put("sub", "fail");

            String list_key = dataOrderRepository.getListKeyByOrderid(videos.get(0).getOrderid());
            String key = "";
            if (list_key != null && list_key.length() != 0) {
                String[] keyArr = list_key.split(",");
                key = keyArr[ran.nextInt(keyArr.length)];
            }
            resp.put("suggest_type", "fail");
            resp.put("suggest_key", key.length() == 0 ? videos.get(0).getVideotitle() : key);
            resp.put("suggest_video", "");
            List<String> arrSource = new ArrayList<>();
            for (int i = 0; i < service.getSuggest(); i++) {
                arrSource.add("suggest");
            }
            for (int i = 0; i < service.getSearch(); i++) {
                arrSource.add("search");
            }
            for (int i = 0; i < service.getDtn(); i++) {
                arrSource.add("dtn");
            }
            for (int i = 0; i < service.getEmbed(); i++) {
                arrSource.add("embed");
            }
            for (int i = 0; i < service.getDirect(); i++) {
                arrSource.add("direct");
            }
            for (int i = 0; i < service.getExternal(); i++) {
                arrSource.add("external");
            }
            for (int i = 0; i < service.getPlaylists(); i++) {
                arrSource.add("playlists");
            }
            String source_view=arrSource.get(ran.nextInt(arrSource.size())).trim();
            if(source_view.equals("suggest")&&service.getType().equals("Special")){
                resp.put("suggest_type", "true");
            }else if(source_view.equals("search")&&service.getType().equals("Special")){
                resp.put("video_title", key.length() == 0 ? videos.get(0).getVideotitle() : key);
            }
            resp.put("source",source_view);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);



        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("fail", "sum");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/updatevideoid", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updatevideoid(@RequestParam(defaultValue = "") String username,
                                         @RequestParam(defaultValue = "") String videoid) {
        JSONObject resp = new JSONObject();
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
        try {
            Long historieId = historyViewRepository.getId(username);
            if (historieId == null) {
                resp.put("status", "fail");
                resp.put("message", "Không tìm thấy username!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            } else {
                if (historyViewRepository.getListVideoById(historieId).length() > 44) {
                    historyViewRepository.updateListVideoNew(videoid, historieId);
                } else {
                    historyViewRepository.updateListVideo(videoid, historieId);
                }
                /*
                List<HistoryView> histories =historyViewRepository.getHistoriesById(historieId);
                if(histories.get(0).getListvideo().length()==0){
                    histories.get(0).setListvideo(videoid);
                }else{
                    histories.get(0).setListvideo(histories.get(0).getListvideo()+","+videoid);
                }
                //histories.get(0).setRunning(1);
                historyViewRepository.save(histories.get(0));

                 */
                resp.put("status", "true");
                resp.put("message", "Update videoid vào history thành công!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/test", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> test(@RequestParam(defaultValue = "") String geo) throws IOException {
        JSONObject resp = new JSONObject();
        if(geo.equals("vn")){

        }
        String proxycheck=proxyRepository.getProxyRandTrafficForCheckAPI();
        resp.put("ff",TikTokApi.getFollowerCount("https://www.tiktok.com/@thuannguyen202203",proxycheck));
        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/update", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> update(@RequestParam(defaultValue = "") String username,
                                  @RequestParam(defaultValue = "") String videoid, @RequestParam(defaultValue = "") String channelid, @RequestParam(defaultValue = "0") Integer duration) {
        JSONObject resp = new JSONObject();

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

        try {

            Long historieId = historyViewRepository.getId(username);
            if (historieId == null) {
                resp.put("status", "fail");
                resp.put("message", "Không tìm thấy username!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            } else {
                //histories.get(0).setProxy(proxy);
                //histories.get(0).setRunning(0);
                //histories.get(0).setVideoid("");
                //histories.get(0).setVps("");
                //historyRepository.save(histories.get(0));
                Integer check_duration = historyViewRepository.checkDurationViewByTimecheck(historieId, (long) (duration));
                if (check_duration > 0) {
                    HistoryViewSum historySum = new HistoryViewSum();
                    historySum.setVideoid(videoid.trim());
                    historySum.setUsername(username);
                    historySum.setTime(System.currentTimeMillis());
                    historySum.setChannelid(channelid);
                    historySum.setDuration(duration);
                    try {
                        historyViewSumRepository.save(historySum);
                    } catch (Exception e) {
                        try {
                            historyViewSumRepository.save(historySum);
                        } catch (Exception f) {
                        }
                    }
                    resp.put("status", "true");
                    resp.put("message", "Update view thành công!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                //historyViewRepository.updateduration(duration,username,videoid);
                resp.put("status", "fail");
                resp.put("message", "Không update duration !");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "delthreadbyusername", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delthreadbyusername(@RequestParam(defaultValue = "") String username) {
        JSONObject resp = new JSONObject();
        if (username.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            Long historieId = historyViewRepository.getId(username.trim());
            historyViewRepository.resetThreadBuffhById(historieId);
            resp.put("status", "true");
            resp.put("message", "Update running thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(value = "getviewbuff7day", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> gettimebuff7day(@RequestParam(defaultValue = "") String user) {
        JSONObject resp = new JSONObject();
        try {
            List<String> time7day;
            if (user.length() == 0) {
                time7day = historyViewSumRepository.Gettimebuff7day();
            } else {
                time7day = historyViewSumRepository.Gettimebuff7day(user.trim());
            }

            JSONArray jsonArray = new JSONArray();
            Integer maxview = 0;

            for (int i = 0; i < time7day.size(); i++) {
                //System.out.println(time7day.get(i).split(",")[1]);
                if (maxview < Integer.parseInt(time7day.get(i).split(",")[1])) {
                    maxview = Integer.parseInt(time7day.get(i).split(",")[1]);
                }
            }
            for (int i = 0; i < time7day.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("date", time7day.get(i).split(",")[0]);
                obj.put("view", time7day.get(i).split(",")[1]);
                obj.put("maxview", maxview.toString());

                jsonArray.add(obj);
            }
            resp.put("view7day", jsonArray);

            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "delthreadcron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delthreadcron() {
        JSONObject resp = new JSONObject();
        try {
            historyViewRepository.resetThreadThan90mcron();
            historyViewRepository.resetThreadcron();
            resp.put("status", "true");
            resp.put("message", "Reset thread error thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "delhistorysumcron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delhistorysumcron() {
        JSONObject resp = new JSONObject();
        try {
            historyViewSumRepository.DelHistorySum();
            resp.put("status", "true");
            resp.put("message", "Delete history thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(value = "delnamebyvps", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delnamebyvps(@RequestParam(defaultValue = "") String vps) {
        JSONObject resp = new JSONObject();
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            historyViewRepository.resetThreadViewByVps(vps.trim());
            historyCommentRepository.resetThreadViewByVps(vps.trim());
            dataCommentRepository.resetRunningCommentByVPS(vps.trim());
            resp.put("status", "true");
            resp.put("message", "Update running thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "vpsrunning", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> vpsrunning(@RequestHeader(defaultValue = "") String Authorization) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            List<VpsRunning> vpsRunnings = historyViewRepository.getvpsrunning();

            //String a=orderRunnings.toString();
            JSONArray jsonArray = new JSONArray();

            //JSONObject jsonObject=new JSONObject().put("")
            //JSONObject jsonObject= (JSONObject) new JSONObject().put("Channelid",orderRunnings.get(0).toString());
            //jsonArray.add(orderRunnings);
            Integer sum_total = 0;
            for (int i = 0; i < vpsRunnings.size(); i++) {
                sum_total = sum_total + vpsRunnings.get(i).getTotal();
                JSONObject obj = new JSONObject();
                obj.put("vps", vpsRunnings.get(i).getVps());
                obj.put("total", vpsRunnings.get(i).getTotal());
                obj.put("time", vpsRunnings.get(i).getTime());
                jsonArray.add(obj);
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");


            resp.put("computers", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "deleteviewthan24h", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> deleteAllViewThan24h() {
        JSONObject resp = new JSONObject();
        try {
            historyViewRepository.deleteAllViewThan24h();
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
}

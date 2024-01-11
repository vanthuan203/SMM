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
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/historyview")
public class HistoryViewController {


    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ProxyRepository proxyRepository;
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
    private OrderSpeedTrue orderSpeedTrue;
    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private IpV4Repository ipV4Repository;
    @Autowired
    private SettingRepository settingRepository;

    @GetMapping(value = "get", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get(@RequestParam(defaultValue = "") String username, @RequestParam(defaultValue = "") String vps, @RequestParam(defaultValue = "0") Integer buffh) {
        JSONObject resp = new JSONObject();
        if (username.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        Random ran = new Random();
        try {
            Thread.sleep(ran.nextInt(1000));
            Long historieId = historyViewRepository.getId(username);
            List<VideoView> videos = null;
            if (historieId == null) {
                resp.put("status", "fail");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            } else {
                List<HistoryView> histories = historyViewRepository.getHistoriesById(historieId);
                int checkRedirect = 0;
                if (buffh == 1) {
                    videos = videoViewRepository.getvideoBuffHByGeo(histories.get(0).getGeo().trim(), histories.get(0).getListvideo(), orderTrue.getValue());
                } else {
                    if (histories.get(0).getGeo().trim().equals("vn")?(ran.nextInt(1000) < settingRepository.getRedirectVN()):(ran.nextInt(1000) < settingRepository.getRedirectUS())) {
                        checkRedirect = 1;
                        videos = videoViewRepository.getvideoBuffHByGeo(histories.get(0).getGeo().trim(), histories.get(0).getListvideo(), orderTrue.getValue());
                        if (videos.size() == 0) {
                            videos = videoViewRepository.getvideoViewByGeo(histories.get(0).getGeo().trim(), histories.get(0).getListvideo(), orderTrue.getValue());
                        }
                    } else {
                        videos = videoViewRepository.getvideoViewByGeo(histories.get(0).getGeo().trim(), histories.get(0).getListvideo(), orderTrue.getValue());
                    }
                }
                if (videos.size() > 0) {
                    histories.get(0).setTimeget(System.currentTimeMillis());
                    histories.get(0).setVideoid(videos.get(0).getVideoid());
                    histories.get(0).setOrderid(videos.get(0).getOrderid());
                    histories.get(0).setChannelid(videos.get(0).getChannelid());
                } else if (buffh == 0 && (histories.get(0).getGeo().trim().equals("vn")?(ran.nextInt(1000) < settingRepository.getRedirectVN()*2):(ran.nextInt(1000) < settingRepository.getRedirectUS()*2)) && checkRedirect == 0) {
                    videos = videoViewRepository.getvideoBuffHByGeo(histories.get(0).getGeo().trim(), histories.get(0).getListvideo(), orderTrue.getValue());
                    if (videos.size() > 0) {
                        histories.get(0).setTimeget(System.currentTimeMillis());
                        histories.get(0).setVideoid(videos.get(0).getVideoid());
                        histories.get(0).setOrderid(videos.get(0).getOrderid());
                        histories.get(0).setChannelid(videos.get(0).getChannelid());
                    } else {
                        videos = videoViewRepository.getvideoViewByGeo(histories.get(0).getGeo().trim(), histories.get(0).getListvideo(), orderSpeedTrue.getValue());
                        if (videos.size() > 0) {
                            histories.get(0).setTimeget(System.currentTimeMillis());
                            histories.get(0).setVideoid(videos.get(0).getVideoid());
                            histories.get(0).setOrderid(videos.get(0).getOrderid());
                            histories.get(0).setChannelid(videos.get(0).getChannelid());
                        } else {
                            histories.get(0).setTimeget(System.currentTimeMillis());
                            historyViewRepository.save(histories.get(0));
                            resp.put("status", "fail");
                            resp.put("username", histories.get(0).getUsername());
                            resp.put("fail", "video");
                            resp.put("message", "Không còn video để view!");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                    }

                } else {
                    videos = videoViewRepository.getvideoViewByGeo(histories.get(0).getGeo().trim(), histories.get(0).getListvideo(), orderSpeedTrue.getValue());
                    if (videos.size() > 0) {
                        histories.get(0).setTimeget(System.currentTimeMillis());
                        histories.get(0).setVideoid(videos.get(0).getVideoid());
                        histories.get(0).setOrderid(videos.get(0).getOrderid());
                        histories.get(0).setChannelid(videos.get(0).getChannelid());
                    } else {
                        histories.get(0).setTimeget(System.currentTimeMillis());
                        historyViewRepository.save(histories.get(0));
                        resp.put("status", "fail");
                        resp.put("username", histories.get(0).getUsername());
                        resp.put("fail", "video");
                        resp.put("message", "Không còn video để view!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                }

                Service service = serviceRepository.getInfoService(videos.get(0).getService());

                histories.get(0).setTimeget(System.currentTimeMillis());
                histories.get(0).setRunning(1);
                historyViewRepository.save(histories.get(0));
                resp.put("live", service.getLive() == 1 ? "true" : "fail");
                resp.put("channel_id", videos.get(0).getChannelid());
                resp.put("status", "true");
                resp.put("video_id", videos.get(0).getVideoid());
                resp.put("video_title", videos.get(0).getVideotitle());
                resp.put("username", histories.get(0).getUsername());
                resp.put("geo", accountRepository.getGeoByUsername(username.trim()));
                resp.put("like", "fail");
                resp.put("sub", "fail");
                if(service.getNiche()==1){
                    String[] nicheArr = service.getKeyniche().split(",");
                    resp.put("niche_key", nicheArr[ran.nextInt(nicheArr.length)]);
                }else{
                    resp.put("niche_key","");
                }
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
                String source_view = arrSource.get(ran.nextInt(arrSource.size())).trim();
                if (source_view.equals("suggest") && service.getType().equals("Special")) {
                    resp.put("suggest_type", "true");
                } else if (source_view.equals("search") && service.getType().equals("Special")) {
                    resp.put("video_title", key.length() == 0 ? videos.get(0).getVideotitle() : key);
                }
                resp.put("source", source_view);

                /////////////////////////////////////////////////////////////////////////////////////////////////////////////////


                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////


                if (service.getMintime() != service.getMaxtime() && service.getLive() == 0) {
                    if (videos.get(0).getDuration() > service.getMaxtime() * 60) {
                        resp.put("video_duration", service.getMintime() * 60 + (service.getMintime() < service.getMaxtime() ? (ran.nextInt((service.getMaxtime() - service.getMintime()) * 30) + (service.getMaxtime() >= 10 ? 30 : 0)) : 0));
                    } else {
                        resp.put("video_duration", service.getMintime() * 60 < videos.get(0).getDuration() ? (service.getMintime() * 60 + ran.nextInt((int) (videos.get(0).getDuration() - service.getMintime() * 60))) : videos.get(0).getDuration());
                    }
                } else if (service.getLive() == 1) {
                    int min_check = (int) ((service.getMintime() * 0.15) > 30 ? 30 : (service.getMintime() * 0.15));
                    if ((System.currentTimeMillis() - videos.get(0).getTimestart()) / 1000 / 60 < min_check) {
                        resp.put("video_duration", service.getMintime() * 60 + (service.getMintime() >= 15 ? 120 : 0));
                    } else {
                        int time_live = videos.get(0).getMinstart() - (int) ((System.currentTimeMillis() - videos.get(0).getTimestart()) / 1000 / 60);
                        resp.put("video_duration", (time_live > 0 ? time_live : 0) * 60 + (service.getMintime() >= 15 ? 120 : 0));
                    }
                } else {
                    if (videos.get(0).getDuration() > service.getMaxtime() * 60) {
                        resp.put("video_duration", service.getMintime() * 60 + (service.getMintime() < service.getMaxtime() ? (ran.nextInt((service.getMaxtime() - service.getMintime()) * 60 + service.getMaxtime() >= 10 ? 60 : 0)) : 0));
                    } else {
                        resp.put("video_duration", videos.get(0).getDuration());
                    }
                }
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }

        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("fail", "sum");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(value = "getview", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getview(@RequestParam(defaultValue = "") String vps, @RequestParam(defaultValue = "0") Integer buffh) {
        JSONObject resp = new JSONObject();
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        Random ran = new Random();
        try {
            Thread.sleep(ran.nextInt(1000));
            //Long historieId = historyViewRepository.getAccToView(vps.trim());
            Long historieId = historyViewRepository.getAccToViewNoCheckProxy(vps.trim());
            if (historieId == null) {
                resp.put("status", "fail");
                resp.put("fail", "user");
                resp.put("message", "Không còn user để view!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            List<VideoView> videos = null;
            List<HistoryView> histories = historyViewRepository.getHistoriesById(historieId);
            if (buffh == 1) {
                videos = videoViewRepository.getvideoBuffHByGeo(histories.get(0).getGeo().trim(), histories.get(0).getListvideo(), orderTrue.getValue());
            } else {
                videos = videoViewRepository.getvideoViewByGeo(histories.get(0).getGeo().trim(), histories.get(0).getListvideo(), orderTrue.getValue());
            }
            if (videos.size() > 0) {
                histories.get(0).setTimeget(System.currentTimeMillis());
                histories.get(0).setVideoid(videos.get(0).getVideoid());
                histories.get(0).setOrderid(videos.get(0).getOrderid());
                histories.get(0).setChannelid(videos.get(0).getChannelid());
            } else if (buffh == 0) {
                videos = videoViewRepository.getvideoBuffHByGeo(histories.get(0).getGeo().trim(), histories.get(0).getListvideo(), orderTrue.getValue());
                if (videos.size() > 0) {
                    histories.get(0).setTimeget(System.currentTimeMillis());
                    histories.get(0).setVideoid(videos.get(0).getVideoid());
                    histories.get(0).setOrderid(videos.get(0).getOrderid());
                    histories.get(0).setChannelid(videos.get(0).getChannelid());
                } else {
                    videos = videoViewRepository.getvideoViewByGeo(histories.get(0).getGeo().trim(), histories.get(0).getListvideo(), orderSpeedTrue.getValue());
                    if (videos.size() > 0) {
                        histories.get(0).setTimeget(System.currentTimeMillis());
                        histories.get(0).setVideoid(videos.get(0).getVideoid());
                        histories.get(0).setOrderid(videos.get(0).getOrderid());
                        histories.get(0).setChannelid(videos.get(0).getChannelid());
                    } else {
                        histories.get(0).setTimeget(System.currentTimeMillis());
                        historyViewRepository.save(histories.get(0));
                        resp.put("status", "fail");
                        resp.put("fail", "video");
                        resp.put("message", "Không còn video để view!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                }
            } else {
                videos = videoViewRepository.getvideoViewByGeo(histories.get(0).getGeo().trim(), histories.get(0).getListvideo(), orderSpeedTrue.getValue());
                if (videos.size() > 0) {
                    histories.get(0).setTimeget(System.currentTimeMillis());
                    histories.get(0).setVideoid(videos.get(0).getVideoid());
                    histories.get(0).setOrderid(videos.get(0).getOrderid());
                    histories.get(0).setChannelid(videos.get(0).getChannelid());
                } else {
                    histories.get(0).setTimeget(System.currentTimeMillis());
                    historyViewRepository.save(histories.get(0));
                    resp.put("status", "fail");
                    resp.put("fail", "video");
                    resp.put("message", "Không còn video để view!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            }
            String[] proxy;
            List<Proxy> proxies;
            if(ipV4Repository.checkIPv4Live(histories.get(0).getTypeproxy())==0&&buffh!=3){
                proxies = proxyRepository.getProxyNotRunningAndLive(histories.get(0).getGeo());
                if(proxies.size()>0){
                    proxy=proxies.get(0).getProxy().split(":");
                }else{
                    histories.get(0).setTimeget(System.currentTimeMillis());
                    historyViewRepository.save(histories.get(0));
                    resp.put("status", "fail");
                    resp.put("fail", "video");
                    resp.put("message", "Không còn video để view!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            }else{
                proxy = histories.get(0).getProxy().split(":");
            }
            Service service = serviceRepository.getInfoService(videos.get(0).getService());

            histories.get(0).setTimeget(System.currentTimeMillis());
            histories.get(0).setRunning(1);

            historyViewRepository.save(histories.get(0));
            resp.put("live", service.getLive() == 1 ? "true" : "fail");
            resp.put("channel_id", videos.get(0).getChannelid());
            resp.put("status", "true");
            resp.put("video_id", videos.get(0).getVideoid());
            resp.put("video_title", videos.get(0).getVideotitle());
            resp.put("username", histories.get(0).getUsername());
            resp.put("geo", histories.get(0).getGeo());
            resp.put("like", "fail");
            resp.put("sub", "fail");
            resp.put("proxy", proxy[0] + ":" + proxy[1] + ":1:1");
            if(service.getNiche()==1){
                String[] nicheArr = service.getKeyniche().split(",");
                resp.put("niche_key", nicheArr[ran.nextInt(nicheArr.length)]);
            }else{
                resp.put("niche_key","");
            }
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
            if (service.getService() == 753 || service.getService() == 153 || service.getService() == 754 || service.getService() == 154) {
                resp.put("live", "true");
            }
            String source_view = arrSource.get(ran.nextInt(arrSource.size())).trim();
            if (source_view.equals("suggest") && service.getType().equals("Special")) {
                resp.put("suggest_type", "true");
            } else if (source_view.equals("search") && service.getType().equals("Special")) {
                resp.put("video_title", key.length() == 0 ? videos.get(0).getVideotitle() : key);
            }
            resp.put("source", source_view);

            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////


            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////


            if (service.getMintime() != service.getMaxtime() && service.getLive() == 0) {
                if (videos.get(0).getDuration() > service.getMaxtime() * 60) {
                    resp.put("video_duration", service.getMintime() * 60 + (service.getMintime() < service.getMaxtime() ? (ran.nextInt((service.getMaxtime() - service.getMintime()) * 30) + (service.getMaxtime() >= 10 ? 30 : 0)) : 0));
                } else {
                    resp.put("video_duration", service.getMintime() * 60 < videos.get(0).getDuration() ? (service.getMintime() * 60 + ran.nextInt((int) (videos.get(0).getDuration() - service.getMintime() * 60))) : videos.get(0).getDuration());
                }
            } else if (service.getLive() == 1) {
                int min_check = (int) ((service.getMintime() * 0.15) > 30 ? 30 : (service.getMintime() * 0.15));
                if ((System.currentTimeMillis() - videos.get(0).getTimestart()) / 1000 / 60 < min_check) {
                    resp.put("video_duration", service.getMintime() * 60 + (service.getMintime() >= 15 ? 120 : 0));
                } else {
                    int time_live = videos.get(0).getMinstart() - (int) ((System.currentTimeMillis() - videos.get(0).getTimestart()) / 1000 / 60);
                    resp.put("video_duration", (time_live > 0 ? time_live : 0) * 60 + (service.getMintime() >= 15 ? 120 : 0));
                }
            } else {
                if (videos.get(0).getDuration() > service.getMaxtime() * 60) {
                    resp.put("video_duration", service.getMintime() * 60 + (service.getMintime() < service.getMaxtime() ? (ran.nextInt((service.getMaxtime() - service.getMintime()) * 60 + service.getMaxtime() >= 10 ? 60 : 0)) : 0));
                } else {
                    resp.put("video_duration", videos.get(0).getDuration());
                }
            }
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);

        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("fail", "sum");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "getlivebyusername", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getlivebyusername(@RequestParam(defaultValue = "") String username) {
        JSONObject resp = new JSONObject();
        if (username.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }

        Random ran = new Random();
        try {
            Thread.sleep(ran.nextInt(1000));
            Long historieId = historyViewRepository.getId(username.trim());
            if(historieId==null){
                resp.put("status", "fail");
                resp.put("fail", "user");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            List<VideoView> videos = null;
            List<HistoryView> histories = historyViewRepository.getHistoriesById(historieId);
            if (histories.get(0).getGeo().trim().equals("live-vn")) {
                videos = videoViewRepository.getvideoPreVer2VNTEST(orderTrue.getValue());
            } else if(histories.get(0).getGeo().trim().equals("live-us")) {
                videos = videoViewRepository.getvideoPreVer2USTEST(orderTrue.getValue());
            }else{
                historyViewRepository.save(histories.get(0));
                resp.put("status", "fail");
                resp.put("username", histories.get(0).getUsername());
                resp.put("fail", "video");
                resp.put("message", "Không còn video để view!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
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
            resp.put("time_end", (videos.get(0).getTimestart()+ TimeUnit.MINUTES.toMillis((long)(service.getMaxtime()*1.5))+TimeUnit.MINUTES.toMillis((ran.nextInt((5))))));
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

    @GetMapping(value = "getlive", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getlive(@RequestParam(defaultValue = "") String vps) {
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
            if (histories.get(0).getGeo().trim().equals("live-vn")) {
                videos = videoViewRepository.getvideoPreVer2VNTEST(orderTrue.getValue());
            } else if(histories.get(0).getGeo().trim().equals("live-us")) {
                videos = videoViewRepository.getvideoPreVer2USTEST(orderTrue.getValue());
            }else{
                historyViewRepository.save(histories.get(0));
                resp.put("status", "fail");
                resp.put("username", histories.get(0).getUsername());
                resp.put("fail", "video");
                resp.put("message", "Không còn video để view!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
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
            String[] proxy;
            List<Proxy> proxies;
            String geo_proxy=histories.get(0).getGeo().trim().split("-")[1];
            if(ipV4Repository.checkIPv4Live(histories.get(0).getTypeproxy())==0){
                proxies = proxyRepository.getProxyNotRunningAndLive(geo_proxy);
                if(proxies.size()>0){
                    proxy=proxies.get(0).getProxy().split(":");
                }else{
                    histories.get(0).setTimeget(System.currentTimeMillis());
                    historyViewRepository.save(histories.get(0));
                    resp.put("status", "fail");
                    resp.put("fail", "proxy");
                    resp.put("message", "Hết proxy khả dụng!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            }else{
                proxy = histories.get(0).getProxy().split(":");
            }
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
                    arrTime.add(TimeUnit.MINUTES.toMillis((ran.nextInt((int)(service.getMaxtime()*0.1)))) +videos.get(0).getTimestart());
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
            resp.put("time_end", (videos.get(0).getTimestart()+ TimeUnit.MINUTES.toMillis((long)(service.getMaxtime()*1.5))+TimeUnit.MINUTES.toMillis((ran.nextInt((5))))));
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
    ResponseEntity<String> delthreadbyusername(@RequestParam(defaultValue = "") String username, @RequestParam(defaultValue = "") String videoid) {
        JSONObject resp = new JSONObject();
        if (username.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            Long historieId = historyViewRepository.getId(username.trim());
            historyViewRepository.resetThreadBuffhById(historieId);
            Long historieIdC = historyCommentRepository.getId(username);
            historyCommentRepository.resetThreadBuffhById(historieIdC);
            //dataCommentRepository.resetRunningComment(username.trim());
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
    ResponseEntity<String> delnamebyvps(@RequestParam(defaultValue = "") String vps) throws InterruptedException {
        JSONObject resp = new JSONObject();
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (historyViewRepository.PROCESSLISTVIEW() >= 30) {
            Random ran = new Random();
            Thread.sleep(1000 + ran.nextInt(2000));
            resp.put("status", "fail");
            resp.put("message", "Đợi reset threads...");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
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

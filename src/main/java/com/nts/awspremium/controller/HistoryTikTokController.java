package com.nts.awspremium.controller;

import com.nts.awspremium.model.*;
import com.nts.awspremium.repositories.*;
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
@RequestMapping(path = "/tiktok")
public class HistoryTikTokController {
    @Autowired
    private ChannelTikTokRepository channelTikTokRepository;
    @Autowired
    private HistoryTiktokRepository historyTiktokRepository;

    @Autowired
    private HistoryFollowerTiktokRepository historyFollowerTiktokRepository;

    @Autowired
    private ActivityTikTokRepository activityTikTokRepository;
    @Autowired
    private HistoryFollowerTikTokSumRepository historyFollowerTikTokSumRepository;
    @Autowired
    private OrderTrafficTrue orderTrue;

    @Autowired
    private IpV4Repository ipV4Repository;
    @Autowired
    private ProxyRepository proxyRepository;
    @Autowired
    private ProxySettingRepository proxySettingRepository;

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @GetMapping(value = "get", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get(@RequestParam(defaultValue = "") String username) {
        JSONObject resp = new JSONObject();
        if (username.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        Random ran = new Random();
        try {
            Thread.sleep(ran.nextInt(500));
            HistoryTikTok historyTikTok = historyTiktokRepository.getHistoryTikTokByUsername(username.trim());
            List<ChannelTiktok> channelTiktoks = null;
            if (historyTikTok == null) {
                resp.put("status", "fail");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            } else {
                if(historyTikTok.getOption_running()==0){
                    if(activityTikTokRepository.checkActivityByUsername(username.trim())==0){
                        resp.put("status", "true");
                        resp.put("task","activity");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }else{
                        historyTikTok.setTimeget(System.currentTimeMillis());
                        historyTiktokRepository.save(historyTikTok);
                        resp.put("status", "fail");
                        resp.put("username", historyTikTok.getUsername());
                        resp.put("fail", "activity");
                        resp.put("message", "Không có nhiêm vụ nuôi tk!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                }
                String list_tiktok_id=historyFollowerTiktokRepository.getListTiktokID(username.trim());
                channelTiktoks = channelTikTokRepository.getChannelTiktokBy(list_tiktok_id==null?"":list_tiktok_id);
                if (channelTiktoks.size() > 0) {
                    historyTikTok.setTimeget(System.currentTimeMillis());
                    historyTikTok.setOrderid(channelTiktoks.get(0).getOrderid());
                    historyTikTok.setRunning(1);
                    historyTiktokRepository.save(historyTikTok);
                } else {
                        historyTikTok.setTimeget(System.currentTimeMillis());
                        historyTiktokRepository.save(historyTikTok);
                        resp.put("status", "fail");
                        resp.put("username", historyTikTok.getUsername());
                        resp.put("fail", "follower");
                        resp.put("message", "Không có nhiêm vụ follower!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                resp.put("status", "true");
                resp.put("task", "follower");
                resp.put("tiktok_id",channelTiktoks.get(0).getTiktok_id());

                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }

        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("fail", "sum");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value = "/updateTask", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateTask(@RequestParam(defaultValue = "") String username,@RequestParam  Boolean status,@RequestParam(defaultValue = "") String task,
                                         @RequestParam(defaultValue = "") String tiktok_id,@RequestParam(defaultValue = "0") Long orderid) {
        JSONObject resp = new JSONObject();
        if (username.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (task.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "task không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        } else {
            if (task.equals("activity")) {
                if (status == true) {
                    ActivityTikTok activityTikTok = new ActivityTikTok();
                    activityTikTok.setUsername(username.trim());
                    activityTikTok.setTime_update(System.currentTimeMillis());
                    activityTikTokRepository.save(activityTikTok);
                    resp.put("status", "true");
                    resp.put("message", "Update activity thành công");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                } else {
                    resp.put("status", "true");
                    resp.put("message", "Update activity thành công");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            }
        }
        if (tiktok_id.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "tiktok_id không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (orderid == 0) {
            resp.put("status", "fail");
            resp.put("message", "orderid không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (status == null) {
            resp.put("status", "fail");
            resp.put("message", "status không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            historyTiktokRepository.resetThreadByUsername(username.trim());
            if (status == true) {
                HistoryFollowerTikTok historyFollowerTikTok=historyFollowerTiktokRepository.getHistoriesByUsername(username.trim());
                if(historyFollowerTikTok==null){
                    HistoryFollowerTikTok historyFollowerTikTokNew=new HistoryFollowerTikTok();
                    historyFollowerTikTokNew.setUsername(username.trim());
                    historyFollowerTikTokNew.setList_tiktok_id(tiktok_id);
                    historyFollowerTikTokNew.setTime_update(System.currentTimeMillis());
                    historyFollowerTiktokRepository.save(historyFollowerTikTokNew);
                }else{
                    historyFollowerTikTok.setList_tiktok_id(historyFollowerTikTok.getList_tiktok_id()+tiktok_id);
                    historyFollowerTikTok.setTime_update(System.currentTimeMillis());
                    historyFollowerTiktokRepository.save(historyFollowerTikTok);
                }
                HistoryFollowerTikTokSum historyFollowerTikTokSum = new HistoryFollowerTikTokSum();
                historyFollowerTikTokSum.setOrderid(orderid);
                historyFollowerTikTokSum.setUsername(username.trim());
                historyFollowerTikTokSum.setTime(System.currentTimeMillis());
                historyFollowerTikTokSumRepository.save(historyFollowerTikTokSum);
                resp.put("status", "true");
                resp.put("message", "Update orderid vào history thành công!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            resp.put("status", "true");
            resp.put("message", "Reset thread thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

}

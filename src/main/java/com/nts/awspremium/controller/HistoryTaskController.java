package com.nts.awspremium.controller;

import com.nts.awspremium.model.*;
import com.nts.awspremium.repositories.*;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/task")
public class HistoryTaskController {
    @Autowired
    private ChannelTikTokRepository channelTikTokRepository;
    @Autowired
    private HistoryTiktokRepository historyTiktokRepository;
    @Autowired
    private VideoViewRepository videoViewRepository;

    @Autowired
    private HistoryFollowerTiktokRepository historyFollowerTiktokRepository;

    @Autowired
    private HistoryViewYoutubeRepository historyViewYoutubeRepository;

    @Autowired
    private HistoryFollowerTiktok24hRepository historyFollowerTiktok24hRepository;

    @Autowired
    private ActivityTikTokRepository activityTikTokRepository;
    @Autowired
    private HistoryFollowerTikTokSumRepository historyFollowerTikTokSumRepository;


    @Autowired
    private OrderFollowerTrue orderFollowerTrue;
    @Autowired
    private OrderTrue orderTrue;

    @Autowired
    private SettingTikTokRepository settingTikTokRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private IpV4Repository ipV4Repository;
    @Autowired
    private AccountRegTikTokRepository accountRegTikTokRepository;
    @Autowired
    private AccountTikTokRepository accountTikTokRepository;

    @Autowired
    private Proxy_IPV4_TikTokRepository proxyIpv4TikTokRepository;

    @Autowired
    private PriorityTasksRepository priorityTasksRepository;

    JSONObject task_tiktok_follower(String username){
        JSONObject resp = new JSONObject();
        try{
            SettingTiktok settingTiktok=settingTikTokRepository.getReferenceById(1L);
            if(historyFollowerTiktok24hRepository.countFollower24hByUsername(username.trim()+"%")>=settingTiktok.getMax_follower()){
                resp.put("status", false);
                return resp;
            }
            String list_tiktok_id=historyFollowerTiktokRepository.getListTiktokID(username.trim());
            List<ChannelTiktok> channelTiktoks = channelTikTokRepository.getChannelTiktokByTask(list_tiktok_id==null?"":list_tiktok_id,orderFollowerTrue.getValue());
            if (channelTiktoks.size() > 0) {
                Service service=serviceRepository.getService(channelTiktoks.get(0).getService());
                /*String proxy=accountTikTokRepository.getProxyByUsername(username.trim());
                if(ipV4Repository.checkIPv4Live(accountTikTokRepository.getProxyByUsername(username.trim()))==0){
                    String proxy_rand=proxyIpv4TikTokRepository.getProxyRandTikTok();
                    if(proxy_rand!=null){
                        proxy=proxy_rand;
                    }else{
                        resp.put("status", false);
                    }
                }
                 */
                resp.put("status", true);
                //resp.put("proxy", proxy);
                resp.put("order_id", channelTiktoks.get(0).getOrderid());
                resp.put("task", service.getTask());
                resp.put("platform", service.getPlatform().toLowerCase());
                resp.put("tiktok_id",channelTiktoks.get(0).getTiktok_id());
                return resp;

            } else {
                resp.put("status", false);
                return resp;
            }
        }catch (Exception e){
            resp.put("status", false);
            return resp;
        }
    }

    JSONObject task_youtube_view(String username){
        JSONObject resp = new JSONObject();
        try{
            String list_video_id=historyViewYoutubeRepository.getListVideoID(username.trim());
            List<VideoView> videoViews = videoViewRepository.getvideoViewByMobile(list_video_id==null?"":list_video_id, orderTrue.getValue());
            if (videoViews.size() > 0) {
                Service service=serviceRepository.getService(videoViews.get(0).getService());
                Random ran=new Random();
                /*String proxy=accountTikTokRepository.getProxyByUsername(username.trim());
                if(ipV4Repository.checkIPv4Live(accountTikTokRepository.getProxyByUsername(username.trim()))==0){
                    String proxy_rand=proxyIpv4TikTokRepository.getProxyRandTikTok();
                    if(proxy_rand!=null){
                        proxy=proxy_rand;
                    }else{
                        resp.put("status", false);
                    }
                }
                 */
                resp.put("status", true);
                resp.put("order_id", videoViews.get(0).getOrderid());
                //resp.put("proxy", proxy);
                resp.put("task", service.getTask());
                resp.put("video_title", videoViews.get(0).getVideotitle());
                resp.put("platform", service.getPlatform().toLowerCase());
                resp.put("video_id", videoViews.get(0).getVideoid());
                if (service.getMintime() != service.getMaxtime() && service.getLive() == 0) {
                    if (videoViews.get(0).getDuration() > service.getMaxtime() * 60) {
                        resp.put("video_duration", service.getMintime() * 60 + (service.getMintime() < service.getMaxtime() ? (ran.nextInt((service.getMaxtime() - service.getMintime()) * 45) + (service.getMaxtime() >= 10 ? 30 : 0)) : 0));
                    } else {
                        resp.put("video_duration", service.getMintime() * 60 < videoViews.get(0).getDuration() ? (service.getMintime() * 60 + ran.nextInt((int)(videoViews.get(0).getDuration() - service.getMintime() * 60))) : videoViews.get(0).getDuration());
                    }
                } else if (service.getLive() == 1) {
                    int min_check = (int) ((service.getMintime() * 0.15) > 30 ? 30 : (service.getMintime() * 0.15));
                    if ((System.currentTimeMillis() - videoViews.get(0).getTimestart()) / 1000 / 60 < min_check) {
                        resp.put("video_duration", service.getMintime() * 60 + (service.getMintime() >= 15 ? 120 : 0));
                    } else {
                        int time_live = videoViews.get(0).getMinstart() - (int) ((System.currentTimeMillis() - videoViews.get(0).getTimestart()) / 1000 / 60);
                        resp.put("video_duration", (time_live > 0 ? time_live : 0) * 60 + (service.getMintime() >= 15 ? 120 : 0));
                    }
                } else {
                    if (videoViews.get(0).getDuration() > service.getMaxtime() * 60) {
                        resp.put("video_duration", service.getMintime() * 60 + (service.getMintime() < service.getMaxtime() ? (ran.nextInt((service.getMaxtime() - service.getMintime()) * 60 + service.getMaxtime() >= 10 ? 60 : 0)) : 0));
                    } else {
                        resp.put("video_duration", videoViews.get(0).getDuration());
                    }
                }
                if(((Integer.parseInt(resp.get("video_duration").toString())<10||Integer.parseInt(resp.get("video_duration").toString())>45))&&service.getMaxtime()==1){
                    resp.put("video_duration",ran.nextInt(30)+10);
                }
                return resp;

            } else {
                resp.put("status", false);
                return resp;
            }
        }catch (Exception e){
            resp.put("status", false);
            return resp;
        }
    }

    @GetMapping(value = "test", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> test(@RequestParam(defaultValue = "") String username) throws InterruptedException {
        Random ran=new Random();
        Thread.sleep(ran.nextInt(500));
        HistoryTikTok historyTikTok = historyTiktokRepository.getHistoryTikTokByUsername(username.trim());
        if (historyTikTok == null) {
            JSONObject resp = new JSONObject();
            resp.put("message", "Username không tồn tại!");
            resp.put("status", false);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        List<PriorityTasks> priorityTasks =priorityTasksRepository.getPriority_task();

        List<String> arrTask = new ArrayList<>();

        for(int i=0;i<priorityTasks.size();i++){
            for (int j = 0; j < priorityTasks.get(i).getPriority(); j++) {
                arrTask.add(priorityTasks.get(i).getTask());
            }
        }
        JSONObject get_task = null;
        String task_index=null;
        while (arrTask.size()>0){
            String task = arrTask.get(ran.nextInt(arrTask.size())).trim();
            while(arrTask.remove(task)) {}
            if(task.equals("task_tiktok_follower")){
                get_task=task_tiktok_follower(username.trim());
            }else if(task.equals("task_youtube_view")){
                get_task=task_youtube_view(username.trim());
            }
            if(get_task.get("status").equals(true)){
                task_index=task;
                break;
            }
        }
        if(get_task.get("status").equals(true)){
            historyTikTok.setTimeget(System.currentTimeMillis());
            historyTikTok.setOrderid(Long.parseLong(get_task.get("order_id").toString()));
            historyTikTok.setRunning(priorityTasksRepository.getState_Task(task_index));
            historyTikTok.setTask(task_index);
            historyTiktokRepository.save(historyTikTok);
            get_task.remove("order_id");
        }else{
            historyTikTok.setRunning(0);
            historyTikTok.setTimeget(System.currentTimeMillis());
            historyTiktokRepository.save(historyTikTok);
        }
        return new ResponseEntity<String>(get_task.toJSONString(), HttpStatus.OK);
    }
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
                resp.put("message", "Username không tồn tại!");
                resp.put("status", "fail");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            } else {
                if(accountTikTokRepository.checkDeviceAndVPS(historyTikTok.getDevice_id().trim(),historyTikTok.getVps().trim())>0){
                    accountTikTokRepository.updateVPSByDevice(historyTikTok.getVps().trim(),historyTikTok.getDevice_id().trim());
                    accountRegTikTokRepository.updateVPSByDevice(historyTikTok.getVps().trim(),historyTikTok.getDevice_id().trim());
                    historyTiktokRepository.updateVPSByDevice(historyTikTok.getVps().trim(),historyTikTok.getDevice_id().trim());
                }
                if(historyTikTok.getOption_running()==0){
                    if(activityTikTokRepository.checkActivityByUsername(username.trim())==0){
                        String proxy=accountTikTokRepository.getProxyByUsername(username.trim());
                        Random rand=new Random();
                        if(ipV4Repository.checkIPv4Live(accountTikTokRepository.getProxyByUsername(username.trim()))==0){
                            String proxy_rand=proxyIpv4TikTokRepository.getProxyRandTikTok();
                            if(proxy_rand!=null){
                                proxy=proxy_rand;
                            }else{
                                historyTikTok.setTimeget(System.currentTimeMillis());
                                historyTiktokRepository.save(historyTikTok);
                                resp.put("status", "fail");
                                resp.put("username", historyTikTok.getUsername());
                                resp.put("fail", "proxy");
                                resp.put("message", "Hết proxy khả dụng");
                                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                            }
                        }
                        historyTikTok.setTimeget(System.currentTimeMillis());
                        historyTikTok.setRunning(2);
                        historyTiktokRepository.save(historyTikTok);
                        resp.put("status", "true");
                        resp.put("proxy", proxy);
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
                }else{
                    return new ResponseEntity<String>(task_tiktok_follower(username.trim()).toString(), HttpStatus.OK);
                }

            }

        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("fail", "sum");
            resp.put("message", e.getMessage());
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            System.out.println(stackTraceElement.getMethodName());
            System.out.println(stackTraceElement.getLineNumber());
            System.out.println(stackTraceElement.getClassName());
            System.out.println(stackTraceElement.getFileName());
            System.out.println("Error : " + e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value = "/updateTask", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateTask(@RequestParam(defaultValue = "") String username,@RequestParam  Boolean status,@RequestParam(defaultValue = "") String task,
                                         @RequestParam(defaultValue = "") String task_id) {
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
                    historyTiktokRepository.resetThreadByUsername(username.trim());
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
        if (task_id.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "tiktok_id không để trống");
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
                    historyFollowerTikTokNew.setList_tiktok_id(task_id.trim()+"|");
                    historyFollowerTikTokNew.setTime_update(System.currentTimeMillis());
                    historyFollowerTiktokRepository.save(historyFollowerTikTokNew);
                }else{
                    historyFollowerTikTok.setList_tiktok_id(historyFollowerTikTok.getList_tiktok_id()+task_id+"|");
                    historyFollowerTikTok.setTime_update(System.currentTimeMillis());
                    historyFollowerTiktokRepository.save(historyFollowerTikTok);
                }
                HistoryFollower24hTikTok historyFollower24hTikTok =new HistoryFollower24hTikTok();
                historyFollower24hTikTok.setCode(username+task_id.trim());
                historyFollower24hTikTok.setTime(System.currentTimeMillis());
                historyFollowerTiktok24hRepository.save(historyFollower24hTikTok);


                HistoryFollowerTikTokSum historyFollowerTikTokSum = new HistoryFollowerTikTokSum();
                historyFollowerTikTokSum.setTiktok_id(task_id.trim());
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
    @GetMapping(value = "delThreadByVPS", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delThreadByVPS(@RequestParam(defaultValue = "") String vps) throws InterruptedException {
        JSONObject resp = new JSONObject();
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            historyTiktokRepository.resetThreadByVps(vps.trim());
            resp.put("status", "true");
            resp.put("message", "Update running thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/dellHisFollower24HByCron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> dellHisFollower24HByCron() {
        JSONObject resp = new JSONObject();
        try{
            historyFollowerTiktok24hRepository.deleteAllByThan24h();
            resp.put("status", "true");
            resp.put("message", "Delete follower >24h thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "delThreadErrorByCron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delThreadErrorByCron() {
        JSONObject resp = new JSONObject();
        try {
            historyTiktokRepository.resetThreadcron();
            resp.put("status", "true");
            resp.put("message", "Reset thread error thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "delHistorySumByCron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delHistorySumByCron() {
        JSONObject resp = new JSONObject();
        try {
            historyFollowerTikTokSumRepository.DelHistorySum();
            resp.put("status", "true");
            resp.put("message", "Delete history thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "updateOptionRunningByCron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateOptionRunningByCron() {
        JSONObject resp = new JSONObject();
        try {
            historyTiktokRepository.updateOptionRunningFollower();
            resp.put("status", "true");
            resp.put("message", "update running follower thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

}

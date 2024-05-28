package com.nts.awspremium.controller;

import com.nts.awspremium.model.*;
import com.nts.awspremium.model_system.OrderThreadCheck;
import com.nts.awspremium.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/account")
public class AccountController {
    @Autowired
    private TaskPriorityRepository taskPriorityRepository;
    @Autowired
    private OrderRunningRepository orderRunningRepository;
    @Autowired
    private OrderThreadCheck orderThreadCheck;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private YoutubeViewHistoryRepository youtubeVideoHistoryRepository;
    @Autowired
    private YoutubeSubscribeHistoryRepository youtubeChannelHistoryRepository;

    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private AccountTaskRepository accountTaskRepository;

    @Autowired
    private SettingTikTokRepository settingTikTokRepository;

    @Autowired
    private TikTokFollower24hRepository tikTokFollower24hRepository;

    @Autowired
    private TikTokAccountHistoryRepository tikTokAccountHistoryRepository;

    Map<String, Object> task_tiktok_follower(String account_id){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            SettingTiktok settingTiktok=settingTikTokRepository.getReferenceById(1L);
            if(tikTokFollower24hRepository.count_Follower_24h_By_Username(account_id.trim()+"%")>=settingTiktok.getMax_follower()){
                resp.put("status", false);
                return resp;
            }
            String list_tiktok_id=tikTokAccountHistoryRepository.get_List_TiktokId_By_AccountId(account_id.trim());
            OrderRunning orderRunning = orderRunningRepository.get_Order_Tiktok_By_Task("tiktok","follower",list_tiktok_id==null?"":list_tiktok_id,orderThreadCheck.getValue());
            if (orderRunning!=null) {
                Service service=orderRunning.getService();
                resp.put("status", true);
                data.put("order_id", orderRunning.getOrder_id());
                data.put("platform", service.getPlatform().toLowerCase());
                data.put("task", service.getTask());
                data.put("task_key",orderRunning.getOrder_key());
                resp.put("data",data);
                return resp;

            } else {
                resp.put("status", false);
                return resp;
            }
        }catch (Exception e){
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            System.out.println(stackTraceElement.getMethodName());
            System.out.println(stackTraceElement.getLineNumber());
            System.out.println(stackTraceElement.getClassName());
            System.out.println(stackTraceElement.getFileName());
            System.out.println("Error : " + e.getMessage());
            resp.put("status", false);
            return resp;
        }
    }
    Map<String, Object> task_youtube_view(String account_id){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            String list_video_id=youtubeVideoHistoryRepository.get_List_VideoId_By_AccountId(account_id.trim());
            OrderRunning orderRunning = orderRunningRepository.get_Order_Tiktok_By_Task("youtube","view",list_video_id==null?"":list_video_id, orderThreadCheck.getValue());
            if (orderRunning!=null) {
                Service service=orderRunning.getService();
                Random ran=new Random();
                resp.put("status", true);
                data.put("order_id", orderRunning.getOrder_id());
                //resp.put("proxy", proxy);
                data.put("platform", service.getPlatform().toLowerCase());
                data.put("task", service.getTask());
                data.put("task_key", orderRunning.getOrder_key());


                if(service.getService_type().trim().equals("Special")){
                    String list_key = orderRunning.getYoutube_list_keyword();
                    String key = "";
                    if (list_key != null && list_key.length() != 0) {
                        String[] keyArr = list_key.split(",");
                        key = keyArr[ran.nextInt(keyArr.length)];
                    }
                    data.put("keyword", key.length() == 0 ? orderRunning.getYoutube_video_title() : key);

                }else{
                    data.put("keyword", orderRunning.getYoutube_video_title());
                }
                if (service.getMin_time() != service.getMax_time()) {
                    if (orderRunning.getDuration() > service.getMax_time() * 60) {
                        data.put("viewing_time", service.getMin_time() * 60 + (service.getMin_time() < service.getMax_time() ? (ran.nextInt((service.getMax_time() - service.getMin_time()) * 45) + (service.getMax_time() >= 10 ? 30 : 0)) : 0));
                    } else {
                        data.put("viewing_time", service.getMin_time() * 60 < orderRunning.getDuration() ? (service.getMin_time() * 60 + ran.nextInt((int)(orderRunning.getDuration() - service.getMin_time() * 60))) : orderRunning.getDuration());
                    }
                }else {
                    if (orderRunning.getDuration() > service.getMax_time() * 60) {
                        data.put("viewing_time", service.getMin_time() * 60 + (service.getMin_time() < service.getMax_time() ? (ran.nextInt((service.getMax_time() - service.getMin_time()) * 60 + service.getMax_time() >= 10 ? 60 : 0)) : 0));
                    } else {
                        data.put("viewing_time", orderRunning.getDuration());
                    }
                }
                if(((Integer.parseInt(data.get("viewing_time").toString())<10||Integer.parseInt(data.get("viewing_time").toString())>45))&&service.getMax_time()==1){
                    data.put("viewing_time",ran.nextInt(30)+10);
                }
                resp.put("data",data);
                return resp;

            } else {
                resp.put("status", false);
                return resp;
            }
        }catch (Exception e){
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            System.out.println(stackTraceElement.getMethodName());
            System.out.println(stackTraceElement.getLineNumber());
            System.out.println(stackTraceElement.getClassName());
            System.out.println(stackTraceElement.getFileName());
            System.out.println("Error : " + e.getMessage());
            resp.put("status", false);
            return resp;
        }
    }

    @GetMapping(value = "getAccount", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> getAccount(@RequestParam(defaultValue = "") String account_id) throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran=new Random();
            Thread.sleep(ran.nextInt(500));
            AccountTask accountTask = accountTaskRepository.get_Account_By_Account_id(account_id.trim());
            if (accountTask == null) {

                resp.put("status", false);
                data.put("message", "Username không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);

            }
            List<TaskPriority> priorityTasks =taskPriorityRepository.getPriority_task();

            List<String> arrTask = new ArrayList<>();

            for(int i=0;i<priorityTasks.size();i++){
                for (int j = 0; j < priorityTasks.get(i).getPriority(); j++) {
                    arrTask.add(priorityTasks.get(i).getTask());
                }
            }
            Map<String, Object> get_task =null;
            String task_index=null;
            while (arrTask.size()>0){
                String task = arrTask.get(ran.nextInt(arrTask.size())).trim();
                System.out.println(task);
                while(arrTask.remove(task)) {}
                if(task.equals("task_tiktok_follower")){
                    get_task=task_tiktok_follower(account_id.trim());
                }else if(task.equals("task_youtube_view")){
                    get_task=task_youtube_view(account_id.trim());
                }
                if(get_task.get("status").equals(true)){
                    task_index=task;
                    break;
                }
            }
            if(get_task==null){
                resp.put("status",false);
                data.put("message","Không có nhiệm vụ!");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            Map<String, Object> respJson=new LinkedHashMap<>();
            Map<String, Object> dataJson=new LinkedHashMap<>();
            if(get_task.get("status").equals(true)){
                dataJson= (Map<String, Object>) get_task.get("data");
                System.out.println(dataJson);
                respJson.put("status",true);
                respJson.put("data",dataJson);
                //--------------------------------------------//
                accountTask.setGet_time(System.currentTimeMillis());
                accountTask.setOrder_id(Long.parseLong(dataJson.get("order_id").toString()));
                accountTask.setRunning(taskPriorityRepository.getState_Task(task_index));
                accountTask.setTask(dataJson.get("task").toString());
                accountTask.setPlatform(dataJson.get("platform").toString());
                accountTask.setTask_key(dataJson.get("task_key").toString());
                accountTaskRepository.save(accountTask);
                //--------------------------------------------//
                dataJson.remove("order_id");
            }else{
                respJson.put("status",false);
                dataJson.put("message","Không có nhiệm vụ!");
                respJson.put("data",dataJson);
                accountTask.setRunning(0);
                accountTask.setGet_time(System.currentTimeMillis());
                accountTaskRepository.save(accountTask);
            }
            return new ResponseEntity<>(respJson, HttpStatus.OK);
        }catch (Exception e){
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            System.out.println(stackTraceElement.getMethodName());
            System.out.println(stackTraceElement.getLineNumber());
            System.out.println(stackTraceElement.getClassName());
            System.out.println(stackTraceElement.getFileName());
            System.out.println("Error : " + e.getMessage());
            resp.put("status", "fail");
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping(value = "/updateTask", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> updateTask(@RequestParam(defaultValue = "") String account_id,@RequestParam  Boolean status,
                                                   @RequestParam(defaultValue = "") String task,
                                                   @RequestParam(defaultValue = "") String task_key,
                                      @RequestParam(defaultValue = "") String platform) {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        if (account_id.length() == 0) {
            resp.put("status", false);
            data.put("message", "username không để trống");
            resp.put("data", data);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
        if (task.length() == 0) {
            resp.put("status", false);
            data.put("message", "task không để trống");
            resp.put("data", data);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }else {
            if (task.equals("activity")) {
                if (status == true) {
                    //////////////////
                } else {
                    resp.put("status", "true");
                    data.put("message", "Update activity thành công");
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
        }
        if (platform.length() == 0) {
            resp.put("status", false);
            data.put("message", "platform không để trống");
            resp.put("data", data);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
        if (status == null) {
            resp.put("status", false);
            data.put("message", "status không để trống");
            resp.put("data", data);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
        try {
            accountTaskRepository.reset_Thread_By_AccountId(account_id.trim());

            if(platform.toLowerCase().trim().equals("youtube")){
                if(task.toLowerCase().trim().equals("view")){
                    if(status==true){
                        YoutubeViewHistory youtubeVideoHistory=youtubeVideoHistoryRepository.get_By_AccountId(account_id.trim());
                        if(youtubeVideoHistory!=null){
                            youtubeVideoHistory.setList_id(youtubeVideoHistory.getList_id()+task_key.trim()+"|");
                            youtubeVideoHistory.setUpdate_time(System.currentTimeMillis());
                            youtubeVideoHistoryRepository.save(youtubeVideoHistory);
                        }else{
                            YoutubeViewHistory youtubeVideoHistory_New=new YoutubeViewHistory();
                            youtubeVideoHistory_New.setAccountTask(accountTaskRepository.get_Account_By_Account_id(account_id.trim()));
                            youtubeVideoHistory_New.setUpdate_time(System.currentTimeMillis());
                            youtubeVideoHistory_New.setList_id(task_key.trim()+"|");
                            youtubeVideoHistoryRepository.save(youtubeVideoHistory_New);
                        }
                    }
                }else  if(task.toLowerCase().trim().equals("sub")){
                    if(status==true){
                        YoutubeSubscribeHistory youtubeChannelHistory=youtubeChannelHistoryRepository.get_By_AccountId(account_id.trim());
                        if(youtubeChannelHistory!=null){
                            youtubeChannelHistory.setList_id(youtubeChannelHistory.getList_id()+task_key.trim()+"|");
                            youtubeChannelHistory.setUpdate_time(System.currentTimeMillis());
                            youtubeChannelHistoryRepository.save(youtubeChannelHistory);
                        }else{
                            YoutubeSubscribeHistory youtubeChannelHistory_New=new YoutubeSubscribeHistory();
                            youtubeChannelHistory_New.setAccountTask(accountTaskRepository.get_Account_By_Account_id(account_id.trim()));
                            youtubeChannelHistory_New.setUpdate_time(System.currentTimeMillis());
                            youtubeChannelHistory_New.setList_id(task_key.trim()+"|");
                            youtubeChannelHistoryRepository.save(youtubeChannelHistory_New);
                        }
                    }
                }
            }else if(platform.toLowerCase().trim().equals("tiktok")){
                if(task.toLowerCase().trim().equals("follower")){
                    if(status==true){
                        TikTokFollowerHistory tikTokAccountHistory=tikTokAccountHistoryRepository.get_By_AccountId(account_id.trim());
                        if(tikTokAccountHistory!=null){
                            tikTokAccountHistory.setList_id(tikTokAccountHistory.getList_id()+task_key.trim()+"|");
                            tikTokAccountHistory.setUpdate_time(System.currentTimeMillis());
                            tikTokAccountHistoryRepository.save(tikTokAccountHistory);
                        }else{
                            TikTokFollowerHistory tikTokAccountHistory_New=new TikTokFollowerHistory();
                            tikTokAccountHistory_New.setAccountTask(accountTaskRepository.get_Account_By_Account_id(account_id.trim()));
                            tikTokAccountHistory_New.setUpdate_time(System.currentTimeMillis());
                            tikTokAccountHistory_New.setList_id(task_key.trim()+"|");
                            tikTokAccountHistoryRepository.save(tikTokAccountHistory_New);
                        }
                        TiktokFollower24h tiktokFollower24h =new TiktokFollower24h();
                        tiktokFollower24h.setId(account_id.trim()+task_key.trim());
                        tiktokFollower24h.setUpdate_time(System.currentTimeMillis());
                        tikTokFollower24hRepository.save(tiktokFollower24h);
                    }
                }
            }
            resp.put("status", true);
            data.put("message", "Update thành công!");
            resp.put("data", data);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (Exception e) {
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            System.out.println(stackTraceElement.getMethodName());
            System.out.println(stackTraceElement.getLineNumber());
            System.out.println(stackTraceElement.getClassName());
            System.out.println(stackTraceElement.getFileName());
            System.out.println("Error : " + e.getMessage());
            resp.put("status", false);
            data.put("message", e.getMessage());
            resp.put("data", data);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
    }


}

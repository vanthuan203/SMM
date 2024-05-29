package com.nts.awspremium.controller;

import com.nts.awspremium.model.*;
import com.nts.awspremium.model_system.MySQLCheck;
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
@RequestMapping(value = "/task")
public class TaskController {
    @Autowired
    private TaskPriorityRepository taskPriorityRepository;
    @Autowired
    private OrderRunningRepository orderRunningRepository;
    @Autowired
    private OrderThreadCheck orderThreadCheck;
    @Autowired
    private MySQLCheck mySQLCheck;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private YoutubeViewHistoryRepository youtubeVideoHistoryRepository;
    @Autowired
    private YoutubeSubscribeHistoryRepository youtubeChannelHistoryRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private AccountTaskRepository accountTaskRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private SettingTikTokRepository settingTikTokRepository;
    @Autowired
    private SettingSystemRepository settingSystemRepository;

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
                data.put("account_id", account_id.trim());
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
                data.put("account_id", account_id.trim());
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
    Map<String, Object> task_youtube_like(String account_id){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            String list_video_id=youtubeVideoHistoryRepository.get_List_VideoId_By_AccountId(account_id.trim());
            OrderRunning orderRunning = orderRunningRepository.get_Order_Tiktok_By_Task("youtube","like",list_video_id==null?"":list_video_id, orderThreadCheck.getValue());
            if (orderRunning!=null) {
                Service service=orderRunning.getService();
                Random ran=new Random();
                resp.put("status", true);
                data.put("order_id", orderRunning.getOrder_id());
                //resp.put("proxy", proxy);
                data.put("account_id", account_id.trim());
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
    @GetMapping(value = "getTaskDevice", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> getTaskDevice(@RequestHeader(defaultValue = "") String Authorization,
                                                   @RequestParam(defaultValue = "") String device_id) throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran=new Random();
            Thread.sleep(500+ran.nextInt(1500));
            Integer checktoken = userRepository.FindUserByToken(Authorization);
            if (checktoken ==0) {
                resp.put("status", false);
                data.put("message", "Token expired");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (device_id.length()==0) {
                resp.put("status", false);
                data.put("message", "device_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //---------------------get_Account------------------//
            SettingSystem settingSystem=settingSystemRepository.getReferenceById(1L);
            Device device=deviceRepository.check_DeviceId(device_id.trim());
            if(device!=null){
                device.setUpdate_time(System.currentTimeMillis());
                deviceRepository.save(device);
                if(device.getNum_account()<settingSystem.getMax_acc()) {
                    String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                    String code="";
                    for(int i=0;i<50;i++){
                        Integer ranver=ran.nextInt(stringrand.length());
                        code=code+stringrand.charAt(ranver);
                    }
                    Account account_get= accountRepository.get_Account_By_DeviceId(device_id,System.currentTimeMillis(),code);
                    if(account_get!=null){
                        AccountTask accountTask=new AccountTask();
                        accountTask.setAccount_id(account_get.getAccount_id().trim());
                        accountTask.setDevice(device);
                        accountTask.setAccount_level(0);
                        accountTask.setGet_time(0L);
                        accountTask.setOrder_id(0L);
                        accountTask.setRunning(0);
                        accountTask.setState(1);
                        accountTask.setState(1);
                        accountTask.setProfile(null);
                        accountTask.setTask_index(0);
                        accountTaskRepository.save(accountTask);

                        device.setNum_account(device.getNum_account()+1);
                        deviceRepository.save(device);
                        resp.put("status", true);
                        data.put("task", "account");
                        data.put("account_id", account_get.getAccount_id().trim());
                        data.put("password", account_get.getPassword().trim());
                        data.put("recover_mail", account_get.getRecover_mail().trim());
                        data.put("auth_2fa", account_get.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }
            }else{
                Device device_new=new Device();
                device_new.setDevice_id(device_id.trim());
                device_new.setAdd_time(System.currentTimeMillis());
                device_new.setState(1);
                device_new.setBox(null);
                device_new.setUpdate_time(System.currentTimeMillis());
                device_new.setNum_account(0);
                deviceRepository.save(device_new);
                device=device_new;
                if(0<settingSystem.getMax_acc()) {
                    String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                    String code="";
                    for(int i=0;i<50;i++){
                        Integer ranver=ran.nextInt(stringrand.length());
                        code=code+stringrand.charAt(ranver);
                    }
                    Account account_get= accountRepository.get_Account_By_DeviceId(device_id,System.currentTimeMillis(),code);
                    System.out.println(account_get);
                    if(account_get!=null){
                        AccountTask accountTask=new AccountTask();
                        accountTask.setAccount_id(account_get.getAccount_id().trim());
                        accountTask.setDevice(device);
                        accountTask.setAccount_level(0);
                        accountTask.setGet_time(0L);
                        accountTask.setOrder_id(0L);
                        accountTask.setRunning(0);
                        accountTask.setState(1);
                        accountTask.setState(1);
                        accountTask.setProfile(null);
                        accountTask.setTask_index(0);
                        accountTaskRepository.save(accountTask);

                        device.setNum_account(device.getNum_account()+1);
                        deviceRepository.save(device);
                        resp.put("status", true);
                        data.put("task", "account");
                        data.put("account_id", account_get.getAccount_id().trim());
                        data.put("password", account_get.getPassword().trim());
                        data.put("recover_mail", account_get.getRecover_mail().trim());
                        data.put("auth_2fa", account_get.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }
            }
            //--------------------end_get_Account----------------------//
            AccountTask accountTask = accountTaskRepository.check_Account_Running_By_DeviceId(device_id.trim());
            //Check số lần get nhiệm vụ của 1 account
            if(accountTask!=null){
                if(accountTask.getTask_index()>=settingSystem.getMax_task()){
                    accountTaskRepository.reset_Thread_Index_By_AccountId(accountTask.getAccount_id().trim());
                    accountTask = accountTaskRepository.get_Account_By_DeviceId(device_id.trim());
                }
            }else{
                accountTask = accountTaskRepository.get_Account_By_DeviceId(device_id.trim());
            }
            if(accountTask==null){
                resp.put("status", false);
                data.put("message", "không có account");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }

            if(!device.getDevice_id().equals(accountTask.getDevice().getDevice_id())){
                resp.put("status", false);
                data.put("message", "account_id & device_id không khớp");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            accountTask.setTask_index(accountTask.getTask_index()+1);
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
                while(arrTask.remove(task)) {}
                if(task.equals("task_tiktok_follower")){
                    get_task=task_tiktok_follower(accountTask.getAccount_id().trim());
                }else if(task.equals("task_youtube_view")){
                    get_task=task_youtube_view(accountTask.getAccount_id().trim());
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


    @GetMapping(value = "getTaskProfile", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> getTaskProfile(@RequestHeader(defaultValue = "") String Authorization,
                                                @RequestParam(defaultValue = "") String device_id,
                                                @RequestParam(defaultValue = "") String profile_id) throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran=new Random();
            Thread.sleep(ran.nextInt(2000));
            Integer checktoken = userRepository.FindUserByToken(Authorization);
            if (checktoken ==0) {
                resp.put("status", false);
                data.put("message", "Token expired");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (device_id.length()==0) {
                resp.put("status", false);
                data.put("message", "device_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //---------------------get_Account------------------//
            SettingSystem settingSystem=settingSystemRepository.getReferenceById(1L);
            Device device=deviceRepository.check_DeviceId(device_id.trim());
            Profile profile =profileRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
            if(device!=null){
                device.setUpdate_time(System.currentTimeMillis());
                deviceRepository.save(device);
                if(profile!=null){
                    if(profile.getNum_account()<settingSystem.getMax_acc()) {
                        Account account_get=null;
                        if(mySQLCheck.getValue()<settingSystem.getMax_mysql()){
                            String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                            String code="";
                            for(int i=0;i<50;i++){
                                Integer ranver=ran.nextInt(stringrand.length());
                                code=code+stringrand.charAt(ranver);
                            }
                            account_get= accountRepository.get_Account_By_DeviceId(device_id,System.currentTimeMillis(),code);
                        }
                        if(account_get!=null){
                            AccountTask accountTask=new AccountTask();
                            accountTask.setAccount_id(account_get.getAccount_id().trim());
                            accountTask.setDevice(device);
                            accountTask.setAccount_level(0);
                            accountTask.setGet_time(0L);
                            accountTask.setOrder_id(0L);
                            accountTask.setRunning(0);
                            accountTask.setState(1);
                            accountTask.setState(1);
                            accountTask.setProfile(profile);
                            accountTask.setTask_index(0);
                            accountTaskRepository.save(accountTask);

                            device.setNum_account(device.getNum_account()+1);
                            deviceRepository.save(device);
                            profile.setNum_account(profile.getNum_account()+1);
                            profileRepository.save(profile);
                            resp.put("status", true);
                            data.put("task", "account");
                            data.put("profile", profile.getProfile_id().split(device_id.trim()+"_")[1]);
                            data.put("account_id", account_get.getAccount_id().trim());
                            data.put("password", account_get.getPassword().trim());
                            data.put("recover_mail", account_get.getRecover_mail().trim());
                            data.put("auth_2fa", account_get.getAuth_2fa().trim());
                            resp.put("data",data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }
                    }else{
                        if(device.getNum_profile()<settingSystem.getMax_profile()){
                            String pro_id=device_id.trim()+"_P-"+(10000000+ran.nextInt(999999));
                            while(profileRepository.count_ProfileId(pro_id)!=0){
                                pro_id=device_id.trim()+"_P-"+(10000000+ran.nextInt(999999));
                            }
                            Profile profile_new=new Profile();
                            profile_new.setProfile_id(pro_id);
                            profile_new.setAdd_time(System.currentTimeMillis());
                            profile_new.setDevice(device);
                            profile_new.setNum_account(0);
                            profile_new.setUpdate_time(0L);
                            profile_new.setState(1);
                            profileRepository.save(profile_new);
                            profile=profile_new;

                            device.setNum_profile(device.getNum_profile()+1);
                            deviceRepository.save(device);

                            resp.put("status", true);
                            data.put("task", "profile_changer");
                            data.put("profile", profile.getProfile_id().split(device_id.trim()+"_")[1]);
                            resp.put("data",data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }else{
                            profile=profileRepository.get_Profile_Get_Account_By_DeviceId(device_id.trim());
                            if(profile!=null){
                                resp.put("status", true);
                                data.put("task", "profile_changer");
                                data.put("profile", profile.getProfile_id().split(device_id.trim()+"_")[1]);
                                resp.put("data",data);
                                return new ResponseEntity<>(resp, HttpStatus.OK);
                            }else{
                                profile=profileRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                            }
                        }
                    }
                }
                else {
                    Profile profile_new=new Profile();
                    profile_new.setProfile_id(profile_id.trim());
                    profile_new.setAdd_time(System.currentTimeMillis());
                    profile_new.setDevice(device);
                    profile_new.setNum_account(0);
                    profile_new.setUpdate_time(0L);
                    profile_new.setState(1);
                    profileRepository.save(profile_new);
                    profile=profile_new;

                    device.setNum_profile(device.getNum_profile()+1);
                    deviceRepository.save(device);

                    if(0<settingSystem.getMax_acc()) {
                        Account account_get=null;
                        if(mySQLCheck.getValue()<settingSystem.getMax_mysql()){
                            String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                            String code="";
                            for(int i=0;i<50;i++){
                                Integer ranver=ran.nextInt(stringrand.length());
                                code=code+stringrand.charAt(ranver);
                            }
                            account_get= accountRepository.get_Account_By_DeviceId(device_id,System.currentTimeMillis(),code);
                        }
                        if(account_get!=null){
                            AccountTask accountTask=new AccountTask();
                            accountTask.setAccount_id(account_get.getAccount_id().trim());
                            accountTask.setDevice(device);
                            accountTask.setAccount_level(0);
                            accountTask.setGet_time(0L);
                            accountTask.setOrder_id(0L);
                            accountTask.setRunning(0);
                            accountTask.setState(1);
                            accountTask.setState(1);
                            accountTask.setProfile(profile);
                            accountTask.setTask_index(0);
                            accountTaskRepository.save(accountTask);

                            device.setNum_account(device.getNum_account()+1);
                            deviceRepository.save(device);
                            profile.setNum_account(profile.getNum_account()+1);
                            profileRepository.save(profile);

                            resp.put("status", true);
                            data.put("task", "account");
                            data.put("profile", profile.getProfile_id().split(device_id.trim()+"_")[1]);
                            data.put("account_id", account_get.getAccount_id().trim());
                            data.put("password", account_get.getPassword().trim());
                            data.put("recover_mail", account_get.getRecover_mail().trim());
                            data.put("auth_2fa", account_get.getAuth_2fa().trim());
                            resp.put("data",data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }
                    }
                }
            }else{
                Device device_new=new Device();
                device_new.setDevice_id(device_id.trim());
                device_new.setAdd_time(System.currentTimeMillis());
                device_new.setState(1);
                device_new.setBox(null);
                device_new.setUpdate_time(System.currentTimeMillis());
                device_new.setNum_account(0);
                device_new.setNum_profile(0);
                deviceRepository.save(device_new);
                device=device_new;

                Profile profile_new=new Profile();
                profile_new.setProfile_id(device_id.trim()+"_"+profile_id.trim());
                profile_new.setAdd_time(System.currentTimeMillis());
                profile_new.setDevice(device);
                profile_new.setNum_account(0);
                profile_new.setUpdate_time(0L);
                profile_new.setState(1);
                profileRepository.save(profile_new);
                profile=profile_new;

                device.setNum_profile(device.getNum_profile()+1);
                deviceRepository.save(device);

                if(0<settingSystem.getMax_acc()) {
                    Account account_get=null;
                    if(mySQLCheck.getValue()<settingSystem.getMax_mysql()){
                        String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                        String code="";
                        for(int i=0;i<50;i++){
                            Integer ranver=ran.nextInt(stringrand.length());
                            code=code+stringrand.charAt(ranver);
                        }
                        account_get= accountRepository.get_Account_By_DeviceId(device_id,System.currentTimeMillis(),code);
                    }
                    if(account_get!=null){
                        AccountTask accountTask=new AccountTask();
                        accountTask.setAccount_id(account_get.getAccount_id().trim());
                        accountTask.setDevice(device);
                        accountTask.setAccount_level(0);
                        accountTask.setGet_time(0L);
                        accountTask.setOrder_id(0L);
                        accountTask.setRunning(0);
                        accountTask.setState(1);
                        accountTask.setState(1);
                        accountTask.setProfile(profile);
                        accountTask.setTask_index(0);
                        accountTaskRepository.save(accountTask);

                        device.setNum_account(device.getNum_account()+1);
                        deviceRepository.save(device);
                        profile.setNum_account(profile.getNum_account()+1);
                        profileRepository.save(profile);

                        resp.put("status", true);
                        data.put("task", "account");
                        data.put("profile", profile.getProfile_id().split(device_id.trim()+"_")[1]);
                        data.put("account_id", account_get.getAccount_id().trim());
                        data.put("password", account_get.getPassword().trim());
                        data.put("recover_mail", account_get.getRecover_mail().trim());
                        data.put("auth_2fa", account_get.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }
            }
            //--------------------end_get_Account----------------------//
            AccountTask accountTask = accountTaskRepository.check_Account_Running_By_ProfileId(device_id.trim()+"_"+profile_id);
            //Check số lần get nhiệm vụ của 1 account
            if(accountTask!=null){
                if(accountTask.getTask_index()>=settingSystem.getMax_task()){
                    accountTaskRepository.reset_Thread_Index_By_AccountId(accountTask.getAccount_id().trim());
                    accountTask = accountTaskRepository.get_Account_By_ProfileId(device_id.trim()+"_"+profile_id.trim());
                }
            }else{
                profile.setUpdate_time(System.currentTimeMillis());
                profileRepository.save(profile);
                accountTask = accountTaskRepository.get_Account_By_ProfileId(device_id.trim()+"_"+profile_id.trim());
            }
            if(accountTask==null){
                accountTaskRepository.reset_Thread_Index_By_ProfileId(profile_id.trim());
                profile=profileRepository.get_Profile_Get_Task(device_id.trim());
                if(profile!=null){
                    resp.put("status", true);
                    data.put("task", "profile_changer");
                    data.put("profile", profile.getProfile_id().split(device_id.trim()+"_")[1]);
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else{
                    resp.put("status", false);
                    data.put("message", "Không có account để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }

            }
            if(!device.getDevice_id().equals(accountTask.getDevice().getDevice_id())){
                resp.put("status", false);
                data.put("message", "account_id & device_id không khớp");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            accountTask.setTask_index(accountTask.getTask_index()+1);
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
                while(arrTask.remove(task)) {}
                if(task.equals("task_tiktok_follower")){
                    get_task=task_tiktok_follower(accountTask.getAccount_id().trim());
                }else if(task.equals("task_youtube_view")){
                    get_task=task_youtube_view(accountTask.getAccount_id().trim());
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
                //System.out.println(dataJson);
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

    @GetMapping(value = "getTaskOFF", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> getTaskOFF(@RequestHeader(defaultValue = "") String Authorization,
                                                 @RequestParam(defaultValue = "") String account_id,
                                                @RequestParam(defaultValue = "") String device_id) throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran=new Random();
            Thread.sleep(500+ran.nextInt(1500));
            Integer checktoken = userRepository.FindUserByToken(Authorization);
            if (checktoken ==0) {
                resp.put("status", false);
                data.put("message", "Token expired");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (account_id.length()==0) {
                resp.put("status", false);
                data.put("message", "account_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (device_id.length()==0) {
                resp.put("status", false);
                data.put("message", "account_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //---------------------get_Account------------------//
            SettingSystem settingSystem=settingSystemRepository.getReferenceById(1L);
            Device device=deviceRepository.check_DeviceId(device_id.trim());
            if(device!=null){
                device.setUpdate_time(System.currentTimeMillis());
                deviceRepository.save(device);
                if(device.getNum_account()<settingSystem.getMax_acc()) {
                    String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                    String code="";
                    for(int i=0;i<50;i++){
                        Integer ranver=ran.nextInt(stringrand.length());
                        code=code+stringrand.charAt(ranver);
                    }
                    Account account_get= accountRepository.get_Account_By_DeviceId(device_id,System.currentTimeMillis(),code);
                    if(account_get!=null){
                        AccountTask accountTask=new AccountTask();
                        accountTask.setAccount_id(account_get.getAccount_id().trim());
                        accountTask.setDevice(device);
                        accountTask.setAccount_level(0);
                        accountTask.setGet_time(0L);
                        accountTask.setOrder_id(0L);
                        accountTask.setRunning(0);
                        accountTask.setState(1);
                        accountTask.setState(1);
                        accountTask.setProfile(null);
                        accountTask.setTask_index(0);
                        accountTaskRepository.save(accountTask);

                        device.setNum_account(device.getNum_account()+1);
                        deviceRepository.save(device);
                        resp.put("status", true);
                        data.put("task", "account");
                        data.put("account_id", account_get.getAccount_id().trim());
                        data.put("password", account_get.getPassword().trim());
                        data.put("recover_mail", account_get.getRecover_mail().trim());
                        data.put("auth_2fa", account_get.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }
            }else{
                Device device_new=new Device();
                device_new.setDevice_id(device_id.trim());
                device_new.setAdd_time(System.currentTimeMillis());
                device_new.setState(1);
                device_new.setBox(null);
                device_new.setUpdate_time(System.currentTimeMillis());
                device_new.setNum_account(0);
                deviceRepository.save(device_new);
                device=device_new;
                if(0<settingSystem.getMax_acc()) {
                    String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                    String code="";
                    for(int i=0;i<50;i++){
                        Integer ranver=ran.nextInt(stringrand.length());
                        code=code+stringrand.charAt(ranver);
                    }
                    Account account_get=null;
                    if(mySQLCheck.getValue()<settingSystem.getMax_mysql()){
                        account_get= accountRepository.get_Account_By_DeviceId(device_id,System.currentTimeMillis(),code);
                    }
                    if(account_get!=null){
                        AccountTask accountTask=new AccountTask();
                        accountTask.setAccount_id(account_get.getAccount_id().trim());
                        accountTask.setDevice(device);
                        accountTask.setAccount_level(0);
                        accountTask.setGet_time(0L);
                        accountTask.setOrder_id(0L);
                        accountTask.setRunning(0);
                        accountTask.setState(1);
                        accountTask.setState(1);
                        accountTask.setProfile(null);
                        accountTask.setTask_index(0);
                        accountTaskRepository.save(accountTask);

                        device.setNum_account(device.getNum_account()+1);
                        deviceRepository.save(device);
                        resp.put("status", true);
                        data.put("task", "account");
                        data.put("account_id", account_get.getAccount_id().trim());
                        data.put("password", account_get.getPassword().trim());
                        data.put("recover_mail", account_get.getRecover_mail().trim());
                        data.put("auth_2fa", account_get.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }
            }
            //--------------------end_get_Account----------------------//
            AccountTask accountTask = accountTaskRepository.get_Account_By_Account_id(account_id.trim());
            if (accountTask == null) {

                resp.put("status", false);
                data.put("message", "account_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);

            }
            if(!device.getDevice_id().equals(accountTask.getDevice())){
                resp.put("status", false);
                data.put("message", "account_id & device_id không khớp");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
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
    ResponseEntity<Map<String, Object>> updateTask(@RequestHeader(defaultValue = "") String Authorization,
                                                   @RequestParam(defaultValue = "") String account_id,@RequestParam  Boolean status,
                                                   @RequestParam(defaultValue = "") String task,
                                                   @RequestParam(defaultValue = "") String task_key,
                                      @RequestParam(defaultValue = "") String platform) {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            Integer checktoken = userRepository.FindUserByToken(Authorization);
            if (checktoken ==0) {
                resp.put("status", false);
                data.put("message", "Token expired");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
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

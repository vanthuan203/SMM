package com.nts.awspremium.controller;

import com.nts.awspremium.model.*;
import com.nts.awspremium.model_system.MySQLCheck;
import com.nts.awspremium.model_system.OrderThreadCheck;
import com.nts.awspremium.platform.facebook.FacebookTask;
import com.nts.awspremium.platform.facebook.FacebookUpdate;
import com.nts.awspremium.platform.tiktok.TiktokTask;
import com.nts.awspremium.platform.tiktok.TiktokUpdate;
import com.nts.awspremium.platform.youtube.YoutubeTask;
import com.nts.awspremium.platform.youtube.YoutubeUpdate;
import com.nts.awspremium.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
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
    private YoutubeSubscriberHistoryRepository youtubeChannelHistoryRepository;

    @Autowired
    private YoutubeLikeHistoryRepository youtubeLikeHistoryRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DataSubscriberRepository dataSubscriberRepository;
    @Autowired
    private AccountTaskRepository accountTaskRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private SettingTikTokRepository settingTikTokRepository;

    @Autowired
    private SettingYoutubeRepository settingYoutubeRepository;
    @Autowired
    private SettingSystemRepository settingSystemRepository;
    @Autowired
    private YoutubeLike24hRepository youtubeLike24hRepository;
    @Autowired
    private YoutubeSubscriber24hRepository youtubeSubscribe24hRepository;

    @Autowired
    private TikTokFollower24hRepository tikTokFollower24hRepository;

    @Autowired
    private FacebookFollower24hRepository facebookFollower24hRepository;

    @Autowired
    private TikTokLike24hRepository tikTokLike24hRepository;

    @Autowired
    private TikTokAccountHistoryRepository tikTokAccountHistoryRepository;

    @Autowired
    private FacebookFollowerHistoryRepository facebookFollowerHistoryRepository;

    @Autowired
    private TikTokLikeHistoryRepository tikTokLikeHistoryRepository;

    @Autowired
    private TikTokCommentHistoryRepository tikTokCommentHistoryRepository;

    @Autowired
    private HistorySumRepository historySumRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    @Autowired
    private DataCommentRepository dataCommentRepository;

    @Autowired
    private TiktokTask tiktokTask;
    @Autowired
    private YoutubeTask youtubeTask;

    @Autowired
    private FacebookTask facebookTask;
    @Autowired
    private YoutubeUpdate youtubeUpdate;
    @Autowired
    private TiktokUpdate tiktokUpdate;
    @Autowired
    private FacebookUpdate facebookUpdate;

    @GetMapping(value = "getTaskDevice", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> getTaskDevice(@RequestHeader(defaultValue = "") String Authorization,
                                                   @RequestParam(defaultValue = "") String device_id) throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran=new Random();
            Thread.sleep(500+ran.nextInt(1500));
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
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
            SettingSystem settingSystem=settingSystemRepository.get_Setting_System();
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
                        accountTask.setAccount(account_get);
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
                        accountTask.setAccount(account_get);
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
                    accountTaskRepository.reset_Thread_Index_By_AccountId(accountTask.getAccount().getAccount_id().trim());
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
            List<TaskPriority> priorityTasks =taskPriorityRepository.get_Priority_Task();

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
                if(task.equals("tiktok_follower")){
                    get_task=tiktokTask.tiktok_follower(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("youtube_view")){
                    get_task=youtubeTask.youtube_view(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("youtube_like")){
                    get_task=youtubeTask.youtube_like(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("task_tiktok_like")){
                    get_task=tiktokTask.tiktok_like(accountTask.getAccount().getAccount_id().trim());
                }
                if(get_task!=null?get_task.get("status").equals(true):false){
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
                accountTask.setRunning(1);
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
            LogError logError =new LogError();
            logError.setMethod_name(stackTraceElement.getMethodName());
            logError.setLine_number(stackTraceElement.getLineNumber());
            logError.setClass_name(stackTraceElement.getClassName());
            logError.setFile_name(stackTraceElement.getFileName());
            logError.setMessage(e.getMessage());
            logError.setAdd_time(System.currentTimeMillis());
            Date date_time = new Date(System.currentTimeMillis());
            // Tạo SimpleDateFormat với múi giờ GMT+7
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            String formattedDate = sdf.format(date_time);
            logError.setDate_time(formattedDate);
            logErrorRepository.save(logError);

            resp.put("status", false);
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
            //Thread.sleep(ran.nextInt(2000));
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
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
            SettingSystem settingSystem=settingSystemRepository.get_Setting_System();
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
                            accountTask.setAccount(account_get);
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
                            accountTask.setAccount(account_get);
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
                        accountTask.setAccount(account_get);
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
                    accountTaskRepository.reset_Thread_Index_By_AccountId(accountTask.getAccount().getAccount_id().trim());
                    if(profile.getNum_account()>1){
                        accountTask = accountTaskRepository.get_Account_By_ProfileId(device_id.trim()+"_"+profile_id.trim());
                    }
                }
            }else{
                profile.setUpdate_time(System.currentTimeMillis());
                profileRepository.save(profile);
                accountTask = accountTaskRepository.get_Account_By_ProfileId(device_id.trim()+"_"+profile_id.trim());
            }
            if(accountTask==null){
                accountTaskRepository.reset_Thread_Index_By_ProfileId(profile_id.trim());
                profile=profileRepository.get_Profile_Get_Task(device_id.trim());
                if(profile!=null&&device.getNum_profile()>1){
                    resp.put("status", true);
                    data.put("task", "profile_changer");
                    data.put("profile", profile.getProfile_id().split(device_id.trim()+"_")[1]);
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else if(profile!=null&&device.getNum_profile()==1){
                    profile.setUpdate_time(System.currentTimeMillis());
                    profileRepository.save(profile);
                    accountTask = accountTaskRepository.get_Account_By_ProfileId(device_id.trim()+"_"+profile_id.trim());
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
            List<TaskPriority> priorityTasks =taskPriorityRepository.get_Priority_Task();

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
                if(task.equals("tiktok_follower")){
                    get_task=tiktokTask.tiktok_follower(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("youtube_view")){
                    get_task=youtubeTask.youtube_view(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("youtube_like")){
                    get_task=youtubeTask.youtube_like(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("youtube_subscriber")){
                    get_task=youtubeTask.youtube_subscriber(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("tiktok_like")){
                    get_task=tiktokTask.tiktok_like(accountTask.getAccount().getAccount_id().trim());
                }
                if(get_task!=null?get_task.get("status").equals(true):false){
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
                accountTask.setRunning(1);
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
            LogError logError =new LogError();
            logError.setMethod_name(stackTraceElement.getMethodName());
            logError.setLine_number(stackTraceElement.getLineNumber());
            logError.setClass_name(stackTraceElement.getClassName());
            logError.setFile_name(stackTraceElement.getFileName());
            logError.setMessage(e.getMessage());
            logError.setAdd_time(System.currentTimeMillis());
            Date date_time = new Date(System.currentTimeMillis());
            // Tạo SimpleDateFormat với múi giờ GMT+7
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            String formattedDate = sdf.format(date_time);
            logError.setDate_time(formattedDate);
            logErrorRepository.save(logError);

            resp.put("status", false);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }

    }


    @GetMapping(value = "getTaskThuan", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> getTaskThuan(@RequestHeader(defaultValue = "") String Authorization,
                                                       @RequestParam(defaultValue = "") String device_id,
                                                       @RequestParam(defaultValue = "") String profile_id) throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran=new Random();
            //Thread.sleep(ran.nextInt(2000));
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
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
            if (profile_id.length()==0) {
                resp.put("status", false);
                data.put("message", "profile_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //---------------------get_Account------------------//
            SettingSystem settingSystem=settingSystemRepository.get_Setting_System();
            Device device=deviceRepository.check_DeviceId(device_id.trim());
            Profile profile=null;
            if(device!=null){
                device.setUpdate_time(System.currentTimeMillis());
                deviceRepository.save(device);
                profile =profileRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                if(profile==null){
                    resp.put("status", false);
                    data.put("message", "profile không tồn tại");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
                }
                if(profile.getNum_account()<settingSystem.getMax_acc()){
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
                        accountTask.setAccount(account_get);
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
                        data.put("platform", "system");
                        data.put("task", "login");
                        data.put("profile", profile.getProfile_id().split(device_id.trim()+"_")[1]);
                        data.put("account_id", account_get.getAccount_id().trim());
                        data.put("password", account_get.getPassword().trim());
                        data.put("recover_mail", account_get.getRecover_mail().trim());
                        data.put("auth_2fa", account_get.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }
            }else
            {
                Device device_new=new Device();
                device_new.setDevice_id(device_id.trim());
                device_new.setAdd_time(System.currentTimeMillis());
                device_new.setState(1);
                device_new.setBox(null);
                device_new.setUpdate_time(System.currentTimeMillis());
                device_new.setNum_account(0);
                device_new.setNum_profile(settingSystem.getMax_profile());
                deviceRepository.save(device_new);
                device=device_new;

                for(int i=0;i<settingSystem.getMax_profile();i++){
                    Profile profile_new=new Profile();
                    profile_new.setProfile_id(device_id.trim()+"_"+i);
                    profile_new.setAdd_time(System.currentTimeMillis());
                    profile_new.setDevice(device);
                    profile_new.setNum_account(0);
                    profile_new.setUpdate_time(0L);
                    profile_new.setState(1);
                    profileRepository.save(profile_new);
                }
                profile=profileRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
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
                    accountTask.setAccount(account_get);
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
                    data.put("platform", "system");
                    data.put("task", "login");
                    data.put("profile", profile.getProfile_id().split(device_id.trim()+"_")[1]);
                    data.put("account_id", account_get.getAccount_id().trim());
                    data.put("password", account_get.getPassword().trim());
                    data.put("recover_mail", account_get.getRecover_mail().trim());
                    data.put("auth_2fa", account_get.getAuth_2fa().trim());
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
            //--------------------end_get_Account----------------------//
            AccountTask accountTask = accountTaskRepository.check_Account_Running_By_ProfileId(device_id.trim()+"_"+profile_id);
            //Check số lần get nhiệm vụ của 1 account
            if(accountTask!=null){
                if(accountTask.getTask_index()>=settingSystem.getMax_task()){
                    accountTaskRepository.reset_Thread_Index_By_AccountId(accountTask.getAccount().getAccount_id().trim());
                    accountTask=null;
                    if(profile.getNum_account()>1){
                        accountTask = accountTaskRepository.get_Account_By_ProfileId(device_id.trim()+"_"+profile_id.trim());
                    }
                }
            }else{
                profile.setUpdate_time(System.currentTimeMillis());
                profileRepository.save(profile);
                accountTask = accountTaskRepository.get_Account_By_ProfileId(device_id.trim()+"_"+profile_id.trim());
            }
            if(accountTask==null){
                accountTaskRepository.reset_Thread_Index_By_ProfileId(profile_id.trim());
                profile=profileRepository.get_Profile_Get_Task(device_id.trim());
                if(device.getNum_profile()>1){
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "profile_changer");
                    data.put("profile_id", profile.getProfile_id().split(device_id.trim()+"_")[1]);
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else if(device.getNum_profile()==1){
                    profile.setUpdate_time(System.currentTimeMillis());
                    profileRepository.save(profile);
                    accountTask = accountTaskRepository.get_Account_By_ProfileId(device_id.trim()+"_"+profile_id.trim());
                }else{
                    resp.put("status", false);
                    data.put("message", "Không có account để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
            accountTask.setTask_index(accountTask.getTask_index()+1);
            List<TaskPriority> priorityTasks =taskPriorityRepository.get_Priority_Task();

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
                if(task.equals("tiktok_follower")){
                    get_task=tiktokTask.tiktok_follower(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("youtube_view")){
                    get_task=youtubeTask.youtube_view(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("youtube_like")){
                    get_task=youtubeTask.youtube_like(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("youtube_subscriber")){
                    get_task=youtubeTask.youtube_subscriber(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("tiktok_like")){
                    get_task=tiktokTask.tiktok_like(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("tiktok_view")){
                    get_task=tiktokTask.tiktok_view(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("tiktok_comment")){
                    get_task=tiktokTask.tiktok_comment(accountTask.getAccount().getAccount_id().trim());
                }
                if(get_task!=null?get_task.get("status").equals(true):false){
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
                accountTask.setRunning(1);
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
            LogError logError =new LogError();
            logError.setMethod_name(stackTraceElement.getMethodName());
            logError.setLine_number(stackTraceElement.getLineNumber());
            logError.setClass_name(stackTraceElement.getClassName());
            logError.setFile_name(stackTraceElement.getFileName());
            logError.setMessage(e.getMessage());
            logError.setAdd_time(System.currentTimeMillis());
            Date date_time = new Date(System.currentTimeMillis());
            // Tạo SimpleDateFormat với múi giờ GMT+7
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            String formattedDate = sdf.format(date_time);
            logError.setDate_time(formattedDate);
            logErrorRepository.save(logError);

            resp.put("status", false);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping(value = "getTask", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> getTask(@RequestHeader(defaultValue = "") String Authorization,
                                                       @RequestParam(defaultValue = "") String device_id,
                                                       @RequestParam(defaultValue = "") String profile_id) throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran=new Random();
            //Thread.sleep(ran.nextInt(2000));
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
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
            SettingSystem settingSystem=settingSystemRepository.get_Setting_System();
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
                            accountTask.setAccount(account_get);
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
                            accountTask.setAccount(account_get);
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
                        accountTask.setAccount(account_get);
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
                    accountTaskRepository.reset_Thread_Index_By_AccountId(accountTask.getAccount().getAccount_id().trim());
                    if(profile.getNum_account()>1){
                        accountTask = accountTaskRepository.get_Account_By_ProfileId(device_id.trim()+"_"+profile_id.trim());
                    }else{
                        accountTask=null;
                    }
                }
            }else{
                profile.setUpdate_time(System.currentTimeMillis());
                profileRepository.save(profile);
                accountTask = accountTaskRepository.get_Account_By_ProfileId(device_id.trim()+"_"+profile_id.trim());
            }
            if(accountTask==null){
                accountTaskRepository.reset_Thread_Index_By_ProfileId(profile_id.trim());
                profile=profileRepository.get_Profile_Get_Task(device_id.trim());
                if(profile!=null&&device.getNum_profile()>1){
                    resp.put("status", true);
                    data.put("task", "profile_changer");
                    data.put("profile", profile.getProfile_id().split(device_id.trim()+"_")[1]);
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else if(profile!=null&&device.getNum_profile()==1){
                    profile.setUpdate_time(System.currentTimeMillis());
                    profileRepository.save(profile);
                    accountTask = accountTaskRepository.get_Account_By_ProfileId(device_id.trim()+"_"+profile_id.trim());
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
            List<TaskPriority> priorityTasks =taskPriorityRepository.get_Priority_Task_By_Platform(accountTask.getPlatform());

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
                if(task.equals("tiktok_follower")){
                    get_task=tiktokTask.tiktok_follower(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("youtube_view")){
                    get_task=youtubeTask.youtube_view(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("youtube_like")){
                    get_task=youtubeTask.youtube_like(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("tiktok_like")){
                    get_task=tiktokTask.tiktok_like(accountTask.getAccount().getAccount_id().trim());
                }
                if(get_task!=null?get_task.get("status").equals(true):false){
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
                accountTask.setRunning(1);
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
            LogError logError =new LogError();
            logError.setMethod_name(stackTraceElement.getMethodName());
            logError.setLine_number(stackTraceElement.getLineNumber());
            logError.setClass_name(stackTraceElement.getClassName());
            logError.setFile_name(stackTraceElement.getFileName());
            logError.setMessage(e.getMessage());
            logError.setAdd_time(System.currentTimeMillis());
            Date date_time = new Date(System.currentTimeMillis());
            // Tạo SimpleDateFormat với múi giờ GMT+7
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            String formattedDate = sdf.format(date_time);
            logError.setDate_time(formattedDate);
            logErrorRepository.save(logError);

            resp.put("status", false);
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
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
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
            SettingSystem settingSystem=settingSystemRepository.get_Setting_System();
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
                        accountTask.setAccount(account_get);
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
                        accountTask.setAccount(account_get);
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
            List<TaskPriority> priorityTasks =taskPriorityRepository.get_Priority_Task();

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
                if(task.equals("tiktok_follower")){
                    get_task=tiktokTask.tiktok_follower(account_id.trim());
                }else if(task.equals("youtube_view")){
                    get_task=youtubeTask.youtube_view(account_id.trim());
                }
                if(get_task!=null?get_task.get("status").equals(true):false){
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
                accountTask.setRunning(1);
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
            LogError logError =new LogError();
            logError.setMethod_name(stackTraceElement.getMethodName());
            logError.setLine_number(stackTraceElement.getLineNumber());
            logError.setClass_name(stackTraceElement.getClassName());
            logError.setFile_name(stackTraceElement.getFileName());
            logError.setMessage(e.getMessage());
            logError.setAdd_time(System.currentTimeMillis());
            Date date_time = new Date(System.currentTimeMillis());
            // Tạo SimpleDateFormat với múi giờ GMT+7
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            String formattedDate = sdf.format(date_time);
            logError.setDate_time(formattedDate);
            logErrorRepository.save(logError);

            resp.put("status", false);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }

    }


    @GetMapping(value = "getTask001", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> getTask001(@RequestHeader(defaultValue = "") String Authorization,
                                                   @RequestParam(defaultValue = "") String account_id,
                                                   @RequestParam(defaultValue = "") String platform,
                                                   @RequestParam(defaultValue = "") String device_id) throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran=new Random();
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
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
            Device device=deviceRepository.check_DeviceId(device_id.trim());
            if(device!=null){
                device.setUpdate_time(System.currentTimeMillis());
                deviceRepository.save(device);
                if(accountTaskRepository.find_AccountTask_By_AccountId(account_id.trim())==0)
                {
                    Account account= accountRepository.get_Account_By_Account_id(account_id.trim());
                    if(account==null){
                        Account account_new=new Account();
                        account_new.setAccount_id(account_id);
                        account_new.setPassword("");
                        account_new.setRecover_mail("");
                        account_new.setLive(1);
                        account_new.setAuth_2fa(account_new.getAuth_2fa());
                        account_new.setComputer_id(null);
                        account_new.setBox_id(null);
                        account_new.setDevice_id(null);
                        account_new.setProfile_id(null);
                        account_new.setAdd_time(System.currentTimeMillis());
                        account_new.setGet_time(System.currentTimeMillis());
                        accountRepository.save(account_new);
                        account=account_new;
                    }


                    AccountTask accountTask=new AccountTask();
                    accountTask.setAccount(account);
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
                if(accountTaskRepository.find_AccountTask_By_AccountId(account_id.trim())==0)
                {
                    Account account= accountRepository.get_Account_By_Account_id(account_id.trim());
                    if(account==null){
                        Account account_new=new Account();
                        account_new.setAccount_id(account_id);
                        account_new.setPassword("");
                        account_new.setRecover_mail("");
                        account_new.setLive(1);
                        account_new.setAuth_2fa(account_new.getAuth_2fa());
                        account_new.setComputer_id(null);
                        account_new.setBox_id(null);
                        account_new.setDevice_id(null);
                        account_new.setProfile_id(null);
                        account_new.setAdd_time(System.currentTimeMillis());
                        account_new.setGet_time(System.currentTimeMillis());
                        accountRepository.save(account_new);
                        account=account_new;
                    }


                    AccountTask accountTask=new AccountTask();
                    accountTask.setAccount(account);
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
                }
            }
            //--------------------end_get_Account----------------------//
            AccountTask accountTask = accountTaskRepository.get_Account_By_Account_id(account_id.trim());

            List<TaskPriority> priorityTasks;
            if(platform.length()==0){
              priorityTasks =taskPriorityRepository.get_Priority_Task();
            }else{
              priorityTasks =taskPriorityRepository.get_Priority_Task_By_Platform(platform.trim());
            }


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
                System.out.println(task);
                if(task.equals("tiktok_follower")){
                    get_task= tiktokTask.tiktok_follower(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("youtube_view")){
                    get_task=youtubeTask.youtube_view(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("youtube_like")){
                    get_task=youtubeTask.youtube_like(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("youtube_subscriber")){
                    get_task=youtubeTask.youtube_subscriber(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("tiktok_like")){
                    get_task=tiktokTask.tiktok_like(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("tiktok_view")){
                    get_task=tiktokTask.tiktok_view(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("tiktok_comment")){
                    get_task=tiktokTask.tiktok_comment(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("facebook_follower")){
                    get_task=facebookTask.facebook_follower(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("facebook_like")){
                    get_task=facebookTask.facebook_like(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("facebook_view")){
                    get_task=facebookTask.facebook_view(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("facebook_comment")){
                    get_task=facebookTask.facebook_comment(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("facebook_member")){
                    get_task=facebookTask.facebook_member(accountTask.getAccount().getAccount_id().trim());
                }
                if(get_task!=null?get_task.get("status").equals(true):false){
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
                accountTask.setRunning(1);
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
            LogError logError =new LogError();
            logError.setMethod_name(stackTraceElement.getMethodName());
            logError.setLine_number(stackTraceElement.getLineNumber());
            logError.setClass_name(stackTraceElement.getClassName());
            logError.setFile_name(stackTraceElement.getFileName());
            logError.setMessage(e.getMessage());
            logError.setAdd_time(System.currentTimeMillis());
            Date date_time = new Date(System.currentTimeMillis());
            // Tạo SimpleDateFormat với múi giờ GMT+7
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            String formattedDate = sdf.format(date_time);
            logError.setDate_time(formattedDate);
            logErrorRepository.save(logError);

            resp.put("status", false);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping(value = "/updateTask", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> updateTask(@RequestHeader(defaultValue = "") String Authorization,
                                                   @RequestParam(defaultValue = "") String account_id,@RequestParam  Boolean status,
                                                   @RequestParam(defaultValue = "") String task,
                                                   @RequestParam(defaultValue = "") String task_key,
                                                   @RequestParam(defaultValue = "0") Integer viewing_time,
                                      @RequestParam(defaultValue = "") String platform) {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
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
            String platform_Check = platform.toLowerCase().trim();
            if(platform_Check.equals("youtube")){
                if(task.toLowerCase().trim().equals("view")&&status==true){
                    youtubeUpdate.youtube_view(account_id.trim(),task_key.trim());
                }else  if(task.toLowerCase().trim().equals("subscriber")&&status==true){
                    youtubeUpdate.youtube_subscriber(account_id.trim(),task_key.trim());
                } else  if(task.toLowerCase().trim().equals("like")&&status==true){
                    youtubeUpdate.youtube_like(account_id.trim(),task_key.trim());
                }
            }else if(platform_Check.equals("tiktok")){
                if(task.toLowerCase().trim().equals("follower")&&status==true){
                   tiktokUpdate.tiktok_follower(account_id.trim(),task_key.trim());
                }else  if(task.toLowerCase().trim().equals("like")&&status==true){
                    tiktokUpdate.tiktok_like(account_id.trim(),task_key.trim());
                }else  if(task.toLowerCase().trim().equals("comment")){
                    tiktokUpdate.tiktok_comment(account_id.trim(),task_key.trim(),status);
                }
            }else if(platform_Check.equals("facebook")){
                if(task.toLowerCase().trim().equals("follower")&&status==true){
                  facebookUpdate.facebook_follower(account_id.trim(),task_key.trim());
                }else  if(task.toLowerCase().trim().equals("like")&&status==true){
                    facebookUpdate.facebook_like(account_id.trim(),task_key.trim());
                }else  if(task.toLowerCase().trim().equals("comment")) {
                    facebookUpdate.facebook_comment(account_id.trim(), task_key.trim(), status);
                }else  if(task.toLowerCase().trim().equals("member")&&status==true) {
                    facebookUpdate.facebook_member(account_id.trim(), task_key.trim());
                }
            }
            if(status==true){
                try {
                    OrderRunning orderRunning=null;
                    if(platform_Check.equals("youtube")&&task.toLowerCase().trim().equals("subscriber")){
                        String order_Key= dataSubscriberRepository.get_ChannelId_By_VideoId(task_key.trim());
                        orderRunning=orderRunningRepository.find_Order_By_Order_Key(order_Key,task.trim(),platform.trim());
                    }else{
                        orderRunning=orderRunningRepository.find_Order_By_Order_Key(task_key.trim(),task.trim(),platform.trim());
                    }
                    if(orderRunning!=null){
                        HistorySum historySum=new HistorySum();
                        historySum.setOrderRunning(orderRunning);
                        historySum.setAccount_id(account_id.trim());
                        historySum.setViewing_time(viewing_time);
                        historySum.setAdd_time(System.currentTimeMillis());
                        historySumRepository.save(historySum);
                    }
                }catch (Exception e){
                    StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
                    LogError logError =new LogError();
                    logError.setMethod_name(stackTraceElement.getMethodName());
                    logError.setLine_number(stackTraceElement.getLineNumber());
                    logError.setClass_name(stackTraceElement.getClassName());
                    logError.setFile_name(stackTraceElement.getFileName());
                    logError.setMessage(e.getMessage());
                    logError.setAdd_time(System.currentTimeMillis());
                    Date date_time = new Date(System.currentTimeMillis());
                    // Tạo SimpleDateFormat với múi giờ GMT+7
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                    String formattedDate = sdf.format(date_time);
                    logError.setDate_time(formattedDate);
                    logErrorRepository.save(logError);
                }
            }
            resp.put("status", true);
            data.put("message", "Update thành công!");
            resp.put("data", data);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (Exception e) {
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            LogError logError =new LogError();
            logError.setMethod_name(stackTraceElement.getMethodName());
            logError.setLine_number(stackTraceElement.getLineNumber());
            logError.setClass_name(stackTraceElement.getClassName());
            logError.setFile_name(stackTraceElement.getFileName());
            logError.setMessage(e.getMessage());
            logError.setAdd_time(System.currentTimeMillis());
            Date date_time = new Date(System.currentTimeMillis());
            // Tạo SimpleDateFormat với múi giờ GMT+7
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            String formattedDate = sdf.format(date_time);
            logError.setDate_time(formattedDate);
            logErrorRepository.save(logError);

            resp.put("status", false);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "resetTaskError", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> resetTaskError() throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            accountTaskRepository.reset_Task_Error();
            resp.put("status",true);
            data.put("message", "reset thành công");
            resp.put("data",data);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        }catch (Exception e){
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            LogError logError =new LogError();
            logError.setMethod_name(stackTraceElement.getMethodName());
            logError.setLine_number(stackTraceElement.getLineNumber());
            logError.setClass_name(stackTraceElement.getClassName());
            logError.setFile_name(stackTraceElement.getFileName());
            logError.setMessage(e.getMessage());
            logError.setAdd_time(System.currentTimeMillis());
            Date date_time = new Date(System.currentTimeMillis());
            // Tạo SimpleDateFormat với múi giờ GMT+7
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            String formattedDate = sdf.format(date_time);
            logError.setDate_time(formattedDate);
            logErrorRepository.save(logError);

            resp.put("status", false);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "resetTaskDevice", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> resetTaskDevice(@RequestHeader(defaultValue = "") String Authorization,
                                                              @RequestParam(defaultValue = "") String device_id) throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            accountTaskRepository.reset_Task_By_DeviceId(device_id.trim());
            resp.put("status",true);
            data.put("message", "reset thành công");
            resp.put("data",data);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        }catch (Exception e){
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            LogError logError =new LogError();
            logError.setMethod_name(stackTraceElement.getMethodName());
            logError.setLine_number(stackTraceElement.getLineNumber());
            logError.setClass_name(stackTraceElement.getClassName());
            logError.setFile_name(stackTraceElement.getFileName());
            logError.setMessage(e.getMessage());
            logError.setAdd_time(System.currentTimeMillis());
            Date date_time = new Date(System.currentTimeMillis());
            // Tạo SimpleDateFormat với múi giờ GMT+7
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            String formattedDate = sdf.format(date_time);
            logError.setDate_time(formattedDate);
            logErrorRepository.save(logError);

            resp.put("status", false);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "resetTaskProfile", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> resetTaskProfile(@RequestHeader(defaultValue = "") String Authorization,
                                                                @RequestParam(defaultValue = "") String device_id,
                                                               @RequestParam(defaultValue = "") String profile_id) throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            accountTaskRepository.reset_Task_By_ProfileId(device_id.trim()+"_"+profile_id);
            resp.put("status",true);
            data.put("message", "reset thành công");
            resp.put("data",data);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        }catch (Exception e){
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            LogError logError =new LogError();
            logError.setMethod_name(stackTraceElement.getMethodName());
            logError.setLine_number(stackTraceElement.getLineNumber());
            logError.setClass_name(stackTraceElement.getClassName());
            logError.setFile_name(stackTraceElement.getFileName());
            logError.setMessage(e.getMessage());
            logError.setAdd_time(System.currentTimeMillis());
            Date date_time = new Date(System.currentTimeMillis());
            // Tạo SimpleDateFormat với múi giờ GMT+7
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            String formattedDate = sdf.format(date_time);
            logError.setDate_time(formattedDate);
            logErrorRepository.save(logError);

            resp.put("status", false);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
    }

}

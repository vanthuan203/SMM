package com.nts.awspremium.controller;

import com.google.gson.JsonObject;
import com.nts.awspremium.model.*;
import com.nts.awspremium.repositories.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/setting")
public class SettingController {
    @Autowired
    private SettingSystemRepository settingSystemRepository;
    @Autowired
    private PlatformRepository platformRepository;
    @Autowired
    private SettingYoutubeRepository settingYoutubeRepository;
    @Autowired
    private SettingTikTokRepository settingTikTokRepository;
    @Autowired
    private SettingFacebookRepository settingFacebookRepository;
    @Autowired
    private SettingXRepository settingXRepository;
    @Autowired
    private SettingInstagramRepository settingInstagramRepository;
    @Autowired
    private SettingThreadsRepository settingThreadsRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    @Autowired
    private TaskPriorityRepository taskPriorityRepository;
    @Autowired
    private OrderRunningRepository orderRunningRepository;
    @Autowired
    private ProfileTaskRepository profileTaskRepository;
    @GetMapping(path = "get_Setting_Platform",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get_Setting_Platform(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if(checktoken ==0){
            resp.put("status",false);
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            JSONArray jsonArray =new JSONArray();
            SettingYoutube settingYoutube=settingYoutubeRepository.get_Setting();
            JSONObject obj = new JSONObject();
            obj.put("id","youtube");
            obj.put("platform","youtube");
            obj.put("max_follower", settingYoutube.getMax_subscriber());
            obj.put("max_like", settingYoutube.getMax_like());
            obj.put("max_day_activity", settingYoutube.getMax_day_activity());
            obj.put("max_activity_24h", settingYoutube.getMax_activity_24h());
            obj.put("update_time", settingYoutube.getUpdate_time());
            jsonArray.add(obj);
            SettingTiktok settingTiktok=settingTikTokRepository.get_Setting();
            JSONObject obj1 = new JSONObject();
            obj1.put("id","tiktok");
            obj1.put("platform","tiktok");
            obj1.put("max_follower", settingTiktok.getMax_follower());
            obj1.put("max_like", settingTiktok.getMax_like());
            obj1.put("max_day_activity", settingTiktok.getMax_day_activity());
            obj1.put("max_activity_24h", settingTiktok.getMax_activity_24h());
            obj1.put("update_time", settingTiktok.getUpdate_time());
            jsonArray.add(obj1);
            resp.put("setting_platform",jsonArray);
            SettingFacebook settingFacebook=settingFacebookRepository.get_Setting();
            JSONObject obj2 = new JSONObject();
            obj2.put("id","facebook");
            obj2.put("platform","facebook");
            obj2.put("max_follower", settingFacebook.getMax_follower());
            obj2.put("max_like", settingFacebook.getMax_like());
            obj2.put("max_day_activity", settingFacebook.getMax_day_activity());
            obj2.put("max_activity_24h", settingFacebook.getMax_activity_24h());
            obj2.put("update_time", settingFacebook.getUpdate_time());
            jsonArray.add(obj2);
            SettingX settingX=settingXRepository.get_Setting();
            JSONObject obj3 = new JSONObject();
            obj3.put("id","x");
            obj3.put("platform","x");
            obj3.put("max_follower", settingX.getMax_follower());
            obj3.put("max_like", settingX.getMax_like());
            obj3.put("max_day_activity", settingX.getMax_day_activity());
            obj3.put("max_activity_24h", settingX.getMax_activity_24h());
            obj3.put("update_time", settingX.getUpdate_time());
            jsonArray.add(obj3);
            SettingInstagram settingInstagram=settingInstagramRepository.get_Setting();
            JSONObject obj4 = new JSONObject();
            obj4.put("id","instagram");
            obj4.put("platform","instagram");
            obj4.put("max_follower", settingInstagram.getMax_follower());
            obj4.put("max_like", settingInstagram.getMax_like());
            obj4.put("max_day_activity", settingInstagram.getMax_day_activity());
            obj4.put("max_activity_24h", settingInstagram.getMax_activity_24h());
            obj4.put("update_time", settingInstagram.getUpdate_time());
            jsonArray.add(obj4);
            SettingThreads settingThreads=settingThreadsRepository.get_Setting();
            JSONObject obj5 = new JSONObject();
            obj5.put("id","threads");
            obj5.put("platform","threads");
            obj5.put("max_follower", settingThreads.getMax_follower());
            obj5.put("max_like", settingThreads.getMax_like());
            obj5.put("max_day_activity", settingThreads.getMax_day_activity());
            obj5.put("max_activity_24h", settingThreads.getMax_activity_24h());
            obj5.put("update_time", settingThreads.getUpdate_time());
            jsonArray.add(obj5);
            resp.put("setting_platform",jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
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

            resp.put("status",false);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }

    }
    @GetMapping(path = "get_Setting_System",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get_Setting_System(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if(checktoken ==0){
            resp.put("status",false);
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            JSONArray jsonArray =new JSONArray();
            SettingSystem settingSystem=settingSystemRepository.get_Setting_System();
            JSONObject obj = new JSONObject();
            obj.put("id",settingSystem.getId());
            obj.put("max_priority", settingSystem.getMax_priority());
            obj.put("max_mysql", settingSystem.getMax_mysql());
            obj.put("reboot_time", settingSystem.getReboot_time());
            obj.put("clear_data_package", settingSystem.getClear_data_package());
            obj.put("time_waiting_task", settingSystem.getTime_waiting_task());
            obj.put("max_thread", settingSystem.getMax_thread());
            obj.put("max_thread_pending", settingSystem.getMax_thread_pending());
            obj.put("max_thread_fix", settingSystem.getMax_thread_fix());
            jsonArray.add(obj);
            resp.put("accounts",jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
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

            resp.put("status",false);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }

    }

    @GetMapping(path = "get_List_Platform",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get_List_Platform(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if(checktoken ==0){
            resp.put("status",false);
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            JSONArray jsonArray =new JSONArray();
            List<Platform> platforms=platformRepository.get_List_Platform();
            for(int i=0;i<platforms.size();i++){
                JSONObject obj = new JSONObject();
                obj.put("id",platforms.get(i).getId());
                obj.put("mode",platforms.get(i).getMode());
                obj.put("platform",platforms.get(i).getPlatform());
                obj.put("priority", platforms.get(i).getPriority());
                obj.put("state", platforms.get(i).getState());
                obj.put("activity", platforms.get(i).getActivity());
                obj.put("register_account", platforms.get(i).getRegister_account());
                obj.put("login_account", platforms.get(i).getLogin_account());
                obj.put("login_time", platforms.get(i).getLogin_time());
                obj.put("register_time", platforms.get(i).getRegister_time());
                obj.put("register_limit", platforms.get(i).getRegister_limit());
                jsonArray.add(obj);
            }

            resp.put("platform",jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
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

            resp.put("status",false);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }

    }

    @GetMapping(path = "get_Task_Priority",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get_Task_Priority(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if(checktoken ==0){
            resp.put("status",false);
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            JSONArray jsonArray =new JSONArray();
            List<TaskPriority> taskPriorityList=taskPriorityRepository.get_All_Priority_Task();
            for(int i=0;i<taskPriorityList.size();i++){
                JSONObject obj = new JSONObject();
                obj.put("task",taskPriorityList.get(i).getTask());
                obj.put("priority", taskPriorityList.get(i).getPriority());
                obj.put("state", taskPriorityList.get(i).getState());
                obj.put("platform", taskPriorityList.get(i).getPlatform());
                obj.put("update_time", taskPriorityList.get(i).getUpdate_time());
                jsonArray.add(obj);
            }

            resp.put("task_priority",jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
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

            resp.put("status",false);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }

    }

    @PostMapping(path = "update_Setting_System",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> update_Setting_System(@RequestHeader(defaultValue = "") String Authorization,
                                                 @RequestBody SettingSystem setting_update
                                                 ){
        JSONObject resp = new JSONObject();
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if(checktoken ==0){
            resp.put("status",false);
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            SettingSystem settingSystem=settingSystemRepository.get_Setting_System();
            settingSystem.setMax_mysql(setting_update.getMax_mysql());
            settingSystem.setMax_priority(setting_update.getMax_priority());
            settingSystem.setReboot_time(setting_update.getReboot_time());
            settingSystem.setClear_data_package(setting_update.getClear_data_package());
            settingSystem.setTime_waiting_task(setting_update.getTime_waiting_task());
            settingSystem.setMax_thread(setting_update.getMax_thread());
            settingSystem.setMax_thread_pending(setting_update.getMax_thread_pending());
            settingSystem.setMax_thread_fix(setting_update.getMax_thread_fix());
            settingSystemRepository.save(settingSystem);
            JSONObject obj = new JSONObject();
            obj.put("id",settingSystem.getId());
            obj.put("max_priority", settingSystem.getMax_priority());
            obj.put("max_mysql", settingSystem.getMax_mysql());
            obj.put("reboot_time", settingSystem.getReboot_time());
            obj.put("clear_data_package", settingSystem.getClear_data_package());
            obj.put("time_waiting_task", settingSystem.getTime_waiting_task());
            obj.put("max_thread", settingSystem.getMax_thread());
            obj.put("max_thread_pending", settingSystem.getMax_thread_pending());
            obj.put("max_thread_fix", settingSystem.getMax_thread_fix());
            resp.put("account",obj);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
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

            resp.put("status",false);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }

    }
    public Boolean  update_State_Platform() {
        try {
            List<String> platforms = serviceRepository.get_Platform_In_OrderRunning();
            platformRepository.update_State_1_Platform(platforms);
            platformRepository.update_State_0_Platform(platforms);
            return true;
        } catch (Exception e) {
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            LogError logError = new LogError();
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

            return false;
        }
    }


    public Boolean  updateTime_Pending_Task() {
        try {
            Integer thread_set= orderRunningRepository.get_Sum_Thread_Order_Running_By_Mode_Auto();
            Integer thread_running= profileTaskRepository.count_Profile_Running_And_Mode_Auto();
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            if(thread_running<thread_set){
                if(thread_running<thread_set*3){
                    settingSystem.setTime_waiting_task((settingSystem.getTime_waiting_task()-5)>0?(settingSystem.getTime_waiting_task()-5):0);
                }
                if(thread_running<settingSystem.getMax_thread()){
                    settingSystem.setMax_thread((settingSystem.getMax_thread()-10)>=0?(settingSystem.getMax_thread()-10):0);
                }
                settingSystemRepository.save(settingSystem);
            }else if(thread_running>thread_set){
                if(thread_running<thread_set*3){
                    settingSystem.setTime_waiting_task((settingSystem.getTime_waiting_task()-10)>0?(settingSystem.getTime_waiting_task()-10):0);
                }else if(thread_running>thread_set*3.5){
                    settingSystem.setTime_waiting_task((settingSystem.getTime_waiting_task()+10)<1050?(settingSystem.getTime_waiting_task()+10):1050);
                }
                if(thread_running>settingSystem.getMax_thread()){
                    settingSystem.setMax_thread((settingSystem.getMax_thread()+10)<settingSystem.getMax_thread_fix()?(settingSystem.getMax_thread()+10):settingSystem.getMax_thread_fix());
                }
                settingSystemRepository.save(settingSystem);
            }
            return true;
        } catch (Exception e) {
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            LogError logError = new LogError();
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

            return false;
        }
    }

    @PostMapping(path = "update_Setting_Platform",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> update_Setting_Platform(@RequestHeader(defaultValue = "") String Authorization,
                                                 @RequestBody JSONObject jsonObject
    ){
        JSONObject resp = new JSONObject();
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if(checktoken ==0){
            resp.put("status",false);
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            if(jsonObject.get("platform").toString().equals("youtube")){
                SettingYoutube settingYoutube=settingYoutubeRepository.get_Setting();
                settingYoutube.setMax_subscriber(Integer.parseInt(jsonObject.get("max_follower").toString()));
                settingYoutube.setMax_like(Integer.parseInt(jsonObject.get("max_like").toString()));
                settingYoutube.setMax_activity_24h(Integer.parseInt(jsonObject.get("max_activity_24h").toString()));
                settingYoutube.setMax_day_activity(Integer.parseInt(jsonObject.get("max_day_activity").toString()));
                settingYoutube.setUpdate_time(System.currentTimeMillis());
                settingYoutubeRepository.save(settingYoutube);
                JSONObject obj = new JSONObject();
                obj.put("id","youtube");
                obj.put("platform","youtube");
                obj.put("max_follower", settingYoutube.getMax_subscriber());
                obj.put("max_like", settingYoutube.getMax_like());
                obj.put("max_day_activity", settingYoutube.getMax_day_activity());
                obj.put("max_activity_24h", settingYoutube.getMax_activity_24h());
                obj.put("update_time", settingYoutube.getUpdate_time());
                resp.put("setting_platform",obj);
            }else if(jsonObject.get("platform").toString().equals("tiktok")){
                SettingTiktok settingTiktok=settingTikTokRepository.get_Setting();
                settingTiktok.setMax_follower(Integer.parseInt(jsonObject.get("max_follower").toString()));
                settingTiktok.setMax_like(Integer.parseInt(jsonObject.get("max_like").toString()));
                settingTiktok.setMax_activity_24h(Integer.parseInt(jsonObject.get("max_activity_24h").toString()));
                settingTiktok.setMax_day_activity(Integer.parseInt(jsonObject.get("max_day_activity").toString()));
                settingTiktok.setUpdate_time(System.currentTimeMillis());
                settingTikTokRepository.save(settingTiktok);
                JSONObject obj = new JSONObject();
                obj.put("id","tiktok");
                obj.put("platform","tiktok");
                obj.put("max_follower", settingTiktok.getMax_follower());
                obj.put("max_like", settingTiktok.getMax_like());
                obj.put("max_day_activity", settingTiktok.getMax_day_activity());
                obj.put("max_activity_24h", settingTiktok.getMax_activity_24h());
                obj.put("update_time", settingTiktok.getUpdate_time());
                resp.put("setting_platform",obj);
            }else if(jsonObject.get("platform").toString().equals("facebook")){
                SettingFacebook settingFacebook=settingFacebookRepository.get_Setting();
                settingFacebook.setMax_follower(Integer.parseInt(jsonObject.get("max_follower").toString()));
                settingFacebook.setMax_like(Integer.parseInt(jsonObject.get("max_like").toString()));
                settingFacebook.setMax_activity_24h(Integer.parseInt(jsonObject.get("max_activity_24h").toString()));
                settingFacebook.setMax_day_activity(Integer.parseInt(jsonObject.get("max_day_activity").toString()));
                settingFacebook.setUpdate_time(System.currentTimeMillis());
                settingFacebookRepository.save(settingFacebook);
                JSONObject obj = new JSONObject();
                obj.put("id","facebook");
                obj.put("platform","facebook");
                obj.put("max_follower", settingFacebook.getMax_follower());
                obj.put("max_like", settingFacebook.getMax_like());
                obj.put("max_day_activity", settingFacebook.getMax_day_activity());
                obj.put("max_activity_24h", settingFacebook.getMax_activity_24h());
                obj.put("update_time", settingFacebook.getUpdate_time());
                resp.put("setting_platform",obj);
            }else if(jsonObject.get("platform").toString().equals("x")){
                SettingX settingX=settingXRepository.get_Setting();
                settingX.setMax_follower(Integer.parseInt(jsonObject.get("max_follower").toString()));
                settingX.setMax_like(Integer.parseInt(jsonObject.get("max_like").toString()));
                settingX.setMax_activity_24h(Integer.parseInt(jsonObject.get("max_activity_24h").toString()));
                settingX.setMax_day_activity(Integer.parseInt(jsonObject.get("max_day_activity").toString()));
                settingX.setUpdate_time(System.currentTimeMillis());
                settingXRepository.save(settingX);
                JSONObject obj = new JSONObject();
                obj.put("id","x");
                obj.put("platform","x");
                obj.put("max_follower", settingX.getMax_follower());
                obj.put("max_like", settingX.getMax_like());
                obj.put("max_day_activity", settingX.getMax_day_activity());
                obj.put("max_activity_24h", settingX.getMax_activity_24h());
                obj.put("update_time", settingX.getUpdate_time());
                resp.put("setting_platform",obj);
            }else if(jsonObject.get("platform").toString().equals("instagram")){
                SettingInstagram settingInstagram=settingInstagramRepository.get_Setting();
                settingInstagram.setMax_follower(Integer.parseInt(jsonObject.get("max_follower").toString()));
                settingInstagram.setMax_like(Integer.parseInt(jsonObject.get("max_like").toString()));
                settingInstagram.setMax_activity_24h(Integer.parseInt(jsonObject.get("max_activity_24h").toString()));
                settingInstagram.setMax_day_activity(Integer.parseInt(jsonObject.get("max_day_activity").toString()));
                settingInstagram.setUpdate_time(System.currentTimeMillis());
                settingInstagramRepository.save(settingInstagram);
                JSONObject obj = new JSONObject();
                obj.put("id","instagram");
                obj.put("platform","instagram");
                obj.put("max_follower", settingInstagram.getMax_follower());
                obj.put("max_like", settingInstagram.getMax_like());
                obj.put("max_day_activity", settingInstagram.getMax_day_activity());
                obj.put("max_activity_24h", settingInstagram.getMax_activity_24h());
                obj.put("update_time", settingInstagram.getUpdate_time());
                resp.put("setting_platform",obj);
            }else if(jsonObject.get("platform").toString().equals("threads")){
                SettingThreads settingThreads=settingThreadsRepository.get_Setting();
                settingThreads.setMax_follower(Integer.parseInt(jsonObject.get("max_follower").toString()));
                settingThreads.setMax_like(Integer.parseInt(jsonObject.get("max_like").toString()));
                settingThreads.setMax_activity_24h(Integer.parseInt(jsonObject.get("max_activity_24h").toString()));
                settingThreads.setMax_day_activity(Integer.parseInt(jsonObject.get("max_day_activity").toString()));
                settingThreads.setUpdate_time(System.currentTimeMillis());
                settingThreadsRepository.save(settingThreads);
                JSONObject obj = new JSONObject();
                obj.put("id","threads");
                obj.put("platform","threads");
                obj.put("max_follower", settingThreads.getMax_follower());
                obj.put("max_like", settingThreads.getMax_like());
                obj.put("max_day_activity", settingThreads.getMax_day_activity());
                obj.put("max_activity_24h", settingThreads.getMax_activity_24h());
                obj.put("update_time", settingThreads.getUpdate_time());
                resp.put("setting_platform",obj);
            }

            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
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

            resp.put("status",false);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }

    }
    @PostMapping(path = "update_Task_Priority",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> update_Task_Priority(@RequestHeader(defaultValue = "") String Authorization,
                                                   @RequestBody TaskPriority taskPriorityBody
    ){
        JSONObject resp = new JSONObject();
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if(checktoken ==0){
            resp.put("status",false);
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            TaskPriority taskPriority=taskPriorityRepository.get_Priority_Task_By_Task(taskPriorityBody.getTask().trim());
            taskPriority.setPriority(taskPriorityBody.getPriority());
            taskPriority.setState(taskPriorityBody.getState());
            taskPriority.setUpdate_time(System.currentTimeMillis());
            taskPriorityRepository.save(taskPriority);
            JSONObject obj = new JSONObject();
            obj.put("task",taskPriority.getTask());
            obj.put("priority",taskPriority.getPriority());
            obj.put("state",taskPriority.getState());
            obj.put("platform",taskPriority.getPlatform());
            obj.put("update_time",taskPriority.getUpdate_time());
            resp.put("task_priority",obj);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
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

            resp.put("status",false);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }

    }

    @PostMapping(path = "update_Platform",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> update_Platform(@RequestHeader(defaultValue = "") String Authorization,
                                                @RequestBody Platform platformBody
    ){
        JSONObject resp = new JSONObject();
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if(checktoken ==0){
            resp.put("status",false);
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            Platform platform=platformRepository.get_Platform_By_Platform_And_Mode(platformBody.getPlatform().trim(),platformBody.getMode().trim());
            platform.setPriority(platformBody.getPriority());
            platform.setState(platformBody.getState());
            platform.setActivity(platformBody.getActivity());
            platform.setLogin_account(platformBody.getLogin_account());
            platform.setRegister_account(platformBody.getRegister_account());
            platform.setLogin_time(platformBody.getLogin_time());
            platform.setRegister_time(platformBody.getRegister_time());
            platform.setRegister_limit(platformBody.getRegister_limit());
            platform.setUpdate_time(System.currentTimeMillis());
            platformRepository.save(platform);
            JSONObject obj = new JSONObject();
            obj.put("mode",platform.getMode());
            obj.put("platform",platform.getPlatform());
            obj.put("priority",platform.getPriority());
            obj.put("state",platform.getState());
            obj.put("activity",platform.getActivity());
            obj.put("register_account",platform.getRegister_account());
            obj.put("login_account",platform.getLogin_account());
            obj.put("login_time",platform.getLogin_time());
            obj.put("register_time",platform.getRegister_time());
            obj.put("register_limit",platform.getRegister_limit());
            obj.put("update_time",platform.getUpdate_time());
            resp.put("platform",obj);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
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

            resp.put("status",false);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }

    }
}

package com.nts.awspremium.controller;

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
@RequestMapping(value = "/mode")
public class ModeController {
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
    private ModeRepository modeRepository;

    @Autowired
    private TikTokFollower24hRepository tikTokFollower24hRepository;

    @Autowired
    private ModeOptionRepository modeOptionRepository;


    @GetMapping(path = "get_List_Mode",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get_List_Mode(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if(checktoken ==0){
            resp.put("status",false);
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            JSONArray jsonArray =new JSONArray();
            List<Mode> modes=modeRepository.get_List_Mode();
            for(int i=0;i<modes.size();i++){
                JSONObject obj = new JSONObject();
                obj.put("mode",modes.get(i).getMode());
                obj.put("time_profile", modes.get(i).getTime_profile());
                obj.put("time_enable_profile", modes.get(i).getTime_enable_profile());
                obj.put("max_profile", modes.get(i).getMax_profile());
                jsonArray.add(obj);
            }

            resp.put("modes",jsonArray);
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


    @GetMapping(path = "get_Option_Mode",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get_Option_Mode(){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<String > platform=platformRepository.get_List_String_Platform();
        List<String > mode=modeRepository.get_List_String_Mode();
        String list_Platform="";
        String list_Mode="";

        for(int i=0;i<platform.size();i++){
            if(i==0){
                list_Platform=platform.get(0);
            }else{
                list_Platform=list_Platform+","+platform.get(i);
            }

        }
        resp.put("list_Platform",list_Platform);
        for(int i=0;i<mode.size();i++){
            if(i==0){
                list_Mode=mode.get(0);
            }else{
                list_Mode=list_Mode+","+mode.get(i);
            }

        }
        resp.put("list_Mode",list_Mode);
        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
    }

    @GetMapping(path = "get_List_Mode_Option",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get_List_Mode_Option(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if(checktoken ==0){
            resp.put("status",false);
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            JSONArray jsonArray =new JSONArray();
            List<ModeOption> modeOptions=modeOptionRepository.get_List_Mode_Option();
            for(int i=0;i<modeOptions.size();i++){
                JSONObject obj = new JSONObject();
                obj.put("id",modeOptions.get(i).getId());
                obj.put("mode", modeOptions.get(i).getMode());
                if(modeOptions.get(i).getMode().equals("auto")&&modeOptions.get(i).getTask().equals("follower")){
                    obj.put("count_24h",  tikTokFollower24hRepository.check_Follower_24h());
                }
                obj.put("platform", modeOptions.get(i).getPlatform());
                obj.put("task", modeOptions.get(i).getTask());
                obj.put("state", modeOptions.get(i).getState());
                obj.put("priority", modeOptions.get(i).getPriority());
                obj.put("max_task", modeOptions.get(i).getMax_task());
                obj.put("time_get_task", modeOptions.get(i).getTime_get_task());
                obj.put("time_waiting_task", modeOptions.get(i).getTime_waiting_task());
                obj.put("day_true_task", modeOptions.get(i).getDay_true_task());
                obj.put("update_time", modeOptions.get(i).getUpdate_time());
                jsonArray.add(obj);
            }

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

    @PostMapping(path = "update_Mode",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> update_Mode(@RequestHeader(defaultValue = "") String Authorization,
                                                 @RequestBody Mode mode
                                                 ){
        JSONObject resp = new JSONObject();
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if(checktoken ==0){
            resp.put("status",false);
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            Mode mode1=modeRepository.get_Mode_Info(mode.getMode());
            mode1.setMax_profile(mode.getMax_profile());
            mode1.setTime_profile(mode.getTime_profile());
            mode1.setTime_enable_profile(mode.getTime_enable_profile());
            mode1.setUpdate_time(System.currentTimeMillis());
            modeRepository.save(mode1);
            JSONObject obj = new JSONObject();
            obj.put("mode",mode1.getMode());
            obj.put("time_profile", mode1.getTime_profile());
            obj.put("time_enable_profile",mode1.getTime_enable_profile());
            obj.put("max_profile", mode1.getMax_profile());
            obj.put("max_account", mode1.getMax_account());
            obj.put("update_time", mode1.getUpdate_time());
            resp.put("mode",obj);
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


    @PostMapping(path = "update_Mode_Option",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> update_Mode_Option(@RequestHeader(defaultValue = "") String Authorization,
                                       @RequestBody ModeOption modeOption
    ){
        JSONObject resp = new JSONObject();
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if(checktoken ==0){
            resp.put("status",false);
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            ModeOption mode1=modeOptionRepository.get_Mode_Option(modeOption.getMode(),modeOption.getPlatform(),modeOption.getTask());
            mode1.setMax_task(modeOption.getMax_task());
            mode1.setPriority(modeOption.getPriority());
            mode1.setState(modeOption.getState());
            mode1.setTime_get_task(modeOption.getTime_get_task());
            mode1.setTime_waiting_task(modeOption.getTime_waiting_task());
            mode1.setDay_true_task(modeOption.getDay_true_task());
            mode1.setUpdate_time(System.currentTimeMillis());
            modeOptionRepository.save(mode1);
            JSONObject obj = new JSONObject();
            obj.put("id",mode1.getId());
            obj.put("mode",mode1.getMode());
            obj.put("platform", mode1.getPlatform());
            obj.put("task",mode1.getTask());
            obj.put("max_task",mode1.getMax_task());
            obj.put("state",mode1.getState());
            obj.put("priority",mode1.getPriority());
            obj.put("time_waiting_task",mode1.getTime_waiting_task());
            obj.put("day_true_task",mode1.getDay_true_task());
            obj.put("time_get_task", mode1.getTime_get_task());
            obj.put("update_time", mode1.getUpdate_time());
            resp.put("setting_platform",obj);
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
            Platform platform=platformRepository.get_Platform_By_PlatformId(platformBody.getPlatform().trim());
            platform.setPriority(platformBody.getPriority());
            platform.setState(platformBody.getState());
            platform.setActivity(platformBody.getActivity());
            platform.setUpdate_time(System.currentTimeMillis());
            platformRepository.save(platform);
            JSONObject obj = new JSONObject();
            obj.put("platform",platform.getPlatform());
            obj.put("priority",platform.getPriority());
            obj.put("state",platform.getState());
            obj.put("activity",platform.getActivity());
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

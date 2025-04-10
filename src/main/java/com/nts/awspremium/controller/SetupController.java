package com.nts.awspremium.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.nts.awspremium.GoogleApi;
import com.nts.awspremium.Openai;
import com.nts.awspremium.StringUtils;
import com.nts.awspremium.TikTokApi;
import com.nts.awspremium.model.*;
import com.nts.awspremium.repositories.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.Proxy;
import java.util.concurrent.TimeUnit;


import java.io.StringReader;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/setup")
public class SetupController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private YoutubeViewHistoryRepository youtubeViewHistoryRepository;
    @Autowired
    private AccountController accountController;
    @Autowired
    private ProfileTaskRepository profileTaskRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;

    @Autowired
    private OrderRunningRepository orderRunningRepository;

    @Autowired
    private AccountNameRepository accountNameRepository;

    @Autowired
    private OpenAiKeyRepository openAiKeyRepository;
    @GetMapping(value = "updateSystem", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> updateSystem(@RequestHeader(defaultValue = "") String Authorization,
                                                            @RequestParam(defaultValue = "") String device_id,
                                                            @RequestParam(defaultValue = "") String profile_id,
                                                            @RequestParam(defaultValue = "") String task,
                                                            @RequestParam(defaultValue = "") String task_key
    ) throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
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
            if (task.length() ==0) {
                resp.put("status", false);
                data.put("message", "task không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            ProfileTask profileTask=null;
            profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
            if(profileTask==null){
                resp.put("status", false);
                data.put("message", "profile_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if(task.trim().equals("update_pi")){
                profileTask.setUpdate_pi(0);
                profileTask.setUpdate_pi_time(System.currentTimeMillis());
                profileTaskRepository.save(profileTask);
            }else if(task.trim().equals("clear_data")){
                profileTask.setClear_data(0);
                profileTask.setClear_data_time(System.currentTimeMillis());
                profileTaskRepository.save(profileTask);
            }
            resp.put("status",true);
            data.put("message", "update thành công");
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
/*
    @GetMapping(value = "/check_task", produces = "application/json;charset=utf8")
    ResponseEntity<Map<String, Object>> check_task(@RequestParam(defaultValue = "") String device_id,@RequestHeader(defaultValue = "") String Authorization) {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if (checktoken >0) {
            resp.put("status", false);
            data.put("message", "Token expired");
            resp.put("data", data);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
        try {
            if (deviceRepository.find_Device(device_id.trim())==0) {
                resp.put("status", false);
                data.put("message", "device_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            if(profileRepository.count_Profile_By_DeviceId(device_id.trim())>2){
                AccountTask accountTask=accountTaskRepository.find_AccountTask_Changer_Profile(device_id.trim());
                if(accountTask!=null){
                    resp.put("status", true);
                    data.put("task", "changer_profile");
                    data.put("barcode",profileRepository.check_Barcode_By_ProfileId(accountTask.getProfile().getProfile_id()));
                    data.put("profile_id",accountTask.getProfile().getProfile_id());
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else{
                    resp.put("status", false);
                    data.put("message", "không có nhiệm vụ");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }else{
                Profile profile=new Profile();
                profile.setProfile_id(device_id.trim()+"_P"+(profileRepository.count_Profile_By_DeviceId(device_id.trim())+1));
                profile.setImei("1");
                profile.setBarcode("1");
                profile.setBt_addr("1");
                profile.setWifi_addr("1");
                profile.setUpdate_time(System.currentTimeMillis());
                profile.setAdd_time(System.currentTimeMillis());
                profile.setState(-1);
                profile.setDevice(deviceRepository.check_DeviceId(device_id));
                profileRepository.save(profile);

                ////////////////////////////////////////

                resp.put("status", true);
                data.put("task", "create_profile");
                data.put("barcode","1");
                data.put("profile_id", device_id.trim()+"_P"+(profileRepository.count_Profile_By_DeviceId(device_id.trim())+1));
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
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

    @GetMapping(value = "/check_changer_info", produces = "application/json;charset=utf8")
    ResponseEntity<Map<String, Object>> check_changer_info(@RequestParam(defaultValue = "") String computer,
                                                           @RequestHeader(defaultValue = "") String Authorization) {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if (checktoken ==0) {
            resp.put("status", false);
            data.put("message", "Token expired");
            resp.put("data", data);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
        try {
            AccountTask accountTask=accountTaskRepository.find_AccountTask_Changer_Profile();
            if(accountTask!=null){
                Profile profile= profileRepository.find_Profile_Changer_Profile(accountTask.getProfile().getProfile_id());
                resp.put("status", true);
                data.put("profile_id",profile.getProfile_id());

                data.put("imei",profile.getImei());
                data.put("bt_addr",profile.getBt_addr());
                data.put("wifi_addr",profile.getWifi_addr());
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }else{
                Profile profile=profileRepository.find_Profile_Changer_Profile();
                if(profile!=null){
                    resp.put("status", true);
                    data.put("profile_id",profile.getProfile_id());
                    data.put("barcode",profile.getBarcode());
                    data.put("imei",profile.getImei());
                    data.put("bt_addr",profile.getBt_addr());
                    data.put("wifi_addr",profile.getWifi_addr());
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else{
                    resp.put("status", false);
                    data.put("message", "không có nhiệm vụ");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
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

    @GetMapping(value = "/update_task", produces = "application/json;charset=utf8")
    ResponseEntity<Map<String, Object>> update_task(@RequestParam(defaultValue = "") String task,
                                                    @RequestParam(defaultValue = "") String profile_id,
                                                    @RequestParam(defaultValue = "false") Boolean status,
                                                    @RequestParam(defaultValue = "") String device_id,@RequestHeader(defaultValue = "") String Authorization) {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if (checktoken ==0) {
            resp.put("status", false);
            data.put("message", "Token expired");
            resp.put("data", data);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
        try {
            if (deviceRepository.find_Device(device_id.trim())==0) {
                resp.put("status", false);
                data.put("message", "device_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            if(profileRepository.count_Profile_By_DeviceId(device_id.trim())>2){
                resp.put("status", true);
                data.put("task", "create_profile");
                data.put("profile_id", device_id.trim()+"_P"+(profileRepository.count_Profile_By_DeviceId(device_id.trim())+1));
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }else{
                resp.put("status", true);
                data.put("task", "create_profile");
                data.put("profile_id", device_id.trim()+"_P"+(profileRepository.count_Profile_By_DeviceId(device_id.trim())+1));
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
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

    @GetMapping(value = "/test", produces = "application/json;charset=utf8")
    ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            String stringList="f";
            resp.put("status", true);
            data.put("task", "create_profile");
            data.put("profile_id", stringList);
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


 */


    @GetMapping(value = "/subscriberCount", produces = "application/json;charset=utf8")
    ResponseEntity<Map<String, Object>> subscriberCount(@RequestParam(defaultValue = "") String link) {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            if (link.length() ==0) {
                resp.put("status", false);
                data.put("error", "link is null");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            String channelId = GoogleApi.getChannelId(link);
            if (channelId == null) {
                resp.put("status", false);
                data.put("error", "Cant filter channel from link");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            String title=channelId.split(",")[0];
            String uId=channelId.split(",")[1];
            int start_Count =GoogleApi.getCountSubcriberCurrent(uId);
            if(start_Count==-2){
                resp.put("status", false);
                data.put("error", "Can't get subcriberCurrent");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }else{
                resp.put("status", true);
                data.put("name",title);
                data.put("uid",uId);
                data.put("subscriberCount",start_Count);
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
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
            //System.out.println(logError.getMessage());


            resp.put("status", false);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/likeCount", produces = "application/json;charset=utf8")
    ResponseEntity<Map<String, Object>> likeCount(@RequestParam(defaultValue = "") String video_id) {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            if (video_id.length() ==0) {
                resp.put("status", false);
                data.put("error", "video_id is null");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }

            int start_Count =GoogleApi.getCountLikeCurrent(video_id.trim());
            if(start_Count==-2){
                resp.put("status", false);
                data.put("error", "Can't get likeCurrent");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }else{
                resp.put("status", true);
                data.put("video_id",video_id);
                data.put("likeCount",start_Count);
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
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
            //System.out.println(logError.getMessage());


            resp.put("status", false);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(value = "/viewCount", produces = "application/json;charset=utf8")
    ResponseEntity<Map<String, Object>> viewCount(@RequestParam(defaultValue = "") String video_id) {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            if (video_id.length() ==0) {
                resp.put("status", false);
                data.put("error", "video_id is null");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }

            long start_Count =GoogleApi.getCountViewCurrent(video_id.trim());
            if(start_Count==-2){
                resp.put("status", false);
                data.put("error", "Can't get viewCurrent");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }else{
                resp.put("status", true);
                data.put("video_id",video_id);
                data.put("viewCount",start_Count);
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
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
            //System.out.println(logError.getMessage());


            resp.put("status", false);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/ping", produces = "application/json;charset=utf8")
    ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            accountController.updateAccountClone();
                resp.put("status", true );
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
            //System.out.println(logError.getMessage());


            resp.put("status", false);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/x", produces = "application/json;charset=utf8")
    ResponseEntity<Map<String, Object>> x() {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            String a =GoogleApi.x("https://x.com/XDevelopers");
            resp.put("status", true);
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
            //System.out.println(logError.getMessage());


            resp.put("status", false);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/getCheckCountoff", produces = "application/json;charset=utf8")
    ResponseEntity<Map<String, Object>> getCheckCountoff() {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            Random random=new Random();
            OrderRunningShow orderRunning=orderRunningRepository.find_Order_By_Start_Count0("tiktok");
            if(orderRunning!=null){
                Thread.sleep(500+random.nextInt(500));
                if(orderRunningRepository.check_Check_Count(orderRunning.getOrder_id())>0){
                    resp.put("status", false);
                }
                orderRunningRepository.update_Check_Count(System.currentTimeMillis(),orderRunning.getOrder_id());
                resp.put("status", true);
                resp.put("order_id", orderRunning.getOrder_id());
                if(orderRunning.getPlatform().equals("tiktok")){
                    if(orderRunning.getTask().equals("follower")){
                        resp.put("link", "https://countik.com/user/"+orderRunning.getOrder_key());
                        resp.put("xpath", "(//h5[@class='count'])[1]");
                    }else if(orderRunning.getTask().equals("like")){
                        resp.put("link", "https://countik.com/video/"+orderRunning.getOrder_key());
                        resp.put("xpath", "(//h5[@class='count'])[2]");
                    }else if(orderRunning.getTask().equals("view")){
                        resp.put("link", "https://countik.com/video/"+orderRunning.getOrder_key());
                        resp.put("xpath", "(//h5[@class='count'])[1]");
                    }else if(orderRunning.getTask().equals("comment")){
                        resp.put("link", "https://countik.com/video/"+orderRunning.getOrder_key());
                        resp.put("xpath", "(//h5[@class='count'])[4]");
                    }
                }
            }else{
                OrderRunningShow orderRunning1=orderRunningRepository.find_Order_By_Curent0("tiktok");
                if(orderRunning1!=null){
                    Thread.sleep(200+random.nextInt(500));
                    if(orderRunningRepository.check_Check_Count(orderRunning1.getOrder_id())>0){
                        resp.put("status", false);
                    }
                    orderRunningRepository.update_Check_Count(System.currentTimeMillis(),orderRunning1.getOrder_id());
                    resp.put("status", true);
                    resp.put("order_id", orderRunning1.getOrder_id());
                    if(orderRunning1.getPlatform().equals("tiktok")){
                        if(orderRunning1.getTask().equals("follower")){
                            resp.put("link", "https://countik.com/user/"+orderRunning1.getOrder_key());
                            resp.put("xpath", "(//h5[@class='count'])[1]");
                        }else if(orderRunning1.getTask().equals("like")){
                            resp.put("link", "https://countik.com/video/"+orderRunning1.getOrder_key());
                            resp.put("xpath", "(//h5[@class='count'])[2]");
                        }else if(orderRunning1.getTask().equals("view")){
                            resp.put("link", "https://countik.com/video/"+orderRunning1.getOrder_key());
                            resp.put("xpath", "(//h5[@class='count'])[1]");
                        }else if(orderRunning1.getTask().equals("comment")){
                            resp.put("link", "https://countik.com/video/"+orderRunning1.getOrder_key());
                            resp.put("xpath", "(//h5[@class='count'])[4]");
                        }
                    }
                }else{
                    resp.put("status", false);
                }
            }
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
            //System.out.println(logError.getMessage());


            resp.put("status", false);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/getCheckCount", produces = "application/json;charset=utf8")
    ResponseEntity<Map<String, Object>> getCheckCount() {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            Random random=new Random();
            OrderRunningShow orderRunning=orderRunningRepository.find_Order_By_Start_Count0("tiktok");
            if(orderRunning!=null){
                Thread.sleep(500+random.nextInt(500));
                if(orderRunningRepository.check_Check_Count(orderRunning.getOrder_id())>0){
                    resp.put("status", false);
                }
                orderRunningRepository.update_Check_Count(System.currentTimeMillis(),orderRunning.getOrder_id());
                resp.put("status", true);
                resp.put("order_id", orderRunning.getOrder_id());
                if(orderRunning.getPlatform().equals("tiktok")){
                    if(orderRunning.getTask().equals("follower")){
                        resp.put("link", "https://livecounts.io/tiktok-live-follower-counter/"+orderRunning.getOrder_key());
                        resp.put("xpath", "(//div[contains(@class,'odometer odometer-auto-theme')])[1]//span[@class='odometer-value']");
                    }else if(orderRunning.getTask().equals("like")){
                        resp.put("link", "https://livecounts.io/tiktok-live-view-counter/"+orderRunning.getOrder_key());
                        resp.put("xpath", "(//div[contains(@class,'odometer odometer-auto-theme')])[2]//span[@class='odometer-value']");
                    }else if(orderRunning.getTask().equals("view")){
                        resp.put("link", "https://livecounts.io/tiktok-live-view-counter/"+orderRunning.getOrder_key());
                        resp.put("xpath", "(//div[contains(@class,'odometer odometer-auto-theme')])[1]//span[@class='odometer-value']");
                    }else if(orderRunning.getTask().equals("comment")){
                        resp.put("link", "https://livecounts.io/tiktok-live-view-counter/"+orderRunning.getOrder_key());
                        resp.put("xpath", "(//div[contains(@class,'odometer odometer-auto-theme')])[3]//span[@class='odometer-value']");
                    }
                }
            }else{
                OrderRunningShow orderRunning1=orderRunningRepository.find_Order_By_Curent0("tiktok");
                if(orderRunning1!=null){
                    Thread.sleep(200+random.nextInt(500));
                    if(orderRunningRepository.check_Check_Count(orderRunning1.getOrder_id())>0){
                        resp.put("status", false);
                    }
                    orderRunningRepository.update_Check_Count(System.currentTimeMillis(),orderRunning1.getOrder_id());
                    resp.put("status", true);
                    resp.put("order_id", orderRunning1.getOrder_id());
                    if(orderRunning1.getPlatform().equals("tiktok")){
                        if(orderRunning1.getTask().equals("follower")){
                            resp.put("link", "https://livecounts.io/tiktok-live-follower-counter/"+orderRunning1.getOrder_key());
                            resp.put("xpath", "(//div[contains(@class,'odometer odometer-auto-theme')])[1]//span[@class='odometer-value']");
                        }else if(orderRunning1.getTask().equals("like")){
                            resp.put("link", "https://livecounts.io/tiktok-live-view-counter/"+orderRunning1.getOrder_key());
                            resp.put("xpath", "(//div[contains(@class,'odometer odometer-auto-theme')])[2]//span[@class='odometer-value']");
                        }else if(orderRunning1.getTask().equals("view")){
                            resp.put("link", "https://livecounts.io/tiktok-live-view-counter/"+orderRunning1.getOrder_key());
                            resp.put("xpath", "(//div[contains(@class,'odometer odometer-auto-theme')])[1]//span[@class='odometer-value']");
                        }else if(orderRunning1.getTask().equals("comment")){
                            resp.put("link", "https://livecounts.io/tiktok-live-view-counter/"+orderRunning1.getOrder_key());
                            resp.put("xpath", "(//div[contains(@class,'odometer odometer-auto-theme')])[3]//span[@class='odometer-value']");
                        }
                    }
                }else{
                    resp.put("status", false);
                }
            }
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
            //System.out.println(logError.getMessage());


            resp.put("status", false);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/updateCheckCount", produces = "application/json;charset=utf8")
    ResponseEntity<Map<String, Object>> updateCheckCount(@RequestParam(defaultValue = "0") Long order_id,
                                              @RequestParam(defaultValue = "0") Integer count,
                                              @RequestParam(defaultValue = "false") Boolean status) {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            OrderRunning orderRunning=orderRunningRepository.get_Order_By_Id(order_id);
            if(orderRunning!=null&&status==true){
                if(orderRunning.getStart_count_time()>0){
                    orderRunning.setCurrent_count(count);
                    orderRunning.setUpdate_current_time(System.currentTimeMillis());
                    orderRunning.setCheck_count(0);
                }else{
                    orderRunning.setStart_count(count);
                    orderRunning.setStart_count_time(System.currentTimeMillis());
                    orderRunning.setCheck_count(0);
                }
                orderRunningRepository.save(orderRunning);
                resp.put("status", true);
            }else if(orderRunning!=null){
                orderRunning.setCheck_count(0);
                orderRunningRepository.save(orderRunning);
                resp.put("status", false);
            }else{
                resp.put("status", false);
            }
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
            //System.out.println(logError.getMessage());


            resp.put("status", false);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(value = "/reset_HistoryView", produces = "application/json;charset=utf8")
    ResponseEntity<Map<String, Object>> reset_HistoryView() {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            youtubeViewHistoryRepository.reset_HistoryView();
            resp.put("data", true);
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


}

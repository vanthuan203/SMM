package com.nts.awspremium.controller;

import com.nts.awspremium.TikTokApi;
import com.nts.awspremium.model.*;
import com.nts.awspremium.repositories.*;
import com.nts.awspremium.MailApi;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private DeviceRepository deviceRepository;


    @Autowired
    private LogErrorRepository logErrorRepository;
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

    @GetMapping(value = "/test2", produces = "application/json;charset=utf8")
    ResponseEntity<Map<String, Object>> test2(@RequestParam String link) {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            JSONObject stringList= TikTokApi.getInfoVideoTikTok(link,2);
            resp.put("status", true);
            data.put("task", link);
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

}

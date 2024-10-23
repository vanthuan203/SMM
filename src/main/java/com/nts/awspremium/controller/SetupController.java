package com.nts.awspremium.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
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


 */


    @GetMapping(value = "/test3", produces = "application/json;charset=utf8")
    ResponseEntity<Map<String, Object>> test3() {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            String driverPath = "drivers/chromedriver.exe";
            System.setProperty("webdriver.chrome.driver", driverPath);

            // Thiết lập tùy chọn Chrome
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--remote-allow-origins=*");
            // Khởi tạo ChromeDriver với tùy chọn
            WebDriver driver = new ChromeDriver(options);
            driver.get("https://livecounts.io/embed/youtube-live-view-counter/H4cg1OymzYw");
            Thread.sleep(6000);
            for(int i=0;i<5;i++){
                try{
                    String count="";
                    List<WebElement> elements =  driver.findElements(By.xpath("(//div[contains(@class,'odometer odometer-auto-theme')])[1]//span[@class='odometer-value']"));
                   for(int j=0;j<elements.size();j++){
                       count=count+elements.get(j).getText();
                   }
                   if(count.length()>0){
                       System.out.println(count);
                       break;
                   }
                }catch (Exception e){
                    Thread.sleep(100);
                }

            }

            driver.quit();
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
            System.out.println(logError.getMessage());


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

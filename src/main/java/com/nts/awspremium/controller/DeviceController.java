package com.nts.awspremium.controller;

import com.nts.awspremium.model.*;
import com.nts.awspremium.repositories.DeviceRepository;
import com.nts.awspremium.repositories.LogErrorRepository;
import com.nts.awspremium.repositories.UserRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/device")
public class DeviceController {
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    @GetMapping(value = "get_List_Device", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> test(@RequestHeader(defaultValue = "") String Authorization,
                                                    @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
                                                    @RequestParam(name = "size", required = false, defaultValue = "5") Integer size,
                                                    @RequestParam(name = "sort_type", required = false, defaultValue = "add_time") String sort_type,
                                                    @RequestParam(name = "key", required = false, defaultValue = "") String key,
                                                    @RequestParam(name = "sort", required = false, defaultValue = "DESC") String sort) throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
            if(checktoken ==0){
                resp.put("status",false);
                data.put("message", "Token expired");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            Sort sortable = null;
            if (sort.equals("ASC")) {
                sortable = Sort.by(sort_type).ascending();
            }
            if (sort.equals("DESC")) {
                sortable = Sort.by(sort_type).descending();
            }
            Pageable pageable = PageRequest.of(page, size, sortable);
            Page<DeviceShow> devicePage;
            if(key.length()>0){
                devicePage=deviceRepository.get_List_Device(pageable,key.trim());
            }else{
                devicePage=deviceRepository.get_List_Device(pageable);
            }
            List<DeviceShow> deviceList=devicePage.getContent();
            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < deviceList.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("device_id", deviceList.get(i).getDevice_id());
                obj.put("add_time", deviceList.get(i).getAdd_time());
                obj.put("running", deviceList.get(i).getRunning());
                obj.put("update_time", deviceList.get(i).getUpdate_time());
                obj.put("get_time", deviceList.get(i).getGet_time());
                obj.put("num_profile", deviceList.get(i).getNum_profile());
                obj.put("num_account", deviceList.get(i).getNum_account());
                obj.put("profile_id", deviceList.get(i).getProfile_id());
                obj.put("platform", deviceList.get(i).getPlatform());
                obj.put("task", deviceList.get(i).getTask());
                obj.put("state", deviceList.get(i).getState());
                jsonArray.add(obj);
            }
            resp.put("device", jsonArray);
            resp.put("page",devicePage.getTotalPages());
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

    @DeleteMapping(value = "delete_Device", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> delete_Order_Running(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String device_id) throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if(checktoken ==0){
            resp.put("status",false);
            data.put("message", "Token expired");
            resp.put("data",data);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
        try{
            String[] device_Arr = device_id.split(",");
            for (int i=0;i<device_Arr.length;i++) {
                deviceRepository.delete_Device_By_DeviceId(device_Arr[i].trim());
            }
            resp.put("device", "");
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

package com.nts.awspremium.controller;

import com.nts.awspremium.GoogleApi;
import com.nts.awspremium.TikTokApi;
import com.nts.awspremium.model.*;
import com.nts.awspremium.controller.*;
import com.nts.awspremium.repositories.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nts.awspremium.controller.GoogleKeyController.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/setup")
public class SetupController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ComputerRepository computerRepository;
    @Autowired
    private BoxRepository boxRepository;
    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private AccountTaskRepository accountTaskRepository;
    @PostMapping(value = "/check_device", produces = "application/json;charset=utf8")
    ResponseEntity<Map<String, Object>> add_device(@RequestBody JSONObject device_info,@RequestHeader(defaultValue = "") String Authorization) {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        Integer checktoken = userRepository.FindUserByToken(Authorization);
        if (checktoken >0) {
            resp.put("status", false);
            data.put("message", "Token expired");
            resp.put("data", data);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
        try {
            Computer computer=computerRepository.check_ComputerId(device_info.get("computer_id").toString().trim());
            if(computer==null){
                Computer computer_new=new Computer();
                computer_new.setComputer_id(device_info.get("computer_id").toString().trim());
                computer_new.setState(1);
                computer_new.setUpdate_time(System.currentTimeMillis());
                computer_new.setAdd_time(System.currentTimeMillis());
                computerRepository.save(computer_new);
                computer=computer_new;
            }
            Box box =boxRepository.check_BoxId(device_info.get("box_id").toString().trim());
            if(box==null){
                Box box_new=new Box();
                box_new.setBox_id(device_info.get("box_id").toString().trim());
                box_new.setState(1);
                box_new.setComputer(computer);
                box_new.setUpdate_time(System.currentTimeMillis());
                box_new.setAdd_time(System.currentTimeMillis());
                boxRepository.save(box_new);
                box=box_new;
            }
            Device device_check=deviceRepository.check_DeviceId(device_info.get("device_id").toString());
            if (device_check!=null) {
                device_check.setUpdate_time(System.currentTimeMillis());
                device_check.setBox(box);
                device_check.setState(1);
                deviceRepository.save(device_check);
                resp.put("status", true);
                data.put("message", "Sẵn sàng sử dụng");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            } else {
                Device device_new =new Device();
                device_new.setDevice_id(device_info.get("device_id").toString());
                device_new.setBox(box);
                device_new.setState(1);
                device_new.setUpdate_time(System.currentTimeMillis());
                device_new.setAdd_time(System.currentTimeMillis());
                deviceRepository.save(device_new);
                resp.put("status", true);
                data.put("message", "Sẵn sàng sử dụng");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
        } catch (Exception e) {
            resp.put("status", "fail");
            data.put("message", e.getMessage());
            resp.put("data", data);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value = "/check_task", produces = "application/json;charset=utf8")
    ResponseEntity<Map<String, Object>> check_task(@RequestParam(defaultValue = "") String device_id,@RequestHeader(defaultValue = "") String Authorization) {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        Integer checktoken = userRepository.FindUserByToken(Authorization);
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
            System.out.println(stackTraceElement.getMethodName());
            System.out.println(stackTraceElement.getLineNumber());
            System.out.println(stackTraceElement.getClassName());
            System.out.println(stackTraceElement.getFileName());
            System.out.println("Error : " + e.getMessage());
            resp.put("status", "fail");
            data.put("message", e.getMessage());
            resp.put("data", data);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/check_changer_info", produces = "application/json;charset=utf8")
    ResponseEntity<Map<String, Object>> check_changer_info(@RequestParam(defaultValue = "") String computer,
                                                           @RequestHeader(defaultValue = "") String Authorization) {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        Integer checktoken = userRepository.FindUserByToken(Authorization);
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
            System.out.println(stackTraceElement.getMethodName());
            System.out.println(stackTraceElement.getLineNumber());
            System.out.println(stackTraceElement.getClassName());
            System.out.println(stackTraceElement.getFileName());
            System.out.println("Error : " + e.getMessage());
            resp.put("status", "fail");
            data.put("message", e.getMessage());
            resp.put("data", data);
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
        Integer checktoken = userRepository.FindUserByToken(Authorization);
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
            resp.put("status", "fail");
            data.put("message", e.getMessage());
            resp.put("data", data);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
    }


}

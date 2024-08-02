package com.nts.awspremium.controller;

import com.nts.awspremium.model.Device;
import com.nts.awspremium.model.LogError;
import com.nts.awspremium.model.ProfileShow;
import com.nts.awspremium.model.ProfileTask;
import com.nts.awspremium.repositories.*;
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
@RequestMapping(value = "/profile")
public class ProfileController {
    @Autowired
    private ProfileTaskRepository profileTaskRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    @Autowired
    private UserRepository userRepository;
    @GetMapping(value = "get_List_Profile", produces = "application/hal+json;charset=utf8")
    private ResponseEntity<Map<String, Object>> get_List_Profile(@RequestHeader(defaultValue = "") String Authorization,
                                                    @RequestParam(name = "device_id", required = false, defaultValue = "") String device_id
                                                   ) throws InterruptedException {
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
            List<ProfileShow> profiles =profileTaskRepository.get_Profile_By_DeviceId(device_id.toString());

            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < profiles.size(); i++) {
                JSONObject obj = new JSONObject();
                Integer num_account=0;
                Integer num_account_die=0;
                String acc_live=profileTaskRepository.get_AccountLive_By_ProfileId(profiles.get(i).getProfile_id());
                String acc_die=profileTaskRepository.get_AccountDie_By_ProfileId(profiles.get(i).getProfile_id());
                if(acc_live!=null){
                    num_account=acc_live.split(",").length;
                }
                if(acc_die!=null){
                    String[] account = acc_die.split(",");
                    Set<String> uniqueAccount = new LinkedHashSet<>(Arrays.asList(account));
                    acc_die = String.join(",", uniqueAccount);
                    num_account_die=acc_die.split(",").length;
                }
                obj.put("device_id", profiles.get(i).getDevice_id());
                obj.put("profile_id", profiles.get(i).getProfile_id());
                obj.put("add_time", profiles.get(i).getAdd_time());
                obj.put("update_time", profiles.get(i).getUpdate_time());
                obj.put("enabled_time", profiles.get(i).getEnabled_time());
                obj.put("enabled", profiles.get(i).getEnabled());
                obj.put("get_time", profiles.get(i).getGet_time());
                obj.put("num_account", num_account);
                obj.put("num_account_die", num_account_die);
                obj.put("acc_live",acc_live );
                obj.put("acc_die",acc_die );
                obj.put("state", profiles.get(i).getState());
                obj.put("platform", profiles.get(i).getPlatform());
                obj.put("task", profiles.get(i).getTask());
                obj.put("state", profiles.get(i).getState());
                obj.put("running", profiles.get(i).getRunning());
                jsonArray.add(obj);
            }
            resp.put("profiles", jsonArray);
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

    public Boolean update_Enabled_Profile(
    ){
        try{
            List<Device> devices =deviceRepository.get_All_Device_Enable0();
            for (Device device:devices) {
                ProfileTask profileTask =profileTaskRepository.get_Profile_Rand_Enable0(device.getDevice_id().trim());
                if (profileTask !=null){
                    profileTask.setEnabled(1);
                    profileTask.setEnabled_time(System.currentTimeMillis());
                    profileTaskRepository.save(profileTask);
                }
            }
            return  true;
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

            return false;
        }

    }


}

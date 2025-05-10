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
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/profile")
public class ProfileController {
    @Autowired
    private ProfileTaskRepository profileTaskRepository;
    @Autowired
    private AccountProfileRepository accountProfileRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SettingTikTokRepository settingTikTokRepository;
    @Autowired
    private TaskSumRepository taskSumRepository;

    public String formatStatus(String raw) {
        if (raw == null || raw.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();

        Arrays.stream(raw.split("\\?"))
                .filter(s -> !s.isEmpty())
                .forEach(entry -> {
                    String[] parts = entry.split("=>");
                    if (parts.length != 2) return;

                    if (parts[0].equals("1")) {
                        // status = true → hiển thị cả success và failure
                        String[] values = parts[1].split(",");
                        String success = values[0].split(":")[1];
                        String fail = values[1].split(":")[1];
                        sb.append(String.format("Success[%s],Failure[%s]", success, fail));
                    } else {
                        // status = false → chỉ đếm tổng
                        int total = Arrays.stream(parts[1].split(","))
                                .mapToInt(s -> Integer.parseInt(s.split(":")[1]))
                                .sum();
                        sb.append(String.format("False[%d]", total));
                    }

                    sb.append("|");
                });

        return sb.toString();
    }
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
            List<ProfileTask> profiles =profileTaskRepository.get_Profile_By_DeviceId(device_id.toString());
            SettingTiktok settingTiktok=settingTikTokRepository.get_Setting();
            JSONArray jsonArray = new JSONArray();
            List<Object[]> acc_Live=profileTaskRepository.get_AccountLive_GroupBy_ProfileId_By_DeviceId(device_id.trim());
            List<Object[]> acc_Die=profileTaskRepository.get_AccountDie_GroupBy_ProfileId_By_DeviceId(device_id.trim());
            List<Object[]> task_List=taskSumRepository.task_Sum_By_DeviceId_GroupBy_ProfileId(device_id.trim());

            Map<String, String> profileLiveMap = new HashMap<>();
            for (Object[] row : acc_Live) {
                String profileId = (String) row[0];
                String platforms = (String) row[1];
                profileLiveMap.put(profileId, platforms);
            }
            Map<String, String> profileDieMap = new HashMap<>();
            for (Object[] row : acc_Die) {
                String profileId = (String) row[0];
                String platforms = (String) row[1];
                profileDieMap.put(profileId, platforms);
            }
            Map<String, String> profileTaskMap = new HashMap<>();
            for (Object[] row : task_List) {
                String profileId = (String) row[0];
                String platforms = (String) row[1];
                profileTaskMap.put(profileId, platforms);
            }

            for (int i = 0; i < profiles.size(); i++) {
                JSONObject obj = new JSONObject();
                Integer num_account=0;
                Integer num_account_die=0;
                String acc_live=profileLiveMap.get(profiles.get(i).getProfile_id());
                String acc_die=profileDieMap.get(profiles.get(i).getProfile_id());
                if(acc_live!=null){
                    num_account=acc_live.split(",").length;
                    String[] account = acc_live.split(",");
                    Set<String> uniqueAccount = new LinkedHashSet<>(Arrays.asList(account));
                    acc_live = String.join(",", uniqueAccount);
                }
                if(acc_die!=null){
                    num_account_die=acc_die.split(",").length;
                    String[] account = acc_die.split(",");
                    Set<String> uniqueAccount = new LinkedHashSet<>(Arrays.asList(account));
                    acc_die = String.join(",", uniqueAccount);
                }

                if(profiles.get(i).getTiktok_lite_version()>settingTiktok.getMax_version()) {
                    obj.put("code_tiktok_version",false);
                }else{
                    obj.put("code_tiktok_version",true);
                }

                obj.put("device_id", profiles.get(i).getDevice().getDevice_id());
                obj.put("profile_id", profiles.get(i).getProfile_id());
                obj.put("add_time", profiles.get(i).getAdd_time());
                obj.put("update_time", profiles.get(i).getUpdate_time());
                obj.put("enabled_time", profiles.get(i).getEnabled_time());
                obj.put("enabled", profiles.get(i).getEnabled());
                obj.put("get_time", profiles.get(i).getGet_time());
                obj.put("num_account", num_account);
                obj.put("num_account_die", num_account_die);
                obj.put("acc_live",acc_live==null?"":acc_live );
                obj.put("acc_die",acc_die==null?"":acc_die );
                obj.put("state", profiles.get(i).getState());
                obj.put("platform", profiles.get(i).getPlatform());
                obj.put("tiktok_lite_version", profiles.get(i).getTiktok_lite_version());
                obj.put("task", profiles.get(i).getTask());
                obj.put("state", profiles.get(i).getState());
                obj.put("running", profiles.get(i).getRunning());
                obj.put("note",formatStatus(profileTaskMap.get(profiles.get(i).getProfile_id())==null?"":profileTaskMap.get(profiles.get(i).getProfile_id())));
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

    @GetMapping(value = "update_Account_Profile", produces = "application/hal+json;charset=utf8")
    private ResponseEntity<Map<String, Object>> update_Account_Profile(@RequestHeader(defaultValue = "") String Authorization,
                                                                 @RequestParam(name = "device_id", required = false, defaultValue = "") String device_id,
                                                                 @RequestParam(name = "profile_id", required = false, defaultValue = "") String profile_id,
                                                                 @RequestParam(name = "live", required = false, defaultValue = "-1") Integer live
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
            if(live ==-1){
                resp.put("status",false);
                data.put("message", "live không để trống");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            AccountProfile accountProfile =accountProfileRepository.get_Account_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube");
            if(accountProfile!=null){
                accountProfile.setLive(live);
                accountProfile.setUpdate_time(System.currentTimeMillis());
                accountProfileRepository.save(accountProfile);
            }

            resp.put("status", true);
            data.put("message", "Update thành công!");
            resp.put("data", data);
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
                ProfileTask profileTask =profileTaskRepository.get_Profile_Rand_Enable0(device.getDevice_id().trim(),device.getMode().trim());
                if (profileTask !=null&&profileTaskRepository.check_Profile_Enabled_And_GoogleLogin(device.getDevice_id().trim())==0){ //&&profileTaskRepository.check_Profile_Enabled_And_GoogleLogin(device.getDevice_id().trim())==0
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

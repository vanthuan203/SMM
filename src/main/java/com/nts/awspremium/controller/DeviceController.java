package com.nts.awspremium.controller;

import com.nts.awspremium.model.*;
import com.nts.awspremium.repositories.*;
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

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
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
    private ProfileTaskRepository profileTaskRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private HttpServletRequest request;
    @GetMapping(value = "get_List_Device", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> get_List_Device(@RequestHeader(defaultValue = "") String Authorization,
                                                    @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
                                                    @RequestParam(name = "size", required = false, defaultValue = "5") Integer size,
                                                    @RequestParam(name = "sort_type", required = false, defaultValue = "add_time") String sort_type,
                                                    @RequestParam(name = "key", required = false, defaultValue = "") String key,
                                                    @RequestParam(name = "state", required = false, defaultValue = "-1") Integer state,
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
            if(state==-1){
                if(key.length()>0){
                    devicePage=deviceRepository.get_List_Device(pageable,key.trim());
                }else{
                    devicePage=deviceRepository.get_List_Device(pageable);
                }
            }else{
                if(key.length()>0){
                    devicePage=deviceRepository.get_List_Device_By_State(pageable,key.trim(),state);
                }else{
                    devicePage=deviceRepository.get_List_Device_By_State(pageable,state);
                }
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
                obj.put("box_id", deviceList.get(i).getBox_id());
                obj.put("rom_version", deviceList.get(i).getRom_version());
                obj.put("mode", deviceList.get(i).getMode());
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

    @GetMapping(value = "check_Device", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> check_Device(@RequestHeader(defaultValue = "") String Authorization,
                                                               @RequestParam(defaultValue = "") String device_id,
                                                               @RequestParam(defaultValue = "") String profile_list,
                                                               @RequestParam(defaultValue = "") String profile_remove,
                                                               @RequestParam(defaultValue = "") String ip,
                                                               @RequestParam(defaultValue = "") String rom_version
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
            if(device_id.trim().length() ==0){
                resp.put("status",false);
                data.put("message", "device_id không để trống");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if(profile_list.trim().length() ==0){
                resp.put("status",false);
                data.put("message", "profile_list không để trống");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            Boolean owner_Running=false;
            List<String> profileId = new ArrayList<>();
            profileId.addAll(Arrays.asList(profile_list.trim().split(",")));
            if(profileId.size()==1&&profileId.contains("0")){
                owner_Running=true;
            }else{
                profileId.remove("0");
            }
            List<String> profile =new ArrayList<>();
            for (int i=0;i<profileId.size();i++ ) {
                profile.add(device_id.trim()+"_"+profileId.get(i).toString().trim());
            }
            Device device=deviceRepository.check_DeviceId(device_id.trim());
            if(device==null){
                Device device_new=new Device();
                device_new.setDevice_id(device_id.trim());
                device_new.setAdd_time(System.currentTimeMillis());
                device_new.setState(0);
                device_new.setUpdate_time(System.currentTimeMillis());
                device_new.setNum_account(0);
                device_new.setReboot(0);
                device_new.setRom_version(rom_version);
                device_new.setBox_id("");
                device_new.setMode("");
                device_new.setNum_profile(profileId.size());
                device_new.setIp_address(ip);
                deviceRepository.save(device_new);
                device=device_new;
            }else{
                device.setRom_version(rom_version);
                device.setIp_address(ip);
                device.setNum_profile(profileId.size());
                deviceRepository.save(device);
            }
            profileTaskRepository.delete_Profile_Not_In(profile,device_id.trim());
            String enabled="";
            Random ran=new Random();
            if(profileTaskRepository.check_Profile_Enabled(device_id.trim())==0){
                enabled=profileId.get(ran.nextInt(profileId.size()));
            }
            for (int i=0;i<profile.size();i++){
                if(profileId.get(i).toString().trim().equals("0")&&!owner_Running){
                    continue;
                }
                ProfileTask profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profileId.get(i).trim());
                if(profileTask==null){
                    ProfileTask profileTask_new=new ProfileTask();
                    profileTask_new.setProfile_id(device_id.trim()+"_"+profileId.get(i).trim());
                    profileTask_new.setAccount_id("");
                    profileTask_new.setAdd_time(System.currentTimeMillis());
                    profileTask_new.setDevice(device);
                    profileTask_new.setAccount_level(0);
                    profileTask_new.setGet_time(0L);
                    profileTask_new.setOrder_id(0L);
                    profileTask_new.setRunning(0);
                    profileTask_new.setState(0);
                    profileTask_new.setTask("");
                    profileTask_new.setPlatform("");
                    profileTask_new.setTask_key("");
                    profileTask_new.setTask_list("");
                    profileTask_new.setTask_index(0);
                    profileTask_new.setTask_index(0);
                    profileTask_new.setValid(1);
                    profileTaskRepository.save(profileTask_new);
                }
            }
            if(enabled.length()!=0){
                profileTaskRepository.update_Enabled_Profile_By_ProfileId(device_id.trim()+"_"+enabled,System.currentTimeMillis());
            }
            if(profile_remove.trim().length()>0&&!profile_remove.trim().equals("0")){
                profileTaskRepository.update_Valid_Profile_By_ProfileId(0,device_id.trim()+"_"+profile_remove.trim());
            }
            resp.put("status",true);
            data.put("message", "Check hoàn tất");
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

    @DeleteMapping(value = "delete_Device", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> delete_Device(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String device_id) throws InterruptedException {
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
            List<String> arrDevice=new ArrayList<>(Arrays.asList(device_id.split(",")));
            deviceRepository.delete_Device_By_List_Device(arrDevice);
            accountRepository.reset_Account_By_ListDevice(arrDevice);
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

    @GetMapping(value = "remove_Profile", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> remove_Profile(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String device_id
            , @RequestParam(defaultValue = "") String profile_id
    ) throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if(checktoken ==0){
            resp.put("status",false);
            data.put("message", "Token expired");
            resp.put("data",data);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
        if(device_id.trim().length() ==0){
            resp.put("status",false);
            data.put("message", "device_id không để trống");
            resp.put("data",data);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
        if(profile_id.trim().length() ==0){
            resp.put("status",false);
            data.put("message", "profile_id không để trống");
            resp.put("data",data);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
        try{
            List<String> arrDevice=new ArrayList<>(Arrays.asList(device_id.split(",")));
            deviceRepository.delete_Device_By_List_Device(arrDevice);
            accountRepository.reset_Account_By_ListDevice(arrDevice);
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

    @GetMapping(value = "update_State", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> update_State(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String device_id,
                                                            @RequestParam(defaultValue = "1") Integer state) throws InterruptedException {
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
            List<String> arrPlatform=new ArrayList<>(Arrays.asList(device_id.split(",")));
            deviceRepository.update_State_By_DeviceId(state,arrPlatform);
            List<DeviceShow> deviceList=deviceRepository.get_List_Device_By_DeviceId(arrPlatform);
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
                obj.put("box_id", deviceList.get(i).getBox_id());
                obj.put("rom_version", deviceList.get(i).getRom_version());
                obj.put("mode", deviceList.get(i).getMode());
                jsonArray.add(obj);
            }
            resp.put("device", jsonArray);
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

    @GetMapping(value = "reset_Device", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> reset_Device(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String device_id
                                                          ) throws InterruptedException {
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
            List<String> arrDevice=new ArrayList<>(Arrays.asList(device_id.split(",")));
            profileTaskRepository.delete_Profile_By_List_Device(arrDevice);
            deviceRepository.update_NumProfile_By_ListDevice(0,arrDevice);
            accountRepository.reset_Account_By_ListDevice(arrDevice);
            List<DeviceShow> deviceList=deviceRepository.get_List_Device_By_DeviceId(arrDevice);
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
                obj.put("box_id", deviceList.get(i).getBox_id());
                obj.put("rom_version", deviceList.get(i).getRom_version());
                obj.put("mode", deviceList.get(i).getMode());
                jsonArray.add(obj);
            }
            resp.put("device", jsonArray);
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

    @GetMapping(value = "update_Mode", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> update_Mode(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String device_id,
                                                            @RequestParam(defaultValue = "") String mode) throws InterruptedException {
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
            List<String> arrPlatform=new ArrayList<>(Arrays.asList(device_id.split(",")));
            deviceRepository.update_Mode_By_DeviceId(mode.trim().toLowerCase(),arrPlatform);
            List<DeviceShow> deviceList=deviceRepository.get_List_Device_By_DeviceId(arrPlatform);
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
                obj.put("box_id", deviceList.get(i).getBox_id());
                obj.put("rom_version", deviceList.get(i).getRom_version());
                obj.put("mode", deviceList.get(i).getMode());
                jsonArray.add(obj);
            }
            resp.put("device", jsonArray);
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

    @PostMapping(path = "update",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> update(@RequestHeader(defaultValue = "") String Authorization,@RequestBody Device device_body){
        JSONObject resp = new JSONObject();
        JSONObject data = new JSONObject();
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if(checktoken ==0){
            resp.put("status",false);
            data.put("message", "Token expired");
            resp.put("data",data);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            Device device=deviceRepository.check_DeviceId(device_body.getDevice_id().trim());
            List<String> arrPlatform=new ArrayList<>(Arrays.asList(device_body.getDevice_id().trim().split(",")));
            device.setState(device_body.getState());
            device.setBox_id(device_body.getBox_id());
            device.setMode(device_body.getMode().trim().toLowerCase());
            deviceRepository.save(device);
            JSONArray jsonArray = new JSONArray();
            List<DeviceShow> deviceList=deviceRepository.get_List_Device_By_DeviceId(arrPlatform);
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
                obj.put("box_id", deviceList.get(i).getBox_id());
                obj.put("rom_version", deviceList.get(i).getRom_version());
                obj.put("mode", deviceList.get(i).getMode());
                jsonArray.add(obj);
            }
            resp.put("device", jsonArray);
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

            resp.put("status", false);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
    }

}

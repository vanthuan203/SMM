package com.nts.awspremium.controller;

import com.nts.awspremium.MailApi;
import com.nts.awspremium.ProxyAPI;
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
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
@Service
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
    private SettingSystemRepository settingSystemRepository;
    @Autowired
    private SettingTikTokRepository settingTikTokRepository;
    @Autowired
    private DeviceIpRepository deviceIpRepository;
    @Autowired
    private IpSumRepository ipSumRepository;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private TaskSumRepository taskSumRepository;

    @Autowired
    private ModeRepository modeRepository;

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
            Page<Device> devicePage;
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

            List<Device> deviceList=devicePage.getContent();
            JSONArray jsonArray = new JSONArray();
            SettingTiktok settingTiktok=settingTikTokRepository.get_Setting();

            for (int i = 0; i < deviceList.size(); i++) {
                JSONObject obj = new JSONObject();
                Integer num_account=0;
                Integer num_account_die=0;
                String acc_live=deviceList.get(i).getAccount_live();
                String acc_die=deviceList.get(i).getAccount_die();
                if(acc_live.length()!=0){
                    num_account=acc_live.split(",").length;
                    String[] account = acc_live.split(",");
                    Set<String> uniqueAccount = new LinkedHashSet<>(Arrays.asList(account));
                    acc_live = String.join(",", uniqueAccount);
                }
                if(acc_die.length()!=0){
                    num_account_die=acc_die.split(",").length;
                    String[] account = acc_die.split(",");
                    Set<String> uniqueAccount = new LinkedHashSet<>(Arrays.asList(account));
                    acc_die = String.join(",", uniqueAccount);
                }
                Long max_version=deviceList.get(i).getTiktok_lite_version();
                if(max_version>settingTiktok.getMax_version()) {
                    obj.put("code_tiktok_version",false);
                }else{
                    obj.put("code_tiktok_version",true);
                }

                obj.put("device_id", deviceList.get(i).getDevice_id());
                obj.put("add_time", deviceList.get(i).getAdd_time());
                obj.put("running", deviceList.get(i).getRunning());
                obj.put("update_time", deviceList.get(i).getUpdate_time());
                obj.put("get_time", deviceList.get(i).getTask_time());
                obj.put("num_profile", deviceList.get(i).getNum_profile());
                obj.put("num_profile_set", deviceList.get(i).getNum_profile_set());
                obj.put("num_account", num_account);
                obj.put("num_account_die", num_account_die);
                obj.put("acc_live",acc_live );
                obj.put("acc_die",acc_die );
                obj.put("profile_id", deviceList.get(i).getProfile_running());
                obj.put("platform", deviceList.get(i).getPlatform());
                obj.put("task", deviceList.get(i).getTask());
                obj.put("state", deviceList.get(i).getState());
                obj.put("box_id", deviceList.get(i).getBox_id());
                obj.put("rom_version", deviceList.get(i).getRom_version());
                obj.put("app_version", deviceList.get(i).getApp_version());
                obj.put("tiktok_version",deviceList.get(i).getTiktok_lite_version());
                obj.put("mode", deviceList.get(i).getMode());
                obj.put("status", deviceList.get(i).getStatus());
                obj.put("ip_changer_time", deviceList.get(i).getIp_changer_time());
                obj.put("profile_time", deviceList.get(i).getProfile_time());
                obj.put("note",formatStatus(deviceList.get(i).getNote()));
                obj.put("note_sum",formatStatus(deviceList.get(i).getNote_sum()));
                obj.put("ip_address", deviceList.get(i).getIp_address());
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
    @Transactional
    @GetMapping(value = "check_Device", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> check_Device(@RequestHeader(defaultValue = "") String Authorization,
                                                               @RequestParam(defaultValue = "") String device_id,
                                                               @RequestParam(defaultValue = "") String profile_list,
                                                               @RequestParam(defaultValue = "") String profile_remove,
                                                               @RequestParam(defaultValue = "") String ip,
                                                               @RequestParam(defaultValue = "") String rom_version,
                                                               @RequestParam(defaultValue = "") String app_version
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


            List<String> profileId = new ArrayList<>();
            profileId.addAll(Arrays.asList(profile_list.trim().split(",")));
            profileId.remove("0");
            List<String> profile =new ArrayList<>();
            for (int i=0;i<profileId.size();i++ ) {
                profile.add(device_id.trim()+"_"+profileId.get(i).toString().trim());
            }

            Device device=deviceRepository.check_DeviceIdLock(device_id.trim());
            if(device==null){
                SettingSystem settingSystem=settingSystemRepository.get_Setting_System();
                Device device_new=new Device();
                device_new.setDevice_id(device_id.trim());
                device_new.setAdd_time(System.currentTimeMillis());
                device_new.setIp_changer_time(System.currentTimeMillis());
                device_new.setState(0);
                device_new.setStatus(1);
                device_new.setUpdate_time(System.currentTimeMillis());
                device_new.setReboot_time(System.currentTimeMillis());
                device_new.setNum_account(0);
                device_new.setReboot(0);
                device_new.setRom_version(rom_version.trim());
                device_new.setApp_version(app_version.trim());
                device_new.setBox_id("");
                device_new.setMode("");
                device_new.setAccount_live("");
                device_new.setAccount_die("");
                device_new.setNum_profile(profile.size());
                device_new.setNum_profile_set(settingSystem.getMax_profile());
                device_new.setIp_address(ip);
                device_new.setTiktok_lite_version(0L);
                device_new.setProfile_time(0L);
                device_new.setNote("");
                deviceRepository.save(device_new);
                device=device_new;
            }else{
                SettingSystem settingSystem=settingSystemRepository.get_Setting_System();
                Mode mode=modeRepository.get_Mode_Info(device.getMode().trim());
                if(mode!=null){
                    device.setNum_profile_set(mode.getMax_profile());
                }
                device.setRom_version(rom_version.trim());
                device.setApp_version(app_version.trim());
                if((device.getIp_address().length()==0 || !device.getIp_address().equals(ip.trim()))&&!ip.trim().contains(":")&&ip.trim().contains(".")){
                    device.setIp_address(ip);
                    device.setIp_changer_time(System.currentTimeMillis());
                    IpSum ipSum=ipSumRepository.get_By_IP(ip.trim());
                    if(ipSum==null){
                        ipSum=new IpSum();
                        ipSum.setIp(ip.trim());
                        ipSum.setAdd_time(System.currentTimeMillis());
                        String as=MailApi.ipCheck(ip.trim());
                        if(as!=null){
                            ipSum.setInfo(as);
                        }
                        ipSumRepository.save(ipSum);
                    }else{
                        ipSum.setAdd_time(System.currentTimeMillis());
                        ipSumRepository.save(ipSum);
                    }
                }

                if((System.currentTimeMillis()-device.getCheck_time())/1000/60>15&&device.getState()==1){
                    if(mode.getAdd_proxy()==1){
                        ProxyAPI.add_While_List(ip.trim());
                    }
                    String acc_live=profileTaskRepository.get_AccountLive_By_DeviceId(device.getDevice_id()+"%");
                    String acc_die=profileTaskRepository.get_AccountDie_By_DeviceId(device.getDevice_id()+"%");
                    String noteP=taskSumRepository.task_Sum_By_DeviceId(device.getDevice_id().trim());
                    String note=taskSumRepository.task_Sum_By_DeviceId_24H(device.getDevice_id().trim());
                    if(acc_die==null){
                        device.setStatus(1);
                    }else{
                        device.setStatus(0);
                    }
                    device.setAccount_live(acc_live==null?"":acc_live);
                    device.setAccount_die(acc_die==null?"":acc_die);
                    device.setNote(note==null?"":note);
                    device.setNote_sum(noteP==null?"":noteP);
                    device.setCheck_time(System.currentTimeMillis());
                    device.setTiktok_lite_version(profileTaskRepository.get_Max_Version_Tiktok_By_DeviceId(device.getDevice_id()));
                    ProfileTask profileTask =profileTaskRepository.get_Profile_By_ProfileId(device.getDevice_id().trim()+"_"+device.getProfile_running().trim());

                }
                device.setNum_profile(profile.size());
                device.setPlatform("");
                device.setTask("");
                device.setRunning(0);
                //device.setProfile_running("");
                deviceRepository.save(device);
                //reset running by device_id
                profileTaskRepository.reset_Task_By_deviceId(device.getDevice_id().trim());
                if(profile.size()>=( mode==null?settingSystem.getMax_profile():mode.getMax_profile())){
                    List<String> profileIdList=profileTaskRepository.get_ProfileId_Not_In(profile.size()==0? Collections.singletonList("") :profile,device_id.trim());
                    if(profileIdList.size()>0){
                        profileTaskRepository.delete_Profile_In(profileIdList,device_id.trim());
                    }
                }
            }
            List<String> profileIdList=profileTaskRepository.get_ProfileId_Not_In_And_Valid0(profile.size()==0? Collections.singletonList("") :profile,device_id.trim());
            if(profileIdList.size()>0){
                profileTaskRepository.delete_Profile_In_And_Valid0(profileIdList,device_id.trim());
            }
            String enabled="";
            Random ran=new Random();
            if(profileTaskRepository.check_Profile_Enabled(device_id.trim())==0&&profile.size()>0){
                enabled=profileId.get(ran.nextInt(profileId.size()));
            }
            for (int i=0;i<profile.size();i++){
                if(profileId.get(i).toString().trim().equals("0")){
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
                    profileTask_new.setAdd_proxy(0);
                    profileTask_new.setDis_proxy(0);
                    profileTask_new.setProxy("");
                    profileTask_new.setNote("");
                    profileTask_new.setTiktok_lite_version(0L);
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
            logError.setFile_name(stackTraceElement.getFileName()+ "|"+device_id);
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
    @Transactional
    public Boolean update_Status_Device(
    ){
        try{
            List<String> list_device =deviceRepository.get_All_Device_DieAcc();
            if(list_device.size()>0){
                deviceRepository.update_Status0_Device(list_device);
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
    public Boolean update_Account_Device(
    ){
        try{

            List<Device> deviceList =deviceRepository.get_All_Device();
            for (int i=0;i<deviceList.size();i++){
                String acc_live=profileTaskRepository.get_AccountLive_By_DeviceId(deviceList.get(i).getDevice_id()+"%");
                String acc_die=profileTaskRepository.get_AccountDie_By_DeviceId(deviceList.get(i).getDevice_id()+"%");
                deviceList.get(i).setAccount_live(acc_live==null?"":acc_live);
                deviceList.get(i).setAccount_die(acc_die==null?"":acc_die);
                deviceRepository.save(deviceList.get(i));
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

    @Transactional
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
            deviceRepository.findAndLockDevices(arrDevice);
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
    @Transactional
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
            deviceRepository.findAndLockDevices(arrPlatform);// Chờ lock trước khi update
            deviceRepository.update_State_By_DeviceId(state,arrPlatform);
            List<Device> deviceList=deviceRepository.get_List_Device_By_DeviceId(arrPlatform);
            JSONArray jsonArray = new JSONArray();
            SettingTiktok settingTiktok=settingTikTokRepository.get_Setting();
            for (int i = 0; i < deviceList.size(); i++) {
                JSONObject obj = new JSONObject();

                Integer num_account=0;
                Integer num_account_die=0;
                String acc_live=deviceList.get(i).getAccount_live();
                String acc_die=deviceList.get(i).getAccount_die();
                if(acc_live.length()!=0){
                    num_account=acc_live.split(",").length;
                    String[] account = acc_live.split(",");
                    Set<String> uniqueAccount = new LinkedHashSet<>(Arrays.asList(account));
                    acc_live = String.join(",", uniqueAccount);
                }
                if(acc_die.length()!=0){
                    num_account_die=acc_die.split(",").length;
                    String[] account = acc_die.split(",");
                    Set<String> uniqueAccount = new LinkedHashSet<>(Arrays.asList(account));
                    acc_die = String.join(",", uniqueAccount);
                }

                Long max_version=deviceList.get(i).getTiktok_lite_version();
                if(max_version>settingTiktok.getMax_version()) {
                    obj.put("code_tiktok_version",false);
                }else{
                    obj.put("code_tiktok_version",true);
                }

                obj.put("device_id", deviceList.get(i).getDevice_id());
                obj.put("add_time", deviceList.get(i).getAdd_time());
                obj.put("running", deviceList.get(i).getRunning());
                obj.put("update_time", deviceList.get(i).getUpdate_time());
                obj.put("get_time", deviceList.get(i).getTask_time());
                obj.put("num_profile", deviceList.get(i).getNum_profile());
                obj.put("num_profile_set", deviceList.get(i).getNum_profile_set());
                obj.put("num_account", num_account);
                obj.put("num_account_die", num_account_die);
                obj.put("acc_live",acc_live );
                obj.put("acc_die",acc_die );
                obj.put("profile_id", deviceList.get(i).getProfile_running());
                obj.put("platform", deviceList.get(i).getPlatform());
                obj.put("task", deviceList.get(i).getTask());
                obj.put("state", deviceList.get(i).getState());
                obj.put("box_id", deviceList.get(i).getBox_id());
                obj.put("rom_version", deviceList.get(i).getRom_version());
                obj.put("app_version", deviceList.get(i).getApp_version());
                obj.put("tiktok_version",deviceList.get(i).getTiktok_lite_version());
                obj.put("mode", deviceList.get(i).getMode());
                obj.put("status", deviceList.get(i).getStatus());
                obj.put("ip_changer_time", deviceList.get(i).getIp_changer_time());
                obj.put("ip_address", deviceList.get(i).getIp_address());
                obj.put("profile_time", deviceList.get(i).getProfile_time());
                obj.put("note",formatStatus(deviceList.get(i).getNote()));
                obj.put("note_sum",formatStatus(deviceList.get(i).getNote_sum()));
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
    @Transactional
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
            deviceRepository.findAndLockDevices(arrDevice);// Chờ lock trước khi update
            deviceRepository.update_NumProfile_By_ListDevice(1,arrDevice);
            accountRepository.reset_Account_By_ListDevice(arrDevice);
            List<Device> deviceList=deviceRepository.get_List_Device_By_DeviceId(arrDevice);
            JSONArray jsonArray = new JSONArray();
            SettingTiktok settingTiktok=settingTikTokRepository.get_Setting();
            for (int i = 0; i < deviceList.size(); i++) {
                JSONObject obj = new JSONObject();

                Integer num_account=0;
                Integer num_account_die=0;
                String acc_live=deviceList.get(i).getAccount_live();
                String acc_die=deviceList.get(i).getAccount_die();
                if(acc_live.length()!=0){
                    num_account=acc_live.split(",").length;
                    String[] account = acc_live.split(",");
                    Set<String> uniqueAccount = new LinkedHashSet<>(Arrays.asList(account));
                    acc_live = String.join(",", uniqueAccount);
                }
                if(acc_die.length()!=0){
                    num_account_die=acc_die.split(",").length;
                    String[] account = acc_die.split(",");
                    Set<String> uniqueAccount = new LinkedHashSet<>(Arrays.asList(account));
                    acc_die = String.join(",", uniqueAccount);
                }

                Long max_version=profileTaskRepository.get_Max_Version_Tiktok_By_DeviceId(deviceList.get(i).getDevice_id());
                if(max_version>settingTiktok.getMax_version()) {
                    obj.put("code_tiktok_version",false);
                }else{
                    obj.put("code_tiktok_version",true);
                }

                obj.put("device_id", deviceList.get(i).getDevice_id());
                obj.put("add_time", deviceList.get(i).getAdd_time());
                obj.put("running", deviceList.get(i).getRunning());
                obj.put("update_time", deviceList.get(i).getUpdate_time());
                obj.put("get_time", deviceList.get(i).getTask_time());
                obj.put("num_profile", deviceList.get(i).getNum_profile());
                obj.put("num_profile_set", deviceList.get(i).getNum_profile_set());
                obj.put("num_account", num_account);
                obj.put("num_account_die", num_account_die);
                obj.put("acc_live",acc_live );
                obj.put("acc_die",acc_die );
                obj.put("profile_id", deviceList.get(i).getProfile_running());
                obj.put("platform", deviceList.get(i).getPlatform());
                obj.put("task", deviceList.get(i).getTask());
                obj.put("state", deviceList.get(i).getState());
                obj.put("box_id", deviceList.get(i).getBox_id());
                obj.put("rom_version", deviceList.get(i).getRom_version());
                obj.put("app_version", deviceList.get(i).getApp_version());
                obj.put("tiktok_version",deviceList.get(i).getTiktok_lite_version());
                obj.put("mode", deviceList.get(i).getMode());
                obj.put("status", deviceList.get(i).getStatus());
                obj.put("ip_changer_time", deviceList.get(i).getIp_changer_time());
                obj.put("ip_address", deviceList.get(i).getIp_address());
                obj.put("profile_time", deviceList.get(i).getProfile_time());
                obj.put("note",formatStatus(deviceList.get(i).getNote()));
                obj.put("note_sum",formatStatus(deviceList.get(i).getNote_sum()));
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
    @Transactional
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
            deviceRepository.findAndLockDevices(arrPlatform);// Chờ lock trước khi update
            deviceRepository.update_Mode_By_DeviceId(mode.trim().toLowerCase(),arrPlatform); // Chỉ update khi không có lệnh nào khác
            List<Device> deviceList=deviceRepository.get_List_Device_By_DeviceId(arrPlatform);
            JSONArray jsonArray = new JSONArray();
            SettingTiktok settingTiktok=settingTikTokRepository.get_Setting();
            for (int i = 0; i < deviceList.size(); i++) {
                JSONObject obj = new JSONObject();

                Integer num_account=0;
                Integer num_account_die=0;
                String acc_live=deviceList.get(i).getAccount_live();
                String acc_die=deviceList.get(i).getAccount_die();
                if(acc_live.length()!=0){
                    num_account=acc_live.split(",").length;
                    String[] account = acc_live.split(",");
                    Set<String> uniqueAccount = new LinkedHashSet<>(Arrays.asList(account));
                    acc_live = String.join(",", uniqueAccount);
                }
                if(acc_die.length()!=0){
                    num_account_die=acc_die.split(",").length;
                    String[] account = acc_die.split(",");
                    Set<String> uniqueAccount = new LinkedHashSet<>(Arrays.asList(account));
                    acc_die = String.join(",", uniqueAccount);
                }

                Long max_version=profileTaskRepository.get_Max_Version_Tiktok_By_DeviceId(deviceList.get(i).getDevice_id());
                if(max_version>settingTiktok.getMax_version()) {
                    obj.put("code_tiktok_version",false);
                }else{
                    obj.put("code_tiktok_version",true);
                }

                obj.put("device_id", deviceList.get(i).getDevice_id());
                obj.put("add_time", deviceList.get(i).getAdd_time());
                obj.put("running", deviceList.get(i).getRunning());
                obj.put("update_time", deviceList.get(i).getUpdate_time());
                obj.put("get_time", deviceList.get(i).getTask_time());
                obj.put("num_profile", deviceList.get(i).getNum_profile());
                obj.put("num_profile_set", deviceList.get(i).getNum_profile_set());
                obj.put("num_account", num_account);
                obj.put("num_account_die", num_account_die);
                obj.put("acc_live",acc_live );
                obj.put("acc_die",acc_die );
                obj.put("profile_id", deviceList.get(i).getProfile_running());
                obj.put("platform", deviceList.get(i).getPlatform());
                obj.put("task", deviceList.get(i).getTask());
                obj.put("state", deviceList.get(i).getState());
                obj.put("box_id", deviceList.get(i).getBox_id());
                obj.put("rom_version", deviceList.get(i).getRom_version());
                obj.put("app_version", deviceList.get(i).getApp_version());
                obj.put("tiktok_version",deviceList.get(i).getTiktok_lite_version());
                obj.put("mode", deviceList.get(i).getMode());
                obj.put("status", deviceList.get(i).getStatus());
                obj.put("ip_changer_time", deviceList.get(i).getIp_changer_time());
                obj.put("ip_address", deviceList.get(i).getIp_address());
                obj.put("profile_time", deviceList.get(i).getProfile_time());
                obj.put("note",formatStatus(deviceList.get(i).getNote()));
                obj.put("note_sum",formatStatus(deviceList.get(i).getNote_sum()));
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
    @Transactional
    @GetMapping(value = "update_Box", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> update_Box(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String device_id,
                                                           @RequestParam(defaultValue = "") String box) throws InterruptedException {
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
            deviceRepository.findAndLockDevices(arrPlatform);// Chờ lock trước khi update
            deviceRepository.update_Box_By_DeviceId(box.trim().toLowerCase(),arrPlatform);
            List<Device> deviceList=deviceRepository.get_List_Device_By_DeviceId(arrPlatform);
            JSONArray jsonArray = new JSONArray();
            SettingTiktok settingTiktok=settingTikTokRepository.get_Setting();
            for (int i = 0; i < deviceList.size(); i++) {
                JSONObject obj = new JSONObject();

                Integer num_account=0;
                Integer num_account_die=0;
                String acc_live=deviceList.get(i).getAccount_live();
                String acc_die=deviceList.get(i).getAccount_die();
                if(acc_live.length()!=0){
                    num_account=acc_live.split(",").length;
                    String[] account = acc_live.split(",");
                    Set<String> uniqueAccount = new LinkedHashSet<>(Arrays.asList(account));
                    acc_live = String.join(",", uniqueAccount);
                }
                if(acc_die.length()!=0){
                    num_account_die=acc_die.split(",").length;
                    String[] account = acc_die.split(",");
                    Set<String> uniqueAccount = new LinkedHashSet<>(Arrays.asList(account));
                    acc_die = String.join(",", uniqueAccount);
                }

                Long max_version=profileTaskRepository.get_Max_Version_Tiktok_By_DeviceId(deviceList.get(i).getDevice_id());
                if(max_version>settingTiktok.getMax_version()) {
                    obj.put("code_tiktok_version",false);
                }else{
                    obj.put("code_tiktok_version",true);
                }

                obj.put("device_id", deviceList.get(i).getDevice_id());
                obj.put("add_time", deviceList.get(i).getAdd_time());
                obj.put("running", deviceList.get(i).getRunning());
                obj.put("update_time", deviceList.get(i).getUpdate_time());
                obj.put("get_time", deviceList.get(i).getTask_time());
                obj.put("num_profile", deviceList.get(i).getNum_profile());
                obj.put("num_profile_set", deviceList.get(i).getNum_profile_set());
                obj.put("num_account", num_account);
                obj.put("num_account_die", num_account_die);
                obj.put("acc_live",acc_live );
                obj.put("acc_die",acc_die );
                obj.put("profile_id", deviceList.get(i).getProfile_running());
                obj.put("platform", deviceList.get(i).getPlatform());
                obj.put("task", deviceList.get(i).getTask());
                obj.put("state", deviceList.get(i).getState());
                obj.put("box_id", deviceList.get(i).getBox_id());
                obj.put("rom_version", deviceList.get(i).getRom_version());
                obj.put("app_version", deviceList.get(i).getApp_version());
                obj.put("tiktok_version",deviceList.get(i).getTiktok_lite_version());
                obj.put("mode", deviceList.get(i).getMode());
                obj.put("status", deviceList.get(i).getStatus());
                obj.put("ip_changer_time", deviceList.get(i).getIp_changer_time());
                obj.put("ip_address", deviceList.get(i).getIp_address());
                obj.put("profile_time", deviceList.get(i).getProfile_time());
                obj.put("note",formatStatus(deviceList.get(i).getNote()));
                obj.put("note_sum",formatStatus(deviceList.get(i).getNote_sum()));
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
    @Transactional
    @PostMapping(path = "update",produces = "application/hal+json;charset=utf8")
    public ResponseEntity<String> update(@RequestHeader(defaultValue = "") String Authorization, @RequestBody Device device_body){
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
            List<String> arrPlatform=new ArrayList<>(Arrays.asList(device_body.getDevice_id().trim().split(",")));
            Device device=deviceRepository.check_DeviceIdLock(device_body.getDevice_id().trim());
            deviceRepository.update_Device_By_DeviceId(device_body.getState(),device_body.getBox_id().trim(),device_body.getMode().trim().toLowerCase(),device_body.getNum_profile_set(),device.getDevice_id());
            JSONArray jsonArray = new JSONArray();
            SettingTiktok settingTiktok=settingTikTokRepository.get_Setting();
            List<Device> deviceList=deviceRepository.get_List_Device_By_DeviceId(arrPlatform);
            for (int i = 0; i < deviceList.size(); i++) {
                JSONObject obj = new JSONObject();

                Integer num_account=0;
                Integer num_account_die=0;
                String acc_live=deviceList.get(i).getAccount_live();
                String acc_die=deviceList.get(i).getAccount_die();
                if(acc_live.length()!=0){
                    num_account=acc_live.split(",").length;
                    String[] account = acc_live.split(",");
                    Set<String> uniqueAccount = new LinkedHashSet<>(Arrays.asList(account));
                    acc_live = String.join(",", uniqueAccount);
                }
                if(acc_die.length()!=0){
                    num_account_die=acc_die.split(",").length;
                    String[] account = acc_die.split(",");
                    Set<String> uniqueAccount = new LinkedHashSet<>(Arrays.asList(account));
                    acc_die = String.join(",", uniqueAccount);
                }

                Long max_version=profileTaskRepository.get_Max_Version_Tiktok_By_DeviceId(deviceList.get(i).getDevice_id());
                if(max_version>settingTiktok.getMax_version()) {
                    obj.put("code_tiktok_version",false);
                }else{
                    obj.put("code_tiktok_version",true);
                }

                obj.put("device_id", deviceList.get(i).getDevice_id());
                obj.put("add_time", deviceList.get(i).getAdd_time());
                obj.put("running", deviceList.get(i).getRunning());
                obj.put("update_time", deviceList.get(i).getUpdate_time());
                obj.put("get_time", deviceList.get(i).getTask_time());
                obj.put("num_profile", deviceList.get(i).getNum_profile());
                obj.put("num_profile_set", deviceList.get(i).getNum_profile_set());
                obj.put("num_account", num_account);
                obj.put("num_account_die", num_account_die);
                obj.put("acc_live",acc_live );
                obj.put("acc_die",acc_die );
                obj.put("profile_id", deviceList.get(i).getProfile_running());
                obj.put("platform", deviceList.get(i).getPlatform());
                obj.put("task", deviceList.get(i).getTask());
                obj.put("state", deviceList.get(i).getState());
                obj.put("box_id", deviceList.get(i).getBox_id());
                obj.put("rom_version", deviceList.get(i).getRom_version());
                obj.put("app_version", deviceList.get(i).getApp_version());
                obj.put("tiktok_version",deviceList.get(i).getTiktok_lite_version());
                obj.put("mode", deviceList.get(i).getMode());
                obj.put("status", deviceList.get(i).getStatus());
                obj.put("ip_changer_time", deviceList.get(i).getIp_changer_time());
                obj.put("ip_address", deviceList.get(i).getIp_address());
                obj.put("profile_time", deviceList.get(i).getProfile_time());
                obj.put("note",formatStatus(deviceList.get(i).getNote()));
                obj.put("note_sum",formatStatus(deviceList.get(i).getNote_sum()));
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

package com.nts.awspremium.controller;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nts.awspremium.*;
import com.nts.awspremium.model.*;
import com.nts.awspremium.model_system.MySQLCheck;
import com.nts.awspremium.model_system.OrderThreadCheck;
import com.nts.awspremium.model_system.OrderThreadSpeedUpCheck;
import com.nts.awspremium.platform.Instagram.InstagramTask;
import com.nts.awspremium.platform.Instagram.InstagramUpdate;
import com.nts.awspremium.platform.facebook.FacebookTask;
import com.nts.awspremium.platform.facebook.FacebookUpdate;
import com.nts.awspremium.platform.threads.ThreadsTask;
import com.nts.awspremium.platform.threads.ThreadsUpdate;
import com.nts.awspremium.platform.tiktok.TiktokTask;
import com.nts.awspremium.platform.tiktok.TiktokUpdate;
import com.nts.awspremium.platform.x.XTask;
import com.nts.awspremium.platform.x.XUpdate;
import com.nts.awspremium.platform.youtube.YoutubeTask;
import com.nts.awspremium.platform.youtube.YoutubeUpdate;
import com.nts.awspremium.repositories.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/task")
public class TaskController {
    @Autowired
    private TaskPriorityRepository taskPriorityRepository;
    @Autowired
    private OrderRunningRepository orderRunningRepository;
    @Autowired
    private MySQLCheck mySQLCheck;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfileTaskRepository profileTaskRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private DataSubscriberRepository dataSubscriberRepository;
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountCloneRepository accountCloneRepository;

    @Autowired
    private AccountProfileRepository accountProfileRepository;
    @Autowired
    private PlatformRepository platformRepository;
    @Autowired
    private SettingSystemRepository settingSystemRepository;
    @Autowired
    private HistorySumRepository historySumRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    @Autowired
    private TaskSumRepository taskSumRepository;
    @Autowired
    private TiktokTask tiktokTask;
    @Autowired
    private YoutubeTask youtubeTask;
    @Autowired
    private FacebookTask facebookTask;
    @Autowired
    private XTask xTask;
    @Autowired
    private InstagramTask instagramTask;
    @Autowired
    private ThreadsTask threadsTask;
    @Autowired
    private YoutubeUpdate youtubeUpdate;
    @Autowired
    private TiktokUpdate tiktokUpdate;
    @Autowired
    private FacebookUpdate facebookUpdate;
    @Autowired
    private XUpdate xUpdate;
    @Autowired
    private ThreadsUpdate threadsUpdate;
    @Autowired
    private InstagramUpdate instagramUpdate;

    @Autowired
    private IpRegisterRepository ipRegisterRepository;

    @Autowired
    private HistoryRegisterRepository historyRegisterRepository;

    @Autowired
    private AccountNameRepository accountNameRepository;

    @Autowired
    private ModeRepository modeRepository;

    @Autowired
    private ModeOptionRepository modeOptionRepository;

    @Autowired
    private IpTask24hRepository ipTask24hRepository;

    @Autowired
    private AccountTaskRepository accountTaskRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private OrderThreadCheck orderThreadCheck;

    @Autowired
    private OrderThreadSpeedUpCheck OrderThreadSpeedUpCheck;

    @Autowired
    private OpenAiKeyRepository openAiKeyRepository;
    @GetMapping(value = "getTask", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> getTask(@RequestHeader(defaultValue = "") String Authorization,
                                                          @RequestParam(defaultValue = "") String device_id,
                                                          @RequestParam(defaultValue = "") Long tiktok_lite_version,
                                                          @RequestParam(defaultValue = "") String profile_id
    ) throws InterruptedException {

        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran=new Random();
            Thread.sleep(ran.nextInt(1500));
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
            if (checktoken ==0) {
                resp.put("status", false);
                data.put("message", "Token expired");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }else if (device_id.length()==0) {
                resp.put("status", false);
                data.put("message", "device_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }else if (profile_id.length()==0) {
                resp.put("status", false);
                data.put("message", "profile_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //---------------------get_Account------------------//
            SettingSystem settingSystem=settingSystemRepository.get_Setting_System();
            Device device=deviceRepository.check_DeviceId(device_id.trim());
            ProfileTask profileTask=null;
            if(device==null) {
                resp.put("status", false);
                data.put("message", "device_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            Mode mode =modeRepository.get_Mode_Info(device.getMode().trim());
            if(mode==null){
                resp.put("status", false);
                data.put("message", "mode không hợp lệ");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            if((System.currentTimeMillis()-device.getUpdate_time())/1000<mode.getTime_waiting_task()&&!device.getMode().contains("dev")&&!profile_id.trim().equals("0")){
                resp.put("status", false);
                data.put("message", "Không thực hiện nhiệm vụ");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }else if(device.getState()==0){
                device.setUpdate_time(System.currentTimeMillis());
                deviceRepository.save(device);
                resp.put("status", false);
                data.put("message", "device_id không làm nhiệm vụ");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }else if(!device.getApp_version().trim().equals(settingSystem.getApp_version().trim())){

                String ver_Device = device.getApp_version().trim().replaceAll("\\.", ""); // Xóa dấu chấm
                String ver_System = settingSystem.getApp_version().trim().replaceAll("\\.", ""); // Xóa dấu chấm
                if(Long.parseLong(ver_Device.length()==0?"0":ver_Device)<Long.parseLong(ver_System)){
                    device.setUpdate_time(System.currentTimeMillis());
                    deviceRepository.save(device);
                    resp.put("status", false);
                    data.put("message", "device_id chưa cập nhật version auto");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
            if(device.getReboot()==1 || (System.currentTimeMillis()-device.getReboot_time())/1000/60>=settingSystem.getReboot_time() ){
                device.setReboot(0);
                device.setUpdate_time(System.currentTimeMillis());
                device.setReboot_time(System.currentTimeMillis());
                deviceRepository.save(device);
                resp.put("status", true);
                data.put("platform", "system");
                data.put("task", "reboot");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }else if(device.getMode().trim().length()==0){
                device.setUpdate_time(System.currentTimeMillis());
                deviceRepository.save(device);
                resp.put("status", false);
                data.put("message", "mode hiện tại trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            device.setNum_profile_set(mode.getMax_profile());
            device.setUpdate_time(System.currentTimeMillis());
            deviceRepository.save(device);



            if((device.getNum_profile()<mode.getMax_profile() || profileTaskRepository.get_Count_Profile_Valid_0_By_DeviceId(device_id.trim())>0 )&&!profile_id.trim().equals("0")){
                resp.put("status", true);
                data.put("platform", "system");
                data.put("task", "profile_changer");
                data.put("profile_id",0);
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
            if(profileTask==null&&!profile_id.trim().equals("0")){
                resp.put("status", false);
                data.put("message", "profile_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }else if(profile_id.trim().equals("0")){
                if(profileTaskRepository.get_Count_Profile_Valid_1_By_DeviceId(device_id.trim())<mode.getMax_profile()){ //device.getNum_profile()<mode.getMax_profile()
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "create_profile");
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
                if(profileTaskRepository.get_Count_Profile_Valid_0_By_DeviceId(device_id.trim())>0){
                    String profile_remove=profileTaskRepository.get_ProfileId_Valid_0_By_DeviceId(device_id.trim());
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "remove_profile");
                    data.put("profile_id",Integer.parseInt(profile_remove.split(device_id.trim()+"_")[1]));
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
                profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim(),profileTask==null?"":profileTask.getProfile_id());
                if(profileTask!=null){
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "profile_changer");
                    data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else{
                    resp.put("status", false);
                    data.put("message", "Không có profile để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }else if(profileTask!=null){
                if(profileTask.getEnabled()==0){
                    profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim(),profileTask.getProfile_id());
                    if(profileTask!=null){
                        if(mode.getProfile_reboot()){
                            profileTask.setReboot(1);
                            profileTaskRepository.save(profileTask);
                        }
                        device.setProfile_running(profileTask.getProfile_id().split(device_id.trim()+"_")[1]);
                        deviceRepository.save(device);
                        resp.put("status", true);
                        data.put("platform", "system");
                        data.put("task", "profile_changer");
                        data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }else{
                        resp.put("status", false);
                        data.put("message", "Không thực hiện nhiệm vụ");
                        resp.put("data", data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }
            } else if(profileTask==null){
                resp.put("status", false);
                data.put("message", "profile_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //check reboot profile
            if(profileTask.getReboot()==1){
                profileTaskRepository.reset_Reboot_By_ProfileId(profileTask.getProfile_id().trim());
                resp.put("status", true);
                data.put("platform", "system");
                data.put("task", "reboot");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            //check update PI
            if(profileTask.getUpdate_pi()==1){
                resp.put("status", true);
                data.put("platform", "system");
                data.put("task", "update_pi");
                data.put("task_key", "fingerprint");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            //check clear data
            if(profileTask.getClear_data()==1){
                accountProfileRepository.update_Live_Tiktok_By_ProfileId(profileTask.getProfile_id().trim());
                resp.put("status", true);
                data.put("platform", "system");
                data.put("task", "clear_data");
                data.put("task_key", settingSystem.getClear_data_package().trim());
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            /*
            if(profileTask.getState()==0&&profileTask.getAdd_proxy()==1){
                profileTask.setAdd_proxy(0);
                profileTaskRepository.save(profileTask);
            }

             */


            if(device.getProfile_running().length()!=0 && !device.getProfile_running().equals(profile_id.trim()) &&!mode.getMode().contains("dev")&&
            profileTaskRepository.check_Profile_Exist(device_id.trim()+"_"+device.getProfile_running().trim())>0){
                resp.put("status", true);
                data.put("platform", "system");
                data.put("task", "profile_changer");
                data.put("profile_id", Integer.parseInt(device.getProfile_running()));
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            //changer profile  khi du thoi gian hoạt động
            if((System.currentTimeMillis()-profileTask.getOnline_time())/1000/60>=mode.getTime_profile() && profileTask.getState()==1 &&!mode.getMode().contains("dev")) {
                if (profileTaskRepository.get_Count_Profile_Enabled(device_id.trim()) > 1) {
                    profileTaskRepository.reset_Thread_Index_By_DeviceId_While_ChangerProfile(device_id.trim());
                    entityManager.clear();
                    profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim(),profileTask.getProfile_id());

                    accountProfileRepository.update_Live_Tiktok_By_ProfileId(profileTask.getProfile_id().trim());//check login lại acc tiktok

                    if(mode.getProfile_reboot()){
                        profileTask.setReboot(1);
                        profileTaskRepository.save(profileTask);
                    }
                    device.setProfile_running(profileTask.getProfile_id().split(device_id.trim()+"_")[1]);
                    deviceRepository.save(device);
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "profile_changer");
                    data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim() + "_")[1]));
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else{
                    profileTaskRepository.reset_Thread_Index_By_DeviceId_While_ChangerProfile_1_On(device_id.trim());
                    entityManager.clear();
                    profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                    accountProfileRepository.update_Live_Tiktok_By_ProfileId(profileTask.getProfile_id().trim());//check login lại acc tiktok
                }
            }else if((System.currentTimeMillis()-profileTask.getOnline_time())/1000/60>=mode.getTime_profile() && profileTask.getState()==1 &&mode.getMode().contains("dev")) {
                profileTaskRepository.reset_Thread_Index_By_DeviceId_While_ChangerProfile_1_On(device_id.trim());
                entityManager.clear();
                profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
            }


            //update time check
            if(profileTask.getState()==0){ // check profile bắt đầu mở file
                profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                entityManager.clear();

                List<String> string_Task_List=platformRepository.get_All_Platform_True(device.getMode());
                String task_List=String.join(",", string_Task_List);
                List<String> arrPlatform=new ArrayList<>(Arrays.asList(task_List.split(",")));

                profileTask.setPlatform(arrPlatform.get(0));
                List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                task_List=String.join(",", subPlatform);
                profileTask.setTask_list(task_List);
                profileTask.setTiktok_lite_version(tiktok_lite_version);
                profileTask.setTask_index(0);
                profileTask.setTask_time(System.currentTimeMillis());
                profileTask.setState(1);
                profileTask.setRequest_index(0);
                profileTask.setOnline_time(System.currentTimeMillis());
                profileTask.setUpdate_time(System.currentTimeMillis());
                profileTaskRepository.save(profileTask);
                device.setProfile_time(System.currentTimeMillis());
                deviceRepository.save(device);
            }else{
                profileTask.setTiktok_lite_version(tiktok_lite_version);
                profileTask.setTask_time(System.currentTimeMillis());
                profileTask.setUpdate_time(System.currentTimeMillis());
                profileTaskRepository.save(profileTask);
            }


            if(mode.getAdd_proxy()==1&&profileTask.getAdd_proxy()==0){
                String[] geo={"th", "za", "kr", "jp", "id", "bd", "eg", "my"};
                //String proxy=ProxyAPI.getSock5Luna("id");
                String proxy=ProxyAPI.getHttpV6(mode.getGeography().trim());
                if(proxy!=null){
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "add_proxy");
                    data.put("task_key",proxy);
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else {
                    resp.put("status", false);
                    data.put("message", "Không thực hiện nhiệm vụ");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }


            //check acc youtube
            AccountProfile accountYoutube=null;
            Platform platform_Youtube_Check=platformRepository.get_Platform_By_Platform_And_Mode("youtube",device.getMode().trim());
            if(accountProfileRepository.check_Count_AccountLive1_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube")<platform_Youtube_Check.getMax_account()&&
            platform_Youtube_Check.getLogin_account()==1){ //check xem đủ tài khoản youtbe live=1 chưa
                AccountProfile accountProfile_Live0=accountProfileRepository.get_AccountLive0_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube");
                if(accountProfile_Live0==null){ // If account null or not live then get new acc
                    if(platform_Youtube_Check.getLogin_account()==1&&
                            (System.currentTimeMillis()-profileTask.getGet_account_time())/1000/60>=15&&
                            accountRepository.check_Count_AccountDie24H_By_Platform_And_ProfileId(profileTask.getProfile_id().trim(),"youtube")<3&& //check acc die in 24h
                            accountProfileRepository.count_Login_By_Platform_And_DeviceId("youtube",device.getDevice_id().trim()+"%",platform_Youtube_Check.getLogin_time())==0 // check time login gần nhất
                    ){
                        String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                        String code="";
                        for(int i=0;i<10;i++){
                            Integer ranver=ran.nextInt(stringrand.length());
                            code=code+stringrand.charAt(ranver);
                        }
                        //get account new
                        Account account_get= accountRepository.get_Account_Youtube_By_ProfileId(device_id.trim()+"_"+profile_id.trim(),device_id,System.currentTimeMillis(),code,device.getMode().trim());
                        if(account_get!=null){ // true

                            AccountProfile accountCheck=accountProfileRepository.get_Account_By_Account_id_And_Platform(account_get.getAccount_id(),"youtube");
                            if(accountCheck!=null){
                                accountProfileRepository.delete(accountCheck);
                            }
                            AccountProfile accountProfile=new AccountProfile();
                            accountProfile.setAccount_id(account_get.getAccount_id());
                            accountProfile.setPassword(account_get.getPassword());
                            accountProfile.setRecover(account_get.getRecover_mail());
                            accountProfile.setRecover_password(account_get.getRecover_mail_password());
                            if(account_get.getName().length()==0){
                                String name=StringUtils.getName();
                                accountProfile.setName(name);
                                account_get.setName(name);
                                accountRepository.save(account_get);
                            }
                            accountProfile.setName(account_get.getName());
                            accountProfile.setPlatform("youtube");
                            accountProfile.setLive(0);
                            accountProfile.setRunning(0);
                            accountProfile.setSign_in(1);
                            accountProfile.setCode(code);
                            accountProfile.setTask_time(System.currentTimeMillis());
                            accountProfile.setConnection_platform("");
                            accountProfile.setChanged(account_get.getChanged());
                            accountProfile.setAvatar(account_get.getAvatar());
                            accountProfile.setAuth_2fa(account_get.getAuth_2fa());
                            accountProfile.setProfileTask(profileTask);
                            accountProfile.setAdd_time(System.currentTimeMillis());
                            accountProfile.setUpdate_time(0L);
                            accountProfileRepository.save(accountProfile);

                            //accountProfileRepository.update_SignIn_By_ProfileId(profileTask.getProfile_id().trim());// reset signin profile

                            profileTask.setGet_account_time(System.currentTimeMillis());
                            profileTaskRepository.save(profileTask);

                            resp.put("status", true);
                            data.put("platform", "youtube");
                            data.put("app", platform_Youtube_Check.getApp_name().trim());
                            data.put("task", "login");
                            data.put("task_key", account_get.getAccount_id().substring(0,account_get.getAccount_id().lastIndexOf("|")));
                            data.put("account_id",account_get.getAccount_id().substring(0,account_get.getAccount_id().lastIndexOf("|")));
                            data.put("password", account_get.getPassword().trim());
                            data.put("name", account_get.getName());
                            data.put("avatar", account_get.getAvatar()==0?false:true);
                            data.put("recover_mail", account_get.getRecover_mail().trim());
                            data.put("recover_mail_password", account_get.getRecover_mail_password().trim());
                            data.put("auth_2fa", account_get.getAuth_2fa().trim());
                            data.put("account_list",accountProfileRepository.get_List_Account_Youtube_By_ProfileId(profileTask.getProfile_id().trim()));
                            resp.put("data",data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }else{
                            profileTask.setGet_account_time(System.currentTimeMillis());
                            profileTaskRepository.save(profileTask);
                            resp.put("status", false);
                            data.put("message", "Không thực hiện nhiệm vụ");
                            resp.put("data", data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }

                    }
                    /*
                    else{
                        resp.put("status", false);
                        data.put("message", "Không thực hiện nhiệm vụ");
                        resp.put("data", data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                     */
                }else if((System.currentTimeMillis()-accountProfile_Live0.getLast_time())/1000/60>=15){
                    accountProfile_Live0.setLast_time(System.currentTimeMillis());
                    accountProfileRepository.save(accountProfile_Live0);

                    //accountProfileRepository.update_SignIn_By_ProfileId(profileTask.getProfile_id().trim());// reset signin profile

                    resp.put("status", true);
                    data.put("platform", "youtube");
                    data.put("app", platform_Youtube_Check.getApp_name().trim());
                    if(accountProfile_Live0.getAccount_id().contains("register")){
                        data.put("task", "register");
                    }else{
                        data.put("task", "login");
                    }
                    data.put("task_key", accountProfile_Live0.getAccount_id().substring(0,accountProfile_Live0.getAccount_id().lastIndexOf("|")));
                    data.put("account_id", accountProfile_Live0.getAccount_id().substring(0,accountProfile_Live0.getAccount_id().lastIndexOf("|")));
                    data.put("password", accountProfile_Live0.getPassword().trim());
                    data.put("name", accountProfile_Live0.getName());
                    data.put("avatar", accountProfile_Live0.getAvatar()==0?false:true);
                    data.put("recover_mail", accountProfile_Live0.getRecover().trim());
                    data.put("recover_mail_password", accountProfile_Live0.getRecover_password().trim());
                    data.put("auth_2fa", accountProfile_Live0.getAuth_2fa().trim());
                    data.put("account_list",accountProfileRepository.get_List_Account_Youtube_By_ProfileId(profileTask.getProfile_id().trim()));
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
                /*
                else{
                    resp.put("status", false);
                    data.put("message", "Không thực hiện nhiệm vụ");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
                 */
            }else if(platform_Youtube_Check.getRegister_account()==1&&platform_Youtube_Check.getState()==1&&profileTask.getRegister_index()==0) {
                AccountProfile accountProfile=accountProfileRepository.get_Account_By_Account_id_And_Platform("register_"+profileTask.getProfile_id()+"|youtube","youtube");
                if(accountProfile==null && historyRegisterRepository.count_Register_By_Platform_And_Time("youtube",5)>=15){
                    /*
                    profileTask.setRegister_index(profileTask.getRegister_index()+1);
                    profileTask.setRequest_index(profileTask.getRequest_index()+1);
                    profileTaskRepository.save(profileTask);

                    resp.put("status", false);
                    data.put("message", "Không thực hiện nhiệm vụ");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);

                     */
                }else if(accountProfile==null && historyRegisterRepository.count_Register_By_Platform_And_DeviceId("youtube",device.getDevice_id().trim()+"%",platform_Youtube_Check.getRegister_time())==0&&
                        accountRepository.check_Count_Register_LessDay_By_DeviceId_And_Platform(device.getDevice_id().trim(),"youtube",7)<platform_Youtube_Check.getRegister_limit()&&
                        accountRepository.check_Count_Register_LessDay_By_ProfileId_And_Platform(device.getDevice_id().trim(),"youtube",7)==0&&
                        accountProfileRepository.count_Register_Task_By_Platform_And_DeviceId("youtube",device.getDevice_id()+"%")==0&&
                        accountProfileRepository.count_Gmail_By_Platform_And_PrfoileId("youtube",profileTask.getProfile_id().trim(),14)==0
                ){
                    String password="Cmc#";
                    String passrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                    for(int i=0;i<6;i++){
                        Integer ranver=ran.nextInt(passrand.length());
                        password=password+passrand.charAt(ranver);
                    }

                    accountProfile=new AccountProfile();
                    accountProfile.setAccount_id("register_"+profileTask.getProfile_id()+"|youtube");
                    accountProfile.setPassword(password);
                    accountProfile.setName("");
                    accountProfile.setAvatar(0);
                    accountProfile.setRunning(0);
                    accountProfile.setSign_in(0);
                    accountProfile.setRecover("");
                    accountProfile.setPlatform("youtube");
                    accountProfile.setLive(-1);
                    accountProfile.setChanged(0);
                    accountProfile.setAuth_2fa("");
                    accountProfile.setCode(password);
                    accountProfile.setTask_time(System.currentTimeMillis());
                    accountProfile.setConnection_platform("");
                    accountProfile.setProfileTask(profileTask);
                    accountProfile.setAdd_time(System.currentTimeMillis());
                    accountProfile.setUpdate_time(0L);
                    accountProfileRepository.save(accountProfile);

                    HistoryRegister historyRegister=new HistoryRegister();
                    historyRegister.setProfileTask(profileTask);
                    historyRegister.setPlatform("youtube");
                    historyRegister.setState(0);
                    historyRegister.setUpdate_time(System.currentTimeMillis());
                    historyRegisterRepository.save(historyRegister);

                    profileTask.setRegister_index(profileTask.getRegister_index()+1);
                    profileTask.setRequest_index(profileTask.getRequest_index()+1);
                    profileTaskRepository.save(profileTask);

                    //accountProfileRepository.update_SignIn_By_ProfileId(profileTask.getProfile_id().trim());// reset signin profile


                    resp.put("status", true);
                    data.put("platform","youtube");
                    data.put("app",platform_Youtube_Check.getApp_name().trim());
                    data.put("task", "register");
                    data.put("task_key", accountProfile.getAccount_id().substring(0,accountProfile.getAccount_id().lastIndexOf("|")));
                    data.put("account_id",accountProfile.getAccount_id().substring(0,accountProfile.getAccount_id().lastIndexOf("|")));
                    data.put("password",accountProfile.getPassword());
                    data.put("name",accountProfile.getName());
                    data.put("avatar",accountProfile.getAvatar()==0?false:true);
                    data.put("recover_mail",  accountProfile.getRecover().trim());
                    data.put("recover_mail_password",  accountProfile.getRecover_password().trim());
                    data.put("auth_2fa", "");
                    data.put("account_list",accountProfileRepository.get_List_Account_Youtube_By_ProfileId(profileTask.getProfile_id().trim()));
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else if(accountProfile!=null){
                    if(accountProfile.getLive()!=-1){
                        Account account =accountRepository.get_Account_By_Account_id(accountProfile.getAccount_id());
                        accountRepository.delete(account);
                        accountProfileRepository.delete(accountProfile);
                    }else{

                        profileTask.setRegister_index(profileTask.getRegister_index()+1);
                        profileTask.setRequest_index(profileTask.getRequest_index()+1);
                        profileTaskRepository.save(profileTask);

                        //accountProfileRepository.update_SignIn_By_ProfileId(profileTask.getProfile_id().trim());// reset signin profile

                        resp.put("status", true);
                        data.put("platform","youtube");
                        data.put("app",platform_Youtube_Check.getApp_name().trim());
                        data.put("task", "register");
                        data.put("task_key", accountProfile.getAccount_id().substring(0,accountProfile.getAccount_id().lastIndexOf("|")));
                        data.put("account_id",accountProfile.getAccount_id().substring(0,accountProfile.getAccount_id().lastIndexOf("|")));
                        data.put("password",accountProfile.getPassword());
                        data.put("name",accountProfile.getName());
                        data.put("avatar",accountProfile.getAvatar()==0?false:true);
                        data.put("recover_mail",  accountProfile.getRecover().trim());
                        data.put("recover_mail_password",  accountProfile.getRecover_password().trim());
                        data.put("auth_2fa", "");
                        data.put("account_list",accountProfileRepository.get_List_Account_Youtube_By_ProfileId(profileTask.getProfile_id().trim()));
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }
            }
            boolean hasLoggedYoutube =accountProfileRepository.check_Count_AccountLive1_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube")>0;
            if(profileTask.getGoogle_time()==0 && hasLoggedYoutube){
                profileTask.setGoogle_time(System.currentTimeMillis());
            }else if(!hasLoggedYoutube){
                profileTask.setGoogle_time(0L);
                device.setUpdate_time(System.currentTimeMillis() + 300 * 1000);
                deviceRepository.save(device);
                resp.put("status", false);
                data.put("message", "Không thực hiện nhiệm vụ");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            // check tài khoản các nền tảng khác
            if(profileTask.getPlatform().length()==0){
                profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                entityManager.clear();

                List<String> string_Task_List=platformRepository.get_All_Platform_True(device.getMode());
                String task_List=String.join(",", string_Task_List);
                List<String> arrPlatform=new ArrayList<>(Arrays.asList(task_List.split(",")));

                profileTask.setPlatform(arrPlatform.get(0));
                List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                task_List=String.join(",", subPlatform);
                profileTask.setTask_list(task_List);
                profileTask.setTask_index(0);
                profileTask.setState(1);
                profileTask.setRequest_index(0);
                profileTaskRepository.save(profileTask);
            }
            if(profileTask.getTask_index()>=platformRepository.get_Priority_By_Platform_And_Mode(profileTask.getPlatform(),device.getMode())) //check giới hạn lần get nhiệm vụ theo platform and mode
            {
                if(profileTask.getTask_list().trim().length()==0){ // nếu trong list platform trống thì lấy lại từ đầu
                    profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                    entityManager.clear();

                    List<String> string_Task_List=platformRepository.get_All_Platform_True(device.getMode());
                    String task_List=String.join(",", string_Task_List);
                    List<String> arrPlatform=new ArrayList<>(Arrays.asList(task_List.split(",")));

                    profileTask.setPlatform(arrPlatform.get(0));
                    List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                    task_List=String.join(",", subPlatform);
                    profileTask.setTask_list(task_List);
                    profileTask.setTask_index(0);
                    profileTask.setState(1);
                    profileTask.setRequest_index(0);
                    profileTaskRepository.save(profileTask);

                }else {
                    String task_List = profileTask.getTask_list();
                    List<String> arrPlatform = new ArrayList<>(Arrays.asList(task_List.split(",")));

                    profileTask.setPlatform(arrPlatform.get(0));
                    List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                    task_List = String.join(",", subPlatform);
                    profileTask.setTask_list(task_List);
                    profileTask.setTask_index(0);
                    profileTask.setRequest_index(0);
                    profileTaskRepository.save(profileTask);
                }
            }


            Platform platform_Check=platformRepository.get_Platform_By_Platform_And_Mode(profileTask.getPlatform().trim(),device.getMode().trim());
            if(!profileTask.getPlatform().trim().equals("youtube")){
                if(accountProfileRepository.check_Count_AccountLive_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),profileTask.getPlatform().trim())<platform_Check.getMax_account()){ //check xem đủ tài khoản youtbe live=1 chưa
                    AccountProfile accountProfile_Live0=accountProfileRepository.get_AccountLive0_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),profileTask.getPlatform().trim());
                    if(accountProfile_Live0==null) {
                        if(platform_Check.getRegister_account()==1 &&
                                platform_Check.getDependent().trim().length()>0&&platform_Check.getConnection_account()==1&&
                                historyRegisterRepository.count_Register_By_Platform_And_DeviceId(profileTask.getPlatform().trim(),profileTask.getDevice().getDevice_id().trim()+"%",platform_Check.getRegister_time())==0&&
                                accountRepository.check_Count_Register_LessDay_By_DeviceId_And_Platform(device.getDevice_id().trim(),profileTask.getPlatform().trim(),7)<platform_Check.getRegister_limit()&&
                                accountProfileRepository.get_Count_Account_DependentLive_By_ProfileId_And_Platform(profileTask.getProfile_id(),platform_Check.getDependent().trim(),"%"+profileTask.getPlatform().trim()+"%")>0
                        ){
                            //Add proxy
                            /*
                            if(mode.getAdd_proxy()==1&&profileTask.getAdd_proxy()==0){
                                String[] geo={"th", "za", "kr", "jp", "id", "bd", "eg", "my"};
                                String proxy=ProxyAPI.getSock5Luna(geo[ran.nextInt(geo.length)]);
                                if(proxy!=null){
                                    resp.put("status", true);
                                    data.put("platform", "system");
                                    data.put("task", "add_proxy");
                                    data.put("task_key",proxy);
                                    resp.put("data",data);
                                    return new ResponseEntity<>(resp, HttpStatus.OK);
                                }else {
                                    resp.put("status", false);
                                    data.put("message", "Không thực hiện nhiệm vụ");
                                    resp.put("data", data);
                                    return new ResponseEntity<>(resp, HttpStatus.OK);
                                }
                            }
                             */
                            //gioi han time reg by platform and time
                            //List<String> list_device =deviceRepository.get_All_Device_By_IP(device.getIp_address().trim());
                            //historyRegisterRepository.count_Register_By_Platform_And_Time(profileTask.getPlatform().trim(),list_device,1)==0
                            Boolean check_Register=false;
                            IpRegister ipRegister_old=ipRegisterRepository.get_Ip_By_Ip_And_Platform(device.getIp_address(),profileTask.getPlatform().trim());
                            if(mode.getAdd_proxy()==1){
                                check_Register=true;
                            }else if(ipRegister_old==null){
                                check_Register=true;
                            }else if(ipRegister_old!=null &&ipRegister_old.getSuccess()==false && (System.currentTimeMillis()-ipRegister_old.getUpdate_time())/1000/60/60>=12) {
                                check_Register=true;
                            }else if(ipRegister_old!=null &&ipRegister_old.getSuccess()==true && (System.currentTimeMillis()-ipRegister_old.getUpdate_time())/1000/60/60>=24) {
                                check_Register=true;
                            }
                            if(check_Register){
                                AccountProfile accountProfile_Dependent=accountProfileRepository.get_Account_DependentLive_By_ProfileId_And_Platform(profileTask.getProfile_id(),platform_Check.getDependent().trim(),"%"+profileTask.getPlatform().trim()+"%");
                                AccountProfile accountCheck=accountProfileRepository.get_Account_By_Account_id_And_Platform(accountProfile_Dependent.getAccount_id().substring(0,accountProfile_Dependent.getAccount_id().lastIndexOf("|"))+"|"+profileTask.getPlatform(),profileTask.getPlatform());
                                if(accountCheck!=null){
                                    accountProfileRepository.delete(accountCheck);
                                }
                                AccountProfile accountProfile=new AccountProfile();
                                accountProfile.setAccount_id(accountProfile_Dependent.getAccount_id().substring(0,accountProfile_Dependent.getAccount_id().lastIndexOf("|"))+"|"+profileTask.getPlatform());
                                accountProfile.setPassword(accountProfile_Dependent.getPassword().trim());
                                if(profileTask.getPlatform().equals("tiktok")){
                                    String name=null;
                                    if(accountProfile_Dependent.getName().length()!=0){
                                        name= Openai.nameTiktok2(accountProfile_Dependent.getName().trim(),openAiKeyRepository.get_OpenAI_Key());
                                        if(name==null){
                                            name=accountProfile_Dependent.getName();
                                        }
                                    }else{
                                        name= Openai.nameTiktok(accountProfile_Dependent.getAccount_id().substring(0,accountProfile_Dependent.getAccount_id().lastIndexOf("@")),openAiKeyRepository.get_OpenAI_Key());
                                    }
                                    if(name!=null){
                                        accountProfile.setName(name);
                                    }else {
                                        AccountName accountName=accountNameRepository.get_AcountName_By_Platform("tiktok");
                                        accountProfile.setName(accountName.getName());
                                    }
                                }else{
                                    accountProfile.setName(accountProfile_Dependent.getName().trim());
                                }
                                accountProfile.setAvatar(0);
                                accountProfile.setRecover(accountProfile_Dependent.getAccount_id().substring(0,accountProfile_Dependent.getAccount_id().lastIndexOf("|"))+"|"+accountProfile_Dependent.getPlatform());
                                accountProfile.setPlatform(profileTask.getPlatform());
                                accountProfile.setLive(-1);
                                accountProfile.setChanged(0);
                                accountProfile.setRunning(0);
                                accountProfile.setSign_in(0);
                                accountProfile.setAuth_2fa("");
                                accountProfile.setProfileTask(profileTask);
                                accountProfile.setAdd_time(System.currentTimeMillis());
                                accountProfile.setUpdate_time(0L);
                                accountProfile.setCode(accountProfile_Dependent.getCode().trim());
                                accountProfile.setTask_time(System.currentTimeMillis());
                                accountProfile.setConnection_platform("");
                                accountProfileRepository.save(accountProfile);
                                if(!accountProfile_Dependent.getConnection_platform().contains(profileTask.getPlatform())){
                                    accountProfile_Dependent.setConnection_platform(accountProfile_Dependent.getConnection_platform().trim()+profileTask.getPlatform()+"|");
                                    accountProfileRepository.save(accountProfile_Dependent);
                                }
                                HistoryRegister historyRegister=new HistoryRegister();
                                historyRegister.setProfileTask(profileTask);
                                historyRegister.setPlatform(profileTask.getPlatform().trim());
                                historyRegister.setState(0);
                                historyRegister.setIp_address(device.getIp_address());
                                historyRegister.setUpdate_time(System.currentTimeMillis());
                                historyRegisterRepository.save(historyRegister);
                                if(ipRegister_old!=null){
                                    ipRegister_old.setSuccess(false);
                                    ipRegister_old.setUpdate_time(System.currentTimeMillis());
                                    ipRegisterRepository.save(ipRegister_old);
                                }else{
                                    IpRegister ipRegister=new IpRegister();
                                    ipRegister.setId(device.getIp_address());
                                    ipRegister.setSuccess(false);
                                    ipRegister.setPlatform(profileTask.getPlatform().trim());
                                    ipRegister.setUpdate_time(System.currentTimeMillis());
                                    ipRegisterRepository.save(ipRegister);
                                }
                                profileTask.setRequest_index(1);
                                profileTaskRepository.save(profileTask);

                                //accountProfileRepository.update_SignIn_By_ProfileId(profileTask.getProfile_id().trim());// reset signin profile

                                resp.put("status", true);
                                data.put("platform", profileTask.getPlatform());
                                data.put("app",platform_Check.getApp_name().trim());
                                data.put("task", "register");
                                data.put("task_key", accountProfile_Dependent.getAccount_id().substring(0,accountProfile_Dependent.getAccount_id().lastIndexOf("|")));
                                data.put("account_id",  accountProfile_Dependent.getAccount_id().substring(0,accountProfile_Dependent.getAccount_id().lastIndexOf("|")));
                                data.put("password",accountProfile_Dependent.getPassword().trim());
                                data.put("recover_mail", accountProfile_Dependent.getAccount_id().substring(0,accountProfile_Dependent.getAccount_id().lastIndexOf("|")));
                                data.put("recover_mail_password", accountProfile_Dependent.getRecover_password().trim());
                                data.put("name",  accountProfile.getName());
                                data.put("avatar", accountProfile.getAvatar()==0?false:true);
                                data.put("auth_2fa", "");
                                resp.put("data",data);
                                return new ResponseEntity<>(resp, HttpStatus.OK);

                            }
                        }else if(platform_Check.getLogin_account()==1&&
                                (System.currentTimeMillis()-profileTask.getGet_account_time())/1000/60>=30&&
                                accountRepository.check_Count_AccountDie24H_By_Platform_And_DeviceId(device.getDevice_id().trim(),profileTask.getPlatform().trim())==0&&
                                accountProfileRepository.count_Login_By_Platform_And_DeviceId(profileTask.getPlatform().trim(),device.getDevice_id().trim()+"%",platform_Check.getLogin_time())==0&&
                                accountProfileRepository.count_Login_Time_Null_By_Platform_And_DeviceId(profileTask.getPlatform().trim(),device.getDevice_id().trim()+"%")==0
                        ){

                            profileTask.setGet_account_time(System.currentTimeMillis());
                            profileTaskRepository.save(profileTask);

                            String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                            String code="";
                            for(int i=0;i<10;i++){
                                Integer ranver=ran.nextInt(stringrand.length());
                                code=code+stringrand.charAt(ranver);
                            }

                            Account account_get= accountRepository.get_Account_Platform_By_ProfileId(device_id.trim()+"_"+profile_id.trim(),device_id,System.currentTimeMillis(),code,profileTask.getPlatform().trim(),device.getMode().trim());
                            if(account_get!=null){


                                AccountProfile accountCheck=accountProfileRepository.get_Account_By_Account_id_And_Platform(account_get.getAccount_id(),profileTask.getPlatform());
                                if(accountCheck!=null){
                                    accountProfileRepository.delete(accountCheck);
                                }

                                AccountProfile accountProfile=new AccountProfile();
                                accountProfile.setAccount_id(account_get.getAccount_id());
                                accountProfile.setPassword(account_get.getPassword());
                                accountProfile.setRecover(account_get.getRecover_mail());
                                accountProfile.setRecover_password(account_get.getRecover_mail_password());
                                accountProfile.setPlatform(profileTask.getPlatform().trim());
                                accountProfile.setLive(0);
                                accountProfile.setChanged(account_get.getChanged());
                                accountProfile.setAvatar(account_get.getAvatar());
                                accountProfile.setRunning(0);
                                accountProfile.setSign_in(1);
                                accountProfile.setCode(code);
                                accountProfile.setTask_time(System.currentTimeMillis());
                                accountProfile.setConnection_platform("");
                                accountProfile.setAuth_2fa(account_get.getAuth_2fa());
                                accountProfile.setProfileTask(profileTask);
                                accountProfile.setAdd_time(System.currentTimeMillis());
                                accountProfile.setUpdate_time(0L);
                                accountProfileRepository.save(accountProfile);

                                //accountProfileRepository.update_SignIn_By_ProfileId(profileTask.getProfile_id().trim());// reset signin profile

                                resp.put("status", true);
                                data.put("platform", profileTask.getPlatform().trim());
                                data.put("app",platform_Check.getApp_name().trim());
                                data.put("task", "sign_in");
                                data.put("task_key", account_get.getAccount_id().substring(0,account_get.getAccount_id().lastIndexOf("|")));
                                data.put("account_id",account_get.getAccount_id().substring(0,account_get.getAccount_id().lastIndexOf("|")));
                                data.put("password", account_get.getPassword().trim());
                                data.put("name", account_get.getName().trim());
                                data.put("avatar", accountProfile.getAvatar()==0?false:true);
                                data.put("recover_mail", account_get.getRecover_mail().trim());
                                data.put("recover_mail_password", account_get.getRecover_mail_password().trim());
                                data.put("auth_2fa", account_get.getAuth_2fa().trim());
                                data.put("account_list",accountProfileRepository.get_List_Account_Youtube_By_ProfileId(profileTask.getProfile_id().trim()));
                                resp.put("data",data);
                                return new ResponseEntity<>(resp, HttpStatus.OK);
                            }
                        }

                    }else if((System.currentTimeMillis()-accountProfile_Live0.getLast_time())/1000/60/60>=6&&
                            accountProfile_Live0.getLive()==-1){//check last time task register

                        if(!(platformRepository.get_Register_Account_Platform_And_Mode(profileTask.getPlatform(),device.getMode())==0&&accountProfile_Live0.getLive()==-1)){
                            accountProfile_Live0.setLast_time(System.currentTimeMillis());
                            accountProfileRepository.save(accountProfile_Live0);

                            resp.put("status", true);
                            data.put("platform",profileTask.getPlatform());
                            data.put("app",platform_Check.getApp_name().trim());
                            data.put("task", "register");
                            data.put("task_key", accountProfile_Live0.getAccount_id().substring(0,accountProfile_Live0.getAccount_id().lastIndexOf("|")));
                            data.put("account_id", accountProfile_Live0.getAccount_id().substring(0,accountProfile_Live0.getAccount_id().lastIndexOf("|")));
                            data.put("password", accountProfile_Live0.getPassword().trim());
                            data.put("name", accountProfile_Live0.getName().trim());
                            data.put("avatar", accountProfile_Live0.getAvatar()==0?false:true);
                            data.put("recover_mail", accountProfile_Live0.getRecover().substring(0,accountProfile_Live0.getRecover().lastIndexOf("|")));
                            data.put("recover_mail_password", accountProfile_Live0.getRecover_password().trim());
                            data.put("auth_2fa", accountProfile_Live0.getAuth_2fa().trim());
                            data.put("account_list",accountProfileRepository.get_List_Account_Youtube_By_ProfileId(profileTask.getProfile_id().trim()));
                            resp.put("data",data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }

                    }
                }
            }

            if(profileTask==null){
                resp.put("status", false);
                data.put("message", "Không thực hiện nhiệm vụ");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }else{
                if( accountProfileRepository.check_Count_AccountLive_By_ProfileId_And_Platform(profileTask.getProfile_id(),profileTask.getPlatform())==0 &&!profileTask.getPlatform().equals("tiktok")){

                    String task_List = profileTask.getTask_list();
                    List<String> arrPlatform = new ArrayList<>(Arrays.asList(task_List.split(",")));
                    profileTask.setPlatform(arrPlatform.get(0));
                    List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                    task_List = String.join(",", subPlatform);
                    profileTask.setTask_list(task_List);
                    profileTask.setRequest_index(0);
                    profileTaskRepository.save(profileTask);
                    resp.put("status", false);
                    data.put("message", "Không thực hiện nhiệm vụ");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }

            AccountProfile accountProfile_Task=accountProfileRepository.get_Account_By_Platform_And_ProfileId(profileTask.getProfile_id(),profileTask.getPlatform());
            if((accountProfile_Task==null)&&profileTask.getPlatform().equals("tiktok")){
                profileTask.setAccount_id(profileTask.getProfile_id().trim());
                profileTask.setTask_index(profileTask.getTask_index()+1);
                profileTaskRepository.save(profileTask);
            }else if ((accountProfile_Task.getLive() == 0) && (System.currentTimeMillis() - accountProfile_Task.getLast_time()) / 1000 / 60 / 60 >= 6) {

                accountProfile_Task.setLast_time(System.currentTimeMillis());
                accountProfileRepository.save(accountProfile_Task);

                //Platform platform_Check=platformRepository.get_Platform_By_Platform_And_Mode(profileTask.getPlatform().trim(),device.getMode().trim());
                resp.put("status", true);
                data.put("platform", profileTask.getPlatform());
                data.put("app", platform_Check.getApp_name().trim());
                if (accountProfile_Task.getLive() == 0 && accountProfile_Task.getSign_in() == 1) {
                    data.put("task", "sign_in");
                } else {
                    data.put("task", "login");
                }
                data.put("task_key", accountProfile_Task.getAccount_id().substring(0, accountProfile_Task.getAccount_id().lastIndexOf("|")));
                data.put("account_id", accountProfile_Task.getAccount_id().substring(0, accountProfile_Task.getAccount_id().lastIndexOf("|")));
                data.put("password", accountProfile_Task.getPassword().trim());
                data.put("name", accountProfile_Task.getName().trim());
                data.put("avatar", accountProfile_Task.getAvatar() == 0 ? false : true);
                if(accountProfile_Task.getSign_in() == 0){
                    if (accountProfile_Task.getRecover().trim().contains("|")) {
                        data.put("recover_mail", accountProfile_Task.getRecover().trim().substring(0, accountProfile_Task.getRecover().trim().lastIndexOf("|")));
                    } else {
                        AccountProfile accountProfile_Dependent = accountProfileRepository.get_Account_By_ProfileId_And_Platform(profileTask.getProfile_id(), platform_Check.getDependent().trim());
                        accountProfile_Task.setRecover(accountProfile_Dependent.getAccount_id());
                        String stringrand = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                        String code = "";
                        for (int i = 0; i < 10; i++) {
                            Integer ranver = ran.nextInt(stringrand.length());
                            code = code + stringrand.charAt(ranver);
                        }
                        accountProfile_Task.setCode(code);
                        accountProfileRepository.save(accountProfile_Task);
                        if (!accountProfile_Dependent.getConnection_platform().contains(profileTask.getPlatform())) {
                            accountProfile_Dependent.setConnection_platform(profileTask.getPlatform() + "|");
                        }
                        accountProfile_Dependent.setCode(code);
                        accountProfileRepository.save(accountProfile_Dependent);
                        data.put("recover_mail", accountProfile_Task.getRecover().trim().substring(0, accountProfile_Task.getRecover().trim().lastIndexOf("|")));
                    }
                }else{
                    if (accountProfile_Task.getRecover().trim().contains("|")) {
                        data.put("recover_mail", accountProfile_Task.getRecover().trim().substring(0, accountProfile_Task.getRecover().trim().lastIndexOf("|")));
                    }else{
                        data.put("recover_mail", accountProfile_Task.getRecover().trim());
                    }
                }
                data.put("recover_mail_password", accountProfile_Task.getRecover_password().trim());
                data.put("auth_2fa", accountProfile_Task.getAuth_2fa().trim());
                data.put("account_list", accountProfileRepository.get_List_Account_Youtube_By_ProfileId(profileTask.getProfile_id().trim()));
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            } else {
                profileTask.setAccount_id(accountProfile_Task.getAccount_id().trim());
                profileTask.setTask_index(profileTask.getTask_index() + 1);
                profileTaskRepository.save(profileTask);
            }

            if(profileTask.getPlatform().equals("tiktok")&&accountProfile_Task!=null&&accountProfile_Task.getLive()==1){
                if(accountProfile_Task.getAvatar()==0 && accountProfile_Task.getLogin_time()!=0 && platform_Check.getChange_info()==1 &&
                        (System.currentTimeMillis()-accountProfile_Task.getLogin_time())/1000/60/60/24>=platform_Check.getChanger_time()&&
                        (System.currentTimeMillis()-accountProfile_Task.getChanged_time())/1000/60/60>=6){ // add_recovery_mail
                    accountProfile_Task.setChanged_time(System.currentTimeMillis());
                    accountProfileRepository.save(accountProfile_Task);
                    resp.put("status", true);
                    data.put("platform", profileTask.getPlatform().trim());
                    data.put("task", "update_avatar");
                    data.put("task_key", accountProfile_Task.getAccount_id().substring(0,accountProfile_Task.getAccount_id().lastIndexOf("|")));
                    data.put("account_id", accountProfile_Task.getAccount_id().substring(0,accountProfile_Task.getAccount_id().lastIndexOf("|")));
                    //data.put("task_link", accountClone.getAvatar_link());
                    data.put("task_link", "http://api.idnetwork.com.vn/image/random?geo=Us");
                    data.put("app", platform_Check.getApp_name().trim());
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                    /*
                    else if(true==false){
                        resp.put("status", true);
                        data.put("platform", profileTask.getPlatform().trim());
                        data.put("task", "add_recovery_mail");
                        data.put("task_key", accountProfile_Task.getAccount_id().substring(0,accountProfile_Task.getAccount_id().lastIndexOf("|")));
                        data.put("account_id", accountProfile_Task.getAccount_id().substring(0,accountProfile_Task.getAccount_id().lastIndexOf("|")));
                        data.put("password", accountProfile_Task.getPassword().trim());
                        data.put("recover_mail", accountProfile_Task.getRecover().trim());
                        data.put("recover_mail_password", accountProfile_Task.getRecover_password().trim());
                        data.put("app", platform_Check.getApp_name().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                     */
                }else if(accountProfile_Task.getChanged()==0 && accountProfile_Task.getAccount_id().startsWith("@user") && accountProfile_Task.getLogin_time()!=0 && platform_Check.getChange_info()==1 &&
                        (System.currentTimeMillis()-accountProfile_Task.getLogin_time())/1000/60/60/24>=platform_Check.getChanger_time()&&
                        (System.currentTimeMillis()-accountProfile_Task.getChanged_time())/1000/60/60>=6){ // add_recovery_mail
                    accountProfile_Task.setChanged_time(System.currentTimeMillis());
                    accountProfileRepository.save(accountProfile_Task);
                    String username=Openai.IdTiktok(StringUtils.getName(),openAiKeyRepository.get_OpenAI_Key());
                    if(username!=null && username.trim().length()<=24){
                        if(!TikTokApi.checkLive(username)){
                            String name=Openai.nameTiktok(username.trim(),openAiKeyRepository.get_OpenAI_Key());
                            if(name!=null && name.trim().length()<=30){
                                resp.put("status", true);
                                data.put("platform", profileTask.getPlatform().trim());
                                data.put("task", "update_username");
                                data.put("task_key","@"+username.trim());
                                data.put("account_id", accountProfile_Task.getAccount_id().substring(0,accountProfile_Task.getAccount_id().lastIndexOf("|")));
                                data.put("name", name.trim());
                                data.put("task_link", "http://api.idnetwork.com.vn/image/random?geo=Us");
                                data.put("app", platform_Check.getApp_name().trim());
                                resp.put("data",data);
                                return new ResponseEntity<>(resp, HttpStatus.OK);
                            }else{
                                resp.put("status",false);
                                data.put("message","Không thực hiện nhiệm vụ!");
                                resp.put("data",data);
                                return new ResponseEntity<>(resp, HttpStatus.OK);
                            }
                        }
                    }else{
                        resp.put("status",false);
                        data.put("message","Không thực hiện nhiệm vụ!");
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }
                if(platform_Check.getClone_info()==1 &&  platform_Check.getAdd_post()==1 && accountProfile_Task.getLogin_time()!=0 &&
                        (System.currentTimeMillis()-accountProfile_Task.getLogin_time())/1000/60/60/24>=platform_Check.getAdd_post_time()&&
                        (System.currentTimeMillis()-accountProfile_Task.getChanged_time())/1000/60/60>=6
                ){
                    accountProfile_Task.setChanged_time(System.currentTimeMillis());
                    accountProfileRepository.save(accountProfile_Task);
                    AccountClone accountClone=accountCloneRepository.get_Account_Clone_By_Account_id(accountProfile_Task.getAccount_id().trim());
                    if(accountClone!=null){
                        if((System.currentTimeMillis()-accountClone.getUpdate_time())/1000/60/60/24>=platform_Check.getAdd_post_time()){
                            JsonArray videoList=TikTokApi.getInfoVideoByChannelByUserId(accountClone.getId_clone(),100);
                            if(videoList!=null){ //null bỏ qua
                                if(videoList.size()==0){ // size=0 acc die
                                    accountCloneRepository.delete(accountClone);
                                }else{
                                    for (JsonElement video: videoList) {
                                        JsonObject videoObj=video.getAsJsonObject();
                                        if(!videoObj.get("play").getAsString().contains("video")){ //photo thì bỏ qua
                                            continue;
                                        }
                                        if(!accountClone.getVideo_list().contains(videoObj.get("video_id").getAsString())){
                                            resp.put("status", true);
                                            data.put("platform", profileTask.getPlatform().trim());
                                            data.put("task", "add_post");
                                            data.put("account_id", accountProfile_Task.getAccount_id().substring(0,accountProfile_Task.getAccount_id().lastIndexOf("|")));
                                            data.put("task_key", videoObj.get("video_id").getAsString());
                                            data.put("task_link", videoObj.get("play").getAsString());
                                            data.put("app", platform_Check.getApp_name().trim());
                                            resp.put("data",data);
                                            return new ResponseEntity<>(resp, HttpStatus.OK);
                                        }
                                    }
                                    if(videoList!=null){ //qua for mà chạy đến đây chứng tỏ hết acc
                                        accountCloneRepository.delete(accountClone);
                                    }
                                }
                            } //pass
                        }
                    }
                }
            }

            List<ModeOption> priorityTasks =modeOptionRepository.get_Priority_Task_By_Platform_And_Mode(profileTask.getPlatform(),device.getMode());
            List<String> arrTask = new ArrayList<>();

            for(int i=0;i<priorityTasks.size();i++){
                for (int j = 0; j < priorityTasks.get(i).getPriority(); j++) {
                    arrTask.add(priorityTasks.get(i).getTask());
                }
            }
            /*
            if(profileTask.getPlatform().equals("tiktok")&&ipTask24hRepository.count_Task_Hour_By_Ip(device.getIp_address()+"%",4)>settingSystem.getIp_task_24h()){ // reboot đổi ip
                profileTaskRepository.reset_Reboot_By_ProfileId(profileTask.getProfile_id().trim());
                resp.put("status", true);
                data.put("platform", "system");
                data.put("task", "reboot");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
             */
            Map<String, Object> get_task =null;
            String task_index=null;
            while (arrTask.size()>0){
                String task = arrTask.get(ran.nextInt(arrTask.size())).trim();
                ModeOption modeOption=modeOptionRepository.get_Mode_Option(device.getMode().trim(),profileTask.getPlatform().trim(),task);
                AccountTask accountTask=accountTaskRepository.get_Acount_Task_By_AccountId(profileTask.getAccount_id());
                Long get_time=0L;
                if(accountTask!=null){
                    if(task.trim().contains("follower")){
                        get_time=accountTask.getFollower_time();
                    }else if(task.trim().contains("subscriber")){
                        get_time=accountTask.getSubscriber_time();
                    }else if(task.trim().contains("view")){
                        get_time=accountTask.getView_time();
                    }else if(task.trim().contains("like")){
                        get_time=accountTask.getLike_time();
                    }else if(task.trim().contains("comment")){
                        get_time=accountTask.getComment_time();
                    }else if(task.trim().contains("repost")){
                        get_time=accountTask.getRepost_time();
                    }else if(task.trim().contains("member")){
                        get_time=accountTask.getMember_time();
                    }else if(task.trim().contains("share")){
                        get_time=accountTask.getShare_time();
                    }else if(task.trim().contains("favorites")){
                        get_time=accountTask.getFavorites_time();
                    }
                }
                if(modeOption==null){
                    resp.put("status",false);
                    data.put("message","modeOption không hợp lệ!");
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else if((System.currentTimeMillis()-get_time)/1000/60<modeOption.getTime_get_task()){
                    while(arrTask.remove(task)) {}
                    continue;
                }
                while(arrTask.remove(task)) {}
                if(profileTask.getPlatform().equals("tiktok") && (accountProfile_Task==null || accountProfile_Task.getLive()<1)){
                    if(task.equals("view")){
                        get_task=tiktokTask.tiktok_view(profileTask.getAccount_id(),device.getMode().trim(),device,0);
                    }
                } else if(profileTask.getPlatform().equals("tiktok")){
                    if(task.equals("follower") && accountProfile_Task.getSign_in()==1){
                        get_task= tiktokTask.tiktok_follower(profileTask.getAccount_id(),device.getMode().trim(),device.getIp_address(),device);
                    }else if(task.equals("like")){
                        get_task=tiktokTask.tiktok_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("view")){
                        get_task=tiktokTask.tiktok_view(profileTask.getAccount_id(),device.getMode().trim(),device, accountProfile_Task.getLive());
                    }else if(task.equals("comment")){
                        get_task=tiktokTask.tiktok_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("share")){
                        get_task=tiktokTask.tiktok_share(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("favorites")){
                        get_task=tiktokTask.tiktok_favorites(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("youtube")){
                    if(task.equals("view")){
                        get_task=youtubeTask.youtube_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("like")){
                        get_task=youtubeTask.youtube_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("subscriber")){
                        get_task=youtubeTask.youtube_subscriber(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("comment")){
                        get_task=youtubeTask.youtube_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("facebook")){
                    if(task.equals("follower")){
                        get_task=facebookTask.facebook_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("like")){
                        get_task=facebookTask.facebook_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("view")){
                        get_task=facebookTask.facebook_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("comment")){
                        get_task=facebookTask.facebook_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("member")){
                        get_task=facebookTask.facebook_member(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("x")){
                    if(task.equals("follower")){
                        get_task=xTask.x_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("like")){
                        get_task=xTask.x_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("view")){
                        get_task=xTask.x_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("comment")){
                        get_task=xTask.x_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("repost")){
                        get_task=xTask.x_repost(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("instagram")){
                    if(task.equals("instagram_follower")){
                        get_task=instagramTask.instagram_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("like")){
                        get_task=instagramTask.instagram_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("view")){
                        get_task=instagramTask.instagram_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("comment")){
                        get_task=instagramTask.instagram_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("threads")){
                    if(task.equals("follower")){
                        get_task=threadsTask.threads_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("like")){
                        get_task=threadsTask.threads_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("view")){
                        get_task=threadsTask.threads_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("comment")){
                        get_task=threadsTask.threads_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("repost")){
                        get_task=threadsTask.threads_repost(profileTask.getAccount_id(),device.getMode().trim());
                    }
                }
                if(get_task!=null?get_task.get("status").equals(true):false){
                    task_index=task;
                    break;
                }
            }
            if(get_task==null){
                resp.put("status",false);
                data.put("message","Không có nhiệm vụ!");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            Map<String, Object> respJson=new LinkedHashMap<>();
            Map<String, Object> dataJson=new LinkedHashMap<>();
            if(get_task.get("status").equals(true)){

                dataJson= (Map<String, Object>) get_task.get("data");

                Thread.sleep(300+ran.nextInt(500));
                if(!OrderThreadSpeedUpCheck.getValue().contains(dataJson.get("order_id").toString())&&!dataJson.get("order_id").toString().equals("-1")){
                    resp.put("status",false);
                    data.put("message","Không có nhiệm vụ!");
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
                List<String> ip_List=deviceRepository.get_IP_Running_Task_By_OrderId(Long.parseLong(dataJson.get("order_id").toString()));
                if(ip_List.size()!=0){
                    Set<String> ipSet = new HashSet<>(ip_List);
                    if(ipSet.contains(device.getIp_address())){
                        resp.put("status",false);
                        data.put("message","Không có nhiệm vụ!");
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }
                if(profileTask.getPlatform().equals("tiktok")&&profileTask.getProfile_id().equals(profileTask.getAccount_id())){
                    dataJson.put("account_id",profileTask.getAccount_id());
                }else{
                    dataJson.put("name",accountProfileRepository.get_Name_By_AccountId(profileTask.getAccount_id()));
                    dataJson.put("avatar",accountProfileRepository.get_Avatar_By_AccountId(profileTask.getAccount_id())==0?false:true);
                    dataJson.put("account_id",dataJson.get("account_id").toString().substring(0,dataJson.get("account_id").toString().indexOf("|")));
                    if(accountProfile_Task.getSign_in() == 0){
                        if(accountProfile_Task.getRecover().trim().contains("|")){
                            dataJson.put("recover_mail",accountProfile_Task.getRecover().trim().substring(0,accountProfile_Task.getRecover().trim().indexOf("|")));
                        }else{
                            //Platform platform_Check=platformRepository.get_Platform_By_Platform_And_Mode(profileTask.getPlatform().trim(),device.getMode().trim());
                            AccountProfile accountProfile_Dependent=accountProfileRepository.get_Account_By_ProfileId_And_Platform(profileTask.getProfile_id(),platform_Check.getDependent().trim());
                            accountProfile_Task.setRecover(accountProfile_Dependent.getAccount_id());
                            String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                            String code="";
                            for(int i=0;i<10;i++){
                                Integer ranver=ran.nextInt(stringrand.length());
                                code=code+stringrand.charAt(ranver);
                            }
                            accountProfile_Task.setCode(code);
                            accountProfileRepository.save(accountProfile_Task);
                            if(!accountProfile_Dependent.getConnection_platform().contains(profileTask.getPlatform())){
                                accountProfile_Dependent.setConnection_platform(profileTask.getPlatform()+"|");
                            }
                            accountProfile_Dependent.setCode(code);
                            accountProfileRepository.save(accountProfile_Dependent);
                            dataJson.put("recover_mail",accountProfile_Task.getRecover().trim().substring(0,accountProfile_Task.getRecover().trim().indexOf("|")));
                        }
                    }else{
                        if(accountProfile_Task.getRecover().trim().contains("|")){
                            dataJson.put("recover_mail",accountProfile_Task.getRecover().trim().substring(0,accountProfile_Task.getRecover().trim().indexOf("|")));
                        }else{
                            dataJson.put("recover_mail",accountProfile_Task.getRecover().trim());
                        }
                    }

                }
                dataJson.put("task_index",profileTask.getTask_index());
                Long version_app=platformRepository.get_Version_App_Platform_And_Mode(dataJson.get("platform").toString(),device.getMode());
                dataJson.put("version_app",version_app==null?0:version_app);
                Integer platform_task=platformRepository.get_Activity_Platform_And_Mode(dataJson.get("platform").toString(),device.getMode());
                dataJson.put("activity",platform_task==0?false:true);
                respJson.put("status",true);
                respJson.put("data",dataJson);
                //--------------------------------------------//
                profileTask.setGet_time(System.currentTimeMillis());
                profileTask.setOrder_id(Long.parseLong(dataJson.get("order_id").toString()));
                profileTask.setRunning(1);
                profileTask.setTask(dataJson.get("task").toString());
                profileTask.setPlatform(dataJson.get("platform").toString());
                profileTask.setTask_key(dataJson.get("task_key").toString());
                //save get task time 20/12/24
                profileTask.setTask_time(System.currentTimeMillis());
                profileTaskRepository.save(profileTask);
                device.setTask_time(System.currentTimeMillis());
                //device.setProfile_running(profile_id);
                device.setTask(dataJson.get("task").toString());
                device.setPlatform(dataJson.get("platform").toString());
                device.setRunning(1);
                deviceRepository.save(device);
                //--------------------------------------------//
                //dataJson.remove("order_id"); // trả về order_id => 17/3/2025



            }else{
                respJson.put("status",false);
                dataJson.put("message","Không có nhiệm vụ!");
                respJson.put("data",dataJson);
                profileTask.setRunning(0);
                profileTask.setGet_time(System.currentTimeMillis());
                profileTaskRepository.save(profileTask);
            }
            return new ResponseEntity<>(respJson, HttpStatus.OK);
        }catch (Exception e){
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            LogError logError =new LogError();
            logError.setMethod_name(stackTraceElement.getMethodName());
            logError.setLine_number(stackTraceElement.getLineNumber());
            logError.setClass_name(stackTraceElement.getClassName());
            logError.setFile_name(stackTraceElement.getFileName() +device_id+"_"+profile_id);
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

    @GetMapping(value = "getTaskOFF", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> getTaskOFF(@RequestHeader(defaultValue = "") String Authorization,
                                                       @RequestParam(defaultValue = "") String device_id,
                                                       @RequestParam(defaultValue = "") Long tiktok_lite_version,
                                                       @RequestParam(defaultValue = "") String profile_id
    ) throws InterruptedException {

        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran=new Random();
            Thread.sleep(ran.nextInt(1500));
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
            if (checktoken ==0) {
                resp.put("status", false);
                data.put("message", "Token expired");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }else if (device_id.length()==0) {
                resp.put("status", false);
                data.put("message", "device_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }else if (profile_id.length()==0) {
                resp.put("status", false);
                data.put("message", "profile_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //---------------------get_Account------------------//
            SettingSystem settingSystem=settingSystemRepository.get_Setting_System();
            Device device=deviceRepository.check_DeviceId(device_id.trim());
            ProfileTask profileTask=null;
            if(device==null) {
                resp.put("status", false);
                data.put("message", "device_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            Mode mode =modeRepository.get_Mode_Info(device.getMode().trim());
            if(mode==null){
                resp.put("status", false);
                data.put("message", "mode không hợp lệ");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }

            if((System.currentTimeMillis()-device.getUpdate_time())/1000<mode.getTime_waiting_task()&&!device.getMode().contains("dev")&&!profile_id.trim().equals("0")){
                resp.put("status", false);
                data.put("message", "Không thực hiện nhiệm vụ");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }else if(device.getState()==0){
                device.setUpdate_time(System.currentTimeMillis());
                deviceRepository.save(device);
                resp.put("status", false);
                data.put("message", "device_id không làm nhiệm vụ");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }else if(!device.getApp_version().trim().equals(settingSystem.getApp_version().trim())){

                String ver_Device = device.getApp_version().trim().replaceAll("\\.", ""); // Xóa dấu chấm
                String ver_System = settingSystem.getApp_version().trim().replaceAll("\\.", ""); // Xóa dấu chấm
                if(Long.parseLong(ver_Device.length()==0?"0":ver_Device)<Long.parseLong(ver_System)){
                    device.setUpdate_time(System.currentTimeMillis());
                    deviceRepository.save(device);
                    resp.put("status", false);
                    data.put("message", "device_id chưa cập nhật version auto");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }

            }
            if(device.getReboot()==1 || (System.currentTimeMillis()-device.getReboot_time())/1000/60>=settingSystem.getReboot_time() ){
                device.setReboot(0);
                device.setUpdate_time(System.currentTimeMillis());
                device.setReboot_time(System.currentTimeMillis());
                deviceRepository.save(device);
                resp.put("status", true);
                data.put("platform", "system");
                data.put("task", "reboot");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }else if(device.getMode().trim().length()==0){
                device.setUpdate_time(System.currentTimeMillis());
                deviceRepository.save(device);
                resp.put("status", false);
                data.put("message", "mode hiện tại trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            device.setNum_profile_set(mode.getMax_profile());
            device.setUpdate_time(System.currentTimeMillis());
            deviceRepository.save(device);



            if((device.getNum_profile()<mode.getMax_profile() || profileTaskRepository.get_Count_Profile_Valid_0_By_DeviceId(device_id.trim())>0 )&&!profile_id.trim().equals("0")){
                resp.put("status", true);
                data.put("platform", "system");
                data.put("task", "profile_changer");
                data.put("profile_id",0);
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
            if(profileTask==null&&!profile_id.trim().equals("0")){
                resp.put("status", false);
                data.put("message", "profile_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }else if(profile_id.trim().equals("0")){
                if(profileTaskRepository.get_Count_Profile_Valid_1_By_DeviceId(device_id.trim())<mode.getMax_profile()){ //device.getNum_profile()<mode.getMax_profile()
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "create_profile");
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
                if(profileTaskRepository.get_Count_Profile_Valid_0_By_DeviceId(device_id.trim())>0){
                    String profile_remove=profileTaskRepository.get_ProfileId_Valid_0_By_DeviceId(device_id.trim());
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "remove_profile");
                    data.put("profile_id",Integer.parseInt(profile_remove.split(device_id.trim()+"_")[1]));
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
                profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim(),profileTask==null?"":profileTask.getProfile_id());
                if(profileTask!=null){
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "profile_changer");
                    data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else{
                    resp.put("status", false);
                    data.put("message", "Không có profile để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }else if(profileTask!=null){
                if(profileTask.getEnabled()==0){
                    profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim(),profileTask.getProfile_id());
                    if(profileTask!=null){
                        if(mode.getProfile_reboot()){
                            profileTask.setReboot(1);
                            profileTaskRepository.save(profileTask);
                        }
                        device.setProfile_running(profileTask.getProfile_id().split(device_id.trim()+"_")[1]);
                        deviceRepository.save(device);
                        resp.put("status", true);
                        data.put("platform", "system");
                        data.put("task", "profile_changer");
                        data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }else{
                        resp.put("status", false);
                        data.put("message", "Không thực hiện nhiệm vụ");
                        resp.put("data", data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }
            } else if(profileTask==null){
                resp.put("status", false);
                data.put("message", "profile_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //check reboot profile
            if(profileTask.getReboot()==1){
                profileTaskRepository.reset_Reboot_By_ProfileId(profileTask.getProfile_id().trim());
                resp.put("status", true);
                data.put("platform", "system");
                data.put("task", "reboot");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            //check update PI
            if(profileTask.getUpdate_pi()==1){
                resp.put("status", true);
                data.put("platform", "system");
                data.put("task", "update_pi");
                data.put("task_key", "fingerprint");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            //check clear data
            if(profileTask.getClear_data()==1){
                accountProfileRepository.update_Live_Tiktok_By_ProfileId(profileTask.getProfile_id().trim());
                resp.put("status", true);
                data.put("platform", "system");
                data.put("task", "clear_data");
                data.put("task_key", settingSystem.getClear_data_package().trim());
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            /*
            if(profileTask.getState()==0&&profileTask.getAdd_proxy()==1){
                profileTask.setAdd_proxy(0);
                profileTaskRepository.save(profileTask);
            }

             */


            if(device.getProfile_running().length()!=0 && !device.getProfile_running().equals(profile_id.trim()) &&!mode.getMode().contains("dev")&&
                    profileTaskRepository.check_Profile_Exist(device_id.trim()+"_"+device.getProfile_running().trim())>0){
                resp.put("status", true);
                data.put("platform", "system");
                data.put("task", "profile_changer");
                data.put("profile_id", Integer.parseInt(device.getProfile_running()));
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            //changer profile  khi du thoi gian hoạt động
            if((System.currentTimeMillis()-profileTask.getOnline_time())/1000/60>=mode.getTime_profile() && profileTask.getState()==1 &&!mode.getMode().contains("dev")) {
                //accountProfileRepository.update_Running_By_ProfileId(profileTask.getProfile_id().trim());
                if (profileTaskRepository.get_Count_Profile_Enabled(device_id.trim()) > 1) {
                    profileTaskRepository.reset_Thread_Index_By_DeviceId_While_ChangerProfile(device_id.trim());
                    entityManager.clear();
                    profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim(),profileTask.getProfile_id());
                    if(mode.getProfile_reboot()){
                        profileTask.setReboot(1);
                        profileTaskRepository.save(profileTask);
                    }
                    device.setProfile_running(profileTask.getProfile_id().split(device_id.trim()+"_")[1]);
                    deviceRepository.save(device);
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "profile_changer");
                    data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim() + "_")[1]));
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else{
                    profileTaskRepository.reset_Thread_Index_By_DeviceId_While_ChangerProfile_1_On(device_id.trim());
                    entityManager.clear();
                    profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                }
            }else if((System.currentTimeMillis()-profileTask.getOnline_time())/1000/60>=mode.getTime_profile() && profileTask.getState()==1 &&mode.getMode().contains("dev")) {
                profileTaskRepository.reset_Thread_Index_By_DeviceId_While_ChangerProfile_1_On(device_id.trim());
                entityManager.clear();
                profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
            }


            //update time check
            if(profileTask.getState()==0){ // check profile bắt đầu mở file
                profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                entityManager.clear();

                List<String> string_Task_List=platformRepository.get_All_Platform_True(device.getMode());
                String task_List=String.join(",", string_Task_List);
                List<String> arrPlatform=new ArrayList<>(Arrays.asList(task_List.split(",")));

                profileTask.setPlatform(arrPlatform.get(0));
                List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                task_List=String.join(",", subPlatform);
                profileTask.setTask_list(task_List);
                profileTask.setTiktok_lite_version(tiktok_lite_version);
                profileTask.setTask_index(0);
                profileTask.setTask_time(System.currentTimeMillis());
                profileTask.setState(1);
                profileTask.setRequest_index(0);
                profileTask.setOnline_time(System.currentTimeMillis());
                profileTask.setUpdate_time(System.currentTimeMillis());
                profileTaskRepository.save(profileTask);
                device.setProfile_time(System.currentTimeMillis());
                deviceRepository.save(device);
            }else{
                profileTask.setTiktok_lite_version(tiktok_lite_version);
                profileTask.setTask_time(System.currentTimeMillis());
                profileTask.setUpdate_time(System.currentTimeMillis());
                profileTaskRepository.save(profileTask);
            }


            //Task Proxy Sock5
           /*
            if(mode.getAdd_proxy()==1&&profileTask.getAdd_proxy()==0&&profileTask.getDis_proxy()==0){
                String proxy=ProxyAPI.getSock5(mode.getGeography().trim());
                if(proxy!=null){
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "add_proxy");
                    data.put("task_key",proxy);
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else {
                    resp.put("status", false);
                    data.put("message", "Không thực hiện nhiệm vụ");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }else if(mode.getAdd_proxy()==1&&profileTask.getAdd_proxy()==1&&profileTask.getDis_proxy()==1){
                resp.put("status", true);
                data.put("platform", "system");
                data.put("task", "disconnect_proxy");
                data.put("task_key",profileTask.getProxy());
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }

            if(mode.getAdd_proxy()==1&&
                    accountProfileRepository.get_Account_Tiktok_By_ProfileId(profileTask.getProfile_id().trim())>0&&
            profileTask.getAdd_proxy()==1){
                resp.put("status", true);
                data.put("platform", "system");
                data.put("task", "disconnect_proxy");
                data.put("task_key",profileTask.getProxy());
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            */

            if(mode.getAdd_proxy()==1&&profileTask.getAdd_proxy()==0){
                String[] geo={"th", "za", "kr", "jp", "id", "bd", "eg", "my"};
                String proxy=ProxyAPI.getSock5Luna("id");
                if(proxy!=null){
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "add_proxy");
                    data.put("task_key",proxy);
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else {
                    resp.put("status", false);
                    data.put("message", "Không thực hiện nhiệm vụ");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }

            //profileTask=profileTaskRepository.get_Profile_By_ProfileId(profileTask.getProfile_id());

            //check acc youtube
            Platform platform_Youtube_Check=platformRepository.get_Platform_By_Platform_And_Mode("youtube",device.getMode().trim());
            if(accountProfileRepository.check_Count_AccountLive1_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube")<platform_Youtube_Check.getMax_account()&&
                    platform_Youtube_Check.getLogin_account()==1){ //check xem đủ tài khoản youtbe live=1 chưa
                AccountProfile accountProfile_Live0=accountProfileRepository.get_AccountLive0_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube");
                if(accountProfile_Live0==null){ // If account null or not live then get new acc
                    if(platform_Youtube_Check.getLogin_account()==1&& (System.currentTimeMillis()-profileTask.getGet_account_time())/1000/60>=15&&
                            accountRepository.check_Count_AccountDie24H_By_Platform_And_ProfileId(profileTask.getProfile_id().trim(),"youtube")<3&& //check acc die in 24h
                            accountProfileRepository.count_Login_By_Platform_And_DeviceId("youtube",device.getDevice_id().trim()+"%",platform_Youtube_Check.getLogin_time())==0 // check time login gần nhất
                    ){
                        String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                        String code="";
                        for(int i=0;i<10;i++){
                            Integer ranver=ran.nextInt(stringrand.length());
                            code=code+stringrand.charAt(ranver);
                        }
                        //get account new
                        Account account_get= accountRepository.get_Account_Youtube_By_ProfileId(device_id.trim()+"_"+profile_id.trim(),device_id,System.currentTimeMillis(),code,device.getMode().trim());
                        if(account_get!=null){ // true

                            AccountProfile accountCheck=accountProfileRepository.get_Account_By_Account_id_And_Platform(account_get.getAccount_id(),"youtube");
                            if(accountCheck!=null){
                                accountProfileRepository.delete(accountCheck);
                            }
                            AccountProfile accountProfile=new AccountProfile();
                            accountProfile.setAccount_id(account_get.getAccount_id());
                            accountProfile.setPassword(account_get.getPassword());
                            accountProfile.setRecover(account_get.getRecover_mail());
                            if(account_get.getName().length()==0){
                                String name=StringUtils.getName();
                                accountProfile.setName(name);
                                account_get.setName(name);
                                accountRepository.save(account_get);
                            }
                            accountProfile.setName(account_get.getName());
                            accountProfile.setPlatform("youtube");
                            accountProfile.setLive(0);
                            accountProfile.setRunning(0);
                            accountProfile.setSign_in(0);
                            accountProfile.setCode(code);
                            accountProfile.setTask_time(System.currentTimeMillis());
                            accountProfile.setConnection_platform("");
                            accountProfile.setChanged(0);
                            accountProfile.setAuth_2fa(account_get.getAuth_2fa());
                            accountProfile.setProfileTask(profileTask);
                            accountProfile.setAdd_time(System.currentTimeMillis());
                            accountProfile.setUpdate_time(0L);
                            accountProfileRepository.save(accountProfile);

                            accountProfileRepository.update_SignIn_By_ProfileId(profileTask.getProfile_id().trim());// reset signin profile

                            profileTask.setGet_account_time(System.currentTimeMillis());
                            profileTaskRepository.save(profileTask);

                            resp.put("status", true);
                            data.put("platform", "youtube");
                            data.put("app", platform_Youtube_Check.getApp_name().trim());
                            data.put("task", "login");
                            data.put("task_key", account_get.getAccount_id().substring(0,account_get.getAccount_id().lastIndexOf("|")));
                            data.put("account_id",account_get.getAccount_id().substring(0,account_get.getAccount_id().lastIndexOf("|")));
                            data.put("password", account_get.getPassword().trim());
                            data.put("name", account_get.getName());
                            data.put("avatar", account_get.getAvatar()==0?false:true);
                            data.put("recover_mail", account_get.getRecover_mail().trim());
                            data.put("auth_2fa", account_get.getAuth_2fa().trim());
                            data.put("account_list",accountProfileRepository.get_List_Account_Youtube_By_ProfileId(profileTask.getProfile_id().trim()));
                            resp.put("data",data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }else{
                            profileTask.setGet_account_time(System.currentTimeMillis());
                            profileTaskRepository.save(profileTask);
                            resp.put("status", false);
                            data.put("message", "Không thực hiện nhiệm vụ");
                            resp.put("data", data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }

                    }else{
                        resp.put("status", false);
                        data.put("message", "Không thực hiện nhiệm vụ");
                        resp.put("data", data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }else if((System.currentTimeMillis()-accountProfile_Live0.getLast_time())/1000/60>=0){
                    accountProfile_Live0.setLast_time(System.currentTimeMillis());
                    accountProfileRepository.save(accountProfile_Live0);

                    accountProfileRepository.update_SignIn_By_ProfileId(profileTask.getProfile_id().trim());// reset signin profile

                    resp.put("status", true);
                    data.put("platform", "youtube");
                    data.put("app", platform_Youtube_Check.getApp_name().trim());
                    if(accountProfile_Live0.getAccount_id().contains("register")){
                        data.put("task", "register");
                    }else{
                        data.put("task", "login");
                    }
                    data.put("task_key", accountProfile_Live0.getAccount_id().substring(0,accountProfile_Live0.getAccount_id().lastIndexOf("|")));
                    data.put("account_id", accountProfile_Live0.getAccount_id().substring(0,accountProfile_Live0.getAccount_id().lastIndexOf("|")));
                    data.put("password", accountProfile_Live0.getPassword().trim());
                    data.put("name", accountProfile_Live0.getName());
                    data.put("avatar", accountProfile_Live0.getAvatar()==0?false:true);
                    data.put("recover_mail", accountProfile_Live0.getRecover().trim());
                    data.put("auth_2fa", accountProfile_Live0.getAuth_2fa().trim());
                    data.put("account_list",accountProfileRepository.get_List_Account_Youtube_By_ProfileId(profileTask.getProfile_id().trim()));
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }else if(platform_Youtube_Check.getRegister_account()==1&&platform_Youtube_Check.getState()==1&&profileTask.getRegister_index()==0) {
                AccountProfile accountProfile=accountProfileRepository.get_Account_By_Account_id_And_Platform("register_"+profileTask.getProfile_id()+"|youtube","youtube");
                if(historyRegisterRepository.count_Register_By_Platform_And_DeviceId("youtube",device.getDevice_id().trim()+"%",platform_Youtube_Check.getRegister_time())==0&&
                        accountRepository.check_Count_Register_LessDay_By_DeviceId_And_Platform(device.getDevice_id().trim(),"youtube",7)<platform_Youtube_Check.getRegister_limit()&&
                        accountRepository.check_Count_Register_LessDay_By_ProfileId_And_Platform(device.getDevice_id().trim(),"youtube",7)==0&&
                        accountProfileRepository.count_Register_Task_By_Platform_And_DeviceId("youtube",device.getDevice_id()+"%")==0&&
                        accountProfileRepository.count_Gmail_By_Platform_And_PrfoileId("youtube",profileTask.getProfile_id().trim(),7)==0&& // check 1 profile 1 gmail
                        accountProfile==null
                ){
                    String password="Cmc#";
                    String passrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                    for(int i=0;i<6;i++){
                        Integer ranver=ran.nextInt(passrand.length());
                        password=password+passrand.charAt(ranver);
                    }

                    accountProfile=new AccountProfile();
                    accountProfile.setAccount_id("register_"+profileTask.getProfile_id()+"|youtube");
                    accountProfile.setPassword(password);
                    accountProfile.setName("");
                    accountProfile.setAvatar(0);
                    accountProfile.setRunning(0);
                    accountProfile.setSign_in(0);
                    accountProfile.setRecover("");
                    accountProfile.setPlatform("youtube");
                    accountProfile.setLive(-1);
                    accountProfile.setChanged(0);
                    accountProfile.setAuth_2fa("");
                    accountProfile.setCode(password);
                    accountProfile.setTask_time(System.currentTimeMillis());
                    accountProfile.setConnection_platform("");
                    accountProfile.setProfileTask(profileTask);
                    accountProfile.setAdd_time(System.currentTimeMillis());
                    accountProfile.setUpdate_time(0L);
                    accountProfileRepository.save(accountProfile);

                    HistoryRegister historyRegister=new HistoryRegister();
                    historyRegister.setProfileTask(profileTask);
                    historyRegister.setPlatform("youtube");
                    historyRegister.setState(0);
                    historyRegister.setUpdate_time(System.currentTimeMillis());
                    historyRegisterRepository.save(historyRegister);

                    profileTask.setRegister_index(profileTask.getRegister_index()+1);
                    profileTask.setRequest_index(profileTask.getRequest_index()+1);
                    profileTaskRepository.save(profileTask);

                    accountProfileRepository.update_SignIn_By_ProfileId(profileTask.getProfile_id().trim());// reset signin profile


                    resp.put("status", true);
                    data.put("platform","youtube");
                    data.put("app",platform_Youtube_Check.getApp_name().trim());
                    data.put("task", "register");
                    data.put("task_key", accountProfile.getAccount_id().substring(0,accountProfile.getAccount_id().lastIndexOf("|")));
                    data.put("account_id",accountProfile.getAccount_id().substring(0,accountProfile.getAccount_id().lastIndexOf("|")));
                    data.put("password",accountProfile.getPassword());
                    data.put("name",accountProfile.getName());
                    data.put("avatar",accountProfile.getAvatar()==0?false:true);
                    data.put("recover_mail",  accountProfile.getRecover().trim());
                    data.put("auth_2fa", "");
                    data.put("account_list",accountProfileRepository.get_List_Account_Youtube_By_ProfileId(profileTask.getProfile_id().trim()));
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else if(accountProfile!=null){
                    if(accountProfile.getLive()!=-1){
                        Account account =accountRepository.get_Account_By_Account_id(accountProfile.getAccount_id());
                        accountRepository.delete(account);
                        accountProfileRepository.delete(accountProfile);
                    }else{

                        accountProfileRepository.update_SignIn_By_ProfileId(profileTask.getProfile_id().trim());// reset signin profile

                        resp.put("status", true);
                        data.put("platform","youtube");
                        data.put("app",platform_Youtube_Check.getApp_name().trim());
                        data.put("task", "register");
                        data.put("task_key", accountProfile.getAccount_id().substring(0,accountProfile.getAccount_id().lastIndexOf("|")));
                        data.put("account_id",accountProfile.getAccount_id().substring(0,accountProfile.getAccount_id().lastIndexOf("|")));
                        data.put("password",accountProfile.getPassword());
                        data.put("name",accountProfile.getName());
                        data.put("avatar",accountProfile.getAvatar()==0?false:true);
                        data.put("recover_mail",  accountProfile.getRecover().trim());
                        data.put("auth_2fa", "");
                        data.put("account_list",accountProfileRepository.get_List_Account_Youtube_By_ProfileId(profileTask.getProfile_id().trim()));
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }
            }


            if(profileTask.getGoogle_time()==0){
                profileTask.setGoogle_time(System.currentTimeMillis());
            }


            // check tài khoản các nền tảng khác
            if(profileTask.getPlatform().length()==0){
                profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                entityManager.clear();

                List<String> string_Task_List=platformRepository.get_All_Platform_True(device.getMode());
                String task_List=String.join(",", string_Task_List);
                List<String> arrPlatform=new ArrayList<>(Arrays.asList(task_List.split(",")));

                profileTask.setPlatform(arrPlatform.get(0));
                List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                task_List=String.join(",", subPlatform);
                profileTask.setTask_list(task_List);
                profileTask.setTask_index(0);
                profileTask.setState(1);
                profileTask.setRequest_index(0);
                profileTaskRepository.save(profileTask);
            }
            if(profileTask.getTask_index()>=platformRepository.get_Priority_By_Platform_And_Mode(profileTask.getPlatform(),device.getMode())) //check giới hạn lần get nhiệm vụ theo platform and mode
            {
                if(profileTask.getTask_list().trim().length()==0){ // nếu trong list platform trống thì lấy lại từ đầu
                    profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                    entityManager.clear();

                    List<String> string_Task_List=platformRepository.get_All_Platform_True(device.getMode());
                    String task_List=String.join(",", string_Task_List);
                    List<String> arrPlatform=new ArrayList<>(Arrays.asList(task_List.split(",")));

                    profileTask.setPlatform(arrPlatform.get(0));
                    List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                    task_List=String.join(",", subPlatform);
                    profileTask.setTask_list(task_List);
                    profileTask.setTask_index(0);
                    profileTask.setState(1);
                    profileTask.setRequest_index(0);
                    profileTaskRepository.save(profileTask);

                }else {
                    String task_List = profileTask.getTask_list();
                    List<String> arrPlatform = new ArrayList<>(Arrays.asList(task_List.split(",")));

                    profileTask.setPlatform(arrPlatform.get(0));
                    List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                    task_List = String.join(",", subPlatform);
                    profileTask.setTask_list(task_List);
                    profileTask.setTask_index(0);
                    profileTask.setRequest_index(0);
                    profileTaskRepository.save(profileTask);
                }
            }


            Platform platform_Check=platformRepository.get_Platform_By_Platform_And_Mode(profileTask.getPlatform().trim(),device.getMode().trim());
            if(!profileTask.getPlatform().trim().equals("youtube")){
                if(accountProfileRepository.check_Count_AccountLive_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),profileTask.getPlatform().trim())<platform_Check.getMax_account()){ //check xem đủ tài khoản youtbe live=1 chưa
                    AccountProfile accountProfile_Live0=accountProfileRepository.get_AccountLive0_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),profileTask.getPlatform().trim());
                    if(accountProfile_Live0==null) {
                        if(platform_Check.getRegister_account()==1 &&
                                platform_Check.getDependent().trim().length()>0&&platform_Check.getConnection_account()==1&&
                                historyRegisterRepository.count_Register_By_Platform_And_DeviceId(profileTask.getPlatform().trim(),profileTask.getDevice().getDevice_id().trim()+"%",platform_Check.getRegister_time())==0&&
                                accountRepository.check_Count_Register_LessDay_By_DeviceId_And_Platform(device.getDevice_id().trim(),profileTask.getPlatform().trim(),7)<platform_Check.getRegister_limit()&&
                                accountProfileRepository.get_Count_Account_DependentLive_By_ProfileId_And_Platform(profileTask.getProfile_id(),platform_Check.getDependent().trim(),"%"+profileTask.getPlatform().trim()+"%")>0
                        ){
                            //Add proxy
                            /*
                            if(mode.getAdd_proxy()==1&&profileTask.getAdd_proxy()==0){
                                String[] geo={"th", "za", "kr", "jp", "id", "bd", "eg", "my"};
                                String proxy=ProxyAPI.getSock5Luna(geo[ran.nextInt(geo.length)]);
                                if(proxy!=null){
                                    resp.put("status", true);
                                    data.put("platform", "system");
                                    data.put("task", "add_proxy");
                                    data.put("task_key",proxy);
                                    resp.put("data",data);
                                    return new ResponseEntity<>(resp, HttpStatus.OK);
                                }else {
                                    resp.put("status", false);
                                    data.put("message", "Không thực hiện nhiệm vụ");
                                    resp.put("data", data);
                                    return new ResponseEntity<>(resp, HttpStatus.OK);
                                }
                            }
                             */
                            //gioi han time reg by platform and time
                            //List<String> list_device =deviceRepository.get_All_Device_By_IP(device.getIp_address().trim());
                            //historyRegisterRepository.count_Register_By_Platform_And_Time(profileTask.getPlatform().trim(),list_device,1)==0
                            Boolean check_Register=false;
                            IpRegister ipRegister_old=ipRegisterRepository.get_Ip_By_Ip_And_Platform(device.getIp_address(),profileTask.getPlatform().trim());
                            if(mode.getAdd_proxy()==1){
                                check_Register=true;
                            }else if(ipRegister_old==null){
                                check_Register=true;
                            }else if(ipRegister_old!=null &&ipRegister_old.getSuccess()==false && (System.currentTimeMillis()-ipRegister_old.getUpdate_time())/1000/60/60>=12) {
                                check_Register=true;
                            }else if(ipRegister_old!=null &&ipRegister_old.getSuccess()==true && (System.currentTimeMillis()-ipRegister_old.getUpdate_time())/1000/60/60>=24) {
                                check_Register=true;
                            }
                            if(check_Register){
                                AccountProfile accountProfile_Dependent=accountProfileRepository.get_Account_DependentLive_By_ProfileId_And_Platform(profileTask.getProfile_id(),platform_Check.getDependent().trim(),"%"+profileTask.getPlatform().trim()+"%");
                                AccountProfile accountCheck=accountProfileRepository.get_Account_By_Account_id_And_Platform(accountProfile_Dependent.getAccount_id().substring(0,accountProfile_Dependent.getAccount_id().lastIndexOf("|"))+"|"+profileTask.getPlatform(),profileTask.getPlatform());
                                if(accountCheck!=null){
                                    accountProfileRepository.delete(accountCheck);
                                }
                                AccountProfile accountProfile=new AccountProfile();
                                accountProfile.setAccount_id(accountProfile_Dependent.getAccount_id().substring(0,accountProfile_Dependent.getAccount_id().lastIndexOf("|"))+"|"+profileTask.getPlatform());
                                accountProfile.setPassword(accountProfile_Dependent.getPassword().trim());
                                if(profileTask.getPlatform().equals("tiktok")){
                                    String name=null;
                                    if(accountProfile_Dependent.getName().length()!=0){
                                        name= Openai.nameTiktok2(accountProfile_Dependent.getName().trim(),openAiKeyRepository.get_OpenAI_Key());
                                        if(name==null){
                                            name=accountProfile_Dependent.getName();
                                        }
                                    }else{
                                        name= Openai.nameTiktok(accountProfile_Dependent.getAccount_id().substring(0,accountProfile_Dependent.getAccount_id().lastIndexOf("@")),openAiKeyRepository.get_OpenAI_Key());
                                    }
                                    if(name!=null){
                                        accountProfile.setName(name);
                                    }else {
                                        AccountName accountName=accountNameRepository.get_AcountName_By_Platform("tiktok");
                                        accountProfile.setName(accountName.getName());
                                    }
                                }else{
                                    accountProfile.setName(accountProfile_Dependent.getName().trim());
                                }
                                accountProfile.setAvatar(0);
                                accountProfile.setRecover(accountProfile_Dependent.getAccount_id().substring(0,accountProfile_Dependent.getAccount_id().lastIndexOf("|"))+"|"+accountProfile_Dependent.getPlatform());
                                accountProfile.setPlatform(profileTask.getPlatform());
                                accountProfile.setLive(-1);
                                accountProfile.setChanged(0);
                                accountProfile.setRunning(0);
                                accountProfile.setSign_in(0);
                                accountProfile.setAuth_2fa("");
                                accountProfile.setProfileTask(profileTask);
                                accountProfile.setAdd_time(System.currentTimeMillis());
                                accountProfile.setUpdate_time(0L);
                                accountProfile.setCode(accountProfile_Dependent.getCode().trim());
                                accountProfile.setTask_time(System.currentTimeMillis());
                                accountProfile.setConnection_platform("");
                                accountProfileRepository.save(accountProfile);
                                if(!accountProfile_Dependent.getConnection_platform().contains(profileTask.getPlatform())){
                                    accountProfile_Dependent.setConnection_platform(accountProfile_Dependent.getConnection_platform().trim()+profileTask.getPlatform()+"|");
                                    accountProfileRepository.save(accountProfile_Dependent);
                                }
                                HistoryRegister historyRegister=new HistoryRegister();
                                historyRegister.setProfileTask(profileTask);
                                historyRegister.setPlatform(profileTask.getPlatform().trim());
                                historyRegister.setState(0);
                                historyRegister.setIp_address(device.getIp_address());
                                historyRegister.setUpdate_time(System.currentTimeMillis());
                                historyRegisterRepository.save(historyRegister);
                                if(ipRegister_old!=null){
                                    ipRegister_old.setSuccess(false);
                                    ipRegister_old.setUpdate_time(System.currentTimeMillis());
                                    ipRegisterRepository.save(ipRegister_old);
                                }else{
                                    IpRegister ipRegister=new IpRegister();
                                    ipRegister.setId(device.getIp_address());
                                    ipRegister.setSuccess(false);
                                    ipRegister.setPlatform(profileTask.getPlatform().trim());
                                    ipRegister.setUpdate_time(System.currentTimeMillis());
                                    ipRegisterRepository.save(ipRegister);
                                }
                                profileTask.setRequest_index(1);
                                profileTaskRepository.save(profileTask);

                                accountProfileRepository.update_SignIn_By_ProfileId(profileTask.getProfile_id().trim());// reset signin profile

                                resp.put("status", true);
                                data.put("platform", profileTask.getPlatform());
                                data.put("app",platform_Check.getApp_name().trim());
                                data.put("task", "register");
                                data.put("task_key", accountProfile_Dependent.getAccount_id().substring(0,accountProfile_Dependent.getAccount_id().lastIndexOf("|")));
                                data.put("account_id",  accountProfile_Dependent.getAccount_id().substring(0,accountProfile_Dependent.getAccount_id().lastIndexOf("|")));
                                data.put("password",accountProfile_Dependent.getPassword().trim());
                                data.put("recover_mail", accountProfile_Dependent.getAccount_id().substring(0,accountProfile_Dependent.getAccount_id().lastIndexOf("|")));
                                data.put("name",  accountProfile.getName());
                                data.put("avatar", accountProfile.getAvatar()==0?false:true);
                                data.put("auth_2fa", "");
                                resp.put("data",data);
                                return new ResponseEntity<>(resp, HttpStatus.OK);

                            }
                        }else if(accountRepository.check_Count_AccountDie24H_By_Platform_And_DeviceId(device.getDevice_id().trim(),profileTask.getPlatform().trim())==0&&
                                platform_Check.getLogin_account()==1&&
                                accountProfileRepository.count_Login_By_Platform_And_DeviceId(profileTask.getPlatform().trim(),device.getDevice_id().trim()+"%",platform_Check.getLogin_time())==0&&
                                accountProfileRepository.count_Login_Time_Null_By_Platform_And_DeviceId(profileTask.getPlatform().trim(),device.getDevice_id().trim()+"%")==0
                        ){
                            String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                            String code="";
                            for(int i=0;i<10;i++){
                                Integer ranver=ran.nextInt(stringrand.length());
                                code=code+stringrand.charAt(ranver);
                            }

                            Account account_get= accountRepository.get_Account_Platform_By_ProfileId(device_id.trim()+"_"+profile_id.trim(),device_id,System.currentTimeMillis(),code,profileTask.getPlatform().trim(),device.getMode().trim());
                            if(account_get!=null){


                                AccountProfile accountCheck=accountProfileRepository.get_Account_By_Account_id_And_Platform(account_get.getAccount_id(),profileTask.getPlatform());
                                if(accountCheck!=null){
                                    accountProfileRepository.delete(accountCheck);
                                }

                                AccountProfile accountProfile=new AccountProfile();
                                accountProfile.setAccount_id(account_get.getAccount_id());
                                accountProfile.setPassword(account_get.getPassword());
                                accountProfile.setRecover(account_get.getRecover_mail());
                                accountProfile.setPlatform(profileTask.getPlatform().trim());
                                accountProfile.setLive(0);
                                accountProfile.setChanged(0);
                                accountProfile.setRunning(0);
                                accountProfile.setSign_in(0);
                                accountProfile.setCode(code);
                                accountProfile.setTask_time(System.currentTimeMillis());
                                accountProfile.setConnection_platform("");
                                accountProfile.setAuth_2fa(account_get.getAuth_2fa());
                                accountProfile.setProfileTask(profileTask);
                                accountProfile.setAdd_time(System.currentTimeMillis());
                                accountProfile.setUpdate_time(0L);
                                accountProfileRepository.save(accountProfile);

                                accountProfileRepository.update_SignIn_By_ProfileId(profileTask.getProfile_id().trim());// reset signin profile

                                resp.put("status", true);
                                data.put("platform", profileTask.getPlatform().trim());
                                data.put("app",platform_Check.getApp_name().trim());
                                if(platform_Check.getPlatform().equals("tiktok")){
                                    data.put("task", "sign_in");
                                }else{
                                    data.put("task", "login");
                                }
                                data.put("task_key", account_get.getAccount_id().substring(0,account_get.getAccount_id().lastIndexOf("|")));
                                data.put("account_id",account_get.getAccount_id().substring(0,account_get.getAccount_id().lastIndexOf("|")));
                                data.put("password", account_get.getPassword().trim());
                                data.put("name", account_get.getName().trim());
                                data.put("avatar", accountProfile.getAvatar()==0?false:true);
                                data.put("recover_mail", account_get.getRecover_mail().trim());
                                data.put("auth_2fa", account_get.getAuth_2fa().trim());
                                data.put("account_list",accountProfileRepository.get_List_Account_Youtube_By_ProfileId(profileTask.getProfile_id().trim()));
                                resp.put("data",data);
                                return new ResponseEntity<>(resp, HttpStatus.OK);
                            }
                        }

                    }else if((System.currentTimeMillis()-accountProfile_Live0.getLast_time())/1000/60/60>=10&&
                            accountProfile_Live0.getLive()==-1){//check last time task login

                        //Add proxy
                        if(mode.getAdd_proxy()==1&&profileTask.getAdd_proxy()==0){
                            String[] geo={"th", "za", "kr", "jp", "id", "bd", "eg", "my"};
                            String proxy=ProxyAPI.getSock5Luna(geo[ran.nextInt(geo.length)]);
                            if(proxy!=null){
                                resp.put("status", true);
                                data.put("platform", "system");
                                data.put("task", "add_proxy");
                                data.put("task_key",proxy);
                                resp.put("data",data);
                                return new ResponseEntity<>(resp, HttpStatus.OK);
                            }else {
                                resp.put("status", false);
                                data.put("message", "Không thực hiện nhiệm vụ");
                                resp.put("data", data);
                                return new ResponseEntity<>(resp, HttpStatus.OK);
                            }
                        }
                        /*
                        Account account=accountRepository.get_Account_By_Password_And_Platfrom(accountProfile_Live0.getPassword().trim(),accountProfile_Live0.getPlatform());
                        if(account!=null&&accountProfile_Live0.getPlatform().equals("tiktok")){
                            if(TikTokApi.checkAccount(account.getAccount_id().substring(0,account.getAccount_id().lastIndexOf("|")).replace("@",""),1)==-1){
                                account.setLive(2812);
                                accountRepository.save(account);
                                Account account_Dep=accountRepository.get_Account_By_Account_id(accountProfile_Live0.getRecover());
                                if(account_Dep!=null){
                                    account_Dep.setDie_dependent(accountProfile_Live0.getPlatform());
                                    account_Dep.setProfile_id("");
                                    account_Dep.setDevice_id("");
                                    account_Dep.setRunning(0);
                                    account_Dep.setLive(2812);
                                    accountRepository.save(account_Dep);
                                }
                                AccountProfile accountProfile_Dep=accountProfileRepository.get_Account_Youtube_By_ProfileId_And_Code(profileTask.getProfile_id().trim(),accountProfile_Live0.getCode().trim());
                                accountProfileRepository.delete(accountProfile_Dep);
                                accountProfileRepository.delete(accountProfile_Live0);
                                historyRegisterRepository.delete_Register_By_Platform_And_ProfileId(accountProfile_Live0.getPlatform().trim(),profileTask.getProfile_id().trim(),24);
                                resp.put("status", false);
                                data.put("message", "Không thực hiện nhiệm vụ");
                                resp.put("data", data);
                                return new ResponseEntity<>(resp, HttpStatus.OK);
                            }
                        }
                         */
                        if(!(platformRepository.get_Register_Account_Platform_And_Mode(profileTask.getPlatform(),device.getMode())==0&&accountProfile_Live0.getLive()==-1)){
                            accountProfile_Live0.setLast_time(System.currentTimeMillis());
                            accountProfileRepository.save(accountProfile_Live0);

                            accountProfileRepository.update_SignIn_By_ProfileId(profileTask.getProfile_id().trim());// reset signin profile

                            resp.put("status", true);
                            data.put("platform",profileTask.getPlatform());
                            data.put("app",platform_Check.getApp_name().trim());
                            if(accountProfile_Live0.getLive()==-1){
                                data.put("task", "register");
                            }else{
                                data.put("task", "login");
                            }
                            data.put("task_key", accountProfile_Live0.getAccount_id().substring(0,accountProfile_Live0.getAccount_id().lastIndexOf("|")));
                            data.put("account_id", accountProfile_Live0.getAccount_id().substring(0,accountProfile_Live0.getAccount_id().lastIndexOf("|")));
                            data.put("password", accountProfile_Live0.getPassword().trim());
                            data.put("name", accountProfile_Live0.getName().trim());
                            data.put("avatar", accountProfile_Live0.getAvatar()==0?false:true);
                            data.put("recover_mail", accountProfile_Live0.getRecover().substring(0,accountProfile_Live0.getRecover().lastIndexOf("|")));
                            data.put("auth_2fa", accountProfile_Live0.getAuth_2fa().trim());
                            data.put("account_list",accountProfileRepository.get_List_Account_Youtube_By_ProfileId(profileTask.getProfile_id().trim()));
                            resp.put("data",data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }

                    }
                }


            }

            if(profileTask==null){
                resp.put("status", false);
                data.put("message", "Không thực hiện nhiệm vụ");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }else{
                if( accountProfileRepository.check_Count_AccountLive_By_ProfileId_And_Platform(profileTask.getProfile_id(),profileTask.getPlatform())==0 &&!profileTask.getPlatform().equals("tiktok")){

                    String task_List = profileTask.getTask_list();
                    List<String> arrPlatform = new ArrayList<>(Arrays.asList(task_List.split(",")));
                    profileTask.setPlatform(arrPlatform.get(0));
                    List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                    task_List = String.join(",", subPlatform);
                    profileTask.setTask_list(task_List);
                    profileTask.setRequest_index(0);
                    profileTaskRepository.save(profileTask);
                    resp.put("status", false);
                    data.put("message", "Không thực hiện nhiệm vụ");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }

            AccountProfile accountProfile_Task=accountProfileRepository.get_Account_By_Platform_And_ProfileId(profileTask.getProfile_id(),profileTask.getPlatform());
            if(platform_Youtube_Check.getMax_account()>1){ //neu num acc >1 on 1 profile
                if(accountProfile_Task!=null&&(System.currentTimeMillis()-accountProfile_Task.getTask_time())/1000/60/60>=24){
                    accountProfile_Task=accountProfileRepository.get_Account_By_Platform_And_ProfileId(profileTask.getProfile_id(),profileTask.getPlatform());
                    accountProfile_Task.setRunning(0);
                    accountProfile_Task.setSign_in(1);
                    accountProfileRepository.save(accountProfile_Task);
                }
                if(accountProfile_Task.getRunning()==0){
                    accountProfile_Task.setTask_time(System.currentTimeMillis());
                    accountProfile_Task.setSign_in(1);
                    accountProfileRepository.save(accountProfile_Task);
                }
            }
            if(accountProfile_Task==null&&profileTask.getPlatform().equals("tiktok")){
                profileTask.setAccount_id(profileTask.getProfile_id().trim());
                profileTask.setTask_index(profileTask.getTask_index()+1);
                profileTaskRepository.save(profileTask);
            } else if(((accountProfile_Task.getSign_in()==1 &&platform_Youtube_Check.getMax_account()>1) || accountProfile_Task.getLive()==0)&&
                    (System.currentTimeMillis()-accountProfile_Task.getLast_time())/1000/60>=10){

                accountProfile_Task.setLast_time(System.currentTimeMillis());
                accountProfileRepository.save(accountProfile_Task);

                //Platform platform_Check=platformRepository.get_Platform_By_Platform_And_Mode(profileTask.getPlatform().trim(),device.getMode().trim());
                resp.put("status", true);
                data.put("platform",profileTask.getPlatform());
                data.put("app",platform_Check.getApp_name().trim());
                if(accountProfile_Task.getLive()==0 &&accountProfile_Task.getSign_in()==0){
                    data.put("task", "login");
                }else if(platform_Youtube_Check.getMax_account()==1) {
                    data.put("task", "login");
                }else{
                    data.put("task", "sign_in");
                }
                data.put("task_key", accountProfile_Task.getAccount_id().substring(0,accountProfile_Task.getAccount_id().lastIndexOf("|")));
                data.put("account_id", accountProfile_Task.getAccount_id().substring(0,accountProfile_Task.getAccount_id().lastIndexOf("|")));
                data.put("password", accountProfile_Task.getPassword().trim());
                data.put("name", accountProfile_Task.getName().trim());
                data.put("avatar", accountProfile_Task.getAvatar()==0?false:true);
                if(accountProfile_Task.getRecover().trim().contains("|")){
                    data.put("recover_mail", accountProfile_Task.getRecover().trim().substring(0,accountProfile_Task.getRecover().trim().lastIndexOf("|")));
                }else{
                    AccountProfile accountProfile_Dependent=accountProfileRepository.get_Account_By_ProfileId_And_Platform(profileTask.getProfile_id(),platform_Check.getDependent().trim());
                    accountProfile_Task.setRecover(accountProfile_Dependent.getAccount_id());
                    String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                    String code="";
                    for(int i=0;i<10;i++){
                        Integer ranver=ran.nextInt(stringrand.length());
                        code=code+stringrand.charAt(ranver);
                    }
                    accountProfile_Task.setCode(code);
                    accountProfileRepository.save(accountProfile_Task);
                    if(!accountProfile_Dependent.getConnection_platform().contains(profileTask.getPlatform())){
                        accountProfile_Dependent.setConnection_platform(profileTask.getPlatform()+"|");
                    }
                    accountProfile_Dependent.setCode(code);
                    accountProfileRepository.save(accountProfile_Dependent);
                    data.put("recover_mail", accountProfile_Task.getRecover().trim().substring(0,accountProfile_Task.getRecover().trim().lastIndexOf("|")));
                }
                data.put("auth_2fa", accountProfile_Task.getAuth_2fa().trim());
                data.put("account_list",accountProfileRepository.get_List_Account_Youtube_By_ProfileId(profileTask.getProfile_id().trim()));
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }else if((accountProfile_Task.getSign_in()==1 &&platform_Youtube_Check.getMax_account()>1) || accountProfile_Task.getLive()==0) {
                profileTask.setAccount_id(accountProfile_Task.getAccount_id().trim());
                profileTask.setTask_index(profileTask.getTask_index()+1);
                profileTaskRepository.save(profileTask);
                resp.put("status", false);
                data.put("message", "Không thực hiện nhiệm vụ");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }else {
                profileTask.setAccount_id(accountProfile_Task.getAccount_id().trim());
                profileTask.setTask_index(profileTask.getTask_index()+1);
                profileTaskRepository.save(profileTask);
            }

            if(accountProfile_Task!=null){
                if(accountProfile_Task.getChanged()==0 && true==false){ // update changer off
                    resp.put("status", true);
                    data.put("platform", profileTask.getPlatform().trim());
                    data.put("task", "update_info");
                    data.put("task_key", accountProfile_Task.getAccount_id().substring(0,accountProfile_Task.getAccount_id().lastIndexOf("|")));
                    data.put("account_id", accountProfile_Task.getAccount_id().substring(0,accountProfile_Task.getAccount_id().lastIndexOf("|")));
                    data.put("password", accountProfile_Task.getPassword().trim());
                    data.put("app", platform_Check.getApp_name().trim());
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
                if(platform_Check.getClone_info()==1&&(System.currentTimeMillis()-accountProfile_Task.getAdd_time())/1000/60/60/24>=platform_Check.getAdd_post_time()){
                    AccountClone accountClone=accountCloneRepository.get_Account_Clone_By_Account_id(accountProfile_Task.getAccount_id().trim());
                    if(accountClone!=null){
                        if(accountProfile_Task.getAvatar()==0){
                            resp.put("status", true);
                            data.put("platform", profileTask.getPlatform().trim());
                            data.put("task", "update_avatar");
                            data.put("task_key", accountProfile_Task.getAccount_id().substring(0,accountProfile_Task.getAccount_id().lastIndexOf("|")));
                            data.put("account_id", accountProfile_Task.getAccount_id().substring(0,accountProfile_Task.getAccount_id().lastIndexOf("|")));
                            //data.put("task_link", accountClone.getAvatar_link());
                            data.put("task_link", "http://api.idnetwork.com.vn/image/random?geo=Us");
                            data.put("app", platform_Check.getApp_name().trim());
                            resp.put("data",data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }else if(platform_Check.getAdd_post()==1&&
                                (System.currentTimeMillis()-accountClone.getUpdate_time())/1000/60/60/24>=platform_Check.getAdd_post_time()){
                            JsonArray videoList=TikTokApi.getInfoVideoByChannelByUserId(accountClone.getId_clone(),100);
                            for (JsonElement video: videoList) {
                                JsonObject videoObj=video.getAsJsonObject();
                                DataFollowerTiktok dataFollowerTiktok =new DataFollowerTiktok();
                                dataFollowerTiktok.setVideo_id(videoObj.get("video_id").getAsString());
                                if(!videoObj.get("play").getAsString().contains("video")){
                                    continue;
                                }
                                if(!accountClone.getVideo_list().contains(videoObj.get("video_id").getAsString())){
                                    resp.put("status", true);
                                    data.put("platform", profileTask.getPlatform().trim());
                                    data.put("task", "add_post");
                                    data.put("account_id", accountProfile_Task.getAccount_id().substring(0,accountProfile_Task.getAccount_id().lastIndexOf("|")));
                                    data.put("task_key", videoObj.get("video_id").getAsString());
                                    data.put("task_link", videoObj.get("play").getAsString());
                                    data.put("app", platform_Check.getApp_name().trim());
                                    resp.put("data",data);
                                    return new ResponseEntity<>(resp, HttpStatus.OK);
                                }
                            }
                            if(videoList!=null){
                                accountCloneRepository.delete(accountClone);
                            }
                        }
                    }
                }
            }

            List<ModeOption> priorityTasks =modeOptionRepository.get_Priority_Task_By_Platform_And_Mode(profileTask.getPlatform(),device.getMode());
            List<String> arrTask = new ArrayList<>();

            for(int i=0;i<priorityTasks.size();i++){
                for (int j = 0; j < priorityTasks.get(i).getPriority(); j++) {
                    arrTask.add(priorityTasks.get(i).getTask());
                }
            }
            /*
            if(profileTask.getPlatform().equals("tiktok")&&ipTask24hRepository.count_Task_Hour_By_Ip(device.getIp_address()+"%",4)>settingSystem.getIp_task_24h()){ // reboot đổi ip
                profileTaskRepository.reset_Reboot_By_ProfileId(profileTask.getProfile_id().trim());
                resp.put("status", true);
                data.put("platform", "system");
                data.put("task", "reboot");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
             */
            Map<String, Object> get_task =null;
            String task_index=null;
            while (arrTask.size()>0){
                String task = arrTask.get(ran.nextInt(arrTask.size())).trim();
                ModeOption modeOption=modeOptionRepository.get_Mode_Option(device.getMode().trim(),profileTask.getPlatform().trim(),task);
                AccountTask accountTask=accountTaskRepository.get_Acount_Task_By_AccountId(profileTask.getAccount_id());
                Long get_time=0L;
                if(accountTask!=null){
                    if(task.trim().contains("follower")){
                        get_time=accountTask.getFollower_time();
                    }else if(task.trim().contains("subscriber")){
                        get_time=accountTask.getSubscriber_time();
                    }else if(task.trim().contains("view")){
                        get_time=accountTask.getView_time();
                    }else if(task.trim().contains("like")){
                        get_time=accountTask.getLike_time();
                    }else if(task.trim().contains("comment")){
                        get_time=accountTask.getFollower_time();
                    }else if(task.trim().contains("repost")){
                        get_time=accountTask.getRepost_time();
                    }else if(task.trim().contains("member")){
                        get_time=accountTask.getMember_time();
                    }else if(task.trim().contains("share")){
                        get_time=accountTask.getShare_time();
                    }else if(task.trim().contains("favorites")){
                        get_time=accountTask.getFavorites_time();
                    }
                }
                if(modeOption==null){
                    resp.put("status",false);
                    data.put("message","modeOption không hợp lệ!");
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else if((System.currentTimeMillis()-get_time)/1000/60<modeOption.getTime_get_task()){
                    while(arrTask.remove(task)) {}
                    continue;
                }
                while(arrTask.remove(task)) {}
                if(profileTask.getPlatform().equals("tiktok") && !profileTask.getAccount_id().contains("@")){
                    if(task.equals("view")){
                        get_task=tiktokTask.tiktok_view(profileTask.getAccount_id(),device.getMode().trim(),device,0);
                    }
                } else if(profileTask.getPlatform().equals("tiktok")){
                    if(task.equals("follower")){
                        get_task= tiktokTask.tiktok_follower(profileTask.getAccount_id(),device.getMode().trim(),device.getIp_address(),device);
                    }else if(task.equals("like")){
                        get_task=tiktokTask.tiktok_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("view")){
                        get_task=tiktokTask.tiktok_view(profileTask.getAccount_id(),device.getMode().trim(),device,accountProfile_Task.getLive());
                    }else if(task.equals("comment")){
                        get_task=tiktokTask.tiktok_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("share")){
                        get_task=tiktokTask.tiktok_share(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("favorites")){
                        get_task=tiktokTask.tiktok_favorites(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("youtube")){
                    if(task.equals("view")){
                        get_task=youtubeTask.youtube_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("like")){
                        get_task=youtubeTask.youtube_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("subscriber")){
                        get_task=youtubeTask.youtube_subscriber(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("comment")){
                        get_task=youtubeTask.youtube_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("facebook")){
                    if(task.equals("follower")){
                        get_task=facebookTask.facebook_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("like")){
                        get_task=facebookTask.facebook_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("view")){
                        get_task=facebookTask.facebook_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("comment")){
                        get_task=facebookTask.facebook_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("member")){
                        get_task=facebookTask.facebook_member(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("x")){
                    if(task.equals("follower")){
                        get_task=xTask.x_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("like")){
                        get_task=xTask.x_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("view")){
                        get_task=xTask.x_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("comment")){
                        get_task=xTask.x_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("repost")){
                        get_task=xTask.x_repost(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("instagram")){
                    if(task.equals("instagram_follower")){
                        get_task=instagramTask.instagram_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("like")){
                        get_task=instagramTask.instagram_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("view")){
                        get_task=instagramTask.instagram_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("comment")){
                        get_task=instagramTask.instagram_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("threads")){
                    if(task.equals("follower")){
                        get_task=threadsTask.threads_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("like")){
                        get_task=threadsTask.threads_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("view")){
                        get_task=threadsTask.threads_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("comment")){
                        get_task=threadsTask.threads_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("repost")){
                        get_task=threadsTask.threads_repost(profileTask.getAccount_id(),device.getMode().trim());
                    }
                }
                if(get_task!=null?get_task.get("status").equals(true):false){
                    task_index=task;
                    break;
                }
            }
            if(get_task==null){
                resp.put("status",false);
                data.put("message","Không có nhiệm vụ!");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            Map<String, Object> respJson=new LinkedHashMap<>();
            Map<String, Object> dataJson=new LinkedHashMap<>();
            if(get_task.get("status").equals(true)){

                dataJson= (Map<String, Object>) get_task.get("data");

                Thread.sleep(300+ran.nextInt(500));
                if(!OrderThreadSpeedUpCheck.getValue().contains(dataJson.get("order_id").toString())){
                    resp.put("status",false);
                    data.put("message","Không có nhiệm vụ!");
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
                List<String> ip_List=deviceRepository.get_IP_Running_Task_By_OrderId(Long.parseLong(dataJson.get("order_id").toString()));
                if(ip_List.size()!=0){
                    Set<String> ipSet = new HashSet<>(ip_List);
                    if(ipSet.contains(device.getIp_address())){
                        resp.put("status",false);
                        data.put("message","Không có nhiệm vụ!");
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }
                if(profileTask.getPlatform().equals("tiktok")&&profileTask.getProfile_id().equals(profileTask.getAccount_id())){
                    dataJson.put("account_id",profileTask.getAccount_id());
                }else{
                    dataJson.put("name",accountProfileRepository.get_Name_By_AccountId(profileTask.getAccount_id()));
                    dataJson.put("avatar",accountProfileRepository.get_Avatar_By_AccountId(profileTask.getAccount_id())==0?false:true);
                    dataJson.put("account_id",dataJson.get("account_id").toString().substring(0,dataJson.get("account_id").toString().indexOf("|")));
                    if(accountProfile_Task.getSign_in()==0){
                        if(accountProfile_Task.getRecover().trim().contains("|")){
                            dataJson.put("recover",accountProfile_Task.getRecover().trim().substring(0,accountProfile_Task.getRecover().trim().indexOf("|")));
                        }else{
                            //Platform platform_Check=platformRepository.get_Platform_By_Platform_And_Mode(profileTask.getPlatform().trim(),device.getMode().trim());
                            AccountProfile accountProfile_Dependent=accountProfileRepository.get_Account_By_ProfileId_And_Platform(profileTask.getProfile_id(),platform_Check.getDependent().trim());
                            accountProfile_Task.setRecover(accountProfile_Dependent.getAccount_id());
                            String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                            String code="";
                            for(int i=0;i<10;i++){
                                Integer ranver=ran.nextInt(stringrand.length());
                                code=code+stringrand.charAt(ranver);
                            }
                            accountProfile_Task.setCode(code);
                            accountProfileRepository.save(accountProfile_Task);
                            if(!accountProfile_Dependent.getConnection_platform().contains(profileTask.getPlatform())){
                                accountProfile_Dependent.setConnection_platform(profileTask.getPlatform()+"|");
                            }
                            accountProfile_Dependent.setCode(code);
                            accountProfileRepository.save(accountProfile_Dependent);
                            dataJson.put("recover",accountProfile_Task.getRecover().trim().substring(0,accountProfile_Task.getRecover().trim().indexOf("|")));
                        }
                    }else{
                        if(accountProfile_Task.getRecover().trim().contains("|")){
                            dataJson.put("recover",accountProfile_Task.getRecover().trim().substring(0,accountProfile_Task.getRecover().trim().indexOf("|")));
                        }else{
                            dataJson.put("recover",accountProfile_Task.getRecover().trim());
                        }
                    }

                }
                dataJson.put("task_index",profileTask.getTask_index());
                Long version_app=platformRepository.get_Version_App_Platform_And_Mode(dataJson.get("platform").toString(),device.getMode());
                dataJson.put("version_app",version_app==null?0:version_app);
                Integer platform_task=platformRepository.get_Activity_Platform_And_Mode(dataJson.get("platform").toString(),device.getMode());
                dataJson.put("activity",platform_task==0?false:true);
                respJson.put("status",true);
                respJson.put("data",dataJson);
                //--------------------------------------------//
                profileTask.setGet_time(System.currentTimeMillis());
                profileTask.setOrder_id(Long.parseLong(dataJson.get("order_id").toString()));
                profileTask.setRunning(1);
                profileTask.setTask(dataJson.get("task").toString());
                profileTask.setPlatform(dataJson.get("platform").toString());
                profileTask.setTask_key(dataJson.get("task_key").toString());
                //save get task time 20/12/24
                profileTask.setTask_time(System.currentTimeMillis());
                profileTaskRepository.save(profileTask);
                device.setTask_time(System.currentTimeMillis());
                //device.setProfile_running(profile_id);
                device.setTask(dataJson.get("task").toString());
                device.setPlatform(dataJson.get("platform").toString());
                device.setRunning(1);
                deviceRepository.save(device);
                //--------------------------------------------//
                //dataJson.remove("order_id"); // trả về order_id => 17/3/2025



            }else{
                respJson.put("status",false);
                dataJson.put("message","Không có nhiệm vụ!");
                respJson.put("data",dataJson);
                profileTask.setRunning(0);
                profileTask.setGet_time(System.currentTimeMillis());
                profileTaskRepository.save(profileTask);
            }
            return new ResponseEntity<>(respJson, HttpStatus.OK);
        }catch (Exception e){
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            LogError logError =new LogError();
            logError.setMethod_name(stackTraceElement.getMethodName());
            logError.setLine_number(stackTraceElement.getLineNumber());
            logError.setClass_name(stackTraceElement.getClassName());
            logError.setFile_name(stackTraceElement.getFileName() +device_id+"_"+profile_id);
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

    @PostMapping(value = "/updateTask", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> updateTask(@RequestHeader(defaultValue = "") String Authorization,
                                                   @RequestBody UpdateTaskRequest updateTaskRequest) {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
            if (checktoken ==0) {
                resp.put("status", false);
                data.put("message", "Token expired");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (updateTaskRequest.getAccount_id().length() == 0) {
                resp.put("status", false);
                data.put("message", "account_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (updateTaskRequest.getTask().length() == 0) {
                resp.put("status", false);
                data.put("message", "task không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (updateTaskRequest.getPlatform().length() == 0) {
                resp.put("status", false);
                data.put("message", "platform không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (updateTaskRequest.getStatus() == null) {
                resp.put("status", false);
                data.put("message", "status không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (updateTaskRequest.getTask_key().length()==0&&!updateTaskRequest.getTask().equals("login")&&!updateTaskRequest.getTask().equals("register")&&!updateTaskRequest.getTask().equals("sign_in")) {
                resp.put("status", false);
                data.put("message", "task_key không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (accountProfileRepository.check_Account_By_AccountId(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim())==0 && !(updateTaskRequest.getPlatform().trim().equals("tiktok")&&updateTaskRequest.getTask().equals("view"))
            ) {
                resp.put("status", false);
                data.put("message", "account_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }


            if(!Set.of("login", "register", "sign_in","add_post","update_info","add_recovery_mail","update_avatar","update_username").contains(updateTaskRequest.getTask().trim())){
                String platform_Check = updateTaskRequest.getPlatform().toLowerCase().trim();
                if(platform_Check.equals("youtube")){
                    if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true){
                        youtubeUpdate.youtube_view(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("subscriber")&&updateTaskRequest.getStatus()==true){
                        youtubeUpdate.youtube_subscriber(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    } else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                        youtubeUpdate.youtube_like(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    } else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("comment")&&updateTaskRequest.getStatus()==true){
                        youtubeUpdate.youtube_comment(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim(),updateTaskRequest.getStatus());
                    }
                }else if(platform_Check.equals("tiktok")){
                    if(updateTaskRequest.getTask().toLowerCase().trim().equals("follower")){
                        tiktokUpdate.tiktok_follower(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim(),updateTaskRequest.getSuccess(),updateTaskRequest.getStatus(),updateTaskRequest.getDevice_id().trim()+"_"+updateTaskRequest.getProfile_id().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                        tiktokUpdate.tiktok_like(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("comment")){
                        tiktokUpdate.tiktok_comment(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim(),updateTaskRequest.getStatus());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true){
                        tiktokUpdate.tiktok_view(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim(),updateTaskRequest.getBonus().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("share")&&updateTaskRequest.getStatus()==true){
                        tiktokUpdate.tiktok_share(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("favorites")&&updateTaskRequest.getStatus()==true){
                        tiktokUpdate.tiktok_favorites(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }
                }else if(platform_Check.equals("facebook")){
                    if(updateTaskRequest.getTask().toLowerCase().trim().equals("follower")&&updateTaskRequest.getStatus()==true){
                        facebookUpdate.facebook_follower(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                        facebookUpdate.facebook_like(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("comment")) {
                        facebookUpdate.facebook_comment(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(), updateTaskRequest.getTask_key().trim(), updateTaskRequest.getStatus());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("member")&&updateTaskRequest.getStatus()==true) {
                        facebookUpdate.facebook_member(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(), updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true) {
                        facebookUpdate.facebook_view(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(), updateTaskRequest.getTask_key().trim());
                    }
                }else if(platform_Check.equals("x")){
                    if(updateTaskRequest.getTask().toLowerCase().trim().equals("follower")&&updateTaskRequest.getStatus()==true){
                        xUpdate.x_follower(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                        xUpdate.x_like(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("comment")) {
                        xUpdate.x_comment(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(), updateTaskRequest.getTask_key().trim(), updateTaskRequest.getStatus());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true) {
                        xUpdate.x_view(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(), updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("repost")&&updateTaskRequest.getStatus()==true) {
                        xUpdate.x_repost(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(), updateTaskRequest.getTask_key().trim());
                    }
                }else if(platform_Check.equals("instagram")){
                    if(updateTaskRequest.getTask().toLowerCase().trim().equals("follower")&&updateTaskRequest.getStatus()==true){
                        instagramUpdate.instagram_follower(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                        instagramUpdate.instagram_like(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("comment")) {
                        instagramUpdate.instagram_comment(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(), updateTaskRequest.getTask_key().trim(), updateTaskRequest.getStatus());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true) {
                        instagramUpdate.instagram_view(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(), updateTaskRequest.getTask_key().trim());
                    }
                }else if(platform_Check.equals("threads")){
                    if(updateTaskRequest.getTask().toLowerCase().trim().equals("follower")&&updateTaskRequest.getStatus()==true){
                        threadsUpdate.threads_follower(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                        threadsUpdate.threads_like(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("comment")) {
                        threadsUpdate.threads_comment(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(), updateTaskRequest.getTask_key().trim(), updateTaskRequest.getStatus());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true) {
                        threadsUpdate.threads_view(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(), updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("repost")&&updateTaskRequest.getStatus()==true) {
                        threadsUpdate.threads_repost(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(), updateTaskRequest.getTask_key().trim());
                    }
                }
                if(updateTaskRequest.getStatus()==true){
                    try {
                        OrderRunning orderRunning= orderRunningRepository.find_Order_By_OrderId(updateTaskRequest.getOrder_id());
                        if(orderRunning!=null&&orderRunning.getService().getTask().equals(updateTaskRequest.getTask().trim())){
                            HistorySum historySum=new HistorySum();
                            historySum.setOrderRunning(orderRunning);
                            historySum.setAccount_id(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim());
                            historySum.setViewing_time(updateTaskRequest.getViewing_time());
                            historySum.setAdd_time(System.currentTimeMillis());
                            historySumRepository.save(historySum);
                        }
                    }catch (Exception e){
                    }
                }

            }
            try{
                profileTaskRepository.reset_Thread_By_ProfileId(updateTaskRequest.getDevice_id().trim()+"_"+updateTaskRequest.getProfile_id().trim());
            }catch (Exception e){

            }
            if(updateTaskRequest.getPlatform().equals("tiktok")&&updateTaskRequest.getAccount_id().trim().equals(updateTaskRequest.getDevice_id().trim()+"_"+updateTaskRequest.getProfile_id().trim())&&updateTaskRequest.getTask().equals("view")){
                resp.put("status", true);
                data.put("message", "Update thành công!");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            try{
                AccountProfile accountProfile=accountProfileRepository.get_Account_By_Account_id_And_Platform(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getPlatform().trim());
                String platform_Dependent=platformRepository.get_Dependent_Connection_By_Platform_And_Mode(updateTaskRequest.getPlatform().trim(),accountProfile.getProfileTask().getDevice().getMode());
                if(updateTaskRequest.getTask().equals("add_post") &&updateTaskRequest.getStatus()==true){
                    AccountClone accountClone=accountCloneRepository.get_Account_Clone_By_Account_id(accountProfile.getAccount_id().trim());
                    if(accountClone!=null) {
                        accountClone.setVideo_list(accountClone.getVideo_list()+updateTaskRequest.getTask_key()+",");
                        accountClone.setUpdate_time(System.currentTimeMillis());
                        accountClone.setCheck_video(true);
                        accountCloneRepository.save(accountClone);
                    }
                }else if(updateTaskRequest.getTask().equals("update_avatar") &&updateTaskRequest.getStatus()==true){
                    if(accountProfile!=null) {
                        accountProfile.setAvatar(1);
                        accountProfileRepository.save(accountProfile);

                        Account account=accountRepository.get_Account_By_Account_id(accountProfile.getAccount_id().trim());
                        if(account!=null){
                            account.setAvatar(1);
                            accountRepository.save(account);
                        }
                    }
                }else if(updateTaskRequest.getTask().equals("update_username") &&updateTaskRequest.getStatus()==true){
                    if(accountProfile!=null) {

                        Account account=accountRepository.get_Account_By_Account_id(accountProfile.getAccount_id().trim());

                        Boolean checkLive=updateTaskRequest.getTask_key().trim().startsWith("@") && TikTokApi.checkLive(updateTaskRequest.getTask_key().trim().replace("@",""));

                        List<String> info=account!=null?(account.getUuid().length()>5?TikTokApi.checkUsernameNickname(account.getUuid(),2):null):null;
                        checkLive=checkLive && (info!=null?updateTaskRequest.getTask_key().trim().contains(info.get(0)):true);
                        if(updateTaskRequest.getPlatform().equals("tiktok")&& checkLive){
                            accountProfile.setAccount_id(updateTaskRequest.getTask_key().trim()+"|"+updateTaskRequest.getPlatform().trim());
                            accountProfile.setName(info!=null?info.get(1):"");
                            accountProfileRepository.save(accountProfile);

                            if(account!=null){
                                account.setAccount_id(updateTaskRequest.getTask_key().trim()+"|"+updateTaskRequest.getPlatform().trim());
                                account.setName(info!=null?info.get(1):"");
                                accountRepository.save(account);
                            }
                            AccountClone accountClone =accountCloneRepository.get_Account_Clone_By_Account_id(account.getAccount_id());
                            if(accountClone!=null){
                                accountClone.setCheck_video(true);
                                accountCloneRepository.save(accountClone);
                            }
                        }else{
                            accountProfile.setName(info!=null?info.get(1):"");
                            accountProfile.setLive(0);
                            accountProfileRepository.save(accountProfile);
                        }
                    }
                }else if(updateTaskRequest.getTask().equals("add_recovery_mail") &&updateTaskRequest.getStatus()==true){
                    if(accountProfile!=null&&updateTaskRequest.getRecover_mail().trim().equals("@")) {
                        //accountProfile.setPassword(updateTaskRequest.getPassword().trim());
                        accountProfile.setRecover(updateTaskRequest.getRecover_mail().trim());
                        if(updateTaskRequest.getRecover_mail_password().trim().length()>0){
                            accountProfile.setRecover_password(updateTaskRequest.getRecover_mail_password().trim());
                        }else{
                            accountProfile.setRecover_password("");
                        }
                        accountProfile.setChanged(1);
                        accountProfile.setChanged_time(System.currentTimeMillis());
                        accountProfileRepository.save(accountProfile);

                        Account account=accountRepository.get_Account_By_Account_id(accountProfile.getAccount_id().trim());
                        if(account!=null){
                            //account.setPassword(updateTaskRequest.getPassword().trim());
                            account.setRecover_mail(updateTaskRequest.getRecover_mail().trim());
                            if(updateTaskRequest.getRecover_mail_password().trim().length()>0){
                                account.setRecover_mail_password(updateTaskRequest.getRecover_mail_password().trim());
                            }else{
                                account.setRecover_mail_password("");
                            }
                            account.setChanged(1);
                            account.setChanged_time(System.currentTimeMillis());
                            accountRepository.save(account);
                        }
                    }
                }else if(updateTaskRequest.getIsLogin()==0 || updateTaskRequest.getIsLogin()==-1){
                    //.update_Than_Task_Index_By_AccountId(updateTaskRequest.getPlatform().trim(),updateTaskRequest.getAccount_id()+"|"+updateTaskRequest.getPlatform().trim());
                    if(accountProfile!=null){
                        if(updateTaskRequest.getAccount_id().trim().startsWith("@")&&updateTaskRequest.getPlatform().equals("tiktok")){
                            accountProfile.setLive(0);
                        }else if(!updateTaskRequest.getAccount_id().trim().startsWith("@")&&updateTaskRequest.getPlatform().equals("tiktok")){
                            accountProfile.setLive(-1);
                        }else{
                            if(updateTaskRequest.getTask().equals("register")){
                                accountProfile.setLive(-1);
                            }else{
                                accountProfile.setLive(0);
                            }
                        }
                        accountProfile.setUpdate_time(System.currentTimeMillis());
                        accountProfileRepository.save(accountProfile);
                    }
                    /*
                    if(updateTaskRequest.getIsLogin()==0){
                        Device device= deviceRepository.check_DeviceId(updateTaskRequest.getDevice_id().trim());
                        if(device!=null){
                            device.setReboot(1);
                            deviceRepository.save(device);
                        }
                    }
                     */
                }else if(updateTaskRequest.getIsLogin()==1&&(updateTaskRequest.getTask().equals("login")||updateTaskRequest.getTask().equals("register"))){  ///Check khi login hoặc reg thành công !!!!!!!!!!!!!
                    Device device= deviceRepository.check_DeviceId(updateTaskRequest.getDevice_id().trim());
                    if(device!=null){
                        IpRegister ipRegister_old=ipRegisterRepository.get_Ip_By_Ip_And_Platform(device.getIp_address(),updateTaskRequest.getPlatform().trim());
                        if(ipRegister_old!=null&&updateTaskRequest.getTask().equals("register")){
                            ipRegister_old.setUpdate_time(System.currentTimeMillis());
                            ipRegister_old.setSuccess(true);
                            ipRegisterRepository.save(ipRegister_old);
                        }
                    }
                    //accountProfileRepository.update_Running_By_ProfileId(updateTaskRequest.getDevice_id().trim()+"_"+updateTaskRequest.getProfile_id().trim());
                    if(accountProfile!=null){
                        //accountProfile.setSign_in(0);
                        accountProfile.setRunning(1);
                        if(updateTaskRequest.getTask_key().length()!=0){

                            Account account=accountRepository.get_Account_By_Account_id(accountProfile.getAccount_id().trim());

                            Boolean checkLive=updateTaskRequest.getTask_key().trim().startsWith("@") && TikTokApi.checkLive(updateTaskRequest.getTask_key().trim().replace("@",""));

                            String uniqueId=account!=null?(account.getUuid().length()>5?TikTokApi.checkUsername(account.getUuid(),2):"NULL-LL"): updateTaskRequest.getTask_key().trim();

                            if(updateTaskRequest.getPlatform().equals("tiktok")&& checkLive && updateTaskRequest.getTask_key().trim().contains(uniqueId) ){
                                if(accountProfile.getLogin_time()==0){
                                    accountProfile.setLogin_time(System.currentTimeMillis());
                                }
                                accountProfile.setAccount_id(updateTaskRequest.getTask_key().trim()+"|"+updateTaskRequest.getPlatform().trim());
                                accountProfile.setLive(1);
                            }else if(updateTaskRequest.getPlatform().equals("tiktok") && (!checkLive || uniqueId.equals("NULL-LL"))){
                            accountProfile.setLive(0);
                        }else if(updateTaskRequest.getPlatform().equals("tiktok")){
                                profileTaskRepository.update_Clear_Data_Profile_By_ProfileId(updateTaskRequest.getDevice_id().trim()+"_"+updateTaskRequest.getProfile_id().trim());
                                accountProfile.setLive(0);
                            }else if(updateTaskRequest.getPlatform().equals("facebook")){
                                if(updateTaskRequest.getTask_key().trim().matches("\\d+")){
                                    if(accountProfile.getLogin_time()==0){
                                        accountProfile.setLogin_time(System.currentTimeMillis());
                                    }
                                    accountProfile.setAccount_id(updateTaskRequest.getTask_key().trim()+"|"+updateTaskRequest.getPlatform().trim());
                                    accountProfile.setLive(1);
                                }else{
                                    accountProfile.setLive(0);
                                }
                            }else{
                                if(accountProfile.getLogin_time()==0){
                                    accountProfile.setLogin_time(System.currentTimeMillis());
                                }
                                accountProfile.setAccount_id(updateTaskRequest.getTask_key().trim()+"|"+updateTaskRequest.getPlatform().trim());
                                accountProfile.setLive(1);
                            }
                            accountProfile.setUpdate_time(System.currentTimeMillis());
                            accountProfileRepository.save(accountProfile);

                            if(account==null&&accountProfile.getLive()==1){
                                account=new Account();
                                account.setAccount_id(accountProfile.getAccount_id().trim());
                                account.setLive(1);
                                account.setPassword(accountProfile.getPassword());
                                account.setName(accountProfile.getName());
                                account.setAvatar(accountProfile.getAvatar());
                                account.setRecover_mail(accountProfile.getRecover());
                                account.setPlatform(accountProfile.getPlatform());
                                if(accountProfile.getSign_in()==1){
                                    account.setMode("login");
                                }else{
                                    account.setMode("register");
                                }
                                account.setRunning(1);
                                account.setAuth_2fa("");
                                account.setProfile_id(accountProfile.getProfileTask().getProfile_id());
                                account.setDevice_id(accountProfile.getProfileTask().getDevice().getDevice_id());
                                account.setAdd_time(System.currentTimeMillis());
                                account.setGet_time(System.currentTimeMillis());
                                account.setUpdate_time(System.currentTimeMillis());
                                accountRepository.save(account);
                            }else if(account!=null&&accountProfile.getLive()==1){
                                account.setAccount_id(updateTaskRequest.getTask_key().trim()+"|"+updateTaskRequest.getPlatform().trim());
                                account.setRecover_mail(accountProfile.getRecover());
                                account.setPassword(accountProfile.getPassword());
                                account.setRunning(1);
                                account.setLive(1);
                                account.setProfile_id(accountProfile.getProfileTask().getProfile_id());
                                account.setDevice_id(accountProfile.getProfileTask().getDevice().getDevice_id());
                                accountRepository.save(account);
                            }
                        }else if(updateTaskRequest.getTask_key().length()==0){
                            accountProfile.setLive(0);
                            accountProfile.setUpdate_time(System.currentTimeMillis());
                            accountProfileRepository.save(accountProfile);
                        }
                        if(platform_Dependent!=null&&!updateTaskRequest.getPlatform().equals("youtube")){
                            Account accountDependent =accountRepository.get_Account_By_Account_id(accountProfile.getRecover().trim());
                            if(accountDependent!=null){
                                if(!accountDependent.getPlatform().equals(updateTaskRequest.getPlatform())){
                                    if(!accountDependent.getDependent().contains(updateTaskRequest.getPlatform())){
                                        if(accountDependent.getDependent().length()==0){
                                            accountDependent.setDependent(updateTaskRequest.getPlatform());
                                        }else{
                                            accountDependent.setDependent(accountDependent.getDependent()+","+updateTaskRequest.getPlatform());
                                        }
                                        List<String> arrDie=new ArrayList<>(Arrays.asList(accountDependent.getDie_dependent().split(",")));
                                        arrDie.removeIf(platform -> platform.contains(updateTaskRequest.getPlatform()));
                                        accountDependent.setDie_dependent(String.join(",", arrDie));

                                        accountRepository.save(accountDependent);
                                    }
                                }else{
                                    accountDependent.setLive(updateTaskRequest.getIsLogin());
                                    accountRepository.save(accountDependent);
                                }
                            }
                        }
                    }////

                }else if(updateTaskRequest.getIsLogin()==1&&updateTaskRequest.getTask().equals("sign_in")){  ///Check khi sign_in hoặc reg thành công !!!!!!!!!!!!!
                    Device device= deviceRepository.check_DeviceId(updateTaskRequest.getDevice_id().trim());
                    if(device!=null){
                        IpRegister ipRegister_old=ipRegisterRepository.get_Ip_By_Ip_And_Platform(device.getIp_address(),updateTaskRequest.getPlatform().trim());
                        if(ipRegister_old!=null&&updateTaskRequest.getTask().equals("register")){
                            ipRegister_old.setUpdate_time(System.currentTimeMillis());
                            ipRegister_old.setSuccess(true);
                            ipRegisterRepository.save(ipRegister_old);
                        }
                    }
                    //accountProfileRepository.update_Running_By_ProfileId(updateTaskRequest.getDevice_id().trim()+"_"+updateTaskRequest.getProfile_id().trim());
                    if(accountProfile!=null){
                        accountProfile.setRunning(1);
                        if(updateTaskRequest.getTask_key().length()!=0){

                            Account account=accountRepository.get_Account_By_Account_id(accountProfile.getAccount_id().trim());

                            Boolean checkLive=updateTaskRequest.getTask_key().trim().startsWith("@") && TikTokApi.checkLive(updateTaskRequest.getTask_key().trim().replace("@",""));

                            String uniqueId=account!=null?(account.getUuid().length()>5?TikTokApi.checkUsername(account.getUuid(),2):"NULL-LL"):"NULL-LL";

                            if(updateTaskRequest.getPlatform().equals("tiktok")&& checkLive && updateTaskRequest.getTask_key().trim().contains(uniqueId) ){
                                if(accountProfile.getLogin_time()==0){
                                    accountProfile.setLogin_time(System.currentTimeMillis());
                                }
                                accountProfile.setAccount_id(updateTaskRequest.getTask_key().trim()+"|"+updateTaskRequest.getPlatform().trim());
                                accountProfile.setLive(1);
                            }else if(updateTaskRequest.getPlatform().equals("tiktok") && (!checkLive || uniqueId.equals("NULL-LL"))){
                                accountProfile.setLive(0);
                            }else if(updateTaskRequest.getPlatform().equals("tiktok")){
                                profileTaskRepository.update_Clear_Data_Profile_By_ProfileId(updateTaskRequest.getDevice_id().trim()+"_"+updateTaskRequest.getProfile_id().trim());
                                accountProfile.setLive(0);
                            }else if(updateTaskRequest.getPlatform().equals("facebook")){
                                if(updateTaskRequest.getTask_key().trim().matches("\\d+")){
                                    if(accountProfile.getLogin_time()==0){
                                        accountProfile.setLogin_time(System.currentTimeMillis());
                                    }
                                    accountProfile.setAccount_id(updateTaskRequest.getTask_key().trim()+"|"+updateTaskRequest.getPlatform().trim());
                                    accountProfile.setLive(1);
                                }else{
                                    accountProfile.setLive(0);
                                }
                            }else{
                                if(accountProfile.getLogin_time()==0){
                                    accountProfile.setLogin_time(System.currentTimeMillis());
                                }
                                accountProfile.setAccount_id(updateTaskRequest.getTask_key().trim()+"|"+updateTaskRequest.getPlatform().trim());
                                accountProfile.setLive(1);
                            }
                            accountProfile.setUpdate_time(System.currentTimeMillis());
                            accountProfileRepository.save(accountProfile);

                            if(account!=null&&accountProfile.getLive()==1){
                                account.setAccount_id(updateTaskRequest.getTask_key().trim()+"|"+updateTaskRequest.getPlatform().trim());
                                account.setRecover_mail(accountProfile.getRecover());
                                account.setPassword(accountProfile.getPassword());
                                account.setRunning(1);
                                account.setLive(1);
                                account.setProfile_id(accountProfile.getProfileTask().getProfile_id());
                                account.setDevice_id(accountProfile.getProfileTask().getDevice().getDevice_id());
                                accountRepository.save(account);
                            }
                        }else if(updateTaskRequest.getTask_key().length()==0){
                            accountProfile.setLive(0);
                            accountProfile.setUpdate_time(System.currentTimeMillis());
                            accountProfileRepository.save(accountProfile);
                        }
                    }
                }else if(updateTaskRequest.getIsLogin()>1&&accountProfile.getSign_in()==0){
                    Boolean check_Die=true;
                    if(accountProfile!=null&&accountProfile.getPlatform().equals("tiktok")&&updateTaskRequest.getIsLogin()!=7){
                        check_Die=accountProfile.getPlatform().equals("tiktok")&&!TikTokApi.checkLive(accountProfile.getAccount_id().substring(0,accountProfile.getAccount_id().lastIndexOf("|")).replace("@",""));
                    }
                    if(platform_Dependent!=null&&!updateTaskRequest.getPlatform().equals("youtube")){
                        if((accountProfile.getPlatform().equals("tiktok")&&check_Die) || !accountProfile.getPlatform().equals("tiktok")){
                            Account accountDependent =accountRepository.get_Account_By_Account_id(accountProfile.getRecover().trim());
                            if(accountDependent!=null){

                                if(!accountDependent.getPlatform().equals(updateTaskRequest.getPlatform())){
                                    if(!accountDependent.getDie_dependent().contains(updateTaskRequest.getPlatform())){
                                        if(accountDependent.getDie_dependent().length()==0){
                                            accountDependent.setDie_dependent(updateTaskRequest.getPlatform());
                                        }else{
                                            accountDependent.setDie_dependent(accountDependent.getDie_dependent()+","+updateTaskRequest.getPlatform());
                                        }
                                    }
                                    accountRepository.save(accountDependent);
                                }
                            }
                        }
                    }
                    if(check_Die){
                        Account accountPlatform=accountRepository.get_Account_By_Account_id(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform());
                        if(accountPlatform!=null){
                            if(accountPlatform.getPlatform().equals("youtube")&&!updateTaskRequest.getTask().equals("register")){
                                accountPlatform.setRunning(0);
                                accountPlatform.setUpdate_time(System.currentTimeMillis());
                                accountPlatform.setDevice_id("");
                                accountPlatform.setProfile_id("");
                                accountPlatform.setLive(updateTaskRequest.getIsLogin());
                                accountRepository.save(accountPlatform);
                            }else{
                                if(accountProfile.getLogin_time()!=0){
                                    accountPlatform.setRunning(0);
                                    accountPlatform.setUpdate_time(System.currentTimeMillis());
                                    accountPlatform.setDie_time(System.currentTimeMillis());
                                    accountPlatform.setLive(updateTaskRequest.getIsLogin());
                                    accountRepository.save(accountPlatform);
                                }else{
                                    accountPlatform.setRunning(0);
                                    accountPlatform.setUpdate_time(System.currentTimeMillis());
                                    accountPlatform.setDie_time(System.currentTimeMillis());
                                    accountPlatform.setDevice_id("");
                                    accountPlatform.setProfile_id("");
                                    accountPlatform.setLive(updateTaskRequest.getIsLogin());
                                    accountRepository.save(accountPlatform);
                                }
                            }
                        }
                        if(accountProfile!=null){
                            accountProfileRepository.delete(accountProfile);
                        }
                    }
                    //profileTaskRepository.update_Than_Task_Index_By_AccountId(updateTaskRequest.getPlatform().trim(),updateTaskRequest.getAccount_id()+"%");
                }else if(updateTaskRequest.getIsLogin()>1&&accountProfile.getSign_in()==1){
                    Boolean check_Die=true;
                    if(accountProfile!=null&&accountProfile.getPlatform().equals("tiktok")&&updateTaskRequest.getIsLogin()!=7){
                        check_Die=accountProfile.getPlatform().equals("tiktok")&&!TikTokApi.checkLive(accountProfile.getAccount_id().substring(0,accountProfile.getAccount_id().lastIndexOf("|")).replace("@",""));
                    }
                    if(check_Die){
                        Account accountPlatform=accountRepository.get_Account_By_Account_id(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform());
                        if(accountPlatform!=null){
                            if(accountPlatform.getPlatform().equals("youtube")&&!updateTaskRequest.getTask().equals("register")){
                                accountPlatform.setRunning(0);
                                accountPlatform.setUpdate_time(System.currentTimeMillis());
                                accountPlatform.setDevice_id("");
                                accountPlatform.setProfile_id("");
                                accountPlatform.setLive(updateTaskRequest.getIsLogin());
                                accountRepository.save(accountPlatform);
                            }else{
                                if(accountProfile.getLogin_time()!=0){
                                    accountPlatform.setRunning(0);
                                    accountPlatform.setUpdate_time(System.currentTimeMillis());
                                    accountPlatform.setDie_time(System.currentTimeMillis());
                                    accountPlatform.setLive(updateTaskRequest.getIsLogin());
                                    accountRepository.save(accountPlatform);
                                }else{
                                    accountPlatform.setRunning(0);
                                    accountPlatform.setUpdate_time(System.currentTimeMillis());
                                    accountPlatform.setDie_time(System.currentTimeMillis());
                                    accountPlatform.setDevice_id("");
                                    accountPlatform.setProfile_id("");
                                    accountPlatform.setLive(updateTaskRequest.getIsLogin());
                                    accountRepository.save(accountPlatform);
                                }
                            }
                        }
                        if(accountProfile!=null){
                            accountProfileRepository.delete(accountProfile);
                        }
                    }

                }
            }catch (Exception e){
                StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
                LogError logError =new LogError();
                logError.setMethod_name(stackTraceElement.getMethodName());
                logError.setLine_number(stackTraceElement.getLineNumber());
                logError.setClass_name(stackTraceElement.getClassName());
                logError.setFile_name(stackTraceElement.getFileName() + "| " + updateTaskRequest.getTask_key().trim()+"|"+updateTaskRequest.getPlatform()+ "|"+updateTaskRequest.getIsLogin()+ "|"+updateTaskRequest.getAccount_id()+ "|"+updateTaskRequest.getTask());
                logError.setMessage(e.getMessage());
                logError.setAdd_time(System.currentTimeMillis());
                Date date_time = new Date(System.currentTimeMillis());
                // Tạo SimpleDateFormat với múi giờ GMT+7
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                String formattedDate = sdf.format(date_time);
                logError.setDate_time(formattedDate);
                logErrorRepository.save(logError);
            }
            resp.put("status", true);
            data.put("message", "Update thành công!");
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

    @PostMapping(value = "/updateTaskOFF", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> updateTaskOFF(@RequestHeader(defaultValue = "") String Authorization,
                                                   @RequestBody UpdateTaskRequest updateTaskRequest) {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
            if (checktoken ==0) {
                resp.put("status", false);
                data.put("message", "Token expired");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (updateTaskRequest.getAccount_id().length() == 0) {
                resp.put("status", false);
                data.put("message", "account_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (updateTaskRequest.getTask().length() == 0) {
                resp.put("status", false);
                data.put("message", "task không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (updateTaskRequest.getPlatform().length() == 0) {
                resp.put("status", false);
                data.put("message", "platform không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (updateTaskRequest.getStatus() == null) {
                resp.put("status", false);
                data.put("message", "status không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (updateTaskRequest.getTask_key().length()==0&&!updateTaskRequest.getTask().equals("login")&&!updateTaskRequest.getTask().equals("register")) {
                resp.put("status", false);
                data.put("message", "task_key không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (accountProfileRepository.check_Account_By_AccountId(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim())==0 && !(updateTaskRequest.getPlatform().trim().equals("tiktok")&&updateTaskRequest.getTask().equals("view"))
            ) {
                resp.put("status", false);
                data.put("message", "account_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }


            if(!updateTaskRequest.getTask().equals("login")&&!updateTaskRequest.getTask().equals("register")&&!updateTaskRequest.getTask().equals("sign_in")&&!updateTaskRequest.getTask().equals("update_info")){
                String platform_Check = updateTaskRequest.getPlatform().toLowerCase().trim();
                if(platform_Check.equals("youtube")){
                    if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true){
                        youtubeUpdate.youtube_view(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("subscriber")&&updateTaskRequest.getStatus()==true){
                        youtubeUpdate.youtube_subscriber(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    } else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                        youtubeUpdate.youtube_like(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    } else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("comment")&&updateTaskRequest.getStatus()==true){
                        youtubeUpdate.youtube_comment(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim(),updateTaskRequest.getStatus());
                    }
                }else if(platform_Check.equals("tiktok")){
                    if(updateTaskRequest.getTask().toLowerCase().trim().equals("follower")){
                        tiktokUpdate.tiktok_follower(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim(),updateTaskRequest.getSuccess(),updateTaskRequest.getStatus(),updateTaskRequest.getDevice_id().trim()+"_"+updateTaskRequest.getProfile_id().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                        tiktokUpdate.tiktok_like(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("comment")){
                        tiktokUpdate.tiktok_comment(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim(),updateTaskRequest.getStatus());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true){
                        tiktokUpdate.tiktok_view(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim(),updateTaskRequest.getBonus().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("share")&&updateTaskRequest.getStatus()==true){
                        tiktokUpdate.tiktok_share(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("favorites")&&updateTaskRequest.getStatus()==true){
                        tiktokUpdate.tiktok_favorites(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }
                }else if(platform_Check.equals("facebook")){
                    if(updateTaskRequest.getTask().toLowerCase().trim().equals("follower")&&updateTaskRequest.getStatus()==true){
                        facebookUpdate.facebook_follower(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                        facebookUpdate.facebook_like(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("comment")) {
                        facebookUpdate.facebook_comment(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(), updateTaskRequest.getTask_key().trim(), updateTaskRequest.getStatus());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("member")&&updateTaskRequest.getStatus()==true) {
                        facebookUpdate.facebook_member(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(), updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true) {
                        facebookUpdate.facebook_view(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(), updateTaskRequest.getTask_key().trim());
                    }
                }else if(platform_Check.equals("x")){
                    if(updateTaskRequest.getTask().toLowerCase().trim().equals("follower")&&updateTaskRequest.getStatus()==true){
                        xUpdate.x_follower(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                        xUpdate.x_like(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("comment")) {
                        xUpdate.x_comment(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(), updateTaskRequest.getTask_key().trim(), updateTaskRequest.getStatus());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true) {
                        xUpdate.x_view(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(), updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("repost")&&updateTaskRequest.getStatus()==true) {
                        xUpdate.x_repost(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(), updateTaskRequest.getTask_key().trim());
                    }
                }else if(platform_Check.equals("instagram")){
                    if(updateTaskRequest.getTask().toLowerCase().trim().equals("follower")&&updateTaskRequest.getStatus()==true){
                        instagramUpdate.instagram_follower(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                        instagramUpdate.instagram_like(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("comment")) {
                        instagramUpdate.instagram_comment(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(), updateTaskRequest.getTask_key().trim(), updateTaskRequest.getStatus());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true) {
                        instagramUpdate.instagram_view(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(), updateTaskRequest.getTask_key().trim());
                    }
                }else if(platform_Check.equals("threads")){
                    if(updateTaskRequest.getTask().toLowerCase().trim().equals("follower")&&updateTaskRequest.getStatus()==true){
                        threadsUpdate.threads_follower(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                        threadsUpdate.threads_like(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("comment")) {
                        threadsUpdate.threads_comment(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(), updateTaskRequest.getTask_key().trim(), updateTaskRequest.getStatus());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true) {
                        threadsUpdate.threads_view(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(), updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("repost")&&updateTaskRequest.getStatus()==true) {
                        threadsUpdate.threads_repost(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(), updateTaskRequest.getTask_key().trim());
                    }
                }
                if(updateTaskRequest.getStatus()==true){
                    try {
                        OrderRunning orderRunning= orderRunningRepository.find_Order_By_OrderId(updateTaskRequest.getOrder_id());
                        if(orderRunning!=null){
                            HistorySum historySum=new HistorySum();
                            historySum.setOrderRunning(orderRunning);
                            historySum.setAccount_id(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim());
                            historySum.setViewing_time(updateTaskRequest.getViewing_time());
                            historySum.setAdd_time(System.currentTimeMillis());
                            historySumRepository.save(historySum);
                        }
                    }catch (Exception e){
                    }
                }

            }
            try{
                profileTaskRepository.reset_Thread_By_ProfileId(updateTaskRequest.getDevice_id().trim()+"_"+updateTaskRequest.getProfile_id().trim());
            }catch (Exception e){

            }
            if(updateTaskRequest.getPlatform().equals("tiktok")&&updateTaskRequest.getAccount_id().trim().equals(updateTaskRequest.getDevice_id().trim()+"_"+updateTaskRequest.getProfile_id().trim())&&updateTaskRequest.getTask().equals("view")){
                resp.put("status", true);
                data.put("message", "Update thành công!");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            try{
                AccountProfile accountProfile=accountProfileRepository.get_Account_By_Account_id_And_Platform(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getPlatform().trim());
                String platform_Dependent=platformRepository.get_Dependent_Connection_By_Platform_And_Mode(updateTaskRequest.getPlatform().trim(),accountProfile.getProfileTask().getDevice().getMode());
                if(updateTaskRequest.getTask().equals("add_post") &&updateTaskRequest.getStatus()==true){
                    AccountClone accountClone=accountCloneRepository.get_Account_Clone_By_Account_id(accountProfile.getAccount_id().trim());
                    if(accountClone!=null) {
                        accountClone.setVideo_list(accountClone.getVideo_list()+updateTaskRequest.getTask_key()+",");
                        accountClone.setUpdate_time(System.currentTimeMillis());
                        accountCloneRepository.save(accountClone);
                    }
                }else if(updateTaskRequest.getTask().equals("update_avatar") &&updateTaskRequest.getStatus()==true){
                    if(accountProfile!=null) {
                        accountProfile.setAvatar(1);
                        accountProfileRepository.save(accountProfile);

                        Account account=accountRepository.get_Account_By_Account_id(accountProfile.getAccount_id().trim());
                        if(account!=null){
                            account.setAvatar(1);
                            accountRepository.save(account);
                        }
                    }
                }else if(updateTaskRequest.getTask().equals("update_info") &&updateTaskRequest.getStatus()==true){
                    if(accountProfile!=null) {
                        accountProfile.setPassword(updateTaskRequest.getPassword().trim());
                        accountProfile.setRecover(updateTaskRequest.getRecover_mail().trim());
                        accountProfile.setChanged(1);
                        accountProfile.setCode("");
                        accountProfileRepository.save(accountProfile);

                        Account account=accountRepository.get_Account_By_Account_id(accountProfile.getAccount_id().trim());
                        if(account!=null){
                            account.setPassword(updateTaskRequest.getPassword().trim());
                            account.setRecover_mail(updateTaskRequest.getRecover_mail().trim());
                            account.setChanged(1);
                            account.setChanged_time(System.currentTimeMillis());
                            accountRepository.save(account);
                        }
                    }
                }else if(updateTaskRequest.getIsLogin()==0 || updateTaskRequest.getIsLogin()==-1){
                    //.update_Than_Task_Index_By_AccountId(updateTaskRequest.getPlatform().trim(),updateTaskRequest.getAccount_id()+"|"+updateTaskRequest.getPlatform().trim());
                    if(accountProfile!=null){
                        if(updateTaskRequest.getAccount_id().trim().startsWith("@")&&updateTaskRequest.getPlatform().equals("tiktok")){
                            accountProfile.setLive(0);
                        }else if(!updateTaskRequest.getAccount_id().trim().startsWith("@")&&updateTaskRequest.getPlatform().equals("tiktok")){
                            accountProfile.setLive(-1);
                        }else{
                            if(updateTaskRequest.getTask().equals("register")){
                                accountProfile.setLive(-1);
                            }else{
                                accountProfile.setLive(0);
                            }
                        }
                        accountProfile.setUpdate_time(System.currentTimeMillis());
                        accountProfileRepository.save(accountProfile);
                    }
                    /*
                    if(updateTaskRequest.getIsLogin()==0){
                        Device device= deviceRepository.check_DeviceId(updateTaskRequest.getDevice_id().trim());
                        if(device!=null){
                            device.setReboot(1);
                            deviceRepository.save(device);
                        }
                    }
                     */
                }else if(updateTaskRequest.getIsLogin()==1&&(updateTaskRequest.getTask().equals("login")||updateTaskRequest.getTask().equals("register")||updateTaskRequest.getTask().equals("sign_in"))){  ///Check khi login hoặc reg thành công !!!!!!!!!!!!!
                    Device device= deviceRepository.check_DeviceId(updateTaskRequest.getDevice_id().trim());
                    if(device!=null){
                        IpRegister ipRegister_old=ipRegisterRepository.get_Ip_By_Ip_And_Platform(device.getIp_address(),updateTaskRequest.getPlatform().trim());
                        if(ipRegister_old!=null&&updateTaskRequest.getTask().equals("register")){
                            ipRegister_old.setUpdate_time(System.currentTimeMillis());
                            ipRegister_old.setSuccess(true);
                            ipRegisterRepository.save(ipRegister_old);
                        }
                    }
                    //accountProfileRepository.update_Running_By_ProfileId(updateTaskRequest.getDevice_id().trim()+"_"+updateTaskRequest.getProfile_id().trim());
                    if(accountProfile!=null){
                        accountProfile.setSign_in(0);
                        accountProfile.setRunning(1);
                        if(accountProfile.getLogin_time()==0){
                            accountProfile.setLogin_time(System.currentTimeMillis());
                        }

                        if(updateTaskRequest.getTask_key().length()!=0){

                            if(updateTaskRequest.getPlatform().equals("tiktok")&&
                                    updateTaskRequest.getTask_key().trim().startsWith("@")&&
                                    TikTokApi.getFollowerCount(updateTaskRequest.getTask_key().trim().replace("@",""),1)>=0){

                                AccountProfile accountProfile_Check=accountProfileRepository.get_Account_By_Account_id_And_Platform(updateTaskRequest.getTask_key().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getPlatform().trim());
                                if(accountProfile_Check!=null&&!updateTaskRequest.getTask_key().trim().equals(updateTaskRequest.getAccount_id().trim())){
                                    accountProfileRepository.delete(accountProfile_Check);
                                    accountProfileRepository.delete(accountProfile);
                                    resp.put("status", true);
                                    data.put("message", "Update thành công!");
                                    resp.put("data", data);
                                    return new ResponseEntity<>(resp, HttpStatus.OK);
                                }
                                accountProfile.setAccount_id(updateTaskRequest.getTask_key().trim()+"|"+updateTaskRequest.getPlatform().trim());
                                accountProfile.setLive(1);
                            }else if(updateTaskRequest.getPlatform().equals("tiktok")){
                                accountProfile.setLive(0);
                            }else if(updateTaskRequest.getPlatform().equals("facebook")){
                                if(updateTaskRequest.getTask_key().trim().matches("\\d+")){
                                    accountProfile.setAccount_id(updateTaskRequest.getTask_key().trim()+"|"+updateTaskRequest.getPlatform().trim());
                                    accountProfile.setLive(1);
                                }else{
                                    accountProfile.setLive(0);
                                }
                            }else{
                                accountProfile.setAccount_id(updateTaskRequest.getTask_key().trim()+"|"+updateTaskRequest.getPlatform().trim());
                                accountProfile.setLive(1);
                            }
                            accountProfile.setUpdate_time(System.currentTimeMillis());
                            accountProfileRepository.save(accountProfile);

                            Account account=accountRepository.get_Account_By_Account_id(accountProfile.getAccount_id().trim());

                           if(account==null&&accountProfile.getLive()==1){
                                    account=new Account();
                                    account.setAccount_id(accountProfile.getAccount_id().trim());
                                    account.setLive(1);
                                    account.setPassword(accountProfile.getPassword());
                                    account.setName(accountProfile.getName());
                                    account.setAvatar(accountProfile.getAvatar());
                                    account.setRecover_mail(accountProfile.getRecover());
                                    account.setPlatform(accountProfile.getPlatform());
                                    account.setMode(updateTaskRequest.getTask());
                                    account.setRunning(1);
                                    account.setAuth_2fa("");
                                    account.setProfile_id(accountProfile.getProfileTask().getProfile_id());
                                    account.setDevice_id(accountProfile.getProfileTask().getDevice().getDevice_id());
                                    account.setAdd_time(System.currentTimeMillis());
                                    account.setGet_time(System.currentTimeMillis());
                                    account.setUpdate_time(System.currentTimeMillis());
                                    accountRepository.save(account);
                           }else if(account!=null&&accountProfile.getLive()==1){
                                account.setAccount_id(updateTaskRequest.getTask_key().trim()+"|"+updateTaskRequest.getPlatform().trim());
                                account.setRecover_mail(accountProfile.getRecover());
                                account.setPassword(accountProfile.getPassword());
                                account.setRunning(1);
                                account.setLive(1);
                                account.setProfile_id(accountProfile.getProfileTask().getProfile_id());
                                account.setDevice_id(accountProfile.getProfileTask().getDevice().getDevice_id());
                                accountRepository.save(account);
                           }
                        }else if(updateTaskRequest.getTask_key().length()==0){
                            accountProfile.setLive(0);
                            accountProfile.setUpdate_time(System.currentTimeMillis());
                            accountProfileRepository.save(accountProfile);
                        }
                        if(platform_Dependent!=null&&!updateTaskRequest.getPlatform().equals("youtube")){
                            Account accountDependent =accountRepository.get_Account_By_Account_id(accountProfile.getRecover().trim());
                            if(accountDependent!=null){
                                if(!accountDependent.getPlatform().equals(updateTaskRequest.getPlatform())){
                                    if(!accountDependent.getDependent().contains(updateTaskRequest.getPlatform())){
                                        if(accountDependent.getDependent().length()==0){
                                            accountDependent.setDependent(updateTaskRequest.getPlatform());
                                        }else{
                                            accountDependent.setDependent(accountDependent.getDependent()+","+updateTaskRequest.getPlatform());
                                        }
                                        List<String> arrDie=new ArrayList<>(Arrays.asList(accountDependent.getDie_dependent().split(",")));
                                        arrDie.removeIf(platform -> platform.contains(updateTaskRequest.getPlatform()));
                                        accountDependent.setDie_dependent(String.join(",", arrDie));

                                        accountRepository.save(accountDependent);
                                    }
                                }else{
                                    accountDependent.setLive(updateTaskRequest.getIsLogin());
                                    accountRepository.save(accountDependent);
                                }
                            }
                        }
                    }////

                }else if(updateTaskRequest.getIsLogin()>1){

                    Boolean check_Die_Tiktok=accountProfile.getPlatform().equals("tiktok")&&TikTokApi.checkAccount(accountProfile.getAccount_id().substring(0,accountProfile.getAccount_id().lastIndexOf("|")).replace("@",""),1)==-1;
                    if(platform_Dependent!=null&&!updateTaskRequest.getPlatform().equals("youtube")){
                        if((accountProfile.getPlatform().equals("tiktok")&&check_Die_Tiktok) || !accountProfile.getPlatform().equals("tiktok")){
                            Account accountDependent =accountRepository.get_Account_By_Account_id(accountProfile.getRecover().trim());
                            if(accountDependent!=null){

                                if(!accountDependent.getPlatform().equals(updateTaskRequest.getPlatform())){
                                    if(!accountDependent.getDie_dependent().contains(updateTaskRequest.getPlatform())){
                                        if(accountDependent.getDie_dependent().length()==0){
                                            accountDependent.setDie_dependent(updateTaskRequest.getPlatform());
                                        }else{
                                            accountDependent.setDie_dependent(accountDependent.getDie_dependent()+","+updateTaskRequest.getPlatform());
                                        }
                                    }
                                    accountRepository.save(accountDependent);
                                }
                            }
                        }
                    }
                    Account accountPlatform=accountRepository.get_Account_By_Account_id(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform());
                    if(accountPlatform!=null){
                        if(accountPlatform.getPlatform().equals("youtube")&&!updateTaskRequest.getTask().equals("register")){
                            accountPlatform.setRunning(0);
                            accountPlatform.setUpdate_time(System.currentTimeMillis());
                            accountPlatform.setProfile_id("");
                            accountPlatform.setProfile_id("");
                            accountPlatform.setLive(updateTaskRequest.getIsLogin());
                            accountRepository.save(accountPlatform);
                        }else{
                            if((accountPlatform.getPlatform().equals("tiktok")&&check_Die_Tiktok) || !accountPlatform.getPlatform().equals("tiktok")){// platform=tiktok & acc die
                                accountPlatform.setRunning(0);
                                accountPlatform.setUpdate_time(System.currentTimeMillis());
                                accountPlatform.setDie_time(System.currentTimeMillis());
                                accountPlatform.setLive(updateTaskRequest.getIsLogin());
                                accountRepository.save(accountPlatform);
                            }else{
                                accountPlatform.setRunning(0);
                                accountPlatform.setUpdate_time(System.currentTimeMillis());
                                accountPlatform.setProfile_id("");
                                accountPlatform.setProfile_id("");
                                accountPlatform.setLive(updateTaskRequest.getIsLogin());
                                accountRepository.save(accountPlatform);
                            }
                        }
                    }
                    //profileTaskRepository.update_Than_Task_Index_By_AccountId(updateTaskRequest.getPlatform().trim(),updateTaskRequest.getAccount_id()+"%");
                    if(accountProfile!=null){
                        if(updateTaskRequest.getPlatform().trim().equals("tiktok")){
                            profileTaskRepository.update_Valid_Profile_By_ProfileId(0,updateTaskRequest.getDevice_id().trim()+"_"+updateTaskRequest.getProfile_id().trim());
                            AccountProfile accountProfile_Check=accountProfileRepository.get_Account_By_ProfileId_And_Platform(accountProfile.getProfileTask().getProfile_id().trim(),"youtube");
                            if(accountProfile_Check!=null){
                                accountProfileRepository.delete(accountProfile_Check);
                            }
                        }
                        accountProfileRepository.delete(accountProfile);
                    }
                }
            }catch (Exception e){
                StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
                LogError logError =new LogError();
                logError.setMethod_name(stackTraceElement.getMethodName());
                logError.setLine_number(stackTraceElement.getLineNumber());
                logError.setClass_name(stackTraceElement.getClassName());
                logError.setFile_name(stackTraceElement.getFileName() + "| " + updateTaskRequest.getTask_key().trim()+"|"+updateTaskRequest.getPlatform()+ "|"+updateTaskRequest.getIsLogin()+ "|"+updateTaskRequest.getAccount_id()+ "|"+updateTaskRequest.getTask());
                logError.setMessage(e.getMessage());
                logError.setAdd_time(System.currentTimeMillis());
                Date date_time = new Date(System.currentTimeMillis());
                // Tạo SimpleDateFormat với múi giờ GMT+7
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                String formattedDate = sdf.format(date_time);
                logError.setDate_time(formattedDate);
                logErrorRepository.save(logError);
            }
            resp.put("status", true);
            data.put("message", "Update thành công!");
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

    @GetMapping(value = "resetTaskError", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> resetTaskError() throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            profileTaskRepository.reset_Task_Error();
            resp.put("status",true);
            data.put("message", "reset thành công");
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

    @GetMapping(value = "resetTaskDevice", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> resetTaskDevice(@RequestHeader(defaultValue = "") String Authorization,
                                                              @RequestParam(defaultValue = "") String device_id) throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            profileTaskRepository.reset_Task_By_DeviceId(device_id.trim());
            resp.put("status",true);
            data.put("message", "reset thành công");
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

    @GetMapping(value = "resetTaskProfile", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> resetTaskProfile(@RequestHeader(defaultValue = "") String Authorization,
                                                                @RequestParam(defaultValue = "") String device_id,
                                                               @RequestParam(defaultValue = "") String profile_id) throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            profileTaskRepository.reset_Task_By_ProfileId(device_id.trim()+"_"+profile_id);
            resp.put("status",true);
            data.put("message", "reset thành công");
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

    @GetMapping(value = "get_Task_7D", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get_Task_7D(@RequestParam(defaultValue = "") String user) {
        JSONObject resp = new JSONObject();
        try {
            List<String> task_7_Day = taskSumRepository.get_Task_7d();
            JSONArray jsonArray = new JSONArray();
            Long max_Task = 0L;

            for (int i = 0; i < task_7_Day.size(); i++) {
                    if (max_Task < Float.parseFloat(task_7_Day.get(i).split(",")[1])) {
                        max_Task = Long.parseLong(task_7_Day.get(i).split(",")[1]);
                    }
            }
            for (int i = 0; i < task_7_Day.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("date", task_7_Day.get(i).split(",")[0]);
                obj.put("task", Long.parseLong(task_7_Day.get(i).split(",")[1]));
                obj.put("max_task", max_Task);

                jsonArray.add(obj);
            }
            resp.put("view7day", jsonArray);

            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

}

package com.nts.awspremium.controller;

import com.nts.awspremium.StringUtils;
import com.nts.awspremium.model.*;
import com.nts.awspremium.model_system.MySQLCheck;
import com.nts.awspremium.model_system.OrderThreadCheck;
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
import com.nts.awspremium.MailApi;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.query.JSqlParserUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
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
    private HistoryRegisterRepository historyRegisterRepository;

    @Autowired
    private AccountNameRepository accountNameRepository;

    @Autowired
    private ModeRepository modeRepository;

    @Autowired
    private ModeOptionRepository modeOptionRepository;

    @Autowired
    private AccountTaskRepository accountTaskRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private OrderThreadCheck orderThreadCheck;
    @Transactional
    @GetMapping(value = "getTask006", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> getTask006(@RequestHeader(defaultValue = "") String Authorization,
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
            Device device=deviceRepository.check_DeviceIdLock(device_id.trim());
            ProfileTask profileTask=null;
            if(device==null){
                resp.put("status", false);
                data.put("message", "device_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }else if((System.currentTimeMillis()-device.getUpdate_time())/1000<settingSystem.getTime_waiting_task()&&!device.getMode().contains("dev")){
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
                if(Long.parseLong(ver_Device)<Long.parseLong(ver_System)){
                    device.setUpdate_time(System.currentTimeMillis());
                    deviceRepository.save(device);
                    resp.put("status", false);
                    data.put("message", "device_id chưa cập nhật version auto");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }

            }else if(device.getReboot()==1 || (System.currentTimeMillis()-device.getReboot_time())/1000/60>=settingSystem.getReboot_time() ){
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

            Mode mode =modeRepository.get_Mode_Info(device.getMode().trim());
            if(mode==null){
                resp.put("status", false);
                data.put("message", "mode không hợp lệ");
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
                if(profileTaskRepository.get_Count_Profile_Valid_0_By_DeviceId(device_id.trim())>0){
                    String profile_remove=profileTaskRepository.get_ProfileId_Valid_0_By_DeviceId(device_id.trim());
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "remove_profile");
                    data.put("profile_id",Integer.parseInt(profile_remove.split(device_id.trim()+"_")[1]));
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
                if(device.getNum_profile()<mode.getMax_profile()){
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "create_profile");
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
                        //profileTask.setOnline_time(System.currentTimeMillis());
                        //profileTaskRepository.save(profileTask);
                        //profileTask.setReboot(1);
                        //profileTaskRepository.save(profileTask);
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
                resp.put("status", true);
                data.put("platform", "system");
                data.put("task", "clear_data");
                data.put("task_key", settingSystem.getClear_data_package().trim());
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            //changer profile  khi du thoi gian hoạt động
            if((System.currentTimeMillis()-profileTask.getOnline_time())/1000/60>=mode.getTime_profile() && profileTask.getState()==1 &&!mode.getMode().contains("dev")) {
                profileTaskRepository.reset_Thread_Index_By_DeviceId_While_ChangerProfile(device_id.trim());
                entityManager.clear();
                if (profileTaskRepository.get_Count_Profile_Enabled(device_id.trim()) > 1) {
                    profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim(),profileTask.getProfile_id());
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "profile_changer");
                    data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim() + "_")[1]));
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else{
                    profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                }
            }else if((System.currentTimeMillis()-profileTask.getOnline_time())/1000/60>=mode.getTime_profile() && profileTask.getState()==1 &&mode.getMode().contains("dev")) {
                profileTaskRepository.reset_Thread_Index_By_DeviceId_While_ChangerProfile(device_id.trim());
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
            }else{
                profileTask.setTiktok_lite_version(tiktok_lite_version);
                profileTask.setTask_time(System.currentTimeMillis());
                profileTask.setUpdate_time(System.currentTimeMillis());
                profileTaskRepository.save(profileTask);
            }

            //profileTask=profileTaskRepository.get_Profile_By_ProfileId(profileTask.getProfile_id());

            //check acc youtube
            Platform platform_Youtube_Check=platformRepository.get_Platform_By_Platform_And_Mode("youtube",device.getMode().trim());
            if(accountProfileRepository.check_AccountLive_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube")==0){
                AccountProfile accountProfile_Check=accountProfileRepository.get_Account_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube");
                if(accountProfile_Check==null){ // If account null or not live then get new acc
                    if(accountRepository.check_Count_AccountDie24H_By_Platform_And_ProfileId(profileTask.getProfile_id().trim(),"youtube")<3&& //check acc die in 24h
                            platform_Youtube_Check.getLogin_account()==1&& // check task login acc
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
                            accountProfile.setName(account_get.getName());
                            accountProfile.setPlatform("youtube");
                            accountProfile.setLive(0);
                            accountProfile.setChanged(0);
                            accountProfile.setAuth_2fa(account_get.getAuth_2fa());
                            accountProfile.setProfileTask(profileTask);
                            accountProfile.setAdd_time(System.currentTimeMillis());
                            accountProfile.setUpdate_time(0L);
                            accountProfileRepository.save(accountProfile);

                            resp.put("status", true);
                            data.put("platform", "youtube");
                            data.put("app", "youtube");
                            data.put("task", "login");
                            data.put("task_key", account_get.getAccount_id().substring(0,account_get.getAccount_id().lastIndexOf("|")));
                            data.put("account_id",account_get.getAccount_id().substring(0,account_get.getAccount_id().lastIndexOf("|")));
                            data.put("password", account_get.getPassword().trim());
                            data.put("name", account_get.getName());
                            data.put("avatar", account_get.getAvatar()==0?false:true);
                            data.put("recover_mail", account_get.getRecover_mail().trim());
                            data.put("auth_2fa", account_get.getAuth_2fa().trim());
                            resp.put("data",data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }else{
                            resp.put("status", false);
                            data.put("message", "Không get được tài khoản youtube");
                            resp.put("data", data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }
                    }else if(accountRepository.check_Count_AccountDie24H_By_Platform_And_ProfileId(profileTask.getProfile_id().trim(),"youtube")<3){// Reset enabled profile and enable profile new
                            if(profileTaskRepository.count_Profile(device.getDevice_id().trim())>1){
                                // off profile
                                profileTask.setEnabled_time(0L);
                                //profileTask.setValid(0);
                                profileTask.setEnabled(0);
                                profileTaskRepository.save(profileTask);

                                ProfileTask profileTask_Check =profileTaskRepository.get_Profile_Rand_Enable0_And_NotIn(profileTask.getProfile_id(),device.getDevice_id().trim());
                                if (profileTask_Check !=null){
                                    profileTaskRepository.reset_Thread_Index_By_DeviceId_While_ChangerProfile(device_id.trim());
                                    entityManager.clear();
                                    profileTask_Check.setEnabled(1);
                                    profileTask_Check.setEnabled_time(System.currentTimeMillis());
                                    profileTaskRepository.save(profileTask_Check);
                                    resp.put("status", true);
                                    data.put("platform", "system");
                                    data.put("task", "profile_changer");
                                    data.put("profile_id", Integer.parseInt(profileTask_Check.getProfile_id().split(device_id.trim()+"_")[1]));
                                    resp.put("data",data);
                                    return new ResponseEntity<>(resp, HttpStatus.OK);
                                }else{
                                    resp.put("status", false);
                                    data.put("message", "Không đủ điều kiện get tài khoản youtube");
                                    resp.put("data", data);
                                    return new ResponseEntity<>(resp, HttpStatus.OK);
                                }
                            }else{
                                resp.put("status", false);
                                data.put("message", "Không đủ điều kiện get tài khoản youtube");
                                resp.put("data", data);
                                return new ResponseEntity<>(resp, HttpStatus.OK);
                            }
                    }else{
                        resp.put("status", false);
                        data.put("message", "Không đủ điều kiện get tài khoản youtube");
                        resp.put("data", data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }

                }else if(accountProfile_Check.getLive()!=1) {

                    if(profileTask.getRequest_index()>1){ //giới hạn số lần trả mail trong một lần mở profile

                        if (profileTaskRepository.get_Count_Profile_Enabled(device_id.trim()) > 1) {
                            profileTaskRepository.reset_Thread_Index_By_DeviceId_While_ChangerProfile(device_id.trim());
                            entityManager.clear();
                            profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim(),profileTask.getProfile_id().trim());
                            //profileTask.setReboot(1);
                            //profileTaskRepository.save(profileTask);
                            resp.put("status", true);
                            data.put("platform", "system");
                            data.put("task", "profile_changer");
                            data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim() + "_")[1]));
                            resp.put("data", data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        } else if (profileTaskRepository.get_Count_Profile_Enabled(device_id.trim()) == 1) {

                            profileTask.setRequest_index(1);
                            profileTaskRepository.save(profileTask);
                            resp.put("status", true);
                            data.put("platform", "youtube");
                            data.put("app", "youtube");
                            if (accountProfile_Check.getAccount_id().contains("register")) {
                                data.put("task", "register");
                            } else {
                                data.put("task", "login");
                            }
                            data.put("task_key", accountProfile_Check.getAccount_id().substring(0, accountProfile_Check.getAccount_id().lastIndexOf("|")));
                            data.put("account_id", accountProfile_Check.getAccount_id().substring(0, accountProfile_Check.getAccount_id().lastIndexOf("|")));
                            data.put("password", accountProfile_Check.getPassword().trim());
                            data.put("name", accountProfile_Check.getName());
                            data.put("avatar", accountProfile_Check.getAvatar() == 0 ? false : true);
                            data.put("recover_mail", accountProfile_Check.getRecover().trim());
                            data.put("auth_2fa", accountProfile_Check.getAuth_2fa().trim());
                            resp.put("data", data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);

                        } else {
                            resp.put("status", false);
                            data.put("message", "Đợi time login || register tài khoản youtube");
                            resp.put("data", data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }
                    }else{
                        profileTask.setRequest_index(profileTask.getRequest_index()+1);
                        profileTaskRepository.save(profileTask);
                        resp.put("status", true);
                        data.put("platform", "youtube");
                        data.put("app", "youtube");
                        if(accountProfile_Check.getAccount_id().contains("register")){
                            data.put("task", "register");
                        }else{
                            data.put("task", "login");
                        }
                        data.put("task_key", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().lastIndexOf("|")));
                        data.put("account_id", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().lastIndexOf("|")));
                        data.put("password", accountProfile_Check.getPassword().trim());
                        data.put("name", accountProfile_Check.getName());
                        data.put("avatar", accountProfile_Check.getAvatar()==0?false:true);
                        data.put("recover_mail", accountProfile_Check.getRecover().trim());
                        data.put("auth_2fa", accountProfile_Check.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }

                }
            }else if(platform_Youtube_Check.getRegister_account()==1&&platform_Youtube_Check.getState()==1&&profileTask.getRegister_index()==0) {
                AccountProfile accountProfile=accountProfileRepository.get_Account_By_Account_id_And_Platform("register_"+profileTask.getProfile_id()+"|youtube","youtube");
                if(historyRegisterRepository.count_Register_By_Platform_And_DeviceId("youtube",device.getDevice_id().trim()+"%",platform_Youtube_Check.getRegister_time())==0&&
                        accountRepository.check_Count_Register_LessDay_By_DeviceId_And_Platform(device.getDevice_id().trim(),"youtube",7)<platform_Youtube_Check.getRegister_limit()&&
                        accountRepository.check_Count_Register_LessDay_By_ProfileId_And_Platform(device.getDevice_id().trim(),"youtube",7)==0&&
                        accountProfileRepository.count_Register_Task_By_Platform_And_DeviceId("youtube",device.getDevice_id()+"%")==0&&
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
                    accountProfile.setRecover("");
                    accountProfile.setPlatform("youtube");
                    accountProfile.setLive(-1);
                    accountProfile.setChanged(0);
                    accountProfile.setAuth_2fa("");
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
                    resp.put("status", true);
                    data.put("platform","youtube");
                    data.put("app","youtube");
                    data.put("task", "register");
                    data.put("task_key", accountProfile.getAccount_id().substring(0,accountProfile.getAccount_id().lastIndexOf("|")));
                    data.put("account_id",accountProfile.getAccount_id().substring(0,accountProfile.getAccount_id().lastIndexOf("|")));
                    data.put("password",accountProfile.getPassword());
                    data.put("name",accountProfile.getName());
                    data.put("avatar",accountProfile.getAvatar()==0?false:true);
                    data.put("recover_mail",  accountProfile.getRecover().trim());
                    data.put("auth_2fa", "");
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else if(accountProfile!=null){
                    if(accountProfile.getLive()!=-1){
                        Account account =accountRepository.get_Account_By_Account_id(accountProfile.getAccount_id());
                        accountRepository.delete(account);
                        accountProfileRepository.delete(accountProfile);
                    }else{
                        resp.put("status", true);
                        data.put("platform","youtube");
                        data.put("app","youtube");
                        data.put("task", "register");
                        data.put("task_key", accountProfile.getAccount_id().substring(0,accountProfile.getAccount_id().lastIndexOf("|")));
                        data.put("account_id",accountProfile.getAccount_id().substring(0,accountProfile.getAccount_id().lastIndexOf("|")));
                        data.put("password",accountProfile.getPassword());
                        data.put("name",accountProfile.getName());
                        data.put("avatar",accountProfile.getAvatar()==0?false:true);
                        data.put("recover_mail",  accountProfile.getRecover().trim());
                        data.put("auth_2fa", "");
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
            if(profileTask!=null){
                if(accountProfileRepository.check_AccountLive_By_ProfileId_And_Platform(profileTask.getProfile_id(),profileTask.getPlatform())==0&&profileTask.getRequest_index()==1){
                    if(profileTask.getTask_list().trim().length()==0){
                        profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                        entityManager.clear();
                        profileTask=null;
                    }else{
                        String task_List = profileTask.getTask_list();
                        List<String> arrPlatform = new ArrayList<>(Arrays.asList(task_List.split(",")));
                        profileTask.setPlatform(arrPlatform.get(0));
                        List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                        task_List = String.join(",", subPlatform);
                        profileTask.setTask_list(task_List);
                        profileTask.setRequest_index(0);
                        profileTaskRepository.save(profileTask);
                    }
                }
                if(profileTask!=null){
                    AccountProfile accountProfile_Check_Platform=accountProfileRepository.get_Account_By_ProfileId_And_Platform(profileTask.getProfile_id(), profileTask.getPlatform());
                    if(accountProfile_Check_Platform==null){
                        Platform platform_Check=platformRepository.get_Platform_By_Platform_And_Mode(profileTask.getPlatform().trim(),device.getMode().trim());
                        Boolean check_GetAccount=true;
                        AccountProfile accountProfile_Dependent=null;
                        Account account_Dependent=null;
                        if(platform_Check.getDependent().trim().length()!=0){
                            accountProfile_Dependent=accountProfileRepository.get_Account_By_ProfileId_And_Platform(profileTask.getProfile_id(),platform_Check.getDependent().trim());
                            if(accountProfile_Dependent==null){
                                check_GetAccount=false;
                            }
                            account_Dependent=accountRepository.get_Account_By_ProfileId_And_Platfrom(profileTask.getProfile_id(),platform_Check.getDependent().trim());
                            if(account_Dependent==null){
                                check_GetAccount=false;
                            }else{
                                if(account_Dependent.getDie_dependent().contains(profileTask.getPlatform())){
                                    check_GetAccount=false;
                                }
                            }
                        }else{

                            check_GetAccount=true;
                        }


                        if(!check_GetAccount){
                            if(profileTask.getTask_list().trim().length()==0){
                                profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                                entityManager.clear();
                                profileTask=null;
                            }else{
                                String task_List = profileTask.getTask_list();
                                List<String> arrPlatform = new ArrayList<>(Arrays.asList(task_List.split(",")));
                                profileTask.setPlatform(arrPlatform.get(0));
                                List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                                task_List = String.join(",", subPlatform);
                                profileTask.setTask_list(task_List);
                                profileTask.setRequest_index(0);
                                profileTaskRepository.save(profileTask);
                            }
                        }else{
                            //Platform platform_Check=platformRepository.get_Platform_By_Platform_And_Mode(profileTask.getPlatform().trim(),device.getMode().trim());
                            //check time reg gan nhat
                            if(platform_Check.getRegister_account()==1 &&
                                    historyRegisterRepository.count_Register_By_Platform_And_DeviceId(profileTask.getPlatform().trim(),device.getDevice_id().trim()+"%",platform_Check.getRegister_time())==0&&
                                    accountRepository.check_Count_Register_LessDay_By_DeviceId_And_Platform(device.getDevice_id().trim(),profileTask.getPlatform().trim(),7)<platform_Check.getRegister_limit()
                            ){
                                //gioi han time reg by platform and time
                                List<String> list_device =deviceRepository.get_All_Device_By_IP(device.getIp_address().trim());
                                if(historyRegisterRepository.count_Register_By_Platform_And_Time(profileTask.getPlatform().trim(),list_device,10)>0){
                                    if(profileTask.getTask_list().trim().length()==0){
                                        profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                                        entityManager.clear();
                                        profileTask=null;
                                    }else{
                                        String task_List = profileTask.getTask_list();
                                        List<String> arrPlatform = new ArrayList<>(Arrays.asList(task_List.split(",")));
                                        profileTask.setPlatform(arrPlatform.get(0));
                                        List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                                        task_List = String.join(",", subPlatform);
                                        profileTask.setTask_list(task_List);
                                        profileTask.setRequest_index(0);
                                        profileTaskRepository.save(profileTask);
                                    }
                                }else if(platform_Check.getDependent().trim().length()>0&&platform_Check.getConnection_account()==1){
                                    AccountProfile accountCheck=accountProfileRepository.get_Account_By_Account_id_And_Platform(accountProfile_Dependent.getAccount_id().substring(0,accountProfile_Dependent.getAccount_id().lastIndexOf("|"))+"|"+profileTask.getPlatform(),profileTask.getPlatform());
                                    if(accountCheck!=null){
                                        accountProfileRepository.delete(accountCheck);
                                    }
                                    AccountProfile accountProfile=new AccountProfile();
                                    accountProfile.setAccount_id(accountProfile_Dependent.getAccount_id().substring(0,accountProfile_Dependent.getAccount_id().lastIndexOf("|"))+"|"+profileTask.getPlatform());
                                    accountProfile.setPassword(accountProfile_Dependent.getPassword().trim());
                                    if(profileTask.getPlatform().equals("tiktok")){
                                        AccountName accountName=accountNameRepository.get_AcountName_By_Platform("tiktok");
                                        accountProfile.setName(accountName.getName());
                                        accountNameRepository.delete(accountName);
                                    }else{
                                        accountProfile.setName("");
                                    }
                                    accountProfile.setAvatar(0);
                                    accountProfile.setRecover(accountProfile_Dependent.getRecover());
                                    accountProfile.setPlatform(profileTask.getPlatform());
                                    accountProfile.setLive(-1);
                                    accountProfile.setChanged(0);
                                    accountProfile.setAuth_2fa("");
                                    accountProfile.setProfileTask(profileTask);
                                    accountProfile.setAdd_time(System.currentTimeMillis());
                                    accountProfile.setUpdate_time(0L);
                                    accountProfileRepository.save(accountProfile);

                                    HistoryRegister historyRegister=new HistoryRegister();
                                    historyRegister.setProfileTask(profileTask);
                                    historyRegister.setPlatform(profileTask.getPlatform().trim());
                                    historyRegister.setState(0);
                                    historyRegister.setUpdate_time(System.currentTimeMillis());
                                    historyRegisterRepository.save(historyRegister);

                                    profileTask.setRequest_index(1);
                                    profileTaskRepository.save(profileTask);
                                    resp.put("status", true);
                                    data.put("platform", profileTask.getPlatform());
                                    if( profileTask.getPlatform().equals("tiktok")){
                                        if(device.getMode().contains("tiktok-lite")){
                                            data.put("app","tiktok-lite");
                                        }else{
                                            data.put("app","tiktok-lite");
                                        }
                                    }else{
                                        data.put("app",profileTask.getPlatform());
                                    }
                                    data.put("task", "register");
                                    data.put("task_key", accountProfile_Dependent.getAccount_id().substring(0,accountProfile_Dependent.getAccount_id().lastIndexOf("|")));
                                    data.put("account_id",  accountProfile_Dependent.getAccount_id().substring(0,accountProfile_Dependent.getAccount_id().lastIndexOf("|")));
                                    data.put("password",accountProfile_Dependent.getPassword().trim());
                                    data.put("recover_mail",  accountProfile_Dependent.getRecover());
                                    data.put("name",  accountProfile.getName());
                                    data.put("avatar", accountProfile.getAvatar()==0?false:true);
                                    data.put("auth_2fa", "");
                                    resp.put("data",data);
                                    return new ResponseEntity<>(resp, HttpStatus.OK);

                                }else if(platform_Check.getConnection_account()==0){

                                    HistoryRegister historyRegister=new HistoryRegister();
                                    historyRegister.setProfileTask(profileTask);
                                    historyRegister.setPlatform(profileTask.getPlatform().trim());
                                    historyRegister.setState(0);
                                    historyRegister.setUpdate_time(System.currentTimeMillis());
                                    historyRegisterRepository.save(historyRegister);

                                    String password="Cmc#";
                                    String passrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                                    for(int i=0;i<6;i++){
                                        Integer ranver=ran.nextInt(passrand.length());
                                        password=password+passrand.charAt(ranver);
                                    }
                                    if(platform_Check.getDependent().trim().length()==0){
                                        JSONArray domains= MailApi.getDomains();
                                        String stringrand="abcdefhijkprstuvwx0123456789";
                                        String mail="";
                                        Boolean success=false;
                                        while (!success&&accountRepository.check_Count_By_AccountId(mail)==0){
                                            for(int i=0;i<20;i++){
                                                Integer ranver=ran.nextInt(stringrand.length());
                                                mail=mail+stringrand.charAt(ranver);
                                            }
                                            mail=mail+"@"+domains.get(ran.nextInt(domains.size()));
                                            success=MailApi.createMail(mail);

                                        }
                                        if(success){

                                            AccountProfile accountCheck=accountProfileRepository.get_Account_By_Account_id_And_Platform(mail+"|"+profileTask.getPlatform(),profileTask.getPlatform());
                                            if(accountCheck!=null){
                                                accountProfileRepository.delete(accountCheck);
                                            }

                                            AccountProfile accountProfile=new AccountProfile();
                                            accountProfile.setAccount_id(mail+"|"+profileTask.getPlatform());
                                            if(profileTask.getPlatform().equals("tiktok")){
                                                AccountName accountName=accountNameRepository.get_AcountName_By_Platform("tiktok");
                                                accountProfile.setName(accountName.getName());
                                                accountNameRepository.delete(accountName);
                                            }else{
                                                accountProfile.setName("");
                                            }
                                            accountProfile.setAvatar(0);
                                            accountProfile.setPassword(password);
                                            accountProfile.setRecover(mail);
                                            accountProfile.setPlatform(profileTask.getPlatform());
                                            accountProfile.setLive(-1);
                                            accountProfile.setChanged(0);
                                            accountProfile.setAuth_2fa("");
                                            accountProfile.setProfileTask(profileTask);
                                            accountProfile.setAdd_time(System.currentTimeMillis());
                                            accountProfile.setUpdate_time(0L);
                                            accountProfileRepository.save(accountProfile);

                                            profileTask.setRequest_index(1);
                                            profileTaskRepository.save(profileTask);
                                            resp.put("status", true);
                                            data.put("platform", profileTask.getPlatform());
                                            if( profileTask.getPlatform().equals("tiktok")){
                                                if(device.getMode().contains("tiktok-lite")){
                                                    data.put("app","tiktok-lite");
                                                }else{
                                                    data.put("app","tiktok-lite");
                                                }
                                            }else{
                                                data.put("app",profileTask.getPlatform());
                                            }
                                            data.put("task", "register");
                                            data.put("task_key", mail);
                                            data.put("account_id", mail);
                                            data.put("password", password);
                                            data.put("name", accountProfile.getName());
                                            data.put("avatar", accountProfile.getAvatar()==0?false:true);
                                            data.put("recover_mail", mail);
                                            data.put("auth_2fa", "");
                                            resp.put("data",data);
                                            return new ResponseEntity<>(resp, HttpStatus.OK);
                                        }
                                    }else{

                                        AccountProfile accountCheck=accountProfileRepository.get_Account_By_Account_id_And_Platform(account_Dependent.getAccount_id().substring(0,account_Dependent.getAccount_id().lastIndexOf("|"))+"|"+profileTask.getPlatform(),profileTask.getPlatform());
                                        if(accountCheck!=null){
                                            accountProfileRepository.delete(accountCheck);
                                        }

                                        AccountProfile accountProfile=new AccountProfile();
                                        accountProfile.setAccount_id(account_Dependent.getAccount_id().substring(0,account_Dependent.getAccount_id().lastIndexOf("|"))+"|"+profileTask.getPlatform());
                                        accountProfile.setPassword(password);
                                        if(profileTask.getPlatform().equals("tiktok")){
                                            AccountName accountName=accountNameRepository.get_AcountName_By_Platform("tiktok");
                                            accountProfile.setName(accountName.getName());
                                            accountNameRepository.delete(accountName);
                                        }else{
                                            accountProfile.setName("");
                                        }
                                        accountProfile.setAvatar(0);
                                        accountProfile.setRecover(account_Dependent.getRecover_mail().trim());
                                        accountProfile.setPlatform(profileTask.getPlatform());
                                        accountProfile.setLive(-1);
                                        accountProfile.setChanged(0);
                                        accountProfile.setAuth_2fa("");
                                        accountProfile.setProfileTask(profileTask);
                                        accountProfile.setAdd_time(System.currentTimeMillis());
                                        accountProfile.setUpdate_time(0L);
                                        accountProfileRepository.save(accountProfile);

                                        profileTask.setRequest_index(1);
                                        profileTaskRepository.save(profileTask);
                                        resp.put("status", true);
                                        data.put("platform", profileTask.getPlatform());
                                        if( profileTask.getPlatform().equals("tiktok")){
                                            if(device.getMode().contains("tiktok-lite")){
                                                data.put("app","tiktok-lite");
                                            }else{
                                                data.put("app","tiktok-lite");
                                            }
                                        }else{
                                            data.put("app",profileTask.getPlatform());
                                        }
                                        data.put("task", "register");
                                        data.put("task_key",  account_Dependent.getAccount_id().substring(0,account_Dependent.getAccount_id().lastIndexOf("|")));
                                        data.put("account_id", account_Dependent.getAccount_id().substring(0,account_Dependent.getAccount_id().lastIndexOf("|")));
                                        data.put("password", password);
                                        data.put("name", accountProfile.getName());
                                        data.put("avatar", accountProfile.getAvatar()==0?false:true);
                                        data.put("recover_mail",account_Dependent.getRecover_mail().trim());
                                        data.put("auth_2fa", "");
                                        resp.put("data",data);
                                        return new ResponseEntity<>(resp, HttpStatus.OK);
                                    }
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
                                    accountProfile.setAuth_2fa(account_get.getAuth_2fa());
                                    accountProfile.setProfileTask(profileTask);
                                    accountProfile.setAdd_time(System.currentTimeMillis());
                                    accountProfile.setUpdate_time(0L);
                                    accountProfileRepository.save(accountProfile);

                                    resp.put("status", true);
                                    data.put("platform", profileTask.getPlatform().trim());
                                    if( profileTask.getPlatform().equals("tiktok")){
                                        if(device.getMode().contains("tiktok-lite")){
                                            data.put("app","tiktok-lite");
                                        }else{
                                            data.put("app","tiktok-lite");
                                        }
                                    }else{
                                        data.put("app",profileTask.getPlatform());
                                    }
                                    data.put("task", "login");
                                    data.put("task_key", account_get.getAccount_id().substring(0,account_get.getAccount_id().lastIndexOf("|")));
                                    data.put("account_id",account_get.getAccount_id().substring(0,account_get.getAccount_id().lastIndexOf("|")));
                                    data.put("password", account_get.getPassword().trim());
                                    data.put("name", account_get.getName().trim());
                                    data.put("avatar", accountProfile.getAvatar()==0?false:true);
                                    data.put("recover_mail", account_get.getRecover_mail().trim());
                                    data.put("auth_2fa", account_get.getAuth_2fa().trim());
                                    resp.put("data",data);
                                    return new ResponseEntity<>(resp, HttpStatus.OK);
                                }else{
                                    if(profileTask.getTask_list().trim().length()==0){
                                        profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                                        entityManager.clear();
                                        profileTask=null;
                                    }else{
                                        String task_List = profileTask.getTask_list();
                                        List<String> arrPlatform = new ArrayList<>(Arrays.asList(task_List.split(",")));
                                        profileTask.setPlatform(arrPlatform.get(0));
                                        List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                                        task_List = String.join(",", subPlatform);
                                        profileTask.setTask_list(task_List);
                                        profileTask.setRequest_index(0);
                                        profileTaskRepository.save(profileTask);
                                    }
                                }
                            }else{
                                if(profileTask.getTask_list().trim().length()==0){
                                    profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                                    entityManager.clear();
                                    profileTask=null;
                                }else{
                                    String task_List = profileTask.getTask_list();
                                    List<String> arrPlatform = new ArrayList<>(Arrays.asList(task_List.split(",")));
                                    profileTask.setPlatform(arrPlatform.get(0));
                                    List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                                    task_List = String.join(",", subPlatform);
                                    profileTask.setTask_list(task_List);
                                    profileTask.setRequest_index(0);
                                    profileTaskRepository.save(profileTask);
                                }
                            }
                        }
                    }else  if(accountProfile_Check_Platform.getLive()!=1&&(System.currentTimeMillis()-accountProfile_Check_Platform.getLast_time())/1000/60>=mode.getTime_profile()){//check last time task login
                        if(!(platformRepository.get_Register_Account_Platform_And_Mode(profileTask.getPlatform(),device.getMode())==0&&accountProfile_Check_Platform.getLive()==-1)){
                            accountProfile_Check_Platform.setLast_time(System.currentTimeMillis());
                            accountProfileRepository.save(accountProfile_Check_Platform);
                            profileTask.setRequest_index(1);
                            profileTaskRepository.save(profileTask);

                            resp.put("status", true);
                            data.put("platform",profileTask.getPlatform());
                            if( profileTask.getPlatform().equals("tiktok")){
                                if(device.getMode().contains("tiktok-lite")){
                                    data.put("app","tiktok-lite");
                                }else{
                                    data.put("app","tiktok-lite");
                                }
                            }else{
                                data.put("app",profileTask.getPlatform());
                            }
                            if(accountProfile_Check_Platform.getLive()==-1){
                                data.put("task", "register");
                            }else{
                                data.put("task", "login");
                            }
                            data.put("task_key", accountProfile_Check_Platform.getAccount_id().substring(0,accountProfile_Check_Platform.getAccount_id().lastIndexOf("|")));
                            data.put("account_id", accountProfile_Check_Platform.getAccount_id().substring(0,accountProfile_Check_Platform.getAccount_id().lastIndexOf("|")));
                            data.put("password", accountProfile_Check_Platform.getPassword().trim());
                            data.put("name", accountProfile_Check_Platform.getName().trim());
                            data.put("avatar", accountProfile_Check_Platform.getAvatar()==0?false:true);
                            data.put("recover_mail", accountProfile_Check_Platform.getRecover().trim());
                            data.put("auth_2fa", accountProfile_Check_Platform.getAuth_2fa().trim());
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
                if( accountProfileRepository.check_AccountLive_By_ProfileId_And_Platform(profileTask.getProfile_id(),profileTask.getPlatform())==0){

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

            profileTask.setAccount_id(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),profileTask.getPlatform()));
            profileTask.setTask_index(profileTask.getTask_index()+1);
            profileTaskRepository.save(profileTask);
            List<ModeOption> priorityTasks =modeOptionRepository.get_Priority_Task_By_Platform_And_Mode(profileTask.getPlatform(),device.getMode());
            List<String> arrTask = new ArrayList<>();

            for(int i=0;i<priorityTasks.size();i++){
                for (int j = 0; j < priorityTasks.get(i).getPriority(); j++) {
                    arrTask.add(priorityTasks.get(i).getTask());
                }
            }
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
                    }
                }
                if(modeOption==null){
                    resp.put("status",false);
                    data.put("message","modeOption không hợp lệ!");
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else if((System.currentTimeMillis()-get_time)/1000/60<modeOption.getTime_get_task()){
                    resp.put("status",false);
                    data.put("message","Không có nhiệm vụ!");
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
                while(arrTask.remove(task)) {}
                if(profileTask.getPlatform().equals("tiktok")){
                    if(task.equals("follower")){
                        get_task= tiktokTask.tiktok_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("like")){
                        get_task=tiktokTask.tiktok_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("view")){
                        get_task=tiktokTask.tiktok_view(profileTask.getAccount_id(),device.getMode().trim());
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
                if(!orderThreadCheck.getValue().contains(dataJson.get("order_id").toString())){
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
                dataJson.put("name",accountProfileRepository.get_Name_By_AccountId(profileTask.getAccount_id()));
                dataJson.put("avatar",accountProfileRepository.get_Avatar_By_AccountId(profileTask.getAccount_id())==0?false:true);
                dataJson.put("account_id",dataJson.get("account_id").toString().substring(0,dataJson.get("account_id").toString().indexOf("|")));
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
            if (updateTaskRequest.getTask_key().length()==0&&!updateTaskRequest.getTask().equals("login")&&!updateTaskRequest.getTask().equals("register")) {
                resp.put("status", false);
                data.put("message", "task_key không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (accountProfileRepository.check_Account_By_AccountId(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim())==0) {
                resp.put("status", false);
                data.put("message", "account_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if(!updateTaskRequest.getTask().equals("login")&&!updateTaskRequest.getTask().equals("register")){
                String platform_Check = updateTaskRequest.getPlatform().toLowerCase().trim();
                if(platform_Check.equals("youtube")){
                    if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true){
                        youtubeUpdate.youtube_view(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("subscriber")&&updateTaskRequest.getStatus()==true){
                        youtubeUpdate.youtube_subscriber(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    } else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                        youtubeUpdate.youtube_like(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }
                }else if(platform_Check.equals("tiktok")){
                    if(updateTaskRequest.getTask().toLowerCase().trim().equals("follower")&&updateTaskRequest.getStatus()==true){
                        tiktokUpdate.tiktok_follower(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim(),updateTaskRequest.getSuccess());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                        tiktokUpdate.tiktok_like(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("comment")){
                        tiktokUpdate.tiktok_comment(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim(),updateTaskRequest.getStatus());
                    }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true){
                        tiktokUpdate.tiktok_view(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getTask_key().trim());
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
                        StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
                        LogError logError =new LogError();
                        logError.setMethod_name(stackTraceElement.getMethodName());
                        logError.setLine_number(stackTraceElement.getLineNumber());
                        logError.setClass_name(stackTraceElement.getClassName());
                        logError.setFile_name(stackTraceElement.getFileName() + "| " + updateTaskRequest.getTask_key().trim()+"|"+updateTaskRequest.getPlatform());
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
                }

            }

            try{
                profileTaskRepository.reset_Thread_By_ProfileId(updateTaskRequest.getDevice_id().trim()+"_"+updateTaskRequest.getProfile_id().trim());
            }catch (Exception e){

            }

            try{
                if(updateTaskRequest.getIsLogin()==0 || updateTaskRequest.getIsLogin()==-1){
                    //.update_Than_Task_Index_By_AccountId(updateTaskRequest.getPlatform().trim(),updateTaskRequest.getAccount_id()+"|"+updateTaskRequest.getPlatform().trim());
                    AccountProfile accountProfile=accountProfileRepository.get_Account_By_Account_id_And_Platform(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getPlatform().trim());
                    if(accountProfile!=null){
                        if(updateTaskRequest.getTask().equals("register")){
                            accountProfile.setLive(-1);
                        }else{
                            accountProfile.setLive(0);
                        }
                        accountProfile.setUpdate_time(System.currentTimeMillis());
                        accountProfileRepository.save(accountProfile);
                    }
                }else if(updateTaskRequest.getIsLogin()==1){  ///Check khi login hoặc reg thành công !!!!!!!!!!!!!

                    AccountProfile accountProfile=accountProfileRepository.get_Account_By_Account_id_And_Platform(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getPlatform().trim());
                    if(accountProfile!=null){
                        if((updateTaskRequest.getTask().equals("login")||updateTaskRequest.getTask().equals("register"))&&updateTaskRequest.getTask_key().length()!=0){

                            //check acc co that su hop le khong
                            if(accountProfileRepository.check_Account_By_AccountId(updateTaskRequest.getTask_key().trim()+"|"+updateTaskRequest.getPlatform().trim())>0&&
                               !updateTaskRequest.getAccount_id().trim().equals(updateTaskRequest.getTask_key().trim())&&updateTaskRequest.getPlatform().equals("tiktok")
                            ){
                                String platform_Dependent=platformRepository.get_Dependent_Connection_By_Platform_And_Mode(updateTaskRequest.getPlatform().trim(),accountProfile.getProfileTask().getDevice().getMode());
                                AccountProfile accountCheck=accountProfileRepository.get_Account_By_Account_id_And_Platform(updateTaskRequest.getTask_key().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getPlatform().trim());
                                Account accountDependent =accountRepository.get_Account_Ddependent_By_ProfileId_And_Platfrom(accountProfile.getProfileTask().getProfile_id(),platform_Dependent);
                                if(accountDependent.getPassword().equals(accountCheck.getPassword())){
                                    accountProfileRepository.delete(accountCheck);
                                }else{
                                    accountProfileRepository.delete(accountProfile);
                                }
                                resp.put("status", true);
                                data.put("message", "Update thành công!");
                                resp.put("data", data);
                                return new ResponseEntity<>(resp, HttpStatus.OK);
                            }
                            if(StringUtils.isValidTikTokID(updateTaskRequest.getTask_key().trim())&&updateTaskRequest.getPlatform().equals("tiktok")){
                                accountProfile.setAccount_id(updateTaskRequest.getTask_key().trim()+"|"+updateTaskRequest.getPlatform().trim());
                                accountProfile.setLive(1);
                            }else if(updateTaskRequest.getPlatform().equals("tiktok")){
                                accountProfile.setLive(0);
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
                            } else if(account!=null){
                                account.setAccount_id(updateTaskRequest.getTask_key().trim()+"|"+updateTaskRequest.getPlatform().trim());
                                accountRepository.save(account);
                            }
                        }else if((updateTaskRequest.getTask().equals("login")||updateTaskRequest.getTask().equals("register"))&&updateTaskRequest.getTask_key().length()==0){
                            if(updateTaskRequest.getTask().equals("register")){
                                accountProfile.setLive(-1);
                            }else{
                                accountProfile.setLive(0);
                            }
                            accountProfile.setUpdate_time(System.currentTimeMillis());
                            accountProfileRepository.save(accountProfile);
                        }else{
                            Account account=accountRepository.get_Account_By_Account_id(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim());
                            if(account==null){
                                account=new Account();
                                account.setAccount_id(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim());
                                account.setPassword(accountProfile.getPassword());
                                account.setName(accountProfile.getName());
                                account.setAvatar(accountProfile.getAvatar());
                                account.setRecover_mail(accountProfile.getRecover());
                                account.setPlatform(accountProfile.getPlatform());
                                account.setLive(1);
                                account.setRunning(1);
                                account.setAuth_2fa("");
                                account.setProfile_id(accountProfile.getProfileTask().getProfile_id());
                                account.setDevice_id(accountProfile.getProfileTask().getDevice().getDevice_id());
                                account.setAdd_time(System.currentTimeMillis());
                                account.setGet_time(System.currentTimeMillis());
                                accountRepository.save(account);
                            }else if(account.getRunning()!=1){
                                account.setRunning(1);
                                account.setProfile_id(accountProfile.getProfileTask().getProfile_id());
                                account.setDevice_id(accountProfile.getProfileTask().getDevice().getDevice_id());
                                accountRepository.save(account);
                            }

                            accountProfile.setLive(1);
                            accountProfile.setUpdate_time(System.currentTimeMillis());
                            accountProfileRepository.save(accountProfile);
                        }
                        String platform_Dependent=platformRepository.get_Dependent_Connection_By_Platform_And_Mode(updateTaskRequest.getPlatform().trim(),accountProfile.getProfileTask().getDevice().getMode());
                        if(platform_Dependent!=null){
                            Account accountDependent =accountRepository.get_Account_Ddependent_By_ProfileId_And_Platfrom(accountProfile.getProfileTask().getProfile_id(),platform_Dependent);
                            if(accountDependent!=null){
                                if(!accountDependent.getPlatform().equals(updateTaskRequest.getPlatform())){
                                    if(!accountDependent.getDependent().contains(updateTaskRequest.getPlatform())){
                                        if(accountDependent.getDependent().length()==0){
                                            accountDependent.setDependent(updateTaskRequest.getPlatform());
                                        }else{
                                            accountDependent.setDependent(accountDependent.getDependent()+","+updateTaskRequest.getPlatform());
                                        }
                                        if(accountDependent.getPassword_dependent().length()==0){
                                            accountDependent.setPassword_dependent(updateTaskRequest.getPlatform()+"|"+accountProfile.getPassword());
                                        }else{
                                            accountDependent.setPassword_dependent(accountDependent.getPassword_dependent()+","+updateTaskRequest.getPlatform()+"|"+accountProfile.getPassword());
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
                        if(accountProfile.getLogin_time()==0){
                            accountProfile.setLogin_time(System.currentTimeMillis());
                            accountProfileRepository.save(accountProfile);
                        }

                    }////

                }else if(updateTaskRequest.getIsLogin()>1){

                    AccountProfile accountProfile=accountProfileRepository.get_Account_By_Account_id_And_Platform(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getPlatform().trim());

                    String platform_Dependent=platformRepository.get_Dependent_Connection_By_Platform_And_Mode(updateTaskRequest.getPlatform().trim(),accountProfile.getProfileTask().getDevice().getMode());
                    if(platform_Dependent!=null){
                        Account accountDependent =accountRepository.get_Account_Ddependent_By_ProfileId_And_Platfrom(accountProfile.getProfileTask().getProfile_id(),platform_Dependent);

                        if(accountDependent!=null){
                            if(!accountDependent.getPlatform().equals(updateTaskRequest.getPlatform())){
                                if(accountDependent.getDependent().contains(updateTaskRequest.getPlatform())){
                                    List<String> arrPlatform =new ArrayList<>(Arrays.asList(accountDependent.getDependent().split(",")));
                                    arrPlatform.remove(updateTaskRequest.getPlatform());
                                    accountDependent.setDependent(String.join(",", arrPlatform));

                            /*
                            List<String> arrPassword =new ArrayList<>(Arrays.asList(account.getPassword_dependent().split(",")));
                            arrPassword.removeIf(platform -> platform.contains(updateTaskRequest.getPlatform()));
                            account.setPassword_dependent(String.join(",", arrPassword));
                             */
                                }
                                if(!accountDependent.getDie_dependent().contains(updateTaskRequest.getPlatform())){
                                    if(accountDependent.getDie_dependent().length()==0){
                                        accountDependent.setDie_dependent(updateTaskRequest.getPlatform());
                                    }else{
                                        accountDependent.setDie_dependent(accountDependent.getDie_dependent()+","+updateTaskRequest.getPlatform());
                                    }
                                }
                                if(!accountDependent.getPassword_dependent().contains(updateTaskRequest.getPlatform()) && accountProfile!=null){
                                    if(accountDependent.getPassword_dependent().length()==0){
                                        accountDependent.setPassword_dependent(updateTaskRequest.getPlatform()+"|"+accountProfile.getPassword());
                                    }else{
                                        accountDependent.setPassword_dependent(accountDependent.getPassword_dependent()+","+updateTaskRequest.getPlatform()+"|"+accountProfile.getPassword());
                                    }
                                }
                                accountRepository.save(accountDependent);
                            }
                        }
                    }
                    Account accountPlatform=accountRepository.get_Account_By_Account_id(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform());
                    if(accountPlatform!=null){
                        accountPlatform.setRunning(0);
                        accountPlatform.setUpdate_time(System.currentTimeMillis());
                        accountPlatform.setDie_time(System.currentTimeMillis());
                        accountPlatform.setLive(updateTaskRequest.getIsLogin());
                        accountRepository.save(accountPlatform);
                    }
                    profileTaskRepository.update_Than_Task_Index_By_AccountId(updateTaskRequest.getPlatform().trim(),updateTaskRequest.getAccount_id()+"%");
                    if(accountProfile!=null){
                        if(updateTaskRequest.getPlatform().trim().equals("tiktok")){
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

}

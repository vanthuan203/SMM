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
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping(value = "getTask003", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> getTask003(@RequestHeader(defaultValue = "") String Authorization,
                                                   @RequestParam(defaultValue = "") String device_id,
                                                   @RequestParam(defaultValue = "") String profile_id,
                                                   @RequestParam(defaultValue = "") String platform) throws InterruptedException {

        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran=new Random();
            Thread.sleep(ran.nextInt(1000));
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
            if (checktoken ==0) {
                resp.put("status", false);
                data.put("message", "Token expired");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (device_id.length()==0) {
                resp.put("status", false);
                data.put("message", "device_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (profile_id.length()==0) {
                resp.put("status", false);
                data.put("message", "profile_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //---------------------get_Account------------------//
            SettingSystem settingSystem=settingSystemRepository.get_Setting_System();
            Device device=deviceRepository.check_DeviceId(device_id.trim());
            ProfileTask profileTask=null;
            if(device==null){
                resp.put("status", false);
                data.put("message", "device_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            device.setUpdate_time(System.currentTimeMillis());
            deviceRepository.save(device);
            profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
            if(profileTask==null&&!profile_id.trim().equals("0")){
                resp.put("status", false);
                data.put("message", "profile_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }else if(profile_id.trim().equals("0")){
                profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim());
                if(profileTask!=null){
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "profile_changer");
                    data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else{
                    resp.put("status", false);
                    data.put("message", "Không có account_id để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }else if(profileTask!=null){
                if(profileTask.getEnabled()==0){
                    profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim());
                    if(profileTask!=null){
                        resp.put("status", true);
                        data.put("platform", "system");
                        data.put("task", "profile_changer");
                        data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }else{
                        resp.put("status", false);
                        data.put("message", "Không có account_id để chạy");
                        resp.put("data", data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }
            }
            else if(profileTask==null){
                resp.put("status", false);
                data.put("message", "profile_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //update time check
            profileTask.setUpdate_time(System.currentTimeMillis());


            if(accountProfileRepository.check_AccountLive_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube")==0){
                AccountProfile accountProfile_Check=accountProfileRepository.get_Account_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube");
                if(accountProfile_Check==null){ // If account null or not live then get new acc
                    Account account_get=null;
                    if(mySQLCheck.getValue()<settingSystem.getMax_mysql()){
                        String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                        String code="";
                        for(int i=0;i<50;i++){
                            Integer ranver=ran.nextInt(stringrand.length());
                            code=code+stringrand.charAt(ranver);
                        }
                        account_get= accountRepository.get_Account_Youtube_By_ProfileId(device_id.trim()+"_"+profile_id.trim(),device_id,System.currentTimeMillis(),code,device.getMode().trim());
                    }
                    if(account_get!=null){
                        AccountProfile accountProfile=new AccountProfile();
                        accountProfile.setAccount_id(account_get.getAccount_id());
                        accountProfile.setPassword(account_get.getPassword());
                        accountProfile.setRecover(account_get.getRecover_mail());
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
                        data.put("task", "login");
                        data.put("task_key", account_get.getAccount_id().substring(0,account_get.getAccount_id().indexOf("|")));
                        data.put("account_id",account_get.getAccount_id().substring(0,account_get.getAccount_id().indexOf("|")));
                        data.put("password", account_get.getPassword().trim());
                        data.put("recover_mail", account_get.getRecover_mail().trim());
                        data.put("auth_2fa", account_get.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }else{
                        profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim());
                        if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())>1){
                            resp.put("status", true);
                            data.put("platform", "system");
                            data.put("task", "profile_changer");
                            data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                            resp.put("data",data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }else if(device.getNum_profile()==1){
                            profileTask.setUpdate_time(System.currentTimeMillis());
                            profileTaskRepository.save(profileTask);
                            profileTask = profileTaskRepository.get_ProfileId_Can_Running_By_DeviceId(device_id.trim());
                        }else{
                            resp.put("status", false);
                            data.put("message", "Không có account_id để chạy");
                            resp.put("data", data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }
                    }
                }else if(profileTask.getRequest_index()>0){ //if live=-1
                    profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                    entityManager.clear();
                    if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())>1){
                        profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim());

                        resp.put("status", true);
                        data.put("platform", "system");
                        data.put("task", "profile_changer");
                        data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }else if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())==1){

                        profileTask.setRequest_index(1);
                        profileTaskRepository.save(profileTask);
                        resp.put("status", true);
                        data.put("platform", "youtube");
                        data.put("task", "login");
                        data.put("task_key", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                        data.put("account_id", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                        data.put("password", accountProfile_Check.getPassword().trim());
                        data.put("recover_mail", accountProfile_Check.getRecover().trim());
                        data.put("auth_2fa", accountProfile_Check.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);

                    }else{
                        resp.put("status", false);
                        data.put("message", "Không có account_id để chạy");
                        resp.put("data", data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }else if(accountProfile_Check.getLive()!=1) {
                    profileTask.setRequest_index(1);
                    profileTaskRepository.save(profileTask);
                    resp.put("status", true);
                    data.put("platform", "youtube");
                    data.put("task", "login");
                    data.put("task_key", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                    data.put("account_id", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                    data.put("password", accountProfile_Check.getPassword().trim());
                    data.put("recover_mail", accountProfile_Check.getRecover().trim());
                    data.put("auth_2fa", accountProfile_Check.getAuth_2fa().trim());
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
            //////////////////////////

            if(profileTask.getState()==0 || profileTask.getPlatform().length()==0){
                profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                entityManager.clear();
                profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                profileTask.setUpdate_time(System.currentTimeMillis());
                String task_List="";
                if(platform.length()==0){
                    List<String> string_Task_List=platformRepository.get_All_Platform_True();
                    task_List=String.join(",", string_Task_List);
                }else{
                    task_List=platform;
                }
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
            if(profileTask.getTask_index()>=platformRepository.get_Priority_By_Platform(profileTask.getPlatform()))
            {
                if(profileTask.getTask_list().trim().length()==0){
                    profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                    entityManager.clear();
                    if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())==1){
                        profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                        profileTask.setUpdate_time(System.currentTimeMillis());
                        String task_List="";
                        if(platform.length()==0){
                            List<String> string_Task_List=platformRepository.get_All_Platform_True();
                            task_List=String.join(",", string_Task_List);
                        }else{
                            task_List=platform;
                        }
                        List<String> arrPlatform=new ArrayList<>(Arrays.asList(task_List.split(",")));
                        profileTask.setPlatform(arrPlatform.get(0));
                        List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                        task_List=String.join(",", subPlatform);
                        profileTask.setTask_list(task_List);
                        profileTask.setTask_index(0);
                        profileTask.setState(1);
                        profileTask.setRequest_index(0);
                        profileTaskRepository.save(profileTask);
                    }else{
                        profileTask=null;
                    }
                }else {
                    String task_List = "";
                    if (platform.length() == 0) {
                        task_List = profileTask.getTask_list();
                    } else {
                        task_List = platform;
                    }
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
                        String task_List = "";
                        if (platform.length() == 0) {
                            task_List = profileTask.getTask_list();
                        } else {
                            task_List = platform;
                        }
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
                        AccountProfile accountProfile_Check_Dependent=accountProfileRepository.get_AccountLike_By_ProfileId_And_Platform(profileTask.getProfile_id(),platformRepository.get_Dependent_By_Platform(profileTask.getPlatform()));
                        Account account_Check=accountRepository.get_Account_By_ProfileId_And_Platfrom(profileTask.getProfile_id(),platformRepository.get_Dependent_By_Platform(profileTask.getPlatform()));
                        Integer connection_account=platformRepository.get_Connection_Account_Platform(profileTask.getPlatform().trim());
                        if((accountProfile_Check_Dependent==null ||  (connection_account==1&&account_Check.getDie_dependent().contains(profileTask.getPlatform()))) &&
                                !profileTask.getPlatform().trim().equals(platformRepository.get_Dependent_By_Platform(profileTask.getPlatform()))){
                            if(profileTask.getTask_list().trim().length()==0){
                                profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                                entityManager.clear();
                                profileTask=null;
                            }else{
                                String task_List = "";
                                if (platform.length() == 0) {
                                    task_List = profileTask.getTask_list();
                                } else {
                                    task_List = platform;
                                }
                                List<String> arrPlatform = new ArrayList<>(Arrays.asList(task_List.split(",")));
                                profileTask.setPlatform(arrPlatform.get(0));
                                List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                                task_List = String.join(",", subPlatform);
                                profileTask.setTask_list(task_List);
                                profileTask.setRequest_index(0);
                                profileTaskRepository.save(profileTask);
                            }
                        }else{
                            if(platformRepository.get_Register_Account_Platform(profileTask.getPlatform())==0){
                                Account account_get=null;
                                if(mySQLCheck.getValue()<settingSystem.getMax_mysql()){
                                    String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                                    String code="";
                                    for(int i=0;i<50;i++){
                                        Integer ranver=ran.nextInt(stringrand.length());
                                        code=code+stringrand.charAt(ranver);
                                    }
                                    account_get= accountRepository.get_Account_Platform_By_ProfileId(device_id.trim()+"_"+profile_id.trim(),device_id,System.currentTimeMillis(),code,profileTask.getPlatform().trim());
                                }
                                if(account_get!=null){
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
                                    data.put("task", "login");
                                    data.put("task_key", account_get.getAccount_id().substring(0,account_get.getAccount_id().indexOf("|")));
                                    data.put("account_id",account_get.getAccount_id().substring(0,account_get.getAccount_id().indexOf("|")));
                                    data.put("password", account_get.getPassword().trim());
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
                                        String task_List = "";
                                        if (platform.length() == 0) {
                                            task_List = profileTask.getTask_list();
                                        } else {
                                            task_List = platform;
                                        }
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
                                if(connection_account>0){

                                    AccountProfile accountProfile=new AccountProfile();
                                    accountProfile.setAccount_id(accountProfile_Check_Dependent.getAccount_id().substring(0,accountProfile_Check_Dependent.getAccount_id().indexOf("|"))+"|"+profileTask.getPlatform());
                                    accountProfile.setPassword(accountProfile_Check_Dependent.getPassword().trim());
                                    accountProfile.setRecover(accountProfile_Check_Dependent.getRecover());
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
                                    data.put("task", "register");
                                    data.put("task_key", accountProfile_Check_Dependent.getAccount_id().substring(0,accountProfile_Check_Dependent.getAccount_id().indexOf("|")));
                                    data.put("account_id",  accountProfile_Check_Dependent.getAccount_id().substring(0,accountProfile_Check_Dependent.getAccount_id().indexOf("|")));
                                    data.put("password",accountProfile_Check_Dependent.getPassword().trim());
                                    data.put("recover_mail",  accountProfile_Check_Dependent.getRecover());
                                    data.put("auth_2fa", "");
                                    resp.put("data",data);
                                    return new ResponseEntity<>(resp, HttpStatus.OK);

                                }else{
                                    String password="Cmc#";
                                    String passrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                                    for(int i=0;i<15;i++){
                                        Integer ranver=ran.nextInt(passrand.length());
                                        password=password+passrand.charAt(ranver);
                                    }
                                    if(platformRepository.get_Dependent_By_Platform(profileTask.getPlatform()).length()==0){
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
                                            AccountProfile accountProfile=new AccountProfile();
                                            accountProfile.setAccount_id(mail+"|"+profileTask.getPlatform());
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
                                            data.put("task", "register");
                                            data.put("task_key", mail);
                                            data.put("account_id", mail);
                                            data.put("password", password);
                                            data.put("recover_mail", mail);
                                            data.put("auth_2fa", "");
                                            resp.put("data",data);
                                            return new ResponseEntity<>(resp, HttpStatus.OK);
                                        }
                                    }else{
                                        AccountProfile accountProfile=new AccountProfile();
                                        accountProfile.setAccount_id(account_Check.getAccount_id().substring(0,account_Check.getAccount_id().indexOf("|"))+"|"+profileTask.getPlatform());
                                        accountProfile.setPassword(password);
                                        accountProfile.setRecover(account_Check.getRecover_mail().trim());
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
                                        data.put("task", "register");
                                        data.put("task_key",  account_Check.getAccount_id().substring(0,account_Check.getAccount_id().indexOf("|")));
                                        data.put("account_id", account_Check.getAccount_id().substring(0,account_Check.getAccount_id().indexOf("|")));
                                        data.put("password", password);
                                        data.put("recover_mail",account_Check.getRecover_mail().trim());
                                        data.put("auth_2fa", "");
                                        resp.put("data",data);
                                        return new ResponseEntity<>(resp, HttpStatus.OK);
                                    }
                                }
                            }
                        }
                    }else if(accountProfile_Check_Platform.getLive()!=1){

                        profileTask.setRequest_index(1);
                        profileTaskRepository.save(profileTask);

                        resp.put("status", true);
                        data.put("platform",profileTask.getPlatform());
                        if(accountProfile_Check_Platform.getLive()==-1){
                            data.put("task", "register");
                        }else{
                            data.put("task", "login");
                        }
                        data.put("task_key", accountProfile_Check_Platform.getAccount_id().substring(0,accountProfile_Check_Platform.getAccount_id().indexOf("|")));
                        data.put("account_id", accountProfile_Check_Platform.getAccount_id().substring(0,accountProfile_Check_Platform.getAccount_id().indexOf("|")));
                        data.put("password", accountProfile_Check_Platform.getPassword().trim());
                        data.put("recover_mail", accountProfile_Check_Platform.getRecover().trim());
                        data.put("auth_2fa", accountProfile_Check_Platform.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }

            }
            if(profileTask==null){
                if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())>1){
                    profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim());
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "profile_changer");
                    data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())==1){
                    profileTask = profileTaskRepository.get_ProfileId_Can_Running_By_DeviceId(device_id.trim());
                }else{
                    resp.put("status", false);
                    data.put("message", "Không có account để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
            profileTask.setAccount_id(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),profileTask.getPlatform()));
            profileTask.setTask_index(profileTask.getTask_index()+1);
            List<TaskPriority> priorityTasks =taskPriorityRepository.get_Priority_Task_By_Platform(profileTask.getPlatform());
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
                while(arrTask.remove(task)) {}
                if(profileTask.getPlatform().equals("tiktok")){
                    if(task.equals("tiktok_follower")){
                        get_task= tiktokTask.tiktok_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_like")){
                        get_task=tiktokTask.tiktok_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_view")){
                        get_task=tiktokTask.tiktok_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_comment")){
                        get_task=tiktokTask.tiktok_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("youtube")){
                    if(task.equals("youtube_view")){
                        get_task=youtubeTask.youtube_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("youtube_like")){
                        get_task=youtubeTask.youtube_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("youtube_subscriber")){
                        get_task=youtubeTask.youtube_subscriber(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("facebook")){
                    if(task.equals("facebook_follower")){
                        get_task=facebookTask.facebook_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_like")){
                        get_task=facebookTask.facebook_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_view")){
                        get_task=facebookTask.facebook_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_comment")){
                        get_task=facebookTask.facebook_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_member")){
                        get_task=facebookTask.facebook_member(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("x")){
                    if(task.equals("x_follower")){
                        get_task=xTask.x_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_like")){
                        get_task=xTask.x_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_view")){
                        get_task=xTask.x_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_comment")){
                        get_task=xTask.x_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_repost")){
                        get_task=xTask.x_repost(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("instagram")){
                    if(task.equals("instagram_follower")){
                        get_task=instagramTask.instagram_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_like")){
                        get_task=instagramTask.instagram_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_view")){
                        get_task=instagramTask.instagram_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_comment")){
                        get_task=instagramTask.instagram_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("threads")){
                    if(task.equals("threads_follower")){
                        get_task=threadsTask.threads_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_like")){
                        get_task=threadsTask.threads_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_view")){
                        get_task=threadsTask.threads_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_comment")){
                        get_task=threadsTask.threads_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_repost")){
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
                dataJson.put("account_id",dataJson.get("account_id").toString().substring(0,dataJson.get("account_id").toString().indexOf("|")));
                dataJson.put("task_index",profileTask.getTask_index());
                Integer platform_task=platformRepository.get_Activity_Platform(dataJson.get("platform").toString());
                dataJson.put("activity",platform_task==0?false:true);
                //System.out.println(dataJson);
                respJson.put("status",true);
                respJson.put("data",dataJson);
                //--------------------------------------------//
                profileTask.setGet_time(System.currentTimeMillis());
                profileTask.setOrder_id(Long.parseLong(dataJson.get("order_id").toString()));
                profileTask.setRunning(1);
                profileTask.setTask(dataJson.get("task").toString());
                profileTask.setPlatform(dataJson.get("platform").toString());
                profileTask.setTask_key(dataJson.get("task_key").toString());
                profileTaskRepository.save(profileTask);
                //--------------------------------------------//
                dataJson.remove("order_id");
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

    @GetMapping(value = "getTask005", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> getTask005(@RequestHeader(defaultValue = "") String Authorization,
                                                   @RequestParam(defaultValue = "") String device_id,
                                                   @RequestParam(defaultValue = "") String profile_id,
                                                   @RequestParam(defaultValue = "") String platform) throws InterruptedException {

        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran=new Random();
            Thread.sleep(ran.nextInt(1000));
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
            if (checktoken ==0) {
                resp.put("status", false);
                data.put("message", "Token expired");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (device_id.length()==0) {
                resp.put("status", false);
                data.put("message", "device_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (profile_id.length()==0) {
                resp.put("status", false);
                data.put("message", "profile_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //---------------------get_Account------------------//
            SettingSystem settingSystem=settingSystemRepository.get_Setting_System();
            Device device=deviceRepository.check_DeviceId(device_id.trim());
            ProfileTask profileTask=null;
            if(device==null){
                resp.put("status", false);
                data.put("message", "device_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            device.setUpdate_time(System.currentTimeMillis());
            deviceRepository.save(device);
            profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
            if(profileTask==null&&!profile_id.trim().equals("0")){
                resp.put("status", false);
                data.put("message", "profile_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }else if(profile_id.trim().equals("0")){
                profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim());
                if(profileTask!=null){
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "profile_changer");
                    data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else{
                    resp.put("status", false);
                    data.put("message", "Không có account_id để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }else if(profileTask!=null){
                if(profileTask.getEnabled()==0){
                    profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim());
                    if(profileTask!=null){
                        resp.put("status", true);
                        data.put("platform", "system");
                        data.put("task", "profile_changer");
                        data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }else{
                        resp.put("status", false);
                        data.put("message", "Không có account_id để chạy");
                        resp.put("data", data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }
            }
            else if(profileTask==null){
                resp.put("status", false);
                data.put("message", "profile_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //update time check
            profileTask.setUpdate_time(System.currentTimeMillis());


            if(accountProfileRepository.check_AccountLive_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube")==0){
                AccountProfile accountProfile_Check=accountProfileRepository.get_Account_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube");
                if(accountProfile_Check==null){ // If account null or not live then get new acc

                    if(platformRepository.get_Register_Account_Platform("youtube")==1&&
                            historyRegisterRepository.count_Register_24h_By_Platform_And_ProfileId("youtube",profileTask.getProfile_id().trim())==0){

                        String password="Cmc#";
                        String passrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                        for(int i=0;i<15;i++){
                            Integer ranver=ran.nextInt(passrand.length());
                            password=password+passrand.charAt(ranver);
                        }

                        AccountProfile accountProfile=new AccountProfile();
                        accountProfile.setAccount_id("register_"+profileTask.getProfile_id()+"|youtube");
                        accountProfile.setPassword(password);
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

                        profileTask.setRegister_index(1);
                        profileTask.setRequest_index(1);
                        profileTaskRepository.save(profileTask);
                        resp.put("status", true);
                        data.put("platform","youtube");
                        data.put("task", "register");
                        data.put("task_key", accountProfile.getAccount_id().substring(0,accountProfile.getAccount_id().indexOf("|")));
                        data.put("account_id",accountProfile.getAccount_id().substring(0,accountProfile.getAccount_id().indexOf("|")));
                        data.put("password",password);
                        data.put("recover_mail",  accountProfile.getRecover().trim());
                        data.put("auth_2fa", "");
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }else if(accountRepository.check_Count_AccountDie24H_By_ProfileId(profileTask.getProfile_id().trim(),"youtube")<3&&
                    platformRepository.get_Login_Account_Platform("youtube")==1){
                        Account account_get=null;
                        if(mySQLCheck.getValue()<settingSystem.getMax_mysql()){
                            String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                            String code="";
                            for(int i=0;i<50;i++){
                                Integer ranver=ran.nextInt(stringrand.length());
                                code=code+stringrand.charAt(ranver);
                            }
                            account_get= accountRepository.get_Account_Youtube_By_ProfileId(device_id.trim()+"_"+profile_id.trim(),device_id,System.currentTimeMillis(),code,device.getMode().trim());
                        }
                        if(account_get!=null){
                            AccountProfile accountProfile=new AccountProfile();
                            accountProfile.setAccount_id(account_get.getAccount_id());
                            accountProfile.setPassword(account_get.getPassword());
                            accountProfile.setRecover(account_get.getRecover_mail());
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
                            data.put("task", "login");
                            data.put("task_key", account_get.getAccount_id().substring(0,account_get.getAccount_id().indexOf("|")));
                            data.put("account_id",account_get.getAccount_id().substring(0,account_get.getAccount_id().indexOf("|")));
                            data.put("password", account_get.getPassword().trim());
                            data.put("recover_mail", account_get.getRecover_mail().trim());
                            data.put("auth_2fa", account_get.getAuth_2fa().trim());
                            resp.put("data",data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }else{
                            profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim());
                            if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())>1){
                                resp.put("status", true);
                                data.put("platform", "system");
                                data.put("task", "profile_changer");
                                data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                                resp.put("data",data);
                                return new ResponseEntity<>(resp, HttpStatus.OK);
                            }else if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())==1){
                                profileTask.setUpdate_time(System.currentTimeMillis());
                                profileTaskRepository.save(profileTask);
                                profileTask = profileTaskRepository.get_ProfileId_Can_Running_By_DeviceId(device_id.trim());
                            }else{
                                resp.put("status", false);
                                data.put("message", "Không có account_id để chạy");
                                resp.put("data", data);
                                return new ResponseEntity<>(resp, HttpStatus.OK);
                            }
                        }
                    }else{// Reset enabled profile and enable profile new
                        if(profileTaskRepository.count_Profile(device.getDevice_id().trim())>1){
                            profileTask.setEnabled_time(0L);
                            profileTask.setEnabled(0);
                            profileTaskRepository.save(profileTask);

                            ProfileTask profileTask_Check =profileTaskRepository.get_Profile_Rand_Enable0_And_NotIn(profileTask.getProfile_id(),device.getDevice_id().trim());
                            if (profileTask_Check !=null){
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
                                if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())>1){
                                    profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim());
                                    resp.put("status", true);
                                    data.put("platform", "system");
                                    data.put("task", "profile_changer");
                                    data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                                    resp.put("data",data);
                                    return new ResponseEntity<>(resp, HttpStatus.OK);
                                }else{
                                    resp.put("status", false);
                                    data.put("message", "Không có account_id để chạy");
                                    resp.put("data", data);
                                    return new ResponseEntity<>(resp, HttpStatus.OK);
                                }
                            }

                        }else{
                            resp.put("status", false);
                            data.put("message", "Không có account_id để chạy");
                            resp.put("data", data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }

                    }

                }else if(profileTask.getRequest_index()>0){ //if live=-1
                    profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                    entityManager.clear();
                    if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())>1){
                        profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim());

                        resp.put("status", true);
                        data.put("platform", "system");
                        data.put("task", "profile_changer");
                        data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }else if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())==1){

                        profileTask.setRequest_index(1);
                        profileTaskRepository.save(profileTask);
                        resp.put("status", true);
                        data.put("platform", "youtube");
                        if(accountProfile_Check.getAccount_id().contains("register")){
                            data.put("task", "register");
                        }else{
                            data.put("task", "login");
                        }
                        data.put("task_key", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                        data.put("account_id", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                        data.put("password", accountProfile_Check.getPassword().trim());
                        data.put("recover_mail", accountProfile_Check.getRecover().trim());
                        data.put("auth_2fa", accountProfile_Check.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);

                    }else{
                        resp.put("status", false);
                        data.put("message", "Không có account_id để chạy");
                        resp.put("data", data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }else if(accountProfile_Check.getLive()!=1) {
                    profileTask.setRequest_index(1);
                    profileTaskRepository.save(profileTask);
                    resp.put("status", true);
                    data.put("platform", "youtube");
                    if(accountProfile_Check.getAccount_id().contains("register")){
                        data.put("task", "register");
                    }else{
                        data.put("task", "login");
                    }
                    data.put("task_key", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                    data.put("account_id", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                    data.put("password", accountProfile_Check.getPassword().trim());
                    data.put("recover_mail", accountProfile_Check.getRecover().trim());
                    data.put("auth_2fa", accountProfile_Check.getAuth_2fa().trim());
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
            //////////////////////////

            if(profileTask.getState()==0 || profileTask.getPlatform().length()==0){
                profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                entityManager.clear();
                profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                profileTask.setUpdate_time(System.currentTimeMillis());
                String task_List="";
                if(platform.length()==0){
                    List<String> string_Task_List=platformRepository.get_All_Platform_True();
                    task_List=String.join(",", string_Task_List);
                }else{
                    task_List=platform;
                }
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
            if(profileTask.getTask_index()>=platformRepository.get_Priority_By_Platform(profileTask.getPlatform()))
            {
                if(profileTask.getTask_list().trim().length()==0){
                    profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                    entityManager.clear();
                    if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())==1){
                        profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                        profileTask.setUpdate_time(System.currentTimeMillis());
                        String task_List="";
                        if(platform.length()==0){
                            List<String> string_Task_List=platformRepository.get_All_Platform_True();
                            task_List=String.join(",", string_Task_List);
                        }else{
                            task_List=platform;
                        }
                        List<String> arrPlatform=new ArrayList<>(Arrays.asList(task_List.split(",")));
                        profileTask.setPlatform(arrPlatform.get(0));
                        List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                        task_List=String.join(",", subPlatform);
                        profileTask.setTask_list(task_List);
                        profileTask.setTask_index(0);
                        profileTask.setState(1);
                        profileTask.setRequest_index(0);
                        profileTaskRepository.save(profileTask);
                    }else{
                        profileTask=null;
                    }
                }else {
                    String task_List = "";
                    if (platform.length() == 0) {
                        task_List = profileTask.getTask_list();
                    } else {
                        task_List = platform;
                    }
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
                        String task_List = "";
                        if (platform.length() == 0) {
                            task_List = profileTask.getTask_list();
                        } else {
                            task_List = platform;
                        }
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

                        AccountProfile accountProfile_Check_Dependent=accountProfileRepository.get_AccountLike_By_ProfileId_And_Platform(profileTask.getProfile_id(),platformRepository.get_Dependent_By_Platform(profileTask.getPlatform()));

                        Account account_Check=accountRepository.get_Account_By_ProfileId_And_Platfrom(profileTask.getProfile_id(),platformRepository.get_Dependent_By_Platform(profileTask.getPlatform()));

                        Integer connection_account=platformRepository.get_Connection_Account_Platform(profileTask.getPlatform().trim());

                        if((accountProfile_Check_Dependent==null ||  (connection_account==1&&account_Check.getDie_dependent().contains(profileTask.getPlatform()))) &&
                                !profileTask.getPlatform().trim().equals(platformRepository.get_Dependent_By_Platform(profileTask.getPlatform()))){
                            if(profileTask.getTask_list().trim().length()==0){
                                profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                                entityManager.clear();
                                profileTask=null;
                            }else{
                                String task_List = "";
                                if (platform.length() == 0) {
                                    task_List = profileTask.getTask_list();
                                } else {
                                    task_List = platform;
                                }
                                List<String> arrPlatform = new ArrayList<>(Arrays.asList(task_List.split(",")));
                                profileTask.setPlatform(arrPlatform.get(0));
                                List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                                task_List = String.join(",", subPlatform);
                                profileTask.setTask_list(task_List);
                                profileTask.setRequest_index(0);
                                profileTaskRepository.save(profileTask);
                            }
                        }else{
                            if((platformRepository.get_Register_Account_Platform(profileTask.getPlatform())==1 || platform.length()!=0)&&
                                    historyRegisterRepository.count_Register_24h_By_Platform_And_ProfileId(profileTask.getPlatform().trim(),profileTask.getProfile_id().trim())==0
                            ){
                                if(connection_account>0){

                                    AccountProfile accountProfile=new AccountProfile();
                                    accountProfile.setAccount_id(accountProfile_Check_Dependent.getAccount_id().substring(0,accountProfile_Check_Dependent.getAccount_id().indexOf("|"))+"|"+profileTask.getPlatform());
                                    accountProfile.setPassword(accountProfile_Check_Dependent.getPassword().trim());
                                    accountProfile.setRecover(accountProfile_Check_Dependent.getRecover());
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
                                    data.put("task", "register");
                                    data.put("task_key", accountProfile_Check_Dependent.getAccount_id().substring(0,accountProfile_Check_Dependent.getAccount_id().indexOf("|")));
                                    data.put("account_id",  accountProfile_Check_Dependent.getAccount_id().substring(0,accountProfile_Check_Dependent.getAccount_id().indexOf("|")));
                                    data.put("password",accountProfile_Check_Dependent.getPassword().trim());
                                    data.put("recover_mail",  accountProfile_Check_Dependent.getRecover());
                                    data.put("auth_2fa", "");
                                    resp.put("data",data);
                                    return new ResponseEntity<>(resp, HttpStatus.OK);

                                }else{

                                    HistoryRegister historyRegister=new HistoryRegister();
                                    historyRegister.setProfileTask(profileTask);
                                    historyRegister.setPlatform(profileTask.getPlatform().trim());
                                    historyRegister.setState(0);
                                    historyRegister.setUpdate_time(System.currentTimeMillis());
                                    historyRegisterRepository.save(historyRegister);

                                    String password="Cmc#";
                                    String passrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                                    for(int i=0;i<15;i++){
                                        Integer ranver=ran.nextInt(passrand.length());
                                        password=password+passrand.charAt(ranver);
                                    }
                                    if(platformRepository.get_Dependent_By_Platform(profileTask.getPlatform()).length()==0){
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
                                            AccountProfile accountProfile=new AccountProfile();
                                            accountProfile.setAccount_id(mail+"|"+profileTask.getPlatform());
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
                                            data.put("task", "register");
                                            data.put("task_key", mail);
                                            data.put("account_id", mail);
                                            data.put("password", password);
                                            data.put("recover_mail", mail);
                                            data.put("auth_2fa", "");
                                            resp.put("data",data);
                                            return new ResponseEntity<>(resp, HttpStatus.OK);
                                        }
                                    }else{
                                        AccountProfile accountProfile=new AccountProfile();
                                        accountProfile.setAccount_id(account_Check.getAccount_id().substring(0,account_Check.getAccount_id().indexOf("|"))+"|"+profileTask.getPlatform());
                                        accountProfile.setPassword(password);
                                        accountProfile.setRecover(account_Check.getRecover_mail().trim());
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
                                        data.put("task", "register");
                                        data.put("task_key",  account_Check.getAccount_id().substring(0,account_Check.getAccount_id().indexOf("|")));
                                        data.put("account_id", account_Check.getAccount_id().substring(0,account_Check.getAccount_id().indexOf("|")));
                                        data.put("password", password);
                                        data.put("recover_mail",account_Check.getRecover_mail().trim());
                                        data.put("auth_2fa", "");
                                        resp.put("data",data);
                                        return new ResponseEntity<>(resp, HttpStatus.OK);
                                    }
                                }
                            }else if(accountRepository.check_Count_AccountDie24H_By_ProfileId(profileTask.getProfile_id().trim(),profileTask.getPlatform().trim())<3&&
                                    platformRepository.get_Login_Account_Platform(profileTask.getPlatform().trim())==1){
                                Account account_get=null;
                                if(mySQLCheck.getValue()<settingSystem.getMax_mysql()){
                                    String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                                    String code="";
                                    for(int i=0;i<50;i++){
                                        Integer ranver=ran.nextInt(stringrand.length());
                                        code=code+stringrand.charAt(ranver);
                                    }
                                    account_get= accountRepository.get_Account_Platform_By_ProfileId(device_id.trim()+"_"+profile_id.trim(),device_id,System.currentTimeMillis(),code,profileTask.getPlatform().trim());
                                }
                                if(account_get!=null){
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
                                    data.put("task", "login");
                                    data.put("task_key", account_get.getAccount_id().substring(0,account_get.getAccount_id().indexOf("|")));
                                    data.put("account_id",account_get.getAccount_id().substring(0,account_get.getAccount_id().indexOf("|")));
                                    data.put("password", account_get.getPassword().trim());
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
                                        String task_List = "";
                                        if (platform.length() == 0) {
                                            task_List = profileTask.getTask_list();
                                        } else {
                                            task_List = platform;
                                        }
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
                                    String task_List = "";
                                    if (platform.length() == 0) {
                                        task_List = profileTask.getTask_list();
                                    } else {
                                        task_List = platform;
                                    }
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
                    }else if(accountProfile_Check_Platform.getLive()!=1){

                        profileTask.setRequest_index(1);
                        profileTaskRepository.save(profileTask);

                        resp.put("status", true);
                        data.put("platform",profileTask.getPlatform());
                        if(accountProfile_Check_Platform.getLive()==-1){
                            data.put("task", "register");
                        }else{
                            data.put("task", "login");
                        }
                        data.put("task_key", accountProfile_Check_Platform.getAccount_id().substring(0,accountProfile_Check_Platform.getAccount_id().indexOf("|")));
                        data.put("account_id", accountProfile_Check_Platform.getAccount_id().substring(0,accountProfile_Check_Platform.getAccount_id().indexOf("|")));
                        data.put("password", accountProfile_Check_Platform.getPassword().trim());
                        data.put("recover_mail", accountProfile_Check_Platform.getRecover().trim());
                        data.put("auth_2fa", accountProfile_Check_Platform.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }

            }
            if(profileTask==null){
                if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())>1){
                    profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim());
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "profile_changer");
                    data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())==1){
                    profileTask = profileTaskRepository.get_ProfileId_Can_Running_By_DeviceId(device_id.trim());
                }else{
                    resp.put("status", false);
                    data.put("message", "Không có account_id để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }else{
                if( accountProfileRepository.check_AccountLive_By_ProfileId_And_Platform(profileTask.getProfile_id(),profileTask.getPlatform())==0){
                    resp.put("status", false);
                    data.put("message", "Không có account_id để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
            profileTask.setAccount_id(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),profileTask.getPlatform()));
            profileTask.setTask_index(profileTask.getTask_index()+1);
            List<TaskPriority> priorityTasks =taskPriorityRepository.get_Priority_Task_By_Platform(profileTask.getPlatform());
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
                while(arrTask.remove(task)) {}
                if(profileTask.getPlatform().equals("tiktok")){
                    if(task.equals("tiktok_follower")){
                        get_task= tiktokTask.tiktok_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_like")){
                        get_task=tiktokTask.tiktok_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_view")){
                        get_task=tiktokTask.tiktok_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_comment")){
                        get_task=tiktokTask.tiktok_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("youtube")){
                    if(task.equals("youtube_view")){
                        get_task=youtubeTask.youtube_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("youtube_like")){
                        get_task=youtubeTask.youtube_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("youtube_subscriber")){
                        get_task=youtubeTask.youtube_subscriber(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("facebook")){
                    if(task.equals("facebook_follower")){
                        get_task=facebookTask.facebook_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_like")){
                        get_task=facebookTask.facebook_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_view")){
                        get_task=facebookTask.facebook_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_comment")){
                        get_task=facebookTask.facebook_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_member")){
                        get_task=facebookTask.facebook_member(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("x")){
                    if(task.equals("x_follower")){
                        get_task=xTask.x_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_like")){
                        get_task=xTask.x_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_view")){
                        get_task=xTask.x_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_comment")){
                        get_task=xTask.x_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_repost")){
                        get_task=xTask.x_repost(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("instagram")){
                    if(task.equals("instagram_follower")){
                        get_task=instagramTask.instagram_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_like")){
                        get_task=instagramTask.instagram_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_view")){
                        get_task=instagramTask.instagram_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_comment")){
                        get_task=instagramTask.instagram_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("threads")){
                    if(task.equals("threads_follower")){
                        get_task=threadsTask.threads_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_like")){
                        get_task=threadsTask.threads_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_view")){
                        get_task=threadsTask.threads_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_comment")){
                        get_task=threadsTask.threads_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_repost")){
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
                dataJson.put("account_id",dataJson.get("account_id").toString().substring(0,dataJson.get("account_id").toString().indexOf("|")));
                dataJson.put("task_index",profileTask.getTask_index());
                Integer platform_task=platformRepository.get_Activity_Platform(dataJson.get("platform").toString());
                dataJson.put("activity",platform_task==0?false:true);
                //System.out.println(dataJson);
                respJson.put("status",true);
                respJson.put("data",dataJson);
                //--------------------------------------------//
                profileTask.setGet_time(System.currentTimeMillis());
                profileTask.setOrder_id(Long.parseLong(dataJson.get("order_id").toString()));
                profileTask.setRunning(1);
                profileTask.setTask(dataJson.get("task").toString());
                profileTask.setPlatform(dataJson.get("platform").toString());
                profileTask.setTask_key(dataJson.get("task_key").toString());
                profileTaskRepository.save(profileTask);
                //--------------------------------------------//
                dataJson.remove("order_id");
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
    @GetMapping(value = "getTask006", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> getTask006(@RequestHeader(defaultValue = "") String Authorization,
                                                   @RequestParam(defaultValue = "") String device_id,
                                                   @RequestParam(defaultValue = "") String rom_version,
                                                   @RequestParam(defaultValue = "") String profile_id,
                                                   @RequestParam(defaultValue = "") String platform) throws InterruptedException {

        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran=new Random();
            Thread.sleep(ran.nextInt(1000));
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
            if (checktoken ==0) {
                resp.put("status", false);
                data.put("message", "Token expired");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (device_id.length()==0) {
                resp.put("status", false);
                data.put("message", "device_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            /*
            if (rom_version.length()==0) {
                resp.put("status", false);
                data.put("message", "rom_version không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }

             */
            if (profile_id.length()==0) {
                resp.put("status", false);
                data.put("message", "profile_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //---------------------get_Account------------------//
            SettingSystem settingSystem=settingSystemRepository.get_Setting_System();
            Device device=deviceRepository.check_DeviceId(device_id.trim());
            ProfileTask profileTask=null;
            if(device==null){
                resp.put("status", false);
                data.put("message", "device_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }else if(device.getState()==0){
                device.setUpdate_time(System.currentTimeMillis());
                deviceRepository.save(device);
                resp.put("status", false);
                data.put("message", "device_id không làm nhiệm vụ");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
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
            device.setRom_version(rom_version.trim());
            device.setUpdate_time(System.currentTimeMillis());
            deviceRepository.save(device);

            if((device.getNum_profile()<device.getNum_profile_set() || profileTaskRepository.get_Count_Profile_Valid_0_By_DeviceId(device_id.trim())>0 )&&!profile_id.trim().equals("0")){
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
                if(device.getNum_profile()<device.getNum_profile_set()){
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "create_profile");
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
                profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim());
                if(profileTask!=null){
                    profileTask.setOnline_time(System.currentTimeMillis());
                    profileTaskRepository.save(profileTask);
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
                    data.put("message", "Không có profile để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }else if(profileTask!=null){
                if(profileTask.getEnabled()==0&&platform.length()==0){
                    profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim());
                    if(profileTask!=null){
                        profileTask.setOnline_time(System.currentTimeMillis());
                        profileTaskRepository.save(profileTask);
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
                        data.put("message", "Không có account_id để chạy");
                        resp.put("data", data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }else if(profileTask.getEnabled()==0&&platform.length()!=0){
                    profileTask.setOnline_time(System.currentTimeMillis());
                    profileTask.setEnabled(1);
                    profileTask.setEnabled_time(System.currentTimeMillis());
                    //profileTask.setReboot(1);
                    profileTaskRepository.save(profileTask);
                }
            }
            else if(profileTask==null){
                resp.put("status", false);
                data.put("message", "profile_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            String profile_Reboot=profileTaskRepository.get_ProfileId_Reboot_1_By_DeviceId(device_id.trim());
            if(profile_Reboot!=null?( !profile_id.trim().equals(profile_Reboot) ):false && platform.trim().length()==0){
                resp.put("status", true);
                data.put("platform", "system");
                data.put("task", "profile_changer");
                data.put("profile_id", Integer.parseInt(profile_Reboot));
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            if(profileTask.getReboot()==1){
                profileTask.setOnline_time(System.currentTimeMillis());
                profileTaskRepository.save(profileTask);
                profileTaskRepository.reset_Reboot_By_DeviceId(device_id.trim());
                resp.put("status", true);
                data.put("platform", "system");
                data.put("task", "reboot");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }

            if(profileTask.getUpdate_pi()==1){
                resp.put("status", true);
                data.put("platform", "system");
                data.put("task", "update_pi");
                data.put("task_key", "fingerprint");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            if(profileTask.getClear_data()==1){
                resp.put("status", true);
                data.put("platform", "system");
                data.put("task", "clear_data");
                data.put("task_key", settingSystem.getClear_data_package().trim());
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            //update time check
            if(profileTask.getOnline_time()==0){
                profileTask.setOnline_time(System.currentTimeMillis());
            }
            profileTask.setUpdate_time(System.currentTimeMillis());

            if(accountProfileRepository.check_AccountLive_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube")==0){
                AccountProfile accountProfile_Check=accountProfileRepository.get_Account_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube");
                if(accountProfile_Check==null){ // If account null or not live then get new acc

                    if((platformRepository.get_Register_Account_Platform("youtube")==1 || platform.length()!=0)&&
                            historyRegisterRepository.count_Register_24h_By_Platform_And_ProfileId("youtube",profileTask.getProfile_id().trim())==0&&
                        accountRepository.check_Count_Account_VeryPhone_By_ProfileId(profileTask.getProfile_id().trim(),"youtube")==0
                    ){

                        String password="Cmc#";
                        String passrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                        for(int i=0;i<6;i++){
                            Integer ranver=ran.nextInt(passrand.length());
                            password=password+passrand.charAt(ranver);
                        }

                        AccountProfile accountProfile=new AccountProfile();
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

                        profileTask.setRegister_index(1);
                        profileTask.setRequest_index(1);
                        profileTaskRepository.save(profileTask);
                        resp.put("status", true);
                        data.put("platform","youtube");
                        data.put("app","youtube");
                        data.put("task", "register");
                        data.put("task_key", accountProfile.getAccount_id().substring(0,accountProfile.getAccount_id().lastIndexOf("|")));
                        data.put("account_id",accountProfile.getAccount_id().substring(0,accountProfile.getAccount_id().lastIndexOf("|")));
                        data.put("password",password);
                        data.put("name",accountProfile.getName());
                        data.put("avatar",accountProfile.getAvatar()==0?false:true);
                        data.put("recover_mail",  accountProfile.getRecover().trim());
                        data.put("auth_2fa", "");
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }else if(accountRepository.check_Count_AccountDie24H_By_ProfileId(profileTask.getProfile_id().trim(),"youtube")<3&&
                            platformRepository.get_Login_Account_Platform("youtube")==1){
                        Account account_get=null;
                        if(mySQLCheck.getValue()<settingSystem.getMax_mysql()){
                            String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                            String code="";
                            for(int i=0;i<50;i++){
                                Integer ranver=ran.nextInt(stringrand.length());
                                code=code+stringrand.charAt(ranver);
                            }
                            account_get= accountRepository.get_Account_Youtube_By_ProfileId(device_id.trim()+"_"+profile_id.trim(),device_id,System.currentTimeMillis(),code,device.getMode().trim());
                        }
                        if(account_get!=null){
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
                            if(platform.length()==0){
                                profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim());
                                if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())>1){
                                    profileTask.setOnline_time(System.currentTimeMillis());
                                    profileTaskRepository.save(profileTask);
                                    //profileTask.setReboot(1);
                                    //profileTaskRepository.save(profileTask);
                                    resp.put("status", true);
                                    data.put("platform", "system");
                                    data.put("task", "profile_changer");
                                    data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                                    resp.put("data",data);
                                    return new ResponseEntity<>(resp, HttpStatus.OK);
                                }else if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())==1){
                                    profileTask.setUpdate_time(System.currentTimeMillis());
                                    profileTaskRepository.save(profileTask);
                                    //profileTask = profileTaskRepository.get_ProfileId_Can_Running_By_DeviceId(device_id.trim());
                                    resp.put("status", false);
                                    data.put("message", "Không có account_id để chạy");
                                    resp.put("data", data);
                                    return new ResponseEntity<>(resp, HttpStatus.OK);
                                }else{
                                    resp.put("status", false);
                                    data.put("message", "Không có account_id để chạy");
                                    resp.put("data", data);
                                    return new ResponseEntity<>(resp, HttpStatus.OK);
                                }
                            }else{
                                resp.put("status", false);
                                data.put("message", "Không có account_id để chạy");
                                resp.put("data", data);
                                return new ResponseEntity<>(resp, HttpStatus.OK);
                            }
                        }
                    }else{// Reset enabled profile and enable profile new
                        if(platform.length()==0){
                            if(profileTaskRepository.count_Profile(device.getDevice_id().trim())>1){
                                //thay vi off thử xóa profile
                                //profileTask.setEnabled_time(0L);
                                profileTask.setValid(0);
                                profileTask.setEnabled(0);
                                profileTaskRepository.save(profileTask);

                                ProfileTask profileTask_Check =profileTaskRepository.get_Profile_Rand_Enable0_And_NotIn(profileTask.getProfile_id(),device.getDevice_id().trim());
                                if (profileTask_Check !=null){
                                    profileTask.setOnline_time(System.currentTimeMillis());
                                    //profileTask.setReboot(1);
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
                                    if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())>1){
                                        profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim());
                                        profileTask.setOnline_time(System.currentTimeMillis());
                                        profileTaskRepository.save(profileTask);
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
                                        data.put("message", "Không có account_id để chạy");
                                        resp.put("data", data);
                                        return new ResponseEntity<>(resp, HttpStatus.OK);
                                    }
                                }
                            }else{
                                resp.put("status", false);
                                data.put("message", "Không có account_id để chạy");
                                resp.put("data", data);
                                return new ResponseEntity<>(resp, HttpStatus.OK);
                            }
                        }else{
                            resp.put("status", false);
                            data.put("message", "Không có account_id để chạy");
                            resp.put("data", data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }
                    }

                }else if(profileTask.getRequest_index()>0&&platform.length()==0 && (System.currentTimeMillis()-profileTask.getOnline_time())/1000/60>=settingSystem.getTime_profile()){ //if live=-1
                    profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                    entityManager.clear();
                    if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())>1){
                        profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim());
                        profileTask.setOnline_time(System.currentTimeMillis());
                        profileTaskRepository.save(profileTask);
                        //profileTask.setReboot(1);
                        //profileTaskRepository.save(profileTask);
                        resp.put("status", true);
                        data.put("platform", "system");
                        data.put("task", "profile_changer");
                        data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }else if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())==1){

                        profileTask.setRequest_index(1);
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

                    }else{
                        resp.put("status", false);
                        data.put("message", "Không có account_id để chạy");
                        resp.put("data", data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }else if(accountProfile_Check.getLive()!=1) {
                    profileTask.setRequest_index(1);
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
            //////////////////////////

            if(profileTask.getGoogle_time()==0){
                profileTask.setGoogle_time(System.currentTimeMillis());
            }

            if(profileTask.getState()==0 || profileTask.getPlatform().length()==0){
                profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                entityManager.clear();
                profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                profileTask.setUpdate_time(System.currentTimeMillis());
                String task_List="";
                if(platform.length()==0){
                    List<String> string_Task_List=platformRepository.get_All_Platform_True();
                    task_List=String.join(",", string_Task_List);
                }else{
                    task_List=platform;
                }
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
            if(profileTask.getTask_index()>=platformRepository.get_Priority_By_Platform(profileTask.getPlatform()))
            {
                if(profileTask.getTask_list().trim().length()==0){
                    profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                    entityManager.clear();
                    if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())==1){
                        profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                        profileTask.setUpdate_time(System.currentTimeMillis());
                        String task_List="";
                        if(platform.length()==0){
                            List<String> string_Task_List=platformRepository.get_All_Platform_True();
                            task_List=String.join(",", string_Task_List);
                        }else{
                            task_List=platform;
                        }
                        List<String> arrPlatform=new ArrayList<>(Arrays.asList(task_List.split(",")));
                        profileTask.setPlatform(arrPlatform.get(0));
                        List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                        task_List=String.join(",", subPlatform);
                        profileTask.setTask_list(task_List);
                        profileTask.setTask_index(0);
                        profileTask.setState(1);
                        profileTask.setRequest_index(0);
                        profileTaskRepository.save(profileTask);
                    }else{
                        profileTask=null;
                    }
                }else {
                    String task_List = "";
                    if (platform.length() == 0) {
                        task_List = profileTask.getTask_list();
                    } else {
                        task_List = platform;
                    }
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
                        String task_List = "";
                        if (platform.length() == 0) {
                            task_List = profileTask.getTask_list();
                        } else {
                            task_List = platform;
                        }
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
                        Boolean check_GetAccount=true;
                        AccountProfile accountProfile_Dependent=null;
                        Account account_Dependent=null;
                        String platform_Dependent=platformRepository.get_Dependent_Connection_By_Platform(profileTask.getPlatform());
                        if(platform_Dependent!=null){
                            accountProfile_Dependent=accountProfileRepository.get_Account_By_ProfileId_And_Platform(profileTask.getProfile_id(),platform_Dependent);
                            if(accountProfile_Dependent==null){
                                check_GetAccount=false;
                            }
                            account_Dependent=accountRepository.get_Account_By_ProfileId_And_Platfrom(profileTask.getProfile_id(),platform_Dependent);
                            if(account_Dependent==null){
                                check_GetAccount=false;
                            }else{
                                if(account_Dependent.getDie_dependent().contains(profileTask.getPlatform())){
                                    check_GetAccount=false;
                                }
                            }
                        }else {
                            check_GetAccount=true;
                        }


                        if(!check_GetAccount){
                            if(profileTask.getTask_list().trim().length()==0){
                                profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                                entityManager.clear();
                                profileTask=null;
                            }else{
                                String task_List = "";
                                if (platform.length() == 0) {
                                    task_List = profileTask.getTask_list();
                                } else {
                                    task_List = platform;
                                }
                                List<String> arrPlatform = new ArrayList<>(Arrays.asList(task_List.split(",")));
                                profileTask.setPlatform(arrPlatform.get(0));
                                List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                                task_List = String.join(",", subPlatform);
                                profileTask.setTask_list(task_List);
                                profileTask.setRequest_index(0);
                                profileTaskRepository.save(profileTask);
                            }
                        }else{
                            if((System.currentTimeMillis()-profileTask.getGoogle_time())/1000/60/60>=platformRepository.get_Time_Register_Account_Platform(profileTask.getPlatform()) || platform.length()!=0){
                                //gioi han time reg by platform and time
                                /*
                                if(historyRegisterRepository.count_Register_By_Platform_And_Time(profileTask.getPlatform().trim(),2)>0){
                                    resp.put("status", false);
                                    data.put("message", "Không có account_id để chạy");
                                    resp.put("data", data);
                                    return new ResponseEntity<>(resp, HttpStatus.OK);
                                }
                                 */
                                if((platformRepository.get_Register_Account_Platform(profileTask.getPlatform())==1 || platform.length()!=0)&&
                                        historyRegisterRepository.count_Register_24h_By_Platform_And_ProfileId(profileTask.getPlatform().trim(),profileTask.getProfile_id().trim())==0
                                ){
                                    if(platform_Dependent!=null){

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

                                    }else{

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
                                        if(platformRepository.get_Dependent_By_Platform(profileTask.getPlatform()).length()==0){
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
                                }else if(accountRepository.check_Count_AccountDie24H_By_ProfileId(profileTask.getProfile_id().trim(),profileTask.getPlatform().trim())<3&&
                                        platformRepository.get_Login_Account_Platform(profileTask.getPlatform().trim())==1){
                                    Account account_get=null;
                                    if(mySQLCheck.getValue()<settingSystem.getMax_mysql()){
                                        String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                                        String code="";
                                        for(int i=0;i<50;i++){
                                            Integer ranver=ran.nextInt(stringrand.length());
                                            code=code+stringrand.charAt(ranver);
                                        }
                                        account_get= accountRepository.get_Account_Platform_By_ProfileId(device_id.trim()+"_"+profile_id.trim(),device_id,System.currentTimeMillis(),code,profileTask.getPlatform().trim());
                                    }
                                    if(account_get!=null){
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
                                            String task_List = "";
                                            if (platform.length() == 0) {
                                                task_List = profileTask.getTask_list();
                                            } else {
                                                task_List = platform;
                                            }
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
                                        String task_List = "";
                                        if (platform.length() == 0) {
                                            task_List = profileTask.getTask_list();
                                        } else {
                                            task_List = platform;
                                        }
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
                                    String task_List = "";
                                    if (platform.length() == 0) {
                                        task_List = profileTask.getTask_list();
                                    } else {
                                        task_List = platform;
                                    }
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
                    }else if(accountProfile_Check_Platform.getLive()!=1){

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
                    /*else if(!StringUtils.isValidTikTokID(accountProfile_Check_Platform.getAccount_id().substring(0,accountProfile_Check_Platform.getAccount_id().lastIndexOf("|")))
                    &&accountProfile_Check_Platform.getPlatform().equals("tiktok")){
                        profileTask.setRequest_index(1);
                        profileTaskRepository.save(profileTask);

                        resp.put("status", true);
                        data.put("platform",profileTask.getPlatform());
                        if( profileTask.getPlatform().equals("tiktok")){
                            if(device.getMode().contains("tiktok-lite")){
                                data.put("app","tiktok-lite");
                            }else{
                                data.put("app","tiktok");
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
                        data.put("recover_mail", accountProfile_Check_Platform.getRecover().trim());
                        data.put("auth_2fa", accountProfile_Check_Platform.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }

                     */
                }

            }
            if(profileTask==null){
                if(platform.length()==0){
                    profileTask = profileTaskRepository.get_Profile_By_ProfileId(device_id.trim()+"_"+profile_id.trim());
                    if((System.currentTimeMillis()-profileTask.getOnline_time())/1000/60>=settingSystem.getTime_profile()){
                        if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())>1){
                            profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim());
                            profileTask.setOnline_time(System.currentTimeMillis());
                            profileTaskRepository.save(profileTask);
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
                            data.put("message", "Không có account_id để chạy");
                            resp.put("data", data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }
                    }else{
                        resp.put("status", false);
                        data.put("message", "Không có account_id để chạy");
                        resp.put("data", data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }else{
                    resp.put("status", false);
                    data.put("message", "Không có account_id để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }

            }else{
                if( accountProfileRepository.check_AccountLive_By_ProfileId_And_Platform(profileTask.getProfile_id(),profileTask.getPlatform())==0){
                    resp.put("status", false);
                    data.put("message", "Không có account_id để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
            //check time get task 12/20/24
            if((System.currentTimeMillis()-profileTask.getTask_time())/1000/60<settingSystem.getTime_get_task()){
                resp.put("status",false);
                data.put("message","Không có nhiệm vụ!");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }

            profileTask.setAccount_id(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),profileTask.getPlatform()));
            profileTask.setTask_index(profileTask.getTask_index()+1);
            List<TaskPriority> priorityTasks =taskPriorityRepository.get_Priority_Task_By_Platform(profileTask.getPlatform());
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
                while(arrTask.remove(task)) {}
                if(profileTask.getPlatform().equals("tiktok")){
                    if(task.equals("tiktok_follower")){
                        get_task= tiktokTask.tiktok_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_like")){
                        get_task=tiktokTask.tiktok_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_view")){
                        get_task=tiktokTask.tiktok_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_comment")){
                        get_task=tiktokTask.tiktok_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("youtube")){
                    if(task.equals("youtube_view")){
                        get_task=youtubeTask.youtube_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("youtube_like")){
                        get_task=youtubeTask.youtube_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("youtube_subscriber")){
                        get_task=youtubeTask.youtube_subscriber(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("facebook")){
                    if(task.equals("facebook_follower")){
                        get_task=facebookTask.facebook_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_like")){
                        get_task=facebookTask.facebook_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_view")){
                        get_task=facebookTask.facebook_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_comment")){
                        get_task=facebookTask.facebook_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_member")){
                        get_task=facebookTask.facebook_member(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("x")){
                    if(task.equals("x_follower")){
                        get_task=xTask.x_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_like")){
                        get_task=xTask.x_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_view")){
                        get_task=xTask.x_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_comment")){
                        get_task=xTask.x_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_repost")){
                        get_task=xTask.x_repost(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("instagram")){
                    if(task.equals("instagram_follower")){
                        get_task=instagramTask.instagram_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_like")){
                        get_task=instagramTask.instagram_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_view")){
                        get_task=instagramTask.instagram_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_comment")){
                        get_task=instagramTask.instagram_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("threads")){
                    if(task.equals("threads_follower")){
                        get_task=threadsTask.threads_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_like")){
                        get_task=threadsTask.threads_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_view")){
                        get_task=threadsTask.threads_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_comment")){
                        get_task=threadsTask.threads_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_repost")){
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
                dataJson.put("name",accountProfileRepository.get_Name_By_AccountId(profileTask.getAccount_id()));
                dataJson.put("avatar",accountProfileRepository.get_Avatar_By_AccountId(profileTask.getAccount_id())==0?false:true);
                dataJson.put("account_id",dataJson.get("account_id").toString().substring(0,dataJson.get("account_id").toString().indexOf("|")));
                dataJson.put("task_index",profileTask.getTask_index());
                Long version_app=platformRepository.get_Version_App_Platform(dataJson.get("platform").toString());
                dataJson.put("version_app",version_app==null?0:version_app);
                Integer activity=platformRepository.get_Activity_Platform(dataJson.get("platform").toString());
                dataJson.put("activity",activity==0?false:true);
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
                dataJson.remove("order_id");



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
    @GetMapping(value = "getTask007", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> getTask007(@RequestHeader(defaultValue = "") String Authorization,
                                                   @RequestParam(defaultValue = "") String device_id,
                                                   @RequestParam(defaultValue = "") String profile_id,
                                                   @RequestParam(defaultValue = "") String rom_version,
                                                   @RequestParam(defaultValue = "") String platform) throws InterruptedException {

        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran=new Random();
            Thread.sleep(ran.nextInt(1000));
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
            if (checktoken ==0) {
                resp.put("status", false);
                data.put("message", "Token expired");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (device_id.length()==0) {
                resp.put("status", false);
                data.put("message", "device_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (rom_version.length()==0) {
                resp.put("status", false);
                data.put("message", "rom_version không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (profile_id.length()==0) {
                resp.put("status", false);
                data.put("message", "profile_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //---------------------get_Account------------------//
            SettingSystem settingSystem=settingSystemRepository.get_Setting_System();
            Device device=deviceRepository.check_DeviceId(device_id.trim());
            ProfileTask profileTask=null;
            if(device==null){
                resp.put("status", false);
                data.put("message", "device_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }else if(device.getState()==0){
                device.setUpdate_time(System.currentTimeMillis());
                deviceRepository.save(device);
                resp.put("status", false);
                data.put("message", "device_id không làm nhiệm vụ");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            if(!settingSystem.getRom_version().trim().equals(rom_version.trim())){
                device.setUpdate_time(System.currentTimeMillis());
                deviceRepository.save(device);
                resp.put("status", true);
                data.put("platform", "system");
                data.put("task", "update_rom");
                data.put("rom_version", settingSystem.getRom_version().trim());
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            device.setRom_version(rom_version.trim());
            device.setUpdate_time(System.currentTimeMillis());
            deviceRepository.save(device);
            profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
            if(profileTask==null&&!profile_id.trim().equals("0")){
                resp.put("status", false);
                data.put("message", "profile_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }else if(profile_id.trim().equals("0")){
                profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim());
                if(profileTask!=null){
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "profile_changer");
                    data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else{
                    resp.put("status", false);
                    data.put("message", "Không có account_id để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }else if(profileTask!=null){
                if(profileTask.getEnabled()==0&&platform.length()==0){
                    profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim());
                    if(profileTask!=null){
                        profileTask.setOnline_time(System.currentTimeMillis());
                        profileTaskRepository.save(profileTask);
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
                        data.put("message", "Không có account_id để chạy");
                        resp.put("data", data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }else if(profileTask.getEnabled()==0&&platform.length()!=0){
                    profileTask.setEnabled(1);
                    profileTask.setEnabled_time(System.currentTimeMillis());
                    profileTaskRepository.save(profileTask);
                }
            }
            else if(profileTask==null){
                resp.put("status", false);
                data.put("message", "profile_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //update time check
            if(profileTask.getOnline_time()==0){
                profileTask.setOnline_time(System.currentTimeMillis());
            }
            profileTask.setUpdate_time(System.currentTimeMillis());


            if(accountProfileRepository.check_AccountLive_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube")==0){
                AccountProfile accountProfile_Check=accountProfileRepository.get_Account_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube");
                if(accountProfile_Check==null){ // If account null or not live then get new acc

                    if((platformRepository.get_Register_Account_Platform("youtube")==1 || platform.length()!=0)&&
                            historyRegisterRepository.count_Register_24h_By_Platform_And_ProfileId("youtube",profileTask.getProfile_id().trim())==0&&
                            accountRepository.check_Count_Account_VeryPhone_By_ProfileId(profileTask.getProfile_id().trim(),"youtube")==0
                    ){

                        String password="Cmc#";
                        String passrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                        for(int i=0;i<6;i++){
                            Integer ranver=ran.nextInt(passrand.length());
                            password=password+passrand.charAt(ranver);
                        }

                        AccountProfile accountProfile=new AccountProfile();
                        accountProfile.setAccount_id("register_"+profileTask.getProfile_id()+"|youtube");
                        accountProfile.setPassword(password);
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

                        profileTask.setRegister_index(1);
                        profileTask.setRequest_index(1);
                        profileTaskRepository.save(profileTask);
                        resp.put("status", true);
                        data.put("platform","youtube");
                        data.put("task", "register");
                        data.put("task_key", accountProfile.getAccount_id().substring(0,accountProfile.getAccount_id().indexOf("|")));
                        data.put("account_id",accountProfile.getAccount_id().substring(0,accountProfile.getAccount_id().indexOf("|")));
                        data.put("password",password);
                        data.put("recover_mail",  accountProfile.getRecover().trim());
                        data.put("auth_2fa", "");
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }else if(accountRepository.check_Count_AccountDie24H_By_ProfileId(profileTask.getProfile_id().trim(),"youtube")<3&&
                            platformRepository.get_Login_Account_Platform("youtube")==1){
                        Account account_get=null;
                        if(mySQLCheck.getValue()<settingSystem.getMax_mysql()){
                            String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                            String code="";
                            for(int i=0;i<50;i++){
                                Integer ranver=ran.nextInt(stringrand.length());
                                code=code+stringrand.charAt(ranver);
                            }
                            account_get= accountRepository.get_Account_Youtube_By_ProfileId(device_id.trim()+"_"+profile_id.trim(),device_id,System.currentTimeMillis(),code,device.getMode().trim());
                        }
                        if(account_get!=null){
                            AccountProfile accountProfile=new AccountProfile();
                            accountProfile.setAccount_id(account_get.getAccount_id());
                            accountProfile.setPassword(account_get.getPassword());
                            accountProfile.setRecover(account_get.getRecover_mail());
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
                            data.put("task", "login");
                            data.put("task_key", account_get.getAccount_id().substring(0,account_get.getAccount_id().indexOf("|")));
                            data.put("account_id",account_get.getAccount_id().substring(0,account_get.getAccount_id().indexOf("|")));
                            data.put("password", account_get.getPassword().trim());
                            data.put("recover_mail", account_get.getRecover_mail().trim());
                            data.put("auth_2fa", account_get.getAuth_2fa().trim());
                            resp.put("data",data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }else{
                            if(platform.length()==0){
                                profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim());
                                if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())>1){
                                    //profileTask.setReboot(1);
                                    //profileTaskRepository.save(profileTask);
                                    resp.put("status", true);
                                    data.put("platform", "system");
                                    data.put("task", "profile_changer");
                                    data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                                    resp.put("data",data);
                                    return new ResponseEntity<>(resp, HttpStatus.OK);
                                }else if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())==1){
                                    profileTask.setUpdate_time(System.currentTimeMillis());
                                    profileTaskRepository.save(profileTask);
                                    //profileTask = profileTaskRepository.get_ProfileId_Can_Running_By_DeviceId(device_id.trim());
                                }else{
                                    resp.put("status", false);
                                    data.put("message", "Không có account_id để chạy");
                                    resp.put("data", data);
                                    return new ResponseEntity<>(resp, HttpStatus.OK);
                                }
                            }else{
                                resp.put("status", false);
                                data.put("message", "Không có account_id để chạy");
                                resp.put("data", data);
                                return new ResponseEntity<>(resp, HttpStatus.OK);
                            }
                        }
                    }else{// Reset enabled profile and enable profile new
                        if(platform.length()==0){
                            if(profileTaskRepository.count_Profile(device.getDevice_id().trim())>1){
                                profileTask.setEnabled_time(0L);
                                profileTask.setEnabled(0);
                                profileTaskRepository.save(profileTask);

                                ProfileTask profileTask_Check =profileTaskRepository.get_Profile_Rand_Enable0_And_NotIn(profileTask.getProfile_id(),device.getDevice_id().trim());
                                if (profileTask_Check !=null){
                                    //profileTask.setReboot(1);
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
                                    if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())>1){
                                        profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim());
                                        resp.put("status", true);
                                        data.put("platform", "system");
                                        data.put("task", "profile_changer");
                                        data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                                        resp.put("data",data);
                                        return new ResponseEntity<>(resp, HttpStatus.OK);
                                    }else{
                                        resp.put("status", false);
                                        data.put("message", "Không có account_id để chạy");
                                        resp.put("data", data);
                                        return new ResponseEntity<>(resp, HttpStatus.OK);
                                    }
                                }
                            }else{
                                resp.put("status", false);
                                data.put("message", "Không có account_id để chạy");
                                resp.put("data", data);
                                return new ResponseEntity<>(resp, HttpStatus.OK);
                            }
                        }else{
                            resp.put("status", false);
                            data.put("message", "Không có account_id để chạy");
                            resp.put("data", data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }
                    }

                }else if(profileTask.getRequest_index()>0&&platform.length()==0){ //if live=-1
                    profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                    entityManager.clear();
                    if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())>1){
                        profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim());
                        //profileTask.setReboot(1);
                        //profileTaskRepository.save(profileTask);
                        resp.put("status", true);
                        data.put("platform", "system");
                        data.put("task", "profile_changer");
                        data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }else if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())==1){

                        profileTask.setRequest_index(1);
                        profileTaskRepository.save(profileTask);
                        resp.put("status", true);
                        data.put("platform", "youtube");
                        if(accountProfile_Check.getAccount_id().contains("register")){
                            data.put("task", "register");
                        }else{
                            data.put("task", "login");
                        }
                        data.put("task_key", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                        data.put("account_id", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                        data.put("password", accountProfile_Check.getPassword().trim());
                        data.put("recover_mail", accountProfile_Check.getRecover().trim());
                        data.put("auth_2fa", accountProfile_Check.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);

                    }else{
                        resp.put("status", false);
                        data.put("message", "Không có account_id để chạy");
                        resp.put("data", data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }else if(accountProfile_Check.getLive()!=1) {
                    profileTask.setRequest_index(1);
                    profileTaskRepository.save(profileTask);
                    resp.put("status", true);
                    data.put("platform", "youtube");
                    if(accountProfile_Check.getAccount_id().contains("register")){
                        data.put("task", "register");
                    }else{
                        data.put("task", "login");
                    }
                    data.put("task_key", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                    data.put("account_id", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                    data.put("password", accountProfile_Check.getPassword().trim());
                    data.put("recover_mail", accountProfile_Check.getRecover().trim());
                    data.put("auth_2fa", accountProfile_Check.getAuth_2fa().trim());
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
            //////////////////////////

            if(profileTask.getState()==0 || profileTask.getPlatform().length()==0){
                profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                entityManager.clear();
                profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                profileTask.setUpdate_time(System.currentTimeMillis());
                String task_List="";
                if(platform.length()==0){
                    List<String> string_Task_List=platformRepository.get_All_Platform_True();
                    task_List=String.join(",", string_Task_List);
                }else{
                    task_List=platform;
                }
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
            if(profileTask.getTask_index()>=platformRepository.get_Priority_By_Platform(profileTask.getPlatform()))
            {
                if(profileTask.getTask_list().trim().length()==0){
                    profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                    entityManager.clear();
                    if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())==1){
                        profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                        profileTask.setUpdate_time(System.currentTimeMillis());
                        String task_List="";
                        if(platform.length()==0){
                            List<String> string_Task_List=platformRepository.get_All_Platform_True();
                            task_List=String.join(",", string_Task_List);
                        }else{
                            task_List=platform;
                        }
                        List<String> arrPlatform=new ArrayList<>(Arrays.asList(task_List.split(",")));
                        profileTask.setPlatform(arrPlatform.get(0));
                        List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                        task_List=String.join(",", subPlatform);
                        profileTask.setTask_list(task_List);
                        profileTask.setTask_index(0);
                        profileTask.setState(1);
                        profileTask.setRequest_index(0);
                        profileTaskRepository.save(profileTask);
                    }else{
                        profileTask=null;
                    }
                }else {
                    String task_List = "";
                    if (platform.length() == 0) {
                        task_List = profileTask.getTask_list();
                    } else {
                        task_List = platform;
                    }
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
                        String task_List = "";
                        if (platform.length() == 0) {
                            task_List = profileTask.getTask_list();
                        } else {
                            task_List = platform;
                        }
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

                        AccountProfile accountProfile_Check_Dependent=accountProfileRepository.get_AccountLike_By_ProfileId_And_Platform(profileTask.getProfile_id(),platformRepository.get_Dependent_By_Platform(profileTask.getPlatform()));

                        Account account_Check=accountRepository.get_Account_By_ProfileId_And_Platfrom(profileTask.getProfile_id(),platformRepository.get_Dependent_By_Platform(profileTask.getPlatform()));

                        Integer connection_account=platformRepository.get_Connection_Account_Platform(profileTask.getPlatform().trim());

                        if((accountProfile_Check_Dependent==null ||  (connection_account==1&&account_Check.getDie_dependent().contains(profileTask.getPlatform()))) &&
                                !profileTask.getPlatform().trim().equals(platformRepository.get_Dependent_By_Platform(profileTask.getPlatform()))){
                            if(profileTask.getTask_list().trim().length()==0){
                                profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                                entityManager.clear();
                                profileTask=null;
                            }else{
                                String task_List = "";
                                if (platform.length() == 0) {
                                    task_List = profileTask.getTask_list();
                                } else {
                                    task_List = platform;
                                }
                                List<String> arrPlatform = new ArrayList<>(Arrays.asList(task_List.split(",")));
                                profileTask.setPlatform(arrPlatform.get(0));
                                List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                                task_List = String.join(",", subPlatform);
                                profileTask.setTask_list(task_List);
                                profileTask.setRequest_index(0);
                                profileTaskRepository.save(profileTask);
                            }
                        }else{
                            if((System.currentTimeMillis()-profileTask.getEnabled_time())/1000/60/60>=platformRepository.get_Time_Register_Account_Platform(profileTask.getPlatform()) || platform.length()!=0){
                                if((platformRepository.get_Register_Account_Platform(profileTask.getPlatform())==1 || platform.length()!=0)&&
                                        historyRegisterRepository.count_Register_24h_By_Platform_And_ProfileId(profileTask.getPlatform().trim(),profileTask.getProfile_id().trim())==0
                                ){
                                    if(connection_account>0){

                                        AccountProfile accountProfile=new AccountProfile();
                                        accountProfile.setAccount_id(accountProfile_Check_Dependent.getAccount_id().substring(0,accountProfile_Check_Dependent.getAccount_id().indexOf("|"))+"|"+profileTask.getPlatform());
                                        accountProfile.setPassword(accountProfile_Check_Dependent.getPassword().trim());
                                        accountProfile.setRecover(accountProfile_Check_Dependent.getRecover());
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
                                        data.put("task", "register");
                                        data.put("task_key", accountProfile_Check_Dependent.getAccount_id().substring(0,accountProfile_Check_Dependent.getAccount_id().indexOf("|")));
                                        data.put("account_id",  accountProfile_Check_Dependent.getAccount_id().substring(0,accountProfile_Check_Dependent.getAccount_id().indexOf("|")));
                                        data.put("password",accountProfile_Check_Dependent.getPassword().trim());
                                        data.put("recover_mail",  accountProfile_Check_Dependent.getRecover());
                                        data.put("auth_2fa", "");
                                        resp.put("data",data);
                                        return new ResponseEntity<>(resp, HttpStatus.OK);

                                    }else{

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
                                        if(platformRepository.get_Dependent_By_Platform(profileTask.getPlatform()).length()==0){
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
                                                AccountProfile accountProfile=new AccountProfile();
                                                accountProfile.setAccount_id(mail+"|"+profileTask.getPlatform());
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
                                                data.put("task", "register");
                                                data.put("task_key", mail);
                                                data.put("account_id", mail);
                                                data.put("password", password);
                                                data.put("recover_mail", mail);
                                                data.put("auth_2fa", "");
                                                resp.put("data",data);
                                                return new ResponseEntity<>(resp, HttpStatus.OK);
                                            }
                                        }else{
                                            AccountProfile accountProfile=new AccountProfile();
                                            accountProfile.setAccount_id(account_Check.getAccount_id().substring(0,account_Check.getAccount_id().indexOf("|"))+"|"+profileTask.getPlatform());
                                            accountProfile.setPassword(password);
                                            accountProfile.setRecover(account_Check.getRecover_mail().trim());
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
                                            data.put("task", "register");
                                            data.put("task_key",  account_Check.getAccount_id().substring(0,account_Check.getAccount_id().indexOf("|")));
                                            data.put("account_id", account_Check.getAccount_id().substring(0,account_Check.getAccount_id().indexOf("|")));
                                            data.put("password", password);
                                            data.put("recover_mail",account_Check.getRecover_mail().trim());
                                            data.put("auth_2fa", "");
                                            resp.put("data",data);
                                            return new ResponseEntity<>(resp, HttpStatus.OK);
                                        }
                                    }
                                }else if(accountRepository.check_Count_AccountDie24H_By_ProfileId(profileTask.getProfile_id().trim(),profileTask.getPlatform().trim())<3&&
                                        platformRepository.get_Login_Account_Platform(profileTask.getPlatform().trim())==1){
                                    Account account_get=null;
                                    if(mySQLCheck.getValue()<settingSystem.getMax_mysql()){
                                        String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                                        String code="";
                                        for(int i=0;i<50;i++){
                                            Integer ranver=ran.nextInt(stringrand.length());
                                            code=code+stringrand.charAt(ranver);
                                        }
                                        account_get= accountRepository.get_Account_Platform_By_ProfileId(device_id.trim()+"_"+profile_id.trim(),device_id,System.currentTimeMillis(),code,profileTask.getPlatform().trim());
                                    }
                                    if(account_get!=null){
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
                                        data.put("task", "login");
                                        data.put("task_key", account_get.getAccount_id().substring(0,account_get.getAccount_id().indexOf("|")));
                                        data.put("account_id",account_get.getAccount_id().substring(0,account_get.getAccount_id().indexOf("|")));
                                        data.put("password", account_get.getPassword().trim());
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
                                            String task_List = "";
                                            if (platform.length() == 0) {
                                                task_List = profileTask.getTask_list();
                                            } else {
                                                task_List = platform;
                                            }
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
                                        String task_List = "";
                                        if (platform.length() == 0) {
                                            task_List = profileTask.getTask_list();
                                        } else {
                                            task_List = platform;
                                        }
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
                                    String task_List = "";
                                    if (platform.length() == 0) {
                                        task_List = profileTask.getTask_list();
                                    } else {
                                        task_List = platform;
                                    }
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
                    }else if(accountProfile_Check_Platform.getLive()!=1){

                        profileTask.setRequest_index(1);
                        profileTaskRepository.save(profileTask);

                        resp.put("status", true);
                        data.put("platform",profileTask.getPlatform());
                        if(accountProfile_Check_Platform.getLive()==-1){
                            data.put("task", "register");
                        }else{
                            data.put("task", "login");
                        }
                        data.put("task_key", accountProfile_Check_Platform.getAccount_id().substring(0,accountProfile_Check_Platform.getAccount_id().indexOf("|")));
                        data.put("account_id", accountProfile_Check_Platform.getAccount_id().substring(0,accountProfile_Check_Platform.getAccount_id().indexOf("|")));
                        data.put("password", accountProfile_Check_Platform.getPassword().trim());
                        data.put("recover_mail", accountProfile_Check_Platform.getRecover().trim());
                        data.put("auth_2fa", accountProfile_Check_Platform.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }

            }
            if(profileTask==null){
                if(platform.length()==0){
                    profileTask = profileTaskRepository.get_Profile_By_ProfileId(device_id.trim()+"_"+profile_id.trim());
                    if((System.currentTimeMillis()-profileTask.getOnline_time())/1000/60>=settingSystem.getTime_profile()){
                        if(profileTaskRepository.get_Count_Profile_Enabled(device_id.trim())>1){
                            profileTask = profileTaskRepository.get_Profile_Get_Task_By_Enabled(device_id.trim());
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
                            data.put("message", "Không có account_id để chạy");
                            resp.put("data", data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }
                    }else{
                        resp.put("status", false);
                        data.put("message", "Không có account_id để chạy");
                        resp.put("data", data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }else{
                    resp.put("status", false);
                    data.put("message", "Không có account_id để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }

            }else{
                if( accountProfileRepository.check_AccountLive_By_ProfileId_And_Platform(profileTask.getProfile_id(),profileTask.getPlatform())==0){
                    resp.put("status", false);
                    data.put("message", "Không có account_id để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
            profileTask.setAccount_id(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),profileTask.getPlatform()));
            profileTask.setTask_index(profileTask.getTask_index()+1);
            List<TaskPriority> priorityTasks =taskPriorityRepository.get_Priority_Task_By_Platform(profileTask.getPlatform());
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
                while(arrTask.remove(task)) {}
                if(profileTask.getPlatform().equals("tiktok")){
                    if(task.equals("tiktok_follower")){
                        get_task= tiktokTask.tiktok_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_like")){
                        get_task=tiktokTask.tiktok_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_view")){
                        get_task=tiktokTask.tiktok_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_comment")){
                        get_task=tiktokTask.tiktok_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("youtube")){
                    if(task.equals("youtube_view")){
                        get_task=youtubeTask.youtube_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("youtube_like")){
                        get_task=youtubeTask.youtube_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("youtube_subscriber")){
                        get_task=youtubeTask.youtube_subscriber(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("facebook")){
                    if(task.equals("facebook_follower")){
                        get_task=facebookTask.facebook_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_like")){
                        get_task=facebookTask.facebook_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_view")){
                        get_task=facebookTask.facebook_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_comment")){
                        get_task=facebookTask.facebook_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_member")){
                        get_task=facebookTask.facebook_member(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("x")){
                    if(task.equals("x_follower")){
                        get_task=xTask.x_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_like")){
                        get_task=xTask.x_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_view")){
                        get_task=xTask.x_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_comment")){
                        get_task=xTask.x_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_repost")){
                        get_task=xTask.x_repost(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("instagram")){
                    if(task.equals("instagram_follower")){
                        get_task=instagramTask.instagram_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_like")){
                        get_task=instagramTask.instagram_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_view")){
                        get_task=instagramTask.instagram_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_comment")){
                        get_task=instagramTask.instagram_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("threads")){
                    if(task.equals("threads_follower")){
                        get_task=threadsTask.threads_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_like")){
                        get_task=threadsTask.threads_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_view")){
                        get_task=threadsTask.threads_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_comment")){
                        get_task=threadsTask.threads_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_repost")){
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
                dataJson.put("account_id",dataJson.get("account_id").toString().substring(0,dataJson.get("account_id").toString().indexOf("|")));
                dataJson.put("task_index",profileTask.getTask_index());
                Long version_app=platformRepository.get_Version_App_Platform(dataJson.get("platform").toString());
                dataJson.put("version_app",version_app==null?0:version_app);
                Integer activity=platformRepository.get_Activity_Platform(dataJson.get("platform").toString());
                dataJson.put("activity",activity==0?false:true);
                //System.out.println(dataJson);
                respJson.put("status",true);
                respJson.put("data",dataJson);
                //--------------------------------------------//
                profileTask.setGet_time(System.currentTimeMillis());
                profileTask.setOrder_id(Long.parseLong(dataJson.get("order_id").toString()));
                profileTask.setRunning(1);
                profileTask.setTask(dataJson.get("task").toString());
                profileTask.setPlatform(dataJson.get("platform").toString());
                profileTask.setTask_key(dataJson.get("task_key").toString());
                profileTaskRepository.save(profileTask);
                //--------------------------------------------//
                dataJson.remove("order_id");
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
    @GetMapping(value = "getTask004", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> getTask004(@RequestHeader(defaultValue = "") String Authorization,
                                                   @RequestParam(defaultValue = "") String device_id,
                                                   @RequestParam(defaultValue = "") String profile_id,
                                                   @RequestParam(defaultValue = "") String platform) throws InterruptedException {

        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran=new Random();
            Thread.sleep(ran.nextInt(1000));
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
            if (checktoken ==0) {
                resp.put("status", false);
                data.put("message", "Token expired");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (device_id.length()==0) {
                resp.put("status", false);
                data.put("message", "device_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (profile_id.length()==0) {
                resp.put("status", false);
                data.put("message", "profile_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //---------------------get_Account------------------//
            SettingSystem settingSystem=settingSystemRepository.get_Setting_System();
            Device device=deviceRepository.check_DeviceId(device_id.trim());
            ProfileTask profileTask=null;
            if(device==null){
                resp.put("status", false);
                data.put("message", "device_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            device.setUpdate_time(System.currentTimeMillis());
            deviceRepository.save(device);
            profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
            if(profileTask==null&&!profile_id.trim().equals("0")){
                resp.put("status", false);
                data.put("message", "profile_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }else if(profile_id.trim().equals("0")){
                profileTask = profileTaskRepository.get_Profile_Get_Task(device_id.trim());
                if(device.getNum_profile()>1){
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
            }else if(profileTask==null){
                resp.put("status", false);
                data.put("message", "profile_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //update time check
            profileTask.setUpdate_time(System.currentTimeMillis());


            if(accountProfileRepository.check_AccountLive_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube")==0){
                AccountProfile accountProfile_Check=accountProfileRepository.get_Account_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube");
                if(accountProfile_Check==null){ // If account null or not live then get new acc
                    Account account_get=null;
                    if(mySQLCheck.getValue()<settingSystem.getMax_mysql()){
                        String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                        String code="";
                        for(int i=0;i<50;i++){
                            Integer ranver=ran.nextInt(stringrand.length());
                            code=code+stringrand.charAt(ranver);
                        }
                        account_get= accountRepository.get_Account_Gmail_By_ProfileId(device_id.trim()+"_"+profile_id.trim(),device_id,System.currentTimeMillis(),code);
                    }
                    if(account_get!=null){
                        AccountProfile accountProfile=new AccountProfile();
                        accountProfile.setAccount_id(account_get.getAccount_id()+"|"+"youtube");
                        accountProfile.setPassword(account_get.getPassword());
                        accountProfile.setRecover(account_get.getRecover_mail());
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
                        data.put("task", "login");
                        data.put("task_key", account_get.getAccount_id());
                        data.put("account_id", account_get.getAccount_id());
                        data.put("password", account_get.getPassword().trim());
                        data.put("recover_mail", account_get.getRecover_mail().trim());
                        data.put("auth_2fa", account_get.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }else{
                        profileTask = profileTaskRepository.get_Profile_Get_Task(device_id.trim());
                        if(device.getNum_profile()>1){
                            resp.put("status", true);
                            data.put("platform", "system");
                            data.put("task", "profile_changer");
                            data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                            resp.put("data",data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }else if(device.getNum_profile()==1){
                            profileTask.setUpdate_time(System.currentTimeMillis());
                            profileTaskRepository.save(profileTask);
                            profileTask = profileTaskRepository.get_ProfileId_Can_Running_By_DeviceId(device_id.trim());
                        }else{
                            resp.put("status", false);
                            data.put("message", "Không có account_id để chạy");
                            resp.put("data", data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }
                    }
                }else if(profileTask.getRequest_index()>0){ //if live=-1
                    profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                    entityManager.clear();
                    if(device.getNum_profile()>1){
                        profileTask = profileTaskRepository.get_Profile_Get_Task(device_id.trim());

                        resp.put("status", true);
                        data.put("platform", "system");
                        data.put("task", "profile_changer");
                        data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }else if(device.getNum_profile()==1){

                        profileTask.setRequest_index(1);
                        profileTaskRepository.save(profileTask);
                        resp.put("status", true);
                        data.put("platform", "youtube");
                        data.put("task", "login");
                        data.put("task_key", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                        data.put("account_id", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                        data.put("password", accountProfile_Check.getPassword().trim());
                        data.put("recover_mail", accountProfile_Check.getRecover().trim());
                        data.put("auth_2fa", accountProfile_Check.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);

                    }else{
                        resp.put("status", false);
                        data.put("message", "Không có account_id để chạy");
                        resp.put("data", data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }else if(accountProfile_Check.getLive()!=1) {
                    profileTask.setRequest_index(1);
                    profileTaskRepository.save(profileTask);
                    resp.put("status", true);
                    data.put("platform", "youtube");
                    data.put("task", "login");
                    data.put("task_key", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                    data.put("account_id", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                    data.put("password", accountProfile_Check.getPassword().trim());
                    data.put("recover_mail", accountProfile_Check.getRecover().trim());
                    data.put("auth_2fa", accountProfile_Check.getAuth_2fa().trim());
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
            //////////////////////////

            if(profileTask.getState()==0 || profileTask.getPlatform().length()==0){
                profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                entityManager.clear();
                profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                profileTask.setUpdate_time(System.currentTimeMillis());
                String task_List="";
                if(platform.length()==0){
                    List<String> string_Task_List=platformRepository.get_All_Platform_True();
                    task_List=String.join(",", string_Task_List);
                }else{
                    task_List=platform;
                }
                List<String> arrPlatform=new ArrayList<>(Arrays.asList(task_List.split(",")));
                Platform platform_Check=platformRepository.get_Platform_By_PlatformId(arrPlatform.get(0));
                profileTask.setPlatform(arrPlatform.get(0));
                List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                task_List=String.join(",", subPlatform);
                profileTask.setTask_list(task_List);
                profileTask.setTask_index(0);
                profileTask.setState(1);
                profileTask.setRequest_index(0);
                profileTaskRepository.save(profileTask);
            }
            if(profileTask.getTask_index()>=platformRepository.get_Priority_By_Platform(profileTask.getPlatform()))
            {
                if(profileTask.getTask_list().trim().length()==0){
                    profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                    entityManager.clear();
                    if(device.getNum_profile()==1){
                        profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                        profileTask.setUpdate_time(System.currentTimeMillis());
                        String task_List="";
                        if(platform.length()==0){
                            List<String> string_Task_List=platformRepository.get_All_Platform_True();
                            task_List=String.join(",", string_Task_List);
                        }else{
                            task_List=platform;
                        }
                        List<String> arrPlatform=new ArrayList<>(Arrays.asList(task_List.split(",")));
                        profileTask.setPlatform(arrPlatform.get(0));
                        List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                        task_List=String.join(",", subPlatform);
                        profileTask.setTask_list(task_List);
                        profileTask.setTask_index(0);
                        profileTask.setState(1);
                        profileTask.setRequest_index(0);
                        profileTaskRepository.save(profileTask);
                    }else{
                        profileTask=null;
                    }
                }else {
                    String task_List = "";
                    if (platform.length() == 0) {
                        task_List = profileTask.getTask_list();
                    } else {
                        task_List = platform;
                    }
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
                        String task_List = "";
                        if (platform.length() == 0) {
                            task_List = profileTask.getTask_list();
                        } else {
                            task_List = platform;
                        }
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
                        AccountProfile accountProfile_Check_Dependent=accountProfileRepository.get_AccountLike_By_ProfileId_And_Platform(profileTask.getProfile_id(),platformRepository.get_Dependent_By_Platform(profileTask.getPlatform()));
                        if(accountProfile_Check_Dependent==null){
                            if(profileTask.getTask_list().trim().length()==0){
                                profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                                entityManager.clear();
                                profileTask=null;
                            }else{
                                String task_List = "";
                                if (platform.length() == 0) {
                                    task_List = profileTask.getTask_list();
                                } else {
                                    task_List = platform;
                                }
                                List<String> arrPlatform = new ArrayList<>(Arrays.asList(task_List.split(",")));
                                profileTask.setPlatform(arrPlatform.get(0));
                                List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                                task_List = String.join(",", subPlatform);
                                profileTask.setTask_list(task_List);
                                profileTask.setRequest_index(0);
                                profileTaskRepository.save(profileTask);
                            }
                        }else{
                            String password="Cmc#";
                            String passrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                            for(int i=0;i<15;i++){
                                Integer ranver=ran.nextInt(passrand.length());
                                password=password+passrand.charAt(ranver);
                            }

                            AccountProfile accountProfile=new AccountProfile();
                            accountProfile.setAccount_id(accountProfile_Check_Dependent.getAccount_id().substring(0,accountProfile_Check_Dependent.getAccount_id().indexOf("|"))+"|"+profileTask.getPlatform());
                            accountProfile.setPassword(password);
                            accountProfile.setRecover(accountProfile_Check_Dependent.getRecover());
                            accountProfile.setPlatform(profileTask.getPlatform());
                            accountProfile.setLive(0);
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
                            data.put("task", "register");
                            data.put("task_key", accountProfile_Check_Dependent.getAccount_id().substring(0,accountProfile_Check_Dependent.getAccount_id().indexOf("|")));
                            data.put("account_id",  accountProfile_Check_Dependent.getAccount_id().substring(0,accountProfile_Check_Dependent.getAccount_id().indexOf("|")));
                            data.put("password", password);
                            data.put("recover_mail",  accountProfile_Check_Dependent.getRecover());
                            data.put("auth_2fa", "");
                            resp.put("data",data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }
                    }else if(accountProfile_Check_Platform.getLive()!=1){

                        profileTask.setRequest_index(1);
                        profileTaskRepository.save(profileTask);

                        resp.put("status", true);
                        data.put("platform",profileTask.getPlatform());
                        data.put("task", "register");
                        data.put("task_key", accountProfile_Check_Platform.getAccount_id().substring(0,accountProfile_Check_Platform.getAccount_id().indexOf("|")));
                        data.put("account_id", accountProfile_Check_Platform.getAccount_id().substring(0,accountProfile_Check_Platform.getAccount_id().indexOf("|")));
                        data.put("password", accountProfile_Check_Platform.getPassword().trim());
                        data.put("recover_mail", accountProfile_Check_Platform.getRecover().trim());
                        data.put("auth_2fa", accountProfile_Check_Platform.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                    profileTask.setAccount_id(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),profileTask.getPlatform()));
                    profileTaskRepository.save(profileTask);
                }

            }
            if(profileTask==null){
                if(device.getNum_profile()>1){
                    profileTask = profileTaskRepository.get_Profile_Get_Task(device_id.trim());
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "profile_changer");
                    data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else if(device.getNum_profile()==1){
                    profileTask = profileTaskRepository.get_ProfileId_Can_Running_By_DeviceId(device_id.trim());
                }else{
                    resp.put("status", false);
                    data.put("message", "Không có account để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
            profileTask.setTask_index(profileTask.getTask_index()+1);
            List<TaskPriority> priorityTasks =taskPriorityRepository.get_Priority_Task_By_Platform(profileTask.getPlatform());
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
                while(arrTask.remove(task)) {}
                if(profileTask.getPlatform().equals("tiktok")){
                    if(task.equals("tiktok_follower")){
                        get_task= tiktokTask.tiktok_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_like")){
                        get_task=tiktokTask.tiktok_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_view")){
                        get_task=tiktokTask.tiktok_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_comment")){
                        get_task=tiktokTask.tiktok_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("youtube")){
                    if(task.equals("youtube_view")){
                        get_task=youtubeTask.youtube_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("youtube_like")){
                        get_task=youtubeTask.youtube_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("youtube_subscriber")){
                        get_task=youtubeTask.youtube_subscriber(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("facebook")){
                    if(task.equals("facebook_follower")){
                        get_task=facebookTask.facebook_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_like")){
                        get_task=facebookTask.facebook_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_view")){
                        get_task=facebookTask.facebook_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_comment")){
                        get_task=facebookTask.facebook_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_member")){
                        get_task=facebookTask.facebook_member(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("x")){
                    if(task.equals("x_follower")){
                        get_task=xTask.x_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_like")){
                        get_task=xTask.x_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_view")){
                        get_task=xTask.x_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_comment")){
                        get_task=xTask.x_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_repost")){
                        get_task=xTask.x_repost(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("instagram")){
                    if(task.equals("instagram_follower")){
                        get_task=instagramTask.instagram_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_like")){
                        get_task=instagramTask.instagram_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_view")){
                        get_task=instagramTask.instagram_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_comment")){
                        get_task=instagramTask.instagram_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("threads")){
                    if(task.equals("threads_follower")){
                        get_task=threadsTask.threads_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_like")){
                        get_task=threadsTask.threads_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_view")){
                        get_task=threadsTask.threads_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_comment")){
                        get_task=threadsTask.threads_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_repost")){
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
                dataJson.put("task_index",profileTask.getTask_index());
                Integer platform_task=platformRepository.get_Activity_Platform(dataJson.get("platform").toString());
                dataJson.put("activity",platform_task==0?false:true);
                //System.out.println(dataJson);
                respJson.put("status",true);
                respJson.put("data",dataJson);
                //--------------------------------------------//
                profileTask.setGet_time(System.currentTimeMillis());
                profileTask.setOrder_id(Long.parseLong(dataJson.get("order_id").toString()));
                profileTask.setRunning(1);
                profileTask.setTask(dataJson.get("task").toString());
                profileTask.setPlatform(dataJson.get("platform").toString());
                profileTask.setTask_key(dataJson.get("task_key").toString());
                profileTaskRepository.save(profileTask);
                //--------------------------------------------//
                dataJson.remove("order_id");
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
    @GetMapping(value = "getTask003OFFNEW", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> getTask003OFFNEW(@RequestHeader(defaultValue = "") String Authorization,
                                                   @RequestParam(defaultValue = "") String device_id,
                                                   @RequestParam(defaultValue = "") String profile_id,
                                                   @RequestParam(defaultValue = "") String platform) throws InterruptedException {

        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran=new Random();
            Thread.sleep(ran.nextInt(1000));
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
            if (checktoken ==0) {
                resp.put("status", false);
                data.put("message", "Token expired");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (device_id.length()==0) {
                resp.put("status", false);
                data.put("message", "device_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (profile_id.length()==0) {
                resp.put("status", false);
                data.put("message", "profile_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //---------------------get_Account------------------//
            SettingSystem settingSystem=settingSystemRepository.get_Setting_System();
            Device device=deviceRepository.check_DeviceId(device_id.trim());
            ProfileTask profileTask=null;
            if(device==null){
                resp.put("status", false);
                data.put("message", "device_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            device.setUpdate_time(System.currentTimeMillis());
            deviceRepository.save(device);
            profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
            if(profileTask==null&&!profile_id.trim().equals("0")){
                resp.put("status", false);
                data.put("message", "profile_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }else if(profile_id.trim().equals("0")){
                profileTask = profileTaskRepository.get_Profile_Get_Task(device_id.trim());
                if(device.getNum_profile()>1){
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
            }else if(profileTask==null){
                resp.put("status", false);
                data.put("message", "profile_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //update time check
            profileTask.setUpdate_time(System.currentTimeMillis());


            if(accountProfileRepository.check_AccountLive_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube")==0){
                AccountProfile accountProfile_Check=accountProfileRepository.get_Account_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube");
                if(accountProfile_Check==null){ // If account null or not live then get new acc
                    Account account_get=null;
                    if(mySQLCheck.getValue()<settingSystem.getMax_mysql()){
                        String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                        String code="";
                        for(int i=0;i<50;i++){
                            Integer ranver=ran.nextInt(stringrand.length());
                            code=code+stringrand.charAt(ranver);
                        }
                        account_get= accountRepository.get_Account_Gmail_By_ProfileId(device_id.trim()+"_"+profile_id.trim(),device_id,System.currentTimeMillis(),code);
                    }
                    if(account_get!=null){
                        AccountProfile accountProfile=new AccountProfile();
                        accountProfile.setAccount_id(account_get.getAccount_id()+"|"+"youtube");
                        accountProfile.setPassword(account_get.getPassword());
                        accountProfile.setRecover(account_get.getRecover_mail());
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
                        data.put("task", "login");
                        data.put("task_key", account_get.getAccount_id());
                        data.put("account_id", account_get.getAccount_id());
                        data.put("password", account_get.getPassword().trim());
                        data.put("recover_mail", account_get.getRecover_mail().trim());
                        data.put("auth_2fa", account_get.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }else{
                        profileTask = profileTaskRepository.get_Profile_Get_Task(device_id.trim());
                        if(device.getNum_profile()>1){
                            resp.put("status", true);
                            data.put("platform", "system");
                            data.put("task", "profile_changer");
                            data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                            resp.put("data",data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }else if(device.getNum_profile()==1){
                            profileTask.setUpdate_time(System.currentTimeMillis());
                            profileTaskRepository.save(profileTask);
                            profileTask = profileTaskRepository.get_ProfileId_Can_Running_By_DeviceId(device_id.trim());
                        }else{
                            resp.put("status", false);
                            data.put("message", "Không có account_id để chạy");
                            resp.put("data", data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }
                    }
                }else if(profileTask.getRequest_index()>0){ //if live=-1
                    profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                    entityManager.clear();
                    if(device.getNum_profile()>1){
                        profileTask = profileTaskRepository.get_Profile_Get_Task(device_id.trim());

                        resp.put("status", true);
                        data.put("platform", "system");
                        data.put("task", "profile_changer");
                        data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }else if(device.getNum_profile()==1){

                        profileTask.setRequest_index(1);
                        profileTaskRepository.save(profileTask);
                        resp.put("status", true);
                        data.put("platform", "youtube");
                        data.put("task", "login");
                        data.put("task_key", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                        data.put("account_id", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                        data.put("password", accountProfile_Check.getPassword().trim());
                        data.put("recover_mail", accountProfile_Check.getRecover().trim());
                        data.put("auth_2fa", accountProfile_Check.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);

                    }else{
                        resp.put("status", false);
                        data.put("message", "Không có account_id để chạy");
                        resp.put("data", data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }else if(accountProfile_Check.getLive()!=1) {
                    profileTask.setRequest_index(1);
                    profileTaskRepository.save(profileTask);
                    resp.put("status", true);
                    data.put("platform", "youtube");
                    data.put("task", "login");
                    data.put("task_key", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                    data.put("account_id", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                    data.put("password", accountProfile_Check.getPassword().trim());
                    data.put("recover_mail", accountProfile_Check.getRecover().trim());
                    data.put("auth_2fa", accountProfile_Check.getAuth_2fa().trim());
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
            //////////////////////////

            if(profileTask.getState()==0 || profileTask.getPlatform().length()==0){
                profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                entityManager.clear();
                profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                profileTask.setUpdate_time(System.currentTimeMillis());
                String task_List="";
                if(platform.length()==0){
                    List<String> string_Task_List=platformRepository.get_All_Platform_True();
                    task_List=String.join(",", string_Task_List);
                }else{
                    task_List=platform;
                }
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
            if(profileTask.getTask_index()>=platformRepository.get_Priority_By_Platform(profileTask.getPlatform()))
            {
                if(profileTask.getTask_list().trim().length()==0){
                    profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                    entityManager.clear();
                    if(device.getNum_profile()==1){
                        profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                        profileTask.setUpdate_time(System.currentTimeMillis());
                        String task_List="";
                        if(platform.length()==0){
                            List<String> string_Task_List=platformRepository.get_All_Platform_True();
                            task_List=String.join(",", string_Task_List);
                        }else{
                            task_List=platform;
                        }
                        List<String> arrPlatform=new ArrayList<>(Arrays.asList(task_List.split(",")));
                        profileTask.setPlatform(arrPlatform.get(0));
                        List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                        task_List=String.join(",", subPlatform);
                        profileTask.setTask_list(task_List);
                        profileTask.setTask_index(0);
                        profileTask.setState(1);
                        profileTask.setRequest_index(0);
                        profileTaskRepository.save(profileTask);
                    }else{
                        profileTask=null;
                    }
                }else {
                    String task_List = "";
                    if (platform.length() == 0) {
                        task_List = profileTask.getTask_list();
                    } else {
                        task_List = platform;
                    }
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
                        String task_List = "";
                        if (platform.length() == 0) {
                            task_List = profileTask.getTask_list();
                        } else {
                            task_List = platform;
                        }
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
                        JSONArray domains= MailApi.getDomains();
                        String stringrand="abcdefhijkprstuvwx0123456789";
                        String mail="";
                        Boolean success=false;
                        while (!success){
                            for(int i=0;i<20;i++){
                                Integer ranver=ran.nextInt(stringrand.length());
                                mail=mail+stringrand.charAt(ranver);
                            }
                            mail=mail+"@"+domains.get(ran.nextInt(domains.size()));
                            success=MailApi.createMail(mail);
                        }
                        if(success){
                            String password="Cmc#";
                            String passrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                            for(int i=0;i<15;i++){
                                Integer ranver=ran.nextInt(passrand.length());
                                password=password+passrand.charAt(ranver);
                            }
                            AccountProfile accountProfile=new AccountProfile();
                            accountProfile.setAccount_id(mail+"|"+profileTask.getPlatform());
                            accountProfile.setPassword(password);
                            accountProfile.setRecover(mail);
                            accountProfile.setPlatform(profileTask.getPlatform());
                            accountProfile.setLive(0);
                            accountProfile.setChanged(0);
                            accountProfile.setAuth_2fa("");
                            accountProfile.setProfileTask(profileTask);
                            accountProfile.setAdd_time(System.currentTimeMillis());
                            accountProfile.setUpdate_time(0L);
                            accountProfileRepository.save(accountProfile);
                            try{
                                Account account=new Account();
                                account.setAccount_id(mail);
                                account.setPassword(password);
                                account.setRecover_mail(mail);
                                account.setPlatform(profileTask.getPlatform());
                                account.setLive(1);
                                account.setRunning(1);
                                account.setAuth_2fa("");
                                account.setProfile_id(profileTask.getProfile_id());
                                account.setDevice_id(profileTask.getDevice().getDevice_id());
                                account.setAdd_time(System.currentTimeMillis());
                                accountRepository.save(account);
                            }catch (Exception e){
                            }
                            profileTask.setRequest_index(1);
                            profileTaskRepository.save(profileTask);
                            resp.put("status", true);
                            data.put("platform", profileTask.getPlatform());
                            data.put("task", "register");
                            data.put("task_key", mail);
                            data.put("account_id", mail);
                            data.put("password", password);
                            data.put("recover_mail", mail);
                            data.put("auth_2fa", "");
                            resp.put("data",data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }
                    }else if(accountProfile_Check_Platform.getLive()!=1){

                        profileTask.setRequest_index(1);
                        profileTaskRepository.save(profileTask);

                        resp.put("status", true);
                        data.put("platform",profileTask.getPlatform());
                        data.put("task", "register");
                        data.put("task_key", accountProfile_Check_Platform.getAccount_id().substring(0,accountProfile_Check_Platform.getAccount_id().indexOf("|")));
                        data.put("account_id", accountProfile_Check_Platform.getAccount_id().substring(0,accountProfile_Check_Platform.getAccount_id().indexOf("|")));
                        data.put("password", accountProfile_Check_Platform.getPassword().trim());
                        data.put("recover_mail", accountProfile_Check_Platform.getRecover().trim());
                        data.put("auth_2fa", accountProfile_Check_Platform.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                    profileTask.setAccount_id(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),profileTask.getPlatform()));
                    profileTaskRepository.save(profileTask);
                }

            }


            if(profileTask==null){
                profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                entityManager.clear();
                profileTask = profileTaskRepository.get_Profile_Get_Task(device_id.trim());
                if(device.getNum_profile()>1){
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "profile_changer");
                    data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else if(device.getNum_profile()==1){
                    profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                    profileTask.setUpdate_time(System.currentTimeMillis());
                    profileTaskRepository.save(profileTask);
                    profileTask = profileTaskRepository.get_ProfileId_Can_Running_By_DeviceId(device_id.trim());
                }else{
                    resp.put("status", false);
                    data.put("message", "Không có account để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
            profileTask.setTask_index(profileTask.getTask_index()+1);
            List<TaskPriority> priorityTasks =taskPriorityRepository.get_Priority_Task_By_Platform(profileTask.getPlatform());
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
                while(arrTask.remove(task)) {}
                if(profileTask.getPlatform().equals("tiktok")){
                    if(task.equals("tiktok_follower")){
                        get_task= tiktokTask.tiktok_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_like")){
                        get_task=tiktokTask.tiktok_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_view")){
                        get_task=tiktokTask.tiktok_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_comment")){
                        get_task=tiktokTask.tiktok_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("youtube")){
                    if(task.equals("youtube_view")){
                        get_task=youtubeTask.youtube_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("youtube_like")){
                        get_task=youtubeTask.youtube_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("youtube_subscriber")){
                        get_task=youtubeTask.youtube_subscriber(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("facebook")){
                    if(task.equals("facebook_follower")){
                        get_task=facebookTask.facebook_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_like")){
                        get_task=facebookTask.facebook_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_view")){
                        get_task=facebookTask.facebook_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_comment")){
                        get_task=facebookTask.facebook_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_member")){
                        get_task=facebookTask.facebook_member(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("x")){
                    if(task.equals("x_follower")){
                        get_task=xTask.x_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_like")){
                        get_task=xTask.x_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_view")){
                        get_task=xTask.x_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_comment")){
                        get_task=xTask.x_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_repost")){
                        get_task=xTask.x_repost(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("instagram")){
                    if(task.equals("instagram_follower")){
                        get_task=instagramTask.instagram_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_like")){
                        get_task=instagramTask.instagram_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_view")){
                        get_task=instagramTask.instagram_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_comment")){
                        get_task=instagramTask.instagram_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("threads")){
                    if(task.equals("threads_follower")){
                        get_task=threadsTask.threads_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_like")){
                        get_task=threadsTask.threads_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_view")){
                        get_task=threadsTask.threads_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_comment")){
                        get_task=threadsTask.threads_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_repost")){
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
                dataJson.put("task_index",profileTask.getTask_index());
                Integer platform_task=platformRepository.get_Activity_Platform(dataJson.get("platform").toString());
                dataJson.put("activity",platform_task==0?false:true);
                //System.out.println(dataJson);
                respJson.put("status",true);
                respJson.put("data",dataJson);
                //--------------------------------------------//
                profileTask.setGet_time(System.currentTimeMillis());
                profileTask.setOrder_id(Long.parseLong(dataJson.get("order_id").toString()));
                profileTask.setRunning(1);
                profileTask.setTask(dataJson.get("task").toString());
                profileTask.setPlatform(dataJson.get("platform").toString());
                profileTask.setTask_key(dataJson.get("task_key").toString());
                profileTaskRepository.save(profileTask);
                //--------------------------------------------//
                dataJson.remove("order_id");
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
    @GetMapping(value = "getTasOwner", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> getTasOwner(@RequestHeader(defaultValue = "") String Authorization,
                                                   @RequestParam(defaultValue = "") String device_id,
                                                   @RequestParam(defaultValue = "") String profile_id,
                                                   @RequestParam(defaultValue = "") String platform) throws InterruptedException {

        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran=new Random();
            Thread.sleep(ran.nextInt(1000));
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
            if (checktoken ==0) {
                resp.put("status", false);
                data.put("message", "Token expired");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (device_id.length()==0) {
                resp.put("status", false);
                data.put("message", "device_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (profile_id.length()==0) {
                resp.put("status", false);
                data.put("message", "profile_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //---------------------get_Account------------------//
            SettingSystem settingSystem=settingSystemRepository.get_Setting_System();
            Device device=deviceRepository.check_DeviceId(device_id.trim());
            ProfileTask profileTask=null;
            if(device==null){
                resp.put("status", false);
                data.put("message", "device_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            device.setUpdate_time(System.currentTimeMillis());
            deviceRepository.save(device);
            profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
            if(profileTask==null){
                resp.put("status", false);
                data.put("message", "profile_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //update time check
            profileTask.setUpdate_time(System.currentTimeMillis());


            if(accountProfileRepository.check_AccountLive_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube")==0){
                AccountProfile accountProfile_Check=accountProfileRepository.get_Account_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube");
                if(accountProfile_Check==null){ // If account null or not live then get new acc
                    Account account_get=null;
                    if(mySQLCheck.getValue()<settingSystem.getMax_mysql()){
                        String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                        String code="";
                        for(int i=0;i<50;i++){
                            Integer ranver=ran.nextInt(stringrand.length());
                            code=code+stringrand.charAt(ranver);
                        }
                        account_get= accountRepository.get_Account_Gmail_By_ProfileId(device_id.trim()+"_"+profile_id.trim(),device_id,System.currentTimeMillis(),code);
                    }
                    if(account_get!=null){
                        AccountProfile accountProfile=new AccountProfile();
                        accountProfile.setAccount_id(account_get.getAccount_id()+"|"+"youtube");
                        accountProfile.setPassword(account_get.getPassword());
                        accountProfile.setRecover(account_get.getRecover_mail());
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
                        data.put("task", "login");
                        data.put("task_key", account_get.getAccount_id());
                        data.put("account_id", account_get.getAccount_id());
                        data.put("password", account_get.getPassword().trim());
                        data.put("recover_mail", account_get.getRecover_mail().trim());
                        data.put("auth_2fa", account_get.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }else{
                        profileTask = profileTaskRepository.get_Profile_Get_Task(device_id.trim());
                        if(device.getNum_profile()>1){
                            resp.put("status", true);
                            data.put("platform", "system");
                            data.put("task", "profile_changer");
                            data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                            resp.put("data",data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }else if(device.getNum_profile()==1){
                            profileTask.setUpdate_time(System.currentTimeMillis());
                            profileTaskRepository.save(profileTask);
                            profileTask = profileTaskRepository.get_ProfileId_Can_Running_By_DeviceId(device_id.trim());
                        }else{
                            resp.put("status", false);
                            data.put("message", "Không có account_id để chạy");
                            resp.put("data", data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }
                    }
                }else if(profileTask.getRequest_index()>0){ //if live=-1
                    profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                    profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                    if(device.getNum_profile()>1){
                        profileTask = profileTaskRepository.get_Profile_Get_Task(device_id.trim());

                        resp.put("status", true);
                        data.put("platform", "system");
                        data.put("task", "profile_changer");
                        data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }else if(device.getNum_profile()==1){

                        profileTask.setRequest_index(1);
                        profileTaskRepository.save(profileTask);
                        resp.put("status", true);
                        data.put("platform", "youtube");
                        data.put("task", "login");
                        data.put("task_key", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                        data.put("account_id", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                        data.put("password", accountProfile_Check.getPassword().trim());
                        data.put("recover_mail", accountProfile_Check.getRecover().trim());
                        data.put("auth_2fa", accountProfile_Check.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);

                    }else{
                        resp.put("status", false);
                        data.put("message", "Không có account_id để chạy");
                        resp.put("data", data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }else if(accountProfile_Check.getLive()!=1) {
                    profileTask.setRequest_index(1);
                    profileTaskRepository.save(profileTask);
                    resp.put("status", true);
                    data.put("platform", "youtube");
                    data.put("task", "login");
                    data.put("task_key", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                    data.put("account_id", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                    data.put("password", accountProfile_Check.getPassword().trim());
                    data.put("recover_mail", accountProfile_Check.getRecover().trim());
                    data.put("auth_2fa", accountProfile_Check.getAuth_2fa().trim());
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
            //////////////////////////

            if(profileTask.getState()==0 || profileTask.getPlatform().length()==0){
                profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                entityManager.clear();
                profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                profileTask.setUpdate_time(System.currentTimeMillis());
                String task_List="";
                if(platform.length()==0){
                    List<String> string_Task_List=platformRepository.get_All_Platform_True();
                    task_List=String.join(",", string_Task_List);
                }else{
                    task_List=platform;
                }
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
            if(profileTask.getTask_index()>=platformRepository.get_Priority_By_Platform(profileTask.getPlatform()))
            {
                if(profileTask.getTask_list().trim().length()==0){
                    profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                    entityManager.clear();
                    profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                    if(device.getNum_profile()==1){
                        profileTask.setUpdate_time(System.currentTimeMillis());
                        String task_List="";
                        if(platform.length()==0){
                            List<String> string_Task_List=platformRepository.get_All_Platform_True();
                            task_List=String.join(",", string_Task_List);
                        }else{
                            task_List=platform;
                        }
                        List<String> arrPlatform=new ArrayList<>(Arrays.asList(task_List.split(",")));
                        profileTask.setPlatform(arrPlatform.get(0));
                        List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                        task_List=String.join(",", subPlatform);
                        profileTask.setTask_list(task_List);
                        profileTask.setTask_index(0);
                        profileTask.setState(1);
                        profileTask.setRequest_index(0);
                        profileTaskRepository.save(profileTask);
                    }else{
                        profileTask=null;
                    }
                }else {
                    String task_List = "";
                    if (platform.length() == 0) {
                        task_List = profileTask.getTask_list();
                    } else {
                        task_List = platform;
                    }
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
                        String task_List = "";
                        if (platform.length() == 0) {
                            task_List = profileTask.getTask_list();
                        } else {
                            task_List = platform;
                        }
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
                        JSONArray domains= MailApi.getDomains();
                        String stringrand="abcdefhijkprstuvwx0123456789";
                        String mail="";
                        Boolean success=false;
                        while (!success){
                            for(int i=0;i<20;i++){
                                Integer ranver=ran.nextInt(stringrand.length());
                                mail=mail+stringrand.charAt(ranver);
                            }
                            mail=mail+"@"+domains.get(ran.nextInt(domains.size()));
                            success=MailApi.createMail(mail);
                        }
                        if(success){
                            String password="Cmc#";
                            String passrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                            for(int i=0;i<15;i++){
                                Integer ranver=ran.nextInt(passrand.length());
                                password=password+passrand.charAt(ranver);
                            }
                            AccountProfile accountProfile=new AccountProfile();
                            accountProfile.setAccount_id(mail+"|"+profileTask.getPlatform());
                            accountProfile.setPassword(password);
                            accountProfile.setRecover(mail);
                            accountProfile.setPlatform(profileTask.getPlatform());
                            accountProfile.setLive(0);
                            accountProfile.setChanged(0);
                            accountProfile.setAuth_2fa("");
                            accountProfile.setProfileTask(profileTask);
                            accountProfile.setAdd_time(System.currentTimeMillis());
                            accountProfile.setUpdate_time(0L);
                            accountProfileRepository.save(accountProfile);
                            try{
                                Account account=new Account();
                                account.setAccount_id(mail);
                                account.setPassword(password);
                                account.setRecover_mail(mail);
                                account.setPlatform(profileTask.getPlatform());
                                account.setLive(1);
                                account.setRunning(1);
                                account.setAuth_2fa("");
                                account.setProfile_id(profileTask.getProfile_id());
                                account.setDevice_id(profileTask.getDevice().getDevice_id());
                                account.setAdd_time(System.currentTimeMillis());
                                accountRepository.save(account);
                            }catch (Exception e){
                            }
                            profileTask.setRequest_index(1);
                            profileTaskRepository.save(profileTask);
                            resp.put("status", true);
                            data.put("platform", profileTask.getPlatform());
                            data.put("task", "register");
                            data.put("task_key", mail);
                            data.put("account_id", mail);
                            data.put("password", password);
                            data.put("recover_mail", mail);
                            data.put("auth_2fa", "");
                            resp.put("data",data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }
                    }else if(accountProfile_Check_Platform.getLive()!=1){

                        profileTask.setRequest_index(1);
                        profileTaskRepository.save(profileTask);

                        resp.put("status", true);
                        data.put("platform",profileTask.getPlatform());
                        data.put("task", "register");
                        data.put("task_key", accountProfile_Check_Platform.getAccount_id().substring(0,accountProfile_Check_Platform.getAccount_id().indexOf("|")));
                        data.put("account_id", accountProfile_Check_Platform.getAccount_id().substring(0,accountProfile_Check_Platform.getAccount_id().indexOf("|")));
                        data.put("password", accountProfile_Check_Platform.getPassword().trim());
                        data.put("recover_mail", accountProfile_Check_Platform.getRecover().trim());
                        data.put("auth_2fa", accountProfile_Check_Platform.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                    profileTask.setAccount_id(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),profileTask.getPlatform()));
                    profileTaskRepository.save(profileTask);
                }

            }


            if(profileTask==null){
                profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                entityManager.clear();
                profileTask = profileTaskRepository.get_Profile_Get_Task(device_id.trim());
                if(device.getNum_profile()>1){
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "profile_changer");
                    data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else if(device.getNum_profile()==1){
                    profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                    profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                    profileTask.setUpdate_time(System.currentTimeMillis());
                    profileTaskRepository.save(profileTask);
                    profileTask = profileTaskRepository.get_ProfileId_Can_Running_By_DeviceId(device_id.trim());
                }else{
                    resp.put("status", false);
                    data.put("message", "Không có account để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
            profileTask.setTask_index(profileTask.getTask_index()+1);
            List<TaskPriority> priorityTasks =taskPriorityRepository.get_Priority_Task_By_Platform(profileTask.getPlatform());
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
                while(arrTask.remove(task)) {}
                if(profileTask.getPlatform().equals("tiktok")){
                    if(task.equals("tiktok_follower")){
                        get_task= tiktokTask.tiktok_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_like")){
                        get_task=tiktokTask.tiktok_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_view")){
                        get_task=tiktokTask.tiktok_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_comment")){
                        get_task=tiktokTask.tiktok_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("youtube")){
                    if(task.equals("youtube_view")){
                        get_task=youtubeTask.youtube_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("youtube_like")){
                        get_task=youtubeTask.youtube_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("youtube_subscriber")){
                        get_task=youtubeTask.youtube_subscriber(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("facebook")){
                    if(task.equals("facebook_follower")){
                        get_task=facebookTask.facebook_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_like")){
                        get_task=facebookTask.facebook_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_view")){
                        get_task=facebookTask.facebook_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_comment")){
                        get_task=facebookTask.facebook_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_member")){
                        get_task=facebookTask.facebook_member(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("x")){
                    if(task.equals("x_follower")){
                        get_task=xTask.x_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_like")){
                        get_task=xTask.x_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_view")){
                        get_task=xTask.x_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_comment")){
                        get_task=xTask.x_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_repost")){
                        get_task=xTask.x_repost(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("instagram")){
                    if(task.equals("instagram_follower")){
                        get_task=instagramTask.instagram_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_like")){
                        get_task=instagramTask.instagram_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_view")){
                        get_task=instagramTask.instagram_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_comment")){
                        get_task=instagramTask.instagram_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("threads")){
                    if(task.equals("threads_follower")){
                        get_task=threadsTask.threads_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_like")){
                        get_task=threadsTask.threads_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_view")){
                        get_task=threadsTask.threads_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_comment")){
                        get_task=threadsTask.threads_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_repost")){
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
                dataJson.put("task_index",profileTask.getTask_index());
                Integer platform_task=platformRepository.get_Activity_Platform(dataJson.get("platform").toString());
                dataJson.put("activity",platform_task==0?false:true);
                //System.out.println(dataJson);
                respJson.put("status",true);
                respJson.put("data",dataJson);
                //--------------------------------------------//
                profileTask.setGet_time(System.currentTimeMillis());
                profileTask.setOrder_id(Long.parseLong(dataJson.get("order_id").toString()));
                profileTask.setRunning(1);
                profileTask.setTask(dataJson.get("task").toString());
                profileTask.setPlatform(dataJson.get("platform").toString());
                profileTask.setTask_key(dataJson.get("task_key").toString());
                profileTaskRepository.save(profileTask);
                //--------------------------------------------//
                dataJson.remove("order_id");
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
    @GetMapping(value = "getTask003OFF", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> getTask003OFF(@RequestHeader(defaultValue = "") String Authorization,
                                                   @RequestParam(defaultValue = "") String device_id,
                                                   @RequestParam(defaultValue = "") String profile_id,
                                                   @RequestParam(defaultValue = "") String platform) throws InterruptedException {

        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran=new Random();
            //Thread.sleep(ran.nextInt(2000));
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
            if (checktoken ==0) {
                resp.put("status", false);
                data.put("message", "Token expired");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (device_id.length()==0) {
                resp.put("status", false);
                data.put("message", "device_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (profile_id.length()==0) {
                resp.put("status", false);
                data.put("message", "profile_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //---------------------get_Account------------------//
            SettingSystem settingSystem=settingSystemRepository.get_Setting_System();
            Device device=deviceRepository.check_DeviceId(device_id.trim());
            ProfileTask profileTask=null;
            if(device==null){
                resp.put("status", false);
                data.put("message", "device_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            device.setUpdate_time(System.currentTimeMillis());
            deviceRepository.save(device);
            profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
            if(profileTask==null&&!profile_id.trim().equals("0")){
                resp.put("status", false);
                data.put("message", "profile_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }else if(profile_id.trim().equals("0")){
                profileTask = profileTaskRepository.get_Profile_Get_Task(device_id.trim());
                if(device.getNum_profile()>1){
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
            }else if(profileTask==null){
                resp.put("status", false);
                data.put("message", "profile_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if(profileTask.getRequest_index()==0){
                profileTask.setUpdate_time(System.currentTimeMillis());
                profileTaskRepository.save(profileTask);
            }
            //Check profile isLogin Google True
            if(accountProfileRepository.check_AccountLive_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube")==0){
                AccountProfile accountProfile_Check=accountProfileRepository.get_Account_By_ProfileId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube");
                if(accountProfile_Check==null){ // If account null or not live then get new acc
                    Account account_get=null;
                    if(mySQLCheck.getValue()<settingSystem.getMax_mysql()){
                        String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                        String code="";
                        for(int i=0;i<50;i++){
                            Integer ranver=ran.nextInt(stringrand.length());
                            code=code+stringrand.charAt(ranver);
                        }
                        account_get= accountRepository.get_Account_Gmail_By_ProfileId(device_id.trim()+"_"+profile_id.trim(),device_id,System.currentTimeMillis(),code);
                    }
                    if(account_get!=null){
                        AccountProfile accountProfile=new AccountProfile();
                        accountProfile.setAccount_id(account_get.getAccount_id()+"|"+"youtube");
                        accountProfile.setPassword(account_get.getPassword());
                        accountProfile.setRecover(account_get.getRecover_mail());
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
                        data.put("task", "login");
                        data.put("task_key", account_get.getAccount_id());
                        data.put("account_id", account_get.getAccount_id());
                        data.put("password", account_get.getPassword().trim());
                        data.put("recover_mail", account_get.getRecover_mail().trim());
                        data.put("auth_2fa", account_get.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }else{
                        profileTask = profileTaskRepository.get_Profile_Get_Task(device_id.trim());
                        if(device.getNum_profile()>1){
                            resp.put("status", true);
                            data.put("platform", "system");
                            data.put("task", "profile_changer");
                            data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                            resp.put("data",data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }else if(device.getNum_profile()==1){
                            profileTask.setUpdate_time(System.currentTimeMillis());
                            profileTaskRepository.save(profileTask);
                            profileTask = profileTaskRepository.get_ProfileId_Can_Running_By_DeviceId(device_id.trim());
                        }else{
                            resp.put("status", false);
                            data.put("message", "Không có account_id để chạy");
                            resp.put("data", data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }
                    }
                }else if((accountProfile_Check.getLive()==-1 && profileTask.getRequest_index()>0) ||
                        (accountProfile_Check.getLive()==0 && profileTask.getRequest_index()>1)
                ){ //if live=-1
                    profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                    profileTask = profileTaskRepository.get_Profile_Get_Task(device_id.trim());
                    if(device.getNum_profile()>1){
                        resp.put("status", true);
                        data.put("platform", "system");
                        data.put("task", "profile_changer");
                        data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }else if(device.getNum_profile()==1){
                        profileTask.setUpdate_time(System.currentTimeMillis());
                        profileTaskRepository.save(profileTask);
                    }else{
                        resp.put("status", false);
                        data.put("message", "Không có account_id để chạy");
                        resp.put("data", data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }else if(accountProfile_Check.getLive()!=1) {
                    profileTask.setRequest_index(profileTask.getRequest_index()+1);
                    profileTaskRepository.save(profileTask);
                    resp.put("status", true);
                    data.put("platform", "youtube");
                    data.put("task", "login");
                    data.put("task_key", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                    data.put("account_id", accountProfile_Check.getAccount_id().substring(0,accountProfile_Check.getAccount_id().indexOf("|")));
                    data.put("password", accountProfile_Check.getPassword().trim());
                    data.put("recover_mail", accountProfile_Check.getRecover().trim());
                    data.put("auth_2fa", accountProfile_Check.getAuth_2fa().trim());
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
            //////////////////////////
            //--------------------end_get_Account----------------------//
            profileTask = profileTaskRepository.check_ProfileId_Running(device_id.trim()+"_"+profile_id);//Check số lần get nhiệm vụ của 1 account
            if(profileTask!=null){
                if(profileTask.getTask_index()>=platformRepository.get_Priority_By_Platform(profileTask.getPlatform())){
                    if(profileTask.getTask_list().trim().length()==0){
                        profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                        profileTask=null;
                    }else{
                        String task_List="";
                        if(platform.length()==0){
                            task_List=profileTask.getTask_list();
                        }else{
                            task_List=platform;
                        }
                        List<String> arrPlatform =new ArrayList<>(Arrays.asList(task_List.split(",")));
                        while (arrPlatform.size()>0){
                            if(accountProfileRepository.check_AccountLive_By_ProfileId_And_Platform(profileTask.getProfile_id(),arrPlatform.get(0))==0&&!arrPlatform.get(0).equals("youtube")){
                                AccountProfile accountProfile_Check_Platform=accountProfileRepository.get_Account_By_ProfileId_And_Platform(profileTask.getProfile_id(), arrPlatform.get(0));
                                if(accountProfile_Check_Platform==null || (accountProfile_Check_Platform!=null?accountProfile_Check_Platform.getLive()>1:false)  ){
                                    JSONArray domains= MailApi.getDomains();
                                    String stringrand="abcdefhijkprstuvwx0123456789";
                                    String mail="";
                                    Boolean success=false;
                                    while (!success){
                                        for(int i=0;i<20;i++){
                                            Integer ranver=ran.nextInt(stringrand.length());
                                            mail=mail+stringrand.charAt(ranver);
                                        }
                                        mail=mail+"@"+domains.get(ran.nextInt(domains.size()));
                                        success=MailApi.createMail(mail);
                                    }
                                    if(success){
                                        String password="Cmc#";
                                        String passrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                                        for(int i=0;i<15;i++){
                                            Integer ranver=ran.nextInt(passrand.length());
                                            password=password+passrand.charAt(ranver);
                                        }
                                        AccountProfile accountProfile=new AccountProfile();
                                        accountProfile.setAccount_id(mail+"|"+arrPlatform.get(0));
                                        accountProfile.setPassword(password);
                                        accountProfile.setRecover(mail);
                                        accountProfile.setPlatform(arrPlatform.get(0));
                                        accountProfile.setLive(0);
                                        accountProfile.setChanged(0);
                                        accountProfile.setAuth_2fa("");
                                        accountProfile.setProfileTask(profileTask);
                                        accountProfile.setAdd_time(System.currentTimeMillis());
                                        accountProfile.setUpdate_time(0L);
                                        accountProfileRepository.save(accountProfile);
                                        try{
                                            Account account=new Account();
                                            account.setAccount_id(mail);
                                            account.setPassword(password);
                                            account.setRecover_mail(mail);
                                            account.setPlatform(arrPlatform.get(0));
                                            account.setLive(1);
                                            account.setRunning(1);
                                            account.setAuth_2fa("");
                                            account.setProfile_id(profileTask.getProfile_id());
                                            account.setDevice_id(profileTask.getDevice().getDevice_id());
                                            account.setAdd_time(System.currentTimeMillis());
                                            accountRepository.save(account);
                                        }catch (Exception e){
                                        }

                                        profileTask.setPlatform(arrPlatform.get(0));
                                        List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                                        task_List=String.join(",", subPlatform);
                                        profileTask.setTask_list(task_List);
                                        profileTask.setAccount_id(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),arrPlatform.get(0)));

                                        resp.put("status", true);
                                        data.put("platform", arrPlatform.get(0));
                                        data.put("task", "register");
                                        data.put("task_key", mail);
                                        data.put("account_id", mail);
                                        data.put("password", password);
                                        data.put("recover_mail", mail);
                                        data.put("auth_2fa", "");
                                        resp.put("data",data);
                                        return new ResponseEntity<>(resp, HttpStatus.OK);
                                    }
                                }else if((accountProfile_Check_Platform.getLive()==-1 && profileTask.getRequest_index()>=1) ||
                                        (accountProfile_Check_Platform.getLive()==0 && profileTask.getRequest_index()>=2)
                                ){
                                    if(arrPlatform.size()==1){
                                        profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                                        profileTask=null;
                                        break;
                                    }else {
                                        profileTask.setRequest_index(0);
                                        profileTaskRepository.save(profileTask);
                                        arrPlatform.remove(0);
                                        continue;
                                    }
                                } else if(accountProfile_Check_Platform.getLive()==0){
                                    profileTask.setPlatform(arrPlatform.get(0));
                                    List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                                    task_List=String.join(",", subPlatform);
                                    profileTask.setTask_list(task_List);
                                    profileTask.setAccount_id(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),arrPlatform.get(0)));
                                    profileTask.setRequest_index(profileTask.getRequest_index()+1);
                                    profileTaskRepository.save(profileTask);
                                    resp.put("status", true);
                                    data.put("platform",arrPlatform.get(0));
                                    data.put("task", "register");
                                    data.put("task_key", accountProfile_Check_Platform.getAccount_id().substring(0,accountProfile_Check_Platform.getAccount_id().indexOf("|")));
                                    data.put("account_id", accountProfile_Check_Platform.getAccount_id().substring(0,accountProfile_Check_Platform.getAccount_id().indexOf("|")));
                                    data.put("password", accountProfile_Check_Platform.getPassword().trim());
                                    data.put("recover_mail", accountProfile_Check_Platform.getRecover().trim());
                                    data.put("auth_2fa", accountProfile_Check_Platform.getAuth_2fa().trim());
                                    resp.put("data",data);
                                    return new ResponseEntity<>(resp, HttpStatus.OK);
                                }
                                else if(accountProfile_Check_Platform.getLive()==-1){
                                    profileTask.setPlatform(arrPlatform.get(0));
                                    List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                                    task_List=String.join(",", subPlatform);
                                    profileTask.setTask_list(task_List);
                                    profileTask.setAccount_id(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),arrPlatform.get(0)));
                                    profileTask.setRequest_index(profileTask.getRequest_index()+1);
                                    profileTaskRepository.save(profileTask);
                                    resp.put("status", true);
                                    data.put("platform",arrPlatform.get(0));
                                    data.put("task", "register");
                                    data.put("task_key", accountProfile_Check_Platform.getAccount_id().substring(0,accountProfile_Check_Platform.getAccount_id().indexOf("|")));
                                    data.put("account_id", accountProfile_Check_Platform.getAccount_id().substring(0,accountProfile_Check_Platform.getAccount_id().indexOf("|")));
                                    data.put("password", accountProfile_Check_Platform.getPassword().trim());
                                    data.put("recover_mail", accountProfile_Check_Platform.getRecover().trim());
                                    data.put("auth_2fa", accountProfile_Check_Platform.getAuth_2fa().trim());
                                    resp.put("data",data);
                                    return new ResponseEntity<>(resp, HttpStatus.OK);
                                }
                                profileTask.setPlatform(arrPlatform.get(0));
                                List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                                task_List=String.join(",", subPlatform);
                                profileTask.setTask_list(task_List);
                                profileTask.setAccount_id(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),arrPlatform.get(0)));
                                profileTaskRepository.save(profileTask);
                                break;
                            }else{
                                profileTask.setPlatform(arrPlatform.get(0));
                                List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                                task_List=String.join(",", subPlatform);
                                profileTask.setTask_list(task_List);
                                profileTask.setAccount_id(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),arrPlatform.get(0)));
                                profileTaskRepository.save(profileTask);
                                break;
                            }
                        }
                    }
                }
            }else{
                profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                profileTask = profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id);
                profileTask.setUpdate_time(System.currentTimeMillis());
                String task_List="";
                if(platform.length()==0){
                    List<String> string_Task_List=platformRepository.get_All_Platform_True();
                    task_List=String.join(",", string_Task_List);
                }else{
                    task_List=platform;
                }
                List<String> arrPlatform =new ArrayList<>(Arrays.asList(task_List.split(",")));
                while (arrPlatform.size()>0){
                    if(accountProfileRepository.check_AccountLive_By_ProfileId_And_Platform(profileTask.getProfile_id(),arrPlatform.get(0))==0&&!arrPlatform.get(0).equals("youtube")){
                        AccountProfile accountProfile_Check_Platform=accountProfileRepository.get_Account_By_ProfileId_And_Platform(profileTask.getProfile_id(), arrPlatform.get(0));
                        if(accountProfile_Check_Platform==null || (accountProfile_Check_Platform!=null?accountProfile_Check_Platform.getLive()>1:false)  ){
                            JSONArray domains= MailApi.getDomains();
                            String stringrand="abcdefhijkprstuvwx0123456789";
                            String mail="";
                            Boolean success=false;
                            while (!success){
                                for(int i=0;i<20;i++){
                                    Integer ranver=ran.nextInt(stringrand.length());
                                    mail=mail+stringrand.charAt(ranver);
                                }
                                mail=mail+"@"+domains.get(ran.nextInt(domains.size()));
                                success=MailApi.createMail(mail);
                            }
                            if(success){
                                String password="Cmc#";
                                String passrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                                for(int i=0;i<15;i++){
                                    Integer ranver=ran.nextInt(passrand.length());
                                    password=password+passrand.charAt(ranver);
                                }
                                AccountProfile accountProfile=new AccountProfile();
                                accountProfile.setAccount_id(mail+"|"+arrPlatform.get(0));
                                accountProfile.setPassword(password);
                                accountProfile.setRecover(mail);
                                accountProfile.setPlatform(arrPlatform.get(0));
                                accountProfile.setLive(0);
                                accountProfile.setChanged(0);
                                accountProfile.setAuth_2fa("");
                                accountProfile.setProfileTask(profileTask);
                                accountProfile.setAdd_time(System.currentTimeMillis());
                                accountProfile.setUpdate_time(0L);
                                accountProfileRepository.save(accountProfile);
                                try{
                                    Account account=new Account();
                                    account.setAccount_id(mail);
                                    account.setPassword(password);
                                    account.setRecover_mail(mail);
                                    account.setPlatform(arrPlatform.get(0));
                                    account.setLive(1);
                                    account.setRunning(1);
                                    account.setAuth_2fa("");
                                    account.setProfile_id(profileTask.getProfile_id());
                                    account.setDevice_id(profileTask.getDevice().getDevice_id());
                                    account.setAdd_time(System.currentTimeMillis());
                                    accountRepository.save(account);
                                }catch (Exception e){
                                }
                                profileTask.setPlatform(arrPlatform.get(0));
                                List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                                task_List=String.join(",", subPlatform);
                                profileTask.setTask_list(task_List);
                                profileTask.setAccount_id(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),arrPlatform.get(0)));

                                resp.put("status", true);
                                data.put("platform", arrPlatform.get(0));
                                data.put("task", "register");
                                data.put("task_key", mail);
                                data.put("account_id", mail);
                                data.put("password", password);
                                data.put("recover_mail", mail);
                                data.put("auth_2fa", "");
                                resp.put("data",data);
                                return new ResponseEntity<>(resp, HttpStatus.OK);

                            }
                        }else if((accountProfile_Check_Platform.getLive()==-1 && profileTask.getRequest_index()>=1) ||
                                (accountProfile_Check_Platform.getLive()==0 && profileTask.getRequest_index()>=2)
                        ){
                            if(arrPlatform.size()==1){
                                profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                                profileTask=null;
                                break;
                            }else {
                                profileTask.setRequest_index(0);
                                profileTaskRepository.save(profileTask);
                                arrPlatform.remove(0);
                                continue;
                            }
                        } else if(accountProfile_Check_Platform.getLive()==0){
                            profileTask.setPlatform(arrPlatform.get(0));
                            List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                            task_List=String.join(",", subPlatform);
                            profileTask.setTask_list(task_List);
                            profileTask.setAccount_id(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),arrPlatform.get(0)));
                            profileTask.setRequest_index(profileTask.getRequest_index()+1);
                            profileTaskRepository.save(profileTask);

                            resp.put("status", true);
                            data.put("platform",arrPlatform.get(0));
                            data.put("task", "register");
                            data.put("task_key", accountProfile_Check_Platform.getAccount_id().substring(0,accountProfile_Check_Platform.getAccount_id().indexOf("|")));
                            data.put("account_id", accountProfile_Check_Platform.getAccount_id().substring(0,accountProfile_Check_Platform.getAccount_id().indexOf("|")));
                            data.put("password", accountProfile_Check_Platform.getPassword().trim());
                            data.put("recover_mail", accountProfile_Check_Platform.getRecover().trim());
                            data.put("auth_2fa", accountProfile_Check_Platform.getAuth_2fa().trim());
                            resp.put("data",data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }else if(accountProfile_Check_Platform.getLive()==-1){
                            profileTask.setPlatform(arrPlatform.get(0));
                            List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                            task_List=String.join(",", subPlatform);
                            profileTask.setTask_list(task_List);
                            profileTask.setAccount_id(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),arrPlatform.get(0)));
                            profileTask.setRequest_index(profileTask.getRequest_index()+1);
                            profileTaskRepository.save(profileTask);
                            resp.put("status", true);
                            data.put("platform",arrPlatform.get(0));
                            data.put("task", "register");
                            data.put("task_key", accountProfile_Check_Platform.getAccount_id().substring(0,accountProfile_Check_Platform.getAccount_id().indexOf("|")));
                            data.put("account_id", accountProfile_Check_Platform.getAccount_id().substring(0,accountProfile_Check_Platform.getAccount_id().indexOf("|")));
                            data.put("password", accountProfile_Check_Platform.getPassword().trim());
                            data.put("recover_mail", accountProfile_Check_Platform.getRecover().trim());
                            data.put("auth_2fa", accountProfile_Check_Platform.getAuth_2fa().trim());
                            resp.put("data",data);
                            return new ResponseEntity<>(resp, HttpStatus.OK);
                        }
                        profileTask.setPlatform(arrPlatform.get(0));
                        List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                        task_List=String.join(",", subPlatform);
                        profileTask.setTask_list(task_List);
                        profileTask.setAccount_id(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),arrPlatform.get(0)));
                        profileTaskRepository.save(profileTask);
                        break;
                    }else{
                        profileTask.setPlatform(arrPlatform.get(0));
                        List<String> subPlatform = arrPlatform.subList(1, arrPlatform.size());
                        task_List=String.join(",", subPlatform);
                        profileTask.setTask_list(task_List);
                        profileTask.setAccount_id(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),arrPlatform.get(0)));
                        profileTaskRepository.save(profileTask);
                        break;
                    }
                }

            }
            if(profileTask==null){
                profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                profileTask = profileTaskRepository.get_Profile_Get_Task(device_id.trim());
                if(device.getNum_profile()>1){
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "profile_changer");
                    data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else if(device.getNum_profile()==1){
                    profileTask.setUpdate_time(System.currentTimeMillis());
                    profileTaskRepository.save(profileTask);
                    profileTask = profileTaskRepository.get_ProfileId_Can_Running_By_DeviceId(device_id.trim());
                }else{
                    resp.put("status", false);
                    data.put("message", "Không có account để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
            profileTask.setTask_index(profileTask.getTask_index()+1);
            List<TaskPriority> priorityTasks =taskPriorityRepository.get_Priority_Task_By_Platform(profileTask.getPlatform());
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
                while(arrTask.remove(task)) {}
                if(profileTask.getPlatform().equals("tiktok")){
                    if(task.equals("tiktok_follower")){
                        get_task= tiktokTask.tiktok_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_like")){
                        get_task=tiktokTask.tiktok_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_view")){
                        get_task=tiktokTask.tiktok_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_comment")){
                        get_task=tiktokTask.tiktok_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("youtube")){
                    if(task.equals("youtube_view")){
                        get_task=youtubeTask.youtube_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("youtube_like")){
                        get_task=youtubeTask.youtube_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("youtube_subscriber")){
                        get_task=youtubeTask.youtube_subscriber(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("facebook")){
                    if(task.equals("facebook_follower")){
                        get_task=facebookTask.facebook_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_like")){
                        get_task=facebookTask.facebook_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_view")){
                        get_task=facebookTask.facebook_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_comment")){
                        get_task=facebookTask.facebook_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_member")){
                        get_task=facebookTask.facebook_member(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("x")){
                    if(task.equals("x_follower")){
                        get_task=xTask.x_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_like")){
                        get_task=xTask.x_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_view")){
                        get_task=xTask.x_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_comment")){
                        get_task=xTask.x_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_repost")){
                        get_task=xTask.x_repost(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("instagram")){
                    if(task.equals("instagram_follower")){
                        get_task=instagramTask.instagram_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_like")){
                        get_task=instagramTask.instagram_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_view")){
                        get_task=instagramTask.instagram_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_comment")){
                        get_task=instagramTask.instagram_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("threads")){
                    if(task.equals("threads_follower")){
                        get_task=threadsTask.threads_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_like")){
                        get_task=threadsTask.threads_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_view")){
                        get_task=threadsTask.threads_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_comment")){
                        get_task=threadsTask.threads_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_repost")){
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
                dataJson.put("task_index",profileTask.getTask_index());
                Integer platform_task=platformRepository.get_Activity_Platform(dataJson.get("platform").toString());
                dataJson.put("activity",platform_task==0?false:true);
                //System.out.println(dataJson);
                respJson.put("status",true);
                respJson.put("data",dataJson);
                //--------------------------------------------//
                profileTask.setGet_time(System.currentTimeMillis());
                profileTask.setOrder_id(Long.parseLong(dataJson.get("order_id").toString()));
                profileTask.setRunning(1);
                profileTask.setTask(dataJson.get("task").toString());
                profileTask.setPlatform(dataJson.get("platform").toString());
                profileTask.setTask_key(dataJson.get("task_key").toString());
                profileTaskRepository.save(profileTask);
                //--------------------------------------------//
                dataJson.remove("order_id");
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
    @GetMapping(value = "getTask002OFF", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> getTask002OFF(@RequestHeader(defaultValue = "") String Authorization,
                                                   @RequestParam(defaultValue = "") String device_id,
                                                   @RequestParam(defaultValue = "") String profile_id,
                                                   @RequestParam(defaultValue = "") String platform) throws InterruptedException {

        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran=new Random();
            //Thread.sleep(ran.nextInt(2000));
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
            if (checktoken ==0) {
                resp.put("status", false);
                data.put("message", "Token expired");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (device_id.length()==0) {
                resp.put("status", false);
                data.put("message", "device_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (profile_id.length()==0) {
                resp.put("status", false);
                data.put("message", "profile_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //---------------------get_Account------------------//
            SettingSystem settingSystem=settingSystemRepository.get_Setting_System();
            Device device=deviceRepository.check_DeviceId(device_id.trim());
            ProfileTask profileTask=null;
            if(device==null){
                resp.put("status", false);
                data.put("message", "device_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            device.setUpdate_time(System.currentTimeMillis());
            deviceRepository.save(device);
            profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
            if(profileTask==null&&!profile_id.trim().equals("0")){
                resp.put("status", false);
                data.put("message", "profile_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }else if(profile_id.trim().equals("0")){
                profileTask = profileTaskRepository.get_Profile_Get_Task(device_id.trim());
                if(device.getNum_profile()>1){
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "profile_changer");
                    data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else if(device.getNum_profile()==1){
                    profileTask.setUpdate_time(System.currentTimeMillis());
                    profileTaskRepository.save(profileTask);
                    profileTask = profileTaskRepository.get_ProfileId_Can_Running_By_DeviceId(device_id.trim());
                }else{
                    resp.put("status", false);
                    data.put("message", "Không có account_id để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }else if(profileTask==null){
                resp.put("status", false);
                data.put("message", "profile_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }

            //Check profile isLogin Google
            if(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube")==null){
                Account account_get=null;
                if(mySQLCheck.getValue()<settingSystem.getMax_mysql()){
                    String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                    String code="";
                    for(int i=0;i<50;i++){
                        Integer ranver=ran.nextInt(stringrand.length());
                        code=code+stringrand.charAt(ranver);
                    }
                    account_get= accountRepository.get_Account_Youtube_By_ProfileId(device_id.trim()+"_"+profile_id.trim(),device_id,System.currentTimeMillis(),code,device.getMode().trim());
                }
                if(account_get!=null){
                    AccountProfile accountProfile=new AccountProfile();
                    accountProfile.setAccount_id(account_get.getAccount_id()+"|"+"youtube");
                    accountProfile.setPassword(account_get.getPassword());
                    accountProfile.setRecover(account_get.getRecover_mail());
                    accountProfile.setPlatform("youtube");
                    accountProfile.setLive(1);
                    accountProfile.setChanged(0);
                    accountProfile.setAuth_2fa(account_get.getAuth_2fa());
                    accountProfile.setProfileTask(profileTask);
                    accountProfile.setAdd_time(System.currentTimeMillis());
                    accountProfile.setUpdate_time(0L);
                    accountProfileRepository.save(accountProfile);

                }
            }
            //--------------------end_get_Account----------------------//
            profileTask = profileTaskRepository.check_ProfileId_Running(device_id.trim()+"_"+profile_id);//Check số lần get nhiệm vụ của 1 account
            if(profileTask!=null){

                if(profileTask.getTask_index()>=platformRepository.get_Priority_By_Platform(profileTask.getPlatform())){
                    if(profileTask.getTask_list().trim().length()==0){
                        profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                        profileTask=null;
                    }else{
                        String task_List="";
                        if(platform.length()==0){
                            task_List=profileTask.getTask_list();
                        }else{
                            task_List=platform;
                        }
                        Integer index_Off=task_List.indexOf(',')<0?task_List.length():task_List.indexOf(',');
                        String platform_index=task_List.indexOf(',')<0?task_List:task_List.substring(0,index_Off);
                        if(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),platform_index)==null){
                            JSONArray domains= MailApi.getDomains();
                            String stringrand="abcdefhijkprstuvwx0123456789";
                            String mail="";
                            Boolean success=false;
                            while (!success){
                                for(int i=0;i<20;i++){
                                    Integer ranver=ran.nextInt(stringrand.length());
                                    mail=mail+stringrand.charAt(ranver);
                                }
                                mail=mail+"@"+domains.get(ran.nextInt(domains.size()));
                                success=MailApi.createMail(mail);
                            }
                            if(success){
                                String password="Cmc#";
                                String passrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                                for(int i=0;i<15;i++){
                                    Integer ranver=ran.nextInt(passrand.length());
                                    password=password+passrand.charAt(ranver);
                                }
                                AccountProfile accountProfile=new AccountProfile();
                                accountProfile.setAccount_id(mail+"|"+platform_index);
                                accountProfile.setPassword(password);
                                accountProfile.setRecover(mail);
                                accountProfile.setPlatform(platform_index);
                                accountProfile.setLive(1);
                                accountProfile.setChanged(0);
                                accountProfile.setAuth_2fa("");
                                accountProfile.setProfileTask(profileTask);
                                accountProfile.setAdd_time(System.currentTimeMillis());
                                accountProfile.setUpdate_time(0L);
                                accountProfileRepository.save(accountProfile);
                                try{
                                    Account account=new Account();
                                    account.setAccount_id(mail);
                                    account.setPassword(password);
                                    account.setRecover_mail(mail);
                                    account.setPlatform(platform_index);
                                    account.setLive(1);
                                    account.setRunning(1);
                                    account.setAuth_2fa("");
                                    account.setProfile_id(profileTask.getProfile_id());
                                    account.setDevice_id(profileTask.getDevice().getDevice_id());
                                    account.setAdd_time(System.currentTimeMillis());
                                    accountRepository.save(account);
                                }catch (Exception e){
                                }
                            }
                        }
                        profileTask.setTask_list(task_List.indexOf(',')<0?"":task_List.substring(index_Off+1));
                        profileTask.setTask_index(0);
                        profileTask.setPlatform(task_List.indexOf(',')<0?task_List:task_List.substring(0,index_Off));
                        profileTask.setAccount_id(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),platform_index));
                    }
                }
            }else{
                profileTaskRepository.reset_Thread_Index_By_DeviceId(device_id.trim());
                profileTask = profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id);
                profileTask.setUpdate_time(System.currentTimeMillis());
                String task_List="";
                if(platform.length()==0){
                    List<String> string_Task_List=platformRepository.get_All_Platform_True();
                    task_List=String.join(",", string_Task_List);
                }else{
                    task_List=platform;
                }
                Integer index_Off=task_List.indexOf(',')<0?task_List.length():task_List.indexOf(',');
                String platform_index=task_List.indexOf(',')<0?task_List:task_List.substring(0,index_Off);
                if(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),platform_index)==null){
                    JSONArray domains= MailApi.getDomains();
                    String stringrand="abcdefhijkprstuvwx0123456789";
                    String mail="";
                    Boolean success=false;
                    while (!success){
                        for(int i=0;i<20;i++){
                            Integer ranver=ran.nextInt(stringrand.length());
                            mail=mail+stringrand.charAt(ranver);
                        }
                        mail=mail+"@"+domains.get(ran.nextInt(domains.size()));
                        success=MailApi.createMail(mail);
                    }
                    if(success){
                        String password="Cmc#";
                        String passrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                        for(int i=0;i<15;i++){
                            Integer ranver=ran.nextInt(passrand.length());
                            password=password+passrand.charAt(ranver);
                        }
                        AccountProfile accountProfile=new AccountProfile();
                        accountProfile.setAccount_id(mail+"|"+platform_index);
                        accountProfile.setPassword(password);
                        accountProfile.setRecover(mail);
                        accountProfile.setPlatform(platform_index);
                        accountProfile.setLive(1);
                        accountProfile.setChanged(0);
                        accountProfile.setAuth_2fa("");
                        accountProfile.setProfileTask(profileTask);
                        accountProfile.setAdd_time(System.currentTimeMillis());
                        accountProfile.setUpdate_time(0L);
                        accountProfileRepository.save(accountProfile);
                        try{
                            Account account=new Account();
                            account.setAccount_id(mail);
                            account.setPassword(password);
                            account.setRecover_mail(mail);
                            account.setPlatform(platform_index);
                            account.setLive(1);
                            account.setRunning(1);
                            account.setAuth_2fa("");
                            account.setProfile_id(profileTask.getProfile_id());
                            account.setDevice_id(profileTask.getDevice().getDevice_id());
                            account.setAdd_time(System.currentTimeMillis());
                            accountRepository.save(account);
                        }catch (Exception e){
                        }
                    }
                }
                profileTask.setTask_list(task_List.indexOf(',')<0?"":task_List.substring(index_Off+1));
                profileTask.setPlatform(task_List.indexOf(',')<0?task_List:task_List.substring(0,index_Off));
                profileTask.setAccount_id(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),platform_index));

            }
            if(profileTask==null){
                profileTaskRepository.reset_Thread_Index_By_ProfileId(device_id.trim()+"_"+profile_id.trim());
                profileTask = profileTaskRepository.get_Profile_Get_Task(device_id.trim());
                if(device.getNum_profile()>1){
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "profile_changer");
                    data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else if(device.getNum_profile()==1){
                    profileTask.setUpdate_time(System.currentTimeMillis());
                    profileTaskRepository.save(profileTask);
                    profileTask = profileTaskRepository.get_ProfileId_Can_Running_By_DeviceId(device_id.trim());
                }else{
                    resp.put("status", false);
                    data.put("message", "Không có account để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
            profileTask.setTask_index(profileTask.getTask_index()+1);
            List<TaskPriority> priorityTasks =taskPriorityRepository.get_Priority_Task_By_Platform(profileTask.getPlatform());
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
                while(arrTask.remove(task)) {}
                if(profileTask.getPlatform().equals("tiktok")){
                    if(task.equals("tiktok_follower")){
                        get_task= tiktokTask.tiktok_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_like")){
                        get_task=tiktokTask.tiktok_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_view")){
                        get_task=tiktokTask.tiktok_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_comment")){
                        get_task=tiktokTask.tiktok_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("youtube")){
                    if(task.equals("youtube_view")){
                        get_task=youtubeTask.youtube_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("youtube_like")){
                        get_task=youtubeTask.youtube_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("youtube_subscriber")){
                        get_task=youtubeTask.youtube_subscriber(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("facebook")){
                    if(task.equals("facebook_follower")){
                        get_task=facebookTask.facebook_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_like")){
                        get_task=facebookTask.facebook_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_view")){
                        get_task=facebookTask.facebook_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_comment")){
                        get_task=facebookTask.facebook_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_member")){
                        get_task=facebookTask.facebook_member(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("x")){
                    if(task.equals("x_follower")){
                        get_task=xTask.x_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_like")){
                        get_task=xTask.x_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_view")){
                        get_task=xTask.x_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_comment")){
                        get_task=xTask.x_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_repost")){
                        get_task=xTask.x_repost(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("instagram")){
                    if(task.equals("instagram_follower")){
                        get_task=instagramTask.instagram_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_like")){
                        get_task=instagramTask.instagram_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_view")){
                        get_task=instagramTask.instagram_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_comment")){
                        get_task=instagramTask.instagram_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("threads")){
                    if(task.equals("threads_follower")){
                        get_task=threadsTask.threads_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_like")){
                        get_task=threadsTask.threads_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_view")){
                        get_task=threadsTask.threads_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_comment")){
                        get_task=threadsTask.threads_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_repost")){
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
                dataJson.put("task_index",profileTask.getTask_index());
                Integer platform_task=platformRepository.get_Activity_Platform(dataJson.get("platform").toString());
                dataJson.put("activity",platform_task==0?false:true);
                //System.out.println(dataJson);
                respJson.put("status",true);
                respJson.put("data",dataJson);
                //--------------------------------------------//
                profileTask.setGet_time(System.currentTimeMillis());
                profileTask.setOrder_id(Long.parseLong(dataJson.get("order_id").toString()));
                profileTask.setRunning(1);
                profileTask.setTask(dataJson.get("task").toString());
                profileTask.setPlatform(dataJson.get("platform").toString());
                profileTask.setTask_key(dataJson.get("task_key").toString());
                profileTaskRepository.save(profileTask);
                //--------------------------------------------//
                dataJson.remove("order_id");
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
    @GetMapping(value = "getTask0023OFF", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> getTask0023OFF(@RequestHeader(defaultValue = "") String Authorization,
                                                   @RequestParam(defaultValue = "") String device_id,
                                                   @RequestParam(defaultValue = "") String profile_id,
                                                   @RequestParam(defaultValue = "") String platform) throws InterruptedException {

        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran=new Random();
            //Thread.sleep(ran.nextInt(2000));
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
            if (checktoken ==0) {
                resp.put("status", false);
                data.put("message", "Token expired");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (device_id.length()==0) {
                resp.put("status", false);
                data.put("message", "device_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (profile_id.length()==0) {
                resp.put("status", false);
                data.put("message", "profile_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //---------------------get_Account------------------//
            SettingSystem settingSystem=settingSystemRepository.get_Setting_System();
            Device device=deviceRepository.check_DeviceId(device_id.trim());
            ProfileTask profileTask=null;
            if(device==null){
                resp.put("status", false);
                data.put("message", "device_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            device.setUpdate_time(System.currentTimeMillis());
            deviceRepository.save(device);
            profileTask =profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
            if(profileTask==null&&!profile_id.trim().equals("0")){
                resp.put("status", false);
                data.put("message", "profile_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }else if(profile_id.trim().equals("0")){
                profileTask = profileTaskRepository.get_Profile_Get_Task(device_id.trim());
                if(device.getNum_profile()>1){
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "profile_changer");
                    data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else if(device.getNum_profile()==1){
                    profileTask.setUpdate_time(System.currentTimeMillis());
                    profileTaskRepository.save(profileTask);
                    profileTask = profileTaskRepository.get_ProfileId_Can_Running_By_DeviceId(device_id.trim());
                }else{
                    resp.put("status", false);
                    data.put("message", "Không có account_id để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }else if(profileTask==null){
                resp.put("status", false);
                data.put("message", "profile_id không tồn tại");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //Check profile isLogin Google
            if(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(device_id.trim()+"_"+profile_id.trim(),"youtube")==null){
                Account account_get=null;
                if(mySQLCheck.getValue()<settingSystem.getMax_mysql()){
                    String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                    String code="";
                    for(int i=0;i<50;i++){
                        Integer ranver=ran.nextInt(stringrand.length());
                        code=code+stringrand.charAt(ranver);
                    }
                    account_get= accountRepository.get_Account_Youtube_By_ProfileId(device_id.trim()+"_"+profile_id.trim(),device_id,System.currentTimeMillis(),code,device.getMode().trim());
                }
                if(account_get!=null){
                    AccountProfile accountProfile=new AccountProfile();
                    accountProfile.setAccount_id(account_get.getAccount_id()+"|"+"youtube");
                    accountProfile.setPassword(account_get.getPassword());
                    accountProfile.setRecover(account_get.getRecover_mail());
                    accountProfile.setPlatform("youtube");
                    accountProfile.setLive(1);
                    accountProfile.setChanged(0);
                    accountProfile.setAuth_2fa(account_get.getAuth_2fa());
                    accountProfile.setProfileTask(profileTask);
                    accountProfile.setAdd_time(System.currentTimeMillis());
                    accountProfile.setUpdate_time(0L);
                    accountProfileRepository.save(accountProfile);
                }
            }
            //--------------------end_get_Account----------------------//
            profileTask = profileTaskRepository.check_ProfileId_Running(device_id.trim()+"_"+profile_id);//Check số lần get nhiệm vụ của 1 account
            if(profileTask!=null){
                if(profileTask.getTask_index()>=platformRepository.get_Priority_By_Platform(profileTask.getPlatform())){
                    if(profileTask.getTask_list().trim().length()==0){
                        profileTaskRepository.reset_Thread_Index_By_ProfileId(profileTask.getProfile_id());
                        profileTask=null;
                    }else{
                        String task_List="";
                        if(platform.length()==0){
                            task_List=profileTask.getTask_list();
                        }else{
                            task_List=platform;
                        }
                        Integer index_Off=task_List.indexOf(',')<0?task_List.length():task_List.indexOf(',');
                        String platform_index=task_List.indexOf(',')<0?task_List:task_List.substring(0,index_Off);
                        if(accountProfileRepository.get_AccountId_Live_By_AccountId_And_Platform(profileTask.getProfile_id(),platform_index)==null){
                            JSONArray domains= MailApi.getDomains();
                            String stringrand="abcdefhijkprstuvwx0123456789";
                            String mail="";
                            Boolean success=false;
                            while (!success){
                                for(int i=0;i<20;i++){
                                    Integer ranver=ran.nextInt(stringrand.length());
                                    mail=mail+stringrand.charAt(ranver);
                                }
                                mail=mail+"@"+domains.get(ran.nextInt(domains.size()));
                                success=MailApi.createMail(mail);
                            }
                            if(success){
                                String password="Cmc#";
                                String passrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                                for(int i=0;i<15;i++){
                                    Integer ranver=ran.nextInt(passrand.length());
                                    password=password+passrand.charAt(ranver);
                                }
                                AccountProfile accountProfile=new AccountProfile();
                                accountProfile.setAccount_id(mail+"|"+platform_index);
                                accountProfile.setPassword(password);
                                accountProfile.setRecover(mail);
                                accountProfile.setPlatform(platform_index);
                                accountProfile.setLive(1);
                                accountProfile.setChanged(0);
                                accountProfile.setAuth_2fa("");
                                accountProfile.setProfileTask(profileTask);
                                accountProfile.setAdd_time(System.currentTimeMillis());
                                accountProfile.setUpdate_time(0L);
                                accountProfileRepository.save(accountProfile);
                                try{
                                    Account account=new Account();
                                    account.setAccount_id(mail);
                                    account.setPassword(password);
                                    account.setRecover_mail(mail);
                                    account.setPlatform(platform_index);
                                    account.setLive(1);
                                    account.setRunning(1);
                                    account.setAuth_2fa("");
                                    account.setProfile_id(profileTask.getProfile_id());
                                    account.setDevice_id(profileTask.getDevice().getDevice_id());
                                    account.setAdd_time(System.currentTimeMillis());
                                    accountRepository.save(account);
                                }catch (Exception e){

                                }

                            }
                        }
                        profileTask.setTask_list(task_List.indexOf(',')<0?"":task_List.substring(index_Off+1));
                        profileTask.setTask_index(0);
                        profileTask.setPlatform(task_List.indexOf(',')<0?task_List:task_List.substring(0,index_Off));
                        profileTask.setAccount_id(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),platform_index));
                    }
                }
            }else{
                profileTask = profileTaskRepository.check_ProfileId(device_id.trim()+"_"+profile_id);
                profileTask.setUpdate_time(System.currentTimeMillis());
                String task_List="";
                if(platform.length()==0){
                    List<String> string_Task_List=platformRepository.get_All_Platform_True_By_ProfileId(device_id.trim()+"_"+profile_id);
                    task_List=String.join(",", string_Task_List);
                }else{
                    task_List=platform;
                }
                Integer index_Off=task_List.indexOf(',')<0?task_List.length():task_List.indexOf(',');
                String platform_index=task_List.indexOf(',')<0?task_List:task_List.substring(0,index_Off);
                if(accountProfileRepository.get_AccountId_Live_By_AccountId_And_Platform(profileTask.getProfile_id(),platform_index)==null){
                    JSONArray domains= MailApi.getDomains();
                    String stringrand="abcdefhijkprstuvwx0123456789";
                    String mail="";
                    Boolean success=false;
                    while (!success){
                        for(int i=0;i<20;i++){
                            Integer ranver=ran.nextInt(stringrand.length());
                            mail=mail+stringrand.charAt(ranver);
                        }
                        mail=mail+"@"+domains.get(ran.nextInt(domains.size()));
                        success=MailApi.createMail(mail);
                    }
                    if(success){
                        String password="Cmc#";
                        String passrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                        for(int i=0;i<15;i++){
                            Integer ranver=ran.nextInt(passrand.length());
                            password=password+passrand.charAt(ranver);
                        }
                        AccountProfile accountProfile=new AccountProfile();
                        accountProfile.setAccount_id(mail+"|"+platform_index);
                        accountProfile.setPassword(password);
                        accountProfile.setRecover(mail);
                        accountProfile.setPlatform(platform_index);
                        accountProfile.setLive(1);
                        accountProfile.setChanged(0);
                        accountProfile.setAuth_2fa("");
                        accountProfile.setProfileTask(profileTask);
                        accountProfile.setAdd_time(System.currentTimeMillis());
                        accountProfile.setUpdate_time(0L);
                        accountProfileRepository.save(accountProfile);

                        try{
                            Account account=new Account();
                            account.setAccount_id(mail);
                            account.setPassword(password);
                            account.setRecover_mail(mail);
                            account.setPlatform(platform_index);
                            account.setLive(1);
                            account.setRunning(1);
                            account.setAuth_2fa("");
                            account.setProfile_id(profileTask.getProfile_id());
                            account.setDevice_id(profileTask.getDevice().getDevice_id());
                            account.setAdd_time(System.currentTimeMillis());
                            accountRepository.save(account);
                        }catch (Exception e){

                        }
                    }
                }
                profileTask.setTask_list(task_List.indexOf(',')<0?"":task_List.substring(index_Off+1));
                profileTask.setPlatform(task_List.indexOf(',')<0?task_List:task_List.substring(0,index_Off));
                profileTask.setAccount_id(accountProfileRepository.get_AccountId_By_AccountId_And_Platform(profileTask.getProfile_id(),platform_index));

            }
            if(profileTask==null){
                profileTaskRepository.reset_Thread_Index_By_ProfileId(device_id.trim()+"_"+profile_id.trim());
                profileTask = profileTaskRepository.get_Profile_Get_Task(device_id.trim());
                if(device.getNum_profile()>1){
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "profile_changer");
                    data.put("profile_id", Integer.parseInt(profileTask.getProfile_id().split(device_id.trim()+"_")[1]));
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else if(device.getNum_profile()==1){
                    profileTask.setUpdate_time(System.currentTimeMillis());
                    profileTaskRepository.save(profileTask);
                    profileTask = profileTaskRepository.get_ProfileId_Can_Running_By_DeviceId(device_id.trim());
                }else{
                    resp.put("status", false);
                    data.put("message", "Không có account để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
            profileTask.setTask_index(profileTask.getTask_index()+1);
            List<TaskPriority> priorityTasks =taskPriorityRepository.get_Priority_Task_By_Platform(profileTask.getPlatform());
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
                while(arrTask.remove(task)) {}
                if(profileTask.getPlatform().equals("tiktok")){
                    if(task.equals("tiktok_follower")){
                        get_task= tiktokTask.tiktok_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_like")){
                        get_task=tiktokTask.tiktok_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_view")){
                        get_task=tiktokTask.tiktok_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("tiktok_comment")){
                        get_task=tiktokTask.tiktok_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("youtube")){
                    if(task.equals("youtube_view")){
                        get_task=youtubeTask.youtube_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("youtube_like")){
                        get_task=youtubeTask.youtube_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("youtube_subscriber")){
                        get_task=youtubeTask.youtube_subscriber(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("facebook")){
                    if(task.equals("facebook_follower")){
                        get_task=facebookTask.facebook_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_like")){
                        get_task=facebookTask.facebook_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_view")){
                        get_task=facebookTask.facebook_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_comment")){
                        get_task=facebookTask.facebook_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("facebook_member")){
                        get_task=facebookTask.facebook_member(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("x")){
                    if(task.equals("x_follower")){
                        get_task=xTask.x_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_like")){
                        get_task=xTask.x_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_view")){
                        get_task=xTask.x_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_comment")){
                        get_task=xTask.x_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("x_repost")){
                        get_task=xTask.x_repost(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("instagram")){
                    if(task.equals("instagram_follower")){
                        get_task=instagramTask.instagram_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_like")){
                        get_task=instagramTask.instagram_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_view")){
                        get_task=instagramTask.instagram_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("instagram_comment")){
                        get_task=instagramTask.instagram_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }
                } else if(profileTask.getPlatform().equals("threads")){
                    if(task.equals("threads_follower")){
                        get_task=threadsTask.threads_follower(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_like")){
                        get_task=threadsTask.threads_like(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_view")){
                        get_task=threadsTask.threads_view(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_comment")){
                        get_task=threadsTask.threads_comment(profileTask.getAccount_id(),device.getMode().trim());
                    }else if(task.equals("threads_repost")){
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
                dataJson.put("task_index",profileTask.getTask_index());
                //System.out.println(dataJson);
                respJson.put("status",true);
                respJson.put("data",dataJson);
                //--------------------------------------------//
                profileTask.setGet_time(System.currentTimeMillis());
                profileTask.setOrder_id(Long.parseLong(dataJson.get("order_id").toString()));
                profileTask.setRunning(1);
                profileTask.setTask(dataJson.get("task").toString());
                profileTask.setPlatform(dataJson.get("platform").toString());
                profileTask.setTask_key(dataJson.get("task_key").toString());
                profileTaskRepository.save(profileTask);
                //--------------------------------------------//
                dataJson.remove("order_id");
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


    @PostMapping(value = "/updateTaskOFFNEW", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> updateTaskOFFNEW(@RequestHeader(defaultValue = "") String Authorization,
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
                data.put("message", "username không để trống");
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
            profileTaskRepository.reset_Thread_By_AccountId(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim());
            String platform_Check = updateTaskRequest.getPlatform().toLowerCase().trim();
            if(platform_Check.equals("youtube")){
                if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true){
                    youtubeUpdate.youtube_view(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("subscriber")&&updateTaskRequest.getStatus()==true){
                    youtubeUpdate.youtube_subscriber(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                } else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                    youtubeUpdate.youtube_like(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }
            }else if(platform_Check.equals("tiktok")){
                if(updateTaskRequest.getTask().toLowerCase().trim().equals("follower")&&updateTaskRequest.getStatus()==true){
                    tiktokUpdate.tiktok_follower(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim(),updateTaskRequest.getSuccess());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                    tiktokUpdate.tiktok_like(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("comment")){
                    tiktokUpdate.tiktok_comment(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim(),updateTaskRequest.getStatus());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true){
                    tiktokUpdate.tiktok_view(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }
            }else if(platform_Check.equals("facebook")){
                if(updateTaskRequest.getTask().toLowerCase().trim().equals("follower")&&updateTaskRequest.getStatus()==true){
                    facebookUpdate.facebook_follower(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                    facebookUpdate.facebook_like(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("comment")) {
                    facebookUpdate.facebook_comment(updateTaskRequest.getAccount_id().trim(), updateTaskRequest.getTask_key().trim(), updateTaskRequest.getStatus());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("member")&&updateTaskRequest.getStatus()==true) {
                    facebookUpdate.facebook_member(updateTaskRequest.getAccount_id().trim(), updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true) {
                    facebookUpdate.facebook_view(updateTaskRequest.getAccount_id().trim(), updateTaskRequest.getTask_key().trim());
                }
            }else if(platform_Check.equals("x")){
                if(updateTaskRequest.getTask().toLowerCase().trim().equals("follower")&&updateTaskRequest.getStatus()==true){
                    xUpdate.x_follower(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                    xUpdate.x_like(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("comment")) {
                    xUpdate.x_comment(updateTaskRequest.getAccount_id().trim(), updateTaskRequest.getTask_key().trim(), updateTaskRequest.getStatus());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true) {
                    xUpdate.x_view(updateTaskRequest.getAccount_id().trim(), updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("repost")&&updateTaskRequest.getStatus()==true) {
                    xUpdate.x_repost(updateTaskRequest.getAccount_id().trim(), updateTaskRequest.getTask_key().trim());
                }
            }else if(platform_Check.equals("instagram")){
                if(updateTaskRequest.getTask().toLowerCase().trim().equals("follower")&&updateTaskRequest.getStatus()==true){
                    instagramUpdate.instagram_follower(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                    instagramUpdate.instagram_like(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("comment")) {
                    instagramUpdate.instagram_comment(updateTaskRequest.getAccount_id().trim(), updateTaskRequest.getTask_key().trim(), updateTaskRequest.getStatus());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true) {
                    instagramUpdate.instagram_view(updateTaskRequest.getAccount_id().trim(), updateTaskRequest.getTask_key().trim());
                }
            }else if(platform_Check.equals("threads")){
                if(updateTaskRequest.getTask().toLowerCase().trim().equals("follower")&&updateTaskRequest.getStatus()==true){
                    threadsUpdate.threads_follower(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                    threadsUpdate.threads_like(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("comment")) {
                    threadsUpdate.threads_comment(updateTaskRequest.getAccount_id().trim(), updateTaskRequest.getTask_key().trim(), updateTaskRequest.getStatus());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true) {
                    threadsUpdate.threads_view(updateTaskRequest.getAccount_id().trim(), updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("repost")&&updateTaskRequest.getStatus()==true) {
                    threadsUpdate.threads_repost(updateTaskRequest.getAccount_id().trim(), updateTaskRequest.getTask_key().trim());
                }
            }
            if(updateTaskRequest.getStatus()==true){
                try {
                    OrderRunning orderRunning=null;
                    if(platform_Check.equals("youtube")&&updateTaskRequest.getTask().toLowerCase().trim().equals("subscriber")){
                        String order_Key= dataSubscriberRepository.get_ChannelId_By_VideoId(updateTaskRequest.getTask_key().trim());
                        orderRunning=orderRunningRepository.find_Order_By_Order_Key(order_Key,updateTaskRequest.getTask().trim(),updateTaskRequest.getPlatform().trim());
                    }else{
                        orderRunning=orderRunningRepository.find_Order_By_Order_Key(updateTaskRequest.getTask_key().trim(),updateTaskRequest.getTask().trim(),updateTaskRequest.getPlatform().trim());
                    }
                    if(orderRunning!=null){
                        HistorySum historySum=new HistorySum();
                        historySum.setOrderRunning(orderRunning);
                        historySum.setAccount_id(updateTaskRequest.getAccount_id().trim());
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
                }
            }
            try{
                if(updateTaskRequest.getIsLogin()==0){
                    profileTaskRepository.update_Than_Task_Index_By_AccountId(updateTaskRequest.getPlatform().trim(),updateTaskRequest.getAccount_id()+"%");
                    AccountProfile accountProfile=accountProfileRepository.get_Account_By_Account_id_And_Platform(updateTaskRequest.getAccount_id().trim()+"%",updateTaskRequest.getPlatform().trim());
                    accountProfile.setLive(0);
                    accountProfile.setUpdate_time(System.currentTimeMillis());
                    accountProfileRepository.save(accountProfile);
                }else if(updateTaskRequest.getIsLogin()==1){
                    AccountProfile accountProfile=accountProfileRepository.get_Account_By_Account_id_And_Platform(updateTaskRequest.getAccount_id().trim()+"%",updateTaskRequest.getPlatform().trim());
                    accountProfile.setLive(1);
                    accountProfile.setUpdate_time(System.currentTimeMillis());
                    accountProfileRepository.save(accountProfile);

                    Account account =accountRepository.get_Account_By_Account_id(updateTaskRequest.getAccount_id().trim());
                    if(!account.getPlatform().equals(updateTaskRequest.getPlatform())){
                        if(!account.getDependent().contains(updateTaskRequest.getPlatform())){
                            if(account.getDependent().length()==0){
                                account.setDependent(updateTaskRequest.getPlatform());
                            }else{
                                account.setDependent(account.getDependent()+","+updateTaskRequest.getPlatform());
                            }
                            if(account.getPassword_dependent().length()==0){
                                account.setPassword_dependent(updateTaskRequest.getPlatform()+"|"+accountProfile.getPassword());
                            }else{
                                account.setPassword_dependent(account.getPassword_dependent()+","+updateTaskRequest.getPlatform()+"|"+accountProfile.getPassword());
                            }
                            List<String> arrDie=new ArrayList<>(Arrays.asList(account.getDie_dependent().split(",")));
                            arrDie.removeIf(platform -> platform.contains(updateTaskRequest.getPlatform()));
                            account.setDie_dependent(String.join(",", arrDie));

                            accountRepository.save(account);
                        }
                    }else{
                        account.setLive(updateTaskRequest.getIsLogin());
                        accountRepository.save(account);
                    }

                }else if(updateTaskRequest.getIsLogin()==-1){
                    profileTaskRepository.update_Than_Task_Index_By_AccountId(updateTaskRequest.getPlatform().trim(),updateTaskRequest.getAccount_id()+"%");
                    AccountProfile accountProfile=accountProfileRepository.get_Account_By_Account_id_And_Platform(updateTaskRequest.getAccount_id().trim()+"%",updateTaskRequest.getPlatform().trim());
                    accountProfile.setLive(-1);
                    accountProfile.setUpdate_time(System.currentTimeMillis());
                    accountProfileRepository.save(accountProfile);
                }else if(updateTaskRequest.getIsLogin()>1){
                    Account account =accountRepository.get_Account_By_Account_id(updateTaskRequest.getAccount_id().trim());
                    AccountProfile accountProfile=accountProfileRepository.get_Account_By_Account_id_And_Platform(updateTaskRequest.getAccount_id().trim()+"%",updateTaskRequest.getPlatform().trim());
                    if(!account.getPlatform().equals(updateTaskRequest.getPlatform())){
                        if(account.getDependent().contains(updateTaskRequest.getPlatform())){
                            List<String> arrPlatform =new ArrayList<>(Arrays.asList(account.getDependent().split(",")));
                            arrPlatform.remove(updateTaskRequest.getPlatform());
                            account.setDependent(String.join(",", arrPlatform));

                            /*
                            List<String> arrPassword =new ArrayList<>(Arrays.asList(account.getPassword_dependent().split(",")));
                            arrPassword.removeIf(platform -> platform.contains(updateTaskRequest.getPlatform()));
                            account.setPassword_dependent(String.join(",", arrPassword));
                             */
                        }
                        if(!account.getDie_dependent().contains(updateTaskRequest.getPlatform())){
                            if(account.getDie_dependent().length()==0){
                                account.setDie_dependent(updateTaskRequest.getPlatform());
                            }else{
                                account.setDie_dependent(account.getDie_dependent()+","+updateTaskRequest.getPlatform());
                            }
                        }
                        if(!account.getPassword_dependent().contains(updateTaskRequest.getPlatform()) && accountProfile!=null){
                            if(account.getPassword_dependent().length()==0){
                                account.setPassword_dependent(updateTaskRequest.getPlatform()+"|"+accountProfile.getPassword());
                            }else{
                                account.setPassword_dependent(account.getPassword_dependent()+","+updateTaskRequest.getPlatform()+"|"+accountProfile.getPassword());
                            }
                        }
                        accountRepository.save(account);
                    }else{
                        account.setRunning(0);
                        account.setLive(updateTaskRequest.getIsLogin());
                        accountRepository.save(account);
                    }
                    profileTaskRepository.update_Than_Task_Index_By_AccountId(updateTaskRequest.getPlatform().trim(),updateTaskRequest.getAccount_id()+"%");
                    if(accountProfile!=null){
                        accountProfileRepository.delete(accountProfile);
                    }
                }
            }catch (Exception e){

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
            profileTaskRepository.reset_Thread_By_AccountId(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim());
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
                        OrderRunning orderRunning=null;
                        if(platform_Check.equals("youtube")&&updateTaskRequest.getTask().toLowerCase().trim().equals("subscriber")){
                            String order_Key= dataSubscriberRepository.get_ChannelId_By_VideoId(updateTaskRequest.getTask_key().trim());
                            orderRunning=orderRunningRepository.find_Order_By_Order_Key(order_Key,updateTaskRequest.getTask().trim(),updateTaskRequest.getPlatform().trim());
                        }else{
                            orderRunning=orderRunningRepository.find_Order_By_Order_Key(updateTaskRequest.getTask_key().trim(),updateTaskRequest.getTask().trim(),updateTaskRequest.getPlatform().trim());
                        }
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
                if(updateTaskRequest.getIsLogin()==0 || updateTaskRequest.getIsLogin()==-1){
                    profileTaskRepository.update_Than_Task_Index_By_AccountId(updateTaskRequest.getPlatform().trim(),updateTaskRequest.getAccount_id()+"|"+updateTaskRequest.getPlatform().trim());
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
                            accountProfile.setLive(0);
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
                        String platform_Dependent=platformRepository.get_Dependent_Connection_By_Platform(updateTaskRequest.getPlatform().trim());
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

                    }////

                }else if(updateTaskRequest.getIsLogin()>1){

                    AccountProfile accountProfile=accountProfileRepository.get_Account_By_Account_id_And_Platform(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim(),updateTaskRequest.getPlatform().trim());

                    String platform_Dependent=platformRepository.get_Dependent_Connection_By_Platform(updateTaskRequest.getPlatform().trim());
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
                data.put("message", "username không để trống");
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
            profileTaskRepository.reset_Thread_By_AccountId(updateTaskRequest.getAccount_id().trim()+"|"+updateTaskRequest.getPlatform().trim());
            String platform_Check = updateTaskRequest.getPlatform().toLowerCase().trim();
            if(platform_Check.equals("youtube")){
                if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true){
                    youtubeUpdate.youtube_view(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("subscriber")&&updateTaskRequest.getStatus()==true){
                    youtubeUpdate.youtube_subscriber(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                } else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                    youtubeUpdate.youtube_like(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }
            }else if(platform_Check.equals("tiktok")){
                if(updateTaskRequest.getTask().toLowerCase().trim().equals("follower")&&updateTaskRequest.getStatus()==true){
                    tiktokUpdate.tiktok_follower(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim(),updateTaskRequest.getSuccess());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                    tiktokUpdate.tiktok_like(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("comment")){
                    tiktokUpdate.tiktok_comment(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim(),updateTaskRequest.getStatus());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true){
                    tiktokUpdate.tiktok_view(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }
            }else if(platform_Check.equals("facebook")){
                if(updateTaskRequest.getTask().toLowerCase().trim().equals("follower")&&updateTaskRequest.getStatus()==true){
                    facebookUpdate.facebook_follower(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                    facebookUpdate.facebook_like(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("comment")) {
                    facebookUpdate.facebook_comment(updateTaskRequest.getAccount_id().trim(), updateTaskRequest.getTask_key().trim(), updateTaskRequest.getStatus());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("member")&&updateTaskRequest.getStatus()==true) {
                    facebookUpdate.facebook_member(updateTaskRequest.getAccount_id().trim(), updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true) {
                    facebookUpdate.facebook_view(updateTaskRequest.getAccount_id().trim(), updateTaskRequest.getTask_key().trim());
                }
            }else if(platform_Check.equals("x")){
                if(updateTaskRequest.getTask().toLowerCase().trim().equals("follower")&&updateTaskRequest.getStatus()==true){
                    xUpdate.x_follower(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                    xUpdate.x_like(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("comment")) {
                    xUpdate.x_comment(updateTaskRequest.getAccount_id().trim(), updateTaskRequest.getTask_key().trim(), updateTaskRequest.getStatus());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true) {
                    xUpdate.x_view(updateTaskRequest.getAccount_id().trim(), updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("repost")&&updateTaskRequest.getStatus()==true) {
                    xUpdate.x_repost(updateTaskRequest.getAccount_id().trim(), updateTaskRequest.getTask_key().trim());
                }
            }else if(platform_Check.equals("instagram")){
                if(updateTaskRequest.getTask().toLowerCase().trim().equals("follower")&&updateTaskRequest.getStatus()==true){
                    instagramUpdate.instagram_follower(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                    instagramUpdate.instagram_like(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("comment")) {
                    instagramUpdate.instagram_comment(updateTaskRequest.getAccount_id().trim(), updateTaskRequest.getTask_key().trim(), updateTaskRequest.getStatus());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true) {
                    instagramUpdate.instagram_view(updateTaskRequest.getAccount_id().trim(), updateTaskRequest.getTask_key().trim());
                }
            }else if(platform_Check.equals("threads")){
                if(updateTaskRequest.getTask().toLowerCase().trim().equals("follower")&&updateTaskRequest.getStatus()==true){
                    threadsUpdate.threads_follower(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("like")&&updateTaskRequest.getStatus()==true){
                    threadsUpdate.threads_like(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("comment")) {
                    threadsUpdate.threads_comment(updateTaskRequest.getAccount_id().trim(), updateTaskRequest.getTask_key().trim(), updateTaskRequest.getStatus());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("view")&&updateTaskRequest.getStatus()==true) {
                    threadsUpdate.threads_view(updateTaskRequest.getAccount_id().trim(), updateTaskRequest.getTask_key().trim());
                }else  if(updateTaskRequest.getTask().toLowerCase().trim().equals("repost")&&updateTaskRequest.getStatus()==true) {
                    threadsUpdate.threads_repost(updateTaskRequest.getAccount_id().trim(), updateTaskRequest.getTask_key().trim());
                }
            }
            if(updateTaskRequest.getStatus()==true){
                try {
                    OrderRunning orderRunning=null;
                    if(platform_Check.equals("youtube")&&updateTaskRequest.getTask().toLowerCase().trim().equals("subscriber")){
                        String order_Key= dataSubscriberRepository.get_ChannelId_By_VideoId(updateTaskRequest.getTask_key().trim());
                        orderRunning=orderRunningRepository.find_Order_By_Order_Key(order_Key,updateTaskRequest.getTask().trim(),updateTaskRequest.getPlatform().trim());
                    }else{
                        orderRunning=orderRunningRepository.find_Order_By_Order_Key(updateTaskRequest.getTask_key().trim(),updateTaskRequest.getTask().trim(),updateTaskRequest.getPlatform().trim());
                    }
                    if(orderRunning!=null){
                        HistorySum historySum=new HistorySum();
                        historySum.setOrderRunning(orderRunning);
                        historySum.setAccount_id(updateTaskRequest.getAccount_id().trim());
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
                }
            }
            try{
                if(updateTaskRequest.getIsLogin()==0){
                    profileTaskRepository.update_Than_Task_Index_By_AccountId(updateTaskRequest.getPlatform().trim(),updateTaskRequest.getAccount_id()+"%");
                    AccountProfile accountProfile=accountProfileRepository.get_Account_By_Account_id_And_Platform(updateTaskRequest.getAccount_id().trim()+"%",updateTaskRequest.getPlatform().trim());
                    accountProfile.setLive(0);
                    accountProfile.setUpdate_time(System.currentTimeMillis());
                    accountProfileRepository.save(accountProfile);
                }else if(updateTaskRequest.getIsLogin()==1){
                    AccountProfile accountProfile=accountProfileRepository.get_Account_By_Account_id_And_Platform(updateTaskRequest.getAccount_id().trim()+"%",updateTaskRequest.getPlatform().trim());
                    accountProfile.setLive(1);
                    accountProfile.setUpdate_time(System.currentTimeMillis());
                    accountProfileRepository.save(accountProfile);
                }else if(updateTaskRequest.getIsLogin()==-1){
                    profileTaskRepository.update_Than_Task_Index_By_AccountId(updateTaskRequest.getPlatform().trim(),updateTaskRequest.getAccount_id()+"%");
                    AccountProfile accountProfile=accountProfileRepository.get_Account_By_Account_id_And_Platform(updateTaskRequest.getAccount_id().trim()+"%",updateTaskRequest.getPlatform().trim());
                    accountProfile.setLive(-1);
                    accountProfile.setUpdate_time(System.currentTimeMillis());
                    accountProfileRepository.save(accountProfile);
                }else if(updateTaskRequest.getIsLogin()>1){
                    profileTaskRepository.update_Than_Task_Index_By_AccountId(updateTaskRequest.getPlatform().trim(),updateTaskRequest.getAccount_id()+"%");
                    AccountProfile accountProfile=accountProfileRepository.get_Account_By_Account_id_And_Platform(updateTaskRequest.getAccount_id().trim()+"%",updateTaskRequest.getPlatform().trim());
                    accountProfileRepository.delete(accountProfile);
                    Account account=accountRepository.get_Account_By_Account_id(updateTaskRequest.getAccount_id().trim());
                    account.setRunning(0);
                    account.setLive(updateTaskRequest.getIsLogin());
                    accountRepository.save(account);
                }
            }catch (Exception e){

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

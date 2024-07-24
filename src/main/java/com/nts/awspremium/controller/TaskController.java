package com.nts.awspremium.controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private OrderThreadCheck orderThreadCheck;
    @Autowired
    private MySQLCheck mySQLCheck;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private YoutubeViewHistoryRepository youtubeVideoHistoryRepository;
    @Autowired
    private YoutubeSubscriberHistoryRepository youtubeChannelHistoryRepository;

    @Autowired
    private YoutubeLikeHistoryRepository youtubeLikeHistoryRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProfileTaskRepository profileTaskRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DataSubscriberRepository dataSubscriberRepository;
    @Autowired
    private AccountTaskRepository accountTaskRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountProfileRepository accountProfileRepository;

    @Autowired
    private PlatformRepository platformRepository;

    @Autowired
    private SettingYoutubeRepository settingYoutubeRepository;
    @Autowired
    private SettingSystemRepository settingSystemRepository;
    @Autowired
    private YoutubeLike24hRepository youtubeLike24hRepository;
    @Autowired
    private YoutubeSubscriber24hRepository youtubeSubscribe24hRepository;

    @Autowired
    private TikTokFollower24hRepository tikTokFollower24hRepository;

    @Autowired
    private FacebookFollower24hRepository facebookFollower24hRepository;

    @Autowired
    private TikTokLike24hRepository tikTokLike24hRepository;

    @Autowired
    private TikTokAccountHistoryRepository tikTokAccountHistoryRepository;

    @Autowired
    private FacebookFollowerHistoryRepository facebookFollowerHistoryRepository;

    @Autowired
    private TikTokLikeHistoryRepository tikTokLikeHistoryRepository;

    @Autowired
    private TikTokCommentHistoryRepository tikTokCommentHistoryRepository;

    @Autowired
    private HistorySumRepository historySumRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    @Autowired
    private DataCommentRepository dataCommentRepository;

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
    @GetMapping(value = "getTask003", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> getTask003(@RequestHeader(defaultValue = "") String Authorization,
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
                        System.out.println(arrPlatform);
                        while (arrPlatform.size()>0){
                            System.out.println("index 1 "+arrPlatform.get(0));
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
                                    System.out.println("Request: #####"+profileTask.getRequest_index());
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
                                    System.out.println("Request: #####"+profileTask.getRequest_index());
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
                    if(profileTask.getTask_list().length()>0){
                        task_List=profileTask.getTask_list();
                    }else{
                        List<String> string_Task_List=platformRepository.get_All_Platform_True();
                        task_List=String.join(",", string_Task_List);
                    }

                }else{
                    task_List=platform;
                }
                List<String> arrPlatform =new ArrayList<>(Arrays.asList(task_List.split(",")));
                System.out.println(arrPlatform);
                while (arrPlatform.size()>0){
                    System.out.println("index 2 "+arrPlatform.get(0));
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
                            System.out.println("Request: #####"+profileTask.getRequest_index());
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
                            System.out.println("Request: #####"+profileTask.getRequest_index());
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
                        get_task= tiktokTask.tiktok_follower(profileTask.getAccount_id());
                    }else if(task.equals("tiktok_like")){
                        get_task=tiktokTask.tiktok_like(profileTask.getAccount_id());
                    }else if(task.equals("tiktok_view")){
                        get_task=tiktokTask.tiktok_view(profileTask.getAccount_id());
                    }else if(task.equals("tiktok_comment")){
                        get_task=tiktokTask.tiktok_comment(profileTask.getAccount_id());
                    }
                } else if(profileTask.getPlatform().equals("youtube")){
                    if(task.equals("youtube_view")){
                        get_task=youtubeTask.youtube_view(profileTask.getAccount_id());
                    }else if(task.equals("youtube_like")){
                        get_task=youtubeTask.youtube_like(profileTask.getAccount_id());
                    }else if(task.equals("youtube_subscriber")){
                        get_task=youtubeTask.youtube_subscriber(profileTask.getAccount_id());
                    }
                } else if(profileTask.getPlatform().equals("facebook")){
                    if(task.equals("facebook_follower")){
                        get_task=facebookTask.facebook_follower(profileTask.getAccount_id());
                    }else if(task.equals("facebook_like")){
                        get_task=facebookTask.facebook_like(profileTask.getAccount_id());
                    }else if(task.equals("facebook_view")){
                        get_task=facebookTask.facebook_view(profileTask.getAccount_id());
                    }else if(task.equals("facebook_comment")){
                        get_task=facebookTask.facebook_comment(profileTask.getAccount_id());
                    }else if(task.equals("facebook_member")){
                        get_task=facebookTask.facebook_member(profileTask.getAccount_id());
                    }
                } else if(profileTask.getPlatform().equals("x")){
                    if(task.equals("x_follower")){
                        get_task=xTask.x_follower(profileTask.getAccount_id());
                    }else if(task.equals("x_like")){
                        get_task=xTask.x_like(profileTask.getAccount_id());
                    }else if(task.equals("x_view")){
                        get_task=xTask.x_view(profileTask.getAccount_id());
                    }else if(task.equals("x_comment")){
                        get_task=xTask.x_comment(profileTask.getAccount_id());
                    }else if(task.equals("x_repost")){
                        get_task=xTask.x_repost(profileTask.getAccount_id());
                    }
                } else if(profileTask.getPlatform().equals("instagram")){
                    if(task.equals("instagram_follower")){
                        get_task=instagramTask.instagram_follower(profileTask.getAccount_id());
                    }else if(task.equals("instagram_like")){
                        get_task=instagramTask.instagram_like(profileTask.getAccount_id());
                    }else if(task.equals("instagram_view")){
                        get_task=instagramTask.instagram_view(profileTask.getAccount_id());
                    }else if(task.equals("instagram_comment")){
                        get_task=instagramTask.instagram_comment(profileTask.getAccount_id());
                    }
                } else if(profileTask.getPlatform().equals("threads")){
                    if(task.equals("threads_follower")){
                        get_task=threadsTask.threads_follower(profileTask.getAccount_id());
                    }else if(task.equals("threads_like")){
                        get_task=threadsTask.threads_like(profileTask.getAccount_id());
                    }else if(task.equals("threads_view")){
                        get_task=threadsTask.threads_view(profileTask.getAccount_id());
                    }else if(task.equals("threads_comment")){
                        get_task=threadsTask.threads_comment(profileTask.getAccount_id());
                    }else if(task.equals("threads_repost")){
                        get_task=threadsTask.threads_repost(profileTask.getAccount_id());
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
    @GetMapping(value = "getTask002", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> getTask002(@RequestHeader(defaultValue = "") String Authorization,
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
                    account_get= accountRepository.get_Account_Youtube_By_ProfileId(device_id.trim()+"_"+profile_id.trim(),device_id,System.currentTimeMillis(),code);
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
                        get_task= tiktokTask.tiktok_follower(profileTask.getAccount_id());
                    }else if(task.equals("tiktok_like")){
                        get_task=tiktokTask.tiktok_like(profileTask.getAccount_id());
                    }else if(task.equals("tiktok_view")){
                        get_task=tiktokTask.tiktok_view(profileTask.getAccount_id());
                    }else if(task.equals("tiktok_comment")){
                        get_task=tiktokTask.tiktok_comment(profileTask.getAccount_id());
                    }
                } else if(profileTask.getPlatform().equals("youtube")){
                    if(task.equals("youtube_view")){
                        get_task=youtubeTask.youtube_view(profileTask.getAccount_id());
                    }else if(task.equals("youtube_like")){
                        get_task=youtubeTask.youtube_like(profileTask.getAccount_id());
                    }else if(task.equals("youtube_subscriber")){
                        get_task=youtubeTask.youtube_subscriber(profileTask.getAccount_id());
                    }
                } else if(profileTask.getPlatform().equals("facebook")){
                    if(task.equals("facebook_follower")){
                        get_task=facebookTask.facebook_follower(profileTask.getAccount_id());
                    }else if(task.equals("facebook_like")){
                        get_task=facebookTask.facebook_like(profileTask.getAccount_id());
                    }else if(task.equals("facebook_view")){
                        get_task=facebookTask.facebook_view(profileTask.getAccount_id());
                    }else if(task.equals("facebook_comment")){
                        get_task=facebookTask.facebook_comment(profileTask.getAccount_id());
                    }else if(task.equals("facebook_member")){
                        get_task=facebookTask.facebook_member(profileTask.getAccount_id());
                    }
                } else if(profileTask.getPlatform().equals("x")){
                    if(task.equals("x_follower")){
                        get_task=xTask.x_follower(profileTask.getAccount_id());
                    }else if(task.equals("x_like")){
                        get_task=xTask.x_like(profileTask.getAccount_id());
                    }else if(task.equals("x_view")){
                        get_task=xTask.x_view(profileTask.getAccount_id());
                    }else if(task.equals("x_comment")){
                        get_task=xTask.x_comment(profileTask.getAccount_id());
                    }else if(task.equals("x_repost")){
                        get_task=xTask.x_repost(profileTask.getAccount_id());
                    }
                } else if(profileTask.getPlatform().equals("instagram")){
                    if(task.equals("instagram_follower")){
                        get_task=instagramTask.instagram_follower(profileTask.getAccount_id());
                    }else if(task.equals("instagram_like")){
                        get_task=instagramTask.instagram_like(profileTask.getAccount_id());
                    }else if(task.equals("instagram_view")){
                        get_task=instagramTask.instagram_view(profileTask.getAccount_id());
                    }else if(task.equals("instagram_comment")){
                        get_task=instagramTask.instagram_comment(profileTask.getAccount_id());
                    }
                } else if(profileTask.getPlatform().equals("threads")){
                    if(task.equals("threads_follower")){
                        get_task=threadsTask.threads_follower(profileTask.getAccount_id());
                    }else if(task.equals("threads_like")){
                        get_task=threadsTask.threads_like(profileTask.getAccount_id());
                    }else if(task.equals("threads_view")){
                        get_task=threadsTask.threads_view(profileTask.getAccount_id());
                    }else if(task.equals("threads_comment")){
                        get_task=threadsTask.threads_comment(profileTask.getAccount_id());
                    }else if(task.equals("threads_repost")){
                        get_task=threadsTask.threads_repost(profileTask.getAccount_id());
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
                    account_get= accountRepository.get_Account_Youtube_By_ProfileId(device_id.trim()+"_"+profile_id.trim(),device_id,System.currentTimeMillis(),code);
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
                    System.out.println(string_Task_List);
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
                        get_task= tiktokTask.tiktok_follower(profileTask.getAccount_id());
                    }else if(task.equals("tiktok_like")){
                        get_task=tiktokTask.tiktok_like(profileTask.getAccount_id());
                    }else if(task.equals("tiktok_view")){
                        get_task=tiktokTask.tiktok_view(profileTask.getAccount_id());
                    }else if(task.equals("tiktok_comment")){
                        get_task=tiktokTask.tiktok_comment(profileTask.getAccount_id());
                    }
                } else if(profileTask.getPlatform().equals("youtube")){
                    if(task.equals("youtube_view")){
                        get_task=youtubeTask.youtube_view(profileTask.getAccount_id());
                    }else if(task.equals("youtube_like")){
                        get_task=youtubeTask.youtube_like(profileTask.getAccount_id());
                    }else if(task.equals("youtube_subscriber")){
                        get_task=youtubeTask.youtube_subscriber(profileTask.getAccount_id());
                    }
                } else if(profileTask.getPlatform().equals("facebook")){
                    if(task.equals("facebook_follower")){
                        get_task=facebookTask.facebook_follower(profileTask.getAccount_id());
                    }else if(task.equals("facebook_like")){
                        get_task=facebookTask.facebook_like(profileTask.getAccount_id());
                    }else if(task.equals("facebook_view")){
                        get_task=facebookTask.facebook_view(profileTask.getAccount_id());
                    }else if(task.equals("facebook_comment")){
                        get_task=facebookTask.facebook_comment(profileTask.getAccount_id());
                    }else if(task.equals("facebook_member")){
                        get_task=facebookTask.facebook_member(profileTask.getAccount_id());
                    }
                } else if(profileTask.getPlatform().equals("x")){
                    if(task.equals("x_follower")){
                        get_task=xTask.x_follower(profileTask.getAccount_id());
                    }else if(task.equals("x_like")){
                        get_task=xTask.x_like(profileTask.getAccount_id());
                    }else if(task.equals("x_view")){
                        get_task=xTask.x_view(profileTask.getAccount_id());
                    }else if(task.equals("x_comment")){
                        get_task=xTask.x_comment(profileTask.getAccount_id());
                    }else if(task.equals("x_repost")){
                        get_task=xTask.x_repost(profileTask.getAccount_id());
                    }
                } else if(profileTask.getPlatform().equals("instagram")){
                    if(task.equals("instagram_follower")){
                        get_task=instagramTask.instagram_follower(profileTask.getAccount_id());
                    }else if(task.equals("instagram_like")){
                        get_task=instagramTask.instagram_like(profileTask.getAccount_id());
                    }else if(task.equals("instagram_view")){
                        get_task=instagramTask.instagram_view(profileTask.getAccount_id());
                    }else if(task.equals("instagram_comment")){
                        get_task=instagramTask.instagram_comment(profileTask.getAccount_id());
                    }
                } else if(profileTask.getPlatform().equals("threads")){
                    if(task.equals("threads_follower")){
                        get_task=threadsTask.threads_follower(profileTask.getAccount_id());
                    }else if(task.equals("threads_like")){
                        get_task=threadsTask.threads_like(profileTask.getAccount_id());
                    }else if(task.equals("threads_view")){
                        get_task=threadsTask.threads_view(profileTask.getAccount_id());
                    }else if(task.equals("threads_comment")){
                        get_task=threadsTask.threads_comment(profileTask.getAccount_id());
                    }else if(task.equals("threads_repost")){
                        get_task=threadsTask.threads_repost(profileTask.getAccount_id());
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
            Profile profile=null;
            if(device!=null){
                device.setUpdate_time(System.currentTimeMillis());
                deviceRepository.save(device);

                if(device.getNum_profile()==0){
                    for(int i=0;i<settingSystem.getMax_profile();i++){
                        Profile profile_new=new Profile();
                        if(i==0){
                            profile_new.setProfile_id(device_id.trim()+"_"+i);
                        }else{
                            profile_new.setProfile_id(device_id.trim()+"_"+(i+9));
                        }
                        profile_new.setAdd_time(System.currentTimeMillis());
                        profile_new.setDevice(device);
                        profile_new.setNum_account(0);
                        profile_new.setUpdate_time(0L);
                        profile_new.setState(1);
                        profileRepository.save(profile_new);
                    }
                }

                profile =profileRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                if(profile==null){
                    resp.put("status", false);
                    data.put("message", "profile không tồn tại");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
                }
                if(profile.getNum_account()<settingSystem.getMax_acc()){
                    Account account_get=null;
                    if(mySQLCheck.getValue()<settingSystem.getMax_mysql()){
                        String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                        String code="";
                        for(int i=0;i<50;i++){
                            Integer ranver=ran.nextInt(stringrand.length());
                            code=code+stringrand.charAt(ranver);
                        }
                        account_get= accountRepository.get_Account_By_DeviceId(device_id,System.currentTimeMillis(),code);
                    }
                    if(account_get!=null){
                        AccountTask accountTask=new AccountTask();
                        accountTask.setAccount(account_get);
                        accountTask.setDevice(device);
                        accountTask.setAccount_level(0);
                        accountTask.setGet_time(0L);
                        accountTask.setOrder_id(0L);
                        accountTask.setRunning(0);
                        accountTask.setState(1);
                        accountTask.setProfile(profile);
                        accountTask.setTask_index(0);
                        accountTaskRepository.save(accountTask);

                        device.setNum_account(device.getNum_account()+1);
                        deviceRepository.save(device);
                        profile.setNum_account(profile.getNum_account()+1);
                        profileRepository.save(profile);

                    }
                }
            }else
            {
                Device device_new=new Device();
                device_new.setDevice_id(device_id.trim());
                device_new.setAdd_time(System.currentTimeMillis());
                device_new.setState(1);
                device_new.setUpdate_time(System.currentTimeMillis());
                device_new.setNum_account(0);
                device_new.setNum_profile(settingSystem.getMax_profile());
                deviceRepository.save(device_new);
                device=device_new;

                for(int i=0;i<settingSystem.getMax_profile();i++){
                    Profile profile_new=new Profile();
                    if(i==0){
                        profile_new.setProfile_id(device_id.trim()+"_"+i);
                    }else{
                        profile_new.setProfile_id(device_id.trim()+"_"+(i+9));
                    }
                    profile_new.setAdd_time(System.currentTimeMillis());
                    profile_new.setDevice(device);
                    profile_new.setNum_account(0);
                    profile_new.setUpdate_time(0L);
                    profile_new.setState(1);
                    profileRepository.save(profile_new);
                }
                profile=profileRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                Account account_get=null;
                if(mySQLCheck.getValue()<settingSystem.getMax_mysql()){
                    String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                    String code="";
                    for(int i=0;i<50;i++){
                        Integer ranver=ran.nextInt(stringrand.length());
                        code=code+stringrand.charAt(ranver);
                    }
                    account_get= accountRepository.get_Account_By_DeviceId(device_id,System.currentTimeMillis(),code);
                }
                if(account_get!=null){
                    AccountTask accountTask=new AccountTask();
                    accountTask.setAccount(account_get);
                    accountTask.setDevice(device);
                    accountTask.setAccount_level(0);
                    accountTask.setGet_time(0L);
                    accountTask.setOrder_id(0L);
                    accountTask.setRunning(0);
                    accountTask.setState(1);
                    accountTask.setProfile(profile);
                    accountTask.setTask_index(0);
                    accountTaskRepository.save(accountTask);

                    device.setNum_account(device.getNum_account()+1);
                    deviceRepository.save(device);
                    profile.setNum_account(profile.getNum_account()+1);
                    profileRepository.save(profile);
                }
            }
            //--------------------end_get_Account----------------------//
            AccountTask accountTask = accountTaskRepository.check_Account_Running_By_ProfileId(device_id.trim()+"_"+profile_id);
            //Check số lần get nhiệm vụ của 1 account
            if(accountTask!=null){
                if(accountTask.getTask_index()>=platformRepository.get_Priority_By_Platform(accountTask.getPlatform())){
                    if(accountTask.getTask_list().trim().length()==0){
                        accountTaskRepository.reset_Thread_Index_By_AccountId(accountTask.getAccount().getAccount_id().trim());
                        accountTask=null;
                        if(profile.getNum_account()>1){
                            accountTask = accountTaskRepository.get_Account_By_ProfileId(device_id.trim()+"_"+profile_id.trim());
                        }
                    }else{
                        String task_List="";
                        if(platform.length()==0){
                            task_List=accountTask.getTask_list();
                        }else{
                            task_List=platform;
                        }
                        Integer index_Off=task_List.indexOf(',')<0?task_List.length():task_List.indexOf(',');
                        accountTask.setTask_list(task_List.indexOf(',')<0?"":task_List.substring(index_Off+1));
                        accountTask.setTask_index(0);
                        accountTask.setPlatform(task_List.indexOf(',')<0?task_List:task_List.substring(0,index_Off));
                    }
                }
            }else{
                profile.setUpdate_time(System.currentTimeMillis());
                profileRepository.save(profile);
                accountTask = accountTaskRepository.get_Account_By_ProfileId(device_id.trim()+"_"+profile_id.trim());
                String task_List="";
                if(platform.length()==0){
                    List<String> string_Task_List=platformRepository.get_All_Platform_True();
                    task_List=String.join(",", string_Task_List);
                }else{
                    task_List=platform;
                }
                Integer index_Off=task_List.indexOf(',')<0?task_List.length():task_List.indexOf(',');
                accountTask.setTask_list(task_List.indexOf(',')<0?"":task_List.substring(index_Off+1));
                accountTask.setPlatform(task_List.indexOf(',')<0?task_List:task_List.substring(0,index_Off));
            }
            if(accountTask==null){
                accountTaskRepository.reset_Thread_Index_By_ProfileId(profile_id.trim());
                profile=profileRepository.get_Profile_Get_Task(device_id.trim());
                if(device.getNum_profile()>1){
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "profile_changer");
                    data.put("profile_id", Integer.parseInt(profile.getProfile_id().split(device_id.trim()+"_")[1]));
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else if(device.getNum_profile()==1){
                    profile.setUpdate_time(System.currentTimeMillis());
                    profileRepository.save(profile);
                    accountTask = accountTaskRepository.get_Account_By_ProfileId(device_id.trim()+"_"+profile_id.trim());
                }else{
                    resp.put("status", false);
                    data.put("message", "Không có account để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
            accountTask.setTask_index(accountTask.getTask_index()+1);
            List<TaskPriority> priorityTasks =taskPriorityRepository.get_Priority_Task_By_Platform(accountTask.getPlatform());
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
                if(accountTask.getPlatform().equals("tiktok")){
                    if(task.equals("tiktok_follower")){
                        get_task= tiktokTask.tiktok_follower(accountTask.getAccount().getAccount_id().trim());
                    }else if(task.equals("tiktok_like")){
                        get_task=tiktokTask.tiktok_like(accountTask.getAccount().getAccount_id().trim());
                    }else if(task.equals("tiktok_view")){
                        get_task=tiktokTask.tiktok_view(accountTask.getAccount().getAccount_id().trim());
                    }else if(task.equals("tiktok_comment")){
                        get_task=tiktokTask.tiktok_comment(accountTask.getAccount().getAccount_id().trim());
                    }
                } else if(accountTask.getPlatform().equals("youtube")){
                    if(task.equals("youtube_view")){
                        get_task=youtubeTask.youtube_view(accountTask.getAccount().getAccount_id().trim());
                    }else if(task.equals("youtube_like")){
                        get_task=youtubeTask.youtube_like(accountTask.getAccount().getAccount_id().trim());
                    }else if(task.equals("youtube_subscriber")){
                        get_task=youtubeTask.youtube_subscriber(accountTask.getAccount().getAccount_id().trim());
                    }
                } else if(accountTask.getPlatform().equals("facebook")){
                    if(task.equals("facebook_follower")){
                        get_task=facebookTask.facebook_follower(accountTask.getAccount().getAccount_id().trim());
                    }else if(task.equals("facebook_like")){
                        get_task=facebookTask.facebook_like(accountTask.getAccount().getAccount_id().trim());
                    }else if(task.equals("facebook_view")){
                        get_task=facebookTask.facebook_view(accountTask.getAccount().getAccount_id().trim());
                    }else if(task.equals("facebook_comment")){
                        get_task=facebookTask.facebook_comment(accountTask.getAccount().getAccount_id().trim());
                    }else if(task.equals("facebook_member")){
                        get_task=facebookTask.facebook_member(accountTask.getAccount().getAccount_id().trim());
                    }
                } else if(accountTask.getPlatform().equals("x")){
                    if(task.equals("x_follower")){
                        get_task=xTask.x_follower(accountTask.getAccount().getAccount_id().trim());
                    }else if(task.equals("x_like")){
                        get_task=xTask.x_like(accountTask.getAccount().getAccount_id().trim());
                    }else if(task.equals("x_view")){
                        get_task=xTask.x_view(accountTask.getAccount().getAccount_id().trim());
                    }else if(task.equals("x_comment")){
                        get_task=xTask.x_comment(accountTask.getAccount().getAccount_id().trim());
                    }else if(task.equals("x_repost")){
                        get_task=xTask.x_repost(accountTask.getAccount().getAccount_id().trim());
                    }
                } else if(accountTask.getPlatform().equals("instagram")){
                    if(task.equals("instagram_follower")){
                        get_task=instagramTask.instagram_follower(accountTask.getAccount().getAccount_id().trim());
                    }else if(task.equals("instagram_like")){
                        get_task=instagramTask.instagram_like(accountTask.getAccount().getAccount_id().trim());
                    }else if(task.equals("instagram_view")){
                        get_task=instagramTask.instagram_view(accountTask.getAccount().getAccount_id().trim());
                    }else if(task.equals("instagram_comment")){
                        get_task=instagramTask.instagram_comment(accountTask.getAccount().getAccount_id().trim());
                    }
                } else if(accountTask.getPlatform().equals("threads")){
                    if(task.equals("threads_follower")){
                        get_task=threadsTask.threads_follower(accountTask.getAccount().getAccount_id().trim());
                    }else if(task.equals("threads_like")){
                        get_task=threadsTask.threads_like(accountTask.getAccount().getAccount_id().trim());
                    }else if(task.equals("threads_view")){
                        get_task=threadsTask.threads_view(accountTask.getAccount().getAccount_id().trim());
                    }else if(task.equals("threads_comment")){
                        get_task=threadsTask.threads_comment(accountTask.getAccount().getAccount_id().trim());
                    }else if(task.equals("threads_repost")){
                        get_task=threadsTask.threads_repost(accountTask.getAccount().getAccount_id().trim());
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
                dataJson.put("task_index",accountTask.getTask_index());
                //System.out.println(dataJson);
                respJson.put("status",true);
                respJson.put("data",dataJson);
                //--------------------------------------------//
                accountTask.setGet_time(System.currentTimeMillis());
                accountTask.setOrder_id(Long.parseLong(dataJson.get("order_id").toString()));
                accountTask.setRunning(1);
                accountTask.setTask(dataJson.get("task").toString());
                accountTask.setPlatform(dataJson.get("platform").toString());
                accountTask.setTask_key(dataJson.get("task_key").toString());
                accountTaskRepository.save(accountTask);
                //--------------------------------------------//
                dataJson.remove("order_id");
            }else{
                respJson.put("status",false);
                dataJson.put("message","Không có nhiệm vụ!");
                respJson.put("data",dataJson);
                accountTask.setRunning(0);
                accountTask.setGet_time(System.currentTimeMillis());
                accountTaskRepository.save(accountTask);
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
    @GetMapping(value = "getTaskThuan", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> getTaskThuan(@RequestHeader(defaultValue = "") String Authorization,
                                                     @RequestParam(defaultValue = "") String device_id,
                                                     @RequestParam(defaultValue = "") String profile_id) throws InterruptedException {
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
            Profile profile=null;
            if(device!=null){
                device.setUpdate_time(System.currentTimeMillis());
                deviceRepository.save(device);
                profile =profileRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                if(profile==null){
                    resp.put("status", false);
                    data.put("message", "profile không tồn tại");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
                }
                if(profile.getNum_account()<settingSystem.getMax_acc()){
                    Account account_get=null;
                    if(mySQLCheck.getValue()<settingSystem.getMax_mysql()){
                        String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                        String code="";
                        for(int i=0;i<50;i++){
                            Integer ranver=ran.nextInt(stringrand.length());
                            code=code+stringrand.charAt(ranver);
                        }
                        account_get= accountRepository.get_Account_By_DeviceId(device_id,System.currentTimeMillis(),code);
                    }
                    if(account_get!=null){
                        AccountTask accountTask=new AccountTask();
                        accountTask.setAccount(account_get);
                        accountTask.setDevice(device);
                        accountTask.setAccount_level(0);
                        accountTask.setGet_time(0L);
                        accountTask.setOrder_id(0L);
                        accountTask.setRunning(0);
                        accountTask.setState(1);
                        accountTask.setState(1);
                        accountTask.setProfile(profile);
                        accountTask.setTask_index(0);
                        accountTaskRepository.save(accountTask);

                        device.setNum_account(device.getNum_account()+1);
                        deviceRepository.save(device);
                        profile.setNum_account(profile.getNum_account()+1);
                        profileRepository.save(profile);

                        resp.put("status", true);
                        data.put("platform", "system");
                        data.put("task", "login");
                        data.put("profile", profile.getProfile_id().split(device_id.trim()+"_")[1]);
                        data.put("account_id", account_get.getAccount_id().trim());
                        data.put("password", account_get.getPassword().trim());
                        data.put("recover_mail", account_get.getRecover_mail().trim());
                        data.put("auth_2fa", account_get.getAuth_2fa().trim());
                        resp.put("data",data);
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }
            }else
            {
                Device device_new=new Device();
                device_new.setDevice_id(device_id.trim());
                device_new.setAdd_time(System.currentTimeMillis());
                device_new.setState(1);
                device_new.setUpdate_time(System.currentTimeMillis());
                device_new.setNum_account(0);
                device_new.setNum_profile(settingSystem.getMax_profile());
                deviceRepository.save(device_new);
                device=device_new;

                for(int i=0;i<settingSystem.getMax_profile();i++){
                    Profile profile_new=new Profile();
                    profile_new.setProfile_id(device_id.trim()+"_"+i);
                    profile_new.setAdd_time(System.currentTimeMillis());
                    profile_new.setDevice(device);
                    profile_new.setNum_account(0);
                    profile_new.setUpdate_time(0L);
                    profile_new.setState(1);
                    profileRepository.save(profile_new);
                }
                profile=profileRepository.check_ProfileId(device_id.trim()+"_"+profile_id.trim());
                Account account_get=null;
                if(mySQLCheck.getValue()<settingSystem.getMax_mysql()){
                    String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                    String code="";
                    for(int i=0;i<50;i++){
                        Integer ranver=ran.nextInt(stringrand.length());
                        code=code+stringrand.charAt(ranver);
                    }
                    account_get= accountRepository.get_Account_By_DeviceId(device_id,System.currentTimeMillis(),code);
                }
                if(account_get!=null){
                    AccountTask accountTask=new AccountTask();
                    accountTask.setAccount(account_get);
                    accountTask.setDevice(device);
                    accountTask.setAccount_level(0);
                    accountTask.setGet_time(0L);
                    accountTask.setOrder_id(0L);
                    accountTask.setRunning(0);
                    accountTask.setState(1);
                    accountTask.setState(1);
                    accountTask.setProfile(profile);
                    accountTask.setTask_index(0);
                    accountTaskRepository.save(accountTask);

                    device.setNum_account(device.getNum_account()+1);
                    deviceRepository.save(device);
                    profile.setNum_account(profile.getNum_account()+1);
                    profileRepository.save(profile);

                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "login");
                    data.put("profile", profile.getProfile_id().split(device_id.trim()+"_")[1]);
                    data.put("account_id", account_get.getAccount_id().trim());
                    data.put("password", account_get.getPassword().trim());
                    data.put("recover_mail", account_get.getRecover_mail().trim());
                    data.put("auth_2fa", account_get.getAuth_2fa().trim());
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
            //--------------------end_get_Account----------------------//
            AccountTask accountTask = accountTaskRepository.check_Account_Running_By_ProfileId(device_id.trim()+"_"+profile_id);
            //Check số lần get nhiệm vụ của 1 account
            if(accountTask!=null){
                if(accountTask.getTask_index()>=settingSystem.getMax_task()){
                    accountTaskRepository.reset_Thread_Index_By_AccountId(accountTask.getAccount().getAccount_id().trim());
                    accountTask=null;
                    if(profile.getNum_account()>1){
                        accountTask = accountTaskRepository.get_Account_By_ProfileId(device_id.trim()+"_"+profile_id.trim());
                    }
                }
            }else{
                profile.setUpdate_time(System.currentTimeMillis());
                profileRepository.save(profile);
                accountTask = accountTaskRepository.get_Account_By_ProfileId(device_id.trim()+"_"+profile_id.trim());
            }
            if(accountTask==null){
                accountTaskRepository.reset_Thread_Index_By_ProfileId(profile_id.trim());
                profile=profileRepository.get_Profile_Get_Task(device_id.trim());
                if(device.getNum_profile()>1){
                    resp.put("status", true);
                    data.put("platform", "system");
                    data.put("task", "profile_changer");
                    data.put("profile_id", profile.getProfile_id().split(device_id.trim()+"_")[1]);
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else if(device.getNum_profile()==1){
                    profile.setUpdate_time(System.currentTimeMillis());
                    profileRepository.save(profile);
                    accountTask = accountTaskRepository.get_Account_By_ProfileId(device_id.trim()+"_"+profile_id.trim());
                }else{
                    resp.put("status", false);
                    data.put("message", "Không có account để chạy");
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }
            accountTask.setTask_index(accountTask.getTask_index()+1);
            List<TaskPriority> priorityTasks =taskPriorityRepository.get_Priority_Task();

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
                if(task.equals("tiktok_follower")){
                    get_task= tiktokTask.tiktok_follower(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("youtube_view")){
                    get_task=youtubeTask.youtube_view(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("youtube_like")){
                    get_task=youtubeTask.youtube_like(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("youtube_subscriber")){
                    get_task=youtubeTask.youtube_subscriber(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("tiktok_like")){
                    get_task=tiktokTask.tiktok_like(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("tiktok_view")){
                    get_task=tiktokTask.tiktok_view(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("tiktok_comment")){
                    get_task=tiktokTask.tiktok_comment(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("facebook_follower")){
                    get_task=facebookTask.facebook_follower(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("facebook_like")){
                    get_task=facebookTask.facebook_like(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("facebook_view")){
                    get_task=facebookTask.facebook_view(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("facebook_comment")){
                    get_task=facebookTask.facebook_comment(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("facebook_member")){
                    get_task=facebookTask.facebook_member(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("x_follower")){
                    get_task=xTask.x_follower(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("x_like")){
                    get_task=xTask.x_like(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("x_view")){
                    get_task=xTask.x_view(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("x_comment")){
                    get_task=xTask.x_comment(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("x_repost")){
                    get_task=xTask.x_repost(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("instagram_follower")){
                    get_task=instagramTask.instagram_follower(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("instagram_like")){
                    get_task=instagramTask.instagram_like(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("instagram_view")){
                    get_task=instagramTask.instagram_view(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("instagram_comment")){
                    get_task=instagramTask.instagram_comment(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("threads_follower")){
                    get_task=threadsTask.threads_follower(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("threads_like")){
                    get_task=threadsTask.threads_like(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("threads_view")){
                    get_task=threadsTask.threads_view(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("threads_comment")){
                    get_task=threadsTask.threads_comment(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("threads_repost")){
                    get_task=threadsTask.threads_repost(accountTask.getAccount().getAccount_id().trim());
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
                //System.out.println(dataJson);
                respJson.put("status",true);
                respJson.put("data",dataJson);
                //--------------------------------------------//
                accountTask.setGet_time(System.currentTimeMillis());
                accountTask.setOrder_id(Long.parseLong(dataJson.get("order_id").toString()));
                accountTask.setRunning(1);
                accountTask.setTask(dataJson.get("task").toString());
                accountTask.setPlatform(dataJson.get("platform").toString());
                accountTask.setTask_key(dataJson.get("task_key").toString());
                accountTaskRepository.save(accountTask);
                //--------------------------------------------//
                dataJson.remove("order_id");
            }else{
                respJson.put("status",false);
                dataJson.put("message","Không có nhiệm vụ!");
                respJson.put("data",dataJson);
                accountTask.setRunning(0);
                accountTask.setGet_time(System.currentTimeMillis());
                accountTaskRepository.save(accountTask);
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


    @GetMapping(value = "getTask001", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> getTask001(@RequestHeader(defaultValue = "") String Authorization,
                                                   @RequestParam(defaultValue = "") String account_id,
                                                   @RequestParam(defaultValue = "") String platform,
                                                   @RequestParam(defaultValue = "") String device_id) throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran=new Random();
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
            if (checktoken ==0) {
                resp.put("status", false);
                data.put("message", "Token expired");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (account_id.length()==0) {
                resp.put("status", false);
                data.put("message", "account_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (device_id.length()==0) {
                resp.put("status", false);
                data.put("message", "account_id không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //---------------------get_Account------------------//
            Device device=deviceRepository.check_DeviceId(device_id.trim());
            if(device!=null){
                device.setUpdate_time(System.currentTimeMillis());
                deviceRepository.save(device);
                if(accountTaskRepository.find_AccountTask_By_AccountId(account_id.trim())==0)
                {
                    Account account= accountRepository.get_Account_By_Account_id(account_id.trim());
                    if(account==null){
                        Account account_new=new Account();
                        account_new.setAccount_id(account_id);
                        account_new.setPassword("");
                        account_new.setRecover_mail("");
                        account_new.setLive(1);
                        account_new.setAuth_2fa(account_new.getAuth_2fa());
                        account_new.setComputer_id(null);
                        account_new.setBox_id(null);
                        account_new.setDevice_id(null);
                        account_new.setProfile_id(null);
                        account_new.setAdd_time(System.currentTimeMillis());
                        account_new.setGet_time(System.currentTimeMillis());
                        accountRepository.save(account_new);
                        account=account_new;
                    }


                    AccountTask accountTask=new AccountTask();
                    accountTask.setAccount(account);
                    accountTask.setDevice(device);
                    accountTask.setAccount_level(0);
                    accountTask.setGet_time(0L);
                    accountTask.setOrder_id(0L);
                    accountTask.setRunning(0);
                    accountTask.setState(1);
                    accountTask.setState(1);
                    accountTask.setProfile(null);
                    accountTask.setTask_index(0);
                    accountTaskRepository.save(accountTask);
                }
            }else{
                Device device_new=new Device();
                device_new.setDevice_id(device_id.trim());
                device_new.setAdd_time(System.currentTimeMillis());
                device_new.setState(1);
                device_new.setUpdate_time(System.currentTimeMillis());
                device_new.setNum_account(0);
                device_new.setNum_profile(0);
                deviceRepository.save(device_new);
                device=device_new;
                if(accountTaskRepository.find_AccountTask_By_AccountId(account_id.trim())==0)
                {
                    Account account= accountRepository.get_Account_By_Account_id(account_id.trim());
                    if(account==null){
                        Account account_new=new Account();
                        account_new.setAccount_id(account_id);
                        account_new.setPassword("");
                        account_new.setRecover_mail("");
                        account_new.setLive(1);
                        account_new.setAuth_2fa(account_new.getAuth_2fa());
                        account_new.setComputer_id(null);
                        account_new.setBox_id(null);
                        account_new.setDevice_id(null);
                        account_new.setProfile_id(null);
                        account_new.setAdd_time(System.currentTimeMillis());
                        account_new.setGet_time(System.currentTimeMillis());
                        accountRepository.save(account_new);
                        account=account_new;
                    }


                    AccountTask accountTask=new AccountTask();
                    accountTask.setAccount(account);
                    accountTask.setDevice(device);
                    accountTask.setAccount_level(0);
                    accountTask.setGet_time(0L);
                    accountTask.setOrder_id(0L);
                    accountTask.setRunning(0);
                    accountTask.setState(1);
                    accountTask.setProfile(null);
                    accountTask.setTask_index(0);
                    accountTaskRepository.save(accountTask);
                }
            }
            //--------------------end_get_Account----------------------//
            AccountTask accountTask = accountTaskRepository.get_Account_By_Account_id(account_id.trim());

            List<TaskPriority> priorityTasks;
            if(platform.length()==0){
              priorityTasks =taskPriorityRepository.get_Priority_Task();
            }else{
              priorityTasks =taskPriorityRepository.get_Priority_Task_By_Platform(platform.trim());
            }


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
                if(task.equals("tiktok_follower")){
                    get_task= tiktokTask.tiktok_follower(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("youtube_view")){
                    get_task=youtubeTask.youtube_view(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("youtube_like")){
                    get_task=youtubeTask.youtube_like(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("youtube_subscriber")){
                    get_task=youtubeTask.youtube_subscriber(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("tiktok_like")){
                    get_task=tiktokTask.tiktok_like(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("tiktok_view")){
                    get_task=tiktokTask.tiktok_view(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("tiktok_comment")){
                    get_task=tiktokTask.tiktok_comment(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("facebook_follower")){
                    get_task=facebookTask.facebook_follower(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("facebook_like")){
                    get_task=facebookTask.facebook_like(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("facebook_view")){
                    get_task=facebookTask.facebook_view(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("facebook_comment")){
                    get_task=facebookTask.facebook_comment(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("facebook_member")){
                    get_task=facebookTask.facebook_member(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("x_follower")){
                    get_task=xTask.x_follower(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("x_like")){
                    get_task=xTask.x_like(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("x_view")){
                    get_task=xTask.x_view(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("x_comment")){
                    get_task=xTask.x_comment(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("x_repost")){
                    get_task=xTask.x_repost(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("instagram_follower")){
                    get_task=instagramTask.instagram_follower(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("instagram_like")){
                    get_task=instagramTask.instagram_like(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("instagram_view")){
                    get_task=instagramTask.instagram_view(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("instagram_comment")){
                    get_task=instagramTask.instagram_comment(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("threads_follower")){
                    get_task=threadsTask.threads_follower(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("threads_like")){
                    get_task=threadsTask.threads_like(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("threads_view")){
                    get_task=threadsTask.threads_view(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("threads_comment")){
                    get_task=threadsTask.threads_comment(accountTask.getAccount().getAccount_id().trim());
                }else if(task.equals("threads_repost")){
                    get_task=threadsTask.threads_repost(accountTask.getAccount().getAccount_id().trim());
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
                respJson.put("status",true);
                respJson.put("data",dataJson);
                //--------------------------------------------//
                accountTask.setGet_time(System.currentTimeMillis());
                accountTask.setOrder_id(Long.parseLong(dataJson.get("order_id").toString()));
                accountTask.setRunning(1);
                accountTask.setTask(dataJson.get("task").toString());
                accountTask.setPlatform(dataJson.get("platform").toString());
                accountTask.setTask_key(dataJson.get("task_key").toString());
                accountTaskRepository.save(accountTask);
                //--------------------------------------------//
                dataJson.remove("order_id");
            }else{
                respJson.put("status",false);
                dataJson.put("message","Không có nhiệm vụ!");
                respJson.put("data",dataJson);
                accountTask.setRunning(0);
                accountTask.setGet_time(System.currentTimeMillis());
                accountTaskRepository.save(accountTask);
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

    @GetMapping(value = "/updateTask", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> updateTask(@RequestHeader(defaultValue = "") String Authorization,
                                                   @RequestParam(defaultValue = "") String account_id,@RequestParam  Boolean status,
                                                   @RequestParam(defaultValue = "") String task,
                                                   @RequestParam(defaultValue = "") String task_key,
                                                   @RequestParam(defaultValue = "0") Integer viewing_time,
                                                   @RequestParam(defaultValue = "-1") Integer islogin,
                                      @RequestParam(defaultValue = "") String platform) {
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
            if (account_id.length() == 0) {
                resp.put("status", false);
                data.put("message", "username không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (task.length() == 0) {
                resp.put("status", false);
                data.put("message", "task không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (platform.length() == 0) {
                resp.put("status", false);
                data.put("message", "platform không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if (status == null) {
                resp.put("status", false);
                data.put("message", "status không để trống");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            profileTaskRepository.reset_Thread_By_AccountId(account_id.trim()+"%");
            String platform_Check = platform.toLowerCase().trim();
            if(platform_Check.equals("youtube")){
                if(task.toLowerCase().trim().equals("view")&&status==true){
                    youtubeUpdate.youtube_view(account_id.trim(),task_key.trim());
                }else  if(task.toLowerCase().trim().equals("subscriber")&&status==true){
                    youtubeUpdate.youtube_subscriber(account_id.trim(),task_key.trim());
                } else  if(task.toLowerCase().trim().equals("like")&&status==true){
                    youtubeUpdate.youtube_like(account_id.trim(),task_key.trim());
                }
            }else if(platform_Check.equals("tiktok")){
                if(task.toLowerCase().trim().equals("follower")&&status==true){
                   tiktokUpdate.tiktok_follower(account_id.trim(),task_key.trim());
                }else  if(task.toLowerCase().trim().equals("like")&&status==true){
                    tiktokUpdate.tiktok_like(account_id.trim(),task_key.trim());
                }else  if(task.toLowerCase().trim().equals("comment")){
                    tiktokUpdate.tiktok_comment(account_id.trim(),task_key.trim(),status);
                }else  if(task.toLowerCase().trim().equals("view")&&status==true){
                    tiktokUpdate.tiktok_view(account_id.trim(),task_key.trim());
                }
            }else if(platform_Check.equals("facebook")){
                if(task.toLowerCase().trim().equals("follower")&&status==true){
                  facebookUpdate.facebook_follower(account_id.trim(),task_key.trim());
                }else  if(task.toLowerCase().trim().equals("like")&&status==true){
                    facebookUpdate.facebook_like(account_id.trim(),task_key.trim());
                }else  if(task.toLowerCase().trim().equals("comment")) {
                    facebookUpdate.facebook_comment(account_id.trim(), task_key.trim(), status);
                }else  if(task.toLowerCase().trim().equals("member")&&status==true) {
                    facebookUpdate.facebook_member(account_id.trim(), task_key.trim());
                }else  if(task.toLowerCase().trim().equals("view")&&status==true) {
                    facebookUpdate.facebook_view(account_id.trim(), task_key.trim());
                }
            }else if(platform_Check.equals("x")){
                if(task.toLowerCase().trim().equals("follower")&&status==true){
                    xUpdate.x_follower(account_id.trim(),task_key.trim());
                }else  if(task.toLowerCase().trim().equals("like")&&status==true){
                    xUpdate.x_like(account_id.trim(),task_key.trim());
                }else  if(task.toLowerCase().trim().equals("comment")) {
                    xUpdate.x_comment(account_id.trim(), task_key.trim(), status);
                }else  if(task.toLowerCase().trim().equals("view")&&status==true) {
                    xUpdate.x_view(account_id.trim(), task_key.trim());
                }else  if(task.toLowerCase().trim().equals("repost")&&status==true) {
                    xUpdate.x_repost(account_id.trim(), task_key.trim());
                }
            }else if(platform_Check.equals("instagram")){
                if(task.toLowerCase().trim().equals("follower")&&status==true){
                    instagramUpdate.instagram_follower(account_id.trim(),task_key.trim());
                }else  if(task.toLowerCase().trim().equals("like")&&status==true){
                    instagramUpdate.instagram_like(account_id.trim(),task_key.trim());
                }else  if(task.toLowerCase().trim().equals("comment")) {
                    instagramUpdate.instagram_comment(account_id.trim(), task_key.trim(), status);
                }else  if(task.toLowerCase().trim().equals("view")&&status==true) {
                    instagramUpdate.instagram_view(account_id.trim(), task_key.trim());
                }
            }else if(platform_Check.equals("threads")){
                if(task.toLowerCase().trim().equals("follower")&&status==true){
                    threadsUpdate.threads_follower(account_id.trim(),task_key.trim());
                }else  if(task.toLowerCase().trim().equals("like")&&status==true){
                    threadsUpdate.threads_like(account_id.trim(),task_key.trim());
                }else  if(task.toLowerCase().trim().equals("comment")) {
                    threadsUpdate.threads_comment(account_id.trim(), task_key.trim(), status);
                }else  if(task.toLowerCase().trim().equals("view")&&status==true) {
                    threadsUpdate.threads_view(account_id.trim(), task_key.trim());
                }else  if(task.toLowerCase().trim().equals("repost")&&status==true) {
                    threadsUpdate.threads_repost(account_id.trim(), task_key.trim());
                }
            }
            if(status==true){
                try {
                    OrderRunning orderRunning=null;
                    if(platform_Check.equals("youtube")&&task.toLowerCase().trim().equals("subscriber")){
                        String order_Key= dataSubscriberRepository.get_ChannelId_By_VideoId(task_key.trim());
                        orderRunning=orderRunningRepository.find_Order_By_Order_Key(order_Key,task.trim(),platform.trim());
                    }else{
                        orderRunning=orderRunningRepository.find_Order_By_Order_Key(task_key.trim(),task.trim(),platform.trim());
                    }
                    if(orderRunning!=null){
                        HistorySum historySum=new HistorySum();
                        historySum.setOrderRunning(orderRunning);
                        historySum.setAccount_id(account_id.trim());
                        historySum.setViewing_time(viewing_time);
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
                if(islogin==0){
                    AccountProfile accountProfile=accountProfileRepository.get_Account_By_Account_id_And_Platform(account_id.trim()+"%",platform.trim());
                    accountProfile.setLive(0);
                    accountProfile.setUpdate_time(System.currentTimeMillis());
                    accountProfileRepository.save(accountProfile);
                    resp.put("status", true);
                    data.put("message", "Update thành công!");
                    data.put("account_id", accountProfile.getAccount_id());
                    data.put("password", accountProfile.getPassword());
                    data.put("recover", accountProfile.getRecover());
                    data.put("2fa", accountProfile.getAuth_2fa());
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else if(islogin==1){
                    AccountProfile accountProfile=accountProfileRepository.get_Account_By_Account_id_And_Platform(account_id.trim()+"%",platform.trim());
                    accountProfile.setLive(1);
                    accountProfile.setUpdate_time(System.currentTimeMillis());
                    accountProfileRepository.save(accountProfile);
                }else{
                    AccountProfile accountProfile=accountProfileRepository.get_Account_By_Account_id_And_Platform(account_id.trim()+"%",platform.trim());
                    accountProfile.setLive(1);
                    accountProfile.setUpdate_time(System.currentTimeMillis());
                    accountProfileRepository.save(accountProfile);
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
            profileTaskRepository.reset_Thread_By_AccountId(updateTaskRequest.getAccount_id().trim()+"%");
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
                    tiktokUpdate.tiktok_follower(updateTaskRequest.getAccount_id().trim(),updateTaskRequest.getTask_key().trim());
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
                    resp.put("status", true);
                    data.put("message", "Update thành công!");
                    data.put("account_id", accountProfile.getAccount_id());
                    data.put("password", accountProfile.getPassword());
                    data.put("recover", accountProfile.getRecover());
                    data.put("2fa", accountProfile.getAuth_2fa());
                    resp.put("data", data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
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
                    System.out.println("Like "+updateTaskRequest.getIsLogin());
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

package com.nts.awspremium.controller;

import com.nts.awspremium.model.*;
import com.nts.awspremium.model_system.MySQLCheck;
import com.nts.awspremium.model_system.OrderThreadCheck;
import com.nts.awspremium.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/account")
public class AccountController {
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
    private ProfileRepository profileRepository;

    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private AccountTaskRepository accountTaskRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private SettingTikTokRepository settingTikTokRepository;
    @Autowired
    private SettingSystemRepository settingSystemRepository;

    @Autowired
    private TikTokFollower24hRepository tikTokFollower24hRepository;

    @Autowired
    private TikTokAccountHistoryRepository tikTokAccountHistoryRepository;


    @PostMapping(value = "add_Account", produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>> add_Account(@RequestHeader(defaultValue = "") String Authorization,
                                                   @RequestBody Account account) throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran=new Random();
            Thread.sleep(500+ran.nextInt(1500));
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
            if (checktoken ==0) {
                resp.put("status", false);
                data.put("message", "Token expired");
                resp.put("data", data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            //---------------------get_Account------------------//
            SettingSystem settingSystem=settingSystemRepository.getReferenceById(1L);
            Device device=deviceRepository.check_DeviceId(account.getDevice_id());
            Profile profile =profileRepository.check_ProfileId(account.getDevice_id().trim()+"_"+account.getProfile_id().trim());
            if(device!=null){
                if(profile!=null){
                    Account account_new=new Account();
                    account_new.setAccount_id(account.getAccount_id());
                    account_new.setPassword(account.getPassword());
                    account_new.setRecover_mail(account.getRecover_mail());
                    account_new.setLive(1);
                    account_new.setAuth_2fa(account_new.getAuth_2fa());
                    account_new.setComputer_id(null);
                    account_new.setBox_id(null);
                    account_new.setDevice_id(account_new.getDevice_id());
                    account_new.setProfile_id(account_new.getProfile_id());
                    account_new.setAdd_time(System.currentTimeMillis());
                    account_new.setGet_time(System.currentTimeMillis());
                    accountRepository.save(account_new);

                    device.setUpdate_time(System.currentTimeMillis());
                    device.setNum_account(profile.getNum_account()+1);
                    deviceRepository.save(device);

                    profile.setUpdate_time(System.currentTimeMillis());
                    profile.setNum_account(profile.getNum_account()+1);
                    profileRepository.save(profile);

                    resp.put("status", true);
                    data.put("message", "add account thành công");
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);


                }else{
                    Profile profile_new=new Profile();
                    profile_new.setProfile_id(account.getProfile_id());
                    profile_new.setAdd_time(System.currentTimeMillis());
                    profile_new.setDevice(device);
                    profile_new.setNum_account(0);
                    profile_new.setUpdate_time(0L);
                    profile_new.setState(1);
                    profileRepository.save(profile_new);
                    profile=profile_new;

                    Account account_new=new Account();
                    account_new.setAccount_id(account.getAccount_id());
                    account_new.setPassword(account.getPassword());
                    account_new.setRecover_mail(account.getRecover_mail());
                    account_new.setLive(1);
                    account_new.setAuth_2fa(account_new.getAuth_2fa());
                    account_new.setComputer_id(null);
                    account_new.setBox_id(null);
                    account_new.setDevice_id(account_new.getDevice_id());
                    account_new.setProfile_id(account_new.getProfile_id());
                    account_new.setAdd_time(System.currentTimeMillis());
                    account_new.setGet_time(System.currentTimeMillis());
                    accountRepository.save(account_new);

                    device.setUpdate_time(System.currentTimeMillis());
                    device.setNum_account(profile.getNum_account()+1);
                    deviceRepository.save(device);

                    profile.setUpdate_time(System.currentTimeMillis());
                    profile.setNum_account(profile.getNum_account()+1);
                    profileRepository.save(profile);

                    resp.put("status", true);
                    data.put("message", "add account thành công");
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }else{
                Device device_new=new Device();
                device_new.setDevice_id(account.getDevice_id());
                device_new.setAdd_time(System.currentTimeMillis());
                device_new.setState(1);
                device_new.setBox(null);
                device_new.setUpdate_time(System.currentTimeMillis());
                device_new.setNum_account(0);
                deviceRepository.save(device_new);
                device=device_new;

                Profile profile_new=new Profile();
                profile_new.setProfile_id(account.getProfile_id());
                profile_new.setAdd_time(System.currentTimeMillis());
                profile_new.setDevice(device);
                profile_new.setNum_account(0);
                profile_new.setUpdate_time(0L);
                profile_new.setState(1);
                profileRepository.save(profile_new);
                profile=profile_new;

                Account account_new=new Account();
                account_new.setAccount_id(account.getAccount_id());
                account_new.setPassword(account.getPassword());
                account_new.setRecover_mail(account.getRecover_mail());
                account_new.setLive(1);
                account_new.setAuth_2fa(account_new.getAuth_2fa());
                account_new.setComputer_id(null);
                account_new.setBox_id(null);
                account_new.setDevice_id(account_new.getDevice_id());
                account_new.setProfile_id(account_new.getProfile_id());
                account_new.setAdd_time(System.currentTimeMillis());
                account_new.setGet_time(System.currentTimeMillis());
                accountRepository.save(account_new);

                device.setUpdate_time(System.currentTimeMillis());
                device.setNum_account(profile.getNum_account()+1);
                deviceRepository.save(device);

                profile.setUpdate_time(System.currentTimeMillis());
                profile.setNum_account(profile.getNum_account()+1);
                profileRepository.save(profile);

                resp.put("status", true);
                data.put("message", "add account thành công");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }

        }catch (Exception e){
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            System.out.println(stackTraceElement.getMethodName());
            System.out.println(stackTraceElement.getLineNumber());
            System.out.println(stackTraceElement.getClassName());
            System.out.println(stackTraceElement.getFileName());
            System.out.println("Error : " + e.getMessage());
            resp.put("status", false);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }

    }


}

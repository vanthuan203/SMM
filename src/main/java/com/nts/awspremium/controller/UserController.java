package com.nts.awspremium.controller;

import com.nts.awspremium.GoogleApi;
import com.nts.awspremium.MailApi;
import com.nts.awspremium.TikTokApi;
import com.nts.awspremium.model.Balance;
import com.nts.awspremium.model.GoogleKey;
import com.nts.awspremium.model.LogError;
import com.nts.awspremium.model.User;
import com.nts.awspremium.repositories.BalanceRepository;
import com.nts.awspremium.repositories.GoogleKeyRepository;
import com.nts.awspremium.repositories.LogErrorRepository;
import com.nts.awspremium.repositories.UserRepository;
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
@RequestMapping(value = "/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    @Autowired
    private BalanceRepository balanceRepository;
    @PostMapping(path = "login",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> login(@RequestBody User user){
        JSONObject resp=new JSONObject();
        try{
            String token =userRepository.find_Token_User_By_User_Pass(user.getUsername(),user.getPassword());
            if(token.trim()==null){
                resp.put("status",false);
                resp.put("message", "Không có account trùng khớp!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }else{
                resp.put("status",true);
                resp.put("message", "Thành công!");
                resp.put("token",token.trim());
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        }
        catch (Exception e){
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

            resp.put("status",false);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(path = "verify_token",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> verify_token(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        try{
            User user=userRepository.find_User_By_Token(Authorization.trim());
            if(Authorization.length()==0|| user==null){
                resp.put("status","fail");
                resp.put("message", "Token expired");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
            }
            JSONObject obj = new JSONObject();
            obj.put("username", user.getUsername());
            obj.put("role", user.getRole());
            obj.put("balance", user.getBalance());
            obj.put("max_order", user.getMax_order());
            obj.put("discount", user.getDiscount());
            obj.put("vip", user.getVip());
            resp.put("status","success");
            resp.put("user",obj);
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

            resp.put("status",false);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping(path = "get_List_User",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get_List_User(){
        JSONObject resp = new JSONObject();
        try {
            List<String > all_User=userRepository.get_All_Username();
            String list_User="";
            for(int i=0;i<all_User.size();i++){
                if(i==0){
                    list_User=all_User.get(0);
                }else{
                    list_User=list_User+","+all_User.get(i);
                }
            }
            resp.put("user",list_User);
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
            resp.put("status",false);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }

    }


    @PostMapping(path = "getCodeTiktokTM",produces = "application/hal+json;charset=utf8")
    ResponseEntity<Map<String, Object>>  getCodeTiktokTM(@RequestBody JSONObject account){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            if(account==null){
                resp.put("status", false);
                data.put("error", "account is null");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if(account.get("email").toString().length()==0){
                resp.put("status", false);
                data.put("error", "email is null");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            if(account.get("password").toString().length()==0){
                resp.put("status", false);
                data.put("error", "password is null");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            String code= MailApi.getCode(account.get("email").toString().trim(),account.get("password").toString().trim());
            if(code!=null){
                if(code.matches("\\d+")){
                    resp.put("status", true);
                    data.put("code", code.trim());
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }else{
                    resp.put("status", false);
                    data.put("error", code.trim());
                    resp.put("data",data);
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }else{
                resp.put("status", false);
                data.put("error","Error servser");
                resp.put("data",data);
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
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
            resp.put("status",false);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping(path = "get_List_Users",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get_List_Users(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        try{
            //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
            if(checktoken ==0){
                resp.put("status",false);
                resp.put("message", "Token expired");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
            }
            JSONArray jsonArray =new JSONArray();
            List<User> users=userRepository.get_All_User();
            for(int i=0;i<users.size();i++){
                JSONObject obj = new JSONObject();
                obj.put("username", users.get(i).getUsername());
                obj.put("role", users.get(i).getRole());
                obj.put("rate", users.get(i).getRate());
                obj.put("balance", users.get(i).getBalance());
                obj.put("discount", users.get(i).getDiscount());
                obj.put("time_add", users.get(i).getTime_add());
                obj.put("vip",users.get(i).getVip());
                obj.put("max_order",users.get(i).getMax_order());
                obj.put("note",users.get(i).getNote());
                jsonArray.add(obj);
            }
            resp.put("accounts",jsonArray);
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
            resp.put("status",false);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping(path = "update_User",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> update_User(@RequestHeader(defaultValue = "") String Authorization,@RequestBody User user_body){
        JSONObject resp = new JSONObject();
        try{
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
            if(checktoken ==0){
                resp.put("status",false);
                resp.put("message", "Token expired");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
            }
            User user=userRepository.find_User_By_Username(user_body.getUsername().trim());
            Float balance_update=balanceRepository.update_Balance(user_body.getBalance(),user.getUsername().trim());
            user.setVip(user_body.getVip());
            user.setMax_order(user_body.getMax_order());
            user.setDiscount(user_body.getDiscount());
            user.setBalance(balance_update);
            user.setRate(user_body.getRate());
            if(user_body.getBalance()!=0){
                Balance balance=new Balance();
                balance.setUser(user_body.getUsername().trim());
                balance.setAdd_time(System.currentTimeMillis());
                balance.setTotal_blance(balance_update);
                balance.setBalance(user_body.getBalance());
                balance.setNote(user_body.getBalance()>0?"Admin nạp tiền":"Admin trừ tiền");
                balanceRepository.save(balance);
            }
            user.setNote(user_body.getNote());
            userRepository.save(user);
            JSONObject obj = new JSONObject();
            obj.put("username", user.getUsername());
            obj.put("role",user.getRole());
            obj.put("balance", user.getBalance());
            obj.put("discount", user.getDiscount());
            obj.put("max_order",user.getMax_order());
            obj.put("vip",user.getVip());
            obj.put("rate",user.getRate());
            obj.put("note",user.getNote());
            obj.put("add_time",user.getTime_add());
            resp.put("account",obj);
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
            resp.put("status",false);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(path = "check_tiktok",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> check_tiktok(@RequestBody String list){
        JSONObject resp = new JSONObject();
        try{
            String[] tiktok =list.split("\\r\\n");
            Integer count_live=0;
            for (int i=0;i<tiktok.length;i++){
                if(TikTokApi.checkAccount(tiktok[i],2)==1){
                    count_live=count_live+1;
                    System.out.println(tiktok[i]+"|live");
                }else{
                    System.out.println(tiktok[i]+"|die");
                }

            }
            resp.put("count_live",count_live.toString()+"/"+tiktok.length);
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
            resp.put("status",false);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(path = "check_Die_Tiktok",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> check_Die_Tiktok(@RequestBody String list){
        JSONObject resp = new JSONObject();
        try{
            String[] tiktok =list.split("\\r\\n");
            Integer count_live=0;
            Integer count_die=0;
            for (int i=0;i<tiktok.length;i++){
                if(TikTokApi.checkLive(tiktok[i])==true){
                    count_live=count_live+1;
                    //System.out.println(tiktok[i]+"|"+i);
                }else if(TikTokApi.checkLive(tiktok[i])==false){
                    count_die=count_die+1;
                    System.out.println(count_die+"|"+tiktok[i]+"|"+i);
                }
            }
            resp.put("count_live",count_live.toString()+"/"+tiktok.length);
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
            resp.put("status",false);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping(path = "add_User",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> add_User(@RequestHeader(defaultValue = "") String Authorization,@RequestBody User user_body){
        JSONObject resp = new JSONObject();
        try{
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
            if(checktoken ==0){
                resp.put("status",false);
                resp.put("message", "Token expired");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
            }
            User user=new User();
            user.setUsername(user_body.getUsername());
            user.setVip(user_body.getVip());
            user.setMax_order(user_body.getMax_order());
            user.setPassword(user_body.getPassword());
            user.setRole(user_body.getRole());
            user.setTime_add(System.currentTimeMillis());
            user.setDiscount(user_body.getDiscount());
            user.setRate(user_body.getRate());
            user.setBalance(user_body.getBalance());
            user.setNote(user_body.getNote());
            String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
            String token="";
            Random ran=new Random();
            for(int i=0;i<30;i++){
                Integer ranver=ran.nextInt(stringrand.length());
                token=token+stringrand.charAt(ranver);
            }
            user.setToken(token);
            userRepository.save(user);
            if(user_body.getBalance()!=0){
                Balance balance=new Balance();
                balance.setUser(user_body.getUsername().trim());
                balance.setAdd_time(System.currentTimeMillis());
                balance.setTotal_blance(user_body.getBalance());
                balance.setBalance(user_body.getBalance());
                balance.setNote(user_body.getBalance()>0?"Admin nạp tiền":"Admin trừ tiền");
                balanceRepository.save(balance);
            }
            JSONObject obj = new JSONObject();
            obj.put("username", user.getUsername());
            obj.put("role",user.getRole());
            obj.put("balance", user.getBalance());
            obj.put("discount", user.getDiscount());
            obj.put("max_order",user.getMax_order());
            obj.put("vip",user.getVip());
            obj.put("rate",user.getRate());
            obj.put("note",user.getNote());
            obj.put("add_time",user.getTime_add());
            resp.put("user",obj);
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
            resp.put("status",false);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
}

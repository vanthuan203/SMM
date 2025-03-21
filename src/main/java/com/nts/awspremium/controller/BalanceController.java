package com.nts.awspremium.controller;

import com.nts.awspremium.model.Balance;
import com.nts.awspremium.model.BalanceShow;
import com.nts.awspremium.model.LogError;
import com.nts.awspremium.repositories.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/balance")
public class BalanceController {
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TikTokFollower24hRepository tikTokFollower24hRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    @GetMapping(path = "get_List_Balance",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get_List_Balance(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String user){
        JSONObject resp = new JSONObject();
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if(checktoken ==0){
            resp.put("status",false);
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            JSONArray jsonArray =new JSONArray();
            List<BalanceShow> balance;
            if(user.length()==0){
                balance =balanceRepository.getAllBalance();

            }else{
                balance=balanceRepository.getAllBalance(user.trim());
            }
            for(int i=0;i<balance.size();i++){
                JSONObject obj = new JSONObject();
                obj.put("user", balance.get(i).getUser());
                obj.put("total_blance", balance.get(i).getTotal_blance());
                obj.put("balance", balance.get(i).getBalance());
                obj.put("note", balance.get(i).getNote());
                obj.put("add_time", balance.get(i).getAdd_time());
                obj.put("id", balance.get(i).getId());
                obj.put("service", balance.get(i).getService());
                obj.put("platform", balance.get(i).getPlatform());
                obj.put("task", balance.get(i).getTask());
                jsonArray.add(obj);
            }
            resp.put("balances",jsonArray);
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
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }

    }

    @GetMapping(value = "get_Balance_7day", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get_Balance_7day(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if(checktoken ==0){
            resp.put("status",false);
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try {
            List<String> time7day;
            List<String> timesub7day;
            time7day = balanceRepository.get_Balance_7day();
            timesub7day = balanceRepository.get_Refund_7day();
            float count_view=0;
            float count_viewsub=0;
            int count_order=0;
            JSONArray jsonArray = new JSONArray();
            Float maxview = 0F;
            Float maxsubview = 0F;

            for (int i = 0; i < time7day.size(); i++) {
                if(time7day.get(i).split(",")[0].equals(timesub7day.get(i).split(",")[0])){
                    count_order=count_order+Integer.parseInt(time7day.get(i).split(",")[2]);
                    count_view=count_view+Float.parseFloat(time7day.get(i).split(",")[1]);
                    if (maxview < Float.parseFloat(time7day.get(i).split(",")[1])) {
                        maxview = Float.parseFloat(time7day.get(i).split(",")[1]);
                    }
                }
            }
            for (int i = 0; i < timesub7day.size(); i++) {
                //System.out.println(time7day.get(i).split(",")[1]);
                count_viewsub=-Float.parseFloat(timesub7day.get(i).split(",")[1])+count_viewsub;
                if (maxsubview < -Float.parseFloat(timesub7day.get(i).split(",")[1])) {
                    maxsubview = -Float.parseFloat(timesub7day.get(i).split(",")[1]);
                }
            }
            for (int i = 0; i < time7day.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("date", time7day.get(i).split(",")[0]);
                obj.put("view", Float.parseFloat(time7day.get(i).split(",")[1]));
                obj.put("viewsub", -Float.parseFloat(timesub7day.get(i).split(",")[1]));
                obj.put("maxview", maxview);
                obj.put("maxsubview", maxsubview);
                obj.put("count_view", count_view);
                obj.put("count_viewsub", count_viewsub);
                obj.put("count_order", count_order);

                jsonArray.add(obj);
            }
            resp.put("view7day", jsonArray);

            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
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
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "balanceNow",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> balanceNow(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if(checktoken ==0){
            resp.put("status",false);
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        Float all=balanceRepository.getAllBalanceNow();
        resp.put("balance",all.toString()+"$|"+tikTokFollower24hRepository.check_Follower_24h()+"F");
        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);


    }


    @GetMapping(path = "fluctuationsNow",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> fluctuationsNow(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if(checktoken ==0){
            resp.put("status",false);
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        Balance balanceHistory=balanceRepository.getfluctuationsNow();
        if(balanceHistory==null){
            resp.put("noti","");
        }else{
            Instant instant = Instant.ofEpochMilli(balanceHistory.getAdd_time());
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
            LocalDateTime newDateTime = dateTime.plusHours(7);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
            String formattedDateTime = newDateTime.format(formatter);

            String value1=balanceHistory.getUser().replace("@gmail.com","")+" ▪\uFE0F "+balanceHistory.getService()+" ▪\uFE0F "+(-balanceHistory.getBalance())+"$";
            String noti= "⏳ "+formattedDateTime+" ▪\uFE0F "+value1;


            resp.put("noti",noti);
        }

        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);


    }


    @GetMapping(path = "fluctuationsMobile",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> fluctuationsMobile(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if(checktoken ==0){
            resp.put("status",false);
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        Balance balanceHistory=balanceRepository.getfluctuationsNow();
        if(balanceHistory==null){
            resp.put("noti","");
        }else{
            Instant instant = Instant.ofEpochMilli(balanceHistory.getAdd_time());
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
            LocalDateTime newDateTime = dateTime.plusHours(7);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
            String formattedDateTime = newDateTime.format(formatter);
            resp.put("noti","⏳ "+ formattedDateTime+" ⏩ "+balanceHistory.getBalance()+"$");
        }

        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);


    }

    @GetMapping(path = "fluctuations5M",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> fluctuations5M(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if(checktoken ==0){
            resp.put("status",false);
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        Float balances=balanceRepository.getfluctuations5M();
        if(balances==null){
            resp.put("price",0);
        }else{
            resp.put("price",balances);
        }

        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);


    }


}

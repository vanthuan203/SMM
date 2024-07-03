package com.nts.awspremium.controller;

import com.nts.awspremium.model.BalanceShow;
import com.nts.awspremium.model.LogError;
import com.nts.awspremium.repositories.BalanceRepository;
import com.nts.awspremium.repositories.LogErrorRepository;
import com.nts.awspremium.repositories.ServiceRepository;
import com.nts.awspremium.repositories.UserRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
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
}

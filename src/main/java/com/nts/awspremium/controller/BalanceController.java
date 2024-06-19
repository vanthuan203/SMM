package com.nts.awspremium.controller;

import com.nts.awspremium.model.BalanceShow;
import com.nts.awspremium.repositories.BalanceRepository;
import com.nts.awspremium.repositories.ServiceRepository;
import com.nts.awspremium.repositories.UserRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/balance")
public class BalanceController {
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private UserRepository userRepository;
    @GetMapping(path = "get_List_Balance",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get_List_Service(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String user){
        JSONObject resp = new JSONObject();
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if(checktoken ==0){
            resp.put("status",false);
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
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

    }
}

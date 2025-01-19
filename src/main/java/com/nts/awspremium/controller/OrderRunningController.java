package com.nts.awspremium.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nts.awspremium.GoogleApi;
import com.nts.awspremium.TikTokApi;
import com.nts.awspremium.model.*;
import com.nts.awspremium.model_system.MySQLCheck;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/order_running")
public class OrderRunningController {

    @Autowired
    private OrderRunningRepository orderRunningRepository;
    @Autowired
    private OrderHistoryRepository orderHistoryRepository;
    @Autowired
    private GoogleKeyRepository googleKeyRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    @Autowired
    private DataCommentRepository dataCommentRepository;

    @Autowired
    private DataFollowerTiktokRepository dataFollowerTiktokRepository;
    String get_key(){
        try{
            GoogleKey key = googleKeyRepository.get_Google_Key();
            key.setGet_count(key.getGet_count() + 1);
            googleKeyRepository.save(key);
            return key.getKey_id();

        }catch (Exception e){
            return null;
        }
    }

    @GetMapping(path = "get_Order_Running", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get_Order_Running(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String user) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        User users=userRepository.find_User_By_Token(Authorization.trim());
        if(Authorization.length()==0|| user==null){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try {
            List<OrderRunningShow> orderRunnings;
            if (user.length() == 0) {
                orderRunnings = orderRunningRepository.get_Order_Running();

            } else {
                orderRunnings = orderRunningRepository.get_Order_Running(user.trim());
            }

            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < orderRunnings.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("order_id", orderRunnings.get(i).getOrder_id());
                obj.put("order_key", orderRunnings.get(i).getOrder_key());
                obj.put("order_link", orderRunnings.get(i).getOrder_link());
                obj.put("total_thread", orderRunnings.get(i).getTotal_thread());
                obj.put("thread", orderRunnings.get(i).getThread());
                obj.put("insert_time", orderRunnings.get(i).getInsert_time());
                obj.put("start_time", orderRunnings.get(i).getStart_time());
                obj.put("update_time", orderRunnings.get(i).getUpdate_time());
                obj.put("update_current_time", orderRunnings.get(i).getUpdate_current_time());
                obj.put("start_count", orderRunnings.get(i).getStart_count());
                obj.put("check_count", orderRunnings.get(i).getCheck_count());
                obj.put("current_count", orderRunnings.get(i).getCurrent_count());
                obj.put("total", orderRunnings.get(i).getTotal());
                obj.put("quantity", orderRunnings.get(i).getQuantity());
                obj.put("note", orderRunnings.get(i).getNote());
                obj.put("service_id", orderRunnings.get(i).getService_id());
                obj.put("username", orderRunnings.get(i).getUsername());
                obj.put("charge", orderRunnings.get(i).getCharge());
                obj.put("task", orderRunnings.get(i).getTask());
                obj.put("platform", orderRunnings.get(i).getPlatform());
                obj.put("bonus", orderRunnings.get(i).getBonus());
                obj.put("mode", orderRunnings.get(i).getMode());
                obj.put("priority", orderRunnings.get(i).getPriority());
                jsonArray.add(obj);
            }

            resp.put("total", orderRunnings.size());
            resp.put("order_running", jsonArray);
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

            resp.put("status", false);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(path = "update_Priority", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> update_Priority(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String order_id) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        User users=userRepository.find_User_By_Token(Authorization.trim());
        if(Authorization.length()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try {
            JSONArray jsonArray = new JSONArray();
            String[] order_Arr = order_id.split(",");
            for(int i=0;i<order_Arr.length;i++){
                OrderRunning orderRunning=orderRunningRepository.get_Order_By_Id(Long.parseLong(order_Arr[i]));
                if(orderRunning==null){
                    continue;
                }
                orderRunning.setPriority(1);
                orderRunningRepository.save(orderRunning);
                OrderRunningShow orderRunningShow=orderRunningRepository.get_Order_Running_By_OrderId(Long.parseLong(order_Arr[i]));
                JSONObject obj = new JSONObject();
                obj.put("order_id", orderRunningShow.getOrder_id());
                obj.put("order_key", orderRunningShow.getOrder_key());
                obj.put("order_link", orderRunningShow.getOrder_link());
                obj.put("total_thread", orderRunningShow.getTotal_thread());
                obj.put("thread", orderRunningShow.getThread());
                obj.put("insert_time", orderRunningShow.getInsert_time());
                obj.put("start_time", orderRunningShow.getStart_time());
                obj.put("update_time", orderRunningShow.getUpdate_time());
                obj.put("update_current_time",orderRunningShow.getUpdate_current_time());
                obj.put("start_count", orderRunningShow.getStart_count());
                obj.put("check_count", orderRunningShow.getCheck_count());
                obj.put("current_count",orderRunningShow.getCurrent_count());
                obj.put("total",orderRunningShow.getTotal());
                obj.put("quantity",orderRunningShow.getQuantity());
                obj.put("note", orderRunningShow.getNote());
                obj.put("service_id", orderRunningShow.getService_id());
                obj.put("username", orderRunningShow.getUsername());
                obj.put("charge", orderRunningShow.getCharge());
                obj.put("task", orderRunningShow.getTask());
                obj.put("platform", orderRunningShow.getPlatform());
                obj.put("bonus",orderRunningShow.getBonus());
                obj.put("mode",orderRunningShow.getMode());
                obj.put("priority", orderRunningShow.getPriority());
                jsonArray.add(obj);
            }
            resp.put("total", order_Arr.length);
            resp.put("order_running", jsonArray);
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

            resp.put("status", false);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(path = "update_Thread", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> update_Thread(@RequestHeader(defaultValue = "") String Authorization, @RequestBody OrderRunning orderRunning_Body) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        User users=userRepository.find_User_By_Token(Authorization.trim());
        if(Authorization.length()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try {
            JSONArray jsonArray = new JSONArray();
            String[] order_Arr = orderRunning_Body.getOrder_key().split("\n");
            for(int i=0;i<order_Arr.length;i++){
                OrderRunning orderRunning=orderRunningRepository.get_Order_By_Id(Long.parseLong(order_Arr[i]));
                if(orderRunning==null){
                    continue;
                }
                orderRunning.setThread(orderRunning_Body.getThread());
                orderRunning.setPriority(orderRunning_Body.getPriority());
                orderRunningRepository.save(orderRunning);
                OrderRunningShow orderRunningShow=orderRunningRepository.get_Order_Running_By_OrderId(Long.parseLong(order_Arr[i]));
                JSONObject obj = new JSONObject();
                obj.put("order_id", orderRunningShow.getOrder_id());
                obj.put("order_key", orderRunningShow.getOrder_key());
                obj.put("order_link", orderRunningShow.getOrder_link());
                obj.put("total_thread", orderRunningShow.getTotal_thread());
                obj.put("thread", orderRunningShow.getThread());
                obj.put("insert_time", orderRunningShow.getInsert_time());
                obj.put("start_time", orderRunningShow.getStart_time());
                obj.put("update_time", orderRunningShow.getUpdate_time());
                obj.put("update_current_time",orderRunningShow.getUpdate_current_time());
                obj.put("start_count", orderRunningShow.getStart_count());
                obj.put("check_count", orderRunningShow.getCheck_count());
                obj.put("current_count",orderRunningShow.getCurrent_count());
                obj.put("total",orderRunningShow.getTotal());
                obj.put("quantity",orderRunningShow.getQuantity());
                obj.put("note", orderRunningShow.getNote());
                obj.put("service_id", orderRunningShow.getService_id());
                obj.put("username", orderRunningShow.getUsername());
                obj.put("charge", orderRunningShow.getCharge());
                obj.put("task", orderRunningShow.getTask());
                obj.put("platform", orderRunningShow.getPlatform());
                obj.put("bonus",orderRunningShow.getBonus());
                obj.put("mode",orderRunningShow.getMode());
                obj.put("priority", orderRunningShow.getPriority());
                jsonArray.add(obj);
            }
            resp.put("total", order_Arr.length);
            resp.put("order_running", jsonArray);
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

            resp.put("status", false);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(path = "update_Order_Running", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> update_Order_Running(@RequestHeader(defaultValue = "") String Authorization, @RequestBody OrderRunning orderRunning_Body) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        User users=userRepository.find_User_By_Token(Authorization.trim());
        if(Authorization.length()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try {
            JSONArray jsonArray = new JSONArray();
            for(int i=0;i<1;i++){
                OrderRunning orderRunning=orderRunningRepository.get_Order_By_Id(orderRunning_Body.getOrder_id());
                if(orderRunning==null){
                    continue;
                }
                orderRunning.setThread(orderRunning_Body.getThread());
                orderRunning.setNote(orderRunning_Body.getNote());
                orderRunning.setPriority(orderRunning_Body.getPriority());
                orderRunningRepository.save(orderRunning);
                OrderRunningShow orderRunningShow=orderRunningRepository.get_Order_Running_By_OrderId(orderRunning_Body.getOrder_id());
                JSONObject obj = new JSONObject();
                obj.put("order_id", orderRunningShow.getOrder_id());
                obj.put("order_key", orderRunningShow.getOrder_key());
                obj.put("order_link", orderRunningShow.getOrder_link());
                obj.put("total_thread", orderRunningShow.getTotal_thread());
                obj.put("thread", orderRunningShow.getThread());
                obj.put("insert_time", orderRunningShow.getInsert_time());
                obj.put("start_time", orderRunningShow.getStart_time());
                obj.put("update_time", orderRunningShow.getUpdate_time());
                obj.put("update_current_time",orderRunningShow.getUpdate_current_time());
                obj.put("start_count", orderRunningShow.getStart_count());
                obj.put("check_count", orderRunningShow.getCheck_count());
                obj.put("current_count",orderRunningShow.getCurrent_count());
                obj.put("total",orderRunningShow.getTotal());
                obj.put("quantity",orderRunningShow.getQuantity());
                obj.put("note", orderRunningShow.getNote());
                obj.put("service_id", orderRunningShow.getService_id());
                obj.put("username", orderRunningShow.getUsername());
                obj.put("charge", orderRunningShow.getCharge());
                obj.put("task", orderRunningShow.getTask());
                obj.put("platform", orderRunningShow.getPlatform());
                obj.put("bonus",orderRunningShow.getBonus());
                obj.put("mode",orderRunningShow.getMode());
                obj.put("priority", orderRunningShow.getPriority());
                jsonArray.add(obj);
            }
            resp.put("order_running", jsonArray);
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

            resp.put("status", false);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "get_Order_Pending", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get_Order_Pending(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String user) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        User users=userRepository.find_User_By_Token(Authorization.trim());
        if(Authorization.length()==0|| user==null){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try {
            List<OrderRunningShow> orderRunnings;
            if (user.length() == 0) {
                orderRunnings = orderRunningRepository.get_Order_Pending();

            } else {
                orderRunnings = orderRunningRepository.get_Order_Pending(user.trim());
            }

            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < orderRunnings.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("order_id", orderRunnings.get(i).getOrder_id());
                obj.put("order_key", orderRunnings.get(i).getOrder_key());
                obj.put("order_link", orderRunnings.get(i).getOrder_link());
                obj.put("total_thread", orderRunnings.get(i).getTotal_thread());
                obj.put("thread", orderRunnings.get(i).getThread());
                obj.put("insert_time", orderRunnings.get(i).getInsert_time());
                obj.put("start_time", orderRunnings.get(i).getStart_time());
                obj.put("update_time", orderRunnings.get(i).getUpdate_time());
                obj.put("start_count", orderRunnings.get(i).getStart_count());
                obj.put("check_count", orderRunnings.get(i).getCheck_count());
                obj.put("current_count", orderRunnings.get(i).getCurrent_count());
                obj.put("total", orderRunnings.get(i).getTotal());
                obj.put("quantity", orderRunnings.get(i).getQuantity());
                obj.put("note", orderRunnings.get(i).getNote());
                obj.put("service_id", orderRunnings.get(i).getService_id());
                obj.put("username", orderRunnings.get(i).getUsername());
                obj.put("charge", orderRunnings.get(i).getCharge());
                obj.put("task", orderRunnings.get(i).getTask());
                obj.put("platform", orderRunnings.get(i).getPlatform());
                obj.put("bonus", orderRunnings.get(i).getBonus());
                obj.put("mode", orderRunnings.get(i).getMode());
                jsonArray.add(obj);
            }

            resp.put("total", orderRunnings.size());
            resp.put("order_running", jsonArray);
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

            resp.put("status", false);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "get_Order_Refill", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get_Order_Refill(@RequestHeader(defaultValue = "") String Authorization) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        User users=userRepository.find_User_By_Token(Authorization.trim());
        if(Authorization.length()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try {
            List<OrderRunningShow> orderRunnings;
            orderRunnings = orderRunningRepository.get_Order_Running("refill@gmail.com");
            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < orderRunnings.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("order_id", orderRunnings.get(i).getOrder_id());
                obj.put("order_key", orderRunnings.get(i).getOrder_key());
                obj.put("order_link", orderRunnings.get(i).getOrder_link());
                obj.put("total_thread", orderRunnings.get(i).getTotal_thread());
                obj.put("thread", orderRunnings.get(i).getThread());
                obj.put("insert_time", orderRunnings.get(i).getInsert_time());
                obj.put("start_time", orderRunnings.get(i).getStart_time());
                obj.put("update_time", orderRunnings.get(i).getUpdate_time());
                obj.put("start_count", orderRunnings.get(i).getStart_count());
                obj.put("check_count", orderRunnings.get(i).getCheck_count());
                obj.put("current_count", orderRunnings.get(i).getCurrent_count());
                obj.put("total", orderRunnings.get(i).getTotal());
                obj.put("quantity", orderRunnings.get(i).getQuantity());
                obj.put("note", orderRunnings.get(i).getNote());
                obj.put("service_id", orderRunnings.get(i).getService_id());
                obj.put("username", orderRunnings.get(i).getUsername());
                obj.put("charge", orderRunnings.get(i).getCharge());
                obj.put("task", orderRunnings.get(i).getTask());
                obj.put("platform", orderRunnings.get(i).getPlatform());
                obj.put("bonus", orderRunnings.get(i).getBonus());
                obj.put("mode", orderRunnings.get(i).getMode());
                obj.put("priority", orderRunnings.get(i).getPriority());
                jsonArray.add(obj);
            }

            resp.put("total", orderRunnings.size());
            resp.put("order_running", jsonArray);
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

            resp.put("status", false);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "update_Total_Buff", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> update_Total_Buff() throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            List<String> totalBuff=orderRunningRepository.get_Total_Buff_Cron();
            for(int i=0;i<totalBuff.size();i++){
                try {
                    orderRunningRepository.update_Total_Buff_By_OrderId(Integer.parseInt(totalBuff.get(i).split(",")[1]),System.currentTimeMillis(),Long.parseLong(totalBuff.get(i).split(",")[0]));
                } catch (Exception e) {

                }
            }
            resp.put("status",true);
            data.put("message", "update thành công");
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

    @GetMapping(value = "update_Current_Total", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> update_Current_Total() throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            List<OrderRunning> orderRunningList=orderRunningRepository.get_Order_By_Check_Count(System.currentTimeMillis());
            for(int i=0;i<orderRunningList.size();i++){
                try {
                    if(orderRunningList.get(i).getService().getPlatform().equals("youtube")){
                        if(orderRunningList.get(i).getService().getTask().equals("subscriber")){
                            int current_Count=GoogleApi.getCountSubcriberCurrent(orderRunningList.get(i).getOrder_key());
                            if(current_Count>=0){
                                orderRunningRepository.update_Current_Count(current_Count,System.currentTimeMillis(),orderRunningList.get(i).getOrder_id());
                            }
                        }else if(orderRunningList.get(i).getService().getTask().equals("like")){
                            int current_Count=GoogleApi.getCountLikeCurrent(orderRunningList.get(i).getOrder_key());
                            if(current_Count>=0){
                                orderRunningRepository.update_Current_Count(current_Count,System.currentTimeMillis(),orderRunningList.get(i).getOrder_id());
                            }
                        }
                    }else  if(orderRunningList.get(i).getService().getPlatform().equals("tiktok")) {
                        if (orderRunningList.get(i).getService().getTask().equals("follower")) {
                            int current_Count = TikTokApi.getFollowerCount(orderRunningList.get(i).getOrder_key().replace("@", ""), 1);
                            if (current_Count >= 0) {
                                orderRunningRepository.update_Current_Count(current_Count,System.currentTimeMillis(),orderRunningList.get(i).getOrder_id());
                            }else if(current_Count==-1){
                                orderRunningRepository.update_Valid_By_OrderId(0,orderRunningList.get(i).getOrder_id());
                            }
                        } else if (orderRunningList.get(i).getService().getTask().equals("like")) {
                            int current_Count = TikTokApi.getCountLike(orderRunningList.get(i).getOrder_key());
                            if (current_Count >= 0) {
                                orderRunningRepository.update_Current_Count(current_Count,System.currentTimeMillis(),orderRunningList.get(i).getOrder_id());
                            }else if(current_Count==-1){
                                orderRunningRepository.update_Valid_By_OrderId(0,orderRunningList.get(i).getOrder_id());
                            }
                        } else if (orderRunningList.get(i).getService().getTask().equals("comment")) {
                            int current_Count = TikTokApi.getCountComment(orderRunningList.get(i).getOrder_key());
                            if (current_Count >= 0) {
                                orderRunningRepository.update_Current_Count(current_Count,System.currentTimeMillis(),orderRunningList.get(i).getOrder_id());
                            }else if(current_Count==-1){
                                orderRunningRepository.update_Valid_By_OrderId(0,orderRunningList.get(i).getOrder_id());
                            }
                        } else if (orderRunningList.get(i).getService().getTask().equals("view")) {
                            int current_Count = TikTokApi.getCountView(orderRunningList.get(i).getOrder_key());
                            if (current_Count >= 0) {
                                orderRunningRepository.update_Current_Count(current_Count,System.currentTimeMillis(),orderRunningList.get(i).getOrder_id());
                            }else if(current_Count==-1){
                                orderRunningRepository.update_Valid_By_OrderId(0,orderRunningList.get(i).getOrder_id());

                            }
                        }
                    }
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
                }
            }
            resp.put("status",true);
            data.put("message", "update thành công");
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

    @GetMapping(value = "check_Valid_OrderRunning", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> check_Valid_OrderRunning() throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            long currentTime = System.currentTimeMillis();
            long threshold = 30 * 60 * 1000;
            List<OrderRunning> orderRunningList=orderRunningRepository.get_Order_Check_Valid(currentTime,threshold);
            for(int i=0;i<orderRunningList.size();i++){
                try {
                    if(orderRunningList.get(i).getService().getPlatform().equals("tiktok")){
                        if(orderRunningList.get(i).getService().getTask().equals("follower")){
                            Integer followerCount=TikTokApi.getFollowerCount(orderRunningList.get(i).getOrder_key().trim().split("@")[1],1);
                            if(followerCount>0){
                                Integer videoCount=TikTokApi.getVideoCount(orderRunningList.get(i).getOrder_key().trim().split("@")[1],2);
                                if(videoCount==0){
                                    delete_Order_Running("api@gmail.com",orderRunningList.get(i).getOrder_id().toString(),1,"This account has no videos");
                                }else{
                                    JsonArray videoList=TikTokApi.getInfoVideoByChannel(orderRunningList.get(i).getOrder_key().trim().split("@")[1],3);
                                    if(videoList==null){
                                        orderRunningRepository.update_Valid_By_OrderId(1,orderRunningList.get(i).getOrder_id());
                                        continue;
                                    }
                                    dataFollowerTiktokRepository.delete_Data_Follower_By_OrderId(orderRunningList.get(i).getOrder_id());

                                    for (JsonElement video: videoList) {
                                        JsonObject videoObj=video.getAsJsonObject();
                                        DataFollowerTiktok dataFollowerTiktok =new DataFollowerTiktok();
                                        dataFollowerTiktok.setVideo_id(videoObj.get("video_id").getAsString());
                                        Long duration=videoObj.get("duration").getAsLong();
                                        if(duration==0){
                                            duration=15L;
                                        }
                                        dataFollowerTiktok.setDuration(duration);
                                        dataFollowerTiktok.setTiktok_id(orderRunningList.get(i).getOrder_key().trim());
                                        dataFollowerTiktok.setOrderRunning(orderRunningList.get(i));
                                        dataFollowerTiktok.setState(1);
                                        dataFollowerTiktok.setAdd_time(System.currentTimeMillis());
                                        dataFollowerTiktok.setAdd_time(System.currentTimeMillis());
                                        //dataFollowerTiktok.setVideo_title(videoObj.get("title").getAsString());
                                        dataFollowerTiktokRepository.save(dataFollowerTiktok);
                                    }
                                    orderRunningRepository.update_Valid_By_OrderId(1,orderRunningList.get(i).getOrder_id());
                                }
                            }else if(followerCount==-1){
                                delete_Order_Running("api@gmail.com",orderRunningList.get(i).getOrder_id().toString(),1,"Could not find this account");
                            }else{
                                orderRunningRepository.update_Valid_By_OrderId(1,orderRunningList.get(i).getOrder_id());
                            }

                        }else{
                            Integer likeCount=TikTokApi.getCountLike(orderRunningList.get(i).getOrder_key());
                            if(likeCount==-1){
                                delete_Order_Running("api@gmail.com",orderRunningList.get(i).getOrder_id().toString(),1,"Video is currently unavailable");
                            }else{
                                orderRunningRepository.update_Valid_By_OrderId(1,orderRunningList.get(i).getOrder_id());
                            }
                        }
                    }

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
                }
            }
            resp.put("status",true);
            data.put("message", "update thành công");
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

    @GetMapping(value = "update_Order_Running_Done_No_Check", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> update_Order_Running_Done_No_Check() throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            List<OrderRunning> orderRunningList=orderRunningRepository.get_Order_Running_Done(0);
            for (int i=0;i<orderRunningList.size();i++){
                OrderHistory orderHistory=new OrderHistory();
                orderHistory.setOrder_id(orderRunningList.get(i).getOrder_id());
                orderHistory.setOrder_key(orderRunningList.get(i).getOrder_key());
                orderHistory.setOrder_link(orderRunningList.get(i).getOrder_link());
                orderHistory.setVideo_title(orderRunningList.get(i).getVideo_title());
                orderHistory.setChannel_id(orderRunningList.get(i).getChannel_id());
                orderHistory.setComment_list(orderRunningList.get(i).getComment_list());
                orderHistory.setKeyword_list(orderRunningList.get(i).getKeyword_list());
                orderHistory.setVideo_list(orderRunningList.get(i).getVideo_list());
                orderHistory.setStart_count(orderRunningList.get(i).getStart_count());
                orderHistory.setInsert_time(orderRunningList.get(i).getInsert_time());
                orderHistory.setStart_time(orderRunningList.get(i).getStart_time());
                orderHistory.setEnd_time(System.currentTimeMillis());
                orderHistory.setDuration(orderRunningList.get(i).getDuration());
                orderHistory.setDuration(orderRunningList.get(i).getDuration());
                orderHistory.setService(orderRunningList.get(i).getService());
                orderHistory.setNote(orderRunningList.get(i).getNote());
                orderHistory.setUser(orderRunningList.get(i).getUser());
                orderHistory.setQuantity(orderRunningList.get(i).getQuantity());
                orderHistory.setTotal(orderRunningList.get(i).getTotal());
                orderHistory.setTime_total(orderRunningList.get(i).getTime_total());
                orderHistory.setUpdate_time(orderRunningList.get(i).getUpdate_time());
                orderHistory.setCharge(orderRunningList.get(i).getCharge());
                orderHistory.setCurrent_count(orderRunningList.get(i).getCurrent_count());
                orderHistory.setRefund(0);
                orderHistory.setCancel(0);
                orderHistory.setRefund_time(0L);
                orderHistory.setRefill_time(0L);
                orderHistory.setRefill(0);
                orderHistory.setOrder_refill(orderRunningList.get(i).getOrder_refill());
                orderHistory.setUpdate_current_time(orderRunningList.get(i).getUpdate_current_time());
                try {
                    orderHistoryRepository.save(orderHistory);
                    orderRunningRepository.delete_Order_Running_By_OrderId(orderRunningList.get(i).getOrder_id());
                } catch (Exception e) {
                }
            }
            resp.put("status",true);
            data.put("message", "update thành công");
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

    @DeleteMapping(value = "delete_Order_Running", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> delete_Order_Running(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String order_id, @RequestParam(defaultValue = "1") Integer cancel,@RequestParam(defaultValue = "") String note) throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        Integer checktoken = userRepository.check_User_By_Token(Authorization);
        if(checktoken ==0){
            resp.put("status",false);
            data.put("message", "Token expired");
            resp.put("data",data);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
        if (order_id.length() == 0) {
            resp.put("status",false);
            data.put("message", "order_id không được để trống");
            resp.put("data",data);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
        try{
            String[] order_Arr = order_id.split(",");
            for (int i=0;i<order_Arr.length;i++){
                OrderRunning orderRunning=orderRunningRepository.get_Order_By_Id(Long.parseLong(order_Arr[i]));
                if(orderRunning==null){
                    continue;
                }
                Service service=serviceRepository.get_Service_By_ServiceId(orderRunning.getService().getService_id());
                OrderHistory orderHistory=new OrderHistory();
                orderHistory.setOrder_id(orderRunning.getOrder_id());
                orderHistory.setOrder_key(orderRunning.getOrder_key());
                orderHistory.setOrder_link(orderRunning.getOrder_link());
                orderHistory.setVideo_title(orderRunning.getVideo_title());
                orderHistory.setChannel_id(orderRunning.getChannel_id());
                orderHistory.setComment_list(orderRunning.getComment_list());
                orderHistory.setKeyword_list(orderRunning.getKeyword_list());
                orderHistory.setVideo_list(orderRunning.getVideo_list());
                orderHistory.setStart_count(orderRunning.getStart_count());
                orderHistory.setInsert_time(orderRunning.getInsert_time());
                orderHistory.setStart_time(orderRunning.getStart_time());
                orderHistory.setDuration(orderRunning.getDuration());
                orderHistory.setDuration(orderRunning.getDuration());
                orderHistory.setService(orderRunning.getService());
                if(note.trim().length()>0){
                    orderHistory.setNote(orderRunning.getNote()+" | "+note.trim());
                }else{
                    orderHistory.setNote(orderRunning.getNote());
                }
                orderHistory.setUser(orderRunning.getUser());
                orderHistory.setQuantity(orderRunning.getQuantity());
                orderHistory.setTotal(orderRunning.getTotal());
                orderHistory.setTime_total(orderRunning.getTime_total());
                orderHistory.setUpdate_time(orderRunning.getUpdate_time());
                orderHistory.setCurrent_count(orderRunning.getCurrent_count());
                orderHistory.setRefund(0);
                orderHistory.setRefund_time(0L);
                orderHistory.setRefill_time(0L);
                orderHistory.setRefill(0);
                orderHistory.setUpdate_current_time(orderRunning.getUpdate_current_time());
                orderHistory.setOrder_refill(orderRunning.getOrder_refill());
                if(service.getCheck_count()==1){
                    int realTime=orderRunning.getCurrent_count()<=0?orderRunning.getTotal():orderRunning.getCurrent_count()-orderRunning.getStart_count();
                    if(realTime>orderRunning.getTotal()){
                        realTime=orderRunning.getTotal();
                    }else if(realTime<0){
                        realTime=0;
                    }
                    orderHistory.setTotal(realTime);
                    if (cancel == 1) {
                        User user=userRepository.find_User_By_Token(Authorization.trim());
                        int remains = orderRunning.getQuantity() - (realTime > orderRunning.getQuantity() ? orderRunning.getQuantity() : realTime);
                        //System.out.println(videoBuffh.get(0).getViewtotal() > videoBuffh.get(0).getVieworder() ? videoBuffh.get(0).getVieworder() : videoBuffh.get(0).getViewtotal());
                        float price_Refund = (Math.round(((remains / (float) orderRunning.getQuantity()) * orderRunning.getCharge()) * 1000000f) / 1000000f);
                        float price_Buff = (orderRunning.getCharge() - price_Refund);

                        orderHistory.setCharge(Math.round(price_Buff * 1000000f) / 1000000f);
                        if (realTime == 0) {
                            orderHistory.setCancel(1);
                        } else if (remains<=0) {
                            orderHistory.setCancel(0);
                        } else {
                            orderHistory.setCancel(2);
                        }
                        //hoàn tiền & add thong báo số dư
                        if (remains > 0) {
                            Float balance_Update=balanceRepository.update_Balance(price_Refund,orderRunning.getUser().getUsername().trim());
                            Balance balance = new Balance();
                            balance.setUser(orderRunning.getUser().getUsername().trim());
                            balance.setAdd_time(System.currentTimeMillis());
                            balance.setTotal_blance(balance_Update);
                            balance.setBalance(price_Refund);
                            balance.setService(orderRunning.getService().getService_id());
                            balance.setNote("Refund "+ (remains)+" " + orderRunning.getService().getTask() + " for Id " + orderRunning.getOrder_id());
                            balanceRepository.save(balance);
                        }
                    }else{
                        orderHistory.setCancel(0);
                        orderHistory.setCharge(orderRunning.getCharge());
                    }
                }else{
                    if (cancel == 1) {
                        User user=userRepository.find_User_By_Token(Authorization.trim());
                        int remains = orderRunning.getQuantity() - (orderRunning.getTotal() > orderRunning.getQuantity() ? orderRunning.getQuantity() : orderRunning.getTotal());
                        //System.out.println(videoBuffh.get(0).getViewtotal() > videoBuffh.get(0).getVieworder() ? videoBuffh.get(0).getVieworder() : videoBuffh.get(0).getViewtotal());
                        float price_Refund = (Math.round(((remains / (float) orderRunning.getQuantity()) * orderRunning.getCharge()) * 1000000f) / 1000000f);
                        float price_Buff = (orderRunning.getCharge() - price_Refund);

                        orderHistory.setCharge(Math.round(price_Buff * 1000000f) / 1000000f);
                        if (orderRunning.getTotal() == 0) {
                            orderHistory.setCancel(1);
                        } else if (remains<=0) {
                            orderHistory.setCancel(0);
                        } else {
                            orderHistory.setCancel(2);
                        }
                        //hoàn tiền & add thong báo số dư
                        if (remains > 0) {
                            Float balance_Update=balanceRepository.update_Balance(price_Refund,orderRunning.getUser().getUsername().trim());
                            Balance balance = new Balance();
                            balance.setUser(orderRunning.getUser().getUsername().trim());
                            balance.setAdd_time(System.currentTimeMillis());
                            balance.setTotal_blance(balance_Update);
                            balance.setBalance(price_Refund);
                            balance.setService(orderRunning.getService().getService_id());
                            balance.setNote("Refund "+ (remains)+" " + orderRunning.getService().getTask() + " for Id " + orderRunning.getOrder_id());
                            balanceRepository.save(balance);
                        }
                    }else{
                        orderHistory.setCancel(0);
                        orderHistory.setCharge(orderRunning.getCharge());
                    }
                }

                try {
                    orderHistory.setEnd_time(System.currentTimeMillis());
                    orderHistoryRepository.save(orderHistory);
                    orderRunningRepository.delete_Order_Running_By_OrderId(orderRunning.getOrder_id());
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
                }
            }
            resp.put("order_running", "");
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
    @GetMapping(value = "update_Order_Running_Done_Check", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> update_Order_Running_Done_Check() throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            List<OrderRunning> orderRunningList=orderRunningRepository.get_Order_Running_Done(1);
            for (int i=0;i<orderRunningList.size();i++){
                int count_check=0;
                String key = get_key();
                if(orderRunningList.get(i).getService().getPlatform().equals("youtube")){ ///////________YOUTUBE_______//////
                    if(orderRunningList.get(i).getService().getTask().equals("like")){
                        int count=GoogleApi.getCountLike(orderRunningList.get(i).getOrder_key(),key.trim());
                        if(count==-2)
                        {
                            continue;
                        }else if(count>=0) {
                            if(count-orderRunningList.get(i).getStart_count()<orderRunningList.get(i).getQuantity()+(orderRunningList.get(i).getService().getBonus()/100F)*orderRunningList.get(i).getQuantity()){
                                continue;
                            }
                        }
                        count_check=count;
                    }else if(orderRunningList.get(i).getService().getTask().equals("view")){
                        int count=GoogleApi.getCountView(orderRunningList.get(i).getOrder_key(),key.trim());
                        if(count==-2)
                        {
                            continue;
                        }else if(count>=0) {
                            if(count-orderRunningList.get(i).getStart_count()<orderRunningList.get(i).getQuantity()+(orderRunningList.get(i).getService().getBonus()/100F)*orderRunningList.get(i).getQuantity()){
                                continue;
                            }
                        }
                        count_check=count;
                    }else if(orderRunningList.get(i).getService().getTask().equals("subscriber")){
                        int count=GoogleApi.getCountSubcriberCurrent(orderRunningList.get(i).getOrder_key());
                        if(count==-2)
                        {
                            continue;
                        }else if(count>=0) {
                            if(count-orderRunningList.get(i).getStart_count()<orderRunningList.get(i).getQuantity()+(orderRunningList.get(i).getService().getBonus()/100F)*orderRunningList.get(i).getQuantity()){
                                continue;
                            }
                        }
                        count_check=count;
                    }
                }else if(orderRunningList.get(i).getService().getPlatform().equals("tiktok")){ ///////________TIKTOK_______//////
                    if(orderRunningList.get(i).getService().getTask().equals("follower")){
                        int count= TikTokApi.getFollowerCount(orderRunningList.get(i).getOrder_key().split("@")[1],1);
                        if(count==-2) {
                            continue;
                        }else if(count>=0){
                            if(count-orderRunningList.get(i).getStart_count()<orderRunningList.get(i).getQuantity()+(orderRunningList.get(i).getService().getBonus()/100F)*orderRunningList.get(i).getQuantity()){
                                continue;
                            }
                        }
                        count_check=count;
                    }else if(orderRunningList.get(i).getService().getTask().equals("like")){
                        int count= TikTokApi.getCountLike(orderRunningList.get(i).getOrder_key());
                        if(count==-2) {
                            continue;
                        }else if(count>=0){
                            if(count-orderRunningList.get(i).getStart_count()<orderRunningList.get(i).getQuantity()+(orderRunningList.get(i).getService().getBonus()/100F)*orderRunningList.get(i).getQuantity()){
                                continue;
                            }
                        }
                        count_check=count;
                    }else if(orderRunningList.get(i).getService().getTask().equals("comment")){
                        int count= TikTokApi.getCountComment(orderRunningList.get(i).getOrder_key());
                        if(count==-2) {
                            continue;
                        }else if(count>=0){
                            if(count-orderRunningList.get(i).getStart_count()<orderRunningList.get(i).getQuantity()+(orderRunningList.get(i).getService().getBonus()/100F)*orderRunningList.get(i).getQuantity()){
                                continue;
                            }
                        }
                        count_check=count;
                    }else if(orderRunningList.get(i).getService().getTask().equals("view")){
                        int count= TikTokApi.getCountView(orderRunningList.get(i).getOrder_key());
                        if(count==-2) {
                            continue;
                        }else if(count>=0){
                            if(count-orderRunningList.get(i).getStart_count()<orderRunningList.get(i).getQuantity()+(orderRunningList.get(i).getService().getBonus()/100F)*orderRunningList.get(i).getQuantity()){
                                continue;
                            }
                        }
                        count_check=count;
                    }
                }
                OrderHistory orderHistory=new OrderHistory();
                orderHistory.setOrder_id(orderRunningList.get(i).getOrder_id());
                orderHistory.setOrder_key(orderRunningList.get(i).getOrder_key());
                orderHistory.setOrder_link(orderRunningList.get(i).getOrder_link());
                orderHistory.setVideo_title(orderRunningList.get(i).getVideo_title());
                orderHistory.setChannel_id(orderRunningList.get(i).getChannel_id());
                orderHistory.setComment_list(orderRunningList.get(i).getComment_list());
                orderHistory.setKeyword_list(orderRunningList.get(i).getKeyword_list());
                orderHistory.setVideo_list(orderRunningList.get(i).getVideo_list());
                orderHistory.setStart_count(orderRunningList.get(i).getStart_count());
                orderHistory.setInsert_time(orderRunningList.get(i).getInsert_time());
                orderHistory.setStart_time(orderRunningList.get(i).getStart_time());
                orderHistory.setEnd_time(System.currentTimeMillis());
                orderHistory.setDuration(orderRunningList.get(i).getDuration());
                orderHistory.setDuration(orderRunningList.get(i).getDuration());
                orderHistory.setService(orderRunningList.get(i).getService());
                orderHistory.setNote(orderRunningList.get(i).getNote());
                orderHistory.setUser(orderRunningList.get(i).getUser());
                orderHistory.setQuantity(orderRunningList.get(i).getQuantity());
                orderHistory.setTotal(orderRunningList.get(i).getTotal());
                orderHistory.setTime_total(orderRunningList.get(i).getTime_total());
                orderHistory.setUpdate_time(orderRunningList.get(i).getUpdate_time());
                orderHistory.setCharge(orderRunningList.get(i).getCharge());
                orderHistory.setRefund(0);
                orderHistory.setCancel(0);
                orderHistory.setRefund_time(0L);
                orderHistory.setRefill_time(0L);
                orderHistory.setRefill(0);
                orderHistory.setCurrent_count(count_check);
                orderHistory.setUpdate_current_time(System.currentTimeMillis());
                orderHistory.setOrder_refill(orderRunningList.get(i).getOrder_refill());

                try {
                    orderHistoryRepository.save(orderHistory);
                    orderRunningRepository.delete_Order_Running_By_OrderId(orderRunningList.get(i).getOrder_id());
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

                }
            }
            resp.put("status",true);
            data.put("message", "update thành công");
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

package com.nts.awspremium.controller;

import com.google.gson.JsonObject;
import com.nts.awspremium.GoogleApi;
import com.nts.awspremium.TikTokApi;
import com.nts.awspremium.model.*;
import com.nts.awspremium.model_system.MySQLCheck;
import com.nts.awspremium.repositories.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                jsonArray.add(obj);
            }

            resp.put("total", orderRunnings.size());
            resp.put("order_running", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
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
            System.out.println(stackTraceElement.getMethodName());
            System.out.println(stackTraceElement.getLineNumber());
            System.out.println(stackTraceElement.getClassName());
            System.out.println(stackTraceElement.getFileName());
            System.out.println("Error : " + e.getMessage());
            resp.put("status", false);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping(value = "update_Current_Total", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> update_Current_Total() throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            List<OrderRunning> orderRunningList=orderRunningRepository.get_Order_By_Check_Count();
            for(int i=0;i<orderRunningList.size();i++){
                try {
                    if(orderRunningList.get(i).getService().getPlatform().equals("youtube")){
                        if(orderRunningList.get(i).getService().getTask().equals("subscriber")){
                            int current_Count=GoogleApi.getCountSubcriber(orderRunningList.get(i).getOrder_key());
                            if(current_Count>=0){
                                orderRunningList.get(i).setCurrent_count(current_Count);
                                orderRunningList.get(i).setUpdate_time(System.currentTimeMillis());
                                orderRunningRepository.save(orderRunningList.get(i));
                            }
                        }else if(orderRunningList.get(i).getService().getTask().equals("like")){
                            int current_Count=GoogleApi.getCountLikeCurrent(orderRunningList.get(i).getOrder_key());
                            if(current_Count>=0){
                                orderRunningList.get(i).setCurrent_count(current_Count);
                                orderRunningList.get(i).setUpdate_time(System.currentTimeMillis());
                                orderRunningRepository.save(orderRunningList.get(i));
                            }
                        }
                    }else  if(orderRunningList.get(i).getService().getPlatform().equals("tiktok")){
                        if(orderRunningList.get(i).getService().getTask().equals("follower")){
                            int current_Count=TikTokApi.getFollowerCount(orderRunningList.get(i).getOrder_key().replace("@",""));
                            System.out.println(current_Count);
                            if(current_Count>=0){
                                orderRunningList.get(i).setCurrent_count(current_Count);
                                orderRunningList.get(i).setUpdate_time(System.currentTimeMillis());
                                orderRunningRepository.save(orderRunningList.get(i));
                            }
                        }else if(orderRunningList.get(i).getService().getTask().equals("like")){
                            JSONObject jsonObject=TikTokApi.getInfoVideoTikTok(orderRunningList.get(i).getOrder_key());
                            if(jsonObject.get("status").equals("success")){
                                int current_Count=Integer.parseInt(jsonObject.get("likes").toString());
                                if(current_Count>=0){
                                    orderRunningList.get(i).setCurrent_count(current_Count);
                                    orderRunningList.get(i).setUpdate_time(System.currentTimeMillis());
                                    orderRunningRepository.save(orderRunningList.get(i));
                                }
                            }
                        }else if(orderRunningList.get(i).getService().getTask().equals("comment")){
                            JSONObject jsonObject=TikTokApi.getInfoVideoTikTok(orderRunningList.get(i).getOrder_key());
                            if(jsonObject.get("status").equals("success")){
                                int current_Count=Integer.parseInt(jsonObject.get("comments").toString());
                                if(current_Count>=0){
                                    orderRunningList.get(i).setCurrent_count(current_Count);
                                    orderRunningList.get(i).setUpdate_time(System.currentTimeMillis());
                                    orderRunningRepository.save(orderRunningList.get(i));
                                }
                            }
                        }else if(orderRunningList.get(i).getService().getTask().equals("view")){
                            JSONObject jsonObject=TikTokApi.getInfoVideoTikTok(orderRunningList.get(i).getOrder_key());
                            if(jsonObject.get("status").equals("success")){
                                int current_Count=Integer.parseInt(jsonObject.get("plays").toString());
                                if(current_Count>=0){
                                    orderRunningList.get(i).setCurrent_count(current_Count);
                                    orderRunningList.get(i).setUpdate_time(System.currentTimeMillis());
                                    orderRunningRepository.save(orderRunningList.get(i));
                                }
                            }
                        }
                    }
                } catch (Exception e) {

                }
            }
            resp.put("status",true);
            data.put("message", "update thành công");
            resp.put("data",data);
            return new ResponseEntity<>(resp, HttpStatus.OK);
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
            System.out.println(stackTraceElement.getMethodName());
            System.out.println(stackTraceElement.getLineNumber());
            System.out.println(stackTraceElement.getClassName());
            System.out.println(stackTraceElement.getFileName());
            System.out.println("Error : " + e.getMessage());
            resp.put("status", false);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value = "update_Order_Running_Done_Check", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> update_Order_Running_Done_Check() throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            List<OrderRunning> orderRunningList=orderRunningRepository.get_Order_Running_Done(0);
            for (int i=0;i<orderRunningList.size();i++){
                String key = get_key();
                if(orderRunningList.get(i).getService().getPlatform().equals("youtube")){ ///////________YOUTUBE_______//////
                    if(orderRunningList.get(i).getService().getTask().equals("like")){
                        int count=GoogleApi.getCountLike(orderRunningList.get(i).getOrder_key(),key.trim());
                        if(count==-2)
                        {
                            continue;
                        }else if(count>=0) {
                            if(count-orderRunningList.get(i).getStart_count()<orderRunningList.get(i).getQuantity()+orderRunningList.get(i).getService().getBonus()*orderRunningList.get(i).getQuantity()){
                                continue;
                            }
                        }
                    }else if(orderRunningList.get(i).getService().getTask().equals("view")){
                        int count=GoogleApi.getCountLike(orderRunningList.get(i).getOrder_key(),key.trim());
                        if(count==-2)
                        {
                            continue;
                        }else if(count>=0) {
                            if(count-orderRunningList.get(i).getStart_count()<orderRunningList.get(i).getQuantity()+orderRunningList.get(i).getService().getBonus()*orderRunningList.get(i).getQuantity()){
                                continue;
                            }
                        }
                    }else if(orderRunningList.get(i).getService().getTask().equals("subscriber")){
                        int count=GoogleApi.getCountSubcriber(orderRunningList.get(i).getOrder_key());
                        if(count==-2)
                        {
                            continue;
                        }else if(count>=0) {
                            if(count-orderRunningList.get(i).getStart_count()<orderRunningList.get(i).getQuantity()+orderRunningList.get(i).getService().getBonus()*orderRunningList.get(i).getQuantity()){
                                continue;
                            }
                        }
                    }
                }else if(orderRunningList.get(i).getService().getPlatform().equals("tiktok")){ ///////________TIKTOK_______//////
                    if(orderRunningList.get(i).getService().getTask().equals("follower")){
                        int count= TikTokApi.getFollowerCount(orderRunningList.get(i).getOrder_key().split("@")[1]);
                        if(count==-2) {
                            continue;
                        }else if(count>=0){
                            if(count-orderRunningList.get(i).getStart_count()<orderRunningList.get(i).getQuantity()+orderRunningList.get(i).getService().getBonus()*orderRunningList.get(i).getQuantity()){
                                continue;
                            }
                        }
                    }else if(orderRunningList.get(i).getService().getTask().equals("like")){
                        JSONObject jsonObject= TikTokApi.getInfoVideoTikTok(orderRunningList.get(i).getOrder_key().split("@")[1]);
                        if(jsonObject==null) {
                            continue;
                        }else{
                            int count=Integer.parseInt(jsonObject.get("likes").toString());
                            if(count-orderRunningList.get(i).getStart_count()<orderRunningList.get(i).getQuantity()+orderRunningList.get(i).getService().getBonus()*orderRunningList.get(i).getQuantity()){
                                continue;
                            }
                        }
                    }else if(orderRunningList.get(i).getService().getTask().equals("comment")){
                        JSONObject jsonObject= TikTokApi.getInfoVideoTikTok(orderRunningList.get(i).getOrder_key().split("@")[1]);
                        if(jsonObject==null) {
                            continue;
                        }else{
                            int count=Integer.parseInt(jsonObject.get("comments").toString());
                            if(count-orderRunningList.get(i).getStart_count()<orderRunningList.get(i).getQuantity()+orderRunningList.get(i).getService().getBonus()*orderRunningList.get(i).getQuantity()){
                                continue;
                            }
                        }
                    }else if(orderRunningList.get(i).getService().getTask().equals("view")){
                        JSONObject jsonObject= TikTokApi.getInfoVideoTikTok(orderRunningList.get(i).getOrder_key().split("@")[1]);
                        if(jsonObject==null) {
                            continue;
                        }else{
                            int count=Integer.parseInt(jsonObject.get("plays").toString());
                            if(count-orderRunningList.get(i).getStart_count()<orderRunningList.get(i).getQuantity()+orderRunningList.get(i).getService().getBonus()*orderRunningList.get(i).getQuantity()){
                                continue;
                            }
                        }
                    }
                }
                OrderHistory orderHistory=new OrderHistory();
                orderHistory.setOrder_id(orderRunningList.get(i).getOrder_id());
                orderHistory.setOrder_key(orderRunningList.get(i).getOrder_key());
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

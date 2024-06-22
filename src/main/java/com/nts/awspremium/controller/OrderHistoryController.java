package com.nts.awspremium.controller;

import com.nts.awspremium.GoogleApi;
import com.nts.awspremium.TikTokApi;
import com.nts.awspremium.model.*;
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
@RequestMapping(value = "/order_history")
public class OrderHistoryController {

    @Autowired
    private OrderRunningRepository orderRunningRepository;
    @Autowired
    private OrderHistoryRepository orderHistoryRepository;
    @Autowired
    private GoogleKeyRepository googleKeyRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BalanceRepository balanceRepository;


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

    @GetMapping(path = "get_Order_History", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get_Order_History(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String user) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        User users=userRepository.find_User_By_Token(Authorization.trim());
        if(Authorization.length()==0|| users==null){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try {
            List<OrderHistoryShow> orderHistories;
            if (user.length() == 0) {
                orderHistories = orderHistoryRepository.get_Order_History();

            } else {
                orderHistories = orderHistoryRepository.get_Order_History(user.trim());
            }
            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < orderHistories.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("order_id", orderHistories.get(i).getOrder_id());
                obj.put("order_key", orderHistories.get(i).getOrder_key());
                obj.put("insert_time", orderHistories.get(i).getInsert_time());
                obj.put("start_time", orderHistories.get(i).getStart_time());
                obj.put("end_time", orderHistories.get(i).getEnd_time());
                obj.put("cancel", orderHistories.get(i).getCancel());
                obj.put("update_time", orderHistories.get(i).getUpdate_time());
                obj.put("start_count", orderHistories.get(i).getStart_count());
                obj.put("check_count", orderHistories.get(i).getCheck_count());
                obj.put("current_count", orderHistories.get(i).getCurrent_count());
                obj.put("total", orderHistories.get(i).getTotal());
                obj.put("quantity", orderHistories.get(i).getQuantity());
                obj.put("note", orderHistories.get(i).getNote());
                obj.put("service_id", orderHistories.get(i).getService_id());
                obj.put("username", orderHistories.get(i).getUsername());
                obj.put("charge", orderHistories.get(i).getCharge());
                obj.put("task", orderHistories.get(i).getTask());
                obj.put("platform", orderHistories.get(i).getPlatform());
                jsonArray.add(obj);
            }

            resp.put("total", orderHistories.size());
            resp.put("order_history", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(path = "refund", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> refund(@RequestHeader(defaultValue = "") String Authorization,
                                  @RequestParam(defaultValue = "") String order_id,
                                  @RequestParam(defaultValue = "true") Boolean check_current,
                                  @RequestParam(defaultValue = "true") Boolean check_end_time) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        User users=userRepository.find_User_By_Token(Authorization.trim());
        if(Authorization.length()==0|| users==null){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try {
            String[] orders = order_id.split(",");
            JSONArray jsonArray = new JSONArray();
            for(int i=0;i<orders.length;i++){
                String status="✔ Không hoàn tiền";
                OrderHistory orderHistory=orderHistoryRepository.get_Order_By_Id(Long.parseLong(orders[i].trim()));
                if(orderHistory==null){
                    continue;
                }
                orderHistory.setUpdate_time(System.currentTimeMillis());
                orderHistoryRepository.save(orderHistory);
                if(orderHistory.getService().getRefund()==0){
                    status="✘ DV không bảo hành";
                }else if(orderHistory.getCancel()==1){
                    if(orderHistory.getRefund()>0){
                            status="✘ Hoàn 100% trước đó";
                        }else{
                            status="✘ Hủy trước đó";
                    }
                }else if(orderRunningRepository.get_Order_By_Order_Key_And_Task(orderHistory.getOrder_key(),orderHistory.getService().getTask())>0){
                    status="✘ Đơn mới đang chạy";
                } else if(orderHistory.getService().getRefund_time()<((System.currentTimeMillis()-orderHistory.getEnd_time())/1000/60/60/24)){
                    status="✘ Hết hạn bảo hành";
                }else if(check_end_time==true&&((System.currentTimeMillis()-orderHistory.getEnd_time())/1000/60/60)<orderHistory.getService().getCheck_end_time()){
                    status="✘ Hoàn thành <"+orderHistory.getService().getCheck_end_time()+"h";
                }else if(check_current==false){
                    Float charge_Refund=orderHistory.getCharge();
                    int quantity_Refund=orderHistory.getQuantity();
                    status="✔ Hoàn "+quantity_Refund+" "+orderHistory.getService().getTask();
                    orderHistory.setTotal(0);
                    orderHistory.setCancel(1);
                    orderHistory.setCharge(0F);
                    orderHistory.setRefund(1);
                    orderHistory.setRefund_time(System.currentTimeMillis());
                    orderHistoryRepository.save(orderHistory);
                    ///////////////////////
                    Float balance_update=balanceRepository.update_Balance(charge_Refund,orderHistory.getUser().getUsername().trim());
                    Balance balance = new Balance();
                    balance.setUser(orderHistory.getUser().getUsername().trim());
                    balance.setAdd_time(System.currentTimeMillis());
                    balance.setTotal_blance(balance_update);
                    balance.setBalance(charge_Refund);
                    balance.setService(orderHistory.getService().getService_id());
                    balance.setNote("Refund " + quantity_Refund+" " +orderHistory.getService().getTask()+ " for Id "+ orderHistory.getOrder_id());
                    balanceRepository.save(balance);
                }else{
                    int current_Count=0;
                    if(orderHistory.getService().getPlatform().equals("youtube")){
                        if(orderHistory.getService().getTask().equals("view")){
                            current_Count=GoogleApi.getCountView(orderHistory.getOrder_key(),get_key());
                        }else if(orderHistory.getService().getTask().equals("like")){
                            current_Count=GoogleApi.getCountLike(orderHistory.getOrder_key(),get_key());
                        }else if(orderHistory.getService().getTask().equals("subscriber")){
                            current_Count=GoogleApi.getCountSubcriber(orderHistory.getOrder_key(),get_key());
                        }
                        if(current_Count>=0){
                            int quantity=orderHistory.getQuantity()>orderHistory.getTotal()?orderHistory.getTotal():orderHistory.getQuantity();
                            System.out.println(quantity);
                            int count_Sum=quantity+orderHistory.getStart_count();
                            int quantity_Refund= count_Sum-current_Count ;
                            if(quantity_Refund<=0){
                                orderHistory.setCurrent_count(current_Count);
                                status="✘ Đủ "+orderHistory.getService().getTask()+" | "+current_Count+"/"+count_Sum;
                            }else{
                                if(quantity_Refund>quantity){
                                    quantity_Refund=quantity;
                                }
                                status="✔ Hoàn "+quantity_Refund+" "+orderHistory.getService().getTask();

                                Float charge_Refund= (Math.round((((quantity_Refund)/(float)quantity)*orderHistory.getCharge()) * 1000000f) / 1000000f);
                                int total=quantity-quantity_Refund;
                                orderHistory.setTotal(total<=0?0:total);
                                orderHistory.setCancel(total<=0?1:2);
                                orderHistory.setCharge(Math.round((orderHistory.getCharge()-charge_Refund) * 1000000f) / 1000000f);
                                orderHistory.setRefund(1);
                                orderHistory.setCurrent_count(current_Count);
                                orderHistory.setRefund_time(System.currentTimeMillis());
                                orderHistoryRepository.save(orderHistory);

                                Float balance_update=balanceRepository.update_Balance(charge_Refund,orderHistory.getUser().getUsername().trim());
                                Balance balance = new Balance();
                                balance.setUser(orderHistory.getUser().getUsername().trim());
                                balance.setAdd_time(System.currentTimeMillis());
                                balance.setTotal_blance(balance_update);
                                balance.setBalance(charge_Refund);
                                balance.setService(orderHistory.getService().getService_id());
                                balance.setNote("Refund " + quantity_Refund+" " +orderHistory.getService().getTask()+ " for Id "+ orderHistory.getOrder_id());
                                balanceRepository.save(balance);
                            }
                        }else{
                            status="✘ Lỗi check Current Count";
                        }
                    }else if(orderHistory.getService().getPlatform().equals("tiktok")){
                        JSONObject videoInfo;
                        if(orderHistory.getService().getTask().equals("follower")){
                            current_Count=TikTokApi.getFollowerCount(orderHistory.getOrder_key().replace("@",""));
                        }else if(orderHistory.getService().getTask().equals("like")){
                            current_Count=TikTokApi.getCountLike(orderHistory.getOrder_key());
                        }else if(orderHistory.getService().getTask().equals("view")){
                            current_Count=TikTokApi.getCountView(orderHistory.getOrder_key());
                        }else if(orderHistory.getService().getTask().equals("comment")){
                            current_Count=TikTokApi.getCountComment(orderHistory.getOrder_key());
                        }
                        if(current_Count>=0){
                            int quantity=orderHistory.getQuantity()>orderHistory.getTotal()?orderHistory.getTotal():orderHistory.getQuantity();
                            System.out.println(quantity);
                            int count_Sum=quantity+orderHistory.getStart_count();
                            int quantity_Refund= count_Sum-current_Count ;
                            if(quantity_Refund<=0){
                                orderHistory.setCurrent_count(current_Count);
                                status="✘ Đủ "+orderHistory.getService().getTask()+" | "+current_Count+"/"+count_Sum;
                            }else{
                                if(quantity_Refund>quantity){
                                    quantity_Refund=quantity;
                                }
                                status="✔ Hoàn "+quantity_Refund+" "+orderHistory.getService().getTask();

                                Float charge_Refund= (Math.round((((quantity_Refund)/(float)quantity)*orderHistory.getCharge()) * 1000000f) / 1000000f);
                                int total=quantity-quantity_Refund;
                                orderHistory.setTotal(total<=0?0:total);
                                orderHistory.setCancel(total<=0?1:2);
                                orderHistory.setCharge(Math.round((orderHistory.getCharge()-charge_Refund) * 1000000f) / 1000000f);
                                orderHistory.setRefund(1);
                                orderHistory.setCurrent_count(current_Count);
                                orderHistory.setRefund_time(System.currentTimeMillis());
                                orderHistoryRepository.save(orderHistory);

                                Float balance_update=balanceRepository.update_Balance(charge_Refund,orderHistory.getUser().getUsername().trim());
                                Balance balance = new Balance();
                                balance.setUser(orderHistory.getUser().getUsername().trim());
                                balance.setAdd_time(System.currentTimeMillis());
                                balance.setTotal_blance(balance_update);
                                balance.setBalance(charge_Refund);
                                balance.setService(orderHistory.getService().getService_id());
                                balance.setNote("Refund " + quantity_Refund+" " +orderHistory.getService().getTask()+ " for Id "+ orderHistory.getOrder_id());
                                balanceRepository.save(balance);
                            }
                        }else{
                            status="✘ Lỗi check Current Count";
                        }
                    }
                }
                OrderHistoryShow orderHistoryShow =orderHistoryRepository.get_Order_History(orderHistory.getOrder_id());
                JSONObject obj = new JSONObject();
                obj.put("order_id", orderHistoryShow.getOrder_id());
                obj.put("order_key", orderHistoryShow.getOrder_key());
                obj.put("insert_time", orderHistoryShow.getInsert_time());
                obj.put("start_time", orderHistoryShow.getStart_time());
                obj.put("end_time", orderHistoryShow.getEnd_time());
                obj.put("cancel", orderHistoryShow.getCancel());
                obj.put("update_time", orderHistoryShow.getUpdate_time());
                obj.put("start_count", orderHistoryShow.getStart_count());
                obj.put("check_count", orderHistoryShow.getCheck_count());
                obj.put("current_count", orderHistoryShow.getCurrent_count());
                obj.put("total", orderHistoryShow.getTotal());
                obj.put("quantity", orderHistoryShow.getQuantity());
                obj.put("note", orderHistoryShow.getNote());
                obj.put("service_id", orderHistoryShow.getService_id());
                obj.put("username", orderHistoryShow.getUsername());
                obj.put("charge", orderHistoryShow.getCharge());
                obj.put("task", orderHistoryShow.getTask());
                obj.put("platform", orderHistoryShow.getPlatform());
                obj.put("status", status);
                jsonArray.add(obj);

            }
            resp.put("order_history", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            System.out.println(stackTraceElement.getMethodName());
            System.out.println(stackTraceElement.getLineNumber());
            System.out.println(stackTraceElement.getClassName());
            System.out.println(stackTraceElement.getFileName());
            System.out.println("Error : " + e.getMessage());
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "refill", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> refill(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String user) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        User users=userRepository.find_User_By_Token(Authorization.trim());
        if(Authorization.length()==0|| users==null){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try {
            List<OrderHistoryShow> orderHistories;
            if (user.length() == 0) {
                orderHistories = orderHistoryRepository.get_Order_History();

            } else {
                orderHistories = orderHistoryRepository.get_Order_History(user.trim());
            }

            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < orderHistories.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("order_id", orderHistories.get(i).getOrder_id());
                obj.put("order_key", orderHistories.get(i).getOrder_key());
                obj.put("insert_time", orderHistories.get(i).getInsert_time());
                obj.put("start_time", orderHistories.get(i).getStart_time());
                obj.put("end_time", orderHistories.get(i).getEnd_time());
                obj.put("cancel", orderHistories.get(i).getCancel());
                obj.put("update_time", orderHistories.get(i).getUpdate_time());
                obj.put("start_count", orderHistories.get(i).getStart_count());
                obj.put("check_count", orderHistories.get(i).getCheck_count());
                obj.put("current_count", orderHistories.get(i).getCurrent_count());
                obj.put("total", orderHistories.get(i).getTotal());
                obj.put("quantity", orderHistories.get(i).getQuantity());
                obj.put("note", orderHistories.get(i).getNote());
                obj.put("service_id", orderHistories.get(i).getService_id());
                obj.put("username", orderHistories.get(i).getUsername());
                obj.put("charge", orderHistories.get(i).getCharge());
                obj.put("task", orderHistories.get(i).getTask());
                obj.put("platform", orderHistories.get(i).getPlatform());
                jsonArray.add(obj);
            }

            resp.put("total", orderHistories.size());
            resp.put("order_history", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "find_Order_History", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> find_Order_History(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String order_key) {
        JSONObject resp = new JSONObject();
        User user=userRepository.find_User_By_Token(Authorization.trim());
        if(Authorization.length()==0|| user==null){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try {
            List<String> ordersArrInput = new ArrayList<>();
            ordersArrInput.addAll(Arrays.asList(order_key.split(",")));
            List<OrderHistoryShow> orderHistories;
            if(user.getRole().equals("ROLE_USER")){
                orderHistories = orderHistoryRepository.get_Order_History_By_Key(ordersArrInput);
            }else{
                orderHistories = orderHistoryRepository.get_Order_History_By_Key(ordersArrInput,user.getUsername().trim());
            }
            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < orderHistories.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("order_id", orderHistories.get(i).getOrder_id());
                obj.put("order_key", orderHistories.get(i).getOrder_key());
                obj.put("insert_time", orderHistories.get(i).getInsert_time());
                obj.put("start_time", orderHistories.get(i).getStart_time());
                obj.put("end_time", orderHistories.get(i).getEnd_time());
                obj.put("cancel", orderHistories.get(i).getCancel());
                obj.put("update_time", orderHistories.get(i).getUpdate_time());
                obj.put("start_count", orderHistories.get(i).getStart_count());
                obj.put("check_count", orderHistories.get(i).getCheck_count());
                obj.put("current_count", orderHistories.get(i).getCurrent_count());
                obj.put("total", orderHistories.get(i).getTotal());
                obj.put("quantity", orderHistories.get(i).getQuantity());
                obj.put("note", orderHistories.get(i).getNote());
                obj.put("service_id", orderHistories.get(i).getService_id());
                obj.put("username", orderHistories.get(i).getUsername());
                obj.put("charge", orderHistories.get(i).getCharge());
                obj.put("task", orderHistories.get(i).getTask());
                obj.put("platform", orderHistories.get(i).getPlatform());
                jsonArray.add(obj);
            }

            resp.put("total", orderHistories.size());
            resp.put("order_history", jsonArray);
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
                            int current_Count=GoogleApi.getCountSubcriberCurrent(orderRunningList.get(i).getOrder_key());
                            if(current_Count>=0){
                                orderRunningList.get(i).setCurrent_count(current_Count);
                                orderRunningList.get(i).setUpdate_time(System.currentTimeMillis());
                                orderRunningRepository.save(orderRunningList.get(i));
                            }
                        }else if(orderRunningList.get(i).getService().getTask().equals("like")){
                            int current_Count=GoogleApi.getCountLikeCurrent(orderRunningList.get(i).getOrder_key());
                            System.out.println(current_Count);
                            if(current_Count>=0){
                                orderRunningList.get(i).setCurrent_count(current_Count);
                                orderRunningList.get(i).setUpdate_time(System.currentTimeMillis());
                                orderRunningRepository.save(orderRunningList.get(i));
                            }
                        }
                    }else  if(orderRunningList.get(i).getService().getPlatform().equals("tiktok")){
                        if(orderRunningList.get(i).getService().getTask().equals("follower")){
                            int current_Count=TikTokApi.getFollowerCount(orderRunningList.get(i).getOrder_key().replace("@",""));
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

    @DeleteMapping(value = "delete_Order_Running", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> delete_Order_Running(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String order_id, @RequestParam(defaultValue = "1") Integer cancel) throws InterruptedException {
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
                OrderHistory orderHistory=new OrderHistory();
                orderHistory.setOrder_id(orderRunning.getOrder_id());
                orderHistory.setOrder_key(orderRunning.getOrder_key());
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
                orderHistory.setNote(orderRunning.getNote());
                orderHistory.setUser(orderRunning.getUser());
                orderHistory.setQuantity(orderRunning.getQuantity());
                orderHistory.setTotal(orderRunning.getTotal());
                orderHistory.setTime_total(orderRunning.getTime_total());
                orderHistory.setUpdate_time(orderRunning.getUpdate_time());
                orderHistory.setCurrent_count(orderRunning.getCurrent_count());
                orderHistory.setRefund(0);
                orderHistory.setRefund_time(0L);
                if (cancel == 1) {
                    User user=userRepository.find_User_By_Token(Authorization.trim());
                    int remains = orderRunning.getQuantity() - (orderRunning.getTotal() > orderRunning.getQuantity() ? orderRunning.getQuantity() : orderRunning.getTotal());
                    //System.out.println(videoBuffh.get(0).getViewtotal() > videoBuffh.get(0).getVieworder() ? videoBuffh.get(0).getVieworder() : videoBuffh.get(0).getViewtotal());
                    float price_Refund = (remains / (float) orderRunning.getQuantity()) * orderRunning.getCharge();
                    float price_Buff = (orderRunning.getCharge() - price_Refund);
                    orderHistory.setCharge(price_Buff);
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
                        balance.setUser(user.getUsername().trim());
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
                try {
                    orderHistory.setEnd_time(System.currentTimeMillis());
                    orderHistoryRepository.save(orderHistory);
                    orderRunningRepository.delete_Order_Running_By_OrderId(orderRunning.getOrder_id());
                } catch (Exception e) {
                }
            }
            resp.put("order_running", "");
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
                        int count=GoogleApi.getCountSubcriberCurrent(orderRunningList.get(i).getOrder_key());
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

package com.nts.awspremium.controller;

import com.nts.awspremium.GoogleApi;
import com.nts.awspremium.TikTokApi;
import com.nts.awspremium.model.*;
import com.nts.awspremium.repositories.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
    @Autowired
    private LogErrorRepository logErrorRepository;
    @Autowired
    private ApiController apiController;

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
                obj.put("order_link", orderHistories.get(i).getOrder_link());
                obj.put("insert_time", orderHistories.get(i).getInsert_time());
                obj.put("start_time", orderHistories.get(i).getStart_time());
                obj.put("end_time", orderHistories.get(i).getEnd_time());
                obj.put("cancel", orderHistories.get(i).getCancel());
                obj.put("update_time", orderHistories.get(i).getUpdate_time());
                obj.put("update_current_time", orderHistories.get(i).getUpdate_current_time());
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
                obj.put("bonus", orderHistories.get(i).getBonus());
                obj.put("mode", orderHistories.get(i).getMode());
                obj.put("refund", orderHistories.get(i).getRefund());
                obj.put("refund_time", orderHistories.get(i).getRefund_time());
                obj.put("refill", orderHistories.get(i).getRefill());
                obj.put("refill_time", orderHistories.get(i).getRefill_time());
                jsonArray.add(obj);
            }

            resp.put("total", orderHistories.size());
            resp.put("order_history", jsonArray);
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
                }else if(orderRunningRepository.get_Order_Refill_By_Order_Key_And_Task(orderHistory.getOrder_key(),orderHistory.getService().getTask())>0){
                    status="✘ Đơn đang bảo hành";
                }else if(orderRunningRepository.get_Order_By_Order_Key_And_Task(orderHistory.getOrder_key(),orderHistory.getService().getTask())>0){
                    status="✘ Đơn mới đang chạy";
                } else if(orderHistory.getService().getRefund_time()<((System.currentTimeMillis()-orderHistory.getEnd_time())/1000/60/60/24)){
                    status="✘ Hết hạn bảo hành";
                }else if(check_end_time==true&&((System.currentTimeMillis()-orderHistory.getEnd_time())/1000/60/60)<orderHistory.getService().getCheck_end_time()){
                    status="✘ Hoàn thành <"+orderHistory.getService().getCheck_end_time()+"h";
                }else if((System.currentTimeMillis()-orderHistoryRepository.get_End_Time_By_Order_Key(orderHistory.getOrder_id()))/1000/60/60<orderHistory.getService().getCheck_end_time()){
                    status="✘ Đơn mới hoàn thành <"+orderHistory.getService().getCheck_end_time()+"h";
                }else if((System.currentTimeMillis()-orderHistoryRepository.get_End_Time_By_Order_Refill(orderHistory.getOrder_id()))/1000/60/60<orderHistory.getService().getCheck_end_time()){
                    status="✘ Hoàn thành BH <"+orderHistory.getService().getCheck_end_time()+"h";
                }else if(orderHistory.getUser().getUsername().equals("refill@gmail.com")){
                    status="✘ Đơn bảo hành";
                }else if(check_current==false){
                    Float charge_Refund=orderHistory.getCharge();
                    int quantity_Refund=orderHistory.getQuantity();
                    status="✔ Hoàn "+quantity_Refund+" "+orderHistory.getService().getTask();
                    orderHistory.setTotal(0);
                    orderHistory.setCancel(1);
                    orderHistory.setCharge(0F);
                    orderHistory.setRefund(orderHistory.getRefund()+1);
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
                            current_Count=GoogleApi.getCountSubcriberCurrent(orderHistory.getOrder_key());
                        }
                    }else if(orderHistory.getService().getPlatform().equals("tiktok")){
                        JSONObject videoInfo;
                        if(orderHistory.getService().getTask().equals("follower")){
                            current_Count=TikTokApi.getFollowerCount(orderHistory.getOrder_key().replace("@",""),2);
                        }else if(orderHistory.getService().getTask().equals("like")){
                            current_Count=TikTokApi.getCountLike(orderHistory.getOrder_key());
                        }else if(orderHistory.getService().getTask().equals("view")){
                            current_Count=TikTokApi.getCountView(orderHistory.getOrder_key());
                        }else if(orderHistory.getService().getTask().equals("comment")){
                            current_Count=TikTokApi.getCountComment(orderHistory.getOrder_key());
                        }
                    }
                    if(current_Count>=0){
                        int quantity=orderHistory.getQuantity()>orderHistory.getTotal()?orderHistory.getTotal():orderHistory.getQuantity();
                        int count_Sum=quantity+orderHistory.getStart_count();
                        int quantity_Refund= count_Sum-current_Count ;
                        if(quantity_Refund<=0){
                            orderHistory.setCurrent_count(current_Count);
                            orderHistory.setUpdate_current_time(System.currentTimeMillis());
                            orderHistoryRepository.save(orderHistory);
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
                            orderHistory.setRefund(orderHistory.getRefund()+1);
                            orderHistory.setCurrent_count(current_Count);
                            orderHistory.setUpdate_current_time(System.currentTimeMillis());
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
                OrderHistoryShow orderHistoryShow =orderHistoryRepository.get_Order_History(orderHistory.getOrder_id());
                JSONObject obj = new JSONObject();
                obj.put("order_id", orderHistoryShow.getOrder_id());
                obj.put("order_key", orderHistoryShow.getOrder_key());
                obj.put("insert_time", orderHistoryShow.getInsert_time());
                obj.put("start_time", orderHistoryShow.getStart_time());
                obj.put("end_time", orderHistoryShow.getEnd_time());
                obj.put("cancel", orderHistoryShow.getCancel());
                obj.put("update_time", orderHistoryShow.getUpdate_time());
                obj.put("update_current_time", orderHistoryShow.getUpdate_current_time());
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
                obj.put("bonus", orderHistoryShow.getBonus());
                obj.put("mode", orderHistoryShow.getMode());
                obj.put("refund", orderHistoryShow.getRefund());
                obj.put("refund_time", orderHistoryShow.getRefund_time());
                obj.put("refill", orderHistoryShow.getRefill());
                obj.put("refill_time", orderHistoryShow.getRefill_time());
                obj.put("status", status);
                jsonArray.add(obj);

            }
            resp.put("order_history", jsonArray);
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

    @GetMapping(path = "checkCount", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> checkCount() {
        JSONObject resp = new JSONObject();
        try {
            String list_Order_Key=orderHistoryRepository.get_List_OrderKey_CheckCount("youtube","view");
            if(list_Order_Key==null){
                resp.put("status", true);
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            List<String> notValid = Arrays.asList(list_Order_Key.split(","));
            OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

            Request request1 = null;
            GoogleKey key = googleKeyRepository.get_Google_Key();
            request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=" + key.getKey_id().trim() + "&fields=items(id,statistics(viewCount))&part=statistics&id=" + list_Order_Key).get().build();
            key.setGet_count(key.getGet_count() + 1);
            googleKeyRepository.save(key);
            Response response1 = client1.newCall(request1).execute();

            String resultJson1 = response1.body().string();

            Object obj1 = new JSONParser().parse(resultJson1);

            JSONObject jsonObject1 = (JSONObject) obj1;
            JSONArray items = (JSONArray) jsonObject1.get("items");
            JSONArray jsonArray = new JSONArray();
            Iterator k = items.iterator();

            while (k.hasNext()) {
                try {
                    JSONObject video = (JSONObject) k.next();
                    JSONObject obj = new JSONObject();
                    JSONObject statistics = (JSONObject) video.get("statistics");
                    orderHistoryRepository.update_Order_CheckCount(Integer.parseInt(statistics.get("viewCount").toString()),System.currentTimeMillis(), video.get("id").toString(),"youtube","view");
                    notValid.remove(video.get("id").toString());
                } catch (Exception e) {
                    continue;
                }

            }
            orderHistoryRepository.update_Order_NotValid(System.currentTimeMillis(),notValid,"youtube","view");
            resp.put("status", true);
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


    @GetMapping(path = "refill", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> refill(@RequestHeader(defaultValue = "") String Authorization,
                                  @RequestParam(defaultValue = "") String order_id) {
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
                }else if(orderRunningRepository.get_Order_Refill_By_Order_Key_And_Task(orderHistory.getOrder_key(),orderHistory.getService().getTask())>0){
                    status="✘ Đơn đang bảo hành";
                }else if(orderRunningRepository.get_Order_By_Order_Key_And_Task(orderHistory.getOrder_key(),orderHistory.getService().getTask())>0){
                    status="✘ Đơn mới đang chạy";
                } else if(orderHistory.getService().getRefund_time()<((System.currentTimeMillis()-orderHistory.getEnd_time())/1000/60/60/24)){
                    status="✘ Hết hạn bảo hành";
                }else if((System.currentTimeMillis()-orderHistory.getEnd_time())/1000/60/60<orderHistory.getService().getCheck_end_time()){
                    status="✘ Hoàn thành <"+orderHistory.getService().getCheck_end_time()+"h";
                }else if((System.currentTimeMillis()-orderHistoryRepository.get_End_Time_By_Order_Key(orderHistory.getOrder_id()))/1000/60/60<orderHistory.getService().getCheck_end_time()){
                    status="✘ Đơn mới hoàn thành <"+orderHistory.getService().getCheck_end_time()+"h";
                }else if((System.currentTimeMillis()-orderHistoryRepository.get_End_Time_By_Order_Refill(orderHistory.getOrder_id()))/1000/60/60<orderHistory.getService().getCheck_end_time()){
                    status="✘ Hoàn thành BH <"+orderHistory.getService().getCheck_end_time()+"h";
                }else if(orderHistory.getUser().getUsername().equals("refill@gmail.com")){
                    status="✘ Đơn bảo hành";
                }else{
                    int current_Count=0;
                    if(orderHistory.getService().getPlatform().equals("youtube")){
                        if(orderHistory.getService().getTask().equals("view")){
                            current_Count=GoogleApi.getCountView(orderHistory.getOrder_key(),get_key());
                        }else if(orderHistory.getService().getTask().equals("like")){
                            current_Count=GoogleApi.getCountLike(orderHistory.getOrder_key(),get_key());
                        }else if(orderHistory.getService().getTask().equals("subscriber")){
                            current_Count=GoogleApi.getCountSubcriberCurrent(orderHistory.getOrder_key());
                        }
                    }else if(orderHistory.getService().getPlatform().equals("tiktok")){
                        if(orderHistory.getService().getTask().equals("follower")){
                            current_Count=TikTokApi.getFollowerCount(orderHistory.getOrder_key().replace("@",""),2);
                        }else if(orderHistory.getService().getTask().equals("like")){
                            current_Count=TikTokApi.getCountLike(orderHistory.getOrder_key());
                        }else if(orderHistory.getService().getTask().equals("view")){
                            current_Count=TikTokApi.getCountView(orderHistory.getOrder_key());
                        }else if(orderHistory.getService().getTask().equals("comment")){
                            current_Count=TikTokApi.getCountComment(orderHistory.getOrder_key());
                        }
                    }
                    if(current_Count>=0){
                        int quantity=orderHistory.getQuantity()>orderHistory.getTotal()?orderHistory.getTotal():orderHistory.getQuantity();
                        int count_Sum=quantity+orderHistory.getStart_count();
                        int quantity_Refund= count_Sum-current_Count ;
                        if(quantity_Refund<=0){
                            orderHistory.setCurrent_count(current_Count);
                            orderHistory.setUpdate_current_time(System.currentTimeMillis());
                            orderHistoryRepository.save(orderHistory);
                            status="✘ Đủ "+orderHistory.getService().getTask()+" | "+current_Count+"/"+count_Sum;
                        }else{
                            if(quantity_Refund>quantity){
                                quantity_Refund=quantity;
                            }
                            DataRequest dataRequest=new DataRequest();
                            dataRequest.setQuantity(quantity_Refund);
                            dataRequest.setLink(orderHistory.getOrder_link());
                            dataRequest.setOrder_refill(orderHistory.getOrder_id());
                            dataRequest.setNote("R"+orderHistory.getOrder_id()+"|"+users.getUsername().replace("@gmail.com",""));
                            dataRequest.setService(orderHistory.getService().getService_id());

                            JSONObject checkTrue=apiController.refill(dataRequest,"refill@gmail.com");
                            if(checkTrue.get("error")==null){
                                status="✔ BH "+quantity_Refund+" "+orderHistory.getService().getTask();
                                orderHistory.setRefill(orderHistory.getRefill()+1);
                                orderHistory.setCurrent_count(current_Count);
                                orderHistory.setUpdate_current_time(System.currentTimeMillis());
                                orderHistory.setRefill_time(System.currentTimeMillis());
                                orderHistoryRepository.save(orderHistory);
                            }else{
                                status="✘ Chờ BH "+quantity_Refund+" "+orderHistory.getService().getTask();
                            }

                        }
                    }else{
                        status="✘ Lỗi check Current Count";
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
                obj.put("update_current_time", orderHistoryShow.getUpdate_current_time());
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
                obj.put("bonus", orderHistoryShow.getBonus());
                obj.put("mode", orderHistoryShow.getMode());
                obj.put("refund", orderHistoryShow.getRefund());
                obj.put("refund_time", orderHistoryShow.getRefund_time());
                obj.put("refill", orderHistoryShow.getRefill());
                obj.put("refill_time", orderHistoryShow.getRefill_time());
                obj.put("status", status);
                jsonArray.add(obj);

            }
            resp.put("order_history", jsonArray);
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
            if(!user.getRole().equals("ROLE_USER")){
                orderHistories = orderHistoryRepository.get_Order_History_By_Key(ordersArrInput);
            }else{
                orderHistories = orderHistoryRepository.get_Order_History_By_Key(ordersArrInput,user.getUsername().trim());
            }
            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < orderHistories.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("order_id", orderHistories.get(i).getOrder_id());
                obj.put("order_key", orderHistories.get(i).getOrder_key());
                obj.put("order_link", orderHistories.get(i).getOrder_link());
                obj.put("insert_time", orderHistories.get(i).getInsert_time());
                obj.put("start_time", orderHistories.get(i).getStart_time());
                obj.put("end_time", orderHistories.get(i).getEnd_time());
                obj.put("cancel", orderHistories.get(i).getCancel());
                obj.put("update_time", orderHistories.get(i).getUpdate_time());
                obj.put("update_current_time", orderHistories.get(i).getUpdate_current_time());
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
                obj.put("bonus", orderHistories.get(i).getBonus());
                obj.put("mode", orderHistories.get(i).getMode());
                obj.put("refund", orderHistories.get(i).getRefund());
                obj.put("refund_time", orderHistories.get(i).getRefund_time());
                obj.put("refill", orderHistories.get(i).getRefill());
                obj.put("refill_time", orderHistories.get(i).getRefill_time());
                jsonArray.add(obj);
            }

            resp.put("total", orderHistories.size());
            resp.put("order_history", jsonArray);
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
}

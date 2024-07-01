package com.nts.awspremium.platform.tiktok;

import com.nts.awspremium.TikTokApi;
import com.nts.awspremium.model.*;
import com.nts.awspremium.model_system.MySQLCheck;
import com.nts.awspremium.model_system.OrderThreadCheck;
import com.nts.awspremium.repositories.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class TiktokOrder {
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private OrderRunningRepository orderRunningRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;

    public JSONObject tiktok_follower(DataRequest data, Service service, User user)  throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        try{
            String tiktok_id= TikTokApi.getTiktokId(data.getLink().trim());
            if (tiktok_id == null) {
                resp.put("error", "Cant filter tiktok_id from link");
                return resp;
            }
            if (orderRunningRepository.get_Order_By_Order_Key_And_Task(tiktok_id.trim(),service.getTask()) > 0) {
                resp.put("error", "This Tiktok_Id in process");
                return resp;
            }
            Integer follower_count=-2;
            int check=0;
            while(follower_count==-2){
                check=check+1;
                follower_count=TikTokApi.getFollowerCount(tiktok_id.trim().split("@")[1]);
                if(check>2){
                    break;
                }

            }
            if(follower_count==-2){
                resp.put("error", "This account cannot be found");
                return resp;
            }
            float priceorder = 0;
            priceorder = (data.getQuantity() / 1000F) * service.getService_rate() * ((float) (user.getRate()) / 100) * ((float) (100 - user.getDiscount()) / 100);
            if (priceorder > (float) user.getBalance()) {
                resp.put("error", "Your balance not enough");
                return resp;
            }

            OrderRunning orderRunning = new OrderRunning();
            orderRunning.setInsert_time(System.currentTimeMillis());
            orderRunning.setQuantity(data.getQuantity());
            orderRunning.setStart_count(follower_count);
            orderRunning.setTotal(0);
            orderRunning.setOrder_key(tiktok_id.trim());
            orderRunning.setUser(user);
            orderRunning.setUpdate_time(0L);
            orderRunning.setStart_time(follower_count<0?0:System.currentTimeMillis());
            orderRunning.setThread(follower_count<0?0:service.getThread());
            orderRunning.setThread_set(service.getThread());
            orderRunning.setNote("");
            orderRunning.setCharge(priceorder);
            orderRunning.setService(service);
            orderRunning.setCurrent_count(0);
            orderRunning.setValid(1);
            orderRunning.setSpeed_up(0);
            orderRunningRepository.save(orderRunning);

            Float balance_update=balanceRepository.update_Balance(-priceorder,user.getUsername().trim());
            Balance balance = new Balance();
            balance.setUser(user.getUsername().trim());
            balance.setAdd_time(System.currentTimeMillis());
            balance.setTotal_blance(balance_update);
            balance.setBalance(-priceorder);
            balance.setService(data.getService());
            balance.setNote("Order " + data.getQuantity()+" " +service.getTask()+ " for Id "+ orderRunning.getOrder_id());
            balanceRepository.save(balance);
            resp.put("order", orderRunning.getOrder_id());
            return resp;

        }catch (Exception e) {
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

            resp.put("error", "Cant insert video");
            return resp;
        }
    }
    public JSONObject tiktok_like(DataRequest data,Service service,User user)  throws IOException, ParseException{
        JSONObject resp = new JSONObject();
        try{
            String video_id= TikTokApi.getVideoId(data.getLink().trim());
            if (video_id == null) {
                resp.put("error", "Cant filter video_id from link");
                return resp;
            }
            if (orderRunningRepository.get_Order_By_Order_Key_And_Task(video_id.trim(),service.getTask()) > 0) {
                resp.put("error", "This video in process");
                return resp;
            }
            Integer like_count=TikTokApi.getCountLike(video_id.trim());
            if(like_count==-2){
                resp.put("error", "This video cannot be found");
                return resp;
            }
            float priceorder = 0;
            priceorder = (data.getQuantity() / 1000F) * service.getService_rate() * ((float) (user.getRate()) / 100) * ((float) (100 - user.getDiscount()) / 100);
            if (priceorder > (float) user.getBalance()) {
                resp.put("error", "Your balance not enough");
                return resp;
            }

            OrderRunning orderRunning = new OrderRunning();
            orderRunning.setInsert_time(System.currentTimeMillis());
            orderRunning.setQuantity(data.getQuantity());
            orderRunning.setStart_count(like_count);
            orderRunning.setTotal(0);
            orderRunning.setOrder_key(video_id.trim());
            orderRunning.setUser(user);
            orderRunning.setUpdate_time(0L);
            orderRunning.setStart_time(System.currentTimeMillis());
            orderRunning.setThread(service.getThread());
            orderRunning.setThread_set(service.getThread());
            orderRunning.setNote("");
            orderRunning.setCharge(priceorder);
            orderRunning.setService(service);
            orderRunning.setValid(1);
            orderRunning.setSpeed_up(0);
            orderRunning.setCurrent_count(0);
            orderRunningRepository.save(orderRunning);

            Float balance_update=balanceRepository.update_Balance(-priceorder,user.getUsername().trim());
            Balance balance = new Balance();
            balance.setUser(user.getUsername().trim());
            balance.setAdd_time(System.currentTimeMillis());
            balance.setTotal_blance(balance_update);
            balance.setBalance(-priceorder);
            balance.setService(data.getService());
            balance.setNote("Order " + data.getQuantity()+" " +service.getTask()+ " for Id "+ orderRunning.getOrder_id());
            balanceRepository.save(balance);
            resp.put("order", orderRunning.getOrder_id());
            return resp;

        }catch (Exception e) {
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

            resp.put("error", "Cant insert video");
            return resp;
        }
    }
    public JSONObject tiktok_comment(DataRequest data,Service service,User user)  throws IOException, ParseException{
        JSONObject resp = new JSONObject();
        try{
            String video_id= TikTokApi.getVideoId(data.getLink().trim());
            if (video_id == null) {
                resp.put("error", "Cant filter video_id from link");
                return resp;
            }
            if (orderRunningRepository.get_Order_By_Order_Key_And_Task(video_id.trim(),service.getTask()) > 0) {
                resp.put("error", "This video in process");
                return resp;
            }
            Integer comment_count=TikTokApi.getCountComment(video_id.trim());
            if(comment_count==-2){
                resp.put("error", "This video cannot be found");
                return resp;
            }
            float priceorder = 0;
            priceorder = (data.getQuantity() / 1000F) * service.getService_rate() * ((float) (user.getRate()) / 100) * ((float) (100 - user.getDiscount()) / 100);
            if (priceorder > (float) user.getBalance()) {
                resp.put("error", "Your balance not enough");
                return resp;
            }

            OrderRunning orderRunning = new OrderRunning();
            orderRunning.setInsert_time(System.currentTimeMillis());
            orderRunning.setQuantity(data.getQuantity());
            orderRunning.setComment_list(data.getList());
            orderRunning.setStart_count(comment_count);
            orderRunning.setTotal(0);
            orderRunning.setOrder_key(video_id.trim());
            orderRunning.setUser(user);
            orderRunning.setUpdate_time(0L);
            orderRunning.setStart_time(0L);
            orderRunning.setThread(service.getThread());
            orderRunning.setThread_set(0);
            orderRunning.setNote("");
            orderRunning.setCharge(priceorder);
            orderRunning.setService(service);
            orderRunning.setValid(1);
            orderRunning.setCurrent_count(0);
            orderRunningRepository.save(orderRunning);

            Float balance_update=balanceRepository.update_Balance(-priceorder,user.getUsername().trim());
            Balance balance = new Balance();
            balance.setUser(user.getUsername().trim());
            balance.setAdd_time(System.currentTimeMillis());
            balance.setTotal_blance(balance_update);
            balance.setBalance(-priceorder);
            balance.setService(data.getService());
            balance.setNote("Order " + data.getQuantity()+" " +service.getTask()+ " for Id "+ orderRunning.getOrder_id());
            balanceRepository.save(balance);
            resp.put("order", orderRunning.getOrder_id());
            return resp;

        }catch (Exception e) {
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

            resp.put("error", "Cant insert video");
            return resp;
        }
    }
    public JSONObject tiktok_view(DataRequest data,Service service,User user)  throws IOException, ParseException{
        JSONObject resp = new JSONObject();
        try{
            String video_id= TikTokApi.getVideoId(data.getLink().trim());
            if (video_id == null) {
                resp.put("error", "Cant filter video_id from link");
                return resp;
            }
            if (orderRunningRepository.get_Order_By_Order_Key_And_Task(video_id.trim(),service.getTask()) > 0) {
                resp.put("error", "This video in process");
                return resp;
            }
            Integer view_count=TikTokApi.getCountView(video_id.trim());
            if(view_count==-2){
                resp.put("error", "This video cannot be found");
                return resp;
            }
            float priceorder = 0;
            priceorder = (data.getQuantity() / 1000F) * service.getService_rate() * ((float) (user.getRate()) / 100) * ((float) (100 - user.getDiscount()) / 100);
            if (priceorder > (float) user.getBalance()) {
                resp.put("error", "Your balance not enough");
                return resp;
            }

            OrderRunning orderRunning = new OrderRunning();
            orderRunning.setInsert_time(System.currentTimeMillis());
            orderRunning.setQuantity(data.getQuantity());
            orderRunning.setStart_count(view_count);
            orderRunning.setTotal(0);
            orderRunning.setOrder_key(video_id.trim());
            orderRunning.setUser(user);
            orderRunning.setUpdate_time(0L);
            orderRunning.setStart_time(System.currentTimeMillis());
            orderRunning.setThread(service.getThread());
            orderRunning.setThread_set(0);
            orderRunning.setNote("");
            orderRunning.setCharge(priceorder);
            orderRunning.setService(service);
            orderRunning.setValid(1);
            orderRunning.setCurrent_count(0);
            orderRunningRepository.save(orderRunning);

            Float balance_update=balanceRepository.update_Balance(-priceorder,user.getUsername().trim());
            Balance balance = new Balance();
            balance.setUser(user.getUsername().trim());
            balance.setAdd_time(System.currentTimeMillis());
            balance.setTotal_blance(balance_update);
            balance.setBalance(-priceorder);
            balance.setService(data.getService());
            balance.setNote("Order " + data.getQuantity()+" " +service.getTask()+ " for Id "+ orderRunning.getOrder_id());
            balanceRepository.save(balance);
            resp.put("order", orderRunning.getOrder_id());
            return resp;

        }catch (Exception e) {
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

            resp.put("error", "Cant insert video");
            return resp;
        }
    }

}

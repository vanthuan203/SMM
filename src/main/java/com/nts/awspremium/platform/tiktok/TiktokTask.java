package com.nts.awspremium.platform.tiktok;

import com.nts.awspremium.model.*;
import com.nts.awspremium.model_system.MySQLCheck;
import com.nts.awspremium.model_system.OrderThreadCheck;
import com.nts.awspremium.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
@RestController
public class TiktokTask {
    @Autowired
    private OrderRunningRepository orderRunningRepository;
    @Autowired
    private OrderThreadCheck orderThreadCheck;
    @Autowired
    private SettingTikTokRepository settingTikTokRepository;
    @Autowired
    private TikTokFollower24hRepository tikTokFollower24hRepository;
    @Autowired
    private TikTokLike24hRepository tikTokLike24hRepository;
    @Autowired
    private TikTokAccountHistoryRepository tikTokAccountHistoryRepository;
    @Autowired
    private TikTokLikeHistoryRepository tikTokLikeHistoryRepository;
    @Autowired
    private TikTokCommentHistoryRepository tikTokCommentHistoryRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    @Autowired
    private DataCommentRepository dataCommentRepository;
    public Map<String, Object> tiktok_comment(String account_id){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran = new Random();
            SettingTiktok settingTiktok=settingTikTokRepository.get_Setting();
            String list_tiktok_video=tikTokCommentHistoryRepository.get_List_VideoId_By_AccountId(account_id.trim());
            OrderRunning orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","comment",list_tiktok_video==null?"":list_tiktok_video,orderThreadCheck.getValue());
            if (orderRunning!=null) {

                dataCommentRepository.update_Running_Comment(System.currentTimeMillis(),account_id.trim(),orderRunning.getOrder_id());
                Thread.sleep(ran.nextInt(500));
                String comment=dataCommentRepository.get_Comment_By_OrderId_And_Username(orderRunning.getOrder_id(),account_id.trim());
                if(comment!=null){
                    Service service=orderRunning.getService();
                    resp.put("status", true);
                    data.put("order_id", orderRunning.getOrder_id());
                    data.put("account_id", account_id.trim());
                    data.put("platform", service.getPlatform().toLowerCase());
                    data.put("task", service.getTask());
                    data.put("task_key",orderRunning.getOrder_key());
                    data.put("comment",comment);
                    resp.put("data",data);
                    return resp;
                }else{
                    resp.put("status", false);
                    return resp;
                }

            } else {
                resp.put("status", false);
                return resp;
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

            resp.put("status", false);
            return resp;
        }
    }
    public Map<String, Object> tiktok_follower(String account_id){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            SettingTiktok settingTiktok=settingTikTokRepository.get_Setting();
            if(tikTokFollower24hRepository.count_Follower_24h_By_Username(account_id.trim()+"%")>=settingTiktok.getMax_follower()){
                resp.put("status", false);
                return resp;
            }
            String list_tiktok_id=tikTokAccountHistoryRepository.get_List_TiktokId_By_AccountId(account_id.trim());
            OrderRunning orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","follower",list_tiktok_id==null?"":list_tiktok_id,orderThreadCheck.getValue());
            if (orderRunning!=null) {
                Service service=orderRunning.getService();
                resp.put("status", true);
                data.put("order_id", orderRunning.getOrder_id());
                data.put("account_id", account_id.trim());
                data.put("platform", service.getPlatform().toLowerCase());
                data.put("task", service.getTask());
                data.put("task_key",orderRunning.getOrder_key());
                resp.put("data",data);
                return resp;

            } else {
                resp.put("status", false);
                return resp;
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

            resp.put("status", false);
            return resp;
        }
    }
    public Map<String, Object> tiktok_like(String account_id){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            SettingTiktok settingTiktok=settingTikTokRepository.get_Setting();
            if(tikTokLike24hRepository.count_Like_24h_By_Username(account_id.trim()+"%")>=settingTiktok.getMax_like()){
                resp.put("status", false);
                return resp;
            }
            String list_videoId=tikTokLikeHistoryRepository.get_List_VideoId_By_AccountId(account_id.trim());
            OrderRunning orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","like",list_videoId==null?"":list_videoId,orderThreadCheck.getValue());
            if (orderRunning!=null) {
                Service service=orderRunning.getService();
                resp.put("status", true);
                data.put("order_id", orderRunning.getOrder_id());
                data.put("account_id", account_id.trim());
                data.put("platform", service.getPlatform().toLowerCase());
                data.put("task", service.getTask());
                data.put("task_key",orderRunning.getOrder_key());
                resp.put("data",data);
                return resp;

            } else {
                resp.put("status", false);
                return resp;
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

            resp.put("status", false);
            return resp;
        }
    }
    public Map<String, Object> tiktok_view(String account_id){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            OrderRunning orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","view","",orderThreadCheck.getValue());
            if (orderRunning!=null) {
                Service service=orderRunning.getService();
                resp.put("status", true);
                data.put("order_id", orderRunning.getOrder_id());
                data.put("account_id", account_id.trim());
                data.put("platform", service.getPlatform().toLowerCase());
                data.put("task", service.getTask());
                data.put("task_key",orderRunning.getOrder_key());
                resp.put("data",data);
                return resp;
            } else {
                resp.put("status", false);
                return resp;
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

            resp.put("status", false);
            return resp;
        }
    }
}

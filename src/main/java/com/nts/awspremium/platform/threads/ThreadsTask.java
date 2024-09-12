package com.nts.awspremium.platform.threads;

import com.nts.awspremium.model.*;
import com.nts.awspremium.model_system.OrderThreadCheck;
import com.nts.awspremium.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
@RestController
public class ThreadsTask {
    @Autowired
    private OrderRunningRepository orderRunningRepository;
    @Autowired
    private OrderThreadCheck orderThreadCheck;
    @Autowired
    private SettingThreadsRepository settingThreadsRepository;
    @Autowired
    private SettingSystemRepository settingSystemRepository;
    @Autowired
    private ThreadsFollower24hRepository threadsFollower24hRepository;
    @Autowired
    private ThreadsComment24hRepository threadsComment24hRepository;
    @Autowired
    private ThreadsView24hRepository threadsView24hRepository;
    @Autowired
    private ThreadsRepost24hRepository threadsRepost24hRepository;
    @Autowired
    private ThreadsLike24hRepository threadsLike24hRepository;
    @Autowired
    private ThreadsFollowerHistoryRepository threadsFollowerHistoryRepository;
    @Autowired
    private ThreadsLikeHistoryRepository threadsLikeHistoryRepository;
    @Autowired
    private ThreadsViewHistoryRepository threadsViewHistoryRepository;
    @Autowired
    private ThreadsRepostHistoryRepository threadsRepostHistoryRepository;
    @Autowired
    private ThreadsCommentHistoryRepository threadsCommentHistoryRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    @Autowired
    private DataCommentRepository dataCommentRepository;
    public Map<String, Object> threads_comment(String account_id,String mode){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            SettingThreads settingThreads=settingThreadsRepository.get_Setting();
            if(threadsComment24hRepository.count_Comment_24h_By_Username(account_id.trim()+"%")>=settingThreads.getMax_comment()){
                resp.put("status", false);
                return resp;
            }
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            String list_History=threadsCommentHistoryRepository.get_List_PostId_By_AccountId(account_id.trim());
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_Priority_By_Task("threads","comment",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task("threads","comment",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("threads","comment",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
            }
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
                    data.put("task_link",orderRunning.getOrder_link());
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
    public Map<String, Object> threads_follower(String account_id,String mode){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{

            SettingThreads settingThreads=settingThreadsRepository.get_Setting();
            if(threadsFollower24hRepository.count_Follower_24h_By_Username(account_id.trim()+"%")>=settingThreads.getMax_follower()){
                resp.put("status", false);
                return resp;
            }
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            String list_History=threadsFollowerHistoryRepository.get_List_Id_By_AccountId(account_id.trim());
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_Priority_By_Task("threads","follower",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task("threads","follower",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("threads","follower",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
            }
            if (orderRunning!=null) {
                Service service=orderRunning.getService();
                resp.put("status", true);
                data.put("order_id", orderRunning.getOrder_id());
                data.put("account_id", account_id.trim());
                data.put("platform", service.getPlatform().toLowerCase());
                data.put("task", service.getTask());
                data.put("task_key",orderRunning.getOrder_key());
                data.put("task_link",orderRunning.getOrder_link());
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
    public Map<String, Object> threads_like(String account_id,String mode){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            SettingThreads settingThreads=settingThreadsRepository.get_Setting();
            if(threadsLike24hRepository.count_Like_24h_By_Username(account_id.trim()+"%")>=settingThreads.getMax_like()){
                resp.put("status", false);
                return resp;
            }
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            String list_History=threadsLikeHistoryRepository.get_List_PostId_By_AccountId(account_id.trim());
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_Priority_By_Task("threads","like",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task("threads","like",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("threads","like",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
            }
            if (orderRunning!=null) {
                Service service=orderRunning.getService();
                resp.put("status", true);
                data.put("order_id", orderRunning.getOrder_id());
                data.put("account_id", account_id.trim());
                data.put("platform", service.getPlatform().toLowerCase());
                data.put("task", service.getTask());
                data.put("task_key",orderRunning.getOrder_key());
                data.put("task_link",orderRunning.getOrder_link());
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
    public Map<String, Object> threads_view(String account_id,String mode){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            SettingThreads settingThreads=settingThreadsRepository.get_Setting();
            if(threadsView24hRepository.count_View_24h_By_Username(account_id.trim()+"%")>=settingThreads.getMax_view()){
                resp.put("status", false);
                return resp;
            }
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            String list_History=threadsViewHistoryRepository.get_List_VideoId_By_AccountId(account_id.trim());
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_Priority_By_Task("threads","view",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task("threads","view",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("threads","view",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
            }
            if (orderRunning!=null) {
                Service service=orderRunning.getService();
                resp.put("status", true);
                data.put("order_id", orderRunning.getOrder_id());
                data.put("account_id", account_id.trim());
                data.put("platform", service.getPlatform().toLowerCase());
                data.put("task", service.getTask());
                data.put("task_key",orderRunning.getOrder_key());
                data.put("task_link",orderRunning.getOrder_link());
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
    public Map<String, Object> threads_repost(String account_id,String mode){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            SettingThreads settingThreads=settingThreadsRepository.get_Setting();
            if(threadsRepost24hRepository.count_Repost_24h_By_Username(account_id.trim()+"%")>=settingThreads.getMax_repost()){
                resp.put("status", false);
                return resp;
            }
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            String list_History=threadsRepostHistoryRepository.get_List_PostId_By_AccountId(account_id.trim());
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_Priority_By_Task("threads","repost",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task("threads","repost",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("threads","repost",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
            }
            if (orderRunning!=null) {
                Service service=orderRunning.getService();
                resp.put("status", true);
                data.put("order_id", orderRunning.getOrder_id());
                data.put("account_id", account_id.trim());
                data.put("platform", service.getPlatform().toLowerCase());
                data.put("task", service.getTask());
                data.put("task_key",orderRunning.getOrder_key());
                data.put("task_link",orderRunning.getOrder_link());
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

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
    private SettingSystemRepository settingSystemRepository;
    @Autowired
    private TikTokFollower24hRepository tikTokFollower24hRepository;
    @Autowired
    private TikTokLike24hRepository tikTokLike24hRepository;
    @Autowired
    private TikTokView24hRepository tikTokView24hRepository;
    @Autowired
    private TikTokComment24hRepository tikTokComment24hRepository;
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
    @Autowired
    private DataFollowerTiktokRepository dataFollowerTiktokRepository;
    public Map<String, Object> tiktok_comment(String account_id,String mode){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            SettingTiktok settingTiktok=settingTikTokRepository.get_Setting();
            if(tikTokComment24hRepository.count_Comment_24h_By_Username(account_id.trim()+"%")>=settingTiktok.getMax_comment()){
                resp.put("status", false);
                return resp;
            }
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            String list_History=tikTokCommentHistoryRepository.get_List_VideoId_By_AccountId(account_id.trim());
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_Priority_By_Task("tiktok","comment",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","comment",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","comment",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
            }
            if (orderRunning!=null) {

                dataCommentRepository.update_Running_Comment(System.currentTimeMillis(),account_id.trim(),orderRunning.getOrder_id());
                Thread.sleep(ran.nextInt(500));
                String comment=dataCommentRepository.get_Comment_By_OrderId_And_Username(orderRunning.getOrder_id(),account_id.trim());
                if(comment!=null){
                    Service service=orderRunning.getService();
                    if(service.getBonus_type()==0 || service.getBonus_list().length()==0 || service.getBonus_list_percent()==0){
                        data.put("bonus","");
                    }else{
                        if(ran.nextInt(100)<service.getBonus_list_percent()){
                            String [] bonus_list=service.getBonus_list().split(",");
                            data.put("bonus",bonus_list[ran.nextInt(bonus_list.length)]);
                        }else{
                            data.put("bonus","");
                        }
                    }
                    resp.put("status", true);
                    data.put("order_id", orderRunning.getOrder_id());
                    data.put("account_id", account_id.trim());
                    data.put("platform", service.getPlatform().toLowerCase());
                    data.put("task", service.getTask());
                    data.put("app", service.getApp());
                    data.put("task_key",orderRunning.getOrder_key());
                    data.put("task_link",orderRunning.getOrder_link());
                    if(orderRunning.getDuration()>service.getLimit_time()){
                        data.put("viewing_time",(int)(((ran.nextInt(service.getMax_time() - service.getMin_time() + 1) + service.getMin_time())/100F)*service.getLimit_time()));
                    }else{
                        data.put("viewing_time",(int)(((ran.nextInt(service.getMax_time() - service.getMin_time() + 1) + service.getMin_time())/100F)*orderRunning.getDuration()));
                    }
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
    public Map<String, Object> tiktok_follower(String account_id,String mode){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            SettingTiktok settingTiktok=settingTikTokRepository.get_Setting();
            if(tikTokFollower24hRepository.count_Follower_24h_By_Username(account_id.trim()+"%")>=settingTiktok.getMax_follower()){
                resp.put("status", false);
                return resp;
            }
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            String list_History=tikTokAccountHistoryRepository.get_List_TiktokId_By_AccountId(account_id.trim());
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_Priority_By_Task("tiktok","follower",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","follower",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","follower",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
            }
            if (orderRunning!=null) {
                Thread.sleep(150+ran.nextInt(300));
                if(!orderThreadCheck.getValue().contains(orderRunning.getOrder_id().toString())){
                    resp.put("status", false);
                    return resp;
                }else if(tikTokFollower24hRepository.check_Follower_24h_By_Username_And_TiktokId(account_id.trim()+orderRunning.getOrder_key().trim())>0){
                    resp.put("status", false);
                    return resp;
                }
                Service service=orderRunning.getService();
                if(service.getBonus_type()==0 || service.getBonus_list().length()==0 || service.getBonus_list_percent()==0){
                    data.put("bonus","");
                }else{
                    if(ran.nextInt(100)<service.getBonus_list_percent()){
                        String [] bonus_list=service.getBonus_list().split(",");
                        data.put("bonus",bonus_list[ran.nextInt(bonus_list.length)]);
                    }else{
                        data.put("bonus","");
                    }
                }
                resp.put("status", true);
                data.put("order_id", orderRunning.getOrder_id());
                data.put("account_id", account_id.trim());
                data.put("platform", service.getPlatform().toLowerCase());
                data.put("task", service.getTask());
                data.put("app", service.getApp());
                data.put("task_key",orderRunning.getOrder_key());
                DataFollowerTiktok dataFollowerTiktok=dataFollowerTiktokRepository.get_Data_Follower(orderRunning.getOrder_id());
                data.put("task_link","https://www.tiktok.com/"+dataFollowerTiktok.getTiktok_id()+"/video/"+dataFollowerTiktok.getVideo_id());
                if(dataFollowerTiktok.getDuration()>service.getLimit_time()){
                    data.put("viewing_time",(int)(((ran.nextInt(service.getMax_time() - service.getMin_time() + 1) + service.getMin_time())/100F)*service.getLimit_time()));
                }else{
                    data.put("viewing_time",(int)(((ran.nextInt(service.getMax_time() - service.getMin_time() + 1) + service.getMin_time())/100F)*dataFollowerTiktok.getDuration()));
                }
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
    public Map<String, Object> tiktok_like(String account_id,String mode){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            SettingTiktok settingTiktok=settingTikTokRepository.get_Setting();
            if(tikTokLike24hRepository.count_Like_24h_By_Username(account_id.trim()+"%")>=settingTiktok.getMax_like()){
                resp.put("status", false);
                return resp;
            }
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            String list_History=tikTokLikeHistoryRepository.get_List_VideoId_By_AccountId(account_id.trim());
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_Priority_By_Task("tiktok","like",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","like",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","like",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
            }
            if (orderRunning!=null) {
                Service service=orderRunning.getService();
                if(service.getBonus_type()==0 || service.getBonus_list().length()==0 || service.getBonus_list_percent()==0){
                    data.put("bonus","");
                }else{
                    if(ran.nextInt(100)<service.getBonus_list_percent()){
                        String [] bonus_list=service.getBonus_list().split(",");
                        data.put("bonus",bonus_list[ran.nextInt(bonus_list.length)]);
                    }else{
                        data.put("bonus","");
                    }
                }
                resp.put("status", true);
                data.put("order_id", orderRunning.getOrder_id());
                data.put("account_id", account_id.trim());
                data.put("platform", service.getPlatform().toLowerCase());
                data.put("task", service.getTask());
                data.put("app", service.getApp());
                data.put("task_key",orderRunning.getOrder_key());
                data.put("task_link",orderRunning.getOrder_link());

                if(orderRunning.getDuration()>service.getLimit_time()){
                    data.put("viewing_time",(int)(((ran.nextInt(service.getMax_time() - service.getMin_time() + 1) + service.getMin_time())/100F)*service.getLimit_time()));
                }else{
                    data.put("viewing_time",(int)(((ran.nextInt(service.getMax_time() - service.getMin_time() + 1) + service.getMin_time())/100F)*orderRunning.getDuration()));
                }

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
    public Map<String, Object> tiktok_view(String account_id,String mode){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            //System.out.println(account_id);
            SettingTiktok settingTiktok=settingTikTokRepository.get_Setting();
            if(tikTokView24hRepository.count_View_24h_By_Username(account_id.trim()+"%")>=settingTiktok.getMax_view()){
                resp.put("status", false);
                return resp;
            }
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_Priority_By_Task("tiktok","view",mode,"",orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","view",mode,"",orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","view",mode,"",orderThreadCheck.getValue());
            }
            if (orderRunning!=null) {
                Service service=orderRunning.getService();
                if(service.getBonus_type()==0 || service.getBonus_list().length()==0 || service.getBonus_list_percent()==0){
                    data.put("bonus","");
                }else{
                    if(ran.nextInt(100)<service.getBonus_list_percent()){
                        String [] bonus_list=service.getBonus_list().split(",");
                        data.put("bonus",bonus_list[ran.nextInt(bonus_list.length)]);
                    }else{
                        data.put("bonus","");
                    }
                }
                resp.put("status", true);
                data.put("order_id", orderRunning.getOrder_id());
                data.put("account_id", account_id.trim());
                data.put("platform", service.getPlatform().toLowerCase());
                data.put("task", service.getTask());
                data.put("app", service.getApp());
                data.put("task_key",orderRunning.getOrder_key());
                data.put("task_link",orderRunning.getOrder_link());
                if(orderRunning.getDuration()>service.getLimit_time()){
                    data.put("viewing_time",(int)(((ran.nextInt(service.getMax_time() - service.getMin_time() + 1) + service.getMin_time())/100F)*service.getLimit_time()));
                }else{
                    data.put("viewing_time",(int)(((ran.nextInt(service.getMax_time() - service.getMin_time() + 1) + service.getMin_time())/100F)*orderRunning.getDuration()));
                }
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

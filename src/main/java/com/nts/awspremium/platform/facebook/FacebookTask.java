package com.nts.awspremium.platform.facebook;

import com.nts.awspremium.model.*;
import com.nts.awspremium.model_system.OrderThreadCheck;
import com.nts.awspremium.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
@RestController
public class FacebookTask {
    @Autowired
    private OrderRunningRepository orderRunningRepository;
    @Autowired
    private OrderThreadCheck orderThreadCheck;
    @Autowired
    private SettingFacebookRepository settingFacebookRepository;
    @Autowired
    private SettingSystemRepository settingSystemRepository;
    @Autowired
    private FacebookFollower24hRepository facebookFollower24hRepository;
    @Autowired
    private FacebookComment24hRepository facebookComment24hRepository;
    @Autowired
    private FacebookView24hRepository facebookView24hRepository;
    @Autowired
    private FacebookMember24hRepository facebookMember24hRepository;
    @Autowired
    private FacebookLike24hRepository facebookLike24hRepository;
    @Autowired
    private FacebookFollowerHistoryRepository facebookFollowerHistoryRepository;
    @Autowired
    private FacebookLikeHistoryRepository facebookLikeHistoryRepository;
    @Autowired
    private FacebookViewHistoryRepository facebookViewHistoryRepository;
    @Autowired
    private FacebookMemberHistoryRepository facebookMemberHistoryRepository;
    @Autowired
    private FacebookCommentHistoryRepository facebookCommentHistoryRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    @Autowired
    private DataCommentRepository dataCommentRepository;
    @Autowired
    private ModeOptionRepository modeOptionRepository;
    public Map<String, Object> facebook_comment(String account_id,String mode){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            ModeOption modeOption=modeOptionRepository.get_Mode_Option(mode.trim(),"facebook","comment");
            if(facebookComment24hRepository.count_Comment_24h_By_Username(account_id.trim()+"%")>=modeOption.getMax_task()){
                resp.put("status", false);
                return resp;
            }
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            String list_History=facebookCommentHistoryRepository.get_List_PostId_By_AccountId(account_id.trim());
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_Priority_By_Task("facebook","comment",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task("facebook","comment",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("facebook","comment",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
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
    public Map<String, Object> facebook_follower(String account_id,String mode){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            ModeOption modeOption=modeOptionRepository.get_Mode_Option(mode.trim(),"facebook","follower");
            if(facebookFollower24hRepository.count_Follower_24h_By_Username(account_id.trim()+"%")>=modeOption.getMax_task()){
                resp.put("status", false);
                return resp;
            }
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            String list_History=facebookFollowerHistoryRepository.get_List_Id_By_AccountId(account_id.trim());
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_Priority_By_Task("facebook","follower",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task("facebook","follower",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("facebook","follower",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
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
    public Map<String, Object> facebook_like(String account_id,String mode){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            ModeOption modeOption=modeOptionRepository.get_Mode_Option(mode.trim(),"facebook","like");
            if(facebookLike24hRepository.count_Like_24h_By_Username(account_id.trim()+"%")>=modeOption.getMax_task()){
                resp.put("status", false);
                return resp;
            }
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            String list_History=facebookLikeHistoryRepository.get_List_PostId_By_AccountId(account_id.trim());
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_Priority_By_Task("facebook","like",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task("facebook","like",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("facebook","like",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
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
    public Map<String, Object> facebook_view(String account_id,String mode){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            ModeOption modeOption=modeOptionRepository.get_Mode_Option(mode.trim(),"facebook","view");
            if(facebookView24hRepository.count_View_24h_By_Username(account_id.trim()+"%")>=modeOption.getMax_task()){
                resp.put("status", false);
                return resp;
            }
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            String list_History=facebookViewHistoryRepository.get_List_VideoId_By_AccountId(account_id.trim());
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_Priority_By_Task("facebook","view",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task("facebook","view",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("facebook","view",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
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
    public Map<String, Object> facebook_member(String account_id,String mode){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            ModeOption modeOption=modeOptionRepository.get_Mode_Option(mode.trim(),"facebook","member");
            if(facebookMember24hRepository.count_Member_24h_By_Username(account_id.trim()+"%")>=modeOption.getMax_task()){
                resp.put("status", false);
                return resp;
            }
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            String list_History=facebookMemberHistoryRepository.get_List_GroupId_By_AccountId(account_id.trim());
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_Priority_By_Task("facebook","member",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task("facebook","member",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("facebook","member",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
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

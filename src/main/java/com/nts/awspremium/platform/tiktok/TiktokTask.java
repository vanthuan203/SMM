package com.nts.awspremium.platform.tiktok;

import com.google.gson.JsonObject;
import com.nts.awspremium.TikTokApi;
import com.nts.awspremium.model.*;
import com.nts.awspremium.model_system.MySQLCheck;
import com.nts.awspremium.model_system.OrderThreadCheck;
import com.nts.awspremium.model_system.OrderThreadSpeedUpCheck;
import com.nts.awspremium.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
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
    private OrderThreadSpeedUpCheck orderThreadSpeedUpCheck;
    @Autowired
    private SettingTikTokRepository settingTikTokRepository;
    @Autowired
    private SettingSystemRepository settingSystemRepository;
    @Autowired
    private TikTokFollower24hRepository tikTokFollower24hRepository;
    @Autowired
    private TikTokLike24hRepository tikTokLike24hRepository;
    @Autowired
    private TiktokFavorites24hRepository tiktokFavorites24hRepository;
    @Autowired
    private TiktokShare24hRepository tiktokShare24hRepository;
    @Autowired
    private TiktokShareHistoryRepository tiktokShareHistoryRepository;
    @Autowired
    private TiktokFavoritesHistoryRepository tiktokFavoritesHistoryRepository;
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
    @Autowired
    private ModeOptionRepository modeOptionRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountTaskRepository accountTaskRepository;
    @Autowired
    private IpTask24hRepository ipTask24hRepository;
    public Map<String, Object> tiktok_comment(String account_id,String mode){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            ModeOption modeOption=modeOptionRepository.get_Mode_Option(mode.trim(),"tiktok","comment");
            if(tikTokComment24hRepository.count_Comment_24h_By_Username(account_id.trim()+"%")>=modeOption.getMax_task()){
                resp.put("status", false);
                return resp;
            }
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            String list_History=tikTokCommentHistoryRepository.get_List_VideoId_By_AccountId(account_id.trim());

            if(accountRepository.check_Account_Task_True(modeOption.getDay_true_task(),"tiktok",account_id.trim())==0){
                mode="activity";
            }
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_Priority_By_Task("tiktok","comment",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","comment",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","comment",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
            }
            if (orderRunning!=null) {
                Thread.sleep(300+ran.nextInt(500));
                if(!orderThreadSpeedUpCheck.getValue().contains(orderRunning.getOrder_id().toString())){
                    resp.put("status", false);
                    return resp;
                }
                String comment=null;
                Long comment_ID= dataCommentRepository.get_Comment_Pending_By_OrderId(orderRunning.getOrder_id());
                if(comment_ID!=null){
                    try{
                        dataCommentRepository.update_Running_Comment_By_CommentId(System.currentTimeMillis(),account_id.trim(),comment_ID);
                        Thread.sleep(ran.nextInt(500));
                        comment=dataCommentRepository.get_Comment_By_CommentId_And_Username(comment_ID,account_id.trim());
                    }catch (Exception e){
                        resp.put("status", false);
                        return resp;
                    }
                }else{
                    resp.put("status", false);
                    return resp;
                }
                //dataCommentRepository.update_Running_Comment(System.currentTimeMillis(),account_id.trim(),orderRunning.getOrder_id());
                //Thread.sleep(ran.nextInt(500));
                //String comment=dataCommentRepository.get_Comment_By_OrderId_And_Username(orderRunning.getOrder_id(),account_id.trim());
                //String comment=dataCommentRepository.update_Running_Comment_PROCEDURE(System.currentTimeMillis(),account_id.trim(),orderRunning.getOrder_id());
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
                    if(orderRunning.getChannel_title()==null){
                        orderRunningRepository.update_Valid_By_OrderId(0,orderRunning.getOrder_id());
                        data.put("channel_title","");
                    }else{
                        data.put("channel_title",orderRunning.getChannel_title());
                    }
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
    public Map<String, Object> tiktok_follower(String account_id,String mode,String ip){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            ModeOption modeOption=modeOptionRepository.get_Mode_Option(mode.trim(),"tiktok","follower");
            if(tikTokFollower24hRepository.count_Follower_24h_By_Username(account_id.trim()+"%")>=modeOption.getMax_task()){
                resp.put("status", false);
                return resp;
            }
            /*
            if(ipTask24hRepository.count_Task_1h_By_Ip(ip+"%")>=50){
                resp.put("status", false);
                return resp;
            }
             */
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            String list_History=tikTokAccountHistoryRepository.get_List_TiktokId_By_AccountId(account_id.trim());

            if(accountRepository.check_Account_Task_True(modeOption.getDay_true_task(),"tiktok",account_id.trim())==0){
                mode="activity";
            }
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_Priority_By_Task("tiktok","follower",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","follower",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","follower",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
            }
            if(orderRunning==null){
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","follower",mode,list_History==null?"":list_History,orderThreadSpeedUpCheck.getValue());
            }
            if (orderRunning!=null) {
                Thread.sleep(200+ran.nextInt(200));
                if(!orderThreadSpeedUpCheck.getValue().contains(orderRunning.getOrder_id().toString())){
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
                if( orderRunning.getChannel_id()==null){
                    JsonObject channelInfo= TikTokApi.getInfoFullChannel(orderRunning.getOrder_key().trim().split("@")[1]);
                    if(channelInfo==null || channelInfo.size()==0){
                        data.put("channel_id", "");
                    }else{
                        orderRunning.setChannel_id(channelInfo.getAsJsonObject("user").get("id").getAsString());
                        orderRunningRepository.save(orderRunning);
                    }
                }

                List<String> arrSource = new ArrayList<>();
                for (int i = 0; i < service.getYoutube_external(); i++) {
                    arrSource.add("external_video");
                }
                for (int i = 0; i < service.getYoutube_search(); i++) {
                    arrSource.add("search_channel");
                }
                for (int i = 0; i < service.getYoutube_direct(); i++) {
                    arrSource.add("external_channel");
                }
                for (int i = 0; i < service.getYoutube_suggest(); i++) {
                    arrSource.add("trending_video");
                }
                if(arrSource.size()==0){
                    arrSource.add("external_video");
                }
                data.put("source", arrSource.get(ran.nextInt(arrSource.size())).trim());


                data.put("channel_id",orderRunning.getOrder_key());
                data.put("user_id",orderRunning.getChannel_id());
                data.put("app", service.getApp());
                data.put("task_key",orderRunning.getOrder_key());
                DataFollowerTiktok dataFollowerTiktok=dataFollowerTiktokRepository.get_Data_Follower(orderRunning.getOrder_id());
                if(dataFollowerTiktok==null){
                    resp.put("status", false);
                    return resp;
                }
                data.put("task_link","https://www.tiktok.com/"+dataFollowerTiktok.getTiktok_id()+"/video/"+dataFollowerTiktok.getVideo_id());
                if(orderRunning.getChannel_title()==null){
                    orderRunningRepository.update_Valid_By_OrderId(0,orderRunning.getOrder_id());
                    data.put("channel_title","");
                }else{
                    data.put("channel_title",orderRunning.getChannel_title());
                }
                if(dataFollowerTiktok.getDuration()>service.getLimit_time()){
                    data.put("viewing_time",(int)(((ran.nextInt(service.getMax_time() - service.getMin_time() + 1) + service.getMin_time())/100F)*service.getLimit_time()));
                }else{
                    data.put("viewing_time",(int)(((ran.nextInt(service.getMax_time() - service.getMin_time() + 1) + service.getMin_time())/100F)*dataFollowerTiktok.getDuration()));
                }
                resp.put("data",data);
                return resp;

            } else {
                AccountTask accountTask=accountTaskRepository.get_Acount_Task_By_AccountId(account_id.trim());
                if(accountTask!=null){
                    accountTask.setFollower_time(System.currentTimeMillis() + 60 * 1000);
                    accountTaskRepository.save(accountTask);
                }
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
            ModeOption modeOption=modeOptionRepository.get_Mode_Option(mode.trim(),"tiktok","like");
            if(tikTokLike24hRepository.count_Like_24h_By_Username(account_id.trim()+"%")>=modeOption.getMax_task()){
                resp.put("status", false);
                return resp;
            }
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            String list_History=tikTokLikeHistoryRepository.get_List_VideoId_By_AccountId(account_id.trim());

            if(accountRepository.check_Account_Task_True(modeOption.getDay_true_task(),"tiktok",account_id.trim())==0){
                mode="activity";
            }
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_Priority_By_Task("tiktok","like",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","like",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","like",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
            }
            if(orderRunning==null){
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","like",mode,list_History==null?"":list_History,orderThreadSpeedUpCheck.getValue());
            }
            if (orderRunning!=null) {
                Thread.sleep(ran.nextInt(300));
                if(!orderThreadSpeedUpCheck.getValue().contains(orderRunning.getOrder_id().toString())){
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
                data.put("task_link",orderRunning.getOrder_link());
                if(orderRunning.getChannel_title()==null){
                    orderRunningRepository.update_Valid_By_OrderId(0,orderRunning.getOrder_id());
                    data.put("channel_title","");
                }else{
                    data.put("channel_title",orderRunning.getChannel_title());
                }

                if( orderRunning.getChannel_id()==null){
                    JsonObject infoVideo= TikTokApi.getInfoVideo(orderRunning.getOrder_link());
                    if(infoVideo==null || infoVideo.size()==0){
                        data.put("channel_id", "");
                    }else{
                        orderRunning.setChannel_id("@"+infoVideo.get("author").getAsJsonObject().get("unique_id").getAsString());
                        orderRunningRepository.save(orderRunning);
                    }
                }
                data.put("channel_id",orderRunning.getChannel_id());

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

    public Map<String, Object> tiktok_share(String account_id,String mode){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            ModeOption modeOption=modeOptionRepository.get_Mode_Option(mode.trim(),"tiktok","share");
            if(tiktokShare24hRepository.count_Share_24h_By_Username(account_id.trim()+"%")>=modeOption.getMax_task()){
                resp.put("status", false);
                return resp;
            }
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            String list_History=tiktokShareHistoryRepository.get_List_VideoId_By_AccountId(account_id.trim());

            if(accountRepository.check_Account_Task_True(modeOption.getDay_true_task(),"tiktok",account_id.trim())==0){
                mode="activity";
            }
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_Priority_By_Task("tiktok","share",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","share",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","share",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
            }
            if(orderRunning==null){
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","share",mode,list_History==null?"":list_History,orderThreadSpeedUpCheck.getValue());
            }
            if (orderRunning!=null) {
                Thread.sleep(ran.nextInt(300));
                if(!orderThreadSpeedUpCheck.getValue().contains(orderRunning.getOrder_id().toString())){
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
                data.put("task_link",orderRunning.getOrder_link());
                if(orderRunning.getChannel_title()==null){
                    orderRunningRepository.update_Valid_By_OrderId(0,orderRunning.getOrder_id());
                    data.put("channel_title","");
                }else{
                    data.put("channel_title",orderRunning.getChannel_title());
                }

                if( orderRunning.getChannel_id()==null){
                    JsonObject infoVideo= TikTokApi.getInfoVideo(orderRunning.getOrder_link());
                    if(infoVideo==null || infoVideo.size()==0){
                        data.put("channel_id", "");
                    }else{
                        orderRunning.setChannel_id("@"+infoVideo.get("author").getAsJsonObject().get("unique_id").getAsString());
                        orderRunningRepository.save(orderRunning);
                    }
                }
                data.put("channel_id",orderRunning.getChannel_id());

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


    public Map<String, Object> tiktok_favorites(String account_id,String mode){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            ModeOption modeOption=modeOptionRepository.get_Mode_Option(mode.trim(),"tiktok","favorites");
            if(tiktokFavorites24hRepository.count_Favorites_24h_By_Username(account_id.trim()+"%")>=modeOption.getMax_task()){
                resp.put("status", false);
                return resp;
            }
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            String list_History=tiktokFavoritesHistoryRepository.get_List_VideoId_By_AccountId(account_id.trim());

            if(accountRepository.check_Account_Task_True(modeOption.getDay_true_task(),"tiktok",account_id.trim())==0){
                mode="activity";
            }
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_Priority_By_Task("tiktok","favorites",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","favorites",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","favorites",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
            }
            if(orderRunning==null){
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","favorites",mode,list_History==null?"":list_History,orderThreadSpeedUpCheck.getValue());
            }
            if (orderRunning!=null) {
                Thread.sleep(ran.nextInt(300));
                if(!orderThreadSpeedUpCheck.getValue().contains(orderRunning.getOrder_id().toString())){
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
                data.put("task_link",orderRunning.getOrder_link());
                if(orderRunning.getChannel_title()==null){
                    orderRunningRepository.update_Valid_By_OrderId(0,orderRunning.getOrder_id());
                    data.put("channel_title","");
                }else{
                    data.put("channel_title",orderRunning.getChannel_title());
                }

                if( orderRunning.getChannel_id()==null){
                    JsonObject infoVideo= TikTokApi.getInfoVideo(orderRunning.getOrder_link());
                    if(infoVideo==null || infoVideo.size()==0){
                        data.put("channel_id", "");
                    }else{
                        orderRunning.setChannel_id("@"+infoVideo.get("author").getAsJsonObject().get("unique_id").getAsString());
                        orderRunningRepository.save(orderRunning);
                    }
                }
                data.put("channel_id",orderRunning.getChannel_id());

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
            ModeOption modeOption=modeOptionRepository.get_Mode_Option(mode.trim(),"tiktok","view");
            mode="auto";
            Integer max_Task=modeOption.getMax_task();
            if(accountRepository.check_Account_Task_True(modeOption.getDay_true_task(),"tiktok",account_id.trim())==0&&account_id.contains("@")){
                max_Task=10;
            }
            if(tikTokView24hRepository.count_View_24h_By_Username(account_id.trim()+"%")>=max_Task){
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
            if(orderRunning==null){
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","view",mode,"",orderThreadSpeedUpCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task("tiktok","view","activity","",orderThreadSpeedUpCheck.getValue());
                }
            }
            if (orderRunning!=null) {
                Thread.sleep(ran.nextInt(300));
                if(!orderThreadSpeedUpCheck.getValue().contains(orderRunning.getOrder_id().toString())){
                    resp.put("status", false);
                    return resp;
                }

                Service service=orderRunning.getService();
                if(service.getBonus_type()==0 || service.getBonus_list().length()==0 || service.getBonus_list_percent()==0){
                    data.put("bonus","");
                }else{
                    if(ran.nextInt(100)<service.getBonus_list_percent()){
                        String [] bonus_list=service.getBonus_list().split(",");

                        String bonus=bonus_list[ran.nextInt(bonus_list.length)];
                        if(bonus.equals("like")){
                            ModeOption modeOptionLike=modeOptionRepository.get_Mode_Option(mode.trim(),"tiktok","like");
                            if(tikTokLike24hRepository.count_Like_24h_By_Username(account_id.trim()+"%")>=modeOptionLike.getMax_task()){
                                data.put("bonus","");
                            }else{
                                String list_History_Like=tikTokLikeHistoryRepository.get_List_VideoId_By_AccountId(account_id.trim());
                                if(list_History_Like==null?true:!list_History_Like.contains(orderRunning.getOrder_key())){
                                    data.put("bonus",bonus);
                                }else{
                                    data.put("bonus","");
                                }
                            }
                        }else if(bonus.equals("share")){
                            ModeOption modeOptionShare=modeOptionRepository.get_Mode_Option(mode.trim(),"tiktok","share");
                            if(tiktokShare24hRepository.count_Share_24h_By_Username(account_id.trim()+"%")>=modeOptionShare.getMax_task()){
                                data.put("bonus","");
                            }else{
                                String list_History_Share=tiktokShareHistoryRepository.get_List_VideoId_By_AccountId(account_id.trim());
                                if(list_History_Share==null?true:!list_History_Share.contains(orderRunning.getOrder_key())){
                                    data.put("bonus",bonus);
                                }else{
                                    data.put("bonus","");
                                }
                            }
                        }else if(bonus.equals("favorites")){
                            ModeOption modeOptionFavorites=modeOptionRepository.get_Mode_Option(mode.trim(),"tiktok","favorites");
                            if(tiktokFavorites24hRepository.count_Favorites_24h_By_Username(account_id.trim()+"%")>=modeOptionFavorites.getMax_task()){
                                data.put("bonus","");
                            }else{
                                String list_History_Favorites=tiktokFavoritesHistoryRepository.get_List_VideoId_By_AccountId(account_id.trim());
                                if(list_History_Favorites==null?true:!list_History_Favorites.contains(orderRunning.getOrder_key())){
                                    data.put("bonus",bonus);
                                }else{
                                    data.put("bonus","");
                                }
                            }
                        }
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

                if(orderRunning.getChannel_title()==null){
                    orderRunningRepository.update_Valid_By_OrderId(0,orderRunning.getOrder_id());
                    data.put("channel_title","");
                }else{
                    data.put("channel_title",orderRunning.getChannel_title());
                }

                if( orderRunning.getChannel_id()==null){
                    JsonObject infoVideo= TikTokApi.getInfoVideo(orderRunning.getOrder_link());
                    if(infoVideo==null || infoVideo.size()==0){
                        data.put("channel_id", "");
                    }else{
                        orderRunning.setChannel_id("@"+infoVideo.get("author").getAsJsonObject().get("unique_id").getAsString());
                        orderRunningRepository.save(orderRunning);
                    }
                }
                data.put("channel_id",orderRunning.getChannel_id());

                if(orderRunning.getDuration()>service.getLimit_time()){
                    data.put("viewing_time",(int)(((ran.nextInt(service.getMax_time() - service.getMin_time() + 1) + service.getMin_time())/100F)*service.getLimit_time()));
                }else{
                    data.put("viewing_time",(int)(((ran.nextInt(service.getMax_time() - service.getMin_time() + 1) + service.getMin_time())/100F)*orderRunning.getDuration()));
                }
                resp.put("data",data);
                return resp;
            } else {
                AccountTask accountTask=accountTaskRepository.get_Acount_Task_By_AccountId(account_id.trim());
                if(accountTask!=null){
                    accountTask.setView_time(System.currentTimeMillis() + 60 * 1000);
                    accountTaskRepository.save(accountTask);
                }
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

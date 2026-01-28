package com.nts.awspremium.platform.youtube;

import com.nts.awspremium.model.*;
import com.nts.awspremium.model_system.MySQLCheck;
import com.nts.awspremium.model_system.OrderThreadCheck;
import com.nts.awspremium.model_system.OrderThreadSpeedUpCheck;
import com.nts.awspremium.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class YoutubeTask {
    @Autowired
    private OrderRunningRepository orderRunningRepository;
    @Autowired
    private OrderThreadCheck orderThreadCheck;
    @Autowired
    private ProfileTaskRepository profileTaskRepository;
    @Autowired
    private KeywordsRepository keywordsRepository;

    @Autowired
    private HistorySumRepository historySumRepository;

    @Autowired
    private OrderThreadSpeedUpCheck orderThreadSpeedUpCheck;
    @Autowired
    private YoutubeViewHistoryRepository youtubeVideoHistoryRepository;
    @Autowired
    private YoutubeSubscriberHistoryRepository youtubeChannelHistoryRepository;
    @Autowired
    private YoutubeLikeHistoryRepository youtubeLikeHistoryRepository;
    @Autowired
    private YoutubeCommentHistoryRepository youtubeCommentHistoryRepository;
    @Autowired
    private DataSubscriberRepository dataSubscriberRepository;
    @Autowired
    private SettingYoutubeRepository settingYoutubeRepository;
    @Autowired
    private SettingSystemRepository settingSystemRepository;
    @Autowired
    private YoutubeLike24hRepository youtubeLike24hRepository;
    @Autowired
    private YoutubeView24hRepository youtubeView24hRepository;
    @Autowired
    private YoutubeComment24hRepository youtubeComment24hRepository;
    @Autowired
    private YoutubeSubscriber24hRepository youtubeSubscribe24hRepository;
    @Autowired
    private DataCommentRepository dataCommentRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    @Autowired
    private ModeOptionRepository modeOptionRepository;
    public Map<String, Object> youtube_comment(String account_id,String mode){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{

            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            SettingYoutube settingYoutube =settingYoutubeRepository.get_Setting();
            String list_History=youtubeCommentHistoryRepository.get_List_VideoId_By_AccountId(account_id.trim());

            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_By_Task_Priority_And_Limit_Time("youtube","comment",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task_And_Limit_Time("youtube","comment",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task_And_Limit_Time("youtube","comment",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
            }
            if(orderRunning==null){
                if(ran.nextInt(100)<settingYoutube.getMax_activity_24h()){
                    return youtube_farm(account_id);
                }else{
                    resp.put("status", false);
                    return resp;
                }
            }
            if (orderRunning!=null) {
                Service service=orderRunning.getService();
                if(historySumRepository.get_Count_By_OrderId(orderRunning.getOrder_id(),service.getLimit_task_time())>0){
                    if(ran.nextInt(100)<settingYoutube.getMax_activity_24h()){
                        return youtube_farm(account_id);
                    }else{
                        resp.put("status", false);
                        return resp;
                    }
                }
                Thread.sleep(300+ran.nextInt(500));
                if(!orderThreadCheck.getValue().contains(orderRunning.getOrder_id().toString())){
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
                    data.put("channel_title",orderRunning.getChannel_title());
                    List<String> arrSource = new ArrayList<>();
                    for (int i = 0; i < service.getYoutube_external(); i++) {
                        arrSource.add("external");
                    }
                    for (int i = 0; i < service.getYoutube_search(); i++) {
                        arrSource.add("search");
                    }
                    for (int i = 0; i < service.getYoutube_direct(); i++) {
                        arrSource.add("direct");
                    }
                    for (int i = 0; i < service.getYoutube_suggest(); i++) {
                        arrSource.add("suggest");
                    }
                    for (int i = 0; i < service.getYoutube_dtn(); i++) {
                        arrSource.add("dtn");
                    }
                    for (int i = 0; i < service.getYoutube_embed(); i++) {
                        arrSource.add("embed");
                    }
                    data.put("source", arrSource.get(ran.nextInt(arrSource.size())).trim());

                    if(service.getService_type().trim().equals("Special")){
                        String list_key = orderRunning.getKeyword_list();
                        String key = "";
                        if (list_key != null && list_key.length() != 0) {
                            String[] keyArr = list_key.split(",");
                            key = keyArr[ran.nextInt(keyArr.length)];
                        }
                        data.put("keyword", key.length() == 0 ? orderRunning.getVideo_title() : key);

                    }else{
                        data.put("keyword", orderRunning.getVideo_title());
                    }
                    if (service.getMin_time() != service.getMax_time()) {
                        if (orderRunning.getDuration() > service.getMax_time() * 60) {
                            data.put("viewing_time", service.getMin_time() * 60 + ran.nextInt((service.getMax_time() - service.getMin_time()) * 45));
                        } else {
                            data.put("viewing_time", service.getMin_time() * 60 < orderRunning.getDuration() ? (service.getMin_time() * 60 + ran.nextInt((int)(orderRunning.getDuration() - service.getMin_time() * 60))) : orderRunning.getDuration());
                        }
                    }else {
                        if (orderRunning.getDuration() > service.getMax_time() * 60) {
                            data.put("viewing_time", service.getMin_time() * 60 + ran.nextInt(30) );
                        } else {
                            data.put("viewing_time", orderRunning.getDuration());
                        }
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
    public Map<String, Object> youtube_view(String account_id,String mode){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            SettingYoutube settingYoutube =settingYoutubeRepository.get_Setting();
            String list_History=youtubeVideoHistoryRepository.get_List_VideoId_By_AccountId(account_id.trim());
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_By_Task_Priority_And_Limit_Time("youtube","view",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task_And_Limit_Time("youtube","view",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task_And_Limit_Time("youtube","view",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
            }
            if(orderRunning==null){
                orderRunning = orderRunningRepository.get_Order_Running_By_Task_And_Limit_Time("youtube","view",mode,list_History==null?"":list_History,orderThreadSpeedUpCheck.getValue());
                if(orderRunning==null){
                    if(ran.nextInt(100)<settingYoutube.getMax_activity_24h()){
                        return youtube_farm(account_id);
                    }else{
                        resp.put("status", false);
                        return resp;
                    }
                }
            }
            if(orderRunning!=null) {
                Service service=orderRunning.getService();
                Thread.sleep(150+ran.nextInt(250));
                if(!orderThreadSpeedUpCheck.getValue().contains(orderRunning.getOrder_id().toString())){
                    resp.put("status", false);
                    return resp;
                }
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
                data.put("task_key", orderRunning.getOrder_key());
                data.put("task_link",orderRunning.getOrder_link());
                //data.put("task_link",orderRunning.getOrder_link()+"&t=0");
                data.put("channel_id", orderRunning.getChannel_id());
                data.put("channel_title", orderRunning.getChannel_title());


                List<String> arrSource = new ArrayList<>();
                for (int i = 0; i < service.getYoutube_external(); i++) {
                    arrSource.add("external");
                }
                for (int i = 0; i < service.getYoutube_external_google(); i++) {
                    arrSource.add("external_google");
                }
                for (int i = 0; i < service.getYoutube_search(); i++) {
                    arrSource.add("search");
                }
                for (int i = 0; i < service.getYoutube_direct(); i++) {
                    arrSource.add("direct");
                }
                for (int i = 0; i < service.getYoutube_suggest(); i++) {
                    arrSource.add("suggest");
                }
                for (int i = 0; i < service.getYoutube_dtn(); i++) {
                    arrSource.add("dtn");
                }
                for (int i = 0; i < service.getYoutube_embed(); i++) {
                    arrSource.add("embed");
                }
                data.put("source", arrSource.get(ran.nextInt(arrSource.size())).trim());

                if(service.getService_type().trim().equals("Special")){
                    String list_key = orderRunning.getKeyword_list();
                    String key = "";
                    if (list_key != null && list_key.length() != 0) {
                        String[] keyArr = list_key.split(",");
                        key = keyArr[ran.nextInt(keyArr.length)];
                    }
                    data.put("keyword", key.length() == 0 ? orderRunning.getVideo_title() : key);

                }else{
                    data.put("keyword", orderRunning.getVideo_title());
                }
                if (service.getMin_time() != service.getMax_time()) {
                    if (orderRunning.getDuration() > service.getMax_time() * 60) {
                        data.put("viewing_time", service.getMin_time() * 60 + ran.nextInt((service.getMax_time() - service.getMin_time()) * 45));
                    } else {
                        data.put("viewing_time", service.getMin_time() * 60 < orderRunning.getDuration() ? (service.getMin_time() * 60 + ran.nextInt((int)(orderRunning.getDuration() - service.getMin_time() * 60))) : orderRunning.getDuration());
                    }
                }else {
                    if (orderRunning.getDuration() > service.getMax_time() * 60) {
                        data.put("viewing_time", service.getMin_time() * 60 + ran.nextInt(30) );
                    } else {
                        data.put("viewing_time", orderRunning.getDuration());
                    }
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

    public Map<String, Object> youtube_farm(String account_id){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran = new Random();
            resp.put("status", true);
            data.put("order_id", -1);
            data.put("account_id", account_id.trim());
            data.put("platform", "youtube");
            data.put("task", "farm");
            data.put("task_key", "random");
            data.put("app", "youtube");
            String list_key = keywordsRepository.getKeyList();
            if (list_key != null && list_key.length() != 0) {
                String[] keyArr = list_key.split(",");
                data.put("keyword",keyArr[ran.nextInt(keyArr.length)]);
            }else{
                data.put("keyword","random");
            }
            List<String> arrSource = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                arrSource.add("home");
            }
            for (int i = 0; i < 15; i++) {
                arrSource.add("shorts");
            }
            for (int i = 0; i < 30; i++) {
                arrSource.add("search");
            }
            data.put("source", arrSource.get(ran.nextInt(arrSource.size())).trim());
            data.put("viewing_time", 1 * 60 + ran.nextInt(30));
            resp.put("data",data);
            return resp;

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

    public Map<String, Object> youtube_subscriber(String account_id,String mode){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            SettingYoutube settingYoutube =settingYoutubeRepository.get_Setting();
            String list_History=youtubeChannelHistoryRepository.get_List_ChannelId_By_AccountId(account_id.trim());
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_By_Task_Priority_And_Limit_Time("youtube","subscriber",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task_And_Limit_Time("youtube","subscriber",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task_And_Limit_Time("youtube","subscriber",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
            }
            if(orderRunning==null){
                if(ran.nextInt(100)<settingYoutube.getMax_activity_24h()){
                    return youtube_farm(account_id);
                }else{
                    resp.put("status", false);
                    return resp;
                }
            }
            if (orderRunning!=null) {
                Service service=orderRunning.getService();
                if(service.getLimit_task_time()>0){
                    if(historySumRepository.get_Count_By_OrderId(orderRunning.getOrder_id(),service.getLimit_task_time())>0){
                        if(ran.nextInt(100)<settingYoutube.getMax_activity_24h()){
                            return youtube_farm(account_id);
                        }else{
                            resp.put("status", false);
                            return resp;
                        }
                    }else if(historySumRepository.get_Count_By_OrderId(orderRunning.getOrder_id(),60)>=(60/service.getLimit_task_time())*service.getThread()){
                        if(ran.nextInt(100)<settingYoutube.getMax_activity_24h()){
                            return youtube_farm(account_id);
                        }else{
                            resp.put("status", false);
                            return resp;
                        }
                    }
                }
                Thread.sleep(200+ran.nextInt(350));
                if(!orderThreadCheck.getValue().contains(orderRunning.getOrder_id().toString())){
                    resp.put("status", false);
                    return resp;
                }
                Thread.sleep(200+ran.nextInt(350));
                if(profileTaskRepository.get_Count_Thread_By_OrderId(orderRunning.getOrder_id())>=orderRunning.getThread()){
                    resp.put("status", false);
                    return resp;
                }
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
                //resp.put("proxy", proxy);
                data.put("account_id", account_id.trim());
                data.put("platform", service.getPlatform().toLowerCase());
                data.put("task", service.getTask());
                data.put("app", service.getApp());
                data.put("channel_id", orderRunning.getChannel_id());
                data.put("channel_title", orderRunning.getChannel_title());
                DataSubscriber dataSubscriber=dataSubscriberRepository.get_Data_Subscriber(orderRunning.getOrder_id());
                data.put("task_key", dataSubscriber.getVideo_id());
                data.put("task_link","https://www.youtube.com/watch?v="+dataSubscriber.getVideo_id());
                data.put("keyword", dataSubscriber.getVideo_title());

                List<String> arrSource = new ArrayList<>();
                for (int i = 0; i < service.getYoutube_external(); i++) {
                    arrSource.add("external");
                }
                for (int i = 0; i < service.getYoutube_external_google(); i++) {
                    arrSource.add("external_google");
                }
                for (int i = 0; i < service.getYoutube_search(); i++) {
                    arrSource.add("search");
                }
                for (int i = 0; i < service.getYoutube_direct(); i++) {
                    arrSource.add("direct");
                }
                for (int i = 0; i < service.getYoutube_suggest(); i++) {
                    arrSource.add("suggest");
                }
                for (int i = 0; i < service.getYoutube_dtn(); i++) {
                    arrSource.add("dtn");
                }
                for (int i = 0; i < service.getYoutube_embed(); i++) {
                    arrSource.add("embed");
                }
                data.put("source", arrSource.get(ran.nextInt(arrSource.size())).trim());

                if (service.getMin_time() != service.getMax_time()) {
                    if (dataSubscriber.getDuration() > service.getMax_time() * 60) {
                        data.put("viewing_time", service.getMin_time() * 60 + ran.nextInt((service.getMax_time() - service.getMin_time()) * 45));
                    } else {
                        data.put("viewing_time", service.getMin_time() * 60 < dataSubscriber.getDuration() ? (service.getMin_time() * 60 + ran.nextInt((int)(dataSubscriber.getDuration() - service.getMin_time() * 60))) : dataSubscriber.getDuration());
                    }
                }else {
                    if (dataSubscriber.getDuration() > service.getMax_time() * 60) {
                        data.put("viewing_time", service.getMin_time() * 60 + ran.nextInt(30) );
                    } else {
                        data.put("viewing_time", dataSubscriber.getDuration());
                    }
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

    public Map<String, Object> youtube_like(String account_id,String mode){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            SettingYoutube settingYoutube =settingYoutubeRepository.get_Setting();
            String list_History=youtubeLikeHistoryRepository.get_List_VideoId_By_AccountId(account_id.trim());
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_By_Task_Priority_And_Limit_Time("youtube","like",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task_And_Limit_Time("youtube","like",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task_And_Limit_Time("youtube","like",mode,list_History==null?"":list_History,orderThreadCheck.getValue());
            }
            if(orderRunning==null){
                if(ran.nextInt(100)<settingYoutube.getMax_activity_24h()){
                    return youtube_farm(account_id);
                }else{
                    resp.put("status", false);
                    return resp;
                }
            }
            if (orderRunning!=null) {
                Service service=orderRunning.getService();
                if(historySumRepository.get_Count_By_OrderId(orderRunning.getOrder_id(),service.getLimit_task_time())>0){
                    if(ran.nextInt(100)<settingYoutube.getMax_activity_24h()){
                        return youtube_farm(account_id);
                    }else{
                        resp.put("status", false);
                        return resp;
                    }
                }
                Thread.sleep(300+ran.nextInt(500));
                if(!orderThreadCheck.getValue().contains(orderRunning.getOrder_id().toString())){
                    resp.put("status", false);
                    return resp;
                }
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
                //resp.put("proxy", proxy);
                data.put("account_id", account_id.trim());
                data.put("platform", service.getPlatform().toLowerCase());
                data.put("task", service.getTask());
                data.put("app", service.getApp());
                data.put("task_key", orderRunning.getOrder_key());
                data.put("task_link",orderRunning.getOrder_link());
                data.put("keyword", orderRunning.getVideo_title());
                data.put("channel_id", orderRunning.getChannel_id());
                data.put("channel_title", orderRunning.getChannel_title());

                List<String> arrSource = new ArrayList<>();
                for (int i = 0; i < service.getYoutube_external(); i++) {
                    arrSource.add("external");
                }
                for (int i = 0; i < service.getYoutube_external_google(); i++) {
                    arrSource.add("external_google");
                }
                for (int i = 0; i < service.getYoutube_search(); i++) {
                    arrSource.add("search");
                }
                for (int i = 0; i < service.getYoutube_direct(); i++) {
                    arrSource.add("direct");
                }
                for (int i = 0; i < service.getYoutube_suggest(); i++) {
                    arrSource.add("suggest");
                }
                for (int i = 0; i < service.getYoutube_dtn(); i++) {
                    arrSource.add("dtn");
                }
                for (int i = 0; i < service.getYoutube_embed(); i++) {
                    arrSource.add("embed");
                }
                data.put("source", arrSource.get(ran.nextInt(arrSource.size())).trim());

                if (service.getMin_time() != service.getMax_time()) {
                    if (orderRunning.getDuration() > service.getMax_time() * 60) {
                        data.put("viewing_time", service.getMin_time() * 60 + ran.nextInt((service.getMax_time() - service.getMin_time()) * 45));
                    } else {
                        data.put("viewing_time", service.getMin_time() * 60 < orderRunning.getDuration() ? (service.getMin_time() * 60 + ran.nextInt((int)(orderRunning.getDuration() - service.getMin_time() * 60))) : orderRunning.getDuration());
                    }
                }else {
                    if (orderRunning.getDuration() > service.getMax_time() * 60) {
                        data.put("viewing_time", service.getMin_time() * 60 + ran.nextInt(30) );
                    } else {
                        data.put("viewing_time", orderRunning.getDuration());
                    }
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

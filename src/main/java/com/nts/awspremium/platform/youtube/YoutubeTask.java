package com.nts.awspremium.platform.youtube;

import com.nts.awspremium.model.*;
import com.nts.awspremium.model_system.MySQLCheck;
import com.nts.awspremium.model_system.OrderThreadCheck;
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
    private YoutubeViewHistoryRepository youtubeVideoHistoryRepository;
    @Autowired
    private YoutubeSubscriberHistoryRepository youtubeChannelHistoryRepository;
    @Autowired
    private YoutubeLikeHistoryRepository youtubeLikeHistoryRepository;
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
    private YoutubeSubscriber24hRepository youtubeSubscribe24hRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    public Map<String, Object> youtube_view(String account_id){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            SettingYoutube settingYoutube=settingYoutubeRepository.get_Setting();
            if(youtubeView24hRepository.count_View_24h_By_Username(account_id.trim()+"%")>=settingYoutube.getMax_view()){
                resp.put("status", false);
                return resp;
            }
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            String list_History=youtubeVideoHistoryRepository.get_List_VideoId_By_AccountId(account_id.trim());
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_Priority_By_Task("youtube","view",list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task("youtube","view",list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("youtube","view",list_History==null?"":list_History,orderThreadCheck.getValue());
            }
            if (orderRunning!=null) {
                Service service=orderRunning.getService();
                resp.put("status", true);
                data.put("order_id", orderRunning.getOrder_id());
                data.put("account_id", account_id.trim());
                data.put("platform", service.getPlatform().toLowerCase());
                data.put("task", service.getTask());
                data.put("task_key", orderRunning.getOrder_key());
                data.put("task_link",orderRunning.getOrder_link());
                data.put("channel_id", orderRunning.getChannel_id());
                data.put("channel_title", orderRunning.getChannel_title());


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

    public Map<String, Object> youtube_subscriber(String account_id){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            SettingYoutube settingYoutube=settingYoutubeRepository.get_Setting();
            if(youtubeSubscribe24hRepository.count_Subscribe_24h_By_Username(account_id.trim()+"%")>=settingYoutube.getMax_subscriber()){
                resp.put("status", false);
                return resp;
            }
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            String list_History=youtubeChannelHistoryRepository.get_List_ChannelId_By_AccountId(account_id.trim());
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_Priority_By_Task("youtube","subscriber",list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task("youtube","subscriber",list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("youtube","subscriber",list_History==null?"":list_History,orderThreadCheck.getValue());
            }
            if (orderRunning!=null) {
                Service service=orderRunning.getService();
                resp.put("status", true);
                data.put("order_id", orderRunning.getOrder_id());
                //resp.put("proxy", proxy);
                data.put("account_id", account_id.trim());
                data.put("platform", service.getPlatform().toLowerCase());
                data.put("task", service.getTask());
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
                for (int i = 0; i < service.getYoutube_search(); i++) {
                    arrSource.add("search");
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

    public Map<String, Object> youtube_like(String account_id){
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            SettingYoutube settingYoutube=settingYoutubeRepository.get_Setting();
            if(youtubeLike24hRepository.count_Like_24h_By_Username(account_id.trim()+"%")>=settingYoutube.getMax_like()){
                resp.put("status", false);
                return resp;
            }
            Random ran = new Random();
            OrderRunning orderRunning=null;
            SettingSystem settingSystem =settingSystemRepository.get_Setting_System();
            String list_History=youtubeLikeHistoryRepository.get_List_VideoId_By_AccountId(account_id.trim());
            if(ran.nextInt(100)<settingSystem.getMax_priority()){
                orderRunning = orderRunningRepository.get_Order_Running_Priority_By_Task("youtube","like",list_History==null?"":list_History,orderThreadCheck.getValue());
                if(orderRunning==null){
                    orderRunning = orderRunningRepository.get_Order_Running_By_Task("youtube","like",list_History==null?"":list_History,orderThreadCheck.getValue());
                }
            }else{
                orderRunning = orderRunningRepository.get_Order_Running_By_Task("youtube","like",list_History==null?"":list_History,orderThreadCheck.getValue());
            }              if (orderRunning!=null) {
                Service service=orderRunning.getService();
                resp.put("status", true);
                data.put("order_id", orderRunning.getOrder_id());
                //resp.put("proxy", proxy);
                data.put("account_id", account_id.trim());
                data.put("platform", service.getPlatform().toLowerCase());
                data.put("task", service.getTask());
                data.put("task_key", orderRunning.getOrder_key());
                data.put("task_link",orderRunning.getOrder_link());
                data.put("keyword", orderRunning.getVideo_title());
                data.put("channel_id", orderRunning.getChannel_id());
                data.put("channel_title", orderRunning.getChannel_title());

                List<String> arrSource = new ArrayList<>();
                for (int i = 0; i < service.getYoutube_external(); i++) {
                    arrSource.add("external");
                }
                for (int i = 0; i < service.getYoutube_search(); i++) {
                    arrSource.add("search");
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

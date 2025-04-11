package com.nts.awspremium.platform.tiktok;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

    @Autowired
    private DataFollowerTiktokRepository dataFollowerTiktokRepository;

    public JSONObject tiktok_follower(DataRequest data, Service service, User user,Boolean pending)  throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        OrderRunning orderRunningCheck=null;
        try{
            String tiktok_id= TikTokApi.getTiktokId(data.getLink().trim());
            if (tiktok_id == null) {
                tiktok_id= TikTokApi.extractTikTokId(data.getLink().trim());
                if(tiktok_id==null){
                    JsonObject infoVideo=TikTokApi.getInfoVideo(data.getLink().trim());
                    if(infoVideo==null){
                        resp.put("error", "Cant filter tiktok_id from link");
                        return resp;
                    }else{
                        tiktok_id= "@"+infoVideo.getAsJsonObject("author").get("unique_id").getAsString();
                    }
                }else {
                    data.setLink("https://www.tiktok.com/"+tiktok_id);
                }
            }
            if (orderRunningRepository.get_Order_By_Order_Key_And_Task(tiktok_id.trim(),service.getTask()) > 0) {
                resp.put("error", "This ID in process");
                return resp;
            }
            JsonObject channelInfo=TikTokApi.getInfoFullChannel(tiktok_id.trim().split("@")[1]);
            if(channelInfo==null || channelInfo.size()==0){
                resp.put("error", "This account cannot be found");
                return resp;
            }else if(channelInfo.getAsJsonObject("user").get("privateAccount").getAsBoolean()){
                resp.put("error", "This account is private");
                return resp;
            }else if(channelInfo.getAsJsonObject("stats").get("videoCount").getAsInt()<1){
                resp.put("error", "The total number of videos in the account must be greater than or equal to 1 videos");
                return resp;
            }

            Integer follower_count=channelInfo.getAsJsonObject("stats").get("followerCount").getAsInt();


            float priceorder = 0;
            priceorder = (data.getQuantity() / 1000F) * service.getService_rate() * ((float) (user.getRate()) / 100) * ((float) (100 - user.getDiscount()) / 100);
            if (priceorder > (float) user.getBalance()) {
                resp.put("error", "Your balance not enough");
                return resp;
            }

            OrderRunning orderRunning = new OrderRunning();
            orderRunning.setInsert_time(System.currentTimeMillis());
            orderRunning.setQuantity(data.getQuantity());
            orderRunning.setOrder_link("https://www.tiktok.com/"+tiktok_id);
            orderRunning.setChannel_title(channelInfo.getAsJsonObject("user").get("nickname").getAsString());
            orderRunning.setStart_count(follower_count);
            orderRunning.setTotal(0);
            orderRunning.setOrder_key(tiktok_id.trim());
            orderRunning.setUser(user);
            orderRunning.setUpdate_time(0L);
            orderRunning.setUpdate_current_time(0L);
            if(pending==false){
                orderRunning.setStart_time(System.currentTimeMillis());
            }else{
                orderRunning.setStart_time(0L);
            }
            orderRunning.setThread(service.getThread());
            orderRunning.setThread_set(service.getThread());
            orderRunning.setNote(data.getNote()==null?"":data.getNote());
            orderRunning.setCharge(priceorder);
            orderRunning.setService(service);
            orderRunning.setCurrent_count(0);
            orderRunning.setCheck_count(0);
            orderRunning.setCheck_count_time(0L);
            orderRunning.setStart_count_time(0L);
            orderRunning.setValid(1);
            orderRunning.setSpeed_up(0);
            orderRunning.setOrder_refill(data.getOrder_refill());
            orderRunning.setPriority(0);
            orderRunningRepository.save(orderRunning);
            orderRunningCheck=orderRunning;

            JsonArray videoList=TikTokApi.getInfoVideoByChannel(tiktok_id.trim().split("@")[1],8);

            if(videoList==null){
                resp.put("error", "Unable to get account video information");
                return resp;
            }

            for (JsonElement video: videoList) {
                JsonObject videoObj=video.getAsJsonObject();
                DataFollowerTiktok dataFollowerTiktok =new DataFollowerTiktok();
                dataFollowerTiktok.setVideo_id(videoObj.get("video_id").getAsString());
                Long duration=videoObj.get("duration").getAsLong();
                if(duration==0){
                    duration=15L;
                }
                dataFollowerTiktok.setDuration(duration);
                dataFollowerTiktok.setTiktok_id(tiktok_id.trim());
                dataFollowerTiktok.setOrderRunning(orderRunning);
                dataFollowerTiktok.setState(1);
                dataFollowerTiktok.setAdd_time(System.currentTimeMillis());
                dataFollowerTiktok.setAdd_time(System.currentTimeMillis());
                //dataFollowerTiktok.setVideo_title(videoObj.get("title").getAsString());
                dataFollowerTiktokRepository.save(dataFollowerTiktok);
            }

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
            if(orderRunningCheck!=null){
                orderRunningRepository.delete_Order_Running_By_OrderId(orderRunningCheck.getOrder_id());
            }
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

            resp.put("error", "Cant insert link");
            return resp;
        }
    }
    public JSONObject tiktok_like(DataRequest data,Service service,User user,Boolean pending)  throws IOException, ParseException{
        JSONObject resp = new JSONObject();
        try{
            String video_id= TikTokApi.getVideoId(data.getLink().trim());
            String link;
            if (video_id == null) {
                   link=data.getLink().trim();
            }else{
                if (orderRunningRepository.get_Order_By_Order_Key_And_Task(video_id.trim(),service.getTask()) > 0) {
                    resp.put("error", "This ID in process");
                    return resp;
                }
                link=data.getLink().trim();
            }

            JsonObject infoVideo=TikTokApi.getInfoVideo(link);
            if(infoVideo==null){
                resp.put("error", "This video cannot be found");
                return resp;
            }
            video_id=infoVideo.get("id").getAsString();
            String unique_id=infoVideo.get("author").getAsJsonObject().get("unique_id").getAsString();
            link="https://www.tiktok.com/@"+unique_id.trim()+"/video/"+video_id.trim();
            Integer like_count=infoVideo.get("digg_count").getAsInt();
            Long duration=infoVideo.get("duration").getAsLong();
            if(duration==0){
                //duration=infoVideo.getAsJsonObject("music_info").get("duration").getAsLong();
                duration=15L;
            }
            if (orderRunningRepository.get_Order_By_Order_Key_And_Task(video_id.trim(),service.getTask()) > 0) {
                resp.put("error", "This ID in process");
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
            orderRunning.setOrder_link(link);
            orderRunning.setChannel_title(infoVideo.get("author").getAsJsonObject().get("nickname").getAsString());
            orderRunning.setStart_count(like_count);
            orderRunning.setTotal(0);
            orderRunning.setOrder_key(video_id);
            orderRunning.setUser(user);
            orderRunning.setUpdate_time(0L);
            orderRunning.setUpdate_current_time(0L);
            if(pending==false){
                orderRunning.setStart_time(System.currentTimeMillis());
            }else{
                orderRunning.setStart_time(0L);
            }
            orderRunning.setThread(service.getThread());
            orderRunning.setThread_set(service.getThread());
            orderRunning.setNote(data.getNote()==null?"":data.getNote());
            orderRunning.setCharge(priceorder);
            orderRunning.setService(service);
            orderRunning.setValid(1);
            orderRunning.setSpeed_up(0);
            orderRunning.setCurrent_count(0);
            orderRunning.setCheck_count(0);
            orderRunning.setCheck_count_time(0L);
            orderRunning.setStart_count_time(0L);
            orderRunning.setOrder_refill(data.getOrder_refill());
            orderRunning.setPriority(0);
            orderRunning.setDuration(duration);
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

            resp.put("error", "Cant insert link");
            return resp;
        }
    }

    public JSONObject tiktok_share(DataRequest data,Service service,User user,Boolean pending)  throws IOException, ParseException{
        JSONObject resp = new JSONObject();
        try{
            String video_id= TikTokApi.getVideoId(data.getLink().trim());
            String link;
            if (video_id == null) {
                link=data.getLink().trim();
            }else{
                if (orderRunningRepository.get_Order_By_Order_Key_And_Task(video_id.trim(),service.getTask()) > 0) {
                    resp.put("error", "This ID in process");
                    return resp;
                }
                link=data.getLink().trim();
            }

            JsonObject infoVideo=TikTokApi.getInfoVideo(link);
            if(infoVideo==null){
                resp.put("error", "This video cannot be found");
                return resp;
            }
            video_id=infoVideo.get("id").getAsString();
            String unique_id=infoVideo.get("author").getAsJsonObject().get("unique_id").getAsString();
            link="https://www.tiktok.com/@"+unique_id.trim()+"/video/"+video_id.trim();
            Integer share_count=infoVideo.get("share_count").getAsInt();
            Long duration=infoVideo.get("duration").getAsLong();
            if(duration==0){
                //duration=infoVideo.getAsJsonObject("music_info").get("duration").getAsLong();
                duration=15L;
            }
            if (orderRunningRepository.get_Order_By_Order_Key_And_Task(video_id.trim(),service.getTask()) > 0) {
                resp.put("error", "This ID in process");
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
            orderRunning.setOrder_link(link);
            orderRunning.setChannel_title(infoVideo.get("author").getAsJsonObject().get("nickname").getAsString());
            orderRunning.setStart_count(share_count);
            orderRunning.setTotal(0);
            orderRunning.setOrder_key(video_id);
            orderRunning.setUser(user);
            orderRunning.setUpdate_time(0L);
            orderRunning.setUpdate_current_time(0L);
            if(pending==false){
                orderRunning.setStart_time(System.currentTimeMillis());
            }else{
                orderRunning.setStart_time(0L);
            }
            orderRunning.setThread(service.getThread());
            orderRunning.setThread_set(service.getThread());
            orderRunning.setNote(data.getNote()==null?"":data.getNote());
            orderRunning.setCharge(priceorder);
            orderRunning.setService(service);
            orderRunning.setValid(1);
            orderRunning.setSpeed_up(0);
            orderRunning.setCurrent_count(0);
            orderRunning.setCheck_count(0);
            orderRunning.setCheck_count_time(0L);
            orderRunning.setStart_count_time(0L);
            orderRunning.setOrder_refill(data.getOrder_refill());
            orderRunning.setPriority(0);
            orderRunning.setDuration(duration);
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

            resp.put("error", "Cant insert link");
            return resp;
        }
    }


    public JSONObject tiktok_favorites(DataRequest data,Service service,User user,Boolean pending)  throws IOException, ParseException{
        JSONObject resp = new JSONObject();
        try{
            String video_id= TikTokApi.getVideoId(data.getLink().trim());
            String link;
            if (video_id == null) {
                link=data.getLink().trim();
            }else{
                if (orderRunningRepository.get_Order_By_Order_Key_And_Task(video_id.trim(),service.getTask()) > 0) {
                    resp.put("error", "This ID in process");
                    return resp;
                }
                link=data.getLink().trim();
            }

            JsonObject infoVideo=TikTokApi.getInfoVideo(link);
            if(infoVideo==null){
                resp.put("error", "This video cannot be found");
                return resp;
            }
            video_id=infoVideo.get("id").getAsString();
            String unique_id=infoVideo.get("author").getAsJsonObject().get("unique_id").getAsString();
            link="https://www.tiktok.com/@"+unique_id.trim()+"/video/"+video_id.trim();
            Integer collect_count=infoVideo.get("collect_count").getAsInt();
            Long duration=infoVideo.get("duration").getAsLong();
            if(duration==0){
                //duration=infoVideo.getAsJsonObject("music_info").get("duration").getAsLong();
                duration=15L;
            }
            if (orderRunningRepository.get_Order_By_Order_Key_And_Task(video_id.trim(),service.getTask()) > 0) {
                resp.put("error", "This ID in process");
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
            orderRunning.setOrder_link(link);
            orderRunning.setChannel_title(infoVideo.get("author").getAsJsonObject().get("nickname").getAsString());
            orderRunning.setStart_count(collect_count);
            orderRunning.setTotal(0);
            orderRunning.setOrder_key(video_id);
            orderRunning.setUser(user);
            orderRunning.setUpdate_time(0L);
            orderRunning.setUpdate_current_time(0L);
            if(pending==false){
                orderRunning.setStart_time(System.currentTimeMillis());
            }else{
                orderRunning.setStart_time(0L);
            }
            orderRunning.setThread(service.getThread());
            orderRunning.setThread_set(service.getThread());
            orderRunning.setNote(data.getNote()==null?"":data.getNote());
            orderRunning.setCharge(priceorder);
            orderRunning.setService(service);
            orderRunning.setValid(1);
            orderRunning.setSpeed_up(0);
            orderRunning.setCurrent_count(0);
            orderRunning.setCheck_count(0);
            orderRunning.setCheck_count_time(0L);
            orderRunning.setStart_count_time(0L);
            orderRunning.setOrder_refill(data.getOrder_refill());
            orderRunning.setPriority(0);
            orderRunning.setDuration(duration);
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

            resp.put("error", "Cant insert link");
            return resp;
        }
    }

    public JSONObject tiktok_comment(DataRequest data,Service service,User user,Boolean pending)  throws IOException, ParseException{
        JSONObject resp = new JSONObject();
        try{
            String video_id= TikTokApi.getVideoId(data.getLink().trim());
            String link;
            if (video_id == null) {
                link=data.getLink().trim();
            }else{
                if (orderRunningRepository.get_Order_By_Order_Key_And_Task(video_id.trim(),service.getTask()) > 0) {
                    resp.put("error", "This ID in process");
                    return resp;
                }
                link=data.getLink().trim();
            }

            JsonObject infoVideo=TikTokApi.getInfoVideo(link);
            if(infoVideo==null){
                resp.put("error", "This video cannot be found");
                return resp;
            }
            video_id=infoVideo.get("id").getAsString();
            String unique_id=infoVideo.get("author").getAsJsonObject().get("unique_id").getAsString();
            link="https://www.tiktok.com/@"+unique_id.trim()+"/video/"+video_id.trim();
            Integer comment_count=infoVideo.get("comment_count").getAsInt();
            Long duration=infoVideo.get("duration").getAsLong();
            if(duration==0){
                //duration=infoVideo.getAsJsonObject("music_info").get("duration").getAsLong();
                duration=15L;
            }
            if (orderRunningRepository.get_Order_By_Order_Key_And_Task(video_id.trim(),service.getTask()) > 0) {
                resp.put("error", "This ID in process");
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
            orderRunning.setOrder_link(link);
            orderRunning.setChannel_title(infoVideo.get("author").getAsJsonObject().get("nickname").getAsString());
            orderRunning.setStart_count(comment_count);
            orderRunning.setTotal(0);
            orderRunning.setOrder_key(video_id);
            orderRunning.setUser(user);
            orderRunning.setComment_list(data.getComments());
            orderRunning.setUpdate_time(0L);
            orderRunning.setUpdate_current_time(0L);
            orderRunning.setStart_time(0L);
            orderRunning.setThread(service.getThread());
            orderRunning.setThread_set(service.getThread());
            orderRunning.setNote(data.getNote()==null?"":data.getNote());
            orderRunning.setCharge(priceorder);
            orderRunning.setService(service);
            orderRunning.setValid(1);
            orderRunning.setSpeed_up(0);
            orderRunning.setCurrent_count(0);
            orderRunning.setCheck_count(0);
            orderRunning.setCheck_count_time(0L);
            orderRunning.setStart_count_time(0L);
            orderRunning.setOrder_refill(data.getOrder_refill());
            orderRunning.setPriority(0);
            orderRunning.setDuration(duration);
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

            resp.put("error", "Cant insert link");
            return resp;
        }
    }
    public JSONObject tiktok_view(DataRequest data,Service service,User user,Boolean pending)  throws IOException, ParseException{
        JSONObject resp = new JSONObject();
        try{
            String video_id= TikTokApi.getVideoId(data.getLink().trim());
            String link;
            if (video_id == null) {
                link=data.getLink().trim();
            }else{
                if (orderRunningRepository.get_Order_By_Order_Key_And_Task(video_id.trim(),service.getTask()) > 0) {
                    resp.put("error", "This ID in process");
                    return resp;
                }
                link=data.getLink().trim();
            }

            JsonObject infoVideo=TikTokApi.getInfoVideo(link);
            if(infoVideo==null){
                resp.put("error", "This video cannot be found");
                return resp;
            }
            video_id=infoVideo.get("id").getAsString();
            String unique_id=infoVideo.get("author").getAsJsonObject().get("unique_id").getAsString();
            link="https://www.tiktok.com/@"+unique_id.trim()+"/video/"+video_id.trim();
            Integer view_count=infoVideo.get("play_count").getAsInt();
            Long duration=infoVideo.get("duration").getAsLong();
            if(duration==0){
                duration=infoVideo.getAsJsonObject("music_info").get("duration").getAsLong();
            }
            if (orderRunningRepository.get_Order_By_Order_Key_And_Task(video_id.trim(),service.getTask()) > 0) {
                resp.put("error", "This ID in process");
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
            orderRunning.setOrder_link(link);
            orderRunning.setChannel_title(infoVideo.get("author").getAsJsonObject().get("nickname").getAsString());
            orderRunning.setStart_count(view_count);
            orderRunning.setTotal(0);
            orderRunning.setOrder_key(video_id);
            orderRunning.setUser(user);
            orderRunning.setUpdate_time(0L);
            orderRunning.setUpdate_current_time(0L);
            if(pending==false){
                orderRunning.setStart_time(System.currentTimeMillis());
            }else{
                orderRunning.setStart_time(0L);
            }
            orderRunning.setThread(service.getThread());
            orderRunning.setThread_set(service.getThread());
            orderRunning.setNote(data.getNote()==null?"":data.getNote());
            orderRunning.setCharge(priceorder);
            orderRunning.setService(service);
            orderRunning.setValid(1);
            orderRunning.setSpeed_up(0);
            orderRunning.setCurrent_count(0);
            orderRunning.setCheck_count(0);
            orderRunning.setCheck_count_time(0L);
            orderRunning.setStart_count_time(0L);
            orderRunning.setOrder_refill(data.getOrder_refill());
            orderRunning.setPriority(0);
            orderRunning.setDuration(duration);
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

            resp.put("error", "Cant insert link");
            return resp;
        }
    }

}

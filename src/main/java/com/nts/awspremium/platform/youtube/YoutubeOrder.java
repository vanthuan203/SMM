package com.nts.awspremium.platform.youtube;

import com.nts.awspremium.GoogleApi;
import com.nts.awspremium.model.*;
import com.nts.awspremium.model_system.MySQLCheck;
import com.nts.awspremium.model_system.OrderThreadCheck;
import com.nts.awspremium.repositories.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
public class YoutubeOrder {
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private OrderRunningRepository orderRunningRepository;
    @Autowired
    private OrderCommentRepository orderCommentRepository;
    @Autowired
    private DataSubscriberRepository dataSubscriberRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    public JSONObject youtube_view(DataRequest data, Service service, User user)  throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        try{
            String videoId = GoogleApi.getYoutubeId(data.getLink());
            if (videoId == null) {
                resp.put("error", "Cant filter videoid from link");
                return resp;
            }
            if (orderRunningRepository.get_Order_By_Order_Key_And_Task(videoId.trim(),service.getTask()) > 0) {
                resp.put("error", "This ID in process");
                return resp;
            }
            OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            Random ran = new Random();
            Request request1 = null;
            Iterator k = null;
            String[] key={"AIzaSyA1mXzdZh1THOmazXeLuU1QNW1GyJqBS_A","AIzaSyA6m4AmAGSiGANwtO2UtHglFFz9RF3YTwI","AIzaSyA8zA-au4ZLpXTqrv3CFqW2dvN0mMQuWaE","AIzaSyAc3zrvWloLGpDZMmex-Kq0UqrVFqJPRac","AIzaSyAct-_8qIpPxSJJFFLno6BBACZsZeYDmPw"};
            request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key="+key[ran.nextInt(key.length)]+"&fields=items(id,snippet(title,channelId,channelTitle,liveBroadcastContent),statistics(viewCount),contentDetails(duration))&part=snippet,statistics,contentDetails&id=" + videoId).get().build();

            Response response1 = client1.newCall(request1).execute();

            String resultJson1 = response1.body().string();
            Object obj1 = new JSONParser().parse(resultJson1);
            JSONObject jsonObject1 = (JSONObject) obj1;
            JSONArray items = (JSONArray) jsonObject1.get("items");
            if (items == null) {
                resp.put("error", "Can't get video info");
                return resp;
            }
            k = items.iterator();
            if (k.hasNext() == false) {
                resp.put("error", "Can't get video info");
                return resp;
            }
            while (k.hasNext()) {
                try {
                    JSONObject video = (JSONObject) k.next();
                    JSONObject contentDetails = (JSONObject) video.get("contentDetails");
                    JSONObject snippet = (JSONObject) video.get("snippet");

                    if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() == 0) {
                        resp.put("error", "This video is a livestream video");
                        return resp;
                    }
                    if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 15) {
                        resp.put("error", "Videos under 15 seconds");
                        return resp;
                    }
                    if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 600&&service.getCheck_time()==1&&service.getMax_time()==10) {
                        resp.put("error", "Video under 10 minutes");
                        return resp;
                    }


                    float priceorder = 0;
                    priceorder = (data.getQuantity() / 1000F) * service.getService_rate() * ((float) (user.getRate()) / 100) * ((float) (100 - user.getDiscount()) / 100);
                    if (priceorder > (float) user.getBalance()) {
                        resp.put("error", "Your balance not enough");
                        return resp;
                    }
                    JSONObject statistics = (JSONObject) video.get("statistics");
                    OrderRunning orderRunning = new OrderRunning();

                    if (snippet.get("liveBroadcastContent").toString().equals("none")) {
                        if(service.getCheck_time()==1) {
                            orderRunning.setThread(-1);
                            orderRunning.setStart_time(0L);
                        }else {
                            orderRunning.setThread(service.getThread());
                            orderRunning.setStart_time(System.currentTimeMillis());
                        }
                    }else{
                        orderRunning.setThread(0);
                        orderRunning.setStart_time(0L);
                    }
                    orderRunning.setDuration(Duration.parse(contentDetails.get("duration").toString()).getSeconds());
                    orderRunning.setInsert_time(System.currentTimeMillis());
                    orderRunning.setTotal(0);
                    orderRunning.setTime_total(0);
                    orderRunning.setQuantity(data.getQuantity());
                    orderRunning.setOrder_link(data.getLink());
                    orderRunning.setUser(user);
                    orderRunning.setChannel_id(snippet.get("channelId").toString());
                    orderRunning.setChannel_title(snippet.get("channelTitle").toString());
                    orderRunning.setVideo_title(snippet.get("title").toString());
                    orderRunning.setOrder_key(video.get("id").toString());
                    orderRunning.setStart_count(Integer.parseInt(statistics.get("viewCount").toString()));
                    ////////////////
                    orderRunning.setCharge(priceorder);
                    orderRunning.setUpdate_time(0L);
                    orderRunning.setUpdate_current_time(0L);
                    orderRunning.setCurrent_count(0);
                    orderRunning.setNote(data.getNote()==null?"":data.getNote());
                    orderRunning.setService(service);
                    orderRunning.setSpeed_up(0);
                    orderRunning.setValid(1);
                    orderRunning.setOrder_refill(data.getOrder_refill());
                    orderRunning.setPriority(0);
                    orderRunning.setStart_count_time(0L);

                    if (service.getService_type().equals("Special")) {
                        orderRunning.setKeyword_list(data.getList());

                    } else if (service.getService_type().equals("Special 1")) {
                        orderRunning.setVideo_list(data.getSuggest());
                        orderRunning.setKeyword_list(data.getList());
                    }
                    orderRunningRepository.save(orderRunning);

                    Float balance_update=balanceRepository.update_Balance(-priceorder,user.getUsername().trim());
                    Balance balance = new Balance();
                    balance.setUser(user.getUsername().trim());
                    balance.setAdd_time(System.currentTimeMillis());
                    balance.setTotal_blance(balance_update);
                    balance.setBalance(-priceorder);
                    balance.setService(data.getService());
                    balance.setNote("Order " + data.getQuantity()+" " +service.getTask()+ " for Id " + orderRunning.getOrder_id());
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

    public JSONObject youtube_comment(DataRequest data, Service service, User user)  throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        try{
            String videoId = GoogleApi.getYoutubeId(data.getLink());
            if (videoId == null) {
                resp.put("error", "Cant filter videoid from link");
                return resp;
            }
            if (orderRunningRepository.get_Order_By_Order_Key_And_Task(videoId.trim(),service.getTask()) > 0) {
                resp.put("error", "This ID in process");
                return resp;
            }
            OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            Random ran = new Random();
            Request request1 = null;
            Iterator k = null;
            String[] key={"AIzaSyA6m4AmAGSiGANwtO2UtHglFFz9RF3YTwI","AIzaSyA8zA-au4ZLpXTqrv3CFqW2dvN0mMQuWaE","AIzaSyAc3zrvWloLGpDZMmex-Kq0UqrVFqJPRac","AIzaSyAct-_8qIpPxSJJFFLno6BBACZsZeYDmPw"};
            request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key="+key[ran.nextInt(key.length)]+"&fields=items(id,snippet(title,description,channelId,channelTitle,liveBroadcastContent),statistics(commentCount),contentDetails(duration))&part=snippet,statistics,contentDetails&id=" + videoId).get().build();

            Response response1 = client1.newCall(request1).execute();

            String resultJson1 = response1.body().string();
            Object obj1 = new JSONParser().parse(resultJson1);
            JSONObject jsonObject1 = (JSONObject) obj1;
            JSONArray items = (JSONArray) jsonObject1.get("items");
            if (items == null) {
                resp.put("error", "Can't get video info");
                return resp;
            }
            k = items.iterator();
            if (k.hasNext() == false) {
                resp.put("error", "Can't get video info");
                return resp;
            }
            while (k.hasNext()) {
                try {
                    JSONObject video = (JSONObject) k.next();
                    JSONObject contentDetails = (JSONObject) video.get("contentDetails");
                    JSONObject snippet = (JSONObject) video.get("snippet");

                    if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() == 0) {
                        resp.put("error", "This video is a livestream video");
                        return resp;
                    }
                    if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 15) {
                        resp.put("error", "Videos under 15 seconds");
                        return resp;
                    }
                    if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 600&&service.getCheck_time()==1&&service.getMax_time()==10) {
                        resp.put("error", "Video under 10 minutes");
                        return resp;
                    }


                    float priceorder = 0;
                    priceorder = (data.getQuantity() / 1000F) * service.getService_rate() * ((float) (user.getRate()) / 100) * ((float) (100 - user.getDiscount()) / 100);
                    if (priceorder > (float) user.getBalance()) {
                        resp.put("error", "Your balance not enough");
                        return resp;
                    }
                    JSONObject statistics = (JSONObject) video.get("statistics");
                    OrderRunning orderRunning = new OrderRunning();

                    if (snippet.get("liveBroadcastContent").toString().equals("none")) {
                        if(service.getCheck_time()==1) {
                            orderRunning.setThread(-1);
                            orderRunning.setStart_time(0L);
                        }else {
                            orderRunning.setThread(service.getThread());
                            orderRunning.setStart_time(System.currentTimeMillis());
                        }
                    }else{
                        orderRunning.setThread(0);
                        orderRunning.setStart_time(0L);
                    }
                    orderRunning.setStart_time(0L);
                    orderRunning.setDuration(Duration.parse(contentDetails.get("duration").toString()).getSeconds());
                    orderRunning.setInsert_time(System.currentTimeMillis());
                    orderRunning.setTotal(0);
                    orderRunning.setTime_total(0);
                    orderRunning.setQuantity(data.getQuantity());
                    orderRunning.setOrder_link(data.getLink());
                    orderRunning.setUser(user);
                    orderRunning.setComment_list(data.getComments());
                    orderRunning.setChannel_id(snippet.get("channelId").toString());
                    orderRunning.setChannel_title(snippet.get("channelTitle").toString());
                    orderRunning.setVideo_title(snippet.get("title").toString());
                    orderRunning.setOrder_key(video.get("id").toString());
                    orderRunning.setStart_count(Integer.parseInt(statistics.get("commentCount").toString()));
                    if(snippet.get("description")!=null&&snippet.get("description").toString().length()>0){
                        orderRunning.setVideo_descriptions(snippet.get("description").toString());
                    }else{
                        orderRunning.setVideo_descriptions("");
                    }
                    ////////////////
                    orderRunning.setCharge(priceorder);
                    orderRunning.setUpdate_time(0L);
                    orderRunning.setUpdate_current_time(0L);
                    orderRunning.setCurrent_count(0);
                    orderRunning.setNote(data.getNote()==null?"":data.getNote());
                    orderRunning.setService(service);
                    orderRunning.setSpeed_up(0);
                    orderRunning.setValid(1);
                    orderRunning.setOrder_refill(data.getOrder_refill());
                    orderRunning.setPriority(0);
                    orderRunning.setStart_count_time(0L);

                    if (service.getService_type().equals("Special")) {
                        orderRunning.setKeyword_list(data.getList());

                    } else if (service.getService_type().equals("Special 1")) {
                        orderRunning.setVideo_list(data.getSuggest());
                        orderRunning.setKeyword_list(data.getList());
                    }
                    orderRunningRepository.save(orderRunning);

                    if(service.getAi()){
                        OrderComment orderComment=new OrderComment();
                        orderComment.setOrderRunning(orderRunning);
                        orderComment.setAi_uuid("");
                        orderComment.setCount_render(0);
                        orderComment.setUpdate_time(0L);
                        orderCommentRepository.save(orderComment);
                    }

                    Float balance_update=balanceRepository.update_Balance(-priceorder,user.getUsername().trim());
                    Balance balance = new Balance();
                    balance.setUser(user.getUsername().trim());
                    balance.setAdd_time(System.currentTimeMillis());
                    balance.setTotal_blance(balance_update);
                    balance.setBalance(-priceorder);
                    balance.setService(data.getService());
                    balance.setNote("Order " + data.getQuantity()+" " +service.getTask()+ " for Id " + orderRunning.getOrder_id());
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

    public JSONObject youtube_like(DataRequest data,Service service,User user)  throws IOException, ParseException{
        JSONObject resp = new JSONObject();
        try{
            String videoId = GoogleApi.getYoutubeId(data.getLink());
            if (videoId == null) {
                resp.put("error", "Cant filter videoid from link");
                return resp;
            }
            if (orderRunningRepository.get_Order_By_Order_Key_And_Task(videoId.trim(),service.getTask()) > 0) {
                resp.put("error", "This ID in process");
                return resp;
            }
            OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            Random ran = new Random();
            Request request1 = null;
            Iterator k = null;
            String[] key={"AIzaSyA1mXzdZh1THOmazXeLuU1QNW1GyJqBS_A","AIzaSyA6m4AmAGSiGANwtO2UtHglFFz9RF3YTwI","AIzaSyA8zA-au4ZLpXTqrv3CFqW2dvN0mMQuWaE","AIzaSyAc3zrvWloLGpDZMmex-Kq0UqrVFqJPRac","AIzaSyAct-_8qIpPxSJJFFLno6BBACZsZeYDmPw"};
            request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key="+key[ran.nextInt(key.length)]+"&fields=items(id,snippet(title,channelId,channelTitle),statistics(likeCount),contentDetails(duration))&part=snippet,statistics,contentDetails&id=" + videoId).get().build();

            Response response1 = client1.newCall(request1).execute();

            String resultJson1 = response1.body().string();
            Object obj1 = new JSONParser().parse(resultJson1);
            JSONObject jsonObject1 = (JSONObject) obj1;
            JSONArray items = (JSONArray) jsonObject1.get("items");
            if (items == null) {
                resp.put("error", "Can't get video info");
                return resp;
            }
            k = items.iterator();
            if (k.hasNext() == false) {
                resp.put("error", "Can't get video info");
                return resp;
            }
            while (k.hasNext()) {
                try {
                    JSONObject video = (JSONObject) k.next();
                    JSONObject snippet = (JSONObject) video.get("snippet");
                    JSONObject contentDetails = (JSONObject) video.get("contentDetails");
                    JSONObject statistics = (JSONObject) video.get("statistics");
                    float priceorder = 0;
                    priceorder = (data.getQuantity() / 1000F) * service.getService_rate() * ((float) (user.getRate()) / 100) * ((float) (100 - user.getDiscount()) / 100);
                    if (priceorder > (float) user.getBalance()) {
                        resp.put("error", "Your balance not enough");
                        return resp;
                    }
                    OrderRunning orderRunning = new OrderRunning();
                    orderRunning.setThread(service.getThread());
                    orderRunning.setThread_set(0);
                    orderRunning.setDuration(Duration.parse(contentDetails.get("duration").toString()).getSeconds());
                    orderRunning.setInsert_time(System.currentTimeMillis());
                    orderRunning.setStart_time(System.currentTimeMillis());
                    orderRunning.setTotal(0);
                    orderRunning.setTime_total(0);
                    orderRunning.setQuantity(data.getQuantity());
                    orderRunning.setOrder_link(data.getLink());
                    orderRunning.setUser(user);
                    orderRunning.setChannel_id(snippet.get("channelId").toString());
                    orderRunning.setChannel_title(snippet.get("channelTitle").toString());
                    orderRunning.setVideo_title(snippet.get("title").toString());
                    orderRunning.setOrder_key(videoId);
                    orderRunning.setStart_count(Integer.parseInt(statistics.get("likeCount").toString()));
                    ////////////////
                    orderRunning.setUpdate_time(0L);
                    orderRunning.setUpdate_current_time(0L);
                    orderRunning.setCurrent_count(0);
                    orderRunning.setCharge(priceorder);
                    orderRunning.setNote(data.getNote()==null?"":data.getNote());
                    orderRunning.setService(service);
                    orderRunning.setValid(1);
                    orderRunning.setSpeed_up(0);
                    orderRunning.setOrder_refill(data.getOrder_refill());
                    orderRunning.setPriority(0);
                    orderRunning.setStart_count_time(0L);

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
    public  JSONObject youtube_subscriber(DataRequest data,Service service,User user)  throws IOException, ParseException{
        JSONObject resp = new JSONObject();
        try{
            String channelId = GoogleApi.getChannelId(data.getLink());
            if (channelId == null) {
                resp.put("error", "Cant filter channel from link");
                return resp;
            }
            String title=channelId.split(",")[0];
            String uId=channelId.split(",")[1];
            if (orderRunningRepository.get_Order_By_Order_Key_And_Task(uId.trim(),service.getTask()) > 0) {
                resp.put("error", "This ID in process");
                return resp;
            }
            List<String> videoList =GoogleApi.getVideoLinks("https://www.youtube.com/channel/"+uId+"/videos");
            if(videoList.size()<1){
                resp.put("error", "The total number of videos in the account must be greater than or equal to 1 videos");
                return resp;
            }
            int start_Count =GoogleApi.getCountSubcriberCurrent(uId);
            if(start_Count==-2){
                resp.put("error", "Can't get SubcriberCurrent");
                return resp;
            }
            float priceorder = 0;
            priceorder = (data.getQuantity() / 1000F) * service.getService_rate() * ((float) (user.getRate()) / 100) * ((float) (100 - user.getDiscount()) / 100);
            if (priceorder > (float) user.getBalance()) {
                resp.put("error", "Your balance not enough");
                return resp;
            }
            OrderRunning orderRunning = new OrderRunning();
            orderRunning.setThread(service.getThread());
            orderRunning.setThread_set(0);
            orderRunning.setDuration(0L);
            orderRunning.setInsert_time(System.currentTimeMillis());
            orderRunning.setStart_time(System.currentTimeMillis());
            orderRunning.setTotal(0);
            orderRunning.setTime_total(0);
            orderRunning.setUpdate_time(0L);
            orderRunning.setUpdate_current_time(0L);
            orderRunning.setQuantity(data.getQuantity());
            orderRunning.setOrder_link(data.getLink());
            orderRunning.setUser(user);
            orderRunning.setChannel_id(uId);
            orderRunning.setChannel_title(title);
            orderRunning.setVideo_title("");
            orderRunning.setOrder_key(uId);
            orderRunning.setStart_count(start_Count);
            orderRunning.setCurrent_count(0);
            ////////////////
            orderRunning.setCharge(priceorder);
            orderRunning.setNote(data.getNote()==null?"":data.getNote());
            orderRunning.setService(service);
            orderRunning.setValid(1);
            orderRunning.setSpeed_up(0);
            orderRunning.setOrder_refill(data.getOrder_refill());
            orderRunning.setPriority(0);
            orderRunning.setStart_count_time(0L);

            orderRunningRepository.save(orderRunning);

            for (int i=0;i<videoList.size();i++){
                String [] video_Info =videoList.get(i).split("~#");
                DataSubscriber dataSubscriber=new DataSubscriber();
                dataSubscriber.setVideo_id(video_Info[0].trim());
                dataSubscriber.setVideo_title(video_Info[1].trim());
                dataSubscriber.setState(1);
                dataSubscriber.setAdd_time(System.currentTimeMillis());
                dataSubscriber.setOrderRunning(orderRunning);
                dataSubscriber.setChannel_id(orderRunning.getOrder_key());
                dataSubscriber.setDuration(Long.parseLong(video_Info[2]));
                dataSubscriberRepository.save(dataSubscriber);
            }

            Float balance_update=balanceRepository.update_Balance(-priceorder,user.getUsername().trim());
            Balance balance = new Balance();
            balance.setUser(user.getUsername().trim());
            balance.setAdd_time(System.currentTimeMillis());
            balance.setTotal_blance(balance_update);
            balance.setBalance(-priceorder);
            balance.setService(data.getService());
            balance.setNote("Order " + data.getQuantity()+" " +service.getTask()+ " for Id " + orderRunning.getOrder_id());
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

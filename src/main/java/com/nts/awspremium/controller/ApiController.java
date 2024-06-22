package com.nts.awspremium.controller;

import com.nts.awspremium.GoogleApi;
import com.nts.awspremium.TikTokApi;
import com.nts.awspremium.model.*;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/api")
public class ApiController {
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private OrderRunningRepository orderRunningRepository;
    @Autowired
    private OrderHistoryRepository orderHistoryRepository;
    @Autowired
    private DataCommentRepository dataCommentRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private DataSubscriberRepository dataSubscriberRepository;
    @Autowired
    private YoutubeSubscriberHistoryRepository youtubeChannelHistoryRepository;

    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private AccountTaskRepository accountTaskRepository;

    @Autowired
    private SettingTikTokRepository settingTikTokRepository;

    @Autowired
    private TikTokFollower24hRepository tikTokFollower24hRepository;

    @Autowired
    private TikTokAccountHistoryRepository tikTokAccountHistoryRepository;

    @Autowired
    private UserRepository userRepository;


    @PostMapping(value = "/ver1", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> ver1(DataRequest data) throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        try{
            User user = userRepository.find_User_By_Token(data.getKey().trim());
            if (data.getKey().length() == 0 || user==null) {
                resp.put("error", "Key not found");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            if (data.getAction().equals("services")) {
                List<Service> services = serviceRepository.get_All_Service_Enabled();
                JSONArray arr_Service = new JSONArray();
                float rate;
                for (int i = 0; i < services.size(); i++) {
                    rate = services.get(i).getService_rate() * ((float) (user.getRate()) / 100) * ((float) (100 - user.getDiscount()) / 100);
                    JSONObject serviceJson = new JSONObject();
                    serviceJson.put("service", services.get(i).getService_id());
                    serviceJson.put("name", services.get(i).getService_name());
                    serviceJson.put("type", services.get(i).getService_type());
                    serviceJson.put("category", services.get(i).getService_category());
                    serviceJson.put("platform", services.get(i).getPlatform());
                    serviceJson.put("rate", rate);
                    serviceJson.put("min", services.get(i).getMin_quantity());
                    serviceJson.put("max", services.get(i).getMax_quantity());
                    arr_Service.add(serviceJson);
                }
                return new ResponseEntity<String>(arr_Service.toJSONString(), HttpStatus.OK);
            }
            if (data.getAction().equals("balance")) {
                JSONObject serviceJson = new JSONObject();
                serviceJson.put("balance", user.getBalance());
                serviceJson.put("currency", "USD");
                return new ResponseEntity<String>(serviceJson.toJSONString(), HttpStatus.OK);
            }

            if (data.getAction().equals("status")) {
                if (data.getOrders().length() == 0) {
                    OrderRunning orderRunning = orderRunningRepository.get_Order_By_Id(data.getOrder());
                    OrderHistory orderHistory = orderHistoryRepository.get_Order_By_Id(data.getOrder());
                    if (orderRunning != null) {
                        resp.put("start_count", orderRunning.getStart_count());
                        resp.put("current_count", orderRunning.getTotal() + orderRunning.getStart_count());
                        resp.put("charge", orderRunning.getCharge());
                        if (orderRunning.getThread() <= 0) {
                            resp.put("status", "Pending");
                        } else {
                            resp.put("status", "In progress");
                        }
                        resp.put("remains", orderRunning.getQuantity() - orderRunning.getTotal());
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    } else {
                        if (orderHistory == null) {
                            resp.put("error", "Incorrect order ID");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        } else {
                            resp.put("start_count", orderHistory.getStart_count());
                            resp.put("current_count", orderHistory.getStart_count() + orderHistory.getTotal());
                            resp.put("charge", orderHistory.getCharge());
                            if (orderHistory.getCancel() == 1) {
                                resp.put("status", "Canceled");
                            } else if (orderHistory.getCancel() == 2) {
                                resp.put("status", "Partial");
                            } else {
                                resp.put("status", "Completed");
                            }
                            resp.put("remains", orderHistory.getQuantity() - orderHistory.getTotal());
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                    }

                } else {
                    List<String> ordersArrInput = new ArrayList<>();
                    ordersArrInput.addAll(Arrays.asList(data.getOrders().split(",")));
                    String listId = String.join(",", ordersArrInput);
                    List<OrderRunning> orderRunnings = orderRunningRepository.get_Order_By_ListId(ordersArrInput);
                    JSONObject orderList = new JSONObject();
                    for (OrderRunning order : orderRunnings) {
                        JSONObject orderJson = new JSONObject();
                        orderJson.put("start_count", order.getStart_count());
                        orderJson.put("current_count", order.getStart_count() + order.getTotal());
                        orderJson.put("charge", order.getCharge());
                        if (order.getThread() <=0) {
                            orderJson.put("status", "Pending");
                        } else {
                            orderJson.put("status", "In progress");
                        }
                        //videoview.put("status", "In progress");
                        orderJson.put("remains", order.getQuantity() - order.getTotal());
                        orderList.put("" + order.getOrder_id(), orderJson);
                        ordersArrInput.remove("" + order.getOrder_id());
                    }
                    String listIdHis = String.join(",", ordersArrInput);
                    List<OrderHistory> orderHistories = orderHistoryRepository.get_Order_By_ListId(ordersArrInput);
                    for (OrderHistory orderH : orderHistories) {
                        JSONObject orderJson = new JSONObject();
                        if (orderHistories != null) {
                            orderJson.put("start_count", orderH.getStart_count());
                            orderJson.put("current_count", orderH.getTotal() + orderH.getStart_count());
                            orderJson.put("charge", orderH.getCharge());
                            if (orderH.getCancel() == 1) {
                                orderJson.put("status", "Canceled");
                            } else if (orderH.getCancel() == 2) {
                                orderJson.put("status", "Partial");
                            } else {
                                orderJson.put("status", "Completed");
                            }
                            orderJson.put("remains", orderH.getQuantity() - orderH.getTotal());
                            orderList.put("" + orderH.getOrder_id(), orderJson);
                            ordersArrInput.remove("" + orderH.getOrder_id());
                        }
                    }
                    for (String orderId : ordersArrInput) {
                        JSONObject orderIdError = new JSONObject();
                        orderIdError.put("error", "Incorrect order ID");
                        orderList.put(orderId, orderIdError);
                    }
                    return new ResponseEntity<String>(orderList.toJSONString(), HttpStatus.OK);
                }
            }
            JSONObject get_task = null;
            if (data.getAction().equals("add")) {

                Service service = serviceRepository.get_Service(data.getService());
                if (service == null) {
                    resp.put("error", "Invalid service");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if(service.getPlatform().trim().equals("youtube")){
                    if(service.getTask().trim().equals("view")){
                        get_task=youtube_view(data,service,user);
                    }else if(service.getTask().trim().equals("like")){
                        get_task=youtube_like(data,service,user);
                    }else if(service.getTask().trim().equals("subscriber")){
                        get_task=youtube_subscriber(data,service,user);
                    }
                }else if(service.getPlatform().trim().equals("tiktok")){
                    if(service.getTask().trim().equals("follower")){
                        get_task=tiktok_follower(data,service,user);
                    }else if(service.getTask().trim().equals("like")){
                        get_task=tiktok_like(data,service,user);
                    }else if(service.getTask().trim().equals("comment")){
                        get_task=tiktok_comment(data,service,user);
                    }else if(service.getTask().trim().equals("view")){
                        get_task=tiktok_view(data,service,user);
                    }
                }
            }
            return new ResponseEntity<String>(get_task.toJSONString(), HttpStatus.OK);
        }catch (Exception e) {
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            System.out.println(stackTraceElement.getMethodName());
            System.out.println(stackTraceElement.getLineNumber());
            System.out.println(stackTraceElement.getClassName());
            System.out.println(stackTraceElement.getFileName());
            System.out.println("Error : " + e.getMessage());
            resp.put("error", "api system error");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
    }


    @PostMapping(value = "/web", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> web(@RequestBody DataRequest data, @RequestHeader(defaultValue = "") String Authorization) throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        try{
            User user = userRepository.find_User_By_Token(Authorization.trim());
            if (user==null) {
                resp.put("error", "Key not found");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            JSONObject get_task = null;
            Service service = serviceRepository.get_Service_Web(data.getService());
            if(service.getPlatform().trim().equals("youtube")){
                if(service.getTask().trim().equals("view")){
                    get_task=youtube_view(data,service,user);
                }else if(service.getTask().trim().equals("like")){
                    get_task=youtube_like(data,service,user);
                }else if(service.getTask().trim().equals("subscriber")){
                    get_task=youtube_subscriber(data,service,user);
                }
            }else if(service.getPlatform().trim().equals("tiktok")){
                if(service.getTask().trim().equals("follower")){
                    get_task=tiktok_follower(data,service,user);
                }else if(service.getTask().trim().equals("like")){
                    get_task=tiktok_like(data,service,user);
                }else if(service.getTask().trim().equals("comment")){
                    get_task=tiktok_comment(data,service,user);
                }else if(service.getTask().trim().equals("view")){
                    get_task=tiktok_view(data,service,user);
                }
            }
            if(get_task.get("error")==null){
                resp.put("order_running", true);
                resp.put("order_id",get_task.get("order"));
            }else{
                resp.put("error", get_task.get("error"));
            }
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch (Exception e) {
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            System.out.println(stackTraceElement.getMethodName());
            System.out.println(stackTraceElement.getLineNumber());
            System.out.println(stackTraceElement.getClassName());
            System.out.println(stackTraceElement.getFileName());
            System.out.println("Error : " + e.getMessage());
            resp.put("error", "api system error");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
    }


    JSONObject youtube_view(DataRequest data,Service service,User user)  throws IOException, ParseException{
        JSONObject resp = new JSONObject();
        try{
            String videoId = GoogleApi.getYoutubeId(data.getLink());
            if (videoId == null) {
                resp.put("error", "Cant filter videoid from link");
                return resp;
            }
            if (orderRunningRepository.get_Order_By_Order_Key_And_Task(videoId.trim(),service.getTask()) > 0) {
                resp.put("error", "This video in process");
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
                    if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 60) {
                        resp.put("error", "Videos under 60 seconds");
                        return resp;
                    }
                    if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 600&&service.getCheck_time()==1&&service.getMax_time()==10) {
                        resp.put("error", "Video under 10 minutes");
                        return resp;
                    }
                    if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 900&&service.getCheck_time()==1&&service.getMax_time()==15) {
                        resp.put("error", "Video under 15 minutes");
                        return resp;
                    }
                    if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 1800&&service.getCheck_time()==1&&service.getMax_time()==30) {
                        resp.put("error", "Video under 30 minutes");
                        return resp;
                    }
                    if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 3600&&service.getCheck_time()==1&&service.getMax_time()==60) {
                        resp.put("error", "Video under 60 minutes");
                        return resp;
                    }
                    if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 7200&&service.getCheck_time()==1&&service.getMax_time()==120) {
                        resp.put("error", "Video under 120 minutes");
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

                    int duration_min=2+(int)(Duration.parse(contentDetails.get("duration").toString()).getSeconds()/60);
                    int thread_set=50;
                    if(duration_min<service.getMin_time()){
                        thread_set= service.getCheck_time()==0?(data.getQuantity() / (60/(duration_min==1?10:duration_min)*2)):(int)(data.getQuantity()/2.6);
                    }else{
                        thread_set= service.getCheck_time()==0?(data.getQuantity() / (60/(service.getMax_time()==1?10:service.getMax_time())*2)):(int)(data.getQuantity()/2.6);
                    }
                    if (thread_set <= 5000){
                        orderRunning.setThread_set(thread_set);
                    }else{
                        orderRunning.setThread_set(5000);
                        thread_set=5000;
                    }
                    Long scheduledStartTime=0L;
                    if (snippet.get("liveBroadcastContent").toString().equals("none")) {
                        if(service.getCheck_time()==1) {
                            orderRunning.setThread(-1);
                            orderRunning.setStart_time(0L);
                        }else {
                            orderRunning.setThread((int)(thread_set*0.05<1?2:(thread_set*0.05)));
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
                    orderRunning.setUser(user);
                    orderRunning.setChannel_id(snippet.get("channelId").toString());
                    orderRunning.setChannel_title(snippet.get("channelTitle").toString());
                    orderRunning.setVideo_title(snippet.get("title").toString());
                    orderRunning.setOrder_key(video.get("id").toString());
                    orderRunning.setStart_count(Integer.parseInt(statistics.get("viewCount").toString()));
                    ////////////////
                    orderRunning.setCharge(priceorder);
                    orderRunning.setUpdate_time(0L);
                    orderRunning.setCurrent_count(0);
                    orderRunning.setNote("");
                    orderRunning.setService(service);
                    orderRunning.setSpeed_up(0);
                    orderRunning.setValid(1);

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
                    System.out.println(stackTraceElement.getMethodName());
                    System.out.println(stackTraceElement.getLineNumber());
                    System.out.println(stackTraceElement.getClassName());
                    System.out.println(stackTraceElement.getFileName());
                    System.out.println("Error : " + e.getMessage());
                    resp.put("error", "Cant insert video");
                    return resp;
                }
            }
            return resp;
        }catch (Exception e) {
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            System.out.println(stackTraceElement.getMethodName());
            System.out.println(stackTraceElement.getLineNumber());
            System.out.println(stackTraceElement.getClassName());
            System.out.println(stackTraceElement.getFileName());
            System.out.println("Error : " + e.getMessage());
            resp.put("error", "Cant insert video");
            return resp;
        }
    }

    JSONObject youtube_like(DataRequest data,Service service,User user)  throws IOException, ParseException{
        JSONObject resp = new JSONObject();
        try{
            String videoId = GoogleApi.getYoutubeId(data.getLink());
            if (videoId == null) {
                resp.put("error", "Cant filter videoid from link");
                return resp;
            }
            if (orderRunningRepository.get_Order_By_Order_Key_And_Task(videoId.trim(),service.getTask()) > 0) {
                resp.put("error", "This video in process");
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
                    int thread=(int)(data.getQuantity()/1000)*service.getThread();
                    if(thread<2){
                        orderRunning.setThread(5);
                    }else{
                        orderRunning.setThread(thread);
                    }
                    orderRunning.setThread_set(0);
                    orderRunning.setDuration(Duration.parse(contentDetails.get("duration").toString()).getSeconds());
                    orderRunning.setInsert_time(System.currentTimeMillis());
                    orderRunning.setStart_time(System.currentTimeMillis());
                    orderRunning.setTotal(0);
                    orderRunning.setTime_total(0);
                    orderRunning.setQuantity(data.getQuantity());
                    orderRunning.setUser(user);
                    orderRunning.setChannel_id(snippet.get("channelId").toString());
                    orderRunning.setChannel_title(snippet.get("channelTitle").toString());
                    orderRunning.setVideo_title(snippet.get("title").toString());
                    orderRunning.setOrder_key(videoId);
                    orderRunning.setStart_count(Integer.parseInt(statistics.get("likeCount").toString()));
                    ////////////////
                    orderRunning.setUpdate_time(0L);
                    orderRunning.setCurrent_count(0);
                    orderRunning.setCharge(priceorder);
                    orderRunning.setNote("");
                    orderRunning.setService(service);
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
                    System.out.println(stackTraceElement.getMethodName());
                    System.out.println(stackTraceElement.getLineNumber());
                    System.out.println(stackTraceElement.getClassName());
                    System.out.println(stackTraceElement.getFileName());
                    System.out.println("Error : " + e.getMessage());
                    resp.put("error", "Cant insert video");
                    return resp;
                }
            }
            return resp;
        }catch (Exception e) {
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            System.out.println(stackTraceElement.getMethodName());
            System.out.println(stackTraceElement.getLineNumber());
            System.out.println(stackTraceElement.getClassName());
            System.out.println(stackTraceElement.getFileName());
            System.out.println("Error : " + e.getMessage());
            resp.put("error", "Cant insert video");
            return resp;
        }
    }

    JSONObject youtube_subscriber(DataRequest data,Service service,User user)  throws IOException, ParseException{
        JSONObject resp = new JSONObject();
        try{
            String channelId = GoogleApi.getChannelId(data.getLink());
            System.out.println(channelId);
            if (channelId == null) {
                resp.put("error", "Cant filter channel from link");
                return resp;
            }
            String title=channelId.split(",")[0];
            String uId=channelId.split(",")[1];
            if (orderRunningRepository.get_Order_By_Order_Key_And_Task(uId.trim(),service.getTask()) > 0) {
                resp.put("error", "This channel in process");
                return resp;
            }
            List<String> videoList =GoogleApi.getVideoLinks("https://www.youtube.com/channel/"+uId+"/videos");
            if(videoList.size()<3){
                resp.put("error", "Can't get video info");
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
            int thread=(int)(data.getQuantity()/1000)*service.getThread();
            if(thread<2){
                orderRunning.setThread(2);
            }else{
                orderRunning.setThread(thread);
            }
            orderRunning.setThread_set(0);
            orderRunning.setDuration(0L);
            orderRunning.setInsert_time(System.currentTimeMillis());
            orderRunning.setStart_time(System.currentTimeMillis());
            orderRunning.setTotal(0);
            orderRunning.setTime_total(0);
            orderRunning.setUpdate_time(0L);
            orderRunning.setQuantity(data.getQuantity());
            orderRunning.setUser(user);
            orderRunning.setChannel_id(uId);
            orderRunning.setChannel_title(title);
            orderRunning.setVideo_title("");
            orderRunning.setOrder_key(uId);
            orderRunning.setStart_count(start_Count);
            orderRunning.setCurrent_count(0);
            ////////////////
            orderRunning.setCharge(priceorder);
            orderRunning.setNote("");
            orderRunning.setService(service);
            orderRunning.setValid(1);
            orderRunning.setSpeed_up(0);
            orderRunningRepository.save(orderRunning);

            for (int i=0;i<videoList.size();i++){
                String [] video_Info =videoList.get(i).split("~#");
                DataSubscriber dataSubscriber=new DataSubscriber();
                dataSubscriber.setVideo_id(video_Info[0].trim());
                dataSubscriber.setVideo_title(video_Info[1].trim());
                dataSubscriber.setState(1);
                dataSubscriber.setAdd_time(System.currentTimeMillis());
                dataSubscriber.setOrderRunning(orderRunning);
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
            System.out.println(stackTraceElement.getMethodName());
            System.out.println(stackTraceElement.getLineNumber());
            System.out.println(stackTraceElement.getClassName());
            System.out.println(stackTraceElement.getFileName());
            System.out.println("Error : " + e.getMessage());
            resp.put("error", "Cant insert video");
            return resp;
        }
    }

    JSONObject tiktok_follower(DataRequest data,Service service,User user)  throws IOException, ParseException{
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
            System.out.println(stackTraceElement.getMethodName());
            System.out.println(stackTraceElement.getLineNumber());
            System.out.println(stackTraceElement.getClassName());
            System.out.println(stackTraceElement.getFileName());
            System.out.println("Error : " + e.getMessage());
            resp.put("error", "Cant insert video");
            return resp;
        }
    }

    JSONObject tiktok_like(DataRequest data,Service service,User user)  throws IOException, ParseException{
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
            Integer like_count=-TikTokApi.getCountLike(video_id.trim());
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
            System.out.println(stackTraceElement.getMethodName());
            System.out.println(stackTraceElement.getLineNumber());
            System.out.println(stackTraceElement.getClassName());
            System.out.println(stackTraceElement.getFileName());
            System.out.println("Error : " + e.getMessage());
            resp.put("error", "Cant insert video");
            return resp;
        }
    }

    JSONObject tiktok_comment(DataRequest data,Service service,User user)  throws IOException, ParseException{
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
            Integer comment_count=-TikTokApi.getCountComment(video_id.trim());
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
            System.out.println(stackTraceElement.getMethodName());
            System.out.println(stackTraceElement.getLineNumber());
            System.out.println(stackTraceElement.getClassName());
            System.out.println(stackTraceElement.getFileName());
            System.out.println("Error : " + e.getMessage());
            resp.put("error", "Cant insert video");
            return resp;
        }
    }

    JSONObject tiktok_view(DataRequest data,Service service,User user)  throws IOException, ParseException{
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
            Integer view_count=-TikTokApi.getCountView(video_id.trim());
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
            System.out.println(stackTraceElement.getMethodName());
            System.out.println(stackTraceElement.getLineNumber());
            System.out.println(stackTraceElement.getClassName());
            System.out.println(stackTraceElement.getFileName());
            System.out.println("Error : " + e.getMessage());
            resp.put("error", "Cant insert video");
            return resp;
        }
    }
}
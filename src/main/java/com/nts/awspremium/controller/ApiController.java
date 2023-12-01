package com.nts.awspremium.controller;

import com.nts.awspremium.GoogleApi;
import com.nts.awspremium.model.*;
import com.nts.awspremium.repositories.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/api")
public class ApiController {
    @Autowired
    private VideoViewRepository videoViewRepository;
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private VideoViewHistoryRepository videoViewHistoryRepository;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private SettingRepository settingRepository;
    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private DataOrderRepository dataOrderRepository;

    @Autowired
    private LimitServiceRepository limitServiceRepository;

    @Autowired
    private VpsRepository vpsRepository;


    @PostMapping(value = "/view", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> view(DataRequest data) throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        try {
            List<Admin> admins = adminRepository.FindByToken(data.getKey().trim());
            if (data.getKey().length() == 0 || admins.size() == 0) {
                resp.put("error", "Key not found");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            //Danh sách dịch vụ view cmc
            if (data.getAction().equals("services")) {
                List<Service> services = serviceRepository.getAllService();
                JSONArray arr = new JSONArray();
                float rate;
                for (int i = 0; i < services.size(); i++) {
                    rate = services.get(i).getRate() * ((float) (admins.get(0).getRate()) / 100) * ((float) (100 - admins.get(0).getDiscount()) / 100);
                    JSONObject serviceBuffH = new JSONObject();
                    serviceBuffH.put("service", services.get(i).getService());
                    serviceBuffH.put("name", services.get(i).getName());
                    serviceBuffH.put("type", services.get(i).getType());
                    serviceBuffH.put("category", services.get(i).getCategory());
                    serviceBuffH.put("rate", rate);
                    serviceBuffH.put("min", services.get(i).getMin());
                    serviceBuffH.put("max", services.get(i).getMax());
                    arr.add(serviceBuffH);
                }
                return new ResponseEntity<String>(arr.toJSONString(), HttpStatus.OK);
            }
            //truy vấn số dư tài khoản
            if (data.getAction().equals("balance")) {
                JSONObject serviceBuffH = new JSONObject();
                serviceBuffH.put("balance", admins.get(0).getBalance());
                serviceBuffH.put("currency", "USD");
                return new ResponseEntity<String>(serviceBuffH.toJSONString(), HttpStatus.OK);
            }
            //Get trạng thái đơns
            if (data.getAction().equals("status")) {
                if (data.getOrders().length() == 0) {
                    VideoView video = videoViewRepository.getVideoViewById(data.getOrder());
                    VideoViewHistory videoHistory = videoViewHistoryRepository.getVideoViewHisById(data.getOrder());
                    if (video != null) {
                        resp.put("start_count", video.getViewstart());
                        resp.put("current_count", video.getViewtotal() + video.getViewstart());
                        resp.put("charge", video.getPrice());
                        if (video.getMaxthreads() <= 0) {
                            resp.put("status", "Pending");
                        } else {
                            resp.put("status", "In progress");
                        }
                        resp.put("remains", video.getVieworder() - video.getViewtotal());
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    } else {
                        if (videoHistory == null) {
                            resp.put("error", "Incorrect order ID");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        } else {
                            resp.put("start_count", videoHistory.getViewstart());
                            resp.put("current_count", videoHistory.getViewstart() + videoHistory.getViewtotal());
                            resp.put("charge", videoHistory.getPrice());
                            if (videoHistory.getCancel() == 1) {
                                resp.put("status", "Canceled");
                            } else if (videoHistory.getCancel() == 2) {
                                resp.put("status", "Partial");
                            } else {
                                resp.put("status", "Completed");
                            }
                            resp.put("remains", videoHistory.getVieworder() - videoHistory.getViewtotal());
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                    }

                } else {
                    List<String> ordersArrInput = new ArrayList<>();
                    ordersArrInput.addAll(Arrays.asList(data.getOrders().split(",")));
                    String listId = String.join(",", ordersArrInput);
                    List<VideoView> videoViews = videoViewRepository.getVideoViewByListId(ordersArrInput);
                    JSONObject videosview = new JSONObject();
                    for (VideoView v : videoViews) {
                        JSONObject videoview = new JSONObject();
                        videoview.put("start_count", v.getViewstart());
                        videoview.put("current_count", v.getViewstart() + v.getViewtotal());
                        videoview.put("charge", v.getPrice());
                        if (v.getMaxthreads() <=0) {
                            videoview.put("status", "Pending");
                        } else {
                            videoview.put("status", "In progress");
                        }
                        //videoview.put("status", "In progress");
                        videoview.put("remains", v.getVieworder() - v.getViewtotal());
                        videosview.put("" + v.getOrderid(), videoview);
                        ordersArrInput.remove("" + v.getOrderid());
                    }
                    String listIdHis = String.join(",", ordersArrInput);
                    List<VideoViewHistory> videoViewHistory = videoViewHistoryRepository.getVideoViewHisByListId(ordersArrInput);
                    for (VideoViewHistory vh : videoViewHistory) {
                        JSONObject videohisview = new JSONObject();
                        if (videoViewHistory != null) {
                            videohisview.put("start_count", vh.getViewstart());
                            videohisview.put("current_count", vh.getViewtotal() + vh.getViewstart());
                            videohisview.put("charge", vh.getPrice());
                            if (vh.getCancel() == 1) {
                                videohisview.put("status", "Canceled");
                            } else if (vh.getCancel() == 2) {
                                videohisview.put("status", "Partial");
                            } else {
                                videohisview.put("status", "Completed");
                            }
                            videohisview.put("remains", vh.getVieworder() - vh.getViewtotal());
                            videosview.put("" + vh.getOrderid(), videohisview);
                            ordersArrInput.remove("" + vh.getOrderid());
                        }
                    }
                    for (String orderId : ordersArrInput) {
                        JSONObject orderIdError = new JSONObject();
                        orderIdError.put("error", "Incorrect order ID");
                        videosview.put(orderId, orderIdError);
                    }
                    return new ResponseEntity<String>(videosview.toJSONString(), HttpStatus.OK);
                }
            }
            if (data.getAction().equals("add")) {

                Service service = serviceRepository.getService(data.getService());
                if (service == null) {
                    resp.put("error", "Invalid service");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

                }
                Setting setting = settingRepository.getReferenceById(1L);
                /*
                if(service.getLive()==1){
                    if((int)(vpsRepository.getSumThreadLive()*0.8)+10000<=videoViewRepository.getSumThreadLive()+data.getQuantity()){
                        resp.put("error", "System busy try again");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                }
                 */
                Integer limitService=limitServiceRepository.getLimitPendingByServiceAndUser(admins.get(0).getUsername().trim(),service.getService());
                if(limitService!=null){
                    if((videoViewRepository.getCountOrderByUserAndService(admins.get(0).getUsername().trim(),service.getService())==null?false:videoViewRepository.getCountOrderByUserAndService(admins.get(0).getUsername().trim(),service.getService())>=limitService*service.getMax())||limitService==0){
                        resp.put("error", "System busy try again");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                }else{
                    if(service.getChecktime()==1){
                        resp.put("error", "System busy try again");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                }
                if (videoViewRepository.getCountOrderByUser(admins.get(0).getUsername().trim()) >= admins.get(0).getMaxorder() || (service.getGeo().equals("vn") && settingRepository.getMaxOrderVN() == 0) ||
                        (service.getGeo().equals("us") && settingRepository.getMaxOrderUS() == 0) || service.getMaxorder() <= videoViewRepository.getCountOrderByService(data.getService())) {
                    resp.put("error", "System busy try again");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if (service.getType().equals("Special") && data.getList().length() == 0) {
                    resp.put("error", "Keyword is null");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if (service.getType().equals("Special") && data.getList().length() == 0) {
                    resp.put("error", "Keyword is null");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if (data.getQuantity() > service.getMax() || data.getQuantity() < service.getMin()) {
                    resp.put("error", "Min/Max order is: " + service.getMin() + "/" + service.getMax());
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                ////////////////////////////////
                String videolist = GoogleApi.getYoutubeId(data.getLink());
                if (videolist == null) {
                    resp.put("error", "Cant filter videoid from link");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                Long last_order_done=videoViewHistoryRepository.checkOrderDoneThan48h(videolist.trim());
                if(last_order_done!=null){
                    if(System.currentTimeMillis()-last_order_done<0){
                        Date date = new Date(last_order_done);
                        SimpleDateFormat format = new SimpleDateFormat("HH:mm yyyy/MM/dd");
                        format.setTimeZone(TimeZone.getTimeZone("Asia/Bangkok"));
                        resp.put("error", "Please order after "+ format.format(date)+ " GMT+7");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                }
                //VIDEOOOOOOOOOOOOOOO
                OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
                Random ran = new Random();
                Request request1 = null;
                String[] key={"AIzaSyANGR4QQn8T3K9V-9TU5Z1i4eOfPg0vEvY","AIzaSyClOKa8qUz3MJD1RKBsjlIDR5KstE2NmMY","AIzaSyCp0GVPdewYRK1fOazk-1UwqdPphzQqn98=","AIzaSyCzYRvwOcNniz3WPYyLQSBCsT2U05_mmmQ"};
                request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key="+key[ran.nextInt(key.length)]+"&fields=items(id,snippet(title,channelId,liveBroadcastContent),statistics(viewCount),contentDetails(duration))&part=snippet,statistics,contentDetails&id=" + videolist).get().build();

                Response response1 = client1.newCall(request1).execute();

                String resultJson1 = response1.body().string();
                Object obj1 = new JSONParser().parse(resultJson1);
                JSONObject jsonObject1 = (JSONObject) obj1;
                JSONArray items = (JSONArray) jsonObject1.get("items");
                if (items == null) {
                    resp.put("error", "Can't get video info");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                Iterator k = items.iterator();
                if (k.hasNext() == false) {
                    resp.put("error", "Can't get video info");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                /////////////////////////////////////////////
                while (k.hasNext()) {
                    try {
                        JSONObject video = (JSONObject) k.next();
                        JSONObject contentDetails = (JSONObject) video.get("contentDetails");
                        if (videoViewRepository.getCountVideoId(video.get("id").toString().trim()) > 0) {
                            resp.put("error", "This video in process");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                        if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() == 0&&service.getLive()==0) {
                            resp.put("error", "This video is a livestream video");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                        if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() != 0&&service.getLive()==1) {
                            resp.put("error", "This video is not a livestream video");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                        if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 60&&service.getLive()==0) {
                            resp.put("error", "Videos under 60 seconds");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                        if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 600&&service.getLive()==0 &&service.getChecktime()==1&&service.getMaxtime()==10) {
                            resp.put("error", "Video under 10 minutes");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                        if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 900&&service.getLive()==0 &&service.getChecktime()==1&&service.getMintime()==15) {
                            resp.put("error", "Video under 15 minutes");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                        if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 1800&&service.getLive()==0 &&service.getChecktime()==1&&service.getMintime()==30) {
                            resp.put("error", "Video under 30 minutes");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                        if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 3600&&service.getLive()==0 &&service.getChecktime()==1&&service.getMintime()==60) {
                            resp.put("error", "Video under 60 minutes");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                        if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 7200&&service.getLive()==0 &&service.getChecktime()==1&&service.getMintime()==120) {
                            resp.put("error", "Video under 120 minutes");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                        /*
                        if(liveStreamingDetails!=null&&Duration.parse(contentDetails.get("duration").toString()).getSeconds() != 0){
                            Instant instant = Instant.parse(liveStreamingDetails.get("actualEndTime").toString());
                            long longTime = instant.getEpochSecond();
                            if((System.currentTimeMillis()/1000-longTime)/60/60<24){
                                resp.put("error", "Livesteam video completed less than 24h");
                                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                            }
                        }
                         */
                        float priceorder = 0;
                        int time = 0;
                        priceorder = (data.getQuantity() / 1000F) * service.getRate() * ((float) (admins.get(0).getRate()) / 100) * ((float) (100 - admins.get(0).getDiscount()) / 100);
                        if (priceorder > (float) admins.get(0).getBalance()) {
                            resp.put("error", "Your balance not enough");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                        JSONObject snippet = (JSONObject) video.get("snippet");
                        /*
                        if(!snippet.get("liveBroadcastContent").toString().equals("none")){
                            resp.put("error", "This video is not a pure public video");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                         */
                        JSONObject statistics = (JSONObject) video.get("statistics");
                        VideoView videoViewhnew = new VideoView();

                        //int thread_set_fix = service.getThread() + ((int) (data.getQuantity() / 1000) - 1) * setting.getLevelthread();
                        int thread_set = data.getQuantity() / (60/service.getMaxtime()*3);
                        if (thread_set <= setting.getMaxthread()){
                            videoViewhnew.setThreadset(thread_set);
                        }else{
                            videoViewhnew.setThreadset(setting.getMaxthread());
                            thread_set=setting.getMaxthread();
                        }

                        if (snippet.get("liveBroadcastContent").toString().equals("none")) {
                            /*
                            int max_thread = service.getThread() + ((int) (data.getQuantity() / 1000) - 1) * setting.getLevelthread();
                            if (max_thread <= setting.getMaxthread()&&limitService==null) {
                                videoViewhnew.setMaxthreads(max_thread);
                            } else if(limitService!=null) {
                                videoViewhnew.setMaxthreads(-1);
                            }else {
                                videoViewhnew.setMaxthreads(setting.getMaxthread());
                            }
                            if(limitService!=null){
                                videoViewhnew.setTimestart(0L);
                            }else{
                                videoViewhnew.setTimestart(System.currentTimeMillis());
                            }
                             */
                            if(limitService!=null) {
                                videoViewhnew.setMaxthreads(-1);
                                videoViewhnew.setTimestart(0L);
                            }else {
                                videoViewhnew.setMaxthreads((int)(thread_set*0.05));
                                videoViewhnew.setTimestart(System.currentTimeMillis());
                            }
                            videoViewhnew.setMinstart(service.getMaxtime());
                        } else if (snippet.get("liveBroadcastContent").toString().equals("live")&& service.getLive()==1) {
                            videoViewhnew.setMaxthreads(data.getQuantity()+(int)(data.getQuantity()*(setting.getBonus()/100F)));
                            videoViewhnew.setTimestart(System.currentTimeMillis());
                            videoViewhnew.setMinstart(service.getMaxtime());
                        }else{
                            videoViewhnew.setMaxthreads(0);
                            videoViewhnew.setTimestart(0L);
                            videoViewhnew.setMinstart(service.getMaxtime());
                        }
                        videoViewhnew.setDuration(Duration.parse(contentDetails.get("duration").toString()).getSeconds());
                        videoViewhnew.setInsertdate(System.currentTimeMillis());
                        videoViewhnew.setView24h(0);
                        videoViewhnew.setViewtotal(0);
                        videoViewhnew.setTimetotal(0);
                        videoViewhnew.setVieworder(data.getQuantity());
                        videoViewhnew.setUser(admins.get(0).getUsername());
                        videoViewhnew.setChannelid(snippet.get("channelId").toString());
                        videoViewhnew.setVideotitle(snippet.get("title").toString());
                        videoViewhnew.setVideoid(video.get("id").toString());
                        videoViewhnew.setViewstart(Integer.parseInt(statistics.get("viewCount").toString()));
                        ////////////////
                        videoViewhnew.setPrice(priceorder);
                        videoViewhnew.setNote("");
                        videoViewhnew.setService(data.getService());
                        videoViewhnew.setValid(1);
                        videoViewRepository.save(videoViewhnew);

                        if (service.getType().equals("Special")) {
                            DataOrder dataOrder = new DataOrder();
                            dataOrder.setOrderid(videoViewhnew.getOrderid());
                            dataOrder.setListvideo(data.getList());
                            dataOrder.setListkey(data.getList());
                            dataOrderRepository.save(dataOrder);
                        } else if (service.getType().equals("Special 1")) {
                            DataOrder dataOrder = new DataOrder();
                            dataOrder.setOrderid(videoViewhnew.getOrderid());
                            dataOrder.setListvideo(data.getSuggest());
                            dataOrder.setListkey(data.getSearch());
                            dataOrderRepository.save(dataOrder);
                        }

                        /*float balance_new = admins.get(0).getBalance() - priceorder;
                        adminRepository.updateBalance(balance_new, admins.get(0).getUsername());
                         */
                        Float balance_update=adminRepository.updateBalanceFine(-priceorder,admins.get(0).getUsername().trim());
                        Balance balance = new Balance();
                        balance.setUser(admins.get(0).getUsername().trim());
                        balance.setTime(System.currentTimeMillis());
                        balance.setTotalblance(balance_update);
                        balance.setBalance(-priceorder);
                        balance.setService(data.getService());
                        balance.setNote("Order " + data.getQuantity() + " view cho video " + videoViewhnew.getVideoid());
                        balanceRepository.save(balance);
                        resp.put("order", videoViewhnew.getOrderid());
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        //new Video(video.get("videoId").toString(), "channel_id", Duration.parse(video.get("duration").toString()).getSeconds(), video.get("title").toString());

                    } catch (Exception e) {
                        resp.put("error", "Cant insert video");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                }
            }
        } catch (Exception e) {
            resp.put("error", "api system error");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
        resp.put("error", "api system error");
        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
    }
}

package com.nts.awspremium.controller;

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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/api")
public class ApiController {
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private ChannelRepository channelRepository;
    @Autowired
    private ProxyRepository proxyRepository;
    @Autowired
    private AccountRepository accountRepository;
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
    private HistoryViewRepository historyViewRepository;
    @Autowired
    private HistorySumRepository historySumRepository;
    @Autowired
    private ProxyHistoryRepository proxyHistoryRepository;
    @Autowired
    private ServiceRepository serviceRepository;

    @PostMapping(value = "/view", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> buffh(DataRequest data) throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        List<Admin> admins = adminRepository.FindByToken(data.getKey().trim());
        if (data.getKey().length() == 0 || admins.size()==0) {
            resp.put("error", "Key not found");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
        Setting setting=settingRepository.getReferenceById(1L);
        //Danh sách dịch vụ view cmc
        if(data.getAction().equals("services")){
            List<Service> services =serviceRepository.getAllService();
            JSONArray arr = new JSONArray();

            for(int i=0;i<services.size();i++){
                JSONObject serviceBuffH= new JSONObject();
                serviceBuffH.put("service", services.get(i).getService());
                serviceBuffH.put("name", services.get(i).getName());
                serviceBuffH.put("type", services.get(i).getType());
                serviceBuffH.put("category", services.get(i).getCategory());
                serviceBuffH.put("rate", services.get(i).getRate());
                serviceBuffH.put("min", services.get(i).getMin());
                serviceBuffH.put("max", services.get(i).getMax());
                arr.add(serviceBuffH);
            }
            return new ResponseEntity<String>(arr.toJSONString(), HttpStatus.OK);
        }
        //truy vấn số dư tài khoản
        if(data.getAction().equals("balance")){
            JSONObject serviceBuffH= new JSONObject();
            serviceBuffH.put("balance", admins.get(0).getBalance());
            serviceBuffH.put("currency", "USD");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
        //Get trạng thái đơns
        if(data.getAction().equals("status")){
            if(data.getOrders().length()==0){
                VideoView video = videoViewRepository.getReferenceById(data.getOrder());
                VideoViewHistory videoHistory=videoViewHistoryRepository.getReferenceById(data.getOrder());

                if(video !=null){
                    resp.put("start_count",video.getViewstart());
                    resp.put("current_count",video.getViewtotal());
                    resp.put("charge", video.getPrice());
                    resp.put("status", "In progress");
                    resp.put("remains", video.getVieworder() - video.getViewtotal());
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }else{
                    if(videoHistory==null){
                        resp.put("error","Incorrect order ID");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }else{
                        resp.put("start_count",videoHistory.getViewstart());
                        resp.put("current_count",videoHistory.getViewbuffend());
                        resp.put("charge", videoHistory.getPrice());
                        if(videoHistory.getCancel()==1){
                            resp.put("status", "Canceled");
                        }else if(videoHistory.getCancel()==2){
                            resp.put("status", "Partial");
                        }else{
                            resp.put("status", "Completed");
                        }
                        resp.put("remains", videoHistory.getVieworder() - videoHistory.getViewbuffend());
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                }

            }else{
                List<String> ordersArrInput = new ArrayList<>();
                ordersArrInput.addAll(Arrays.asList(data.getOrders().split(",")));
                List<VideoView> videoViews= videoViewRepository.getVideoViewByListId(data.getOrders());
                JSONObject videosview = new JSONObject();
                for (VideoView v:videoViews){
                    JSONObject videoview = new JSONObject();
                    videoview.put("start_count",v.getViewstart());
                    videoview.put("current_count",v.getViewtotal());
                    videoview.put("charge", v.getPrice());
                    videoview.put("status", "In progress");
                    videoview.put("remains", v.getVieworder() - v.getViewtotal());
                    videosview.put(""+v.getVideoid(),videoview);
                    ordersArrInput.remove(""+v.getVideoid());
                }
                String listIdHis = String.join(",", ordersArrInput);
                List<VideoViewHistory> videoViewHistory= videoViewHistoryRepository.getVideoViewHisByListId(listIdHis);
                for(VideoViewHistory vh:videoViewHistory){
                    JSONObject videohisview = new JSONObject();
                    if(videoViewHistory!=null){
                        videohisview.put("start_count",vh.getViewstart());
                        videohisview.put("current_count",vh.getViewbuffend());
                        videohisview.put("charge", vh.getPrice());
                        if(vh.getCancel()==1){
                            videohisview.put("status", "Canceled");
                        }else if(vh.getCancel()==2){
                            videohisview.put("status", "Partial");
                        }else{
                            videohisview.put("status", "Completed");
                        }
                        videohisview.put("remains", vh.getVieworder() - vh.getViewbuffend());
                        videosview.put(""+vh.getVideoid(),videohisview);
                    }
                    ordersArrInput.remove(""+vh.getVideoid());
                }
                for (String orderId : ordersArrInput) {
                    JSONObject orderIdError = new JSONObject();
                    orderIdError.put("error", "Incorrect order ID");
                    videosview.put(orderId, orderIdError);
                }
                return new ResponseEntity<String>(videosview.toJSONString(), HttpStatus.OK);
            }
        }
        if(data.getAction().equals("add")){
            if (data.getQuantity() < 100) {
                resp.put("error", "Min quantity is 100");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            if (data.getQuantity() > 120000) {
                resp.put("error", "Max quantity is 120000");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            if(videoViewRepository.getCountOrderByUser(admins.get(0).getUsername().trim())>=admins.get(0).getMaxorder() || settingRepository.getMaxOrder()==0){
                resp.put("error", "System busy try again");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            ////////////////////////////////
            String videolist = data.getLink().replace("\n", ",");
            //VIDEOOOOOOOOOOOOOOO
            int count = StringUtils.countOccurrencesOf(videolist, ",") + 1;
            OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

            Request request1 = null;

            request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=AIzaSyClOKa8qUz3MJD1RKBsjlIDR5KstE2NmMY&fields=items(id,snippet(title,channelId),statistics(viewCount),contentDetails(duration))&part=snippet,statistics,contentDetails&id=" + videolist).get().build();

            Response response1 = client1.newCall(request1).execute();

            String resultJson1 = response1.body().string();

            Object obj1 = new JSONParser().parse(resultJson1);

            JSONObject jsonObject1 = (JSONObject) obj1;
            JSONArray items = (JSONArray) jsonObject1.get("items");
            if(items==null){
                resp.put("error","Cant load info of video");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            Iterator k = items.iterator();
            /////////////////////////////////////////////
            while (k.hasNext()) {
                try {
                    JSONObject video = (JSONObject) k.next();
                    JSONObject contentDetails = (JSONObject) video.get("contentDetails");
                    if(videoViewRepository.getCountVideoId(video.get("id").toString().trim())>0){
                        resp.put("error", "This video in process");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    Service service=serviceRepository.getService(data.getService());
                    if(service==null){
                        resp.put("error", "Service not found ");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    //System.out.println((float)(videoBuffh.getTimebuff())/4000*setting.getPricerate()*((float)(100-admins.get(0).getDiscount())/100));
                    float priceorder=0;
                    int time=0;
                    priceorder=(float)(data.getQuantity())/1000*setting.getPricerate()*((float)(100-admins.get(0).getDiscount())/100);

                    if(priceorder>(float)admins.get(0).getBalance()){
                        resp.put("error", "Your balance not enough");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    JSONObject snippet = (JSONObject) video.get("snippet");
                    JSONObject statistics = (JSONObject) video.get("statistics");
                    VideoView videoViewhnew= new VideoView();
                    videoViewhnew.setDuration(Duration.parse(contentDetails.get("duration").toString()).getSeconds());
                    videoViewhnew.setInsertdate(System.currentTimeMillis());
                    videoViewhnew.setUser(admins.get(0).getUsername());
                    videoViewhnew.setChannelid(snippet.get("channelId").toString());
                    videoViewhnew.setVideotitle(snippet.get("title").toString());
                    videoViewhnew.setVideoid(video.get("id").toString());
                    videoViewhnew.setViewstart(Integer.parseInt(statistics.get("viewCount").toString()));
                    videoViewhnew.setMaxthreads(200);
                    videoViewhnew.setPrice(priceorder);
                    videoViewhnew.setService(data.getService());
                    videoViewRepository.save(videoViewhnew);


                    float balance_new=admins.get(0).getBalance()-priceorder;
                    adminRepository.updateBalance(balance_new,admins.get(0).getUsername());
                    Balance balance=new Balance();
                    balance.setUser(admins.get(0).getUsername().trim());
                    balance.setTime(System.currentTimeMillis());
                    balance.setTotalblance(balance_new);
                    balance.setBalance(-priceorder);
                    balance.setNote("Order " +data.getQuantity() +"view cho video "+videoViewhnew.getVideoid());
                    balanceRepository.save(balance);
                    resp.put("order",videoViewhnew.getOrderid());
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    //new Video(video.get("videoId").toString(), "channel_id", Duration.parse(video.get("duration").toString()).getSeconds(), video.get("title").toString());

                } catch (Exception e) {
                    resp.put("error", "Cant insert video");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            }
        }
        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
    }
}

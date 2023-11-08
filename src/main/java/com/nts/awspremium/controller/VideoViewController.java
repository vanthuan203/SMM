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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/videoview")
public class VideoViewController {
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private SettingRepository settingRepository;
    @Autowired
    private VideoViewHistoryRepository videoViewHistoryRepository;
    @Autowired
    private VideoViewRepository videoViewRepository;
    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private DataOrderRepository dataOrderRepository;

    @Autowired
    private AutoRefillRepository autoRefillRepository;

    @Autowired
    private GoogleAPIKeyRepository googleAPIKeyRepository;

    @Autowired
    private LimitServiceRepository limitServiceRepository;

    @PostMapping(value = "/orderview", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> orderview(@RequestBody VideoView videoView, @RequestHeader(defaultValue = "") String Authorization) throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        //System.out.println(videoView.getService());
        try {
            List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
            if (Authorization.length() == 0 || admins.size() == 0) {
                resp.put("status", "fail");
                resp.put("message", "Token expired");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            Service service = serviceRepository.getServiceNoCheckEnabled(videoView.getService());
            /*
            if (service == null) {
                resp.put("videoview", "Service not found ");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
             */
            /*
            if (videoView.getVieworder() > service.getMax() || videoView.getVieworder() < service.getMin()) {
                resp.put("error", "Min/Max order is: " + service.getMin() + "/" + service.getMax());
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
             */
            Integer limitService=limitServiceRepository.getLimitPendingByServiceAndUser(admins.get(0).getUsername().trim(),service.getService());
            if(limitService!=null){
                if((videoViewRepository.getCountOrderByUserAndService(admins.get(0).getUsername().trim(),service.getService())==null?false:videoViewRepository.getCountOrderByUserAndService(admins.get(0).getUsername().trim(),service.getService())>=limitService*service.getMax())||limitService==0){
                    resp.put("videoview", "System busy try again");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            }else{
                if(service.getChecktime()==1){
                    resp.put("videoview", "System busy try again");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            }
            if (videoViewRepository.getCountOrderByUser(admins.get(0).getUsername().trim()) >= admins.get(0).getMaxorder() || (service.getGeo().equals("vn") && settingRepository.getMaxOrderVN() == 0) ||
                    (service.getGeo().equals("us") && settingRepository.getMaxOrderUS() == 0) || service.getMaxorder() <= videoViewRepository.getCountOrderByService(videoView.getService())) {
                resp.put("videoview", "System busy try again!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            String videolist = GoogleApi.getYoutubeId(videoView.getVideoid());
            //VIDEOOOOOOOOOOOOOOO
            int count = StringUtils.countOccurrencesOf(videolist, ",") + 1;
            OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

            Request request1 = null;

            request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=AIzaSyBNcxI9kl_ODPwf49hMSHyohn6Q7IaKdMI&fields=items(id,snippet(title,channelId,liveBroadcastContent),statistics(viewCount),contentDetails(duration),liveStreamingDetails(scheduledStartTime))&part=liveStreamingDetails,snippet,statistics,contentDetails&id=" + videolist).get().build();

            Response response1 = client1.newCall(request1).execute();

            String resultJson1 = response1.body().string();

            Object obj1 = new JSONParser().parse(resultJson1);

            JSONObject jsonObject1 = (JSONObject) obj1;
            JSONArray items = (JSONArray) jsonObject1.get("items");
            if (items == null) {
                resp.put("videoview", "Fail check video!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            Iterator k = items.iterator();
            if (k.hasNext() == false) {
                resp.put("videoview", "Fail check video!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            Setting setting = settingRepository.getReferenceById(1L);
            while (k.hasNext()) {
                try {
                    JSONObject video = (JSONObject) k.next();
                    JSONObject contentDetails = (JSONObject) video.get("contentDetails");
                    if (videoViewRepository.getCountVideoId(video.get("id").toString().trim()) > 0) {
                        resp.put("videoview", "This video in process!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() != 0&&service.getLive()==1) {
                        resp.put("videoview", "This video is not a livestream video");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    /*
                    if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 600&&service.getLive()==0 &&service.getChecktime()==1&&service.getMaxtime()==10) {
                        resp.put("videoview", "Video under 10 minutes");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                     */
                    if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 900&&service.getLive()==0 &&service.getChecktime()==1&&service.getMintime()==15) {
                        resp.put("videoview", "Video under 15 minutes");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 1800&&service.getLive()==0 &&service.getChecktime()==1&&service.getMintime()==30) {
                        resp.put("videoview", "Video under 30 minutes");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 3600&&service.getLive()==0 &&service.getChecktime()==1&&service.getMintime()==60) {
                        resp.put("videoview", "Video under 60 minutes");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 7200&&service.getLive()==0 &&service.getChecktime()==1&&service.getMintime()==120) {
                        resp.put("videoview", "Video under 120 minutes");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    float priceorder = 0;
                    int time = 0;
                    priceorder = (videoView.getVieworder() / 1000F) * service.getRate() * ((float) (admins.get(0).getRate()) / 100) * ((float) (100 - admins.get(0).getDiscount()) / 100);
                    if (priceorder > (float) admins.get(0).getBalance()) {
                        resp.put("videoview", "Your balance not enough");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    JSONObject snippet = (JSONObject) video.get("snippet");
                    Long scheduledStartTime=0L;
                    if (!snippet.get("liveBroadcastContent").toString().equals("none")&&service.getLive()==0) {
                        resp.put("videoview", "This video is not a pure public video");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }else if(snippet.get("liveBroadcastContent").toString().equals("upcoming")&&service.getLive()==1){
                        JSONObject liveStreamingDetails = (JSONObject) video.get("liveStreamingDetails");
                        Instant instant = Instant.parse(liveStreamingDetails.get("scheduledStartTime").toString());
                        scheduledStartTime=instant.toEpochMilli();
                    }

                    JSONObject statistics = (JSONObject) video.get("statistics");
                    VideoView videoViewhnew = new VideoView();
                    videoViewhnew.setDuration(Duration.parse(contentDetails.get("duration").toString()).getSeconds());
                    videoViewhnew.setInsertdate(scheduledStartTime==0?System.currentTimeMillis():scheduledStartTime);
                    videoViewhnew.setView24h(0);
                    videoViewhnew.setViewtotal(0);
                    videoViewhnew.setTimetotal(0);
                    videoViewhnew.setVieworder(videoView.getVieworder());
                    videoViewhnew.setUser(admins.get(0).getUsername());
                    videoViewhnew.setChannelid(snippet.get("channelId").toString());
                    videoViewhnew.setVideotitle(snippet.get("title").toString());
                    videoViewhnew.setVideoid(video.get("id").toString());
                    videoViewhnew.setViewstart(Integer.parseInt(statistics.get("viewCount").toString()));
                    if(service.getChecktime()==1){
                        int thread_set = service.getThread() + ((int) (videoView.getVieworder() / 1000) - 1) * setting.getLevelthread();
                        if (thread_set <= setting.getMaxthread()){
                            videoViewhnew.setThreadset(thread_set);
                        }else{
                            videoViewhnew.setThreadset(setting.getMaxthread());
                        }
                        videoViewhnew.setTimestart(0L);
                        videoViewhnew.setMaxthreads(-1);
                    }else{
                        videoViewhnew.setTimestart(System.currentTimeMillis());
                        videoViewhnew.setMaxthreads(videoView.getMaxthreads());
                        videoViewhnew.setThreadset(videoView.getMaxthreads());
                    }
                    videoViewhnew.setPrice(priceorder);
                    videoViewhnew.setNote(videoView.getNote());
                    videoViewhnew.setService(videoView.getService());
                    videoViewhnew.setValid(1);
                    videoViewhnew.setMinstart(service.getMaxtime());
                    videoViewRepository.save(videoViewhnew);

                    Float balance_update=adminRepository.updateBalanceFine(-priceorder,admins.get(0).getUsername().trim());
                    Balance balance = new Balance();
                    balance.setUser(admins.get(0).getUsername().trim());
                    balance.setTime(System.currentTimeMillis());
                    balance.setTotalblance(balance_update);
                    balance.setBalance(-priceorder);
                    balance.setService(videoView.getService());
                    balance.setNote("Order " + videoView.getVieworder() + " view cho video " + videoViewhnew.getVideoid());
                    balanceRepository.save(balance);

                    resp.put("videoview", "true");
                    resp.put("balance", admins.get(0).getBalance());
                    resp.put("price", priceorder);
                    resp.put("time", time);
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    //new Video(video.get("videoId").toString(), "channel_id", Duration.parse(video.get("duration").toString()).getSeconds(), video.get("title").toString());

                } catch (Exception e) {
                    resp.put("videoview", e.getMessage());
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            }
            resp.put("videoview", "error");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("videoview", "Fail check video!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
    }


    @GetMapping(value = "/checkduration", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> checkduration(@RequestParam(defaultValue = "") String listvideo) throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        //System.out.println(listvideo);
        //VIDEOOOOOOOOOOOOOOO
        OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        Request request1 = null;

        request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=AIzaSyClOKa8qUz3MJD1RKBsjlIDR5KstE2NmMY&fields=items(id,contentDetails(duration))&part=contentDetails&id=" + listvideo).get().build();

        Response response1 = client1.newCall(request1).execute();

        String resultJson1 = response1.body().string();

        Object obj1 = new JSONParser().parse(resultJson1);

        JSONObject jsonObject1 = (JSONObject) obj1;
        JSONArray items = (JSONArray) jsonObject1.get("items");
        JSONArray jsonArray = new JSONArray();
        Iterator k = items.iterator();

        while (k.hasNext()) {
            try {
                JSONObject video = (JSONObject) k.next();
                JSONObject obj = new JSONObject();
                JSONObject contentDetails = (JSONObject) video.get("contentDetails");
                obj.put("duration", Duration.parse(contentDetails.get("duration").toString()).getSeconds());
                return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                //jsonArray.add(obj);
            } catch (Exception e) {
                resp.put("status", e);
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }

        }
        resp.put("durations", jsonArray);
        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
    }

    @GetMapping(value = "/updateviewendcron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateviewendcron() throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        List<String> listvideo = videoViewHistoryRepository.getVideoViewHistoriesCheckViewEnd(25);
        if (listvideo.size() == 0) {
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
        String s_videoid = "";
        for (int i = 0; i < listvideo.size(); i++) {
            if (i == 0) {
                s_videoid = listvideo.get(i);
            } else {
                s_videoid = s_videoid + "," + listvideo.get(i);
            }
        }
        //VIDEOOOOOOOOOOOOOOO
        OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        Request request1 = null;
        List<GoogleAPIKey> keys = googleAPIKeyRepository.getAllByState();
        request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=" + keys.get(0).getKey().trim() + "&fields=items(id,statistics(viewCount))&part=statistics&id=" + s_videoid).get().build();
        keys.get(0).setCount(keys.get(0).getCount() + 1L);
        googleAPIKeyRepository.save(keys.get(0));
        Response response1 = client1.newCall(request1).execute();

        String resultJson1 = response1.body().string();

        Object obj1 = new JSONParser().parse(resultJson1);

        JSONObject jsonObject1 = (JSONObject) obj1;
        JSONArray items = (JSONArray) jsonObject1.get("items");
        JSONArray jsonArray = new JSONArray();
        Iterator k = items.iterator();

        while (k.hasNext()) {
            try {
                JSONObject video = (JSONObject) k.next();
                JSONObject obj = new JSONObject();
                JSONObject statistics = (JSONObject) video.get("statistics");
                videoViewHistoryRepository.updateviewend(Integer.parseInt(statistics.get("viewCount").toString()),System.currentTimeMillis(), video.get("id").toString());
                //jsonArray.add(obj);
            } catch (Exception e) {
                resp.put("status", e);
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }

        }

        resp.put("status", "true");
        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
    }

    @GetMapping(value = "/updateviewendthan5hcron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateviewendthan5hcron() throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        List<String> listvideo = videoViewHistoryRepository.getVideoViewHistoriesCheckViewEndThan5h(25);
        if (listvideo.size() == 0) {
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
        String s_videoid = "";
        for (int i = 0; i < listvideo.size(); i++) {
            if (i == 0) {
                s_videoid = listvideo.get(i);
            } else {
                s_videoid = s_videoid + "," + listvideo.get(i);
            }
        }
        //VIDEOOOOOOOOOOOOOOO
        OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        Request request1 = null;
        List<GoogleAPIKey> keys = googleAPIKeyRepository.getAllByState();
        request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=" + keys.get(0).getKey().trim() + "&fields=items(id,statistics(viewCount))&part=statistics&id=" + s_videoid).get().build();
        keys.get(0).setCount(keys.get(0).getCount() + 1L);
        googleAPIKeyRepository.save(keys.get(0));
        Response response1 = client1.newCall(request1).execute();

        String resultJson1 = response1.body().string();

        Object obj1 = new JSONParser().parse(resultJson1);

        JSONObject jsonObject1 = (JSONObject) obj1;
        JSONArray items = (JSONArray) jsonObject1.get("items");
        JSONArray jsonArray = new JSONArray();
        Iterator k = items.iterator();

        while (k.hasNext()) {
            try {
                JSONObject video = (JSONObject) k.next();
                JSONObject obj = new JSONObject();
                JSONObject statistics = (JSONObject) video.get("statistics");
                videoViewHistoryRepository.updateviewendthan5h(Integer.parseInt(statistics.get("viewCount").toString()), video.get("id").toString());
                //jsonArray.add(obj);
            } catch (Exception e) {
                resp.put("status", e);
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }

        }

        resp.put("status", "true");
        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
    }

    @GetMapping(value = "/updateRunningOrder", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateRunningOrder() throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        List<VideoView> listvideo = videoViewRepository.getAllOrderPending();
        if (listvideo.size() == 0) {
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
        String s_videoid = "";
        for (int i = 0; i < listvideo.size(); i++) {
            if (i == 0) {
                s_videoid = listvideo.get(i).getVideoid();
            } else {
                s_videoid = s_videoid + "," + listvideo.get(i).getVideoid();
            }
            //System.out.println(s_videoid);
        }
        //VIDEOOOOOOOOOOOOOOO
        OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        Request request1 = null;
        List<GoogleAPIKey> keys = googleAPIKeyRepository.getAllByState();
        request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=" + keys.get(0).getKey().trim() + "&fields=items(id,snippet(liveBroadcastContent),statistics(viewCount))&part=snippet,statistics&id=" + s_videoid).get().build();
        keys.get(0).setCount(keys.get(0).getCount() + 1L);
        googleAPIKeyRepository.save(keys.get(0));
        Response response1 = client1.newCall(request1).execute();

        String resultJson1 = response1.body().string();

        Object obj1 = new JSONParser().parse(resultJson1);

        JSONObject jsonObject1 = (JSONObject) obj1;
        JSONArray items = (JSONArray) jsonObject1.get("items");
        if(items==null){
            resp.put("status", "fail");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
        JSONArray jsonArray = new JSONArray();
        Iterator k = items.iterator();
        Setting setting = settingRepository.getReferenceById(1L);
        while (k.hasNext()) {
            try {
                JSONObject video = (JSONObject) k.next();
                JSONObject obj = new JSONObject();
                JSONObject statistics = (JSONObject) video.get("statistics");
                JSONObject snippet = (JSONObject) video.get("snippet");
                if (snippet.get("liveBroadcastContent").toString().equals("none")) {
                    VideoView videoView = videoViewRepository.getVideoViewByVideoid(video.get("id").toString());
                    videoViewRepository.updatePendingOrderByVideoId(Integer.parseInt(statistics.get("viewCount").toString()),videoView.getMaxthreads()+ (int)(videoView.getThreadset()*0.05), System.currentTimeMillis(), video.get("id").toString());
                }
            } catch (Exception e) {
                resp.put("status", e);
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }

        }
        resp.put("status", "true");
        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
    }
    @GetMapping(value = "/updateRunningOrder701Cron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateRunningOrder701Cron() throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        List<VideoView> videoViews = videoViewRepository.getAllOrderPending701();
        Setting setting = settingRepository.getReferenceById(1L);
        TimeZone timeZone = TimeZone.getTimeZone("GMT+7");
        Calendar calendar = Calendar.getInstance(timeZone);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.HOUR_OF_DAY);
        if(hour>=15||hour<14){
            resp.put("status", "fail");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
        for (int i = 0; i < videoViews.size(); i++) {
            Service service = serviceRepository.getInfoService(videoViews.get(i).getService());
            Integer limitService=limitServiceRepository.getLimitRunningByServiceAndUser(videoViews.get(i).getUser().trim(),videoViews.get(i).getService());
            if(limitService!=null){
                if((videoViewRepository.getCountOrderRunningByUserAndService(videoViews.get(i).getUser().trim(),videoViews.get(i).getService())==null?false:videoViewRepository.getCountOrderRunningByUserAndService(videoViews.get(i).getUser().trim(),videoViews.get(i).getService())>=limitService*service.getMax())||limitService==0){
                    continue;
                }
            }
            int max_thread = service.getThread() + ((int) (videoViews.get(i).getVieworder() / 500)-1) *25;
            if (max_thread > setting.getMaxthread()) {
                max_thread = setting.getMaxthread();
            }
            videoViews.get(i).setMaxthreads(max_thread);
            videoViews.get(i).setTimestart(System.currentTimeMillis());
            videoViewRepository.save(videoViews.get(i));

        }
        resp.put("status", "true");
        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
    }

    @GetMapping(value = "/updateRunningOrder703CronOff", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateRunningOrder703CronOff() throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        List<VideoView> videoViews = videoViewRepository.getAllOrderPending701();
        Setting setting = settingRepository.getReferenceById(1L);
        TimeZone timeZone = TimeZone.getTimeZone("GMT+7");
        Calendar calendar = Calendar.getInstance(timeZone);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.HOUR_OF_DAY);
        if(hour>=11&&hour<14){
            resp.put("status", "fail");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
        for (int i = 0; i < videoViews.size(); i++) {
            Service service = serviceRepository.getInfoService(videoViews.get(i).getService());
            Integer CountOrderRunningByService=videoViewRepository.getCountOrderRunningByService(videoViews.get(i).getService());
            if((CountOrderRunningByService==null?false:CountOrderRunningByService>=setting.getMaxorder()*service.getMax())){
                break;
            }
            Integer limitService=limitServiceRepository.getLimitRunningByServiceAndUser(videoViews.get(i).getUser().trim(),videoViews.get(i).getService());
            Integer CountOrderRunningByUserAndService=videoViewRepository.getCountOrderRunningByUserAndService(videoViews.get(i).getUser().trim(),videoViews.get(i).getService());
            Integer CountOrderDoneByServiceAndUserInOneDay=videoViewHistoryRepository.getCountOrderDoneByServiceAndUserInOneDay(videoViews.get(i).getService(),videoViews.get(i).getUser().trim());
            if(limitService!=null){
                if(((CountOrderRunningByUserAndService==null?
                        (CountOrderDoneByServiceAndUserInOneDay==null?0:CountOrderDoneByServiceAndUserInOneDay):
                        (CountOrderRunningByUserAndService+(CountOrderDoneByServiceAndUserInOneDay==null?0:CountOrderDoneByServiceAndUserInOneDay)))>=limitService*service.getMax())
                        ||limitService==0
                        ||(CountOrderRunningByService==null?false:CountOrderRunningByService>=setting.getMaxorder()*service.getMax())){
                    continue;
                }
            }
            videoViews.get(i).setMaxthreads((int)(videoViews.get(i).getThreadset()*0.05));
            videoViews.get(i).setTimestart(System.currentTimeMillis());
            videoViewRepository.save(videoViews.get(i));

        }
        resp.put("status", "true");
        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
    }

    @GetMapping(value = "/updateRunningOrderBuffHVN", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateRunningOrderBuffHVN() throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        List<VideoView> videoViews = videoViewRepository.getAllOrderPendingBuffHVN();
        Setting setting = settingRepository.getReferenceById(1L);
        TimeZone timeZone = TimeZone.getTimeZone("GMT+7");
        Calendar calendar = Calendar.getInstance(timeZone);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.HOUR_OF_DAY);
        if(hour>=10&&hour<=23){
            resp.put("status", "fail");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
        for (int i = 0; i < videoViews.size(); i++) {
            Service service = serviceRepository.getInfoService(videoViews.get(i).getService());
            Integer CountOrderRunningByService=videoViewRepository.getCountOrderRunningByCheckTimeVN();
            if((CountOrderRunningByService==null?false:CountOrderRunningByService>=setting.getMaxorderbuffhvn()*service.getMax())){
                break;
            }
            Integer limitService=limitServiceRepository.getLimitRunningByServiceAndUser(videoViews.get(i).getUser().trim(),videoViews.get(i).getService());
            Integer CountOrderRunningByUserAndService=videoViewRepository.getCountOrderRunningByUserAndService(videoViews.get(i).getUser().trim(),videoViews.get(i).getService());
            Integer CountOrderDoneByServiceAndUserInOneDay=videoViewHistoryRepository.getCountOrderDoneByServiceAndUserInOneDay(videoViews.get(i).getService(),videoViews.get(i).getUser().trim());
            if(limitService!=null){
                if(((CountOrderRunningByUserAndService==null?
                        (CountOrderDoneByServiceAndUserInOneDay==null?0:CountOrderDoneByServiceAndUserInOneDay):
                        (CountOrderRunningByUserAndService+(CountOrderDoneByServiceAndUserInOneDay==null?0:CountOrderDoneByServiceAndUserInOneDay)))>=limitService*service.getMax())
                        ||limitService==0
                        ||(CountOrderRunningByService==null?false:CountOrderRunningByService>=setting.getMaxorderbuffhvn()*service.getMax())){
                    continue;
                }
            }
            videoViews.get(i).setMaxthreads((int)(videoViews.get(i).getThreadset()*0.05));
            videoViews.get(i).setTimestart(System.currentTimeMillis());
            videoViewRepository.save(videoViews.get(i));
        }
        resp.put("status", "true");
        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
    }

    @GetMapping(value = "/updateRunningOrderBuffHUS", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateRunningOrderBuffHUS() throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        List<VideoView> videoViews = videoViewRepository.getAllOrderPendingBuffHUS();
        Setting setting = settingRepository.getReferenceById(1L);
        TimeZone timeZone = TimeZone.getTimeZone("GMT+7");
        Calendar calendar = Calendar.getInstance(timeZone);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.HOUR_OF_DAY);
        if(hour>=10&&hour<=23){
            resp.put("status", "fail");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
        for (int i = 0; i < videoViews.size(); i++) {
            Service service = serviceRepository.getInfoService(videoViews.get(i).getService());
            Integer CountOrderRunningByService=videoViewRepository.getCountOrderRunningByCheckTimeUS();
            if((CountOrderRunningByService==null?false:CountOrderRunningByService>=setting.getMaxorderbuffhus()*service.getMax())){
                break;
            }
            Integer limitService=limitServiceRepository.getLimitRunningByServiceAndUser(videoViews.get(i).getUser().trim(),videoViews.get(i).getService());
            Integer CountOrderRunningByUserAndService=videoViewRepository.getCountOrderRunningByUserAndService(videoViews.get(i).getUser().trim(),videoViews.get(i).getService());
            Integer CountOrderDoneByServiceAndUserInOneDay=videoViewHistoryRepository.getCountOrderDoneByServiceAndUserInOneDay(videoViews.get(i).getService(),videoViews.get(i).getUser().trim());
            if(limitService!=null){
                if(((CountOrderRunningByUserAndService==null?
                        (CountOrderDoneByServiceAndUserInOneDay==null?0:CountOrderDoneByServiceAndUserInOneDay):
                        (CountOrderRunningByUserAndService+(CountOrderDoneByServiceAndUserInOneDay==null?0:CountOrderDoneByServiceAndUserInOneDay)))>=limitService*service.getMax())
                        ||limitService==0
                        ||(CountOrderRunningByService==null?false:CountOrderRunningByService>=setting.getMaxorderbuffhus()*service.getMax())){
                    continue;
                }
            }
            videoViews.get(i).setMaxthreads((int)(videoViews.get(i).getThreadset()*0.05));
            videoViews.get(i).setTimestart(System.currentTimeMillis());
            videoViewRepository.save(videoViews.get(i));
        }
        resp.put("status", "true");
        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
    }

    @GetMapping(value = "/updateRunningOrder701", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateRunningOrder701(@RequestParam(defaultValue = "1") Integer limit,@RequestParam(defaultValue = "") String user,@RequestParam(defaultValue = "8000") Integer vieworder,@RequestParam(defaultValue = "1") Integer limituser) throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        List<VideoView> videoViews;
        if(user.length()!=0){
            videoViews = videoViewRepository.getAllOrderPending701(user.trim(),vieworder,limit);
        }else{
            videoViews = videoViewRepository.getAllOrderPending701(vieworder,limit);
        }

        Setting setting = settingRepository.getReferenceById(1L);
        for (int i = 0; i < videoViews.size(); i++) {
            Service service = serviceRepository.getInfoService(videoViews.get(i).getService());
            if(limituser==1){
                Integer limitService=limitServiceRepository.getLimitRunningByServiceAndUser(videoViews.get(i).getUser().trim(),videoViews.get(i).getService());
                if(limitService!=null){
                    if(videoViewRepository.getCountOrderRunningByUserAndService(videoViews.get(i).getUser().trim(),videoViews.get(i).getService())==null?false:videoViewRepository.getCountOrderRunningByUserAndService(videoViews.get(i).getUser().trim(),videoViews.get(i).getService())>=limitService*service.getMax()){
                        continue;
                    }
                }
            }
            int max_thread = service.getThread() + ((int) (videoViews.get(i).getVieworder() / 500)-1) *25;
            if (max_thread > setting.getMaxthread()) {
                max_thread = setting.getMaxthread();
            }
            videoViews.get(i).setMaxthreads(max_thread);
            videoViews.get(i).setTimestart(System.currentTimeMillis());
            videoViewRepository.save(videoViews.get(i));

        }
        resp.put("status", "true");
        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
    }


    @GetMapping(path = "getorderview", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getorderview(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String user) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            List<OrderViewRunning> orderRunnings;
            if (user.length() == 0) {
                orderRunnings = videoViewRepository.getOrder();

            } else {
                orderRunnings = videoViewRepository.getOrder(user.trim());
            }

            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < orderRunnings.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("orderid", orderRunnings.get(i).getOrderId());
                obj.put("videoid", orderRunnings.get(i).getVideoId());
                obj.put("videotitle", orderRunnings.get(i).getVideoTitle());
                obj.put("viewstart", orderRunnings.get(i).getViewStart());
                obj.put("maxthreads", orderRunnings.get(i).getMaxthreads());
                obj.put("insertdate", orderRunnings.get(i).getInsertDate());
                obj.put("timestart", orderRunnings.get(i).getTimeStart());
                obj.put("total", orderRunnings.get(i).getTotal());
                obj.put("vieworder", orderRunnings.get(i).getViewOrder());
                obj.put("note", orderRunnings.get(i).getNote());
                obj.put("duration", orderRunnings.get(i).getDuration());
                obj.put("service", orderRunnings.get(i).getService());
                obj.put("user", orderRunnings.get(i).getUser());

                obj.put("view24h", orderRunnings.get(i).getView24h());
                obj.put("viewtotal", orderRunnings.get(i).getViewTotal());
                obj.put("price", orderRunnings.get(i).getPrice());
                jsonArray.add(obj);
            }

            resp.put("total", orderRunnings.size());
            resp.put("videoview", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "getorderviewpending", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getorderviewpending(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String user) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            List<OrderViewRunning> orderRunnings;
            if (user.length() == 0) {
                orderRunnings = videoViewRepository.getOrderPending();

            } else {
                orderRunnings = videoViewRepository.getOrderPending(user.trim());
            }

            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < orderRunnings.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("orderid", orderRunnings.get(i).getOrderId());
                obj.put("videoid", orderRunnings.get(i).getVideoId());
                obj.put("videotitle", orderRunnings.get(i).getVideoTitle());
                obj.put("viewstart", orderRunnings.get(i).getViewStart());
                obj.put("maxthreads", orderRunnings.get(i).getMaxthreads());
                obj.put("insertdate", orderRunnings.get(i).getInsertDate());
                obj.put("timestart", orderRunnings.get(i).getTimeStart());
                obj.put("total", orderRunnings.get(i).getTotal());
                obj.put("vieworder", orderRunnings.get(i).getViewOrder());
                obj.put("note", orderRunnings.get(i).getNote());
                obj.put("duration", orderRunnings.get(i).getDuration());
                obj.put("service", orderRunnings.get(i).getService());
                obj.put("user", orderRunnings.get(i).getUser());
                obj.put("priority", orderRunnings.get(i).getPriority());
                obj.put("view24h", orderRunnings.get(i).getView24h());
                obj.put("viewtotal", orderRunnings.get(i).getViewTotal());
                obj.put("price", orderRunnings.get(i).getPrice());
                jsonArray.add(obj);
            }

            resp.put("total", orderRunnings.size());
            resp.put("videoview", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "getorderviewcheckcannel", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getorderviewcheckcannel(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String user) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            List<OrderViewRunning> orderRunnings;
            if (user.length() == 0) {
                orderRunnings = videoViewRepository.getOrderCheckCancel();

            } else {
                orderRunnings = videoViewRepository.getOrderCheckCancel(user.trim());
            }

            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < orderRunnings.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("orderid", orderRunnings.get(i).getOrderId());
                obj.put("videoid", orderRunnings.get(i).getVideoId());
                obj.put("videotitle", orderRunnings.get(i).getVideoTitle());
                obj.put("viewstart", orderRunnings.get(i).getViewStart());
                obj.put("maxthreads", orderRunnings.get(i).getMaxthreads());
                obj.put("insertdate", orderRunnings.get(i).getInsertDate());
                obj.put("timestart", orderRunnings.get(i).getTimeStart());
                obj.put("total", orderRunnings.get(i).getTotal());
                obj.put("vieworder", orderRunnings.get(i).getViewOrder());
                obj.put("note", orderRunnings.get(i).getNote());
                obj.put("duration", orderRunnings.get(i).getDuration());
                obj.put("service", orderRunnings.get(i).getService());
                obj.put("user", orderRunnings.get(i).getUser());

                obj.put("view24h", orderRunnings.get(i).getView24h());
                obj.put("viewtotal", orderRunnings.get(i).getViewTotal());
                obj.put("price", orderRunnings.get(i).getPrice());
                jsonArray.add(obj);
            }

            resp.put("total", orderRunnings.size());
            resp.put("videoview", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "updateorderviewcron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateorderviewcron() {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            Setting setting = settingRepository.getReferenceById(1L);
            List<String> viewBuff;
            List<String> viewBuff24h;
            List<VideoView> videoViewList = videoViewRepository.getAllOrderView();
            viewBuff = videoViewRepository.getTotalViewBuff();

            for (int i = 0; i < videoViewList.size(); i++) {
                int viewtotal = 0;
                int view24h = 0;
                for (int j = 0; j < viewBuff.size(); j++) {
                    if (videoViewList.get(i).getVideoid().equals(viewBuff.get(j).split(",")[0])) {
                        viewtotal = Integer.parseInt(viewBuff.get(j).split(",")[1]);
                    }
                }
                try {
                    videoViewRepository.updateViewOrderByVideoId(viewtotal, view24h, System.currentTimeMillis(), videoViewList.get(i).getVideoid());
                } catch (Exception e) {

                }
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");

            resp.put("total", videoViewList.size());
            resp.put("videoview", true);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "updateorderbuffhcron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateorderbuffhcron() {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            List<String> timeTotal;
            List<VideoView> videoBuffhList = videoViewRepository.getAllOrderBuffh();
            timeTotal = videoViewRepository.getTimeBuffVideo();

            for (int i = 0; i < videoBuffhList.size(); i++) {
                int timebufftotal = 0;
                int viewbufftotal = 0;
                for (int j = 0; j < timeTotal.size(); j++) {
                    if (videoBuffhList.get(i).getVideoid().equals(timeTotal.get(j).split(",")[0])) {
                        timebufftotal = Integer.parseInt(timeTotal.get(j).split(",")[1]);
                        viewbufftotal = Integer.parseInt(timeTotal.get(j).split(",")[2]);
                    }
                }
                try {
                    videoViewRepository.updateTimeViewOrderByVideoId(timebufftotal, viewbufftotal,System.currentTimeMillis(), videoBuffhList.get(i).getVideoid());
                } catch (Exception e) {

                }
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");

            resp.put("total", videoBuffhList.size());
            resp.put("videobuff", true);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "updateordercheckcancelcron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateordercheckcancelcron() {
        JSONObject resp = new JSONObject();
        try {
            videoViewRepository.updateOrderCheckCancel();
            resp.put("status", true);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "DeleteOrderNotValidCron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> DeleteOrderNotValidCron() {
        JSONObject resp = new JSONObject();
        try {
            List<VideoView> videoViews=videoViewRepository.getAllOrderCheckCancel();
            for(int i=0;i<videoViews.size();i++){

                OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                Request request1 = null;
                List<GoogleAPIKey> keys = googleAPIKeyRepository.getAllByState();
                request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=" + keys.get(0).getKey().trim() + "&fields=items(id,contentDetails(regionRestriction(blocked)))&part=id,contentDetails&id=" + videoViews.get(i).getVideoid().trim()).get().build();
                keys.get(0).setCount(keys.get(0).getCount() + 1L);
                googleAPIKeyRepository.save(keys.get(0));

                Response response1 = client1.newCall(request1).execute();

                String resultJson1 = response1.body().string();

                Object obj1 = new JSONParser().parse(resultJson1);

                JSONObject jsonObject1 = (JSONObject) obj1;
                JSONArray items = (JSONArray) jsonObject1.get("items");
                if (items == null) {
                   continue;
                }
                //System.out.println(items);
                Iterator k = items.iterator();
                if (k.hasNext() == false) {
                    delete("1",videoViews.get(i).getVideoid().trim(),1);
                    continue;
                }else{
                    while (k.hasNext()) {
                        try {
                            JSONObject video = (JSONObject) k.next();
                            JSONObject contentDetails = (JSONObject) video.get("contentDetails");
                            JSONObject regionRestriction = (JSONObject) contentDetails.get("regionRestriction");
                            if(regionRestriction!=null){
                                if(regionRestriction.get("blocked").toString().indexOf("VN")>0&&videoViewRepository.getServiceByVideoId(videoViews.get(i).getVideoid().trim(),"vn")>0){
                                    delete("1",videoViews.get(i).getVideoid().trim(),1);
                                }else if(regionRestriction.get("blocked").toString().indexOf("US")>0&&videoViewRepository.getServiceByVideoId(videoViews.get(i).getVideoid().trim(),"us")>0){
                                    delete("1",videoViews.get(i).getVideoid().trim(),1);
                                }else{
                                    videoViewRepository.updateOrderCheck(videoViews.get(i).getVideoid().trim());
                                }
                            }else{
                                videoViewRepository.updateOrderCheck(videoViews.get(i).getVideoid().trim());
                            }
                        }catch (Exception e){
                            break;
                        }
                    }
                }
            }
            resp.put("status", true);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping(path = "bhview", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> bhview(@RequestBody() VideoViewHistory videoid, @RequestHeader(defaultValue = "") String Authorization) {
        JSONObject resp = new JSONObject();
        List<Admin> admin = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admin.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        System.out.println(videoid.getVideoid().trim());
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            List<VideoViewHistory> videoViewHistories = videoViewHistoryRepository.getVideoBHByVideoId(videoid.getVideoid().trim());
            JSONArray jsonArray = new JSONArray();
            Setting setting = settingRepository.getReferenceById(1L);
            List<Admin> admins = adminRepository.GetAdminByUser("baohanh01@gmail.com");
            if (videoViewHistories.size() == 0) {
                long orderid = 0L;
                try {
                    orderid = Long.parseLong(videoid.getVideoid().trim());
                } catch (Exception e) {
                    resp.put("videoview", "Lịch sử đơn trống!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                videoViewHistories = videoViewHistoryRepository.getVideoBHByOrderId(orderid);
                if (videoViewHistories.size() == 0) {
                    resp.put("videoview", "Lịch sử đơn trống!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                /*
                if (orderid != videoViewHistories.get(0).getOrderid()) {
                    resp.put("videoview", "Không đủ ĐK bảo hành!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                 */
            }
            for (int i = 0; i < videoViewHistories.size(); i++) {
                JSONObject obj = new JSONObject();
                if ((videoViewHistories.get(0).getRefund() == null ? 0 : videoViewHistories.get(0).getRefund()) == 1) {
                    resp.put("videoview", "Đã refund trước đó!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if (videoViewHistories.get(0).getCancel() > 0) {
                    resp.put("videoview", "Đã hủy trước đó!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if (videoViewRepository.getCountVideoId(videoViewHistories.get(i).getVideoid().trim()) > 0) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Đơn đang chạy!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }

                if (System.currentTimeMillis() - videoViewHistories.get(i).getEnddate() < 1000 * 3600 * 12) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistories.get(i).setWaitbh(1);
                    videoViewHistories.get(i).setTimecheckbh(videoViewHistories.get(i).getEnddate() + (12 * 60 * 60 * 1000));
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                    obj.put("videoview", "Bảo hành sau: " + dateFormat.format((new Date(videoViewHistories.get(i).getEnddate() + (12 * 60 * 60 * 1000)))));
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                List<VideoViewHistory> viewHistories = videoViewHistoryRepository.getTimeBHByVideoId(videoViewHistories.get(i).getVideoid().trim());
                if (viewHistories.size() > 0) {
                    if (System.currentTimeMillis() - viewHistories.get(0).getEnddate() < 1000 * 3600 * 12) {
                        videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                        videoViewHistoryRepository.save(videoViewHistories.get(i));
                        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                        obj.put("videoview", "Bảo hành sau: " + dateFormat.format(new Date(viewHistories.get(0).getEnddate() + (12 * 60 * 60 * 1000))));
                        return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                    }
                }

                OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                Request request1 = null;
                request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=AIzaSyD5KyNKQtDkpgpav-R9Tgl1aYSPMN8AwUw&fields=items(statistics(viewCount))&part=statistics&id=" + videoViewHistories.get(i).getVideoid().trim()).get().build();

                Response response1 = client1.newCall(request1).execute();

                String resultJson1 = response1.body().string();

                Object obj1 = new JSONParser().parse(resultJson1);

                JSONObject jsonObject1 = (JSONObject) obj1;
                JSONArray items = (JSONArray) jsonObject1.get("items");
                if (items == null) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Không check được view!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                Iterator k = items.iterator();
                if (k.hasNext() == false) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Không check được view!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                while (k.hasNext()) {
                    try {
                        JSONObject video = (JSONObject) k.next();
                        JSONObject statistics = (JSONObject) video.get("statistics");
                        if (Integer.parseInt(statistics.get("viewCount").toString()) - videoViewHistories.get(i).getViewstart() - videoViewHistories.get(i).getVieworder() < 0) {
                            if (Integer.parseInt(statistics.get("viewCount").toString()) - (int) videoViewHistories.get(i).getViewstart() >= 0) {
                                int baohanh = 0;
                                baohanh = (int) ((int) (videoViewHistories.get(i).getViewstart() + videoViewHistories.get(i).getVieworder() - Integer.parseInt(statistics.get("viewCount").toString())));
                                if (baohanh < 50) {
                                    baohanh = 50;
                                } else if (baohanh > videoViewHistories.get(i).getVieworder()) {
                                    baohanh = videoViewHistories.get(i).getVieworder();
                                }
                                float priceorder = 0;
                                Service service = serviceRepository.getInfoService(videoViewHistories.get(i).getService());
                                priceorder = (baohanh / 1000F) * service.getRate() * ((float) (admins.get(0).getRate()) / 100) * ((float) (100 - admins.get(0).getDiscount()) / 100);
                                if (priceorder > (float) admins.get(0).getBalance()) {
                                    obj.put("videoview", "Số tiền không đủ!");
                                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                                }

                                VideoView videoViewhnew = new VideoView();
                                videoViewhnew.setDuration(videoViewHistories.get(i).getDuration());
                                videoViewhnew.setInsertdate(System.currentTimeMillis());
                                videoViewhnew.setView24h(0);
                                videoViewhnew.setViewtotal(0);
                                videoViewhnew.setVieworder(baohanh);
                                videoViewhnew.setUser(admins.get(0).getUsername());
                                videoViewhnew.setChannelid(videoViewHistories.get(i).getChannelid());
                                videoViewhnew.setVideotitle(videoViewHistories.get(i).getVideotitle());
                                videoViewhnew.setVideoid(videoViewHistories.get(i).getVideoid());
                                videoViewhnew.setViewstart(Integer.parseInt(statistics.get("viewCount").toString()));
                                int max_thread = service.getThread() + (((int) (baohanh < 1000 ? 1000 : baohanh) / 1000) - 1) * setting.getLevelthread();
                                if (max_thread <= setting.getMaxthread()) {
                                    videoViewhnew.setMaxthreads(max_thread);
                                } else {
                                    videoViewhnew.setMaxthreads(setting.getMaxthread());
                                }
                                videoViewhnew.setPrice(priceorder);
                                videoViewhnew.setNote(videoViewHistories.get(i).getUser() + "| BHL" + (int) (videoViewHistories.get(i).getNumbh() + 1));
                                videoViewhnew.setService(videoViewHistories.get(i).getService());
                                videoViewhnew.setValid(1);
                                videoViewRepository.save(videoViewhnew);
                                videoViewHistories.get(i).setNumbh(videoViewHistories.get(i).getNumbh() + 1);
                                videoViewHistoryRepository.save(videoViewHistories.get(i));
                                if (service.getType().equals("Special")) {
                                    String list_key = dataOrderRepository.getListKeyByOrderid(videoViewHistories.get(i).getOrderid());
                                    DataOrder dataOrder = new DataOrder();
                                    dataOrder.setOrderid(videoViewhnew.getOrderid());
                                    dataOrder.setListvideo(list_key);
                                    dataOrder.setListkey(list_key);
                                    dataOrderRepository.save(dataOrder);
                                }
                                Float balance_update=adminRepository.updateBalanceFine(-priceorder,admins.get(0).getUsername().trim());
                                Balance balance = new Balance();
                                balance.setUser(admins.get(0).getUsername().trim());
                                balance.setTime(System.currentTimeMillis());
                                balance.setTotalblance(balance_update);
                                balance.setBalance(-priceorder);
                                balance.setService(service.getService());
                                balance.setNote("Bảo hành " + baohanh + " view cho video " + videoViewHistories.get(i).getVideoid());
                                balanceRepository.save(balance);


                                obj.put(videoViewHistories.get(i).getVideoid().trim(), "Bảo hành " + baohanh + " view!");
                                obj.put("videoview", "true");
                                obj.put("balance", admins.get(0).getBalance());
                                obj.put("price", priceorder);
                                obj.put("time", baohanh);
                                return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                            } else {
                                videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                                videoViewHistoryRepository.save(videoViewHistories.get(i));
                                obj.put("videoview", "View check < view start! (Chọn refund)");
                                return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                            }
                        } else {
                            videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                            videoViewHistoryRepository.save(videoViewHistories.get(i));
                            obj.put("videoview", "Không cần bảo hành!");
                            return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                        }
                    } catch (Exception e) {
                        System.out.println(e.getStackTrace()[0].getLineNumber());
                        throw new RuntimeException(e);
                    }
                }
            }
            resp.put("rep", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            System.out.println(e.getStackTrace()[0].getLineNumber());
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "AutoBH", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> AutoBH(@RequestParam(defaultValue = "1") Integer start, @RequestParam(defaultValue = "5") Integer end, @RequestParam(defaultValue = "2") Integer limit, Integer bonus) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            LocalTime currentTime = LocalTime.now();
            int hour = currentTime.getHour();
            AutoRefill autoRefill = autoRefillRepository.getReferenceById(1L);
            if (autoRefill.getEnabled() == 0) {
                resp.put("rep", "AutoBH Off");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            } else if ((System.currentTimeMillis() - autoRefill.getTimelastrun() < 1000 * autoRefill.getCron() * 60) || ((hour < autoRefill.getTimestart() || hour > autoRefill.getTimend()) && (autoRefill.getTimestart() != autoRefill.getTimend()))) {
                resp.put("rep", "AutoBH not in Cron");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            } else if (videoViewRepository.getCountOrderByUser("baohanh01@gmail.com") > autoRefill.getLimitrefillorder()) {
                resp.put("rep", "AutoBH Max Order");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            int total_refill = 0;
            List<VideoViewHistory> videoViewHistories = videoViewHistoryRepository.getVideoCheckBH(autoRefill.getStart() * 24, autoRefill.getEnd() * 24, autoRefill.getLimitorder());
            JSONArray jsonArray = new JSONArray();
            Setting setting = settingRepository.getReferenceById(1L);
            List<Admin> admins = adminRepository.GetAdminByUser("baohanh01@gmail.com");
            videoViewHistoryRepository.updatetimchecknomaxid();
            for (int i = 0; i < videoViewHistories.size(); i++) {
                DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                String end_done = "End: " + dateFormat.format(videoViewHistories.get(i).getEnddate()) + " | ";
                JSONObject obj = new JSONObject();
                if (videoViewRepository.getCountVideoId(videoViewHistories.get(i).getVideoid().trim()) > 0) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put(videoViewHistories.get(i).getVideoid().trim(), end_done + "Đơn đang chạy!");
                    jsonArray.add(obj);
                    continue;
                }
                List<VideoViewHistory> viewHistories = videoViewHistoryRepository.getTimeBHByVideoId(videoViewHistories.get(i).getVideoid().trim());
                if (viewHistories.size() > 0) {
                    if (System.currentTimeMillis() - viewHistories.get(0).getEnddate() < 1000 * 3600 * 12) {
                        videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                        videoViewHistoryRepository.save(videoViewHistories.get(i));
                        obj.put(videoViewHistories.get(i).getVideoid().trim(), end_done + "Đơn đã được bảo hành chưa quá 12h!");
                        jsonArray.add(obj);
                        continue;
                    }
                }
                OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
                List<GoogleAPIKey> keys = googleAPIKeyRepository.getAllByState();
                Request request1 = null;
                request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=" + keys.get(0).getKey().trim() + "&fields=items(statistics(viewCount))&part=statistics&id=" + videoViewHistories.get(i).getVideoid().trim()).get().build();
                keys.get(0).setCount(keys.get(0).getCount() + 1L);
                googleAPIKeyRepository.save(keys.get(0));
                Response response1 = client1.newCall(request1).execute();

                String resultJson1 = response1.body().string();

                Object obj1 = new JSONParser().parse(resultJson1);

                JSONObject jsonObject1 = (JSONObject) obj1;
                JSONArray items = (JSONArray) jsonObject1.get("items");
                if (items == null) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put(videoViewHistories.get(i).getVideoid().trim(), end_done + "Không check được view!");
                    jsonArray.add(obj);
                    continue;
                }
                Iterator k = items.iterator();
                if (k.hasNext() == false) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put(videoViewHistories.get(i).getVideoid().trim(), end_done + "Không check được view!");
                    jsonArray.add(obj);
                    continue;
                }
                while (k.hasNext()) {
                    try {
                        JSONObject video = (JSONObject) k.next();
                        JSONObject statistics = (JSONObject) video.get("statistics");
                        if (Integer.parseInt(statistics.get("viewCount").toString()) - videoViewHistories.get(i).getViewstart() - videoViewHistories.get(i).getVieworder() < 0) {
                            if (Integer.parseInt(statistics.get("viewCount").toString()) - (int) videoViewHistories.get(i).getViewstart() > 0) {
                                int baohanh = 0;
                                baohanh = (int) ((int) (videoViewHistories.get(i).getViewstart() + videoViewHistories.get(i).getVieworder() - Integer.parseInt(statistics.get("viewCount").toString())));
                                if (baohanh < 50) {
                                    baohanh = 50;
                                } else if (baohanh > videoViewHistories.get(i).getVieworder()) {
                                    baohanh = videoViewHistories.get(i).getVieworder();
                                }
                                float priceorder = 0;
                                Service service = serviceRepository.getInfoService(videoViewHistories.get(i).getService());
                                priceorder = (baohanh / 1000F) * service.getRate() * ((float) (admins.get(0).getRate()) / 100) * ((float) (100 - admins.get(0).getDiscount()) / 100);
                                if (priceorder > (float) admins.get(0).getBalance()) {
                                    obj.put(videoViewHistories.get(i).getVideoid().trim(), end_done + "Số tiền không đủ!");
                                    jsonArray.add(obj);
                                    continue;
                                }

                                VideoView videoViewhnew = new VideoView();
                                videoViewhnew.setDuration(videoViewHistories.get(i).getDuration());
                                videoViewhnew.setInsertdate(System.currentTimeMillis());
                                videoViewhnew.setView24h(0);
                                videoViewhnew.setViewtotal(0);
                                videoViewhnew.setVieworder(baohanh);
                                videoViewhnew.setUser(admins.get(0).getUsername());
                                videoViewhnew.setChannelid(videoViewHistories.get(i).getChannelid());
                                videoViewhnew.setVideotitle(videoViewHistories.get(i).getVideotitle());
                                videoViewhnew.setVideoid(videoViewHistories.get(i).getVideoid());
                                videoViewhnew.setViewstart(Integer.parseInt(statistics.get("viewCount").toString()));
                                int max_thread = service.getThread() + (((int) (baohanh < 1000 ? 1000 : baohanh) / 1000) - 1) * setting.getLevelthread();
                                if (max_thread <= setting.getMaxthread()) {
                                    videoViewhnew.setMaxthreads(max_thread);
                                } else {
                                    videoViewhnew.setMaxthreads(setting.getMaxthread());
                                }
                                videoViewhnew.setPrice(priceorder);
                                videoViewhnew.setNote(videoViewHistories.get(i).getUser() + "| BHL" + (int) (videoViewHistories.get(i).getNumbh() + 1));
                                videoViewhnew.setService(videoViewHistories.get(i).getService());
                                videoViewhnew.setValid(1);
                                videoViewRepository.save(videoViewhnew);
                                videoViewHistories.get(i).setNumbh(videoViewHistories.get(i).getNumbh() + 1);
                                videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                                videoViewHistoryRepository.save(videoViewHistories.get(i));
                                if (service.getType().equals("Special")) {
                                    String list_key = dataOrderRepository.getListKeyByOrderid(videoViewHistories.get(i).getOrderid());
                                    DataOrder dataOrder = new DataOrder();
                                    dataOrder.setOrderid(videoViewhnew.getOrderid());
                                    dataOrder.setListvideo(list_key);
                                    dataOrder.setListkey(list_key);
                                    dataOrderRepository.save(dataOrder);
                                }
                                Float balance_update=adminRepository.updateBalanceFine(-priceorder,admins.get(0).getUsername().trim());
                                Balance balance = new Balance();
                                balance.setUser(admins.get(0).getUsername().trim());
                                balance.setTime(System.currentTimeMillis());
                                balance.setTotalblance(balance_update);
                                balance.setBalance(-priceorder);
                                balance.setService(service.getService());
                                balance.setNote("Bảo hành " + baohanh + " view cho video " + videoViewHistories.get(i).getVideoid());
                                balanceRepository.save(balance);

                                total_refill = total_refill + 1;

                                obj.put(videoViewHistories.get(i).getVideoid().trim(), end_done + "Bảo hành:" + baohanh + "/" + videoViewHistories.get(i).getVieworder() + " | " + (int) (baohanh / (float) videoViewHistories.get(i).getVieworder() * 100) + "%");
                                jsonArray.add(obj);
                                continue;
                            } else {
                                videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                                videoViewHistoryRepository.save(videoViewHistories.get(i));
                                obj.put(videoViewHistories.get(i).getVideoid().trim(), end_done + "View check < view start!");
                                jsonArray.add(obj);
                                continue;
                            }
                        } else {
                            videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                            videoViewHistoryRepository.save(videoViewHistories.get(i));
                            obj.put(videoViewHistories.get(i).getVideoid().trim(), end_done + "Không cần bảo hành!");
                            jsonArray.add(obj);
                            continue;
                        }
                    } catch (Exception e) {
                        videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                        videoViewHistoryRepository.save(videoViewHistories.get(i));
                        obj.put(videoViewHistories.get(i).getVideoid().trim(), end_done + "Không cần bảo hành!");
                        jsonArray.add(obj);
                        continue;
                    }
                }

            }
            autoRefill.setTimelastrun(System.currentTimeMillis());
            autoRefill.setTotalrefill(total_refill);
            autoRefillRepository.save(autoRefill);
            resp.put("rep", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "AutoBH3701", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> AutoBH3701(@RequestParam(defaultValue = "1") Integer start, @RequestParam(defaultValue = "5") Integer end, @RequestParam(defaultValue = "2") Integer limit, Integer bonus) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            LocalTime currentTime = LocalTime.now();
            int hour = currentTime.getHour();
            AutoRefill autoRefill = autoRefillRepository.getReferenceById(1L);
            if (autoRefill.getEnabled() == 0) {
                resp.put("rep", "AutoBH Off");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            } else if ((System.currentTimeMillis() - autoRefill.getTimelastrun() < 1000 * autoRefill.getCron() * 60) || ((hour < autoRefill.getTimestart() || hour > autoRefill.getTimend()) && (autoRefill.getTimestart() != autoRefill.getTimend()))) {
                resp.put("rep", "AutoBH not in Cron");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            } else if (videoViewRepository.getCountOrderByUser("baohanh01@gmail.com") > autoRefill.getLimitrefillorder()) {
                resp.put("rep", "AutoBH Max Order");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            int total_refill = 0;
            List<VideoViewHistory> videoViewHistories = videoViewHistoryRepository.getVideoCheckBH(autoRefill.getStart(), autoRefill.getEnd(), autoRefill.getLimitorder());
            JSONArray jsonArray = new JSONArray();
            Setting setting = settingRepository.getReferenceById(1L);
            List<Admin> admins = adminRepository.GetAdminByUser("baohanh01@gmail.com");
            videoViewHistoryRepository.updatetimchecknomaxid();
            for (int i = 0; i < videoViewHistories.size(); i++) {
                videoViewHistories.get(i).setViewstart(videoViewHistoryRepository.getViewStart3701(videoViewHistories.get(i).getVideoid().trim()));
                DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                String end_done = "End: " + dateFormat.format(videoViewHistories.get(i).getEnddate()) + " | ";
                JSONObject obj = new JSONObject();
                if (videoViewRepository.getCountVideoId(videoViewHistories.get(i).getVideoid().trim()) > 0) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put(videoViewHistories.get(i).getVideoid().trim(), end_done + "Đơn đang chạy!");
                    jsonArray.add(obj);
                    continue;
                }
                List<VideoViewHistory> viewHistories = videoViewHistoryRepository.getTimeBHByVideoId(videoViewHistories.get(i).getVideoid().trim());
                if (viewHistories.size() > 0) {
                    if (System.currentTimeMillis() - viewHistories.get(0).getEnddate() < 1000 * 3600 * 12) {
                        videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                        videoViewHistoryRepository.save(videoViewHistories.get(i));
                        obj.put(videoViewHistories.get(i).getVideoid().trim(), end_done + "Đơn đã được bảo hành chưa quá 12h!");
                        jsonArray.add(obj);
                        continue;
                    }
                }
                OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
                List<GoogleAPIKey> keys = googleAPIKeyRepository.getAllByState();
                Request request1 = null;
                request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=" + keys.get(0).getKey().trim() + "&fields=items(statistics(viewCount))&part=statistics&id=" + videoViewHistories.get(i).getVideoid().trim()).get().build();
                keys.get(0).setCount(keys.get(0).getCount() + 1L);
                googleAPIKeyRepository.save(keys.get(0));
                Response response1 = client1.newCall(request1).execute();

                String resultJson1 = response1.body().string();

                Object obj1 = new JSONParser().parse(resultJson1);

                JSONObject jsonObject1 = (JSONObject) obj1;
                JSONArray items = (JSONArray) jsonObject1.get("items");
                if (items == null) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put(videoViewHistories.get(i).getVideoid().trim(), end_done + "Không check được view!");
                    jsonArray.add(obj);
                    continue;
                }
                Iterator k = items.iterator();
                if (k.hasNext() == false) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put(videoViewHistories.get(i).getVideoid().trim(), end_done + "Không check được view!");
                    jsonArray.add(obj);
                    continue;
                }
                while (k.hasNext()) {
                    try {
                        JSONObject video = (JSONObject) k.next();
                        JSONObject statistics = (JSONObject) video.get("statistics");
                        if (Integer.parseInt(statistics.get("viewCount").toString()) - videoViewHistories.get(i).getViewstart() - videoViewHistories.get(i).getVieworder() < 0) {
                            if (Integer.parseInt(statistics.get("viewCount").toString()) - (int) videoViewHistories.get(i).getViewstart() > 0) {
                                int baohanh = 0;
                                baohanh = (int) ((int) (videoViewHistories.get(i).getViewstart() + videoViewHistories.get(i).getVieworder() - Integer.parseInt(statistics.get("viewCount").toString())));
                                if (baohanh < 50) {
                                    baohanh = 50;
                                } else if (baohanh > videoViewHistories.get(i).getVieworder()) {
                                    baohanh = videoViewHistories.get(i).getVieworder();
                                }
                                float priceorder = 0;
                                Service service = serviceRepository.getInfoService(videoViewHistories.get(i).getService());
                                priceorder = (baohanh / 1000F) * service.getRate() * ((float) (admins.get(0).getRate()) / 100) * ((float) (100 - admins.get(0).getDiscount()) / 100);
                                if (priceorder > (float) admins.get(0).getBalance()) {
                                    obj.put(videoViewHistories.get(i).getVideoid().trim(), end_done + "Số tiền không đủ!");
                                    jsonArray.add(obj);
                                    continue;
                                }

                                VideoView videoViewhnew = new VideoView();
                                videoViewhnew.setDuration(videoViewHistories.get(i).getDuration());
                                videoViewhnew.setInsertdate(System.currentTimeMillis());
                                videoViewhnew.setView24h(0);
                                videoViewhnew.setViewtotal(0);
                                videoViewhnew.setVieworder(baohanh);
                                videoViewhnew.setUser(admins.get(0).getUsername());
                                videoViewhnew.setChannelid(videoViewHistories.get(i).getChannelid());
                                videoViewhnew.setVideotitle(videoViewHistories.get(i).getVideotitle());
                                videoViewhnew.setVideoid(videoViewHistories.get(i).getVideoid());
                                videoViewhnew.setViewstart(Integer.parseInt(statistics.get("viewCount").toString()));
                                int max_thread = service.getThread() + (((int) (baohanh < 1000 ? 1000 : baohanh) / 1000) - 1) * setting.getLevelthread();
                                if (max_thread <= setting.getMaxthread()) {
                                    videoViewhnew.setMaxthreads(max_thread);
                                } else {
                                    videoViewhnew.setMaxthreads(setting.getMaxthread());
                                }
                                videoViewhnew.setPrice(priceorder);
                                videoViewhnew.setNote(videoViewHistories.get(i).getUser() + "| BHL" + (int) (videoViewHistories.get(i).getNumbh() + 1));
                                videoViewhnew.setService(videoViewHistories.get(i).getService());
                                videoViewhnew.setValid(1);
                                videoViewRepository.save(videoViewhnew);
                                videoViewHistories.get(i).setNumbh(videoViewHistories.get(i).getNumbh() + 1);
                                videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                                videoViewHistoryRepository.save(videoViewHistories.get(i));
                                if (service.getType().equals("Special")) {
                                    String list_key = dataOrderRepository.getListKeyByOrderid(videoViewHistories.get(i).getOrderid());
                                    DataOrder dataOrder = new DataOrder();
                                    dataOrder.setOrderid(videoViewhnew.getOrderid());
                                    dataOrder.setListvideo(list_key);
                                    dataOrder.setListkey(list_key);
                                    dataOrderRepository.save(dataOrder);
                                }
                                Float balance_update=adminRepository.updateBalanceFine(-priceorder,admins.get(0).getUsername().trim());
                                Balance balance = new Balance();
                                balance.setUser(admins.get(0).getUsername().trim());
                                balance.setTime(System.currentTimeMillis());
                                balance.setTotalblance(balance_update);
                                balance.setBalance(-priceorder);
                                balance.setService(service.getService());
                                balance.setNote("Bảo hành " + baohanh + " view cho video " + videoViewHistories.get(i).getVideoid());
                                balanceRepository.save(balance);

                                total_refill = total_refill + 1;

                                obj.put(videoViewHistories.get(i).getVideoid().trim(), end_done + "Bảo hành:" + baohanh + "/" + videoViewHistories.get(i).getVieworder() + " | " + (int) (baohanh / (float) videoViewHistories.get(i).getVieworder() * 100) + "%");
                                jsonArray.add(obj);
                                continue;
                            } else {
                                videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                                videoViewHistoryRepository.save(videoViewHistories.get(i));
                                obj.put(videoViewHistories.get(i).getVideoid().trim(), end_done + "View check < view start!");
                                jsonArray.add(obj);
                                continue;
                            }
                        } else {
                            videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                            videoViewHistoryRepository.save(videoViewHistories.get(i));
                            obj.put(videoViewHistories.get(i).getVideoid().trim(), end_done + "Không cần bảo hành!");
                            jsonArray.add(obj);
                            continue;
                        }
                    } catch (Exception e) {
                        videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                        videoViewHistoryRepository.save(videoViewHistories.get(i));
                        obj.put(videoViewHistories.get(i).getVideoid().trim(), end_done + "Không cần bảo hành!");
                        jsonArray.add(obj);
                        continue;
                    }
                }

            }
            autoRefill.setTimelastrun(System.currentTimeMillis());
            autoRefill.setTotalrefill(total_refill);
            autoRefillRepository.save(autoRefill);
            resp.put("rep", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(path = "htview", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> htview(@RequestBody() VideoViewHistory videoid, @RequestHeader(defaultValue = "") String Authorization) {

        JSONObject resp = new JSONObject();
        List<Admin> admin = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admin.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        //System.out.println(videoid.getVideoid().trim());
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            List<VideoViewHistory> videoViewHistories = videoViewHistoryRepository.getVideoBHByVideoId(videoid.getVideoid().trim());
            JSONArray jsonArray = new JSONArray();
            Setting setting = settingRepository.getReferenceById(1L);
            List<Admin> admins = adminRepository.GetAdminByUser("baohanh01@gmail.com");
            if (videoViewHistories.size() == 0) {
                long orderid = 0L;
                try {
                    orderid = Long.parseLong(videoid.getVideoid().trim());
                } catch (Exception e) {
                    resp.put("videoview", "Lịch sử đơn trống!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                videoViewHistories = videoViewHistoryRepository.getVideoBHByOrderId(orderid);
                if (videoViewHistories.size() == 0) {
                    resp.put("videoview", "Lịch sử đơn trống!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            }

            for (int i = 0; i < videoViewHistories.size(); i++) {
                JSONObject obj = new JSONObject();
                /*
                if ((videoViewHistories.get(0).getRefund() == null ? 0 : videoViewHistories.get(0).getRefund()) == 1) {
                    resp.put("videoview", "Đã refund trước đó!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                 */
                if (videoViewHistories.get(0).getCancel() > 0 && (videoViewHistories.get(0).getRefund() == null ? 0 : videoViewHistories.get(0).getRefund()) == 0) {
                    resp.put("videoview", "Đã hủy trước đó!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if (videoViewHistories.get(0).getPrice() == 0) {
                    resp.put("videoview", "Đã refund 100%");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if (videoViewRepository.getCountVideoIdNotIsBH(videoViewHistories.get(i).getVideoid().trim()) > 0) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Đơn đang chạy!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }

                if (System.currentTimeMillis() - videoViewHistories.get(i).getEnddate() < 1000 * 3600 * 12) {
                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                    obj.put("videoview", "Refund sau: " + dateFormat.format((new Date(videoViewHistories.get(i).getEnddate() + (12 * 60 * 60 * 1000)))));
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                /*
                List<VideoViewHistory> viewHistories =videoViewHistoryRepository.getTimeBHByVideoId(videoViewHistories.get(i).getVideoid().trim());
                if (viewHistories.size()>0) {
                    if(System.currentTimeMillis()-viewHistories.get(0).getEnddate()< 1000 * 3600 * 24){
                        videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                        videoViewHistoryRepository.save(videoViewHistories.get(i));
                        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                        obj.put("videoview", "Refund sau: " +dateFormat.format(new Date(viewHistories.get(0).getEnddate()+(12 * 60 * 60 * 1000))));
                        return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                    }
                }

                 */
                OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
                List<GoogleAPIKey> keys = googleAPIKeyRepository.getAllByState();
                Request request1 = null;
                request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=" + keys.get(0).getKey().trim() + "&fields=items(statistics(viewCount))&part=statistics&id=" + videoViewHistories.get(i).getVideoid().trim()).get().build();
                keys.get(0).setCount(keys.get(0).getCount() + 1L);
                googleAPIKeyRepository.save(keys.get(0));
                Response response1 = client1.newCall(request1).execute();

                String resultJson1 = response1.body().string();

                Object obj1 = new JSONParser().parse(resultJson1);

                JSONObject jsonObject1 = (JSONObject) obj1;
                JSONArray items = (JSONArray) jsonObject1.get("items");
                if (items == null) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Không check được view!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                Iterator k = items.iterator();
                if (k.hasNext() == false) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Không check được view!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                while (k.hasNext()) {
                    try {
                        JSONObject video = (JSONObject) k.next();
                        JSONObject statistics = (JSONObject) video.get("statistics");
                        Service service = serviceRepository.getInfoService(videoViewHistories.get(i).getService());
                        List<Admin> user = adminRepository.getAdminByUser(videoViewHistories.get(i).getUser());
                        //Hoàn tiền những view chưa buff
                        int viewcount = Integer.parseInt(statistics.get("viewCount").toString());
                        int viewFix = videoViewHistories.get(i).getVieworder() > videoViewHistories.get(i).getViewtotal() ? videoViewHistories.get(i).getViewtotal() : videoViewHistories.get(i).getVieworder();
                        int viewthan = viewFix + videoViewHistories.get(i).getViewstart() - viewcount;
                        if (viewthan > viewFix) {
                            viewthan = viewFix;
                        }
                        float price_refund = ((viewthan) / (float) viewFix) * videoViewHistories.get(i).getPrice();
                        //float pricebuffed=(videoBuffh.get(0).getViewtotal()/1000F)*service.getRate()*((float)(100-admins.get(0).getDiscount())/100);
                        if (videoViewHistories.get(i).getPrice() < price_refund) {
                            price_refund = videoViewHistories.get(i).getPrice();
                        }
                        float pricebuffed = (videoViewHistories.get(i).getPrice() - price_refund);
                        videoViewHistories.get(i).setPrice(pricebuffed);
                        videoViewHistories.get(i).setViewend(viewcount);
                        videoViewHistories.get(i).setTimecheckbh(System.currentTimeMillis());
                        videoViewHistories.get(i).setViewtotal(viewFix - viewthan);
                        videoViewHistories.get(i).setRefund(1);
                        if (price_refund == videoViewHistories.get(i).getPrice()) {
                            videoViewHistories.get(i).setCancel(1);
                        } else {
                            videoViewHistories.get(i).setCancel(2);
                        }
                        videoViewHistoryRepository.save(videoViewHistories.get(i));
                        //hoàn tiền & add thong báo số dư
                        videoViewRepository.deletevideoByVideoIdBH(videoViewHistories.get(i).getVideoid());
                        Float balance_update=adminRepository.updateBalanceFine(price_refund,videoViewHistories.get(i).getUser().trim());
                        Balance balance = new Balance();
                        balance.setUser(user.get(0).getUsername().trim());
                        balance.setTime(System.currentTimeMillis());
                        balance.setTotalblance(balance_update);
                        balance.setBalance(price_refund);
                        balance.setService(videoViewHistories.get(i).getService());
                        balance.setNote("Refund " + (viewthan) + "view cho " + videoViewHistories.get(i).getVideoid());
                        balanceRepository.save(balance);

                        obj.put(videoViewHistories.get(i).getVideoid().trim(), "Refund  " + viewthan + " view!");
                        obj.put("videoview", "true");
                        obj.put("videoid", videoViewHistories.get(i).getVideoid().trim());
                        obj.put("balance", admins.get(0).getBalance());
                        obj.put("price", price_refund);
                        obj.put("time", viewthan);
                        return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                    } catch (Exception e) {
                        System.out.println(e.getStackTrace()[0].getLineNumber());
                        throw new RuntimeException(e);
                    }
                }
            }
            resp.put("rep", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            System.out.println(e.getStackTrace()[0].getLineNumber());
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "htviewfindorder", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> htviewfindorder(@RequestParam() Long orderid, @RequestHeader(defaultValue = "") String Authorization) {

        JSONObject resp = new JSONObject();
        List<Admin> admin = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admin.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        //System.out.println(videoid.getVideoid().trim());
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            JSONArray jsonArray = new JSONArray();
            Setting setting = settingRepository.getReferenceById(1L);
            List<Admin> admins = adminRepository.GetAdminByUser("baohanh01@gmail.com");
            List<VideoViewHistory> videoViewHistories = videoViewHistoryRepository.getVideoBHByOrderId(orderid);
            if (videoViewHistories.size() == 0) {
                    resp.put("videoview", "Lịch sử đơn trống!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            for (int i = 0; i < videoViewHistories.size(); i++) {
                Service service = serviceRepository.getInfoService(videoViewHistories.get(i).getService());
                JSONObject obj = new JSONObject();
                /*
                if ((videoViewHistories.get(0).getRefund() == null ? 0 : videoViewHistories.get(0).getRefund()) == 1) {
                    resp.put("videoview", "Đã refund trước đó!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                 */
                if (videoViewHistories.get(0).getCancel() ==1) {
                    resp.put("videoview", "Đã hủy trước đó!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if (videoViewHistories.get(0).getPrice() == 0) {
                    resp.put("videoview", "Đã refund 100%");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if (videoViewRepository.getCountVideoIdNotIsBH(videoViewHistories.get(i).getVideoid().trim()) > 0) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Đơn đang chạy!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                if(service.getChecktime()==0&&(System.currentTimeMillis()- videoViewHistories.get(i).getEnddate())/1000/60/60<8){
                    obj.put("videoview", "Hoàn thành chưa đủ 8h!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                /*
                List<VideoViewHistory> viewHistories =videoViewHistoryRepository.getTimeBHByVideoId(videoViewHistories.get(i).getVideoid().trim());
                if (viewHistories.size()>0) {
                    if(System.currentTimeMillis()-viewHistories.get(0).getEnddate()< 1000 * 3600 * 24){
                        videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                        videoViewHistoryRepository.save(videoViewHistories.get(i));
                        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                        obj.put("videoview", "Refund sau: " +dateFormat.format(new Date(viewHistories.get(0).getEnddate()+(12 * 60 * 60 * 1000))));
                        return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                    }
                }

                 */
                OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
                List<GoogleAPIKey> keys = googleAPIKeyRepository.getAllByState();
                Request request1 = null;
                request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=" + keys.get(0).getKey().trim() + "&fields=items(statistics(viewCount))&part=statistics&id=" + videoViewHistories.get(i).getVideoid().trim()).get().build();
                keys.get(0).setCount(keys.get(0).getCount() + 1L);
                googleAPIKeyRepository.save(keys.get(0));
                Response response1 = client1.newCall(request1).execute();

                String resultJson1 = response1.body().string();

                Object obj1 = new JSONParser().parse(resultJson1);

                JSONObject jsonObject1 = (JSONObject) obj1;
                JSONArray items = (JSONArray) jsonObject1.get("items");
                if (items == null) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Không check được view!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                Iterator k = items.iterator();
                if (k.hasNext() == false) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Không check được view!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                while (k.hasNext()) {
                    try {
                        JSONObject video = (JSONObject) k.next();
                        JSONObject statistics = (JSONObject) video.get("statistics");
                        List<Admin> user = adminRepository.getAdminByUser(videoViewHistories.get(i).getUser());
                        //Hoàn tiền những view chưa buff
                        int viewcount = Integer.parseInt(statistics.get("viewCount").toString());
                        int viewFix = videoViewHistories.get(i).getVieworder() > videoViewHistories.get(i).getViewtotal() ? videoViewHistories.get(i).getViewtotal() : videoViewHistories.get(i).getVieworder();
                        int viewthan = viewFix + videoViewHistories.get(i).getViewstart() - viewcount;
                        if(viewthan<=0){
                            if(service.getChecktime()==0){
                                videoViewHistories.get(i).setViewend(viewcount);
                                videoViewHistories.get(i).setTimecheckbh(System.currentTimeMillis());
                            }
                            videoViewHistoryRepository.save(videoViewHistories.get(i));
                            break;
                        }
                        if (viewthan > viewFix) {
                            viewthan = viewFix;
                        }
                        float price_refund = ((viewthan) / (float) viewFix) * videoViewHistories.get(i).getPrice();
                        //float pricebuffed=(videoBuffh.get(0).getViewtotal()/1000F)*service.getRate()*((float)(100-admins.get(0).getDiscount())/100);
                        if (videoViewHistories.get(i).getPrice() < price_refund) {
                            price_refund = videoViewHistories.get(i).getPrice();
                        }
                        float pricebuffed = (videoViewHistories.get(i).getPrice() - price_refund);
                        videoViewHistories.get(i).setPrice(pricebuffed);
                        videoViewHistories.get(i).setViewend(viewcount);
                        videoViewHistories.get(i).setTimecheckbh(System.currentTimeMillis());
                        videoViewHistories.get(i).setViewtotal(viewFix - viewthan);
                        videoViewHistories.get(i).setRefund(1);
                        if (videoViewHistories.get(i).getViewtotal()==0) {
                            videoViewHistories.get(i).setCancel(1);
                        } else {
                            videoViewHistories.get(i).setCancel(2);
                        }
                        videoViewHistoryRepository.save(videoViewHistories.get(i));
                        //hoàn tiền & add thong báo số dư
                        videoViewRepository.deletevideoByVideoIdBH(videoViewHistories.get(i).getVideoid());
                        Float balance_update=adminRepository.updateBalanceFine(price_refund,videoViewHistories.get(i).getUser().trim());
                        Balance balance = new Balance();
                        balance.setUser(user.get(0).getUsername().trim());
                        balance.setTime(System.currentTimeMillis());
                        balance.setTotalblance(balance_update);
                        balance.setBalance(price_refund);
                        balance.setService(videoViewHistories.get(i).getService());
                        balance.setNote("Refund " + (viewthan) + "view cho " + videoViewHistories.get(i).getVideoid());
                        balanceRepository.save(balance);

                        obj.put(videoViewHistories.get(i).getVideoid().trim(), "Refund  " + viewthan + " view!");
                        obj.put("videoview", "true");
                        obj.put("videoid", videoViewHistories.get(i).getVideoid().trim());
                        obj.put("balance", admins.get(0).getBalance());
                        obj.put("price", price_refund);
                        obj.put("time", viewthan);
                        return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                    } catch (Exception e) {
                        System.out.println(e.getStackTrace()[0].getLineNumber());
                        throw new RuntimeException(e);
                    }
                }
            }
            resp.put("rep", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            System.out.println(e.getStackTrace()[0].getLineNumber());
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping(path = "ht100view", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> ht100view(@RequestBody() VideoViewHistory videoid, @RequestHeader(defaultValue = "") String Authorization) {

        JSONObject resp = new JSONObject();
        List<Admin> admin = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admin.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        //System.out.println(videoid.getVideoid().trim());
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            List<VideoViewHistory> videoViewHistories = videoViewHistoryRepository.getVideoBHByVideoId(videoid.getVideoid().trim());
            JSONArray jsonArray = new JSONArray();
            Setting setting = settingRepository.getReferenceById(1L);
            List<Admin> admins = adminRepository.GetAdminByUser("baohanh01@gmail.com");
            if (videoViewHistories.size() == 0) {
                long orderid = 0L;
                try {
                    orderid = Long.parseLong(videoid.getVideoid().trim());
                } catch (Exception e) {
                    resp.put("videoview", "Lịch sử đơn trống!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                videoViewHistories = videoViewHistoryRepository.getVideoBHByOrderId(orderid);
                if (videoViewHistories.size() == 0) {
                    resp.put("videoview", "Lịch sử đơn trống!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            }

            for (int i = 0; i < videoViewHistories.size(); i++) {
                JSONObject obj = new JSONObject();
                List<Admin> user = adminRepository.getAdminByUser(videoViewHistories.get(i).getUser());
                if (videoViewHistories.get(0).getPrice() == 0) {
                    resp.put("videoview", "Đã refund 100%");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if (videoViewRepository.getCountVideoIdNotIsBH(videoViewHistories.get(i).getVideoid().trim()) > 0) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Đơn đang chạy!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                int viewthan=videoViewHistories.get(i).getVieworder()>videoViewHistories.get(i).getViewtotal()?videoViewHistories.get(i).getViewtotal():videoViewHistories.get(i).getVieworder();
                Float price_refund=videoViewHistories.get(i).getPrice();
                videoViewHistories.get(i).setPrice(0F);
                videoViewHistories.get(i).setViewtotal(0);
                videoViewHistories.get(i).setTimecheckbh(System.currentTimeMillis());
                videoViewHistories.get(i).setRefund(1);
                videoViewHistories.get(i).setCancel(1);

                videoViewHistoryRepository.save(videoViewHistories.get(i));
                //hoàn tiền & add thong báo số dư
                videoViewRepository.deletevideoByVideoIdBH(videoViewHistories.get(i).getVideoid());
                Float balance_update=adminRepository.updateBalanceFine(price_refund,videoViewHistories.get(i).getUser());
                Balance balance = new Balance();
                balance.setUser(user.get(0).getUsername().trim());
                balance.setTime(System.currentTimeMillis());
                balance.setTotalblance(balance_update);
                balance.setBalance(price_refund);
                balance.setService(videoViewHistories.get(i).getService());
                balance.setNote("Refund " + (viewthan) + "view cho " + videoViewHistories.get(i).getVideoid());
                balanceRepository.save(balance);

                obj.put(videoViewHistories.get(i).getVideoid().trim(), "Refund  " + viewthan + " view!");
                obj.put("videoview", "true");
                obj.put("videoid", videoViewHistories.get(i).getVideoid().trim());
                obj.put("balance", admins.get(0).getBalance());
                obj.put("price", price_refund);
                obj.put("time", viewthan);
                return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
            }
            resp.put("rep", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            System.out.println(e.getStackTrace()[0].getLineNumber());
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(path = "htviewbyvideoid", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> htviewbyvideoid(@RequestBody() VideoViewHistory videoid, @RequestHeader(defaultValue = "") String Authorization) {

        JSONObject resp = new JSONObject();
        List<Admin> admin = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admin.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        //System.out.println(videoid.getVideoid().trim());
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            List<VideoViewHistory> videoViewHistories = videoViewHistoryRepository.getVideoBHByVideoIdNoMaxOrderId(videoid.getVideoid().trim());
            JSONArray jsonArray = new JSONArray();
            JSONObject obj = new JSONObject();
            Setting setting = settingRepository.getReferenceById(1L);
            List<Admin> admins = adminRepository.GetAdminByUser("baohanh01@gmail.com");
            if (videoViewHistories.size() == 0) {
                resp.put("videoview", "Lịch sử đơn trống!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            int viewCount = 0;
            //////////////////////////////////////////////////////////////////////////////////////////
            OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

            List<GoogleAPIKey> keys = googleAPIKeyRepository.getAllByState();
            Request request1 = null;
            request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=" + keys.get(0).getKey().trim() + "&fields=items(statistics(viewCount))&part=statistics&id=" + videoViewHistories.get(0).getVideoid().trim()).get().build();
            keys.get(0).setCount(keys.get(0).getCount() + 1L);
            googleAPIKeyRepository.save(keys.get(0));
            Response response1 = client1.newCall(request1).execute();

            String resultJson1 = response1.body().string();

            Object obj1 = new JSONParser().parse(resultJson1);

            JSONObject jsonObject1 = (JSONObject) obj1;
            JSONArray items = (JSONArray) jsonObject1.get("items");
            if (items == null) {
                obj.put("videoview", "Không check được view!");
                return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
            }
            Iterator k = items.iterator();
            if (k.hasNext() == false) {
                obj.put("videoview", "Không check được view!");
                return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
            }
            while (k.hasNext()) {
                try {
                    JSONObject video = (JSONObject) k.next();
                    JSONObject statistics = (JSONObject) video.get("statistics");
                    viewCount = Integer.parseInt(statistics.get("viewCount").toString());
                    if(videoViewHistories.get(0).getViewstart()>viewCount){
                        obj.put("videoview", "view HT < view Start");
                        return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                    }
                } catch (Exception e) {
                    System.out.println(e.getStackTrace()[0].getLineNumber());
                    throw new RuntimeException(e);
                }
            }
            /////////////////////////////////////////////////////////////////////////////////////////
            Service service = serviceRepository.getInfoService(videoViewHistories.get(0).getService());
            List<Admin> user = adminRepository.getAdminByUser(videoViewHistories.get(0).getUser());
            int vieworder_sum = 0;
            int viewthan_sum = 0;
            Float price_refund_sum = 0F;
            for (int i = 0; i < videoViewHistories.size(); i++) {
                vieworder_sum = vieworder_sum + (videoViewHistories.get(i).getVieworder() > videoViewHistories.get(i).getViewtotal() ? videoViewHistories.get(i).getViewtotal() : videoViewHistories.get(i).getVieworder());
            }
            int viewBH = vieworder_sum + videoViewHistories.get(0).getViewstart() - viewCount;
            System.out.println(viewBH);
            for (int i = videoViewHistories.size() - 1; i >= 0; i--) {
                if (viewBH <= 0) {
                    break;
                }
                System.out.println(viewBH);
                if (videoViewHistories.get(0).getCancel() > 0 && (videoViewHistories.get(0).getRefund() == null ? 0 : videoViewHistories.get(0).getRefund()) == 0) {
                    resp.put("videoview", "Đã hủy trước đó!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if (videoViewHistories.get(0).getPrice() == 0) {
                    continue;
                }
                if (videoViewRepository.getCountVideoId(videoViewHistories.get(i).getVideoid().trim()) > 0) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Đơn đang chạy!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }

                if (System.currentTimeMillis() - videoViewHistories.get(i).getEnddate() < 1000 * 3600 * 12) {
                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                    obj.put("videoview", "Refund sau: " + dateFormat.format((new Date(videoViewHistories.get(i).getEnddate() + (12 * 60 * 60 * 1000)))));
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                int viewFix = videoViewHistories.get(i).getVieworder() > videoViewHistories.get(i).getViewtotal() ? videoViewHistories.get(i).getViewtotal() : videoViewHistories.get(i).getVieworder();
                if (viewFix < viewBH) {
                    int viewthan = viewFix;
                    viewthan_sum = viewthan_sum + viewthan;
                    float price_refund = videoViewHistories.get(i).getPrice();
                    System.out.println(price_refund+"|"+viewthan);
                    //float pricebuffed=(videoBuffh.get(0).getViewtotal()/1000F)*service.getRate()*((float)(100-admins.get(0).getDiscount())/100);
                    if (videoViewHistories.get(i).getPrice() < price_refund) {
                        price_refund = videoViewHistories.get(i).getPrice();
                    }

                    price_refund_sum = price_refund_sum + price_refund;
                    float pricebuffed = (videoViewHistories.get(i).getPrice() - price_refund);
                    videoViewHistories.get(i).setPrice(pricebuffed);
                    videoViewHistories.get(i).setViewend(viewCount);
                    videoViewHistories.get(i).setTimecheckbh(System.currentTimeMillis());
                    videoViewHistories.get(i).setViewtotal(viewFix - viewthan);
                    videoViewHistories.get(i).setRefund(1);
                    if (price_refund == videoViewHistories.get(i).getPrice()) {
                        videoViewHistories.get(i).setCancel(1);
                    } else {
                        videoViewHistories.get(i).setCancel(2);
                    }
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    //hoàn tiền & add thong báo số dư
                    Float balance_update=adminRepository.updateBalanceFine(price_refund,videoViewHistories.get(i).getUser());
                    Balance balance = new Balance();
                    balance.setUser(user.get(0).getUsername().trim());
                    balance.setTime(System.currentTimeMillis());
                    balance.setTotalblance(balance_update);
                    balance.setBalance(price_refund);
                    balance.setService(videoViewHistories.get(i).getService());
                    balance.setNote("Refund " + (viewthan) + "view cho " + videoViewHistories.get(i).getVideoid());
                    balanceRepository.save(balance);
                    viewBH = viewBH - viewthan;
                } else {
                    int viewthan = viewFix + videoViewHistories.get(i).getViewstart() - viewCount;
                    if (viewthan > viewFix) {
                        viewthan = viewFix;
                    }
                    viewthan_sum = viewthan_sum + viewthan;
                    float price_refund = ((viewthan) / (float) viewFix) * videoViewHistories.get(i).getPrice();
                    //float pricebuffed=(videoBuffh.get(0).getViewtotal()/1000F)*service.getRate()*((float)(100-admins.get(0).getDiscount())/100);
                    if (videoViewHistories.get(i).getPrice() < price_refund) {
                        price_refund = videoViewHistories.get(i).getPrice();
                    }
                    price_refund_sum = price_refund_sum + price_refund;
                    float pricebuffed = (videoViewHistories.get(i).getPrice() - price_refund);
                    videoViewHistories.get(i).setPrice(pricebuffed);
                    videoViewHistories.get(i).setViewend(viewCount);
                    videoViewHistories.get(i).setViewtotal(viewFix - viewthan);
                    videoViewHistories.get(i).setRefund(1);
                    if (price_refund == videoViewHistories.get(i).getPrice()) {
                        videoViewHistories.get(i).setCancel(1);
                    } else {
                        videoViewHistories.get(i).setCancel(2);
                    }
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    //hoàn tiền & add thong báo số dư
                    Float balance_update=adminRepository.updateBalanceFine(price_refund,videoViewHistories.get(i).getUser());
                    Balance balance = new Balance();
                    balance.setUser(user.get(0).getUsername().trim());
                    balance.setTime(System.currentTimeMillis());
                    balance.setTotalblance(balance_update);
                    balance.setBalance(price_refund);
                    balance.setService(videoViewHistories.get(i).getService());
                    balance.setNote("Refund " + (viewthan) + "view cho " + videoViewHistories.get(i).getVideoid());
                    balanceRepository.save(balance);
                    viewBH = viewBH - viewthan;
                }
            }
            obj.put(videoViewHistories.get(0).getVideoid().trim(), "Refund  " + viewthan_sum + " view!");
            obj.put("videoview", "true");
            obj.put("videoid", videoViewHistories.get(0).getVideoid().trim());
            obj.put("balance", admins.get(0).getBalance());
            obj.put("price", price_refund_sum);
            obj.put("time", viewthan_sum);
            return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            System.out.println(e.getStackTrace()[0].getLineNumber());
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(path = "checkbhview", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> checkbhview(@RequestBody() VideoViewHistory videoid, @RequestHeader(defaultValue = "") String Authorization) {

        JSONObject resp = new JSONObject();
        List<Admin> admin = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admin.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        //System.out.println(videoid.getVideoid().trim());
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            List<VideoViewHistory> videoViewHistories = videoViewHistoryRepository.getVideoBHByVideoId(videoid.getVideoid().trim());
            JSONArray jsonArray = new JSONArray();
            Setting setting = settingRepository.getReferenceById(1L);
            List<Admin> admins = adminRepository.GetAdminByUser("baohanh01@gmail.com");
            if (videoViewHistories.size() == 0) {
                long orderid = 0L;
                try {
                    orderid = Long.parseLong(videoid.getVideoid().trim());
                } catch (Exception e) {
                    resp.put("videoview", "Lịch sử đơn trống!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                videoViewHistories = videoViewHistoryRepository.getVideoBHByOrderId(orderid);
                if (videoViewHistories.size() == 0) {
                    resp.put("videoview", "Lịch sử đơn trống!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                /*
                if (orderid != videoViewHistories.get(0).getOrderid()) {
                    resp.put("videoview", "Không đủ ĐK bảo hành! (OrderId :"+videoViewHistories.get(0).getOrderid()+")");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }

                 */
            }
            for (int i = 0; i < videoViewHistories.size(); i++) {
                JSONObject obj = new JSONObject();
                /*
                if ((videoViewHistories.get(0).getRefund() == null ? 0 : videoViewHistories.get(0).getRefund()) == 1) {
                    resp.put("videoview", "Đã refund trước đó!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                 */
                if (videoViewHistories.get(0).getCancel() > 0 && (videoViewHistories.get(0).getRefund() == null ? 0 : videoViewHistories.get(0).getRefund()) == 0) {
                    resp.put("videoview", "Đã hủy trước đó!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                /*
                if (videoViewRepository.getCountVideoId(videoViewHistories.get(i).getVideoid().trim()) > 0) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Đơn đang chạy!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                if (System.currentTimeMillis() - videoViewHistories.get(i).getEnddate() < 1000 * 3600 * 24) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Chưa đủ time bh!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }


                 */
                OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                Request request1 = null;
                request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=AIzaSyAyZOpEPeztraSXPk0Gwx-YqqZcmMONamQ&fields=items(statistics(viewCount))&part=statistics&id=" + videoViewHistories.get(i).getVideoid().trim()).get().build();

                Response response1 = client1.newCall(request1).execute();

                String resultJson1 = response1.body().string();

                Object obj1 = new JSONParser().parse(resultJson1);

                JSONObject jsonObject1 = (JSONObject) obj1;
                JSONArray items = (JSONArray) jsonObject1.get("items");
                if (items == null) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Không check được view!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                Iterator k = items.iterator();
                if (k.hasNext() == false) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Không check được view!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                while (k.hasNext()) {
                    try {
                        JSONObject video = (JSONObject) k.next();
                        JSONObject statistics = (JSONObject) video.get("statistics");
                        int baohanh = 0;
                        int viewFix = videoViewHistories.get(i).getVieworder() > videoViewHistories.get(i).getViewtotal() ? videoViewHistories.get(i).getViewtotal() : videoViewHistories.get(i).getVieworder();
                        baohanh = (int) ((int) (videoViewHistories.get(i).getViewstart() + viewFix - Integer.parseInt(statistics.get("viewCount").toString())));

                        obj.put(videoViewHistories.get(i).getVideoid().trim(), "Bảo hành " + baohanh + " view!");
                        obj.put("orderid", videoViewHistories.get(i).getOrderid());
                        obj.put("videoid", videoViewHistories.get(i).getVideoid());
                        obj.put("viewstart", videoViewHistories.get(i).getViewstart());
                        obj.put("videoview", "true");
                        obj.put("timestart", videoViewHistories.get(i).getInsertdate());
                        obj.put("timeend", videoViewHistories.get(i).getEnddate());
                        obj.put("vieworder", viewFix);
                        obj.put("refund", videoViewHistories.get(i).getRefund());
                        obj.put("viewcount", Integer.parseInt(statistics.get("viewCount").toString()));
                        obj.put("viewbh", baohanh);
                        return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                    } catch (Exception e) {
                        System.out.println(e.getStackTrace()[0].getLineNumber());
                        throw new RuntimeException(e);
                    }
                }
            }
            resp.put("rep", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            System.out.println(e.getStackTrace()[0].getLineNumber());
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping(path = "checkbhvideoidview", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> checkbhvideoidview(@RequestBody() VideoViewHistory videoid, @RequestHeader(defaultValue = "") String Authorization) {

        JSONObject resp = new JSONObject();
        List<Admin> admin = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admin.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        //System.out.println(videoid.getVideoid().trim());
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            List<VideoViewHistory> videoViewHistories = videoViewHistoryRepository.getVideoBHByVideoIdNoMaxOrderId(videoid.getVideoid().trim());
            JSONArray jsonArray = new JSONArray();
            Setting setting = settingRepository.getReferenceById(1L);
            List<Admin> admins = adminRepository.GetAdminByUser("baohanh01@gmail.com");
            if (videoViewHistories.size() == 0) {
                long orderid = 0L;
                try {
                    orderid = Long.parseLong(videoid.getVideoid().trim());
                } catch (Exception e) {
                    resp.put("videoview", "Lịch sử đơn trống!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                videoViewHistories = videoViewHistoryRepository.getVideoBHByOrderidNoMaxOrderId(orderid);
                if (videoViewHistories.size() == 0) {
                    resp.put("videoview", "Lịch sử đơn trống!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            }
            int vieworder_sum = 0;
            int viewtotal_sum = 0;
            String orderid_sum = "";
            Long timestart = 0L;
            Long timeend = 0L;
            for (int i = 0; i < videoViewHistories.size(); i++) {
                orderid_sum = orderid_sum + " | " + videoViewHistories.get(i).getOrderid();
                vieworder_sum = vieworder_sum + (videoViewHistories.get(i).getVieworder() > videoViewHistories.get(i).getViewtotal() ? videoViewHistories.get(i).getViewtotal() : videoViewHistories.get(i).getVieworder());
                viewtotal_sum = viewtotal_sum + videoViewHistories.get(i).getViewtotal();
                if (i == videoViewHistories.size() - 1) {
                    timestart = videoViewHistories.get(i).getInsertdate();
                    timeend = videoViewHistories.get(i).getEnddate();
                }

            }
            for (int i = 0; i < 1; i++) {
                JSONObject obj = new JSONObject();
                if (videoViewRepository.getCountVideoId(videoViewHistories.get(i).getVideoid().trim()) > 0) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Đơn đang chạy!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                Request request1 = null;
                request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=AIzaSyAyZOpEPeztraSXPk0Gwx-YqqZcmMONamQ&fields=items(statistics(viewCount))&part=statistics&id=" + videoViewHistories.get(i).getVideoid().trim()).get().build();

                Response response1 = client1.newCall(request1).execute();

                String resultJson1 = response1.body().string();

                Object obj1 = new JSONParser().parse(resultJson1);

                JSONObject jsonObject1 = (JSONObject) obj1;
                JSONArray items = (JSONArray) jsonObject1.get("items");
                if (items == null) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Không check được view!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                Iterator k = items.iterator();
                if (k.hasNext() == false) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Không check được view!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                while (k.hasNext()) {
                    try {
                        JSONObject video = (JSONObject) k.next();
                        JSONObject statistics = (JSONObject) video.get("statistics");
                        int baohanh = 0;
                        int viewFix = vieworder_sum;
                        baohanh = (int) ((int) (videoViewHistories.get(i).getViewstart() + viewFix - Integer.parseInt(statistics.get("viewCount").toString())));

                        obj.put(videoViewHistories.get(i).getVideoid().trim(), "Bảo hành " + baohanh + " view!");
                        obj.put("orderid", orderid_sum);
                        obj.put("videoid", videoViewHistories.get(i).getVideoid());
                        obj.put("viewstart", videoViewHistories.get(i).getViewstart());
                        obj.put("videoview", "true");
                        obj.put("timestart", timestart);
                        obj.put("timeend", timeend);
                        obj.put("vieworder", viewFix);
                        obj.put("refund", videoViewHistories.get(i).getRefund());
                        obj.put("viewcount", Integer.parseInt(statistics.get("viewCount").toString()));
                        obj.put("viewbh", baohanh);
                        return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                    } catch (Exception e) {
                        System.out.println(e.getStackTrace()[0].getLineNumber());
                        throw new RuntimeException(e);
                    }
                }
            }
            resp.put("rep", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            System.out.println(e.getStackTrace()[0].getLineNumber());
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(path = "bhviewbyvideoid", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> bhviewbyvideoid(@RequestBody() VideoViewHistory videoid, @RequestHeader(defaultValue = "") String Authorization) {
        JSONObject resp = new JSONObject();
        List<Admin> admin = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admin.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        //System.out.println(videoid.getVideoid().trim());
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            List<VideoViewHistory> videoViewHistories = videoViewHistoryRepository.getVideoBHByVideoIdNoMaxOrderIdCancel0(videoid.getVideoid().trim());
            JSONArray jsonArray = new JSONArray();
            Setting setting = settingRepository.getReferenceById(1L);
            List<Admin> admins = adminRepository.GetAdminByUser("baohanh01@gmail.com");
            if (videoViewHistories.size() == 0) {
                resp.put("videoview", "Lịch sử đơn trống!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            int vieworder_sum = 0;
            String orderid_sum = "";
            for (int i = 0; i < videoViewHistories.size(); i++) {
                orderid_sum = orderid_sum + " | " + videoViewHistories.get(i).getOrderid();
                vieworder_sum = vieworder_sum + (videoViewHistories.get(i).getVieworder() > videoViewHistories.get(i).getViewtotal() ? videoViewHistories.get(i).getViewtotal() : videoViewHistories.get(i).getVieworder());
            }
            for (int i = 0; i < 1; i++) {
                JSONObject obj = new JSONObject();
                if (videoViewRepository.getCountVideoId(videoViewHistories.get(i).getVideoid().trim()) > 0) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Đơn đang chạy!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                if (System.currentTimeMillis() - videoViewHistories.get(i).getEnddate() < 1000 * 3600 * 12) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistories.get(i).setWaitbh(1);
                    videoViewHistories.get(i).setTimecheckbh(videoViewHistories.get(i).getEnddate() + (12 * 60 * 60 * 1000));
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                    obj.put("videoview", "Bảo hành sau: " + dateFormat.format((new Date(videoViewHistories.get(i).getEnddate() + (12 * 60 * 60 * 1000)))));
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                List<VideoViewHistory> viewHistories = videoViewHistoryRepository.getTimeBHByVideoId(videoViewHistories.get(i).getVideoid().trim());
                if (viewHistories.size() > 0) {
                    if (System.currentTimeMillis() - viewHistories.get(0).getEnddate() < 1000 * 3600 * 12) {
                        videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                        videoViewHistoryRepository.save(videoViewHistories.get(i));
                        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                        obj.put("videoview", "Bảo hành sau: " + dateFormat.format(new Date(viewHistories.get(0).getEnddate() + (12 * 60 * 60 * 1000))));
                        return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                    }
                }
                OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                Request request1 = null;
                request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=AIzaSyD5KyNKQtDkpgpav-R9Tgl1aYSPMN8AwUw&fields=items(statistics(viewCount))&part=statistics&id=" + videoViewHistories.get(i).getVideoid().trim()).get().build();

                Response response1 = client1.newCall(request1).execute();

                String resultJson1 = response1.body().string();

                Object obj1 = new JSONParser().parse(resultJson1);

                JSONObject jsonObject1 = (JSONObject) obj1;
                JSONArray items = (JSONArray) jsonObject1.get("items");
                if (items == null) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Không check được view!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                Iterator k = items.iterator();
                if (k.hasNext() == false) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Không check được view!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                while (k.hasNext()) {
                    try {
                        JSONObject video = (JSONObject) k.next();
                        JSONObject statistics = (JSONObject) video.get("statistics");
                        int baohanh = 0;
                        int viewFix = vieworder_sum;
                        baohanh = videoViewHistories.get(i).getViewstart() + viewFix - Integer.parseInt(statistics.get("viewCount").toString());
                        System.out.println(videoViewHistories.get(i).getViewstart()+"|"+Integer.parseInt(statistics.get("viewCount").toString()));
                        if(videoViewHistories.get(i).getViewstart()>Integer.parseInt(statistics.get("viewCount").toString())){
                            obj.put("videoview", "view HT < view Start");
                            return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                        }
                        if (baohanh < 50) {
                            baohanh = 50;
                        }
                        float priceorder = 0;
                        Service service = serviceRepository.getInfoService(videoViewHistories.get(i).getService());
                        priceorder = (baohanh / 1000F) * service.getRate() * ((float) (admins.get(0).getRate()) / 100) * ((float) (100 - admins.get(0).getDiscount()) / 100);
                        if (priceorder > (float) admins.get(0).getBalance()) {
                            obj.put("videoview", "Số tiền không đủ!");
                            return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                        }

                        VideoView videoViewhnew = new VideoView();
                        videoViewhnew.setDuration(videoViewHistories.get(i).getDuration());
                        videoViewhnew.setInsertdate(System.currentTimeMillis());
                        videoViewhnew.setView24h(0);
                        videoViewhnew.setViewtotal(0);
                        videoViewhnew.setVieworder(baohanh);
                        videoViewhnew.setUser(admins.get(0).getUsername());
                        videoViewhnew.setChannelid(videoViewHistories.get(i).getChannelid());
                        videoViewhnew.setVideotitle(videoViewHistories.get(i).getVideotitle());
                        videoViewhnew.setVideoid(videoViewHistories.get(i).getVideoid());
                        videoViewhnew.setViewstart(Integer.parseInt(statistics.get("viewCount").toString()));
                        int max_thread = service.getThread() + (((int) (baohanh < 1000 ? 1000 : baohanh) / 1000) - 1) * setting.getLevelthread();
                        if (max_thread <= setting.getMaxthread()) {
                            videoViewhnew.setMaxthreads(max_thread);
                        } else {
                            videoViewhnew.setMaxthreads(setting.getMaxthread());
                        }
                        videoViewhnew.setPrice(priceorder);
                        videoViewhnew.setNote(videoViewHistories.get(i).getUser() + "| BHL" + (int) (videoViewHistories.get(i).getNumbh() + 1));
                        videoViewhnew.setService(videoViewHistories.get(i).getService());
                        videoViewhnew.setValid(1);
                        videoViewRepository.save(videoViewhnew);
                        videoViewHistories.get(i).setNumbh(videoViewHistories.get(i).getNumbh() + 1);
                        videoViewHistoryRepository.save(videoViewHistories.get(i));
                        if (service.getType().equals("Special")) {
                            String list_key = dataOrderRepository.getListKeyByOrderid(videoViewHistories.get(i).getOrderid());
                            DataOrder dataOrder = new DataOrder();
                            dataOrder.setOrderid(videoViewhnew.getOrderid());
                            dataOrder.setListvideo(list_key);
                            dataOrder.setListkey(list_key);
                            dataOrderRepository.save(dataOrder);
                        }
                        Float balance_update=adminRepository.updateBalanceFine(-priceorder,admins.get(0).getUsername().trim());
                        Balance balance = new Balance();
                        balance.setUser(admins.get(0).getUsername().trim());
                        balance.setTime(System.currentTimeMillis());
                        balance.setTotalblance(balance_update);
                        balance.setBalance(-priceorder);
                        balance.setService(service.getService());
                        balance.setNote("Bảo hành " + baohanh + " view cho video " + videoViewHistories.get(i).getVideoid());
                        balanceRepository.save(balance);

                        obj.put(videoViewHistories.get(i).getVideoid().trim(), "Bảo hành " + baohanh + " view!");
                        obj.put("videoview", "true");
                        obj.put("balance", admins.get(0).getBalance());
                        obj.put("price", priceorder);
                        obj.put("time", baohanh);
                        return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                    } catch (Exception e) {
                        System.out.println(e.getStackTrace()[0].getLineNumber());
                        throw new RuntimeException(e);
                    }
                }
            }
            resp.put("rep", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            System.out.println(e.getStackTrace()[0].getLineNumber());
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "getorderviewhhistory", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getorderviewhhistory(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String user) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            List<VideoViewHistory> orderRunnings;
            if (user.length() == 0) {
                orderRunnings = videoViewHistoryRepository.getVideoViewHistories();
            } else {
                orderRunnings = videoViewHistoryRepository.getVideoViewHistories(user.trim());
            }
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < orderRunnings.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("orderid", orderRunnings.get(i).getOrderid());
                obj.put("videoid", orderRunnings.get(i).getVideoid());
                obj.put("videotitle", orderRunnings.get(i).getVideotitle());
                obj.put("viewstart", orderRunnings.get(i).getViewstart());
                obj.put("maxthreads", orderRunnings.get(i).getMaxthreads());
                obj.put("insertdate", orderRunnings.get(i).getInsertdate());
                obj.put("user", orderRunnings.get(i).getUser());
                obj.put("note", orderRunnings.get(i).getNote());
                obj.put("duration", orderRunnings.get(i).getDuration());
                obj.put("enddate", orderRunnings.get(i).getEnddate());
                obj.put("cancel", orderRunnings.get(i).getCancel());
                obj.put("timestart", orderRunnings.get(i).getTimestart());
                obj.put("timecheckbh", orderRunnings.get(i).getTimecheckbh());
                obj.put("viewend", orderRunnings.get(i).getViewend());
                obj.put("viewtotal", orderRunnings.get(i).getViewtotal());
                obj.put("vieworder", orderRunnings.get(i).getVieworder());
                obj.put("price", orderRunnings.get(i).getPrice());
                obj.put("service", orderRunnings.get(i).getService());
                jsonArray.add(obj);
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");

            resp.put("total", orderRunnings.size());
            resp.put("videoview", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "findorder", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> findorder(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String videoid) {
        JSONObject resp = new JSONObject();
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            Integer find_channelid=videoid.trim().indexOf("@");
            System.out.println(find_channelid);
            if(find_channelid==0){
                videoid=videoid.replace("@","");
                System.out.println(videoid);
            }
            List<String> ordersArrInput = new ArrayList<>();
            ordersArrInput.addAll(Arrays.asList(videoid.split(",")));
            List<VideoViewHistory> orderRunnings;
            if(admins.get(0).getRole().equals("ROLE_ADMIN")){
                if(find_channelid==0){
                    orderRunnings = videoViewHistoryRepository.getVideoViewHistoriesByListChannelId(ordersArrInput);
                }else{
                    orderRunnings = videoViewHistoryRepository.getVideoViewHistoriesByListVideoId(ordersArrInput);
                }
            }else{
                if(find_channelid==0){
                    orderRunnings = videoViewHistoryRepository.getVideoViewHistoriesByListChannelId(ordersArrInput,admins.get(0).getUsername().trim());
                }else{
                    orderRunnings = videoViewHistoryRepository.getVideoViewHistoriesByListVideoId(ordersArrInput,admins.get(0).getUsername().trim());
                }
            }
            if (orderRunnings.size() == 0) {
                resp.put("status", "fail");
                resp.put("total", orderRunnings.size());
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < orderRunnings.size(); i++) {
                JSONObject obj = new JSONObject();
                String infoQ;
                if(admins.get(0).getRole().equals("ROLE_ADMIN")) {
                    infoQ = videoViewHistoryRepository.getInfoSumOrderByVideoId(orderRunnings.get(i).getVideoid(), orderRunnings.get(i).getOrderid());
                }else {
                    infoQ = videoViewHistoryRepository.getInfoSumOrderByVideoId(orderRunnings.get(i).getVideoid(), orderRunnings.get(i).getOrderid(),admins.get(0).getUsername().trim());
                }
                if(infoQ!=null){
                    obj.put("info", infoQ);
                }else{
                    obj.put("info", "");
                }
                obj.put("orderid", orderRunnings.get(i).getOrderid());
                obj.put("videoid", orderRunnings.get(i).getVideoid());
                obj.put("videotitle", orderRunnings.get(i).getVideotitle());
                obj.put("viewstart", orderRunnings.get(i).getViewstart());
                obj.put("maxthreads", orderRunnings.get(i).getMaxthreads());
                obj.put("insertdate", orderRunnings.get(i).getInsertdate());
                obj.put("user", orderRunnings.get(i).getUser());
                obj.put("note", orderRunnings.get(i).getNote());
                obj.put("duration", orderRunnings.get(i).getDuration());
                obj.put("enddate", orderRunnings.get(i).getEnddate());
                obj.put("cancel", orderRunnings.get(i).getCancel());
                obj.put("timestart", orderRunnings.get(i).getTimestart());
                obj.put("timecheckbh", orderRunnings.get(i).getTimecheckbh());
                obj.put("viewend", orderRunnings.get(i).getViewend());
                obj.put("viewtotal", orderRunnings.get(i).getViewtotal());
                obj.put("vieworder", orderRunnings.get(i).getVieworder());
                obj.put("price", orderRunnings.get(i).getPrice());
                obj.put("service", orderRunnings.get(i).getService());
                jsonArray.add(obj);
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");
            resp.put("total", orderRunnings.size());
            resp.put("videoview", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(path = "getcountviewbufforder", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getcountviewbufforder(@RequestParam(defaultValue = "") String user) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            Integer countvieworder = 0;
            if (user.length() == 0) {
                countvieworder = videoViewRepository.getCountViewBuffOrder();
            } else {
                countvieworder = videoViewRepository.getCountViewBuffOrder(user.trim());
            }
            resp.put("totalvieworder", countvieworder);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "getkey", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getkey() {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<GoogleAPIKey> keys = googleAPIKeyRepository.getAllByState();
        keys.get(0).setCount(keys.get(0).getCount() + 1L);
        googleAPIKeyRepository.save(keys.get(0));
        resp.put("key", keys.get(0).getKey());
        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
    }
    @GetMapping(path = "getinfo", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getinfo(@RequestParam(defaultValue = "") Long orderid) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            VideoView videoView = videoViewRepository.getInfoByOrderId(orderid);
            JSONArray jsonArray = new JSONArray();


            JSONObject obj = new JSONObject();
            obj.put("orderid", videoView.getOrderid());
            obj.put("videoid", videoView.getVideoid());
            obj.put("videotitle", videoView.getVideotitle());
            obj.put("viewstart", videoView.getViewstart());
            obj.put("maxthreads", videoView.getMaxthreads());
            obj.put("insertdate", videoView.getInsertdate());
            obj.put("vieworder", videoView.getVieworder());
            obj.put("note", videoView.getNote());
            obj.put("duration", videoView.getDuration());
            obj.put("service", videoView.getService());

            obj.put("view24h", videoView.getView24h());
            obj.put("viewtotal", videoView.getViewtotal());
            obj.put("price", videoView.getPrice());
            if (serviceRepository.getService(videoView.getService()).getType().equals("Special")) {
                obj.put("keyword", dataOrderRepository.getListKeyByOrderid(orderid));
            } else {
                obj.put("keyword", "");
            }
            jsonArray.add(obj);

            resp.put("info", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "getcountviewbuffedorder", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getcountviewbuffedorder(@RequestParam(defaultValue = "") String user) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            Integer countvieworder = 0;
            if (user.length() == 0) {
                countvieworder = videoViewRepository.getCountViewBuffedOrder();
            } else {
                countvieworder = videoViewRepository.getCountViewBuffedOrder(user.trim());
            }
            resp.put("totalviewbuffedorder", countvieworder);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(path = "delete", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delete(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String videoid, @RequestParam(defaultValue = "1") Integer cancel) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (videoid.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "videoid không được để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            String[] videoidArr = videoid.split(",");
            for (int i = 0; i < videoidArr.length; i++) {

                Long enddate = System.currentTimeMillis();
                List<VideoView> videoBuffh = videoViewRepository.getVideoBuffhById(videoidArr[i].trim());
                VideoViewHistory videoBuffhnew = new VideoViewHistory();
                videoBuffhnew.setOrderid(videoBuffh.get(0).getOrderid());
                videoBuffhnew.setDuration(videoBuffh.get(0).getDuration());
                videoBuffhnew.setInsertdate(videoBuffh.get(0).getInsertdate());
                videoBuffhnew.setService(videoBuffh.get(0).getService());
                videoBuffhnew.setChannelid(videoBuffh.get(0).getChannelid());
                videoBuffhnew.setVideotitle(videoBuffh.get(0).getVideotitle());
                videoBuffhnew.setVideoid(videoBuffh.get(0).getVideoid());
                videoBuffhnew.setViewstart(videoBuffh.get(0).getViewstart());
                videoBuffhnew.setVieworder(videoBuffh.get(0).getVieworder());
                videoBuffhnew.setMaxthreads(videoBuffh.get(0).getMaxthreads());
                videoBuffhnew.setNote(videoBuffh.get(0).getNote());
                videoBuffhnew.setNumbh(0);
                videoBuffhnew.setTimecheck(0L);
                videoBuffhnew.setTimestart(videoBuffh.get(0).getTimestart());
                //videoBuffhnew.setPrice(videoBuffh.get(0).getPrice());
                if (cancel == 1) {
                    Service service = serviceRepository.getInfoService(videoBuffh.get(0).getService());
                    List<Admin> user = adminRepository.getAdminByUser(videoBuffh.get(0).getUser());
                    //Hoàn tiền những view chưa buff
                    int viewbuff = videoBuffh.get(0).getViewtotal();
                    int viewthan = videoBuffh.get(0).getVieworder() - (videoBuffh.get(0).getViewtotal() > videoBuffh.get(0).getVieworder() ? videoBuffh.get(0).getVieworder() : videoBuffh.get(0).getViewtotal());
                    //System.out.println(videoBuffh.get(0).getViewtotal() > videoBuffh.get(0).getVieworder() ? videoBuffh.get(0).getVieworder() : videoBuffh.get(0).getViewtotal());
                    float price_refund = (viewthan / (float) videoBuffh.get(0).getVieworder()) * videoBuffh.get(0).getPrice();
                    //float pricebuffed=(videoBuffh.get(0).getViewtotal()/1000F)*service.getRate()*((float)(100-admins.get(0).getDiscount())/100);
                    float pricebuffed = (videoBuffh.get(0).getPrice() - price_refund);
                    videoBuffhnew.setPrice(pricebuffed);
                    if (viewbuff == 0) {
                        videoBuffhnew.setCancel(1);
                    } else if (viewbuff >= videoBuffh.get(0).getVieworder()) {
                        videoBuffhnew.setCancel(0);
                    } else {
                        videoBuffhnew.setCancel(2);
                    }
                    //hoàn tiền & add thong báo số dư
                    if (viewthan > 0) {
                        Float balance_update=adminRepository.updateBalanceFine(price_refund,videoBuffh.get(0).getUser().trim());
                        Balance balance = new Balance();
                        balance.setUser(user.get(0).getUsername().trim());
                        balance.setTime(System.currentTimeMillis());
                        balance.setTotalblance(balance_update);
                        balance.setBalance(price_refund);
                        balance.setService(videoBuffh.get(0).getService());
                        balance.setNote("Refund " + (viewthan) + " view cho " + videoBuffh.get(0).getVideoid());
                        balanceRepository.save(balance);
                    }
                } else {
                    videoBuffhnew.setPrice(videoBuffh.get(0).getPrice());
                    videoBuffhnew.setCancel(0);
                }
                videoBuffhnew.setUser(videoBuffh.get(0).getUser());
                videoBuffhnew.setEnddate(enddate);
                videoBuffhnew.setViewtotal(videoBuffh.get(0).getViewtotal());
                videoBuffhnew.setTimetotal(videoBuffh.get(0).getTimetotal() == null ? 0 : videoBuffh.get(0).getTimetotal());
                videoViewHistoryRepository.save(videoBuffhnew);
                videoViewRepository.deletevideoByVideoId(videoidArr[i].trim());
            }
            resp.put("videoview", "");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(path = "updatechanneldonecron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updatechanneldonecron() {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            //historyRepository.updateHistoryByAccount();
            List<VideoView> videoBuffh = videoViewRepository.getOrderFullView();
            for (int i = 0; i < videoBuffh.size(); i++) {
                Long enddate = System.currentTimeMillis();

                VideoViewHistory videoBuffhnew = new VideoViewHistory();
                videoBuffhnew.setOrderid(videoBuffh.get(i).getOrderid());
                videoBuffhnew.setDuration(videoBuffh.get(i).getDuration());
                videoBuffhnew.setInsertdate(videoBuffh.get(i).getInsertdate());
                videoBuffhnew.setChannelid(videoBuffh.get(i).getChannelid());
                videoBuffhnew.setVideotitle(videoBuffh.get(i).getVideotitle());
                videoBuffhnew.setVideoid(videoBuffh.get(i).getVideoid());
                videoBuffhnew.setViewstart(videoBuffh.get(i).getViewstart());
                videoBuffhnew.setMaxthreads(videoBuffh.get(i).getMaxthreads());
                videoBuffhnew.setNote(videoBuffh.get(i).getNote());
                videoBuffhnew.setCancel(0);
                videoBuffhnew.setNumbh(0);
                videoBuffhnew.setTimecheck(0L);
                videoBuffhnew.setTimestart(videoBuffh.get(i).getTimestart());
                videoBuffhnew.setUser(videoBuffh.get(i).getUser());
                videoBuffhnew.setEnddate(enddate);
                videoBuffhnew.setService(videoBuffh.get(i).getService());
                videoBuffhnew.setViewtotal(videoBuffh.get(i).getViewtotal());
                videoBuffhnew.setVieworder(videoBuffh.get(i).getVieworder());
                videoBuffhnew.setPrice(videoBuffh.get(i).getPrice());
                videoBuffhnew.setTimetotal(videoBuffh.get(0).getTimetotal() == null ? 0 : videoBuffh.get(0).getTimetotal());
                try {
                    videoViewHistoryRepository.save(videoBuffhnew);
                    videoViewRepository.deletevideoByVideoId(videoBuffh.get(i).getVideoid().trim());
                } catch (Exception e) {

                }
            }
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "updateThreadByThreadSet", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateThreadByThreadSet() {
        JSONObject resp = new JSONObject();
        Random ran = new Random();
        try {
            Thread.sleep(ran.nextInt(1000));
            videoViewRepository.updateThreadByThreadSet5m();
            Thread.sleep(ran.nextInt(1000));
            videoViewRepository.updateThreadByThreadSet10m();
            Thread.sleep(ran.nextInt(1000));
            videoViewRepository.updateThreadByThreadSet15m();
            Thread.sleep(ran.nextInt(1000));
            videoViewRepository.updateThreadByThreadSet20m();
            Thread.sleep(ran.nextInt(1000));
            videoViewRepository.updateThreadByThreadSet30m();
            Thread.sleep(ran.nextInt(1000));
            updateRunningOrder();
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
        catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(path = "updateorderlivedonecron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateorderlivedonecron() {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            //historyRepository.updateHistoryByAccount();
            List<VideoView> videoBuffh = videoViewRepository.getOrderFullLive();
            for (int i = 0; i < videoBuffh.size(); i++) {
                Long enddate = System.currentTimeMillis();

                VideoViewHistory videoBuffhnew = new VideoViewHistory();
                videoBuffhnew.setOrderid(videoBuffh.get(i).getOrderid());
                videoBuffhnew.setDuration(videoBuffh.get(i).getDuration());
                videoBuffhnew.setInsertdate(videoBuffh.get(i).getInsertdate());
                videoBuffhnew.setChannelid(videoBuffh.get(i).getChannelid());
                videoBuffhnew.setVideotitle(videoBuffh.get(i).getVideotitle());
                videoBuffhnew.setVideoid(videoBuffh.get(i).getVideoid());
                videoBuffhnew.setViewstart(videoBuffh.get(i).getViewstart());
                videoBuffhnew.setMaxthreads(videoBuffh.get(i).getMaxthreads());
                videoBuffhnew.setNote(videoBuffh.get(i).getNote());
                videoBuffhnew.setCancel(0);
                videoBuffhnew.setNumbh(0);
                videoBuffhnew.setTimecheck(0L);
                videoBuffhnew.setTimestart(videoBuffh.get(i).getTimestart());
                videoBuffhnew.setUser(videoBuffh.get(i).getUser());
                videoBuffhnew.setEnddate(enddate);
                videoBuffhnew.setService(videoBuffh.get(i).getService());
                videoBuffhnew.setViewtotal(videoBuffh.get(i).getViewtotal());
                videoBuffhnew.setVieworder(videoBuffh.get(i).getVieworder());
                videoBuffhnew.setPrice(videoBuffh.get(i).getPrice());
                videoBuffhnew.setTimetotal(videoBuffh.get(0).getTimetotal() == null ? 0 : videoBuffh.get(0).getTimetotal());
                try {
                    videoViewHistoryRepository.save(videoBuffhnew);
                    videoViewRepository.deletevideoByVideoId(videoBuffh.get(i).getVideoid().trim());
                } catch (Exception e) {

                }
            }
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "updateorderbuffh60mdonecron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateorderbuffh60mdonecron() {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            //historyRepository.updateHistoryByAccount();
            List<VideoView> videoBuffh = videoViewRepository.getOrderFullTime60m();
            for (int i = 0; i < videoBuffh.size(); i++) {
                Long enddate = System.currentTimeMillis();

                VideoViewHistory videoBuffhnew = new VideoViewHistory();
                videoBuffhnew.setOrderid(videoBuffh.get(i).getOrderid());
                videoBuffhnew.setDuration(videoBuffh.get(i).getDuration());
                videoBuffhnew.setInsertdate(videoBuffh.get(i).getInsertdate());
                videoBuffhnew.setChannelid(videoBuffh.get(i).getChannelid());
                videoBuffhnew.setVideotitle(videoBuffh.get(i).getVideotitle());
                videoBuffhnew.setVideoid(videoBuffh.get(i).getVideoid());
                videoBuffhnew.setViewstart(videoBuffh.get(i).getViewstart());
                videoBuffhnew.setMaxthreads(videoBuffh.get(i).getMaxthreads());
                videoBuffhnew.setNote(videoBuffh.get(i).getNote());
                videoBuffhnew.setCancel(0);
                videoBuffhnew.setNumbh(0);
                videoBuffhnew.setTimecheck(0L);
                videoBuffhnew.setTimestart(videoBuffh.get(i).getTimestart());
                videoBuffhnew.setUser(videoBuffh.get(i).getUser());
                videoBuffhnew.setEnddate(enddate);
                videoBuffhnew.setService(videoBuffh.get(i).getService());
                videoBuffhnew.setViewtotal(videoBuffh.get(i).getViewtotal());
                videoBuffhnew.setVieworder(videoBuffh.get(i).getVieworder());
                videoBuffhnew.setPrice(videoBuffh.get(i).getPrice());
                videoBuffhnew.setTimetotal(videoBuffh.get(0).getTimetotal() == null ? 0 : videoBuffh.get(0).getTimetotal());
                try {
                    videoViewHistoryRepository.save(videoBuffhnew);
                    videoViewRepository.deletevideoByVideoId(videoBuffh.get(i).getVideoid().trim());
                } catch (Exception e) {

                }
            }
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(path = "updateorderbuffh30mdonecron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateorderbuffh30mdonecron() {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            //historyRepository.updateHistoryByAccount();
            List<VideoView> videoBuffh = videoViewRepository.getOrderFullTime30m();
            for (int i = 0; i < videoBuffh.size(); i++) {
                Long enddate = System.currentTimeMillis();

                VideoViewHistory videoBuffhnew = new VideoViewHistory();
                videoBuffhnew.setOrderid(videoBuffh.get(i).getOrderid());
                videoBuffhnew.setDuration(videoBuffh.get(i).getDuration());
                videoBuffhnew.setInsertdate(videoBuffh.get(i).getInsertdate());
                videoBuffhnew.setChannelid(videoBuffh.get(i).getChannelid());
                videoBuffhnew.setVideotitle(videoBuffh.get(i).getVideotitle());
                videoBuffhnew.setVideoid(videoBuffh.get(i).getVideoid());
                videoBuffhnew.setViewstart(videoBuffh.get(i).getViewstart());
                videoBuffhnew.setMaxthreads(videoBuffh.get(i).getMaxthreads());
                videoBuffhnew.setNote(videoBuffh.get(i).getNote());
                videoBuffhnew.setCancel(0);
                videoBuffhnew.setNumbh(0);
                videoBuffhnew.setTimecheck(0L);
                videoBuffhnew.setTimestart(videoBuffh.get(i).getTimestart());
                videoBuffhnew.setUser(videoBuffh.get(i).getUser());
                videoBuffhnew.setEnddate(enddate);
                videoBuffhnew.setService(videoBuffh.get(i).getService());
                videoBuffhnew.setViewtotal(videoBuffh.get(i).getViewtotal());
                videoBuffhnew.setVieworder(videoBuffh.get(i).getVieworder());
                videoBuffhnew.setPrice(videoBuffh.get(i).getPrice());
                videoBuffhnew.setTimetotal(videoBuffh.get(0).getTimetotal() == null ? 0 : videoBuffh.get(0).getTimetotal());
                try {
                    videoViewHistoryRepository.save(videoBuffhnew);
                    videoViewRepository.deletevideoByVideoId(videoBuffh.get(i).getVideoid().trim());
                } catch (Exception e) {

                }
            }
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "updateorderbuffh15mdonecron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateorderbuffh15mdonecron() {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            //historyRepository.updateHistoryByAccount();
            List<VideoView> videoBuffh = videoViewRepository.getOrderFullTime15m();
            for (int i = 0; i < videoBuffh.size(); i++) {
                Long enddate = System.currentTimeMillis();

                VideoViewHistory videoBuffhnew = new VideoViewHistory();
                videoBuffhnew.setOrderid(videoBuffh.get(i).getOrderid());
                videoBuffhnew.setDuration(videoBuffh.get(i).getDuration());
                videoBuffhnew.setInsertdate(videoBuffh.get(i).getInsertdate());
                videoBuffhnew.setChannelid(videoBuffh.get(i).getChannelid());
                videoBuffhnew.setVideotitle(videoBuffh.get(i).getVideotitle());
                videoBuffhnew.setVideoid(videoBuffh.get(i).getVideoid());
                videoBuffhnew.setViewstart(videoBuffh.get(i).getViewstart());
                videoBuffhnew.setMaxthreads(videoBuffh.get(i).getMaxthreads());
                videoBuffhnew.setNote(videoBuffh.get(i).getNote());
                videoBuffhnew.setCancel(0);
                videoBuffhnew.setNumbh(0);
                videoBuffhnew.setTimecheck(0L);
                videoBuffhnew.setTimestart(videoBuffh.get(i).getTimestart());
                videoBuffhnew.setUser(videoBuffh.get(i).getUser());
                videoBuffhnew.setEnddate(enddate);
                videoBuffhnew.setService(videoBuffh.get(i).getService());
                videoBuffhnew.setViewtotal(videoBuffh.get(i).getViewtotal());
                videoBuffhnew.setVieworder(videoBuffh.get(i).getVieworder());
                videoBuffhnew.setPrice(videoBuffh.get(i).getPrice());
                videoBuffhnew.setTimetotal(videoBuffh.get(0).getTimetotal() == null ? 0 : videoBuffh.get(0).getTimetotal());
                try {
                    videoViewHistoryRepository.save(videoBuffhnew);
                    videoViewRepository.deletevideoByVideoId(videoBuffh.get(i).getVideoid().trim());
                } catch (Exception e) {

                }
            }
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "updateorderbuffh10mdonecron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateorderbuffh10mdonecron() {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            //historyRepository.updateHistoryByAccount();
            List<VideoView> videoBuffh = videoViewRepository.getOrderFullTime10m();
            for (int i = 0; i < videoBuffh.size(); i++) {
                Long enddate = System.currentTimeMillis();

                VideoViewHistory videoBuffhnew = new VideoViewHistory();
                videoBuffhnew.setOrderid(videoBuffh.get(i).getOrderid());
                videoBuffhnew.setDuration(videoBuffh.get(i).getDuration());
                videoBuffhnew.setInsertdate(videoBuffh.get(i).getInsertdate());
                videoBuffhnew.setChannelid(videoBuffh.get(i).getChannelid());
                videoBuffhnew.setVideotitle(videoBuffh.get(i).getVideotitle());
                videoBuffhnew.setVideoid(videoBuffh.get(i).getVideoid());
                videoBuffhnew.setViewstart(videoBuffh.get(i).getViewstart());
                videoBuffhnew.setMaxthreads(videoBuffh.get(i).getMaxthreads());
                videoBuffhnew.setNote(videoBuffh.get(i).getNote());
                videoBuffhnew.setCancel(0);
                videoBuffhnew.setNumbh(0);
                videoBuffhnew.setTimecheck(0L);
                videoBuffhnew.setTimestart(videoBuffh.get(i).getTimestart());
                videoBuffhnew.setUser(videoBuffh.get(i).getUser());
                videoBuffhnew.setEnddate(enddate);
                videoBuffhnew.setService(videoBuffh.get(i).getService());
                videoBuffhnew.setViewtotal(videoBuffh.get(i).getViewtotal());
                videoBuffhnew.setVieworder(videoBuffh.get(i).getVieworder());
                videoBuffhnew.setPrice(videoBuffh.get(i).getPrice());
                videoBuffhnew.setTimetotal(videoBuffh.get(0).getTimetotal() == null ? 0 : videoBuffh.get(0).getTimetotal());
                try {
                    videoViewHistoryRepository.save(videoBuffhnew);
                    videoViewRepository.deletevideoByVideoId(videoBuffh.get(i).getVideoid().trim());
                } catch (Exception e) {

                }
            }
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "refund", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> refund(@RequestParam(defaultValue = "") String orderid) {
        JSONObject resp = new JSONObject();
        if(orderid.length()==0){
            resp.put("status", "fail");
            resp.put("message", "OrderId không được trống!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
        List<String> ordersArrInput = new ArrayList<>();
        ordersArrInput.addAll(Arrays.asList(orderid.split(",")));
        try {
            videoViewHistoryRepository.updateRefund(ordersArrInput);
            resp.put("status", "true");
            resp.put("message", "Refund đơn thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "updateordercheck", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateordercheck(@RequestParam(defaultValue = "") String videoid) {
        JSONObject resp = new JSONObject();
        try {
            String[] videoidArr = videoid.split(",");
            for (int i = 0; i < videoidArr.length; i++) {
                videoViewRepository.updateOrderCheck(videoidArr[i]);
            }
            resp.put("videoview", "");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "updatecheckcancel", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updatecheckcancel(@RequestParam(defaultValue = "") String videoid) {
        JSONObject resp = new JSONObject();
        try {
            videoViewRepository.updateCheckCancel(videoid.trim());
            resp.put("status", "true");
            resp.put("message", "update trạng thái đơn thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping(path = "update", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> update(@RequestHeader(defaultValue = "") String Authorization, @RequestBody VideoView videoBuffh) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            String[] videoidIdArr = videoBuffh.getVideoid().split("\n");
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < videoidIdArr.length; i++) {
                List<VideoView> video = videoViewRepository.getVideoBuffhById(videoidIdArr[i].trim());
                float priceorder = 0;
                if (videoBuffh.getVieworder() != video.get(0).getVieworder()) {
                    Service service = serviceRepository.getInfoService(video.get(0).getService());
                    List<Admin> user = adminRepository.getAdminByUser(videoBuffh.getUser());
                    priceorder = ((videoBuffh.getVieworder() - video.get(0).getVieworder())) * (video.get(0).getPrice() / video.get(0).getVieworder());

                    if (priceorder > (float) user.get(0).getBalance()) {
                        resp.put("message", "Số tiền không đủ!!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    int timethan = videoBuffh.getVieworder() - video.get(0).getVieworder();

                    //
                    if (timethan != 0) {
                        Float balance_update=adminRepository.updateBalanceFine(-priceorder,videoBuffh.getUser());
                        Balance balance = new Balance();
                        balance.setUser(videoBuffh.getUser());
                        balance.setTime(System.currentTimeMillis());
                        balance.setTotalblance(balance_update);
                        balance.setBalance(-priceorder);
                        balance.setService(videoBuffh.getService());
                        if (priceorder < 0) {
                            balance.setNote("Refund " + (-timethan) + " view cho " + videoBuffh.getVideoid());
                        } else if (timethan != 0) {
                            balance.setNote("Order thêm " + timethan + " view cho " + videoBuffh.getVideoid());
                        }

                        balanceRepository.save(balance);
                    }
                }
                video.get(0).setMaxthreads(videoBuffh.getMaxthreads());
                if(video.get(0).getTimestart()==0){
                    video.get(0).setTimestart(System.currentTimeMillis());
                }
                video.get(0).setVieworder(videoBuffh.getVieworder());
                video.get(0).setNote(videoBuffh.getNote());
                video.get(0).setPrice(videoBuffh.getPrice() + priceorder);
                videoViewRepository.save(video.get(0));

                List<OrderViewRunning> orderRunnings = videoViewRepository.getVideoViewById(videoidIdArr[i].trim());
                JSONObject obj = new JSONObject();
                obj.put("orderid", orderRunnings.get(0).getOrderId());
                obj.put("videoid", orderRunnings.get(0).getVideoId());
                obj.put("videotitle", orderRunnings.get(0).getVideoTitle());
                obj.put("viewstart", orderRunnings.get(0).getViewStart());
                obj.put("maxthreads", orderRunnings.get(0).getMaxthreads());
                obj.put("insertdate", orderRunnings.get(0).getInsertDate());
                obj.put("timestart", orderRunnings.get(0).getTimeStart());
                obj.put("total", orderRunnings.get(0).getTotal());
                obj.put("note", orderRunnings.get(0).getNote());
                obj.put("duration", orderRunnings.get(0).getDuration());
                obj.put("vieworder", orderRunnings.get(0).getViewOrder());
                obj.put("service", orderRunnings.get(0).getService());
                obj.put("user", orderRunnings.get(0).getUser());
                obj.put("viewtotal", orderRunnings.get(0).getViewTotal());
                obj.put("view24h", orderRunnings.get(0).getView24h());
                obj.put("price", videoBuffh.getPrice() + priceorder);

                jsonArray.add(obj);
            }
            resp.put("videoview", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "updateRefundHis", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateRefundHis(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String orderid,@RequestParam(defaultValue = "1") Integer checkview) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            String[] videoidIdArr = orderid.split(",");
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < videoidIdArr.length; i++) {
                String status="No refunds";
                Integer viewcheck=-1;
                VideoViewHistory video = videoViewHistoryRepository.getVideoViewHisById(Long.parseLong(videoidIdArr[i].trim()));
                Service service = serviceRepository.getServiceNoCheckEnabled(video.getService());
                if((service.getChecktime()==0?(System.currentTimeMillis()- video.getEnddate())/1000/60/60>=8:true) && checkview==1 && (service.getChecktime()==0?(videoViewHistoryRepository.CheckOrderViewRefund(video.getOrderid())==1):true) && (service.getChecktime()==1?video.getViewend()>-1:true && video.getCancel()!=1) && (service.getChecktime()==1?(video.getTimecheckbh()>0?video.getViewend()<video.getVieworder()+video.getViewstart():true):true ) ){
                    System.out.println("Check");
                    OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
                    Request request1 = null;
                    List<GoogleAPIKey> keys = googleAPIKeyRepository.getAllByState();
                    request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=" + keys.get(0).getKey().trim() + "&fields=items(id,statistics(viewCount))&part=statistics&id=" + video.getVideoid()).get().build();
                    keys.get(0).setCount(keys.get(0).getCount() + 1L);
                    googleAPIKeyRepository.save(keys.get(0));
                    Response response1 = client1.newCall(request1).execute();
                    String resultJson1 = response1.body().string();
                    Object obj1 = new JSONParser().parse(resultJson1);
                    JSONObject jsonObject1 = (JSONObject) obj1;
                    JSONArray items = (JSONArray) jsonObject1.get("items");
                    Iterator k = items.iterator();
                    if (items != null || k.hasNext() != false) {
                        try {
                            JSONObject videocheck = (JSONObject) k.next();
                            JSONObject obj = new JSONObject();
                            JSONObject statistics = (JSONObject) videocheck.get("statistics");
                            viewcheck=Integer.parseInt(statistics.get("viewCount").toString());
                        } catch (Exception e) {
                        }
                    }
                }
                if(((viewcheck!=-1 && viewcheck<video.getVieworder()+video.getViewstart())) && ((service.getChecktime()==1?video.getViewend()>-1:true) && video.getCancel()!=1) ){
                    status="Refunded";
                    float price_refund=video.getPrice();
                    video.setViewtotal(0);
                    video.setCancel(1);
                    video.setPrice(0F);
                    if(viewcheck!=-1){
                        video.setViewend(viewcheck);
                    }
                    video.setTimecheckbh(System.currentTimeMillis());
                    videoViewHistoryRepository.save(video);
                    List<Admin> user = adminRepository.getAdminByUser(video.getUser());
                    //
                    Float balance_update=adminRepository.updateBalanceFine(price_refund,video.getUser().trim());
                    Balance balance = new Balance();
                    balance.setUser(user.get(0).getUsername().trim());
                    balance.setTime(System.currentTimeMillis());
                    balance.setTotalblance(balance_update);
                    balance.setBalance(price_refund);
                    balance.setService(video.getService());
                    balance.setNote("Refund " + (video.getVieworder()) + " view cho " + video.getVideoid());
                    balanceRepository.save(balance);
                }else if(service.getChecktime()==1 && video.getTimecheckbh()==0 && video.getViewend()>-1 && video.getCancel()!=1 && viewcheck>=0){
                    video.setViewend(viewcheck);
                    videoViewHistoryRepository.save(video);
                }else if(service.getChecktime()==0 && viewcheck>=0){
                    video.setViewend(viewcheck);
                    video.setTimecheckbh(System.currentTimeMillis());
                    videoViewHistoryRepository.save(video);
                }else if(service.getChecktime()==1 && video.getTimecheckbh()>0 && (video.getViewend()<video.getVieworder()+video.getViewstart()) && video.getCancel()!=1 && viewcheck>=0){
                    video.setViewend(viewcheck);
                    video.setTimecheckbh(System.currentTimeMillis());
                    videoViewHistoryRepository.save(video);
                }
                String infoQ =videoViewHistoryRepository.getInfoSumOrderByVideoId(video.getVideoid(),video.getOrderid());
                JSONObject obj = new JSONObject();
                if(infoQ!=null){
                    obj.put("info", infoQ);
                }else{
                    obj.put("info", "");
                }
                obj.put("orderid", video.getOrderid());
                obj.put("videoid", video.getVideoid());
                obj.put("videotitle", video.getVideotitle());
                obj.put("viewstart", video.getViewstart());
                obj.put("maxthreads", video.getMaxthreads());
                obj.put("insertdate", video.getInsertdate());
                obj.put("user", video.getUser());
                obj.put("note", video.getNote());
                obj.put("duration", video.getDuration());
                obj.put("enddate", video.getEnddate());
                obj.put("cancel", video.getCancel());
                obj.put("timestart",video.getTimestart());
                obj.put("timecheckbh", video.getTimecheckbh());
                obj.put("viewend", video.getViewend());
                obj.put("viewtotal", video.getViewtotal());
                obj.put("vieworder", video.getVieworder());
                obj.put("price", video.getPrice());
                obj.put("service", video.getService());
                obj.put("status", status);

                jsonArray.add(obj);
            }
            resp.put("videoview", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "updateBHHis", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateBHHis(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String orderid,@RequestParam(defaultValue = "1") Integer checkview) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            String[] videoidIdArr = orderid.split(",");
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < videoidIdArr.length; i++) {
                String status="No refill";
                Integer viewcheck=-1;
                VideoViewHistory video = videoViewHistoryRepository.getVideoViewHisById(Long.parseLong(videoidIdArr[i].trim()));
                Service service = serviceRepository.getServiceNoCheckEnabled(video.getService());
                System.out.println("Oke: "+videoViewHistoryRepository.CheckOrderViewRefund(video.getOrderid()));
                if(videoViewRepository.getCountVideoId(video.getVideoid().trim()) > 0 && (System.currentTimeMillis()- video.getEnddate())/1000/60/60>24 && checkview==1 && (service.getChecktime()==0?(videoViewHistoryRepository.CheckOrderViewRefund(video.getOrderid())==1):true) && (service.getChecktime()==1?video.getViewend()>-1:true && video.getCancel()!=1) && (service.getChecktime()==1?(video.getTimecheckbh()>0?video.getViewend()<video.getVieworder()+video.getViewstart():true):true ) ){
                    OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
                    Request request1 = null;
                    List<GoogleAPIKey> keys = googleAPIKeyRepository.getAllByState();
                    request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=" + keys.get(0).getKey().trim() + "&fields=items(id,statistics(viewCount))&part=statistics&id=" + video.getVideoid()).get().build();
                    keys.get(0).setCount(keys.get(0).getCount() + 1L);
                    googleAPIKeyRepository.save(keys.get(0));
                    Response response1 = client1.newCall(request1).execute();
                    String resultJson1 = response1.body().string();
                    Object obj1 = new JSONParser().parse(resultJson1);
                    JSONObject jsonObject1 = (JSONObject) obj1;
                    JSONArray items = (JSONArray) jsonObject1.get("items");
                    Iterator k = items.iterator();
                    if (items != null || k.hasNext() != false) {
                        try {
                            JSONObject videocheck = (JSONObject) k.next();
                            JSONObject obj = new JSONObject();
                            JSONObject statistics = (JSONObject) videocheck.get("statistics");
                            viewcheck=Integer.parseInt(statistics.get("viewCount").toString());
                        } catch (Exception e) {
                        }
                    }
                }
                if(((viewcheck!=-1 && viewcheck<video.getVieworder()+video.getViewstart()) || (service.getChecktime()==1?(video.getViewend()<video.getVieworder()+video.getViewstart()):false) || checkview==0) && ((service.getChecktime()==1?video.getViewend()>-1:true) && video.getCancel()!=1 && (System.currentTimeMillis()- video.getEnddate())/1000/60/60>24 && videoViewRepository.getCountVideoId(video.getVideoid().trim()) > 0) ){
                    status="Refilled";
                    float price_refund=video.getPrice();
                    video.setViewtotal(0);
                    video.setCancel(1);
                    video.setPrice(0F);
                    if(viewcheck!=-1){
                        video.setViewend(viewcheck);
                    }
                    video.setTimecheckbh(System.currentTimeMillis());
                    videoViewHistoryRepository.save(video);
                    List<Admin> user = adminRepository.getAdminByUser(video.getUser());
                    //
                    Float balance_update=adminRepository.updateBalanceFine(price_refund,video.getUser().trim());
                    Balance balance = new Balance();
                    balance.setUser(user.get(0).getUsername().trim());
                    balance.setTime(System.currentTimeMillis());
                    balance.setTotalblance(balance_update);
                    balance.setBalance(price_refund);
                    balance.setService(video.getService());
                    balance.setNote("Refund " + (video.getVieworder()) + " view cho " + video.getVideoid());
                    balanceRepository.save(balance);
                }else if(service.getChecktime()==1 && video.getTimecheckbh()==0 && video.getViewend()>-1 && video.getCancel()!=1 && viewcheck>=0){
                    video.setViewend(viewcheck);
                    videoViewHistoryRepository.save(video);
                }else if(service.getChecktime()==0 && viewcheck>=0){
                    video.setViewend(viewcheck);
                    video.setTimecheckbh(System.currentTimeMillis());
                    videoViewHistoryRepository.save(video);
                }
                String infoQ =videoViewHistoryRepository.getInfoSumOrderByVideoId(video.getVideoid(),video.getOrderid());
                JSONObject obj = new JSONObject();
                if(infoQ!=null){
                    obj.put("info", infoQ);
                }else{
                    obj.put("info", "");
                }
                obj.put("orderid", video.getOrderid());
                obj.put("videoid", video.getVideoid());
                obj.put("videotitle", video.getVideotitle());
                obj.put("viewstart", video.getViewstart());
                obj.put("maxthreads", video.getMaxthreads());
                obj.put("insertdate", video.getInsertdate());
                obj.put("user", video.getUser());
                obj.put("note", video.getNote());
                obj.put("duration", video.getDuration());
                obj.put("enddate", video.getEnddate());
                obj.put("cancel", video.getCancel());
                obj.put("timestart",video.getTimestart());
                obj.put("timecheckbh", video.getTimecheckbh());
                obj.put("viewend", video.getViewend());
                obj.put("viewtotal", video.getViewtotal());
                obj.put("vieworder", video.getVieworder());
                obj.put("price", video.getPrice());
                obj.put("service", video.getService());
                obj.put("status", status);

                jsonArray.add(obj);
            }
            resp.put("videoview", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "updateRefillHis", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateRefillHis(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String orderid) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            String[] videoidIdArr = orderid.split(",");
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < videoidIdArr.length; i++) {
                String status="No refunds";
                VideoViewHistory video = videoViewHistoryRepository.getVideoViewHisById(Long.parseLong(videoidIdArr[i].trim()));
                Float price_old=video.getPrice();
                Service service = serviceRepository.getInfoService(video.getService());
                VideoViewHistory video_refil;
                if(service.getChecktime()==1 || (service.getChecktime()==0&&videoViewHistoryRepository.CheckOrderViewRefund(video.getOrderid())==0)){
                    video_refil=video;
                }else{
                    htviewfindorder(video.getOrderid(),"1");
                    video_refil= videoViewHistoryRepository.getVideoViewHisById(Long.parseLong(videoidIdArr[i].trim()));
                    if(price_old!=video_refil.getPrice()){
                        status="Refunded";
                    }
                }
                JSONObject obj = new JSONObject();
                String infoQ =videoViewHistoryRepository.getInfoSumOrderByVideoId(video_refil.getVideoid(),video_refil.getOrderid());
                if(infoQ!=null){
                    obj.put("info", infoQ);
                }else{
                    obj.put("info", "");
                }
                obj.put("orderid", video_refil.getOrderid());
                obj.put("videoid", video_refil.getVideoid());
                obj.put("videotitle", video_refil.getVideotitle());
                obj.put("viewstart", video_refil.getViewstart());
                obj.put("maxthreads", video_refil.getMaxthreads());
                obj.put("insertdate", video_refil.getInsertdate());
                obj.put("user", video_refil.getUser());
                obj.put("note", video_refil.getNote());
                obj.put("duration", video_refil.getDuration());
                obj.put("enddate", video_refil.getEnddate());
                obj.put("cancel", video_refil.getCancel());
                obj.put("timestart",video_refil.getTimestart());
                obj.put("timecheckbh", video_refil.getTimecheckbh());
                obj.put("viewend", video_refil.getViewend());
                obj.put("viewtotal", video_refil.getViewtotal());
                obj.put("vieworder", video_refil.getVieworder());
                obj.put("price", video_refil.getPrice());
                obj.put("service", video_refil.getService());
                obj.put("status", status);

                jsonArray.add(obj);
            }
            resp.put("videoview", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(path = "updatethread", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updatethread(@RequestHeader(defaultValue = "") String Authorization, @RequestBody VideoView videoBuffh) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            String[] videoidIdArr = videoBuffh.getVideoid().split("\n");
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < videoidIdArr.length; i++) {
                List<VideoView> video = videoViewRepository.getVideoBuffhById(videoidIdArr[i].trim());
                float priceorder = 0;
                video.get(0).setMaxthreads(videoBuffh.getMaxthreads());
                if(video.get(0).getTimestart()==0){
                    video.get(0).setTimestart(System.currentTimeMillis());
                }
                videoViewRepository.save(video.get(0));

                List<OrderViewRunning> orderRunnings = videoViewRepository.getVideoViewById(videoidIdArr[i].trim());
                JSONObject obj = new JSONObject();
                obj.put("orderid", orderRunnings.get(0).getOrderId());
                obj.put("videoid", orderRunnings.get(0).getVideoId());
                obj.put("videotitle", orderRunnings.get(0).getVideoTitle());
                obj.put("viewstart", orderRunnings.get(0).getViewStart());
                obj.put("maxthreads", videoBuffh.getMaxthreads());
                obj.put("insertdate", orderRunnings.get(0).getInsertDate());
                obj.put("timestart", orderRunnings.get(0).getTimeStart());
                obj.put("t", orderRunnings.get(0).getInsertDate());
                obj.put("total", orderRunnings.get(0).getTotal());
                obj.put("note", orderRunnings.get(0).getNote());
                obj.put("duration", orderRunnings.get(0).getDuration());
                obj.put("service", orderRunnings.get(0).getService());
                obj.put("user", orderRunnings.get(0).getUser());
                obj.put("viewtotal", orderRunnings.get(0).getViewTotal());
                obj.put("view24h", orderRunnings.get(0).getView24h());
                obj.put("price", orderRunnings.get(0).getPrice());
                obj.put("vieworder", orderRunnings.get(0).getViewOrder());


                jsonArray.add(obj);
            }
            resp.put("videoview", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(path = "updatepriority", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updatepriority(@RequestHeader(defaultValue = "") String Authorization, @RequestBody VideoView videoBuffh) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            String[] videoidIdArr = videoBuffh.getVideoid().split("\n");
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < videoidIdArr.length; i++) {
                List<VideoView> video = videoViewRepository.getVideoBuffhById(videoidIdArr[i].trim());
                video.get(0).setPriority(videoBuffh.getPriority());
                videoViewRepository.save(video.get(0));

                List<OrderViewRunning> orderRunnings = videoViewRepository.getVideoViewById(videoidIdArr[i].trim());
                JSONObject obj = new JSONObject();
                obj.put("orderid", orderRunnings.get(0).getOrderId());
                obj.put("videoid", orderRunnings.get(0).getVideoId());
                obj.put("videotitle", orderRunnings.get(0).getVideoTitle());
                obj.put("viewstart", orderRunnings.get(0).getViewStart());
                obj.put("maxthreads", videoBuffh.getMaxthreads());
                obj.put("insertdate", orderRunnings.get(0).getInsertDate());
                obj.put("timestart", orderRunnings.get(0).getTimeStart());
                obj.put("total", orderRunnings.get(0).getTotal());
                obj.put("note", orderRunnings.get(0).getNote());
                obj.put("duration", orderRunnings.get(0).getDuration());
                obj.put("service", orderRunnings.get(0).getService());
                obj.put("user", orderRunnings.get(0).getUser());
                obj.put("viewtotal", orderRunnings.get(0).getViewTotal());
                obj.put("view24h", orderRunnings.get(0).getView24h());
                obj.put("price", orderRunnings.get(0).getPrice());
                obj.put("vieworder", orderRunnings.get(0).getViewOrder());
                obj.put("priority", orderRunnings.get(0).getPriority());


                jsonArray.add(obj);
            }
            resp.put("videoview", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "updatethreadpending", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updatethreadpending(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String videoid) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        Setting setting = settingRepository.getReferenceById(1L);
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            String[] videoidIdArr = videoid.split(",");
            JSONArray jsonArray = new JSONArray();
            for (int i = videoidIdArr.length-1;i >=0; i--) {
                List<VideoView> video = videoViewRepository.getVideoBuffhById(videoidIdArr[i].trim());
                Service service = serviceRepository.getInfoService(video.get(0).getService());
                if(videoViewRepository.getCountOrderRunningByService(video.get(0).getService())==null?false:videoViewRepository.getCountOrderRunningByService(video.get(0).getService())>=setting.getMaxorder()*service.getMax()){
                    break;
                }
                video.get(0).setMaxthreads((int)(video.get(0).getThreadset()*0.05));
                video.get(0).setTimestart(System.currentTimeMillis());
                videoViewRepository.save(video.get(0));

                List<OrderViewRunning> orderRunnings = videoViewRepository.getVideoViewById(videoidIdArr[i].trim());
                JSONObject obj = new JSONObject();
                obj.put("orderid", orderRunnings.get(0).getOrderId());
                obj.put("videoid", orderRunnings.get(0).getVideoId());
                obj.put("videotitle", orderRunnings.get(0).getVideoTitle());
                obj.put("viewstart", orderRunnings.get(0).getViewStart());
                obj.put("maxthreads", orderRunnings.get(0).getMaxthreads());
                obj.put("insertdate", orderRunnings.get(0).getInsertDate());
                obj.put("timestart", orderRunnings.get(0).getTimeStart());
                obj.put("total", orderRunnings.get(0).getTotal());
                obj.put("note", orderRunnings.get(0).getNote());
                obj.put("duration", orderRunnings.get(0).getDuration());
                obj.put("service", orderRunnings.get(0).getService());
                obj.put("user", orderRunnings.get(0).getUser());
                obj.put("viewtotal", orderRunnings.get(0).getViewTotal());
                obj.put("view24h", orderRunnings.get(0).getView24h());
                obj.put("price", orderRunnings.get(0).getPrice());
                obj.put("vieworder", orderRunnings.get(0).getViewOrder());
                obj.put("priority", orderRunnings.get(0).getPriority());


                jsonArray.add(obj);
            }
            resp.put("videoview", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
}

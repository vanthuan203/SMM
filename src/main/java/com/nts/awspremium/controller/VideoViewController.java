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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
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

    @PostMapping(value = "/orderview", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> orderview(@RequestBody VideoView videoView, @RequestHeader(defaultValue = "") String Authorization) throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        System.out.println(videoView.getService());
        try {
            List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
            if (Authorization.length() == 0 || admins.size() == 0) {
                resp.put("status", "fail");
                resp.put("message", "Token expired");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            Service service = serviceRepository.getService(videoView.getService());
            if (service == null) {
                resp.put("videoview", "Service not found ");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            if (videoView.getVieworder() > service.getMax() || videoView.getVieworder() < service.getMin()) {
                resp.put("error", "Min/Max order is: " + service.getMin() + "/" + service.getMax());
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            if (videoViewRepository.getCountOrderByUser(admins.get(0).getUsername().trim()) >= admins.get(0).getMaxorder() || (service.getGeo().equals("vn") && settingRepository.getMaxOrderVN() == 0) ||
                    (service.getGeo().equals("us") && settingRepository.getMaxOrderUS() == 0)) {
                resp.put("videoview", "System busy try again!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            String videolist = GoogleApi.getYoutubeId(videoView.getVideoid());
            //VIDEOOOOOOOOOOOOOOO
            int count = StringUtils.countOccurrencesOf(videolist, ",") + 1;
            OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

            Request request1 = null;

            request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=AIzaSyClOKa8qUz3MJD1RKBsjlIDR5KstE2NmMY&fields=items(id,snippet(title,channelId,liveBroadcastContent),statistics(viewCount),contentDetails(duration))&part=snippet,statistics,contentDetails&id=" + videolist).get().build();

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
            while (k.hasNext()) {
                try {
                    JSONObject video = (JSONObject) k.next();
                    JSONObject contentDetails = (JSONObject) video.get("contentDetails");
                    if (videoViewRepository.getCountVideoId(video.get("id").toString().trim()) > 0) {
                        resp.put("videoview", "This video in process!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 3600 && videoView.getService() == 999) {
                        resp.put("error", "video under 60 minutes");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 1800 && videoView.getService() == 998) {
                        resp.put("error", "video under 30 minutes");
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
                    if (!snippet.get("liveBroadcastContent").toString().equals("none")) {
                        resp.put("error", "This video is not a pure public video");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    JSONObject statistics = (JSONObject) video.get("statistics");
                    VideoView videoViewhnew = new VideoView();
                    videoViewhnew.setDuration(Duration.parse(contentDetails.get("duration").toString()).getSeconds());
                    videoViewhnew.setInsertdate(System.currentTimeMillis());
                    videoViewhnew.setView24h(0);
                    videoViewhnew.setViewtotal(0);
                    videoViewhnew.setVieworder(videoView.getVieworder());
                    videoViewhnew.setUser(admins.get(0).getUsername());
                    videoViewhnew.setChannelid(snippet.get("channelId").toString());
                    videoViewhnew.setVideotitle(snippet.get("title").toString());
                    videoViewhnew.setVideoid(video.get("id").toString());
                    videoViewhnew.setViewstart(Integer.parseInt(statistics.get("viewCount").toString()));
                    videoViewhnew.setMaxthreads(videoView.getMaxthreads());
                    videoViewhnew.setPrice(priceorder);
                    videoViewhnew.setNote(videoView.getNote());
                    videoViewhnew.setService(videoView.getService());
                    videoViewhnew.setValid(1);
                    videoViewRepository.save(videoViewhnew);

                    float balance_new = admins.get(0).getBalance() - priceorder;
                    adminRepository.updateBalance(balance_new, admins.get(0).getUsername());
                    Balance balance = new Balance();
                    balance.setUser(admins.get(0).getUsername().trim());
                    balance.setTime(System.currentTimeMillis());
                    balance.setTotalblance(balance_new);
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
                    resp.put("videoview", "error");
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
        System.out.println(listvideo);
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
        List<String> listvideo = videoViewHistoryRepository.getOrderHistorythan5h();
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

        request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=AIzaSyClOKa8qUz3MJD1RKBsjlIDR5KstE2NmMY&fields=items(id,statistics(viewCount))&part=statistics&id=" + s_videoid).get().build();

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
                videoViewHistoryRepository.updateviewend(Integer.parseInt(statistics.get("viewCount").toString()), video.get("id").toString());
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
            System.out.println(s_videoid);
        }
        //VIDEOOOOOOOOOOOOOOO
        OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        Request request1 = null;

        request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=AIzaSyClOKa8qUz3MJD1RKBsjlIDR5KstE2NmMY&fields=items(id,snippet(liveBroadcastContent),statistics(viewCount))&part=snippet,statistics&id=" + s_videoid).get().build();

        Response response1 = client1.newCall(request1).execute();

        String resultJson1 = response1.body().string();

        Object obj1 = new JSONParser().parse(resultJson1);

        JSONObject jsonObject1 = (JSONObject) obj1;
        JSONArray items = (JSONArray) jsonObject1.get("items");
        JSONArray jsonArray = new JSONArray();
        Iterator k = items.iterator();
        Setting setting = settingRepository.getReferenceById(1L);
        while (k.hasNext()) {
            try {
                JSONObject video = (JSONObject) k.next();
                JSONObject obj = new JSONObject();
                JSONObject statistics = (JSONObject) video.get("statistics");
                JSONObject snippet = (JSONObject) video.get("snippet");
                System.out.println(video.get("id").toString());
                System.out.println(snippet.get("liveBroadcastContent").toString());
                if (snippet.get("liveBroadcastContent").toString().equals("none")) {
                    VideoView videoView =videoViewRepository.getVideoViewByVideoid(video.get("id").toString());
                    Service service = serviceRepository.getService(videoView.getService());
                    int max_thread = service.getThread() + ((int)(videoView.getVieworder() / 1000) - 1) * setting.getLevelthread();
                    if (max_thread > setting.getMaxthread()) {
                        max_thread=setting.getMaxthread();
                    }
                    videoViewRepository.updatePendingOrderByVideoId(Integer.parseInt(statistics.get("viewCount").toString()),max_thread,System.currentTimeMillis(), video.get("id").toString());
                }
            } catch (Exception e) {
                resp.put("status", e);
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }

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
            List<String> viewBuff;
            List<String> viewBuff24h;
            List<VideoView> videoViewList = videoViewRepository.getAllOrder();
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


    @PostMapping(path = "bhview", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> bhview(@RequestBody() VideoViewHistory videoid, @RequestHeader(defaultValue = "") String Authorization) {
        String[] key={"AIzaSyBDREST_tkBJtwLwP7R_AzhMufMcMQ0W8U",
                "AIzaSyBz0U1oenDUX3Grm8Fi15YzXuKnQwApPoU",
                "AIzaSyD3rM4Ti-mOr51d50sTZ3j2Zk_FXJBi-Pg",
                "AIzaSyDMNegTFfuEpXFjh6SJr6uIkG9rtgsscAg",
                "AIzaSyBZYdBJN5nG-oXM_otg7X3cc3Pil2HZJpk",
                "AIzaSyAdz8OjRNSbhYLTuGtJpl7EHIfXf2AM_zw",
                "AIzaSyBCIiJFQWchkroL9EUWx4WC1toHZUN6e-I",
                "AIzaSyBJHNbAzs53GFqcGAIWctCeJ9rcDMYbEsw",
                "AIzaSyCtIX1X_38DRcT8UODtk9SaDi7PBAkyNR4",
                "AIzaSyCJhgx-pGa7fRLzLF7LlH_P9aF6Mn5hjks",
                "AIzaSyBxoTek2x9iUPAXJTlhSM42MAOGvak1dfg",
                "AIzaSyArs1DU8lr2ZZOHwHNm9r4fQCJgNk0OpU8",
                "AIzaSyB9xxqdqaI6VfZzy0xWfAjbYz_h3r6i6LI",
                "AIzaSyARRNkpFjJwi8h6D7DSPDqzU5dN9KYJL7c",
                "AIzaSyAWqilV12eEQ3aRCFh7boVwnwT5vGd-fkA",
                "AIzaSyBnFcVBXsi5XEjexd6y2qz1DaSU1OyN7O0",
                "AIzaSyCOYZxFucHqJdKUlREppWv2x51vWBr01hQ",
                "AIzaSyAtHO7oWPc_qfpBDGrhKWdXBe8t6W1PfyI",
                "AIzaSyBoNOzrLwmcGegL__Pcawc_HCMO86Zr3DY",
                "AIzaSyD3qlV4yE9gGzB8lPQ0oSCJeIOJNVC__W8",
                "AIzaSyCJP63LRZf84rvQEhjl3TbW8ZLr9CESwOQ",
                "AIzaSyCjRcccCp6JmqHUeGyHS69d6p6EgY-qk4s",
                "AIzaSyDazh82pL59xL5WrEJp5ACcQm4NJvWSsBk",
                "AIzaSyAhBaXXfGooDevibtZHuD0s98uHmy2uR_Y",
                "AIzaSyD5mCaCbu4FtDiVyqInA_FBThkZ5ziuTI0",
                "AIzaSyCX4l1yvTaSmt1wxgmzcmrvgfzudn8oYdk",
                "AIzaSyDdSn1dGnR03AqjQOX4pN0lPuU2pezrpDM",
                "AIzaSyCBmm0tWYu25LDacVcwWsKBlf7AXhy7F7M"};
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
                    resp.put("videoview", "Đã hoàn trước đó!");
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

                if (System.currentTimeMillis() - videoViewHistories.get(i).getEnddate() < 1000 * 3600 * 24) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistories.get(i).setWaitbh(1);
                    videoViewHistories.get(i).setTimecheckbh(videoViewHistories.get(i).getEnddate()+(24 * 60 * 60 * 1000));
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                    obj.put("videoview", "Đã lên lịch check BH lúc"+dateFormat.format((new Date(videoViewHistories.get(i).getEnddate()+(24 * 60 * 60 * 1000)))));
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                List<VideoViewHistory> viewHistories =videoViewHistoryRepository.getTimeBHByVideoId(videoViewHistories.get(i).getVideoid().trim());
                if (viewHistories.size()>0) {
                    if(System.currentTimeMillis()-viewHistories.get(0).getEnddate()< 1000 * 3600 * 24){
                        videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                        videoViewHistoryRepository.save(videoViewHistories.get(i));
                        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                        obj.put("videoview", "Bảo hành chưa quá 24h! | " +dateFormat.format(new Date(viewHistories.get(0).getEnddate())));
                        return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                    }
                }

                OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                Request request1 = null;
                Random rand = new Random();
                request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key="+key[rand.nextInt(key.length)]+"&fields=items(statistics(viewCount))&part=statistics&id=" + videoViewHistories.get(i).getVideoid().trim()).get().build();

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
                            if (Integer.parseInt(statistics.get("viewCount").toString()) - (int) videoViewHistories.get(i).getViewstart() > 0) {
                                int baohanh = 0;
                                baohanh = (int) ((int) (videoViewHistories.get(i).getViewstart() + videoViewHistories.get(i).getVieworder() - Integer.parseInt(statistics.get("viewCount").toString())));
                                if (baohanh < 50) {
                                    baohanh = 50;
                                } else if (baohanh > videoViewHistories.get(i).getVieworder()) {
                                    baohanh = videoViewHistories.get(i).getVieworder();
                                }
                                float priceorder = 0;
                                Service service = serviceRepository.getService(videoViewHistories.get(i).getService());
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

                                float balance_new = admins.get(0).getBalance() - priceorder;
                                System.out.println(balance_new);
                                adminRepository.updateBalance(balance_new, admins.get(0).getUsername());
                                Balance balance = new Balance();
                                balance.setUser(admins.get(0).getUsername().trim());
                                balance.setTime(System.currentTimeMillis());
                                balance.setTotalblance(balance_new);
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
                                obj.put("videoview", "View check < view start! (Chọn hoàn tiền)");
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
        String[] key={"AIzaSyBDREST_tkBJtwLwP7R_AzhMufMcMQ0W8U",
                "AIzaSyBz0U1oenDUX3Grm8Fi15YzXuKnQwApPoU",
                "AIzaSyD3rM4Ti-mOr51d50sTZ3j2Zk_FXJBi-Pg",
                "AIzaSyDMNegTFfuEpXFjh6SJr6uIkG9rtgsscAg",
                "AIzaSyBZYdBJN5nG-oXM_otg7X3cc3Pil2HZJpk",
                "AIzaSyAdz8OjRNSbhYLTuGtJpl7EHIfXf2AM_zw",
                "AIzaSyBCIiJFQWchkroL9EUWx4WC1toHZUN6e-I",
                "AIzaSyBJHNbAzs53GFqcGAIWctCeJ9rcDMYbEsw",
                "AIzaSyCtIX1X_38DRcT8UODtk9SaDi7PBAkyNR4",
                "AIzaSyCJhgx-pGa7fRLzLF7LlH_P9aF6Mn5hjks",
                "AIzaSyBxoTek2x9iUPAXJTlhSM42MAOGvak1dfg",
                "AIzaSyArs1DU8lr2ZZOHwHNm9r4fQCJgNk0OpU8",
                "AIzaSyB9xxqdqaI6VfZzy0xWfAjbYz_h3r6i6LI",
                "AIzaSyARRNkpFjJwi8h6D7DSPDqzU5dN9KYJL7c",
                "AIzaSyAWqilV12eEQ3aRCFh7boVwnwT5vGd-fkA",
                "AIzaSyBnFcVBXsi5XEjexd6y2qz1DaSU1OyN7O0",
                "AIzaSyCOYZxFucHqJdKUlREppWv2x51vWBr01hQ",
                "AIzaSyAtHO7oWPc_qfpBDGrhKWdXBe8t6W1PfyI",
                "AIzaSyBoNOzrLwmcGegL__Pcawc_HCMO86Zr3DY",
                "AIzaSyD3qlV4yE9gGzB8lPQ0oSCJeIOJNVC__W8",
                "AIzaSyCJP63LRZf84rvQEhjl3TbW8ZLr9CESwOQ",
                "AIzaSyCjRcccCp6JmqHUeGyHS69d6p6EgY-qk4s",
                "AIzaSyDazh82pL59xL5WrEJp5ACcQm4NJvWSsBk",
                "AIzaSyAhBaXXfGooDevibtZHuD0s98uHmy2uR_Y",
                "AIzaSyD5mCaCbu4FtDiVyqInA_FBThkZ5ziuTI0",
                "AIzaSyCX4l1yvTaSmt1wxgmzcmrvgfzudn8oYdk",
                "AIzaSyDdSn1dGnR03AqjQOX4pN0lPuU2pezrpDM",
                "AIzaSyCBmm0tWYu25LDacVcwWsKBlf7AXhy7F7M"};
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            List<VideoViewHistory> videoViewHistories = videoViewHistoryRepository.getVideoCheckBH(start*24, end*24, limit);
            JSONArray jsonArray = new JSONArray();
            Setting setting = settingRepository.getReferenceById(1L);
            List<Admin> admins = adminRepository.GetAdminByUser("baohanh01@gmail.com");
            videoViewHistoryRepository.updatetimchecknomaxid();
            for (int i = 0; i < videoViewHistories.size(); i++) {
                DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                String end_done="End: "+dateFormat.format(videoViewHistories.get(i).getEnddate()) + " | ";
                JSONObject obj = new JSONObject();
                if (videoViewRepository.getCountVideoId(videoViewHistories.get(i).getVideoid().trim()) > 0) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put(videoViewHistories.get(i).getVideoid().trim(), "Đơn đang chạy!");
                    jsonArray.add(obj);
                    continue;
                }
                List<VideoViewHistory> viewHistories =videoViewHistoryRepository.getTimeBHByVideoId(videoViewHistories.get(i).getVideoid().trim());
                if (viewHistories.size()>0) {
                    if(System.currentTimeMillis()-viewHistories.get(0).getEnddate()< 1000 * 3600 * 24){
                        videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                        videoViewHistoryRepository.save(videoViewHistories.get(i));
                        obj.put(videoViewHistories.get(i).getVideoid().trim(), "Đơn đã được bảo hành chưa quá 24h!");
                        jsonArray.add(obj);
                        continue;
                    }
                }
                OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                Request request1 = null;
                Random rand =new Random();
                request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key="+key[rand.nextInt(key.length)]+"&fields=items(statistics(viewCount))&part=statistics&id=" + videoViewHistories.get(i).getVideoid().trim()).get().build();

                Response response1 = client1.newCall(request1).execute();

                String resultJson1 = response1.body().string();

                Object obj1 = new JSONParser().parse(resultJson1);

                JSONObject jsonObject1 = (JSONObject) obj1;
                JSONArray items = (JSONArray) jsonObject1.get("items");
                if (items == null) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put(videoViewHistories.get(i).getVideoid().trim(),end_done+ "Không check được view!");
                    jsonArray.add(obj);
                    continue;
                }
                Iterator k = items.iterator();
                if (k.hasNext() == false) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put(videoViewHistories.get(i).getVideoid().trim(),end_done+ "Không check được view!");
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
                                Service service = serviceRepository.getService(videoViewHistories.get(i).getService());
                                priceorder = (baohanh / 1000F) * service.getRate() * ((float) (admins.get(0).getRate()) / 100) * ((float) (100 - admins.get(0).getDiscount()) / 100);
                                if (priceorder > (float) admins.get(0).getBalance()) {
                                    obj.put(videoViewHistories.get(i).getVideoid().trim(),end_done+ "Số tiền không đủ!");
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

                                float balance_new = admins.get(0).getBalance() - priceorder;
                                System.out.println(balance_new);
                                adminRepository.updateBalance(balance_new, admins.get(0).getUsername());
                                Balance balance = new Balance();
                                balance.setUser(admins.get(0).getUsername().trim());
                                balance.setTime(System.currentTimeMillis());
                                balance.setTotalblance(balance_new);
                                balance.setBalance(-priceorder);
                                balance.setService(service.getService());
                                balance.setNote("Bảo hành " + baohanh + " view cho video " + videoViewHistories.get(i).getVideoid());
                                balanceRepository.save(balance);
                                obj.put(videoViewHistories.get(i).getVideoid().trim(),end_done+"Bảo hành:" + baohanh +"/"+videoViewHistories.get(i).getVieworder()+" | "+(int)(baohanh/(float)videoViewHistories.get(i).getVieworder()*100)+"%");
                                jsonArray.add(obj);
                                continue;
                            } else {
                                videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                                videoViewHistoryRepository.save(videoViewHistories.get(i));
                                obj.put(videoViewHistories.get(i).getVideoid().trim(),end_done+ "View check < view start!");
                                jsonArray.add(obj);
                                continue;
                            }
                        } else {
                            videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                            videoViewHistoryRepository.save(videoViewHistories.get(i));
                            obj.put(videoViewHistories.get(i).getVideoid().trim(), "Không cần bảo hành!");
                            jsonArray.add(obj);
                            continue;
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

            }
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
                if (orderid != videoViewHistories.get(0).getOrderid()) {
                    resp.put("videoview", "Không đủ ĐK hoàn tiền!(New orderId: "+videoViewHistories.get(0).getOrderid()+")");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            }

            for (int i = 0; i < videoViewHistories.size(); i++) {
                JSONObject obj = new JSONObject();
                if ((videoViewHistories.get(0).getRefund() == null ? 0 : videoViewHistories.get(0).getRefund()) == 1) {
                    resp.put("videoview", "Đã hoàn trước đó!");
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

                if (System.currentTimeMillis() - videoViewHistories.get(i).getEnddate() < 1000 * 3600 * 24) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Chưa đủ time check  hoàn tiền!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }

                OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                Request request1 = null;

                request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=AIzaSyClOKa8qUz3MJD1RKBsjlIDR5KstE2NmMY&fields=items(statistics(viewCount))&part=statistics&id=" + videoViewHistories.get(i).getVideoid().trim()).get().build();

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
                        Service service = serviceRepository.getService(videoViewHistories.get(i).getService());
                        List<Admin> user = adminRepository.getAdminByUser(videoViewHistories.get(i).getUser());
                        //Hoàn tiền những view chưa buff
                        int viewcount = Integer.parseInt(statistics.get("viewCount").toString());
                        int viewthan = videoViewHistories.get(i).getVieworder() + videoViewHistories.get(i).getViewstart() - viewcount;
                        if (viewthan > videoViewHistories.get(i).getVieworder()) {
                            viewthan = videoViewHistories.get(i).getVieworder();
                        }
                        float price_refund = ((viewthan) / (float) videoViewHistories.get(i).getVieworder()) * videoViewHistories.get(i).getPrice();
                        //float pricebuffed=(videoBuffh.get(0).getViewtotal()/1000F)*service.getRate()*((float)(100-admins.get(0).getDiscount())/100);
                        if (videoViewHistories.get(i).getPrice() < price_refund) {
                            price_refund = videoViewHistories.get(i).getPrice();
                        }
                        float pricebuffed = (videoViewHistories.get(i).getPrice() - price_refund);
                        videoViewHistories.get(i).setPrice(pricebuffed);
                        videoViewHistories.get(i).setViewend(viewcount);
                        videoViewHistories.get(i).setViewtotal(videoViewHistories.get(i).getVieworder()-viewthan);
                        videoViewHistories.get(i).setRefund(1);
                        if( price_refund == videoViewHistories.get(i).getPrice()){
                            videoViewHistories.get(i).setCancel(1);
                        }else{
                            videoViewHistories.get(i).setCancel(2);
                        }
                        videoViewHistoryRepository.save(videoViewHistories.get(i));
                        //hoàn tiền & add thong báo số dư
                        float balance_new = user.get(0).getBalance() + price_refund;
                        user.get(0).setBalance(balance_new);
                        adminRepository.save(user.get(0));
                        //
                        Balance balance = new Balance();
                        balance.setUser(user.get(0).getUsername().trim());
                        balance.setTime(System.currentTimeMillis());
                        balance.setTotalblance(balance_new);
                        balance.setBalance(price_refund);
                        balance.setService(videoViewHistories.get(i).getService());
                        balance.setNote("Hoàn " + (viewthan) + "view cho " + videoViewHistories.get(i).getVideoid());
                        balanceRepository.save(balance);

                        obj.put(videoViewHistories.get(i).getVideoid().trim(), "Hoàn  " + viewthan + " view!");
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


    @PostMapping(path = "checkbhview", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> checkbhview(@RequestBody() VideoViewHistory videoid, @RequestHeader(defaultValue = "") String Authorization) {

        JSONObject resp = new JSONObject();
        String[] key={"AIzaSyBDREST_tkBJtwLwP7R_AzhMufMcMQ0W8U",
                "AIzaSyBz0U1oenDUX3Grm8Fi15YzXuKnQwApPoU",
                "AIzaSyD3rM4Ti-mOr51d50sTZ3j2Zk_FXJBi-Pg",
                "AIzaSyDMNegTFfuEpXFjh6SJr6uIkG9rtgsscAg",
                "AIzaSyBZYdBJN5nG-oXM_otg7X3cc3Pil2HZJpk",
                "AIzaSyAdz8OjRNSbhYLTuGtJpl7EHIfXf2AM_zw",
                "AIzaSyBCIiJFQWchkroL9EUWx4WC1toHZUN6e-I",
                "AIzaSyBJHNbAzs53GFqcGAIWctCeJ9rcDMYbEsw",
                "AIzaSyCtIX1X_38DRcT8UODtk9SaDi7PBAkyNR4",
                "AIzaSyCJhgx-pGa7fRLzLF7LlH_P9aF6Mn5hjks",
                "AIzaSyBxoTek2x9iUPAXJTlhSM42MAOGvak1dfg",
                "AIzaSyArs1DU8lr2ZZOHwHNm9r4fQCJgNk0OpU8",
                "AIzaSyB9xxqdqaI6VfZzy0xWfAjbYz_h3r6i6LI",
                "AIzaSyARRNkpFjJwi8h6D7DSPDqzU5dN9KYJL7c",
                "AIzaSyAWqilV12eEQ3aRCFh7boVwnwT5vGd-fkA",
                "AIzaSyBnFcVBXsi5XEjexd6y2qz1DaSU1OyN7O0",
                "AIzaSyCOYZxFucHqJdKUlREppWv2x51vWBr01hQ",
                "AIzaSyAtHO7oWPc_qfpBDGrhKWdXBe8t6W1PfyI",
                "AIzaSyBoNOzrLwmcGegL__Pcawc_HCMO86Zr3DY",
                "AIzaSyD3qlV4yE9gGzB8lPQ0oSCJeIOJNVC__W8",
                "AIzaSyCJP63LRZf84rvQEhjl3TbW8ZLr9CESwOQ",
                "AIzaSyCjRcccCp6JmqHUeGyHS69d6p6EgY-qk4s",
                "AIzaSyDazh82pL59xL5WrEJp5ACcQm4NJvWSsBk",
                "AIzaSyAhBaXXfGooDevibtZHuD0s98uHmy2uR_Y",
                "AIzaSyD5mCaCbu4FtDiVyqInA_FBThkZ5ziuTI0",
                "AIzaSyCX4l1yvTaSmt1wxgmzcmrvgfzudn8oYdk",
                "AIzaSyDdSn1dGnR03AqjQOX4pN0lPuU2pezrpDM",
                "AIzaSyCBmm0tWYu25LDacVcwWsKBlf7AXhy7F7M"};
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
                    resp.put("videoview", "Không đủ ĐK bảo hành! (OrderId :"+videoViewHistories.get(0).getOrderid()+")");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }

                 */
            }
            for (int i = 0; i < videoViewHistories.size(); i++) {
                JSONObject obj = new JSONObject();
                if ((videoViewHistories.get(0).getRefund() == null ? 0 : videoViewHistories.get(0).getRefund()) == 1) {
                    resp.put("videoview", "Đã hoàn trước đó!");
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
                /*
                if (System.currentTimeMillis() - videoViewHistories.get(i).getEnddate() < 1000 * 3600 * 24) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Chưa đủ time bh!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }


                 */
                OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                Request request1 = null;
                Random rand =new Random();
                request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key="+key[rand.nextInt(key.length)]+"&fields=items(statistics(viewCount))&part=statistics&id=" + videoViewHistories.get(i).getVideoid().trim()).get().build();

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
                        baohanh = (int) ((int) (videoViewHistories.get(i).getViewstart() + videoViewHistories.get(i).getVieworder() - Integer.parseInt(statistics.get("viewCount").toString())));

                        obj.put(videoViewHistories.get(i).getVideoid().trim(), "Bảo hành " + baohanh + " view!");
                        obj.put("orderid", videoViewHistories.get(i).getOrderid());
                        obj.put("videoid", videoViewHistories.get(i).getVideoid());
                        obj.put("viewstart", videoViewHistories.get(i).getViewstart());
                        obj.put("videoview", "true");
                        obj.put("timestart", videoViewHistories.get(i).getInsertdate());
                        obj.put("timeend", videoViewHistories.get(i).getEnddate());
                        obj.put("vieworder", videoViewHistories.get(i).getVieworder());
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
                //obj.put("home_rate", orderRunnings.get(i).get());
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
            List<VideoViewHistory> orderRunnings = videoViewHistoryRepository.getVideoViewHistoriesByVideoId(videoid.trim());
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < orderRunnings.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("orderid", orderRunnings.get(i).getOrderid());
                obj.put("videoid", orderRunnings.get(i).getVideoid());
                obj.put("viewstart", orderRunnings.get(i).getViewstart());
                obj.put("insertdate", orderRunnings.get(i).getInsertdate());
                obj.put("user", orderRunnings.get(i).getUser());
                obj.put("note", orderRunnings.get(i).getNote());
                obj.put("enddate", orderRunnings.get(i).getEnddate());
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
            if (videoView.getService() == 669 || videoView.getService() == 688 || videoView.getService() == 689) {
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
                //videoBuffhnew.setPrice(videoBuffh.get(0).getPrice());
                if (cancel == 1) {
                    Service service = serviceRepository.getService(videoBuffh.get(0).getService());
                    List<Admin> user = adminRepository.getAdminByUser(videoBuffh.get(0).getUser());
                    //Hoàn tiền những view chưa buff
                    int viewbuff = videoBuffh.get(0).getViewtotal();
                    float price_refund = ((videoBuffh.get(0).getVieworder() - videoBuffh.get(0).getViewtotal()) / (float) videoBuffh.get(0).getVieworder()) * videoBuffh.get(0).getPrice();
                    //float pricebuffed=(videoBuffh.get(0).getViewtotal()/1000F)*service.getRate()*((float)(100-admins.get(0).getDiscount())/100);
                    float pricebuffed = (videoBuffh.get(0).getPrice() - price_refund);
                    videoBuffhnew.setPrice(pricebuffed);
                    if (viewbuff == 0) {
                        videoBuffhnew.setCancel(1);
                    } else {
                        videoBuffhnew.setCancel(2);
                    }
                    //hoàn tiền & add thong báo số dư
                    int viewthan = (int) (videoBuffh.get(0).getVieworder() - viewbuff);
                    float balance_new = user.get(0).getBalance() + price_refund;
                    user.get(0).setBalance(balance_new);
                    adminRepository.save(user.get(0));
                    //
                    Balance balance = new Balance();
                    balance.setUser(user.get(0).getUsername().trim());
                    balance.setTime(System.currentTimeMillis());
                    balance.setTotalblance(balance_new);
                    balance.setBalance(price_refund);
                    balance.setService(videoBuffh.get(0).getService());
                    balance.setNote("Hoàn " + (viewthan) + "view cho " + videoBuffh.get(0).getVideoid());
                    balanceRepository.save(balance);
                } else {
                    videoBuffhnew.setPrice(videoBuffh.get(0).getPrice());
                    videoBuffhnew.setCancel(0);
                }
                videoBuffhnew.setUser(videoBuffh.get(0).getUser());
                videoBuffhnew.setEnddate(enddate);
                videoBuffhnew.setViewtotal(videoBuffh.get(0).getViewtotal());
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
                videoBuffhnew.setUser(videoBuffh.get(i).getUser());
                videoBuffhnew.setEnddate(enddate);
                videoBuffhnew.setService(videoBuffh.get(i).getService());
                videoBuffhnew.setViewtotal(videoBuffh.get(i).getViewtotal());
                videoBuffhnew.setVieworder(videoBuffh.get(i).getVieworder());
                videoBuffhnew.setPrice(videoBuffh.get(i).getPrice());
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
            resp.put("status", "true" + videoid);
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
                    Service service = serviceRepository.getService(video.get(0).getService());
                    priceorder = ((videoBuffh.getVieworder() - video.get(0).getVieworder())) * (video.get(0).getPrice() / video.get(0).getVieworder());

                    if (priceorder > (float) admins.get(0).getBalance()) {
                        resp.put("message", "Số tiền không đủ!!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    int timethan = videoBuffh.getVieworder() - video.get(0).getVieworder();
                    float balance_new = admins.get(0).getBalance() - priceorder;
                    admins.get(0).setBalance(balance_new);
                    adminRepository.save(admins.get(0));


                    //
                    if (timethan != 0) {
                        Balance balance = new Balance();
                        balance.setUser(admins.get(0).getUsername().trim());
                        balance.setTime(System.currentTimeMillis());
                        balance.setTotalblance(balance_new);
                        balance.setBalance(-priceorder);
                        balance.setService(videoBuffh.getService());
                        if (priceorder < 0) {
                            balance.setNote("Hoàn " + (-timethan) + " view cho " + videoBuffh.getVideoid());
                        } else if (timethan != 0) {
                            balance.setNote("Order thêm " + timethan + " view cho " + videoBuffh.getVideoid());
                        }

                        balanceRepository.save(balance);
                    }
                }
                video.get(0).setMaxthreads(videoBuffh.getMaxthreads());
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
                videoViewRepository.save(video.get(0));

                List<OrderViewRunning> orderRunnings = videoViewRepository.getVideoViewById(videoidIdArr[i].trim());
                JSONObject obj = new JSONObject();
                obj.put("orderid", orderRunnings.get(0).getOrderId());
                obj.put("videoid", orderRunnings.get(0).getVideoId());
                obj.put("videotitle", orderRunnings.get(0).getVideoTitle());
                obj.put("viewstart", orderRunnings.get(0).getViewStart());
                obj.put("maxthreads", videoBuffh.getMaxthreads());
                obj.put("insertdate", orderRunnings.get(0).getInsertDate());
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
}

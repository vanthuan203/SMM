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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/videocomment")
public class VideoCommentController {

    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private DataCommentRepository dataCommentRepository;

    @Autowired
    private DataReplyCommentRepository dataReplyCommentRepository;
    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private VideoViewHistoryRepository videoViewHistoryRepository;
    @Autowired
    private VideoViewRepository videoViewRepository;

    @Autowired
    private VideoCommentRepository videoCommentRepository;

    @Autowired
    private VideoCommentHistoryRepository videoCommentHistoryRepository;
    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private DataOrderRepository dataOrderRepository;

    @Autowired
    private GoogleAPIKeyRepository googleAPIKeyRepository;

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
            if (videoView.getVieworder()>service.getMax() || videoView.getVieworder()<service.getMin()) {
                resp.put("error", "Min/Max order is: "+service.getMin()+"/"+service.getMax());
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            if (videoCommentRepository.getCountOrderByUser(admins.get(0).getUsername().trim()) >= admins.get(0).getMaxorder() || (service.getGeo().equals("vn") && settingRepository.getMaxOrderVN() == 0) ||
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
                    priceorder = (videoView.getVieworder() / 1000F) * service.getRate()* ((float) (admins.get(0).getRate())/ 100) * ((float) (100 - admins.get(0).getDiscount()) / 100);
                    if (priceorder > (float) admins.get(0).getBalance()) {
                        resp.put("videoview", "Your balance not enough");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    JSONObject snippet = (JSONObject) video.get("snippet");
                    if(!snippet.get("liveBroadcastContent").toString().equals("none")){
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
            List<OrderCommentRunning> orderRunnings;
            if (user.length() == 0) {
                orderRunnings = videoCommentRepository.getOrder();

            } else {
                orderRunnings = videoCommentRepository.getOrder(user.trim());
            }

            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < orderRunnings.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("orderid", orderRunnings.get(i).getOrderId());
                obj.put("videoid", orderRunnings.get(i).getVideoId());
                obj.put("videotitle", orderRunnings.get(i).getVideoTitle());
                obj.put("commentstart", orderRunnings.get(i).getCommentStart());
                obj.put("maxthreads", orderRunnings.get(i).getMaxthreads());
                obj.put("insertdate", orderRunnings.get(i).getInsertDate());
                obj.put("total", orderRunnings.get(i).getTotal());
                obj.put("commentorder", orderRunnings.get(i).getCommentOrder());
                obj.put("note", orderRunnings.get(i).getNote());
                obj.put("duration", orderRunnings.get(i).getDuration());
                obj.put("service", orderRunnings.get(i).getService());
                obj.put("user", orderRunnings.get(i).getUser());
                obj.put("commenttotal", orderRunnings.get(i).getCommentTotal());
                obj.put("price", orderRunnings.get(i).getPrice());
                obj.put("geo",  orderRunnings.get(i).getGeo());
                obj.put("lc_code",  orderRunnings.get(i).getLc_code());
                jsonArray.add(obj);
            }

            resp.put("total", orderRunnings.size());
            resp.put("videocomment", jsonArray);
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
            List<OrderCommentRunning> orderRunnings;
            if (user.length() == 0) {
                orderRunnings = videoCommentRepository.getOrderCheckCancel();

            } else {
                orderRunnings = videoCommentRepository.getOrderCheckCancel(user.trim());
            }

            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < orderRunnings.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("orderid", orderRunnings.get(i).getOrderId());
                obj.put("videoid", orderRunnings.get(i).getVideoId());
                obj.put("videotitle", orderRunnings.get(i).getVideoTitle());
                obj.put("commentstart", orderRunnings.get(i).getCommentStart());
                obj.put("maxthreads", orderRunnings.get(i).getMaxthreads());
                obj.put("insertdate", orderRunnings.get(i).getInsertDate());
                obj.put("total", orderRunnings.get(i).getTotal());
                obj.put("commentorder", orderRunnings.get(i).getCommentOrder());
                obj.put("note", orderRunnings.get(i).getNote());
                obj.put("duration", orderRunnings.get(i).getDuration());
                obj.put("service", orderRunnings.get(i).getService());
                obj.put("user", orderRunnings.get(i).getUser());
                obj.put("commenttotal", orderRunnings.get(i).getCommentTotal());
                obj.put("price", orderRunnings.get(i).getPrice());
                jsonArray.add(obj);
            }

            resp.put("total", orderRunnings.size());
            resp.put("videocomment", jsonArray);
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
            List<VideoComment> videoViewList = videoCommentRepository.getAllOrder();
            viewBuff = videoCommentRepository.getTotalCommentBuffByDataComment();

            for (int i = 0; i < videoViewList.size(); i++) {
                int viewtotal = 0;
                for (int j = 0; j < viewBuff.size(); j++) {
                    if (videoViewList.get(i).getVideoid().equals(viewBuff.get(j).split(",")[0])) {
                        viewtotal = Integer.parseInt(viewBuff.get(j).split(",")[1]);
                    }
                }
                try {
                    videoCommentRepository.updateViewOrderByVideoId(viewtotal, System.currentTimeMillis(), videoViewList.get(i).getVideoid());
                } catch (Exception e) {

                }
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");

            resp.put("total", videoViewList.size());
            resp.put("videocomment", true);
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

    //sua sau


    //sua sau
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
                if(orderid!=videoViewHistories.get(0).getOrderid()){
                    resp.put("videoview", "Không đủ ĐK bảo hành!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            }
            for (int i = 0; i < videoViewHistories.size(); i++) {
                JSONObject obj = new JSONObject();
                if ((videoViewHistories.get(0).getRefund()==null?0:videoViewHistories.get(0).getRefund()) == 1) {
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

                if (System.currentTimeMillis() - videoViewHistories.get(i).getEnddate() < 1000 * 3600 * 24) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Chưa đủ time check bh!");
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
                                priceorder = (baohanh / 1000F) * service.getRate() * ((float) (admins.get(0).getRate())/ 100)*((float) (100 - admins.get(0).getDiscount()) / 100);
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
                                obj.put("videoview", "View check < view start!");
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
                if(orderid!=videoViewHistories.get(0).getOrderid()){
                    resp.put("videoview", "Không đủ ĐK refund!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            }

            for (int i = 0; i < videoViewHistories.size(); i++) {
                JSONObject obj = new JSONObject();
                if ((videoViewHistories.get(0).getRefund()==null?0:videoViewHistories.get(0).getRefund()) == 1) {
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

                if (System.currentTimeMillis() - videoViewHistories.get(i).getEnddate() < 1000 * 3600 * 24) {
                    videoViewHistories.get(i).setTimecheck(System.currentTimeMillis());
                    videoViewHistoryRepository.save(videoViewHistories.get(i));
                    obj.put("videoview", "Chưa đủ time check refund!");
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
                        int viewthan = videoViewHistories.get(i).getVieworder()+videoViewHistories.get(i).getViewstart() - viewcount;
                        if(viewthan>videoViewHistories.get(i).getVieworder()){
                            viewthan=  videoViewHistories.get(i).getVieworder();
                        }
                        float price_refund = ((viewthan) / (float) videoViewHistories.get(i).getVieworder()) * videoViewHistories.get(i).getPrice();
                        //float pricebuffed=(videoBuffh.get(0).getViewtotal()/1000F)*service.getRate()*((float)(100-admins.get(0).getDiscount())/100);
                        if(videoViewHistories.get(i).getPrice()<price_refund){
                            price_refund=videoViewHistories.get(i).getPrice();
                        }
                        float pricebuffed = (videoViewHistories.get(i).getPrice() - price_refund);
                        videoViewHistories.get(i).setPrice(pricebuffed);
                        videoViewHistories.get(i).setViewend(viewcount);
                        videoViewHistories.get(i).setRefund(1);
                        videoViewHistoryRepository.save(videoViewHistories.get(i));
                        //hoàn tiền & add thong báo số dư
                        Float balance_update=adminRepository.updateBalanceFine(price_refund,user.get(0).getUsername().trim());
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


    @PostMapping(path = "checkbhview", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> checkbhview(@RequestBody() VideoViewHistory videoid, @RequestHeader(defaultValue = "") String Authorization) {

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
                if(orderid!=videoViewHistories.get(0).getOrderid()){
                    resp.put("videoview", "Không đủ ĐK bảo hành!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            }
            for (int i = 0; i < videoViewHistories.size(); i++) {
                JSONObject obj = new JSONObject();
                if ((videoViewHistories.get(0).getRefund()==null?0:videoViewHistories.get(0).getRefund()) == 1) {
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
            List<OrderCommentHistory> orderRunnings;
            if (user.length() == 0) {
                orderRunnings = videoCommentHistoryRepository.getVideoViewHistories();
            } else {
                orderRunnings = videoCommentHistoryRepository.getVideoViewHistories(user.trim());
            }
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < orderRunnings.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("orderid", orderRunnings.get(i).getOrderId());
                obj.put("videoid", orderRunnings.get(i).getVideoid());
                obj.put("commentstart", orderRunnings.get(i).getCommentstart());
                obj.put("insertdate", orderRunnings.get(i).getInsertdate());
                obj.put("user", orderRunnings.get(i).getUser());
                obj.put("note", orderRunnings.get(i).getNote());
                obj.put("enddate", orderRunnings.get(i).getEnddate());
                obj.put("cancel", orderRunnings.get(i).getCancel());
                //obj.put("home_rate", orderRunnings.get(i).get());
                obj.put("commentend", orderRunnings.get(i).getCommentend());
                obj.put("commenttotal", orderRunnings.get(i).getCommenttotal());
                obj.put("commentorder", orderRunnings.get(i).getCommentorder());
                obj.put("price", orderRunnings.get(i).getPrice());
                obj.put("service", orderRunnings.get(i).getService());
                obj.put("geo", orderRunnings.get(i).getGeo());
                obj.put("lc_code",  orderRunnings.get(i).getLc_code());
                jsonArray.add(obj);
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");

            resp.put("total", orderRunnings.size());
            resp.put("videocomment", jsonArray);
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
            List<String> ordersArrInput = new ArrayList<>();
            ordersArrInput.addAll(Arrays.asList(videoid.split(",")));
            List<VideoCommentHistory> orderRunnings = videoCommentHistoryRepository.getVideoViewHistoriesByListVideoId(ordersArrInput);
            if (orderRunnings.size() == 0) {
                resp.put("status", "fail");
                resp.put("total", orderRunnings.size());
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < orderRunnings.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("orderid", orderRunnings.get(i).getOrderid());
                obj.put("videoid", orderRunnings.get(i).getVideoid());
                obj.put("videotitle", orderRunnings.get(i).getVideotitle());
                obj.put("commentstart", orderRunnings.get(i).getCommentstart());
                obj.put("maxthreads", orderRunnings.get(i).getMaxthreads());
                obj.put("insertdate", orderRunnings.get(i).getInsertdate());
                obj.put("user", orderRunnings.get(i).getUser());
                obj.put("note", orderRunnings.get(i).getNote());
                obj.put("duration", orderRunnings.get(i).getDuration());
                obj.put("enddate", orderRunnings.get(i).getEnddate());
                obj.put("cancel", orderRunnings.get(i).getCancel());
                //obj.put("home_rate", orderRunnings.get(i).get());
                obj.put("commentend", orderRunnings.get(i).getCommentend());
                obj.put("commenttotal", orderRunnings.get(i).getCommenttotal());
                obj.put("commentorder", orderRunnings.get(i).getCommentorder());
                obj.put("price", orderRunnings.get(i).getPrice());
                obj.put("service", orderRunnings.get(i).getService());
                jsonArray.add(obj);
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");
            resp.put("total", orderRunnings.size());
            resp.put("videocomment", jsonArray);
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
                List<VideoComment> videoBuffh = videoCommentRepository.getVideoBuffhById(videoidArr[i].trim());
                VideoCommentHistory videoBuffhnew = new VideoCommentHistory();
                videoBuffhnew.setOrderid(videoBuffh.get(0).getOrderid());
                videoBuffhnew.setDuration(videoBuffh.get(0).getDuration());
                videoBuffhnew.setInsertdate(videoBuffh.get(0).getInsertdate());
                videoBuffhnew.setService(videoBuffh.get(0).getService());
                videoBuffhnew.setChannelid(videoBuffh.get(0).getChannelid());
                videoBuffhnew.setVideotitle(videoBuffh.get(0).getVideotitle());
                videoBuffhnew.setVideoid(videoBuffh.get(0).getVideoid());
                videoBuffhnew.setCommentstart(videoBuffh.get(0).getCommentstart());
                videoBuffhnew.setCommentorder(videoBuffh.get(0).getCommentorder());
                videoBuffhnew.setMaxthreads(videoBuffh.get(0).getMaxthreads());
                videoBuffhnew.setNote(videoBuffh.get(0).getNote());
                videoBuffhnew.setLc_code(videoBuffh.get(0).getLc_code());
                videoBuffhnew.setNumbh(0);
                videoBuffhnew.setTimecheck(0L);
                //videoBuffhnew.setPrice(videoBuffh.get(0).getPrice());
                if (cancel == 1) {
                    Service service = serviceRepository.getService(videoBuffh.get(0).getService());
                    List<Admin> user = adminRepository.getAdminByUser(videoBuffh.get(0).getUser());
                    //Hoàn tiền những view chưa buff
                    int viewbuff = videoBuffh.get(0).getCommenttotal();
                    float price_refund = ((videoBuffh.get(0).getCommentorder() - videoBuffh.get(0).getCommenttotal()) / (float) videoBuffh.get(0).getCommentorder()) * videoBuffh.get(0).getPrice();
                    //float pricebuffed=(videoBuffh.get(0).getViewtotal()/1000F)*service.getRate()*((float)(100-admins.get(0).getDiscount())/100);
                    float pricebuffed = (videoBuffh.get(0).getPrice() - price_refund);
                    videoBuffhnew.setPrice(pricebuffed);
                    if (viewbuff == 0) {
                        videoBuffhnew.setCancel(1);
                    } else {
                        videoBuffhnew.setCancel(2);
                    }
                    //hoàn tiền & add thong báo số dư
                    int viewthan = (int) (videoBuffh.get(0).getCommentorder() - viewbuff);
                    //
                    Float balance_update=adminRepository.updateBalanceFine(price_refund,user.get(0).getUsername().trim());
                    Balance balance = new Balance();
                    balance.setUser(user.get(0).getUsername().trim());
                    balance.setTime(System.currentTimeMillis());
                    balance.setTotalblance(balance_update);
                    balance.setBalance(price_refund);
                    balance.setService(videoBuffh.get(0).getService());
                    balance.setNote("Refund " + (viewthan) + " cmt cho video " + videoBuffh.get(0).getVideoid());
                    balanceRepository.save(balance);
                } else {
                    videoBuffhnew.setPrice(videoBuffh.get(0).getPrice());
                    videoBuffhnew.setCancel(0);
                }
                videoBuffhnew.setUser(videoBuffh.get(0).getUser());
                videoBuffhnew.setEnddate(enddate);
                videoBuffhnew.setCommenttotal(videoBuffh.get(0).getCommenttotal());
                videoCommentHistoryRepository.save(videoBuffhnew);
                videoCommentRepository.deletevideoByVideoId(videoidArr[i].trim());
            }
            resp.put("videocomment", "");
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
            List<VideoComment> videoBuffh = videoCommentRepository.getOrderFullCmt();
            for (int i = 0; i < videoBuffh.size(); i++) {
                Long enddate = System.currentTimeMillis();

                VideoCommentHistory videoBuffhnew = new VideoCommentHistory();
                videoBuffhnew.setOrderid(videoBuffh.get(i).getOrderid());
                videoBuffhnew.setDuration(videoBuffh.get(i).getDuration());
                videoBuffhnew.setInsertdate(videoBuffh.get(i).getInsertdate());
                videoBuffhnew.setChannelid(videoBuffh.get(i).getChannelid());
                videoBuffhnew.setVideotitle(videoBuffh.get(i).getVideotitle());
                videoBuffhnew.setVideoid(videoBuffh.get(i).getVideoid());
                videoBuffhnew.setCommentstart(videoBuffh.get(i).getCommentstart());
                videoBuffhnew.setMaxthreads(videoBuffh.get(i).getMaxthreads());
                videoBuffhnew.setNote(videoBuffh.get(i).getNote());
                videoBuffhnew.setCancel(0);
                videoBuffhnew.setNumbh(0);
                videoBuffhnew.setTimecheck(0L);
                videoBuffhnew.setUser(videoBuffh.get(i).getUser());
                videoBuffhnew.setLc_code(videoBuffh.get(i).getLc_code());
                videoBuffhnew.setEnddate(enddate);
                videoBuffhnew.setService(videoBuffh.get(i).getService());
                videoBuffhnew.setCommenttotal(videoBuffh.get(i).getCommenttotal());
                videoBuffhnew.setCommentorder(videoBuffh.get(i).getCommentorder());
                videoBuffhnew.setPrice(videoBuffh.get(i).getPrice());
                try {
                    videoCommentHistoryRepository.save(videoBuffhnew);
                    videoCommentRepository.deletevideoByVideoId(videoBuffh.get(i).getVideoid().trim());
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

    @GetMapping(path = "updateVideoReplyDoneCron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateVideoReplyDoneCron() {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            //historyRepository.updateHistoryByAccount();
            List<VideoComment> videoBuffh = videoCommentRepository.getOrderFullReply();
            for (int i = 0; i < videoBuffh.size(); i++) {
                Long enddate = System.currentTimeMillis();

                VideoCommentHistory videoBuffhnew = new VideoCommentHistory();
                videoBuffhnew.setOrderid(videoBuffh.get(i).getOrderid());
                videoBuffhnew.setDuration(videoBuffh.get(i).getDuration());
                videoBuffhnew.setInsertdate(videoBuffh.get(i).getInsertdate());
                videoBuffhnew.setChannelid(videoBuffh.get(i).getChannelid());
                videoBuffhnew.setVideotitle(videoBuffh.get(i).getVideotitle());
                videoBuffhnew.setVideoid(videoBuffh.get(i).getVideoid());
                videoBuffhnew.setCommentstart(videoBuffh.get(i).getCommentstart());
                videoBuffhnew.setMaxthreads(videoBuffh.get(i).getMaxthreads());
                videoBuffhnew.setNote(videoBuffh.get(i).getNote());
                videoBuffhnew.setCancel(0);
                videoBuffhnew.setNumbh(0);
                videoBuffhnew.setTimecheck(0L);
                videoBuffhnew.setUser(videoBuffh.get(i).getUser());
                videoBuffhnew.setEnddate(enddate);
                videoBuffhnew.setService(videoBuffh.get(i).getService());
                videoBuffhnew.setCommenttotal(videoBuffh.get(i).getCommenttotal());
                videoBuffhnew.setCommentorder(videoBuffh.get(i).getCommentorder());
                videoBuffhnew.setPrice(videoBuffh.get(i).getPrice());
                videoBuffhnew.setLc_code(videoBuffh.get(i).getLc_code());
                try {
                    videoCommentHistoryRepository.save(videoBuffhnew);
                    videoCommentRepository.deletevideoByVideoId(videoBuffh.get(i).getVideoid().trim());
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


    @GetMapping(path = "updateStateComment", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateStateComment() {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            //historyRepository.updateHistoryByAccount();
            List<VideoComment> videoComments = videoCommentRepository.getOrderThreadNull();
            Setting setting = settingRepository.getReferenceById(1L);
            for (int i = 0; i < videoComments.size(); i++) {
                String[] comments = videoComments.get(i).getListcomment().split("\n");
                System.out.println(comments);
                for (int j = 0; j < comments.length; j++) {
                    if (comments[j].length() == 0) {
                        continue;
                    }
                    DataComment dataComment = new DataComment();
                    dataComment.setOrderid(videoComments.get(i).getOrderid());
                    dataComment.setComment(comments[j]);
                    dataComment.setUsername("");
                    dataComment.setRunning(0);
                    dataComment.setTimeget(0L);
                    dataComment.setVps("");
                    dataCommentRepository.save(dataComment);
                }
                Service service = serviceRepository.getService(videoComments.get(i).getService());
                int max_thread = service.getThread() + ((int)(videoComments.get(i).getCommentorder() / 30)<1?0:(int)(videoComments.get(i).getCommentorder() / 30) - 1)*3;
                 if (max_thread <= 50) {
                     videoComments.get(i).setMaxthreads(max_thread);
                 } else {
                     videoComments.get(i).setMaxthreads(50);
                 }
                videoCommentRepository.save(videoComments.get(i));
            }
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "updateStateReply", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateStateReply() {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            //historyRepository.updateHistoryByAccount();
            List<VideoComment> videoComments = videoCommentRepository.getOrderReplyThreadNull();
            Setting setting = settingRepository.getReferenceById(1L);
            for (int i = 0; i < videoComments.size(); i++) {
                Service service = serviceRepository.getService(videoComments.get(i).getService());
                String[] comments = videoComments.get(i).getListcomment().split("\n");
                List<String> arrCmt = new ArrayList<>();
                if(service.getReply()==1){
                    for (int j = 0; j < comments.length; j++) {
                        if (comments[j].length() == 0) {
                            continue;
                        }
                        int check_done=0;
                        if(comments[j].indexOf("|")>0){
                            String[] cmt_reply=comments[j].split("\\|");
                            if(!arrCmt.contains(cmt_reply[0].trim())){
                                arrCmt.add(cmt_reply[0].trim());
                                DataComment dataComment = new DataComment();
                                dataComment.setOrderid(videoComments.get(i).getOrderid());
                                dataComment.setComment(cmt_reply[0].trim());
                                dataComment.setUsername("");
                                dataComment.setRunning(0);
                                dataComment.setTimeget(0L);
                                dataComment.setVps("");
                                dataCommentRepository.save(dataComment);
                            }else{
                                check_done=1;
                            }

                            DataReplyComment dataReplyComment=new DataReplyComment();
                            dataReplyComment.setComment_id(dataCommentRepository.getByCommentId(videoComments.get(i).getOrderid(),cmt_reply[0].trim()));
                            dataReplyComment.setOrderid(videoComments.get(i).getOrderid());
                            dataReplyComment.setReply(cmt_reply[1].trim());
                            dataReplyComment.setRunning(-1);
                            dataReplyComment.setCheck_done(check_done);
                            dataReplyComment.setTimeget(0L);
                            dataReplyComment.setUsername("");
                            dataReplyComment.setVps("");
                            dataReplyCommentRepository.save(dataReplyComment);
                        }else{
                            if(!arrCmt.equals(comments[j].trim())) {

                                arrCmt.add(comments[j].trim());

                                DataComment dataComment = new DataComment();
                                dataComment.setOrderid(videoComments.get(i).getOrderid());
                                dataComment.setComment(comments[j].trim());
                                dataComment.setUsername("");
                                dataComment.setRunning(0);
                                dataComment.setTimeget(0L);
                                dataComment.setVps("");
                                dataCommentRepository.save(dataComment);
                            }
                        }
                    }
                }else{
                    for (int j = 0; j < comments.length; j++) {
                        if (comments[j].length() == 0) {
                            continue;
                        }
                        DataReplyComment dataReplyComment=new DataReplyComment();
                        dataReplyComment.setComment_id(-1L);
                        dataReplyComment.setOrderid(videoComments.get(i).getOrderid());
                        dataReplyComment.setReply(comments[j].trim());
                        dataReplyComment.setRunning(0);
                        dataReplyComment.setCheck_done(1);
                        dataReplyComment.setTimeget(0L);
                        dataReplyComment.setUsername("");
                        dataReplyComment.setLink(videoComments.get(i).getLc_code());
                        dataReplyComment.setVps("");
                        dataReplyCommentRepository.save(dataReplyComment);
                    }
                }
                int max_thread = service.getThread() + ((int)(videoComments.get(i).getCommentorder() / 30)<1?0:(int)(videoComments.get(i).getCommentorder() / 30) - 1)*3;
                if (max_thread <= 50) {
                    videoComments.get(i).setMaxthreads(max_thread);
                } else {
                    videoComments.get(i).setMaxthreads(50);
                }
                videoCommentRepository.save(videoComments.get(i));
            }
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            System.out.println(stackTraceElement.getMethodName());
            System.out.println(stackTraceElement.getLineNumber());
            System.out.println(stackTraceElement.getClassName());
            System.out.println(stackTraceElement.getFileName());
            System.out.println("Error : " + e.getMessage());
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
            videoCommentRepository.updateCheckCancel(videoid.trim());
            resp.put("status", "true"+videoid);
            resp.put("message", "update trạng thái đơn thành công!");
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
            List<OrderCommentRunning> orderRunnings = videoCommentRepository.getOrderCancelThan2h();
            for (int i=0;i<orderRunnings.size();i++){
                delete("1",orderRunnings.get(i).getVideoId(),1);
            }
            List<VideoComment> videoComments=videoCommentRepository.getAllOrderCheckCancel();
            for(int i=0;i<videoComments.size();i++){

                OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                Request request1 = null;
                List<GoogleAPIKey> keys = googleAPIKeyRepository.getAllByState();
                request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=" + keys.get(0).getKey().trim() + "&fields=items(id,contentDetails(regionRestriction(blocked)))&part=id,contentDetails&id=" + videoComments.get(i).getVideoid().trim()).get().build();
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
                    delete("1",videoComments.get(i).getVideoid().trim(),1);
                    continue;
                }else {
                    while (k.hasNext()) {
                        try {
                            JSONObject video = (JSONObject) k.next();
                            JSONObject contentDetails = (JSONObject) video.get("contentDetails");
                            JSONObject regionRestriction = (JSONObject) contentDetails.get("regionRestriction");
                            if (regionRestriction != null) {
                                if (regionRestriction.get("blocked").toString().indexOf("VN") > 0 && videoCommentRepository.getServiceByVideoId(videoComments.get(i).getVideoid().trim(), "vn") > 0) {
                                    delete("1", videoComments.get(i).getVideoid().trim(), 1);
                                } else if (regionRestriction.get("blocked").toString().indexOf("US") > 0 && videoCommentRepository.getServiceByVideoId(videoComments.get(i).getVideoid().trim(), "us") > 0) {
                                    delete("1", videoComments.get(i).getVideoid().trim(), 1);
                                }else if (regionRestriction.get("blocked").toString().indexOf("KR") > 0 && videoCommentRepository.getServiceByVideoId(videoComments.get(i).getVideoid().trim(), "kr") > 0) {
                                    delete("1", videoComments.get(i).getVideoid().trim(), 1);
                                } else {
                                    videoCommentRepository.updateOrderCheck(videoComments.get(i).getVideoid().trim());
                                }
                            } else {
                                videoCommentRepository.updateOrderCheck(videoComments.get(i).getVideoid().trim());
                            }
                        } catch (Exception e) {
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
            videoCommentHistoryRepository.updateRefund(ordersArrInput);
            resp.put("status", "true");
            resp.put("message", "Refund đơn thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping(path = "update", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> update(@RequestHeader(defaultValue = "") String Authorization, @RequestBody VideoComment videoBuffh) {
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
                List<VideoComment> video = videoCommentRepository.getVideoBuffhById(videoidIdArr[i].trim());
                Service service = serviceRepository.getService(video.get(0).getService());
                float priceorder = 0;
                if (videoBuffh.getCommentorder() != video.get(0).getCommentorder()) {
                    List<Admin> user = adminRepository.getAdminByUser(videoBuffh.getUser());
                    priceorder = ((videoBuffh.getCommentorder() - video.get(0).getCommentorder())) * (video.get(0).getPrice() / video.get(0).getCommentorder());

                    if (priceorder > (float) user.get(0).getBalance()) {
                        resp.put("message", "Số tiền không đủ!!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    int timethan = videoBuffh.getCommentorder() - video.get(0).getCommentorder();

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
                video.get(0).setCommentorder(videoBuffh.getCommentorder());
                video.get(0).setNote(videoBuffh.getNote());
                video.get(0).setPrice(videoBuffh.getPrice() + priceorder);
                videoCommentRepository.save(video.get(0));

                List<OrderCommentRunning> orderRunnings = videoCommentRepository.getVideoViewById(videoidIdArr[i].trim());
                JSONObject obj = new JSONObject();
                obj.put("orderid", orderRunnings.get(0).getOrderId());
                obj.put("videoid", orderRunnings.get(0).getVideoId());
                obj.put("videotitle", orderRunnings.get(0).getVideoTitle());
                obj.put("viewstart", orderRunnings.get(0).getCommentStart());
                obj.put("maxthreads", orderRunnings.get(0).getMaxthreads());
                obj.put("insertdate", orderRunnings.get(0).getInsertDate());
                obj.put("total", orderRunnings.get(0).getTotal());
                obj.put("note", orderRunnings.get(0).getNote());
                obj.put("duration", orderRunnings.get(0).getDuration());
                obj.put("commentorder", orderRunnings.get(0).getCommentOrder());
                obj.put("service", orderRunnings.get(0).getService());
                obj.put("user", orderRunnings.get(0).getUser());
                obj.put("geo", service.getGeo());
                obj.put("commenttotal", orderRunnings.get(0).getCommentTotal());
                obj.put("price", videoBuffh.getPrice() + priceorder);

                jsonArray.add(obj);
            }
            resp.put("videocomment", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    String refundCMTByVideoComment(@RequestBody() VideoCommentHistory videoCommentHistory) {

        try {
            Service service = serviceRepository.getInfoService(videoCommentHistory.getService());
            JSONObject obj = new JSONObject();

            OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            List<GoogleAPIKey> keys = googleAPIKeyRepository.getAllByState();
            Request request1 = null;
            request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=" + keys.get(0).getKey().trim() + "&fields=items(statistics(commentCount))&part=statistics&id=" + videoCommentHistory.getVideoid().trim()).get().build();
            keys.get(0).setCount(keys.get(0).getCount() + 1L);
            googleAPIKeyRepository.save(keys.get(0));
            Response response1 = client1.newCall(request1).execute();

            String resultJson1 = response1.body().string();

            Object obj1 = new JSONParser().parse(resultJson1);

            JSONObject jsonObject1 = (JSONObject) obj1;
            JSONArray items = (JSONArray) jsonObject1.get("items");
            if (items == null) {
                videoCommentHistory.setTimecheck(System.currentTimeMillis());
                videoCommentHistoryRepository.save(videoCommentHistory);
                return "Không check được cmt";
            }
            Iterator k = items.iterator();
            if (k.hasNext() == false) {
                videoCommentHistory.setTimecheck(System.currentTimeMillis());
                videoCommentHistoryRepository.save(videoCommentHistory);
                return "Không check được cmt";
            }
            while (k.hasNext()) {
                try {
                    JSONObject video = (JSONObject) k.next();
                    JSONObject statistics = (JSONObject) video.get("statistics");
                    List<Admin> user = adminRepository.getAdminByUser(videoCommentHistory.getUser());
                    //Hoàn tiền những view chưa buff
                    int cmtCount = Integer.parseInt(statistics.get("commentCount").toString());
                    int cmtFix = videoCommentHistory.getCommentorder() > videoCommentHistory.getCommenttotal() ? videoCommentHistory.getCommenttotal() : videoCommentHistory.getCommentorder();
                    int cmtThan = cmtFix + videoCommentHistory.getCommentstart() - cmtCount;
                    if(cmtThan<=0){
                        if(service.getChecktime()==0){
                            videoCommentHistory.setCommentend(cmtCount);
                            videoCommentHistory.setTimecheck(System.currentTimeMillis());
                        }
                        videoCommentHistoryRepository.save(videoCommentHistory);
                        return "Đủ cmt | " +cmtCount+"/"+(cmtFix+videoCommentHistory.getCommentstart());
                    }

                    float price_refund = ((cmtThan) / (float) cmtFix) * videoCommentHistory.getPrice();
                    //float pricebuffed=(videoBuffh.get(0).getViewtotal()/1000F)*service.getRate()*((float)(100-admins.get(0).getDiscount())/100);
                    if (videoCommentHistory.getPrice() < price_refund) {
                        price_refund = videoCommentHistory.getPrice();
                    }
                    float pricebuffed = (videoCommentHistory.getPrice() - price_refund);
                    videoCommentHistory.setPrice(pricebuffed);
                    videoCommentHistory.setCommentend(cmtCount);
                    videoCommentHistory.setTimecheck(System.currentTimeMillis());
                    videoCommentHistory.setCommenttotal(cmtFix - cmtThan);
                    videoCommentHistory.setNumbh(1);
                    if (videoCommentHistory.getCommenttotal()==0) {
                        videoCommentHistory.setCancel(1);
                    } else {
                        videoCommentHistory.setCancel(2);
                    }
                    videoCommentHistoryRepository.save(videoCommentHistory);
                    //hoàn tiền & add thong báo số dư
                    Float balance_update=adminRepository.updateBalanceFine(price_refund,videoCommentHistory.getUser().trim());
                    Balance balance = new Balance();
                    balance.setUser(user.get(0).getUsername().trim());
                    balance.setTime(System.currentTimeMillis());
                    balance.setTotalblance(balance_update);
                    balance.setBalance(price_refund);
                    balance.setService(videoCommentHistory.getService());
                    balance.setNote("Refund " + (cmtThan) + " cmt cho " + videoCommentHistory.getVideoid());
                    balanceRepository.save(balance);

                    if(videoCommentHistory.getPrice()==0){
                        return "Đã hoàn 100%";
                    }else{
                        return "Đã hoàn phần thiếu";
                    }
                } catch (Exception e) {
                    return "Fail";
                }
            }
            return "Fail";
        } catch (Exception e) {
            return "Fail";
        }
    }
    @GetMapping(path = "updateRefundHis", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateRefundHis(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String orderid) {
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
                VideoCommentHistory video = videoCommentHistoryRepository.getVideoViewHisById(Long.parseLong(videoidIdArr[i].trim()));
                Float price_old=video.getPrice();
                Service service = serviceRepository.getInfoService(video.getService());
                VideoCommentHistory video_refil=video;
                if(service.getRefill()==0){
                    status="DV không bảo hành";
                }else if(video.getUser().equals("baohanh01@gmail.com")){
                    status="Đơn bảo hành";
                } else if(videoCommentRepository.getCountVideoIdNotPending(video.getVideoid())>0){
                    status="Đơn mới đang chạy";
                }else if(video.getCancel()==1){
                    status="Được hủy trước đó";
                }else if(serviceRepository.checkGuarantee(video.getEnddate(),service.getMaxtimerefill())==0){
                    status="Quá hạn "+service.getMaxtimerefill()+" ngày";
                }else{
                    status=refundCMTByVideoComment(video);
                    video_refil= videoCommentHistoryRepository.getVideoViewHisById(Long.parseLong(videoidIdArr[i].trim()));
                }

                JSONObject obj = new JSONObject();
                obj.put("orderid", video_refil.getOrderid());
                obj.put("videoid", video_refil.getVideoid());
                obj.put("videotitle", video_refil.getVideotitle());
                obj.put("commentstart",video_refil.getCommentstart());
                obj.put("maxthreads", video_refil.getMaxthreads());
                obj.put("insertdate", video_refil.getInsertdate());
                obj.put("user", video_refil.getUser());
                obj.put("note", video_refil.getNote());
                obj.put("duration", video_refil.getDuration());
                obj.put("enddate", video_refil.getEnddate());
                obj.put("cancel", video_refil.getCancel());
                //obj.put("home_rate", orderRunnings.get(i).get());
                obj.put("commentend", video_refil.getCommentend());
                obj.put("commenttotal", video_refil.getCommenttotal());
                obj.put("commentorder", video_refil.getCommentorder());
                obj.put("price", video_refil.getPrice());
                obj.put("service", video_refil.getService());
                obj.put("status", status);

                jsonArray.add(obj);
            }
            resp.put("videocomment", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(path = "updatethread", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updatethread(@RequestHeader(defaultValue = "") String Authorization, @RequestBody VideoComment videoBuffh) {
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
                List<VideoComment> video = videoCommentRepository.getVideoBuffhById(videoidIdArr[i].trim());
                video.get(0).setMaxthreads(videoBuffh.getMaxthreads());
                videoCommentRepository.save(video.get(0));

                List<OrderCommentRunning> orderRunnings = videoCommentRepository.getVideoViewById(videoidIdArr[i].trim());
                JSONObject obj = new JSONObject();
                obj.put("orderid", orderRunnings.get(0).getOrderId());
                obj.put("videoid", orderRunnings.get(0).getVideoId());
                obj.put("videotitle", orderRunnings.get(0).getVideoTitle());
                obj.put("commentstart", orderRunnings.get(0).getCommentStart());
                obj.put("maxthreads", videoBuffh.getMaxthreads());
                obj.put("insertdate", orderRunnings.get(0).getInsertDate());
                obj.put("total", orderRunnings.get(0).getTotal());
                obj.put("note", orderRunnings.get(0).getNote());
                obj.put("duration", orderRunnings.get(0).getDuration());
                obj.put("service", orderRunnings.get(0).getService());
                obj.put("user", orderRunnings.get(0).getUser());
                obj.put("commenttotal", orderRunnings.get(0).getCommentTotal());
                obj.put("price", orderRunnings.get(0).getPrice());
                obj.put("commentorder", orderRunnings.get(0).getCommentOrder());


                jsonArray.add(obj);
            }
            resp.put("videocomment", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
}

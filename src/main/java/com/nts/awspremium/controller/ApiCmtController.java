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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
public class ApiCmtController {
    @Autowired
    private VideoViewRepository videoViewRepository;
    @Autowired
    private VideoCommentRepository videoCommentRepository;

    @Autowired
    private VideoCommentHistoryRepository videoCommentHistoryRepository;

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
    private DataCommentRepository dataCommentRepository;

    @PostMapping(value = "/cmt", produces = "application/hal+json;charset=utf8")
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
                List<Service> services = serviceRepository.getAllServiceCmt();
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
                    VideoComment video = videoCommentRepository.getVideoViewById(data.getOrder());
                    VideoCommentHistory videoHistory = videoCommentHistoryRepository.getVideoViewHisById(data.getOrder());
                    if (video != null) {
                        resp.put("start_count", video.getCommentstart());
                        resp.put("current_count", videoHistory.getCommentstart() + videoHistory.getCommenttotal());
                        resp.put("charge", video.getPrice());
                        resp.put("status", "In progress");
                        resp.put("remains", video.getCommentorder() - video.getCommenttotal());
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    } else {
                        if (videoHistory == null) {
                            resp.put("error", "Incorrect order ID");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        } else {
                            resp.put("start_count", videoHistory.getCommentstart());
                            resp.put("current_count", videoHistory.getCommentstart() + videoHistory.getCommenttotal());
                            resp.put("charge", videoHistory.getPrice());
                            if (videoHistory.getCancel() == 1) {
                                resp.put("status", "Canceled");
                            } else if (videoHistory.getCancel() == 2) {
                                resp.put("status", "Partial");
                            } else {
                                resp.put("status", "Completed");
                            }
                            resp.put("remains", videoHistory.getCommentorder() - videoHistory.getCommenttotal());
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                    }

                } else {
                    List<String> ordersArrInput = new ArrayList<>();
                    ordersArrInput.addAll(Arrays.asList(data.getOrders().split(",")));
                    String listId = String.join(",", ordersArrInput);
                    List<VideoComment> videoViews = videoCommentRepository.getVideoViewByListId(ordersArrInput);
                    JSONObject videosview = new JSONObject();
                    for (VideoComment v : videoViews) {
                        JSONObject videoview = new JSONObject();
                        videoview.put("start_count", v.getCommentstart());
                        videoview.put("current_count", v.getCommentstart() + v.getCommenttotal());
                        videoview.put("charge", v.getPrice());
                        videoview.put("status", "In progress");
                        videoview.put("remains", v.getCommentorder() - v.getCommenttotal());
                        videosview.put("" + v.getOrderid(), videoview);
                        ordersArrInput.remove("" + v.getOrderid());
                    }
                    String listIdHis = String.join(",", ordersArrInput);
                    List<VideoCommentHistory> videoViewHistory = videoCommentHistoryRepository.getVideoViewHisByListId(ordersArrInput);
                    for (VideoCommentHistory vh : videoViewHistory) {
                        JSONObject videohisview = new JSONObject();
                        if (videoViewHistory != null) {
                            videohisview.put("start_count", vh.getCommentstart());
                            videohisview.put("current_count", vh.getCommentstart()+vh.getCommenttotal());
                            videohisview.put("charge", vh.getPrice());
                            if (vh.getCancel() == 1) {
                                videohisview.put("status", "Canceled");
                            } else if (vh.getCancel() == 2) {
                                videohisview.put("status", "Partial");
                            } else {
                                videohisview.put("status", "Completed");
                            }
                            videohisview.put("remains", vh.getCommentorder() - vh.getCommenttotal());
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
                /*
                if (data.getQuantity() < 100) {
                    resp.put("error", "Min quantity is 100");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if (data.getQuantity() > 120000) {
                    resp.put("error", "Max quantity is 120000");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                 */
                Service service = serviceRepository.getService(data.getService());
                Setting setting = settingRepository.getReferenceById(1L);
                if (videoViewRepository.getCountOrderByUser(admins.get(0).getUsername().trim()) >= admins.get(0).getMaxorder() || (service.getGeo().equals("vn") && settingRepository.getMaxOrderVN() == 0) ||
                        (service.getGeo().equals("us") && settingRepository.getMaxOrderUS() == 0)) {
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

                int count = StringUtils.countOccurrencesOf(videolist, ",") + 1;
                OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                Request request1 = null;

                request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=AIzaSyClOKa8qUz3MJD1RKBsjlIDR5KstE2NmMY&fields=items(id,snippet(title,channelId,liveBroadcastContent),statistics(commentCount),contentDetails(duration))&part=snippet,statistics,contentDetails&id=" + videolist).get().build();

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
                        if (service == null) {
                            resp.put("error", "Service not found ");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                        if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() == 0) {
                            resp.put("error", "This video is a livestream video");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                        if (Duration.parse(contentDetails.get("duration").toString()).getSeconds() < 60) {
                            resp.put("error", "Videos under 60 seconds");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                        float priceorder = 0;
                        int time = 0;
                        priceorder = (data.getQuantity() / 1000F) * service.getRate() * ((float) (admins.get(0).getRate()) / 100) * ((float) (100 - admins.get(0).getDiscount()) / 100);
                        if (priceorder > (float) admins.get(0).getBalance()) {
                            resp.put("error", "Your balance not enough");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                        JSONObject snippet = (JSONObject) video.get("snippet");
                        if (!snippet.get("liveBroadcastContent").toString().equals("none")) {
                            resp.put("error", "This video is not a pure public video");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                        JSONObject statistics = (JSONObject) video.get("statistics");
                        VideoComment videoViewhnew = new VideoComment();
                        videoViewhnew.setDuration(Duration.parse(contentDetails.get("duration").toString()).getSeconds());
                        videoViewhnew.setInsertdate(System.currentTimeMillis());
                        videoViewhnew.setCommenttotal(0);
                        videoViewhnew.setCommentorder(data.getQuantity());
                        videoViewhnew.setUser(admins.get(0).getUsername());
                        videoViewhnew.setChannelid(snippet.get("channelId").toString());
                        videoViewhnew.setVideotitle(snippet.get("title").toString());
                        videoViewhnew.setVideoid(video.get("id").toString());
                        videoViewhnew.setCommentstart(Integer.parseInt(statistics.get("commentCount").toString()));
                        //int max_thread = service.getThread() + ((int) (data.getQuantity() / 1000) - 1) * setting.getLevelthread();
                        //int max_thread = (int)(data.getQuantity() / 2);
                        /*
                        if (max_thread <= setting.getMaxthread()) {
                            videoViewhnew.setMaxthreads(max_thread);
                        } else {
                            videoViewhnew.setMaxthreads(setting.getMaxthread());
                        }

                         */
                        videoViewhnew.setMaxthreads(service.getThread());
                        videoViewhnew.setPrice(priceorder);
                        videoViewhnew.setNote("");
                        videoViewhnew.setService(data.getService());
                        videoCommentRepository.save(videoViewhnew);

                        //list comment
                        String[] comments = data.getComments().split("\n");
                        System.out.println(comments);
                        for (int i = 0; i < comments.length; i++) {
                            DataComment dataComment = new DataComment();
                            dataComment.setOrderid(videoViewhnew.getOrderid());
                            dataComment.setComment(comments[i]);
                            dataComment.setUsername("");
                            dataComment.setRunning(0);
                            dataComment.setTimeget(0L);
                            dataComment.setVps("");
                            dataCommentRepository.save(dataComment);
                        }

                        float balance_new = admins.get(0).getBalance() - priceorder;
                        adminRepository.updateBalance(balance_new, admins.get(0).getUsername());
                        Balance balance = new Balance();
                        balance.setUser(admins.get(0).getUsername().trim());
                        balance.setTime(System.currentTimeMillis());
                        balance.setTotalblance(balance_new);
                        balance.setBalance(-priceorder);
                        balance.setService(data.getService());
                        balance.setNote("Order " + data.getQuantity() + " comment cho video " + videoViewhnew.getVideoid());
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

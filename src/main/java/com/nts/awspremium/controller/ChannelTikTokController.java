package com.nts.awspremium.controller;

import com.nts.awspremium.GoogleApi;
import com.nts.awspremium.TikTokApi;
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
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/channel_tiktok")
public class ChannelTikTokController {
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private SettingTikTokRepository settingTikTokRepository;
    @Autowired
    private VideoViewHistoryRepository videoViewHistoryRepository;
    @Autowired
    private VideoViewRepository videoViewRepository;
    @Autowired
    private ChannelTikTokRepository channelTikTokRepository;

    @Autowired
    private ChannelTikTokHistoryRepository channelTikTokHistoryRepository;
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

    @Autowired
    private ProxyRepository proxyRepository;

    @PostMapping(value = "/orderFollower", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> orderFollower(@RequestBody ChannelTiktok channelTiktok, @RequestHeader(defaultValue = "") String Authorization) throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        //System.out.println(videoView.getService());
        try {
            List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
            if (Authorization.length() == 0 || admins.size() == 0) {
                resp.put("status", "fail");
                resp.put("channel_tiktok", "Token expired");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            Service service = serviceRepository.getServiceTikTokNoCheckEnabled(channelTiktok.getService());
            if (service == null) {
                resp.put("channel_tiktok", "Service not found ");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            /*
            if (videoView.getVieworder() > service.getMax() || videoView.getVieworder() < service.getMin()) {
                resp.put("error", "Min/Max order is: " + service.getMin() + "/" + service.getMax());
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
             */
            if (service.getMaxorder() <= channelTikTokRepository.getCountOrderByService(channelTiktok.getService())) {
                resp.put("channel_tiktok", "System busy try again!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            if (channelTiktok.getTiktok_id().trim().length() == 0) {
                resp.put("channel_tiktok", "Link is null");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            Setting setting = settingRepository.getReferenceById(1L);
            String tiktok_id= TikTokApi.getTiktokId(channelTiktok.getTiktok_id().trim());
            if (tiktok_id == null) {
                resp.put("channel_tiktok", "Cant filter tiktok_id from link");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            if (channelTikTokRepository.getCountTiktokId(tiktok_id.trim()) > 0) {
                resp.put("channel_tiktok", "This tiktok_id in process");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            Integer follower_count=TikTokApi.getFollowerCountLive(tiktok_id.trim().split("@")[1]);
            if(follower_count==-100){
                resp.put("channel_tiktok", "This account cannot be found");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            float priceorder = 0;
            int time = 0;
            priceorder = (channelTiktok.getFollower_order() / 1000F) * service.getRate() * ((float) (admins.get(0).getRate()) / 100) * ((float) (100 - admins.get(0).getDiscount()) / 100);
            if (priceorder > (float) admins.get(0).getBalance()) {
                resp.put("channel_tiktok", "Your balance not enough");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }

            ChannelTiktok channelTiktok_Add = new ChannelTiktok();
            channelTiktok_Add.setInsert_date(System.currentTimeMillis());
            channelTiktok_Add.setFollower_order(channelTiktok.getFollower_order());
            channelTiktok_Add.setFollower_start(follower_count);
            channelTiktok_Add.setFollower_total(0);
            channelTiktok_Add.setTiktok_id(tiktok_id.trim());
            channelTiktok_Add.setUser(admins.get(0).getUsername());
            channelTiktok_Add.setTime_update(0L);
            channelTiktok_Add.setTime_start(follower_count<0?0:System.currentTimeMillis());
            channelTiktok_Add.setMax_threads(follower_count<0?0:service.getThread());
            channelTiktok_Add.setNote("");
            channelTiktok_Add.setPrice(priceorder);
            channelTiktok_Add.setService(channelTiktok.getService());
            channelTiktok_Add.setValid(1);
            channelTikTokRepository.save(channelTiktok_Add);

            Float balance_update=adminRepository.updateBalanceFine(-priceorder,admins.get(0).getUsername().trim());
            Balance balance = new Balance();
            balance.setUser(admins.get(0).getUsername().trim());
            balance.setTime(System.currentTimeMillis());
            balance.setTotalblance(balance_update);
            balance.setBalance(-priceorder);
            balance.setService(channelTiktok.getService());
            balance.setNote("Order " + channelTiktok.getFollower_order() + " follower cho tiktok_id " + tiktok_id.trim());
            balanceRepository.save(balance);
            resp.put("channel_tiktok", "true");
            resp.put("balance", admins.get(0).getBalance());
            resp.put("price", priceorder);
            resp.put("time", time);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("channel_tiktok", "Fail check video!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
    }

    @GetMapping(path = "getOrderFollower", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getOrderFollower(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String user) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            List<OrderFollowerTikTokRunning> orderFollowerTikTokRunnings;
            if (user.length() == 0) {
                orderFollowerTikTokRunnings = channelTikTokRepository.getOrder();

            } else {
                orderFollowerTikTokRunnings = channelTikTokRepository.getOrder(user.trim());
            }

            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < orderFollowerTikTokRunnings.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("orderid", orderFollowerTikTokRunnings.get(i).getOrderId());
                obj.put("tiktok_id", orderFollowerTikTokRunnings.get(i).getTiktok_id());
                obj.put("follower_start", orderFollowerTikTokRunnings.get(i).getFollower_start());
                obj.put("max_threads", orderFollowerTikTokRunnings.get(i).getMax_threads());
                obj.put("insert_date", orderFollowerTikTokRunnings.get(i).getInsert_date());
                obj.put("time_start", orderFollowerTikTokRunnings.get(i).getTime_start());
                obj.put("total", orderFollowerTikTokRunnings.get(i).getTotal());
                obj.put("follower_order", orderFollowerTikTokRunnings.get(i).getFollower_order());
                obj.put("note", orderFollowerTikTokRunnings.get(i).getNote());
                obj.put("service", orderFollowerTikTokRunnings.get(i).getService());
                obj.put("geo", serviceRepository.getGeoByService(orderFollowerTikTokRunnings.get(i).getService()));
                obj.put("user", orderFollowerTikTokRunnings.get(i).getUser());
                obj.put("follower_total", orderFollowerTikTokRunnings.get(i).getFollower_total());
                obj.put("price", orderFollowerTikTokRunnings.get(i).getPrice());
                jsonArray.add(obj);
            }

            resp.put("total", orderFollowerTikTokRunnings.size());
            resp.put("channel_tiktok", jsonArray);
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
        try {
            List<String> followerBuff;
            List<ChannelTiktok> channelTiktokList = channelTikTokRepository.getAllOrderFollower();
            followerBuff = channelTikTokRepository.getTotalFollowerBuff();

            for (int i = 0; i < channelTiktokList.size(); i++) {
                int followerTotal = 0;
                for (int j = 0; j < followerBuff.size(); j++) {
                    if (channelTiktokList.get(i).getTiktok_id().equals(followerBuff.get(j).split(",")[0])) {
                        followerTotal = Integer.parseInt(followerBuff.get(j).split(",")[1]);
                    }
                }
                try {
                    channelTikTokRepository.updateFollowerByTiktokId(followerTotal, System.currentTimeMillis(), channelTiktokList.get(i).getTiktok_id());
                } catch (Exception e) {

                }
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");

            resp.put("total", channelTiktokList.size());
            resp.put("channel_tiktok", true);
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
                /*
                if(service.getChecktime()==0&&(System.currentTimeMillis()- videoViewHistories.get(i).getEnddate())/1000/60/60<8){
                    obj.put("videoview", "Hoàn thành chưa đủ 8h!");
                    return new ResponseEntity<String>(obj.toJSONString(), HttpStatus.OK);
                }
                 */
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


    String refundFollowerByOrderId(@RequestBody() ChannelTikTokHistory channelTikTokHistory) {
        try {
            SettingTiktok settingTiktok = settingTikTokRepository.getReferenceById(1L);
            Service service = serviceRepository.getInfoService(channelTikTokHistory.getService());

            Integer follower_count=TikTokApi.getFollowerCountLive(channelTikTokHistory.getTiktok_id().trim().split("@")[1]);
            if(follower_count<0){
                return "Không check được follower";
            }
            List<Admin> user = adminRepository.getAdminByUser(channelTikTokHistory.getUser());
            //Hoàn tiền những follower chưa buff
            int followerFix = channelTikTokHistory.getFollower_order() > channelTikTokHistory.getFollower_total() ? channelTikTokHistory.getFollower_total() : channelTikTokHistory.getFollower_order();
            int followerThan = followerFix + channelTikTokHistory.getFollower_start() - follower_count;
            if(followerThan<=0){
                if(service.getChecktime()==0){
                    channelTikTokHistory.setFollower_end(follower_count);
                    channelTikTokHistory.setTime_check_refill(System.currentTimeMillis());
                }
                channelTikTokHistoryRepository.save(channelTikTokHistory);
                return "Đủ follower | " +follower_count+"/"+(followerFix+channelTikTokHistory.getFollower_start());
            }
            if (followerThan > followerFix||followerFix-followerThan<10) {
                followerThan = followerFix;
            }
            float price_refund = ((followerThan) / (float) followerFix) * channelTikTokHistory.getPrice();
            if (channelTikTokHistory.getPrice() < price_refund) {
                price_refund = channelTikTokHistory.getPrice();
            }
            float pricebuffed = ( channelTikTokHistory.getPrice()- price_refund);
            channelTikTokHistory.setPrice(pricebuffed);
            channelTikTokHistory.setFollower_end(follower_count);
            channelTikTokHistory.setTime_check_refill(System.currentTimeMillis());
            channelTikTokHistory.setFollower_total(followerFix - followerThan);
            channelTikTokHistory.setRefund(1);
            if (channelTikTokHistory.getFollower_total()==0) {
                channelTikTokHistory.setCancel(1);
            } else {
                channelTikTokHistory.setCancel(2);
            }
            channelTikTokHistoryRepository.save(channelTikTokHistory);
            //hoàn tiền & add thong báo số dư

            Float balance_update=adminRepository.updateBalanceFine(price_refund,channelTikTokHistory.getUser().trim());
            Balance balance = new Balance();
            balance.setUser(user.get(0).getUsername().trim());
            balance.setTime(System.currentTimeMillis());
            balance.setTotalblance(balance_update);
            balance.setBalance(price_refund);
            balance.setService(channelTikTokHistory.getService());
            balance.setNote("Refund " + (followerThan) + "follower cho " + channelTikTokHistory.getTiktok_id());
            balanceRepository.save(balance);

            if(channelTikTokHistory.getPrice()==0){
                return "Đã hoàn 100%";
            }else{
                return "Đã hoàn phần thiếu";
            }
        } catch (Exception e) {
            return "Fail";
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

    @GetMapping(path = "getOrderFollowerHistory", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getOrderFollowerHistory(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String user) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            List<ChannelTikTokHistory> orderRunnings;
            if (user.length() == 0) {
                orderRunnings = channelTikTokHistoryRepository.getOrderFollowerTiktokHistories();
            } else {
                orderRunnings = channelTikTokHistoryRepository.getOrderFollowerTiktokHistories(user.trim());
            }
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < orderRunnings.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("orderid", orderRunnings.get(i).getOrderid());
                obj.put("tiktok_id", orderRunnings.get(i).getTiktok_id());
                obj.put("follower_start", orderRunnings.get(i).getFollower_start());
                obj.put("max_threads", orderRunnings.get(i).getMax_threads());
                obj.put("insert_date", orderRunnings.get(i).getInsert_date());
                obj.put("user", orderRunnings.get(i).getUser());
                obj.put("note", orderRunnings.get(i).getNote());
                obj.put("end_date", orderRunnings.get(i).getEnd_date());
                obj.put("cancel", orderRunnings.get(i).getCancel());
                obj.put("time_start", orderRunnings.get(i).getTime_start());
                obj.put("time_check_refill", orderRunnings.get(i).getTime_check_refill());
                obj.put("follower_end", orderRunnings.get(i).getFollower_end());
                obj.put("follower_total", orderRunnings.get(i).getFollower_total());
                obj.put("follower_order", orderRunnings.get(i).getFollower_order());
                obj.put("price", orderRunnings.get(i).getPrice());
                obj.put("service", orderRunnings.get(i).getService());
                jsonArray.add(obj);
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");

            resp.put("total", orderRunnings.size());
            resp.put("channel_tiktok", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "findorder", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> findorder(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String tiktok_id) {
        JSONObject resp = new JSONObject();
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            List<String> ordersArrInput = new ArrayList<>();
            ordersArrInput.addAll(Arrays.asList(tiktok_id.split(",")));
            List<ChannelTikTokHistory> orderRunnings;
            if(admins.get(0).getRole().equals("ROLE_ADMIN") || admins.get(0).getRole().equals("ROLE_SUPPORT")){
                    orderRunnings = channelTikTokHistoryRepository.getChannelTikTokHistoriesListById(ordersArrInput);
            }else{
                    orderRunnings = channelTikTokHistoryRepository.getChannelTikTokHistoriesListById(ordersArrInput,admins.get(0).getUsername().trim());
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
                if(admins.get(0).getRole().equals("ROLE_ADMIN") || admins.get(0).getRole().equals("ROLE_SUPPORT")) {
                    infoQ = channelTikTokHistoryRepository.getInfoSumOrderByTiktokId(orderRunnings.get(i).getTiktok_id(), orderRunnings.get(i).getOrderid());
                }else {
                    infoQ = channelTikTokHistoryRepository.getInfoSumOrderByTiktokId(orderRunnings.get(i).getTiktok_id(), orderRunnings.get(i).getOrderid(),admins.get(0).getUsername().trim());
                }
                if(infoQ!=null){
                    obj.put("info", infoQ);
                }else{
                    obj.put("info", "");
                }
                obj.put("orderid", orderRunnings.get(i).getOrderid());
                obj.put("tiktok_id", orderRunnings.get(i).getTiktok_id());
                obj.put("follower_start", orderRunnings.get(i).getFollower_start());
                obj.put("max_threads", orderRunnings.get(i).getMax_threads());
                obj.put("insert_date", orderRunnings.get(i).getInsert_date());
                obj.put("user", orderRunnings.get(i).getUser());
                obj.put("note", orderRunnings.get(i).getNote());
                obj.put("end_date", orderRunnings.get(i).getEnd_date());
                obj.put("cancel", orderRunnings.get(i).getCancel());
                obj.put("time_start", orderRunnings.get(i).getTime_start());
                obj.put("time_check_refill", orderRunnings.get(i).getTime_check_refill());
                obj.put("follower_end", orderRunnings.get(i).getFollower_end());
                obj.put("follower_total", orderRunnings.get(i).getFollower_total());
                obj.put("follower_order", orderRunnings.get(i).getFollower_order());
                obj.put("price", orderRunnings.get(i).getPrice());
                obj.put("service", orderRunnings.get(i).getService());
                jsonArray.add(obj);
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");
            resp.put("total", orderRunnings.size());
            resp.put("channel_tiktok", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }





    @DeleteMapping(path = "delete", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delete(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String tiktok_id, @RequestParam(defaultValue = "1") Integer cancel) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (tiktok_id.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "tiktok_id không được để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            String[] tiktokArr = tiktok_id.split(",");
            for (int i = 0; i < tiktokArr.length; i++) {

                Long enddate = System.currentTimeMillis();
                List<ChannelTiktok> channelTiktoks = channelTikTokRepository.getChannelTiktokByTiktokId(tiktokArr[i].trim());
                if(channelTiktoks.size()==0){
                    continue;
                }
                ChannelTikTokHistory channelTikTokHistory = new ChannelTikTokHistory();
                channelTikTokHistory.setOrderid(channelTiktoks.get(0).getOrderid());
                channelTikTokHistory.setInsert_date(channelTiktoks.get(0).getInsert_date());
                channelTikTokHistory.setService(channelTiktoks.get(0).getService());
                channelTikTokHistory.setTiktok_id(channelTiktoks.get(0).getTiktok_id());
                channelTikTokHistory.setFollower_start(channelTiktoks.get(0).getFollower_start());
                channelTikTokHistory.setFollower_order(channelTiktoks.get(0).getFollower_order());
                channelTikTokHistory.setMax_threads(channelTiktoks.get(0).getMax_threads());
                channelTikTokHistory.setNote(channelTiktoks.get(0).getNote());
                channelTikTokHistory.setTime_check_refill(0L);
                channelTikTokHistory.setTime_update(0L);
                channelTikTokHistory.setTime_start(channelTiktoks.get(0).getTime_start());
                channelTikTokHistory.setFollower_end(-1);
                //videoBuffhnew.setPrice(videoBuffh.get(0).getPrice());
                if (cancel == 1) {
                    Service service = serviceRepository.getInfoService(channelTiktoks.get(0).getService());
                    List<Admin> user = adminRepository.getAdminByUser(channelTiktoks.get(0).getUser());
                    //Hoàn tiền những view chưa buff
                    int viewbuff = channelTiktoks.get(0).getFollower_total();
                    int viewthan = channelTiktoks.get(0).getFollower_order() - (channelTiktoks.get(0).getFollower_total() > channelTiktoks.get(0).getFollower_order() ? channelTiktoks.get(0).getFollower_order() : channelTiktoks.get(0).getFollower_total());
                    //System.out.println(videoBuffh.get(0).getViewtotal() > videoBuffh.get(0).getVieworder() ? videoBuffh.get(0).getVieworder() : videoBuffh.get(0).getViewtotal());
                    float price_refund = (viewthan / (float) channelTiktoks.get(0).getFollower_order()) * channelTiktoks.get(0).getPrice();
                    //float pricebuffed=(videoBuffh.get(0).getViewtotal()/1000F)*service.getRate()*((float)(100-admins.get(0).getDiscount())/100);
                    float pricebuffed = (channelTiktoks.get(0).getPrice() - price_refund);
                    channelTikTokHistory.setPrice(pricebuffed);
                    if (viewbuff == 0) {
                        channelTikTokHistory.setCancel(1);
                    } else if (viewbuff >= channelTiktoks.get(0).getFollower_order()) {
                        channelTikTokHistory.setCancel(0);
                    } else {
                        channelTikTokHistory.setCancel(2);
                    }
                    //hoàn tiền & add thong báo số dư
                    if (viewthan > 0) {
                        Float balance_update=adminRepository.updateBalanceFine(price_refund,channelTiktoks.get(0).getUser().trim());
                        Balance balance = new Balance();
                        balance.setUser(user.get(0).getUsername().trim());
                        balance.setTime(System.currentTimeMillis());
                        balance.setTotalblance(balance_update);
                        balance.setBalance(price_refund);
                        balance.setService(channelTiktoks.get(0).getService());
                        balance.setNote("Refund " + (viewthan) + " follower cho " + channelTiktoks.get(0).getTiktok_id());
                        balanceRepository.save(balance);
                    }
                } else {
                    channelTikTokHistory.setPrice(channelTiktoks.get(0).getPrice());
                    channelTikTokHistory.setCancel(0);
                }
                channelTikTokHistory.setUser(channelTiktoks.get(0).getUser());
                channelTikTokHistory.setEnd_date(enddate);
                channelTikTokHistory.setFollower_total(channelTiktoks.get(0).getFollower_total());
                channelTikTokHistoryRepository.save(channelTikTokHistory);
                channelTikTokRepository.deleteOrderByOrderId(channelTiktoks.get(0).getOrderid());
            }
            resp.put("channel_tiktok", "");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            //dong loi
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


    @GetMapping(path = "updateOrderDoneByCron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateOrderDoneByCron() {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            SettingTiktok settingTiktok=settingTikTokRepository.getReferenceById(1L);
            List<ChannelTiktok> channelTiktoks = channelTikTokRepository.getOrderFullFollowerOrder();
            for (int i = 0; i < channelTiktoks.size(); i++) {

                Integer follower_count=TikTokApi.getFollowerCountLive(channelTiktoks.get(i).getTiktok_id().trim().split("@")[1]);
                if(follower_count<channelTiktoks.get(i).getFollower_order()+channelTiktoks.get(i).getFollower_order()*(settingTiktok.getMax_bonus()/100)+channelTiktoks.get(i).getFollower_start()){
                    continue;
                }

                Long enddate = System.currentTimeMillis();

                ChannelTikTokHistory channelTikTokHistory = new ChannelTikTokHistory();
                channelTikTokHistory.setOrderid(channelTiktoks.get(i).getOrderid());
                channelTikTokHistory.setTiktok_id(channelTiktoks.get(i).getTiktok_id());
                channelTikTokHistory.setPrice(channelTiktoks.get(i).getPrice());
                channelTikTokHistory.setFollower_order(channelTiktoks.get(i).getFollower_order());
                channelTikTokHistory.setFollower_start(channelTiktoks.get(i).getFollower_start());
                channelTikTokHistory.setFollower_total(channelTiktoks.get(i).getFollower_total());
                channelTikTokHistory.setFollower_end(-1);
                channelTikTokHistory.setInsert_date(channelTiktoks.get(i).getInsert_date());
                channelTikTokHistory.setInsert_date(enddate);
                channelTikTokHistory.setTime_check(0L);
                channelTikTokHistory.setTime_update(0L);
                channelTikTokHistory.setCancel(0);
                channelTikTokHistory.setCancel(channelTiktoks.get(i).getService());
                channelTikTokHistory.setMax_threads(channelTiktoks.get(i).getMax_threads());
                channelTikTokHistory.setNote(channelTiktoks.get(i).getNote());
                channelTikTokHistory.setUser(channelTiktoks.get(i).getUser());
                channelTikTokHistory.setTime_start(channelTiktoks.get(i).getTime_start());
                channelTikTokHistory.setRefund(0);
                channelTikTokHistory.setTime_check_refill(0L);
                channelTikTokHistoryRepository.save(channelTikTokHistory);
                try {
                    channelTikTokHistoryRepository.save(channelTikTokHistory);
                    channelTikTokRepository.deleteOrderByOrderId(channelTiktoks.get(i).getOrderid());
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
    ResponseEntity<String> update(@RequestHeader(defaultValue = "") String Authorization, @RequestBody ChannelTiktok channelTiktok) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            String[] tiktokIdArr = channelTiktok.getTiktok_id().split("\n");
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < tiktokIdArr.length; i++) {
                List<ChannelTiktok> channel = channelTikTokRepository.getChannelTiktokByTiktokId(tiktokIdArr[i].trim());
                float priceorder = 0;
                if (channelTiktok.getFollower_order() != channel.get(0).getFollower_order()) {
                    Service service = serviceRepository.getInfoService(channel.get(0).getService());
                    List<Admin> user = adminRepository.getAdminByUser(channelTiktok.getUser());
                    priceorder = ((channelTiktok.getFollower_order() - channel.get(0).getFollower_order())) * (channel.get(0).getPrice() / channel.get(0).getFollower_order());

                    if (priceorder > (float) user.get(0).getBalance()) {
                        resp.put("message", "Số tiền không đủ!!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    int timethan = channelTiktok.getFollower_order() - channel.get(0).getFollower_order();

                    //
                    if (timethan != 0) {
                        Float balance_update=adminRepository.updateBalanceFine(-priceorder,channelTiktok.getUser());
                        Balance balance = new Balance();
                        balance.setUser(channelTiktok.getUser());
                        balance.setTime(System.currentTimeMillis());
                        balance.setTotalblance(balance_update);
                        balance.setBalance(-priceorder);
                        balance.setService(channelTiktok.getService());
                        if (priceorder < 0) {
                            balance.setNote("Refund " + (-timethan) + " follower cho " + channelTiktok.getTiktok_id());
                        } else if (timethan != 0) {
                            balance.setNote("Order thêm " + timethan + " follower cho " + channelTiktok.getTiktok_id());
                        }
                        balanceRepository.save(balance);
                    }
                }
                channel.get(0).setMax_threads(channelTiktok.getMax_threads());
                if(channel.get(0).getTime_start()==0){
                    channel.get(0).setTime_start(System.currentTimeMillis());
                }
                channel.get(0).setFollower_order(channelTiktok.getFollower_order());
                channel.get(0).setNote(channelTiktok.getNote());
                channel.get(0).setPrice(channelTiktok.getPrice() + priceorder);
                channelTikTokRepository.save(channel.get(0));

                List<OrderFollowerTikTokRunning> orderRunnings = channelTikTokRepository.getOrderByTiktokId(tiktokIdArr[i].trim());
                JSONObject obj = new JSONObject();
                obj.put("orderid", orderRunnings.get(0).getOrderId());
                obj.put("tiktok_id", orderRunnings.get(0).getTiktok_id());
                obj.put("follower_start", orderRunnings.get(0).getFollower_start());
                obj.put("max_threads", orderRunnings.get(0).getMax_threads());
                obj.put("insert_date", orderRunnings.get(0).getInsert_date());
                obj.put("time_start", orderRunnings.get(0).getTime_start());
                obj.put("total", orderRunnings.get(0).getTotal());
                obj.put("follower_order", orderRunnings.get(0).getFollower_order());
                obj.put("note", orderRunnings.get(0).getNote());
                obj.put("service", orderRunnings.get(0).getService());
                obj.put("geo", serviceRepository.getGeoByService(orderRunnings.get(i).getService()));
                obj.put("user",orderRunnings.get(0).getUser());
                obj.put("follower_total", orderRunnings.get(0).getFollower_total());
                obj.put("price", orderRunnings.get(0).getPrice());

                jsonArray.add(obj);
            }
            resp.put("channel_tiktok", jsonArray);
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
            String[] OrderIdArr = orderid.split(",");
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < OrderIdArr.length; i++) {
                String status="No refunds";
                ChannelTikTokHistory channel = channelTikTokHistoryRepository.getChannelTikTokHistoriesById(Long.parseLong(OrderIdArr[i].trim()));
                Float price_old=channel.getPrice();
                Service service = serviceRepository.getInfoService(channel.getService());

                ChannelTikTokHistory channel_refil=channel;
                if(service.getRefill()==0){
                    status="DV không bảo hành";
                }else if(serviceRepository.checkGuarantee(channel.getEnd_date(),service.getMaxtimerefill())==0){
                    status="Quá hạn "+service.getMaxtimerefill()+" ngày";
                }else if(channel.getUser().equals("baohanh01@gmail.com")){
                    status="Đơn bảo hành";
                }else if(channelTikTokRepository.getCountTiktokIdIsRefill(channel.getTiktok_id().trim())>0){
                    status="Đang bảo hành";
                }else if(channelTikTokRepository.getCountTiktokIdNotPending(channel.getTiktok_id().trim())>0){
                    status="Đơn mới đang chạy";
                }else if(channel.getCancel()==1){
                    status="Được hủy trước đó";
                }else{
                    status=refundFollowerByOrderId(channel);
                    channel_refil= channelTikTokHistoryRepository.getChannelTikTokHistoriesById(Long.parseLong(OrderIdArr[i].trim()));
                }
                JSONObject obj = new JSONObject();
                String infoQ =channelTikTokHistoryRepository.getInfoSumOrderByTiktokId(channel_refil.getTiktok_id(),channel_refil.getOrderid());
                if(infoQ!=null){
                    obj.put("info", infoQ);
                }else{
                    obj.put("info", "");
                }
                obj.put("orderid", channel_refil.getOrderid());
                obj.put("tiktok_id", channel_refil.getTiktok_id());
                obj.put("follower_start", channel_refil.getFollower_start());
                obj.put("max_threads", channel_refil.getMax_threads());
                obj.put("insert_date", channel_refil.getInsert_date());
                obj.put("user", channel_refil.getUser());
                obj.put("note", channel_refil.getNote());
                obj.put("end_date", channel_refil.getEnd_date());
                obj.put("cancel", channel_refil.getCancel());
                obj.put("time_start", channel_refil.getTime_start());
                obj.put("time_check_refill", channel_refil.getTime_check_refill());
                obj.put("follower_end", channel_refil.getFollower_end());
                obj.put("follower_total",channel_refil.getFollower_total());
                obj.put("follower_order", channel_refil.getFollower_order());
                obj.put("price", channel_refil.getPrice());
                obj.put("service", channel_refil.getService());
                obj.put("status", status);
                jsonArray.add(obj);
            }
            resp.put("channel_tiktok", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(path = "updatethread", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updatethread(@RequestHeader(defaultValue = "") String Authorization, @RequestBody ChannelTiktok channelTiktok) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            String[] TitkokIdArr = channelTiktok.getTiktok_id().split("\n");
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < TitkokIdArr.length; i++) {
                List<ChannelTiktok> channel = channelTikTokRepository.getChannelTiktokByTiktokId(TitkokIdArr[i].trim());
                float priceorder = 0;
                channel.get(0).setMax_threads(channelTiktok.getMax_threads());
                if(channel.get(0).getTime_start()==0){
                    channel.get(0).setTime_start(System.currentTimeMillis());
                }
                channelTikTokRepository.save(channel.get(0));

                List<OrderFollowerTikTokRunning> orderRunnings = channelTikTokRepository.getOrderByTiktokId(TitkokIdArr[i].trim());
                JSONObject obj = new JSONObject();
                obj.put("orderid", orderRunnings.get(0).getOrderId());
                obj.put("tiktok_id", orderRunnings.get(0).getTiktok_id());
                obj.put("follower_start", orderRunnings.get(0).getFollower_start());
                obj.put("max_threads", orderRunnings.get(0).getMax_threads());
                obj.put("insert_date", orderRunnings.get(0).getInsert_date());
                obj.put("time_start", orderRunnings.get(0).getTime_start());
                obj.put("total", orderRunnings.get(0).getTotal());
                obj.put("follower_order", orderRunnings.get(0).getFollower_order());
                obj.put("note", orderRunnings.get(0).getNote());
                obj.put("service", orderRunnings.get(0).getService());
                obj.put("geo", serviceRepository.getGeoByService(orderRunnings.get(i).getService()));
                obj.put("user",orderRunnings.get(0).getUser());
                obj.put("follower_total", orderRunnings.get(0).getFollower_total());
                obj.put("price", orderRunnings.get(0).getPrice());


                jsonArray.add(obj);
            }
            resp.put("channel_tiktok", jsonArray);
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
                if(videoViewRepository.getCountOrderRunningByService(video.get(0).getService())==null?false:videoViewRepository.getCountOrderRunningByService(video.get(0).getService())>=(service.getGeo().equals("vn")?setting.getMaxorderbuffhvn():setting.getMaxorderbuffhus())*service.getMax()){
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

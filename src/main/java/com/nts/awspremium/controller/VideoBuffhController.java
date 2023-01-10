package com.nts.awspremium.controller;

import com.nts.awspremium.model.*;
import com.nts.awspremium.repositories.*;
import okhttp3.RequestBody;
import okhttp3.*;
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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/videobuffh")
public class VideoBuffhController {
    @Autowired
    private ChannelRepository channelRepository;
    @Autowired
    private VideoRepository videoRepository;
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private SettingRepository settingRepository;
    @Autowired
    OrderRunningRepository orderRunningRepository;
    @Autowired
    OrderBuffhRunningRepository orderBuffhRunningRepository;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private VideoBuffhRepository videoBuffhRepository;
    @Autowired
    private VideoBuffhHistoryRepository videoBuffhHistoryRepository;

    @PostMapping(value = "/orderbuffh", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> orderbuffh(@org.springframework.web.bind.annotation.RequestBody VideoBuffh videoBuffh, @RequestHeader(defaultValue = "") String Authorization) throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        try {
            List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
            if (Authorization.length() == 0 || admins.size() == 0) {
                resp.put("status", "fail");
                resp.put("message", "Token expired");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            if(videoBuffhRepository.getCountOrderByUser(admins.get(0).getUsername().trim())>=admins.get(0).getMaxorder()){
                resp.put("videobuffh", "Vượt giới hạn đơn!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            String videolist = videoBuffh.getVideoid().replace("\n", ",");
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
                resp.put("videobuffh","Fail check video!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            Iterator k = items.iterator();
            if(k.hasNext()==false){
                resp.put("videobuffh","Fail check video!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            while (k.hasNext()) {
                try {
                    long blance=admins.get(0).getBalance();
                    JSONObject video = (JSONObject) k.next();
                    JSONObject contentDetails = (JSONObject) video.get("contentDetails");
                    if(videoBuffhRepository.getCountVideoId(video.get("id").toString().trim())>0){
                        resp.put("videobuffh","Đơn đã tồn tại!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    if(Duration.parse(contentDetails.get("duration").toString()).getSeconds()<1800){
                        resp.put("videobuffh","time dưới 30p!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }


                    Setting setting=settingRepository.getReferenceById(1L);
                    //System.out.println((float)(videoBuffh.getTimebuff())/4000*setting.getPricerate()*((float)(100-admins.get(0).getDiscount())/100));
                    float priceorder=0;
                    int time=0;
                    if(admins.get(0).getVip()==1){
                        if(Duration.parse(contentDetails.get("duration").toString()).getSeconds()<3600){
                            time=30;
                        }else if(Duration.parse(contentDetails.get("duration").toString()).getSeconds()<7200){
                            time=60;
                        }else{
                            time=120;
                        }
                        priceorder=(float)(videoBuffh.getTimebuff())/4000*setting.getPricerate()*((float)(100-admins.get(0).getDiscount())/100);
                    }else{
                        if(Duration.parse(contentDetails.get("duration").toString()).getSeconds()<3600){
                            time=30;
                            priceorder=(float)(videoBuffh.getTimebuff())/4000*setting.getPricerate()*(float)1.1*((float)(100-admins.get(0).getDiscount())/100);
                        }else if(Duration.parse(contentDetails.get("duration").toString()).getSeconds()<7200){
                            time=60;
                            priceorder=(float)(videoBuffh.getTimebuff())/4000*setting.getPricerate()*(float)1.05*((float)(100-admins.get(0).getDiscount())/100);
                        }else{
                            time=120;
                            priceorder=(float)(videoBuffh.getTimebuff())/4000*setting.getPricerate()*((float)(100-admins.get(0).getDiscount())/100);
                        }
                    }
                    if(priceorder>(float)admins.get(0).getBalance()){
                        resp.put("videobuffh","Số tiền không đủ!!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    admins.get(0).setBalance((long) (blance-priceorder));

                    JSONObject snippet = (JSONObject) video.get("snippet");
                    JSONObject statistics = (JSONObject) video.get("statistics");
                    VideoBuffh videoBuffhnew= new VideoBuffh();
                    videoBuffhnew.setDuration(Duration.parse(contentDetails.get("duration").toString()).getSeconds());
                    videoBuffhnew.setOptionbuff(videoBuffh.getOptionbuff());
                    videoBuffhnew.setInsertdate(System.currentTimeMillis());
                    videoBuffhnew.setUser(videoBuffh.getUser());
                    videoBuffhnew.setOptionbuff(videoBuffh.getOptionbuff());
                    videoBuffhnew.setChannelid(snippet.get("channelId").toString());
                    videoBuffhnew.setVideotitle(snippet.get("title").toString());
                    videoBuffhnew.setTimebuff(videoBuffh.getTimebuff());
                    videoBuffhnew.setVideoid(video.get("id").toString());
                    videoBuffhnew.setEnabled(videoBuffh.getEnabled());
                    videoBuffhnew.setDirectrate(videoBuffh.getDirectrate());
                    videoBuffhnew.setHomerate(videoBuffh.getHomerate());
                    videoBuffhnew.setSuggestrate(videoBuffh.getSuggestrate());
                    videoBuffhnew.setSearchrate(videoBuffh.getSearchrate());
                    videoBuffhnew.setViewstart(Integer.parseInt(statistics.get("viewCount").toString()));
                    videoBuffhnew.setMaxthreads(videoBuffh.getMaxthreads());
                    videoBuffhnew.setNote(videoBuffh.getNote());
                    videoBuffhnew.setMobilerate(videoBuffh.getMobilerate());
                    videoBuffhnew.setLikerate(videoBuffh.getLikerate());

                    videoBuffhRepository.save(videoBuffhnew);
                    adminRepository.save(admins.get(0));

                    resp.put("videobuffh","true");
                    resp.put("price",priceorder);
                    resp.put("time",time);
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    //new Video(video.get("videoId").toString(), "channel_id", Duration.parse(video.get("duration").toString()).getSeconds(), video.get("title").toString());

                } catch (Exception e) {
                    resp.put("videobuffh", "error");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            }
            resp.put("videobuffh", "error");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch (Exception e){
            resp.put("videobuffh","Fail check video!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }

        /*
        try{
            List<OrderBuffhRunning> orderRunnings=orderBuffhRunningRepository.getOrderNewAdd(count);
            //System.out.println(timeBuff.get(0).split(",")[0]);
            //String a=orderRunnings.toString();
            JSONArray jsonArray= new JSONArray();
            //JSONObject jsonObject=new JSONObject().put("")
            //JSONObject jsonObject= (JSONObject) new JSONObject().put("Channelid",orderRunnings.get(0).toString());
            //jsonArray.add(orderRunnings);

            for(int i=0;i<orderRunnings.size();i++){
                JSONObject obj = new JSONObject();
                obj.put("videoid", orderRunnings.get(i).getVideoId());
                obj.put("videotitle", orderRunnings.get(i).getVideoTitle());
                obj.put("viewstart", orderRunnings.get(i).getViewStart());
                obj.put("maxthreads", orderRunnings.get(i).getMaxthreads());
                obj.put("insertdate", orderRunnings.get(i).getInsertDate());
                obj.put("total", orderRunnings.get(i).getTotal());
                obj.put("timebuff", orderRunnings.get(i).getTimeBuff());
                obj.put("note", orderRunnings.get(i).getNote());
                obj.put("duration", orderRunnings.get(i).getDuration());
                obj.put("optionbuff", orderRunnings.get(i).getOptionBuff());
                obj.put("mobilerate", orderRunnings.get(i).getMobileRate());
                obj.put("searchrate", orderRunnings.get(i).getSearchRate());
                obj.put("suggestrate", orderRunnings.get(i).getSuggestRate());
                obj.put("directrate", orderRunnings.get(i).getDirectRate());
                obj.put("homerate", orderRunnings.get(i).getHomeRate());
                obj.put("likerate", orderRunnings.get(i).getLikeRate());
                obj.put("commentrate", orderRunnings.get(i).getCommentRate());
                obj.put("user", orderRunnings.get(i).getUser());
                //obj.put("home_rate", orderRunnings.get(i).get());
                obj.put("enabled", orderRunnings.get(i).getEnabled());
                obj.put("timebuffhtotal", 0);
                obj.put("viewtotal", 0);
                obj.put("timebuffh24h", 0);
                obj.put("view24h",0);

                jsonArray.add(obj);
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");

            resp.put("total",orderRunnings.size());
            resp.put("videobuff",jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }

         */

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
                obj.put("duration",Duration.parse(contentDetails.get("duration").toString()).getSeconds() );
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
    ResponseEntity<String> checkduration() throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        List<String> listvideo=videoBuffhHistoryRepository.getOrderHistorythan5h();
        if(listvideo.size()==0){
            resp.put("status","true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
        String s_videoid="";
        for(int i=0;i<listvideo.size();i++){
            if(i==0){
                s_videoid=listvideo.get(i);
            }else {
                s_videoid=s_videoid+","+listvideo.get(i);
            }
        }
        System.out.println(s_videoid);
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
                System.out.println(Integer.parseInt(statistics.get("viewCount").toString()));
                videoBuffhHistoryRepository.updateviewend(Integer.parseInt(statistics.get("viewCount").toString()),video.get("id").toString());
                //jsonArray.add(obj);
            } catch (Exception e) {
                resp.put("status", e);
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }

        }
        resp.put("status","true");
        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
    }


    @GetMapping(path = "getorderbuffh",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getorderbuffh(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String user){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            List<OrderBuffhRunning> orderRunnings;
            List<String> timeBuff;
            List<String> timeBuff24h;
            if(user.length()==0){
                orderRunnings=orderBuffhRunningRepository.getOrder();
                //timeBuff =videoBuffhRepository.getTimeBuffVideo();
                //timeBuff24h =videoBuffhRepository.getTimeBuff24hVideo();
            }else{
                orderRunnings=orderBuffhRunningRepository.getOrder(user.trim());
                //timeBuff =videoBuffhRepository.getTimeBuffVideo(user.trim());
                //timeBuff24h =videoBuffhRepository.getTimeBuff24hVideo(user.trim());
            }

            //System.out.println(timeBuff.get(0).split(",")[0]);
            //String a=orderRunnings.toString();
            JSONArray jsonArray= new JSONArray();

            //JSONObject jsonObject=new JSONObject().put("")
            //JSONObject jsonObject= (JSONObject) new JSONObject().put("Channelid",orderRunnings.get(0).toString());
            //jsonArray.add(orderRunnings);

            for(int i=0;i<orderRunnings.size();i++){
                JSONObject obj = new JSONObject();
                obj.put("videoid", orderRunnings.get(i).getVideoId());
                obj.put("videotitle", orderRunnings.get(i).getVideoTitle());
                obj.put("viewstart", orderRunnings.get(i).getViewStart());
                obj.put("maxthreads", orderRunnings.get(i).getMaxthreads());
                obj.put("insertdate", orderRunnings.get(i).getInsertDate());
                obj.put("total", orderRunnings.get(i).getTotal());
                obj.put("timebuff", orderRunnings.get(i).getTimeBuff());
                obj.put("note", orderRunnings.get(i).getNote());
                obj.put("duration", orderRunnings.get(i).getDuration());
                obj.put("optionbuff", orderRunnings.get(i).getOptionBuff());
                obj.put("mobilerate", orderRunnings.get(i).getMobileRate());
                obj.put("searchrate", orderRunnings.get(i).getSearchRate());
                obj.put("suggestrate", orderRunnings.get(i).getSuggestRate());
                obj.put("directrate", orderRunnings.get(i).getDirectRate());
                obj.put("homerate", orderRunnings.get(i).getHomeRate());
                obj.put("likerate", orderRunnings.get(i).getLikeRate());
                obj.put("commentrate", orderRunnings.get(i).getCommentRate());
                obj.put("user", orderRunnings.get(i).getUser());
                obj.put("enabled", orderRunnings.get(i).getEnabled());
                /*
                for(int j=0;j<timeBuff.size();j++){
                    if(orderRunnings.get(i).getVideoId().equals(timeBuff.get(j).split(",")[0])){
                        obj.put("timebuffhtotal", timeBuff.get(j).split(",")[1]);
                        obj.put("viewtotal", timeBuff.get(j).split(",")[2]);
                        break;
                    }
                }
                for(int j=0;j<timeBuff24h.size();j++){
                    if(orderRunnings.get(i).getVideoId().equals(timeBuff24h.get(j).split(",")[0])){
                        obj.put("timebuffh24h", timeBuff24h.get(j).split(",")[1]);
                        obj.put("view24h", timeBuff24h.get(j).split(",")[2]);
                        break;
                    }
                }

                 */
                obj.put("timebuffh24h", orderRunnings.get(i).getTimeBuff24h());
                obj.put("view24h",orderRunnings.get(i).getView24h());
                obj.put("timebuffhtotal", orderRunnings.get(i).getTimeBuffTotal());
                obj.put("viewtotal",orderRunnings.get(i).getViewTotal());
                jsonArray.add(obj);
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");

            resp.put("total",orderRunnings.size());
            resp.put("videobuff",jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "updateorderbuffhcron",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateorderbuffhcron(){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try{
            List<String> timeBuff;
            List<String> timeBuff24h;
            List<VideoBuffh> videoBuffhList=videoBuffhRepository.getAllOrder();
            timeBuff =videoBuffhRepository.getTimeBuffVideo();
            timeBuff24h =videoBuffhRepository.getTimeBuff24hVideo();

            for(int i=0;i<videoBuffhList.size();i++){
                int timebufftotal=0;
                int timebuff24h=0;
                int viewtotal=0;
                int view24h=0;
                for(int j=0;j<timeBuff.size();j++){
                    if(videoBuffhList.get(i).getVideoid().equals(timeBuff.get(j).split(",")[0])){
                        timebufftotal=Integer.parseInt(timeBuff.get(j).split(",")[1]);
                        viewtotal=Integer.parseInt(timeBuff.get(j).split(",")[2]);
                    }
                }
                for(int j=0;j<timeBuff24h.size();j++){
                    if(videoBuffhList.get(i).getVideoid().equals(timeBuff24h.get(j).split(",")[0])){
                        timebuff24h=Integer.parseInt(timeBuff24h.get(j).split(",")[1]);
                        view24h=Integer.parseInt(timeBuff24h.get(j).split(",")[2]);
                    }
                }
                try{
                    videoBuffhRepository.updateTimeViewOrderByVideoId(timebufftotal,timebuff24h,viewtotal,view24h,System.currentTimeMillis(),videoBuffhList.get(i).getVideoid());
                }catch (Exception e){

                }
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");

            resp.put("total",videoBuffhList.size());
            resp.put("videobuff",true);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "getorderbuffhhistory",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getorderbuffhhistory(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String user){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            List<VideoBuffhHistory> orderRunnings;
            if(user.length()==0){
                orderRunnings=videoBuffhHistoryRepository.getVideoBuffhHistories();
            }else{
                orderRunnings=videoBuffhHistoryRepository.getVideoBuffhHistories(user.trim());
            }
            JSONArray jsonArray= new JSONArray();
            for(int i=0;i<orderRunnings.size();i++){
                JSONObject obj = new JSONObject();
                obj.put("videoid", orderRunnings.get(i).getVideoid());
                obj.put("videotitle", orderRunnings.get(i).getVideotitle());
                obj.put("viewstart", orderRunnings.get(i).getViewstart());
                obj.put("maxthreads", orderRunnings.get(i).getMaxthreads());
                obj.put("insertdate", orderRunnings.get(i).getInsertdate());
                obj.put("user", orderRunnings.get(i).getUser());
                obj.put("timebuff", orderRunnings.get(i).getTimebuff());
                obj.put("note", orderRunnings.get(i).getNote());
                obj.put("duration", orderRunnings.get(i).getDuration());
                obj.put("optionbuff", orderRunnings.get(i).getOptionbuff());
                obj.put("mobilerate", orderRunnings.get(i).getMobilerate());
                obj.put("searchrate", orderRunnings.get(i).getSearchrate());
                obj.put("suggestrate", orderRunnings.get(i).getSuggestrate());
                obj.put("directrate", orderRunnings.get(i).getDirectrate());
                obj.put("homerate", orderRunnings.get(i).getHomerate());
                obj.put("likerate", orderRunnings.get(i).getLikerate());
                obj.put("commentrate", orderRunnings.get(i).getCommentrate());
                obj.put("enddate", orderRunnings.get(i).getEnddate());
                obj.put("cancel", orderRunnings.get(i).getCancel());
                //obj.put("home_rate", orderRunnings.get(i).get());
                obj.put("viewend", orderRunnings.get(i).getViewend());
                obj.put("enabled", orderRunnings.get(i).getEnabled());
                obj.put("timebuffhtotal", orderRunnings.get(i).getTimebuffend());
                obj.put("viewtotal", orderRunnings.get(i).getViewbuffend());

                jsonArray.add(obj);
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");

            resp.put("total",orderRunnings.size());
            resp.put("videobuff",jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "getcounttimebufforder",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getcounttimebufforder(@RequestParam(defaultValue = "") String user) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            Integer counttimeorder=0;
            if(user.length()==0){
                counttimeorder=videoBuffhRepository.getCountTimeBuffOrder();
            }else{
                counttimeorder=videoBuffhRepository.getCountTimeBuffOrder(user.trim());
            }
            resp.put("totaltimeorder",counttimeorder);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "getcounttimebuffedorder",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getcounttimebuffedorder(@RequestParam(defaultValue = "") String user) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            Integer counttimeorder=0;
            if(user.length()==0){
                counttimeorder =videoBuffhRepository.getCountTimeBuffedOrder();
            }else{
                counttimeorder =videoBuffhRepository.getCountTimeBuffedOrder(user.trim());
            }
            resp.put("totaltimebuffedorder",counttimeorder);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "getorderfilterbuffh",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getorderfilterbuffh(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String key,@RequestParam(defaultValue = "") String user){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            List<OrderBuffhRunning> orderRunnings;
            if(user.length()==0){
                orderRunnings=orderBuffhRunningRepository.getOrderFilter("%"+key+"%");
            }else{
                orderRunnings=orderBuffhRunningRepository.getOrderFilter("%"+key+"%",user.trim());
            }

            //System.out.println(timeBuff.get(0).split(",")[0]);
            //String a=orderRunnings.toString();
            JSONArray jsonArray= new JSONArray();

            //JSONObject jsonObject=new JSONObject().put("")
            //JSONObject jsonObject= (JSONObject) new JSONObject().put("Channelid",orderRunnings.get(0).toString());
            //jsonArray.add(orderRunnings);

            for(int i=0;i<orderRunnings.size();i++){
                JSONObject obj = new JSONObject();
                obj.put("videoid", orderRunnings.get(i).getVideoId());
                obj.put("videotitle", orderRunnings.get(i).getVideoTitle());
                obj.put("viewstart", orderRunnings.get(i).getViewStart());
                obj.put("maxthreads", orderRunnings.get(i).getMaxthreads());
                obj.put("insertdate", orderRunnings.get(i).getInsertDate());
                obj.put("total", orderRunnings.get(i).getTotal());
                obj.put("timebuff", orderRunnings.get(i).getTimeBuff());
                obj.put("note", orderRunnings.get(i).getNote());
                obj.put("duration", orderRunnings.get(i).getDuration());
                obj.put("optionbuff", orderRunnings.get(i).getOptionBuff());
                obj.put("mobilerate", orderRunnings.get(i).getMobileRate());
                obj.put("searchrate", orderRunnings.get(i).getSearchRate());
                obj.put("suggestrate", orderRunnings.get(i).getSuggestRate());
                obj.put("directrate", orderRunnings.get(i).getDirectRate());
                obj.put("homerate", orderRunnings.get(i).getHomeRate());
                obj.put("likerate", orderRunnings.get(i).getLikeRate());
                obj.put("commentrate", orderRunnings.get(i).getCommentRate());
                obj.put("user", orderRunnings.get(i).getUser());
                obj.put("enabled", orderRunnings.get(i).getEnabled());

                String timeBuff =videoBuffhRepository.getTimeBuffByVideoId(orderRunnings.get(i).getVideoId().trim());
                if(timeBuff==null){
                    obj.put("timebuffhtotal", 0);
                    obj.put("viewtotal", 0);
                }else{
                    obj.put("timebuffhtotal", timeBuff.split(",")[1]);
                    obj.put("viewtotal", timeBuff.split(",")[2]);
                }


                String timeBuff24h =videoBuffhRepository.getTimeBuff24hByVideoId(orderRunnings.get(i).getVideoId().trim());

                if(timeBuff24h==null){
                    obj.put("timebuffh24h", 0);
                    obj.put("view24h",0);
                }else{
                    obj.put("timebuffh24h", timeBuff24h.split(",")[1]);
                    obj.put("view24h", timeBuff24h.split(",")[2]);
                }


                jsonArray.add(obj);
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");

            resp.put("total",orderRunnings.size());
            resp.put("videobuff",jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(path = "getorderbypercentbuffh",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getorderbypercentbuffh(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") Integer key,@RequestParam(defaultValue = "") String user){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            List<OrderBuffhRunning> orderRunnings;
            List<String> timeBuff;
            List<String> timeBuff24h;
            if(user.length()==0){
                orderRunnings=orderBuffhRunningRepository.getOrder();
                timeBuff =videoBuffhRepository.getTimeBuffVideo();
                timeBuff24h =videoBuffhRepository.getTimeBuff24hVideo();
            }else{
                orderRunnings=orderBuffhRunningRepository.getOrder(user.trim());
                timeBuff =videoBuffhRepository.getTimeBuffVideo(user.trim());
                timeBuff24h =videoBuffhRepository.getTimeBuff24hVideo(user.trim());
            }
            JSONArray jsonArray= new JSONArray();

            //JSONObject jsonObject=new JSONObject().put("")
            //JSONObject jsonObject= (JSONObject) new JSONObject().put("Channelid",orderRunnings.get(0).toString());
            //jsonArray.add(orderRunnings);

            for(int i=0;i<orderRunnings.size();i++){
                JSONObject obj = new JSONObject();
                Integer time=0;
                for(int j=0;j<timeBuff.size();j++){
                    if(orderRunnings.get(i).getVideoId().equals(timeBuff.get(j).split(",")[0])){
                        time= Integer.parseInt(timeBuff.get(j).split(",")[1]);
                        obj.put("timebuffhtotal", timeBuff.get(j).split(",")[1]);
                        obj.put("viewtotal", timeBuff.get(j).split(",")[2]);
                        break;
                    }
                }
                //System.out.println((long)(time/3600)/(long)orderRunnings.get(i).getTimeBuff());
                if(((time/36)/orderRunnings.get(i).getTimeBuff())<key)
                {
                    //System.out.println(((time/36)/orderRunnings.get(i).getTimeBuff()));
                    continue;
                }
                for(int j=0;j<timeBuff24h.size();j++){
                    if(orderRunnings.get(i).getVideoId().equals(timeBuff24h.get(j).split(",")[0])){
                        obj.put("timebuffh24h", timeBuff24h.get(j).split(",")[1]);
                        obj.put("view24h", timeBuff24h.get(j).split(",")[2]);
                        break;
                    }
                }

                obj.put("videoid", orderRunnings.get(i).getVideoId());
                obj.put("videotitle", orderRunnings.get(i).getVideoTitle());
                obj.put("viewstart", orderRunnings.get(i).getViewStart());
                obj.put("maxthreads", orderRunnings.get(i).getMaxthreads());
                obj.put("insertdate", orderRunnings.get(i).getInsertDate());
                obj.put("total", orderRunnings.get(i).getTotal());
                obj.put("timebuff", orderRunnings.get(i).getTimeBuff());
                obj.put("note", orderRunnings.get(i).getNote());
                obj.put("duration", orderRunnings.get(i).getDuration());
                obj.put("optionbuff", orderRunnings.get(i).getOptionBuff());
                obj.put("mobilerate", orderRunnings.get(i).getMobileRate());
                obj.put("searchrate", orderRunnings.get(i).getSearchRate());
                obj.put("suggestrate", orderRunnings.get(i).getSuggestRate());
                obj.put("directrate", orderRunnings.get(i).getDirectRate());
                obj.put("homerate", orderRunnings.get(i).getHomeRate());
                obj.put("likerate", orderRunnings.get(i).getLikeRate());
                obj.put("commentrate", orderRunnings.get(i).getCommentRate());
                obj.put("user", orderRunnings.get(i).getUser());
                obj.put("enabled", orderRunnings.get(i).getEnabled());


                jsonArray.add(obj);
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");

            resp.put("total",orderRunnings.size());
            resp.put("videobuff",jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(path = "getorderfilterbuffhhistory",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getorderfilterbuffhhistory(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String key,@RequestParam(defaultValue = "") String user){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            List<VideoBuffhHistory> orderRunnings;
            if(user.length()==0){
                orderRunnings=videoBuffhHistoryRepository.getOrderHistoryFilter("%"+key+"%");
            }else{
                orderRunnings=videoBuffhHistoryRepository.getOrderHistoryFilter("%"+key+"%",user.trim());
            }
            //System.out.println(timeBuff.get(0).split(",")[0]);
            //String a=orderRunnings.toString();
            JSONArray jsonArray= new JSONArray();

            //JSONObject jsonObject=new JSONObject().put("")
            //JSONObject jsonObject= (JSONObject) new JSONObject().put("Channelid",orderRunnings.get(0).toString());
            //jsonArray.add(orderRunnings);

            for(int i=0;i<orderRunnings.size();i++){
                JSONObject obj = new JSONObject();
                obj.put("videoid", orderRunnings.get(i).getVideoid());
                obj.put("videotitle", orderRunnings.get(i).getVideotitle());
                obj.put("viewstart", orderRunnings.get(i).getViewstart());
                obj.put("maxthreads", orderRunnings.get(i).getMaxthreads());
                obj.put("insertdate", orderRunnings.get(i).getInsertdate());
                //obj.put("total", orderRunnings.get(i).getTotal());
                obj.put("timebuff", orderRunnings.get(i).getTimebuff());
                obj.put("note", orderRunnings.get(i).getNote());
                obj.put("duration", orderRunnings.get(i).getDuration());
                obj.put("optionbuff", orderRunnings.get(i).getOptionbuff());
                obj.put("mobilerate", orderRunnings.get(i).getMobilerate());
                obj.put("searchrate", orderRunnings.get(i).getSearchrate());
                obj.put("suggestrate", orderRunnings.get(i).getSuggestrate());
                obj.put("directrate", orderRunnings.get(i).getDirectrate());
                obj.put("homerate", orderRunnings.get(i).getHomerate());
                obj.put("likerate", orderRunnings.get(i).getLikerate());
                obj.put("commentrate", orderRunnings.get(i).getCommentrate());
                obj.put("enddate", orderRunnings.get(i).getEnddate());
                obj.put("cancel", orderRunnings.get(i).getCancel());
                obj.put("viewend", orderRunnings.get(i).getViewend());
                obj.put("user", orderRunnings.get(i).getUser());
                obj.put("enabled", orderRunnings.get(i).getEnabled());
                obj.put("timebuffhtotal", orderRunnings.get(i).getTimebuffend());
                obj.put("viewtotal", orderRunnings.get(i).getViewbuffend());

                jsonArray.add(obj);
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");

            resp.put("total",orderRunnings.size());
            resp.put("videobuff",jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @DeleteMapping(path = "delete",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delete(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String videoid,@RequestParam(defaultValue = "1") Integer cancel){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        if(videoid.length()==0){
            resp.put("status","fail");
            resp.put("message", "channelid không được để trống");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            String[] videoidArr=videoid.split(",");
            for(int i=0;i<videoidArr.length;i++){
                Long enddate=System.currentTimeMillis();
                List<VideoBuffh> videoBuffh =videoBuffhRepository.getVideoBuffhById(videoidArr[i].trim());
                VideoBuffhHistory videoBuffhnew= new VideoBuffhHistory();
                videoBuffhnew.setDuration(videoBuffh.get(0).getDuration());
                videoBuffhnew.setOptionbuff(videoBuffh.get(0).getOptionbuff());
                videoBuffhnew.setInsertdate(videoBuffh.get(0).getInsertdate());
                videoBuffhnew.setOptionbuff(videoBuffh.get(0).getOptionbuff());
                videoBuffhnew.setChannelid(videoBuffh.get(0).getChannelid());
                videoBuffhnew.setVideotitle(videoBuffh.get(0).getVideotitle());
                videoBuffhnew.setTimebuff(videoBuffh.get(0).getTimebuff());
                videoBuffhnew.setVideoid(videoBuffh.get(0).getVideoid());
                videoBuffhnew.setEnabled(videoBuffh.get(0).getEnabled());
                videoBuffhnew.setDirectrate(videoBuffh.get(0).getDirectrate());
                videoBuffhnew.setHomerate(videoBuffh.get(0).getHomerate());
                videoBuffhnew.setSuggestrate(videoBuffh.get(0).getSuggestrate());
                videoBuffhnew.setSearchrate(videoBuffh.get(0).getSearchrate());
                videoBuffhnew.setViewstart(videoBuffh.get(0).getViewstart());
                videoBuffhnew.setMaxthreads(videoBuffh.get(0).getMaxthreads());
                videoBuffhnew.setNote(videoBuffh.get(0).getNote());
                videoBuffhnew.setMobilerate(videoBuffh.get(0).getMobilerate());
                videoBuffhnew.setLikerate(videoBuffh.get(0).getLikerate());
                videoBuffhnew.setCommentrate(videoBuffh.get(0).getCommentrate());
                if(cancel==1){
                    videoBuffhnew.setCancel(1);
                }else{
                    videoBuffhnew.setCancel(0);
                }
                videoBuffhnew.setUser(videoBuffh.get(0).getUser());
                videoBuffhnew.setEnddate(enddate);
                videoBuffhnew.setTimebuffend(videoBuffh.get(0).getTimebufftotal());
                videoBuffhnew.setViewbuffend(videoBuffh.get(0).getViewtotal());
                videoBuffhHistoryRepository.save(videoBuffhnew);
                videoBuffhRepository.deletevideoByVideoId(videoidArr[i].trim());
            }
            resp.put("videobuffh","");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(path = "updatechanneldonecron",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updatechanneldonecron(){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try{
            historyRepository.updateHistoryByAccount();
            List<VideoBuffh> videoBuffh =orderBuffhRunningRepository.getOrderFullBuffh();
            for(int i=0;i<videoBuffh.size();i++){
                Long enddate=System.currentTimeMillis();

                VideoBuffhHistory videoBuffhnew= new VideoBuffhHistory();
                videoBuffhnew.setDuration(videoBuffh.get(i).getDuration());
                videoBuffhnew.setOptionbuff(videoBuffh.get(i).getOptionbuff());
                videoBuffhnew.setInsertdate(videoBuffh.get(i).getInsertdate());
                videoBuffhnew.setOptionbuff(videoBuffh.get(i).getOptionbuff());
                videoBuffhnew.setChannelid(videoBuffh.get(i).getChannelid());
                videoBuffhnew.setVideotitle(videoBuffh.get(i).getVideotitle());
                videoBuffhnew.setTimebuff(videoBuffh.get(i).getTimebuff());
                videoBuffhnew.setVideoid(videoBuffh.get(i).getVideoid());
                videoBuffhnew.setEnabled(videoBuffh.get(i).getEnabled());
                videoBuffhnew.setDirectrate(videoBuffh.get(i).getDirectrate());
                videoBuffhnew.setHomerate(videoBuffh.get(i).getHomerate());
                videoBuffhnew.setSuggestrate(videoBuffh.get(i).getSuggestrate());
                videoBuffhnew.setSearchrate(videoBuffh.get(i).getSearchrate());
                videoBuffhnew.setViewstart(videoBuffh.get(i).getViewstart());
                videoBuffhnew.setMaxthreads(videoBuffh.get(i).getMaxthreads());
                videoBuffhnew.setNote(videoBuffh.get(0).getNote());
                videoBuffhnew.setMobilerate(videoBuffh.get(i).getMobilerate());
                videoBuffhnew.setLikerate(videoBuffh.get(i).getLikerate());
                videoBuffhnew.setCommentrate(videoBuffh.get(i).getCommentrate());
                videoBuffhnew.setCancel(0);
                videoBuffhnew.setUser(videoBuffh.get(i).getUser());
                videoBuffhnew.setEnddate(enddate);
                videoBuffhnew.setTimebuffend(videoBuffh.get(i).getTimebufftotal());
                videoBuffhnew.setViewbuffend(videoBuffh.get(i).getViewtotal());
                try{
                    videoBuffhHistoryRepository.save(videoBuffhnew);
                    videoBuffhRepository.DeleteVideoBuffhDone(videoBuffh.get(i).getVideoid().trim());
                }catch (Exception e){

                }
            }
            resp.put("status","true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping(path = "update",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> update(@RequestHeader(defaultValue = "") String Authorization,@org.springframework.web.bind.annotation.RequestBody VideoBuffh videoBuffh){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            String[] videoidIdArr=videoBuffh.getVideoid().split("\n");
            JSONArray jsonArray =new JSONArray();
            for(int i=0;i<videoidIdArr.length;i++){
                List<VideoBuffh> video=videoBuffhRepository.getVideoBuffhById(videoidIdArr[i].trim());

                Setting setting=settingRepository.getReferenceById(1L);
                //System.out.println((float)(videoBuffh.getTimebuff())/4000*setting.getPricerate()*((float)(100-admins.get(0).getDiscount())/100));
                float priceorder=0;
                if(video.get(0).getDuration()<3600){
                    priceorder=(float)(videoBuffh.getTimebuff()-video.get(0).getTimebuff())/4000*setting.getPricerate()*(float)1.1*((float)(100-admins.get(0).getDiscount())/100);
                }else if(video.get(0).getDuration()<7200){
                    priceorder=(float)(videoBuffh.getTimebuff()-video.get(0).getTimebuff())/4000*setting.getPricerate()*(float)1.05*((float)(100-admins.get(0).getDiscount())/100);
                }else{
                    priceorder=(float)(videoBuffh.getTimebuff()-video.get(0).getTimebuff())/4000*setting.getPricerate()*((float)(100-admins.get(0).getDiscount())/100);
                }
                if(priceorder>(float)admins.get(0).getBalance()){
                    resp.put("message","Số tiền không đủ!!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                admins.get(0).setBalance(admins.get(0).getBalance()-(long)priceorder);
                video.get(0).setMaxthreads(videoBuffh.getMaxthreads());
                video.get(0).setEnabled(videoBuffh.getEnabled());
                video.get(0).setOptionbuff(videoBuffh.getOptionbuff());
                video.get(0).setHomerate(videoBuffh.getHomerate());
                video.get(0).setSuggestrate(videoBuffh.getSuggestrate());
                video.get(0).setCommentrate(videoBuffh.getCommentrate());
                video.get(0).setDirectrate(videoBuffh.getDirectrate());
                video.get(0).setLikerate(videoBuffh.getLikerate());
                video.get(0).setTimebuff(videoBuffh.getTimebuff());
                video.get(0).setSearchrate(videoBuffh.getSearchrate());
                video.get(0).setNote(videoBuffh.getNote());
                videoBuffhRepository.save(video.get(0));
                List<OrderBuffhRunning> orderRunnings=orderBuffhRunningRepository.getVideoBuffhById(videoidIdArr[i].trim());
                JSONObject obj = new JSONObject();
                obj.put("videoid", orderRunnings.get(0).getVideoId());
                obj.put("videotitle", orderRunnings.get(0).getVideoTitle());
                obj.put("viewstart", orderRunnings.get(0).getViewStart());
                obj.put("maxthreads", orderRunnings.get(0).getMaxthreads());
                obj.put("insertdate", orderRunnings.get(0).getInsertDate());
                obj.put("total", orderRunnings.get(0).getTotal());
                obj.put("timebuff", orderRunnings.get(0).getTimeBuff());
                obj.put("note", orderRunnings.get(0).getNote());
                obj.put("duration", orderRunnings.get(0).getDuration());
                obj.put("optionbuff", orderRunnings.get(0).getOptionBuff());
                obj.put("mobilerate", orderRunnings.get(0).getMobileRate());
                obj.put("searchrate", orderRunnings.get(0).getSearchRate());
                obj.put("suggestrate", orderRunnings.get(0).getSuggestRate());
                obj.put("directrate", orderRunnings.get(0).getDirectRate());
                obj.put("homerate", orderRunnings.get(0).getHomeRate());
                obj.put("likerate", orderRunnings.get(0).getLikeRate());
                obj.put("commentrate", orderRunnings.get(0).getCommentRate());
                obj.put("enabled", orderRunnings.get(0).getEnabled());
                obj.put("user", orderRunnings.get(0).getUser());
                obj.put("timebuffhtotal", orderRunnings.get(0).getTimeBuffTotal());
                obj.put("viewtotal", orderRunnings.get(0).getViewTotal());
                obj.put("timebuffh24h", orderRunnings.get(0).getTimeBuff24h());
                obj.put("view24h",orderRunnings.get(0).getView24h());

                jsonArray.add(obj);
            }
            resp.put("videobuff",jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
}

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
    OrderRunningRepository orderRunningRepository;
    @Autowired
    OrderBuffhRunningRepository orderBuffhRunningRepository;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private VideoBuffhRepository videoBuffhRepository;
    @GetMapping("/getvideo")
    ResponseEntity<ResponseObject> getvideo(@RequestParam(defaultValue = "") String channelid) throws IOException {
          OkHttpClient client = new OkHttpClient().newBuilder()
                  .build();
          MediaType mediaType = MediaType.parse("application/json");
          RequestBody body = RequestBody.create(mediaType, "");
          Request request = new Request.Builder()
                  .url("https://backend.simplesolution.co/UserDataApi/channelVideosNonKey/"+channelid)
                  .method("GET", body)
                  .addHeader("Content-Type", "application/json")
                  .build();

          Response response = client.newCall(request).execute();
          String resultJson=response.body().string();
          return ResponseEntity.status(HttpStatus.OK).body(
                  new ResponseObject("true","Token không hợp lệ!",resultJson)
          );
    }

    @GetMapping("/test")
    ResponseEntity<String> test(@RequestParam(defaultValue = "") String channelid) throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        OkHttpClient clientchannel = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        Request requestchannel = null;

        requestchannel = new Request.Builder().url("https://www.googleapis.com/youtube/v3/channels?part=snippet&id="+channelid.trim()+"&key=AIzaSyClOKa8qUz3MJD1RKBsjlIDR5KstE2NmMY").get().build();

        Response responsechannel = clientchannel.newCall(requestchannel).execute();

        String resultJsonChannel = responsechannel.body().string();

        Object objchannel = new JSONParser().parse(resultJsonChannel);

        JSONObject jsonObjectChannel = (JSONObject) objchannel;
        JSONArray itemsChannel= (JSONArray) jsonObjectChannel.get("items");
        JSONObject itemChannel=(JSONObject) itemsChannel.get(0);
        JSONObject snippetObj=(JSONObject) itemChannel.get("snippet");
        /*
        JSONArray jsonArrayChannel = (JSONArray) jsonObjectChannel.get("items");
        for (Object item : jsonArrayChannel) {
            JSONObject channel = (JSONObject) item;
            JSONObject id = (JSONObject) channel.get("snippet");
        }


         */
        resp.put("status", "fail");
        resp.put("message", snippetObj.get("title"));
        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
    }



    @PostMapping(value = "/ordermanual", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> ordermanual(@org.springframework.web.bind.annotation.RequestBody ChannelOrder channelOrder, @RequestHeader(defaultValue = "") String Authorization) throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        String list_video = channelOrder.getList_video();
        if (list_video.length() == 0) {
            String[] channellist = channelOrder.getChannel_id().split("\n");
            Integer index = 0;
            while (index < channellist.length) {
                List<Channel> channels = channelRepository.getChannelById(channellist[index]);
                String pageToken = "";
                if (channels.size() == 0) {

                    OkHttpClient clientchannel = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                    Request requestchannel = null;

                    requestchannel = new Request.Builder().url("https://www.googleapis.com/youtube/v3/channels?part=snippet&id="+channellist[index].trim()+"&key=AIzaSyClOKa8qUz3MJD1RKBsjlIDR5KstE2NmMY").get().build();

                    Response responsechannel = clientchannel.newCall(requestchannel).execute();

                    String resultJsonChannel = responsechannel.body().string();

                    Object objchannel = new JSONParser().parse(resultJsonChannel);

                    JSONObject jsonObjectChannel = (JSONObject) objchannel;
                    JSONArray itemsChannel= (JSONArray) jsonObjectChannel.get("items");
                    JSONObject itemChannel=(JSONObject) itemsChannel.get(0);
                    JSONObject snippetObj=(JSONObject) itemChannel.get("snippet");


                    Channel channel = new Channel();
                    channel.setChannelid(channellist[index].trim());
                    channel.setTitle(snippetObj.get("title").toString());
                    channel.setEnabled(channelOrder.getEnabled());
                    channel.setDirectrate(channelOrder.getDirect_rate());
                    channel.setHomerate(channelOrder.getHome_rate());
                    channel.setSearchrate(channelOrder.getSearch_rate());
                    channel.setSuggestrate(channelOrder.getSuggest_rate());
                    channel.setPremiumrate(100);
                    channel.setMaxthreads(channelOrder.getMax_thread());
                    channel.setMobilerate(100);
                    channel.setViewpercent(channelOrder.getView_percent());
                    channel.setInsertdate(System.currentTimeMillis());
                    channelRepository.save(channel);


                    while (true) {

                        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                        Request request = null;

                        request = new Request.Builder().url("https://www.googleapis.com/youtube/v3/search?key=AIzaSyClOKa8qUz3MJD1RKBsjlIDR5KstE2NmMY&channelId=" + channellist[index].trim() + "&part=id&maxResults=50&kind=video" +
                                (pageToken.length() == 0 ? "" : "&pageToken=" + pageToken)).get().build();

                        Response response = client.newCall(request).execute();

                        String resultJson = response.body().string();

                        Object obj = new JSONParser().parse(resultJson);
                        JSONObject jsonObject = (JSONObject) obj;
                        pageToken = (String) jsonObject.get("nextPageToken");
                        if (pageToken == null) {
                            pageToken = "";
                        }
                        JSONArray jsonArray = (JSONArray) jsonObject.get("items");
                        list_video="";
                        for (Object item : jsonArray) {
                            JSONObject video = (JSONObject) item;
                            JSONObject id = (JSONObject) video.get("id");
                            list_video = (list_video.length() == 0 ? "" : list_video + ",") + id.get("videoId");
                        }
                        //videooooooooooooooooo
                        OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                        Request request1 = null;

                        request1 = new Request.Builder().url("https://backend.simplesolution.co/UserDataApi/channelVideosInfoNonKey?list_video=" + list_video).get().build();

                        Response response1 = client1.newCall(request1).execute();

                        String resultJson1 = response1.body().string();

                        Object obj1 = new JSONParser().parse(resultJson1);

                        JSONObject jsonObject1 = (JSONObject) obj1;
                        JSONArray items = (JSONArray) jsonObject1.get("videos");
                        Iterator i = items.iterator();

                        while (i.hasNext()) {
                            try {
                                JSONObject video = (JSONObject) i.next();
                                if(Duration.parse(video.get("duration").toString()).getSeconds()<600){
                                    continue;
                                }
                                Video videos = new Video();
                                videos.setVideoid(video.get("videoId").toString());
                                videos.setChannelid(channellist[index].trim());
                                videos.setDuration(Duration.parse(video.get("duration").toString()).getSeconds());
                                videos.setTitle(video.get("title").toString());
                                //new Video(video.get("videoId").toString(), "channel_id", Duration.parse(video.get("duration").toString()).getSeconds(), video.get("title").toString());
                                videoRepository.save(videos);
                            } catch (Exception e) {
                                resp.put("status", "Fail");
                                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                            }
                        }
                        if (pageToken.length() == 0) {
                            break;
                        }

                    }
                }
                index++;
            }
        }else{
                List<Channel> channels = channelRepository.getChannelById(channelOrder.getChannel_id());
                if (channels.size() == 0) {

                    OkHttpClient clientchannel = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                    Request requestchannel = null;

                    requestchannel = new Request.Builder().url("https://www.googleapis.com/youtube/v3/channels?part=snippet&id="+channelOrder.getChannel_id().trim()+"&key=AIzaSyClOKa8qUz3MJD1RKBsjlIDR5KstE2NmMY").get().build();

                    Response responsechannel = clientchannel.newCall(requestchannel).execute();

                    String resultJsonChannel = responsechannel.body().string();

                    Object objchannel = new JSONParser().parse(resultJsonChannel);

                    JSONObject jsonObjectChannel = (JSONObject) objchannel;
                    JSONArray itemsChannel= (JSONArray) jsonObjectChannel.get("items");
                    JSONObject itemChannel=(JSONObject) itemsChannel.get(0);
                    JSONObject snippetObj=(JSONObject) itemChannel.get("snippet");


                    Channel channel = new Channel();
                    channel.setChannelid(channelOrder.getChannel_id());
                    //channel.setTitle("title");
                    channel.setTitle(snippetObj.get("title").toString());
                    channel.setEnabled(channelOrder.getEnabled());
                    channel.setDirectrate(channelOrder.getDirect_rate());
                    channel.setHomerate(channelOrder.getHome_rate());
                    channel.setSearchrate(channelOrder.getSearch_rate());
                    channel.setSuggestrate(channelOrder.getSuggest_rate());
                    channel.setPremiumrate(100);
                    channel.setMaxthreads(channelOrder.getMax_thread());
                    channel.setMobilerate(100);
                    channel.setViewpercent(channelOrder.getView_percent());
                    channel.setInsertdate(System.currentTimeMillis());
                    channelRepository.save(channel);
                }

                //VIDEOOOOOOOOOOOOOOO
                OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                Request request1 = null;

                request1 = new Request.Builder().url("https://backend.simplesolution.co/UserDataApi/channelVideosInfoNonKey?list_video=" + list_video).get().build();

                Response response1 = client1.newCall(request1).execute();

                String resultJson1 = response1.body().string();

                Object obj1 = new JSONParser().parse(resultJson1);

                JSONObject jsonObject1 = (JSONObject) obj1;
                JSONArray items = (JSONArray) jsonObject1.get("videos");
                Iterator i = items.iterator();

                while (i.hasNext()) {
                    try {

                        JSONObject video = (JSONObject) i.next();
                        if(Duration.parse(video.get("duration").toString()).getSeconds()<600){
                            continue;
                        }
                        Video videos = new Video();
                        videos.setVideoid(video.get("videoId").toString());
                        videos.setChannelid(channelOrder.getChannel_id());
                        videos.setDuration(Duration.parse(video.get("duration").toString()).getSeconds());
                        videos.setTitle(video.get("title").toString());
                        //new Video(video.get("videoId").toString(), "channel_id", Duration.parse(video.get("duration").toString()).getSeconds(), video.get("title").toString());
                        videoRepository.save(videos);
                    } catch (Exception e) {
                        resp.put("status", "Fail");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                }
                resp.put("channel", "true");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        resp.put("status", "fail");
        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
    }

    @PostMapping(value = "/orderbuffh", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> orderbuffh(@org.springframework.web.bind.annotation.RequestBody VideoBuffh videoBuffh, @RequestHeader(defaultValue = "") String Authorization) throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }

        String videolist = videoBuffh.getVideoid().replace("\n",",");
        //VIDEOOOOOOOOOOOOOOO
        int count = StringUtils.countOccurrencesOf(videolist, ",")+1;
        OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        Request request1 = null;

        request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=AIzaSyClOKa8qUz3MJD1RKBsjlIDR5KstE2NmMY&fields=items(id,snippet(title,channelId),statistics(viewCount),contentDetails(duration))&part=snippet,statistics,contentDetails&id=" + videolist).get().build();

        Response response1 = client1.newCall(request1).execute();

        String resultJson1 = response1.body().string();

        Object obj1 = new JSONParser().parse(resultJson1);

        JSONObject jsonObject1 = (JSONObject) obj1;
        JSONArray items = (JSONArray) jsonObject1.get("items");
        Iterator k = items.iterator();

        while (k.hasNext()) {
            try {
                JSONObject video = (JSONObject) k.next();
                JSONObject contentDetails = (JSONObject) video.get("contentDetails");
                if(Duration.parse(contentDetails.get("duration").toString()).getSeconds()<600){
                    continue;
                }
                System.out.println(video);
                JSONObject snippet = (JSONObject) video.get("snippet");
                JSONObject statistics = (JSONObject) video.get("statistics");
                VideoBuffh videoBuffhnew= new VideoBuffh();
                videoBuffhnew.setDuration(Duration.parse(contentDetails.get("duration").toString()).getSeconds());
                videoBuffhnew.setOptionbuff(videoBuffh.getOptionbuff());
                videoBuffhnew.setInsertdate(System.currentTimeMillis());
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
                videoBuffhnew.setCommentrate(videoBuffh.getCommentrate());
                videoBuffhRepository.save(videoBuffhnew);
                //new Video(video.get("videoId").toString(), "channel_id", Duration.parse(video.get("duration").toString()).getSeconds(), video.get("title").toString());

            } catch (Exception e) {
                resp.put("status", e);
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        }
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

    }


    @GetMapping(value = "/checkduration", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> checkduration(@RequestParam(defaultValue = "") String listvideo) throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        System.out.println(listvideo);
        //VIDEOOOOOOOOOOOOOOO
        OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        Request request1 = null;

        request1 = new Request.Builder().url("https://www.googleapis.com/youtube/v3/videos?key=AIzaSyA7km25RCx-pTfOkX4fexR_wrtJoEachGw&fields=items(id,contentDetails(duration))&part=contentDetails&id=" + listvideo).get().build();

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

    @PostMapping(value = "/orderbufflike", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> orderbufflike(@org.springframework.web.bind.annotation.RequestBody ChannelOrder channelOrder, @RequestHeader(defaultValue = "") String Authorization) throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        String list_video = channelOrder.getList_video();
        if (list_video.length() == 0) {
            String[] channellist = channelOrder.getChannel_id().split("\n");
            Integer index = 0;
            while (index < channellist.length) {
                List<Channel> channels = channelRepository.getChannelById(channellist[index]);
                String pageToken = "";
                if (channels.size() == 0) {

                    OkHttpClient clientchannel = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                    Request requestchannel = null;

                    requestchannel = new Request.Builder().url("https://www.googleapis.com/youtube/v3/channels?part=snippet&id="+channellist[index].trim()+"&key=AIzaSyClOKa8qUz3MJD1RKBsjlIDR5KstE2NmMY").get().build();

                    Response responsechannel = clientchannel.newCall(requestchannel).execute();

                    String resultJsonChannel = responsechannel.body().string();

                    Object objchannel = new JSONParser().parse(resultJsonChannel);

                    JSONObject jsonObjectChannel = (JSONObject) objchannel;
                    JSONArray itemsChannel= (JSONArray) jsonObjectChannel.get("items");
                    JSONObject itemChannel=(JSONObject) itemsChannel.get(0);
                    JSONObject snippetObj=(JSONObject) itemChannel.get("snippet");


                    Channel channel = new Channel();
                    channel.setChannelid(channellist[index].trim());
                    channel.setTitle(snippetObj.get("title").toString());
                    channel.setEnabled(channelOrder.getEnabled());
                    channel.setDirectrate(channelOrder.getDirect_rate());
                    channel.setHomerate(channelOrder.getHome_rate());
                    channel.setSearchrate(channelOrder.getSearch_rate());
                    channel.setSuggestrate(channelOrder.getSuggest_rate());
                    channel.setPremiumrate(100);
                    channel.setMaxthreads(channelOrder.getMax_thread());
                    channel.setMobilerate(100);
                    channel.setViewpercent(channelOrder.getView_percent());
                    channel.setInsertdate(System.currentTimeMillis());
                    channelRepository.save(channel);


                    while (true) {

                        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                        Request request = null;

                        request = new Request.Builder().url("https://www.googleapis.com/youtube/v3/search?key=AIzaSyClOKa8qUz3MJD1RKBsjlIDR5KstE2NmMY&channelId=" + channellist[index].trim() + "&part=id&maxResults=50&kind=video" +
                                (pageToken.length() == 0 ? "" : "&pageToken=" + pageToken)).get().build();

                        Response response = client.newCall(request).execute();

                        String resultJson = response.body().string();

                        Object obj = new JSONParser().parse(resultJson);
                        JSONObject jsonObject = (JSONObject) obj;
                        pageToken = (String) jsonObject.get("nextPageToken");
                        if (pageToken == null) {
                            pageToken = "";
                        }
                        JSONArray jsonArray = (JSONArray) jsonObject.get("items");
                        list_video="";
                        for (Object item : jsonArray) {
                            JSONObject video = (JSONObject) item;
                            JSONObject id = (JSONObject) video.get("id");
                            list_video = (list_video.length() == 0 ? "" : list_video + ",") + id.get("videoId");
                        }
                        //videooooooooooooooooo
                        OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                        Request request1 = null;

                        request1 = new Request.Builder().url("https://backend.simplesolution.co/UserDataApi/channelVideosInfoNonKey?list_video=" + list_video).get().build();

                        Response response1 = client1.newCall(request1).execute();

                        String resultJson1 = response1.body().string();

                        Object obj1 = new JSONParser().parse(resultJson1);

                        JSONObject jsonObject1 = (JSONObject) obj1;
                        JSONArray items = (JSONArray) jsonObject1.get("videos");
                        Iterator i = items.iterator();

                        while (i.hasNext()) {
                            try {
                                JSONObject video = (JSONObject) i.next();
                                if(Duration.parse(video.get("duration").toString()).getSeconds()<60){
                                    continue;
                                }
                                Video videos = new Video();
                                videos.setVideoid(video.get("videoId").toString());
                                videos.setChannelid(channellist[index].trim());
                                videos.setDuration(Duration.parse(video.get("duration").toString()).getSeconds());
                                videos.setTitle(video.get("title").toString());
                                //new Video(video.get("videoId").toString(), "channel_id", Duration.parse(video.get("duration").toString()).getSeconds(), video.get("title").toString());
                                videoRepository.save(videos);
                            } catch (Exception e) {
                                resp.put("status", "Fail");
                                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                            }
                        }
                        if (pageToken.length() == 0) {
                            break;
                        }

                    }
                }
                index++;
            }
        }else{
            List<Channel> channels = channelRepository.getChannelById(channelOrder.getChannel_id());
            if (channels.size() == 0) {

                OkHttpClient clientchannel = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                Request requestchannel = null;

                requestchannel = new Request.Builder().url("https://www.googleapis.com/youtube/v3/channels?part=snippet&id="+channelOrder.getChannel_id().trim()+"&key=AIzaSyClOKa8qUz3MJD1RKBsjlIDR5KstE2NmMY").get().build();

                Response responsechannel = clientchannel.newCall(requestchannel).execute();

                String resultJsonChannel = responsechannel.body().string();

                Object objchannel = new JSONParser().parse(resultJsonChannel);

                JSONObject jsonObjectChannel = (JSONObject) objchannel;
                JSONArray itemsChannel= (JSONArray) jsonObjectChannel.get("items");
                JSONObject itemChannel=(JSONObject) itemsChannel.get(0);
                JSONObject snippetObj=(JSONObject) itemChannel.get("snippet");


                Channel channel = new Channel();
                channel.setChannelid(channelOrder.getChannel_id());
                //channel.setTitle("title");
                channel.setTitle(snippetObj.get("title").toString());
                channel.setEnabled(channelOrder.getEnabled());
                channel.setDirectrate(channelOrder.getDirect_rate());
                channel.setHomerate(channelOrder.getHome_rate());
                channel.setSearchrate(channelOrder.getSearch_rate());
                channel.setSuggestrate(channelOrder.getSuggest_rate());
                channel.setPremiumrate(100);
                channel.setMaxthreads(channelOrder.getMax_thread());
                channel.setMobilerate(100);
                channel.setViewpercent(channelOrder.getView_percent());
                channel.setInsertdate(System.currentTimeMillis());
                channelRepository.save(channel);
            }

            //VIDEOOOOOOOOOOOOOOO
            OkHttpClient client1 = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

            Request request1 = null;

            request1 = new Request.Builder().url("https://backend.simplesolution.co/UserDataApi/channelVideosInfoNonKey?list_video=" + list_video).get().build();

            Response response1 = client1.newCall(request1).execute();

            String resultJson1 = response1.body().string();

            Object obj1 = new JSONParser().parse(resultJson1);

            JSONObject jsonObject1 = (JSONObject) obj1;
            JSONArray items = (JSONArray) jsonObject1.get("videos");
            Iterator i = items.iterator();

            while (i.hasNext()) {
                try {
                    JSONObject video = (JSONObject) i.next();
                    if(Duration.parse(video.get("duration").toString()).getSeconds()<600){
                        continue;
                    }
                    Video videos = new Video();
                    videos.setVideoid(video.get("videoId").toString());
                    videos.setChannelid(channelOrder.getChannel_id());
                    videos.setDuration(Duration.parse(video.get("duration").toString()).getSeconds());
                    videos.setTitle(video.get("title").toString());
                    //new Video(video.get("videoId").toString(), "channel_id", Duration.parse(video.get("duration").toString()).getSeconds(), video.get("title").toString());
                    videoRepository.save(videos);
                } catch (Exception e) {
                    resp.put("status", "Fail");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            }
            resp.put("channel", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
        resp.put("status", "fail");
        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
    }

    @GetMapping(path = "orderrunning",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> orderrunning(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            List<OrderRunning> orderRunnings=orderRunningRepository.getOrderRunning();
            //String a=orderRunnings.toString();
            JSONArray jsonArray= new JSONArray();

            //JSONObject jsonObject=new JSONObject().put("")
            //JSONObject jsonObject= (JSONObject) new JSONObject().put("Channelid",orderRunnings.get(0).toString());
            //jsonArray.add(orderRunnings);

            for(int i=0;i<orderRunnings.size();i++){
                JSONObject obj = new JSONObject();
                obj.put("channel_id", orderRunnings.get(i).getChannelId());
                obj.put("channel_title", orderRunnings.get(i).getTitle());
                obj.put("total", orderRunnings.get(i).getTotal());
                obj.put("max_threads", orderRunnings.get(i).getMaxthreads());
                jsonArray.add(obj);
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");


            resp.put("statics",jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(path = "getorder",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getorder(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            List<OrderRunning> orderRunnings=orderRunningRepository.getOrder();
            List<String> timeBuff =channelRepository.getTimeBuffChannel();
            List<String> timeBuff24h =channelRepository.getTimeBuff24hChannel();
            //System.out.println(timeBuff.get(0).split(",")[0]);
            //String a=orderRunnings.toString();
            JSONArray jsonArray= new JSONArray();

            //JSONObject jsonObject=new JSONObject().put("")
            //JSONObject jsonObject= (JSONObject) new JSONObject().put("Channelid",orderRunnings.get(0).toString());
            //jsonArray.add(orderRunnings);

            for(int i=0;i<orderRunnings.size();i++){
                JSONObject obj = new JSONObject();
                obj.put("channel_id", orderRunnings.get(i).getChannelId());
                obj.put("channel_title", orderRunnings.get(i).getTitle());
                obj.put("total", orderRunnings.get(i).getTotal());
                obj.put("max_threads", orderRunnings.get(i).getMaxthreads());
                obj.put("insert_date", orderRunnings.get(i).getInsertdate());
                obj.put("view_percent", orderRunnings.get(i).getViewpercent());
                //obj.put("home_rate", orderRunnings.get(i).get());
                obj.put("enabled", orderRunnings.get(i).getEnabled());
                for(int j=0;j<timeBuff.size();j++){
                    if(orderRunnings.get(i).getChannelId().equals(timeBuff.get(j).split(",")[0])){
                        obj.put("view_need", timeBuff.get(j).split(",")[1]);
                        obj.put("view_total", timeBuff.get(j).split(",")[2]);
                        break;
                    }
                }
                for(int j=0;j<timeBuff24h.size();j++){
                    if(orderRunnings.get(i).getChannelId().equals(timeBuff24h.get(j).split(",")[0])){
                        obj.put("like_rate", timeBuff24h.get(j).split(",")[1]);
                        obj.put("comment_rate", timeBuff24h.get(j).split(",")[2]);
                        break;
                    }
                }
                jsonArray.add(obj);
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");

            resp.put("total",orderRunnings.size());
            resp.put("channels",jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "getorderbuffh",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getorderbuffh(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            List<OrderBuffhRunning> orderRunnings=orderBuffhRunningRepository.getOrder();
            List<String> timeBuff =videoBuffhRepository.getTimeBuffVideo();
            List<String> timeBuff24h =videoBuffhRepository.getTimeBuff24hVideo();
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
                //obj.put("home_rate", orderRunnings.get(i).get());
                obj.put("enabled", orderRunnings.get(i).getEnabled());
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
    ResponseEntity<String> delete(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String videoid){
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
            videoBuffhRepository.deletevideoByVideoId(videoid);
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
            List<OrderRunning> list_done =orderRunningRepository.getOrderFullBuffh();
            for(int i=0;i<list_done.size();i++){
               channelRepository.updateChannelBuffhDone(list_done.get(i).getChannelId().trim());
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
            System.out.println(videoidIdArr[0]);
            for(int i=0;i<videoidIdArr.length;i++){
                List<VideoBuffh> video=videoBuffhRepository.getVideoBuffhById(videoidIdArr[i].trim());
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
                System.out.println(video.get(0).getVideoid());
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
                obj.put("timebuffhtotal", 0);
                obj.put("viewtotal", 0);
                obj.put("timebuffh24h", 0);
                obj.put("view24h",0);

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

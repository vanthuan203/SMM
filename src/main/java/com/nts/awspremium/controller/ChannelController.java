package com.nts.awspremium.controller;
import com.nts.awspremium.model.*;
import com.nts.awspremium.repositories.*;
import okhttp3.*;
import okhttp3.RequestBody;
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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/channel")
public class ChannelController {
    @Autowired
    private ChannelRepository channelRepository;
    @Autowired
    private VideoRepository videoRepository;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    OrderRunningRepository orderRunningRepository;
    @Autowired
    private HistoryRepository historyRepository;
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
    ResponseEntity<String> orderbuffh(@org.springframework.web.bind.annotation.RequestBody ChannelOrder channelOrder, @RequestHeader(defaultValue = "") String Authorization) throws IOException, ParseException {
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
                                if(Duration.parse(video.get("duration").toString()).getSeconds()<3600){
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
    @DeleteMapping(path = "delete",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delete(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String channelid){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        if(channelid.length()==0){
            resp.put("status","fail");
            resp.put("message", "channelid không được để trống");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            channelRepository.deleteChannelById(channelid);
            videoRepository.deleteAllByChannelId(channelid);
            resp.put("channel","");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(path = "updatesingle",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updatesingle(@RequestHeader(defaultValue = "") String Authorization,@org.springframework.web.bind.annotation.RequestBody ChannelOrder channelOrder){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            String[] channelIdArr=channelOrder.getChannel_id().split("\n");
            JSONArray jsonArray =new JSONArray();
            for(int i=0;i<channelIdArr.length;i++){
                List<Channel> channels=channelRepository.getChannelById(channelIdArr[i].trim());
                channels.get(0).setMaxthreads(channelOrder.getMax_thread());
                channels.get(0).setEnabled(channelOrder.getEnabled());
                channels.get(0).setViewpercent(channelOrder.getView_percent());
                channels.get(0).setHomerate(channelOrder.getHome_rate());
                channels.get(0).setSuggestrate(channelOrder.getSuggest_rate());
                channels.get(0).setSearchrate(channelOrder.getSearch_rate());
                channels.get(0).setDirectrate(channelOrder.getDirect_rate());
                channelRepository.save(channels.get(0));
                List<OrderRunning> orderRunningbyVps=orderRunningRepository.getOrderByChannelid(channelIdArr[i].trim());
                JSONObject obj = new JSONObject();
                obj.put("channel_id", channelIdArr[i].trim());
                obj.put("channel_title", channels.get(0).getTitle());
                obj.put("total", orderRunningbyVps.get(0).getTotal());
                obj.put("max_threads", channelOrder.getMax_thread());
                //obj.put("insert_date", channelOrder.getin);
                obj.put("view_percent", channelOrder.getView_percent());
                //obj.put("home_rate", orderRunnings.get(i).get());
                obj.put("enabled", channelOrder.getEnabled());
                jsonArray.add(obj);
                if(channelIdArr.length==1){
                    resp.put("channel",obj);
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            }
            resp.put("channels",jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
}

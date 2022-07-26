package com.nts.awspremium.controller;


import com.nts.awspremium.model.Channel;
import com.nts.awspremium.model.ChannelOrder;
import com.nts.awspremium.model.ChannelOrders;
import com.nts.awspremium.model.Video;
import com.nts.awspremium.repositories.AdminRepository;
import com.nts.awspremium.repositories.ChannelRepository;
import com.nts.awspremium.repositories.VideoRepository;
import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class HomeController {
    @Autowired
    private VideoRepository videoRepository;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private ChannelRepository channelRepository;

    @GetMapping("/")
    public String index() {
        return "Vocuc203 || AccPremium + Proxy";
    }

    //Get video tren channel
    @PostMapping(value = "/order", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> order( @RequestHeader(defaultValue = "") String Authorization, @RequestBody ChannelOrder channelOrder) throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        String list_video="";
        List<Channel> channels = channelRepository.getChannelById(channelOrder.getChannel_id().trim());
        String pageToken="";
        if (channels.size() == 0) {

            OkHttpClient clientchannel = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

            Request requestchannel = null;

            requestchannel = new Request.Builder().url("https://backend.simplesolution.co/UserDataApi/getChannelInfo/" + channelOrder.getChannel_id().trim()).get().build();

            Response responsechannel = clientchannel.newCall(requestchannel).execute();

            String resultJsonChannel = responsechannel.body().string();

            Object objchannel = new JSONParser().parse(resultJsonChannel);
            JSONObject jsonObjectChannel = (JSONObject) objchannel;
            Channel channel = new Channel();
            channel.setChannelid(channelOrder.getChannel_id());
            channel.setTitle(jsonObjectChannel.get("title").toString());
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


            while(true){

                OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                Request request = null;

                request = new Request.Builder().url("https://www.googleapis.com/youtube/v3/search?key=AIzaSyAStTN9Ldm9tS--vF8lCj_sZqErX-NoIqk&channelId="+channelOrder.getChannel_id().trim()+"&part=id&maxResults=50&kind=video"+
                        (pageToken.length()==0?"":"&pageToken="+pageToken)).get().build();

                Response response = client.newCall(request).execute();

                String resultJson = response.body().string();

                Object obj = new JSONParser().parse(resultJson);
                JSONObject jsonObject = (JSONObject) obj;
                pageToken= (String) jsonObject.get("nextPageToken");
                if(pageToken==null){
                    pageToken="";
                }
                JSONArray jsonArray =(JSONArray) jsonObject.get("items");
                for(Object item:jsonArray ){
                    JSONObject video=(JSONObject) item;
                    JSONObject id=(JSONObject) video.get("id");
                    list_video=(list_video.length()==0?"":list_video+",")+id.get("videoId");
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
                        Video videos = new Video();
                        videos.setVideoid(video.get("videoId").toString());
                        videos.setChannelid(channelOrder.getChannel_id().trim());
                        videos.setDuration(Duration.parse(video.get("duration").toString()).getSeconds());
                        videos.setTitle(video.get("title").toString());
                        //new Video(video.get("videoId").toString(), "channel_id", Duration.parse(video.get("duration").toString()).getSeconds(), video.get("title").toString());
                        videoRepository.save(videos);
                    } catch (Exception e) {
                        resp.put("status", "Fail");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                }
                if(pageToken.length()==0){
                    break;
                }

            }
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

        }
        resp.put("status", "fail");
        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
    }


}

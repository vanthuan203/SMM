package com.nts.awspremium.system;

import com.nts.awspremium.model.Balance;
import com.nts.awspremium.model.SettingTiktok;
import com.nts.awspremium.repositories.BalanceRepository;
import com.nts.awspremium.repositories.ProfileTaskRepository;
import com.nts.awspremium.repositories.SettingTikTokRepository;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Component
public class RunBotERROR {
    @Autowired
    private ProfileTaskRepository profileTaskRepository;
    @Autowired
    private SettingTikTokRepository settingTikTokRepository;
    @Autowired
    private Environment env;
    @PostConstruct
    public void init() throws InterruptedException {
        try{
            if(Integer.parseInt(env.getProperty("server.port"))==8000){
                new Thread(() -> {
                    Long id=0L;
                    String value1="";
                    String value2="";
                    String value3="";
                    //Random rand =new Random();
                    while (true) {
                        try{
                            Long max_version=profileTaskRepository.get_Max_Version_Tiktok_In_System();
                            SettingTiktok settingTiktok=settingTikTokRepository.get_Setting();
                            if(max_version>settingTiktok.getMax_version()){
                                value1="\uD83D\uDED1 "+"The latest version of the tiktok lite app is "+max_version;
                                OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
                                Request request = null;
                                request = new Request.Builder().url("https://api.telegram.org/bot8071869909:AAE9uJgNWGpNta9i3x9d2-RRujp55yjLXoU/sendMessage?chat_id=-4697624673&text="+value1).get().build();

                                Response response = client.newCall(request).execute();
                            }
                            Thread.sleep(900000);
                        } catch (Exception e) {
                            continue;
                        }
                    }
                }).start();
            }

        }catch (Exception e){

        }



    }
}





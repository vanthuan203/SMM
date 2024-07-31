package com.nts.awspremium.system;

import com.nts.awspremium.controller.ProfileController;
import com.nts.awspremium.controller.SettingController;
import com.nts.awspremium.platform.youtube.YoutubeUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class RunPlatform {
    @Autowired
    private SettingController settingController;
    @Autowired
    private Environment env;
    @PostConstruct
    public void init() throws InterruptedException {
        try{
            if(Integer.parseInt(env.getProperty("server.port"))==8000){
                new Thread(() -> {
                    //Random rand =new Random();
                    while (true) {
                        try {
                            try {
                                Thread.sleep(30000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            settingController.update_State_Platform();
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





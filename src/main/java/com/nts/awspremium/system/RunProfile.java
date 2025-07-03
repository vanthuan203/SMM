package com.nts.awspremium.system;

import com.nts.awspremium.controller.ProfileController;
import com.nts.awspremium.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class RunProfile {
    @Autowired
    private ProfileController profileController;
    @Autowired
    private Environment env;
    //@PostConstruct
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
                            profileController.update_Enabled_Profile();
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





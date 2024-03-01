package com.nts.awspremium.controller;

import com.nts.awspremium.model.OrderSpeedTrue;
import com.nts.awspremium.model.OrderTrue;
import com.nts.awspremium.repositories.IpV4Repository;
import com.nts.awspremium.repositories.VideoViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Random;

@Component
public class AutoRunCheckProxy {
    @Autowired
    private ProxyController proxyController;
    @Autowired
    private IpV4Repository ipV4Repository;
    @Autowired
    private OrderSpeedTrue orderSpeedTrue;
    @Autowired
    private Environment env;
    @PostConstruct
    public void init() throws InterruptedException {
        try{
            int num_Cron= Integer.parseInt(env.getProperty("server.port"))-7999;
            for(int i=num_Cron;i<=num_Cron+10;i++){
                int finalI = i;
                new Thread(() -> {
                    Random rand =new Random();
                    while(true) {
                        try {
                            Thread.sleep(5000+rand.nextInt(5000));
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        try{
                            proxyController.checkproxyMain(finalI);
                        }catch (Exception e){
                            continue;
                        }
                    }
                }).start();
            }
        }catch (Exception e){

        }



    }
}





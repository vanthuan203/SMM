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
            int num_Cron= Integer.parseInt(env.getProperty("server.port"))-8000;
            for(int i=1;i<=10;i++){
                int finalI = i+(num_Cron==0?0:(num_Cron*10));
                //System.out.println(finalI);
                new Thread(() -> {
                    Random rand =new Random();
                    while(true) {
                        try{
                            proxyController.checkproxyMain(finalI);
                        }catch (Exception e){
                            continue;
                        }
                        try {
                            Thread.sleep(300000+rand.nextInt(50000));
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).start();
            }
        }catch (Exception e){

        }



    }
}





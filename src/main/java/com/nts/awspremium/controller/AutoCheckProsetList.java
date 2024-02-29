package com.nts.awspremium.controller;

import com.nts.awspremium.model.CheckProsetListTrue;
import com.nts.awspremium.model.OrderTrue;
import com.nts.awspremium.repositories.HistoryViewRepository;
import com.nts.awspremium.repositories.ProxyRepository;
import com.nts.awspremium.repositories.VideoViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Random;

@Component
public class AutoCheckProsetList {
    @Autowired
    private ProxyRepository proxyRepository;
    @Autowired
    private HistoryViewRepository historyViewRepository ;
    @Autowired
    private CheckProsetListTrue checkProsetListTrue;
    @Autowired
    private Environment env;
    @PostConstruct
    public void init() throws InterruptedException {
        try{
            int num_Cron= Integer.parseInt(env.getProperty("server.port"))-8000;
            for(int i=num_Cron;i<1;i++) {
                new Thread(() -> {
                    Random rand = new Random();
                    while (true) {
                        try {
                            try {
                                Thread.sleep(100 + rand.nextInt(200));
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            //checkProsetListTrue.setValue(proxyRepository.PROCESSLISTVIEW());
                            checkProsetListTrue.setValue(historyViewRepository.PROCESSLISTVIEW());
                            //System.out.println(checkProsetListTrue.getValue());
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





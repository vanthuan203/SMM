package com.nts.awspremium.controller;

import com.nts.awspremium.model.CheckProsetListTrue;
import com.nts.awspremium.model.OrderTrue;
import com.nts.awspremium.repositories.HistoryViewRepository;
import com.nts.awspremium.repositories.ProxyRepository;
import com.nts.awspremium.repositories.VideoViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostConstruct
    public void init() throws InterruptedException {
        try{
            new Thread(() -> {
                Random rand =new Random();
                while(true) {
                    try{
                        try {
                            Thread.sleep(100+rand.nextInt(200));
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        checkProsetListTrue.setValue(proxyRepository.PROCESSLISTVIEW());
                        //checkProsetListTrue.setValue(historyViewRepository.PROCESSLISTVIEW());
                        //System.out.println(checkProsetListTrue.getValue());
                    }catch (Exception e){
                        continue;
                    }
                }
            }).start();
        }catch (Exception e){

        }
    }
}





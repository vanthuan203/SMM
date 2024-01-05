package com.nts.awspremium.controller;

import com.nts.awspremium.model.OrderTrue;
import com.nts.awspremium.repositories.VideoViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Random;

@Component
public class AutoRunCheck {
    @Autowired
    private VideoViewRepository videoViewRepository;
    @Autowired
    private OrderTrue orderTrue;

    //@PostConstruct
    public void init() throws InterruptedException {
        try{
            new Thread(() -> {
                //Random rand =new Random();
                while(true) {
                    try{
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        orderTrue.setValue(videoViewRepository.getListOrderTrueThreadON());
                        //System.out.println(String.join(", ", orderTrue.getValue()));
                    }catch (Exception e){
                        continue;
                    }
                }
            }).start();
        }catch (Exception e){

        }



    }
}





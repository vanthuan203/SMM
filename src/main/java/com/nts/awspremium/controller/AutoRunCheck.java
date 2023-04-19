package com.nts.awspremium.controller;

import com.nts.awspremium.model.OrderTrue;
import com.nts.awspremium.repositories.VideoViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AutoRunCheck {
    @Autowired
    private VideoViewRepository videoViewRepository;
    @Autowired
    private OrderTrue orderTrue;

    @PostConstruct
    public void init() throws InterruptedException {
        new Thread(() -> {
                while(true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    orderTrue.setValue(videoViewRepository.getListOrderTrueThread());
                    //System.out.println(String.join(", ", orderTrue.getValue()));
                }
        }).start();


    }
}





package com.nts.awspremium.controller;

import com.nts.awspremium.model.OrderTrafficTrue;
import com.nts.awspremium.model.OrderTrue;
import com.nts.awspremium.repositories.VideoViewRepository;
import com.nts.awspremium.repositories.WebTrafficRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AutoRunTrafficCheck {
    @Autowired
    private WebTrafficRepository webTrafficRepository;
    @Autowired
    private OrderTrafficTrue orderTrue;
    @Autowired
    private Environment env;
    //@PostConstruct
    public void init() throws InterruptedException {
        try{
                new Thread(() -> {
                    //Random rand =new Random();
                    while (true) {
                        try {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            orderTrue.setValue(webTrafficRepository.getListOrderTrueThreadON());
                            //System.out.println(String.join(", ", orderTrue.getValue()));
                        } catch (Exception e) {
                            continue;
                        }
                    }
                }).start();
        }catch (Exception e){

        }



    }
}





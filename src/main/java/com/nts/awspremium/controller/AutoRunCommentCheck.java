package com.nts.awspremium.controller;

import com.nts.awspremium.model.OrderCommentTrue;
import com.nts.awspremium.model.OrderSpeedTrue;
import com.nts.awspremium.model.OrderTrue;
import com.nts.awspremium.repositories.VideoCommentRepository;
import com.nts.awspremium.repositories.VideoViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Random;

@Component
public class AutoRunCommentCheck {
    @Autowired
    private VideoCommentRepository videoCommentRepository;
    @Autowired
    private OrderCommentTrue orderCommentTrue;
    @Autowired
    private Environment env;
    @PostConstruct
    public void init() throws InterruptedException {
        try{
                new Thread(() -> {
                    Random rand =new Random();
                    while (true) {
                        try {
                            orderCommentTrue.setValue(videoCommentRepository.getListOrderTrueThreadON());
                            //System.out.println(orderCommentTrue.getValue());
                            try {
                                Thread.sleep(rand.nextInt(50));
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        } catch (Exception e) {
                            continue;
                        }
                    }
                }).start();
        }catch (Exception e){

        }



    }
}





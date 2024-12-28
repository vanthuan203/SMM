package com.nts.awspremium.system;

import com.nts.awspremium.controller.OrderRunningController;
import com.nts.awspremium.repositories.AccountRepository;
import com.nts.awspremium.repositories.OrderRunningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class RunOrderValidCheck {
    @Autowired
    private OrderRunningController orderRunningController;
    @Autowired
    private OrderRunningRepository orderRunningRepository;
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
                                Thread.sleep(300000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            orderRunningController.check_Valid_OrderRunning();
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





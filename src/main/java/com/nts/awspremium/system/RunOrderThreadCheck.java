package com.nts.awspremium.system;

import com.nts.awspremium.model_system.OrderThreadCheck;
import com.nts.awspremium.repositories.OrderRunningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Random;

@Component
public class RunOrderThreadCheck {
    @Autowired
    private OrderRunningRepository orderRunningRepository;
    @Autowired
    private OrderThreadCheck orderThreadCheck;
    @Autowired
    private Environment env;
    @PostConstruct
    public void init() throws InterruptedException {
        try{
                new Thread(() -> {
                    Random rand =new Random();
                    while (true) {
                        try {
                            orderThreadCheck.setValue(orderRunningRepository.get_List_Order_Thread_True());
                            //System.out.println(orderFollowerTrue.getValue());
                            try {
                                Thread.sleep(rand.nextInt(150));
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





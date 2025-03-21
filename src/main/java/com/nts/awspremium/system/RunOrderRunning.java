package com.nts.awspremium.system;

import com.nts.awspremium.controller.DataConmentController;
import com.nts.awspremium.controller.OrderRunningController;
import com.nts.awspremium.model_system.OrderThreadCheck;
import com.nts.awspremium.repositories.OrderRunningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Random;

@Component
public class RunOrderRunning {
    @Autowired
    private OrderRunningRepository orderRunningRepository;
    @Autowired
    private OrderRunningController orderRunningController;
    @Autowired
    private DataConmentController dataConmentController;
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
                            Long check_time=System.currentTimeMillis();
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            orderRunningController.update_Total_Buff();
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            Integer check_count_num=orderRunningController.update_Check_Count_Num();
                            for(int i=1;i<=check_count_num;i++) {
                                int finalI = i;
                                new Thread(() -> {
                                        try {
                                            orderRunningController.update_Current_Total(finalI);
                                        } catch (Exception e) {
                                        }

                                }).start();
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            orderRunningController.update_Order_Running_Done_No_Check();
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            orderRunningController.update_Order_Running_Done_Check();
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            dataConmentController.update_Running_Comment();
                            while (true){
                                if((System.currentTimeMillis()-check_time)/1000<60){
                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }else{
                                    break;
                                }
                            }
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





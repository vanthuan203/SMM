package com.nts.awspremium.system;

import com.nts.awspremium.controller.OrderRunningController;
import com.nts.awspremium.controller.TaskController;
import com.nts.awspremium.model_system.OrderThreadCheck;
import com.nts.awspremium.repositories.AccountTaskRepository;
import com.nts.awspremium.repositories.OrderRunningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class RunAccountTask {
    @Autowired
    private AccountTaskRepository accountTaskRepository;
    @Autowired
    private TaskController taskController;
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
                                Thread.sleep(60000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            taskController.resetTaskError();
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





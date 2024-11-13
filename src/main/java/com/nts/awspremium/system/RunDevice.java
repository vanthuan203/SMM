package com.nts.awspremium.system;

import com.nts.awspremium.controller.DeviceController;
import com.nts.awspremium.controller.ProfileController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class RunDevice {
    @Autowired
    private DeviceController deviceController;
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
                            deviceController.update_Status_Device();
                            try {
                                Thread.sleep(300000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
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





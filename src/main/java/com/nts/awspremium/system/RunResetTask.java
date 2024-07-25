package com.nts.awspremium.system;

import com.nts.awspremium.controller.DataConmentController;
import com.nts.awspremium.controller.TaskController;
import com.nts.awspremium.platform.Instagram.InstagramUpdate;
import com.nts.awspremium.platform.facebook.FacebookUpdate;
import com.nts.awspremium.platform.threads.ThreadsUpdate;
import com.nts.awspremium.platform.tiktok.TiktokUpdate;
import com.nts.awspremium.platform.x.XUpdate;
import com.nts.awspremium.platform.youtube.YoutubeUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class RunResetTask {
    @Autowired
    private DataConmentController dataConmentController;
    @Autowired
    private TaskController taskController;
    @Autowired
    private Environment env;
    @Autowired
    private XUpdate xUpdate;
    @Autowired
    private YoutubeUpdate youtubeUpdate;
    @Autowired
    private TiktokUpdate tiktokUpdate;
    @Autowired
    private InstagramUpdate instagramUpdate;
    @Autowired
    private FacebookUpdate facebookUpdate;
    @Autowired
    private ThreadsUpdate threadsUpdate;
    @PostConstruct
    public void init() throws InterruptedException {
        try{
            if(Integer.parseInt(env.getProperty("server.port"))==8000){
                new Thread(() -> {
                    //Random rand =new Random();
                    while (true) {
                        try {
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            taskController.resetTaskError();
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            dataConmentController.reset_Running_Comment();
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            youtubeUpdate.youtube_delete_task_24h();
                            tiktokUpdate.tiktok_delete_task_24h();
                            instagramUpdate.instagram_delete_task_24h();
                            facebookUpdate.facebook_delete_task_24h();
                            threadsUpdate.threads_delete_task_24h();
                            xUpdate.x_delete_task_24h();
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





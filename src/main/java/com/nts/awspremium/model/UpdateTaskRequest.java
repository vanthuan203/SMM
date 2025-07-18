package com.nts.awspremium.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTaskRequest {
    private String account_id="";
    private String device_id="";
    private String profile_id="";
    private Boolean status=false;
    private Boolean success=null;
    private String task="";
    private String task_key="";
    private Long order_id=0L;
    private String platform="";
    private Integer isLogin=-1;
    private Integer viewing_time=0;
    private String bonus="";
}

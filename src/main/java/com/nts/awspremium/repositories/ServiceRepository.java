package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Account;
import com.nts.awspremium.model.Service;
import com.nts.awspremium.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service,Integer> {
    @Query(value = "SELECT * FROM service where enabled=1",nativeQuery = true)
    public List<Service> get_All_Service_Enabled();
    @Query(value = "SELECT * FROM service where enabled=1",nativeQuery = true)
    public List<Service> get_All_Service();

    @Query(value = "SELECT * FROM service where service_id=?1 and enabled=1 limit 1",nativeQuery = true)
    public Service get_Service(Integer service);

    @Query(value = "SELECT * FROM service where service_id=?1 limit 1",nativeQuery = true)
    public Service get_Service_Web(Integer service);

    @Query(value = "Select CONCAT_WS(' | ',service_id,service_name,platform,task,concat(service_rate,'$')) from service where platform=?1 and mode=?2",nativeQuery = true)
    public List<String> get_All_Service_Web(String platform,String mode);

    @Query(value = "SELECT s.platform FROM Data.service s join order_running o on s.service_id=o.service_id group by s.platform",nativeQuery = true)
    public List<String> get_Platform_In_OrderRunning();

    @Query(value = "Select CONCAT_WS(' | ',service_id,service_name,platform,task,concat(service_rate,'$')) from service where enabled=1 and platform=?1 and mode=?2",nativeQuery = true)
    public List<String> get_All_Service_Enabled_Web(String platform ,String mode);

    @Query(value = "SELECT task FROM service group by task",nativeQuery = true)
    public List<String>  get_All_Task();
    @Query(value = "SELECT service_type FROM service group by service_type",nativeQuery = true)
    public List<String>  get_All_Type();

    @Query(value = "SELECT platform FROM service group by platform",nativeQuery = true)
    public List<String>  get_All_Platform();
    @Query(value = "SELECT mode FROM service where mode!='' group by mode",nativeQuery = true)
    public List<String>  get_All_Mode();


}

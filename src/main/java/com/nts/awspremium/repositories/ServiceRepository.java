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

    @Query(value = "SELECT * FROM service where service_id=?1 and enabled=1 limit 1",nativeQuery = true)
    public Service get_Service(Integer service);

    @Query(value = "SELECT * FROM service where service_id=?1 limit 1",nativeQuery = true)
    public Service get_Service_Web(Integer service);

    @Query(value = "Select CONCAT_WS(' | ',service_id,service_name,platform,task,concat(service_rate,'$')) from service",nativeQuery = true)
    public List<String> get_All_Service_Web();

    @Query(value = "Select CONCAT_WS(' | ',service_id,service_name,platform,task,concat(service_rate,'$')) from service where enabled=1",nativeQuery = true)
    public List<String> get_All_Service_Enabled_Web();


}

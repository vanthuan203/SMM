package com.nts.awspremium.repositories;


import com.nts.awspremium.model.Admin;
import com.nts.awspremium.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface ServiceRepository extends JpaRepository<Service,Integer> {
    @Query(value = "SELECT * FROM service where enabled=1 and type!=\"Custom Comments\"",nativeQuery = true)
    public List<Service> getAllService();
    @Query(value = "Select geo from service group by geo",nativeQuery = true)
    public List<String> GetAllGeoService();
    @Query(value = "SELECT * FROM service ",nativeQuery = true)
    public List<Service> getAllServiceByWeb();

    @Query(value = "SELECT * FROM service where enabled=1 and type=\"Custom Comments\"",nativeQuery = true)
    public List<Service> getAllServiceCmt();

    @Query(value = "SELECT * FROM service where enabled=1 and category=\"Website\"",nativeQuery = true)
    public List<Service> getAllServiceTraffic();

    @Query(value = "SELECT * FROM service where enabled=1 and category=\"Tiktok\"",nativeQuery = true)
    public List<Service> getAllServiceTiktok();


    @Query(value = "SELECT * FROM service where service=?1 and enabled=1 limit 1",nativeQuery = true)
    public Service getService(Integer service);

    @Query(value = "SELECT geo FROM service where service=?1 limit 1",nativeQuery = true)
    public String getGeoByService(Integer service);

    @Query(value = "SELECT * FROM service where service=?1 limit 1",nativeQuery = true)
    public Service getServiceNoCheckEnabled(Integer service);

    @Query(value = "SELECT * FROM service where service=?1 and category=\"Tiktok\" limit 1",nativeQuery = true)
    public Service getServiceTikTokNoCheckEnabled(Integer service);

    @Query(value = "SELECT service FROM service where geo=?1 and maxtime<=5 and enabled=1 and niche=0 and category!='Custom Comments' and type='Default'  order by rand()  limit 1",nativeQuery = true)
    public Integer getServiceRand(String geo);


    @Query(value = "SELECT * FROM service where service=?1 and enabled=1 limit 1",nativeQuery = true)
    public Service getServiceCmt(Integer service);

    @Query(value = "SELECT * FROM service where service=?1 limit 1",nativeQuery = true)
    public Service getInfoService(Integer service);

    @Query(value = "Select count(*) from admin where token=?1",nativeQuery = true)
    public Integer FindAdminByToken(String Authorization);


    @Query(value = "Select CONCAT_WS(' | ',service,name,note,concat(rate,'$')) from service ",nativeQuery = true)
    public List<String> GetAllService();


    @Query(value = "Select CONCAT_WS(' | ',service,name,note,concat(rate,'$')) from service where category='Website' ",nativeQuery = true)
    public List<String> GetAllServiceTraffic();

    @Query(value = "Select CONCAT_WS(' | ',service,name,concat(rate,'$')) from service where category='TikTok' ",nativeQuery = true)
    public List<String> GetAllServiceFollowerTiktok();

    @Query(value = "Select CONCAT_WS(' | ',service,name,note,concat(rate,'$')) from service where enabled=1",nativeQuery = true)
    public List<String> GetAllServiceEnabled();

    @Query(value = "Select CONCAT_WS(' | ',service,name,concat(rate,'$')) from service where category='TikTok' and enabled=1",nativeQuery = true)
    public List<String> GetAllServiceTiktokEnabled();

    @Query(value = "Select CONCAT_WS(' | ',service,name,note,concat(rate,'$')) from service where category='Website' and enabled=1",nativeQuery = true)
    public List<String> GetAllServiceTrafficEnabled();

    @Query(value = "Select * from service where service=?1",nativeQuery = true)
    public List<Service> GetServiceById(Integer service);


    @Query(value = "SELECT FROM_UNIXTIME((?1/1000+(7-TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)*60*60),'%Y-%m-%d %H:%i:%s')>=DATE_SUB(DATE_FORMAT(CONVERT_TZ(NOW(), @@session.time_zone, '+07:00'),'%Y-%m-%d %H:%i:%s'),INTERVAL ?2 DAY)",nativeQuery = true)
    public Integer checkGuarantee(Long end_date,Integer max_refill);
    @Query(value = "SELECT FROM_UNIXTIME((?1/1000+(7-TIME_TO_SEC(TIMEDIFF(NOW(), UTC_TIMESTAMP)) / 3600)*60*60),'%Y-%m-%d %H:%i:%s')>=DATE_SUB(DATE_FORMAT(CONVERT_TZ(NOW(), @@session.time_zone, '+07:00'),'%Y-%m-%d 14:0:0'),INTERVAL ?2 DAY)",nativeQuery = true)
    public Integer checkGuaranteeByTime(Long end_date,Integer max_refill);

    @Query(value = "Select * from admin where token=?1",nativeQuery = true)
    public List<Admin>  FindByToken(String Authorization);
}

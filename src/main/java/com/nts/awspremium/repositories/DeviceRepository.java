package com.nts.awspremium.repositories;

import com.nts.awspremium.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface DeviceRepository extends JpaRepository<Device,String> {

    @Query(value = "Select * from device where device_id=?1 limit 1",nativeQuery = true)
    public Device check_DeviceId(String device_id);
    @Query(value = "select count(*) from device where device_id=?1",nativeQuery = true)
    public Integer find_Device(String device_id);

    @Query(value = "select d.* from device d join profile_task p on d.device_id=p.device_id and p.enabled=0 and  d.mode!='' group by  d.device_id",nativeQuery = true)
    public List<Device> get_All_Device_Enable0();

    @Query(value = "select * from device ",nativeQuery = true)
    public List<Device> get_All_Device();
    @Query(value = "SELECT device_id FROM Data.account where live>1 and device_id in(select device_id from device where status=1) group by device_id",nativeQuery = true)
    public List<String> get_All_Device_DieAcc();


    @Modifying
    @Transactional
    @Query(value = "delete from device where device_id=?1",nativeQuery = true)
    public void delete_Device_By_DeviceId(String device_id);

    @Modifying
    @Transactional
    @Query(value = "update device set status=0  where device_id in(?1)",nativeQuery = true)
    public void update_Status_Device(List<String> device_id);

    @Modifying
    @Transactional
    @Query(value = "delete from device where device_id in(?1)",nativeQuery = true)
    public void delete_Device_By_List_Device(List<String> device_id);

    @Modifying
    @Transactional
    @Query(value = "update device set state=?1  where device_id in(?2)",nativeQuery = true)
    public void update_State_By_DeviceId(Integer state,List<String> device_id);

    @Modifying
    @Transactional
    @Query(value = "update device set mode=?1  where device_id in(?2)",nativeQuery = true)
    public void update_Mode_By_DeviceId(String mode,List<String> device_id);

    @Modifying
    @Transactional
    @Query(value = "update device set box_id=?1  where device_id in(?2)",nativeQuery = true)
    public void update_Box_By_DeviceId(String box_id,List<String> device_id);

    @Modifying
    @Transactional
    @Query(value = "update device set num_profile=?1,account_die='',account_live='',status=1  where device_id in(?2)",nativeQuery = true)
    public void update_NumProfile_By_ListDevice(Integer num_profile,List<String> device_id);

    @Query(value = "SELECT new com.nts.awspremium.model.DeviceShow(d.device_id,d.box_id,d.rom_version,d.mode,d.state,d.ip_address,d.ip_changer_time,d.status,MAX(a.running),d.add_time,d.update_time,MAX(a.get_time),d.num_account,d.account_live,d.account_die,d.num_profile,d.num_profile_set,a.profile_id,a.platform,a.task) FROM Device d left join ProfileTask  a on a.device.device_id=d.device_id and a.running=1 group by d.device_id")
    Page<DeviceShow> get_List_Device(Pageable pageable);

    @Query(value = "SELECT new com.nts.awspremium.model.DeviceShow(d.device_id,d.box_id,d.rom_version,d.mode,d.state,d.ip_address,d.ip_changer_time,d.status,MAX(a.running),d.add_time,d.update_time,MAX(a.get_time),d.num_account,d.account_live,d.account_die,d.num_profile,d.num_profile_set,a.profile_id,a.platform,a.task) FROM Device d left join ProfileTask  a on a.device.device_id=d.device_id and a.running=1 where d.device_id in (?1) group by d.device_id")
    List<DeviceShow> get_List_Device_By_DeviceId(List<String> device);

    @Query(value = "SELECT new com.nts.awspremium.model.DeviceShow(d.device_id,d.box_id,d.rom_version,d.mode,d.state,d.ip_address,d.ip_changer_time,d.status,MAX(a.running),d.add_time,d.update_time,MAX(a.get_time),d.num_account,d.account_live,d.account_die,d.num_profile,d.num_profile_set,a.profile_id,a.platform,a.task) FROM Device d left join ProfileTask  a on a.device.device_id=d.device_id and a.running=1 where d.state=?1 group by d.device_id")
    Page<DeviceShow> get_List_Device_By_State(Pageable pageable,Integer state);

    @Query(value = "SELECT new com.nts.awspremium.model.DeviceShow(d.device_id,d.box_id,d.rom_version,d.mode,d.state,d.ip_address,d.ip_changer_time,d.status,a.running,d.add_time,d.update_time,a.get_time,d.num_account,d.account_live,d.account_die,d.num_profile,d.num_profile_set,a.profile_id,a.platform,a.task) " +
            "FROM Device d left join ProfileTask  a on a.device.device_id=d.device_id where d.device_id=?1 group by d.device_id")
    Page<DeviceShow> get_List_Device(Pageable pageable, String device_id);

    @Query(value = "SELECT new com.nts.awspremium.model.DeviceShow(d.device_id,d.box_id,d.rom_version,d.mode,d.state,d.ip_address,d.ip_changer_time,d.status,a.running,d.add_time,d.update_time,a.get_time,d.num_account,d.account_live,d.account_die,d.num_profile,d.num_profile_set,a.profile_id,a.platform,a.task) " +
            "FROM Device d left join ProfileTask  a on a.device.device_id=d.device_id where d.device_id=?1 and d.state=?2 group by d.device_id")
    Page<DeviceShow> get_List_Device_By_State(Pageable pageable, String device_id,Integer state);



}

package com.nts.awspremium.repositories;


import com.nts.awspremium.model.Admin;
import com.nts.awspremium.model.AutoRefill;
import com.nts.awspremium.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface AutoRefillRepository extends JpaRepository<AutoRefill,Long> {
    @Query(value = "SELECT * FROM autorefill where id=1",nativeQuery = true)
    public List<AutoRefill> getAutoRefill();

    @Query(value = "SELECT * FROM service where service=?1 limit 1",nativeQuery = true)
    public Service getService(Integer service);

    @Query(value = "Select count(*) from admin where token=?1",nativeQuery = true)
    public Integer FindAdminByToken(String Authorization);

    @Query(value = "Select * from admin where token=?1",nativeQuery = true)
    public List<Admin>  FindByToken(String Authorization);
}

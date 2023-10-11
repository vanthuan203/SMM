package com.nts.awspremium.repositories;


import com.nts.awspremium.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.criteria.CriteriaBuilder;
import javax.transaction.Transactional;
import java.util.List;

public interface AdminRepository extends JpaRepository<Admin,Long> {
    @Query(value = "Select count(*) from admin where token=?1",nativeQuery = true)
    public Integer FindAdminByToken(String Authorization);

    @Query(value = "?1",nativeQuery = true)
    public Integer queryAdmin(String query);

    @Query(value = "Select * from admin where username=?1 and password=?2 limit 1",nativeQuery = true)
    public List<Admin> FindAdminByUserPass(String username, String password);

    @Query(value = "Select count(*) from admin where username=?1",nativeQuery = true)
    public Integer FindAdminByUser(String username);

    @Query(value = "Select * from admin where username=?1 limit 1",nativeQuery = true)
    public List<Admin> getAdminByUser(String username);

    @Query(value = "Select balance from admin where username=?1 limit 1",nativeQuery = true)
    public Long getBlance(String username);
    @Modifying
    @Transactional
    @Query(value = "update admin set balance=?1 where username=?2",nativeQuery = true)
    public Integer updateBalance(Float balance,String username);

    @Query(value = "call update_balance(?1,?2)",nativeQuery = true)
    public Float updateBalanceFine(Float a,String username);

    @Query(value = "Select username from admin",nativeQuery = true)
    public List<String> GetAllUser();

    @Query(value = "Select * from admin",nativeQuery = true)
    public List<Admin> GetAllUsers();

    @Query(value = "Select * from admin where username=?1",nativeQuery = true)
    public List<Admin> GetAdminByUser(String username);

    @Query(value = "Select * from admin where token=?1",nativeQuery = true)
    public List<Admin>  FindByToken(String Authorization);
}

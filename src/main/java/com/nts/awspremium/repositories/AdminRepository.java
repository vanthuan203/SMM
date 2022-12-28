package com.nts.awspremium.repositories;


import com.nts.awspremium.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;

public interface AdminRepository extends JpaRepository<Admin,Long> {
    @Query(value = "Select count(*) from admin where token=?1",nativeQuery = true)
    public Integer FindAdminByToken(String Authorization);

    @Query(value = "Select * from admin where username=?1 and password=?2 limit 1",nativeQuery = true)
    public List<Admin> FindAdminByUserPass(String username, String password);

    @Query(value = "Select count(*) from admin where username=?1",nativeQuery = true)
    public Integer FindAdminByUser(String username);

    @Query(value = "Select username from admin",nativeQuery = true)
    public List<String> GetAllUser();

    @Query(value = "Select * from admin where username=?1",nativeQuery = true)
    public List<Admin> GetAdminByUser(String username);

    @Query(value = "Select * from admin where token=?1",nativeQuery = true)
    public List<Admin>  FindByToken(String Authorization);
}

package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Recover;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface RecoverRepository extends JpaRepository<Recover,String> {
    @Query(value = "SELECT * from recover order by timeget asc limit 1",nativeQuery = true)
    public Recover getRecover();

    @Query(value = "SELECT count(*) from recover where username=?1",nativeQuery = true)
    public Integer checkRecover(String username);
}
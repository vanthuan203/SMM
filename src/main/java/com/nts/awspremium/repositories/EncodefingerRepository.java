package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Cookie;
import com.nts.awspremium.model.Encodefinger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;


public interface EncodefingerRepository extends JpaRepository<Encodefinger,Long> {
    @Modifying
    @Transactional
    @Query(value = "UPDATE encodefinger SET encodefinger=?1 where username=?2",nativeQuery = true)
    public void updateEncodefingerSub(String encodefinger,String username);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO encodefinger(username,encodefinger) VALUES(?1,?2)",nativeQuery = true)
    public void insertEncodefingerSub(String username,String encodefinger);

    @Query(value = "SELECT encodefinger FROM encodefinger WHERE username=?1 limit 1",nativeQuery = true)
    public String findEncodefingerSub(String username);
}

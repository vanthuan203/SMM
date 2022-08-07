package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Cookie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface CookieRepository extends JpaRepository<Cookie,Long> {
    @Modifying
    @Transactional
    @Query(value = "UPDATE cookie SET cookie=?1 where username=?2",nativeQuery = true)
    public void updateCookieSub(String cookie,String username);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO cookie(username,cookie) VALUES(?1,?2)",nativeQuery = true)
    public void insertCookieSub(String username,String cookie);


    @Query(value = "SELECT cookie FROM cookie WHERE username=?1 limit 1",nativeQuery = true)
    public String findCookieSub(String username);
    @Query(value = "SELECT cookie FROM cookie WHERE id=?1 limit 1",nativeQuery = true)
    public String findCookieSubById(Long id);
    @Query(value = "SELECT id FROM cookie WHERE username=?1 limit 1",nativeQuery = true)
    public Long findIdSubByUsername(String username);
}

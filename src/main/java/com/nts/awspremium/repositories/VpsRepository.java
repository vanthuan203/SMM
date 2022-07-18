package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Vps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VpsRepository extends JpaRepository<Vps,Integer> {
    @Query(value = "select * from vps",nativeQuery = true)
    public List<Vps> getListVPS();
    @Query(value = "select * from vps where vps=?1",nativeQuery = true)
    public List<Vps> findVPS(String vps);
}

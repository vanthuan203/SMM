package com.nts.awspremium.repositories;


import com.nts.awspremium.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service,Integer> {
    @Query(value = "SELECT * FROM service",nativeQuery = true)
    public List<Service> getAllService();

    @Query(value = "SELECT * FROM service where service=?1 limit 1",nativeQuery = true)
    public Service getService(Integer service);
}

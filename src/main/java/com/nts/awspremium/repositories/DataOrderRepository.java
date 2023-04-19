package com.nts.awspremium.repositories;

import com.nts.awspremium.model.DataOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DataOrderRepository extends JpaRepository<DataOrder,Long> {
    @Query(value = "SELECT listvideo FROM dataorder WHERE orderid=?1",nativeQuery = true)
    public String getListKeyByOrderid(Long orderid);
}

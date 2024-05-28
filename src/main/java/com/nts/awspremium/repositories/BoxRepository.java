package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Box;
import com.nts.awspremium.model.Computer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BoxRepository extends JpaRepository<Box,String> {

    @Query(value = "Select * from box where box_id=?1 limit 1",nativeQuery = true)
    public Box check_BoxId(String box_id);
    @Query(value = "select count(*) from box where box_id=?1",nativeQuery = true)
    public Integer find_box(String device_id);



}

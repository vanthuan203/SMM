package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Computer;
import com.nts.awspremium.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ComputerRepository extends JpaRepository<Computer,String> {

    @Query(value = "Select * from computer where computer_id=?1 limit 1",nativeQuery = true)
    public Computer check_ComputerId(String computer_id);
    @Query(value = "select count(*) from device where computer_id=?1",nativeQuery = true)
    public Integer find_Computer(String computer_id);



}

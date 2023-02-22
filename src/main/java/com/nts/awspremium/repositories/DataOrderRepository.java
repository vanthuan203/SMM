package com.nts.awspremium.repositories;

import com.nts.awspremium.model.DataOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataOrderRepository extends JpaRepository<DataOrder,Long> {
}

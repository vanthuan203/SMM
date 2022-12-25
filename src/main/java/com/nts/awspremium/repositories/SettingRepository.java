package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingRepository  extends JpaRepository<Setting,Long> {
}

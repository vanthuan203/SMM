package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Mode;
import com.nts.awspremium.model.ModeOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ModeRepository extends JpaRepository<Mode,String> {
    @Query(value = "SELECT * FROM mode where mode=?1 limit 1",nativeQuery = true)
    public Mode get_Mode_Info(String mode);

    @Query(value = "SELECT * FROM mode ",nativeQuery = true)
    public List<Mode> get_List_Mode();

    @Query(value = "SELECT mode FROM mode ",nativeQuery = true)
    public List<String> get_List_String_Mode();

}

package com.nts.awspremium.repositories;
import com.nts.awspremium.model.Platform;
import com.nts.awspremium.model.TaskPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface PlatformRepository extends JpaRepository<Platform,String> {
    @Query(value = "SELECT GROUP_CONCAT(platform SEPARATOR ',') AS concatenated_rows from platform  where priority>0 and state=1 order by rand()",nativeQuery = true)
    public String get_All_Platform();

    @Query(value = "SELECT platform from platform  where priority>0 and state=1 and mode='auto' order by rand()",nativeQuery = true)
    public List<String> get_All_Platform_True();
    @Query(value = "SELECT platform from platform  where priority>0 and state=1 and \n" +
            "INSTR(IF((SELECT GROUP_CONCAT(platform ) AS platform FROM Data.account_profile where live!=1 and profile_id=?1) is null\n" +
            ",'',(SELECT GROUP_CONCAT(platform ) AS platform FROM Data.account_profile where live!=1 and profile_id=?1))\n" +
            ",platform)=0 order by rand()",nativeQuery = true)
    public List<String> get_All_Platform_True_By_ProfileId(String profile_id);
    @Query(value = "SELECT dependent FROM platform where platform=?1 limit 1",nativeQuery = true)
    public String get_Dependent_By_Platform(String platform);

    @Query(value = "SELECT priority FROM platform where platform=?1 limit 1",nativeQuery = true)
    public Integer get_Priority_By_Platform(String platform);

    @Query(value = "SELECT activity FROM platform where platform=?1 limit 1",nativeQuery = true)
    public Integer get_Activity_Platform(String platform);

    @Query(value = "SELECT connection_account FROM platform where platform=?1 limit 1",nativeQuery = true)
    public Integer get_Connection_Account_Platform(String platform);

    @Query(value = "SELECT * FROM platform ",nativeQuery = true)
    public List<Platform> get_List_Platform();
    @Query(value = "SELECT * FROM platform where platform=?1 ",nativeQuery = true)
    public Platform get_Platform_By_PlatformId(String platform);

    @Modifying
    @Transactional
    @Query(value = "UPDATE platform SET state=1 where platform in(?1) and mode='auto'",nativeQuery = true)
    public Integer update_State_1_Platform(List<String> platform);

    @Modifying
    @Transactional
    @Query(value = "UPDATE platform SET state=0 where platform not in(?1) and mode='auto'",nativeQuery = true)
    public Integer update_State_0_Platform(List<String> platform);

}
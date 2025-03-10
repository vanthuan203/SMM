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

    @Query(value = "SELECT platform from platform  where priority>0 and state=1 and mode=?1 order by rand()",nativeQuery = true)
    public List<String> get_All_Platform_True(String mode);
    @Query(value = "SELECT platform from platform  where priority>0 and mode=?1 and state=1 and \n" +
            "INSTR(IF((SELECT GROUP_CONCAT(platform ) AS platform FROM Data.account_profile where live!=1 and profile_id=?2) is null\n" +
            ",'',(SELECT GROUP_CONCAT(platform ) AS platform FROM Data.account_profile where live!=1 and profile_id=?2))\n" +
            ",platform)=0 order by rand()",nativeQuery = true)
    public List<String> get_All_Platform_True_By_ProfileId(String mode,String profile_id);
    @Query(value = "SELECT dependent FROM platform where platform=?1 and mode=?2 limit 1",nativeQuery = true)
    public String get_Dependent_By_Platform_And_Mode(String platform,String mode);

    @Query(value = "SELECT dependent FROM platform where platform=?1 and mode=?2 and connection_account=1  limit 1",nativeQuery = true)
    public String get_Dependent_Connection_By_Platform_And_Mode(String platform,String mode);

    @Query(value = "SELECT priority FROM platform where platform=?1 and mode=?2 limit 1",nativeQuery = true)
    public Integer get_Priority_By_Platform_And_Mode(String platform,String mode);

    @Query(value = "SELECT activity FROM platform where platform=?1 and mode=?2 limit 1",nativeQuery = true)
    public Integer get_Activity_Platform_And_Mode(String platform,String mode);

    @Query(value = "SELECT platform FROM platform group by platform  ",nativeQuery = true)
    public List<String> get_List_String_Platform();

    @Query(value = "SELECT connection_account FROM platform where platform=?1 and mode=?2 limit 1",nativeQuery = true)
    public Integer get_Connection_Account_Platform_And_Mode(String platform,String mode);

    @Query(value = "SELECT register_account FROM platform where platform=?1 and mode=?2 limit 1",nativeQuery = true)
    public Integer get_Register_Account_Platform_And_Mode(String platform,String mode);

    @Query(value = "SELECT register_time FROM platform where platform=?1 and mode=?2 limit 1",nativeQuery = true)
    public Integer get_Time_Register_Account_Platform_And_Mode(String platform,String mode);

    @Query(value = "SELECT login_account FROM platform where platform=?1 and mode=?2 limit 1",nativeQuery = true)
    public Integer get_Login_Account_Platform_And_Mode(String platform,String mode);
    @Query(value = "SELECT version_app FROM platform where platform=?1 and mode=?2 limit 1",nativeQuery = true)
    public Long get_Version_App_Platform_And_Mode(String platform,String mode);

    @Query(value = "SELECT * FROM platform ",nativeQuery = true)
    public List<Platform> get_List_Platform();
    @Query(value = "SELECT * FROM platform where platform=?1 ",nativeQuery = true)
    public Platform get_Platform_By_PlatformId(String platform);

    @Query(value = "SELECT * FROM platform where platform=?1 and mode=?2 ",nativeQuery = true)
    public Platform get_Platform_By_Platform_And_Mode(String platform,String mode);

    @Modifying
    @Transactional
    @Query(value = "UPDATE platform SET state=1 where platform in(?1) and mode='auto'",nativeQuery = true)
    public Integer update_State_1_Platform(List<String> platform);

    @Modifying
    @Transactional
    @Query(value = "UPDATE platform SET state=0 where platform not in(?1) and mode='auto'",nativeQuery = true)
    public Integer update_State_0_Platform(List<String> platform);

}
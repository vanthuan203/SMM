package com.nts.awspremium.repositories;

import com.nts.awspremium.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User,String> {
    @Query(value = "Select count(*) from user where token=?1",nativeQuery = true)
    public Integer FindUserByToken(String Authorization);


    @Query(value = "Select * from user where username=?1 and password=?2 limit 1",nativeQuery = true)
    public List<User> FindAdminByUserPass(String username, String password);

    @Query(value = "Select count(*) from user where username=?1",nativeQuery = true)
    public Integer FindAdminByUser(String username);

    @Query(value = "Select * from user where username=?1 limit 1",nativeQuery = true)
    public List<User> getAdminByUser(String username);

    @Query(value = "call update_balance(?1,?2)",nativeQuery = true)
    public Float updateBalanceFine(Float a,String username);

    @Query(value = "Select username from user",nativeQuery = true)
    public List<String> GetAllUser();

    @Query(value = "Select * from user",nativeQuery = true)
    public List<User> GetAllUsers();

    @Query(value = "Select * from user where username=?1",nativeQuery = true)
    public List<User> GetAdminByUser(String username);

    @Query(value = "Select * from user where token=?1 limit 1",nativeQuery = true)
    public User  find_User_By_Token(String Authorization);
}

package com.nts.awspremium.repositories;

import com.nts.awspremium.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User,String> {
    @Query(value = "Select count(*) from user where token=?1",nativeQuery = true)
    public Integer check_User_By_Token(String Authorization);

    @Query(value = "Select token from user where username=?1 and password=?2 limit 1",nativeQuery = true)
    public String find_Token_User_By_User_Pass(String username, String password);

    @Query(value = "Select * from user where token=?1 limit 1",nativeQuery = true)
    public User  find_User_By_Token(String Authorization);

    @Query(value = "Select username from user",nativeQuery = true)
    public List<String> get_All_User();
}

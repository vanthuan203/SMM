package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Socks_IPV4;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface Socks_IPV4Repository extends JpaRepository<Socks_IPV4,String> {

    @Query(value = "SELECT * FROM socks_ipv4 where ip=?1",nativeQuery = true)
    public List<Socks_IPV4> getIPSocksByIp(String ip);
}

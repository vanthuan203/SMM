package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Proxy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProxyRepository extends JpaRepository<Proxy, Integer> {
    @Query(value = "SELECT * FROM proxy where state=1 order by timeget asc limit 1",nativeQuery = true)
    public List<Proxy> getProxy();

    @Query(value = "SELECT * FROM proxy where state=1 and (timeget is null or timeget=0) order by rand() asc limit 1",nativeQuery = true)
    public List<Proxy> getProxyTimeGetNull();

    @Query(value = "SELECT * FROM proxy where state=1 and proxy NOT LIKE ?1 order by timeget asc limit 1",nativeQuery = true)
    public List<Proxy> getProxy(String proxy);

    @Query(value = "SELECT * FROM proxy where state=1 and (timeget is null or timeget=0) and proxy NOT LIKE ? order by rand() asc limit 1",nativeQuery = true)
    public List<Proxy> getProxyTimeGetNull(String proxy);
}

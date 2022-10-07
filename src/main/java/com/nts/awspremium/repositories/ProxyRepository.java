package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Proxy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface ProxyRepository extends JpaRepository<Proxy, Integer> {
    @Query(value = "select * from proxy where proxy not in (SELECT proxy FROM AccPremium.proxyhistory where state=1 and round((UNIX_TIMESTAMP()-id/1000)/60)<5 group by proxy  order by count(proxy)  desc) order by running asc,rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxy();

    @Query(value = "select * from proxy order by timeget asc limit 1",nativeQuery = true)
    public List<Proxy> getProxySub();

    @Query(value = "select * from proxy where proxy not in (SELECT proxy FROM AccPremium.proxyhistory where state=1 and round((UNIX_TIMESTAMP()-id/1000)/60)<5 group by proxy  order by count(proxy)  desc) order by running asc,rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxyTimeGetNull();

    @Query(value = "select * from proxy where proxy NOT LIKE ?1 and proxy not in (SELECT proxy FROM AccPremium.proxyhistory where state=1 and round((UNIX_TIMESTAMP()-id/1000)/60)<5  group by proxy  order by count(proxy)  desc) order by running asc,rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxy(String proxy);

    @Query(value = "select * from proxy where proxy NOT LIKE ?1 and proxy not in (SELECT proxy FROM AccPremium.proxyhistory where state=1 and round((UNIX_TIMESTAMP()-id/1000)/60)<5  group by proxy  order by count(proxy)  desc) order by running asc,rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxyTimeGetNull(String proxy);

    @Query(value = "SELECT * FROM proxy where proxy=?1 limit 1",nativeQuery = true)
    public List<Proxy> findProxy(String proxy);
    @Query(value = "SELECT count(*) FROM proxy where proxy=?1",nativeQuery = true)
    public Integer checkproxynull(String proxy);

    @Query(value = "SELECT count(*) FROM proxy",nativeQuery = true)
    public Integer countProxy();

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM proxyhistory where round((UNIX_TIMESTAMP()-id/1000)/60/60) >3",nativeQuery = true)
    public Integer deleteProxyHisThan24h();
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM proxy where ipv4=?1 ",nativeQuery = true)
    public Integer deleteProxyByIpv4(String ipv4);
}

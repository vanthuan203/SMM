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

    @Query(value = "select * from proxy where state=1 order by timeget asc,rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxyUpdate();

    @Query(value = "select * from proxy where state=1 and typeproxy like ?1 order by timeget asc,rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxyBuff(String typeproxy);
    @Query(value = "select * from proxy where state=1 order by timeget asc,rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxyBuff();

    @Query(value = "SELECT ipv4 FROM AccPremium.proxy where typeproxy not like '%vn%' group by ipv4 ",nativeQuery = true)
    public List<String> getIpv4ProxyBuff();

    @Query(value = "select * from proxy where state=1 and proxy NOT LIKE ?1 order by timeget asc,rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxyBuffByIpv4(String proxy);

    @Query(value = "select * from proxy where state=1 and typeproxy like ?1 and proxy NOT LIKE ?2 order by timeget asc,rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxyBuffByIpv4(String typeproxy,String proxy);
    @Query(value = "select * from proxy where state=1 and proxy NOT LIKE ?1 order by timeget asc,rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxyUpdate(String proxy);

    @Query(value = "select * from proxy where  ipv4 in (select ipv4 from ipv4 where state=1 and timereset=20) and round((UNIX_TIMESTAMP()-timeget/1000)/60)>=20 order by timeget asc,rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxySubT1();

    @Query(value = "select * from proxy where  ipv4 in (select ipv4 from ipv4 where state=1 and timereset=30) and round((UNIX_TIMESTAMP()-timeget/1000)/60)>=60 order by timeget asc,rand() limit 1",nativeQuery = true)
    public List<Proxy> getProxySubT2();

    @Query(value = "SELECT * from proxy where proxy like ?1 and round((UNIX_TIMESTAMP()-timeget/1000)/60)>=20 order by timeget asc,rand() limit 1;",nativeQuery = true)
    public List<Proxy> getProxySubByIpv4T1(String ipv4);
    @Query(value = "SELECT * from proxy where proxy like ?1 and round((UNIX_TIMESTAMP()-timeget/1000)/60)>=60 order by timeget asc,rand() limit 1;",nativeQuery = true)
    public List<Proxy> getProxySubByIpv4T2(String ipv4);


    @Query(value = "select * from proxy where state=1  order by timeget asc limit 1",nativeQuery = true)
    public List<Proxy> getProxyV4();

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

    @Query(value = "SELECT count(*) FROM proxy where state=?1 and proxy like ?2 limit 1",nativeQuery = true)
    public Integer checkState(Integer state,String ipv4);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM proxyhistory where round((UNIX_TIMESTAMP()-id/1000)/60/60) >3",nativeQuery = true)
    public Integer deleteProxyHisThan24h();

    @Modifying
    @Transactional
    @Query(value = "update proxy set typeproxy='pending' where ipv4=?1 limit 250",nativeQuery = true)
    public Integer updatepending(String ipv4);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM proxy where ipv4=?1 ",nativeQuery = true)
    public Integer deleteProxyByIpv4(String ipv4);

    @Modifying
    @Transactional
    @Query(value = "UPDATE proxy set state=?1  where ipv4 like ?2 ",nativeQuery = true)
    public Integer updateState(Integer state,String ipv4);
}

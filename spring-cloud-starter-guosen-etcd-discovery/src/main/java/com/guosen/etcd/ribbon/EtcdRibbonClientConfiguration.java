package com.guosen.etcd.ribbon;

import com.guosen.etcd.EtcdDiscoveryProperties;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import com.netflix.loadbalancer.ServerListFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnRibbonEtcd
public class EtcdRibbonClientConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServerList<?> ribbonServerList(IClientConfig config, EtcdDiscoveryProperties etcdDiscoveryProperties) {
        EtcdServerList serverList = new EtcdServerList(etcdDiscoveryProperties);
        serverList.initWithNiwsConfig(config);
        return serverList;
    }

}

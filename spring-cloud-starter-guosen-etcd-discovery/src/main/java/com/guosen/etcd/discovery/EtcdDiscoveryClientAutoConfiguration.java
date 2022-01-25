package com.guosen.etcd.discovery;

import com.guosen.etcd.ConditionalOnEtcdDiscoveryEnabled;
import com.guosen.etcd.EtcdDiscoveryProperties;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.cloud.client.CommonsClientAutoConfiguration;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnEtcdDiscoveryEnabled
@AutoConfigureBefore({ SimpleDiscoveryClientAutoConfiguration.class,
        CommonsClientAutoConfiguration.class })
public class EtcdDiscoveryClientAutoConfiguration {

//    @Bean
//    public EtcdDiscoveryProperties etcdProperties() {
//        return new EtcdDiscoveryProperties();


//    }

    @Bean
    public EtcdDiscoveryClient etcdDiscoveryClient(EtcdDiscoveryProperties etcdDiscoveryProperties) {
        return new EtcdDiscoveryClient(etcdDiscoveryProperties);
    }
}

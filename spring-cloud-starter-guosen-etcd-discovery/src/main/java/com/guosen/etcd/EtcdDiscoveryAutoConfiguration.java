package com.guosen.etcd;

import com.guosen.etcd.registry.EtcdAutoServiceRegistration;
import com.guosen.etcd.registry.EtcdRegistration;
import com.guosen.etcd.registry.EtcdServiceRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EtcdDiscoveryProperties.class)
@ConditionalOnProperty(value = "spring.cloud.service-registry.auto-registration.enabled", matchIfMissing = true)
@AutoConfigureAfter({ AutoServiceRegistrationConfiguration.class,
        AutoServiceRegistrationAutoConfiguration.class })
public class EtcdDiscoveryAutoConfiguration {

    @Bean
    public EtcdServiceRegistry etcdServiceRegistry(EtcdDiscoveryProperties etcdDiscoveryProperties) {
        return new EtcdServiceRegistry(etcdDiscoveryProperties);
    }

    @Bean
    @ConditionalOnBean(AutoServiceRegistrationProperties.class)
    public EtcdRegistration etcdRegistration(EtcdDiscoveryProperties etcdDiscoveryProperties) {
        return new EtcdRegistration(etcdDiscoveryProperties);
    }

    @Bean
    @ConditionalOnBean(AutoServiceRegistrationProperties.class)
    public EtcdAutoServiceRegistration etcdAutoServiceRegistration(EtcdServiceRegistry etcdServiceRegistry,
                                       AutoServiceRegistrationProperties autoServiceRegistrationProperties,
                                       EtcdRegistration etcdRegistration) {
        return new EtcdAutoServiceRegistration(etcdServiceRegistry, autoServiceRegistrationProperties, etcdRegistration);

    }
}

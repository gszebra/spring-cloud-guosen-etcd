package com.guosen.etcd.registry;

import com.guosen.etcd.EtcdDiscoveryProperties;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

public class EtcdRegistration implements Registration {

    private final EtcdDiscoveryProperties etcdDiscoveryProperties;

    public EtcdRegistration(EtcdDiscoveryProperties etcdDiscoveryProperties) {
        this.etcdDiscoveryProperties = etcdDiscoveryProperties;
    }

    @Override
    public String getServiceId() {
        return etcdDiscoveryProperties.getService();
    }

    @Override
    public String getHost() {
        return etcdDiscoveryProperties.getIp();
    }

    @Override
    public int getPort() {
        return etcdDiscoveryProperties.getPort();
    }

    public void setPort(int port) {
        etcdDiscoveryProperties.setPort(port);
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public URI getUri() {
        return DefaultServiceInstance.getUri(this);
    }

    @Override
    public Map<String, String> getMetadata() {
        return Collections.emptyMap();
    }

    public EtcdDiscoveryProperties getEtcdDiscoveryProperties() {
        return etcdDiscoveryProperties;
    }
}

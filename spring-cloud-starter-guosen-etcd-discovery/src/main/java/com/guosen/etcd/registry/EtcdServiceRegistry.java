package com.guosen.etcd.registry;

import com.alibaba.fastjson.JSONObject;
import com.coreos.jetcd.Client;
import com.coreos.jetcd.Lease;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.guosen.etcd.EtcdDiscoveryProperties;
import com.guosen.etcd.EtcdServiceInstance;
import com.guosen.etcd.util.EtcdClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Etcd registry implementation
 */
public class EtcdServiceRegistry implements ServiceRegistry<EtcdRegistration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EtcdServiceRegistry.class);

    private final ScheduledExecutorService SCHEDULE_EXECUTOR =
            Executors.newScheduledThreadPool(1,
                    new ThreadFactoryBuilder()
                            .setNameFormat("EtcdRefresh-pool-")
                            .setDaemon(true)
                            .build());

    private final EtcdDiscoveryProperties etcdDiscoveryProperties;

    private final Client client;

    private String registryKey;

    private Long leaseId;

    private final long ttl;

    private String registerInfo;

    public EtcdServiceRegistry(EtcdDiscoveryProperties etcdDiscoveryProperties) {
        this.etcdDiscoveryProperties = etcdDiscoveryProperties;
        this.client = etcdDiscoveryProperties.getClient();
        this.ttl = etcdDiscoveryProperties.getTtl();
    }

    @Override
    public void register(EtcdRegistration registration) {

        registryKey = getServiceEtcdPath(registration);

        EtcdServiceInstance etcdServiceInstance = new EtcdServiceInstance();
        etcdServiceInstance.setServiceId(etcdDiscoveryProperties.getService());
        etcdServiceInstance.setHost(registration.getHost());
        etcdServiceInstance.setPort(registration.getPort());
        etcdServiceInstance.setSecure(registration.isSecure());
        etcdServiceInstance.setMetadata(registration.getMetadata());

        registerInfo = JSONObject.toJSONString(etcdServiceInstance);

        leaseId = EtcdClientUtil.registerWithLease(client, registryKey, registerInfo, ttl);

        SCHEDULE_EXECUTOR.scheduleAtFixedRate(this::refreshTtl, 0, ttl, TimeUnit.SECONDS);
    }

    @Override
    public void deregister(EtcdRegistration registration) {
        SCHEDULE_EXECUTOR.shutdown();
    }

    @Override
    public void close() {

    }

    @Override
    public void setStatus(EtcdRegistration registration, String status) {

    }

    @Override
    public <T> T getStatus(EtcdRegistration registration) {
        return null;
    }

    public void refreshTtl() {
        int checkTimeout = (int)etcdDiscoveryProperties.getTtl() / 2;
        if (!EtcdClientUtil.checkKeyExist(client, registryKey, checkTimeout)) {
            LOGGER.warn("register key {} is not in etcd, refresh it.", registryKey);
            leaseId = EtcdClientUtil.registerWithLease(client, registryKey, registerInfo, ttl);
            return;
        }

        Lease lease = client.getLeaseClient();
        if (!EtcdClientUtil.checkLeaseExist(lease, leaseId, checkTimeout)) {
            leaseId = EtcdClientUtil.registerWithLease(client, registryKey, registerInfo, ttl);
            return;
        }

        if (!EtcdClientUtil.keepAliveOnce(lease, leaseId, checkTimeout)) {
            EtcdClientUtil.revoke(lease, leaseId, checkTimeout);
            leaseId = EtcdClientUtil.registerWithLease(client, registryKey, registerInfo, ttl);
        }
    }

    private String getServiceEtcdPath(EtcdRegistration registration) {

        String prefix = etcdDiscoveryProperties.getDiscoveryPrefix();

        // /spring/cloud/etcd/default/com.guosen.HelloService/10a41371-a71e-4460-a8ab-6caa2dd7a487
        return prefix + "/" + etcdDiscoveryProperties.getGroup() + "/" + registration.getServiceId() + "/" + UUID.randomUUID();
    }


}

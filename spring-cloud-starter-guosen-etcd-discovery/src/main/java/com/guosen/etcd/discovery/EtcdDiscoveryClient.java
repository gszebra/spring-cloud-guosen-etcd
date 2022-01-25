package com.guosen.etcd.discovery;

import com.alibaba.fastjson.JSONObject;
import com.coreos.jetcd.Client;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;
import com.guosen.etcd.EtcdDiscoveryProperties;
import com.guosen.etcd.EtcdServiceInstance;
import com.guosen.etcd.util.EtcdClientUtil;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EtcdDiscoveryClient implements DiscoveryClient {

    private EtcdDiscoveryProperties etcdDiscoveryProperties;

    public EtcdDiscoveryClient(EtcdDiscoveryProperties etcdDiscoveryProperties) {
        this.etcdDiscoveryProperties = etcdDiscoveryProperties;
    }

    @Override
    public String description() {
        return "Spring Cloud Etcd Discovery Client";
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {

        Client client = etcdDiscoveryProperties.getClient();
        String prefix = etcdDiscoveryProperties.getDiscoveryPrefix();
        String group = etcdDiscoveryProperties.getGroup();

        String path = prefix + "/" + group + "/" + serviceId;

        List<KeyValue> kvs = EtcdClientUtil.getByPrefix(client, path, etcdDiscoveryProperties.getTtl());
        if (CollectionUtils.isEmpty(kvs)) {
            return Collections.emptyList();
        }

        return kvs.stream()
                .map((keyValue) -> toServiceInstance(serviceId, keyValue))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getServices() {
        Client client = etcdDiscoveryProperties.getClient();
        String prefix = etcdDiscoveryProperties.getDiscoveryPrefix();
        String group = etcdDiscoveryProperties.getGroup();
        String serviceIdPrefix = prefix + "/" + group;

        List<KeyValue> kvs = EtcdClientUtil.getByPrefix(client, serviceIdPrefix, etcdDiscoveryProperties.getTtl());
        if (CollectionUtils.isEmpty(kvs)) {
            return Collections.emptyList();
        }

        return kvs.stream()
                .map(KeyValue::getKey)
                .map(ByteSequence::toStringUtf8)
                .collect(Collectors.toList());
    }

    private ServiceInstance toServiceInstance(String serviceId, KeyValue keyValue) {
        String valueStr = keyValue.getValue().toStringUtf8();
        JSONObject value = JSONObject.parseObject(valueStr);

        return value.toJavaObject(EtcdServiceInstance.class);
    }
}

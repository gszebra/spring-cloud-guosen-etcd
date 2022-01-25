package com.guosen.etcd.ribbon;

import com.alibaba.fastjson.JSONObject;
import com.coreos.jetcd.Client;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;
import com.guosen.etcd.EtcdDiscoveryProperties;
import com.guosen.etcd.util.EtcdClientUtil;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EtcdServerList extends AbstractServerList<EtcdServer> {

    private final EtcdDiscoveryProperties discoveryProperties;

    private String serviceId;

    public EtcdServerList(EtcdDiscoveryProperties discoveryProperties) {
        this.discoveryProperties = discoveryProperties;
    }

    @Override
    public List<EtcdServer> getInitialListOfServers() {
        return getServers();
    }

    @Override
    public List<EtcdServer> getUpdatedListOfServers() {
        return getServers();
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
        this.serviceId = clientConfig.getClientName();
    }

    private List<EtcdServer> getServers() {
        Client client = discoveryProperties.getClient();
        String prefix = discoveryProperties.getDiscoveryPrefix();
        String group = discoveryProperties.getGroup();
        String serviceIdPrefix = prefix + "/" + group + "/" + serviceId;

        List<KeyValue> kvs = EtcdClientUtil.getByPrefix(client, serviceIdPrefix, discoveryProperties.getTtl());
        if (CollectionUtils.isEmpty(kvs)) {
            return Collections.emptyList();
        }

        return kvs.stream()
                .map((keyValue) -> toEtcdServer(serviceId, keyValue))
                .collect(Collectors.toList());
    }

    private EtcdServer toEtcdServer(String serviceId, KeyValue keyValue) {
        String valueStr = keyValue.getValue().toStringUtf8();
        JSONObject value = JSONObject.parseObject(valueStr);

        String host = value.getString("host");
        int port = value.getInteger("port");
        EtcdServer etcdServer = new EtcdServer(host, port);

        return etcdServer;

    }
}

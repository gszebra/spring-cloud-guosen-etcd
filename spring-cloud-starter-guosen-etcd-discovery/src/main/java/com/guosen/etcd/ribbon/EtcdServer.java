package com.guosen.etcd.ribbon;

import com.netflix.loadbalancer.Server;

public class EtcdServer extends Server {
    public EtcdServer(String host, int port) {
        super(host, port);
    }
}

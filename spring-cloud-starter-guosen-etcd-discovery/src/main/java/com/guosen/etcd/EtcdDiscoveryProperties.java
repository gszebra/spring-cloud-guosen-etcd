package com.guosen.etcd;

import com.coreos.jetcd.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("spring.cloud.etcd.discovery")
public class EtcdDiscoveryProperties {

    @Value("${spring.cloud.etcd.discovery.service:${spring.application.name:}}")
    private String service;

    /**
     * Etcd server address
     */
    private String serverAddr;

    /**
     * The ip address your want to register for your service instance, needn't to set it
     * if the auto detect ip works well.
     */
    private String ip;

    /**
     * Port
     */
    private int port;

    /**
     * which network interface's ip you want to register
     */
    private String networkInterface = "";

    /**
     * discovery prefix for spring cloud etcd
     */
    private String discoveryPrefix = "/spring/cloud/etcd";

    /**
     * 服务组只允许本服务的发现
     */
    private String group = "default";

    /**
     * 服务TTL时间
     */
    private int ttl = 10;

    /**
     * extra metadata to register.
     */
    private Map<String, String> metadata = new HashMap<>();

    /**
     * if you just want to subscribe, but don't want to register your service, set it to
     * false.
     */
    private boolean registerEnabled = true;

    /**
     * Etcd client
     */
    private Client client;


    @Autowired
    private InetUtils inetUtils;

    @PostConstruct
    public void init() throws SocketException {


        if (StringUtils.isEmpty(ip)) {
            // traversing network interfaces if didn't specify a interface
            if (StringUtils.isEmpty(networkInterface)) {
                ip = inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
            }
            else {
                NetworkInterface netInterface = NetworkInterface
                        .getByName(networkInterface);
                if (null == netInterface) {
                    throw new IllegalArgumentException(
                            "no such interface " + networkInterface);
                }

                Enumeration<InetAddress> inetAddress = netInterface.getInetAddresses();
                while (inetAddress.hasMoreElements()) {
                    InetAddress currentAddress = inetAddress.nextElement();
                    if (currentAddress instanceof Inet4Address
                            && !currentAddress.isLoopbackAddress()) {
                        ip = currentAddress.getHostAddress();
                        break;
                    }
                }

                if (StringUtils.isEmpty(ip)) {
                    throw new RuntimeException("cannot find available ip from"
                            + " network interface " + networkInterface);
                }

            }
        }
    }

    public Client getClient() {
        if (client != null) {
            return client;
        }

        if (StringUtils.isEmpty(serverAddr)) {
            throw new IllegalArgumentException("Invalid serverAddr " + serverAddr);
        }
        String[] serverAddrs = serverAddr.split(",");
        client = Client.builder().endpoints(serverAddrs).build();

        return client;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDiscoveryPrefix() {
        return discoveryPrefix;
    }

    public void setDiscoveryPrefix(String discoveryPrefix) {
        this.discoveryPrefix = discoveryPrefix;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public boolean getRegisterEnabled() {
        return registerEnabled;
    }

    public void setRegisterEnabled(boolean registerEnabled) {
        this.registerEnabled = registerEnabled;
    }
}

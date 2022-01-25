package com.guosen.etcd.registry;

import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;

public class EtcdAutoServiceRegistration extends AbstractAutoServiceRegistration<EtcdRegistration>  {

    private EtcdRegistration etcdRegistration;


    public EtcdAutoServiceRegistration(EtcdServiceRegistry serviceRegistry,
                                       AutoServiceRegistrationProperties autoServiceRegistrationProperties,
                                       EtcdRegistration etcdRegistration) {
        super(serviceRegistry, autoServiceRegistrationProperties);
        this.etcdRegistration = etcdRegistration;
    }

    @Override
    protected void register() {
        this.etcdRegistration.setPort(getPort().get());
        super.register();
    }

    @Override
    protected Object getConfiguration() {
        return etcdRegistration.getEtcdDiscoveryProperties();
    }

    @Override
    protected boolean isEnabled() {
        return etcdRegistration.getEtcdDiscoveryProperties().getRegisterEnabled();
    }

    @Override
    protected EtcdRegistration getRegistration() {
        return etcdRegistration;
    }

    @Override
    protected EtcdRegistration getManagementRegistration() {
        return null;
    }
}

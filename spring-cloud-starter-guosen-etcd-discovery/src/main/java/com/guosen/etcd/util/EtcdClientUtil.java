package com.guosen.etcd.util;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.Lease;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.lease.LeaseGrantResponse;
import com.coreos.jetcd.lease.LeaseKeepAliveResponse;
import com.coreos.jetcd.lease.LeaseRevokeResponse;
import com.coreos.jetcd.lease.LeaseTimeToLiveResponse;
import com.coreos.jetcd.options.GetOption;
import com.coreos.jetcd.options.LeaseOption;
import com.coreos.jetcd.options.PutOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Etcd client util
 */
public final class EtcdClientUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(EtcdClientUtil.class);

    private EtcdClientUtil(){}


    public static Long registerWithLease(Client client, String key, String value, long ttl) {
        Lease lease = client.getLeaseClient();
        LeaseGrantResponse resp;
        try {
            resp = lease.grant(ttl).get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }

        long leaseId = resp.getID();
        PutOption option = PutOption.newBuilder().withLeaseId(leaseId).build();
        client.getKVClient().put(ByteSequence.fromString(key), ByteSequence.fromString(value), option);

        return leaseId;
    }

    /**
     * Check if etcd key exist
     * @param client    Etcd client
     * @param key       key to be check
     * @param checkTimeOutSecond  the check operation timeout
     * @return  true : exist; false : no exist
     */
    public static boolean checkKeyExist(Client client, String key, int checkTimeOutSecond) {
        GetResponse getResponse = null;
        try {
            getResponse = client.getKVClient().get(ByteSequence.fromString(key)).get(checkTimeOutSecond, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.error("Failed to get etcd key : {}", key, e);
        }

        return getResponse != null && getResponse.getCount() != 0;
    }

    /**
     * Check if etcd lease exist
     * @param client    Etcd client
     * @param leaseId       leaseId to be check
     * @param checkTimeOutSecond  the check operation timeout
     * @return  true : exist; false : no exist
     */
    public static boolean checkLeaseExist(Lease lease, long leaseId, int checkTimeOutSecond) {
        LeaseTimeToLiveResponse leaseTimeToLiveResponse = null;
        try {
            leaseTimeToLiveResponse = lease.timeToLive(leaseId, LeaseOption.DEFAULT).get(checkTimeOutSecond, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.error("Failed to get etcd lease : {}", leaseId, e);
        }

        return leaseTimeToLiveResponse != null;
    }


    public static boolean keepAliveOnce(Lease lease, long leaseId, int checkTimeOutSecond) {
        LeaseKeepAliveResponse resp = null;
        try {
            resp = lease.keepAliveOnce(leaseId).get(checkTimeOutSecond, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.error("Failed to keepAliveOnce etcd lease : {}", leaseId, e);
        }

        return resp != null;
    }


    public static boolean revoke(Lease lease, long leaseId, int checkTimeOutSecond) {
        LeaseRevokeResponse leaseRevokeResponse = null;
        try {
            leaseRevokeResponse = lease.revoke(leaseId).get(checkTimeOutSecond, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.error("Failed to revoke etcd lease : {}", leaseId, e);
        }

        return leaseRevokeResponse != null;
    }

    public static List<KeyValue> getByPrefix(Client client, String prefix, int checkTimeOutSecond) {
        GetResponse getResponse = null;
        ByteSequence prefixByteSequence = ByteSequence.fromBytes(prefix.getBytes(StandardCharsets.UTF_8));

        try {
            getResponse = client.getKVClient()
                    .get(prefixByteSequence, GetOption.newBuilder().withPrefix(prefixByteSequence).build())
                    .get(checkTimeOutSecond, TimeUnit.SECONDS);
            return getResponse.getKvs();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.error("Failed to get etcd prefix : {}", prefix, e);
        }

        return Collections.emptyList();
    }
}

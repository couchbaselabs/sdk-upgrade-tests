package com.couchbase.utils;

public class ClusterTestInfo {
    String cluster;
    String user;
    String password;
    String bucket;

    public String getCluster() {
        return cluster;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getBucket() {
        return bucket;
    }

    public String getScope() {
        return scope;
    }

    public String getCollection() {
        return collection;
    }

    String scope;
    String collection;
}

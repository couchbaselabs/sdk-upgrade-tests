package com.couchbase.utils;

import com.couchbase.grpc.protocol.CreateConnectionRequest;

public class RequestVariableExtractor {


  private static ClusterTestInfo testInfo = new ClusterTestInfo();

  public static void extractConnectionRequest(CreateConnectionRequest request) {
    testInfo.cluster = request.getClusterHostname();
    testInfo.user = request.getClusterUsername();
    testInfo.password = request.getClusterPassword();
    testInfo.bucket = request.getBucketName();
    testInfo.scope = request.getScopeName();
    testInfo.collection = request.getCollectionName();

    System.out.println("Setting cluster to : " + testInfo.cluster);
    System.out.println("bucket: "+ testInfo.bucket);
    System.out.println("Collection: " + testInfo.collection);

    System.setProperty("cluster", testInfo.cluster);
    System.setProperty("clusterUser", testInfo.user);
    System.setProperty("clusterPassword", testInfo.password);
    System.setProperty("bucket", testInfo.bucket);
    System.setProperty("scope", testInfo.scope);
    System.setProperty("collection", testInfo.collection);


  }

  public static ClusterTestInfo getTestInfo() {
    return testInfo;
  }
}

package com.couchbase.test;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.time.Duration;

public class UpgradeTestBase {
  private static String clusterName;
  private static String user;
  private static String password;
  private static String bucketName;
  private static String scopeName;
  private static String collectionName;

  public static Cluster cluster;
  public static Bucket bucket;
  public static Scope scope;
  public static Collection collection;

  @BeforeAll
  public static void CreateCouchbaseConnection() {
    clusterName = System.getProperty("cluster");
    user = System.getProperty("clusterUser");
    password = System.getProperty("clusterPassword");
    bucketName = System.getProperty("bucket");
    scopeName = System.getProperty("scope");
    collectionName = System.getProperty("collection");

    System.out.println("GOT CLUSTER VALUE: " + clusterName);


    cluster = Cluster.connect(clusterName, user, password);
    cluster.waitUntilReady(Duration.ofSeconds(15L));

    bucket = cluster.bucket(bucketName);
    scope = bucket.scope(scopeName);
    collection = scope.collection(collectionName);
  }

  @AfterAll
  static void tearDown() {
    cluster.disconnect();
  }
}

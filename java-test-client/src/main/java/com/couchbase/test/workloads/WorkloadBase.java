package com.couchbase.test.workloads;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.utils.ClusterTestInfo;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class WorkloadBase {

  public Cluster cluster;
  public Bucket bucket;
  public Scope scope;
  public Collection collection;

  public Map<String, Integer> results;
  public boolean stopped = false;
  private double passPercentage = 0.99;
  private double successRatio;


  public WorkloadBase(ClusterTestInfo info) {
    cluster = Cluster.connect(info.getCluster(), info.getUser(), info.getPassword());
    //cluster.waitUntilReady(Duration.ofSeconds(15L));

    bucket = cluster.bucket(info.getBucket());
    scope = bucket.scope(info.getScope());
    collection = scope.collection(info.getCollection());

    results = new HashMap<>();
  }

  public void run() {

  }

  public void stop(){
    stopped = true;
    boolean passed = testPassed();
    //System.out.println(String.format("Workload %s with percentage %f", passed ? "passed" : "failed", successRatio));
    System.out.println(String.format("Workload %s", passed ? "passed" : "failed"));

  }


  public boolean testPassed() {
    AtomicReference<Double> success = new AtomicReference<>(0.0);
    AtomicReference<Double> totalErrors = new AtomicReference<>(0.0);

    results.forEach((s, i) -> {
      if (!s.equals("SUCCESS")) {
        totalErrors.updateAndGet(v -> v + i);
      } else {
        success.set(Double.valueOf(i));
      }
    });

    successRatio = success.get() / totalErrors.get();

    return successRatio > passPercentage;
  }

  //TODO: Timestamped results so can accurately determine if test passed
  public void addResult(String s) {
    try {
      int n = results.get(s);
      results.replace(s, n, ++n);
    } catch (NullPointerException p) {
      results.put(s, 1);
    }
  }
}

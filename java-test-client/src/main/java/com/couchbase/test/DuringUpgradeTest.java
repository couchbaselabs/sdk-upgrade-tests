package com.couchbase.test;

import com.couchbase.test.workloads.KeyValueWorkload;
import com.couchbase.test.workloads.QueryWorkload;
import com.couchbase.test.workloads.TransactionsWorkload;
import com.couchbase.test.workloads.WorkloadBase;
import com.couchbase.utils.ClusterTestInfo;

import java.util.ArrayList;
import java.util.List;

public class DuringUpgradeTest {

  ClusterTestInfo info;
  List<WorkloadBase> workloadsToRun = new ArrayList<>();

  KeyValueWorkload keyValueWorkload;
  QueryWorkload queryWorkload;
  TransactionsWorkload txnWorkload;


  public DuringUpgradeTest(ClusterTestInfo info) {
    this.info = info;
    //TODO: Pick the workloads to run from cmdline
    keyValueWorkload = new KeyValueWorkload(info);
    queryWorkload = new QueryWorkload(info);
    txnWorkload = new TransactionsWorkload(info);


    workloadsToRun.add(keyValueWorkload);
    workloadsToRun.add(queryWorkload);
    workloadsToRun.add(txnWorkload);
  }

  public void run() {
    for(WorkloadBase workload : workloadsToRun) {
      Thread thread = new Thread(workload);
      thread.start();
    }
  }

  public void stop() {
    for(WorkloadBase workload : workloadsToRun) {
      workload.stop();//Will exit run() and thread will be destroyed automatically
    }

//    keyValueWorkload.stop();
//    keyValueWorkload.results.forEach((s, i) ->
//            System.out.println(String.format("Got %s , %d times.", s, i)));
//    queryWorkload.stop();
//    queryWorkload.results.forEach((s, i) ->
//            System.out.println(String.format("Got %s , %d times.", s, i)));
  }

}

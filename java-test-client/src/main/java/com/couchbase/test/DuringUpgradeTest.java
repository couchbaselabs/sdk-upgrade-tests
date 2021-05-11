package com.couchbase.test;

import com.couchbase.grpc.protocol.Workload;
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


  public DuringUpgradeTest(ClusterTestInfo info, List<Workload> workloads) {
    this.info = info;
    workloads.forEach(workload -> {
      switch (workload) {
        case KV:
          KeyValueWorkload keyValueWorkload = new KeyValueWorkload(info);
          workloadsToRun.add(keyValueWorkload);
          break;
        case QUERY:
          QueryWorkload queryWorkload = new QueryWorkload(info);
          workloadsToRun.add(queryWorkload);
          break;
        case TXN_KV:
          TransactionsWorkload txnWorkload = new TransactionsWorkload(info, false);
          workloadsToRun.add(txnWorkload);
          break;
        case TXN_QUERY:
          TransactionsWorkload txnQueryWorkload = new TransactionsWorkload(info, true);
          workloadsToRun.add(txnQueryWorkload);
        case UNRECOGNIZED:
          break;
      }
    });
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
  }

}

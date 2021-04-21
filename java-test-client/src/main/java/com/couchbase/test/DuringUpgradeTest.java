package com.couchbase.test;

import com.couchbase.test.workloads.KeyValueWorkload;
import com.couchbase.test.workloads.QueryWorkload;
import com.couchbase.utils.ClusterTestInfo;

//TODO: Make list of workloads that are run amd add the right ones to a list dependent on services
public class DuringUpgradeTest {

  ClusterTestInfo info;
  KeyValueWorkload keyValueWorkload;
  QueryWorkload queryWorkload;


  public DuringUpgradeTest(ClusterTestInfo info) {
    this.info = info;
    keyValueWorkload = new KeyValueWorkload(info);
    queryWorkload = new QueryWorkload(info);
  }

  public void run() {
//    keyValueWorkload.run();
    queryWorkload.run();
  }

  public void stop() {
//    keyValueWorkload.stop();
//    keyValueWorkload.results.forEach((s, i) ->
//            System.out.println(String.format("Got %s , %d times.", s, i)));
    queryWorkload.stop();
    queryWorkload.results.forEach((s, i) ->
            System.out.println(String.format("Got %s , %d times.", s, i)));
  }

}

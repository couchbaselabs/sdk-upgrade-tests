package com.couchbase.test.workloads;

import com.couchbase.utils.ClusterTestInfo;

public class KeyValueWorkload extends WorkloadBase{

  public KeyValueWorkload(ClusterTestInfo info) {
    super(info);
  }

  @Override
  public void run() {
    int id = 0;
    String res;
    while (!stopped && id < 1400) {
      try {
        collection.upsert(String.valueOf(id), "Foo: bar " + id);
        res = "SUCCESS";
      } catch (Exception e){
        res = e.getMessage();
      }
      addResult(res);
      id++;
      //id %= 1000;
    }
  }

}

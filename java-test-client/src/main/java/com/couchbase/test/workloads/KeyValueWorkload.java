package com.couchbase.test.workloads;

import com.couchbase.utils.ClusterTestInfo;

public class KeyValueWorkload extends WorkloadBase{

  public KeyValueWorkload(ClusterTestInfo info) {
    super(info);
  }

  //TODO: Add complete CRUD ops
  @Override
  public void run() {
    System.out.println("RUNNING KV");
    int id = 0;
    String res;
    while (!stopped) {
      try {
        collection.upsert(String.valueOf(id), "Foo: bar " + id);
        res = "SUCCESS";
      } catch (Exception e){
        res = e.getMessage();
      }
      addResult(res);
      id++;
      id %= 15000;
    }
  }

}

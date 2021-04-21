package com.couchbase.test.workloads;

import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.utils.ClusterTestInfo;
import com.google.protobuf.RpcUtil;

public class QueryWorkload extends WorkloadBase{
  public QueryWorkload(ClusterTestInfo info) {
    super(info);
  }

  String text = "Finally, be strong in the Lord and in his mighty power. Put on the full armor of God so that you can take your stand against the devil’s schemes. For our struggle is not against flesh and blood, but against the rulers, against the authorities, against the powers of this dark world and against the spiritual forces of evil in the heavenly realms. Therefore put on the full armor of God, so that when the day of evil comes, you may be able to stand your ground, and after you have done everything, to stand. Stand firm then, with the belt of truth buckled around your waist, with the breastplate of righteousness in place, and with your feet fitted with the readiness that comes from the gospel of peace. In addition to all this, take up the shield of faith, with which you can extinguish all the flaming arrows of the evil one. Take the helmet of salvation and the sword of the Spirit, which is the word of God. And pray in the Spirit on all occasions with all kinds of prayers and requests. With this in mind, be alert and always keep on praying for all the saints.";

  @Override
  public void run() {
    extraLoad();

    int id = 0;
    String res;
    String statement = "SELECT * from `" + bucket.name() + "` WHERE `tag` = \"n1ql\"";
    while (!stopped && id < 1100) {
      try {
        QueryResult queryResult = cluster.query(statement, QueryOptions.queryOptions().adhoc(false));
        System.out.println(queryResult.metaData().status());
        res = "SUCCESS";
      } catch (Exception e){
        res = e.getMessage();
      }
      addResult(res);
      id++;
      //id %= 1000;
    }
    System.out.println("DONE QUERIES");
  }

  private void extraLoad() {
    int id = 0;
    String res;
    while (id < 10000) {
      try {
        JsonObject obj = JsonObject.create().put("type", "n1qldoc")
                .put("id", id)
                .put("tag", "n1ql")
                .put("bible_stuff", text);
        collection.upsert(String.valueOf(id), obj);//"{\"type\":\"n1qldoc\",\"id\":\""+ id+"\",\"tag\":\"n1ql\"}");
      } catch (Exception e){
      }
      id++;
      //id %= 1000;
    }
  }
}

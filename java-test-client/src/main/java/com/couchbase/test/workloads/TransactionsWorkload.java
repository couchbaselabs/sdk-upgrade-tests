package com.couchbase.test.workloads;

import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.transactions.TransactionGetResult;
import com.couchbase.transactions.error.TransactionFailed;
import com.couchbase.utils.ClusterTestInfo;
import com.couchbase.transactions.Transactions;
import com.couchbase.transactions.config.TransactionConfigBuilder;
import com.couchbase.transactions.TransactionDurabilityLevel;


//TODO: Add query txns
public class TransactionsWorkload extends WorkloadBase{
  private static Transactions transactions;
  private static boolean queryMode;
  private static final int MAX_DOCS = 15000;

  public TransactionsWorkload(ClusterTestInfo info, boolean queryMode) {
    super(info);
    this.queryMode = queryMode;
    transactions = Transactions.create(cluster,
      TransactionConfigBuilder.create().durabilityLevel(TransactionDurabilityLevel.PERSIST_TO_MAJORITY)
        .build());
  }

  @Override
  public void run() {
    System.out.println("RUNNING TXNS" + (queryMode ? " WITH QUERY" : " WITH KV ONLY"));
    String randomIdString = "txnId";

    JsonObject insertObject = JsonObject.create().put("content", "txn-inserted");
    JsonObject replaceObject = JsonObject.create().put("content", "txn-replaced");
    JsonObject queryInsert = JsonObject.create().put("content", "txn-query-insert");

    String res;


    int i = 0;
    while(!stopped) {
      int finalI = i;
      try{
        transactions.run(ctx -> {
          if (!queryMode) {
            ctx.insert(collection, finalI + randomIdString, insertObject);

            if(finalI > 0) {
              TransactionGetResult transactionGetResult = ctx.get(collection, (finalI-1) + randomIdString);
              ctx.replace(transactionGetResult, replaceObject);
            }
          } else {
            ctx.query("INSERT INTO " + bucket.name() + " VALUES ('" + (MAX_DOCS + finalI) + randomIdString + "', " + queryInsert + ")");
            if(finalI > 0) {
              ctx.query("UPDATE " + bucket.name() + " SET content = 'updated-query' WHERE META().id = '" + (MAX_DOCS + finalI - 1) + "'");
            }
          }
        });
        res  = "SUCCESS";
      } catch (TransactionFailed e) {
        res = e.getMessage();
      } catch (Exception e) {
        //Shouldnt get here;
        //TODO: Add logger
        res = e.getMessage();
      }
      addResult(res);
      i++;
      i %= MAX_DOCS;
    }

  }
}

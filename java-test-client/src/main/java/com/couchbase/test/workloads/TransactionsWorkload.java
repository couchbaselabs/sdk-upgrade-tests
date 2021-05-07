package com.couchbase.test.workloads;

import com.couchbase.client.java.json.JsonObject;
import com.couchbase.transactions.TransactionGetResult;
import com.couchbase.transactions.error.TransactionFailed;
import com.couchbase.utils.ClusterTestInfo;
import com.couchbase.transactions.Transactions;
import com.couchbase.transactions.config.TransactionConfigBuilder;
import com.couchbase.transactions.TransactionDurabilityLevel;


//TODO: Add query txns
public class TransactionsWorkload extends WorkloadBase{
  private static Transactions transactions;

  public TransactionsWorkload(ClusterTestInfo info) {
    super(info);
    transactions = Transactions.create(cluster,
      TransactionConfigBuilder.create().durabilityLevel(TransactionDurabilityLevel.PERSIST_TO_MAJORITY)
        .build());
  }

  @Override
  public void run() {
    String randomIdString = "txnId";
    JsonObject insertObject = JsonObject.create().put("content", "txn-inserted");
    JsonObject replaceObject = JsonObject.create().put("content", "txn-replaced");
    String res = "FAIL";


    int i = 0;
    while(!stopped) {
      int finalI = i;
      try{
        transactions.run(ctx -> {
          ctx.insert(collection, finalI + randomIdString, insertObject);

          if(finalI > 0) {
            TransactionGetResult transactionGetResult = ctx.get(collection, (finalI-1) + randomIdString);
            ctx.replace(transactionGetResult, replaceObject);
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
      i %= 15000;
    }

  }
}

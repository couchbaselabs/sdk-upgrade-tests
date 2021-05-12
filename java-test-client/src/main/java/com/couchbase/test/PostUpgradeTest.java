package com.couchbase.test;

import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.codec.RawBinaryTranscoder;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.transactions.TransactionDurabilityLevel;
import com.couchbase.transactions.TransactionGetResult;
import com.couchbase.transactions.Transactions;
import com.couchbase.transactions.config.TransactionConfigBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static com.couchbase.client.java.kv.GetOptions.getOptions;
import static com.couchbase.client.java.kv.UpsertOptions.upsertOptions;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PostUpgradeTest {// extends UpgradeTestBase{

  private static String clusterName;
  private static String user;
  private static String password;
  private static String bucketName;
  private static String scopeName;
  private static String collectionName;
  private static String fullCollectionName;

  public static Cluster cluster;
  public static Bucket bucket;
  public static Scope scope;
  public static Collection collection;
  private static Transactions transactions;

  @BeforeAll
  public static void CreateCouchbaseConnection() {
    clusterName = System.getProperty("upgradedCluster");
    user = System.getProperty("clusterUser");
    password = System.getProperty("clusterPassword");
    bucketName = System.getProperty("bucket");
    scopeName = System.getProperty("scope");
    collectionName = System.getProperty("collection");

    cluster = Cluster.connect(clusterName, user, password);
    cluster.waitUntilReady(Duration.ofSeconds(15L));
    transactions = Transactions.create(cluster,
      TransactionConfigBuilder.create().durabilityLevel(TransactionDurabilityLevel.MAJORITY).build());

    bucket = cluster.bucket(bucketName);
    scope = bucket.scope(scopeName);
    collection = scope.collection(collectionName);

    fullCollectionName = "`"+ collection.bucketName() +"`.`"+ collection.scopeName() +"`.`"+ collection.name() +"`";
  }

  @AfterAll
  static void tearDown() {
    cluster.disconnect();
  }

  @Test
  public void doUpsertTestPost() {
    String id = "id2";// Do randomised
    String content = "foo=bar";

    collection.upsert(id , content);

    assertEquals(collection.get(id).contentAs(String.class), content);
  }

  private static final JsonObject content = JsonObject.create()
    .put("foo", "bar")
    .put("created", true)
    .put("age", 12);

  private static final JsonObject replaceContent = JsonObject.create().put("foo", "bar");

  @Test
  void emptyIfGetNotFound() {
    assertThrows(DocumentNotFoundException.class, () -> collection.get(UUID.randomUUID().toString()));
  }

  @Test
  void getWithProjection() {
    String id = UUID.randomUUID().toString();

    MutationResult mutationResult = collection.upsert(id, content);
    assertTrue(mutationResult.cas() != 0);

    GetResult getResult = collection.get(id, getOptions().project("foo", "created", "notfound"));
    assertTrue(getResult.cas() != 0);
    assertFalse(getResult.expiryTime().isPresent());

    JsonObject decoded = getResult.contentAsObject();
    assertEquals("bar", decoded.getString("foo"));
    assertEquals(true, decoded.getBoolean("created"));
    assertFalse(decoded.containsKey("age"));
  }


  @Test
  void fullDocWithExpiration() {
    String id = UUID.randomUUID().toString();

    MutationResult mutationResult = collection.upsert(
            id,
            content,
            upsertOptions().expiry(Duration.ofSeconds(5))
    );
    assertTrue(mutationResult.cas() != 0);

    GetResult getResult = collection.get(id, getOptions().withExpiry(true));
    assertTrue(getResult.expiryTime().isPresent());
    assertTrue(getResult.expiryTime().get().toEpochMilli() > 0);
    assertEquals(content, getResult.contentAsObject());
  }

  @Test
  void fullDocWithExpirationAndCustomTranscoder() {
    String id = UUID.randomUUID().toString();

    MutationResult mutationResult = collection.upsert(
            id,
            content.toBytes(),
            upsertOptions().expiry(Duration.ofSeconds(5)).transcoder(RawBinaryTranscoder.INSTANCE)
    );
    assertTrue(mutationResult.cas() != 0);


    GetResult getResult = collection.get(id, getOptions().withExpiry(true).transcoder(RawBinaryTranscoder.INSTANCE));
    assertTrue(getResult.expiryTime().isPresent());
    assertTrue(getResult.expiryTime().get().toEpochMilli() > 0);
    assertEquals(content, JsonObject.fromJson(getResult.contentAs(byte[].class)));
  }

  @Test
  void insertKvTxnTest() {
    String id = UUID.randomUUID().toString();

    transactions.run(ctx -> {
      ctx.insert(collection, id , content);
    });
    GetResult getResult = collection.get(id);
    assertEquals(content, getResult.contentAsObject());
  }

  @Test
  void replaceKvTxnTest() {
    String id = UUID.randomUUID().toString();

    collection.insert(id, content);

    transactions.run(ctx -> {
      TransactionGetResult getResult = ctx.get(collection, id);
      ctx.replace(getResult, replaceContent);
    });
    GetResult getResult = collection.get(id);
    assertEquals(replaceContent, getResult.contentAsObject());
  }

  @Test
  void removeKvTxnTest() {
    String id = UUID.randomUUID().toString();

    collection.insert(id, content);

    transactions.run(ctx -> {
      TransactionGetResult getResult = ctx.get(collection, id);
      ctx.remove(getResult);
    });
    try{
      GetResult getResult = collection.get(id);
      assertTrue(getResult.contentAsObject().equals(JsonObject.create()));
    } catch (DocumentNotFoundException ex) {
      //Expected res
      assertTrue(ex.getMessage().contains("Document with the given id not found"));
    }
  }

  @Test
  void insertQueryTxnTest() {
    String id = UUID.randomUUID().toString();

    transactions.run(ctx -> {
      ctx.query("INSERT INTO " + fullCollectionName + " VALUES ('" + id + "', " + content + ")");
    });
    GetResult getResult = collection.get(id);
    assertEquals(content, getResult.contentAsObject());
  }

  @Test
  void updateQueryTxnTest() {
    String id = UUID.randomUUID().toString();

    collection.insert(id, content);

    transactions.run(ctx -> {
      ctx.query("UPDATE " + fullCollectionName + " SET content = 'updated-query' WHERE META().id = '" + id + "'");
    });
    GetResult getResult = collection.get(id);
    assertEquals(replaceContent, getResult.contentAsObject());
  }

  @Test
  void selectQueryTxnTest() {
    String id = UUID.randomUUID().toString();

    collection.insert(id, content);

    transactions.run(ctx -> {
      QueryResult result = ctx.query("SELECT `default`.* from `default` WHERE META().id = '" + id + "'");
      assertTrue(result.metaData().metrics().isPresent());
    });
  }

  @Test
  void removeQueryTxnTest() {
    String id = UUID.randomUUID().toString();

    collection.insert(id, content);

    transactions.run(ctx -> {
      ctx.query("DELETE FROM " + fullCollectionName + " WHERE META().id = '" + id + "'");
    });
    try{
      GetResult getResult = collection.get(id);
      assertTrue(getResult.contentAsObject().equals(JsonObject.create()));
    } catch (DocumentNotFoundException ex) {
      //Expected res
      assertTrue(ex.getMessage().contains("Document with the given id not found"));
    }
  }


}

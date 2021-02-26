package com.couchbase.test;


import com.couchbase.client.core.error.CasMismatchException;
import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.core.error.TimeoutException;
import com.couchbase.client.core.retry.RetryReason;
import com.couchbase.client.java.codec.RawBinaryTranscoder;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.ExistsResult;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.MutationResult;
import org.junit.jupiter.api.Test;


import java.time.Duration;
import java.util.EnumSet;
import java.util.UUID;

import static com.couchbase.client.java.kv.GetAndLockOptions.getAndLockOptions;
import static com.couchbase.client.java.kv.GetOptions.getOptions;
import static com.couchbase.client.java.kv.InsertOptions.insertOptions;
import static com.couchbase.client.java.kv.RemoveOptions.removeOptions;
import static com.couchbase.client.java.kv.UpsertOptions.upsertOptions;
import static com.couchbase.test.utils.Util.waitUntilCondition;
import static org.junit.jupiter.api.Assertions.*;

public class PreUpgradeTest extends UpgradeTestBase{

//  @Test
//  public void doUpsertTest() {
//    String id = "id1";// Do randomised
//    String content = "foo=bar";
//
//    collection.upsert(id , content);
//
//
//    assertEquals(collection.get(id).contentAs(String.class), content);
//  }

  // More transactions-related tests to be added by Praneeth

  @Test
  void insertAndGet() {
    String id = UUID.randomUUID().toString();
    MutationResult insertResult = collection.insert(id, "Hello, World");

    assertTrue(insertResult.cas() != 0);
    assertTrue(insertResult.mutationToken().isPresent());

    GetResult getResult = collection.get(id);
    assertEquals("Hello, World", getResult.contentAs(String.class));
    assertTrue(getResult.cas() != 0);
    assertFalse(getResult.expiryTime().isPresent());
  }

  /**
   * Mock does not support Get Meta, so we need to ignore it there.
   */
  @Test
  void exists() {
    String id = UUID.randomUUID().toString();

    assertFalse(collection.exists(id).exists());

    MutationResult insertResult = collection.insert(id, "Hello, World");
    assertTrue(insertResult.cas() != 0);

    ExistsResult existsResult = collection.exists(id);

    assertEquals(insertResult.cas(), existsResult.cas());
    assertTrue(existsResult.exists());
    assertFalse(collection.exists("some_id").exists());
  }

  @Test
  void emptyIfGetNotFound() {
    assertThrows(DocumentNotFoundException.class, () -> collection.get(UUID.randomUUID().toString()));
  }

  @Test
  void getWithProjection() {
    String id = UUID.randomUUID().toString();

    JsonObject content = JsonObject.create()
            .put("foo", "bar")
            .put("created", true)
            .put("age", 12);

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

  /**
   * Right now the mock does not support xattr/macro expansion so this test is
   * ignored on the mock. Once the mock supports it, please remove the ignore
   * annotation.
   *
   * <p>See https://github.com/couchbase/CouchbaseMock/issues/46</p>
   */
  @Test
  void fullDocWithExpiration() {
    String id = UUID.randomUUID().toString();

    JsonObject content = JsonObject.create()
            .put("foo", "bar")
            .put("created", true)
            .put("age", 12);

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

    JsonObject content = JsonObject.create()
            .put("foo", "bar")
            .put("created", true)
            .put("age", 12);

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

  /**
   * Right now the mock does not support xattr/macro expansion so this test is
   * ignored on the mock. Once the mock supports it, please remove the ignore
   * annotation.
   *
   * <p>See https://github.com/couchbase/CouchbaseMock/issues/46</p>
   */
  @Test
  void projectionWithExpiration() {
    String id = UUID.randomUUID().toString();

    JsonObject content = JsonObject.create()
            .put("foo", "bar")
            .put("created", true)
            .put("age", 12);

    MutationResult mutationResult = collection.upsert(
            id,
            content,
            upsertOptions().expiry(Duration.ofSeconds(5))
    );
    assertTrue(mutationResult.cas() != 0);

    GetResult getResult = collection.get(
            id,
            getOptions().project("foo", "created").withExpiry(true)
    );
    assertTrue(getResult.cas() != 0);
    assertTrue(getResult.expiryTime().isPresent());
    assertTrue(getResult.expiryTime().get().toEpochMilli() > 0);

    JsonObject decoded = getResult.contentAsObject();
    assertEquals("bar", decoded.getString("foo"));
    assertEquals(true, decoded.getBoolean("created"));
    assertFalse(decoded.containsKey("age"));
  }

  /**
   * We need to ignore this test on the mock because the mock returns TMPFAIL instead of LOCKED when the
   * document is locked (which used to be the old functionality but now since XERROR is negotiated it
   * returns LOCKED properly).
   *
   * <p>Once the mock is modified to return LOCKED, this test can also be run on the mock again.</p>
   */
  @Test
  void getAndLock() {
    String id = UUID.randomUUID().toString();

    JsonObject expected = JsonObject.create().put("foo", true);
    MutationResult insert = collection.insert(id, expected);

    assertTrue(insert.cas() != 0);

    GetResult getAndLock = collection.getAndLock(id, Duration.ofSeconds(30));

    assertTrue(getAndLock.cas() != 0);
    assertNotEquals(insert.cas(), getAndLock.cas());
    assertEquals(expected, getAndLock.contentAsObject());

    TimeoutException exception = assertThrows(
            TimeoutException.class,
            () -> collection.getAndLock(id, Duration.ofSeconds(30), getAndLockOptions().timeout(Duration.ofMillis(100)))
    );
    assertEquals(EnumSet.of(RetryReason.KV_LOCKED), exception.context().requestContext().retryReasons());
    assertThrows(DocumentNotFoundException.class, () -> collection.getAndLock("some_doc", Duration.ofSeconds(30)));
  }

  /**
   * This test is ignored against the mock because right now it does not bump the CAS like
   * the server does when getAndTouch is called.
   *
   * <p>Remove the ignore as soon as https://github.com/couchbase/CouchbaseMock/issues/49 is
   * fixed.</p>
   */
  @Test
  void getAndTouch() {
    String id = UUID.randomUUID().toString();

    JsonObject expected = JsonObject.create().put("foo", true);
    MutationResult insert = collection.insert(
            id,
            expected,
            insertOptions().expiry(Duration.ofSeconds(10))
    );
    assertTrue(insert.cas() != 0);

    GetResult getAndTouch = collection.getAndTouch(id, Duration.ofSeconds(1));

    assertTrue(getAndTouch.cas() != 0);
    assertNotEquals(insert.cas(), getAndTouch.cas());
    assertEquals(expected, getAndTouch.contentAsObject());

    waitUntilCondition(() -> {
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        // ignored.
      }
      try {
        collection.get(id);
        return false;
      } catch (DocumentNotFoundException knf) {
        return true;
      }
    });
  }

  @Test
  void remove() {
    String id = UUID.randomUUID().toString();

    JsonObject expected = JsonObject.create().put("foo", true);
    MutationResult insert = collection.insert(
            id,
            expected,
            insertOptions().expiry(Duration.ofSeconds(2))
    );
    assertTrue(insert.cas() != 0);

    assertThrows(
            CasMismatchException.class,
            () -> collection.remove(id, removeOptions().cas(insert.cas() + 100))
    );

    MutationResult result = collection.remove(id);
    assertTrue(result.cas() != insert.cas());

    assertThrows(DocumentNotFoundException.class, () -> collection.remove(id));
  }

}

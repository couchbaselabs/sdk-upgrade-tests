package com.couchbase.exceptions;/*
 * Copyright (c) 2013 Couchbase, Inc.
 */

public class InsufficientNodesException extends IllegalStateException {
  public InsufficientNodesException(String msg) {
    super(msg);
  }
}

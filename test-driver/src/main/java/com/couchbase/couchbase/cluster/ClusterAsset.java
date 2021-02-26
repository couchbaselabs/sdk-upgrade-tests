package com.couchbase.couchbase.cluster;/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.google.gson.JsonObject;

/**
 *
 * @author mnunberg
 */
public interface ClusterAsset {
  public JsonObject getRawJson();
}

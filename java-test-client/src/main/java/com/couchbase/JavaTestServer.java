package com.couchbase;


import io.grpc.ServerBuilder;

import java.io.IOException;

public class JavaTestServer {

  public static void main(String args[]) throws IOException, InterruptedException {
    io.grpc.Server server = ServerBuilder
            .forPort(8080)
            .addService(new UpgradeTestingService() {
            }).build();

    server.start();
    server.awaitTermination();
  }
}

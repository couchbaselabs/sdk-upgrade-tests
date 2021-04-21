package com.couchbase;

import com.couchbase.grpc.protocol.CreateConnectionRequest;
import com.couchbase.grpc.protocol.CreateConnectionResponse;
import com.couchbase.grpc.protocol.UpgradeTestRequest;
import com.couchbase.grpc.protocol.UpgradeTestResponse;
import com.couchbase.grpc.protocol.UpgradeTestingServiceGrpc;
import com.couchbase.utils.RequestVariableExtractor;
import io.grpc.stub.StreamObserver;


import static com.couchbase.utils.RequestVariableExtractor.extractConnectionRequest;


public class UpgradeTestingService extends UpgradeTestingServiceGrpc.UpgradeTestingServiceImplBase {
  TestDispatcher testDispatcher = new TestDispatcher();



  @Override
  public void createConnection(CreateConnectionRequest request, StreamObserver<CreateConnectionResponse> responseObserver) {
    extractConnectionRequest(request);
    testDispatcher.setClusterTestInfo(RequestVariableExtractor.getTestInfo());

    CreateConnectionResponse response = CreateConnectionResponse.newBuilder()
            .setConnected(doConnection(request))
            .setErrors("")
            .build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void doUpgradeTests(UpgradeTestRequest request, StreamObserver<UpgradeTestResponse> responseObserver) {
    String tmp = "";

    UpgradeTestResponse response = UpgradeTestResponse.newBuilder()
            .setDone(tmp)
            .setErrors("")
            .build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();

    switch(request.getTestType()) {
      case PRE_UPGRADE:
        //result = junit.run(PreUpgradeTest.class);
        testDispatcher.runPreUpgradeTests();
        break;
      case DURING_UPGRADE:
        testDispatcher.runDuringUpgradeTests();
        break;
      case POST_UPGRADE:
        testDispatcher.runPostUpgradeTests();
        break;
      default:
        tmp = "ERROR";
    }
    System.out.println(tmp);
  }


  public boolean doConnection(CreateConnectionRequest req) {
    System.out.println("CREATED CONNECTION");
    return true;
  }


}

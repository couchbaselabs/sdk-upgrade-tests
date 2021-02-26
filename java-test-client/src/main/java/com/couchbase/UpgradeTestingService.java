package com.couchbase;

import com.couchbase.grpc.protocol.*;
import com.couchbase.grpc.protocol.UpgradeTestingServiceGrpc;
import com.couchbase.utils.RequestVariableExtractor;
import io.grpc.stub.StreamObserver;


//import org.junit.internal.TextListener;
//import org.junit.runner.JUnitCore;
//import org.junit.runner.Result;

import static com.couchbase.utils.RequestVariableExtractor.extractConnectionRequest;


public class UpgradeTestingService extends UpgradeTestingServiceGrpc.UpgradeTestingServiceImplBase {
 // JUnitCore junit = new JUnitCore();
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
    //Do routing to the right tests and upgrade info?
    String tmp = "";
    //Result result;

    //junit.addListener(new TextListener(System.out));


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

    UpgradeTestResponse response = UpgradeTestResponse.newBuilder()
            .setDone(tmp)
            .setErrors("")
            .build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }


  public boolean doConnection(CreateConnectionRequest req) {
    System.out.println("CREATED CONNECTION");
    return true;
  }


}

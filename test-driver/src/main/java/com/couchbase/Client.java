package com.couchbase;

import com.couchbase.constants.Strings;
import com.couchbase.couchbase.cluster.ClusterConfigure;
import com.couchbase.couchbase.couchbase.CouchbaseInstaller;
import com.couchbase.couchbase.upgrade.UpgradeSwapAction;
import com.couchbase.exceptions.RestApiException;
import com.couchbase.grpc.protocol.*;
import com.couchbase.grpc.protocol.UpgradeTestingServiceGrpc;
import com.couchbase.inputparameters.InputParameters;
import com.couchbase.logging.LogUtil;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;

import java.util.concurrent.ExecutionException;

public class Client {

  private static Logger logger= LogUtil.getLogger(Client.class);
  public static InputParameters inputParameters;

  private static ManagedChannel channel;
  private static UpgradeTestingServiceGrpc.UpgradeTestingServiceBlockingStub stub;

  private static ClusterConfigure clusterConfigure;
  private static UpgradeSwapAction upgrader;



  public static void main(String args[]) {
    logger.info("Starting Cluster Installation/Config");

    inputParameters= new InputParameters(args);
    inputParameters.parseParameters();

    couchbaseInstall();
    clusterConfigure();

    if (!connectToTestServer()) {
      logger.error("Failed to create connection. Stopping testing.");
    }

    Upgrade upgradeType = inputParameters.getUpgradeType();

    logger.info("Cluster is setup. Beginning testing.");

    sendUpgradeTestRequest(upgradeType, Tests.PRE_UPGRADE);

    performUpgradeTesting(upgradeType);

    sendUpgradeTestRequest(upgradeType, Tests.POST_UPGRADE);
  }

  private static void performUpgradeTesting(Upgrade upgrade) {
    sendUpgradeTestRequest(upgrade, Tests.DURING_UPGRADE);

    doUpgrade();

    stopUpgradeTests();
  }

  private static void stopUpgradeTests() {
  }

  private static void doUpgrade() {
    logger.info("Starting upgrade");
    try {
      upgrader.start().get();
    } catch (RestApiException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    logger.info("Upgrade completed.");

  }

  private static String startUpgradeTests() {
    UpgradeTestResponse response = stub.doUpgradeTests(UpgradeTestRequest.newBuilder()
            .setUpgradeType(Upgrade.OFFLINE)
            .setTestType(Tests.DURING_UPGRADE)
            .build());

    logger.error(response.getErrors());
    return response.getDone();
  }

  private static boolean connectToTestServer() {
    channel = ManagedChannelBuilder.forAddress("localhost", 8080)
            .usePlaintext()
            .build();

    stub = UpgradeTestingServiceGrpc.newBlockingStub(channel);

    CreateConnectionResponse response = stub.createConnection(CreateConnectionRequest.newBuilder()
            .setClusterHostname(inputParameters.getClusterNodes().getFirst().ip)
            .setBucketName(inputParameters.getbucketName())
            .setClusterUsername(Strings.ADMIN_USER)
            .setClusterPassword(Strings.PASSWORD)
            .setScopeName("_default")
            .setCollectionName("_default")
            .build());
    if(!response.getConnected()) {
      logger.error(response.getErrors());
    }
    return response.getConnected();
  }


  private static String sendUpgradeTestRequest(Upgrade upgrade, Tests test) {
    UpgradeTestResponse response = stub.doUpgradeTests(UpgradeTestRequest.newBuilder()
            .setUpgradeType(upgrade)
            .setTestType(test)
            .build());

    logger.error(response.getErrors());
    return response.getDone();
  }



  public static void couchbaseInstall() {

    try{
      if (inputParameters.getCouchbaseInstall()) {
        logger.info("Begin Couchbase installation on individual nodes");

        new CouchbaseInstaller(inputParameters).couchbaseInstall();

        logger.info("Completed Couchbase installation on individual nodes. Proceeding with Cluster Configuration");
      } else {
        logger.info("Not Installing couchbase Since the user input for installcouchbase is set to : " + inputParameters.getCouchbaseInstall());
      }
    }catch(Exception ex){
      logger.error("Exception during CB installation:{}",ex.getMessage());
    }

  }

  public static void clusterConfigure() {

    try{
      if (inputParameters.getClusterConfigure()) {
        logger.info("Begin Cluster Configuration");

        clusterConfigure = new ClusterConfigure(inputParameters);
        clusterConfigure.clusterConfigure();
        upgrader = new UpgradeSwapAction(clusterConfigure, "kv");
        upgrader.setup(2);

        logger.info("Completed Cluster Configuration");
      } else {
        logger.info("Not executing Cluster Configuration since clusterConfigure is set to: " + inputParameters.getClusterConfigure());
      }
    }catch(Exception ex){
      logger.error("Exception during Cluster configuration:{}",ex.getMessage());
    }

  }
}

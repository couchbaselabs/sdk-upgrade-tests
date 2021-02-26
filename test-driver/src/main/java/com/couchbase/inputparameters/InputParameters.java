package com.couchbase.inputparameters;

import com.couchbase.grpc.protocol.Upgrade;
import com.couchbase.logging.LogUtil;
import org.slf4j.Logger;

import java.util.LinkedList;

/**
 * This class is used mainly install cluster.
 * If you are using the CI pipelines or already have a cluster installed,Please ignore this class
 */

public class InputParameters {
  static Logger logger;
  private String[] parameters;

  private LinkedList<Host> hosts = new LinkedList<Host>();

  private String sshUsername = "root";
  private String sshPassword = "couchbase";

  private boolean couchbaseInstall = true;
  private boolean clusterConfigure = true;

  private String clusterVersion;
  private String buildType = "enterprise";

  private String bucketName = "default";
  private String bucketType = "COUCHBASE";

  private String secBucketName = "secBucket";
  private String secBucketType = "EPHEMERAL";

  private boolean addDefaultBucket = false;
  private String nodeQuota = "1500";
  private String bucketPassword = "password";
  private int bucketReplicaCount = 1;
  private String bucketEphemeralEvictionPolicy = "NOEVICTION";
  private int bucketRamSize = 256;
  private String bucketSaslPassword = "password";
  private boolean setStorageMode = true;


  private String n1qlIndexName = "n1qlIdx1";
  private String n1qlIndexType = "secondary";
  private String n1qlFieldsToIndex = "tag,type";

  private int useMaxConn = 0;
  private boolean enableAutoFailOver = false;
  private int autoFailoverTimeout = 5;
  private boolean useSSH = true;
  //TODO: Make these params from cmd line

  private boolean upgrade = true;
  private String upgradeVersion = "6.6.1-9213";
  private int activeNodes = 2;
  private Upgrade upgradeType;

  public InputParameters(String[] parameters) {
    this.parameters = parameters;
    logger = LogUtil.getLogger(InputParameters.class);
  }

  public void parseParameters() {

    //Override the default values if user has given inputs
    for (String parameter : parameters) {
      switch (parameter.split("=")[0]) {
        case "host":
          hosts.add(new Host(parameter.split("=")[1].split(":")[0], parameter.split("=")[1].split(":")[1]));
          break;
        case "couchbaseInstall":
          setInstallCouchbase(Boolean.parseBoolean(parameter.split("=")[1]));
          break;
        case "clusterConfigure":
          setInitializeCluster(Boolean.parseBoolean(parameter.split("=")[1]));
          break;
        case "bucketType":
          this.bucketType = parameter.split("=")[1];
          break;
        case "clusterVersion":
          this.clusterVersion = parameter.split("=")[1];
          logger.info("clusterVersion:{}", clusterVersion);
          break;
        case "upgradeVersion":
          this.upgradeVersion = parameter.split("=")[1];
          logger.info("upgradeVersion:{}", upgradeVersion);
          break;
        case "upgradeType":
          this.upgradeType = Upgrade.valueOf(parameter.split("=")[1]);
          break;
        case "preUpgradeNodes":
          this.activeNodes = Integer.parseInt(parameter.split("=")[1]);
          break;
        default:
          logger.warn("Undefined input: {} Ignoring it", parameter.split("=")[0]);
      }
    }

    if (hosts.size() == 0) {
      logger.error("Host information not given hence cannot install/configure Couchbase");
      System.exit(-1);
    }
  }

  public boolean getsetStorageMode() {
    return setStorageMode;
  }

  public String getsshUsername() {
    return sshUsername;
  }

  public String getsshPassword() {
    return sshPassword;
  }

  public LinkedList<Host> getClusterNodes() {
    return hosts;
  }

  public String getClusterVersion() {
    return clusterVersion;
  }

  public void setclusterVersion(String Version) {
    this.clusterVersion = Version;
  }

  public boolean getupgrade() {
    return upgrade;
  }


  public String getUpgradeVersion() {
    return upgradeVersion;
  }

  public String getbuildtype() {
    return buildType;
  }

  public boolean getCouchbaseInstall() {
    return couchbaseInstall;
  }

  public boolean getClusterConfigure() {
    return clusterConfigure;
  }

  public boolean shouldUseSSH() {
    return useSSH;
  }

  public String getnodeQuota() {
    return nodeQuota;
  }

  public String getbucketName() {
    return bucketName;
  }

  public String getSecBucketName() {
    return secBucketName;
  }

  public String getbucketPassword() {
    return bucketPassword;
  }

  public String getbucketType() {
    return bucketType;
  }

  public String getSecBucketType() {
    return secBucketType;
  }


  public String getn1qlFieldsToIndex() {
    return n1qlFieldsToIndex;
  }

  public boolean getenableAutoFailOver() {
    return enableAutoFailOver;
  }

  public int getAutoFailoverTimeout() {
    return autoFailoverTimeout;
  }

  public String getn1qlIndexName() {
    return n1qlIndexName;
  }

  public String getn1qlIndexType() {
    return n1qlIndexType;
  }

  public int getbucketRamSize() {
    return bucketRamSize;
  }

  public int getbucketReplicaCount() {
    return bucketReplicaCount;
  }

  public String getBucketEphemeralEvictionPolicy() {
    return bucketEphemeralEvictionPolicy;
  }

  public String getBucketSaslPassword() {
    return bucketSaslPassword;
  }

  public int getUseMaxConn() {
    return useMaxConn;
  }

  public boolean getAddDefaultBucket() {
    return addDefaultBucket;
  }


  public void setInstallCouchbase(boolean couchbaseInstall) {
    this.couchbaseInstall = couchbaseInstall;
  }

  public void setInitializeCluster(boolean clusterConfigure) {
    this.clusterConfigure = clusterConfigure;
  }

  public int getActiveNodes() {
    return activeNodes;
  }

  public Upgrade getUpgradeType() {
    return upgradeType;
  }
}

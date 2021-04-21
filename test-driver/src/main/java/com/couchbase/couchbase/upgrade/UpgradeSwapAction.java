package com.couchbase.couchbase.upgrade;

import com.couchbase.couchbase.cluster.ClusterConfigure;
import com.couchbase.exceptions.RestApiException;
import com.couchbase.couchbase.nodes.NodeHost;
import com.couchbase.couchbase.nodes.NodelistBuilder;
import com.couchbase.logging.LogUtil;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;



public class UpgradeSwapAction {
    List<NodeHost> toAdd = new ArrayList<>();
    Collection<NodeHost> toRemove;
    String services;
    final private static Logger logger = LogUtil.getLogger(UpgradeSwapAction.class);
    ClusterConfigure cluster;

    public UpgradeSwapAction(ClusterConfigure cluster, String service) {
        this.cluster = cluster;
        this.services = service;
        System.out.println("UPGRADER HAS BEEN INITIALISED");
    }

    public void setup(int numNodes) {
        toAdd.addAll(cluster.getNodelist().getFree());//cluster.nlb.reserveFree(numNodes, true, services));
        toRemove = cluster.getNodelist().getActive();//cluster.nlb.reserveForRemoval(numNodes, true, true, services);//cluster.getNodelist().getAll();// nlb.reserveForRemoval(numNodes, true, true, services);
    }

    public Future<Boolean> start() throws RestApiException, ExecutionException,InterruptedException  {
        if (toAdd.size() != toRemove.size()) {
            throw new IllegalArgumentException("Number of nodes to be added: " + toAdd.size() + " must " +
                    "be equal to number of nodes to be removed:" + toRemove.size());
        }

        List<NodeHost> nodesReservedForRemoval = new ArrayList<NodeHost>(toRemove);

        int ii = 0;
        for(NodeHost an:toAdd) {
            List<NodeHost> addNodeColl = Arrays.asList(an);
            ii++;
            for(NodeHost rn:nodesReservedForRemoval) {
                int oldVersion = Integer.valueOf(cluster.getNodeVersion(rn).split("\\.")[0]);
                logger.info("service="+rn.getServices()+", version="+oldVersion);

                List<NodeHost> removeNodeColl = Arrays.asList(rn);
                nodesReservedForRemoval.remove(rn);
                // copy services from node to be removed
                cluster.addAndRebalance(addNodeColl, rn.getServices()).get();
                logger.info("added "+an.getHostname()+" with service="+rn.getServices()+" version="+an.getVersion());

//                // if index node is added and server version is prespock, copy index
//                if (rn.getServices().contains("index") && oldVersion <= 4 && cluster.IndexType.equals("secondary")) {
//                    logger.info("creating index to "+an.getHostname());
//                    addN1qlIndex(cluster, an.getHostname());
//                }

                if (toAdd.size() == ii) {
                    Future<Boolean> result = cluster.removeAndRebalance(removeNodeColl);
//                    // if index node is added and server version is prespock and only primary index is created,
//                    // create a primary index after removing prespock node
//                    if (rn.getServices().contains("index") && oldVersion <= 4 && cluster.IndexType.equals("primary")) {
//                        logger.info("creating index to " + an.getHostname());
//                        addN1qlIndex(cluster, an.getHostname());
//                    }
                    return result;
                } else {
                    cluster.removeAndRebalance(removeNodeColl).get();

//                    // if index node is added and server version is prespock and only primary index is created,
//                    // create a primary index after removing prespock node
//                    if (rn.getServices().contains("index") && oldVersion <= 4 && cluster.IndexType.equals("primary")) {
//                        logger.info("creating index to " + an.getHostname());
//                        addN1qlIndex(cluster, an.getHostname());
//                    }
                }
                Thread.sleep(1000);
                break;
            }
        }

        return null;
    }

//    private void addN1qlIndex(String targetNode) {
//        try {
//            // create index
//            logger.info("Creating n1ql index");
//            final String[] n1qlParams = {"tag,type"};
//            cluster.createN1QLIndex("default", n1qlParams, targetNode, false);
//
//            logger.info("Sleep 5 sec");
//            Thread.sleep(5000);
//        } catch (Exception e) {
//            logger.info("Exception:"+e.toString());
//        }
//    }
//
//    public Future<Boolean> undo(CBCluster  cluster) throws RestApiException {
//        return cluster.swapAndRebalance(toRemove, toAdd, this.services);
//    }



}


package com.couchbase.couchbase.cluster;

import com.couchbase.constants.Strings;
import com.couchbase.constants.defaults;
import com.couchbase.couchbase.bucket.Bucket;
import com.couchbase.couchbase.bucket.BucketConfig;
import com.couchbase.couchbase.couchbase.CouchbaseAdmin;
import com.couchbase.couchbase.nodes.Node;
import com.couchbase.couchbase.nodes.NodeHost;
import com.couchbase.couchbase.nodes.Nodelist;
import com.couchbase.couchbase.nodes.NodelistBuilder;
import com.couchbase.couchbase.utils.RebalanceWaiter;
import com.couchbase.exceptions.*;
import com.couchbase.inputparameters.Host;
import com.couchbase.inputparameters.InputParameters;
import com.couchbase.logging.LogUtil;
import com.couchbase.utils.AliasLookup;
import com.couchbase.utils.Retryer;
import com.couchbase.utils.remote.ConnectionInfo;
import com.couchbase.utils.remote.ServiceLogin;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class ClusterConfigure {
    final private static Logger logger = LogUtil.getLogger(ClusterConfigure.class);
    private InputParameters inputParameters;
    public NodelistBuilder nlb = null;
    private Nodelist nodelist;
    final private ArrayList<NodeHost> nodes = new ArrayList<NodeHost>();
    final private AliasLookup ourAliases = new AliasLookup();
    private ClusterConfigureUtils clusterconfigureutils;



    public ClusterConfigure(InputParameters inputParameters){
        this.inputParameters=inputParameters;
    }

    public Nodelist getNodelist() {
        return nodelist;
    }

    static public abstract class RestRetryer extends Retryer<RestApiException> {
        RestRetryer(int seconds) {
            super(seconds * 1000, 500, RestApiException.class);
        }

        @Override
        protected void handleError(RestApiException caught) throws RestApiException, InterruptedException {
            if (caught.getStatusLine().getStatusCode() >= 500) {
                call();
            } else if (caught.getStatusLine().getStatusCode() == 409) {
                logger.error("N1QL Index was not deleted from previous run");
                return;
            } else {
                throw HarnessException.create(HarnessError.CLUSTER, caught);
            }
        }
    }

    public void clusterConfigure(){
             configureCluster();
            clusterconfigureutils = new ClusterConfigureUtils(nodelist,inputParameters);

            // Ensure we can connect to the REST port
            testRestApiConnection();

            //Stop rebalance
            try  {
                stopRebalance();
            } catch (Exception ex) {
                throw new ClusterException(ex);
            }

            // Now we need to reset all the cluster nodes
            try  {
                resetClusterNodes();
            } catch (RestApiException ex) {
                throw new ClusterException(ex);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (inputParameters.shouldUseSSH()) {
                    setupNodesSSH();
                }
                setupNewCluster();
            } catch (RestApiException | InterruptedException ex) {
                throw new HarnessException(ex);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }


    private String configureCluster(){
        try{
            for (String alias : defaults.ipalias) {
                List<String> aliases = Arrays.asList(alias.split("/"));
                ourAliases.associateAlias(aliases);
            }
            addNodesFromSpec();

            for (NodeHost nn : nodes) {
                nn.getAdmin().getAliasLookupCache().merge(ourAliases);
            }

            nlb = new NodelistBuilder(nodes, defaults.numGroups, inputParameters.getUpgradeVersion(), inputParameters.getActiveNodes());
            this.nodelist= nlb.build();
            //TODO: Remove this?
            //nlb.reserveForRemoval(2, false, true, "kv");

        }catch(Exception e){
            logger.error("Exception during configuring cluster: "+e);
            System.exit(-1);
        }

        return nodelist.getMaster().host;
    }

    private void testRestApiConnection(){
        try {
            new RestRetryer(defaults.RestTimeout) {
                @Override
                protected boolean tryOnce() throws RestApiException {
                    for (NodeHost nn : nodelist.getAll()) {
                        logger.debug(" nn.getAdmin().getInfo(): "+ nn.getAdmin().getInfo());
                    }
                    return true;
                }
            }.call();
        } catch (RestApiException ex) {
            throw HarnessException.create(HarnessError.CLUSTER, ex);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void  stopRebalance(){
        for (NodeHost node : nodelist.getAll()) {
            try {
                node.getAdmin().stopRebalance();
            } catch (RestApiException ex) {
                //logger.debug("Stop rebalance failed", ex);
            }
        }

    }

    /**
     * Adds nodes from the specification strings provided to the cluster
     * options.
     */
    private void addNodesFromSpec() {
        NodeHost nn;
        // Gets all the relevant options and handles them there.
        int i = 1;
        for (Host host : inputParameters.getClusterNodes()) {
            nn = NodeHost.fromSpec(host.ip+":",
                    getRestLogin(),
                    getUnixLogin(),
                    inputParameters.getClusterVersion(),
                    host.hostservices );
            if (i > inputParameters.getActiveNodes()) {
                System.out.println("SETTING TO UPGRAGE");
                nn.setState(Collections.singleton(NodeHost.State.UPGRADE));
            }
            logger.debug("Nodes order {}", nn.asUri());
            node(nn);
            i++;
        }
    }

    /**
     * Adds a node to the cluster.
     * @param nn A node to add
     * @return The builder
     */
    public ClusterConfigure node(NodeHost nn) {
        if (nodes.contains(nn)) {
            logger.debug("Node {} already exists. Replacing", nn);
            nodes.remove(nn);
        }
        nodes.add(nn);
        logger.debug("Nodes collection {}", nodes);
        return this;
    }

    /**
     * @see #node(NodeHost)
     */
    public ClusterConfigure node(Collection<NodeHost> nn) {
        for (NodeHost node : nn) {
            node(node);
        }
        return this;
    }

    private void resetClusterNodes() throws Exception, ClusterException {
        // First, discover the cluster
        Map<String,Collection<NodeHost>> clusterNodes = new HashMap<String, Collection<NodeHost>>();
        final AtomicReference<ConnectionInfo> refInfo = new AtomicReference<ConnectionInfo>();
        for (final NodeHost node : nodelist.getAll()) {
            new RestRetryer(defaults.RestTimeout) {
                @Override
                protected boolean tryOnce() throws RestApiException {
                    refInfo.set(node.getAdmin().getInfo());
                    return true;
                }

            }.call();

            ConnectionInfo info = refInfo.get();

            if (!info.hasCluster()) {
                logger.trace("Not resetting {}. No cluster", node);
                continue;
            }

            ClusterConfigureUtils.addToValue(clusterNodes, info.getClusterIdentifier(), node);
            logger.trace("Node {} is a member of cluster {}",
                    node.getKey(), info.getClusterIdentifier());

        }
        for (Collection<NodeHost> llCluster : clusterNodes.values()) {
            clusterconfigureutils.clearSingleCluster(llCluster);
        }
    }


    private void setupNodesSSH() throws ClusterException {
        ExecutorService svc = Executors.newCachedThreadPool();
        List<Future<Boolean>> taskList = new ArrayList<Future<Boolean>>();
        for (final NodeHost nn : nodelist.getAll()) {
            Future<Boolean> ft = svc.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    nn.initSSH();
                    return true;
                }
            });

            taskList.add(ft);
        }

        svc.shutdown();
        for (Future ft : taskList) {
            try {
                ft.get();
            } catch (Exception ex) {
                throw new ClusterException(ex);
            }
        }
    }


    private void setupNewCluster() throws Exception {
        final CouchbaseAdmin adm = nodelist.getMaster().getAdmin();

        logger.debug("setup service of initial node {}", adm);
        try {
            new RestRetryer(3) {
                @Override
                protected boolean tryOnce() throws RestApiException {
                    adm.setupInitialService(nodelist.getMaster().getServices());
                    logger.trace("Initial service added");
                    return true;
                }
            }.call();
        } catch (Exception e) {
            logger.debug("provision is done in previous test");
        }


        logger.debug("Provisioning initial node {}", adm);
        new RestRetryer(defaults.RestTimeout) {
            @Override
            protected boolean tryOnce() throws RestApiException {
                adm.initNewCluster(inputParameters.getnodeQuota());
                logger.trace("Provisioning done");
                return true;
            }
        }.call();

        // set hostname to master node
        String nodeHostname = String.valueOf(nodelist.getMaster());
        if (nodeHostname.contains(".com")) {
            nodeHostname = nodeHostname.replace(":8091", "");
            nodeHostname = nodeHostname.replace("http://", "");
            adm.setupInitialHostname(nodeHostname);
            logger.debug("Set hostname to master node");
        }

        // create user
        adm.createUser("default", "password");
        if (!inputParameters.getbucketName().isEmpty() && !inputParameters.getbucketName().equals("default"))
            adm.createUser(inputParameters.getbucketName(), inputParameters.getbucketPassword());

        JoinReadyPoller.poll(nodelist.getActiveAux(), defaults.RestTimeout);


        for (final NodeHost nn : nodelist.getActiveAux()) {
            new RestRetryer(defaults.RestTimeout) {
                @Override
                protected boolean tryOnce() throws RestApiException, InsufficientNodesException {
                    logger.info("Adding node {} with services {}", nn.getAdmin().getEntryPoint(), nn.getServices());
                    adm.addNewNode(nn.getAdmin().getEntryPoint(), nn.getServices());
                    return true;
                }
            }.call();
        }

        logger.info("All nodes added. Will rebalance");
        adm.rebalance();
        try {
            RebalanceWaiter.poll(adm).get();
        } catch (ExecutionException ex) {
            throw new ClusterException(ex);
        } catch (InterruptedException ex) {
            throw new ClusterException(ex);
        }

        if (inputParameters.getsetStorageMode()) {
            clusterconfigureutils.setupIndexStorageMode("memory_optimized");
        } else {
            clusterconfigureutils.setupIndexStorageMode("forestdb");
        }
        clusterconfigureutils.setupServerGroups(adm);

        // Again, really make sure no buckets exist..
        for (Bucket bkt : adm.getBuckets().values()) {
            logger.warn("Still have bucket {}", bkt);
        }

        // Now, add the buckets.
        if (inputParameters.getAddDefaultBucket() &&
                inputParameters.getbucketName().equals("default") == false) {
            BucketConfig bConfig = new BucketConfig("default");
            bConfig.bucketType = Bucket.BucketType.COUCHBASE;
            bConfig.ramQuotaMB = 256;
            adm.createBucket(bConfig);
        }
        String bucketName = clusterconfigureutils.setupMainBucket(adm);
        clusterconfigureutils.setupSecondaryBucket(adm); // To cover MB-26144, since it happens when multiple buckets are created on Spock node
        clusterconfigureutils.waitForBucketReady();

        if (inputParameters.getUseMaxConn()!=0) {
            adm.setClusterMaxConn(inputParameters.getUseMaxConn());
        }

        for (NodeHost nn : nodelist.getActiveAux()) {
            nn.ensureNObject();
        }
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException ex) {

        }




        logger.info("Creating n1ql index");
        // run twice to assure at least 2 index nodes have the n1ql index
        clusterconfigureutils.createN1QLIndex(bucketName, inputParameters.getn1qlFieldsToIndex().split(","), null, true);
        //createN1QLIndex(bucketName, clusterOptions.getn1qlFieldsToIndex().split(","));

        // due to huge files created by fts index in data/@fts, it blocks rebalance thus blocks subdoc test
        // onece we resolved this issue, then turn this back on
    /*logger.info("Creating fts index");
    createFTSIndex(bucketName);*/

        clusterconfigureutils.createAnalyticsDataSet(bucketName, true);
        clusterconfigureutils.connectLocalAnalyticsDataSet(true);

        logger.info("setting up auto failover = " + inputParameters.getenableAutoFailOver() +
                " with timeout = " + inputParameters.getAutoFailoverTimeout());
        adm.setupAutoFailover(inputParameters.getenableAutoFailOver(),
                inputParameters.getAutoFailoverTimeout());
    }

    public ServiceLogin getRestLogin() {
        return new ServiceLogin(Strings.ADMIN_USER,
                Strings.PASSWORD,
                -1);
    }

    public ServiceLogin getUnixLogin() {
        return new ServiceLogin(inputParameters.getsshUsername(),
                inputParameters.getsshPassword(),
                -1);
    }

    public String getNodeVersion(NodeHost node) throws RestApiException {
        logger.debug("Lookup installed server version of {}", node);
        List<Node> nodes = nodelist.getAdmin().getNodes();
        String version = "";
        for (Node currentNode: nodes) {
            String currenthost = currentNode.getNSOtpNode();
            String nodehost = node.getHostname();
            if (currentNode.getNSOtpNode().equals("ns_1@"+node.getHostname())) {
                version = currentNode.getClusterVersion();
                break;
            }
        }
        return version;
    }

    /**
     * Adds nodes and rebalances the cluster, returning a Future object
     * @param nodes A list of nodes to add. The nodes must not be active
     * @return A future which can be waited on for rebalance completion.
     */
    public Future<Boolean> addAndRebalance(Collection<NodeHost> nodes, String services) throws RestApiException {
        logger.debug("Adding Nodes {}", nodes);
        addNodes(nodes, services);
        nodelist.getAdmin().rebalance();
        return RebalanceWaiter.poll(nodelist.getAdmin());
    }

    /**
     * Adds nodes the cluster.
     * @param nodes A list of nodes to add. The nodes must not be active
     * @throws RestApiException
     */
    public void addNodes(Collection<NodeHost> nodes, String services) throws RestApiException {
        logger.debug("Adding nodes {}", nodes);
//        if (!nodelist.getFree().containsAll(nodes)) {
//            throw new IllegalArgumentException("Nodes must all be free");
//        }

        for (NodeHost nn : nodes) {
            nodelist.getMaster().getAdmin().addNewNode(nn.getAdmin(), services);
        }

        nodelist.activate(nodes);
    }

    private static List<Node> mkNodeList(Collection<NodeHost> coll) throws RestApiException {
        List<Node> ll = new ArrayList<Node>();
        for (NodeHost nn : coll) {
            nn.ensureNObject();
            ll.add(nn.getNObject());
        }
        return ll;
    }

    /**
     * Remove and rebalance nodes from the cluster.
     * @param nodes Nodes to remove. The nodes must be active.
     * @return A future to wait on. When its {@code get()} method returns the
     * rebalance will have been completed.
     * @throws RestApiException
     */
    public Future<Boolean> removeAndRebalance(Collection<NodeHost> nodes)
            throws RestApiException {
        logger.debug("Removing Nodes {}", nodes);
        if (!nodelist.getActive().containsAll(nodes)) {
            throw new IllegalArgumentException("Not all nodes specified were active");
        }

        List<NodeHost> nextActive = nodelist.getNextActive(nodes);
        nodelist.maybeSwitchMaster(nextActive);

        List<Node> rbKnown = mkNodeList(nextActive);
        List<Node> rbEject = mkNodeList(nodes);

        // They're still part of the cluster..
        rbKnown.addAll(rbEject);

        nodelist.getAdmin().rebalance(rbKnown, null, rbEject);
        nodelist.remove(nodes);
        return RebalanceWaiter.poll(nodelist.getMaster().getAdmin());
    }
}
package com.couchbase.couchbase.couchbase;

import com.couchbase.exceptions.HarnessError;
import com.couchbase.exceptions.HarnessException;
import com.couchbase.exceptions.RestApiException;
import com.couchbase.inputparameters.Host;
import com.couchbase.inputparameters.InputParameters;
import com.couchbase.logging.LogUtil;
import com.couchbase.utils.Retryer;
import com.couchbase.utils.remote.RemoteCommands;
import com.couchbase.utils.remote.SSHConnection;
import com.couchbase.utils.remote.SSHLoggingCommand;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class CouchbaseInstaller {
    final private static Logger logger = LogUtil.getLogger(CouchbaseInstaller.class);
    private InputParameters inputParameters;
    VersionTuple vTuple;
    public static final String ARCH_32 = "x86";
    public static final String ARCH_64 = "x86_64";
    public static final String AMD_64 = "amd64";
    public static final String RSRC_SCRIPT = "installer/cluster-install.py";
    static final String SHERLOCK_BUILD_URL = "http://latestbuilds.service.couchbase.com/builds/latestbuilds/couchbase-server/sherlock/";
    static final String WATSON_BUILD_URL = "http://latestbuilds.service.couchbase.com/builds/latestbuilds/couchbase-server/watson/";
    static final String SPOCK_BUILD_URL = "http://latestbuilds.service.couchbase.com/builds/latestbuilds/couchbase-server/spock/";
    static final String VULCAN_BUILD_URL = "http://latestbuilds.service.couchbase.com/builds/latestbuilds/couchbase-server/vulcan/";
    static final String ALICE_BUILD_URL="http://latestbuilds.service.couchbase.com/builds/latestbuilds/couchbase-server/alice/";
    static final String MADHATTER_BUILD_URL="http://latestbuilds.service.couchbase.com/builds/latestbuilds/couchbase-server/mad-hatter/";
    static final String CHESHIRECAT_BUILD_URL="http://latestbuilds.service.couchbase.com/builds/latestbuilds/couchbase-server/cheshire-cat/";

    public CouchbaseInstaller(InputParameters inputParameters){
        this.inputParameters=inputParameters;
    }

    static class VersionTuple {
        final String full;
        final String major;
        final String minor;
        final String patch;
        final String build;

        public VersionTuple(String major, String minor, String patch, String full, String build) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.full = full;
            this.build = build;
        }

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

    public static VersionTuple parse(String vString) throws Exception {
        String[] parts = vString.split("\\.");
        String trailer[] = parts[parts.length - 1].split("-");
        return new VersionTuple(parts[0], parts[1], trailer[0], vString, trailer[1]);
    }

    public void couchbaseInstall() throws ExecutionException {
        logger.info("Installing couchbase now");
        Collection<Host> nodes= inputParameters.getClusterNodes();
        ExecutorService svc = Executors.newFixedThreadPool(nodes.size());
        List<Future> futures = new ArrayList<Future>();

        for (final Host node : nodes) {
            Future f = svc.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    runNode(node);
                    return null;
                }
            });
            futures.add(f);
        }

        svc.shutdown();
        for (Future f : futures) {
            try {
                f.get();
            } catch (InterruptedException ex) {
                throw new ExecutionException(ex);
            }
        }
    }

    public void runNode(Host node) throws IOException {
        /**
         * Now to get system information.. we need SSH
         */
        SSHConnection sshConn;
        sshConn = new SSHConnection(inputParameters.getsshUsername(),
                inputParameters.getsshPassword(),
                node.ip);
        try{
            sshConn.connect();
            logger.debug("SSH Initialized for {}", this);

            RemoteCommands.OSInfo osInfo = RemoteCommands.getSystemInfo(sshConn);

            try{
                if (inputParameters.getupgrade()) {
                    vTuple = parse(inputParameters.getUpgradeVersion());
                    inputParameters.setclusterVersion(inputParameters.getUpgradeVersion());
                }else{
                    vTuple =parse(inputParameters.getClusterVersion());
                }
            } catch (Exception ex) {
                throw new IOException("Unable to parse version " + ex.getStackTrace());
            }
            /**
             * Build URL.
             */

            URL dlUrl = new URL(buildURL(vTuple, osInfo));

            InputStream is = getInstallScript();

            String remoteScript = "cluster-install.py";
            SSHLoggingCommand rmCmd = new SSHLoggingCommand(sshConn, "rm -rf " + remoteScript);
            try {
                rmCmd.execute();
                rmCmd.waitForExit(Integer.MAX_VALUE);
            }catch(Exception e){
                logger.error("Unable to install CB due to error: "+ e);
                System.exit(-1);
            }
            finally {
                rmCmd.close();
            }

            //noinspection OctalInteger
            sshConn.copyTo(remoteScript, is, 0755);
            SSHLoggingCommand cmd = new SSHLoggingCommand(sshConn, "python " + remoteScript + " " + dlUrl);
            try {
                cmd.execute();
                cmd.waitForExit(Integer.MAX_VALUE);
            }catch(Exception e){
                logger.error("Unable to install CB due to error: "+ e);
                System.exit(-1);
            }
            finally {
                cmd.close();
            }
        }finally {
            sshConn.close();
        }

    }


    private static InputStream getInstallScript() {
        InputStream is = CouchbaseInstaller.class
                .getClassLoader().getResourceAsStream(RSRC_SCRIPT);
        if (is == null) {
            throw new RuntimeException("Can't find script:" + RSRC_SCRIPT);
        }
        return is;
    }

    private String buildURL(VersionTuple vTuple, RemoteCommands.OSInfo osInfo) throws IOException {
        if (osInfo.getArch() == null || osInfo.getPlatform() == null || osInfo.getPackageType() == null) {
            throw new IOException("Unable to get os info");
        }
        String baseUrl = "";
        if (vTuple.major.equals("4")) {
            int minor = Integer.parseInt(vTuple.minor);
            baseUrl = SHERLOCK_BUILD_URL;
            if (minor >= 5) {
                baseUrl = WATSON_BUILD_URL;
            }
        } else if (vTuple.major.equals("5")) {
            int minor = Integer.parseInt(vTuple.minor);
            baseUrl = SPOCK_BUILD_URL;
            if (minor >= 5){
                baseUrl = VULCAN_BUILD_URL;
            }
        }
        else if (vTuple.major.equals("6")) {
            int minor = Integer.parseInt(vTuple.minor);
            baseUrl = ALICE_BUILD_URL;
            if (minor >= 5){
                baseUrl = MADHATTER_BUILD_URL;
            }
        } else if (vTuple.major.equals("7")) {
            baseUrl = CHESHIRECAT_BUILD_URL;
        }
        if (baseUrl == "") {
            throw new IOException("Base url not found for build. Installer supports only sherlock, watson and spock");
        }

        StringBuilder urlStr = new StringBuilder();
        urlStr.append(baseUrl + vTuple.build + "/");
        urlStr.append("couchbase-server-" + inputParameters.getbuildtype());
        if (osInfo.getPlatform().contains("centos")) {
            urlStr.append("-");
            urlStr.append(vTuple.full + "-");
            urlStr.append(osInfo.getPlatform());
            urlStr.append("." + osInfo.getArch() + "." + osInfo.getPackageType());
        } else {
            urlStr.append("_");
            urlStr.append(vTuple.full + "-");
            urlStr.append(osInfo.getPlatform());
            urlStr.append("_" + osInfo.getArch() + "." + osInfo.getPackageType());
        }
        return urlStr.toString();
    }

}
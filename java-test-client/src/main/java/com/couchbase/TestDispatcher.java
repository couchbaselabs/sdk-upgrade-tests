package com.couchbase;

import com.couchbase.test.DuringUpgradeTest;
import com.couchbase.test.PostUpgradeTest;
import com.couchbase.test.PreUpgradeTest;
import com.couchbase.utils.ClusterTestInfo;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;


public class TestDispatcher {

  private static ClusterTestInfo clusterTestInfo;

  public TestDispatcher() {

  }

  public static void runPreUpgradeTests() {
    System.out.println("DOING PRE UPGRADE");
    LauncherDiscoveryRequest launchRequest = LauncherDiscoveryRequestBuilder.request()
            .selectors(
                    //selectPackage("com.couchbase.tests"),
                    selectClass(PreUpgradeTest.class)
            )
            .build();

    Launcher launcher = LauncherFactory.create();

    // Register a listener of your choice
    SummaryGeneratingListener listener = new SummaryGeneratingListener();
    launcher.registerTestExecutionListeners(listener);

    launcher.execute(launchRequest);

    TestExecutionSummary summary = listener.getSummary();
    summary.printTo(new PrintWriter(System.out));

  }

  public static void runDuringUpgradeTests() {
    System.out.println("STARING DURING UPGRADE TESTS");
    DuringUpgradeTest duringUpgradeTest = new DuringUpgradeTest(clusterTestInfo);

    duringUpgradeTest.run();

  }

  public static void runPostUpgradeTests() {
    System.out.println("DOING POST UPGRADE TESTS");
    LauncherDiscoveryRequest launchRequest = LauncherDiscoveryRequestBuilder.request()
            .selectors(
                    //selectPackage("com.couchbase.tests"),
                    selectClass(PostUpgradeTest.class)
            )
            .build();

    Launcher launcher = LauncherFactory.create();

    // Register a listener of your choice
    SummaryGeneratingListener listener = new SummaryGeneratingListener();
    launcher.registerTestExecutionListeners(listener);

    launcher.execute(launchRequest);

    TestExecutionSummary summary = listener.getSummary();
    summary.printTo(new PrintWriter(System.out));

  }

  public void setClusterTestInfo(ClusterTestInfo clusterTestInfo) {
    this.clusterTestInfo = clusterTestInfo;
  }
}

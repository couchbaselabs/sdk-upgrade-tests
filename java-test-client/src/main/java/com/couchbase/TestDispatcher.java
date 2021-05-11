package com.couchbase;

import com.couchbase.grpc.protocol.Workload;
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
import org.junit.platform.reporting.legacy.xml.LegacyXmlReportGeneratingListener;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;


public class TestDispatcher {

  private static ClusterTestInfo clusterTestInfo;
  private static DuringUpgradeTest duringUpgradeTest;

  public TestDispatcher() {

  }

  public static void runPreUpgradeTests() {
    System.out.println("DOING PRE UPGRADE");
    runTests(PreUpgradeTest.class);
  }

  public static void runDuringUpgradeTests(boolean stop, List<Workload> workloadList) {
    if (!stop) {
      System.out.println("STARING DURING UPGRADE TESTS");
      duringUpgradeTest = new DuringUpgradeTest(clusterTestInfo, workloadList);
      duringUpgradeTest.run();
    } else {
      System.out.println("STOPPING DURING UPGRADE TESTS");
      duringUpgradeTest.stop();
    }
  }

  public static void runPostUpgradeTests() {
    System.out.println("DOING POST UPGRADE TESTS");
    runTests(PostUpgradeTest.class);
  }

  public void setClusterTestInfo(ClusterTestInfo clusterTestInfo) {
    this.clusterTestInfo = clusterTestInfo;
  }

  private static void runTests(Class testClass) {
    LauncherDiscoveryRequest launchRequest = LauncherDiscoveryRequestBuilder.request()
      .selectors(
        selectClass(testClass)
      )
      .build();

    Launcher launcher = LauncherFactory.create();

    SummaryGeneratingListener listener = new SummaryGeneratingListener();
    launcher.registerTestExecutionListeners(listener);

    Path reportsDir = Paths.get("target", "xml-reports", testClass.getName());

    LegacyXmlReportGeneratingListener xmlReportGeneratingListener = new LegacyXmlReportGeneratingListener(reportsDir, new PrintWriter(System.out));
    launcher.registerTestExecutionListeners(xmlReportGeneratingListener);

    launcher.execute(launchRequest);

    TestExecutionSummary summary = listener.getSummary();
    summary.printTo(new PrintWriter(System.out));
  }
}

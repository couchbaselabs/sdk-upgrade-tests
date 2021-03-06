package com.couchbase.utils.remote;/*
 * Copyright (c) 2013 Couchbase, Inc.
 */


import com.couchbase.logging.LogEntry;
import com.couchbase.logging.LogUtil;
import com.couchbase.logging.LoggingLineGobbler;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;

public class SSHLoggingCommand extends SSHCommand {
  private LoggingLineGobbler gobbleStderr;
  private LoggingLineGobbler gobbleStdout;
  private final Logger logger = LogUtil.getLogger(SSHLoggingCommand.class);
  private final String prefix;

  private void setupCommon(LoggingLineGobbler gobbler) {
    gobbler.setShouldFilter(false);
    gobbler.setLinePrefix(prefix);
    gobbler.start();
  }

  @Override
  public void handleStderr(InputStream serr) throws IOException {
    gobbleStderr = new LoggingLineGobbler(serr, logger, LogEntry.Level.WARN);
    setupCommon(gobbleStderr);
  }

  @Override
  public void handleStdout(InputStream sout) throws IOException {
    gobbleStdout = new LoggingLineGobbler(sout, logger, LogEntry.Level.DEBUG);
    setupCommon(gobbleStdout);
  }
  
  public SSHLoggingCommand(SSHConnection parent, String cmd, String prefix) {
    super(parent, cmd);
    this.prefix = prefix;
  }

  public SSHLoggingCommand(SSHConnection parent, String cmd) {
    this(parent, cmd, parent.getHostname());
  }
}

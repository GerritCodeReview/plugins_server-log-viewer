// Copyright (C) 2012 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.gerrit.plugins;

import com.google.gerrit.extensions.annotations.Export;
import com.google.gerrit.server.config.SitePaths;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
@Export("/*")
public final class LogViewerServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final Logger log = LoggerFactory
      .getLogger(LogViewerServlet.class);
  private final File logDir;

  @Inject
  LogViewerServlet(final SitePaths site) {
    this.logDir = site.logs_dir;
  }

  @Override
  public void doGet(final HttpServletRequest req, final HttpServletResponse rsp)
      throws IOException {
    if (req.getPathInfo() != null) {
      String logName = req.getPathInfo().replaceAll("^/", "");
      // String infoStr = "This file is " + logName;
      File logFile = new File(resolve(logDir), logName);
      if (logFile.canRead()) {
        log.debug("Opening " + logFile.getPath());
        writeResponse(logFile, rsp);
        rsp.setContentType("text/html");
        // rsp.setCharacterEncoding("UTF-8");
        rsp.setHeader("Content-Length", Long.toString(logFile.length()));
        // rsp.getOutputStream().write(data);
        return;
      }
    }
  }

  private static File resolve(final File logs_dir) {
    try {
      return logs_dir.getCanonicalFile();
    } catch (IOException e) {
      return logs_dir.getAbsoluteFile();
    }
  }

  private void writeResponse(File logFile, HttpServletResponse rsp)
      throws IOException {
    FileReader reader = new FileReader(logFile);
    try {
      PrintWriter out = rsp.getWriter();
      try {
        char[] tmp = new char[1024];
        int n;
        while ((n = reader.read(tmp)) > 0) {
          out.write(tmp, 0, n);
        }
      } finally {
        out.close();
      }
    } finally {
      reader.close();
    }
  }
}

/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import groovy.transform.ToString

/**
 *
 * @author akhikhl
 */
@ToString
class JavaExecParams {

  String main

  List<String> jvmArgs = []

  List<String> args = []

  boolean debug = false

  Map<String, String> systemProperties = [:]

  void arg(String a) {
    args.add(a)
  }

  void args(String... a) {
    args.addAll(a)
  }

  void jvmArg(String a) {
    jvmArgs.add(a)
  }

  void jvmArgs(String... a) {
    jvmArgs.addAll(a)
  }

  void systemProperties(Map<String, String> m) {
    if(systemProperties == null)
      systemProperties = [:]
    systemProperties << m
  }

  void systemProperty(String name, String value) {
    if(systemProperties == null)
      systemProperties = [:]
    systemProperties[name] = value
  }
}


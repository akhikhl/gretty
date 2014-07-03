/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
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
    systemProperties << m
  }

  void systemProperty(String name, String value) {
    systemProperties[name] = value
  }
}


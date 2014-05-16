/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty8;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author akhikhl
 */
public class Messages {

  private static ResourceBundle res = ResourceBundle.getBundle(Messages.class.getName(), Locale.ENGLISH);

  public static String getString(String key) {
    return res.getString(key);
  }
}


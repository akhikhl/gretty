/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.apache.commons.io.FilenameUtils

/**
 *
 * @author akhikhl
 */
class WebappUtils {

  static String getWebAppDestinationDirName(String resourceBase) {
    def file = resourceBase
    if(!(file instanceof File))
      file = new File(file.toString())
    FilenameUtils.getBaseName(file.name).replaceAll(/([\da-zA-Z_.-]+?)-((\d+\.)+[\da-zA-Z_.-]*)/, '$1')
  }	
}


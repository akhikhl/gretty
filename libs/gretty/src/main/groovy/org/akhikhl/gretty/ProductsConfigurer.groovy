/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
final class ProductsConfigurer {

  protected final Project project
  protected final File outputDir

  ProductsConfigurer(Project project) {
    this.project = project
    outputDir = new File(project.buildDir, 'output')
  }

  void configureProducts() {
    project.afterEvaluate {
      project.task('buildAllProducts')
      project.task('archiveAllProducts')
      project.products.productsMap.each { productName, product ->
        new ProductConfigurer(project, outputDir, productName, product).configureProduct()
      }
    }
  }
}

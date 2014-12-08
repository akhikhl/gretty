/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
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
      project.task('buildAllProducts', group: 'gretty') {
        description = 'Builds all configured gretty products.'
      }
      project.task('archiveAllProducts', group: 'gretty') {
        description = 'Archives all configured gretty products.'
      }
      project.products.productsMap.each { productName, product ->
        new ProductConfigurer(project, outputDir, productName, product).configureProduct()
      }
    }
  }
}

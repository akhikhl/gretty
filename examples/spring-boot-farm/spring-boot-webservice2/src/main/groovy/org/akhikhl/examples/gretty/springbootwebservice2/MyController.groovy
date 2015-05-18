/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.examples.gretty.springbootwebservice2

import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@RestController
@RequestMapping('/mycontroller')
class MyController {

  @RequestMapping(value = '/getdate', method = RequestMethod.POST)
  Map home() {
    return [ date: new Date().format('EEE, d MMM yyyy') ]
  }
}

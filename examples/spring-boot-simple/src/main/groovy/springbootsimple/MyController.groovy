package springbootsimple

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

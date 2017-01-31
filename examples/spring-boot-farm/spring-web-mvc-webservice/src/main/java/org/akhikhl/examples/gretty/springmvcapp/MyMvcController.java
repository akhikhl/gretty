package org.akhikhl.examples.gretty.springmvcapp;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class MyMvcController {
    @RequestMapping(method = RequestMethod.GET)
    public String test() {
        return "MVC TEST";
    }
}

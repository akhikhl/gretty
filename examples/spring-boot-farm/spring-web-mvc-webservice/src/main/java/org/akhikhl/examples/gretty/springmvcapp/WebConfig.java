package org.akhikhl.examples.gretty.springmvcapp;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@EnableWebMvc
@Configuration
@ComponentScan(basePackages = "org.akhikhl.examples.gretty.springmvcapp")
public class WebConfig extends WebMvcConfigurerAdapter {
}

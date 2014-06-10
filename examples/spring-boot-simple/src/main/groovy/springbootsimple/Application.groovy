package springbootsimple

import java.util.Arrays

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@EnableAutoConfiguration
@ComponentScan
class Application {

  static void main(String[] args) {
    SpringApplication.run(Application.class, args)
  }
}


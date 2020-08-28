package me.corningrey.camunda;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author corningrey
 */
@SpringBootApplication
@MapperScan("me.corningrey.camunda.**.dao")
public class CamundaBpmnApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CamundaBpmnApiApplication.class, args);
    }

}

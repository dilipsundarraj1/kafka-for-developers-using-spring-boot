


More info about kafka---->>>
Usually we don't need to use @EnableKafka annotation in springboot application as spring boot app provides by default the benefits of @EnableKafka. If we are using spring app (not spring boot app) then we have to use @EnableKafka for kafka configuration.

spring-kafka documents---->>>>
https://docs.spring.io/spring-kafka/docs/2.8.4/reference/pdf/spring-kafka-reference.pdf

https://docs.spring.io/spring-kafka/docs/2.8.4/reference/pdf/spring-kafka-reference.pdf


How to do the work in a separate thread in spring application-------->>>>>>

https://www.concretepage.com/spring/example_initializing_bean_spring

@RestController

public class ControllerClass implements InitializingBean {

  private final ServiceClass serviceClass;

public ControllerClass(ServiceClass serviceClass){
    this.serviceClass = serviceClass;
}

@Override
public void afterPropertiesSet(){
Thread threadObject = new Thread(serviceClass);
threadObject.start();
}
}


@Component
public class ServiceClass implements runnable{

public void run(){
// so basically threadObject.start(); above will call this run method
}
}




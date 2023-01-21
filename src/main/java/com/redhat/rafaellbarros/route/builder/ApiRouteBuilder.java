package com.redhat.rafaellbarros.route.builder;

import com.redhat.rafaellbarros.model.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.kafka.KafkaConstants;

public class ApiRouteBuilder extends RouteBuilder {

    protected String KAFKA_TOPIC = "{{quarkus.openshift.env.vars.kafka-topic}}";
    protected String KAFKA_TOPIC_PROCESSED = "{{quarkus.openshift.env.vars.kafka-topic-processed}}";
    protected String KAFKA_BOOTSTRAP_SERVERS = "{{quarkus.openshift.env.vars.kafka-bootstrap-servers}}";
    protected String KAFKA_GROUP_ID = "{{quarkus.openshift.env.vars.kafka-group-id}}";

    @Override
    public void configure() {

        //Route that consumes message to kafka topic
        from("kafka:"+ KAFKA_TOPIC + "?brokers=" + KAFKA_BOOTSTRAP_SERVERS + "&groupId=" + KAFKA_GROUP_ID)
                .routeId("kafkaConsumerRawTopic")
                .unmarshal(new JacksonDataFormat(Message.class))
                .log("Message received from Kafka Topic raw : ${body}")
                .to("direct:insertProcessedTopic")
        ;

        //Route insert object on Processed Topic
        from("direct:insertProcessedTopic")
                .routeId("insertProcessedTopic")
                .marshal().json()   // marshall message to send to kafka
                .setHeader(KafkaConstants.KEY, constant("Camel")) // Key of the message
                .to("kafka:"+ KAFKA_TOPIC_PROCESSED + "?brokers=" + KAFKA_BOOTSTRAP_SERVERS)
                .log("Message send from Kafka Topic processed : ${body}")
        ;
    }
}
package msa.emaia.outbox;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Not lazy: the app has spring.main.lazy-initialization=true globally. That
 * also makes Boot's auto-configured RabbitAdmin lazy, so it never registers
 * in time to auto-declare exchanges/queues/bindings on the broker at startup.
 * @Lazy(false) is repeated on every bean method here because the class-level
 * annotation alone did not override the global lazy-init post-processor.
 */
@Configuration
@Lazy(false)
public class RabbitMQConfig {

    public static final String EVENTS_EXCHANGE = "emaia.events";
    public static final String SUPPLIER_DELETED_QUEUE = "supplier.deleted.queue";
    public static final String SUPPLIER_DELETED_ROUTING_KEY = "supplier.deleted";

    public static final String SUPPLIER_CREATED_ROUTING_KEY = "partner.supplier.created";
    public static final String SUPPLIER_UPDATED_ROUTING_KEY = "partner.supplier.updated";
    public static final String SUPPLIER_STATUS_CHANGED_ROUTING_KEY = "partner.supplier.status_changed";
    public static final String PROJECT_CREATED_ROUTING_KEY = "partner.project.created";
    public static final String PROJECT_CLOSED_ROUTING_KEY = "partner.project.closed";

    // Files de test pour vérifier la publication (Livrable de dev)
    public static final String PARTNER_SUPPLIER_TEST_QUEUE = "partner.supplier.test.queue";
    public static final String PARTNER_PROJECT_TEST_QUEUE = "partner.project.test.queue";

    // Consommateur de projection (Jour 3 après-midi) + dead-letter queue associée
    public static final String EVENTS_DLX = "emaia.events.dlx";
    public static final String PARTNER_SUPPLIER_PROJECTION_QUEUE = "partner.supplier.projection.queue";
    public static final String PARTNER_SUPPLIER_PROJECTION_DLQ = "partner.supplier.projection.dlq";
    public static final String PARTNER_PROJECT_PROJECTION_QUEUE = "partner.project.projection.queue";
    public static final String PARTNER_PROJECT_PROJECTION_DLQ = "partner.project.projection.dlq";

    // Consommateurs de notifications (Jour 3 après-midi) + dead-letter queue associée
    public static final String ADMIN_NOTIFICATION_REQUESTED_ROUTING_KEY = "assessment.admin_notification.requested";
    public static final String SUPPLIER_EMAIL_REQUESTED_ROUTING_KEY = "assessment.supplier_email.requested";
    public static final String ADMIN_NOTIFICATION_REQUESTED_QUEUE = "assessment.admin_notification.requested.queue";
    public static final String ADMIN_NOTIFICATION_REQUESTED_DLQ = "assessment.admin_notification.requested.dlq";
    public static final String SUPPLIER_EMAIL_REQUESTED_QUEUE = "assessment.supplier_email.requested.queue";
    public static final String SUPPLIER_EMAIL_REQUESTED_DLQ = "assessment.supplier_email.requested.dlq";

    @Bean
    @Lazy(false)
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    @Lazy(false)
    public TopicExchange eventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE);
    }

    @Bean
    @Lazy(false)
    public Queue supplierDeletedQueue() {
        return new Queue(SUPPLIER_DELETED_QUEUE, true);
    }

    @Bean
    @Lazy(false)
    public Binding supplierDeletedBinding() {
        return BindingBuilder.bind(supplierDeletedQueue()).to(eventsExchange()).with(SUPPLIER_DELETED_ROUTING_KEY);
    }

    @Bean
    @Lazy(false)
    public Queue partnerSupplierTestQueue() {
        return new Queue(PARTNER_SUPPLIER_TEST_QUEUE, true);
    }

    @Bean
    @Lazy(false)
    public Binding partnerSupplierTestBinding() {
        return BindingBuilder.bind(partnerSupplierTestQueue()).to(eventsExchange()).with("partner.supplier.*");
    }

    @Bean
    @Lazy(false)
    public Queue partnerProjectTestQueue() {
        return new Queue(PARTNER_PROJECT_TEST_QUEUE, true);
    }

    @Bean
    @Lazy(false)
    public Binding partnerProjectTestBinding() {
        return BindingBuilder.bind(partnerProjectTestQueue()).to(eventsExchange()).with("partner.project.*");
    }

    @Bean
    @Lazy(false)
    public TopicExchange eventsDeadLetterExchange() {
        return new TopicExchange(EVENTS_DLX);
    }

    @Bean
    @Lazy(false)
    public Queue partnerSupplierProjectionDlq() {
        return new Queue(PARTNER_SUPPLIER_PROJECTION_DLQ, true);
    }

    @Bean
    @Lazy(false)
    public Binding partnerSupplierProjectionDlqBinding() {
        return BindingBuilder.bind(partnerSupplierProjectionDlq()).to(eventsDeadLetterExchange()).with("partner.supplier.*");
    }

    @Bean
    @Lazy(false)
    public Queue partnerProjectProjectionDlq() {
        return new Queue(PARTNER_PROJECT_PROJECTION_DLQ, true);
    }

    @Bean
    @Lazy(false)
    public Binding partnerProjectProjectionDlqBinding() {
        return BindingBuilder.bind(partnerProjectProjectionDlq()).to(eventsDeadLetterExchange()).with("partner.project.*");
    }

    @Bean
    @Lazy(false)
    public Queue partnerSupplierProjectionQueue() {
        return QueueBuilder.durable(PARTNER_SUPPLIER_PROJECTION_QUEUE)
                .withArgument("x-dead-letter-exchange", EVENTS_DLX)
                .build();
    }

    @Bean
    @Lazy(false)
    public Binding partnerSupplierProjectionBinding() {
        return BindingBuilder.bind(partnerSupplierProjectionQueue()).to(eventsExchange()).with("partner.supplier.*");
    }

    @Bean
    @Lazy(false)
    public Queue partnerProjectProjectionQueue() {
        return QueueBuilder.durable(PARTNER_PROJECT_PROJECTION_QUEUE)
                .withArgument("x-dead-letter-exchange", EVENTS_DLX)
                .build();
    }

    @Bean
    @Lazy(false)
    public Binding partnerProjectProjectionBinding() {
        return BindingBuilder.bind(partnerProjectProjectionQueue()).to(eventsExchange()).with("partner.project.*");
    }

    @Bean
    @Lazy(false)
    public Queue adminNotificationRequestedDlq() {
        return new Queue(ADMIN_NOTIFICATION_REQUESTED_DLQ, true);
    }

    @Bean
    @Lazy(false)
    public Binding adminNotificationRequestedDlqBinding() {
        return BindingBuilder.bind(adminNotificationRequestedDlq()).to(eventsDeadLetterExchange()).with(ADMIN_NOTIFICATION_REQUESTED_ROUTING_KEY);
    }

    @Bean
    @Lazy(false)
    public Queue adminNotificationRequestedQueue() {
        return QueueBuilder.durable(ADMIN_NOTIFICATION_REQUESTED_QUEUE)
                .withArgument("x-dead-letter-exchange", EVENTS_DLX)
                .build();
    }

    @Bean
    @Lazy(false)
    public Binding adminNotificationRequestedBinding() {
        return BindingBuilder.bind(adminNotificationRequestedQueue()).to(eventsExchange()).with(ADMIN_NOTIFICATION_REQUESTED_ROUTING_KEY);
    }

    @Bean
    @Lazy(false)
    public Queue supplierEmailRequestedDlq() {
        return new Queue(SUPPLIER_EMAIL_REQUESTED_DLQ, true);
    }

    @Bean
    @Lazy(false)
    public Binding supplierEmailRequestedDlqBinding() {
        return BindingBuilder.bind(supplierEmailRequestedDlq()).to(eventsDeadLetterExchange()).with(SUPPLIER_EMAIL_REQUESTED_ROUTING_KEY);
    }

    @Bean
    @Lazy(false)
    public Queue supplierEmailRequestedQueue() {
        return QueueBuilder.durable(SUPPLIER_EMAIL_REQUESTED_QUEUE)
                .withArgument("x-dead-letter-exchange", EVENTS_DLX)
                .build();
    }

    @Bean
    @Lazy(false)
    public Binding supplierEmailRequestedBinding() {
        return BindingBuilder.bind(supplierEmailRequestedQueue()).to(eventsExchange()).with(SUPPLIER_EMAIL_REQUESTED_ROUTING_KEY);
    }

    @Bean
    @Lazy(false)
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @Lazy(false)
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    /**
     * ApplicationRunner beans are always resolved and invoked by
     * SpringApplication.run() after context startup, regardless of
     * spring.main.lazy-initialization - unlike relying on RabbitAdmin's own
     * ContextRefreshedEvent listener, which never fired here.
     */
    @Bean
    @Lazy(false)
    public ApplicationRunner rabbitTopologyInitializer(RabbitAdmin rabbitAdmin) {
        return args -> rabbitAdmin.initialize();
    }

    /**
     * Same lazy-init problem hits @RabbitListener containers: with
     * spring.main.lazy-initialization=true, the SimpleRabbitListenerContainerFactory
     * infrastructure isn't eagerly created either, so the container backing
     * SupplierDeletedEventListener never starts consuming on its own.
     * Forcing the registry to start here, after topology init.
     */
    @Bean
    @Lazy(false)
    public ApplicationRunner rabbitListenerStarter(RabbitListenerEndpointRegistry registry) {
        return args -> {
            if (!registry.isRunning()) {
                registry.start();
            }
        };
    }
}
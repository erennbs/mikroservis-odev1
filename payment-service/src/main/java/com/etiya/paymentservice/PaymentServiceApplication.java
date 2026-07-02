package com.etiya.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// payment-service hem tuketici (order-created / Inbox) hem uretici (outbox -> Debezium ->
// payment-created) rolundedir. Outbox yayinini order-service gibi Debezium (CDC) yapar;
// bu yuzden burada da polling/@Scheduled yoktur.
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}

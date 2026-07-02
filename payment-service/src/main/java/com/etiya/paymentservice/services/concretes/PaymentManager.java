package com.etiya.paymentservice.services.concretes;

import com.etiya.paymentservice.entities.Payment;
import com.etiya.paymentservice.events.PaymentCreatedEvent;
import com.etiya.paymentservice.outbox.OutboxService;
import com.etiya.paymentservice.repositories.PaymentRepository;
import com.etiya.paymentservice.services.abstracts.PaymentService;
import com.etiya.paymentservice.services.dtos.requests.CreatePaymentRequest;
import com.etiya.paymentservice.services.dtos.requests.UpdatePaymentRequest;
import com.etiya.paymentservice.services.dtos.responses.CreatedPaymentResponse;
import com.etiya.paymentservice.services.dtos.responses.DeletedPaymentResponse;
import com.etiya.paymentservice.services.dtos.responses.GetAllPaymentsResponse;
import com.etiya.paymentservice.services.dtos.responses.GetByIdPaymentResponse;
import com.etiya.paymentservice.services.dtos.responses.UpdatedPaymentResponse;
import com.etiya.paymentservice.services.exceptions.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business layer implementation. Maps between request/response DTOs and the entity,
 * and applies business rules before delegating to the data access layer.
 *
 * <p>{@link #add} additionally records a PaymentCreated event in the outbox table within the SAME
 * transaction as the payment write (Transactional Outbox), so downstream services get notified
 * atomically. This is the single creation path: the OrderCreated inbox handler also creates payments
 * by calling {@link #add}, so both the CRUD API and event-driven creation emit PaymentCreated.</p>
 */
@Service
public class PaymentManager implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OutboxService outboxService;

    public PaymentManager(PaymentRepository paymentRepository, OutboxService outboxService) {
        this.paymentRepository = paymentRepository;
        this.outboxService = outboxService;
    }

    @Override
    @Transactional
    public CreatedPaymentResponse add(CreatePaymentRequest request) {
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setCustomerId(request.getCustomerId());
        payment.setAmount(request.getAmount());
        payment.setStatus(request.getStatus());

        Payment saved = paymentRepository.save(payment);

        // Transactional Outbox: insert PaymentCreated into the outbox table in the SAME transaction
        // as the payment write (atomic). Debezium then streams the insert from the WAL to the
        // "payment-created" topic; there is no inline broker publish and no polling relay.
        outboxService.record(
                "Payment",
                String.valueOf(saved.getId()),
                "PaymentCreated",
                new PaymentCreatedEvent(
                        saved.getId(),
                        saved.getOrderId(),
                        saved.getCustomerId(),
                        saved.getAmount(),
                        saved.getStatus()));

        return new CreatedPaymentResponse(
                saved.getId(),
                saved.getOrderId(),
                saved.getCustomerId(),
                saved.getAmount(),
                saved.getStatus());
    }

    @Override
    public UpdatedPaymentResponse update(UpdatePaymentRequest request) {
        Payment payment = findPaymentOrThrow(request.getId());
        payment.setOrderId(request.getOrderId());
        payment.setCustomerId(request.getCustomerId());
        payment.setAmount(request.getAmount());
        payment.setStatus(request.getStatus());

        Payment saved = paymentRepository.save(payment);

        return new UpdatedPaymentResponse(
                saved.getId(),
                saved.getOrderId(),
                saved.getCustomerId(),
                saved.getAmount(),
                saved.getStatus());
    }

    @Override
    public DeletedPaymentResponse delete(int id) {
        Payment payment = findPaymentOrThrow(id);
        paymentRepository.deleteById(id);
        return new DeletedPaymentResponse(payment.getId(), payment.getOrderId());
    }

    @Override
    public List<GetAllPaymentsResponse> getAll() {
        return paymentRepository.findAll().stream()
                .map(payment -> new GetAllPaymentsResponse(
                        payment.getId(),
                        payment.getOrderId(),
                        payment.getCustomerId(),
                        payment.getAmount(),
                        payment.getStatus()))
                .toList();
    }

    @Override
    public GetByIdPaymentResponse getById(int id) {
        Payment payment = findPaymentOrThrow(id);
        return new GetByIdPaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getStatus());
    }

    private Payment findPaymentOrThrow(int id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Payment not found with id: " + id));
    }
}

package com.pagamentos.controller;

import com.pagamentos.model.Payment;
import com.pagamentos.model.PaymentStatus;
import com.pagamentos.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    @Value("${app.payment.webhook-secret}")
    private String webhookSecret;

    private final PaymentRepository paymentRepository;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        if (sigHeader == null) {
            return ResponseEntity.badRequest().body("Missing Stripe-Signature header");
        }

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("⚠️  Webhook signature verification failed.", e);
            return ResponseEntity.badRequest().body("Invalid signature");
        } catch (Exception e) {
            log.error("⚠️  Webhook error.", e);
            return ResponseEntity.badRequest().body("Webhook error");
        }

        // Processar o evento
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        }

        log.info("Received event: {} - {}", event.getId(), event.getType());

        switch (event.getType()) {
            case "payment_intent.succeeded":
                log.info("Payment succeeded!");
                // Atualizar status do pagamento no banco
                break;

            case "payment_intent.payment_failed":
                log.info("Payment failed!");
                // Atualizar status do pagamento no banco
                break;

            case "charge.refunded":
                log.info("Charge refunded!");
                // Atualizar status para reembolsado
                break;

            default:
                log.warn("Unhandled event type: {}", event.getType());
        }

        return ResponseEntity.ok("Webhook received");
    }
}
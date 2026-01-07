package com.pagamentos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentGatewayFactory {

    private final Map<String, PaymentGatewayService> gatewayServices;

    @Autowired
    public PaymentGatewayFactory(Map<String, PaymentGatewayService> gatewayServices) {
        this.gatewayServices = gatewayServices;
    }

    public PaymentGatewayService getGateway(String gatewayName) {
        String serviceName = gatewayName.toLowerCase() + "Service";
        PaymentGatewayService service = gatewayServices.get(serviceName);

        if (service == null) {
            throw new RuntimeException("Gateway não suportado: " + gatewayName);
        }

        return service;
    }
}
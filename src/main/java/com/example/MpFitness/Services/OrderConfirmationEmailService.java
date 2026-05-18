package com.example.MpFitness.Services;

import com.example.MpFitness.Model.Pedido;

public interface OrderConfirmationEmailService {
    void sendOrderConfirmationEmail(Pedido pedido);
}

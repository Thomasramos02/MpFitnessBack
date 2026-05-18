package com.example.MpFitness.Services;

import com.example.MpFitness.Model.Pedido;
import com.example.MpFitness.Model.PedidoItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
public class OrderConfirmationEmailServiceImpl implements OrderConfirmationEmailService {

    private final JavaMailSender mailSender;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Value("${spring.mail.username:no-reply@mpfitness.local}")
    private String fromEmail;

    public OrderConfirmationEmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOrderConfirmationEmail(Pedido pedido) {
        try {
            if (pedido.getCliente() == null || pedido.getCliente().getEmail() == null) {
                log.warn("Pedido {} nao possui email de cliente", pedido.getId());
                return;
            }

            String toEmail = pedido.getCliente().getEmail();
            String subject = String.format("Pedido Confirmado #%d - MP Fitness Store", pedido.getId());
            String body = buildOrderConfirmationEmailBody(pedido);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email de confirmacao de pedido enviado para {} - Pedido: {}", toEmail, pedido.getId());
        } catch (Exception e) {
            log.error("Erro ao enviar email de confirmacao do pedido {}: {}", pedido.getId(), e.getMessage(), e);
            // Nao lancamos excecao para nao quebrar o fluxo de checkout
        }
    }

    private String buildOrderConfirmationEmailBody(Pedido pedido) {
        StringBuilder body = new StringBuilder();
        
        body.append(String.format("Ola %s,%n%n", pedido.getCliente().getNome()));
        body.append("Seu pedido foi confirmado com sucesso!%n%n");
        
        body.append("=== DETALHES DO PEDIDO ===%n");
        body.append(String.format("Numero do Pedido: #%d%n", pedido.getId()));
        body.append(String.format("Data do Pedido: %s%n", pedido.getDataCompra().format(dateFormatter)));
        body.append(String.format("Status: %s%n%n", formatStatus(pedido.getStatusPedido().toString())));
        
        body.append("=== PRODUTOS ===%n");
        for (PedidoItem item : pedido.getProdutos()) {
            body.append(String.format("%s%n", item.getNomeProduto()));
            body.append(String.format("  Quantidade: %d%n", item.getQuantidade()));
            body.append(String.format("  Valor Unitario: %s%n", currencyFormat.format(item.getValorUnitario())));
            body.append(String.format("  Subtotal: %s%n%n", 
                currencyFormat.format(item.getValorUnitario().multiply(new BigDecimal(item.getQuantidade())))));
        }
        
        body.append("=== RESUMO FINANCEIRO ===%n");
        body.append(String.format("Subtotal dos Produtos: %s%n", currencyFormat.format(pedido.getValorProdutos())));
        body.append(String.format("Frete: %s%n", currencyFormat.format(pedido.getValorFrete())));
        body.append(String.format("VALOR TOTAL: %s%n%n", currencyFormat.format(pedido.getValorTotal())));
        
        body.append("=== ENDERECO DE ENTREGA ===%n");
        if (pedido.getEnderecoEntrega() != null) {
            body.append(String.format("%s, %s%n", 
                pedido.getEnderecoEntrega().getRua(), 
                pedido.getEnderecoEntrega().getNumero()));
            if (pedido.getEnderecoEntrega().getComplemento() != null) {
                body.append(String.format("Complemento: %s%n", pedido.getEnderecoEntrega().getComplemento()));
            }
            body.append(String.format("%s - %s%n", 
                pedido.getEnderecoEntrega().getBairro(), 
                pedido.getEnderecoEntrega().getCidade()));
            body.append(String.format("%s - %s%n", 
                pedido.getEnderecoEntrega().getEstado(), 
                pedido.getEnderecoEntrega().getCep()));
        }
        body.append("%n");
        
        if (pedido.getCodigoRastreamento() != null && !pedido.getCodigoRastreamento().isEmpty()) {
            body.append("=== RASTREAMENTO ===%n");
            body.append(String.format("Codigo de Rastreamento: %s%n", pedido.getCodigoRastreamento()));
            body.append("Acompanhe seu pedido atraves de nossa plataforma.%n%n");
        }
        
        body.append("=== INFORMACOES DE CONTATO ===%n");
        body.append("Duvidas sobre seu pedido? Entre em contato conosco:%n");
        body.append("WhatsApp: (37) 98849-3019%n");
        body.append("Email: contato@mpfitness.com.br%n%n");
        
        body.append("Obrigado pela sua compra!%n");
        body.append("MP Fitness Store%n");
        
        return body.toString();
    }

    private String formatStatus(String status) {
        return switch(status) {
            case "PRE_PEDIDO" -> "Aguardando Pagamento";
            case "AGUARDANDO_PAGAMENTO" -> "Aguardando Pagamento";
            case "PAGAMENTO_CONFIRMADO" -> "Pagamento Confirmado";
            case "PREPARANDO_ENVIO" -> "Preparando para Envio";
            case "ENVIADO" -> "Enviado";
            case "ENTREGUE" -> "Entregue";
            case "CANCELADO" -> "Cancelado";
            case "RESERVA_EXPIRADA" -> "Reserva Expirada";
            default -> status;
        };
    }
}

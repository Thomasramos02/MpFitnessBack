# Backend - Tarefas Essenciais

## ✅ 1. Google OAuth2 - Credenciais (5 min) - CONCLUÍDO

**O que foi feito:**

- [x] Client ID/Secret do Google Cloud Console em `application.properties`
- [x] Confirmado `frontend.url=http://localhost:3000`
- [x] Backend roda em 8080 e autentication funciona

**Arquivos utilizados:**

- `application.properties` (linhas 60-63)
- `OAuth2LoginSuccessHandler.java` (setHttpOnly=false para Opção B)

---

## ✅ 2. Recuperação de Senha - CONCLUÍDO

**O que foi feito:**

- [x] Endpoint `POST /api/auth/forgot-password` funcionando
- [x] Endpoint `POST /api/auth/reset-password` funcionando
- [x] Envio de email via Gmail SMTP configurado
- [x] JWT token para reset de senha implementado
- [x] SecurityConfig atualizado para permitir endpoints públicos

**Arquivos criados/modificados:**

- `ForgotPasswordController.java` ✅
- `ForgotPasswordService.java` / `ForgotPasswordServiceImpl.java` ✅
- `PasswordResetEmailService.java` / `PasswordResetEmailServiceImpl.java` ✅
- `MailConfig.java` (Configuração SMTP com STARTTLS) ✅
- `application.properties` (Email SMTP) ✅

**⚠️ IMPORTANTE - CONFIGURAÇÃO DE EMAIL:**

**Passo a passo para atualizar email:**

1. Abrir `src/main/resources/application.properties` linha 41
2. Trocar `spring.mail.username=thomasramosoliveira@gmail.com` pelo email do dono
3. Ir em: https://myaccount.google.com/security → "Senhas de app"
4. Criar nova senha de app para "Mail"
5. Copiar senha gerada e colar em `spring.mail.password=`
6. **NUNCA usar a senha comum da conta, sempre usar Senha de App**

**Teste:**

- [x] `mvnw.cmd clean test` - Todos os testes passam
- [x] Email de reset enviado com sucesso
- [x] Link de reset funciona

---

## 3. Integração Mercado Pago (EM PLANEJAMENTO)

**O que fazer - Passo a Passo:**

**FASE 1 - Configuração Básica (2h)**

1. [ ] Adicionar credenciais do Mercado Pago em `application.properties`:
   ```properties
   mercado_pago.access_token=${MERCADO_PAGO_ACCESS_TOKEN:}
   mercado_pago.webhook_secret=${MERCADO_PAGO_WEBHOOK_SECRET:}
   ```
2. [ ] Criar classe `MercadoPagoConfig.java` para inicializar SDK
3. [ ] Criar `MercadoPagoService.java` para encapsular lógica

**FASE 2 - Endpoints de Pagamento (3h)**

1. [ ] POST `/api/payments/create` - Criar preferência de pagamento
2. [ ] POST `/api/payments/webhook` - Receber notificações
3. [ ] GET `/api/payments/{payment_id}` - Consultar status

**FASE 3 - Atualização de Pedidos (2h)**

1. [ x] Atualizar `PedidoStatus` com novos estados
2. [x ] Implementar lógica de transição de status
3. [ ] Adicionar testes integrados
4. [ x] Adicionar Logica de retirar pedido da loja na classe meus pedidos
5. [x ] Adicionar observações em meus pedidos
6. [x ] Informar tanto no gerenciar pedido e meus pedidos tipo de entrega do pedido: Retirar da loja ou Endereço
7. [ x ] Retirar subcategorias no filtro de http://localhost:3000/produtos
8. [x ] Adicionar feedbacks para cliente como senha errada, o email nao existe etc, retirar debug da tela frontend
9. [x ] Em detalhes de um produto depois de ser clicado o card, mostrar inf'ormações reais do produto(Implementar) ex: http://localhost:3000/produtos/17
10. [ x ] Traduzir lista de desejo (wishlist) para portugues ---> Estoque na wishlist é fake
11. [ ]ajustar responsivamente componentes
12. [ x ]ajustar logica de apos pagamento criar produto, nao antes isso no Mercado Pago
13. [ x] logica de frete
14. [ ] integrar mercado pago
15. [ x] Rupturua de estoque

**Arquivos a criar:**

- `MercadoPagoConfig.java`
- `MercadoPagoService.java`
- `PaymentController.java` (expandir)
- `MercadoPagoTest.java`

**Teste:**

```bash
# Após implementar
mvnw.cmd clean test -Dtest=MercadoPagoTest
```

---

## 4. Dashboard - Ruptura de Estoque (FUTURO)

**O que fazer:**

- [x ] Endpoint `GET /api/admin/metrics/stockout-products`
- [x ] Lógica de produtos com quantidade <= 0
- [ ] Testes para validar

---

## ✅ Checklist Final

- [x] Backend compila sem erros
- [x] Google OAuth funcionando ✅
- [x] Recuperação de senha funcionando ✅
- [x] Email configurado (mas trocar para email do dono)
- [ ] Testes Mercado Pago implementados
- [ ] Testes passam: `mvnw.cmd clean test`

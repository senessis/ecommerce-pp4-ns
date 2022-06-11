package pl.jkanclerz.sales.payment;

public interface PaymentGateway {
    RegisterPaymentResponse register(RegisterPaymentRequest registerPaymentRequest);
}

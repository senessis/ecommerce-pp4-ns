package pl.jkanclerz.sales;

import pl.jkanclerz.sales.cart.Cart;
import pl.jkanclerz.sales.cart.CartItem;
import pl.jkanclerz.sales.cart.CartStorage;
import pl.jkanclerz.sales.offerting.Offer;
import pl.jkanclerz.sales.offerting.OfferCalculator;
import pl.jkanclerz.sales.payment.DummyPaymentGateway;
import pl.jkanclerz.sales.payment.PaymentDetails;
import pl.jkanclerz.sales.payment.PaymentGateway;
import pl.jkanclerz.sales.product.ListProductDetailsProvider;
import pl.jkanclerz.sales.product.ProductDetails;
import pl.jkanclerz.sales.product.ProductDetailsProvider;
import pl.jkanclerz.sales.product.ProductNotAvailableException;
import pl.jkanclerz.sales.reservation.InMemoryReservationStorage;
import pl.jkanclerz.sales.reservation.Reservation;

import java.util.UUID;

public class Sales {
    private CartStorage cartStorage;
    private ProductDetailsProvider productDetailsProvider;

    private PaymentGateway paymentGateway;

    private InMemoryReservationStorage reservationStorage;

    private OfferCalculator offerCalculator;

    public Sales(CartStorage cartStorage, ProductDetailsProvider productDetailsProvider, PaymentGateway paymentGateway, InMemoryReservationStorage reservationStorage, OfferCalculator offerCalculator) {
        this.cartStorage = cartStorage;
        this.productDetailsProvider = productDetailsProvider;
        this.paymentGateway = paymentGateway;
        this.reservationStorage = reservationStorage;
        this.offerCalculator = offerCalculator;
    }

    public Offer getCurrentOffer(String customerId) {
        Cart cart = cartStorage.getBy(customerId)
                .orElse(Cart.getEmptyCart());

        return offerCalculator.calculateOffer(cart);
    }

    public void addToCart(String customerId, String productId) {
        Cart cart = cartStorage.getBy(customerId)
                .orElse(Cart.getEmptyCart());

        ProductDetails details = productDetailsProvider.getById(productId)
                        .orElseThrow(ProductNotAvailableException::new);

        cart.add(CartItem.of(productId,
                details.getName(),
                details.getPrice()));

        cartStorage.save(customerId, cart);
    }

    public PaymentDetails acceptOffer(String customerId, CustomerData customerData) {
        Cart cart = cartStorage.getBy(customerId)
                .orElse(Cart.getEmptyCart());

        Offer currentOffer = offerCalculator.calculateOffer(cart);

        String reservationId = UUID.randomUUID().toString();

        Reservation reservation = Reservation.of(reservationId, currentOffer.getTotal(), customerData);
        reservation.registerPayment(paymentGateway);

        reservationStorage.save(reservation);

        return new PaymentDetails(reservationId, reservation.getPaymentId(), reservation.getPaymentUrl());
    }


}

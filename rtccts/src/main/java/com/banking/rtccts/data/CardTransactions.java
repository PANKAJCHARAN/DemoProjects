package com.banking.rtccts.data;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "card_transactions")
public class CardTransactions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;
    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false)
    private CreditCard creditCard;
    @Column(name = "amount", nullable = false)
    private double amount;
    @Column(name = "timestamp", nullable = false)
    private Long timestamp;
    @Column(name = "merchant_id", nullable = false)
    private String merchantId;

    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL)
    private CardAuthLog auth;
}

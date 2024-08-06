package com.banking.rtccts.data;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "credit_card")
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Long cardId;
    @Column(name = "card_number", unique = true, nullable = false)
    private int cardNumber;
    @Column(name = "customer_name", nullable = false)
    private String customerName;
    @Column(name = "card_limit", nullable = false)
    private double limit;
    @Column(name = "transaction_limit", nullable = false)
    private double transactionLimit;

    @OneToMany(mappedBy = "creditCard")
    private List<CardTransactions> transactions;
}

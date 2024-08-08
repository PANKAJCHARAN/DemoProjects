package com.banking.rtccts.data;

import jakarta.persistence.*;
import lombok.Data;

import java.security.Timestamp;

@Data
@Entity
@Table(name = "auth_log")
public class CardAuthLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_id")
    private Long authId;
    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false)
    private CreditCard creditCard;
    @Column(name = "status", nullable = false)
    private String status;
    @JoinColumn(name = "amount", nullable = false)
    private double transactionAmount;
    @JoinColumn(name = "timestamp", nullable = false)
    private Long timestamp;
}

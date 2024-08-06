package com.banking.rtccts.data;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "auth_log")
public class CardAuthLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_id")
    private Long authId;
    @Column(name = "card_number", unique = true, nullable = false)
    private int cardNumber;
    @Column(name = "is_authorized", nullable = false)
    private boolean isAuthorized;
    @JoinColumn(name = "amount", nullable = false)
    private double transactionAmount;
    @JoinColumn(name = "timestamp", nullable = false)
    private Long timestamp;
}

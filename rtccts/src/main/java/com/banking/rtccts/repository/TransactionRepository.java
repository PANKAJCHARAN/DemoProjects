package com.banking.rtccts.repository;

import com.banking.rtccts.data.CardTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<CardTransactions, Long> {
}

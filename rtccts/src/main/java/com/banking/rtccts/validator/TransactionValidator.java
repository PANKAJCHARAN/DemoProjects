package com.banking.rtccts.validator;

import com.banking.rtccts.data.CreditCard;
import com.banking.rtccts.exception.CreditCardNotFoundException;
import com.banking.rtccts.repository.CreditCardRepository;
import com.rtccts.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Predicate;

@Component
public class TransactionValidator {

    @Autowired
    private CreditCardRepository creditCardRepository;

    Predicate<Transaction> isTransactionAmountLessThanLimit = transaction -> {
        Double limit = Optional.ofNullable(creditCardRepository.findByCardNumber(transaction.getCardNumber())
                        .orElseThrow(() -> new CreditCardNotFoundException("No Credit Card Found")))
                .map(CreditCard::getLimit)
                .get();
        if (transaction.getAmount() <= limit) {
            return true;
        } else {
            throw new IllegalArgumentException("Transaction amount exceeds the limit for this credit card.");
        }
    };

    Predicate<Transaction> isTransactionAmountLessThanTransactionLimit = transaction -> {
        Double transactionLimit = Optional.ofNullable(creditCardRepository.findByCardNumber(transaction.getCardNumber())
                        .orElseThrow(() -> new CreditCardNotFoundException("No Credit Card Found")))
                .map(CreditCard::getTransactionLimit)
                .get();

        if (transaction.getAmount() <= transactionLimit) {
            return true;
        } else {
            throw new IllegalArgumentException("Transaction amount exceeds the transaction amount per transaction for this credit card.");
        }
    };

    Predicate<Transaction> isValidTransaction = isTransactionAmountLessThanTransactionLimit.and(isTransactionAmountLessThanLimit);

    public boolean validateTransactionLimit(Transaction transaction) {
        if (!isValidTransaction.test(transaction)) {
            throw new IllegalArgumentException("Transaction cannot be processed");
        } else {
            return true;
        }
    }


}

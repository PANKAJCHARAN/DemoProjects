package com.banking.rtccts.service;

import com.banking.rtccts.config.KafkaConsumerConfig;
import com.banking.rtccts.data.CardAuthLog;
import com.banking.rtccts.data.CardTransactions;
import com.banking.rtccts.data.CreditCard;
import com.banking.rtccts.repository.CardAuthRepository;
import com.banking.rtccts.repository.CreditCardRepository;
import com.banking.rtccts.repository.TransactionRepository;
import com.rtccts.Transaction;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private CreditCardRepository creditCardRepository;
    @Autowired
    private CardAuthRepository cardAuthRepository;
    @Autowired
    private KafkaTemplate<String, Transaction> kafkaTemplate;

    @Transactional
    public void processRecord(Transaction transaction) {
        try{
            Optional.ofNullable(creditCardRepository.findById(transaction.getCardNumber())
                            .orElseThrow(() -> new RuntimeException("No Credit Card Found")))
                    .ifPresent(creditCard -> {
                        if (transaction.getAmount() < creditCard.getLimit()) {
                            logger.info("Card authorized for the payment of amount {}", transaction.getAmount());
                            creditCard.setLimit(transaction.getAmount() - creditCard.getLimit());
                            CreditCard creditCardResponse = creditCardRepository.save(creditCard);
                            transactionRepository.save(getTransaction(transaction, creditCardResponse));
                            cardAuthRepository.save(getCardAuth(transaction,true));

                        } else {
                            logger.info("Card not authorized for the payment of amount {}", transaction.getAmount());
                            cardAuthRepository.save(getCardAuth(transaction,false));
                        }
                    });
        }catch (Exception e){
            kafkaTemplate.send("dead-letter-queue", transaction);
            logger.error("Failed to save record");
            throw new RuntimeException("Failed to save records: " + e.getMessage(), e);
        }

    }


    private CardTransactions getTransaction(Transaction transaction, CreditCard creditCard) {
        CardTransactions cardTransactions = new CardTransactions();
        cardTransactions.setTransactionId(transaction.getTransactionId());
        cardTransactions.setTimestamp(transaction.getTimestamp());
        cardTransactions.setAmount(transaction.getAmount());
        cardTransactions.setMerchantId(transaction.getMerchantId());
        cardTransactions.setCreditCard(creditCard);
        return cardTransactions;
    }

    private CardAuthLog getCardAuth(Transaction transaction, boolean isAuthorised) {
        CardAuthLog cardAuthLog = new CardAuthLog();
        cardAuthLog.setAuthorized(isAuthorised);
        cardAuthLog.setTransactionAmount(transaction.getAmount());
        cardAuthLog.setTimestamp(transaction.getTimestamp());
        cardAuthLog.setCardNumber(transaction.getCardNumber());

        return cardAuthLog;
    }

}

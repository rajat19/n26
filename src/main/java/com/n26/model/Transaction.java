package com.n26.model;

import com.n26.model.request.TransactionRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.Instant;

@Data
@Slf4j
public class Transaction {
    private BigDecimal amount;
    private long timestamp;

    public static Transaction from(TransactionRequest transactionRequest) throws ParseException{
        Transaction transaction = new Transaction();
        log.info("Request: {}", transactionRequest);

        Instant instant = Instant.parse(transactionRequest.getTimestamp());
        transaction.amount = BigDecimal.valueOf(Double.parseDouble(transactionRequest.getAmount()));
        transaction.timestamp = instant.toEpochMilli();
        return transaction;
    }
}

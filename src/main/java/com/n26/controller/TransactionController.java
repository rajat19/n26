package com.n26.controller;

import com.n26.model.Transaction;
import com.n26.model.request.TransactionRequest;
import com.n26.service.TransactionService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @PostMapping("/transactions")
    @SneakyThrows
    public ResponseEntity<?> addTransaction(@RequestBody TransactionRequest transactionRequest) throws ParseException {
        Transaction transaction = Transaction.from(transactionRequest);
        transactionService.addTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @DeleteMapping("/transactions")
    public ResponseEntity<?> deleteAll() {
        transactionService.deleteAllTransactions();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}

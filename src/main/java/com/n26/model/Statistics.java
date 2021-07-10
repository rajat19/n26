package com.n26.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
public class Statistics {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal sum;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal avg;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal max;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal min;
    private long count;

    public Statistics() {
        reset();
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum.setScale(2, RoundingMode.HALF_UP);
    }

    public void setAvg(BigDecimal avg) {
        this.avg = avg.setScale(2, RoundingMode.HALF_UP);
    }

    public void setMax(BigDecimal max) {
        this.max = max.setScale(2, RoundingMode.HALF_UP);
    }

    public void setMin(BigDecimal min) {
        this.min = min.setScale(2, RoundingMode.HALF_UP);
    }

    public void setCount(long count) {
        this.count = count;
    }

    public void reset() {
        setSum(BigDecimal.valueOf(0.00));
        setMin(BigDecimal.valueOf(Long.MAX_VALUE));
        setMax(BigDecimal.valueOf(Long.MIN_VALUE));
        setAvg(BigDecimal.valueOf(0.00));
        setCount(0);
    }

    public void resetToZero() {
        reset();
        setMax(BigDecimal.valueOf(0.00));
        setMin(BigDecimal.valueOf(0.00));
    }

    public void createFromTransaction(Transaction transaction) {
        BigDecimal amount = transaction.getAmount();
        setAvg(amount);
        setMin(amount);
        setMax(amount);
        setSum(amount);
        setCount(1);
    }
}

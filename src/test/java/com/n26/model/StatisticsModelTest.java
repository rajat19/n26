package com.n26.model;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
public class StatisticsModelTest {

    @Test
    public void testAddStatistics() {
        Statistics statistics1 = new Statistics();
        statistics1.setSum(BigDecimal.ONE);
        statistics1.setCount(1);
        statistics1.setMax(BigDecimal.ONE);
        statistics1.setMin(BigDecimal.ONE);
        statistics1.setAvg(BigDecimal.ONE);

        Statistics statistics2 = new Statistics();
        statistics2.setSum(BigDecimal.ONE);
        statistics2.setCount(1);
        statistics2.setMax(BigDecimal.ONE);
        statistics2.setMin(BigDecimal.ONE);
        statistics2.setAvg(BigDecimal.ONE);

        Statistics result = new Statistics();
        result.addStatistics(statistics1);
        result.addStatistics(statistics2);
        System.out.println(result);
    }
}

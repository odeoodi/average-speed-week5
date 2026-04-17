package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class AvgSpd1Test {

    @Test
    @DisplayName("Should calculate average speed correctly")
    void testAvgSpd() {
        // Given
        double distance = 100.0;
        double time = 50.0;

        // When
        double result = AvgSpd1.avgSpd(distance, time);

        // Then
        assertEquals(2.0, result, 0.0001);
    }

    @ParameterizedTest
    @DisplayName("Should calculate multiple speed scenarios")
    @CsvSource({
            "100, 2, 50",
            "150, 3, 50",
            "75.5, 2.5, 30.2",
            "0, 5, 0"
    })
    void testAvgSpdWithVariousInputs(double distance, double time, double expected) {
        assertEquals(expected, AvgSpd1.avgSpd(distance, time), 0.0001);
    }

    @Test
    @DisplayName("Should throw exception when time is zero")
    void testAvgSpdWithZeroTime() {
        // Change ArithmeticException to IllegalArgumentException
        assertThrows(IllegalArgumentException.class,
                () -> AvgSpd1.avgSpd(100.0, 0.0));
    }

    @Test
    @DisplayName("Should calculate and log correctly")
    void testCalculateAndLogAvgSpeed() {
        // This tests the method that calls the logger
        double result = AvgSpd1.calculateAndLogAvgSpeed(100.0, 50.0);
        assertEquals(2.0, result, 0.0001);
    }
}
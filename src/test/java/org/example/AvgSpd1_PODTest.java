package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AvgSpd1_PODTest {

    private AvgSpd1_POD.CalculateHandler calculateHandler;
    private AvgSpd1_POD.RootHandler rootHandler;
    private HttpExchange mockExchange;
    private Headers mockHeaders;

    @BeforeEach
    void setUp() {
        calculateHandler = new AvgSpd1_POD.CalculateHandler();
        rootHandler = new AvgSpd1_POD.RootHandler();
        mockExchange = mock(HttpExchange.class);
        mockHeaders = mock(Headers.class);

        when(mockExchange.getResponseHeaders()).thenReturn(mockHeaders);
    }

    // ========== avgSpd() Business Logic Tests ==========

    @Test
    @DisplayName("avgSpd - valid distance and time returns correct speed")
    void avgSpd_validInputs_returnsCorrectSpeed() {
        assertEquals(50.0, AvgSpd1_POD.avgSpd(100.0, 2.0), 0.001);
        assertEquals(25.0, AvgSpd1_POD.avgSpd(100.0, 4.0), 0.001);
        assertEquals(0.0, AvgSpd1_POD.avgSpd(0.0, 5.0), 0.001);
    }

    @ParameterizedTest
    @CsvSource({
            "100, 4, 25",
            "150, 3, 50",
            "200, 5, 40",
            "75.5, 2.5, 30.2"
    })
    @DisplayName("avgSpd - parameterized tests")
    void avgSpd_parameterizedInputs_returnsExpectedSpeed(double distance, double time, double expected) {
        assertEquals(expected, AvgSpd1_POD.avgSpd(distance, time), 0.001);
    }

    @Test
    @DisplayName("avgSpd - time is zero throws exception")
    void avgSpd_timeIsZero_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> AvgSpd1_POD.avgSpd(100.0, 0.0));
    }

    @Test
    @DisplayName("avgSpd - negative time throws exception")
    void avgSpd_negativeTime_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> AvgSpd1_POD.avgSpd(100.0, -5.0));
    }

    // ========== CalculateHandler Tests ==========

    @Test
    @DisplayName("CalculateHandler - valid parameters returns result")
    void calculateHandler_validParameters_returnsResult() throws IOException {
        String query = "distance=100&time=2";
        URI mockUri = URI.create("http://localhost:8081/calculate?" + query);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(mockExchange.getRequestURI()).thenReturn(mockUri);
        when(mockExchange.getResponseBody()).thenReturn(outputStream);

        calculateHandler.handle(mockExchange);

        String response = outputStream.toString(StandardCharsets.UTF_8);

        // Check for content using regex patterns that handle both . and , as decimal separators
        assertTrue(response.contains("Result"), "Response should contain 'Result'");
        assertTrue(response.matches("(?s).*Distance: 100[.,]00 km.*"), "Response should contain distance 100,00 or 100.00");
        assertTrue(response.matches("(?s).*Time: 2[.,]00 hours.*"), "Response should contain time 2,00 or 2.00");
        assertTrue(response.matches("(?s).*Average Speed: 50[.,]00 km/h.*"), "Response should contain speed 50,00 or 50.00");
    }

    @Test
    @DisplayName("CalculateHandler - decimal parameters")
    void calculateHandler_decimalParameters_returnsResult() throws IOException {
        String query = "distance=125.5&time=3.5";
        URI mockUri = URI.create("http://localhost:8081/calculate?" + query);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(mockExchange.getRequestURI()).thenReturn(mockUri);
        when(mockExchange.getResponseBody()).thenReturn(outputStream);

        calculateHandler.handle(mockExchange);

        String response = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(response.matches("(?s).*Distance: 125[.,]50 km.*"), "Response should contain distance 125,50 or 125.50");
        assertTrue(response.matches("(?s).*Time: 3[.,]50 hours.*"), "Response should contain time 3,50 or 3.50");
        assertTrue(response.matches("(?s).*Average Speed: 35[.,]86 km/h.*"), "Response should contain speed 35,86 or 35.86");
    }

    @Test
    @DisplayName("CalculateHandler - missing distance returns error")
    void calculateHandler_missingDistance_returnsError() throws IOException {
        String query = "time=2";
        URI mockUri = URI.create("http://localhost:8081/calculate?" + query);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(mockExchange.getRequestURI()).thenReturn(mockUri);
        when(mockExchange.getResponseBody()).thenReturn(outputStream);

        calculateHandler.handle(mockExchange);

        String response = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(response.contains("Error"));
        assertTrue(response.contains("Invalid input"));
    }

    @Test
    @DisplayName("CalculateHandler - missing time returns error")
    void calculateHandler_missingTime_returnsError() throws IOException {
        String query = "distance=100";
        URI mockUri = URI.create("http://localhost:8081/calculate?" + query);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(mockExchange.getRequestURI()).thenReturn(mockUri);
        when(mockExchange.getResponseBody()).thenReturn(outputStream);

        calculateHandler.handle(mockExchange);

        String response = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(response.contains("Error"));
    }

    @Test
    @DisplayName("CalculateHandler - empty query returns error")
    void calculateHandler_emptyQuery_returnsError() throws IOException {
        URI mockUri = URI.create("http://localhost:8081/calculate");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(mockExchange.getRequestURI()).thenReturn(mockUri);
        when(mockExchange.getResponseBody()).thenReturn(outputStream);

        calculateHandler.handle(mockExchange);

        String response = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(response.contains("Error"));
    }

    @Test
    @DisplayName("CalculateHandler - invalid distance returns error")
    void calculateHandler_invalidDistance_returnsError() throws IOException {
        String query = "distance=abc&time=2";
        URI mockUri = URI.create("http://localhost:8081/calculate?" + query);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(mockExchange.getRequestURI()).thenReturn(mockUri);
        when(mockExchange.getResponseBody()).thenReturn(outputStream);

        calculateHandler.handle(mockExchange);

        String response = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(response.contains("Error"));
    }

    @Test
    @DisplayName("CalculateHandler - invalid time returns error")
    void calculateHandler_invalidTime_returnsError() throws IOException {
        String query = "distance=100&time=xyz";
        URI mockUri = URI.create("http://localhost:8081/calculate?" + query);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(mockExchange.getRequestURI()).thenReturn(mockUri);
        when(mockExchange.getResponseBody()).thenReturn(outputStream);

        calculateHandler.handle(mockExchange);

        String response = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(response.contains("Error"));
    }

    @Test
    @DisplayName("CalculateHandler - time is zero returns error")
    void calculateHandler_timeIsZero_returnsError() throws IOException {
        String query = "distance=100&time=0";
        URI mockUri = URI.create("http://localhost:8081/calculate?" + query);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(mockExchange.getRequestURI()).thenReturn(mockUri);
        when(mockExchange.getResponseBody()).thenReturn(outputStream);

        calculateHandler.handle(mockExchange);

        String response = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(response.contains("Error"));
    }

    @Test
    @DisplayName("CalculateHandler - negative time returns error")
    void calculateHandler_negativeTime_returnsError() throws IOException {
        String query = "distance=100&time=-5";
        URI mockUri = URI.create("http://localhost:8081/calculate?" + query);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(mockExchange.getRequestURI()).thenReturn(mockUri);
        when(mockExchange.getResponseBody()).thenReturn(outputStream);

        calculateHandler.handle(mockExchange);

        String response = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(response.contains("Error"));
    }

    @Test
    @DisplayName("CalculateHandler - URL encoded parameters")
    void calculateHandler_urlEncodedParameters_returnsResult() throws IOException {
        String distance = URLEncoder.encode("100", StandardCharsets.UTF_8);
        String time = URLEncoder.encode("2", StandardCharsets.UTF_8);
        String query = "distance=" + distance + "&time=" + time;
        URI mockUri = URI.create("http://localhost:8081/calculate?" + query);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(mockExchange.getRequestURI()).thenReturn(mockUri);
        when(mockExchange.getResponseBody()).thenReturn(outputStream);

        calculateHandler.handle(mockExchange);

        String response = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(response.matches("(?s).*Average Speed: 50[.,]00 km/h.*"), "Response should contain speed 50,00 or 50.00");
    }

    @Test
    @DisplayName("CalculateHandler - extra parameters")
    void calculateHandler_extraParameters_returnsResult() throws IOException {
        String query = "distance=100&extra=test&time=2";
        URI mockUri = URI.create("http://localhost:8081/calculate?" + query);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(mockExchange.getRequestURI()).thenReturn(mockUri);
        when(mockExchange.getResponseBody()).thenReturn(outputStream);

        calculateHandler.handle(mockExchange);

        String response = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(response.matches("(?s).*Average Speed: 50[.,]00 km/h.*"), "Response should contain speed 50,00 or 50.00");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "distance=100&time=2",
            "time=2&distance=100"
    })
    @DisplayName("CalculateHandler - different parameter orders")
    void calculateHandler_differentParameterOrders(String query) throws IOException {
        URI mockUri = URI.create("http://localhost:8081/calculate?" + query);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(mockExchange.getRequestURI()).thenReturn(mockUri);
        when(mockExchange.getResponseBody()).thenReturn(outputStream);

        calculateHandler.handle(mockExchange);

        String response = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(response.matches("(?s).*Average Speed: 50[.,]00 km/h.*"), "Response should contain speed 50,00 or 50.00");
    }

    // ========== RootHandler Tests ==========

    @Test
    @DisplayName("RootHandler - returns HTML form")
    void rootHandler_returnsHtmlForm() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(mockExchange.getResponseBody()).thenReturn(outputStream);

        rootHandler.handle(mockExchange);

        String response = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(response.contains("Average Speed Calculator"));
        assertTrue(response.contains("Distance"));
        assertTrue(response.contains("Time"));
        assertTrue(response.contains("Calculate"));
    }

    @Test
    @DisplayName("RootHandler - sets content type")
    void rootHandler_setsContentType() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(mockExchange.getResponseBody()).thenReturn(outputStream);

        rootHandler.handle(mockExchange);

        verify(mockHeaders).set(eq("Content-Type"), eq("text/html; charset=UTF-8"));
    }

    // ========== Response Content Tests ==========

    @Test
    @DisplayName("CalculateHandler - success response has calculate again link")
    void calculateHandler_successResponseHasCalculateAgainLink() throws IOException {
        String query = "distance=100&time=2";
        URI mockUri = URI.create("http://localhost:8081/calculate?" + query);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(mockExchange.getRequestURI()).thenReturn(mockUri);
        when(mockExchange.getResponseBody()).thenReturn(outputStream);

        calculateHandler.handle(mockExchange);

        String response = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(response.contains("Calculate Again"));
    }

    @Test
    @DisplayName("CalculateHandler - error response has try again link")
    void calculateHandler_errorResponseHasTryAgainLink() throws IOException {
        String query = "distance=invalid";
        URI mockUri = URI.create("http://localhost:8081/calculate?" + query);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(mockExchange.getRequestURI()).thenReturn(mockUri);
        when(mockExchange.getResponseBody()).thenReturn(outputStream);

        calculateHandler.handle(mockExchange);

        String response = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(response.contains("Try Again"));
    }
}
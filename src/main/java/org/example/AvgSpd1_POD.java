package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class AvgSpd1_POD {

    private static final Logger logger =
            Logger.getLogger(AvgSpd1_POD.class.getName());

    public static void main(String[] args) throws IOException {
        int port = 8081;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new RootHandler());
        server.createContext("/calculate", new CalculateHandler());

        server.setExecutor(null);
        server.start();

        logger.info("Server started on port " + 8081);
    }

    // ---------- Root page ----------

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Average Speed Calculator</title>
                </head>
                <body>
                    <h2>Average Speed Calculator</h2>
                    <form action="/calculate" method="get">
                        Distance (km):
                        <input type="number" step="any" name="distance" required><br>
                        Time (hours):
                        <input type="number" step="any" name="time" required><br>
                        <button type="submit">Calculate</button>
                    </form>
                </body>
                </html>
            """;

            exchange.getResponseHeaders()
                    .set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, html.getBytes().length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(html.getBytes());
            }
        }
    }

    // ---------- Calculation ----------

    static class CalculateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response;

            try {
                String query = exchange.getRequestURI().getQuery();

                // Handle null query
                if (query == null || query.isEmpty()) {
                    throw new IllegalArgumentException("Missing parameters");
                }

                String[] params = query.split("&");

                double distance = 0;
                double time = 0;
                boolean hasDistance = false;
                boolean hasTime = false;

                for (String param : params) {
                    String[] kv = param.split("=", 2); // Limit to 2 parts
                    if (kv.length != 2) continue;

                    // URL decode the parameter values
                    String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);

                    if ("distance".equals(key)) {
                        distance = Double.parseDouble(value);
                        hasDistance = true;
                    } else if ("time".equals(key)) {
                        time = Double.parseDouble(value);
                        hasTime = true;
                    }
                }

                if (!hasDistance || !hasTime) {
                    throw new IllegalArgumentException("Missing required parameters");
                }

                double result = avgSpd(distance, time);

                response = String.format("""
                    <html>
                    <body>
                        <h2>Result</h2>
                        <p>Distance: %.2f km</p>
                        <p>Time: %.2f hours</p>
                        <p><strong>Average Speed: %.2f km/h</strong></p>
                        <a href="/">Calculate Again</a>
                    </body>
                    </html>
                """, distance, time, result);

            } catch (Exception e) {
                logger.warning("Calculation error: " + e.getMessage());
                response = """
                    <html>
                    <body>
                        <h2>Error</h2>
                        <p>Invalid input. Please provide valid numbers.</p>
                        <a href="/">Try Again</a>
                    </body>
                    </html>
                """;
            }

            exchange.getResponseHeaders()
                    .set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes().length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    // ---------- Business logic ----------

    public static double avgSpd(double distance, double time) {
        if (time <= 0) {  // Changed from == 0 to <= 0 to catch negative values too
            throw new IllegalArgumentException("Time must be greater than zero");
        }
        return distance / time;
    }
}
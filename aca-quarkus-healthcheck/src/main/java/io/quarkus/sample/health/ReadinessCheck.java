package io.quarkus.sample.health;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import jakarta.enterprise.context.ApplicationScoped;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import io.quarkus.sample.TodoResource;

@Readiness
@ApplicationScoped
public class ReadinessCheck implements HealthCheck {
    private static final String READINESS_CHECK = TodoResource.class.getSimpleName() + " API reachable";
    private static final String API_URL = "http://localhost:8080/api";

    private boolean isHealthy() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public HealthCheckResponse call() {
        return isHealthy() ? HealthCheckResponse.up(READINESS_CHECK) : HealthCheckResponse.down(READINESS_CHECK);
    }
}

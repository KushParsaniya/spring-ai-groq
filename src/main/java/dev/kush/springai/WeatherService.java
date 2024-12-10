package dev.kush.springai;

import java.util.function.Function;

public class WeatherService implements Function<WeatherService.Request, WeatherService.Response> {

    enum Unit {C, F}

    public record Request(String city) {
    }

    public record Response(float temperature, Unit unit) {
    }

    @Override
    public Response apply(Request request) {
        return switch (request.city.toLowerCase()) {
            case "london" -> new Response(20, Unit.C);
            case "new york" -> new Response(68, Unit.F);
            default -> new Response(25, Unit.C);
        };
    }
}


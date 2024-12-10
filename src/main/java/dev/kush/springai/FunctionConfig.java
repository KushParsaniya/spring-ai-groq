package dev.kush.springai;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration
public class FunctionConfig {

    @Bean
    @Description("Get temperature data by city name")
    public Function<WeatherService.Request, WeatherService.Response> weatherByCity() {
        return new WeatherService();
    }
}

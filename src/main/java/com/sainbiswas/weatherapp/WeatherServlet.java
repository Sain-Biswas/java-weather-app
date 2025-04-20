package com.sainbiswas.weatherapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@WebServlet(name = "WeatherServlet", value = "/weather")
public class WeatherServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(WeatherServlet.class.getName());

    // API key management - prefer environment variable, fallback to hardcoded for development
    private static final String API_KEY = System.getenv("OPENWEATHERMAP_API_KEY");
    private static final String FALLBACK_API_KEY = "OPENWEATHERMAP_API_KEY"; // Replace with your API key

    // Simple caching mechanism
    private Map<String, CachedWeatherData> weatherCache = new HashMap<>();
    private static final long CACHE_EXPIRY_MINUTES = 30;

    private static class CachedWeatherData {
        private final String jsonResponse;
        private final LocalDateTime timestamp;

        public CachedWeatherData(String jsonResponse) {
            this.jsonResponse = jsonResponse;
            this.timestamp = LocalDateTime.now();
        }

        public boolean isExpired() {
            return java.time.temporal.ChronoUnit.MINUTES.between(timestamp, LocalDateTime.now()) > CACHE_EXPIRY_MINUTES;
        }

        public String getJsonResponse() {
            return jsonResponse;
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("WeatherServlet initialized");

        // Log warning if using fallback key
        if (API_KEY == null) {
            logger.warning("Using fallback API key. Set OPENWEATHER_API_KEY environment variable for production.");
        }
    }

    @Override
    public void destroy() {
        weatherCache.clear();
        logger.info("WeatherServlet destroyed");
        super.destroy();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // Forward to the JSP page on initial load
        RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // Get city name from the form
        String city = request.getParameter("city");

        if (city != null) {
            // Trim whitespace
            city = city.trim();

            // Basic validation - only allow letters, spaces, and some punctuation
            if (!city.matches("^[a-zA-Z\\s\\.,'-]+$")) {
                request.setAttribute("error", "Invalid city name. Please use only letters and spaces.");
            } else if (city.isEmpty()) {
                request.setAttribute("error", "Please enter a city name");
            } else {
                try {
                    // Determine which API key to use
                    String apiKey = (API_KEY != null) ? API_KEY : FALLBACK_API_KEY;

                    // Construct API URL
                    String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" +
                            city + "&units=metric&APPID=" + apiKey;

                    logger.info("Processing weather request for city: " + city);

                    // Check cache first
                    String response_API_str;
                    CachedWeatherData cachedData = weatherCache.get(city.toLowerCase());

                    if (cachedData != null && !cachedData.isExpired()) {
                        // Use cached data
                        response_API_str = cachedData.getJsonResponse();
                        logger.info("Using cached data for city: " + city);
                    } else {
                        // Create connection to API
                        URL url = new URL(apiUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");

                        // Read the response
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(connection.getInputStream()));
                        StringBuilder response_API = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            response_API.append(line);
                        }
                        reader.close();

                        // Store in cache
                        response_API_str = response_API.toString();
                        weatherCache.put(city.toLowerCase(), new CachedWeatherData(response_API_str));
                    }

                    // Parse JSON response
                    JsonObject jsonObject = JsonParser.parseString(response_API_str).getAsJsonObject();

                    // Extract weather data
                    double temperature = jsonObject.getAsJsonObject("main").get("temp").getAsDouble();
                    int humidity = jsonObject.getAsJsonObject("main").get("humidity").getAsInt();
                    double windSpeed = jsonObject.getAsJsonObject("wind").get("speed").getAsDouble();
                    int visibility = jsonObject.get("visibility").getAsInt() / 1000; // Convert to km
                    String weatherCondition = jsonObject.getAsJsonArray("weather")
                            .get(0).getAsJsonObject()
                            .get("description").getAsString();
                    String weatherIcon = jsonObject.getAsJsonArray("weather")
                            .get(0).getAsJsonObject()
                            .get("icon").getAsString();
                    int cloudCover = jsonObject.getAsJsonObject("clouds").get("all").getAsInt();

                    // Get current date and time
                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
                    String date = now.format(dateFormatter);
                    String time = now.format(timeFormatter);

                    // Set attributes to be used in JSP
                    request.setAttribute("city", city);
                    request.setAttribute("temperature", Math.round(temperature));
                    request.setAttribute("humidity", humidity);
                    request.setAttribute("windSpeed", windSpeed);
                    request.setAttribute("visibility", visibility);
                    request.setAttribute("weatherCondition", weatherCondition);
                    request.setAttribute("weatherIcon", weatherIcon);
                    request.setAttribute("cloudCover", cloudCover);
                    request.setAttribute("date", date);
                    request.setAttribute("time", time);
                    request.setAttribute("weatherData", true);

                } catch (java.net.UnknownHostException e) {
                    request.setAttribute("error", "Network connection issue. Please check your internet connection.");
                    logger.log(Level.SEVERE, "Network connection error", e);
                } catch (java.net.ConnectException e) {
                    request.setAttribute("error", "Could not connect to weather service. Please try again later.");
                    logger.log(Level.SEVERE, "Connection error to weather service", e);
                } catch (Exception e) {
                    request.setAttribute("error", "City not found or error in fetching data: " + e.getMessage());
                    logger.log(Level.SEVERE, "Error fetching weather data", e);
                }
            }
        } else {
            request.setAttribute("error", "Please enter a city name");
        }

        // Forward to the JSP page
        RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsp");
        dispatcher.forward(request, response);
    }
}

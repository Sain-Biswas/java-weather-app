package com.sainbiswas.weatherapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serial;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "WeatherServlet", value = "/weather")
public class WeatherServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String API_KEY = "YOUR_OPENWEATHERMAP_API_KEY";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String city = request.getParameter("city");

        if (city != null && !city.trim().isEmpty()) {
            try {

                String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" +
                        city + "&APPID=" + API_KEY;


                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");


                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                StringBuilder response_API = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response_API.append(line);
                }
                reader.close();


                JsonObject jsonObject = JsonParser.parseString(response_API.toString()).getAsJsonObject();


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


                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
                String date = now.format(dateFormatter);
                String time = now.format(timeFormatter);


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

            } catch (Exception e) {
                request.setAttribute("error", "City not found or error in fetching data");
                e.printStackTrace();
            }
        } else {
            request.setAttribute("error", "Please enter a city name");
        }


        RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsp");
        dispatcher.forward(request, response);
    }
}

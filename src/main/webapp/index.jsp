<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Weather App</title>
  <!-- Tailwind CSS CDN -->
  <link href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css" rel="stylesheet">
  <!-- Font Awesome for icons -->
  <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" rel="stylesheet">
  <style>
    .weather-card {
      transition: all 0.3s ease;
    }
    .weather-card:hover {
      transform: translateY(-5px);
    }
    .fade-in {
      animation: fadeIn 0.5s ease-in;
    }
    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(10px); }
      to { opacity: 1; transform: translateY(0); }
    }
    .pulse {
      animation: pulse 2s infinite;
    }
    @keyframes pulse {
      0% { transform: scale(1); }
      50% { transform: scale(1.05); }
      100% { transform: scale(1); }
    }
  </style>
</head>
<body class="bg-gradient-to-br from-blue-400 to-purple-600 min-h-screen flex justify-center items-center p-4 font-sans">
<div class="max-w-md w-full bg-white rounded-xl shadow-2xl overflow-hidden weather-card">
  <!-- Header with gradient -->
  <div class="bg-gradient-to-r from-blue-500 to-indigo-600 p-6 text-white">
    <h1 class="text-3xl font-bold text-center">Weather Forecast</h1>
    <p class="text-center text-blue-100 mt-1">Real-time weather information</p>

    <!-- Search Form with animation -->
    <form action="weather" method="post" class="mt-4">
      <div class="flex items-center bg-blue-400 bg-opacity-30 rounded-lg p-1 focus-within:ring-2 focus-within:ring-white transition duration-300">
        <input
                type="text"
                name="city"
                placeholder="Enter city name"
                class="flex-1 py-2 px-4 bg-transparent text-white placeholder-blue-100 focus:outline-none"
                required
        >
        <button
                type="submit"
                class="bg-white text-blue-600 px-4 py-2 rounded-lg font-semibold hover:bg-blue-50 transition duration-300 transform hover:scale-105"
        >
          <i class="fas fa-search mr-1"></i> Search
        </button>
      </div>
    </form>
  </div>

  <!-- Error Message with animation -->
  <% if (request.getAttribute("error") != null) { %>
  <div class="p-6 text-center fade-in">
    <div class="bg-red-100 text-red-700 p-4 rounded-lg shadow">
      <i class="fas fa-exclamation-circle mr-2"></i>
      <%= request.getAttribute("error") %>
    </div>
  </div>
  <% } %>

  <!-- Weather Data with animations -->
  <% if (request.getAttribute("weatherData") != null) { %>
  <div class="p-6 fade-in">
    <!-- City and Date -->
    <div class="text-center mb-6">
      <h2 class="text-3xl font-bold text-gray-800"><%= request.getAttribute("city") %></h2>
      <p class="text-gray-500"><%= request.getAttribute("date") %> | <%= request.getAttribute("time") %></p>
    </div>

    <!-- Weather Icon and Temperature -->
    <div class="flex justify-center items-center mb-6">
      <img
              src="https://openweathermap.org/img/wn/<%= request.getAttribute("weatherIcon") %>@4x.png"
              alt="Weather Icon"
              class="w-32 h-32 pulse"
      >
      <div class="text-center">
        <div class="text-6xl font-bold text-gray-800"><%= request.getAttribute("temperature") %>°C</div>
        <p class="text-xl text-gray-600 capitalize"><%= request.getAttribute("weatherCondition") %></p>
      </div>
    </div>

    <!-- Weather Details with hover effects -->
    <div class="grid grid-cols-2 gap-4">
      <div class="bg-blue-50 p-4 rounded-lg flex items-center transform hover:scale-105 transition duration-300 hover:bg-blue-100 hover:shadow-md">
        <i class="fas fa-wind text-blue-500 text-xl mr-3"></i>
        <div>
          <p class="text-gray-500 text-sm">Wind Speed</p>
          <p class="text-gray-800 font-semibold"><%= request.getAttribute("windSpeed") %> m/s</p>
        </div>
      </div>

      <div class="bg-blue-50 p-4 rounded-lg flex items-center transform hover:scale-105 transition duration-300 hover:bg-blue-100 hover:shadow-md">
        <i class="fas fa-tint text-blue-500 text-xl mr-3"></i>
        <div>
          <p class="text-gray-500 text-sm">Humidity</p>
          <p class="text-gray-800 font-semibold"><%= request.getAttribute("humidity") %>%</p>
        </div>
      </div>

      <div class="bg-blue-50 p-4 rounded-lg flex items-center transform hover:scale-105 transition duration-300 hover:bg-blue-100 hover:shadow-md">
        <i class="fas fa-eye text-blue-500 text-xl mr-3"></i>
        <div>
          <p class="text-gray-500 text-sm">Visibility</p>
          <p class="text-gray-800 font-semibold"><%= request.getAttribute("visibility") %> km</p>
        </div>
      </div>

      <div class="bg-blue-50 p-4 rounded-lg flex items-center transform hover:scale-105 transition duration-300 hover:bg-blue-100 hover:shadow-md">
        <i class="fas fa-cloud text-blue-500 text-xl mr-3"></i>
        <div>
          <p class="text-gray-500 text-sm">Cloud Cover</p>
          <p class="text-gray-800 font-semibold"><%= request.getAttribute("cloudCover") %>%</p>
        </div>
      </div>
    </div>
  </div>
  <% } else if (request.getAttribute("error") == null) { %>
  <!-- Initial state with animation -->
  <div class="p-8 text-center">
    <img
            src="https://cdn-icons-png.flaticon.com/512/1779/1779940.png"
            alt="Weather Icon"
            class="w-32 h-32 mx-auto mb-4 opacity-50 pulse"
    >
    <p class="text-gray-500">Enter a city name to get the current weather information</p>
  </div>
  <% } %>

  <!-- Footer -->
  <div class="bg-gray-50 p-4 text-center text-gray-500 text-sm border-t">
    <p>Weather data provided by OpenWeatherMap</p>
    <p class="mt-1">© <%= java.time.Year.now().getValue() %> Weather App</p>
  </div>
</div>

<script>
  // JavaScript for enhanced functionality
  document.addEventListener('DOMContentLoaded', function() {
    // Focus on the search input when page loads
    const searchInput = document.querySelector('input[name="city"]');
    if (searchInput) {
      searchInput.focus();
    }

    // Add event listeners for form submission
    const form = document.querySelector('form');
    if (form) {
      form.addEventListener('submit', function() {
        const submitButton = this.querySelector('button[type="submit"]');
        if (submitButton) {
          submitButton.innerHTML = '<i class="fas fa-spinner fa-spin mr-1"></i> Loading...';
          submitButton.disabled = true;
        }
      });
    }
  });
</script>
</body>
</html>

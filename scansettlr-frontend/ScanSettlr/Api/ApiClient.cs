using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;

namespace ScanSettlr.Api
{
    public class ApiClient
    {
        private readonly HttpClient _httpClient;

        public ApiClient()
        {
            _httpClient = new HttpClient
            {
                BaseAddress = new Uri("http://192.168.0.104:8080/"),
                Timeout = TimeSpan.FromSeconds(10)
            };
        }

        public async Task<ApiResponse<T>> ExecuteAsync<T>(HttpMethod method, string endpoint, 
            object? body = null, bool authorize = true)
        {
            try
            {
                var request = new HttpRequestMessage(method, endpoint);

                if (authorize)
                {
                    var token = Preferences.Get("auth_token", null);
                    if (!string.IsNullOrEmpty(token))
                    {
                        request.Headers.Authorization =
                            new AuthenticationHeaderValue("Bearer", token);
                    }
                }

                if (body != null)
                {
                    var json = JsonSerializer.Serialize(body);
                    request.Content = new StringContent(json, Encoding.UTF8, "application/json");
                }

                var response = await _httpClient.SendAsync(request);

                var content = await response.Content.ReadAsStringAsync();

                if (!response.IsSuccessStatusCode)
                {
                    return new ApiResponse<T>
                    {
                        ErrorMessage = content,
                        StatusCode = (int)response.StatusCode
                    };
                }

                T? data = default;

                if (!string.IsNullOrWhiteSpace(content))
                {
                    data = JsonSerializer.Deserialize<T>(
                        content,
                        new JsonSerializerOptions { PropertyNameCaseInsensitive = true });
                }

                return new ApiResponse<T>
                {
                    Data = data,
                    StatusCode = (int)response.StatusCode
                };
            }
            catch (TaskCanceledException)
            {
                return new ApiResponse<T>
                {
                    ErrorMessage = "Request timed out"
                };
            }
            catch (Exception ex)
            {
                return new ApiResponse<T>
                {
                    ErrorMessage = ex.Message
                };
            }
        }

        public async Task<ApiResponse> ExecuteAsync(HttpMethod method, string endpoint,
                object? body = null, bool authorize = true)
        {
            var result = await ExecuteAsync<object>(method, endpoint, body, authorize);

            return new ApiResponse
            {
                ErrorMessage = result.ErrorMessage,
                StatusCode = result.StatusCode
            };
        }
    }
}

using ScanSettlr.Api;
using System.Net.Http.Headers;

namespace ScanSettlr
{
    public partial class App : Application
    {
        private readonly ApiClient ApiClient;
        public IServiceProvider Services;

        public App(ApiClient apiClient, IServiceProvider services)
        {
            InitializeComponent();

            ApiClient = apiClient;
            Services = services;

            MainPage = new NavigationPage(
                services.GetRequiredService<LoadingPage>()
            );

            _ = InitializeAppAsync();
        }

        private async Task InitializeAppAsync()
        {
            var token = Preferences.Get("auth_token", null);

            if (!string.IsNullOrWhiteSpace(token))
            {
                var isValid = await ValidateTokenAsync();

                if (isValid)
                {
                    SetMainPage<MainPage>();
                    return;
                }

                Preferences.Remove("auth_token");
            }

            SetMainPage<WelcomePage>();
        }

        private async Task<bool> ValidateTokenAsync()
        {
            var result = await ApiClient.ExecuteAsync<object>(
                HttpMethod.Get,
                "auth/validate"
            );

            return result.ErrorMessage == null;
        }

        private void SetMainPage<T>() where T : Page
        {
            MainThread.BeginInvokeOnMainThread(() =>
            {
                MainPage = new NavigationPage(
                    Services.GetRequiredService<T>()
                );
            });
        }

        private async void OnBackClicked(object sender, EventArgs e)
        {
            if (MainPage is NavigationPage nav && nav.Navigation.NavigationStack.Count > 1)
            {
                await nav.PopAsync();
            }
        }

    }
}

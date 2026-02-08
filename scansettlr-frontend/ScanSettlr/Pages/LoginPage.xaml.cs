using ScanSettlr.Api;
using ScanSettlr.Api.Schema;

namespace ScanSettlr
{
    public partial class LoginPage : ContentPage
    {
        private readonly ApiClient ApiClient;

        public LoginPage(ApiClient apiClient)
        {
            ApiClient = apiClient;
            NavigationPage.SetHasNavigationBar(this, false);
            InitializeComponent();
        }


        private async void OnLoginClicked(object sender, EventArgs e)
        {
            var loginRequest = new LoginRequest
            {
                username = emailEntry.Text,
                password = passwordEntry.Text
            };

            var response = await ApiClient.ExecuteAsync<LoginResponse>(
                HttpMethod.Post,
                "auth/login",
                loginRequest,
                authorize: false
            );

            if (response.ErrorMessage != null || response.Data == null)
            {
                await DisplayAlert("Login Failed", response.ErrorMessage ?? "Unknown error", "OK");
                return;
            }

            Preferences.Set("auth_token", response.Data.token);
            Preferences.Set("username", response.Data.username);
            Preferences.Set("user_id", response.Data.userId);

            Application.Current.MainPage = new NavigationPage(new MainPage(ApiClient));
        }

        private async void OnTapToSignUp(object sender, EventArgs e)
        {
            await Navigation.PushAsync(new SignUpPage(ApiClient));
        }
    }

}

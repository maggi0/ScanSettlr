using ScanSettlr.Api.Schema;
using ScanSettlr.Api;

namespace ScanSettlr
{
    public partial class SignUpPage : ContentPage
    {
        private readonly ApiClient ApiClient;

        public SignUpPage(ApiClient apiClient)
        {
            this.ApiClient = apiClient;
            NavigationPage.SetHasNavigationBar(this, false);
            InitializeComponent();
        }

        private async void OnSignUpClicked(object sender, EventArgs e)
        {
            if (passwordEntry.Text != confirmEntry.Text)
            {
                await DisplayAlert("Error", "Passwords do not match", "OK");
                return;
            }

            var signUpRequest = new SignUpRequest
            {
                username = nameEntry.Text,
                email = emailEntry.Text,
                password = passwordEntry.Text
            };

            var response = await ApiClient.ExecuteAsync<LoginResponse>(
                HttpMethod.Post,
                "auth/register",
                signUpRequest,
                authorize: false
            );

            if (response.ErrorMessage == null)
            {
                await DisplayAlert("Success", "Account created", "OK");
                await Navigation.PopAsync();
            }
            else
            {
                await DisplayAlert("Signup Failed", response.ErrorMessage, "OK");
            }
        }

        private async void OnTapToLogin(object sender, EventArgs e)
        {
            await Navigation.PushAsync(new LoginPage(ApiClient));
        }

    }

}

using ScanSettlr.Api;

namespace ScanSettlr
{
    public partial class WelcomePage : ContentPage
    {
        private ApiClient ApiClient;

        public WelcomePage(ApiClient apiClient)
        {
            NavigationPage.SetHasNavigationBar(this, false);
            InitializeComponent();

            ApiClient = apiClient;
        }

        private async void OnLoginClicked(object sender, EventArgs e)
        {
            await Navigation.PushAsync(new LoginPage(ApiClient));
        }

        private async void OnSignUpClicked(object sender, EventArgs e)
        {
            await Navigation.PushAsync(new SignUpPage(ApiClient));
        }
    }

}

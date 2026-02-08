using Microsoft.Maui.Controls;

namespace ScanSettlr.Controls
{
    public partial class SettingsOverlay : ContentView
    {
        public TopBar TopBar { get; set; }

        public string SelectedCurrency => CurrencyPicker.SelectedItem?.ToString();

        public SettingsOverlay()
        {
            InitializeComponent();

            CurrencyPicker.SelectedItem = "PLN";

            CurrencyPicker.SelectedIndexChanged += (s, e) =>
            {
                UpdateTopBarCurrency();
            };
        }

        public async void Show()
        {
            IsVisible = true;
            DimBackground.Opacity = 0;

            await DimBackground.FadeTo(0.5, 250);
        }

        public async void Hide()
        {
            await DimBackground.FadeTo(0, 200);
            IsVisible = false;
        }

        private void CloseClicked(object sender, EventArgs e)
        {
            Hide();
        }

        private async void SignOutClicked(object sender, EventArgs e)
        {
            Hide();

            SecureStorage.Remove("jwt");
            SecureStorage.Remove("refresh_token");
            SecureStorage.Remove("username");

            await MainThread.InvokeOnMainThreadAsync(() =>
            {
                var app = (App)Application.Current;
                var welcomePage = app.Services.GetRequiredService<WelcomePage>();
                Application.Current.MainPage = new NavigationPage(welcomePage);

            });
        }

        private void UpdateTopBarCurrency()
        {
            if (TopBar != null)
            {
                TopBar.Currency = SelectedCurrency;
            }
        }
    }
}

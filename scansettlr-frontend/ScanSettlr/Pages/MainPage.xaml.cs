using ScanSettlr.Api;
using ScanSettlr.Api.Model;
using ScanSettlr.Api.Schema;
using ScanSettlr.Controls;

namespace ScanSettlr
{
    public partial class MainPage : ContentPage
    {
        private ApiClient ApiClient;

        public MainPage(ApiClient apiClient)
        {
            ApiClient = apiClient;
            
            NavigationPage.SetHasNavigationBar(this, false);
            
            InitializeComponent();

            TopBar.Overlay = SettingsOverlayPopup;
            SettingsOverlayPopup.TopBar = TopBar;

            _ = LoadExpenseGroups();
        }

        private async Task LoadExpenseGroups()
        {
            var username = GetUsername();
            if (username == "")
            {
                await DisplayAlert("Error", "Failed to get username. Please log in.", "OK");
            }

            var response = await ApiClient.ExecuteAsync<List<ExpenseGroup>>(
                HttpMethod.Get,
                $"expenseGroup/user/{username}"
            );

            if (response.ErrorMessage == null && response.Data != null)
            {
                ExpenseGroupsList.ItemsSource = response.Data;
            }
            else
            {
                await DisplayAlert("Error", "Failed to load expense groups: " + response.ErrorMessage, "OK");
            }
        }

        private async void OnExpenseGroupClicked(object sender, EventArgs e)
        {
            if (sender is Button button && button.BindingContext is ExpenseGroup group)
            {
                await Navigation.PushAsync(new TransactionsPage(ApiClient, group.id, group.name));
            }
        }

        private async void OnAddNewGroupClicked(object sender, EventArgs e)
        {
            if (sender is Button)
            {
                await Navigation.PushAsync(new AddExpenseGroupPage(ApiClient));
            }
        }

        private string GetUsername()
        {
            var username = Preferences.Get("username", null);
            if (string.IsNullOrEmpty(username))
            {
                DisplayAlert("Error", "No username found. Please log in.", "OK");
                return "";
            }
            return username;
        }
    }
}

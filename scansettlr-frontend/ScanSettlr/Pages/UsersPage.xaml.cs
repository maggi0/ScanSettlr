using ScanSettlr.Api;
using ScanSettlr.Api.Model;
using ScanSettlr.Api.Schema;
using System.Text.Json;

namespace ScanSettlr
{
    public partial class UsersPage : ContentPage
    {
        private ApiClient ApiClient;
        public string GroupId { get; }
        public string GroupName { get; }

        public UsersPage(ApiClient apiClient, string groupId, string groupName)
        {
            NavigationPage.SetHasNavigationBar(this, false);
            InitializeComponent();
            ApiClient = apiClient;
            GroupId = groupId;
            GroupName = groupName;
            BindingContext = this;
            OverrideBackButton();
            SwitchBar.SetActiveTab("Users");
            LoadUsers();
        }

        private async void LoadUsers()
        {
            var response = await ApiClient.ExecuteAsync<GetBalancesResponse>(
                HttpMethod.Get,
                $"expenseGroup/{GroupId}/balances"
            );

            if (response.ErrorMessage == null && response.Data?.balances != null)
            {
                var userBalances = response.Data.balances
                    .Select(kvp => new UserBalance
                    {
                        name = kvp.Key,
                        balance = kvp.Value
                    })
                    .ToList();

                UsersList.ItemsSource = userBalances;
            }
            else
            {
                await DisplayAlert("Error", "Failed to load balances: " + response.ErrorMessage, "OK");
            }
        }

        private void OverrideBackButton()
        {
            TopBarView.BackCommand = new Command(() =>
            {
                Application.Current.MainPage = new NavigationPage(new MainPage(ApiClient));
            });
        }

        private void OnTabSelected(object sender, string tabName)
        {
            if (tabName == "Transactions")
                Navigation.PushAsync(new TransactionsPage(ApiClient, GroupId, GroupName));
            else if (tabName == "Settlements")
                Navigation.PushAsync(new SettlementsPage(ApiClient, GroupId, GroupName));
        }

        private async void AddUserButton_Clicked(object sender, EventArgs e)
        {
            await Navigation.PushAsync(new AddUsersPage(ApiClient, GroupId));
        }
    }
}

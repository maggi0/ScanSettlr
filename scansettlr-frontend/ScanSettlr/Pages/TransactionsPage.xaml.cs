using ScanSettlr.Api;
using ScanSettlr.Api.Model;
using ScanSettlr.Api.Schema;

namespace ScanSettlr
{
    public partial class TransactionsPage : ContentPage
    {
        private ApiClient ApiClient;

        public string GroupId { get; }
        public string GroupName { get; }

        public TransactionsPage(ApiClient apiClient, string groupId, string groupName)
        {
            NavigationPage.SetHasNavigationBar(this, false);
            InitializeComponent();
            ApiClient = apiClient;
            GroupId = groupId;
            GroupName = groupName;
            BindingContext = this;
            OverrideBackButton();
            InitializeSettingsButton();
            SwitchBar.SetActiveTab("Transactions");
            LoadTransactions();
        }

        private async void LoadTransactions()
        {
            var response = await ApiClient.ExecuteAsync<List<Transaction>>(
                HttpMethod.Get,
                $"expense/expenseGroup/{GroupId}"
            );

            if (response.ErrorMessage == null && response.Data != null)
            {
                TransactionsList.ItemsSource = response.Data;
            }
            else
            {
                await DisplayAlert("Error", "Failed to load transactions: " + response.ErrorMessage, "OK");
            }
        }

        private void OverrideBackButton()
        {
            TopBarView.BackCommand = new Command(() =>
            {
                Application.Current.MainPage = new NavigationPage(new MainPage(ApiClient));
            });
        }

        private void InitializeSettingsButton()
        {
            TopBarView.SettingsCommand = new Command(() =>
            {
                SettingsPopup.Show();
            });
        }

        protected override void OnAppearing()
        {
            base.OnAppearing();
            LoadTransactions();
        }

        private void OnTabSelected(object sender, string tabName)
        {
            if (tabName == "Users")
                Navigation.PushAsync(new UsersPage(ApiClient, GroupId, GroupName));
            else if (tabName == "Settlements")
                Navigation.PushAsync(new SettlementsPage(ApiClient, GroupId, GroupName));
        }

        private async void OnTransactionClicked(object sender, EventArgs e)
        {
            if (sender is Button button && button.BindingContext is Transaction transaction)
            {
                await Navigation.PushAsync(new TransactionPage(ApiClient, GroupId, transaction.id, transaction.name));
            }
        }

        private async void OnAddNewTransactionClicked(object sender, EventArgs e)
        {
            if (sender is Button button)
            {
                await Navigation.PushAsync(new TransactionDetailsPage(ApiClient, GroupId));
            }
        }
    }
}

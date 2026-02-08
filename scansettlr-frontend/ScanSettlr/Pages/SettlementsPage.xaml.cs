using ScanSettlr.Api;
using ScanSettlr.Api.Model;
using System.Collections.ObjectModel;

namespace ScanSettlr
{
    public partial class SettlementsPage : ContentPage
    {
        private ApiClient ApiClient;
        public string GroupId { get; }
        public string GroupName { get; }

        public Command<Settlement> TogglePaidCommand { get; }

        private ObservableCollection<Settlement> _settlements;


        public SettlementsPage(ApiClient apiClient, string groupId, string groupName)
        {
            NavigationPage.SetHasNavigationBar(this, false);
            InitializeComponent();
            ApiClient = apiClient;
            GroupId = groupId;
            GroupName = groupName;
            BindingContext = this;
            OverrideBackButton();
            SwitchBar.SetActiveTab("Settlements");
            
            _ = LoadSettlements();

            TogglePaidCommand = new Command<Settlement>(async settlement =>
            {
                if (settlement == null)
                    return;

                var success = await MarkAsPaid(settlement.id);

                if (success)
                {
                    _settlements.Remove(settlement);
                }
            });
        }

        private async Task LoadSettlements()
        {
            var response = await ApiClient.ExecuteAsync<List<Settlement>>(
                HttpMethod.Get,
                $"expenseGroup/{GroupId}/settlements"
            );

            if (response.ErrorMessage == null)
            {
                if (response.Data != null)
                {
                    _settlements = new ObservableCollection<Settlement>(response.Data);
                    SettlementsList.ItemsSource = _settlements;
                }
            }
            else
            {
                await DisplayAlert("Error", "Failed to load settlements: " + response.ErrorMessage, "OK");
            }
        }

        private async Task<bool> MarkAsPaid(string settlementId)
        {
            var response = await ApiClient.ExecuteAsync(
                HttpMethod.Put,
                $"settlement/{settlementId}/pay"
            );

            if (response.ErrorMessage != null)
            {
                await DisplayAlert(
                    "Error",
                    "Failed to mark settlement as paid: " + response.ErrorMessage,
                    "OK");

                return false;
            }

            return true;
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
            else if (tabName == "Users")
                Navigation.PushAsync(new UsersPage(ApiClient, GroupId, GroupName));
        }
    }
}

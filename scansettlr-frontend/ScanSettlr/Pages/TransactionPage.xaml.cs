using ScanSettlr.Api;
using ScanSettlr.Api.Model;
using ScanSettlr.Api.Schema;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Runtime.CompilerServices;

namespace ScanSettlr
{
    public partial class TransactionPage : ContentPage, INotifyPropertyChanged
    {
        private ApiClient ApiClient;

        public string GroupId { get; }
        public string TransactionId { get; }
        public string TransactionName { get; }

        private Person lender;
        public Person Lender
        {
            get => lender;
            set { lender = value; OnPropertyChanged(); }
        }

        private ObservableCollection<Person> borrowers = new();
        public ObservableCollection<Person> Borrowers
        {
            get => borrowers;
            set { borrowers = value; OnPropertyChanged(); }
        }

        public TransactionPage(ApiClient apiClient, string groupId, string transactionId, string transactionName)
        {
            NavigationPage.SetHasNavigationBar(this, false);
            InitializeComponent();
            ApiClient = apiClient;
            GroupId = groupId;
            TransactionId = transactionId;
            TransactionName = transactionName;
            BindingContext = this;
            LoadTransactionDetails();
        }

        private async void LoadTransactionDetails()
        {
            var response = await ApiClient.ExecuteAsync<GetTransactionResponse>(
                HttpMethod.Get,
                $"expense/{TransactionId}"
            );

            if (response.ErrorMessage == null && response.Data is GetTransactionResponse data)
            {
                Lender = data.lender;

                Borrowers.Clear();
                foreach (var b in data.borrowers)
                    Borrowers.Add(b);
            }
            else
            {
                await DisplayAlert("Error", "Failed to load transactions details: " + response.ErrorMessage, "OK");
            }
        }

        private async void OnEditTransactionClicked(object sender, EventArgs e)
        {
            if (sender is Button button)
            {
                await Navigation.PushAsync(new TransactionDetailsPage(ApiClient, GroupId, TransactionId, TransactionName, Lender, Borrowers));
            }
        }
        protected override void OnAppearing()
        {
            base.OnAppearing();
            LoadTransactionDetails();
        }


        public event PropertyChangedEventHandler PropertyChanged;
        private void OnPropertyChanged([CallerMemberName] string prop = null) =>
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(prop));
    }

}

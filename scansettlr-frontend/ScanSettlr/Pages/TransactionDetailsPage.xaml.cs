using ScanSettlr.Api;
using ScanSettlr.Api.Model;
using ScanSettlr.Api.Schema;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Runtime.CompilerServices;

namespace ScanSettlr
{
    public partial class TransactionDetailsPage : ContentPage, INotifyPropertyChanged
    {
        private ApiClient ApiClient;

        private string title;
        public string Title
        {
            get => title;
            set { title = value; OnPropertyChanged(); }
        }

        public string GroupId { get; }
        public string TransactionId { get; }
        public Person initialLender { get; }
        public ObservableCollection<Person> initialBorrowers { get; }

        public ObservableCollection<string> AvailableCurrencies { get; } = new();
        public ObservableCollection<Currency> Currencies { get; } = new();
        public ObservableCollection<User> AvailableUsers { get; } = new();


        private User selectedLender;
        public User SelectedLender
        {
            get => selectedLender;
            set { selectedLender = value; OnPropertyChanged(); }
        }

        public ObservableCollection<Participant> Participants { get; set; } = new();

        public TransactionDetailsPage(ApiClient apiClient, string groupId, string transactionId, string transactionName, Person lender, ObservableCollection<Person> borrowers)
        {
            NavigationPage.SetHasNavigationBar(this, false);
            InitializeComponent();
            
            ApiClient = apiClient;
            GroupId = groupId;
            TransactionId = transactionId;
            Title = transactionName;
            initialLender = lender;
            initialBorrowers = borrowers;

            BindingContext = this;

            LoadInitialData();
        }

        public TransactionDetailsPage(ApiClient apiClient, string groupId) // TODO: need to create new transaction
        {
            NavigationPage.SetHasNavigationBar(this, false);
            InitializeComponent();

            ApiClient = apiClient;
            GroupId = groupId;

            BindingContext = this;

            LoadInitialData();
        }

        private async void LoadInitialData()
        {
            await LoadParticipants();
            await LoadCurrencies();

            ApplyInitialTransactionData();
        }

        private async Task LoadCurrencies()
        {
            var response = await ApiClient.ExecuteAsync<List<string>>(
                HttpMethod.Get,
                "currency"
            );

            if (response.ErrorMessage == null && response.Data != null)
            {
                AvailableCurrencies.Clear();
                foreach (var c in response.Data)
                    AvailableCurrencies.Add(c);

                Currencies.Clear();
                Currencies.Add(new Currency
                {
                    Amount = "",
                    SelectedCurrency = AvailableCurrencies.First()
                });
            }
            else
            {
                await DisplayAlert("Error", "Failed to load currencies: " + response.ErrorMessage, "OK");
            }
        }

        private async Task LoadParticipants()
        {
            var response = await ApiClient.ExecuteAsync<List<User>>(
                HttpMethod.Get,
                $"expenseGroup/{GroupId}/users"
            );

            if (response.ErrorMessage == null && response.Data != null)
            {
                AvailableUsers.Clear();
                Participants.Clear();

                foreach (var user in response.Data)
                {
                    AvailableUsers.Add(user);

                    Participants.Add(new Participant
                    {
                        UserId = user.id,
                        Name = user.username,
                        IsSelected = true,
                        Amount = "0"
                    });
                }
            }
        }

        private void ApplyInitialTransactionData()
        {
            if (initialLender != null)
            {
                SelectedLender = AvailableUsers
                    .FirstOrDefault(u => u.username == initialLender.Name);
            }

            if (initialBorrowers != null)
            {
                foreach (var p in Participants)
                {
                    var match = initialBorrowers
                        .FirstOrDefault(b => b.Name == p.Name);

                    if (match != null)
                    {
                        p.IsSelected = true;
                        p.Amount = match.Amount.ToString("0.00");
                    }
                    else
                    {
                        p.IsSelected = false;
                    }
                }

                if (Currencies.Any())
                {
                    var total = initialBorrowers.Sum(b => b.Amount);
                    Currencies[0].Amount = total.ToString("0.00");
                }
            }
        }

        private void OnSplitEvenlyClicked(object sender, EventArgs e)
        {
            if (!Currencies.Any() || !decimal.TryParse(Currencies[0].Amount, out var totalAmount))
            {
                DisplayAlert("Error", "Please enter a valid total amount first.", "OK");
                return;
            }

            var selectedParticipants = Participants.Where(p => p.IsSelected).ToList();
            if (!selectedParticipants.Any())
            {
                DisplayAlert("Error", "No participants selected.", "OK");
                return;
            }

            var splitAmount = totalAmount / selectedParticipants.Count;

            foreach (var participant in selectedParticipants)
            {
                participant.Amount = splitAmount.ToString("0.00");
            }
        }

        private async void OnAdvancedOptionsClicked(object sender, EventArgs e)
        {
            if (sender is Button button)
            {
                await Navigation.PushAsync(new TransactionItemsPage(ApiClient, GroupId, TransactionId, Title)); // TODO: Fix?
            }
        }

        private async void OnSaveTransactionClicked(object sender, EventArgs e)
        {
            if (string.IsNullOrWhiteSpace(titleEntry.Text))
            {
                await DisplayAlert("Error", "Title is required", "OK");
                return;
            }

            if (SelectedLender == null)
            {
                await DisplayAlert("Error", "Select a payer", "OK");
                return;
            }

            var borrowerDetails = new Dictionary<string, decimal>();

            foreach (var p in Participants.Where(p => p.IsSelected))
            {
                borrowerDetails[p.UserId] =
                    decimal.TryParse(p.Amount, out var amt) ? amt : 0;
            }

            bool isUpdate = !string.IsNullOrEmpty(TransactionId);

            if (isUpdate)
            {
                var request = new CreateExpenseRequest
                {
                    expenseId = TransactionId,
                    expenseGroupId = GroupId,
                    name = titleEntry.Text,
                    lenderId = SelectedLender.id,
                    borrowerDetails = borrowerDetails
                };

                var response = await ApiClient.ExecuteAsync(
                    HttpMethod.Put,
                    "expense",
                    request
                );

                if (response.ErrorMessage == null)
                {
                    await DisplayAlert("Success", "Expense updated", "OK");
                    await Navigation.PopAsync();
                }
                else
                {
                    await DisplayAlert("Error", response.ErrorMessage, "OK");
                }
            }
            else
            {
                var request = new CreateExpenseRequest
                {
                    expenseGroupId = GroupId,
                    name = titleEntry.Text,
                    lenderId = SelectedLender.id,
                    borrowerDetails = borrowerDetails
                };

                var response = await ApiClient.ExecuteAsync(
                    HttpMethod.Post,
                    "expense",
                    request
                );

                if (response.ErrorMessage == null)
                {
                    await DisplayAlert("Success", "Expense created", "OK");
                    await Navigation.PopAsync();
                }
                else
                {
                    await DisplayAlert("Error", response.ErrorMessage, "OK");
                }
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;
        private void OnPropertyChanged([CallerMemberName] string name = "") =>
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(name));
    }
}
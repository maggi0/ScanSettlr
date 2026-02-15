using ScanSettlr.Api;
using ScanSettlr.Api.Model;
using ScanSettlr.Api.Schema;
using System.Collections.ObjectModel;

namespace ScanSettlr
{
    public partial class TransactionItemsPage : ContentPage
    {
        private ApiClient ApiClient;

        public string GroupId { get; }
        public string TransactionId { get; }
        public string TransactionName { get; }

        public ObservableCollection<string> AvailableCurrencies { get; set; } = new();

        public ObservableCollection<Currency> Currencies { get; set; } = new()
        {
            new Currency { Amount = "", SelectedCurrency = "PLN" }
        };

        public ObservableCollection<User> AvailableUsers { get; set; } = new();

        public ObservableCollection<Item> Items { get; set; } = new();

        public Command<Item> DeleteItemCommand { get; }

        private string _selectedCurrency;

        public string SelectedCurrency
        {
            get => _selectedCurrency;
            set
            {
                if (_selectedCurrency != value)
                {
                    _selectedCurrency = value;
                    OnPropertyChanged();
                }
            }
        }

        public TransactionItemsPage(ApiClient apiClient, string groupId, string transactionId, string transactionName)
        {
            NavigationPage.SetHasNavigationBar(this, false);

            ApiClient = apiClient;
            GroupId = groupId;
            TransactionId = transactionId;
            TransactionName = transactionName;

            InitializeComponent();
            BindingContext = this;

            DeleteItemCommand = new Command<Item>(OnDeleteItem);
            _selectedCurrency = AvailableCurrencies.FirstOrDefault() ?? "PLN";


            _ = LoadInitialData();
        }

        private async Task LoadInitialData()
        {
            await LoadUsers();
            await LoadItems();
            await LoadCurrencies();
        }

        private async Task LoadUsers()
        {
            var response = await ApiClient.ExecuteAsync<List<User>>(
                HttpMethod.Get,
                $"expenseGroup/{GroupId}/users"
            );

            if (response.ErrorMessage == null && response.Data != null)
            {
                AvailableUsers.Clear();
                foreach (var user in response.Data)
                    AvailableUsers.Add(user);
            }
        }

        private async Task LoadItems()
        {
            var response = await ApiClient.ExecuteAsync<GetItemsResponse>(
                HttpMethod.Get,
                $"expense/{TransactionId}/items"
            );

            if (response.ErrorMessage == null)
            {
                if (response.Data is GetItemsResponse getItemsResponse)
                {
                    Items.Clear();

                    foreach (var backendItem in getItemsResponse.Items)
                    {
                        var paidByUser = AvailableUsers
                            .FirstOrDefault(u => u.username.Equals(backendItem.paidBy));

                        Items.Add(new Item
                        {
                            name = backendItem.name,
                            amount = backendItem.amount,
                            paidBy = paidByUser
                        });
                    }

                    if (!Items.Any())
                    {
                        Items.Add(new Item
                        {
                            name = "",
                            amount = "",
                            paidBy = AvailableUsers.FirstOrDefault()
                        });
                    }
                }
                return;
            }
            else
            {
                await DisplayAlert("Error", "Failed to load items: " + response.ErrorMessage, "OK");
                return;
            }
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

                if (!Currencies.Any())
                {
                    Currencies.Add(new Currency
                    {
                        Amount = "",
                        SelectedCurrency = AvailableCurrencies.FirstOrDefault() ?? "PLN"
                    });
                }
                else
                {
                    foreach (var cur in Currencies)
                    {
                        if (!AvailableCurrencies.Contains(cur.SelectedCurrency))
                            cur.SelectedCurrency = AvailableCurrencies.FirstOrDefault() ?? "PLN";
                    }
                }
            }
            else
            {
                await DisplayAlert("Error", "Failed to load currencies: " + response.ErrorMessage, "OK");
            }
        }


        private void OnDeleteItem(Item item)
        {
            if (item != null && Items.Contains(item))
            {
                Items.Remove(item);
            }
        }

        private async void OnSendReceiptClicked(object sender, EventArgs e)
        {
            try
            {
                var result = await FilePicker.PickAsync(new PickOptions
                {
                    PickerTitle = "Select a receipt photo",
                    FileTypes = FilePickerFileType.Images
                });

                if (result == null)
                    return;

                var response = await ApiClient.UploadFileAsync<Dictionary<string, string>>(
                    endpoint: "receipt",
                    filePath: result.FullPath
                );

                if (!string.IsNullOrEmpty(response.ErrorMessage))
                {
                    await DisplayAlert("Error", response.ErrorMessage, "OK");
                    return;
                }

                var items = response.Data!;

                var message = string.Join("\n", items.Select(i => $"{i.Key}: {i.Value}"));


                bool accept = await DisplayAlert(
                    "Parsed Receipt",
                    message + "\n\nDo you want to use this data?",
                    "Yes",
                    "No"
                );

                if (!accept)
                {
                    return;
                }

                foreach (var kv in items)
                { 
                    Items.Add(new Item
                    {
                        name = kv.Key,
                        amount = kv.Value,
                        paidBy = null
                    });
                }
            }
            catch (Exception ex)
            {
                await DisplayAlert("Error", ex.Message, "OK");
            }
        }

        private async void OnCalculateDetailsClicked(object sender, EventArgs e)
        {
            if (string.IsNullOrWhiteSpace(SelectedCurrency))
            {
                await DisplayAlert("Error", "Please select a currency.", "OK");
                return;
            }

            if (!Items.Any())
            {
                await DisplayAlert("Error", "No items to calculate.", "OK");
                return;
            }


            var invalidItems = Items
                .Where(i => string.IsNullOrWhiteSpace(i.name)
                            || string.IsNullOrWhiteSpace(i.amount)
                            || !decimal.TryParse(i.amount, out _)
                            || i.paidBy == null)
                .ToList();

            if (invalidItems.Any())
            {
                await DisplayAlert("Error", "Please fill all item names, amounts, and assign a user for each item.", "OK");
                return;
            }

            OnSaveItemsClicked(sender, e);

            var borrowerTotals = new Dictionary<User, decimal>();

            foreach (var item in Items)
            {
                if (item.paidBy == null) continue;

                if (!decimal.TryParse(item.amount, out var amount))
                    continue;

                if (!borrowerTotals.ContainsKey(item.paidBy))
                    borrowerTotals[item.paidBy] = 0;

                borrowerTotals[item.paidBy] += amount;
            }

            if (!borrowerTotals.Any())
            {
                await DisplayAlert("Error", "No valid item amounts found.", "OK");
                return;
            }

            var lender = borrowerTotals.OrderByDescending(kv => kv.Value).First().Key;

            var borrowers = new ObservableCollection<Person>(
                borrowerTotals.Select(kv => new Person
                {
                    Name = kv.Key.username,
                    Amount = kv.Value
                })
            );

            var totalAmount = borrowers.Sum(b => b.Amount);

            foreach (var b in borrowers)
            {
                if (b.Name == lender.username)
                    b.Amount = totalAmount;
            }

            await Navigation.PushAsync(new TransactionDetailsPage(
                ApiClient,
                GroupId,
                TransactionId,
                TransactionName,
                new Person { Name = lender.username, Amount = totalAmount },
                borrowers
            ));
        }

        private async void OnSaveItemsClicked(object sender, EventArgs e)
        {
            var validItems = Items
                .Where(i =>
                    !string.IsNullOrWhiteSpace(i.name) &&
                    decimal.TryParse(i.amount, out _) &&
                    i.paidBy != null)
                .ToList();

            if (!validItems.Any())
            {
                await DisplayAlert("Error", "Add at least one valid item", "OK");
                return;
            }

            var requestItems = validItems.Select(i => new Item
            {
                name = i.name,
                amount = i.amount,
                paidBy = i.paidBy
            }).ToList();

            var response = await ApiClient.ExecuteAsync(
                HttpMethod.Post,
                $"expense/{TransactionId}/items",
                requestItems
            );

            if (response.ErrorMessage == null)
            {
                await DisplayAlert("Success", "Items saved", "OK");
                await Navigation.PopAsync();
            }
            else
            {
                await DisplayAlert("Error", "Error saving items: " + response.ErrorMessage, "OK");
            }
        }


        private void OnAddItemClicked(object sender, EventArgs e)
        {
            Items.Add(new Item
            {
                name = "",
                amount = "",
                paidBy = AvailableUsers.FirstOrDefault()
            });
        }

    }
}

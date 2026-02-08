using ScanSettlr.Api;
using ScanSettlr.Api.Model;
using ScanSettlr.Api.Schema;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Text;
using System.Text.Json;
using System.Windows.Input;

namespace ScanSettlr
{
    public partial class AddUsersPage : ContentPage, INotifyPropertyChanged
    {
        public event PropertyChangedEventHandler PropertyChanged;

        private ApiClient ApiClient;
        private string searchQuery;
        public string SearchQuery
        {
            get => searchQuery;
            set
            {
                if (searchQuery != value)
                {
                    searchQuery = value;
                    PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(SearchQuery)));
                }
            }
        }

        public ObservableCollection<User> SearchResults { get; } = new();
        public ObservableCollection<User> SelectedUsers { get; } = new();
        private ObservableCollection<User> users = new();
        public ObservableCollection<User> Users
        {
            get => users;
            set
            {
                if (users != value)
                {
                    users = value;
                    PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(Users)));
                }
            }
        }


        public ICommand SearchCommand { get; }
        public ICommand AddUsersCommand { get; }
        public ICommand RemoveUserCommand { get; }

        public string GroupId { get; }

        public AddUsersPage(ApiClient apiClient, string groupId)
        {
            NavigationPage.SetHasNavigationBar(this, false);
            
            InitializeComponent();
            
            SearchCommand = new Command(async () => await OnSearch());
            AddUsersCommand = new Command(OnAddUsers);
            RemoveUserCommand = new Command<User>(OnRemoveUser);
            
            BindingContext = this;

            ApiClient = apiClient;
            GroupId = groupId;

            _ = LoadExistingUsers();
        }

        private async Task LoadExistingUsers()
        {
            var response = await ApiClient.ExecuteAsync<List<User>>(HttpMethod.Get, $"expenseGroup/{GroupId}/users");

            if (response.ErrorMessage == null && response.Data != null)
            {
                Users.Clear();
                foreach (var u in response.Data)
                    Users.Add(u);
            }
        }

        private async Task OnSearch()
        {
            if (string.IsNullOrWhiteSpace(SearchQuery)) return;

            var response = await ApiClient.ExecuteAsync<List<User>>(
                HttpMethod.Get,
                $"user/search?query={SearchQuery}"
            );

            if (response.ErrorMessage == null && response.Data is List<User> users)
            {
                SearchResults.Clear();
                foreach (var u in users)
                    SearchResults.Add(u);
            }
            else
            {
                await DisplayAlert("Error", "Failed to search for users: " + response.ErrorMessage, "OK");
            }
        }
        private void OnSelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            SelectedUsers.Clear();

            foreach (var user in e.CurrentSelection.OfType<User>())
            {
                SelectedUsers.Add(user);
            }
        }


        private async void OnAddUsers()
        {
            if (SelectedUsers.Count == 0) return;

            foreach (var u in SelectedUsers)
            {
                if (!Users.Any(existing => existing.id == u.id))
                {
                    MainThread.BeginInvokeOnMainThread(() =>
                    {
                        Users.Add(u);
                    });
                }
            }

            var userIds = SelectedUsers.Select(u => u.id).ToList();

            var response = await ApiClient.ExecuteAsync(
                HttpMethod.Post,
                $"expenseGroup/{GroupId}/users",
                new AddUsersRequest { ids = userIds }
            );

            if (response.ErrorMessage == null)
            {
                await DisplayAlert("Success", "Users added successfully!", "OK");
                SearchResults.Clear();
                SelectedUsers.Clear();
            }
            else
            {
                await DisplayAlert("Error", "Failed to add users: " + response.ErrorMessage, "OK");
            }
        }

        private async void OnRemoveUser(User user)
        {
            if (user == null) return;

            var response = await ApiClient.ExecuteAsync(
                HttpMethod.Delete,
                $"expenseGroup/{GroupId}/users/{user.id}"
            );

            if (response.ErrorMessage == null)
            {
                await DisplayAlert("Success", "User deleted successfully!", "OK");
                MainThread.BeginInvokeOnMainThread(() =>
                {
                    Users.Remove(user);
                });
            }
            else
            {
                await DisplayAlert("Error", "Failed to remove users: " + response.ErrorMessage, "OK");
            }
        }
    }
}

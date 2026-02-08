using ScanSettlr.Api;
using ScanSettlr.Api.Schema;
using System.Windows.Input;

namespace ScanSettlr
{
    public partial class AddExpenseGroupPage : ContentPage
    {
        private ApiClient ApiClient;

        public ICommand CreateGroupCommand { get; }

        public AddExpenseGroupPage(ApiClient apiClient)
        {
            NavigationPage.SetHasNavigationBar(this, false);
            
            InitializeComponent();
            
            CreateGroupCommand = new Command(async () => await OnCreateGroup());
            
            BindingContext = this;

            this.ApiClient = apiClient;
        }

        private async Task OnCreateGroup()
        {
            if (string.IsNullOrWhiteSpace(nameEntry.Text))
            {
                await DisplayAlert("Error", "Group name cannot be empty.", "OK");
                return;
            }

            var response = await ApiClient.ExecuteAsync<CreateExpenseGroupResponse>(
                HttpMethod.Post,
                "expenseGroup",
                new CreateExpenseGroupRequest { name = nameEntry.Text }
            );

            if (response.ErrorMessage == null && response.Data is CreateExpenseGroupResponse createExpenseGroupResponse)
            {
                var expenseGroupId = createExpenseGroupResponse.Id;
                var userId = GetUserId();
                if (userId == null)
                {
                    await DisplayAlert("Error", "No user ID found. Please log in.", "OK");
                    return;
                }

                var addCurrentUserResponse = await ApiClient.ExecuteAsync(
                    HttpMethod.Post,
                    $"expenseGroup/{expenseGroupId}/users",
                    new AddUsersRequest { ids = new List<string> { userId } }
                );

                if (addCurrentUserResponse.ErrorMessage == null)
                {
                    Application.Current.MainPage = new NavigationPage(new MainPage(ApiClient));
                }
                else
                {
                    await DisplayAlert("Error", "Failed add current user to new expense group: " + addCurrentUserResponse.ErrorMessage, "OK");
                }
            }
            else
            {
                await DisplayAlert("Error", "Failed to create group: " + response.ErrorMessage, "OK");
            }
        }

        private string? GetUserId()
        {
            var userId = Preferences.Get("user_id", null);
            if (string.IsNullOrEmpty(userId))
            {
                return null;
            }

            return userId;
        }
    }
}

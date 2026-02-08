using Microsoft.Extensions.Logging;
using ScanSettlr.Api;

namespace ScanSettlr
{
    public static class MauiProgram
    {
        public static MauiApp CreateMauiApp()
        {
            var builder = MauiApp.CreateBuilder();
            builder
                .UseMauiApp<App>()
                .ConfigureFonts(fonts =>
                {
                    fonts.AddFont("OpenSans-Regular.ttf", "OpenSansRegular");
                    fonts.AddFont("OpenSans-Semibold.ttf", "OpenSansSemibold");
                });

#if DEBUG
    		builder.Logging.AddDebug();
#endif
            builder.Services.AddSingleton<ApiClient>();

            // Register pages
            builder.Services.AddTransient<LoadingPage>();
            builder.Services.AddTransient<WelcomePage>();
            builder.Services.AddTransient<MainPage>();
            builder.Services.AddTransient<LoginPage>();
            builder.Services.AddTransient<SignUpPage>();
            builder.Services.AddTransient<UsersPage>();
            builder.Services.AddTransient<TransactionsPage>();
            builder.Services.AddTransient<SettlementsPage>();
            builder.Services.AddTransient<AddUsersPage>();
            builder.Services.AddTransient<AddExpenseGroupPage>();
            builder.Services.AddTransient<TransactionDetailsPage>();
            builder.Services.AddTransient<TransactionItemsPage>();
            builder.Services.AddTransient<TransactionPage>();

            return builder.Build();
        }
    }
}

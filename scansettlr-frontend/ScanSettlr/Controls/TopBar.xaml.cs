using System.Windows.Input;
using Microsoft.Maui.Controls;

namespace ScanSettlr.Controls
{
    public partial class TopBar : ContentView
    {
        public SettingsOverlay Overlay { get; set; }

        public static readonly BindableProperty TitleProperty =
            BindableProperty.Create(nameof(Title), typeof(string), typeof(TopBar), string.Empty);

        public static readonly BindableProperty CurrencyProperty =
            BindableProperty.Create(nameof(Currency), typeof(string), typeof(TopBar), "PLN", propertyChanged: OnCurrencyChanged);

        public string Title
        {
            get => (string)GetValue(TitleProperty);
            set => SetValue(TitleProperty, value);
        }

        public string Currency
        {
            get => (string)GetValue(CurrencyProperty);
            set => SetValue(CurrencyProperty, value);
        }

        // ShowBackButton
        public static readonly BindableProperty ShowBackButtonProperty =
            BindableProperty.Create(nameof(ShowBackButton), typeof(bool), typeof(TopBar), true);

        public bool ShowBackButton
        {
            get => (bool)GetValue(ShowBackButtonProperty);
            set => SetValue(ShowBackButtonProperty, value);
        }

        // ShowSettingsButton
        public static readonly BindableProperty ShowSettingsButtonProperty =
            BindableProperty.Create(nameof(ShowSettingsButton), typeof(bool), typeof(TopBar), true);

        public bool ShowSettingsButton
        {
            get => (bool)GetValue(ShowSettingsButtonProperty);
            set => SetValue(ShowSettingsButtonProperty, value);
        }

        // Optional ICommand for Back
        public static readonly BindableProperty BackCommandProperty =
            BindableProperty.Create(nameof(BackCommand), typeof(ICommand), typeof(TopBar), null);

        public ICommand BackCommand
        {
            get => (ICommand)GetValue(BackCommandProperty);
            set => SetValue(BackCommandProperty, value);
        }

        // Optional ICommand for Settings
        public static readonly BindableProperty SettingsCommandProperty =
            BindableProperty.Create(nameof(SettingsCommand), typeof(ICommand), typeof(TopBar), null);

        public ICommand SettingsCommand
        {
            get => (ICommand)GetValue(SettingsCommandProperty);
            set => SetValue(SettingsCommandProperty, value);
        }

        public TopBar()
        {
            InitializeComponent();
        }

        private static void OnCurrencyChanged(BindableObject bindable, object oldValue, object newValue)
        {
            if (bindable is TopBar topBar)
            {
                // force UI update
                topBar.OnPropertyChanged(nameof(Currency));
            }
        }

        private async void OnBackClicked(object sender, EventArgs e)
        {
            if (BackCommand != null && BackCommand.CanExecute(null))
            {
                BackCommand.Execute(null);
                return;
            }

            // Default behavior
            if (Application.Current?.MainPage?.Navigation?.NavigationStack?.Count > 1)
                await Application.Current.MainPage.Navigation.PopAsync();
        }

        private void OnSettingsClicked(object sender, EventArgs e)
        {
            if (SettingsCommand != null && SettingsCommand.CanExecute(null))
            {
                SettingsCommand.Execute(null);
                return;
            }

            Overlay?.Show();
        }
    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ScanSettlr.Controls
{
    public partial class ExpenseGroupSwitchBar : ContentView
    {
        private static Color ButtonNotActive = Color.FromArgb("#2BB9D4");
        private static Color ButtonActive = Color.FromArgb("#34DFFF");
        public event EventHandler<string> TabSelected;

        public Color SettlementsColor { get; set; } = ButtonNotActive;
        public Color TransactionsColor { get; set; } = ButtonNotActive;
        public Color UsersColor { get; set; } = ButtonNotActive;

        public ExpenseGroupSwitchBar()
        {
            InitializeComponent();
        }

        public void SetActiveTab(string tabName)
        {
            SettlementsColor = tabName == "Settlements" ? ButtonActive : ButtonNotActive;
            TransactionsColor = tabName == "Transactions" ? ButtonActive : ButtonNotActive;
            UsersColor = tabName == "Users" ? ButtonActive : ButtonNotActive;
            OnPropertyChanged(nameof(SettlementsColor));
            OnPropertyChanged(nameof(TransactionsColor));
            OnPropertyChanged(nameof(UsersColor));
        }

        private void SettlementsClicked(object sender, EventArgs e) =>
            TabSelected?.Invoke(this, "Settlements");

        private void TransactionsClicked(object sender, EventArgs e) =>
            TabSelected?.Invoke(this, "Transactions");

        private void UsersClicked(object sender, EventArgs e) =>
            TabSelected?.Invoke(this, "Users");
    }
}

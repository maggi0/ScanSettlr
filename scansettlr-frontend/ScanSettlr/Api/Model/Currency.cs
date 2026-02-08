using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;

namespace ScanSettlr.Api.Model
{
    public class Currency : INotifyPropertyChanged
    {
        private string amount;
        private string selectedCurrency;

        public string Amount
        {
            get => amount;
            set { amount = value; OnPropertyChanged(); }
        }

        public string SelectedCurrency
        {
            get => selectedCurrency;
            set { selectedCurrency = value; OnPropertyChanged(); }
        }

        public event PropertyChangedEventHandler PropertyChanged;
        private void OnPropertyChanged([CallerMemberName] string name = "") =>
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(name));
    }
}

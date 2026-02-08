using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;

namespace ScanSettlr.Api.Model
{
    public class UserBalance : INotifyPropertyChanged
    {
        private decimal _balance;
        public string name { get; set; }

        public decimal balance
        {
            get => _balance;
            set { _balance = value; OnPropertyChanged(); OnPropertyChanged(nameof(balancePositive)); }
        }

        public bool balancePositive => balance >= 0;

        public event PropertyChangedEventHandler PropertyChanged;
        private void OnPropertyChanged([CallerMemberName] string name = null)
            => PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(name));
    }
}

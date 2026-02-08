using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;

namespace ScanSettlr.Api.Model
{
    public class Person : INotifyPropertyChanged
    {
        private string name;
        private decimal amount;

        public string Name
        {
            get => name;
            set { name = value; OnPropertyChanged(); }
        }

        public decimal Amount
        {
            get => amount;
            set { amount = value; OnPropertyChanged(); }
        }

        public event PropertyChangedEventHandler PropertyChanged;

        private void OnPropertyChanged([CallerMemberName] string propName = null) =>
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propName));
    }
}

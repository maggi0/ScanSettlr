using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;

namespace ScanSettlr.Api.Model
{
    public class Item : INotifyPropertyChanged
    {
        private string _name;
        private string _amount;
        private User _paidBy;

        public string name { get => _name; set { _name = value; OnPropertyChanged(); } }
        public string amount { get => _amount; set { _amount = value; OnPropertyChanged(); } }
        public User paidBy { get => _paidBy; set { _paidBy = value; OnPropertyChanged(); } }

        public event PropertyChangedEventHandler PropertyChanged;
        private void OnPropertyChanged([CallerMemberName] string prop = null) =>
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(prop));
    }

}

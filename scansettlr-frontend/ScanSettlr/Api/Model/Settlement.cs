using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ScanSettlr.Api.Model
{
    public class Settlement : BindableObject
    {
        private string _lender;
        private string _borrower;

        public string lender
        {
            get => _lender;
            set { _lender = value; OnPropertyChanged(); OnPropertyChanged(nameof(description)); }
        }
        public string borrower
        {
            get => _borrower;
            set { _borrower = value; OnPropertyChanged(); OnPropertyChanged(nameof(description)); }
        }

        private bool _isPaid;
        public bool isPaid
        {
            get => _isPaid;
            set
            {
                if (_isPaid != value)
                {
                    _isPaid = value;
                    OnPropertyChanged();
                }
            }
        }

        public string id { get; set; }
        public decimal amount { get; set; }

        public string description => $"{borrower} -> {lender}";

    }
}

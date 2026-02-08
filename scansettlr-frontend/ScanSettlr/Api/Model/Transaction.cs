using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ScanSettlr.Api.Model
{
    public class Transaction
    {
        public string id { get; set; }
        public string name { get; set; }
        public Dictionary<string, decimal> borrowerDetails { get; set; }

        public decimal amount => borrowerDetails?.Values.Sum() ?? 0m;
    }
}

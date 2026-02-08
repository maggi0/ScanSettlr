using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ScanSettlr.Api.Schema
{
    public class CreateExpenseRequest
    {
        public string expenseId { get; set; }
        public string expenseGroupId { get; set; }
        public string name { get; set; }
        public string lenderId { get; set; }
        public Dictionary<string, decimal> borrowerDetails { get; set; }
    }
}

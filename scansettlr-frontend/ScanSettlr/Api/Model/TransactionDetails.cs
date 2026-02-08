using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ScanSettlr.Api.Model
{
    public class TransactionDetails
    {
        public Person Lender { get; set; }
        public List<Person> Borrowers { get; set; }
    }
}

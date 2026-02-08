using ScanSettlr.Api.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ScanSettlr.Api.Schema
{
    public class GetTransactionResponse
    {
        public Person lender { get; set; }
        public List<Person> borrowers { get; set; }
    }
}

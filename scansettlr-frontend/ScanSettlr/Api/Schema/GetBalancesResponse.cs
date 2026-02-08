using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ScanSettlr.Api.Schema
{
    public class GetBalancesResponse
    {
        public Dictionary<string, decimal> balances { get; set; } = new();
    }
}

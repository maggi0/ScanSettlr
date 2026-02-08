using ScanSettlr.Api.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ScanSettlr.Api.Schema
{
    public class GetItemsResponse
    {
        public List<Item> Items { get; set; }
    }
}

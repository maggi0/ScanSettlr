using ScanSettlr.Api.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ScanSettlr.Api.Schema
{
    public class AddItemsRequest
    {
        public List<Item> Items { get; set; }
    }
}

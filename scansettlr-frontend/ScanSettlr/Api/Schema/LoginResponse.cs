using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ScanSettlr.Api.Schema
{
    class LoginResponse
    {
        public string token { get; set; }
        public string username { get; set; }
        public string userId { get; set; }
    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ScanSettlr.Api
{
    public class ApiResponse<T>
    {
        public T? Data { get; set; }
        public string? ErrorMessage { get; set; }
        public int StatusCode { get; set; }
    }

    public class ApiResponse
    {
        public string? ErrorMessage { get; set; }
        public int StatusCode { get; set; }
    }
}

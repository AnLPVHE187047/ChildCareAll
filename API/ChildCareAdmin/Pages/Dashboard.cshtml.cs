using Microsoft.AspNetCore.Mvc.RazorPages;

namespace ChildCareAdmin.Pages
{
    public class DashboardModel : PageModel
    {
        private readonly IHttpClientFactory _clientFactory;

        public decimal TotalRevenue { get; set; }
        public int TotalServices { get; set; }
        public int TotalUsers { get; set; }
        public int TotalStaff { get; set; }
        public int CompletedAppointments { get; set; }
        public int PendingAppointments { get; set; }
        public int ConfirmedAppointments { get; set; }
        public int CancelledAppointments { get; set; }

        public List<MonthlyRevenue> RevenueData { get; set; } = new();

        public DashboardModel(IHttpClientFactory clientFactory)
        {
            _clientFactory = clientFactory;
        }

        public async Task OnGetAsync()
        {
            var client = _clientFactory.CreateClient("ChildCareApi");

            var token = HttpContext.Session.GetString("AuthToken");
            if (string.IsNullOrEmpty(token))
            {
                Response.Redirect("/Auth/Login");
                return;
            }

            client.DefaultRequestHeaders.Authorization =
                new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);

            // Lấy dữ liệu thống kê
            var statsResponse = await client.GetAsync("statistics/dashboard");
            if (statsResponse.IsSuccessStatusCode)
            {
                var stats = await statsResponse.Content.ReadFromJsonAsync<DashboardStats>();
                if (stats != null)
                {
                    TotalRevenue = stats.TotalRevenue;
                    TotalServices = stats.TotalServices;
                    TotalUsers = stats.TotalUsers;
                    TotalStaff = stats.TotalStaff;
                    CompletedAppointments = stats.CompletedAppointments;
                    PendingAppointments = stats.PendingAppointments;
                    ConfirmedAppointments = stats.ConfirmedAppointments;
                    CancelledAppointments = stats.CancelledAppointments;
                }
            }

            // Lấy dữ liệu doanh thu theo tháng
            var year = DateTime.Now.Year;
            var revenueResponse = await client.GetAsync($"statistics/revenue-by-month?year={year}");
            if (revenueResponse.IsSuccessStatusCode)
            {
                RevenueData = await revenueResponse.Content.ReadFromJsonAsync<List<MonthlyRevenue>>() ?? new();
            }
        }

        public class DashboardStats
        {
            public decimal TotalRevenue { get; set; }
            public int TotalServices { get; set; }
            public int TotalUsers { get; set; }
            public int TotalStaff { get; set; }
            public int CompletedAppointments { get; set; }
            public int PendingAppointments { get; set; }
            public int ConfirmedAppointments { get; set; }
            public int CancelledAppointments { get; set; }
        }

        public class MonthlyRevenue
        {
            public int Month { get; set; }
            public int Year { get; set; }
            public decimal Revenue { get; set; }
        }
    }
}
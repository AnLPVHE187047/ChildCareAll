namespace ChildCare.Api.DTOs
{
    public class DashboardStatsDTO
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

    public class RevenueByMonthDTO
    {
        public int Month { get; set; }
        public int Year { get; set; }
        public decimal Revenue { get; set; }
    }
}

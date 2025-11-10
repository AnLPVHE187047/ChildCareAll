using ChildCare.Api.DTOs;

namespace ChildCare.Api.Repositories
{
    public interface IStatisticsRepository
    {
        Task<DashboardStatsDTO> GetDashboardStatsAsync();
        Task<IEnumerable<RevenueByMonthDTO>> GetRevenueByMonthAsync(int year);
    }
}

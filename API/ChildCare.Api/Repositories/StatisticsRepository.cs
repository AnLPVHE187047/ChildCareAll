// Repositories/StatisticsRepository.cs
using ChildCare.Api.DTOs;
using ChildCare.Api.Models;
using Microsoft.EntityFrameworkCore;

namespace ChildCare.Api.Repositories
{
    public class StatisticsRepository : IStatisticsRepository
    {
        private readonly ChildCareDbContext _context;

        public StatisticsRepository(ChildCareDbContext context)
        {
            _context = context;
        }

        public async Task<DashboardStatsDTO> GetDashboardStatsAsync()
        {
            // Tổng doanh thu từ các appointment đã hoàn thành
            var totalRevenue = await _context.Appointments
                .Include(a => a.Service)
                .Where(a => a.Status == "Completed")
                .SumAsync(a => a.Service.Price);

            // Tổng số dịch vụ
            var totalServices = await _context.Services.CountAsync();

            // Tổng số user (không tính admin)
            var totalUsers = await _context.Users
                .Where(u => u.Role == "Customer")
                .CountAsync();

            // Tổng số staff
            var totalStaff = await _context.Staff.CountAsync();

            // Thống kê appointment theo status
            var completedAppointments = await _context.Appointments
                .CountAsync(a => a.Status == "Completed");

            var pendingAppointments = await _context.Appointments
                .CountAsync(a => a.Status == "Pending");

            var confirmedAppointments = await _context.Appointments
                .CountAsync(a => a.Status == "Confirmed");

            var cancelledAppointments = await _context.Appointments
                .CountAsync(a => a.Status == "Cancelled");

            return new DashboardStatsDTO
            {
                TotalRevenue = totalRevenue,
                TotalServices = totalServices,
                TotalUsers = totalUsers,
                TotalStaff = totalStaff,
                CompletedAppointments = completedAppointments,
                PendingAppointments = pendingAppointments,
                ConfirmedAppointments = confirmedAppointments,
                CancelledAppointments = cancelledAppointments
            };
        }

        public async Task<IEnumerable<RevenueByMonthDTO>> GetRevenueByMonthAsync(int year)
        {
            var revenueByMonth = await _context.Appointments
                .Include(a => a.Service)
                .Where(a => a.Status == "Completed" && a.AppointmentDate.Year == year)
                .GroupBy(a => a.AppointmentDate.Month)
                .Select(g => new RevenueByMonthDTO
                {
                    Month = g.Key,
                    Year = year,
                    Revenue = g.Sum(a => a.Service.Price)
                })
                .OrderBy(r => r.Month)
                .ToListAsync();

            return revenueByMonth;
        }
    }
}
using ChildCare.Api.DTOs;
using ChildCare.Api.Models;
using Microsoft.EntityFrameworkCore;

namespace ChildCare.Api.Repositories
{
    public class StaffRepository : IStaffRepository
    {
        private readonly ChildCareDbContext _context;

        public StaffRepository(ChildCareDbContext context)
        {
            _context = context;
        }

        public async Task<IEnumerable<StaffDropdownDTO>> GetAllForDropdownAsync()
        {
            return await _context.Staff
                .Select(s => new StaffDropdownDTO
                {
                    StaffID = s.StaffId,
                    FullName = s.FullName
                })
                .ToListAsync();
        }

        public async Task<IEnumerable<AppointmentResponseDTO>> GetStaffScheduleAsync(int staffId, DateOnly date)
        {
            return await _context.Appointments
                .Include(a => a.Service)
                .Include(a => a.User)
                .Where(a => a.StaffId == staffId
                         && a.AppointmentDate == date
                         && a.Status != "Cancelled")
                .Select(a => new AppointmentResponseDTO
                {
                    AppointmentID = a.AppointmentId,
                    UserName = a.User.FullName,
                    ServiceName = a.Service.Name,
                    StaffName = null,
                    AppointmentDate = a.AppointmentDate.ToDateTime(TimeOnly.MinValue),
                    AppointmentTime = a.AppointmentTime.ToTimeSpan(),
                    Address = a.Address,
                    Status = a.Status,
                    CreatedAt = a.CreatedAt
                })
                .ToListAsync();
        }

        public async Task<IEnumerable<AppointmentResponseDTO>> FilterStaffAppointmentsAsync(
            int staffId, string? customerName, int? month, int? week, int? dayOfWeek)
        {
            var query = _context.Appointments
                .Include(a => a.Service)
                .Include(a => a.User)
                .Include(a => a.Staff)
                .Where(a => a.StaffId == staffId && a.Status != "Cancelled");

            // Filter by customer name
            if (!string.IsNullOrEmpty(customerName))
                query = query.Where(a => a.User.FullName.Contains(customerName));

            // Get current year for calculations
            int currentYear = DateTime.Now.Year;

            // Filter by month
            if (month.HasValue)
            {
                query = query.Where(a => a.AppointmentDate.Month == month.Value
                                      && a.AppointmentDate.Year == currentYear);
            }

            // Filter by week (week of month)
            if (week.HasValue && month.HasValue)
            {
                var (startDate, endDate) = GetWeekRangeInMonth(currentYear, month.Value, week.Value);
                query = query.Where(a => a.AppointmentDate >= startDate && a.AppointmentDate <= endDate);
            }

            // Filter by day of week (convert to DateOnly compatible filter)
            if (dayOfWeek.HasValue)
            {
                // Map input: 1=Monday, 2=Tuesday, ..., 7=Sunday
                // Convert to .NET DayOfWeek: Sunday=0, Monday=1, ..., Saturday=6
                int targetDayOfWeek = dayOfWeek.Value == 7 ? 0 : dayOfWeek.Value;

                // Use SQL Server's DATEPART function for better performance
                query = query.Where(a => EF.Functions.DateDiffDay(
                    new DateTime(1900, 1, 1), // A Monday
                    a.AppointmentDate.ToDateTime(TimeOnly.MinValue)) % 7 == (targetDayOfWeek + 6) % 7);
            }

            // Execute query and order results
            var results = await query
                .OrderByDescending(a => a.AppointmentDate)
                .ThenByDescending(a => a.AppointmentTime)
                .Select(a => new AppointmentResponseDTO
                {
                    AppointmentID = a.AppointmentId,
                    UserName = a.User.FullName,
                    ServiceName = a.Service.Name,
                    StaffName = a.Staff != null ? a.Staff.FullName : null,
                    AppointmentDate = a.AppointmentDate.ToDateTime(a.AppointmentTime),
                    AppointmentTime = a.AppointmentTime.ToTimeSpan(),
                    Address = a.Address,
                    Status = a.Status,
                    CreatedAt = a.CreatedAt
                })
                .ToListAsync();

            return results;
        }

        public async Task<bool> UpdateAppointmentStatusAsync(int appointmentId, string status)
        {
            var appointment = await _context.Appointments.FindAsync(appointmentId);
            if (appointment == null) return false;

            // Get current date (server time)
            var today = DateOnly.FromDateTime(DateTime.Now);
            var appointmentDate = appointment.AppointmentDate;

            // Business rules validation
            if (status == "Confirmed")
            {
                // Chỉ có thể Confirm trước ngày đặt lịch
                if (appointmentDate <= today)
                {
                    throw new InvalidOperationException("Không thể xác nhận sau ngày đặt lịch");
                }

                // Chỉ có thể Confirm từ trạng thái Pending
                if (appointment.Status != "Pending")
                {
                    throw new InvalidOperationException("Chỉ có thể xác nhận lịch hẹn đang ở trạng thái Pending");
                }
            }
            else if (status == "Completed")
            {
                // Chỉ có thể Complete sau ngày đặt lịch
                if (appointmentDate > today)
                {
                    throw new InvalidOperationException("Không thể hoàn tất trước ngày đặt lịch");
                }

                // Chỉ có thể Complete từ trạng thái Confirmed
                if (appointment.Status != "Confirmed")
                {
                    throw new InvalidOperationException("Chỉ có thể hoàn tất lịch hẹn đã được xác nhận");
                }
            }
            else if (status == "Cancelled")
            {
                // Có thể hủy bất kỳ lúc nào (trừ khi đã Completed)
                if (appointment.Status == "Completed")
                {
                    throw new InvalidOperationException("Không thể hủy lịch hẹn đã hoàn tất");
                }
            }

            appointment.Status = status;
            _context.Appointments.Update(appointment);
            await _context.SaveChangesAsync();
            return true;
        }

        public async Task<AppointmentResponseDTO?> GetAppointmentDetailAsync(int appointmentId)
        {
            return await _context.Appointments
                .Include(a => a.User)
                .Include(a => a.Service)
                .Include(a => a.Staff)
                .Where(a => a.AppointmentId == appointmentId)
                .Select(a => new AppointmentResponseDTO
                {
                    AppointmentID = a.AppointmentId,
                    UserName = a.User.FullName,
                    ServiceName = a.Service.Name,
                    StaffName = a.Staff != null ? a.Staff.FullName : null,
                    AppointmentDate = a.AppointmentDate.ToDateTime(TimeOnly.MinValue),
                    AppointmentTime = a.AppointmentTime.ToTimeSpan(),
                    Address = a.Address,
                    Status = a.Status,
                    CreatedAt = a.CreatedAt
                })
                .FirstOrDefaultAsync();
        }

        public async Task<int?> GetStaffIdByUserAsync(int userId)
        {
            var staff = await _context.Staff
                .Where(s => s.UserId == userId)
                .Select(s => (int?)s.StaffId)
                .FirstOrDefaultAsync();

            return staff;
        }

        /// <summary>
        /// Get the date range for a specific week within a month
        /// Logic: Week starts on Monday, week 1 is the week containing the 1st of the month
        /// Example for November 2025 (starts on Saturday):
        /// Week 1: Sat 1 - Sun 2 (partial week)
        /// Week 2: Mon 3 - Sun 9
        /// Week 3: Mon 10 - Sun 16
        /// Week 4: Mon 17 - Sun 23
        /// Week 5: Mon 24 - Sun 30
        /// </summary>
        private (DateOnly startDate, DateOnly endDate) GetWeekRangeInMonth(int year, int month, int week)
        {
            if (week < 1 || week > 6)
                throw new ArgumentException("Week must be between 1 and 6");

            var firstDayOfMonth = new DateOnly(year, month, 1);
            var lastDayOfMonth = new DateOnly(year, month, DateTime.DaysInMonth(year, month));

            // Find the Monday of the week containing the 1st day (or the 1st itself if it's a Monday)
            int daysFromMonday = ((int)firstDayOfMonth.DayOfWeek - (int)DayOfWeek.Monday + 7) % 7;
            var firstMonday = firstDayOfMonth.AddDays(-daysFromMonday);

            // If the first Monday is before the month starts, it's part of week 1
            // Calculate start and end of the requested week
            var weekStartDate = firstMonday.AddDays((week - 1) * 7);
            var weekEndDate = weekStartDate.AddDays(6);

            // Clamp to month boundaries
            DateOnly startDate = weekStartDate < firstDayOfMonth ? firstDayOfMonth : weekStartDate;
            DateOnly endDate = weekEndDate > lastDayOfMonth ? lastDayOfMonth : weekEndDate;

            // Validate that the week exists in this month
            if (startDate > lastDayOfMonth)
                throw new ArgumentException($"Week {week} does not exist in this month");

            return (startDate, endDate);
        }
    }
}
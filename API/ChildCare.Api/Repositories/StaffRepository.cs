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
     int staffId, string? customerName, int? month, int? week)
        {
            var query = _context.Appointments
                .Include(a => a.Service)
                .Include(a => a.User)
                .Include(a => a.Staff)
                .Where(a => a.StaffId == staffId && a.Status != "Cancelled");

            if (!string.IsNullOrEmpty(customerName))
                query = query.Where(a => a.User.FullName.Contains(customerName));

            if (month.HasValue)
                query = query.Where(a => a.AppointmentDate.Month == month.Value);

            if (week.HasValue && month.HasValue)
            {
                int currentYear = DateTime.Now.Year;

                var startOfWeek = GetStartOfWeek(currentYear, month.Value, week.Value);
                var endOfWeek = startOfWeek.AddDays(6);

                query = query.Where(a => a.AppointmentDate >= startOfWeek && a.AppointmentDate <= endOfWeek);
            }

            return await Task.FromResult(
                query.AsEnumerable()
                    .OrderByDescending(a => a.AppointmentDate.ToDateTime(TimeOnly.MinValue))
                    .Select(a => new AppointmentResponseDTO
                    {
                        AppointmentID = a.AppointmentId,
                        UserName = a.User.FullName,
                        ServiceName = a.Service.Name,
                        StaffName = a.StaffId != null ? a.Staff.FullName : null,
                        AppointmentDate = a.AppointmentDate.ToDateTime(a.AppointmentTime),
                        AppointmentTime = a.AppointmentTime.ToTimeSpan(),
                        Address = a.Address,
                        Status = a.Status,
                        CreatedAt = a.CreatedAt
                    })
                    .ToList()
            );
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
                    StaffName = a.Staff.FullName,
                    AppointmentDate = a.AppointmentDate.ToDateTime(TimeOnly.MinValue),
                    AppointmentTime = a.AppointmentTime.ToTimeSpan(),
                    Address = a.Address,
                    Status = a.Status,
                    CreatedAt = a.CreatedAt
                })
                .FirstOrDefaultAsync();
        }
        private DateOnly GetStartOfWeek(int year, int month, int week)
        {
            var firstDayOfMonth = new DateOnly(year, month, 1);

            int offset = ((int)DayOfWeek.Monday - (int)firstDayOfMonth.DayOfWeek + 7) % 7;
            var firstMonday = firstDayOfMonth.AddDays(offset);

            var startOfWeek = firstMonday.AddDays((week - 1) * 7);
            return startOfWeek;
        }
        public async Task<int?> GetStaffIdByUserAsync(int userId)
        {
            var staff = await _context.Staff
                .Where(s => s.UserId == userId)
                .Select(s => (int?)s.StaffId)
                .FirstOrDefaultAsync();

            return staff;
        }

    }
}

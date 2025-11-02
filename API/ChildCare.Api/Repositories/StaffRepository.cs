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
    }
}

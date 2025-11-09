using ChildCare.Api.DTOs;
using ChildCare.Api.Models;
using Microsoft.EntityFrameworkCore;

namespace ChildCare.Api.Repositories
{
    public class AppointmentRepository : IAppointmentRepository
    {
        private readonly ChildCareDbContext _context;

        public AppointmentRepository(ChildCareDbContext context)
        {
            _context = context;
        }

        public async Task<IEnumerable<AppointmentResponseDTO>> GetAllAsync()
        {
            return await _context.Appointments
                .Include(a => a.User)
                .Include(a => a.Service)
                .Include(a => a.Staff)
                .Select(a => new AppointmentResponseDTO
                {
                    AppointmentID = a.AppointmentId,
                    UserName = a.User.FullName,
                    ServiceName = a.Service.Name,
                    StaffName = a.Staff != null ? a.Staff.FullName : null,
                    StaffID = a.StaffId,
                    AppointmentDate = a.AppointmentDate.ToDateTime(TimeOnly.MinValue),
                    AppointmentTime = a.AppointmentTime.ToTimeSpan(),
                    Address = a.Address,
                    Status = a.Status,
                    CreatedAt = a.CreatedAt
                })
                .OrderByDescending(a => a.CreatedAt)
                .ToListAsync();
        }

        public async Task<IEnumerable<AppointmentResponseDTO>> GetByUserIdAsync(int userId)
        {
            var appointments = await _context.Appointments
    .Include(a => a.User)
    .Include(a => a.Service)
    .Include(a => a.Staff)
    .Where(a => a.UserId == userId)
    .ToListAsync(); 

            return appointments
                .Select(a => new AppointmentResponseDTO
                {
                    AppointmentID = a.AppointmentId,
                    UserName = a.User.FullName,
                    ServiceName = a.Service.Name,
                    StaffName = a.Staff?.FullName,
                    StaffID = a.StaffId,
                    AppointmentDate = a.AppointmentDate.ToDateTime(TimeOnly.MinValue),
                    AppointmentTime = a.AppointmentTime.ToTimeSpan(),
                    Address = a.Address,
                    Status = a.Status,
                    CreatedAt = a.CreatedAt
                })
                .OrderByDescending(a => a.AppointmentDate)
                .ThenByDescending(a => a.AppointmentTime)
                .ToList();

        }

        public async Task<AppointmentResponseDTO?> GetByIdAsync(int id)
        {
            var a = await _context.Appointments
                .Include(a => a.User)
                .Include(a => a.Service)
                .Include(a => a.Staff)
                .FirstOrDefaultAsync(a => a.AppointmentId == id);

            if (a == null) return null;

            return new AppointmentResponseDTO
            {
                AppointmentID = a.AppointmentId,
                UserName = a.User.FullName,
                ServiceName = a.Service.Name,
                StaffName = a.Staff?.FullName,
                StaffID = a.StaffId,
                AppointmentDate = a.AppointmentDate.ToDateTime(TimeOnly.MinValue),
                AppointmentTime = a.AppointmentTime.ToTimeSpan(),
                Address = a.Address,
                Status = a.Status,
                CreatedAt = a.CreatedAt
            };
        }

        public async Task<AppointmentResponseDTO> CreateAsync(AppointmentCreateDTO dto)
        {
            var service = await _context.Services.FindAsync(dto.ServiceID);
            if (service == null)
                throw new Exception("Service not found");

            var user = await _context.Users.FindAsync(dto.UserID);
            if (user == null)
                throw new Exception("User not found");

            Staff? staff = null;
            if (dto.StaffID > 0)
            {
                staff = await _context.Staff.FindAsync(dto.StaffID);
                if (staff == null)
                    throw new Exception("Staff not found");
            }

            var appointmentDate = DateOnly.FromDateTime(dto.AppointmentDate);
            var appointmentTime = TimeOnly.FromTimeSpan(dto.AppointmentTime);
            var newStart = appointmentTime;
            var newEnd = newStart.AddMinutes(service.DurationMinutes);

            var existingAppointments = await _context.Appointments
                .Include(a => a.Service)
                .Where(a => a.StaffId == dto.StaffID
                         && a.AppointmentDate == appointmentDate
                         && a.Status != "Cancelled")
                .ToListAsync();

            foreach (var existing in existingAppointments)
            {
                var existingStart = existing.AppointmentTime;
                var existingEnd = existingStart.AddMinutes(existing.Service.DurationMinutes);

                bool hasOverlap = newStart < existingEnd && newEnd > existingStart;

                if (hasOverlap)
                {
                    throw new Exception(
                        $"Staff is already busy during this time slot. " +
                        $"Existing appointment: {existingStart.ToString("HH:mm")} - {existingEnd.ToString("HH:mm")} " +
                        $"(Service: {existing.Service.Name}). " +
                        $"Your requested time: {newStart.ToString("HH:mm")} - {newEnd.ToString("HH:mm")}."
                    );
                }
            }

            var appointment = new Appointment
            {
                UserId = dto.UserID,
                ServiceId = dto.ServiceID,
                StaffId = dto.StaffID > 0 ? dto.StaffID : null,
                AppointmentDate = appointmentDate,
                AppointmentTime = appointmentTime,
                Address = dto.Address ?? string.Empty,
                Status = "Pending",
                CreatedAt = DateTime.Now
            };

            _context.Appointments.Add(appointment);
            await _context.SaveChangesAsync();

            return new AppointmentResponseDTO
            {
                AppointmentID = appointment.AppointmentId,
                UserName = user.FullName,
                ServiceName = service.Name,
                StaffName = staff?.FullName,
                StaffID = staff.StaffId,
                AppointmentDate = appointment.AppointmentDate.ToDateTime(TimeOnly.MinValue),
                AppointmentTime = appointment.AppointmentTime.ToTimeSpan(),
                Address = appointment.Address,
                Status = appointment.Status,
                CreatedAt = appointment.CreatedAt
            };
        }

        public async Task<bool> UpdateStatusAsync(int id, AppointmentUpdateStatusDTO dto)
        {
            var a = await _context.Appointments.FindAsync(id);
            if (a == null) return false;

            a.Status = dto.Status ?? a.Status;
            await _context.SaveChangesAsync();
            return true;
        }

        public async Task<bool> DeleteAsync(int id)
        {
            var a = await _context.Appointments.FindAsync(id);
            if (a == null) return false;

            _context.Appointments.Remove(a);
            await _context.SaveChangesAsync();
            return true;
        }

        public async Task<IEnumerable<string>> GetStatusesAsync()
        {
            return await _context.Appointments
                .Select(a => a.Status)
                .Distinct()
                .OrderBy(s => s)
                .ToListAsync();
        }
        public async Task<IEnumerable<AppointmentResponseDTO>> FilterAppointmentsAsync(
      string? userName,
      int? month,
      int? week,
      string? status,
      int? userId = null)
        {
            var query = _context.Appointments
                .Include(a => a.User)
                .Include(a => a.Service)
                .Include(a => a.Staff)
                .AsQueryable();

            if (userId.HasValue)
            {
                query = query.Where(a => a.UserId == userId.Value);
            }

            if (!string.IsNullOrEmpty(userName))
            {
                query = query.Where(a => a.User.FullName.Contains(userName));
            }

            if (month.HasValue)
            {
                query = query.Where(a => a.AppointmentDate.Month == month.Value);
            }

            if (week.HasValue)
            {
                query = query.Where(a => ((a.AppointmentDate.Day - 1) / 7 + 1) == week.Value);
            }

            if (!string.IsNullOrEmpty(status))
            {
                query = query.Where(a => a.Status == status);
            }

            return query
              .AsEnumerable()
              .Select(a => new AppointmentResponseDTO
              {
                  AppointmentID = a.AppointmentId,
                  UserName = a.User.FullName,
                  ServiceName = a.Service.Name,
                  StaffName = a.Staff?.FullName,
                  StaffID = a.StaffId,
                  AppointmentDate = a.AppointmentDate.ToDateTime(TimeOnly.MinValue),
                  AppointmentTime = a.AppointmentTime.ToTimeSpan(),
                  Address = a.Address,
                  Status = a.Status,
                  CreatedAt = a.CreatedAt
              })
              .OrderByDescending(a => a.AppointmentDate)
              .ThenByDescending(a => a.AppointmentTime)
              .ToList();
        }
        public async Task<bool> CancelAsync(int id, int userId)
        {
            var appointment = await _context.Appointments.FirstOrDefaultAsync(a => a.AppointmentId == id && a.UserId == userId);
            if (appointment == null)
                return false; 

            if (appointment.Status != "Pending")
                throw new Exception("You can only cancel appointments that are still pending.");

            appointment.Status = "Cancelled";
            await _context.SaveChangesAsync();
            return true;
        }



    }
}
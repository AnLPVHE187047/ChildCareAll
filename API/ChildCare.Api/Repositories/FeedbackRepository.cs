using ChildCare.Api.DTOs;
using ChildCare.Api.Models;
using Microsoft.EntityFrameworkCore;

namespace ChildCare.Api.Repositories
{
    public class FeedbackRepository : IFeedbackRepository
    {
        private readonly ChildCareDbContext _context;

        public FeedbackRepository(ChildCareDbContext context)
        {
            _context = context;
        }

        public async Task<IEnumerable<FeedbackResponseDTO>> GetAllAsync()
        {
            return await _context.Feedbacks
                .Include(f => f.User)
                .Include(f => f.Staff)
                .Select(f => new FeedbackResponseDTO
                {
                    FeedbackID = f.FeedbackId,
                    UserName = f.User.FullName,
                    StaffName = f.Staff != null ? f.Staff.FullName : null,
                    Rating = f.Rating,
                    Comment = f.Comment,
                    CreatedAt = f.CreatedAt
                })
                .ToListAsync();
        }

        public async Task<FeedbackResponseDTO?> GetByIdAsync(int id)
        {
            var f = await _context.Feedbacks
                .Include(x => x.User)
                .Include(x => x.Staff)
                .FirstOrDefaultAsync(x => x.FeedbackId == id);

            if (f == null) return null;

            return new FeedbackResponseDTO
            {
                FeedbackID = f.FeedbackId,
                UserName = f.User.FullName,
                StaffName = f.Staff?.FullName,
                Rating = f.Rating,
                Comment = f.Comment,
                CreatedAt = f.CreatedAt
            };
        }

        public async Task<FeedbackResponseDTO> CreateAsync(FeedbackCreateDTO dto)
        {
            var appointment = await _context.Appointments
                .Include(a => a.Staff)
                .FirstOrDefaultAsync(a =>
                    a.AppointmentId == dto.AppointmentID &&
                    a.UserId == dto.UserID &&
                    a.Status == "Completed");

            if (appointment == null)
                throw new Exception("Appointment not found or not completed yet.");

            bool alreadyFeedback = await _context.Feedbacks.AnyAsync(f =>
                f.AppointmentId == dto.AppointmentID);

            if (alreadyFeedback)
                throw new Exception("You have already given feedback for this appointment.");

            var f = new Feedback
            {
                UserId = dto.UserID,
                StaffId = dto.StaffID ?? appointment.StaffId,
                AppointmentId = dto.AppointmentID, 
                Rating = dto.Rating,
                Comment = dto.Comment,
                CreatedAt = DateTime.UtcNow
            };

            _context.Feedbacks.Add(f);
            await _context.SaveChangesAsync();

            return await GetByIdAsync(f.FeedbackId)
                ?? throw new Exception("Feedback creation failed.");
        }

        public async Task<bool> DeleteAsync(int id)
        {
            var f = await _context.Feedbacks.FindAsync(id);
            if (f == null) return false;

            _context.Feedbacks.Remove(f);
            await _context.SaveChangesAsync();
            return true;
        }
        public async Task<IEnumerable<AppointmentFeedbackDTO>> GetCompletedAppointmentsForFeedbackAsync(int userId)
        {
            var completedAppointments = await _context.Appointments
                .Include(a => a.Service)
                .Include(a => a.Staff)
                .Where(a => a.UserId == userId && a.Status == "Completed")
                .Select(a => new AppointmentFeedbackDTO
                {
                    AppointmentID = a.AppointmentId,
                    ServiceName = a.Service.Name,
                    StaffName = a.Staff != null ? a.Staff.FullName : "N/A",
                    AppointmentDate = a.AppointmentDate,
                    AppointmentTime = a.AppointmentTime,
                    // ✅ Kiểm tra theo AppointmentId thay vì UserId + StaffId
                    IsFeedbackGiven = _context.Feedbacks.Any(f => f.AppointmentId == a.AppointmentId)
                })
                .ToListAsync();

            return completedAppointments;
        }
        public async Task<IEnumerable<FeedbackResponseDTO_Staff>> GetFeedbacksByStaffIdAsync(int staffId)
        {
            return await (from f in _context.Feedbacks
                          join a in _context.Appointments on f.AppointmentId equals a.AppointmentId 
                          join s in _context.Services on a.ServiceId equals s.ServiceId
                          where f.StaffId == staffId
                          orderby f.CreatedAt descending
                          select new FeedbackResponseDTO_Staff
                          {
                              FeedbackID = f.FeedbackId,
                              UserName = f.User.FullName,
                              ServiceName = s.Name,
                              Rating = f.Rating,
                              Comment = f.Comment,
                              CreatedAt = f.CreatedAt ?? DateTime.Now,
                              AppointmentDate = a.AppointmentDate.ToDateTime(TimeOnly.MinValue)
                          })
                          .Distinct() 
                          .ToListAsync();
        }


        public async Task<double> GetAverageRatingByStaffIdAsync(int staffId)
        {
            var feedbacks = await _context.Feedbacks
                .Where(f => f.StaffId == staffId)
                .ToListAsync();

            if (!feedbacks.Any())
                return 0;

            return Math.Round(feedbacks.Average(f => f.Rating), 1);
        }
    }
}

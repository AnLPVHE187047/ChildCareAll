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
            bool hasCompletedAppointment = await _context.Appointments.AnyAsync(a =>
                a.UserId == dto.UserID &&
                a.StaffId == dto.StaffID &&
                a.Status == "Completed");

            if (!hasCompletedAppointment)
                throw new Exception("You can only feedback after completed appointments.");

            bool alreadyFeedback = await _context.Feedbacks.AnyAsync(f =>
                f.UserId == dto.UserID &&
                f.StaffId == dto.StaffID);

            if (alreadyFeedback)
                throw new Exception("You have already given feedback for this staff.");

            var f = new Feedback
            {
                UserId = dto.UserID,
                StaffId = dto.StaffID,
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
                    IsFeedbackGiven = _context.Feedbacks.Any(f =>
                        f.UserId == userId && f.StaffId == a.StaffId)
                })
                .ToListAsync();

            return completedAppointments;
        }
    }
}

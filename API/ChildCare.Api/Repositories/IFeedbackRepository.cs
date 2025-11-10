using ChildCare.Api.DTOs;

namespace ChildCare.Api.Repositories
{
    public interface IFeedbackRepository
    {
        Task<IEnumerable<FeedbackResponseDTO>> GetAllAsync();
        Task<FeedbackResponseDTO?> GetByIdAsync(int id);
        Task<FeedbackResponseDTO> CreateAsync(FeedbackCreateDTO dto);
        Task<bool> DeleteAsync(int id);

        // 🆕 Lấy các lịch completed mà user chưa feedback
        Task<IEnumerable<AppointmentFeedbackDTO>> GetCompletedAppointmentsForFeedbackAsync(int userId);
        Task<IEnumerable<FeedbackResponseDTO_Staff>> GetFeedbacksByStaffIdAsync(int staffId);
        Task<double> GetAverageRatingByStaffIdAsync(int staffId);
    }
}

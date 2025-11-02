using ChildCare.Api.DTOs;

namespace ChildCare.Api.Repositories
{
    public interface IFeedbackRepository
    {
        Task<IEnumerable<FeedbackResponseDTO>> GetAllAsync();
        Task<FeedbackResponseDTO?> GetByIdAsync(int id);
        Task<FeedbackResponseDTO> CreateAsync(FeedbackCreateDTO dto);
        Task<bool> DeleteAsync(int id);
    }
}

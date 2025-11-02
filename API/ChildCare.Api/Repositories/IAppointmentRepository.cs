using ChildCare.Api.DTOs;

namespace ChildCare.Api.Repositories
{
    public interface IAppointmentRepository
    {
        Task<IEnumerable<AppointmentResponseDTO>> GetAllAsync();
        Task<IEnumerable<AppointmentResponseDTO>> GetByUserIdAsync(int userId);
        Task<AppointmentResponseDTO?> GetByIdAsync(int id);
        Task<AppointmentResponseDTO> CreateAsync(AppointmentCreateDTO dto);
        Task<bool> UpdateStatusAsync(int id, AppointmentUpdateStatusDTO dto);
        Task<bool> DeleteAsync(int id);
        Task<IEnumerable<string>> GetStatusesAsync();
        Task<IEnumerable<AppointmentResponseDTO>> FilterAppointmentsAsync(
      string? userName,
      int? month,
      int? week,
      string? status,
      int? userId = null);


    }
}
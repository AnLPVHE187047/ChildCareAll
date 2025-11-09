using ChildCare.Api.DTOs;

namespace ChildCare.Api.Repositories
{
    public interface IStaffRepository
    {
        Task<IEnumerable<StaffDropdownDTO>> GetAllForDropdownAsync();
        Task<IEnumerable<AppointmentResponseDTO>> GetStaffScheduleAsync(int staffId, DateOnly date);

        Task<IEnumerable<AppointmentResponseDTO>> FilterStaffAppointmentsAsync(
        int staffId,
        string? customerName,
        int? month,
        int? week,
        int? dayOfWeek
    );

        Task<bool> UpdateAppointmentStatusAsync(int appointmentId, string status);


        Task<AppointmentResponseDTO?> GetAppointmentDetailAsync(int appointmentId);
        Task<int?> GetStaffIdByUserAsync(int userId);
    }
}

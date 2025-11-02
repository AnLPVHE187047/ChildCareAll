using ChildCare.Api.DTOs;

namespace ChildCare.Api.Repositories
{
    public interface IStaffRepository
    {
        Task<IEnumerable<StaffDropdownDTO>> GetAllForDropdownAsync();
        Task<IEnumerable<AppointmentResponseDTO>> GetStaffScheduleAsync(int staffId, DateOnly date);
    }
}

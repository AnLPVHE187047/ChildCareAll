using ChildCare.Api.DTOs;
using ChildCare.Api.Models;

namespace ChildCare.Api.Repositories
{
    public interface IServiceRepository
    {
        Task<IEnumerable<ServiceResponseDTO>> GetAllAsync();
        Task<ServiceResponseDTO?> GetByIdAsync(int id);
        Task<ServiceResponseDTO> CreateAsync(ServiceCreateDTO dto);
        Task<bool> UpdateAsync(int id, ServiceCreateDTO dto);
        Task<bool> DeleteAsync(int id);
    }
}

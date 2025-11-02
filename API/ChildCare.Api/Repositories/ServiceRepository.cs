using ChildCare.Api.DTOs;
using ChildCare.Api.Models;
using Microsoft.EntityFrameworkCore;

namespace ChildCare.Api.Repositories
{
    public class ServiceRepository : IServiceRepository
    {
        private readonly ChildCareDbContext _context;

        public ServiceRepository(ChildCareDbContext context)
        {
            _context = context;
        }

        public async Task<IEnumerable<ServiceResponseDTO>> GetAllAsync()
        {
            return await _context.Services
                .Select(s => new ServiceResponseDTO
                {
                    ServiceID = s.ServiceId,
                    Name = s.Name,
                    Description = s.Description,
                    Price = s.Price,
                    DurationMinutes = s.DurationMinutes,
                    CreatedAt = (DateTime)s.CreatedAt!,
                    ImageUrl = s.ImageUrl 
                })
                .ToListAsync();
        }


        public async Task<ServiceResponseDTO?> GetByIdAsync(int id)
        {
            var s = await _context.Services.FindAsync(id);
            if (s == null) return null;

            return new ServiceResponseDTO
            {
                ServiceID = s.ServiceId,
                Name = s.Name,
                Description = s.Description!,
                Price = s.Price,
                DurationMinutes = s.DurationMinutes,
                CreatedAt = (DateTime)s.CreatedAt!,
                ImageUrl = s.ImageUrl
            };
        }

        public async Task<ServiceResponseDTO> CreateAsync(ServiceCreateDTO dto)
        {
            var s = new Service
            {
                Name = dto.Name,
                Description = dto.Description,
                Price = dto.Price,
                DurationMinutes = dto.DurationMinutes,
                ImageUrl = dto.ImageUrl
            };
            _context.Services.Add(s);
            await _context.SaveChangesAsync();

            return new ServiceResponseDTO
            {
                ServiceID = s.ServiceId,
                Name = s.Name,
                Description = s.Description,
                Price = s.Price,
                DurationMinutes = s.DurationMinutes,
                CreatedAt = (DateTime)s.CreatedAt!,
                ImageUrl = s.ImageUrl
            };
        }

        public async Task<bool> UpdateAsync(int id, ServiceCreateDTO dto)
        {
            var s = await _context.Services.FindAsync(id);
            if (s == null) return false;

            s.Name = dto.Name;
            s.Description = dto.Description;
            s.Price = dto.Price;
            s.DurationMinutes = dto.DurationMinutes;
            s.ImageUrl = dto.ImageUrl; 
            await _context.SaveChangesAsync();
            return true;
        }

        public async Task<bool> DeleteAsync(int id)
        {
            var s = await _context.Services.FindAsync(id);
            if (s == null) return false;

            FileHelper.DeleteServiceImage(s.ImageUrl);

            _context.Services.Remove(s);
            await _context.SaveChangesAsync();
            return true;
        }
    }
}

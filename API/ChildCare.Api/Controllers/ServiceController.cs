using ChildCare.Api.DTOs;
using ChildCare.Api.Models;
using ChildCare.Api.Repositories;
using Microsoft.AspNetCore.Mvc;

namespace ChildCare.Api.Controllers
{
    [Route("api/services")]
    [ApiController]
    public class ServiceController : ControllerBase
    {
        private readonly IServiceRepository _repo;

        public ServiceController(IServiceRepository repo)
        {
            _repo = repo;
        }

        [HttpGet]
        public async Task<IActionResult> GetAll() => Ok(await _repo.GetAllAsync());

        [HttpGet("{id}")]
        public async Task<IActionResult> GetById(int id)
        {
            var item = await _repo.GetByIdAsync(id);
            return item == null ? NotFound() : Ok(item);
        }

        [HttpPost]
        public async Task<IActionResult> Create([FromForm] ServiceCreateDTO dto, IFormFile? ImageFile)
        {
            if (ImageFile != null)
            {
                var folderPath = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot/images/services");
                if (!Directory.Exists(folderPath))
                    Directory.CreateDirectory(folderPath);

                var fileName = Path.GetFileName(ImageFile.FileName);
                var filePath = Path.Combine(folderPath, fileName);

                using var stream = new FileStream(filePath, FileMode.Create);
                await ImageFile.CopyToAsync(stream);

                var baseUrl = $"{Request.Scheme}://{Request.Host}";
                dto.ImageUrl = $"{baseUrl}/images/services/{fileName}";
            }


            var item = await _repo.CreateAsync(dto);
            return CreatedAtAction(nameof(GetById), new { id = item.ServiceID }, item);
        }

        [HttpPut("{id}")]
        public async Task<IActionResult> Update(int id, [FromForm] ServiceCreateDTO dto, IFormFile? ImageFile)
        {
            var existingService = await _repo.GetByIdAsync(id);
            if (existingService == null)
                return NotFound();

            if (ImageFile != null)
            {

                FileHelper.DeleteServiceImage(existingService.ImageUrl);

                var folderPath = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot/images/services");
                if (!Directory.Exists(folderPath))
                    Directory.CreateDirectory(folderPath);

                var fileName = Path.GetFileName(ImageFile.FileName);
                var filePath = Path.Combine(folderPath, fileName);

                using var stream = new FileStream(filePath, FileMode.Create);
                await ImageFile.CopyToAsync(stream);

                var baseUrl = $"{Request.Scheme}://{Request.Host}";
                dto.ImageUrl = $"{baseUrl}/images/services/{fileName}";
            }
            else
            {
                dto.ImageUrl = existingService.ImageUrl;
            }

            var ok = await _repo.UpdateAsync(id, dto);
            return ok ? NoContent() : NotFound();
        }



        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(int id)
        {
            var ok = await _repo.DeleteAsync(id);
            return ok ? NoContent() : NotFound();
        }
        [HttpGet("search")]
        public async Task<IActionResult> Search([FromQuery] string? name, [FromQuery] decimal? minPrice, [FromQuery] decimal? maxPrice)
        {
            var all = await _repo.GetAllAsync();

            var filtered = all.AsQueryable();

            if (!string.IsNullOrEmpty(name))
                filtered = filtered.Where(s => s.Name.Contains(name, StringComparison.OrdinalIgnoreCase));

            if (minPrice.HasValue)
                filtered = filtered.Where(s => s.Price >= minPrice.Value);

            if (maxPrice.HasValue)
                filtered = filtered.Where(s => s.Price <= maxPrice.Value);

            return Ok(filtered);
        }
        [HttpGet("filter")]
        public async Task<IActionResult> Filter(
    [FromQuery] string? name,
    [FromQuery] decimal? minPrice,
    [FromQuery] decimal? maxPrice,
    [FromQuery] int? minDuration,
    [FromQuery] int? maxDuration,
    [FromQuery] string? sortBy,
    [FromQuery] string? sortOrder,
    [FromQuery] int page = 1,
    [FromQuery] int pageSize = 10)
        {
            var all = await _repo.GetAllAsync();
            var query = all.AsQueryable();

            if (!string.IsNullOrEmpty(name))
                query = query.Where(s => s.Name.Contains(name, StringComparison.OrdinalIgnoreCase));

            if (minPrice.HasValue)
                query = query.Where(s => s.Price >= minPrice.Value);

            if (maxPrice.HasValue)
                query = query.Where(s => s.Price <= maxPrice.Value);

            if (minDuration.HasValue)
                query = query.Where(s => s.DurationMinutes >= minDuration.Value);

            if (maxDuration.HasValue)
                query = query.Where(s => s.DurationMinutes <= maxDuration.Value);

            query = (sortBy?.ToLower(), sortOrder?.ToLower()) switch
            {
                ("price", "asc") => query.OrderBy(s => s.Price),
                ("price", "desc") => query.OrderByDescending(s => s.Price),
                ("duration", "asc") => query.OrderBy(s => s.DurationMinutes),
                ("duration", "desc") => query.OrderByDescending(s => s.DurationMinutes),
                _ => query.OrderBy(s => s.ServiceID)
            };

            var totalItems = query.Count();
            var totalPages = (int)Math.Ceiling(totalItems / (double)pageSize);
            var items = query.Skip((page - 1) * pageSize).Take(pageSize).ToList();

            return Ok(new
            {
                Page = page,
                PageSize = pageSize,
                TotalPages = totalPages,
                TotalItems = totalItems,
                Data = items
            });
        }


    }
}

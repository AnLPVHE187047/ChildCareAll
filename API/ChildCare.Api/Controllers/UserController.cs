using ChildCare.Api.DTOs;
using ChildCare.Api.Models;
using ChildCare.Api.Repositories;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace ChildCare.Api.Controllers
{
    [Route("api/users")]
    [ApiController]
    [Authorize]
    public class UserController : ControllerBase
    {
        private readonly IUserRepository _repo;
        private readonly IWebHostEnvironment _env;

        public UserController(IUserRepository repo, IWebHostEnvironment env)
        {
            _repo = repo;
            _env = env;
        }

        // GET: api/users
        [HttpGet]
        public async Task<IActionResult> GetAll()
        {
            var users = await _repo.GetAllAsync(Request);
            return Ok(users);
        }

        // GET: api/users/{id}
        [HttpGet("{id}")]
        public async Task<IActionResult> GetById(int id)
        {
            var user = await _repo.GetByIdAsync(id, Request);
            return user == null ? NotFound() : Ok(user);
        }

        // GET: api/users/search?query=&role=
        [HttpGet("search")]
        public async Task<IActionResult> Search([FromQuery] string? query = "", [FromQuery] string? role = "")
        {
            var users = await _repo.GetAllBySearch(query ?? "", role ?? "", Request);
            return Ok(users);
        }

        // POST: api/users
        [HttpPost]
        [AllowAnonymous]
        public async Task<IActionResult> Create([FromForm] UserCreateDTO dto)
        {
            var user = new User
            {
                FullName = dto.FullName,
                Email = dto.Email,
                Phone = dto.Phone,
                PasswordHash = BCrypt.Net.BCrypt.HashPassword(dto.Password),
                Role = dto.Role,
                CreatedAt = DateTime.Now,
                IsActive = dto.IsActive
            };

            var created = await _repo.AddUserWithImageAsync(user, dto.ImageFile, _env, Request);

            return CreatedAtAction(nameof(GetById), new { id = created.UserId }, created);
        }

        // PUT: api/users/{id}
        [HttpPut("{id}")]
        public async Task<IActionResult> Update(int id, [FromForm] UserUpdateDTO dto, IFormFile? imageFile)
        {
            var updated = await _repo.UpdateUserWithImageAsync(id, dto, imageFile, _env, Request);
            return updated ? NoContent() : NotFound();
        }
        [HttpPut("{id}/admin")]
        public async Task<IActionResult> UpdateByAdmin(int id, [FromForm] UserUpdateDTO dto, IFormFile? imageFile)
        {
            var updated = await _repo.UpdateUserWithImageAsyncByAdmin(id, dto, imageFile, _env, Request);
            return updated ? NoContent() : NotFound();
        }
        // PATCH: api/users/{id}/changepassword
        [HttpPatch("{id}/changepassword")]
        public async Task<IActionResult> ChangePassword(int id, [FromBody] ChangePasswordDTO dto)
        {
            var ok = await _repo.ChangePasswordAsync(id, dto.NewPassword);
            return ok ? NoContent() : NotFound();
        }

        // DELETE: api/users/{id}
        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(int id)
        {
            var ok = await _repo.DeleteAsync(id);
            return ok ? NoContent() : NotFound();
        }

        public class ChangePasswordDTO
        {
            public string NewPassword { get; set; } = null!;
        }
    }
}

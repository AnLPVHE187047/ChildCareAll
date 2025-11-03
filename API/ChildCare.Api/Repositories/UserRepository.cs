using ChildCare.Api.DTOs;
using ChildCare.Api.Models;
using Microsoft.EntityFrameworkCore;
using BCrypt.Net;

namespace ChildCare.Api.Repositories
{
    public class UserRepository : IUserRepository
    {
        private readonly ChildCareDbContext _context;

        public UserRepository(ChildCareDbContext context)
        {
            _context = context;
        }

        #region GET
        public async Task<IEnumerable<UserResponseDTO>> GetAllAsync(HttpRequest request)
        {
            var users = await _context.Users
                .Select(u => new UserResponseDTO
                {
                    UserID = u.UserId,
                    FullName = u.FullName,
                    Email = u.Email!,
                    Phone = u.Phone!,
                    Role = u.Role,
                    IsActive = u.IsActive ?? false,
                    CreatedAt = u.CreatedAt,
                    ImageUrl = u.ImageUrl
                }).ToListAsync();

            users.ForEach(u =>
            {
                if (!string.IsNullOrEmpty(u.ImageUrl) && !u.ImageUrl.StartsWith("http"))
                    u.ImageUrl = $"{request.Scheme}://{request.Host}{u.ImageUrl}";
            });

            return users;
        }

        public async Task<UserResponseDTO?> GetByIdAsync(int id, HttpRequest request)
        {
            var u = await _context.Users.FindAsync(id);
            if (u == null) return null;

            var dto = new UserResponseDTO
            {
                UserID = u.UserId,
                FullName = u.FullName,
                Email = u.Email!,
                Phone = u.Phone!,
                Role = u.Role,
                CreatedAt = u.CreatedAt,
                IsActive = u.IsActive ?? false,
                ImageUrl = u.ImageUrl
            };

            if (!string.IsNullOrEmpty(dto.ImageUrl) && !dto.ImageUrl.StartsWith("http"))
                dto.ImageUrl = $"{request.Scheme}://{request.Host}{dto.ImageUrl}";

            return dto;
        }

        public async Task<IEnumerable<UserResponseDTO>> GetAllBySearch(string searchQuery, string role, HttpRequest request)
        {
            var users = await _context.Users
                .Where(u =>
                    (string.IsNullOrEmpty(searchQuery) || u.FullName.Contains(searchQuery) || u.Email!.Contains(searchQuery)) &&
                    (string.IsNullOrEmpty(role) || u.Role.Contains(role))
                )
                .Select(u => new UserResponseDTO
                {
                    UserID = u.UserId,
                    FullName = u.FullName,
                    Email = u.Email!,
                    Phone = u.Phone!,
                    Role = u.Role,
                    CreatedAt = u.CreatedAt,
                    IsActive = u.IsActive ?? false,
                    ImageUrl = u.ImageUrl
                })
                .ToListAsync();

            users.ForEach(u =>
            {
                if (!string.IsNullOrEmpty(u.ImageUrl) && !u.ImageUrl.StartsWith("http"))
                    u.ImageUrl = $"{request.Scheme}://{request.Host}{u.ImageUrl}";
            });

            return users;
        }
        #endregion

        #region CREATE / UPDATE
        public async Task<User> AddUserAsync(User user)
        {
            user.IsActive ??= true;
            _context.Users.Add(user);
            await _context.SaveChangesAsync();
            return user;
        }

        public async Task<User> AddUserWithImageAsync(User user, IFormFile? imageFile, IWebHostEnvironment env, HttpRequest request)
        {
            user.IsActive ??= true;
            if (imageFile != null && imageFile.Length > 0)
            {
                string uploadsFolder = Path.Combine(env.WebRootPath, "uploads");
                if (!Directory.Exists(uploadsFolder)) Directory.CreateDirectory(uploadsFolder);

                string uniqueFileName = Guid.NewGuid().ToString() + Path.GetExtension(imageFile.FileName);
                string filePath = Path.Combine(uploadsFolder, uniqueFileName);

                using var fileStream = new FileStream(filePath, FileMode.Create);
                await imageFile.CopyToAsync(fileStream);

                user.ImageUrl = $"/uploads/{uniqueFileName}";
            }

            _context.Users.Add(user);
            await _context.SaveChangesAsync();

            if (!string.IsNullOrEmpty(user.ImageUrl))
                user.ImageUrl = $"{request.Scheme}://{request.Host}{user.ImageUrl}";

            return user;
        }

        // CHỈ UPDATE THÔNG TIN, KHÔNG UPDATE ẢNH
        public async Task<bool> UpdateAsync(int id, UserUpdateDTO dto)
        {   
            var user = await _context.Users.FindAsync(id);
            user.IsActive ??= true;
            if (user == null) return false;

            user.FullName = dto.FullName;
            user.Email = dto.Email;
            user.Phone = dto.Phone;
            user.Role = dto.Role;
            user.IsActive = dto.IsActive;

            await _context.SaveChangesAsync();
            return true;
        }

        // UPDATE THÔNG TIN + ẢNH
        public async Task<bool> UpdateUserWithImageAsync(int id, UserUpdateDTO dto, IFormFile? imageFile, IWebHostEnvironment env, HttpRequest request)
        {
            var user = await _context.Users.FindAsync(id);
            if (user == null) return false;

            user.FullName = dto.FullName;
            user.Email = dto.Email;
            user.Phone = dto.Phone;
            user.Role = dto.Role;
            user.IsActive = dto.IsActive;

            if (imageFile != null && imageFile.Length > 0)
            {
                string uploadsFolder = Path.Combine(env.WebRootPath, "uploads");
                if (!Directory.Exists(uploadsFolder))
                    Directory.CreateDirectory(uploadsFolder);

                // Xóa ảnh cũ
                if (!string.IsNullOrEmpty(user.ImageUrl))
                {
                    try
                    {
                        // ✅ Lấy tên file từ URL (dù là relative hay absolute)
                        string oldFileName = user.ImageUrl.Contains("/uploads/")
                            ? user.ImageUrl.Split("/uploads/").Last()
                            : Path.GetFileName(user.ImageUrl);

                        string oldFilePath = Path.Combine(uploadsFolder, oldFileName);
                        if (File.Exists(oldFilePath))
                        {
                            File.Delete(oldFilePath);
                        }
                    }
                    catch (Exception ex)
                    {
                        Console.WriteLine($"[WARN] Failed to delete old image: {ex.Message}");
                    }
                }
                
                string uniqueFileName = Guid.NewGuid().ToString() + Path.GetExtension(imageFile.FileName);
                string filePath = Path.Combine(uploadsFolder, uniqueFileName);

                using (var fileStream = new FileStream(filePath, FileMode.Create))
                {
                    await imageFile.CopyToAsync(fileStream);
                }

                user.ImageUrl = $"/uploads/{uniqueFileName}";
            }

            await _context.SaveChangesAsync();
            return true;
        }

        #endregion

        #region DELETE / PASSWORD
        public async Task<bool> DeleteAsync(int id)
        {
            var u = await _context.Users.FindAsync(id);
            if (u == null) return false;

            _context.Users.Remove(u);
            await _context.SaveChangesAsync();
            return true;
        }

        public async Task<bool> ChangePasswordAsync(int id, string newPassword)
        {
            var user = await _context.Users.FindAsync(id);
            if (user == null) return false;

            user.PasswordHash = BCrypt.Net.BCrypt.HashPassword(newPassword);
            await _context.SaveChangesAsync();
            return true;
        }
        #endregion

        #region AUTH
        public async Task<User?> AuthenticateAsync(string email, string password)
        {
            var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == email && u.IsActive == true);
            if (user == null) return null;

            bool verified = BCrypt.Net.BCrypt.Verify(password, user.PasswordHash);
            return verified ? user : null;
        }

        public async Task<User?> GetUserByEmailAsync(string email)
        {
            return await _context.Users.FirstOrDefaultAsync(u => u.Email == email);
        }
        #endregion
    }
}

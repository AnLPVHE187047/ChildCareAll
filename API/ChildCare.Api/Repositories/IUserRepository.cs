using ChildCare.Api.DTOs;
using ChildCare.Api.Models;
using Microsoft.AspNetCore.Mvc;

namespace ChildCare.Api.Repositories
{
    public interface IUserRepository
    {
        Task<IEnumerable<UserResponseDTO>> GetAllAsync(HttpRequest request);
        Task<UserResponseDTO?> GetByIdAsync(int id, HttpRequest request);
        Task<bool> UpdateAsync(int id, UserUpdateDTO dto);
        Task<bool> DeleteAsync(int id);
        Task<bool> ChangePasswordAsync(int id, string newPassword);
        Task<User?> AuthenticateAsync(string email, string password);
        Task<IEnumerable<UserResponseDTO>> GetAllBySearch(string searchQuery, string role, HttpRequest request);
        Task<User> AddUserAsync(User user);
        Task<User> AddUserWithImageAsync(User user, IFormFile? imageFile, IWebHostEnvironment env, HttpRequest request);
        Task<User?> GetUserByEmailAsync(string email);
        Task<bool> UpdateUserWithImageAsync(int id, UserUpdateDTO dto, IFormFile? imageFile, IWebHostEnvironment env, HttpRequest request);
        Task<bool> UpdateUserWithImageAsyncByAdmin(int id, UserUpdateDTO dto, IFormFile? imageFile, IWebHostEnvironment env, HttpRequest request);

    }
}

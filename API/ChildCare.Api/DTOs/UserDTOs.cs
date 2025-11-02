namespace ChildCare.Api.DTOs
{
  

    public class UserUpdateDTO
    {
        public string FullName { get; set; } = null!;
        public string Email { get; set; } = null!;
        public string Phone { get; set; } = null!;
        public string Role { get; set; } = "Parent";
        public bool IsActive { get; set; }
    }
    public class UserCreateDTO
    {
        public string FullName { get; set; } = null!;
        public string Email { get; set; } = null!;
        public string Phone { get; set; } = null!;
        public string Password { get; set; } = null!;
        public string Role { get; set; } = "Parent";
        public bool IsActive { get; set; } = true;

        public IFormFile? ImageFile { get; set; }
    }

    public class UserResponseDTO
    {
        public int UserID { get; set; }
        public string FullName { get; set; } = null!;
        public string Email { get; set; } = null!;
        public string Phone { get; set; } = null!;
        public string Role { get; set; } = null!;
        public DateTime? CreatedAt { get; set; }
        public bool IsActive { get; set; }
        public string? ImageUrl { get; set; }
    }
}

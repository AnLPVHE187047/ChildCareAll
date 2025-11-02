namespace ChildCareAdmin.Models
{
    public class ServiceUploadDTO
    {
        public int ServiceID { get; set; } 
        public string Name { get; set; } = null!;
        public string Description { get; set; } = null!;
        public decimal Price { get; set; }
        public int DurationMinutes { get; set; }
        public IFormFile? ImageFile { get; set; }
        public string? ImageUrl { get; set; }
    }

}

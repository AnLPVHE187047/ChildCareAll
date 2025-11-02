namespace ChildCare.Api.DTOs
{
    public class FeedbackCreateDTO
    {
        public int UserID { get; set; }
        public int? StaffID { get; set; }
        public int Rating { get; set; } 
        public string? Comment { get; set; }
    }

    public class FeedbackResponseDTO
    {
        public int FeedbackID { get; set; }
        public string? UserName { get; set; }
        public string? StaffName { get; set; }
        public int Rating { get; set; }
        public string? Comment { get; set; }
        public DateTime? CreatedAt { get; set; }
    }
}

namespace ChildCare.Api.DTOs
{
    public class AppointmentCreateDTO
    {
        public int UserID { get; set; }
        public int ServiceID { get; set; }
        public int StaffID { get; set; }
        public DateTime AppointmentDate { get; set; }
        public TimeSpan AppointmentTime { get; set; }
        public string? Address { get; set; }
    }

    public class AppointmentUpdateStatusDTO
    {
        public string? Status { get; set; } 
    }

    public class AppointmentResponseDTO
    {
        public int AppointmentID { get; set; }
        public string? UserName { get; set; }
        public string? ServiceName { get; set; }
        public string? StaffName { get; set; }
        public DateTime AppointmentDate { get; set; }
        public TimeSpan AppointmentTime { get; set; }
        public string? Address { get; set; }
        public string? Status { get; set; }
        public DateTime? CreatedAt { get; set; }
    }
  
}

namespace ChildCare.Api.DTOs
{
    public class AppointmentFeedbackDTO
    {
        public int AppointmentID { get; set; }
        public string ServiceName { get; set; } = string.Empty;
        public string StaffName { get; set; } = string.Empty;
        public DateOnly AppointmentDate { get; set; }
        public TimeOnly AppointmentTime { get; set; }
        public bool IsFeedbackGiven { get; set; }
    }
}

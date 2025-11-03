namespace ChildCare.Api.DTOs
{
    public class PaymentCreateDTO
    {
        public int AppointmentID { get; set; }
        public string PaymentMethod { get; set; } = string.Empty; 
    }
}

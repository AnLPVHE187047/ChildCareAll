namespace ChildCare.Api.DTOs
{
    public class RegisterResponse
    {
        public bool Success { get; set; }
        public string Message { get; set; }
        public int? UserID { get; set; }
    }
}

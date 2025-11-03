using ChildCare.Api.DTOs;

namespace ChildCare.Api.Repositories
{
    public interface IPaymentRepository
    {
        Task<string> CreatePaymentUrlAsync(PaymentCreateDTO dto, int userId);
        Task<bool> ProcessCallbackAsync(string transactionId, string status);
    }
}

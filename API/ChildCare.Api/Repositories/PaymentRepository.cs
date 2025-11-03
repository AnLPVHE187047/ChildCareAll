using ChildCare.Api.DTOs;
using ChildCare.Api.Models;
using Microsoft.EntityFrameworkCore;
using System.Web;
using System.Security.Cryptography;
using System.Text;

namespace ChildCare.Api.Repositories
{
    public class PaymentRepository : IPaymentRepository
    {
        private readonly ChildCareDbContext _context;
        private readonly IConfiguration _config;

        public PaymentRepository(ChildCareDbContext context, IConfiguration config)
        {
            _context = context;
            _config = config;
        }

        public async Task<string> CreatePaymentUrlAsync(PaymentCreateDTO dto, int userId)
        {
            var appointment = await _context.Appointments
                .Include(a => a.Service)
                .FirstOrDefaultAsync(a => a.AppointmentId == dto.AppointmentID && a.UserId == userId);

            if (appointment == null)
                throw new Exception("Appointment not found or not owned by this user.");

            if (appointment.Status == "Paid")
                throw new Exception("Appointment already paid.");

            var amount = appointment.Service.Price;
            var orderId = DateTime.Now.Ticks.ToString();

            // Cấu hình VNPay
            var vnpUrl = _config["VNPay:BaseUrl"];
            var vnpReturnUrl = _config["VNPay:ReturnUrl"];
            var vnpTmnCode = _config["VNPay:TmnCode"];
            var vnpHashSecret = _config["VNPay:HashSecret"];

            // Tạo danh sách tham số theo thứ tự
            var vnpParams = new SortedList<string, string>
            {
                { "vnp_Version", "2.1.0" },
                { "vnp_Command", "pay" },
                { "vnp_TmnCode", vnpTmnCode },
                { "vnp_Amount", ((int)(Math.Round(amount * 100))).ToString() },
                { "vnp_CreateDate", DateTime.Now.ToString("yyyyMMddHHmmss") },
                { "vnp_ExpireDate", DateTime.Now.AddMinutes(15).ToString("yyyyMMddHHmmss") },
                { "vnp_CurrCode", "VND" },
                { "vnp_IpAddr", "127.0.0.1" },
                { "vnp_Locale", "vn" },
                { "vnp_OrderInfo", $"Thanh toan don hang {dto.AppointmentID}" },
                { "vnp_OrderType", "other" },
                { "vnp_ReturnUrl", vnpReturnUrl },
                { "vnp_TxnRef", orderId }
            };

            // ✅ Tạo chuỗi signData đúng chuẩn (CHƯA URL encode)
            var signData = string.Join("&", vnpParams.Select(kvp => $"{kvp.Key}={kvp.Value}"));

            using var hmac = new HMACSHA512(Encoding.UTF8.GetBytes(vnpHashSecret));
            var hashBytes = hmac.ComputeHash(Encoding.UTF8.GetBytes(signData));
            var vnpSecureHash = BitConverter.ToString(hashBytes).Replace("-", "").ToUpper();

            // ✅ Encode từng giá trị cho URL hiển thị
            var queryString = string.Join("&", vnpParams.Select(kvp => $"{kvp.Key}={HttpUtility.UrlEncode(kvp.Value)}"));
            var finalUrl = $"{vnpUrl}?{queryString}&vnp_SecureHash={vnpSecureHash}";

            // Lưu payment
            var payment = new Payment
            {
                AppointmentId = dto.AppointmentID,
                UserId = userId,
                Amount = amount,
                PaymentMethod = "VNPay",
                TransactionId = orderId,
                PaymentStatus = "Pending"
            };
            _context.Payments.Add(payment);
            await _context.SaveChangesAsync();

            Console.WriteLine("🔗 VNPay URL: " + finalUrl);
            return finalUrl;
        }

        public async Task<bool> ProcessCallbackAsync(string transactionId, string status)
        {
            var payment = await _context.Payments.FirstOrDefaultAsync(p => p.TransactionId == transactionId);
            if (payment == null) return false;

            payment.PaymentStatus = status == "00" ? "Success" : "Failed";
            await _context.SaveChangesAsync();

            if (payment.PaymentStatus == "Success")
            {
                var appointment = await _context.Appointments.FindAsync(payment.AppointmentId);
                if (appointment != null)
                {
                    appointment.Status = "Paid";
                    await _context.SaveChangesAsync();
                }
            }

            return true;
        }
    }
}

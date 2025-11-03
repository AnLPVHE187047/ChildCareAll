using ChildCare.Api.DTOs;
using ChildCare.Api.Repositories;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace ChildCare.Api.Controllers
{
    [Route("api/payments")]
    [ApiController]
    [Authorize]
    public class PaymentController : ControllerBase
    {
        private readonly IPaymentRepository _repo;

        public PaymentController(IPaymentRepository repo)
        {
            _repo = repo;
        }

        [HttpPost("create")]
        public async Task<IActionResult> CreatePayment([FromBody] PaymentCreateDTO dto)
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            if (userIdClaim == null)
                return Unauthorized("Missing user ID");

            var userId = int.Parse(userIdClaim.Value);
            try
            {
                var url = await _repo.CreatePaymentUrlAsync(dto, userId);
                return Ok(new { paymentUrl = url });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [AllowAnonymous]
        [HttpGet("callback")]
        public async Task<IActionResult> PaymentCallback([FromQuery] string vnp_TxnRef, [FromQuery] string vnp_ResponseCode)
        {
            await _repo.ProcessCallbackAsync(vnp_TxnRef, vnp_ResponseCode);
            return Ok("Payment processed");
        }
    }
}

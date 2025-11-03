using ChildCare.Api.DTOs;
using ChildCare.Api.Repositories;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace ChildCare.Api.Controllers
{
    [Route("api/appointments")]
    [ApiController]
    [Authorize]
    public class AppointmentController : ControllerBase
    {
        private readonly IAppointmentRepository _repo;

        public AppointmentController(IAppointmentRepository repo)
        {
            _repo = repo;
        }

        [HttpGet]
        public async Task<IActionResult> GetAll() => Ok(await _repo.GetAllAsync());

        // API mới: Lấy appointments theo userId
        [HttpGet("user/{userId}")]
        public async Task<IActionResult> GetByUserId(int userId)
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            if (userIdClaim == null)
                return Unauthorized("Token missing user ID");

            // Kiểm tra user chỉ được xem appointment của chính mình
            if (int.Parse(userIdClaim.Value) != userId)
                return Forbid("You can only view your own appointments");

            var appointments = await _repo.GetByUserIdAsync(userId);
            return Ok(appointments);
        }

        // API mới: Lấy appointments của user hiện tại (từ token)
        [HttpGet("my-appointments")]
        public async Task<IActionResult> GetMyAppointments(
      [FromQuery] string? userName,
      [FromQuery] int? month,
      [FromQuery] int? week,
      [FromQuery] string? status)
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            if (userIdClaim == null)
                return Unauthorized("Token missing user ID");

            int userId = int.Parse(userIdClaim.Value);

            // Chỉ lấy appointments của user hiện tại
            var appointments = await _repo.FilterAppointmentsAsync(userName, month, week, status, userId);

            return Ok(appointments);
        }


        [HttpGet("{id}")]
        public async Task<IActionResult> GetById(int id)
        {
            var item = await _repo.GetByIdAsync(id);
            return item == null ? NotFound() : Ok(item);
        }

        [HttpPost]
        public async Task<IActionResult> CreateAppointment([FromBody] AppointmentCreateDTO appointment)
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            if (userIdClaim == null)
                return Unauthorized("Token missing user ID");

            appointment.UserID = int.Parse(userIdClaim.Value);

            try
            {
                var item = await _repo.CreateAsync(appointment);
                return CreatedAtAction(nameof(GetById), new { id = item.AppointmentID }, item);
            }
            catch (Exception ex)
            {
                return Conflict(new { message = ex.Message });
            }
        }

        [HttpPut("{id}/status")]
        public async Task<IActionResult> UpdateStatus(int id, AppointmentUpdateStatusDTO dto)
        {
            var ok = await _repo.UpdateStatusAsync(id, dto);
            return ok ? NoContent() : NotFound();
        }

        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(int id)
        {
            var ok = await _repo.DeleteAsync(id);
            return ok ? NoContent() : NotFound();
        }

        [HttpGet("statuses")]
        public async Task<IActionResult> GetStatuses()
        {
            var statuses = await _repo.GetStatusesAsync();
            return Ok(statuses);
        }
        [HttpGet("filter")]
        public async Task<IActionResult> Filter(
    [FromQuery] string? userName,
    [FromQuery] int? month,
    [FromQuery] int? week,
    [FromQuery] string? status)
        {
            var result = await _repo.FilterAppointmentsAsync(userName, month, week, status);
            return Ok(result);
        }
        [HttpPut("{id}/cancel")]
        public async Task<IActionResult> CancelAppointment(int id)
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            if (userIdClaim == null)
                return Unauthorized("Token missing user ID");

            int userId = int.Parse(userIdClaim.Value);

            try
            {
                var ok = await _repo.CancelAsync(id, userId);
                if (!ok)
                    return NotFound("Appointment not found or not owned by this user");

                return Ok(new { message = "Appointment cancelled successfully" });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }


    }
}
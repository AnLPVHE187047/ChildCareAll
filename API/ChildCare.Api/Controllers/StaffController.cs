using ChildCare.Api.Repositories;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace ChildCare.Api.Controllers
{
    [Route("api/staffs")]
    [ApiController]
    [Authorize]
    public class StaffController : Controller
    {
        private readonly IStaffRepository _repo;

        public StaffController(IStaffRepository repo)
        {
            _repo = repo;
        }

        [HttpGet]
        public async Task<IActionResult> GetAll()
        {
            var staffList = await _repo.GetAllForDropdownAsync();
            return Ok(staffList);
        }
        [HttpGet("{staffId}/schedule")]
        public async Task<IActionResult> GetStaffSchedule(int staffId, [FromQuery] string date)
        {
            if (string.IsNullOrEmpty(date))
                return BadRequest(new { message = "Date parameter is required" });

            if (!DateOnly.TryParse(date, out DateOnly appointmentDate))
                return BadRequest(new { message = "Invalid date format. Use yyyy-MM-dd" });

            var appointments = await _repo.GetStaffScheduleAsync(staffId, appointmentDate);

            return Ok(appointments);
        }
        [HttpGet("{staffId}/appointments")]
        public async Task<IActionResult> GetFilteredAppointments(
          int staffId,
          [FromQuery] string? customerName,
          [FromQuery] int? month,
          [FromQuery] int? week)
        {
            var appointments = await _repo.FilterStaffAppointmentsAsync(staffId, customerName, month, week);
            return Ok(appointments);
        }

        [HttpGet("appointments/{appointmentId}")]
        public async Task<IActionResult> GetAppointmentDetail(int appointmentId)
        {
            var appointment = await _repo.GetAppointmentDetailAsync(appointmentId);
            if (appointment == null)
                return NotFound(new { message = "Appointment not found" });

            return Ok(appointment);
        }
        [HttpGet("by-user/{userId}")]
        public async Task<IActionResult> GetStaffIdByUser(int userId)
        {
            var staffId = await _repo.GetStaffIdByUserAsync(userId);
            if (staffId == null)
                return NotFound(new { message = "No staff found for this user." });

            return Ok(staffId);
        }

    }
}

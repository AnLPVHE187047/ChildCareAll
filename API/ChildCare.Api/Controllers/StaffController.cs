using ChildCare.Api.Repositories;
using Microsoft.AspNetCore.Mvc;

namespace ChildCare.Api.Controllers
{
    [Route("api/staffs")]
    [ApiController]
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

    }
}

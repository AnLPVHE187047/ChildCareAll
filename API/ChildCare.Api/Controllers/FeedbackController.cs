using ChildCare.Api.DTOs;
using ChildCare.Api.Repositories;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace ChildCare.Api.Controllers
{
    [Route("api/feedbacks")]
    [ApiController]
    [Authorize]
    public class FeedbackController : ControllerBase
    {
        private readonly IFeedbackRepository _repo;

        public FeedbackController(IFeedbackRepository repo)
        {
            _repo = repo;
        }

        [HttpGet]
        public async Task<IActionResult> GetAll() => Ok(await _repo.GetAllAsync());

        [HttpGet("{id}")]
        public async Task<IActionResult> GetById(int id)
        {
            var item = await _repo.GetByIdAsync(id);
            return item == null ? NotFound() : Ok(item);
        }

        [HttpPost]
        public async Task<IActionResult> Create([FromBody] FeedbackCreateDTO dto)
        {
            try
            {
                var item = await _repo.CreateAsync(dto);
                return CreatedAtAction(nameof(GetById), new { id = item.FeedbackID }, item);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(int id)
        {
            var ok = await _repo.DeleteAsync(id);
            return ok ? NoContent() : NotFound();
        }
        [HttpGet("completed/{userId}")]
        public async Task<IActionResult> GetCompletedAppointmentsForFeedback(int userId)
        {
            var result = await _repo.GetCompletedAppointmentsForFeedbackAsync(userId);
            return Ok(result);
        }
        [HttpGet("staff/{staffId}")]
        public async Task<IActionResult> GetFeedbacksByStaff(int staffId)
        {
            var feedbacks = await _repo.GetFeedbacksByStaffIdAsync(staffId);
            return Ok(feedbacks);
        }

        [HttpGet("staff/{staffId}/rating")]
        public async Task<IActionResult> GetAverageRating(int staffId)
        {
            var rating = await _repo.GetAverageRatingByStaffIdAsync(staffId);
            return Ok(new { averageRating = rating });
        }

    }
}

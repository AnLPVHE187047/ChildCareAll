using ChildCare.Api.DTOs;
using ChildCare.Api.Repositories;
using Microsoft.AspNetCore.Mvc;

namespace ChildCare.Api.Controllers
{
    [Route("api/feedbacks")]
    [ApiController]
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
        public async Task<IActionResult> Create(FeedbackCreateDTO dto)
        {
            var item = await _repo.CreateAsync(dto);
            return CreatedAtAction(nameof(GetById), new { id = item.FeedbackID }, item);
        }

        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(int id)
        {
            var ok = await _repo.DeleteAsync(id);
            return ok ? NoContent() : NotFound();
        }
    }
}

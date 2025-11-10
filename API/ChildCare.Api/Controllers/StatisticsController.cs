// Controllers/StatisticsController.cs
using ChildCare.Api.Repositories;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace ChildCare.Api.Controllers
{
    [Route("api/statistics")]
    [ApiController]
    [Authorize(Roles = "Admin")]
    public class StatisticsController : ControllerBase
    {
        private readonly IStatisticsRepository _repo;

        public StatisticsController(IStatisticsRepository repo)
        {
            _repo = repo;
        }

        [HttpGet("dashboard")]
        public async Task<IActionResult> GetDashboardStats()
        {
            var stats = await _repo.GetDashboardStatsAsync();
            return Ok(stats);
        }

        [HttpGet("revenue-by-month")]
        public async Task<IActionResult> GetRevenueByMonth([FromQuery] int year)
        {
            if (year == 0)
                year = DateTime.Now.Year;

            var revenue = await _repo.GetRevenueByMonthAsync(year);
            return Ok(revenue);
        }
    }
}
using ChildCareAdmin.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace ChildCareAdmin.Pages.Appointments
{
    public class IndexModel : PageModel
    {
        private readonly IHttpClientFactory _clientFactory;
        public List<AppointmentDTO> Appointments { get; set; } = new();
        public List<string> StatusList { get; set; } = new();
        public IndexModel(IHttpClientFactory clientFactory)
        {
            _clientFactory = clientFactory;
        }

        public async Task OnGetAsync()
        {
            var client = _clientFactory.CreateClient("ChildCareApi");
            Appointments = await client.GetFromJsonAsync<List<AppointmentDTO>>("appointments");
            StatusList = await client.GetFromJsonAsync<List<string>>("appointments/statuses");
        }

        public async Task<IActionResult> OnPostUpdateStatusAsync(int id, string status)
        {
            var client = _clientFactory.CreateClient("ChildCareApi");
            await client.PutAsJsonAsync($"appointments/{id}/status", new { status });
            return RedirectToPage();
        }
    }
}

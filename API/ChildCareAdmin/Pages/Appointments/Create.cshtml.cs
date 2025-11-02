using ChildCareAdmin.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace ChildCareAdmin.Pages.Appointments
{
    public class CreateModel : PageModel
    {
        private readonly IHttpClientFactory _clientFactory;
        [BindProperty]
        public AppointmentCreateDTO Appointment { get; set; } = new();

        public CreateModel(IHttpClientFactory clientFactory)
        {
            _clientFactory = clientFactory;
        }

        public async Task<IActionResult> OnPostAsync()
        {
            var client = _clientFactory.CreateClient("ChildCareApi");
            await client.PostAsJsonAsync("appointments", Appointment);
            return RedirectToPage("Index");
        }
    }
}

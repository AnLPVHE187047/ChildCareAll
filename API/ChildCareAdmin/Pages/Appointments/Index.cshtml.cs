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

        [BindProperty(SupportsGet = true)]
        public string SearchUser { get; set; } = string.Empty;

        [BindProperty(SupportsGet = true)]
        public int? Month { get; set; }

        [BindProperty(SupportsGet = true)]
        public int? Week { get; set; }

        [BindProperty(SupportsGet = true)]
        public string Status { get; set; } = string.Empty;

        public IndexModel(IHttpClientFactory clientFactory)
        {
            _clientFactory = clientFactory;
        }

        public async Task OnGetAsync()
        {
            var client = _clientFactory.CreateClient("ChildCareApi");

            var token = HttpContext.Session.GetString("AuthToken");

            if (string.IsNullOrEmpty(token))
            {
                Response.Redirect("/Auth/Login");
                return;
            }

            client.DefaultRequestHeaders.Authorization =
                new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);

            var query = new List<string>();
            if (!string.IsNullOrEmpty(SearchUser)) query.Add($"userName={SearchUser}");
            if (Month.HasValue) query.Add($"month={Month.Value}");
            if (Week.HasValue) query.Add($"week={Week.Value}");
            if (!string.IsNullOrEmpty(Status)) query.Add($"status={Status}");
            var url = "appointments/filter";
            if (query.Any())
                url += "?" + string.Join("&", query);

            var appointmentsResponse = await client.GetAsync(url);
            if (appointmentsResponse.IsSuccessStatusCode)
            {
                Appointments = await appointmentsResponse.Content.ReadFromJsonAsync<List<AppointmentDTO>>();
            }

            var statusResponse = await client.GetAsync("appointments/statuses");
            if (statusResponse.IsSuccessStatusCode)
            {
                StatusList = await statusResponse.Content.ReadFromJsonAsync<List<string>>();
            }
        }

        public async Task<IActionResult> OnPostUpdateStatusAsync(int id, string status)
        {
            var client = _clientFactory.CreateClient("ChildCareApi");

            var token = HttpContext.Session.GetString("AuthToken");

            if (string.IsNullOrEmpty(token))
            {
                Response.Redirect("/Auth/Login");
                return Page();
            }

            client.DefaultRequestHeaders.Authorization =
                new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);

            var response = await client.PutAsJsonAsync($"appointments/{id}/status", new { status });

            if (!response.IsSuccessStatusCode)
            {
                TempData["ErrorMessage"] = "Failed to update status. Please try again.";
            }

            return RedirectToPage(new { SearchUser, Month, Week, Status });
        }
    }
}

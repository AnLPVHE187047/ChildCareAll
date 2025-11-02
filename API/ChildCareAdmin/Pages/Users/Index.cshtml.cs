using ChildCareAdmin.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using System.Net.Http.Headers;

namespace ChildCareAdmin.Pages.Users
{
    public class IndexModel : PageModel
    {
        private readonly IHttpClientFactory _clientFactory;
        public List<UserDTO> Users { get; set; } = new();

        [BindProperty(SupportsGet = true)]
        public string SearchQuery { get; set; } = "";

        [BindProperty(SupportsGet = true)]
        public string RoleFilter { get; set; } = "";

        public IndexModel(IHttpClientFactory clientFactory)
        {
            _clientFactory = clientFactory;
        }

        public async Task OnGetAsync()
        {
            var token = HttpContext.Session.GetString("AuthToken");
            var role = HttpContext.Session.GetString("UserRole");

            if (string.IsNullOrEmpty(token))
            {
                Response.Redirect("/Auth/Login");
                return;
            }

            if (string.IsNullOrEmpty(role) || role != "Admin")
            {
                TempData["ErrorMessage"] = "Access denied. Admin only.";
                Response.Redirect("/Auth/Login");
                return;
            }

            var client = _clientFactory.CreateClient("ChildCareApi");
            client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);

            string url = "users";

            if (!string.IsNullOrEmpty(SearchQuery) || !string.IsNullOrEmpty(RoleFilter))
            {
                url = $"users/search?query={SearchQuery}&role={RoleFilter}";
            }

            var users = await client.GetFromJsonAsync<List<UserDTO>>(url);
            if (users != null)
            {
                foreach (var u in users)
                {
                    if (!string.IsNullOrEmpty(u.ImageUrl) && !u.ImageUrl.StartsWith("http"))
                    {
                        var scheme = Request.Scheme;
                        var host = Request.Host.Value;
                        u.ImageUrl = $"{scheme}://{host}{u.ImageUrl}";
                    }
                }

                Users = users;
            }
        }
    }
}

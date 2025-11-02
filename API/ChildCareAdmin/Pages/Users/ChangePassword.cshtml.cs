using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using System.Net.Http.Headers;

namespace ChildCareAdmin.Pages.Users
{
    public class ChangePasswordModel : PageModel
    {
        private readonly IHttpClientFactory _clientFactory;
        public ChangePasswordModel(IHttpClientFactory clientFactory) => _clientFactory = clientFactory;

        [BindProperty]
        public string NewPassword { get; set; } = null!;

        public async Task<IActionResult> OnPostAsync(int id)
        {
            var client = _clientFactory.CreateClient("ChildCareApi");

            var token = HttpContext.Session.GetString("AuthToken");
            if (!string.IsNullOrEmpty(token))
            {
                client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);
            }

            await client.PatchAsJsonAsync($"users/{id}/changepassword", new { NewPassword });
            return RedirectToPage("Index");
        }
    }

}

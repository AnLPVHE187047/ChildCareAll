using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Threading.Tasks;

namespace ChildCareAdmin.Pages.Auth
{
    public class LoginModel : PageModel
    {
        private readonly IHttpClientFactory _clientFactory;
        public LoginModel(IHttpClientFactory clientFactory)
        {
            _clientFactory = clientFactory;
        }

        [BindProperty] public string Email { get; set; }
        [BindProperty] public string Password { get; set; }
        public string ErrorMessage { get; set; }

        public async Task<IActionResult> OnPostAsync()
        {
            var client = _clientFactory.CreateClient("ChildCareApi");

            var loginRequest = new { Email = Email, Password = Password };
            var response = await client.PostAsJsonAsync("auth/login", loginRequest);

            if (!response.IsSuccessStatusCode)
            {
                ErrorMessage = "Invalid email or password.";
                return Page();
            }

            var result = await response.Content.ReadFromJsonAsync<LoginResponse>();
            if (result == null || string.IsNullOrEmpty(result.Token))
            {
                ErrorMessage = "Login failed.";
                return Page();
            }
            else if (result.Role!="Admin")
            {
                ErrorMessage = "Do not access this page";
                return Page();
            }
                HttpContext.Session.SetString("AuthToken", result.Token);
            HttpContext.Session.SetString("UserRole", result.Role ?? "");
            return RedirectToPage("/Users/Index");
        }

        public class LoginResponse
        {
            public string Token { get; set; }
            public string FullName { get; set; }
            public string Role { get; set; }
        }
    }
}

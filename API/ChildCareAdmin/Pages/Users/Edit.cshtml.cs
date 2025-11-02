using ChildCareAdmin.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace ChildCareAdmin.Pages.Users
{
    public class EditModel : PageModel
    {
        private readonly IHttpClientFactory _clientFactory;

        public EditModel(IHttpClientFactory clientFactory)
        {
            _clientFactory = clientFactory;
        }

        [BindProperty]
        public UserUpdateDTO Input { get; set; } = new();

        [BindProperty]
        public IFormFile? ImageFile { get; set; }

        public string? CurrentImageUrl { get; set; }

        public async Task<IActionResult> OnGetAsync(int id)
        {
            var token = HttpContext.Session.GetString("AuthToken");
            if (string.IsNullOrEmpty(token))
                return RedirectToPage("/Auth/Login");

            var client = _clientFactory.CreateClient("ChildCareApi");
            client.DefaultRequestHeaders.Authorization =
                new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);

            var user = await client.GetFromJsonAsync<UserDTO>($"users/{id}");
            if (user == null) return NotFound();

            Input = new UserUpdateDTO
            {
                FullName = user.FullName,
                Email = user.Email,
                Phone = user.Phone,
                Role = user.Role,
                IsActive = user.IsActive
            };

            CurrentImageUrl = user.ImageUrl;
            return Page();
        }

        public async Task<IActionResult> OnPostAsync(int id)
        {
            if (!ModelState.IsValid)
                return Page();

            var token = HttpContext.Session.GetString("AuthToken");
            if (string.IsNullOrEmpty(token))
                return RedirectToPage("/Auth/Login");

            var client = _clientFactory.CreateClient("ChildCareApi");
            client.DefaultRequestHeaders.Authorization =
                new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);

            var content = new MultipartFormDataContent();
            content.Add(new StringContent(Input.FullName), "FullName");
            content.Add(new StringContent(Input.Email), "Email");
            content.Add(new StringContent(Input.Phone), "Phone");
            content.Add(new StringContent(Input.Role), "Role");
            content.Add(new StringContent(Input.IsActive.ToString()), "IsActive");

            if (ImageFile != null)
            {
                var streamContent = new StreamContent(ImageFile.OpenReadStream());
                streamContent.Headers.ContentType = new System.Net.Http.Headers.MediaTypeHeaderValue(ImageFile.ContentType);
                content.Add(streamContent, "imageFile", ImageFile.FileName);
            }

            var response = await client.PutAsync($"users/{id}", content);

            if (response.IsSuccessStatusCode)
                return RedirectToPage("Index");

            ModelState.AddModelError(string.Empty, "Failed to update user.");
            return Page();
        }
    }

    public class UserUpdateDTO
    {
        public string FullName { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;
        public string Phone { get; set; } = string.Empty;
        public string Role { get; set; } = "Parent";
        public bool IsActive { get; set; } = true;
    }
}

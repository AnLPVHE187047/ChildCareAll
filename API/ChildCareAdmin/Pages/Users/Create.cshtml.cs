using ChildCareAdmin.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using System.Net.Http.Headers;

namespace ChildCareAdmin.Pages.Users
{
    public class CreateModel : PageModel
    {
        private readonly IHttpClientFactory _clientFactory;
        public CreateModel(IHttpClientFactory clientFactory)
        {
            _clientFactory = clientFactory;
        }

        [BindProperty]
        public UserCreateDTO User { get; set; } = new();

        public async Task<IActionResult> OnPostAsync()
        {
            if (!ModelState.IsValid) return Page();

            var client = _clientFactory.CreateClient("ChildCareApi");

            var content = new MultipartFormDataContent
            {
                { new StringContent(User.FullName), "FullName" },
                { new StringContent(User.Email), "Email" },
                { new StringContent(User.Phone), "Phone" },
                { new StringContent(User.Password), "Password" },
                { new StringContent(User.Role), "Role" }
            };

            if (User.ImageFile != null)
            {
                var stream = User.ImageFile.OpenReadStream();
                content.Add(new StreamContent(stream)
                {
                    Headers = { ContentDisposition = new ContentDispositionHeaderValue("form-data")
                        { Name = "ImageFile", FileName = User.ImageFile.FileName } }
                }, "ImageFile", User.ImageFile.FileName);
            }

            var res = await client.PostAsync("users", content);
            if (res.IsSuccessStatusCode)
                return RedirectToPage("Index");

            TempData["ErrorMessage"] = "Failed to create user.";
            return Page();
        }
    }
}

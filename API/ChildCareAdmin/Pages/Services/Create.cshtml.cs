// CreateModel.cs
using ChildCareAdmin.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using System.Net.Http.Headers;

namespace ChildCareAdmin.Pages.Services
{
    public class CreateModel : PageModel
    {
        private readonly IHttpClientFactory _clientFactory;
        [BindProperty]
        public ServiceUploadDTO Service { get; set; } = new();

        public CreateModel(IHttpClientFactory clientFactory)
        {
            _clientFactory = clientFactory;
        }

        public async Task<IActionResult> OnPostAsync()
        {
            if (!ModelState.IsValid) return Page();

            var client = _clientFactory.CreateClient("ChildCareApi");

            var form = new MultipartFormDataContent();
            form.Add(new StringContent(Service.Name), "Name");
            form.Add(new StringContent(Service.Description), "Description");
            form.Add(new StringContent(Service.Price.ToString()), "Price");
            form.Add(new StringContent(Service.DurationMinutes.ToString()), "DurationMinutes");

            if (Service.ImageFile != null)
            {
                var streamContent = new StreamContent(Service.ImageFile.OpenReadStream());
                streamContent.Headers.ContentType = new MediaTypeHeaderValue(Service.ImageFile.ContentType);
                form.Add(streamContent, "ImageFile", Service.ImageFile.FileName);
            }

            var response = await client.PostAsync("services", form);
            if (response.IsSuccessStatusCode)
                return RedirectToPage("Index");

            ModelState.AddModelError("", "Failed to create service");
            return Page();
        }
    }
}

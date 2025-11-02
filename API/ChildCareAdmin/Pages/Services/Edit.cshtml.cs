// EditModel.cs
using ChildCareAdmin.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using System.Net.Http.Headers;

namespace ChildCareAdmin.Pages.Services
{
    public class EditModel : PageModel
    {
        private readonly IHttpClientFactory _clientFactory;
        [BindProperty]
        public ServiceUploadDTO Service { get; set; } = new();

        public EditModel(IHttpClientFactory clientFactory)
        {
            _clientFactory = clientFactory;
        }

        public async Task OnGetAsync(int id)
        {
            var client = _clientFactory.CreateClient("ChildCareApi");
            var service = await client.GetFromJsonAsync<ServiceDTO>($"services/{id}");
            if (service != null)
            {
                Service = new ServiceUploadDTO
                {
                    ServiceID = service.ServiceID,
                    Name = service.Name,
                    Description = service.Description,
                    Price = service.Price,
                    DurationMinutes = service.DurationMinutes,
                    ImageUrl = service.ImageUrl
                };
            }
        }

        public async Task<IActionResult> OnPostAsync()
        {
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

            var response = await client.PutAsync($"services/{Service.ServiceID}", form);
            if (response.IsSuccessStatusCode)
                return RedirectToPage("Index");

            ModelState.AddModelError("", "Failed to update service");
            return Page();
        }
    }
}

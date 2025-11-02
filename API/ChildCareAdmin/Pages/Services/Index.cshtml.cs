using ChildCareAdmin.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace ChildCareAdmin.Pages.Services
{
    public class IndexModel : PageModel
    {
        private readonly IHttpClientFactory _clientFactory;
        public IndexModel(IHttpClientFactory clientFactory) => _clientFactory = clientFactory;

        // Filter + Sort + Paging
        [BindProperty(SupportsGet = true)] public string? Name { get; set; }
        [BindProperty(SupportsGet = true)] public decimal? MinPrice { get; set; }
        [BindProperty(SupportsGet = true)] public decimal? MaxPrice { get; set; }
        [BindProperty(SupportsGet = true)] public int? MinDuration { get; set; }
        [BindProperty(SupportsGet = true)] public int? MaxDuration { get; set; }
        [BindProperty(SupportsGet = true)] public string? SortBy { get; set; }
        [BindProperty(SupportsGet = true)] public string? SortOrder { get; set; }
        [BindProperty(SupportsGet = true)] public int Page { get; set; } = 1;
        [BindProperty(SupportsGet = true)] public int PageSize { get; set; } = 10;

        public List<ServiceDTO> Services { get; set; } = new();
        public int TotalPages { get; set; }

        public async Task OnGetAsync()
        {
            var client = _clientFactory.CreateClient("ChildCareApi");
            var query = new List<string>();
            if (!string.IsNullOrEmpty(Name)) query.Add($"name={Name}");
            if (MinPrice.HasValue) query.Add($"minPrice={MinPrice}");
            if (MaxPrice.HasValue) query.Add($"maxPrice={MaxPrice}");
            if (MinDuration.HasValue) query.Add($"minDuration={MinDuration}");
            if (MaxDuration.HasValue) query.Add($"maxDuration={MaxDuration}");
            if (!string.IsNullOrEmpty(SortBy)) query.Add($"sortBy={SortBy}");
            if (!string.IsNullOrEmpty(SortOrder)) query.Add($"sortOrder={SortOrder}");
            query.Add($"page={Page}");
            query.Add($"pageSize={PageSize}");

            var url = "services/filter?" + string.Join("&", query);

            var response = await client.GetFromJsonAsync<FilterResponse>(url);
            Services = response?.Data ?? new List<ServiceDTO>();
            TotalPages = response?.TotalPages ?? 1;
        }

        public async Task<IActionResult> OnPostDeleteAsync(int id)
        {
            var client = _clientFactory.CreateClient("ChildCareApi");
            await client.DeleteAsync($"services/{id}");
            return RedirectToPage();
        }

        public class FilterResponse
        {
            public int Page { get; set; }
            public int PageSize { get; set; }
            public int TotalPages { get; set; }
            public int TotalItems { get; set; }
            public List<ServiceDTO> Data { get; set; } = new();
        }
    }
}

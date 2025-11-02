namespace ChildCare.Api.Repositories
{
    
        public static class FileHelper
        {
            public static void DeleteServiceImage(string? imageUrl)
            {
                if (string.IsNullOrEmpty(imageUrl))
                {
                    Console.WriteLine("⚠️ ImageUrl is null or empty");
                    return;
                }

                try
                {
                    Console.WriteLine($"🔍 Original ImageUrl: {imageUrl}");

                    var decodedUrl = Uri.UnescapeDataString(imageUrl);
                    Console.WriteLine($"🔍 Decoded URL: {decodedUrl}");

                    string fileName;
                    if (Uri.TryCreate(decodedUrl, UriKind.Absolute, out var uri))
                    {
                        fileName = Path.GetFileName(uri.LocalPath);
                    }
                    else
                    {
                        fileName = Path.GetFileName(decodedUrl);
                    }

                    Console.WriteLine($"🔍 Extracted fileName: {fileName}");

                    var folderPath = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot/images/services");
                    var filePath = Path.Combine(folderPath, fileName);

                    Console.WriteLine($"🔍 Full file path: {filePath}");
                    Console.WriteLine($"🔍 File exists: {File.Exists(filePath)}");

                    if (File.Exists(filePath))
                    {
                        File.Delete(filePath);
                        Console.WriteLine($"✅ Deleted image: {filePath}");
                    }
                    else
                    {
                        Console.WriteLine($"⚠️ Image not found at: {filePath}");

                        if (Directory.Exists(folderPath))
                        {
                            var files = Directory.GetFiles(folderPath);
                            Console.WriteLine($"📁 Files in folder:");
                            foreach (var file in files)
                            {
                                Console.WriteLine($"   - {Path.GetFileName(file)}");
                            }
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"❌ Error deleting image: {ex.Message}");
                    Console.WriteLine($"❌ Stack trace: {ex.StackTrace}");
                }
            }
        } 
}

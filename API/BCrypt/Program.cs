using System;
using BCrypt.Net;

public class Program
{
    public static void Main()
    {
        var hash = BCrypt.Net.BCrypt.HashPassword("123456");
        Console.WriteLine(hash);
    }
}

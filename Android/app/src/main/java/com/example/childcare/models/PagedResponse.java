package com.example.childcare.models;

import java.util.List;

public class PagedResponse<T> {
    public int Page;
    public int PageSize;
    public int TotalPages;
    public int TotalItems;
    public List<T> Data;
}


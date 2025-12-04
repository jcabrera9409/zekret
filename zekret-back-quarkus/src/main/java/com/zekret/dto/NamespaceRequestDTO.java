package com.zekret.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NamespaceRequestDTO (
    @NotBlank(message = "Name cannot be blank")
    @Size(min=1, max = 100, message = "Name cannot exceed 100 characters")
    String name,

    @NotBlank(message = "Description cannot be blank")
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    String description
) { }

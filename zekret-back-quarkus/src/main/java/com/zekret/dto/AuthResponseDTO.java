package com.zekret.dto;

public record AuthResponseDTO(String access_token, String refresh_token, String message) {
}
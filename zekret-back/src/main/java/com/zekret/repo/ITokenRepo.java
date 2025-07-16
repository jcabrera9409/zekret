package com.zekret.repo;

import com.zekret.model.Token;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITokenRepo extends IGenericRepo<Token, Long> {
    Page<Token> findAll(Pageable pageable);
    Optional<Token> findByAccessToken(String accessToken);
    Optional<Token> findByRefreshToken(String refreshToken);
    List<Token> findByUserIdAndLoggedOutFalse(Long idUsuario);
}

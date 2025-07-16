package com.zekret.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zekret.model.User;

import jakarta.transaction.Transactional;

public interface IUserRepo extends IGenericRepo<User, Long> {
	Optional<User> findByEmailOrUsername(String email, String username);
	@Transactional
	@Modifying
	@Query("UPDATE User u SET u.password = :password WHERE u.id = :id")
	int updatePasswordById(@Param("id") Long id, @Param("password") String password);
}

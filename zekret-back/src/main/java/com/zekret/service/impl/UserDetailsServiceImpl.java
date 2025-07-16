package com.zekret.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.zekret.repo.IUserRepo;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private IUserRepo repo;

	@Override
	public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
		return repo.findByEmailOrUsername(correo, "")
				.orElseThrow(() -> new UsernameNotFoundException("Email not found: " + correo));
	}
	
}
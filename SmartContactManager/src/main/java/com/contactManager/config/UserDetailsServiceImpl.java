package com.contactManager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.contactManager.dao.UserRepository;
import com.contactManager.entities.User;

public class UserDetailsServiceImpl implements UserDetailsService {
	
	@Autowired
	private UserRepository userRepo;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		User user = userRepo.getUserByEmail(username);
		
		if(user==null)
		{
			throw new UsernameNotFoundException("Could not found user !!");
		}
		
		CustomUserdetails customUserdetails = new CustomUserdetails(user);
		return customUserdetails;
	}

}

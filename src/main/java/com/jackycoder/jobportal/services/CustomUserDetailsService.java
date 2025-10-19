package com.jackycoder.jobportal.services;

import com.jackycoder.jobportal.entity.Users;
import com.jackycoder.jobportal.repository.UsersRepository;
import com.jackycoder.jobportal.util.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UsersRepository usersRepository;

    //constructor injection
    @Autowired
    public CustomUserDetailsService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    //Tell Spring Security how to retrieve a user from the database
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //Since we're using "Optional", then I have the "orElse".
        //So, if we don't find that given person, I'm going to throw an exception and I'll throw
        //this new "UsernameNotFoundException"
        Users user = usersRepository
                .findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Could not found user"));

        return new CustomUserDetails(user);
    }
}

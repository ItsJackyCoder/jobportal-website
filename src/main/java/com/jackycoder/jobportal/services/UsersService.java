package com.jackycoder.jobportal.services;

import com.jackycoder.jobportal.entity.JobSeekerProfile;
import com.jackycoder.jobportal.entity.RecruiterProfile;
import com.jackycoder.jobportal.entity.Users;
import com.jackycoder.jobportal.repository.JobSeekerProfileRepository;
import com.jackycoder.jobportal.repository.RecruiterProfileRepository;
import com.jackycoder.jobportal.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class UsersService {
    private final UsersRepository usersRepository;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;

    private final PasswordEncoder passwordEncoder;

    //constructor injection
    @Autowired //optional(因為只有一個constructor)
    public UsersService(UsersRepository usersRepository,
                        JobSeekerProfileRepository jobSeekerProfileRepository,
                        RecruiterProfileRepository recruiterProfileRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.jobSeekerProfileRepository = jobSeekerProfileRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;

        //The actual encoder that we'll inject is the "BCrypt" password encoder that we
        //set up in our configuration file
        this.passwordEncoder = passwordEncoder;
    }

    //add a new user
    public Users addNew(Users users){
        users.setActive(true);
        users.setRegistrationDate(new Date(System.currentTimeMillis()));

        //During registration ... encrypt user password
        users.setPassword(passwordEncoder.encode(users.getPassword()));

        //先把users存起來,DB會生成「主鍵」並回填到users物件!!!
        //接著才去存RecruiterProfile/JobSeekerProfile,這樣可以確保關聯的主鍵一定存在
        Users savedUser = usersRepository.save(users); //這個回傳的物件是「持久態(managed)」,而且主鍵已經被填好!

        int userTypeId = users.getUserTypeId().getUserTypeId();

        if(userTypeId == 1){ //1: recruiter
            recruiterProfileRepository.save(new RecruiterProfile(savedUser));
        }else{
            jobSeekerProfileRepository.save(new JobSeekerProfile(savedUser));
        }

        return savedUser;
    }

    public Optional<Users> getUserByEmail(String email){
        return usersRepository.findByEmail(email);
    }

    public Object getCurrentUserProfile() {
        //get the current logged-in user from the "SecurityContextHolder"
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(!(authentication instanceof AnonymousAuthenticationToken)){
            String username = authentication.getName();

            Users users = usersRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Could not find user"));

            int userId = users.getUserId();

            if(authentication.getAuthorities()
                    .contains(new SimpleGrantedAuthority("Recruiter"))){

                return recruiterProfileRepository
                        .findById(userId).orElse(new RecruiterProfile());
            }else{

                return jobSeekerProfileRepository
                        .findById(userId).orElse(new JobSeekerProfile());
            }
        }

        return null;
    }

    public Users getCurrentUser() {
        //參考IPAD p.208
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(!(authentication instanceof AnonymousAuthenticationToken)){
            String username = authentication.getName();

            Users user = usersRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Could not find user"));

            return user;
        }

        return null;
    }

    //老師說,可以直接用上面的getUserByEmail(String email)就可以了!
    public Users findByEmail(String currentUsername) {
        return usersRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}

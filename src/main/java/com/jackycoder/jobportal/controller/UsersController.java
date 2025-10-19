package com.jackycoder.jobportal.controller;

import com.jackycoder.jobportal.entity.Users;
import com.jackycoder.jobportal.entity.UsersType;
import com.jackycoder.jobportal.services.UsersService;
import com.jackycoder.jobportal.services.UsersTypeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

//login/logout/register
@Controller
public class UsersController {
    private final UsersTypeService usersTypeService;
    private final UsersService usersService;

    //constructor injection
    @Autowired //optional(因為只有一個constructor)
    public UsersController(UsersTypeService usersTypeService, UsersService usersService) {
        this.usersTypeService = usersTypeService;
        this.usersService = usersService;
    }

    //show user register form
    @GetMapping("/register")
    public String register(Model model) {
        //make use of the "model" to kind of pre-populate some basic form data
        //that I'll need here.

        List<UsersType> usersTypes = usersTypeService.getAll();

        model.addAttribute("getAllTypes", usersTypes);

        //I'll add an empty "User" object, just so we have an instance that we can use for
        //our form data
        model.addAttribute("user", new Users());

        return "register";
    }


    @PostMapping("/register/new")
    public String userRegistration(@Valid Users users, RedirectAttributes redirectAttributes) {
        Optional<Users> optionalUsers = usersService.getUserByEmail(users.getEmail());

        if (optionalUsers.isPresent()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Email already registered, try to login or register with other email."
            );

            return "redirect:/register";
        }

        usersService.addNew(users);

        return "redirect:/dashboard/";
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        Object errorMsg = request.getSession().getAttribute("loginError");

        if (errorMsg != null) {
            model.addAttribute("loginError", errorMsg);
            request.getSession().removeAttribute("loginError"); // 一次性訊息
        }
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication
                = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        return "redirect:/";
    }
}

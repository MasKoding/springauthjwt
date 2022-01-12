package org.project.learn.springauthrestapi.controller;

import org.project.learn.springauthrestapi.entity.ERole;
import org.project.learn.springauthrestapi.entity.Roles;
import org.project.learn.springauthrestapi.entity.Users;
import org.project.learn.springauthrestapi.payload.request.LoginRequest;
import org.project.learn.springauthrestapi.payload.request.SignupRequest;
import org.project.learn.springauthrestapi.payload.response.JwtResponse;
import org.project.learn.springauthrestapi.payload.response.MessageResponse;
import org.project.learn.springauthrestapi.repository.RoleRepository;
import org.project.learn.springauthrestapi.repository.UserRepository;
import org.project.learn.springauthrestapi.security.jwt.JwtUtils;
import org.project.learn.springauthrestapi.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*",maxAge = 3600)
@RestController
@RequestMapping(value = "/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    Logger logger = LoggerFactory.getLogger(AuthController.class);


    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest){
        Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails =  (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item->item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,userDetails.getId(),userDetails.getUsername(),userDetails.getEmail(),roles));

    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest){
        try {
            if(userRepository.existsByUsername(signupRequest.getUsername())){
                return ResponseEntity.badRequest().body(new MessageResponse("Error : Username is already taken"));

            }

            if(userRepository.existsByEmail(signupRequest.getEmail())){
                return ResponseEntity.badRequest().body(new MessageResponse("Error : Email is already taken"));
            }

//        create new user's account
            Users users = new Users(signupRequest.getUsername(),signupRequest.getEmail(),
                    encoder.encode(signupRequest.getPassword()));

            Set<String> strRoles = signupRequest.getRoles();
            Set<Roles> roles = new HashSet<>();

            if(strRoles == null){
                Roles userRole =  roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(()->new RuntimeException("Error: Role is not found"));
                roles.add(userRole);
            }else{
                strRoles.forEach(role->{
                    switch (role){
                        case "admin":
                            Roles adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                    .orElseThrow(()->new RuntimeException("Error: Role is not found"));
                            roles.add(adminRole);
                            break;
                        case "mod":
                            Roles modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                    .orElseThrow(()->new RuntimeException("Error : Role is not found"));
                            roles.add(modRole);
                            break;
                        default:
                            Roles userRole = roleRepository.findByName(ERole.ROLE_USER)
                                    .orElseThrow(()->new RuntimeException("Error: Role is not found"));
                            roles.add(userRole);



                    }
                });
            }

            users.setRoles(roles);
            userRepository.save(users);
        }catch (Exception ex){
            logger.error("error :{}",ex);
            throw ex;
        }


        return ResponseEntity.ok(new MessageResponse("User registered successfully"));

    }
}

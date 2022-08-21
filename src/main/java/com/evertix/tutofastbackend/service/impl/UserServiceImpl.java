package com.evertix.tutofastbackend.service.impl;

import com.evertix.tutofastbackend.exception.ResourceNotFoundException;
import com.evertix.tutofastbackend.model.*;
import com.evertix.tutofastbackend.repository.CourseRepository;
import com.evertix.tutofastbackend.repository.RoleRepository;
import com.evertix.tutofastbackend.repository.UserRepository;
import com.evertix.tutofastbackend.resource.PlanResource;
import com.evertix.tutofastbackend.resource.PlanSaveResource;
import com.evertix.tutofastbackend.resource.UserResource;
import com.evertix.tutofastbackend.security.payload.response.MessageResponse;
import com.evertix.tutofastbackend.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Override
    public Boolean userExistsByUsername(String username) {
        return this.userRepository.existsByUsername(username);
    }

    @Override
    public Boolean userExistsByEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

    @Override
    public List<User> getAllTeachersOfOneCourse(Long courseId) {
        return courseRepository.findById(courseId).map(course1 -> {
            return course1.getTeachers();
        }).orElseThrow(()-> new ResourceNotFoundException("Course not found"));
    }

    @Override
    public User createUser(User user) {
        return this.userRepository.save(user);
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(()->
                new ResourceNotFoundException("User with Id: "+userId+" not found"));
    }

    @Override
    public User updateUser(Long userId, User userDetails) {
        return userRepository.findById(userId).map(user -> {
            user.setUsername(userDetails.getUsername());
            user.setPassword(userDetails.getPassword());
            user.setName(userDetails.getName());
            user.setLastName(userDetails.getLastName());
            user.setBirthday(userDetails.getBirthday());
            user.setPhone(userDetails.getPhone());
            user.setAddress(userDetails.getAddress());
            user.setAverageStars(userDetails.getAverageStars());
            user.setActive(userDetails.getActive());
            user.setLinkedin(userDetails.getLinkedin());
            return userRepository.save(user);
        }).orElseThrow(()-> new ResourceNotFoundException("User whit Id: "+userId+" not found"));
    }

    @Override
    public ResponseEntity<?> deleteUser(Long userId) {
        return userRepository.findById(userId).map(user -> {
            userRepository.delete(user);
            return ResponseEntity.ok().build();
        }).orElseThrow(()-> new ResourceNotFoundException("User with Id: "+userId+" not found"));
    }

    @Override
    public User setLinkedinProfile(Long userId,String linkedin) {
        User user = this.userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User with Id: "+userId+" not found"));
        user.setLinkedin(linkedin);
        return this.userRepository.save(user);

    }

    @Override
    public ResponseEntity<?> addCourses(Long userId, List<Long> coursesId) {
        User user = this.userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User with Id: "+userId+" not found"));
        List<Course> coursesList = new ArrayList<>();
        Optional<Role> role = this.roleRepository.findByName(ERole.ROLE_TEACHER);
        if (user.getRoles().contains(role.orElse(null))){
            for(Long courseId: coursesId){
                Course course = this.courseRepository.findById(courseId).orElseThrow(()-> new ResourceNotFoundException("Course with Id: "+courseId+" not found"));
                if(!user.getCourses().contains(course)){
                    coursesList.add(course);
                }
            }
            user.setCourses(coursesList);
            return ResponseEntity.ok(userRepository.save(user));
        }else {
            return ResponseEntity.badRequest().body(new MessageResponse("You can set courses only for teachers"));
        }
    }

    @Override
    public ResponseEntity<?> removeCourses(Long userId, List<Long> coursesId) {
        User user = this.userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User with Id: "+userId+" not found"));
        List<Course> coursesList = new ArrayList<>();
        Optional<Role> role = this.roleRepository.findByName(ERole.ROLE_TEACHER);
        if (user.getRoles().contains(role.orElse(null))){
            for(Long courseId: coursesId){
                Course course = this.courseRepository.findById(courseId).orElseThrow(()-> new ResourceNotFoundException("Course with Id: "+courseId+" not found"));
                user.getCourses().remove(course);
            }
            return ResponseEntity.ok(userRepository.save(user));
        }else {
            return ResponseEntity.badRequest().body(new MessageResponse("You can set courses only for teachers"));
        }

    }

    @Override
    public ResponseEntity<?> banUser(Long userId) {
        return ResponseEntity.ok(
                this.userRepository.findById(userId).map(user -> {
                    //user.setActive(false);
                    user.setBanned(true);
                    return userRepository.save(user);
                }).orElseThrow(()-> new ResourceNotFoundException("User with Id: "+userId+" not found"))
        );
    }

    @Override
    public ResponseEntity<?> activateTeacher(Long userId) {
        return ResponseEntity.ok(
                this.userRepository.findById(userId).map(user -> {
                    Optional<Role> role = this.roleRepository.findByName(ERole.ROLE_TEACHER);
                    if (user.getRoles().contains(role.orElse(null))){
                        user.setActive(true);
                        return ResponseEntity.ok(userRepository.save(user));
                    }else {
                        return ResponseEntity.badRequest().body(new MessageResponse("You can only activate user with role teacher"));
                    }
                })
        );
    }

    @Override
    public User getUserByUsername(String username) {
        return this.userRepository.findByUsername(username).orElseThrow(()->new ResourceNotFoundException("User not found"));
    }


    @Override
    public Page<UserResource> getAllUsers(Pageable pageable) {
        Page<User> usersPage=this.userRepository.findAll(pageable);
        List<UserResource> users=usersPage.stream().map(this::convertToResource).collect(Collectors.toList());
        return new PageImpl<>(users,pageable,usersPage.getTotalElements());
    }

    //TODO: WRITE A QUERY TO OPTIMIZE
    @Override
    public Page<UserResource> getAllUsersStudents(Pageable pageable) {
        //Set<Role> roles = new HashSet<>();
        //roles.add(roleRepository.findByName(ERole.ROLE_TEACHER).get());
        //Page<User> usersPage=this.userRepository.findAllByRoles(roles,pageable);
        List<UserResource> users=userRepository.getAllUserByRole("ROLE_STUDENT").stream().map(this::convertToResource).collect(Collectors.toList());
        //return new PageImpl<>(users,pageable,usersPage.getTotalElements());
        return new PageImpl<>(users,pageable,pageable.getPageSize());
    }

    @Override
    public String getRoleByUsername(String username) {
        Optional<Role> roleTeacher = this.roleRepository.findByName(ERole.ROLE_TEACHER);
        Optional<Role> roleStudent = this.roleRepository.findByName(ERole.ROLE_STUDENT);
        Optional<Role> roleAdmin = this.roleRepository.findByName(ERole.ROLE_ADMIN);
        User user = this.userRepository.findByUsername(username).orElseThrow(()->new ResourceNotFoundException("User not found"));
        if (user.getRoles().contains(roleStudent.orElse(null))){
            return "ROLE_STUDENT";
        }else if (user.getRoles().contains(roleTeacher.orElse(null))) {
            return "ROLE_TEACHER";
        } else if (user.getRoles().contains(roleAdmin.orElse(null))){
            return "ROLE_ADMIN";
        } else {
            return "NONE";
        }
    }

    //TODO: WRITE A QUERY TO OPTIMIZE
    @Override
    public Page<UserResource> getAllUsersTeachers(Pageable pageable) {
        //Set<Role> roles = new HashSet<>();
        //roles.add(roleRepository.findByName(ERole.ROLE_TEACHER).get());
        //Page<User> usersPage=this.userRepository.findAllByRoles(roles,pageable);
        List<UserResource> users=userRepository.getAllUserByRole("ROLE_TEACHER").stream().map(this::convertToResource).collect(Collectors.toList());
        //return new PageImpl<>(users,pageable,usersPage.getTotalElements());
        return new PageImpl<>(users,pageable,pageable.getPageSize());
    }

    private User convertToEntity(UserResource resource){return mapper.map(resource, User.class);}
    private UserResource convertToResource(User entity){return mapper.map(entity, UserResource.class);}

}

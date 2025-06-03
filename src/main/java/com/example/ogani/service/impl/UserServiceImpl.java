package com.example.ogani.service.impl;

import java.util.HashSet; 
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.ogani.entity.ERole;
import com.example.ogani.entity.Role;
import com.example.ogani.entity.User;
import com.example.ogani.entity.Order;
import com.example.ogani.entity.OrderStatus;
import com.example.ogani.entity.OrderDetail;
import com.example.ogani.entity.VNPayTransaction;
import com.example.ogani.exception.BadRequestException;
import com.example.ogani.exception.NotFoundException;
import com.example.ogani.model.request.ChangePasswordRequest;
import com.example.ogani.model.request.CreateUserRequest;
import com.example.ogani.model.request.UpdateProfileRequest;
import com.example.ogani.repository.RoleRepository;
import com.example.ogani.repository.UserRepository;
import com.example.ogani.repository.OrderRepository;
import com.example.ogani.repository.OrderDetailRepository;
import com.example.ogani.repository.VNPayTransactionRepository;
import com.example.ogani.service.UserService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private VNPayTransactionRepository vnPayTransactionRepository;

    @Override
    public void register(CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        Set<String> strRoles = request.getRole();
        Set<Role> roles = new HashSet<>();
      
        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                case "admin":
                    Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roles.add(adminRole);
                    break;
                case "mod":
                    Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roles.add(modRole);
                    break;
                default:
                    Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roles.add(userRole);
                }
            });
        }
        user.setRoles(roles);
        user.setEnabled(true); // Set default status as enabled
        userRepository.save(user);
    }

    @Override
    public User getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("Not Found User"));
        return user;
    }
    @Override
    public User getUserById(long id){
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("Not Found User"));
        return user;
    }
    @Override
    public List<User> getList() {
        return userRepository.findAll(Sort.by("id").descending());
    }

    @Override
    public Page<User> getListWithPagination(Pageable pageable, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return userRepository.findByUsernameContainingOrEmailContaining(search, search, pageable);
        }
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public void deleteUser(long id) {
        // Check if user exists
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        // Check if user is admin
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_ADMIN);
        if (isAdmin) {
            throw new BadRequestException("Cannot delete admin user");
        }

        // Get all orders for this user
        List<Order> userOrders = orderRepository.getOrderByUser(id);
        
        // Check if user has any pending orders
        boolean hasPendingOrders = userOrders.stream()
                .anyMatch(order -> order.getOrderStatus() == OrderStatus.PENDING);
        if (hasPendingOrders) {
            throw new BadRequestException("Cannot delete user with pending orders");
        }

        // Delete all order details and orders for this user
        for (Order order : userOrders) {
            // Delete VNPay transactions first
            VNPayTransaction transaction = vnPayTransactionRepository.findByOrderId(order.getId());
            if (transaction != null) {
                transaction.setOrder(null); // Break the relationship
                vnPayTransactionRepository.delete(transaction);
            }

            // Delete order details
            List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
            for (OrderDetail detail : orderDetails) {
                detail.setOrder(null); // Break the bidirectional relationship
                detail.setProduct(null); // Break the product relationship
            }
            orderDetailRepository.deleteAll(orderDetails);
            
            // Break relationships before deleting order
            order.setUser(null);
            order.setOrderdetails(null);
            orderRepository.delete(order);
        }

        // Clear user roles first to avoid foreign key constraint issues
        user.getRoles().clear();
        userRepository.save(user);

        // Break any remaining relationships
        user.setOrders(null);

        // Delete user
        try {
            userRepository.delete(user);
        } catch (Exception e) {
            throw new BadRequestException("Error deleting user: " + e.getMessage());
        }
    }

    @Override
    public User updateUser(UpdateProfileRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new NotFoundException("Not Found User"));
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setCountry(request.getCountry());
        user.setState(request.getState());
        user.setAddress(request.getAddress());
        user.setPhone(request.getPhone());
        userRepository.save(user);
        return user;
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new NotFoundException("Not Found User"));

        if (!encoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Old Password Not Same");
        }
        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void toggleUserStatus(long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Not Found User"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUserById(Long id, UpdateProfileRequest request) {
        // Check if user exists
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        // Update user information
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setCountry(request.getCountry());
        user.setState(request.getState());
        user.setAddress(request.getAddress());
        user.setPhone(request.getPhone());
        Set<Role> roles = new HashSet<>();
        user.setRoles(roles);

        // Save and return updated user
        return userRepository.save(user);
    }
    @Override
    public User createUser(CreateUserRequest request){
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setCountry(request.getCountry());
        user.setState(request.getState());
        user.setAddress(request.getAddress());
        user.setPhone(request.getPhone());
        Set<Role> roles = new HashSet<>();
        user.setRoles(roles);
        userRepository.save(user);
        return user;
    }
}

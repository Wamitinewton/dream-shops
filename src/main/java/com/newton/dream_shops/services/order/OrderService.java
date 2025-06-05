package com.newton.dream_shops.services.order;

import com.newton.dream_shops.dto.order.OrderDto;
import com.newton.dream_shops.enums.OrderStatus;
import com.newton.dream_shops.exception.CustomException;
import com.newton.dream_shops.models.cart.Cart;
import com.newton.dream_shops.models.cart.CartItem;
import com.newton.dream_shops.models.order.Order;
import com.newton.dream_shops.models.order.OrderItem;
import com.newton.dream_shops.models.product.Product;
import com.newton.dream_shops.repository.order.OrderRepository;
import com.newton.dream_shops.repository.product.ProductRepository;
import com.newton.dream_shops.services.cart.cart.ICartService;
import com.newton.dream_shops.util.jwt.JwtHelperService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ICartService cartService;
    private final ModelMapper modelMapper;
    private final JwtHelperService jwtHelperService;

    @Override
    @Transactional
    public OrderDto placeOrder(HttpServletRequest request) {
        Long userId = jwtHelperService.getCurrentUserIdFromRequest(request);
        if (userId == null) {
            throw new CustomException("User authentication required");
        }

        Cart cart = cartService.getOrCreateCartForUser(userId);
        validateCartForOrder(cart);

        validateInventoryAvailability(cart);

        Order order = createOrder(cart);

        List<OrderItem> orderItemsList = createOrderItems(order, cart);
        order.setOrderItems(new HashSet<>(orderItemsList));
        order.setTotalPrice(calculateTotalAmount(orderItemsList));

        Order savedOrder = orderRepository.save(order);

        cartService.clearCartForUser(userId);

        return convertToDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto placeOrderForUser(Long userId) {
        if (userId == null) {
            throw new CustomException("User ID cannot be null");
        }

        Cart cart = cartService.getOrCreateCartForUser(userId);
        validateCartForOrder(cart);

        validateInventoryAvailability(cart);

        Order order = createOrder(cart);

        List<OrderItem> orderItemsList = createOrderItems(order, cart);
        order.setOrderItems(new HashSet<>(orderItemsList));
        order.setTotalPrice(calculateTotalAmount(orderItemsList));

        Order savedOrder = orderRepository.save(order);

        cartService.clearCartForUser(userId);

        return convertToDto(savedOrder);
    }

    private void validateCartForOrder(Cart cart) {
        if (cart == null) {
            throw new CustomException("No cart found for user");
        }

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new CustomException("Cannot place order with empty cart");
        }

        if (cart.getTotalAmount() == null || cart.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("Invalid cart total amount");
        }
    }

    private void validateInventoryAvailability(Cart cart) {
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            if (product == null) {
                throw new CustomException("Invalid product in cart");
            }

            if (product.getInventory() < cartItem.getQuantity()) {
                throw new CustomException(
                        String.format("Insufficient inventory for product '%s'. Available: %d, Requested: %d",
                                product.getName(), product.getInventory(), cartItem.getQuantity()));
            }

            if (cartItem.getQuantity() <= 0) {
                throw new CustomException("Invalid quantity in cart item");
            }
        }
    }

    private Order createOrder(Cart cart) {
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    private List<OrderItem> createOrderItems(Order order, Cart cart) {
        return cart.getCartItems().stream().map(cartItem -> {
            Product product = cartItem.getProduct();

            int newInventory = product.getInventory() - cartItem.getQuantity();
            product.setInventory(newInventory);
            productRepository.save(product);
            return new OrderItem(
                    order,
                    product,
                    cartItem.getQuantity(),
                    cartItem.getUnitPrice());
        }).toList();
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> orderItemsList) {
        return orderItemsList
                .stream()
                .map(orderItem -> orderItem.getPrice().multiply(
                        new BigDecimal(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional
    public OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Order not found"));

        validateStatusTransition(order.getOrderStatus(), newStatus);

        OrderStatus oldStatus = order.getOrderStatus();
        order.setOrderStatus(newStatus);

        if (newStatus == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED) {
            restoreInventoryForCancelledOrder(order);
        }

        Order savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        boolean isValidTransition = switch (currentStatus) {
            case PENDING -> newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.CANCELLED;
            case PROCESSING -> newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELLED;
            case SHIPPED -> newStatus == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };

        if (!isValidTransition) {
            throw new CustomException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
        }
    }

    private void restoreInventoryForCancelledOrder(Order order) {
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            int restoredInventory = product.getInventory() + orderItem.getQuantity();
            product.setInventory(restoredInventory);
            productRepository.save(product);

        }
    }

    @Override
    public OrderDto getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .map(this::convertToDto)
                .orElseThrow(() -> new CustomException("Order not found"));
    }

    @Override
    public List<OrderDto> getUserOrders(HttpServletRequest request) {
        Long userId = jwtHelperService.getCurrentUserIdFromRequest(request);
        if (userId == null) {
            throw new CustomException("User authentication required");
        }
        return getUserOrdersByUserId(userId);
    }

    @Override
    public List<OrderDto> getUserOrdersByUserId(Long userId) {
        if (userId == null) {
            throw new CustomException("User ID cannot be null");
        }

        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<OrderDto> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByOrderStatus(status);
        return orders.stream()
                .map(this::convertToDto)
                .toList();
    }

    private OrderDto convertToDto(Order order) {
        return modelMapper.map(order, OrderDto.class);
    }
}
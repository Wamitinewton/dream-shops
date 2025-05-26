package com.newton.dream_shops.services.order;

import com.newton.dream_shops.dto.order.OrderDto;
import com.newton.dream_shops.enums.OrderStatus;
import com.newton.dream_shops.exception.CustomException;
import com.newton.dream_shops.models.cart.Cart;
import com.newton.dream_shops.models.order.Order;
import com.newton.dream_shops.models.order.OrderItem;
import com.newton.dream_shops.models.product.Product;
import com.newton.dream_shops.repository.order.OrderRepository;
import com.newton.dream_shops.repository.product.ProductRepository;
import com.newton.dream_shops.services.cart.ICartService;
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


    @Override
    public Order placeOrder(Long userId) {
        Cart cart = cartService.getCartByUserId(userId);
        Order order = createOrder(cart);
        List<OrderItem> orderItemsList = createOrderItems(order, cart);
        order.setOrderItems(new HashSet<>(orderItemsList));
        order.setTotalPrice(calculateTotalAmount(orderItemsList));
        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(userId);
        return savedOrder;
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
            product.setInventory(product.getInventory() - cartItem.getQuantity());
            productRepository.save(product);
            return new OrderItem(
                    order,
                    product,
                    cartItem.getQuantity(),
                    cartItem.getUnitPrice()
            );
        }).toList();
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> orderItemsList) {
        return orderItemsList
                .stream()
                .map(orderItem -> orderItem.getPrice().multiply(
                        new BigDecimal(orderItem.getQuantity())
                )).reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    @Override
    public OrderDto getOrderById(Long orderId) {
        return orderRepository.findById(orderId).map(this :: convertToDto)
        .orElseThrow(() -> new CustomException("Order not found"));
    }

    @Override
    public List<OrderDto> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders
        .stream()
        .map(this::convertToDto)
        .toList();
    }

    private OrderDto convertToDto(Order order) {
        return modelMapper.map(order, OrderDto.class);
    }
}

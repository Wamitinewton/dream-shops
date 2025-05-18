package com.newton.dream_shops.services.cart;

import com.newton.dream_shops.exception.ResourceNotFoundException;
import com.newton.dream_shops.models.Cart;
import com.newton.dream_shops.models.CartItem;
import com.newton.dream_shops.models.Product;
import com.newton.dream_shops.repository.CartItemRepository;
import com.newton.dream_shops.repository.CartRepository;
import com.newton.dream_shops.services.products.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartItemService implements ICartItemService {
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final IProductService productService;
    private final ICartService cartService;

    @Override
    public void addItemToCart(Long cartId, Long productId, int quantity) {
        //1. Get The Cart
        //2. Get The Product
        //3. Check if the product is already in the cart
        //4. If yes, then increase the quantity with the requested quantity
        //5. If no, then initiate a new cart item entry

        Cart cart = cartService.getCart(cartId);
        Product product = productService.getProductById(productId);
        CartItem cartItem = cart.getCartItems()
                .stream()
                .filter(item -> item.getProduct()
                        .getId().equals(productId))
                .findFirst()
                .orElse(new CartItem());

        if (cartItem.getId() == null) {
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setUnitPrice(product.getPrice());
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        }
        cartItem.setTotalPrice();
        cart.addItem(cartItem);
        cartItemRepository.save(cartItem);
        cartRepository.save(cart);
    }

    @Override
    public void removeItemFromCart(Long cartId, Long productId) {
        Cart cart = cartService.getCart(cartId);
        CartItem cartItem = getCartItemById(cartId, productId);
        cart.removeItem(cartItem);
        cartRepository.save(cart);
    }

    @Override
    public void updateItemInCart(Long cartId, Long productId, int quantity) {
        Cart cart = cartService.getCart(cartId);
        cart.getCartItems()
                .stream()
                .filter(item -> item.getProduct()
                        .getId().equals(productId))
                .findFirst()
                .ifPresent(item ->{
                    item.setQuantity(quantity);
                    item.setUnitPrice(item.getProduct().getPrice());
                    item.setTotalPrice();
                });
        BigDecimal totalAmount = cart.getTotalAmount();
        cart.setTotalAmount(totalAmount);
        cartRepository.save(cart);
    }

    @Override
    public CartItem getCartItemById(Long cartId, Long productId) {
        Cart cart = cartService.getCart(cartId);
        return cart.getCartItems()
                .stream()
                .filter(item -> item.getProduct()
                        .getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
    }
}

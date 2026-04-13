package com.example.demo.controller;

import com.example.demo.model.CartItem;
import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class CartController {

    @Autowired
    private ProductService productService;

    // View Cart Page
    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {

        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        if (cart == null) {
            cart = new ArrayList<>();
        }

        double grandTotal = 0;

        for (CartItem item : cart) {
            grandTotal += item.getTotalPrice();
        }

        model.addAttribute("cartItems", cart);
        model.addAttribute("grandTotal", grandTotal);

        return "cart";
    }

    // Add Product to Cart
    @GetMapping("/addToCart/{id}")
    public String addToCart(@PathVariable("id") long id, HttpSession session) {

        Product product = productService.getProductById(id);

        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        if (cart == null) {
            cart = new ArrayList<>();
        }

        boolean found = false;

        for (CartItem item : cart) {
            if (item.getProductId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + 1);
                found = true;
                break;
            }
        }

        if (!found) {
            CartItem newItem = new CartItem(
                    product.getId(),
                    product.getProductName(),
                    product.getProductPrice(),
                    1
            );
            cart.add(newItem);
        }

        session.setAttribute("cart", cart);

        return "redirect:/shop";
    }

    // Remove Item from Cart
    @GetMapping("/removeFromCart/{id}")
    public String removeFromCart(@PathVariable("id") long id, HttpSession session) {

        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        if (cart != null) {
            cart.removeIf(item -> item.getProductId().equals(id));
            session.setAttribute("cart", cart);
        }

        return "redirect:/cart";
    }
}
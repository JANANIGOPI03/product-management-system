package com.example.demo.controller;

import com.example.demo.model.CartItem;
import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.model.Product;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class CheckoutController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductService productService;

    @GetMapping("/checkout")
    public String checkoutPage(Model model) {
        model.addAttribute("order", new Order());
        return "checkout";
    }

    @PostMapping("/placeOrder")
    public String placeOrder(@ModelAttribute("order") Order order, HttpSession session) {

        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {
            return "redirect:/cart";
        }

        double total = 0;
        for (CartItem item : cart) {
            total += item.getTotalPrice();
        }

        order.setTotalAmount(total);
        order.setOrderDate(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        for (CartItem item : cart) {

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(savedOrder.getId());
            orderItem.setProductId(item.getProductId());
            orderItem.setProductName(item.getProductName());
            orderItem.setProductPrice(item.getProductPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setTotalPrice(item.getTotalPrice());

            orderItemRepository.save(orderItem);

            Product product = productService.getProductById(item.getProductId());

            int currentStock = (product.getStockQuantity() != null) ? product.getStockQuantity() : 0;
            int updatedStock = currentStock - item.getQuantity();

            if (updatedStock < 0) {
                updatedStock = 0;
            }

            product.setStockQuantity(updatedStock);
            productService.saveProduct(product);
        }

        session.removeAttribute("cart");

        return "redirect:/order-success";
    }

    @GetMapping("/order-success")
    public String orderSuccessPage() {
        return "order_success";
    }
}
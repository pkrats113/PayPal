package com.Razor.Controller;

import com.Razor.Service.PayPalService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;



@Controller
@RequestMapping("/paypal")
public class PayPalController {

    @Autowired
    private PayPalService paypalService;

    @PostMapping("/pay")
    public RedirectView pay(HttpServletRequest request) {
        String cancelUrl = request.getRequestURL().toString() + "/cancel";
        String successUrl = request.getRequestURL().toString() + "/success";
        try {
            Payment payment = paypalService.createPayment(
                    10.00,
                    "USD",
                    "paypal",
                    "sale",
                    "Payment description",
                    cancelUrl,
                    successUrl);
            for (Links links : payment.getLinks()) {
                if (links.getRel().equals("approval_url")) {
                    return new RedirectView(links.getHref());
                }
            }
        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }
        return new RedirectView("/error");
    }

    @GetMapping("/success")
    public String success(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId) {
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            if (payment.getState().equals("approved")) {
                return "success";
            }
        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }
        return "error";
    }

    @GetMapping("/cancel")
    public String cancelPay() {
        return "cancel";
    }
}


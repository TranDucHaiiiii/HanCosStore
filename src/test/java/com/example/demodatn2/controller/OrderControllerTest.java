package com.example.demodatn2.controller;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class OrderControllerTest {

    @Test
    void resolveShippingFee_prefersSessionFeeOverFormValue() throws Exception {
        OrderController controller = new OrderController(
                null,
                null,
                null,
                null,
                null,
                null
        );

        HttpSession session = Mockito.mock(HttpSession.class);

        when(session.getAttribute("CURRENT_SHIPPING_FEE")).thenReturn(new BigDecimal("37222"));

        Method method = OrderController.class.getDeclaredMethod("resolveShippingFee", HttpSession.class, BigDecimal.class);
        method.setAccessible(true);

        BigDecimal resolved = (BigDecimal) method.invoke(controller, session, new BigDecimal("0"));

        assertThat(resolved).isEqualByComparingTo("37222");
    }

    @Test
    void resolveShippingFee_fallsBackToFormValueWhenSessionMissing() throws Exception {
        OrderController controller = new OrderController(
                null,
                null,
                null,
                null,
                null,
                null
        );

        HttpSession session = Mockito.mock(HttpSession.class);

        when(session.getAttribute("CURRENT_SHIPPING_FEE")).thenReturn(null);

        Method method = OrderController.class.getDeclaredMethod("resolveShippingFee", HttpSession.class, BigDecimal.class);
        method.setAccessible(true);

        BigDecimal resolved = (BigDecimal) method.invoke(controller, session, new BigDecimal("30000"));

        assertThat(resolved).isEqualByComparingTo("30000");
    }
}




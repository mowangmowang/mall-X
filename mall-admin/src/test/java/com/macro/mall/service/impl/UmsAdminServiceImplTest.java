package com.macro.mall.service.impl;

import com.macro.mall.model.UmsAdmin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UmsAdminServiceImplTest {

    @Test
    void umsAdmin_constructorAndGetters_shouldWork() {
        UmsAdmin admin = new UmsAdmin();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword("encodedPassword");
        admin.setIcon("test-icon");

        assertEquals(1L, admin.getId());
        assertEquals("admin", admin.getUsername());
        assertEquals("encodedPassword", admin.getPassword());
        assertEquals("test-icon", admin.getIcon());
    }

    @Test
    void umsAdmin_passwordShouldBeSettable() {
        UmsAdmin admin = new UmsAdmin();
        String rawPassword = "plainPassword123";
        String encodedPassword = "$2a$10$encodedHash";

        admin.setPassword(encodedPassword);

        assertEquals(encodedPassword, admin.getPassword());
        assertNotEquals(rawPassword, admin.getPassword());
    }

    @Test
    void umsAdmin_statusField_shouldWork() {
        UmsAdmin admin = new UmsAdmin();
        admin.setStatus(1);
        assertEquals(1, admin.getStatus());

        admin.setStatus(0);
        assertEquals(0, admin.getStatus());
    }
}

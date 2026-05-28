package com.example.rentalcommon.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaginationUtilTest {

    @Test
    void normalizePageShouldClampToOne() {
        assertEquals(1, PaginationUtil.normalizePage(null));
        assertEquals(1, PaginationUtil.normalizePage(0));
        assertEquals(1, PaginationUtil.normalizePage(-3));
        assertEquals(2, PaginationUtil.normalizePage(2));
    }

    @Test
    void normalizeSizeShouldClampIntoExpectedRange() {
        assertEquals(PaginationUtil.DEFAULT_SIZE, PaginationUtil.normalizeSize(null));
        assertEquals(PaginationUtil.DEFAULT_SIZE, PaginationUtil.normalizeSize(0));
        assertEquals(PaginationUtil.DEFAULT_SIZE, PaginationUtil.normalizeSize(-9));
        assertEquals(10, PaginationUtil.normalizeSize(10));
        assertEquals(PaginationUtil.MAX_SIZE, PaginationUtil.normalizeSize(PaginationUtil.MAX_SIZE + 100));
    }
}


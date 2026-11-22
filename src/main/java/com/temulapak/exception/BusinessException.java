package com.temulapak.exception;

/** Pelanggaran aturan bisnis yang pesannya aman ditampilkan ke pengguna. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

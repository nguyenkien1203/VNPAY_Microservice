package com.restaurant.cache.exception;


import lombok.Getter;

/**
 * The type Create redis exception.
 *
 * @author hoangnlv @vnpay.vn
 */
@Getter
public class CacheException extends Exception {
    private final String code;
    private final String des;

    public CacheException(String code, String des) {
        this.code = code;
        this.des = des;
    }
}

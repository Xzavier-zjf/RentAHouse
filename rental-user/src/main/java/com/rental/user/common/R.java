package com.rental.user.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class R {
    private Integer code;
    private String msg;
    private Object data;

    public static R ok(Object data) {
        return new R(200, "success", data);
    }

    public static R error(String msg) {
        return new R(500, msg, null);
    }
}


package com.sky.context;

import com.sky.constant.IsAdminConstant;
import com.sky.constant.MessageConstant;

public class BaseContext {

    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    public static ThreadLocal<Boolean> threadLocal2 = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }

    public static void removeCurrentId() {
        threadLocal.remove();
    }

    public static void setIsAdmin(Boolean isAdmin) {
          threadLocal2.set(isAdmin);
    }

    public static Boolean getIsAdmin() {
        return threadLocal2.get();
    }

}

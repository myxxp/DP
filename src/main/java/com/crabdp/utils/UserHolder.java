package com.crabdp.utils;

import com.crabdp.dto.UserDTO;

public class UserHolder {
    /**
     * 保存用户信息
     * ThreadLocal是一个线程内部的数据存储类，可以在指定线程内存储数据，数据存储以后，只有指定线程可以得到存储数据。
     * 通过ThreadLocal的get()和set()方法就可以得到所存储的数据。
     * ThreadLocal提供了线程内部的局部变量，每个线程都可以通过set()和get()方法来对这个局部变量进行操作，而不会和其他线程的局部变量产生冲突。
     * 从而实现线程隔离，每个线程中的数据都是独立的，不共享的。
     * 通过ThreadLocal可以让某个对象在一个线程中全局可见，对其他线程而言是不可见的。
     * 通过ThreadLocal可以实现线程内的数据共享，这样就可以省去在每个方法中传递参数的麻烦。
     *
     */
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user){
        tl.set(user);
    }

    public static UserDTO getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
}

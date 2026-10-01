/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util.flash;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 *
 * @author default
 */
public class Flash {
    private static final String FLASH_KEY = "flashMessage";

    private Flash() {
    }

    public static void success(
            HttpServletRequest request,
            String message) {

        set(
            request.getSession(),
            FlashType.SUCCESS,
            message
        );
    }

    public static void error(
            HttpServletRequest request,
            String message) {

        set(
            request.getSession(),
            FlashType.ERROR,
            message
        );
    }

    public static void warning(
            HttpServletRequest request,
            String message) {

        set(
            request.getSession(),
            FlashType.WARNING,
            message
        );
    }

    public static void info(
            HttpServletRequest request,
            String message) {

        set(
            request.getSession(),
            FlashType.INFO,
            message
        );
    }

    private static void set(
            HttpSession session,
            FlashType type,
            String message) {

        session.setAttribute(
            FLASH_KEY,
            new FlashMessage(type, message)
        );
    }

    public static String getSessionKey() {
        return FLASH_KEY;
    }
}

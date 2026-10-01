/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util.flash;

/**
 *
 * @author default
 */
public class FlashMessage {
    private final FlashType type;
    private final String message;

    public FlashMessage(FlashType type, String message) {
        this.type = type;
        this.message = message;
    }

    public FlashType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}

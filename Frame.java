/**
 * Frame which holds the panel.
 *
 * @author BAXTER BERDINNER
 * @version 7/03/2023
 */

//Import nessesary extensions.
import java.awt.*;
import javax.swing.*;

public class Frame extends JFrame {
    Panel panel;

    static Image iconImg = new ImageIcon("icon.png").getImage();

    public Frame() {
        panel = new Panel(); // Creates a new instance of panel class.

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.add(panel);
        
        this.setIconImage(iconImg);
        this.setTitle("BASEMENT BOMBER");

        this.pack();
        this.setLocationRelativeTo(null);
        this.setResizable(false); // Stops user from being able to resize the window.
        this.setVisible(true);
    }
}
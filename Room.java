
/**
 * Code for rooms.
 *
 * @author BAXTER BERDINNER
 * @version 20/02/2023
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.lang.Math;
import java.awt.geom.*;
import java.util.ArrayList;

public class Room {
    int x, y, level;
    static int width = Panel.CHUNK * 18, height = Panel.CHUNK * 3;

    Image roomImg;

    Rectangle col;
    Rectangle floor;

    ArrayList<Enemy> enemy = new ArrayList<Enemy>();


    public Room(int x, int y, int level) {
        this.x = x;
        this.y = y; // Setting x, y, level to be ints passed from Panel.
        this.level = level;

        roomImg = new ImageIcon("room" + level + "Dark.png").getImage();
        // Images named "room1, room2..." each level will have different image.
        col = new Rectangle(x, y, width, height); // Paralax added later in Panel.
        floor = new Rectangle(x, y + height, width, Panel.CHUNK);
        // Creates rectangle 1 chunk tall at the bottom of a room.

        populate();
    }

    public void paint(Graphics g) { // Will be called in Panel paint method.
        Graphics2D g2D = (Graphics2D) g;

        if (Panel.roomLevel == level) {
            if (level == 0 & Panel.gameTime % 2 == 0)
                // This will constantly flick between true and false for level 0, and always be
                // false for other levels.
                roomImg = new ImageIcon("room" + level + "Flicker.png").getImage();
            else
                roomImg = new ImageIcon("room" + level + ".png").getImage();
        } else
            roomImg = new ImageIcon("room" + level + "Dark.png").getImage();

        g2D.drawImage(roomImg, x, y + Panel.parallax, width, height, null);
    }

    public void populate() { // Adds enemies to the level.
        for (int i = 0; i < level * 3 + 1; i++) {
            enemy.add(new Enemy(level));
        }
    }
}

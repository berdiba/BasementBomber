
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
    static int CHUNK = Panel.CHUNK;

    static int x = CHUNK * 2;
    int y, level;
    static int width = CHUNK * 18, height = CHUNK * 3;
    static int outlineOffset = Panel.PIXEL * 8; // Offset of the roomOutline.

    Image roomImg; // Images defined later on.
    Image roomOutlineImg;

    Rectangle col;
    Rectangle ceiling, floor;

    ArrayList<Enemy> enemy = new ArrayList<Enemy>();

    public Room(int y, int level) {
        this.y = y; // Setting x, y, level to be ints passed from Panel.
        this.level = level;

        roomImg = new ImageIcon("room" + level + "Dark.png").getImage();
        // Images named "room1, room2..." each level will have different image.
        col = new Rectangle(x, y, width, height); // Paralax added later in Panel.
        ceiling = new Rectangle(x, y + height, width, CHUNK / 2);
        floor = new Rectangle(x, y - CHUNK / 2, width, CHUNK / 2);
        // Creates rectangle 1 chunk tall at the bottom of a room.

        populate();
    }

    public void paint(Graphics g) { // Will be called in Panel paint method.
        Graphics2D g2D = (Graphics2D) g;

        if (Panel.lastRoom == level) {
            if (level <= 3 & Panel.gameTime % 2 == 0) {
                // This will constantly flick between true and false for level 0, and always be
                // false for other levels.
                roomImg = new ImageIcon("room" + level + "Flicker.png").getImage();
                roomOutlineImg = new ImageIcon("roomOutline.png").getImage();

            } else {
                roomImg = new ImageIcon("room" + level + ".png").getImage();
                roomOutlineImg = new ImageIcon("roomOutline.png").getImage();

            }
        } else {
            roomImg = new ImageIcon("room" + level + "Dark.png").getImage();
            roomOutlineImg = new ImageIcon("roomOutlineDark.png").getImage();
        }

        g2D.drawImage(roomOutlineImg, x - outlineOffset + Panel.damageWobbleX,
                y - outlineOffset + Panel.parallax + Panel.damageWobbleY, null);
        g2D.drawImage(roomImg, x + Panel.damageWobbleX, y + Panel.parallax + Panel.damageWobbleY, width, height, null);
    }

    public void populate() { // Adds enemies to the level.
        for (int i = 0; i < Math.min(level, 3) + 4; i++) {
            enemy.add(new Enemy(level, false));
        }
        if (level == 4)
            enemy.add(new Enemy(level, true));

    }

    public boolean isClear() {
        if (enemy.size() == 0)
            return true;
        else
            return false;
    }
}


/**
 * Code for rooms.
 *
 * @author BAXTER BERDINNER
 * @version 20/02/2023
 */

import java.awt.*;
import javax.swing.*;
import java.lang.Math;
import java.util.ArrayList;

public class Room {
    static int CHUNK = Panel.CHUNK;

    int y, level;
    static int x = CHUNK * 2;
    static int width = CHUNK * 18, height = CHUNK * 3;
    static int outlineOffset = Panel.PIXEL * 8; // Offset of the roomOutline.

    static int enemyCountBase = 4, enemyCountMod = 3;

    Image roomImg; // Images defined later on.
    Image roomOutlineImg;

    Rectangle col;
    Rectangle ceiling, floor, top; // Top used for particle collisions from dummy.

    ArrayList<Enemy> enemy = new ArrayList<Enemy>(); // Arraylist which holds all enemy classes.

    public Room(int y, int level) {
        this.y = y; // Setting x, y, level to be ints passed from Panel.
        this.level = level;

        roomImg = new ImageIcon("room" + level + "Dark.png").getImage();
        // Images named "room1, room2..." each level will have different image.
        col = new Rectangle(x, y, width, height); // Paralax added later in Panel.
        ceiling = new Rectangle(x, y - CHUNK / 2, width, CHUNK / 2);
        floor = new Rectangle(x, y + height, width, CHUNK / 2);
        // Creates rectangle 1 chunk tall at the bottom of a room.

        if (level == 0) // Creates extra collision box on level 0 for particle collisions on surface.
            top = new Rectangle(x, y - CHUNK * 4 / 3, width, CHUNK / 2);
    }

    public void paint(Graphics g) { // Will be called in Panel paint method.
        Graphics2D g2D = (Graphics2D) g;

        if (Panel.lastRoom == level && !Panel.gameOver) {
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
        for (int i = 0; i < Math.min(level, enemyCountMod) + enemyCountBase; i++) {
            enemy.add(new Enemy(level, false, false));
        }
        if (level == 0)
            enemy.add(new Enemy(level, false, true));
        if (level == 4)
            enemy.add(new Enemy(level, true, false));
    }

    public boolean isClear() { // Enemies on level 0 include indestructable surface dummy, which is ignored.
        if (enemy.size() == 0 || level == 0 && enemy.size() - 1 == 0)
            return true;
        else
            return false;
    }
}

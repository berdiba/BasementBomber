
/**
 * Code for enemies.
 *
 * @author BAXTER BERDINNER
 * @version 20/02/2023
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.lang.Math;
import java.awt.geom.*;

public class Enemy {
    int level;
    int width, height;
    int x, y;
    int colXOffset = 8, colYOffset = 2;

    Image enemyImg;

    Rectangle col;

    public Enemy(int level) {
        this.level = level;

        enemyImg = new ImageIcon("enemy"+level+".png").getImage();

        width = enemyImg.getWidth(null);
        height = enemyImg.getHeight(null);

        x = (int) (Math.random() * (Room.width - width) + Panel.roomX);
        y = Panel.roomYBase + Panel.roomYLevel * level + Room.height - height;

        col = new Rectangle(x, y + Panel.parallax, width - colXOffset * 2, height - colYOffset * 2);
    }

    public void paint(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;

        if (Panel.lastInRoom == level)
        enemyImg = new ImageIcon("enemy"+level+".png").getImage();
        else
        enemyImg = null;

        g2D.drawImage(enemyImg, x, y + Panel.parallax, null);
    }
}
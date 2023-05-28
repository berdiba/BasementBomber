
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
    int x, y, level;
    int width, height;

    Image enemyImg;

    Rectangle enemyCol;

    public Enemy(int x, int y, int level) {
        this.x = x;
        this.y = y; //y = Panel.room.get(level).y + Panel.room.get(level).height
        this.level = level;

        enemyImg = new ImageIcon("enemy"+level+".png").getImage();

        width = enemyImg.getWidth(null);
        height = enemyImg.getHeight(null);

        enemyCol = new Rectangle(x, y + Panel.parallax, width, height);
    }

    public void paint(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;

        g2D.drawImage(enemyImg, x, y + Panel.parallax, null);
    }
}
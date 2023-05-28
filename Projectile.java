/**
 * Code for projectiles.
 *
 * @author BAXTER BERDINNER
 * @version 17/03/2023
 */

import java.awt.*;
import javax.swing.*;

public class Projectile {
    int width = 12;
    int height = 4;
    int x, y;
    int projectileSpeed = 32;
    int damage, blastRadius;

    boolean facingLeft;

    String type;

    Image projectileImg;
    Rectangle projectileCol;


    public Projectile(boolean facingLeft, int x, int y, String type) {
        this.facingLeft = facingLeft;

        if(facingLeft)
        this.x = x;
        else
        this.x = x + Panel.playerWidth;

        this.y = y + Panel.playerHeight * 19 / 32 + Panel.parallax;
        this.type = type;

        projectileImg = new ImageIcon(type+"Projectile.png").getImage();
        projectileCol = new Rectangle(x, y, width, height);
    }

    public void paint(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;

        if (facingLeft)
        g2D.drawImage(projectileImg, x + width, y, -width, height, null);
        else
        g2D.drawImage(projectileImg, x, y, width, height, null);

    }

    public void move() {
        if (facingLeft)
            x = x - projectileSpeed;
        else
            x = x + projectileSpeed;

    }

    public void hit() {
        switch (type) {
            case "bazooka":
                damage = 1;
                blastRadius = 64;
                break;
        }
    }
}
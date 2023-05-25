/**
 * Code for projectiles.
 *
 * @author BAXTER BERDINNER
 * @version 17/03/2023
 */

import java.awt.*;
import javax.swing.*;

public class Projectile {
    int WIDTH = 12;
    int HEIGHT = 4;
    int X, Y;
    int projectileSpeed = 32;
    int damage, blastRadius;

    boolean facingLeft;

    String type;

    Image projectileImg;
    Rectangle projectileCol;


    public Projectile(boolean facingLeft, int X, int Y, String type) {
        this.facingLeft = facingLeft;

        if(facingLeft)
        this.X = X;
        else
        this.X = X + Panel.playerWidth;

        this.Y = Y + Panel.playerHeight * 19 / 32 + Panel.parallax;
        this.type = type;

        projectileImg = new ImageIcon(type+"Projectile.png").getImage();
        projectileCol = new Rectangle(X, Y, WIDTH, HEIGHT);
    }

    public void paint(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;

        if (facingLeft)
        g2D.drawImage(projectileImg, X + WIDTH, Y, -WIDTH, HEIGHT, null);
        else
        g2D.drawImage(projectileImg, X, Y, WIDTH, HEIGHT, null);

    }

    public void move() {
        if (facingLeft)
            X = X - projectileSpeed;
        else
            X = X + projectileSpeed;

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
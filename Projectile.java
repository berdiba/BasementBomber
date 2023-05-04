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

    Rectangle projectileRect;
    Image projectileImg;

    public Projectile(boolean facingLeft, int X, int Y, String type) {
        projectileRect = new Rectangle(X, Y, WIDTH, HEIGHT);
        projectileImg = new ImageIcon("bazookaProjectile.png").getImage();

        this.facingLeft = facingLeft;

        if(facingLeft)
        this.X = X;
        else
        this.X = X + Panel.playerWidth;

        this.Y = Y + Panel.playerHeight * 19 / 32;
        this.type = type;
        System.out.println(Panel.playerHeight);
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
            System.out.println(facingLeft);

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
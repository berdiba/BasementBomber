
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

    int speedMax, speed, idleSpeed = (int) (Math.random() * 3) + 1, chaseSpeed = 2, wobble,
            viewDistance = Panel.CHUNK * 8;
    // Speed determined by level + idleSpeed, or when chasing player + chaseSpeed.

    int gravity = 10, up = -gravity;

    int decision, decisionTime = 0, decisionMax = 180, newDecision = (int) (Math.random() * decisionMax);
    // DecisionMax measured in frames. 120 frames at 60 fps is 2 seconds.

    int growHeight = 0;
    int healthMax, health;

    Boolean takingAction = false, facingLeft = true;

    Image enemyImg;

    Rectangle col;
    Rectangle viewCol;
    Rectangle damageColLeft, damageColRight;

    public Enemy(int level) {
        this.level = level;

        enemyImg = new ImageIcon("enemy" + level + ".png").getImage();

        width = enemyImg.getWidth(null);
        height = enemyImg.getHeight(null);

        x = (int) (Math.random() * (Room.width - width) + Panel.roomX); // Set x to be random number within room bounds.
        y = Panel.roomYBase + Panel.roomYLevel * level + Room.height - height;

        if (level != 3)
            speedMax = Math.min(level + idleSpeed, 2 + idleSpeed); // Speed caps out at level 2.
        else {
            speedMax = Math.max(idleSpeed - 2, 1); // Mummies on level 3 are much slower.
            chaseSpeed = (int) Math.round(Math.random() * 2);
        }

        healthMax = Math.max(1, level);
        health = healthMax;

        col = new Rectangle(x, y, width, height);
        viewCol = new Rectangle(col.x - viewDistance, col.y, col.width + viewDistance * 2, col.height);
    }

    public void paint(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;

        switch (level) { // Determines enemies appearance based on health and level.
            case 2:
                if (health == 1)
                    enemyImg = new ImageIcon("enemy2hurt.png").getImage();
                break;
            case 3:
                if (health == 2)
                    enemyImg = new ImageIcon("enemy3hurt1.png").getImage();
                if (health == 1)
                    enemyImg = new ImageIcon("enemy3hurt2.png").getImage();
                break;
        }

        if (Panel.lastInRoom == level) { // Activate when player enters enemies level.
            if (growHeight < height) {
                growHeight = growHeight + height / 4; // Increase growheight.
                if (facingLeft)
                    g2D.drawImage(enemyImg, x + width, y + Panel.parallax - growHeight + width, -width + wobble,
                            growHeight,
                            null);
                else
                    g2D.drawImage(enemyImg, x, y + Panel.parallax - growHeight + width + wobble, width, growHeight,
                            null);
            } else if (facingLeft)
                g2D.drawImage(enemyImg, x + width, y + Panel.parallax - growHeight + width + wobble, -width, growHeight,
                        null);
            else
                g2D.drawImage(enemyImg, x, y + Panel.parallax - growHeight + width + wobble, width, growHeight, null);
        } else if (growHeight > 0) {
            growHeight = growHeight - height / 4; // Decrease growheight.
            if (facingLeft)
                g2D.drawImage(enemyImg, x + width, y + Panel.parallax - growHeight + width, -width + wobble, growHeight,
                        null);
            else
                g2D.drawImage(enemyImg, x, y + Panel.parallax - growHeight + width + wobble, width, growHeight, null);
        }
    }

    public void move() {
        y = y + up + gravity;
        col = new Rectangle(x + colXOffset, y + Panel.parallax + colYOffset, width - colXOffset * 2,
                height - colYOffset * 2);
        // Update enemty collider.
        viewCol = new Rectangle(col.x - viewDistance, col.y, col.width + viewDistance * 2, col.height);
        // Update enemy range of vision.
        damageColLeft = new Rectangle(col.x, col.y + Panel.PIXEL, 1, col.height);
        damageColRight = new Rectangle(col.x + col.width, col.y + Panel.PIXEL, 1, col.height);

        if (decisionTime < newDecision && !takingAction) // Triggers only when player isnt taking an action.
            decisionTime++; // Increace decisionTime until it reaches newDecision
        else {
            takingAction = true; // When decisionTime = newDecision, take an action.

            if (decisionTime == newDecision)
                if (level % 2 == 0 && x > Panel.WIDTH / 2 && Math.random() < 0.8)
                    decision = 1; // When enemy is on an even level and near ladder, bias them to moving left.
                else if (level % 2 == 1 && x < Panel.WIDTH / 2 && Math.random() < 0.8)
                    decision = 2; // When enemy is on an odd level and near ladder, bias them to moving right.
                else
                    decision = (int) (Math.random() * 3); // Make a random decision.

            switch (decision) {
                case 0:
                    decisionTime = 0; // Stay still
                    break;
                case 1:
                    moveLeft(); // Move left
                    break;
                case 2:
                    moveRight(); // Move right
                    break;
            }
            decisionTime--; // Enemy moves left / right until decisiontime reaches 0.

            if (decisionTime <= 0) {
                takingAction = false; // Stop taking action.
                newDecision = (int) (Math.random() * decisionMax); // Regenerate the decision.
            }
        }
    }

    public void moveLeft() {
        facingLeft = true;
        x = x - speed; // Move left.
        if (col.intersects(Panel.wallLeftCol))
            decision = 2; // Makes enemy move right instead for remaining decision duration.
        wobble = (int) (Math.sin(Panel.gameTime) * 2); // Alternates between -1 and 1.
    }

    public void moveRight() {
        facingLeft = false;
        x = x + speed;
        if (col.intersects(Panel.wallRightCol))
            decision = 1;
        wobble = (int) (Math.sin(Panel.gameTime) * 2); // Alternates between -1 and 1.
    }

    public void checkCollisions() {
        if (viewCol.intersects(Panel.playerCol)) { // Trigger when player is within enemies line of sight.
            decisionTime = decisionMax; // Reset decision.
            speed = speedMax + chaseSpeed; // Increase speed.
            if (viewCol.x + viewCol.width / 2 > Panel.playerCol.x + Panel.playerCol.width / 2) {
                decision = 1; // Move left / right depending on where player is relative to enemy.
            } else {
                decision = 2;
            }
        } else
            speed = speedMax; // Set speed back to normal.
    }
}

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
    int speed = level + 3;

    int decision, decisionTime = 0, decisionMax = 120, newDecision = (int) (Math.random() * decisionMax),
            moveDuration = 120;
    // DecisionMax measured in frames. 120 frames at 60 fps is 2 seconds.

    int growHeight = 0;

    Boolean takingAction = false;

    Image enemyImg;

    Rectangle col;

    public Enemy(int level) {
        this.level = level;

        enemyImg = new ImageIcon("enemy" + level + ".png").getImage();

        width = enemyImg.getWidth(null);
        height = enemyImg.getHeight(null);

        x = (int) (Math.random() * (Room.width - width) + Panel.roomX);
        y = Panel.roomYBase + Panel.roomYLevel * level + Room.height - height;

        col = new Rectangle(x, y, width, height);
    }

    public void paint(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;

        if (Panel.lastInRoom == level) { // Activate when player enters enemies level.
            if (growHeight < height) {
                growHeight = growHeight + height / 4; // Increase growheight.
                g2D.drawImage(enemyImg, x, y + Panel.parallax - growHeight + width, width, growHeight, null);
            } else
                g2D.drawImage(enemyImg, x, y + Panel.parallax, null);
        } else if (growHeight > 0) {
            growHeight = growHeight - height / 4; // Decrease growheight.
            g2D.drawImage(enemyImg, x, y + Panel.parallax - growHeight + width, width, growHeight, null);
        }
    }

    public void move() {
        col = new Rectangle(x + colXOffset, y + Panel.parallax + colYOffset, width - colXOffset * 2,
                height - colYOffset * 2);
        // Update player collider.

        if (decisionTime < newDecision && !takingAction) // Triggers only when player isnt taking an action.
            decisionTime++; // Increace decisionTime until it reaches newDecision
        else {
            takingAction = true; // When decisionTime = newDecision, take an action.

            if (decisionTime == newDecision)
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
        x = x - speed; // Move left.
        if (col.intersects(Panel.wallLeftCol))
            decision = 2; // Makes enemy move right instead for remaining decision duration.
    }

    public void moveRight() {
        x = x + speed;
        if (col.intersects(Panel.wallRightCol))
            decision = 1;
    }
}
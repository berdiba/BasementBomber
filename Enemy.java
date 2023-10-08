
/**
 * Code for enemies.
 *
 * @author BAXTER BERDINNER
 * @version 20/02/2023
 */

import java.awt.*;
import javax.swing.*;
import java.lang.Math;

public class Enemy {
    static int CHUNK = Panel.CHUNK;
    static int PIXEL = Panel.PIXEL;

    int level;
    int width, height;
    int x, y;

    int colXOffset = 8, colYOffset = 2;

    int speedMax, speed, idleSpeed = (int) (Math.random() * 3) + 1, chaseSpeed = 2, wobble,
            viewDistance = CHUNK * 10;
    // Speed determined by level + idleSpeed, or when chasing player + chaseSpeed.

    int gravity = 10, up = -gravity;

    int decision, decisionTime = 0, decisionMax = 180, newDecision = (int) (Math.random() * decisionMax);
    // DecisionMax measured in frames. 120 frames at 60 fps is 2 seconds.

    int growHeight = 0;
    int healthMax, health;

    int launchSpeed, launchSpeedMax;

    Boolean takingAction = false, facingLeft = false, isBoss, isDummy, chasing = false;

    Image enemyImg;
    Image alertImg;

    Rectangle col = new Rectangle(0, 0, 1, 1);
    Rectangle viewCol = new Rectangle(0, 0, 1, 1);
    Rectangle damageColLeft = new Rectangle(0, 0, 1, 1), damageColRight = new Rectangle(0, 0, 1, 1);

    public Enemy(int level, boolean isBoss, boolean isDummy) {
        this.level = level;
        this.isBoss = isBoss;
        this.isDummy = isDummy;

        if (!isBoss && !isDummy) {
            enemyImg = new ImageIcon("enemy" + level + ".png").getImage();
            alertImg = new ImageIcon("alert.png").getImage();

            width = enemyImg.getWidth(null);
            height = enemyImg.getHeight(null);

            if (level % 2 == 0) // Even levels where ladder is on the right.
                x = (int) (Math.random() * (Room.width / 2 - width) + Room.x);
            // Set x to be random number within room bounds.
            else
                x = (int) (Math.random() * (Room.width / 2 - width) + Room.x + Room.width / 2);

            y = Panel.roomYBase + Panel.roomYLevel * level + Room.height - height;

            if (level != 3)
                speedMax = Math.min(level + idleSpeed, 2 + idleSpeed); // Speed caps out at level 2.
            else {
                speedMax = Math.max(idleSpeed - 2, 1); // Mummies on level 3 are much slower.
                chaseSpeed = (int) Math.round(Math.random() * 2);
            }

            healthMax = Math.max(1, level);
            if (level == 4)
                healthMax = 1; // Enemies aside from boss on level 4 have low health.
            health = healthMax;

            col = new Rectangle(x, y, width, height);

            launchSpeedMax = 20;

        } else if (isBoss)
            bossEnemy();
        else if (isDummy)
            dummyEnemy();
    }

    public void bossEnemy() {
        enemyImg = new ImageIcon("enemy" + level + "Boss.png").getImage();

        width = enemyImg.getWidth(null);
        height = enemyImg.getHeight(null);

        x = CHUNK * 2;
        y = Panel.roomYBase + Panel.roomYLevel * level + Room.height - height;

        speedMax = 1; // Boss is very slow.
        chaseSpeed = 1;

        healthMax = 24;
        health = healthMax;

        col = new Rectangle(x, y, width, height);

        launchSpeedMax = 8;
    }

    public void dummyEnemy() {
        enemyImg = new ImageIcon("dummy.png").getImage();

        width = enemyImg.getWidth(null);
        height = enemyImg.getHeight(null);

        x = CHUNK * 8;
        y = Panel.HEIGHT / 2 - height;

        speedMax = 0; // Dummy cannot move.
        chaseSpeed = 0;

        healthMax = 2048;
        health = healthMax;

        col = new Rectangle(x, y, width, height);

        launchSpeedMax = 4;
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

        if (isBoss)
            switch (health) {
                case 1:
                    enemyImg = new ImageIcon("enemy" + level + "BossHurt8.png").getImage();
                    break;
                case 2:
                    enemyImg = new ImageIcon("enemy" + level + "BossHurt7.png").getImage();
                    break;
                case 3:
                    enemyImg = new ImageIcon("enemy" + level + "BossHurt6.png").getImage();
                    break;
                case 4:
                    enemyImg = new ImageIcon("enemy" + level + "BossHurt5.png").getImage();
                    break;
                case 6:
                    enemyImg = new ImageIcon("enemy" + level + "BossHurt4.png").getImage();
                    break;
                case 10:
                    enemyImg = new ImageIcon("enemy" + level + "BossHurt3.png").getImage();
                    break;
                case 14:
                    enemyImg = new ImageIcon("enemy" + level + "BossHurt2.png").getImage();
                    break;
                case 18:
                    enemyImg = new ImageIcon("enemy" + level + "BossHurt1.png").getImage();
                    break;
            }

        if (Panel.lastRoom == level || isDummy) { // Activate when player enters enemies level.
            if (growHeight < height) {
                growHeight = growHeight + height / 4; // Increase growheight.
                if (facingLeft)
                    g2D.drawImage(enemyImg, x + width + Panel.damageWobbleX,
                            y + Panel.parallax - growHeight + height + Panel.damageWobbleY,
                            -width + wobble, growHeight, null);
                else
                    g2D.drawImage(enemyImg, x + Panel.damageWobbleX,
                            y + Panel.parallax - growHeight + height + wobble + Panel.damageWobbleY,
                            width, growHeight, null);
            } else if (facingLeft)
                g2D.drawImage(enemyImg, x + width + Panel.damageWobbleX,
                        y + Panel.parallax - growHeight + height + wobble, -width + Panel.damageWobbleY,
                        growHeight, null);
            else
                g2D.drawImage(enemyImg, x + Panel.damageWobbleX,
                        y + Panel.parallax - growHeight + height + wobble + Panel.damageWobbleY,
                        width, growHeight, null);
        } else if (growHeight > 0) {
            growHeight = growHeight - height / 4; // Decrease growheight.
            if (facingLeft)
                g2D.drawImage(enemyImg, x + width + Panel.damageWobbleX,
                        y + Panel.parallax - growHeight + height + Panel.damageWobbleY,
                        -width + wobble, growHeight, null);
            else
                g2D.drawImage(enemyImg, x + Panel.damageWobbleX,
                        y + Panel.parallax - growHeight + height + wobble + Panel.damageWobbleY,
                        width, growHeight, null);
        }

        if (chasing)
            g2D.drawImage(alertImg, x + Panel.damageWobbleX + width / 2 - PIXEL,
                    y + Panel.parallax + wobble + Panel.damageWobbleY - height, null);
    }

    public void move() {
        if (!isDummy)
            y = y + up + gravity + -Math.abs(launchSpeed * 2 / 3);
        x = x + launchSpeed;

        col = new Rectangle(x + colXOffset, y + Panel.parallax + colYOffset, width - colXOffset * 2,
                height - colYOffset * 2);

        if (isBoss || level == 3) // Boss and mummies has double view distance.
            viewCol = new Rectangle(col.x - viewDistance * 2, col.y, col.width + viewDistance * 4, col.height);
        else if (level == 4 || level == 2) // Level 4 enemies sans boss have small view distance.
            viewCol = new Rectangle(col.x - viewDistance / 2, col.y, col.width + viewDistance, col.height);
        else // Other enemies have normal view distance.
            viewCol = new Rectangle(col.x - viewDistance, col.y, col.width + viewDistance * 2, col.height);

        if (!isDummy) {
            damageColLeft = new Rectangle(col.x, col.y + Panel.PIXEL, 1, col.height);
            damageColRight = new Rectangle(col.x + col.width, col.y + Panel.PIXEL, 1, col.height);
        } else {
            damageColLeft = new Rectangle(0, 0, 0, 0);
            damageColRight = new Rectangle(0, 0, 0, 0);
        }

        if (decisionTime < newDecision && !takingAction) // Triggers only when enemy isnt taking an action.
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

        if (launchSpeed != 0)
            if (launchSpeed > 0)
                launchSpeed--; // Constantly decrease launchSpeed if it above 0.
            else
                launchSpeed++;
    }

    public void moveLeft() {
        if (!isDummy) {
            facingLeft = true;
            x = x - speed; // Move left.
            if (col.intersects(Panel.wallLeftCol))
                decision = 2; // Makes enemy move right instead for remaining decision duration.
            wobble = (int) (Math.sin(Panel.gameTime) * 2); // Alternates between -1 and 1.
        }
    }

    public void moveRight() { // Same thing as moveLeft but in opposite direction.
        if (!isDummy) {
            facingLeft = false;
            x = x + speed;
            if (col.intersects(Panel.wallRightCol))
                decision = 1;
            wobble = (int) (Math.sin(Panel.gameTime) * 2);
        }
    }

    public void checkCollisions() {
        if (viewCol.intersects(Panel.playerCol) || health < healthMax && level == Panel.inRoom) {
            // Trigger when player is within enemies line of sight,
            // or when player is in same room as enemy and enemy has taken damage.
            chasing = true;
            decisionTime = decisionMax; // Reset decision.
            speed = speedMax + chaseSpeed; // Increase speed.
            if (viewCol.x + viewCol.width / 2 > Panel.playerCol.x + Panel.playerCol.width / 2) {
                decision = 1; // Move left / right depending on where player is relative to enemy.
            } else {
                decision = 2;
            }
        } else {
            chasing = false;
            speed = speedMax; // Set speed back to normal.
        }

        if (x < Room.x - PIXEL)
            launch(true);
        if (x + width > Room.x + Room.width + PIXEL)
            launch(false);
    }

    public void launch(boolean isLeft) {
        if (isLeft)
            launchSpeed = launchSpeedMax;
        else
            launchSpeed = -launchSpeedMax;
    }
}
/**
 * Panel which runs all graphics.
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

public class Panel extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener {
    // Panel variables.
    Thread gameThread;

    final static int WIDTH = 1400, HEIGHT = 600;

    // Integers.
    static int playerX, playerY, playerWidth, playerHeight;
    int playerBoxXOffset = 8;

    int fogX = 0;
    int fogX2 = -WIDTH; //Position of duplicate fog placed behind the original to create seamless fog movement across screen.
    int fogSpeed = 1;

    int playerSpeed = 10;
    int playerLeft = 0, playerRight = 0, playerUp = 0, playerJump = 0;

    int gravity = 10;

    // Booleans.
    boolean movingLeft = false, movingRight = false, touchingGround = true, playerJumped = false, facingLeft = false;

    // Characters.
    char key;

    // Rectangles
    Rectangle ground = new Rectangle(0, HEIGHT / 2, WIDTH, HEIGHT * 2);
    Rectangle playerBox;

    // Images.
    Image backgroundImg;
    Image fogImg;
    Image playerImg;
    Image playerItemImg;

    // Classes.
    Room room;
    Enemy enemy;

    //ArrayLists
    ArrayList<Projectile> projectile = new ArrayList<Projectile>();


    public Panel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        // this.setBackground(BGColour);

        addKeyListener(this); // Setting up listeners here as they are used throughought the whole game.
        addMouseListener(this);
        addMouseMotionListener(this);

        // Setup images.
        backgroundImg = new ImageIcon("background.png").getImage();
        fogImg = new ImageIcon("fog.png").getImage();
        playerImg = new ImageIcon("player.png").getImage();
        playerItemImg = new ImageIcon("bazooka.png").getImage();


        playerWidth = playerImg.getWidth(null); // Null because theres no specified image observer.
        playerHeight = playerImg.getHeight(null);

        playerX = WIDTH / 2 - playerWidth / 2;
        playerY = -WIDTH / 2;

        playerBox = new Rectangle(0, 0, playerWidth - 16, playerHeight - 4); //X and Y pos determined by moving player.


        gameThread = new Thread(this);
        gameThread.start();
    }

    public void run() // Game loop.
    {
        long lastTime = System.nanoTime();
        double Ticks = 60.0;
        double ns = 1000000000 / Ticks;
        double delta = 0;
        while (true) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            if (delta >= 1) {
                repaint();
                move();
                checkCollisions();
                delta--;
            }
        }
    }

    public void paint(Graphics g) {
        super.paint(g); // Paints the background using the parent class.

        Graphics2D g2D = (Graphics2D) g;

        Toolkit.getDefaultToolkit().sync(); // Supposedly makes game run smoother.

        g2D.drawImage(backgroundImg, 0, 0, null);
        g2D.drawImage(fogImg, fogX, -HEIGHT/2, null);
        g2D.drawImage(fogImg, fogX2, -HEIGHT/2, null);


        g.setColor(new Color(80, 80, 80)); // Paint ground.
        g.fillRect(ground.x, ground.y, ground.width, ground.height);
        g.setColor(new Color(100, 100, 100));
        g2D.setStroke(new BasicStroke(5));
        g.drawLine(ground.x, ground.y + 2, ground.width, ground.y + 2);

        g.setColor(Color.green); //Code to draw player hitbox.
        g2D.setStroke(new BasicStroke(2));
        g.drawRect(playerBox.x, playerBox.y, playerBox.width, playerBox.height);

        // Painting images
        if (facingLeft) {
            g2D.drawImage(playerImg, playerX + playerWidth, playerY, -playerWidth, playerHeight, null);
            g2D.drawImage(playerItemImg, playerX + playerWidth, playerY, -playerWidth, playerHeight, null);
        } else {
            g2D.drawImage(playerImg, playerX, playerY, playerWidth, playerHeight, null);
            g2D.drawImage(playerItemImg, playerX, playerY, playerWidth, playerHeight, null);
        }

        for (int i=0; i<projectile.size(); i++)
        projectile.get(i).paint(g);
    }

    public void menu() {
        startGame();
    }

    public void startGame() {

    }

    public void move() {
        playerX = playerX + playerLeft + playerRight;
        playerY = playerY + playerUp + gravity;

        playerBox.x = playerX + playerBoxXOffset;
        playerBox.y = playerY;

        fogX = fogX + fogSpeed;
        fogX2 = fogX2 + fogSpeed;
        if (fogX >= WIDTH)
        fogX = -WIDTH;
        if (fogX2 >= WIDTH)
        fogX2 = -WIDTH;

        for (int i=0; i<projectile.size(); i++)
        projectile.get(i).move();

        accelarate();
    }

    public void checkCollisions() {
        if (playerJump < 0) // Constantly increase playerjump towards 0 if it is less than 1.
            playerJump++;

        if (playerBox.intersects(ground.x, ground.y - 1, ground.width, ground.height)) {
            touchingGround = true;
            playerUp = playerJump - gravity; // Subtract gravity to counteract its effects locally just within player when touching ground.
        } else {
            touchingGround = false;
            playerUp = playerJump;
        }

        if (playerBox.intersects(ground)) // Checks ground.y + 1 so that player still intersects with ground and doesent get pulled back into ground by gravity.
            playerY--; // Pushes player back up out of the ground, as gravity clips player into ground.
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        // System.out.println(e.getKeyCode());
        switch (e.getKeyCode()) {
            case 65:
                key = 'a';
                movingLeft = true;
                break;
            case 68:
                key = 'd';
                movingRight = true;
                break;
            case 32:
            shoot();
                break;
        }
        if (e.getKeyCode() == 87) {
            if (touchingGround)
                jump();
        }
    }

    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case 65:
                movingLeft = false;
                if(movingRight)
                facingLeft = false;
                break;
            case 68:
                movingRight = false;
                if(movingLeft)
                facingLeft = true;
                break;
            case 87:
                playerJumped = false;
        }
    }

    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}

    public void accelarate() {
        if (movingLeft) {
            if (playerLeft > -playerSpeed)
                if (key == 'a') {
                    playerLeft--;
                    if (!movingRight)
                    facingLeft = true;
                }
        } else if (playerLeft < 0)
            playerLeft++;

        if (movingRight) {
            if (playerRight < playerSpeed)
                if (key == 'd') {
                    playerRight++;
                    if (!movingLeft)
                    facingLeft = false;
                }
        } else if (playerRight > 0)
            playerRight--;
    }

    public void jump() {
        if (!playerJumped) {
            playerJump = -20;
            playerJumped = true;
        }
    }

    public void shoot()
    {
        projectile.add(new Projectile(facingLeft, playerX, playerY, "bazooka"));
        System.out.println(facingLeft);
    }

    public void newRoom() {

    }

    public void newEnemy() {

    }

    public boolean isFocusTraversable() // Lets JPanel accept users input.
    {
        return true;
    }
}
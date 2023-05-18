
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
import java.awt.Cursor;

public class Panel extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener {
    // Panel variables.
    Thread gameThread;

    final static int WIDTH = 1400, HEIGHT = 600, CHUNK = 64;

    // Integers.
    static int playerX, playerY, playerWidth, playerHeight;
    int playerColXOffset = 8, playerColYOffset = 2;

    int fogX = 0;
    int fog2X = -WIDTH; // Position of duplicate fog placed behind the original to create seamless fog
                        // movement across screen.
    int fogSpeed = 1;

    int playerSpeed = 10;
    int playerClimbSpeed = 0;
    int playerLeft = 0, playerRight = 0, playerUp = 0, playerJump = 0;

    int gravity = 10;

    int gameTime = 0;

    // Booleans.
    boolean movingLeft = false, movingRight = false, touchingGround = true, playerJumped = false, facingLeft = false,
            showControlls = true, onLadder = false;

    // Characters.
    char key;

    // Rectangles
    Rectangle groundCol = new Rectangle(0, HEIGHT / 2, WIDTH, HEIGHT * 2); // Collision for ground.
    Rectangle playerCol; // Collision box for player.

    // Images.
    Image backgroundImg, groundImg, fogImg, playerImg, playerItemImg, buttonsImg;

    // Classes.
    Room room;
    Enemy enemy;

    // ArrayLists
    ArrayList<Projectile> projectile = new ArrayList<Projectile>();
    ArrayList<Ladder> ladder = new ArrayList<Ladder>();

    public Panel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        // this.setCursor(HAND_CURSOR);
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!WORK ON
        // LATER!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        addKeyListener(this); // Setting up listeners here as they are used throughought the whole game.
        addMouseListener(this);
        addMouseMotionListener(this);

        // Setup images.
        backgroundImg = new ImageIcon("background.png").getImage();
        groundImg = new ImageIcon("ground.png").getImage();
        fogImg = new ImageIcon("fog.png").getImage();
        playerImg = new ImageIcon("player.png").getImage();
        playerItemImg = new ImageIcon("bazooka.png").getImage();
        // buttonsImg set up later on.

        playerWidth = playerImg.getWidth(null); // Null because theres no specified image observer.
        playerHeight = playerImg.getHeight(null);

        playerX = WIDTH / 2 - playerWidth / 2;
        playerY = -WIDTH / 2;

        playerCol = new Rectangle(0, 0, playerWidth - playerColXOffset * 2, playerHeight - playerColYOffset * 2);
        // X and Y pos determined my moving player.

        menu();
    }

    public void run() // Game loop.
    {
        long lastTime = System.nanoTime();
        double ticks = 60.0;
        double ns = 1000000000 / ticks;
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
                gameTime++;
            }
        }
    }

    public void paint(Graphics g) {
        super.paint(g); // Paints the background using the parent class.

        Graphics2D g2D = (Graphics2D) g;

        Toolkit.getDefaultToolkit().sync(); // Supposedly makes game run smoother.

        // Paint background.
        g2D.drawImage(backgroundImg, 0, 0, null);
        g2D.drawImage(fogImg, fogX, -HEIGHT / 2, null);
        g2D.drawImage(fogImg, fog2X, -HEIGHT / 2, null);
        // Paint foreground.
        g2D.drawImage(groundImg, groundCol.x, groundCol.y, null);
        for (int i = 0; i < ladder.size(); i++)
            ladder.get(i).paint(g);
        
        // Paint UI.
        if (showControlls) {
             // Using Math.sin() gives a value that switches between negative and positive at a controlled rate.
            if (Math.sin(gameTime / 4) > 0) 
                buttonsImg = new ImageIcon("buttons1.png").getImage();
            else
                buttonsImg = new ImageIcon("buttons2.png").getImage();
            g2D.drawImage(buttonsImg, CHUNK/2, CHUNK/2, null);
        }

        g.setColor(Color.green); //Code to draw player hitbox.
        g2D.setStroke(new BasicStroke(2));
        g.drawRect(playerCol.x, playerCol.y, playerCol.width, playerCol.height);

        for (int i = 0; i < ladder.size(); i++)
        {
            g.drawRect(ladder.get(i).ladderCol.x, ladder.get(i).ladderCol.y, ladder.get(i).ladderCol.width, ladder.get(i).ladderCol.height);
        }
        

        // Painting images
        if (facingLeft) {
            g2D.drawImage(playerImg, playerX + playerWidth, playerY, -playerWidth, playerHeight, null);
            g2D.drawImage(playerItemImg, playerX + playerWidth, playerY, -playerWidth, playerHeight, null);
        } else {
            g2D.drawImage(playerImg, playerX, playerY, playerWidth, playerHeight, null);
            g2D.drawImage(playerItemImg, playerX, playerY, playerWidth, playerHeight, null);
        }

        for (int i = 0; i < projectile.size(); i++)
            projectile.get(i).paint(g);
    }

    public void menu() {
        startGame();
    }

    public void startGame() {
        //Upon starting the game, add ladders in nessecary positions to arraylist of ladders.
        ladder.add(new Ladder(CHUNK*18, CHUNK*4)); 

        gameThread = new Thread(this);
        gameThread.start();
    }

    public void move() {
        playerX = playerX + playerLeft + playerRight;
        playerY = playerY + playerUp + gravity + playerClimbSpeed;

        playerCol.x = playerX + playerColXOffset;
        playerCol.y = playerY + playerColYOffset;

        fogX = fogX + fogSpeed;
        fog2X = fog2X + fogSpeed;
        if (fogX >= WIDTH)
            fogX = -WIDTH;
        if (fog2X >= WIDTH)
            fog2X = -WIDTH;

        if (!onLadder)
        playerClimbSpeed = 0; 
        //Define this here so when someone holds down climb the player will stop climbing when they leave a ladder.

        for (int i = 0; i < projectile.size(); i++)
            projectile.get(i).move();

        accelarate();
    }

    public void checkCollisions() {
        if (playerJump < 0) // Constantly increase playerjump towards 0 if it is less than 1.
            playerJump++;

        if (playerCol.intersects(groundCol.x, groundCol.y - 1, groundCol.width, groundCol.height)) {
            touchingGround = true;
            playerUp = playerJump - gravity; // Subtract gravity to counteract its effects locally just within player
                                             // when touching groundCol.
        } else {
            touchingGround = false;
            playerUp = playerJump;
        }

        if (playerCol.intersects(groundCol) && !onLadder) // Checks groundCol.y + 1 so that player still intersects with groundCol and doesent get pulled back into groundCol by gravity.
            playerY--; // Pushes player back up out of the groundCol, as gravity clips player into
                       // groundCol.

        for (int i = 0; i < ladder.size(); i++)
        if (ladder.get(i).ladderCol.contains(playerCol))
        onLadder = true;
        else
        onLadder = false;

        if (onLadder)
        gravity = 0;
        else
        gravity = 10;

        for (int j = 0; j < projectile.size(); j++)
            if (projectile.get(j).X < -WIDTH || projectile.get(j).X > WIDTH * 2)
                projectile.remove(j); //Remove projectiles that travel off the screen.
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
            case 38:
                if (onLadder)
                playerClimbSpeed = -6;
                break;
            case 40:
                if (onLadder)
                playerClimbSpeed = 6;
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
                if (movingRight)
                    facingLeft = false;
                break;
            case 68:
                movingRight = false;
                if (movingLeft)
                    facingLeft = true;
                break;
            case 38:
                playerClimbSpeed = 0;
                break;
            case 40:
                playerClimbSpeed = 0;
                break;
            case 87:
                playerJumped = false;
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

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

    public void shoot() {
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

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
    // World variables.
    Thread gameThread;

    final static int WIDTH = 1400, HEIGHT = 600, CHUNK = 64;

    int gravity = 10;
    int gameTime = 0;

    char key;

    static int parallax = 0;
    static int panYSpeed = 8;
    static int inRoom = -1, lastInRoom = inRoom;

    // Integers.
    static int playerX, playerY, playerWidth, playerHeight;
    int playerColXOffset = 8, playerColYOffset = 2; // X and Y offsets of player collider.

    int playerSpeed = 10, playerJumpHeight = -20, playerClimbSpeed = 0;
    int playerLeft = 0, playerRight = 0, playerUp = 0, playerJump = 0;
    int playerWobble = 0; // Controlls the bobbing up and down of player when walking.

    int fogX = 0;
    int fog2X = -WIDTH; // Duplicate fog placed behind original to create seamless fog movement.
    int fogSpeed = 1;

    // Booleans.
    boolean movingLeft = false, movingRight = false, facingLeft = false;
    boolean touchingGround = true, playerJumped = false;
    boolean onLadder = false, climbingLadder = false, onLadderTop = false, onLadderBottom = false;
    boolean showControlls = true, panYAccelerating = false, panYDone = false;
    boolean inWallLeft = false, inWallRight = false;

    // Rectangles
    Rectangle groundCol = new Rectangle(0, HEIGHT / 2, WIDTH, CHUNK); // Collision for ground.
    Rectangle playerCol; // Collision box for player.
    Rectangle wallLeftCol = new Rectangle(0, 0, CHUNK * 2 + 8, HEIGHT);
    Rectangle wallRightCol = new Rectangle(WIDTH - CHUNK * 2, 0, CHUNK * 2, HEIGHT);

    // Images.
    Image backgroundImg, groundImg, fogImg, buttonsImg;
    Image playerImg, playerOnLadderImg, playerClimbImg, playerItemImg;

    // ArrayLists
    ArrayList<Projectile> projectile = new ArrayList<Projectile>();
    ArrayList<Ladder> ladder = new ArrayList<Ladder>();
    ArrayList<Room> room = new ArrayList<Room>();

    public Panel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(new Color(175, 207, 194));

        addKeyListener(this); // Setting up listeners here as they are used throughought the whole game.
        addMouseListener(this);
        addMouseMotionListener(this);

        // Setup images.
        backgroundImg = new ImageIcon("background.png").getImage();
        groundImg = new ImageIcon("ground.png").getImage();
        fogImg = new ImageIcon("fog.png").getImage();

        playerImg = new ImageIcon("player.png").getImage();
        playerOnLadderImg = new ImageIcon("playerOnLadder.png").getImage();
        playerClimbImg = new ImageIcon("playerClimb.png").getImage();
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

    public void run() { // Game loop.
        long lastTime = System.nanoTime();
        double ticks = 60.0; // Game will refresh 60 times per second.
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
        g2D.drawImage(backgroundImg, 0, parallax / 2, null);
        g2D.drawImage(fogImg, fogX, -HEIGHT / 2 + parallax, null);
        g2D.drawImage(fogImg, fog2X, -HEIGHT / 2 + parallax, null);

        // Paint foreground.
        g.setColor(new Color(39, 46, 69)); // Set colour to ground colour.
        g2D.fillRect(groundCol.x, groundCol.y, groundCol.width, HEIGHT * 16);
        g2D.drawImage(groundImg, groundCol.x, groundCol.y, null);

        for (int i = 0; i < room.size(); i++)
            room.get(i).paint(g);
        for (int i = 0; i < ladder.size(); i++)
            ladder.get(i).paint(g);

        paintUI(g, g2D);
        //paintCol(g, g2D);
        paintPlayer(g, g2D);

        for (int i = 0; i < projectile.size(); i++)
            projectile.get(i).paint(g);
    }

    public void paintUI(Graphics g, Graphics2D g2D) {
        // Paint UI.
        if (showControlls) {
            // Using Math.sin() gives a value that switches between negative and positive at
            // a controlled rate.
            if (Math.sin(gameTime / 4) > 0)
                buttonsImg = new ImageIcon("buttons1.png").getImage();
            else
                buttonsImg = new ImageIcon("buttons2.png").getImage();
            g2D.drawImage(buttonsImg, CHUNK / 2 + parallax * 8, CHUNK / 2, null);
            // When parallax increases, buttons are moved to the side.
        }
    }

    public void paintCol(Graphics g, Graphics2D g2D) {
        // Draw collision boxes
        g.setColor(Color.green); // Code to draw player hitbox.
        g2D.setStroke(new BasicStroke(2));

        g.drawRect(playerCol.x, playerCol.y, playerCol.width, playerCol.height);

        for (int i = 0; i < ladder.size(); i++) {
            g.setColor(Color.blue); // Code to draw player hitbox.
            g.drawRect(ladder.get(i).ladderCol.x, ladder.get(i).ladderCol.y,
                    ladder.get(i).ladderCol.width,
                    ladder.get(i).ladderCol.height);

            g.setColor(Color.yellow); // Code to draw player hitbox.
            g.drawRect(ladder.get(i).ladderTopCol.x, ladder.get(i).ladderTopCol.y,
                    ladder.get(i).ladderTopCol.width,
                    ladder.get(i).ladderTopCol.height);

            g.drawRect(ladder.get(i).ladderBottomCol.x, ladder.get(i).ladderBottomCol.y,
                    ladder.get(i).ladderBottomCol.width, ladder.get(i).ladderBottomCol.height);
        }

        for (int i = 0; i < room.size(); i++) {
            g.setColor(Color.yellow);
            g.drawRect(room.get(i).roomCol.x, room.get(i).roomCol.y, room.get(i).roomCol.width,
                    room.get(i).roomCol.height);
            g.setColor(Color.cyan);
            g.drawRect(room.get(i).floor.x, room.get(i).floor.y, room.get(i).floor.width,
                    room.get(i).floor.height);
        }

        g.setColor(Color.cyan);
        g.drawRect(wallLeftCol.x, wallLeftCol.y, wallLeftCol.width, wallLeftCol.height);
        g.drawRect(wallRightCol.x, wallRightCol.y, wallRightCol.width, wallRightCol.height);
    }

    public void paintPlayer(Graphics g, Graphics2D g2D) {
        // Drawing player.
        if (!onLadder) {
            if (facingLeft) {
                g2D.drawImage(playerImg, playerX + playerWidth, playerY + playerWobble + parallax, -playerWidth,
                        playerHeight,
                        null);
                g2D.drawImage(playerItemImg, playerX + playerWidth, playerY + playerWobble + parallax, -playerWidth,
                        playerHeight,
                        null);
            } else {
                g2D.drawImage(playerImg, playerX, playerY + playerWobble + parallax, playerWidth, playerHeight, null);
                g2D.drawImage(playerItemImg, playerX, playerY + playerWobble + parallax, playerWidth, playerHeight,
                        null);
            }
        } else { // Draw player differently when on ladder.
            if (climbingLadder)
                if (Math.sin(gameTime) > 0)
                    g2D.drawImage(playerClimbImg, playerX, playerY + parallax, playerWidth, playerHeight, null);
                else
                    g2D.drawImage(playerOnLadderImg, playerX, playerY + parallax, playerWidth, playerHeight, null);
            else
                g2D.drawImage(playerOnLadderImg, playerX, playerY + parallax, playerWidth, playerHeight, null);
        }
    }

    public void menu() {
        startGame();
    }

    public void startGame() {
        // Upon starting the game, add ladders to arraylist of ladders.
        ladder.add(new Ladder(CHUNK * 18, CHUNK * 4));
        ladder.add(new Ladder(CHUNK * 3, CHUNK * 8));
        room.add(new Room(CHUNK * 2, CHUNK * 6, 0)); // Start at level 0 as index starts at 0.
        room.add(new Room(CHUNK * 2, CHUNK * 10, 1)); // Start at level 0 as index starts at 0.

        gameThread = new Thread(this);
        gameThread.start();
    }

    public void move() {
        if (inRoom >= 0) // Makes sure pan only triggers when player is in a room.
            if (lastInRoom != inRoom) { // Check to see if player is in a new room.
                panYDone = false;
                if (lastInRoom > inRoom)
                    panY(inRoom, false); // Pan down to specified room.
                else
                    panY(inRoom, true);
                if (panYDone) // Once panYDone is true, set lastInRoom to be inRoom.
                    lastInRoom = inRoom;
            }

        playerX = playerX + playerLeft + playerRight;
        playerY = playerY + playerUp + gravity + playerClimbSpeed;
        playerCol.x = playerX + playerColXOffset;
        playerCol.y = playerY + playerColYOffset + parallax;

        if (movingLeft && !inWallLeft && !inWallLeft || movingRight && !inWallLeft && !inWallRight) {
            playerWobble = (int) (Math.sin(gameTime) * 2);
        } // Alternates between 1 and -1 to create a bobbing up and down motion.

        if (onLadder) {
            playerSpeed = 2;
            playerWobble = 0;
        } else {
            playerSpeed = 10;
        }

        fogX = fogX + fogSpeed;
        fog2X = fog2X + fogSpeed;
        if (fogX >= WIDTH)
            fogX = -WIDTH;
        if (fog2X >= WIDTH)
            fog2X = -WIDTH;

        if (!onLadder)
            playerClimbSpeed = 0;
        // Define this here so when someone holds down climb player will stop climbing
        // when they leave ladder. Needs to be defined here so it's constantly updating.

        for (int i = 0; i < projectile.size(); i++)
            projectile.get(i).move();

        moveCol();
        accelarate();
    }

    public void panY(int level, Boolean up) {
        if (up) { // If up is true.
            if (room.get(level).roomCol.y + room.get(level).roomCol.height / 2 > HEIGHT / 2) {
                panYAccelerating = true;
                // Check center of room against center of screen.
                if (panYSpeed <= 32 && panYAccelerating) { // Make sure panYSpeed is less than / equal to 32.
                    parallax -= panYSpeed; // Reduce parallax.
                    panYSpeed++; // Increase panYSpeed towards maximum of 32.
                } else {
                    panYAccelerating = false;
                    // Set panYAccelerating to be false, stopping above statement re-triggering.
                    if (panYSpeed >= 8) {
                        parallax -= panYSpeed; // Reduce parallax.
                        panYSpeed--; // Increase panYSpeed towards maximum of 32.
                    }
                }
            } else
                panYDone = true; // Once finished panning, set panYDone to true.
        } else { // If up is false.
            if (room.get(level).roomCol.y + room.get(level).roomCol.height / 2 < HEIGHT / 2) {
                // Check center of room against center of screen.
                if (panYSpeed <= 32 && panYAccelerating) { // Make sure panYSpeed is less than / equal to 32.
                    parallax += panYSpeed; // Increase parallax.
                    panYSpeed++; // Increase panYSpeed towards maximum of 32.
                } else {
                    panYAccelerating = false;
                    // Set panYAccelerating to be false, stopping above statement re-triggering.
                    if (panYSpeed >= 8) {
                        parallax += panYSpeed; // Increase parallax.
                        panYSpeed--; // Increase panYSpeed towards maximum of 32.
                    }
                }
            } else
                panYDone = true;
        }

    }

    public void moveCol() { // Adds parallax effect to colliders.
        groundCol = new Rectangle(0, HEIGHT / 2 + parallax, WIDTH, CHUNK);

        for (int i = 0; i < room.size(); i++) {
            room.get(i).roomCol.y = room.get(i).Y + parallax;
            room.get(i).floor.y = room.get(i).Y + room.get(i).HEIGHT + parallax;
        }

        for (int i = 0; i < ladder.size(); i++)
            ladder.get(i).ladderCol.y = ladder.get(i).Y - ladder.get(i).offset * 2 + parallax;
        // Only need to add parallax to ladderCol, as ladderTop and BottomCol are tied
        // to ladderCol.
        for (int i = 0; i < ladder.size(); i++)
            ladder.get(i).ladderTopCol.y = ladder.get(i).ladderCol.y - ladder.get(i).offset;

        for (int i = 0; i < ladder.size(); i++)
            ladder.get(i).ladderBottomCol.y = ladder.get(i).ladderCol.y + ladder.get(i).ladderCol.height - playerHeight;
    }

    public void accelarate() {
        if (movingLeft && !inWallLeft) { // Check direction.
            if (playerLeft > -playerSpeed) // Check if player can accelarate any more.
                if (key == 'a') { // Check to see if key pressed is 'a'.
                    playerLeft--; // Accelarate player up to player speed.
                    if (!movingRight)
                        facingLeft = true;
                }
        } else if (playerLeft < 0)
            playerLeft++;

        if (movingRight && !inWallRight) {
            if (playerRight < playerSpeed)
                if (key == 'd') {
                    playerRight++;
                    if (!movingLeft)
                        facingLeft = false;
                }
        } else if (playerRight > 0)
            playerRight--;
    }

    public void checkCollisions() {
        if (playerCol.intersects(wallLeftCol)) {
            inWallLeft = true; // Stops player from being able to move left.
            playerLeft = 0;
        } else
            inWallLeft = false;

        if (playerCol.intersects(wallRightCol)) {
            inWallRight = true;
            playerRight = 0;
        } else
            inWallRight = false;

        for (int i = 0; i < ladder.size(); i++) {
            if (ladder.get(i).ladderCol.contains(playerCol)) { // Check to see if player is colliding with any of the
                                                               // ladders.
                onLadder = true; // Set onLadder to true. Important this is done before gravity is calculated.
                break; // Important to break. This stops onLadder from being set to false unessesarily.
            } else
                onLadder = false;
        }

        for (int i = 0; i < ladder.size(); i++)
            if (ladder.get(i).ladderTopCol.contains(playerCol)) { // Check to see if player is colliding with tops of
                                                                  // the ladders.
                onLadderTop = true;
                break;
                // Set onLadderTop to true. This variable controlls jittering that happens if
                // player is holiding down buttons when on ladder.
            } else
                onLadderTop = false;

        for (int i = 0; i < ladder.size(); i++)
            if (ladder.get(i).ladderBottomCol.contains(playerCol)) { // Check to see if player is colliding with bottoms
                                                                     // of the ladders.
                onLadderBottom = true;
                break;
                // Set onLadderTop to true. This variable controlls jittering that happens if
                // player is holiding down buttons when on ladder.
            } else
                onLadderBottom = false;

        if (onLadder)
            gravity = 0;
        else
            gravity = 10;

        if (playerJump < 0) // Constantly increase playerjump towards 0 if it is less than 1.
            playerJump++;

        for (int i = 0; i < room.size(); i++)
            if (room.get(i).roomCol.contains(playerCol)) {
                inRoom = room.get(i).level;
                break;
            } else
                inRoom = -1;

        if (playerCol.intersects(groundCol.x, groundCol.y - 1, groundCol.width, groundCol.height)) {
            touchingGround = true;
            playerUp = playerJump - gravity; // Subtract gravity to counteract its effects locally just within player
                                             // when touching groundCol.
        } else
            for (int i = 0; i < room.size(); i++)
                if (playerCol.intersects(room.get(i).floor.x, room.get(i).floor.y - 1, room.get(i).floor.width,
                        room.get(i).floor.height)) {
                    touchingGround = true;
                    playerUp = playerJump - gravity; // Subtract gravity to counteract its effects locally just within
                                                     // player when touching groundCol.
                    break;
                } else {
                    touchingGround = false;
                    playerUp = playerJump;
                }

        if (playerCol.intersects(groundCol) && !onLadder) // Checks groundCol.y + 1 so that player still intersects with
                                                          // groundCol and doesent get pulled back into groundCol by
                                                          // gravity.
            playerY--; // Pushes player back up out of the groundCol, as gravity clips player into
                       // groundCol.

        for (int i = 0; i < room.size(); i++)
            if (playerCol.intersects(room.get(i).floor) && !onLadder)
                playerY--;

        for (int j = 0; j < projectile.size(); j++)
            if (projectile.get(j).X < -WIDTH || projectile.get(j).X > WIDTH * 2)
                projectile.remove(j); // Remove projectiles that travel off the screen.
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        // System.out.println(e.getKeyCode());
        switch (e.getKeyCode()) {
            case 65:
                if (!inWallLeft) {
                    key = 'a';
                    movingLeft = true;
                }
                break;
            case 68:
                if (!inWallRight) {
                    key = 'd';
                    movingRight = true;
                }
                break;
            case 83: // If player on ladder and ladder bottom, player wont fall but can't climb any
                     // lower.
                if (onLadder && !onLadderBottom) {
                    playerClimbSpeed = 6;
                    climbingLadder = true;
                }
                break;
            case 32:
                if (!onLadder)
                    shoot();
                break;
        }
        if (e.getKeyCode() == 87) {
            if (touchingGround && !onLadder) // Player cannot jump while on ladder.
                jump();
            else if (onLadder && !onLadderTop) {
                playerClimbSpeed = -6;
                climbingLadder = true;

            }
            // If player on ladder and ladder top, player wont fall but can not climb any
            // higher.
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
            case 83:
                playerClimbSpeed = 0;
                climbingLadder = false;
                break;
            case 87:
                playerJumped = false;
                playerClimbSpeed = 0;
                climbingLadder = false;
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

    public void jump() {
        if (!playerJumped) { // Player cannot jump in mid-air.
            playerJump = playerJumpHeight;
            playerJumped = true;
        }
    }

    public void shoot() {
        projectile.add(new Projectile(facingLeft, playerX, playerY, "bazooka"));
        // System.out.println(facingLeft);
    }

    public boolean isFocusTraversable() // Lets JPanel accept users input.
    {
        return true;
    }
}
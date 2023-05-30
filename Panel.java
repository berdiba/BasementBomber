
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
    // World variables.
    Thread gameThread;

    final static int WIDTH = 1400, HEIGHT = 600, CHUNK = 64;

    int gravity = 10;
    static int gameTime = 0;

    char key;

    static int parallax = 0;
    static int panYSpeed = 8;
    static int inRoom = -1, lastInRoom = inRoom, roomLevel = -1;
    static int roomX = CHUNK * 2, roomYBase = CHUNK * 6, roomYLevel = CHUNK * 4;

    // Integers.
    static int playerX, playerY, playerWidth, playerHeight;
    int playerColXOffset = 8, playerColYOffset = 2; // x and y offsets of player collider.

    int playerSpeed = 10, playerJumpHeight = -24, playerClimbSpeed = 0;
    int playerLeft = 0, playerRight = 0, playerUp = 0, playerJump = 0;
    int playerWobble = 0; // Controls the bobbing up and down of player when walking.
    int recoil, recoilMax = -6; // Controls weapon x recoil when it shoots.

    int launchSpeed, launchSpeedMax = playerSpeed * 2;

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
    static Rectangle groundCol; // Collision for ground.
    static Rectangle playerCol; // Collision box for player.
    static Rectangle wallLeftCol = new Rectangle(0, -HEIGHT * 8, CHUNK * 2 + 8, HEIGHT * 16);
    static Rectangle wallRightCol = new Rectangle(WIDTH - CHUNK * 2, -HEIGHT * 8, CHUNK * 2, HEIGHT * 16);

    // Images.
    Image backgroundImg, groundImg, fogImg, buttonsImg;
    Image playerImg, playerOnLadderImg, playerClimbImg, playerItemImg;

    // ArrayLists
    static ArrayList<Projectile> projectile = new ArrayList<Projectile>();
    static ArrayList<Room> room = new ArrayList<Room>();
    static ArrayList<Ladder> ladder = new ArrayList<Ladder>();

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
        // x and y pos determined my moving player.

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

        for (int i = 0; i < room.size(); i++) {
            room.get(i).paint(g);
            ladder.get(i).paint(g);
        }
        for (int i = 0; i < room.size(); i++) {
            for (int j = 0; j < room.get(i).enemy.size(); j++)
                room.get(i).enemy.get(j).paint(g); // Paint enemies.
        }

        // Paint projectiles.
        g.setColor(new Color(39, 46, 69));
        g2D.setStroke(new BasicStroke(2));
        for (int i = 0; i < projectile.size(); i++) {
            g.drawRect(projectile.get(i).x, projectile.get(i).y - 1, projectile.get(i).width,
                    projectile.get(i).height + 1); // Draw outline for projectile to make it more visible.
            projectile.get(i).paint(g);
        }

        paintUI(g, g2D);
        //paintCol(g, g2D);
        paintPlayer(g, g2D);
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
            g2D.drawImage(buttonsImg, CHUNK / 2 + parallax * 2, CHUNK / 2, null);
            // When parallax increases, buttons are moved to the side.
        }
    }

    public void paintCol(Graphics g, Graphics2D g2D) {
        /**
         * PLAYER and ENEMIES drawn in GREEN.
         * MAJOR COLLISION BOXES drawn in BLUE.
         * MINOR COLLISION BOXES drawn in CYAN.
         * MAIN-AOE BOXES drawn in YELLOW.
         * SUB-AOE BOXES drawn in ORANGE.
         * DAMAGE BOXES drawn in RED.
         */

        // Draw collision boxes
        g2D.setStroke(new BasicStroke(2));
        g.setColor(Color.green);
        g.drawRect(playerCol.x, playerCol.y, playerCol.width, playerCol.height); // Player hitbox.
        for (int i = 0; i < room.size(); i++)
            for (int j = 0; j < room.get(i).enemy.size(); j++) {
                g.setColor(Color.orange);
                g.drawRect(room.get(i).enemy.get(j).viewCol.x, room.get(i).enemy.get(j).viewCol.y,
                        room.get(i).enemy.get(j).viewCol.width, room.get(i).enemy.get(j).viewCol.height);
                g.setColor(Color.green);
                g.drawRect(room.get(i).enemy.get(j).col.x, room.get(i).enemy.get(j).col.y,
                        room.get(i).enemy.get(j).col.width, room.get(i).enemy.get(j).col.height);
                g.setColor(Color.red);
                g.drawRect(room.get(i).enemy.get(j).col.x, room.get(i).enemy.get(j).col.y,
                        1, room.get(i).enemy.get(j).col.height);
                g.drawRect(room.get(i).enemy.get(j).col.x + room.get(i).enemy.get(j).col.width,
                        room.get(i).enemy.get(j).col.y, 1,
                        room.get(i).enemy.get(j).col.height);
            }

        for (int i = 0; i < ladder.size(); i++) { // Draw all ladder colliders
            g.setColor(Color.orange);
            g.drawRect(ladder.get(i).topCol.x, ladder.get(i).topCol.y, ladder.get(i).topCol.width,
                    ladder.get(i).topCol.height);
            g.drawRect(ladder.get(i).bottomCol.x, ladder.get(i).bottomCol.y,
                    ladder.get(i).bottomCol.width, ladder.get(i).bottomCol.height);
            g.setColor(Color.cyan);
            g.drawRect(ladder.get(i).leftCol.x, ladder.get(i).leftCol.y,
                    ladder.get(i).leftCol.width, ladder.get(i).leftCol.height);
            g.drawRect(ladder.get(i).rightCol.x, ladder.get(i).rightCol.y,
                    ladder.get(i).rightCol.width, ladder.get(i).rightCol.height);
            g.setColor(Color.yellow);
            g.drawRect(ladder.get(i).col.x, ladder.get(i).col.y, ladder.get(i).col.width,
                    ladder.get(i).col.height);
        }

        for (int i = 0; i < room.size(); i++) {
            g.setColor(Color.yellow);
            g.drawRect(room.get(i).col.x, room.get(i).col.y, room.get(i).col.width,
                    room.get(i).col.height);
            g.setColor(Color.blue);
            g.drawRect(room.get(i).floor.x, room.get(i).floor.y, room.get(i).floor.width,
                    room.get(i).floor.height);
        }

        g.setColor(Color.blue);
        g.drawRect(wallLeftCol.x, wallLeftCol.y, wallLeftCol.width, wallLeftCol.height);
        g.drawRect(wallRightCol.x, wallRightCol.y, wallRightCol.width, wallRightCol.height);
    }

    public void paintPlayer(Graphics g, Graphics2D g2D) {
        // Drawing player.
        if (!onLadder) {
            if (facingLeft) {
                g2D.drawImage(playerImg, playerX + playerWidth - recoil / 2, playerY + playerWobble + parallax,
                        -playerWidth,
                        playerHeight,
                        null);
                g2D.drawImage(playerItemImg, playerX + playerWidth - recoil, playerY + playerWobble + parallax,
                        -playerWidth,
                        playerHeight,
                        null);
            } else {
                g2D.drawImage(playerImg, playerX + recoil / 2, playerY + playerWobble + parallax, playerWidth,
                        playerHeight, null);
                g2D.drawImage(playerItemImg, playerX + recoil, playerY + playerWobble + parallax, playerWidth,
                        playerHeight,
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
        ladder.add(new Ladder(CHUNK * 18, CHUNK * 4, 0));
        ladder.add(new Ladder(CHUNK * 3, CHUNK * 8, 1));
        room.add(new Room(roomX, roomYBase + roomYLevel * 0, 0)); // Start at level 0 as index starts at 0.
        room.add(new Room(roomX, roomYBase + roomYLevel * 1, 1));

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

        playerY = playerY + playerUp + gravity + playerClimbSpeed + -Math.abs(launchSpeed * 2 / 3);
        playerX = playerX + playerLeft + playerRight + launchSpeed;
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

        if (recoil < 0)
            recoil++;
        // When recoil is set to be recoilMax, slowly push gun back to original
        // position.

        for (int i = 0; i < room.size(); i++) {
            for (int j = 0; j < room.get(i).enemy.size(); j++)
                room.get(i).enemy.get(j).move(); // Move enemies.
        }

        if (launchSpeed != 0)
            if (launchSpeed > 0)
                launchSpeed--; // Constantly decrease launchSpeed if it above 0.
            else
                launchSpeed++;

        moveCol();
        accelarate();
    }

    public void panY(int level, Boolean up) {
        if (up) { // If up is true.
            if (room.get(level).col.y + room.get(level).col.height / 2 > HEIGHT / 2) {
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
            if (room.get(level).col.y + room.get(level).col.height / 2 < HEIGHT / 2) {
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
            room.get(i).col.y = room.get(i).y + parallax;
            room.get(i).floor.y = room.get(i).y + Room.height + parallax;
            // Enemy col updated in enemy move function.
        }

        for (int i = 0; i < ladder.size(); i++) {
            ladder.get(i).col.y = ladder.get(i).y - ladder.get(i).offset * 2 + parallax;
            ladder.get(i).topCol.y = ladder.get(i).col.y - ladder.get(i).offset;
            ladder.get(i).bottomCol.y = ladder.get(i).col.y + ladder.get(i).col.height - playerHeight;
            ladder.get(i).leftCol.y = ladder.get(i).col.y;
            ladder.get(i).rightCol.y = ladder.get(i).col.y;
            // Parallax added to col, no need to add to ladderLeft or right a seconnd
            // time.
        }
        // Only need to add parallax to col, as ladderTop and BottomCol are tied
        // to col.
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
        // Code for intersecting with left and right colliders.
        if (playerCol.intersects(wallLeftCol)) {
            inWallLeft = true; // Stops player from being able to move left.
            playerLeft = 0;
            launchSpeed = Math.abs(launchSpeed);
        } else
            inWallLeft = false;

        if (playerCol.intersects(wallRightCol)) {
            inWallRight = true;
            playerRight = 0;
            launchSpeed = -Math.abs(launchSpeed);
        } else
            inWallRight = false;

        checkLadderCollisions();
        checkGroundCollisions();

        if (playerJump < 0) // Constantly increase playerjump towards 0 if it is less than 1.
            playerJump++;

        for (int i = 0; i < room.size(); i++)
            if (room.get(i).col.contains(playerCol)) {
                inRoom = room.get(i).level;
                roomLevel = room.get(i).level; // Used for illumination.
                break;
            } else
                inRoom = -1;

        for (int i = 0; i < room.size(); i++)
            for (int j = 0; j < room.get(i).enemy.size(); j++) {
                room.get(i).enemy.get(j).checkCollisions(); // Move enemies.

                if (room.get(i).enemy.get(j).col.intersects(room.get(i).floor.x, room.get(i).floor.y - 1,
                        room.get(i).floor.width, room.get(i).floor.height)) {
                    // Check enemy col against room floor pushed down 1;
                    room.get(i).enemy.get(j).up = -room.get(i).enemy.get(j).gravity; // Nullify enemy gravity.
                } else
                    room.get(i).enemy.get(j).up = 0; // Let enemy fall back down to ground.

                if (room.get(i).enemy.get(j).col.intersects(room.get(i).floor))
                    room.get(i).enemy.get(j).y--; // Make sure enemy doesent get stuck in ground.

                if (playerCol.intersects(room.get(i).enemy.get(j).col.x, room.get(i).enemy.get(j).col.y,
                        1, room.get(i).enemy.get(j).col.height)) {
                    damage(false);
                }
                if (playerCol.intersects(room.get(i).enemy.get(j).col.x + room.get(i).enemy.get(j).col.width,
                        room.get(i).enemy.get(j).col.y, 1,
                        room.get(i).enemy.get(j).col.height)) {
                    damage(true);
                }
            }

        for (int j = 0; j < projectile.size(); j++)
            if (projectile.get(j).x < -WIDTH || projectile.get(j).x > WIDTH * 2)
                projectile.remove(j); // Remove projectiles that travel off the screen.
    }

    public void checkLadderCollisions() {
        // Code for intersecting with left and right ladder colliders.
        for (int i = 0; i < ladder.size(); i++) {
            if (playerCol.intersects(ladder.get(i).leftCol) && inRoom < 0
                    && playerCol.y + playerCol.width > groundCol.y) {
                inWallLeft = true; // Stops player from being able to move left.
                playerLeft = 0;
                playerX++;
            } else
                inWallLeft = false;

            if (playerCol.intersects(ladder.get(i).rightCol) && inRoom < 0
                    && playerCol.y + playerCol.width > groundCol.y) {
                inWallRight = true;
                playerRight = 0;
                playerX--;
            } else
                inWallRight = false;
        }

        for (int i = 0; i < ladder.size(); i++) {
            if (ladder.get(i).col.contains(playerCol)) { // Check to see if player is colliding with any of the
                                                         // ladders.
                onLadder = true; // Set onLadder to true. Important this is done before gravity is calculated.
                break; // Important to break. This stops onLadder from being set to false unessesarily.
            } else
                onLadder = false;
        }

        for (int i = 0; i < ladder.size(); i++)
            if (ladder.get(i).topCol.contains(playerCol)) { // Check to see if player is colliding with tops of
                                                            // the ladders.
                onLadderTop = true;
                break;
                // Set onLadderTop to true. This variable controlls jittering that happens if
                // player is holiding down buttons when on ladder.
            } else
                onLadderTop = false;

        for (int i = 0; i < ladder.size(); i++)
            if (ladder.get(i).bottomCol.contains(playerCol)) { // Check to see if player is colliding with bottoms
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
    }

    public void checkGroundCollisions() {
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

        if (playerCol.intersects(groundCol) && !onLadder)
            // Checks groundCol.y + 1 so that player still intersects with and doesent get
            // pulled back into groundCol by gravity.
            playerY--;
        // Pushes player back up out of the groundCol, as gravity clips player into
        // groundCol.

        for (int i = 0; i < room.size(); i++)
            if (playerCol.intersects(room.get(i).floor) && !onLadder)
                playerY--;
    }

    public void damage(Boolean isLeft) {
        if (isLeft) {
            launchSpeed = launchSpeedMax;
            playerLeft = 0;
            playerRight = 0;
        } else {
            launchSpeed = -launchSpeedMax;
            playerLeft = 0;
            playerRight = 0;
        }
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
        recoil = recoilMax; // This pushes gun backwards by recoilMax pixels.
    }

    public boolean isFocusTraversable() // Lets JPanel accept users input.
    {
        return true;
    }
}
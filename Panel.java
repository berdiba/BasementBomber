
/**
 * Panel which manages most of the game.
 *
 * @author BAXTER BERDINNER
 * @version 20/02/2023
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.lang.Math;
// import java.awt.geom.*; // Remove later on if never used.
import java.util.ArrayList;

public class Panel extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener {
    // World variables.
    Thread gameThread;

    final static int WIDTH = 1400, HEIGHT = 600, PIXEL = 4, CHUNK = PIXEL * 16;

    static int gravityMax = 10, gravity = gravityMax;
    static int gameTime = 0;

    static char key; // Used for player movement left / right.

    // Images.
    static Image backgroundImg = new ImageIcon("background.png").getImage();
    static Image groundImg = new ImageIcon("ground.png").getImage();
    static Image fogImg = new ImageIcon("fog.png").getImage();

    static Image playerImg = new ImageIcon("player.png").getImage();
    static Image playerOnLadderImg = new ImageIcon("playerOnLadder.png").getImage();
    static Image playerClimbImg = new ImageIcon("playerClimb.png").getImage();
    static Image playerItemImg = new ImageIcon("bazooka.png").getImage();

    static Image heartEmptyImg = new ImageIcon("heartEmpty.png").getImage();
    static Image heartFullImg = new ImageIcon("heartFull.png").getImage();
    static Image heartEmptyRedImg = new ImageIcon("heartEmptyRed.png").getImage();
    static Image heartFullRedImg = new ImageIcon("heartFullRed.png").getImage();

    static Image reloadBarEmptyImg = new ImageIcon("reloadBarEmpty.png").getImage();
    static Image reloadBarFullImg = new ImageIcon("reloadBarFull.png").getImage();
    static Image reloadBarRedImg = new ImageIcon("reloadBarRed.png").getImage();

    static Image dashBarEmptyImg = new ImageIcon("dashBarEmpty.png").getImage();
    static Image dashBarFullImg = new ImageIcon("dashBarFull.png").getImage();
    static Image dashBarRedImg = new ImageIcon("dashBarRed.png").getImage();
    static Image buttonsImg; // buttonsImg set up later on.

    // Integers.
    static int parallax = 0;
    static int panYSpeed = 8;
    static int inRoom = -1, lastInRoom = inRoom, roomLevel = -1;
    static int roomX = CHUNK * 2, roomYBase = CHUNK * 6, roomYLevel = CHUNK * 4;

    static int playerX, playerY, playerWidth, playerHeight;
    static int playerColXOffset = 8, playerColYOffset = 2; // x and y offsets of player collider.

    static int playerSpeedMax = 10, playerSpeed = playerSpeedMax, playerJumpHeight = -24;
    static int playerSpeedLadder = 2, playerClimbSpeed = 0;
    static int playerLeft = 0, playerRight = 0, playerUp = 0, playerJump = 0;
    static int playerWobble = 0; // Controls the bobbing up and down of player when walking.

    static int dashResetCooldownTime = 60, dashResetCooldown = -dashResetCooldownTime;
    static int dashCooldownTime = 10, dashCooldown = -dashCooldownTime;
    static int dashSpeedMax = playerSpeed * 2, dashSpeed = 0;

    static int recoil, recoilMax = -6; // Controls weapon x recoil when it shoots.
    static int shootCooldown = gameTime, shootCooldownTime = 120; // CooldownTime measured in ticks.
    static int reloadBarWidth = reloadBarEmptyImg.getWidth(null), reloadBarHeight = reloadBarEmptyImg.getHeight(null);
    static int dashBarWidth = dashBarEmptyImg.getWidth(null), dashBarHeight = dashBarEmptyImg.getHeight(null);

    static int healthMax = 600, health = healthMax, healthCooldown = gameTime;
    static int healthWidth = (CHUNK + CHUNK / 8) * healthMax + CHUNK / 4;
    static int damageWobbleX, damageWobbleY;
    static int damageFlashMax = 8, damageFlash;
    static int UIParallax = -healthWidth;

    static int particlesDensity = 512, particlesMax = 4, colorMod;
    // particlesDensity inversely proportional to particles.

    static int launchSpeed, launchSpeedMax = playerSpeedMax * 2;

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
    static Rectangle playerCol; // Collision box for player.
    static Rectangle groundCol; // Collision for ground.
    static Rectangle wallLeftCol = new Rectangle(0, -HEIGHT * 8, CHUNK * 2 + 8, HEIGHT * 16);
    static Rectangle wallRightCol = new Rectangle(WIDTH - CHUNK * 2, -HEIGHT * 8, CHUNK * 2, HEIGHT * 16);

    // Colors.
    Color playerBlood;
    Color enemyBlood;
    Color blast;
    Color wood;

    // ArrayLists
    static ArrayList<Projectile> projectile = new ArrayList<Projectile>();
    static ArrayList<Room> room = new ArrayList<Room>();
    static ArrayList<Ladder> ladder = new ArrayList<Ladder>();
    static ArrayList<Particles> particles = new ArrayList<Particles>();

    public Panel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(new Color(175, 207, 194)); // Sets background colour to be teal.

        addKeyListener(this); // Setting up listeners here as they are used throughought the whole game.
        addMouseListener(this);
        addMouseMotionListener(this);

        setVariables();

        menu();
    }

    public void setVariables() { // Setup some variables related to images here.
        playerWidth = playerImg.getWidth(null); // Null because theres no specified image observer.
        playerHeight = playerImg.getHeight(null);

        playerX = WIDTH / 2 - playerWidth / 2;
        playerY = -WIDTH / 2;

        playerCol = new Rectangle(0, 0, playerWidth - playerColXOffset * 2, playerHeight - playerColYOffset * 2);
        // x and y pos determined my moving player.
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

    public void run() { // Game loop.
        long lastTime = System.nanoTime();
        double ticks = 60.0; // Game will refresh 60.0 times per second.
        double ns = 1000000000 / ticks;
        double delta = 0;
        while (true) { // Constantly run.
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

    public void paint(Graphics g) { // Controlls all graphics.
        super.paint(g); // Paints the background using the parent class.

        Graphics2D g2D = (Graphics2D) g;

        Toolkit.getDefaultToolkit().sync(); // Supposedly makes game run smoother.

        paintBackground(g, g2D);
        paintForeground(g, g2D);
        paintProjectiles(g, g2D);
        paintPlayer(g, g2D);
        paintParticles(g, g2D);
        // paintCol(g, g2D);
        paintUI(g, g2D); // Do this last, as UI renders ontop of everything else.
    }

    public void paintBackground(Graphics g, Graphics2D g2D) { // Paints background and fog.
        g2D.drawImage(backgroundImg, 0, parallax / 4, null);
        g2D.drawImage(fogImg, fogX, -HEIGHT / 2 + parallax, null);
        g2D.drawImage(fogImg, fog2X, -HEIGHT / 2 + parallax, null);
    }

    public void paintForeground(Graphics g, Graphics2D g2D) { // Paints ground, rooms, ladders, enemies.
        g.setColor(new Color(39, 46, 69)); // Set colour to ground colour.
        g2D.fillRect(groundCol.x + damageWobbleX, groundCol.y + damageWobbleY, groundCol.width, HEIGHT * 16);
        g2D.drawImage(groundImg, groundCol.x + damageWobbleX + CHUNK, groundCol.y + damageWobbleY, null);

        for (int i = 0; i < room.size(); i++) {
            // Creates temp variable i, runs code until i is no longer < than room.size().
            room.get(i).paint(g); // For every room i, call its paint method using Graphics g.
            ladder.get(i).paint(g);
        }
        for (int i = 0; i < room.size(); i++) {
            for (int j = 0; j < room.get(i).enemy.size(); j++)
                room.get(i).enemy.get(j).paint(g); // Paint enemies.
        }
    }

    public void paintProjectiles(Graphics g, Graphics2D g2D) { // Paints player projectiles.
        g.setColor(new Color(39, 46, 69));
        g2D.setStroke(new BasicStroke(2));
        for (int i = 0; i < projectile.size(); i++)
            projectile.get(i).paint(g);
    }

    public void paintPlayer(Graphics g, Graphics2D g2D) { // Paints player.
        if (!onLadder) {
            if (facingLeft) { // Player faces direction of travel.
                g2D.drawImage(playerImg, playerX + playerWidth - recoil / 2, playerY + playerWobble + parallax,
                        -playerWidth, playerHeight, null);
                g2D.drawImage(playerItemImg, playerX + playerWidth - recoil, playerY + playerWobble + parallax,
                        -playerWidth, playerHeight, null);
            } else {
                g2D.drawImage(playerImg, playerX + recoil / 2, playerY + playerWobble + parallax, playerWidth,
                        playerHeight, null);
                g2D.drawImage(playerItemImg, playerX + recoil, playerY + playerWobble + parallax, playerWidth,
                        playerHeight, null);
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

    public void paintParticles(Graphics g, Graphics2D g2D) { // Paints particles.
        for (int i = 0; i < particles.size(); i++)
            particles.get(i).paint(g);
    }

    public void paintCol(Graphics g, Graphics2D g2D) { // Paints collision boxes for everything.
        /**
         * PLAYER and ENEMIES drawn in GREEN.
         * MAJOR COLLISION BOXES drawn in BLUE.
         * MINOR COLLISION BOXES drawn in CYAN.
         * MAIN-AOE BOXES drawn in YELLOW.
         * SUB-AOE BOXES drawn in ORANGE.
         * DAMAGE BOXES drawn in RED.
         * PARTICLES and PROJECTILES drawn in PINK.
         */

        // Draw collision boxes
        g2D.setStroke(new BasicStroke(2));

        g.setColor(Color.blue);
        g.drawRect(wallLeftCol.x, wallLeftCol.y, wallLeftCol.width, wallLeftCol.height);
        g.drawRect(wallRightCol.x, wallRightCol.y, wallRightCol.width, wallRightCol.height);

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

        for (int i = 0; i < room.size(); i++) { // Draw room colliders
            g.setColor(Color.blue); // Floor and ceiling colliders.
            g.drawRect(room.get(i).ceiling.x, room.get(i).ceiling.y, room.get(i).ceiling.width,
                    room.get(i).ceiling.height);
            g.drawRect(room.get(i).floor.x, room.get(i).floor.y, room.get(i).floor.width,
                    room.get(i).floor.height);
            g.setColor(Color.yellow); // Room collider.
            g.drawRect(room.get(i).col.x, room.get(i).col.y, room.get(i).col.width,
                    room.get(i).col.height);
        }

        g.setColor(Color.green); // Player hitbox.
        g.drawRect(playerCol.x, playerCol.y, playerCol.width, playerCol.height);
        for (int i = 0; i < room.size(); i++)
            for (int j = 0; j < room.get(i).enemy.size(); j++) {
                g.setColor(Color.orange); // Enemy view distance.
                g.drawRect(room.get(i).enemy.get(j).viewCol.x, room.get(i).enemy.get(j).viewCol.y,
                        room.get(i).enemy.get(j).viewCol.width, room.get(i).enemy.get(j).viewCol.height);
                g.setColor(Color.green); // Enemy hitbox.
                g.drawRect(room.get(i).enemy.get(j).col.x, room.get(i).enemy.get(j).col.y,
                        room.get(i).enemy.get(j).col.width, room.get(i).enemy.get(j).col.height);
                g.setColor(Color.red); // Enemy damage collider.
                g.drawRect(room.get(i).enemy.get(j).damageColLeft.x, room.get(i).enemy.get(j).damageColLeft.y,
                        room.get(i).enemy.get(j).damageColLeft.width, room.get(i).enemy.get(j).damageColLeft.height);
                g.drawRect(room.get(i).enemy.get(j).damageColRight.x, room.get(i).enemy.get(j).damageColRight.y,
                        room.get(i).enemy.get(j).damageColRight.width, room.get(i).enemy.get(j).damageColRight.height);
            }

        g.setColor(Color.pink);
        for (int i = 0; i < particles.size(); i++)
            g.drawRect(particles.get(i).col.x, particles.get(i).col.y, particles.get(i).col.width,
                    particles.get(i).col.height);
        for (int i = 0; i < projectile.size(); i++)
            g.drawRect(projectile.get(i).col.x, projectile.get(i).col.y, projectile.get(i).col.width,
                    projectile.get(i).col.height);
    }

    public void paintUI(Graphics g, Graphics2D g2D) { // Paints user interface.
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

        if (damageFlash > 0) {
            for (int i = 0; i < healthMax; i++)
                g2D.drawImage(heartEmptyRedImg, (CHUNK + CHUNK / 8) * i + CHUNK / 4 + damageWobbleX + UIParallax,
                        CHUNK / 4 + damageWobbleY, null);
            for (int i = 0; i < health; i++)
                g2D.drawImage(heartFullRedImg, (CHUNK + CHUNK / 8) * i + CHUNK / 4 + damageWobbleX + UIParallax,
                        CHUNK / 4 + damageWobbleY, null);
        } else {
            for (int i = 0; i < healthMax; i++)
                g2D.drawImage(heartEmptyImg, (CHUNK + CHUNK / 8) * i + CHUNK / 4 + UIParallax, CHUNK / 4, null);
            for (int i = 0; i < health; i++)
                g2D.drawImage(heartFullImg, (CHUNK + CHUNK / 8) * i + CHUNK / 4 + UIParallax, CHUNK / 4, null);
        }

        g2D.drawImage(reloadBarEmptyImg, WIDTH - reloadBarWidth - CHUNK / 4, CHUNK / 4, null);
        if (shootCooldown < gameTime - shootCooldownTime) {
            g2D.drawImage(reloadBarFullImg, WIDTH - reloadBarWidth - CHUNK / 4, CHUNK / 4, null);
        } else {
            g.setColor(new Color(78, 110, 96));
            g.fillRect(WIDTH - reloadBarWidth - CHUNK / 4 + PIXEL, CHUNK / 4 + PIXEL * 2,
                    ((gameTime - shootCooldown) * reloadBarWidth / shootCooldownTime) - PIXEL * 2,
                    reloadBarHeight - PIXEL * 3);
        }

        g2D.drawImage(dashBarEmptyImg, WIDTH - dashBarWidth - CHUNK / 4, reloadBarHeight + CHUNK / 2, null);
        if (dashCooldown < gameTime - dashResetCooldownTime) {
            g2D.drawImage(dashBarFullImg, WIDTH - dashBarWidth - CHUNK / 4, reloadBarHeight + CHUNK / 2, null);
        } else {
            g.setColor(new Color(78, 110, 96));
            g.fillRect(WIDTH - dashBarWidth - CHUNK / 4 + PIXEL, reloadBarHeight + CHUNK / 2 + PIXEL * 2,
                    ((gameTime - dashResetCooldown) * (dashBarWidth + PIXEL * 16) / dashResetCooldownTime) - PIXEL * 2,
                    dashBarHeight - PIXEL * 3);
        }
    }

    public void move() {
        // Update player position.
        playerY = playerY + playerUp + gravity + playerClimbSpeed + -Math.abs(launchSpeed * 2 / 3);
        playerX = playerX + playerLeft + playerRight + dashSpeed + launchSpeed;
        playerCol.x = playerX + playerColXOffset;
        playerCol.y = playerY + playerColYOffset + parallax;

        if (movingLeft && !inWallLeft && !inWallLeft || movingRight && !inWallLeft && !inWallRight) {
            playerWobble = (int) (Math.sin(gameTime) * 2);
        } // Alternates between 1 and -1 to create a bobbing up and down motion.

        if (damageFlash > 0) {
            damageWobbleX = (int) (Math.sin(gameTime) * 3);
            damageWobbleY = (int) (Math.sin(gameTime + 1) * 3);
        } else {
            damageWobbleX = 0;
            damageWobbleY = 0;
        }

        if (onLadder) {
            playerSpeed = playerSpeedLadder;
            playerWobble = 0;
        } else {
            playerSpeed = playerSpeedMax;
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

        for (int i = 0; i < particles.size(); i++)
            particles.get(i).move();

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
        dash();
    }

    public void moveCol() { // Adds parallax effect to colliders.
        groundCol = new Rectangle(-CHUNK, HEIGHT / 2 + parallax, WIDTH + CHUNK * 2, CHUNK);

        for (int i = 0; i < room.size(); i++) {
            room.get(i).col.y = room.get(i).y + parallax;
            room.get(i).ceiling.y = room.get(i).y - CHUNK / 2 + parallax;
            room.get(i).floor.y = room.get(i).y + Room.height + parallax;
            // Enemy col updated in enemy move function.
        }

        for (int i = 0; i < ladder.size(); i++) {
            ladder.get(i).col.y = ladder.get(i).y - ladder.get(i).offset * 2 + parallax;
            ladder.get(i).topCol.y = ladder.get(i).col.y - ladder.get(i).offset;
            ladder.get(i).bottomCol.y = ladder.get(i).col.y + ladder.get(i).col.height - playerHeight;
            ladder.get(i).leftCol.y = ladder.get(i).col.y;
            ladder.get(i).rightCol.y = ladder.get(i).col.y;
            // Parallax added to col, no need to add to ladderLeft or right a second time.
            // Only need to add parallax to col, as ladderTop and BottomCol are tied to col.
        }
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

    public void dash() {
        if (dashCooldown > gameTime - dashCooldownTime) {
            if (facingLeft) // Dash in the direction player is facing.
                dashSpeed = -dashSpeedMax;
            else
                dashSpeed = dashSpeedMax;
            dashResetCooldown = gameTime; // Reset dashResetCoolDown.
        } else
            dashSpeed = dashSpeed * 3 / 4;
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

        if (playerCol.intersects(wallLeftCol.x - playerSpeed, wallLeftCol.y, wallLeftCol.width, wallLeftCol.height)) {
            playerX = playerX + playerWidth; // When player is launched into the wall, force it back out.
        }
        if (playerCol.intersects(wallRightCol.x + playerSpeed, wallRightCol.y, wallRightCol.width,
                wallRightCol.height)) {
            playerX = playerX - playerWidth;
        }

        for (int j = 0; j < particles.size(); j++) {
            if (particles.get(j).col.intersects(wallLeftCol)) {
                particles.get(j).xSpeed = Math.abs(particles.get(j).xSpeed);
            }
            if (particles.get(j).col.intersects(wallRightCol))
                particles.get(j).xSpeed = -Math.abs(particles.get(j).xSpeed);
        }

        checkPan();
        checkLadderCollisions();
        checkGroundCollisions();
        checkEnemyCollisions();
        checkProjectileCollisions();

        if (playerJump < 0) // Constantly increase playerjump towards 0 if it is less than 1.
            playerJump++;
        for (int i = 0; i < room.size(); i++)
            if (room.get(i).col.contains(playerCol)) {
                inRoom = room.get(i).level;
                roomLevel = room.get(i).level; // Used for illumination.
                break;
            } else
                inRoom = -1;

        killStrayProjectiles();
        killOldParticles();
    }

    public void checkPan() { // Detects when panY should be triggered.
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
    }

    public void panY(int level, Boolean up) { // Moves camera up / down to room players in.
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

        if (-parallax < (healthWidth / 4))
            UIParallax = -parallax * 4 - healthWidth + 16;
    }

    public void checkLadderCollisions() { // Collisions between player and ladders.
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
            gravity = 0; // So that player can stay still while on ladder.
        else
            gravity = gravityMax;
    }

    public void checkGroundCollisions() { // Collisions between player and ground and ceilings.
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

        for (int i = 0; i < room.size(); i++) {
            if (playerCol.intersects(room.get(i).floor) && !onLadder)
                playerY--;
            if (playerCol.intersects(room.get(i).ceiling) && !onLadder) {
                launchSpeed = -Math.abs(launchSpeed);
                playerJump = -Math.abs(playerJump);
                playerY = playerY + playerWidth;
            }

            for (int j = 0; j < particles.size(); j++)
                if (particles.get(j).col.intersects(room.get(i).floor)) { // Stop particles from phasing through floor.
                    particles.get(j).ySpeed = -Math.abs(particles.get(j).ySpeed / 2);
                    particles.get(j).xSpeed = particles.get(j).xSpeed / 2;
                }
        }

        for (int i = 0; i < ladder.size(); i++) // Run through every ladder.
            if (inRoom == ladder.get(i).level && touchingGround) {
                // Checks if player is touching ground in the same room as specified ladder.
                if (!ladder.get(i).ladderBroken)
                    for (int j = 0; j < (ladder.get(i).col.width * ladder.get(i).col.height) / particlesDensity
                            * 8; j++) {
                        colorMod = (int) (Math.random() * 40);
                        wood = new Color(120 + colorMod * 2, 50 + colorMod * 2, 40 + colorMod * 2);
                        particles.add(new Particles(ladder.get(i).x,
                                ladder.get(i).y + ladder.get(i).col.height / 2, ladder.get(i).col.width / 2,
                                ladder.get(i).col.height / 2, 0, -4 - (int) (Math.random() * 8), wood,
                                120, 1, 0, true, true));
                        ladder.get(i).col.x = WIDTH * 2; // Teleport ladder off screen, essensially removing it.
                        ladder.get(i).ladderBroken = true;
                    }
            }
    }

    public void checkEnemyCollisions() { // Manages enemy collisions with ground, and hitting player.
        for (int i = 0; i < room.size(); i++)
            for (int j = 0; j < room.get(i).enemy.size(); j++) {
                room.get(i).enemy.get(j).checkCollisions(); // Check enemy collisions.

                if (room.get(i).enemy.get(j).col.intersects(room.get(i).floor.x, room.get(i).floor.y - 1,
                        room.get(i).floor.width, room.get(i).floor.height)) {
                    // Check enemy col against room floor pushed down 1;
                    room.get(i).enemy.get(j).up = -room.get(i).enemy.get(j).gravity; // Nullify enemy gravity.
                } else
                    room.get(i).enemy.get(j).up = 0; // Let enemy fall back down to ground.

                if (room.get(i).enemy.get(j).col.intersects(room.get(i).floor))
                    room.get(i).enemy.get(j).y--; // Make sure enemy doesent get stuck in ground.

                if (playerCol.intersects(room.get(i).enemy.get(j).damageColLeft)) {
                    damage(false);
                }
                if (playerCol.intersects(room.get(i).enemy.get(j).damageColRight)) {
                    damage(true);
                }
            }
        if (damageFlash > 0)
            damageFlash--; // Decrease damageFlash to 0.
    }

    public void damage(Boolean isLeft) {
        damageFlash = damageFlashMax; // Set damageFlash to its maximum value.
        playerJump = 0; // Stops player from being able to be launched and jump at the same time.
        if (isLeft) {
            launchSpeed = launchSpeedMax; // Launches the player left.
            playerLeft = 0;
            playerRight = 0;
            for (int i = 0; i < (playerWidth * playerHeight) / particlesDensity; i++) {

                playerBlood = new Color((colorMod * 2 + 110), 20, 20);
                particles.add(new Particles(playerX, playerY, playerWidth, playerHeight, 10, -10,
                        playerBlood, 60, 1, 0.2f, true, true));
            }
        } else {
            launchSpeed = -launchSpeedMax;
            playerLeft = 0;
            playerRight = 0;
            for (int i = 0; i < (playerWidth * playerHeight) / particlesDensity; i++) {

                playerBlood = new Color((colorMod * 2 + 110), 20, 20);
                particles.add(new Particles(playerX, playerY, playerWidth, playerHeight, -10, -10,
                        playerBlood, 60, 1, 0.2f, true, true));
            }
        }
        if (health > 1 && healthCooldown < gameTime - 30) {
            health--;
            healthCooldown = gameTime;
        } else if (health == 1)
            kill();
    }

    public void kill() {
        System.exit(0);
    }

    public void checkProjectileCollisions() {
        for (int i = 0; i < projectile.size(); i++)
            for (int j = 0; j < room.size(); j++)
                for (int k = 0; k < room.get(j).enemy.size(); k++) { // Run thru every projectile and enemy per level.
                    if (room.get(j).enemy.get(k).col.intersects(projectile.get(i).col)) {

                        if (projectile.get(i).col.intersects(room.get(j).enemy.get(k).col.x,
                                room.get(j).enemy.get(k).col.y,
                                1, room.get(j).enemy.get(k).col.height))
                            for (int l = 0; l < (playerWidth * playerHeight) / particlesDensity * 16; l++) {

                                colorMod = (int) (Math.random() * 40); // Adds random variation to particle colours.
                                if (room.get(j).level == 1)
                                    enemyBlood = new Color(20, (100 - colorMod), (colorMod + 110));
                                else
                                    enemyBlood = new Color((colorMod * 2 + 110), 20, 20);

                                particles.add(new Particles(room.get(j).enemy.get(k).x, room.get(j).enemy.get(k).y,
                                        room.get(j).enemy.get(k).width, room.get(j).enemy.get(k).height, 10, -10,
                                        enemyBlood, 60, 1, 0.2f, true, true));
                            }
                        else if (projectile.get(i).col.intersects(room.get(j).enemy.get(k).col.x +
                                room.get(j).enemy.get(k).col.width, room.get(j).enemy.get(k).col.y, 1,
                                room.get(j).enemy.get(k).col.height))
                            for (int l = 0; l < (playerWidth * playerHeight) / particlesDensity * 16; l++) {

                                colorMod = (int) (Math.random() * 40);
                                if (room.get(j).level == 1)
                                    enemyBlood = new Color(20, (100 - colorMod), (colorMod + 110));
                                else
                                    enemyBlood = new Color((colorMod * 2 + 110), 20, 20);

                                particles.add(new Particles(room.get(j).enemy.get(k).x, room.get(j).enemy.get(k).y,
                                        room.get(j).enemy.get(k).width, room.get(j).enemy.get(k).height, -10, -10,
                                        enemyBlood, 60, 1, 0.2f, true, true));
                            }
                        room.get(j).enemy.remove(k); // Remove enemy.
                        projectile.get(i).x = WIDTH * 4; // Send it off screen to get killed.
                    }
                }
    }

    public void killStrayProjectiles() {
        for (int i = 0; i < projectile.size(); i++)
            if (projectile.get(i).x < -WIDTH || projectile.get(i).x > WIDTH * 2)
                projectile.remove(i); // Remove projectiles that travel off the screen.
    }

    public void killOldParticles() { // Removes particles.
        for (int i = 0; i < particles.size(); i++)
            if (particles.get(i).age >= particles.get(i).ageMax)
                particles.remove(i); // Remove projectiles that travel off the screen.
        if (particles.size() > particlesMax)
            particles.remove(0); // If too many particles on screen, remove the first particles in arrayList.
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
            case 16:
                if (dashResetCooldown < gameTime - dashResetCooldownTime && !onLadder) {
                    // Only triggers when dashResetCooldown has surpassed dashResetCooldownTime
                    dashCooldown = gameTime; // Reset dashCooldown, making player dash.
                }
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

    public void shoot() {
        if (shootCooldown < gameTime - shootCooldownTime) {
            projectile.add(new Projectile(facingLeft, playerX, playerY, "bazooka"));
            recoil = recoilMax; // This pushes gun backwards by recoilMax pixels.
            for (int i = 0; i < (playerWidth * playerHeight) / particlesDensity * 2; i++)
                if (facingLeft) {

                    colorMod = (int) (Math.random() * 40);
                    blast = new Color(colorMod * 2 + 170, colorMod * 2 + 120, colorMod * 2 + 60);
                    particles.add(new Particles(playerX + playerWidth * 3 / 4, playerY + playerHeight / 2,
                            16, 8, 20, 1, blast,
                            10, 6, 2, false, true));
                } else {
                    colorMod = (int) (Math.random() * 40);
                    blast = new Color(colorMod * 2 + 170, colorMod * 2 + 120, colorMod * 2 + 60);
                    particles.add(new Particles(playerX + playerWidth / 4, playerY + playerHeight / 2,
                            -16, 8, -20, 1, blast,
                            10, 6, 2, false, true));
                }
            shootCooldown = gameTime;
        }
    }

    public void jump() {
        if (!playerJumped) { // Player cannot jump in mid-air.
            playerJump = playerJumpHeight;
            playerJumped = true;
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

    // Theese will be used later on when making menu.
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

    public boolean isFocusTraversable() { // Lets JPanel accept users input.
        return true;
    }
}
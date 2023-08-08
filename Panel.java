
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

public class Panel extends JPanel implements Runnable, KeyListener {
    // World variables.
    Thread gameThread;

    double ticks = 60.0;
    double ns = 1000000000 / ticks;

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

    static Image missionFailedImg = new ImageIcon("missionFailed.png").getImage();
    static Image titleImg = new ImageIcon("title.png").getImage();
    static Image titleBG1Img = new ImageIcon("titleBG1.png").getImage();
    static Image titleBG2Img = new ImageIcon("titleBG2.png").getImage();

    // Buttons Imgs set up later on.
    static Image buttonsImg, deathButtonsImg, winButtonsImg, winButtonsDarkImg, menuButtonsImg, settingsButtonsImg,
            settingsDifficultyImg, settingsGameSpeedImg, settingsEnemyCountImg, settingsExtraBloodImg,
            settingsPartyModeImg;

    // Integers.
    static int difficulty = 1; // Ranges between 0 and 2. 1 Default.
    static int gameSpeed = 1;
    static int enemyCount = 1;

    static int parallaxMax = HEIGHT * 2, parallax = parallaxMax;
    static int panYSpeed = 8;
    static int inRoom = -1, lastInRoom = inRoom, lastRoom = -1;
    static int roomYBase = CHUNK * 6, roomYLevel = CHUNK * 4;

    static int playerWidth = playerImg.getWidth(null); // Null because theres no specified image observer.
    static int playerHeight = playerImg.getHeight(null);

    static int playerXStart = WIDTH / 2 - playerWidth / 2, playerYStart = HEIGHT / 2 - playerHeight;
    static int playerX = playerXStart, playerY = playerYStart;
    static int playerColXOffset = 8, playerColYOffset = 2; // x and y offsets of player collider.

    static int playerSpeedMax = 10, playerSpeed = playerSpeedMax, playerJumpHeight = -24;
    static int playerSpeedLadder = 2, playerClimbSpeedUp = 0, playerClimbSpeedDown = 0, playerClimbSpeedMax = 6;
    static int playerLeft = 0, playerRight = 0, playerUp = 0, playerJump = 0;
    static int playerWobble = 0; // Controls the bobbing up and down of player when walking.

    static int dashResetCooldownTime = 30, dashResetCooldown = -dashResetCooldownTime;
    static int dashCooldownTime = 10, dashCooldown = -dashCooldownTime;
    static int dashSpeedMax = playerSpeed * 5 / 2, dashSpeed = 0;

    static int recoil, recoilMax = -6; // Controls weapon x recoil when it shoots.
    static int shootCooldown = gameTime, shootCooldownTime = 60; // CooldownTime measured in ticks.
    static int reloadBarWidth = reloadBarEmptyImg.getWidth(null), reloadBarHeight = reloadBarEmptyImg.getHeight(null);
    static int dashBarWidth = dashBarEmptyImg.getWidth(null), dashBarHeight = dashBarEmptyImg.getHeight(null);
    static int deathCooldown = gameTime, deathCooldownTime = 60, deathUIY = HEIGHT;
    // deathCoolDown used for wait time after death before player can press buttons.

    static int healthMax = 8, health = healthMax, healthCooldown = gameTime;
    static int healthWidth = (CHUNK + CHUNK / 8) * healthMax + CHUNK / 4;
    static int damageWobbleX, damageWobbleY;
    static int titleUIWobbleX, titleUIWobbleY;
    static int damageFlashMax = 8, damageFlash;
    static int UIOffset = 0;

    static int particlesDensity = 1024, bloodDensity = particlesDensity, particlesMax = 4, colorMod;
    // particlesDensity inversely proportional to particles.

    static int launchSpeed, launchSpeedMax = playerSpeedMax * 2;

    static int fogX = 0, fog2X = -WIDTH, fogSpeed = 1;
    // Duplicate fog placed behind original to create seamless fog movement.

    static boolean movingLeft = false, movingRight = false, facingLeft = false;
    static boolean touchingGround = true, playerJumped = false;
    static boolean onLadder = false, climbingLadder = false, onLadderTop = false, onLadderBottom = false;
    static boolean showControlls = true, panYAccelerating = false, panYDone = false;
    static boolean inWallLeft = false, inWallRight = false;
    static boolean reloadBarRed = false, shootButtonPushed = false;
    static boolean canDash = true, dashing = false, dashBarFull = false, dashBarRed = false, dashButtonPushed = false;
    static boolean titleScreen = true, gameOver = false, win = false;
    static boolean settings = false, extraBlood = false, partyMode = false, partyModeActive = false;

    static Rectangle playerCol = new Rectangle(0, 0, playerWidth - playerColXOffset * 2,
            playerHeight - playerColYOffset * 2);; // Collision box for player.
    static Rectangle groundCol = new Rectangle(-CHUNK, HEIGHT / 2 + parallax, WIDTH + CHUNK * 2, CHUNK);
    static Rectangle wallLeftCol = new Rectangle(0, -HEIGHT * 8, CHUNK * 2 + 8, HEIGHT * 16);
    static Rectangle wallRightCol = new Rectangle(WIDTH - CHUNK * 2, -HEIGHT * 8, CHUNK * 2, HEIGHT * 16);

    static Color playerBlood, enemyBlood, blast, wood;
    static Color rainbow;

    static ArrayList<Projectile> projectile = new ArrayList<Projectile>();
    static ArrayList<Room> room = new ArrayList<Room>();
    static ArrayList<Ladder> ladder = new ArrayList<Ladder>();
    static ArrayList<Particles> particles = new ArrayList<Particles>();

    public Panel() { // Sets up some properties.
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(new Color(175, 207, 194)); // Sets background colour to be a teal-ish.

        addKeyListener(this);

        startGame();
    }

    public void startGame() { // Adds rooms and ladders to ArrayLists, starts gameThread.
        for (int i = 0; i < 5; i++) {
            room.add(new Room(roomYBase + roomYLevel * i, i));
            if (i % 2 == 0)
                ladder.add(new Ladder(CHUNK * 18, CHUNK * (i + 1) * 4, i));
            else
                ladder.add(new Ladder(CHUNK * 3, CHUNK * (i + 1) * 4, i));
        }

        gameThread = new Thread(this);
        gameThread.start(); // Nessesary for game to function properly.
    }

    public void run() { // Game loop.
        long lastTime = System.nanoTime();
        double delta = 0;
        while (true) { // Constantly run.
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            if (delta >= 1) {
                repaint();
                move();
                checkCollisions();
                checkWin();
                delta--;
                gameTime++;
            }
        }
    }

    public void paint(Graphics g) { // Controlls all graphics.
        super.paint(g); // Paints the background using the parent class.

        Graphics2D g2D = (Graphics2D) g;

        Toolkit.getDefaultToolkit().sync(); // Supposedly makes game run smoother.

        buttonFlash();
        paintBackground(g, g2D);
        paintForeground(g, g2D);
        if (!gameOver && !titleScreen) { // Player not drawn when game over.
            paintParticles(g, g2D);
            paintProjectiles(g, g2D);
            paintPlayer(g, g2D);
            paintUI(g, g2D); // Do this last, as UI renders ontop of everything else.
        }
        // paintCol(g, g2D);
        if (partyMode && partyModeActive) // PartyModeActive only triggers when titleScreen is false.
            paintPartyMode(g, g2D);
        paintDeathUI(g, g2D);
        paintMenuUI(g, g2D);
    }

    public void buttonFlash() { // Controlls repeated flashing of any button tooltips.
        // Math.sin() gives switching value between -/+ at repeated rate.
        if (Math.sin(gameTime / 4) > 0) { // Alternates between dark and light button.
            buttonsImg = new ImageIcon("buttons1.png").getImage();
            deathButtonsImg = new ImageIcon("deathButtons1.png").getImage();
            winButtonsImg = new ImageIcon("winButtons1.png").getImage();
            winButtonsDarkImg = new ImageIcon("winButtonsDark1.png").getImage();
            menuButtonsImg = new ImageIcon("menuButtons1.png").getImage();
            settingsButtonsImg = new ImageIcon("settingsButtons1.png").getImage();
        } else {
            buttonsImg = new ImageIcon("buttons2.png").getImage();
            deathButtonsImg = new ImageIcon("deathButtons2.png").getImage();
            winButtonsDarkImg = new ImageIcon("winButtonsDark2.png").getImage();
            winButtonsImg = new ImageIcon("winButtons2.png").getImage();
            winButtonsDarkImg = new ImageIcon("winButtonsDark2.png").getImage();
            menuButtonsImg = new ImageIcon("menuButtons2.png").getImage();
            settingsButtonsImg = new ImageIcon("settingsButtons2.png").getImage();
        }
    }

    public void paintBackground(Graphics g, Graphics2D g2D) { // Paints background and fog.
        g2D.drawImage(backgroundImg, 0, parallax / 4, null);
        g2D.drawImage(fogImg, fogX, -HEIGHT / 2 + fogImg.getHeight(null) / 2 + parallax, null);
        g2D.drawImage(fogImg, fog2X, -HEIGHT / 2 + fogImg.getHeight(null) / 2 + parallax, null);
    }

    public void paintForeground(Graphics g, Graphics2D g2D) { // Paints ground, rooms, ladders, enemies.
        g.setColor(new Color(39, 46, 69)); // Set colour to ground colour.
        g2D.fillRect(groundCol.x + damageWobbleX, groundCol.y + damageWobbleY, groundCol.width, HEIGHT * 16);
        g2D.drawImage(groundImg, groundCol.x + damageWobbleX + CHUNK, groundCol.y + damageWobbleY, null);

        if (partyMode && partyModeActive)
            paintPartyMode(g, g2D); // Trigger in foreground as well to have bolder strobe effect in background.

        for (int i = 0; i < room.size(); i++) {
            // Creates temp variable i, runs code until i is no longer < than room.size().
            room.get(i).paint(g); // For every room i, call its paint method using Graphics g.
            ladder.get(i).paint(g);
        }

        for (int i = 0; i < room.size(); i++) {
            // Purposefully seperate for loop here. Makes sure enemies render over ladder.
            for (int j = 0; j < room.get(i).enemy.size(); j++)
                if (!gameOver)
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
                g2D.drawImage(playerImg, playerX + playerWidth - recoil / 2 + damageWobbleX,
                        playerY + playerWobble + parallax, -playerWidth, playerHeight, null);
                g2D.drawImage(playerItemImg, playerX + playerWidth - recoil + damageWobbleX,
                        playerY + playerWobble + parallax, -playerWidth, playerHeight, null);
            } else {
                g2D.drawImage(playerImg, playerX + recoil / 2 + damageWobbleX,
                        playerY + playerWobble + parallax, playerWidth, playerHeight, null);
                g2D.drawImage(playerItemImg, playerX + recoil + damageWobbleX,
                        playerY + playerWobble + parallax, playerWidth, playerHeight, null);
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

    public void paintPartyMode(Graphics g, Graphics2D g2D) {
        rainbow = Color.getHSBColor((float) Math.abs(Math.sin((float) gameTime / 60)), 1.0f, 1.0f);

        g.setColor(new Color(rainbow.getRed(), rainbow.getGreen(), rainbow.getBlue(), 20));
        g.fillRect(0, 0, WIDTH, HEIGHT);
    }

    public void paintUI(Graphics g, Graphics2D g2D) { // Paints user interface.
        UIOffset = -Math.max(parallax / 8, 0); // Used for making UI slide onto screen at start of game.

        if (showControlls) {
            g2D.drawImage(buttonsImg, WIDTH / 4 + playerX / 2 + playerWidth / 2 - buttonsImg.getWidth(null) / 2,
                    HEIGHT / 2 + PIXEL * 10 + parallax * 3, null);
            // When parallax increases, buttons are moved off screen.
        }

        if (damageFlash > 0) { // Paint red hearts if player is damaged.
            for (int i = 0; i < healthMax; i++) // Paint empty red hearts equal to healthMax.
                g2D.drawImage(heartEmptyRedImg, (CHUNK + CHUNK / 8) * i + CHUNK / 4 + damageWobbleX,
                        CHUNK / 4 + damageWobbleY + UIOffset, null);
            for (int i = 0; i < health; i++) // Paint full red hearts ontop of empty hearts equal to current health.
                g2D.drawImage(heartFullRedImg, (CHUNK + CHUNK / 8) * i + CHUNK / 4 + damageWobbleX,
                        CHUNK / 4 + damageWobbleY + UIOffset, null);
        } else { // Paint hearts.
            for (int i = 0; i < healthMax; i++) // Paint empty hearts equal to healthMax.
                g2D.drawImage(heartEmptyImg, (CHUNK + CHUNK / 8) * i + CHUNK / 4, CHUNK / 4 + UIOffset, null);
            for (int i = 0; i < health; i++) // Paint full hearts ontop of empty hearts equal to current health.
                g2D.drawImage(heartFullImg, (CHUNK + CHUNK / 8) * i + CHUNK / 4, CHUNK / 4 + UIOffset, null);
        }

        if (shootCooldown < gameTime - shootCooldownTime) { // Paint full reloadBar if player can shoot.
            g2D.drawImage(reloadBarFullImg, WIDTH - reloadBarWidth - CHUNK / 4, CHUNK / 4 + UIOffset, null);
        } else {
            if (reloadBarRed) { // Paint red reloadBar if cannot shoot but tries to anyway.
                g2D.drawImage(reloadBarRedImg, WIDTH - reloadBarWidth - CHUNK / 4, CHUNK / 4 + UIOffset, null);
                g.setColor(new Color(148, 90, 80));
                g.fillRect(WIDTH - reloadBarWidth - CHUNK / 4 + PIXEL, CHUNK / 4 + PIXEL * 2 + UIOffset,
                        ((gameTime - shootCooldown) * reloadBarWidth / shootCooldownTime) - PIXEL * 2,
                        reloadBarHeight - PIXEL * 3);
            } else { // Paint reloadBarEmpty when player cannot shoot.
                g2D.drawImage(reloadBarEmptyImg, WIDTH - reloadBarWidth - CHUNK / 4, CHUNK / 4 + UIOffset, null);
                g.setColor(new Color(78, 110, 96));
                g.fillRect(WIDTH - reloadBarWidth - CHUNK / 4 + PIXEL, CHUNK / 4 + PIXEL * 2 + UIOffset,
                        ((gameTime - shootCooldown) * reloadBarWidth / shootCooldownTime) - PIXEL * 2,
                        reloadBarHeight - PIXEL * 3);
            }
        }

        if (dashCooldown < gameTime - dashResetCooldownTime) { // Paint dashBarFull.
            g2D.drawImage(dashBarFullImg, WIDTH - dashBarWidth - CHUNK / 4, reloadBarHeight + CHUNK / 2 + UIOffset,
                    null);
        } else {
            if (dashBarRed) { // Paint dashBarRed if player can't dash but tries to anyway.
                g2D.drawImage(dashBarRedImg, WIDTH - dashBarWidth - CHUNK / 4, reloadBarHeight + CHUNK / 2 + UIOffset,
                        null);
                g.setColor(new Color(148, 90, 80));
                g.fillRect(WIDTH - dashBarWidth - CHUNK / 4 + PIXEL, reloadBarHeight + CHUNK / 2 + PIXEL * 2 + UIOffset,
                        ((gameTime - dashCooldown) * (dashBarWidth) / dashResetCooldownTime) - PIXEL * 2,
                        dashBarHeight - PIXEL * 3);
            } else { // Paint dashBarRed if player can't dash.
                g2D.drawImage(dashBarEmptyImg, WIDTH - dashBarWidth - CHUNK / 4 + UIOffset, reloadBarHeight + CHUNK / 2,
                        null);
                g.setColor(new Color(78, 110, 96));
                g.fillRect(WIDTH - dashBarWidth - CHUNK / 4 + PIXEL, reloadBarHeight + CHUNK / 2 + PIXEL * 2 + UIOffset,
                        ((gameTime - dashCooldown) * (dashBarWidth) / dashResetCooldownTime) - PIXEL * 2,
                        dashBarHeight - PIXEL * 3);
            }
        }
    }

    public void paintDeathUI(Graphics g, Graphics2D g2D) { // Paints death screen interface.
        if (win)
            missionFailedImg = new ImageIcon("missionSuccess.png").getImage();

        if (gameOver && !win) // Only paints on gameOver screen.
            if (deathUIY < HEIGHT / 2) // This makes deathButtons appear to slide in from behind missionFailedImg.
                g2D.drawImage(deathButtonsImg, WIDTH / 2 - deathButtonsImg.getWidth(null) / 2,
                        HEIGHT - deathUIY + deathButtonsImg.getHeight(null) / 2, null);

        if (gameOver) {
            // Draw missionFailed title image.
            g2D.drawImage(titleBG1Img, WIDTH / 2 - titleBG1Img.getWidth(null) / 2 + titleUIWobbleX,
                    deathUIY - titleBG1Img.getHeight(null) / 3 + titleUIWobbleY, null);
            g2D.drawImage(missionFailedImg, WIDTH / 2 - titleBG1Img.getWidth(null) / 2 - titleUIWobbleX,
                    deathUIY - titleBG1Img.getHeight(null) / 3 - titleUIWobbleY, null);

            if (deathUIY > HEIGHT / 2 - CHUNK) { // Check if missionFailedImg is in center of screen.
                deathUIY = deathUIY - CHUNK / 2; // Animate missionFailedImg quickly moving upwards.
            }
        }
    }

    public void paintMenuUI(Graphics g, Graphics2D g2D) { // Paints title screen interface.
        checkSettingsButtons();

        if (titleScreen) {
            if (settings) { // Displaying all settings options.
                g2D.drawImage(settingsButtonsImg, WIDTH / 2 - settingsButtonsImg.getWidth(null) / 2,
                        HEIGHT / 2 - settingsButtonsImg.getHeight(null) / 2 - 1,
                        null);
                g2D.drawImage(settingsDifficultyImg, CHUNK * 11 + PIXEL * 11, CHUNK + PIXEL * 13, null);
                g2D.drawImage(settingsGameSpeedImg, CHUNK * 11 + PIXEL * 11, CHUNK * 2 + PIXEL * 10 + 2, null);
                g2D.drawImage(settingsEnemyCountImg, CHUNK * 11 + PIXEL * 11, CHUNK * 3 + PIXEL * 8, null);
                g2D.drawImage(settingsExtraBloodImg, CHUNK * 11 + PIXEL * 11, CHUNK * 4 + PIXEL * 5 + 2, null);
                g2D.drawImage(settingsPartyModeImg, CHUNK * 11 + PIXEL * 11, CHUNK * 5 + PIXEL * 3, null);

            } else {
                g2D.drawImage(menuButtonsImg, WIDTH / 2 - menuButtonsImg.getWidth(null) / 2,
                        HEIGHT / 2 + menuButtonsImg.getHeight(null) + CHUNK * 3 + parallax - parallaxMax
                                - Math.min(gameTime * 8 - 480, 0),
                        null);

                g2D.drawImage(titleBG2Img, WIDTH / 2 - titleBG2Img.getWidth(null) / 2 - titleUIWobbleX / 2,
                        HEIGHT / 2 - titleBG2Img.getHeight(null) / 2 - CHUNK / 2 + parallax - parallaxMax
                                - titleUIWobbleY / 2 - Math.min(gameTime * 8 - 240, 0),
                        null);

                g2D.drawImage(titleBG1Img, WIDTH / 2 - titleBG1Img.getWidth(null) / 2 + titleUIWobbleX,
                        HEIGHT / 2 - titleBG1Img.getHeight(null) / 2 - CHUNK / 2 + parallax - parallaxMax
                                + titleUIWobbleY
                                - Math.min(gameTime * 8 - 240, 0),
                        null);

                g2D.drawImage(titleImg, WIDTH / 2 - titleImg.getWidth(null) / 2 - titleUIWobbleX,
                        HEIGHT / 2 - titleBG1Img.getHeight(null) / 2 - CHUNK / 2 + parallax - parallaxMax
                                - titleUIWobbleY
                                - Math.min(gameTime * 8 - 240, 0),
                        null);
            }
        }

        if (win && parallax < 256)
            g2D.drawImage(winButtonsImg, WIDTH / 2 - winButtonsImg.getWidth(null) / 2,
                    HEIGHT - winButtonsImg.getHeight(null) * 2, null);
        else if (win)
            g2D.drawImage(winButtonsDarkImg, WIDTH / 2 - winButtonsDarkImg.getWidth(null) / 2,
                    HEIGHT - winButtonsDarkImg.getHeight(null) * 2, null);
    }

    public void checkSettingsButtons() { // Controlls visuals of settings buttons.
        switch (difficulty) {
            case 0:
                settingsDifficultyImg = new ImageIcon("easyButton.png").getImage();
                break;
            case 1:
                settingsDifficultyImg = new ImageIcon("normButton.png").getImage();
                break;
            case 2:
                settingsDifficultyImg = new ImageIcon("hardButton.png").getImage();
                break;
        }

        switch (gameSpeed) {
            case 0:
                settingsGameSpeedImg = new ImageIcon("slowButton.png").getImage();
                break;
            case 1:
                settingsGameSpeedImg = new ImageIcon("normButton.png").getImage();
                break;
            case 2:
                settingsGameSpeedImg = new ImageIcon("fastButton.png").getImage();
                break;
        }

        switch (enemyCount) {
            case 0:
                settingsEnemyCountImg = new ImageIcon("lowButton.png").getImage();
                break;
            case 1:
                settingsEnemyCountImg = new ImageIcon("normButton.png").getImage();
                break;
            case 2:
                settingsEnemyCountImg = new ImageIcon("highButton.png").getImage();
                break;
        }

        if (extraBlood)
            settingsExtraBloodImg = new ImageIcon("onButton.png").getImage();
        else
            settingsExtraBloodImg = new ImageIcon("offButton.png").getImage();

        if (partyMode)
            settingsPartyModeImg = new ImageIcon("onButton.png").getImage();
        else
            settingsPartyModeImg = new ImageIcon("offButton.png").getImage();
    }

    public void move() { // Controlls all movement.
        // Update player position.
        playerY = playerY + playerUp + gravity + playerClimbSpeedUp + playerClimbSpeedDown
                + -Math.abs(launchSpeed * 2 / 3);
        playerX = playerX + playerLeft + playerRight + dashSpeed + launchSpeed;
        playerCol.x = playerX + playerColXOffset;
        playerCol.y = playerY + playerColYOffset + parallax;

        if (movingLeft && !inWallLeft && !inWallLeft || movingRight && !inWallLeft && !inWallRight) {
            playerWobble = (int) (Math.sin(gameTime) * 2);
        } // Alternates between 1 and -1 to create a bobbing up and down motion.

        if (damageFlash > 0) {
            damageWobbleX = (int) (Math.sin(gameTime) * 3); // Similar to playerWobble, alternating between +/- 1.
            damageWobbleY = (int) (Math.sin(gameTime + 1) * 3);
        } else if (gameOver || partyMode) {
            damageWobbleX = (int) (Math.sin((double) gameTime / 2) * 2);
        } else {
            damageWobbleX = 0;
            damageWobbleY = 0;
        }

        titleUIWobbleX = (int) (Math.sin((double) gameTime / 16) * 4); // Controlls wobble seen in game over screen.
        titleUIWobbleY = (int) (Math.sin((double) gameTime / 16 + 1) * 4);

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

        if (!onLadder) {
            playerClimbSpeedUp = 0;
            playerClimbSpeedDown = 0;
        }
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
                if (!gameOver)
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
            room.get(0).top.y = room.get(0).y - CHUNK * 4 / 3 + parallax;
            // Enemy col updated in enemy move function.
        }

        for (int i = 0; i < ladder.size(); i++) {
            ladder.get(i).col.y = ladder.get(i).y - ladder.get(i).offset * 2 + parallax;
            ladder.get(i).topCol.y = ladder.get(i).col.y - ladder.get(i).offset;
            ladder.get(i).bottomCol.y = ladder.get(i).col.y + ladder.get(i).col.height - playerHeight;
            ladder.get(i).leftCol.y = ladder.get(i).col.y;
            ladder.get(i).leftCol.height = ladder.get(i).col.height;
            ladder.get(i).rightCol.y = ladder.get(i).col.y;
            ladder.get(i).rightCol.height = ladder.get(i).col.height;
            // Update ladder left/rightCol height, as ladders shorten when destroyed.
            // Parallax added to col, no need to add to ladderLeft or right a second time.
            // Only need to add parallax to col, as ladderTop and BottomCol are tied to col.
        }
    }

    public void accelarate() { // Controlls player smoothly accelarating and decelarating up to max speed.
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

    public void dash() { // Makes player quickly dash forward in specified direction after cooldown.
        if (dashCooldown > gameTime - dashCooldownTime && canDash) {
            dashing = true;
            if (facingLeft) // Dash in the direction player is facing.
                dashSpeed = -dashSpeedMax;
            else
                dashSpeed = dashSpeedMax;
            dashResetCooldown = gameTime; // Reset dashResetCoolDown.
        } else if (!canDash) {
            dashSpeed = 0;
            dashing = false;
        } else {
            dashSpeed = dashSpeed * 3 / 4;
            dashing = false;
        }
    }

    public void checkCollisions() { // Detection of collisions between everything.
        if (!gameOver) {
            if (playerJump < 0) // Constantly increase playerjump towards 0 if it is less than 1.
                playerJump++;
            for (int i = 0; i < room.size(); i++) {
                room.get(i).isClear(); // Check to see if room is clear of enemies.
                if (room.get(i).col.contains(playerCol)) {
                    inRoom = room.get(i).level;
                    lastRoom = room.get(i).level; // Used for illumination.
                    break;
                } else
                    inRoom = -1;
            }
        }

        checkPan();
        checkLadderCollisions();
        checkWallCollisions();
        checkGroundCollisions();
        checkEnemyCollisions();
        checkProjectileCollisions();
        killStrayProjectiles();
        killOldParticles();
    }

    public void checkPan() { // Detects when panY should be triggered.
        if (lastRoom >= 0) // Makes sure pan only triggers when player is in a room.
            if (lastInRoom != lastRoom) { // Check to see if player is in a new room.
                panYDone = false;
                if (lastInRoom > lastRoom) {
                    panY(lastRoom, true); // Pan up to specified room.
                } else {
                    panY(lastRoom, false);
                }
                if (panYDone) // Once panYDone is true, set lastInRoom to be inRoom.
                    lastInRoom = lastRoom;
            }
        if (gameOver && win && parallax < parallaxMax) // Pan upwards if player wins.
            parallax = parallax + 2;
        else if (gameOver && parallax > -HEIGHT * 3 && !win)
            parallax = parallax - 2; // Pan downwards if player dies.

        if (!titleScreen && !gameOver && lastInRoom == -1 && parallax != 0) { // This triggers as soon as titleScreen is
            // false.
            if (parallax > 0)
                parallax -= CHUNK; // Pans camera down to ground level.
            if (parallax < 0)
                parallax++;
        }
    }

    public void panY(int level, Boolean up) { // Moves camera up / down to room players in.
        if (!up) {
            if (room.get(level).col.y + room.get(level).col.height / 2 > HEIGHT / 2) {
                panYAccelerating = true;
                // Check center of room against center of screen.
                if (panYSpeed <= 32 && panYAccelerating) { // Make sure panYSpeed is less than / equal to 32.
                    panYSpeed++; // Increase panYSpeed towards maximum of 32.
                } else {
                    panYAccelerating = false;
                    // Set panYAccelerating to be false, stopping above statement re-triggering.
                }
                parallax -= panYSpeed; // Reduce parallax.
            } else
                panYDone = true; // Once finished panning, set panYDone to true.
        } else { // If up is false.
            if (room.get(level).col.y + room.get(level).col.height / 2 < HEIGHT / 2) {
                // Check center of room against center of screen.
                if (panYSpeed <= 32 && panYAccelerating) { // Make sure panYSpeed is less than / equal to 32.
                    panYSpeed++; // Increase panYSpeed towards maximum of 32.
                } else {
                    panYAccelerating = false;
                    // Set panYAccelerating to be false, stopping above statement re-triggering.
                }
                parallax += panYSpeed; // Increase parallax.
            } else {
                panYDone = true;
            }
        }
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

            if (ladder.get(i).col.contains(playerCol) && room.get(Math.max(lastRoom, 0)).isClear() ||
                    ladder.get(i).col.contains(playerCol) && lastRoom == -1 ||
                    ladder.get(i).col.contains(playerCol) && ladder.get(i).level == lastRoom) {
                // Check to see if player is colliding with any of the ladders.
                onLadder = true; // Set onLadder to true. Important this is done before gravity is calculated.
                break; // Important to break. This stops onLadder from being set to false unessesarily.
            } else
                onLadder = false;

            if (ladder.get(i).topCol.contains(playerCol)) {
                // Check to see if player is colliding with tops of the ladders.
                onLadderTop = true;
                break;
                // Set onLadderTop to true. This variable controlls jittering that happens if
                // player is holiding down buttons when on ladder.
            } else
                onLadderTop = false;

            if (ladder.get(i).bottomCol.contains(playerCol)) {
                onLadderBottom = true;
                break;
            } else
                onLadderBottom = false;
        }

        if (onLadder && inRoom != -1)
            gravity = 1; // So that player very slowly slides down ladder.
        else if (onLadder)
            gravity = 0;
        else
            gravity = gravityMax;
    }

    public void checkWallCollisions() { // Code for intersecting with left and right wall colliders.
        if (playerCol.intersects(wallLeftCol)) {
            inWallLeft = true; // Stops player from being able to move left.
            playerLeft = 0;
            launchSpeed = Math.abs(launchSpeed);
        } else {
            inWallLeft = false;
        }

        if (playerCol.intersects(wallRightCol)) {
            inWallRight = true;
            playerRight = 0;
            launchSpeed = -Math.abs(launchSpeed);
        } else {
            inWallRight = false;
        }

        if (playerCol.intersects(wallLeftCol.x - playerSpeed, wallLeftCol.y, wallLeftCol.width, wallLeftCol.height)) {
            playerX = playerX + (playerWidth / 4); // When player is launched into the wall, force it back out.
        }
        if (playerCol.intersects(wallRightCol.x + playerSpeed, wallRightCol.y, wallRightCol.width,
                wallRightCol.height)) {
            playerX = playerX - (playerWidth / 4);
        }

        if (playerCol.intersects(wallLeftCol.x + playerSpeed / 2, wallLeftCol.y, wallLeftCol.width, wallLeftCol.height)
                || playerCol.intersects(wallRightCol.x - playerSpeed / 2, wallRightCol.y,
                        wallRightCol.width, wallRightCol.height))
            canDash = false;
        else
            canDash = true;

        for (int j = 0; j < particles.size(); j++) {
            if (particles.get(j).col.intersects(wallLeftCol)) {
                particles.get(j).xSpeed = Math.abs(particles.get(j).xSpeed);
                particles.get(j).x += CHUNK;
            }
            if (particles.get(j).col.intersects(wallRightCol)) {
                particles.get(j).xSpeed = -Math.abs(particles.get(j).xSpeed);
                particles.get(j).x -= CHUNK;
            }
        }
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
                playerY--; // Push player out of floor.
            if (playerCol.intersects(room.get(i).ceiling) && !onLadder) {
                launchSpeed = -Math.abs(launchSpeed); // Bounce player off ceiling.
                playerJump = -Math.abs(playerJump);
                playerY = playerY + playerWidth; // Makes sure player doesent clip through ceiling at high velocities.
            }

            for (int j = 0; j < particles.size(); j++)
                if (particles.get(j).col.intersects(room.get(i).floor) ||
                        particles.get(j).col.intersects(room.get(0).top)) { // Stop particles from phasing through
                                                                            // floor.
                    particles.get(j).ySpeed = -Math.abs(particles.get(j).ySpeed / 2);
                    particles.get(j).xSpeed = particles.get(j).xSpeed / 2;
                }
        }

        // Code to destroy ladder.
        for (int i = 0; i < ladder.size(); i++) // Run through every ladder.
            if (inRoom == ladder.get(i).level && touchingGround ||
                    inRoom == ladder.get(i).level && damageFlash > 0) {
                // Triggers if player touches ground or gets hit by enemy and
                // ladder is in same room as player.
                if (!ladder.get(i).ladderBroken) {
                    for (int j = 0; j < (ladder.get(i).col.width * ladder.get(i).col.height) /
                            particlesDensity * 8; j++) {

                        colorMod = (int) (Math.random() * 40);
                        changeWoodColour();

                        particles.add(new Particles(ladder.get(i).x,
                                ladder.get(i).y + ladder.get(i).col.height / 2, ladder.get(i).col.width / 2,
                                ladder.get(i).col.height / 2, 0, -4 - (int) (Math.random() * 8), wood,
                                120, 1, 0, true, true));

                        ladder.get(i).ladderBroken = true; // Changes ladder image to broken state.
                    }
                    ladder.get(i).col.height = (ladder.get(i).ladderImg.getHeight(null) +
                            ladder.get(i).offset * 2) / 2; // Remove bottom half of ladder col.
                }
            }
    }

    public void changeWoodColour() {
        if (!partyMode)
            wood = new Color(120 + colorMod * 2, 50 + colorMod * 2, 40 + colorMod * 2);
        else
            wood = new Color(
                    Color.getHSBColor((float) (Math.random() * 360), 0.8f, 0.6f).getRGB());
    }

    public void checkEnemyCollisions() { // Manages enemy collisions with ground, and hitting player.
        for (int i = 0; i < room.size(); i++)
            for (int j = 0; j < room.get(i).enemy.size(); j++) {
                if (!gameOver)
                    room.get(i).enemy.get(j).checkCollisions(); // Check enemy collisions.

                if (room.get(i).enemy.get(j).col.intersects(room.get(i).floor.x, room.get(i).floor.y - 1,
                        room.get(i).floor.width, room.get(i).floor.height)) {
                    // Check enemy col against room floor pushed down 1;
                    room.get(i).enemy.get(j).up = -room.get(i).enemy.get(j).gravity; // Nullify enemy gravity.
                } else
                    room.get(i).enemy.get(j).up = 0; // Let enemy fall back down to ground.

                if (room.get(i).enemy.get(j).col.intersects(room.get(i).floor) && !room.get(i).enemy.get(j).isDummy)
                    room.get(i).enemy.get(j).y--; // Make sure enemy doesent get stuck in ground.

                if (playerCol.intersects(room.get(i).enemy.get(j).damageColLeft) && room.get(i).enemy.get(j).isBoss
                        && difficulty != 0) {
                    canDash = false;
                    damage(false);
                }
                if (playerCol.intersects(room.get(i).enemy.get(j).damageColRight) && room.get(i).enemy.get(j).isBoss
                        && difficulty != 0) {
                    canDash = false;
                    damage(true);
                }

                if (playerCol.intersects(room.get(i).enemy.get(j).damageColLeft)
                        && !dashing && healthCooldown < gameTime - 30 && !gameOver) {
                    damage(false); // Player cant get hurt while dashing.
                }
                if (playerCol.intersects(room.get(i).enemy.get(j).damageColRight)
                        && !dashing && healthCooldown < gameTime - 30 && !gameOver) {
                    damage(true);
                }
            }
        if (damageFlash > 0)
            damageFlash--; // Decrease damageFlash to 0.
    }

    public void damage(Boolean isLeft) { // Reduces player health, deals knockback, calls kill funcion.
        damageFlash = damageFlashMax; // Set damageFlash to its maximum value.
        playerJump = 0; // Stops player from being able to be launched and jump at the same time.

        if (isLeft) {
            if (health > 1 && healthCooldown < gameTime - 30)
                launchSpeed = launchSpeedMax; // Launches the player left.
            else
                launchSpeed = launchSpeedMax * 2 / 3; // Launches the player less strongly.

            playerLeft = 0;
            playerRight = 0;
            for (int i = 0; i < (playerWidth * playerHeight) / bloodDensity * 4; i++) {
                colorMod = (int) (Math.random() * 40);
                changePlayerBloodColour();
                particles.add(new Particles(playerX, playerY, playerWidth, playerHeight, 10, -10,
                        playerBlood, 60, 1, 0.2f, true, true));
            }
        } else {
            if (health > 1 && healthCooldown < gameTime - 30)
                launchSpeed = -launchSpeedMax; // Launches the player right.
            else
                launchSpeed = -launchSpeedMax * 2 / 3;
            playerLeft = 0;
            playerRight = 0;
            for (int i = 0; i < (playerWidth * playerHeight) / bloodDensity * 4; i++) {
                colorMod = (int) (Math.random() * 40);
                changePlayerBloodColour();
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

    public void changePlayerBloodColour() {
        if (!partyMode)
            playerBlood = new Color((colorMod * 2 + 110), 20, 20);
        else
            playerBlood = new Color(
                    Color.getHSBColor((float) (Math.random() * 360), 0.8f, 0.6f).getRGB());
    }

    public void kill() { // Kills player, sets player respawn location, enables deathUI.
        if (lastRoom == 0)
            lastRoom = 1;

        lastInRoom = Math.max(0, lastRoom - 1);

        playerX = playerXStart; // Reset playerX/Y pos.
        playerY = room.get(lastInRoom).y + CHUNK * 3 - playerHeight * 2;

        deathCooldown = gameTime; // Add a delay between death screen appearing and accepting keyboard input.

        gameOver = true;
    }

    public void checkProjectileCollisions() { // Collisions between enemies and projectiles.
        // Detects when an enemy gets hit by a projectile, then deals damage to the
        // enemy, puts out particles, and knocks enemy back.
        for (int i = 0; i < projectile.size(); i++)
            for (int j = 0; j < room.size(); j++)
                for (int k = 0; k < room.get(j).enemy.size(); k++) { // Run thru every projectile and enemy per level.
                    if (room.get(j).enemy.get(k).col.intersects(projectile.get(i).col)) {

                        if (projectile.get(i).col.intersects(room.get(j).enemy.get(k).col.x,
                                room.get(j).enemy.get(k).col.y,
                                1, room.get(j).enemy.get(k).col.height))
                            for (int l = 0; l < (playerWidth * playerHeight) / bloodDensity * 16; l++) {

                                changeEnemyBloodColour(room.get(j).level, room.get(j).enemy.get(k).isDummy);

                                particles.add(new Particles(room.get(j).enemy.get(k).x, room.get(j).enemy.get(k).y,
                                        room.get(j).enemy.get(k).width, room.get(j).enemy.get(k).height, 10, -10,
                                        enemyBlood, 60, 1, 0.2f, true, true));

                                room.get(j).enemy.get(k).launch(true);
                            }
                        else if (projectile.get(i).col.intersects(room.get(j).enemy.get(k).col.x +
                                room.get(j).enemy.get(k).col.width, room.get(j).enemy.get(k).col.y, 1,
                                room.get(j).enemy.get(k).col.height))
                            for (int l = 0; l < (playerWidth * playerHeight) / bloodDensity * 16; l++) {

                                changeEnemyBloodColour(room.get(j).level, room.get(j).enemy.get(k).isDummy);

                                particles.add(new Particles(room.get(j).enemy.get(k).x, room.get(j).enemy.get(k).y,
                                        room.get(j).enemy.get(k).width, room.get(j).enemy.get(k).height, -10, -10,
                                        enemyBlood, 60, 1, 0.2f, true, true));

                                room.get(j).enemy.get(k).launch(false);

                            }
                        if (room.get(j).enemy.get(k).health > 1) {
                            // If enemy has over 1 health, subtract 1 instead of killing them.
                            room.get(j).enemy.get(k).health--;
                        } else
                            room.get(j).enemy.remove(k); // Remove enemy.

                        projectile.get(i).x = WIDTH * 4; // Send projectile off screen to get killed.
                    }
                }
    }

    public void changeEnemyBloodColour(int level, boolean isDummy) { // Changes blood colour depending on enemy type.
        colorMod = (int) (Math.random() * 40); // Adds random variation to particle colours.

        if (!partyMode)
            switch (level) {
                case 0:
                    if (!isDummy) // Red blood.
                        enemyBlood = new Color((colorMod * 2 + 110), 20, 20);
                    else // Dummy brown.
                        enemyBlood = new Color(colorMod + 100, colorMod + 70, colorMod + 60);
                    break;
                case 1: // Blue-green goblin blood.
                    enemyBlood = new Color(20, (100 - colorMod), (colorMod + 110));
                    break;
                case 2: // Robot "blood".
                    enemyBlood = new Color(colorMod + 40, colorMod + 60, colorMod + 80);
                    break;
                case 3: // Mummy brown.
                    enemyBlood = new Color(colorMod + 100, colorMod + 70, colorMod + 60);
                    break;
                default: // Red blood;
                    enemyBlood = new Color((colorMod * 2 + 110), 20, 20);
                    break;
            }
        else {
            enemyBlood = new Color(
                    Color.getHSBColor((float) (Math.random() * 360), 0.8f, 0.6f).getRGB());
        }
    }

    public void killStrayProjectiles() { // Kills particles that travel off screen.
        for (int i = 0; i < projectile.size(); i++)
            if (projectile.get(i).x < -WIDTH || projectile.get(i).x > WIDTH * 2)
                projectile.remove(i);
    }

    public void killOldParticles() { // Removes particles after a certian time.
        for (int i = 0; i < particles.size(); i++)
            if (particles.get(i).age >= particles.get(i).ageMax)
                particles.remove(i);
        if (particles.size() > particlesMax)
            particles.remove(0); // If too many particles on screen, remove the first particles in arrayList.
    }

    public void checkWin() { // Triggers win condition when all enemies are dead.
        if (room.get(room.size() - 1).enemy.size() == 0 && shootCooldown < gameTime - shootCooldownTime
                && !titleScreen) {
            win = true;
            gameOver = true;
        }
    }

    public void keyTyped(KeyEvent e) { // Nessesary for the keyBoardListener to function properly.
    }

    public void keyPressed(KeyEvent e) { // Manages all keyboard presses.
        if (!gameOver && !titleScreen) { // Player can only move if not dead / on title screen.
            switch (e.getKeyCode()) {
                case 37: // Move left.
                    if (!inWallLeft) {
                        key = 'a';
                        movingLeft = true;
                    }
                    break;
                case 38:
                    if (touchingGround && !onLadder) // Player cannot jump while on ladder.
                        jump();
                    else if (onLadder && !onLadderTop) {
                        playerClimbSpeedUp = -playerClimbSpeedMax;
                        climbingLadder = true;
                    }
                    break;
                case 39: // Move right.
                    if (!inWallRight) {
                        key = 'd';
                        movingRight = true;
                    }
                    break;
                case 40: // Move down.
                    // If player on ladder & ladder bottom, player wont fall but can't climb lower.
                    if (onLadder && !onLadderBottom) {
                        playerClimbSpeedDown = playerClimbSpeedMax;
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
                        dashButtonPushed = true;
                    }
                    if (!(dashCooldown < gameTime - dashResetCooldownTime) && !dashButtonPushed)
                        dashBarRed = true;
                    break;
            }

        } else if (gameOver && deathCooldown < gameTime - deathCooldownTime || win)
            // Only trigger on game over screen.
            // 1 second delay between game over and acceptung user input.
            switch (e.getKeyCode()) {
                case 32: // Reset.
                    if (!win)
                        reset();
                    break;
                case 81: // Quit.
                    System.exit(0);
                    break;
            }
        else if (titleScreen && gameTime > 60) {
            // Only trigger on title screen.
            // 1 second delay between game start and acceptung user input.
            switch (e.getKeyCode()) {
                case 32: // Start game.
                    if (!settings) {
                        applySettings();
                        for (int i = 0; i < room.size(); i++)
                            room.get(i).populate();
                        titleScreen = false;
                    }
                    break;
                case 83: // Settings.
                    settings = !settings;
                    break;
                case 81: // Quit.
                    if (!settings)
                        System.exit(0);
                    break;
            }
            if (settings)
                switch (e.getKeyCode()) {
                    case 49:
                        if (difficulty < 2)
                            difficulty++;
                        else
                            difficulty = 0;
                        break;
                    case 50:
                        if (gameSpeed < 2)
                            gameSpeed++;
                        else
                            gameSpeed = 0;
                        break;
                    case 51:
                        if (enemyCount < 2)
                            enemyCount++;
                        else
                            enemyCount = 0;
                        break;
                    case 52:
                        extraBlood = !extraBlood;
                        break;
                    case 53:
                        partyMode = !partyMode;
                        break;
                }
        }
    }

    public void shoot() { // Shoots bullet from player traveling in direction player is facing.
        if (shootCooldown < gameTime - shootCooldownTime) {
            shootButtonPushed = true;
            reloadBarRed = false; // Stops reloadBar staying red when space is held down.

            projectile.add(new Projectile(facingLeft, playerX, playerY, "bazooka"));
            recoil = recoilMax; // This pushes gun backwards by recoilMax pixels.

            for (int i = 0; i < (playerWidth * playerHeight) / particlesDensity * 8; i++)
                if (facingLeft) {
                    colorMod = (int) (Math.random() * 40); // Randomise particle colour.
                    changeBlastColour();
                    particles.add(new Particles(playerX + playerWidth * 3 / 4, playerY + playerHeight / 2,
                            16, 8, 20, 1, blast, // Add shoot particles.
                            10, 6, 2, false, true));
                } else {
                    colorMod = (int) (Math.random() * 40);
                    changeBlastColour();
                    particles.add(new Particles(playerX + playerWidth / 4, playerY + playerHeight / 2,
                            -16, 8, -20, 1, blast,
                            10, 6, 2, false, true));
                }
            shootCooldown = gameTime;
        }
        if (!(shootCooldown < gameTime - shootCooldownTime) && !shootButtonPushed)
            reloadBarRed = true;
    }

    public void changeBlastColour() {
        if (!partyMode)
            blast = new Color(colorMod * 2 + 170, colorMod * 2 + 120, colorMod * 2 + 60);
        else
            blast = new Color(
                    Color.getHSBColor((float) (Math.random() * 360), 0.7f, 0.8f).getRGB());
    }

    public void jump() { // Manages player jump.
        if (!playerJumped) { // Player cannot jump in mid-air.
            playerJump = playerJumpHeight;
            playerJumped = true;
        }
    }

    public void reset() { // Resets health and nessesaey UI elements.
        gameOver = false;

        health = healthMax;

        for (int i = 0; i < room.size(); i++)
            for (int j = 0; j < room.get(i).enemy.size(); j++)
                if (room.get(i).enemy.get(j).isBoss)
                    room.get(i).enemy.get(j).x = CHUNK * 2;

        checkPan(); // Makes sure camera pans up to room above players death.

        deathUIY = HEIGHT; // Resets deathUIY back to bottom of screen.
    }

    public void applySettings() { // Initiates settings.
        scaleDifficulty();
        scaleGameSpeed();
        scaleEnemyCount();
        toggleExtraBlood();
        togglePartyMode();
    }

    public void scaleDifficulty() {
        switch (difficulty) {
            case 0: // Easy.
                healthMax = 10;
                health = healthMax;
                dashResetCooldownTime = 15;
                shootCooldownTime = 30;
                heartFullImg = new ImageIcon("heartFullEasy.png").getImage();
                break;
            case 1: // Normal.
                healthMax = 8;
                health = healthMax;
                dashResetCooldownTime = 30;
                shootCooldownTime = 60;
                break;
            case 2: // Hard.
                healthMax = 6;
                health = healthMax;
                dashResetCooldownTime = 60;
                shootCooldownTime = 120;
                heartFullImg = new ImageIcon("heartFullHard.png").getImage();
                break;
        }
    }

    public void scaleGameSpeed() {
        if (gameSpeed == 0)
            ticks = 40.0;
        if (gameSpeed == 1)
            ticks = 60.0;
        if (gameSpeed == 2)
            ticks = 80.0;
        ns = 1000000000 / ticks;
    }

    public void scaleEnemyCount() {
        if (enemyCount == 0) {
            Room.enemyCountBase = 2;
            Room.enemyCountMod = 2;
        }
        if (enemyCount == 1) {
            Room.enemyCountBase = 4;
            Room.enemyCountMod = 3;
        }
        if (enemyCount == 2) {
            Room.enemyCountBase = 6;
            Room.enemyCountMod = 6;
        }
    }

    public void toggleExtraBlood() { // Manages extra blood option in settings.
        if (extraBlood)
            bloodDensity = 256;
        else
            bloodDensity = 1024;
    }

    public void togglePartyMode() {
        partyModeActive = true; // Makes sure party mode only triggers once titleScreen has been disabled.
    }

    public void keyReleased(KeyEvent e) { // Detects when keys are released.
        switch (e.getKeyCode()) {
            case 37:
                movingLeft = false;
                if (movingRight)
                    facingLeft = false;
                break;
            case 39:
                movingRight = false;
                if (movingLeft)
                    facingLeft = true;
                break;
            case 40:
                playerClimbSpeedDown = 0;
                climbingLadder = false;
                break;
            case 38:
                playerJumped = false;
                playerClimbSpeedUp = 0;
                climbingLadder = false;
                break;
            case 32:
                shootButtonPushed = false;
                reloadBarRed = false;
                break;
            case 16:
                dashBarRed = false;
                dashButtonPushed = false;
                break;
        }
    }

    public boolean isFocusTraversable() { // Lets JPanel accept users input.
        return true;
    }
}
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

public class Panel extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener
{
    //Panel variables.
    Thread gameThread;

    final static int WIDTH = 1400;
    final static int HEIGHT = 600;

    //Integers.
    int playerX;
    int playerY;
    int playerWidth;
    int playerHeight;

    int playerSpeed = 6;
    int playerLeft = 0;
    int playerRight = 0;

    //Booleans.
    boolean movingLeft = false;
    boolean movingRight = false;

    //Characters.
    char key;

    //Images.
    Image playerImg;

    //Classes.
    Player player;
    Room room;
    Enemy enemy;

    public Panel()
    {
        this.setPreferredSize(new Dimension(WIDTH,HEIGHT));    
        //this.setBackground(BGColour);

        addKeyListener(this); //Setting up listeners here as they are used throughought the whole game.
        addMouseListener(this);
        addMouseMotionListener(this);

        //Setup images.
        playerImg = new ImageIcon("garfield.png").getImage();
        playerWidth = playerImg.getWidth(null); //Null because theres no specified image observer.
        playerHeight = playerImg.getHeight(null);
        playerX = WIDTH / 2 - playerWidth / 2;
        playerY = playerHeight;

        gameThread = new Thread(this);
        gameThread.start(); 
    }

    public void run() //Game loop.
    {
        long lastTime = System.nanoTime();
        double Ticks = 60.0;
        double ns = 1000000000 / Ticks;
        double delta = 0;
        while(true)
        {
            long now = System.nanoTime();
            delta += (now - lastTime)/ns;
            lastTime = now;

            if(delta >= 1)
            {
                repaint();
                move();
                checkCollisions();
                delta--;
            }
        }
    }

    public void paint(Graphics g)
    {
        super.paint(g); //Paints the background using the parent class.

        Graphics2D g2D = (Graphics2D) g;

        Toolkit.getDefaultToolkit().sync(); //Supposedly makes game run smoother.

        g.setColor(Color.PINK);
        g2D.setStroke(new BasicStroke(6));
        Line2D line2 = new Line2D.Float(0, 0, WIDTH, HEIGHT);
        g2D.draw(line2);

        //Painting images
        g2D.drawImage(playerImg, playerX, playerY, null);
    }

    public void menu()
    {
        startGame();
    }

    public void startGame()
    {
        //newPlayer();
    }

    public void move()
    {
        playerX = playerX + playerLeft + playerRight;
        accelarate();
        gravity();
    }

    public void checkCollisions()
    {

    }

    public void keyTyped(KeyEvent e) {}

    public void keyPressed(KeyEvent e) 
    {
        switch(e.getKeyCode())
        {
            case 65: key = 'a';
                movingLeft = true;
                break;
            case 68: key = 'd';
                movingRight = true;
                break;
        }
    }

    public void keyReleased(KeyEvent e)
    {
        switch(e.getKeyCode())
        {
            case 65: movingLeft = false;
                break;
            case 68: movingRight = false;
                break;
        }
    }

    public void mouseClicked(MouseEvent e) {}

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {}

    public void mouseDragged(MouseEvent e) {} 

    public void mouseMoved(MouseEvent e) {}

    public void accelarate()
    {
        if(movingLeft)
        {
            if(playerLeft > -playerSpeed)
                if(key == 'a')
                    playerLeft--;
        }else
        if(playerLeft < 0)
                playerLeft++;

        if(movingRight)
        {
            if(playerRight < playerSpeed)
                if(key == 'd')
                    playerRight++;
        }else
        if(playerRight > 0)
                playerRight--;
    }
    
    public void gravity()
    {
        
    }

    public void newRoom()
    {

    }

    public void newEnemy()
    {

    }

    public boolean isFocusTraversable() //Lets JPanel accept users input.
    {
        return true;
    }
}
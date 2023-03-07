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
    Thread gameThread;
    
    final int WIDTH = 512;
    final int HEIGHT = 512;
    
    public Panel()
    {
        this.setPreferredSize(new Dimension(WIDTH,HEIGHT));    
        //this.setBackground(BGColour);

        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);

        this.isFocusTraversable();
        
        gameThread = new Thread(this);
        gameThread.start();
    }
    
    public void run() //Game loop
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
                delta--;
            }
        }
    }
    
    public void paint(Graphics g)
    {
        super.paint(g); //Paints the background using the parent class.

        Graphics2D g2D = (Graphics2D) g;

        Toolkit.getDefaultToolkit().sync(); //Supposedly makes game run smoother.
    }

    public void keyTyped(KeyEvent e) {}

    public void keyPressed(KeyEvent e)
    {
        
    }

    public void keyReleased(KeyEvent e)
    {
        
    }

    public void mouseClicked(MouseEvent e) {}

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {}

    public void mouseDragged(MouseEvent e) {} 

    public void mouseMoved(MouseEvent e) {}
}
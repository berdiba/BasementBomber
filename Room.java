
/**
 * Code for rooms.
 *
 * @author BAXTER BERDINNER
 * @version 20/02/2023
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.lang.Math;
import java.awt.geom.*;

public class Room {
    int X, Y, level;
    int WIDTH = Panel.CHUNK * 18, HEIGHT = Panel.CHUNK * 3;

    Image roomImg;

    Rectangle roomCol;
    Rectangle floor;

    public Room(int X, int Y, int level) {
        this.X = X;
        this.Y = Y; //Setting X, Y, level to be ints passed from Panel.
        this.level = level;

        roomImg = new ImageIcon("room"+level+".png").getImage();
        //Images named "room1, room2..." each level will have different image.
        roomCol = new Rectangle(X, Y, WIDTH, HEIGHT); // Paralax added later in Panel.
        floor = new Rectangle(X, Y + HEIGHT, WIDTH, Panel.CHUNK); 
        // Creates rectangle 1 chunk tall at the bottom of a room.
    }

    public void paint(Graphics g) { //Will be called in Panel paint method.
        Graphics2D g2D = (Graphics2D) g;

        g2D.drawImage(roomImg, X, Y + Panel.parallax, WIDTH, HEIGHT, null);
    }
}

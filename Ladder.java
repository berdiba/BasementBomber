/**
 * Code for ladders.
 *
 * @author BAXTER BERDINNER
 * @version 18/05/2023
 */

 import java.awt.*;
 import javax.swing.*;

public class Ladder {
    int X, Y;
    int offset = 24;
    
    Image ladderImg;
    Rectangle ladderCol;


    public Ladder(int X, int Y){
        this.X = X;
        this.Y = Y;

        ladderImg = new ImageIcon("ladder.png").getImage();
        ladderCol = new Rectangle(X - offset, Y - offset, ladderImg.getWidth(null) + offset * 2, ladderImg.getHeight(null) + offset * 2);
    }

    public void paint(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;

        g2D.drawImage(ladderImg, X, Y, null);
    }
}
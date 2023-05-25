/**
 * Code for ladders.
 *
 * @author BAXTER BERDINNER
 * @version 18/05/2023
 */

 import java.awt.*;
 import javax.swing.*;

public class Ladder {
    int CHUNK = Panel.CHUNK;

    int X, Y;
    int offset = 24;
    
    Image ladderImg;
    Rectangle ladderCol;
    Rectangle ladderTopCol;
    Rectangle ladderBottomCol;
    Rectangle ladderLeftCol;
    Rectangle ladderRightCol;

    public Ladder(int X, int Y){
        this.X = X;
        this.Y = Y;

        ladderImg = new ImageIcon("ladder.png").getImage();
        ladderCol = new Rectangle(X - offset, Y - offset * 2, ladderImg.getWidth(null) + offset * 2, ladderImg.getHeight(null) + offset * 2);
        ladderTopCol = new Rectangle(ladderCol.x, ladderCol.y - offset, ladderCol.width, offset * 4 );
        ladderBottomCol = new Rectangle(ladderCol.x, ladderCol.y + ladderCol.height - Panel.playerHeight, ladderCol.width, offset * 4);
        // ladderTop and ladderBottom offset above and below ladder.
        ladderLeftCol = new Rectangle(ladderCol.x - CHUNK + CHUNK / 4, ladderCol.y, CHUNK, ladderCol.height);
        ladderRightCol = new Rectangle(ladderCol.x + ladderCol.width - CHUNK / 4, ladderCol.y, CHUNK, ladderCol.height);
    }

    public void paint(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;

        g2D.drawImage(ladderImg, X, Y + Panel.parallax, null);
    }
}
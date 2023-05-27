
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

    int X, Y, level;
    int offset = 24;
    int stayLight = 0; // Makes sure ladder doesen't switch back to dark after being illuminated.

    Image ladderImg;
    Rectangle ladderCol;
    Rectangle ladderTopCol;
    Rectangle ladderBottomCol;
    Rectangle ladderLeftCol;
    Rectangle ladderRightCol;

    public Ladder(int X, int Y, int level) {
        this.X = X;
        this.Y = Y;
        this.level = level;

        ladderImg = new ImageIcon("ladder.png").getImage();
        ladderCol = new Rectangle(X - offset, Y - offset * 2, ladderImg.getWidth(null) + offset * 2,
                ladderImg.getHeight(null) + offset * 2);
        ladderTopCol = new Rectangle(ladderCol.x, ladderCol.y - offset, ladderCol.width, offset * 4);
        ladderBottomCol = new Rectangle(ladderCol.x, ladderCol.y + ladderCol.height - Panel.playerHeight,
                ladderCol.width, offset * 4);
        // ladderTop and ladderBottom offset above and below ladder.
        ladderLeftCol = new Rectangle(ladderCol.x - CHUNK + CHUNK / 4, ladderCol.y, CHUNK, ladderCol.height);
        ladderRightCol = new Rectangle(ladderCol.x + ladderCol.width - CHUNK / 4, ladderCol.y, CHUNK, ladderCol.height);
    }

    public void paint(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;

        if (Panel.inRoom < level - 1 && stayLight == 0) {
            ladderImg = new ImageIcon("ladderDark.png").getImage();
        } else if (Panel.inRoom == level - 1 && stayLight <= 0) {
            ladderImg = new ImageIcon("ladderDarkBottom.png").getImage();
            stayLight = 1;
        } else if (Panel.inRoom == level && stayLight <= 1) {
            ladderImg = new ImageIcon("ladder.png").getImage();
            stayLight = 2;
        }

        g2D.drawImage(ladderImg, X, Y + Panel.parallax, null);
    }
}
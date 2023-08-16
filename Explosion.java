
/**
 * Code for explosion created upon a projectile hitting an enemy.
 *
 * @author BAXTER BERDINNER
 * @version 14/08/2023
 */

import java.awt.*;
import javax.swing.*;

public class Explosion {

   int x, y, startAge; 
   // Startage set to gameTime to find the change in gameTime since Explosion creation.

   int animationSpeed = 3;

   Image explosionImg;

   public Explosion(int x, int y, int startAge) { // Setup variables.
      this.x = x;
      this.y = y;

      this.startAge = startAge;
   }

   public void paint(Graphics2D g2D) { // Paint explosion, taking into account parallax and dammageWobbleX&Y.
      g2D.drawImage(explosionImg, x + Panel.damageWobbleX, y + Panel.damageWobbleY + Panel.parallax, null);
   }

   public void update() {
      if (!Panel.partyMode) // Different explosionImg for partyMode.
         explosionImg = new ImageIcon(
               "explosion/" + (int) ((Panel.gameTime - startAge) / animationSpeed + 1) + ".png").getImage();
      else // ExplosionImg named from 1 to 8. Cycles through explosionImgs.
         explosionImg = new ImageIcon(
               "explosion/party" + (int) ((Panel.gameTime - startAge) / animationSpeed + 1) + ".png").getImage();
   }
}

/**
 * Code for explosion created upon a projectile hitting an enemy.
 *
 * @author BAXTER BERDINNER
 * @version 14/08/2023
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.lang.Math;
import java.awt.geom.*;
import java.util.ArrayList;

public class Explosion {

   int x, y, startAge;

   int animationSpeed = 3;

   Image explosionImg;

   public Explosion(int x, int y, int startAge) {
      this.x = x;
      this.y = y;

      this.startAge = startAge;
   }

   public void paint(Graphics2D g2D) {
      g2D.drawImage(explosionImg, x + Panel.damageWobbleX, y + Panel.damageWobbleY + Panel.parallax, null);
   }

   public void update() {
      if (!Panel.partyMode)
      explosionImg = new ImageIcon(
         "explosion/" + (int) ((Panel.gameTime - startAge) / animationSpeed + 1) + ".png").getImage();
         else
         explosionImg = new ImageIcon(
         "explosion/party" + (int) ((Panel.gameTime - startAge) / animationSpeed + 1) + ".png").getImage();
      // if ((Panel.gameTime - startAge))
   }
}

/**
 * Code for particles created upon an objects death or destruction.
 *
 * @author BAXTER BERDINNER
 * @version 17/03/2023
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.lang.Math;
import java.awt.geom.*;
import java.util.ArrayList;

public class Particles {
   int x, y, width, height, xSpeed, ySpeed;
   int particleWidth = 4, particleHeight = 4;
   int xOffset, yOffset;

   Color color;

   int age = Panel.gameTime, ageMax;
   boolean gravity, drag;

   Rectangle col;

   public Particles(int x, int y, int width, int height, int xSpeed, int ySpeed, Color color, int ageMax, float spreadX,
         float spreadY, boolean gravity, boolean drag) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;

      xOffset = (int) (Math.random() * width * spreadX);
      yOffset = (int) (Math.random() * height * spreadY);

      this.xSpeed = xSpeed + (int) (Math.random() * width - xOffset) / 4;
      this.ySpeed = ySpeed + (int) (Math.random() * height - yOffset) / 4;

      this.color = color;

      this.ageMax = age + ageMax;
      this.gravity = gravity;
      this.drag = drag;

      col = new Rectangle(x, y, particleWidth, particleHeight);
   }

   public void paint(Graphics g) {
      g.setColor(color);
      g.fillRect(x + xOffset,
            y + yOffset + Panel.parallax, particleWidth, particleHeight);
   }

   public void move() {
      x = x + xSpeed + Panel.damageWobbleX; // Move particles.
      y = y + ySpeed - Math.abs(Panel.damageWobbleY * 2); 
      // Makes it look like particles bounce when camera shakes.

      col = new Rectangle(x + xOffset,
            y + yOffset + Panel.parallax, particleWidth, particleHeight);

      // Increase or decrease x and y towards 0.
      if (xSpeed != 0 && drag) {
         if (xSpeed > 0)
            xSpeed--;
         if (xSpeed < 0)
            xSpeed++;
      }

      if (gravity)
         ySpeed++; // Make particles fall downwards.

      age++;
   }
}
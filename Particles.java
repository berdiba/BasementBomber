
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

public class Particles {
   int x, y, width, height, xSpeed, ySpeed;
   int xOffset, yOffset;
   Color color;

   int age = Panel.gameTime, ageMax = age + 60;

   int particleCount, particleDensity = 32;

   public Particles(int x, int y, int width, int height, int xSpeed, int ySpeed, Color color) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;

      xOffset = (int) (Math.random() * width);
      yOffset = (int) (Math.random() * height);

      this.xSpeed = xSpeed + (int) (Math.random() * width - xOffset) / 4;
      this.ySpeed = ySpeed + (int) (Math.random() * height) / 4;

      this.color = color;

      // Particles dependent on density of particles, width, and height.
      particleCount = particleDensity * width * height;
   }

   public void paint(Graphics g) {
      g.setColor(color);
      for (int i = 0; i < particleCount; i++) {
         g.fillRect(x + xOffset, y + yOffset + Panel.parallax, 8, 8);
         System.out.println(i);
      }
   }

   public void move() {
      x = x + xSpeed; // Move particles.
      y = y + ySpeed;

      // Increase or decrease x and y towards 0.
      if (xSpeed != 0)
         if (xSpeed > 0)
            xSpeed--;
         else if (x < 0)
            xSpeed++;
      ySpeed++; // Always make particles fall downwards.

      age++;
   }
}
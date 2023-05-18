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
import java.util.Random;
import java.awt.geom.*;
 
 public class Particles
 {
    // Center coordinates
    double centerX = 0;
    double centerY = 0;

    double radius = 1;

    // Random number generator
    Random random = new Random();

     public Particles(int originX, int originY, int width, int height)
     {
        
        // Generate a random angle in radians
        double angle = random.nextDouble() * Math.PI * 2;

        // Calculate x and y coordinates around the circle
        double x = centerX + radius * Math.cos(angle);
        double y = centerY + radius * Math.sin(angle);

        System.out.println("Direction of travel: (" + x + ", " + y + ")");
    }
 }
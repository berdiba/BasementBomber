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
 
 public class Particles
 {
    int center;

     public Particles(int x, int y, int width, int height)
     {
        center = width/2;
        //Made it so particles are propelled farther outwards from center the farther away they start from center. Print row and collum of particles with dimensions 4x4. Particles affect by gravity and also collisions, and will have a random ammount if time that they despawn in. Add colours later on.
     }
 }
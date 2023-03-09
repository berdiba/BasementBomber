/**
 * Code for the player.
 *
 * @author BAXTER BERDINNER
 * @version 20/02/2023
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.lang.Math;
import java.awt.geom.*;

public class Player extends Rectangle
{
    public Player(int width, int height)
    {
        setBounds(Panel.WIDTH/2, Panel.HEIGHT, width, height);
    }
}

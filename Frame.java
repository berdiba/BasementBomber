/**
 * Frame which holds the panel.
 *
 * @author BAXTER BERDINNER
 * @version 7/03/2023
 */

//Import nessesary extensions.
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class Frame extends JFrame implements ActionListener
{
    JMenuBar menuBar;
    JMenu menu;
    JMenuItem menuItem;
    
    Panel panel;
    
    Image CD = new ImageIcon("").getImage();

    public Frame()
    {
        panel = new Panel(); //Creates a new instance of panel class.

        //!!!!!!!!!!!!!!!BEFORE MAKING THE PANEL DO THE SEPERATE MENU CLASS FIRST. or not. maybe find another solution.

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.add(panel);
        
        this.setIconImage(CD);
        this.setTitle("Basement Bomber");
        
        menuSetup();
        
        this.pack();
        this.setLocationRelativeTo(null);
        this.setResizable(false); //Stops user from being able to resize the window.
        this.setVisible(true);
    }
    
    public void menuSetup()
    {
        menuBar = new JMenuBar(); //Menu things.
        this.setJMenuBar(menuBar);

        menu = new JMenu("Options");
        menuBar.add(menu);

        menuItem = new JMenuItem("Quit");
        menuItem.addActionListener(this);
        menuItem.setAccelerator(KeyStroke.getKeyStroke("ESCAPE"));
        menu.add(menuItem);
    }
    
    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();

        switch(cmd) 
        {
            case "Quit" : System.exit(0);
                break;
            default : System.out.println("Invalid input detected.");
        }
    }
}
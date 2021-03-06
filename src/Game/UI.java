package Game;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

// This class takes care of all the graphics to display a certain state
public class UI extends JPanel
{
    private JFrame window;
    private int[][] state;
    private int size;

    // Constructor: sets everything up
    public UI(int x, int y, int _size)
    {
        size = _size;
        setPreferredSize(new Dimension(x * size, y * size));

        window = new JFrame("Pentomino");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.add(this);
        window.pack();
        window.setVisible(true);

        state = new int[x][y];
        for (int i = 0; i < state.length; i++){
            for (int j = 0; j < state[i].length; j++){
                state[i][j] = -1;
            }
        }
    }

    
    /** 
     * @param g
     */
    // Paint function, called by the system if required for a new frame, uses the state stored by the UI class
    public void paintComponent(Graphics g)
    {
        Graphics2D localGraphics2D = (Graphics2D) g;

        //localGraphics2D.setColor(Color.LIGHT_GRAY);
        localGraphics2D.fill(getVisibleRect());

        // draw lines
        localGraphics2D.setColor(Color.BLACK);
        for (int i = 0; i <= state.length; i++)
        {
            localGraphics2D.drawLine(i * size, 0, i * size, state[0].length * size);
        }
        for (int i = 0; i <= state[0].length; i++)
        {
            localGraphics2D.drawLine(0, i * size, state.length * size, i * size);
        }

        // draw blocks
        for (int i = 0; i < state.length; i++)
        {
            for (int j = 0; j < state[0].length; j++)
            {
                localGraphics2D.setColor(GetColorOfID(state[i][j]));
                localGraphics2D.fill(new Rectangle2D.Double(i * size + 1, j * size + 1, size - 1, size - 1));
            }
        }
    }

    
    /** 
     * @param i
     * @return Color
     */
    // Decodes the ID of a pentomino into a color
    private Color GetColorOfID(int i)
    {
        if(i==0) return Color.BLACK;
        else if(i==1) return Color.GREEN;
        else return Color.RED;
    }

    
    /** 
     * @param _state
     */
    // This function should be called to update the displayed state (Makes a copy)
    public void setState(int[][] _state)
    {
        for (int i = 0; i < state.length; i++){
            for (int j = 0; j < state[i].length; j++){
                state[i][j] = _state[i][j];
            }
        }

        // Tells the system a frame update is required
        repaint();
    }

    
    /** 
     * @return Component
     */
    public Component getWindow() {
        return window;
    }
}

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

public class VerticalLabelPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private static final int fontSize = 12, theta = 270;

	private String axisName;
	
	public VerticalLabelPanel(String axisName) {
		this.axisName = axisName;
		this.setPreferredSize(new Dimension(fontSize + 5, this.getHeight()));
	}
	
	@Override
    protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(new Font(null, Font.BOLD, fontSize));

        AffineTransform at = new AffineTransform();
        at.rotate(Math.toRadians(theta));
        g2.setFont(g2.getFont().deriveFont(at));
        g2.drawString(axisName, fontSize, this.getHeight()/2);
	}
}

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JPanel;

public class GraphDisplayPanel extends JPanel implements Serializable{
	private static final long serialVersionUID = 1L;
	private static final double plane_reduce_rate = 0.1, color_rate = 0.8;
	private static final Color[] function_color = new Color[] {Color.RED, Color.BLUE, Color.GREEN,
		Color.MAGENTA, Color.ORANGE, Color.CYAN, Color.PINK,
		Color.LIGHT_GRAY, Color.GRAY, Color.DARK_GRAY};
	private static final int xGapMaxCount = 20, yGapMaxCount = 20;
	
	private ArrayList<ArrayList<Point2D.Double>> functions;
	
	private int functionCount;
	private double xMin, xMax, yMin, yMax, xMinDraw, xMaxDraw, yMinDraw, yMaxDraw, xDrawRange, yDrawRange, panelWidth, panelHeight;

    public GraphDisplayPanel() {
    	functions = new ArrayList<ArrayList<Point2D.Double>>();
		xMin = xMax = yMin = yMax = functionCount = 0;
    }

    @Override
    protected void paintComponent(Graphics g) {
    	Graphics2D g2 = (Graphics2D) g;
		
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        panelWidth = getWidth();
        panelHeight = getHeight();
        g2.setColor(Color.WHITE);
        g2.fill(new Rectangle2D.Double(0, 0, panelWidth, panelHeight));
        
        g2.setColor(Color.BLACK);
        g2.draw(new Line2D.Double(0, transY(0), panelWidth, transY(0)));
        g2.draw(new Line2D.Double(transX(0), 0, transX(0), panelHeight));
        
        g2.drawString("0", (float)transX(0)-10, (float)transY(0)+10);
        //g2.drawString(x, (float)(panelWidth * 0.97), (float)transY(0) - 6);
        //g2.drawString(y, (float)transX(0) + 6, (float)(panelHeight * 0.03));
        
        int xGapPower = (int)Math.log10((xMax - xMin)/10);
        int xGap = (int)Math.pow(10.0, (double)xGapPower);
        while(xGapMaxCount < (xMax - xMin) / xGap) {
        	++xGapPower;
        	xGap = (int)Math.pow(10.0, (double)xGapPower);
        }
        int xGapInit = (int)(xMin / xGap) * xGap;
        for(int i = xGapInit > xMin ? xGapInit : xGapInit + xGap; i < (int)xMax; i+=xGap) {
        	if(i != 0) {
        		Shape dot = new Ellipse2D.Double(transX(i)-3, transY(0)-3, 6, 6);
        		g2.draw(dot);
        		g2.fill(dot);
        		g2.drawString(String.valueOf(i), (float)transX(i), (float)transY(0)+15);
        	}
        }
        
        int yGapPower = (int)Math.log10((yMax - yMin)/10);
        int yGap = (int)Math.pow(10.0, (double)yGapPower);
        while(yGapMaxCount < (yMax - yMin) / yGap) {
        	++yGapPower;
        	yGap = (int)Math.pow(10.0, (double)yGapPower);
        }
        int yGapInit = (int)(yMin / yGap) * yGap;
        for(int i = yGapInit > yMin ? yGapInit : yGapInit + yGap; i < (int)yMax; i+=yGap) {
        	if(i != 0) {
        		Shape dot = new Ellipse2D.Double(transX(0)-3, transY(i)-3, 6, 6);
        		g2.draw(dot);
        		g2.fill(dot);
        		g2.drawString(String.valueOf(i), (float)transX(0)-30, (float)transY(i));
        	}
        }
        
        for(int i=0; i<functionCount; ++i) {
	        if(i < function_color.length)
	        	g2.setColor(function_color[i]);
	        else {
	        	int quotient = i / function_color.length;
	        	Color color = function_color[i % function_color.length];
	        	g2.setColor(new Color((int)(color.getRed() * Math.pow(color_rate, quotient)), (int)(color.getGreen() * Math.pow(color_rate, quotient)), (int)(color.getBlue() * Math.pow(color_rate, quotient))));
	        }
        	ArrayList<Point2D.Double> function = functions.get(i);
        	int pointCount = function.size();
        	double prevX = transX(function.get(0).getX()), prevY = transY(function.get(0).getY());
        	for(int j=1; j<pointCount; ++j) {
        		double nowX = transX(function.get(j).getX()), nowY = transY(function.get(j).getY()); 		
        		g2.draw(new Line2D.Double(prevX, prevY, nowX, nowY));
        		prevX = nowX;
        		prevY = nowY;
        	}
        }
    }
    
    /**
     * 그래프를 추가한다.
     * @param function 추가될 함수
     */
    public void add_graph(ArrayList<Point2D.Double> function) {
		++functionCount;
		functions.add(function);
		set_range();
		repaint();
	}
    
    /**
     * 그래프를 제거한다.
     * @param index 제거할 그래프의 인덱스
     * @return 그래프 제거 성공 여부 (제거 성공은 true, 제거 실패는 false.)
     */
    public boolean remove_graph(int index) {
    	if(index < 0 || index > functionCount) return false;
		--functionCount;
		functions.remove(index);
		set_range();
		repaint();
		return true;
	}
	
	/**
	 * 그래프를 그리기 위한 설정.
	 * x값의 최소값, 최대값, 범위를 구하고 y값도 최소값, 최대값, 범위를 구한다.
	 * x축과 y축이 보여야 하므로, 각 범위의 최소값이 0보다 크거나 최대값이 0보다 작으면 0으로 맞춘다.
	 */
	public void set_range() {
		if(functionCount > 0) {
			ArrayList<Point2D.Double> lastFunction = functions.get(functionCount-1);
			for(Point2D.Double dataPoint : lastFunction) {	
				xMin = dataPoint.x < xMin ? dataPoint.x : xMin;
				xMax = dataPoint.x > xMax ? dataPoint.x : xMax;
				yMin = dataPoint.y < yMin ? dataPoint.y : yMin;
				yMax = dataPoint.y > yMax ? dataPoint.y : yMax;
			}
			xMinDraw = xMin - (xMax - xMin) * plane_reduce_rate / 2;
			xMaxDraw = xMax + (xMax - xMin) * plane_reduce_rate / 2;
			yMinDraw = yMin - (yMax - yMin) * plane_reduce_rate / 2;
			yMaxDraw = yMax + (yMax - yMin) * plane_reduce_rate / 2;
			xDrawRange = xMaxDraw - xMinDraw;
			yDrawRange = yMaxDraw - yMinDraw;
		}
	}
	
	/**
	 * 함수의 x값을 화면의 x값으로 변환시켜준다.
	 * @param x			함수의 x값
	 * @return			화면의 x값
	 */
	public double transX(double x) {
		return (x - xMinDraw) * panelWidth / xDrawRange;
	}
	
	/**
	 * 함수의 y값을 화면의 x값으로 변환시켜준다.
	 * @param y			함수의 y값
	 * @return			화면의 y값
	 */
	public double transY(double y) {
		return panelHeight - (y - yMinDraw) * panelHeight / yDrawRange;
	}
	
	/**
	 * index번째의 함수 데이터를 반환한다.
	 * @param index 색인
	 * @return	index번째의 함수 데이터
	 */
	public ArrayList<Point2D.Double> get_function(int index) {
		return functions.get(index);
	}
}

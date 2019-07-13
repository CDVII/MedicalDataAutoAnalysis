import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class AnalysisFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final double width_ratio = 0.6, height_ratio = 0.6;
	private static final int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
	private static final int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
	private static final int frame_width = (int)(width_ratio * screen_width);
	private static final int frame_height = (int)(height_ratio * screen_height); 
	private static final String title_name = "�м� ȭ��", xAxisName = "����", yAxisName = "��";
	
	private JPanel mainPanel, graphPanel, xlabelPanel, ylabelPanel;
	private JButton analysisButton;
	
	private ButtonActionListener listener;
	private ArrayList<ArrayList<Point2D.Double>> functions;
	
	/**
	 * ������
	 */
	public AnalysisFrame(int fileNumber, int dataNumber, ArrayList<ArrayList<Point2D.Double>> functions) {
		if(fileNumber == -1 && dataNumber == -1)	// ��ü �м� ���
			setTitle(title_name + " ( ��ü �м� ��� )");
		else										// ���� �м� ���
			setTitle(title_name + " ( " + fileNumber + "��° ���� : " + dataNumber + "��° ������ )");
		
		this.functions = functions;
		initPanel();
		
		// ������ �߰�
		listener = new ButtonActionListener();
		
		// �м� ��ư �߰�
		analysisButton = new JButton("�м�");
		analysisButton.addActionListener(listener);
		add(analysisButton, BorderLayout.SOUTH);
		
		drawGraph();
		
		setLocation((screen_width - frame_width) / 2, (screen_height - frame_height) / 2);
		setPreferredSize(new Dimension(frame_width, frame_height));
		pack();
		dispose();			// ���� â�� �ݱ� (���� â�� �ݾƵ� �ٸ� â�� ������ ����.)
		setVisible(true);
	}
	
	/**
	 * �г� �ʱ�ȭ
	 */
	private void initPanel() {
		// �����г�
		mainPanel = new JPanel(new BorderLayout());
		add(mainPanel, BorderLayout.CENTER);

		// ���� �ؽ�Ʈ
		xlabelPanel = new JPanel();
		xlabelPanel.add(new JLabel(xAxisName));
		mainPanel.add(xlabelPanel, BorderLayout.SOUTH);

		// ���� �ؽ�Ʈ
		ylabelPanel = new VerticalLabelPanel(yAxisName);
		mainPanel.add(ylabelPanel, BorderLayout.WEST);

		// �׷���
		graphPanel = new GraphDisplayPanel();
		mainPanel.add(graphPanel, BorderLayout.CENTER);
	}
	
	/**
	 * �׷��� �׸���
	 */
	private void drawGraph() {
		for(int i=0; i<functions.size(); ++i)
			((GraphDisplayPanel)graphPanel).add_graph(functions.get(i));
	}
	
	/**
	 * �׷��� 1���� �м��ؼ� �������� �������� �Ǵ��Ѵ�.
	 * @param data	�׷��� 1���� ������
	 * @return �м� ��� (������ ����, ������ �ƴϸ� ����)
	 * -3	���͵��� ����
	 * -2	���� ����
	 * -1	���ú�����ġ ����
	 * 0	����
	 */
	public int analysis(ArrayList<Point2D.Double> data)
	{
		double[] values = new double[data.size()];
		for(int i=0; i<values.length; ++i)
			values[i] = data.get(i).getY();
		
		Peak[] peaks = new Peak[3];
		int idx=149,t=0,flag=0;
		//int ch1=0,ch2=0,ch3=0;
		while(idx>1)
		{
			Peak peak = new Peak();
			idx = findRight(peak, values, idx);
			//ch1=idx;
			if(idx>0)
			{
				idx=findHigh(peak, values, idx);
				//ch2=idx;
				if(idx>0)
				{
					idx = findLeft(peak, values, idx);
					//ch3=idx;
					if(t<3)
					{
						if(peak.right-peak.left>5)
						{
							peaks[t]=peak;
							t++;
							//System.out.println(t+": r: "+ch1+" : "+data[ch1]+"\n"+t+": h: "+ch2+" : "+data[ch2]+"\n"+t+": l: "+ch3+" : "+data[ch3]);
						}
					}
					else
					{t=4;break;}
				}
			}
		}
		double min, max;
		min=max=values[149];
		for(int i=149; i>=0; i--)
		{
			if(values[i]<min)
				min=values[i];
			if(values[i]>max)
				max=values[i];
		}
		
		if(peaks[0]!=null&&peaks[0].high<100)
			flag=-3;
		if(t>4||t==0||min==0)
			flag=-2;
		else//if(peaks[0].right-peaks[0].high>peaks[0].high-peaks[0].left)
		{
			double slopeR = (values[peaks[0].high] - values[peaks[0].high+7])/7;
			double slopeL = (values[peaks[0].high] - values[peaks[0].high-7])/7;
			if(slopeL>slopeR)
				flag=-1;
			if(max-min<20)
				flag=-1;
		}
		//System.out.println(peaks[0].right);
		if(flag<0)
		{
			//if(peaks[0]!=null)
				//System.out.println(t+": r: "+peaks[0].right+" : "+data[peaks[0].right]+"\n"+t+": h: "+peaks[0].high+" : "+data[peaks[0].high]+"\n"+t+": l: "+peaks[0].left+" : "+data[peaks[0].left]);
			//else
				//System.out.println(t+": null");
		}
		return flag;
	}
	
	private static final double fix = 0.7;
	class Peak{
		int right, left, high;
		public Peak(){
			right=0; left=0; high=0;
		}
		public Peak(int right, int left, int high){
			this.right=right; this.left=left; this.high=high;
		}
	}
	
	private int findRight(Peak peak, double[] data, int idx) {
		for(int i=idx; i>0; i--) {
			if(data[i]+fix<=data[i-1]&&data[i]+fix*2<=data[i-2]) {
				peak.right=i;
				return i;
			}
		}
		return 0;
	}
	
	private int findHigh(Peak peak, double[] data, int idx) {
		for(int i=idx; i>0; i--) {
			if(data[i]>data[i-1]) {
				peak.high = i;
				return i;
			}
		}
		return 0;
	}
	
	private int findLeft(Peak peak, double[] data, int idx) {
		for(int i=idx; i>0; i--) {
			if((data[i]-data[i-1] < fix && data[i]-data[i-2] < 2 * fix)) //||data[i]<data[peak.right])
			{
				peak.left = i; 
				return i;
			}
		}
		return 0;
	}
	
	private class ButtonActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource().equals(analysisButton)) {		// �м� ��ư ó��
				if(functions.size() < 1) return;	// �����Ͱ� ���� ��,
				boolean valid = true;				// ��ȿ�� �˻�
				int index = 0;						// �����Ͱ� 1���� ��,
				if(functions.size() > 1) {			// �����Ͱ� ���� ��,
					String indexString = JOptionPane.showInputDialog("�� ��° �����͸� �м��Ͻðڽ��ϱ�?");
					if(!isNumber(indexString)) {		// ���ڰ� �ƴ� �����͸� �޾��� ��,	
						JOptionPane.showMessageDialog(null, "���ڷ� �Է��� �ֽʽÿ�!", "�Է� ����", JOptionPane.ERROR_MESSAGE);
						valid = false;
					}
					else {
						index = Integer.parseInt(indexString);
						if(index < 1 || index > functions.size()) {
							JOptionPane.showMessageDialog(null, "������ ��ȣ ���� �ʰ�!", "������ ����", JOptionPane.ERROR_MESSAGE);
							valid = false;
						}
						else
							--index;			// ���� �Է°����� index�� ���߱����� 1�� ���ҽ�Ų��.
					}
				}
				if(valid) {				// ��ȿ�� �������� �� �м��Ѵ�.
					int result = analysis(functions.get(index));
					switch(result) {
					case -3 : JOptionPane.showMessageDialog(null, "���͵��� ���� ������", "���", JOptionPane.ERROR_MESSAGE); break;
					case -2 : JOptionPane.showMessageDialog(null, "���� ���� ������", "���", JOptionPane.ERROR_MESSAGE);	break;
					case -1 : JOptionPane.showMessageDialog(null, "���ú�����ġ ���� ������", "���", JOptionPane.ERROR_MESSAGE); break;
					case 0 : JOptionPane.showMessageDialog(null, "���� ������", "���", JOptionPane.INFORMATION_MESSAGE); break;
					}
				}
			}
		}
	}
	
	/**
	 * �������� Ȯ���Ѵ�.
	 * @param word	�ܾ�
	 * @return	�����̸� true, �ƴϸ� false.
	 */
	private boolean isNumber(String word) {
		try {
			Double.parseDouble(word);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}

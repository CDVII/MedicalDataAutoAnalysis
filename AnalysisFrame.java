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
	private static final String title_name = "분석 화면", xAxisName = "범위", yAxisName = "농도";
	
	private JPanel mainPanel, graphPanel, xlabelPanel, ylabelPanel;
	private JButton analysisButton;
	
	private ButtonActionListener listener;
	private ArrayList<ArrayList<Point2D.Double>> functions;
	
	/**
	 * 생성자
	 */
	public AnalysisFrame(int fileNumber, int dataNumber, ArrayList<ArrayList<Point2D.Double>> functions) {
		if(fileNumber == -1 && dataNumber == -1)	// 단체 분석 모드
			setTitle(title_name + " ( 단체 분석 모드 )");
		else										// 단일 분석 모드
			setTitle(title_name + " ( " + fileNumber + "번째 파일 : " + dataNumber + "번째 데이터 )");
		
		this.functions = functions;
		initPanel();
		
		// 리스너 추가
		listener = new ButtonActionListener();
		
		// 분석 버튼 추가
		analysisButton = new JButton("분석");
		analysisButton.addActionListener(listener);
		add(analysisButton, BorderLayout.SOUTH);
		
		drawGraph();
		
		setLocation((screen_width - frame_width) / 2, (screen_height - frame_height) / 2);
		setPreferredSize(new Dimension(frame_width, frame_height));
		pack();
		dispose();			// 현재 창만 닫기 (현재 창을 닫아도 다른 창에 영향이 없다.)
		setVisible(true);
	}
	
	/**
	 * 패널 초기화
	 */
	private void initPanel() {
		// 메인패널
		mainPanel = new JPanel(new BorderLayout());
		add(mainPanel, BorderLayout.CENTER);

		// 가로 텍스트
		xlabelPanel = new JPanel();
		xlabelPanel.add(new JLabel(xAxisName));
		mainPanel.add(xlabelPanel, BorderLayout.SOUTH);

		// 세로 텍스트
		ylabelPanel = new VerticalLabelPanel(yAxisName);
		mainPanel.add(ylabelPanel, BorderLayout.WEST);

		// 그래프
		graphPanel = new GraphDisplayPanel();
		mainPanel.add(graphPanel, BorderLayout.CENTER);
	}
	
	/**
	 * 그래프 그리기
	 */
	private void drawGraph() {
		for(int i=0; i<functions.size(); ++i)
			((GraphDisplayPanel)graphPanel).add_graph(functions.get(i));
	}
	
	/**
	 * 그래프 1개를 분석해서 오류인지 정상인지 판단한다.
	 * @param data	그래프 1개의 데이터
	 * @return 분석 결과 (음수는 오류, 음수가 아니면 정상)
	 * -3	모터동작 오류
	 * -2	삽입 오류
	 * -1	샘플분주위치 오류
	 * 0	정상
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
			if(e.getSource().equals(analysisButton)) {		// 분석 버튼 처리
				if(functions.size() < 1) return;	// 데이터가 없을 때,
				boolean valid = true;				// 유효성 검사
				int index = 0;						// 데이터가 1개일 때,
				if(functions.size() > 1) {			// 데이터가 많을 때,
					String indexString = JOptionPane.showInputDialog("몇 번째 데이터를 분석하시겠습니까?");
					if(!isNumber(indexString)) {		// 숫자가 아닌 데이터를 받았을 때,	
						JOptionPane.showMessageDialog(null, "숫자로 입력해 주십시오!", "입력 오류", JOptionPane.ERROR_MESSAGE);
						valid = false;
					}
					else {
						index = Integer.parseInt(indexString);
						if(index < 1 || index > functions.size()) {
							JOptionPane.showMessageDialog(null, "데이터 번호 범위 초과!", "데이터 오류", JOptionPane.ERROR_MESSAGE);
							valid = false;
						}
						else
							--index;			// 실제 입력값에서 index로 맞추기위해 1을 감소시킨다.
					}
				}
				if(valid) {				// 유효한 데이터일 때 분석한다.
					int result = analysis(functions.get(index));
					switch(result) {
					case -3 : JOptionPane.showMessageDialog(null, "모터동작 오류 데이터", "결과", JOptionPane.ERROR_MESSAGE); break;
					case -2 : JOptionPane.showMessageDialog(null, "삽입 오류 데이터", "결과", JOptionPane.ERROR_MESSAGE);	break;
					case -1 : JOptionPane.showMessageDialog(null, "샘플분주위치 오류 데이터", "결과", JOptionPane.ERROR_MESSAGE); break;
					case 0 : JOptionPane.showMessageDialog(null, "정상 데이터", "결과", JOptionPane.INFORMATION_MESSAGE); break;
					}
				}
			}
		}
	}
	
	/**
	 * 숫자인지 확인한다.
	 * @param word	단어
	 * @return	숫자이면 true, 아니면 false.
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

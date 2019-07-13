import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Client extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private static final double width_ratio = 0.5, height_ratio = 0.5;
	private static final int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
	private static final int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
	private static final int frame_width = (int)(width_ratio * screen_width);
	private static final int frame_height = (int)(height_ratio * screen_height); 
	private static final String title_name = "Client";
	
	private static final double default_value = -1.0;			// 파일에서 데이터의 값이 숫자가 아닐 때 dafault 값
	private static final String scanEnd = "#### END ####";		// 스캔의 끝 텍스트(파일에서 이 부분은 공통이다.)
	private static final String xAxisName = "범위", yAxisName = "농도";
	
	private JPanel mainPanel, graphPanel, xlabelPanel, ylabelPanel;
	private JLabel xLabel;
	private JMenuBar menubar;
	private JMenu file_menu, analysis;
	private JMenuItem fileOpen, dataInit, exit, uniAnalysis, multiAnalysis;
	private JFileChooser fileChooser;
	
	private MenuItemActionListener listener;
	private ArrayList<Integer> fileDataCount;
	
	/**
	 * 생성자
	 */
	public Client()	{
		super(title_name);
		
		initObjects();
		initComponents();
		
		setLocation((screen_width - frame_width) / 2, (screen_height - frame_height) / 2);
		setPreferredSize(new Dimension(frame_width, frame_height));
		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	/**
	 * 객체 초기화
	 */
	private void initObjects() {
		// event처리기 등록
		listener = new MenuItemActionListener();

		fileDataCount = new ArrayList<Integer>();
	}
	
	/**
	 * 컴포넌트 초기화
	 */
	private void initComponents() {		
		initPanel();		// 패널 초기화
		initMenuBar();		// 메뉴바 초기화 (메뉴바는 BorderLayout.NORTH를 사용하여 화면 위에 배치)
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
		xLabel = new JLabel(xAxisName);
		xlabelPanel.add(xLabel);
		mainPanel.add(xlabelPanel, BorderLayout.SOUTH);

		// 세로 텍스트
		ylabelPanel = new VerticalLabelPanel(yAxisName);
		mainPanel.add(ylabelPanel, BorderLayout.WEST);

		// 그래프
		graphPanel = new GraphDisplayPanel();
		mainPanel.add(graphPanel, BorderLayout.CENTER);
	}

	/**
	 * 메뉴바 초기화
	 */
	private void initMenuBar() {
		menubar = new JMenuBar();
		add(menubar, BorderLayout.NORTH);

		file_menu = new JMenu("파일");
		menubar.add(file_menu);

		fileOpen = new JMenuItem("파일 열기");
		fileOpen.addActionListener(listener);
		file_menu.add(fileOpen);

		dataInit = new JMenuItem("데이터 초기화");
		dataInit.addActionListener(listener);
		file_menu.add(dataInit);

		exit = new JMenuItem("종료");
		exit.addActionListener(listener);
		file_menu.add(exit);

		analysis = new JMenu("분석");
		menubar.add(analysis);
		
		uniAnalysis = new JMenuItem("단일 분석");
		uniAnalysis.addActionListener(listener);
		analysis.add(uniAnalysis);
		
		multiAnalysis = new JMenuItem("단체 분석");
		multiAnalysis.addActionListener(listener);
		analysis.add(multiAnalysis);
	}
	
	/**
	 * 메뉴 아이템 이벤트 처리 클래스
	 * @author HwiYong Chang
	 */
	private class MenuItemActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Object selected = e.getSource();
			if(selected.equals(fileOpen)) {					// 파일 열기
				fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileNameExtensionFilter("CSV", "csv"));	// 파일 확장자 필터
				fileChooser.setMultiSelectionEnabled(false);							// 다중 선택 불가
				if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)	{	// 정상적인 파일을 선택하였을 때,
					//System.out.println(fileChooser.getSelectedFile().getName());
					ArrayList<ArrayList<Point2D.Double>> result = getData(fileChooser.getSelectedFile());
					if(result != null)		// 제대로 데이터를 추출했을 때,
						for(int i=0; i<result.size(); ++i)
							((GraphDisplayPanel)graphPanel).add_graph(result.get(i));
				}
				else																	// 정상적인 파일을 선택하지 못했을 때,
					JOptionPane.showMessageDialog(null, "csv 파일이 선택되지 않았습니다.", "파일 열기 오류", JOptionPane.ERROR_MESSAGE);	
			} else if(selected.equals(dataInit)) {			// 데이터 초기화
				if(JOptionPane.showConfirmDialog(null, "기록된 모든 데이터를 삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					remove(mainPanel);
					fileDataCount = new ArrayList<Integer>();
					initPanel();
					revalidate(); repaint();
				}
			} else if(selected.equals(exit))				// 종료
				System.exit(0);
			else if(selected.equals(uniAnalysis)) {			// 단일 분석
				JTextField fileNumberTextField = new JTextField();
				JTextField dataNumberTextField = new JTextField();
				
				Object[] message = {
				    "파일 번호:", fileNumberTextField,
				    "데이터 번호:", dataNumberTextField
				};

				ArrayList<ArrayList<Point2D.Double>> data = new ArrayList<ArrayList<Point2D.Double>>();
				int confirm = JOptionPane.showConfirmDialog(null, message, "단일 데이터 입력", JOptionPane.OK_CANCEL_OPTION);
				if (confirm == JOptionPane.OK_OPTION) {				// 확인 버튼을 눌렀을 때,
					if(!isNumber(fileNumberTextField.getText()) || !isNumber(dataNumberTextField.getText()))	// 숫자가 아닌 데이터가 있을 때,	
						JOptionPane.showMessageDialog(null, "숫자로 입력해 주십시오!", "입력 오류", JOptionPane.ERROR_MESSAGE);
					else {
						int fileNumber = Integer.parseInt(fileNumberTextField.getText());
						int dataNumber = Integer.parseInt(dataNumberTextField.getText());
						if(fileNumber < 1 || fileNumber > fileDataCount.size())							// 파일 번호 범위 초과
							JOptionPane.showMessageDialog(null, "파일 번호 범위 초과!", "파일 오류", JOptionPane.ERROR_MESSAGE);
						else if(dataNumber < 1 || dataNumber > fileDataCount.get(fileNumber-1))		// 데이터 번호 범위 초과
							JOptionPane.showMessageDialog(null, "데이터 번호 범위 초과!", "데이터 오류", JOptionPane.ERROR_MESSAGE);
						else {
							int index = 0;
							for(int i=1; i<fileNumber; ++i)
								index += fileDataCount.get(i-1);
							index += dataNumber-1;
							data.add(((GraphDisplayPanel)graphPanel).get_function(index));
							new AnalysisFrame(fileNumber, dataNumber, data);
						}
					}
				}
			} else if(selected.equals(multiAnalysis)) {		// 단체 분석
				JTextField fileNumberTextField = new JTextField();
				JTextField dataNumberTextField = new JTextField();
				
				Object[] message = {
				    "파일 번호:", fileNumberTextField,
				    "데이터 번호:", dataNumberTextField
				};

				ArrayList<ArrayList<Point2D.Double>> data = new ArrayList<ArrayList<Point2D.Double>>();
				int conti = 0;
				do {
					int confirm = JOptionPane.showConfirmDialog(null, message, "추가 데이터 입력", JOptionPane.OK_CANCEL_OPTION);
					if (confirm == JOptionPane.OK_OPTION) {
						if(!isNumber(fileNumberTextField.getText()) || !isNumber(dataNumberTextField.getText()))	// 숫자가 아닌 데이터가 있을 때,	
							JOptionPane.showMessageDialog(null, "숫자로 입력해 주십시오!", "입력 오류", JOptionPane.ERROR_MESSAGE);
						else {
							int fileNumber = Integer.parseInt(fileNumberTextField.getText());
							int dataNumber = Integer.parseInt(dataNumberTextField.getText());
							if(fileNumber < 1 || fileNumber > fileDataCount.size())							// 파일 번호 범위 초과
								JOptionPane.showMessageDialog(null, "파일 번호 범위 초과!", "파일 오류", JOptionPane.ERROR_MESSAGE);
							else if(dataNumber < 1 || dataNumber > fileDataCount.get(fileNumber-1))		// 데이터 번호 범위 초과
								JOptionPane.showMessageDialog(null, "데이터 번호 범위 초과!", "데이터 오류", JOptionPane.ERROR_MESSAGE);
							else {
								int index = 0;
								for(int i=1; i<fileNumber; ++i)
									index += fileDataCount.get(i-1);
								index += dataNumber-1;
								data.add(((GraphDisplayPanel)graphPanel).get_function(index));
							}
						}
					}
					// 데이터를 더 추가할 것인지에 대한 응답 물어보기
					conti = JOptionPane.showConfirmDialog(null, "데이터를 더 추가하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
				}while(conti == JOptionPane.OK_OPTION);		// 데이터를 더 추가할 것이라면 반복문 실행
				if(data.size() > 0)							// 데이터가 있을 때,
					new AnalysisFrame(-1, -1, data);
			}
		}
	}
	
	/**
	 * csv파일의 데이터에서 여러 사람의 데이터를 추출한다.
	 * @param file	csv파일
	 * @return	여러 사람의 데이터
	 */
	private ArrayList<ArrayList<Point2D.Double>> getData(File file) {
		try {
			ArrayList<ArrayList<Point2D.Double>> tempData = new ArrayList<ArrayList<Point2D.Double>>();	
			Scanner input = new Scanner(file);
			String[] words = input.nextLine().split(",");
			
			int columnCount = words.length;
			double[] xPointer = new double[columnCount];
			Arrays.fill(xPointer, 1.0);			// 모든 x값을 1로 초기화
			for(int i=0; i<columnCount; ++i)	// (x, y)좌표를 넣을 리스트 생성
				tempData.add(new ArrayList<Point2D.Double>());
			
			boolean blankLine = false, scan = false;
			while(input.hasNextLine()) {
				String line = input.nextLine();
				words = line.split(",");
				if(!scan && !blankLine && (line.equals("") || words.length == 0)) {	// 빈줄 (빈줄 다음이 스캔 시작 줄)
					blankLine = true;
					continue;
				}
				if(blankLine) {													// 스캔 시작 줄
					scan = true;
					blankLine = false;
					continue;
				}
				if(scan && words.length > 0 && words[0].equals(scanEnd))		// 스캔 끝 줄
					break;
				if(scan) {
					for(int i=0; i<words.length; ++i) {
						if(isNumber(words[i]))		// 숫자일 때,
							tempData.get(i).add(new Point2D.Double(xPointer[i], Double.parseDouble(words[i])));
						else						// 숫자가 아닐 때,
							tempData.get(i).add(new Point2D.Double(xPointer[i], default_value));
						++xPointer[i];
					}
				}
			}
			input.close();
			fileDataCount.add(columnCount);
			return tempData;
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
			return null;
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

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
	
	private static final double default_value = -1.0;			// ���Ͽ��� �������� ���� ���ڰ� �ƴ� �� dafault ��
	private static final String scanEnd = "#### END ####";		// ��ĵ�� �� �ؽ�Ʈ(���Ͽ��� �� �κ��� �����̴�.)
	private static final String xAxisName = "����", yAxisName = "��";
	
	private JPanel mainPanel, graphPanel, xlabelPanel, ylabelPanel;
	private JLabel xLabel;
	private JMenuBar menubar;
	private JMenu file_menu, analysis;
	private JMenuItem fileOpen, dataInit, exit, uniAnalysis, multiAnalysis;
	private JFileChooser fileChooser;
	
	private MenuItemActionListener listener;
	private ArrayList<Integer> fileDataCount;
	
	/**
	 * ������
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
	 * ��ü �ʱ�ȭ
	 */
	private void initObjects() {
		// eventó���� ���
		listener = new MenuItemActionListener();

		fileDataCount = new ArrayList<Integer>();
	}
	
	/**
	 * ������Ʈ �ʱ�ȭ
	 */
	private void initComponents() {		
		initPanel();		// �г� �ʱ�ȭ
		initMenuBar();		// �޴��� �ʱ�ȭ (�޴��ٴ� BorderLayout.NORTH�� ����Ͽ� ȭ�� ���� ��ġ)
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
		xLabel = new JLabel(xAxisName);
		xlabelPanel.add(xLabel);
		mainPanel.add(xlabelPanel, BorderLayout.SOUTH);

		// ���� �ؽ�Ʈ
		ylabelPanel = new VerticalLabelPanel(yAxisName);
		mainPanel.add(ylabelPanel, BorderLayout.WEST);

		// �׷���
		graphPanel = new GraphDisplayPanel();
		mainPanel.add(graphPanel, BorderLayout.CENTER);
	}

	/**
	 * �޴��� �ʱ�ȭ
	 */
	private void initMenuBar() {
		menubar = new JMenuBar();
		add(menubar, BorderLayout.NORTH);

		file_menu = new JMenu("����");
		menubar.add(file_menu);

		fileOpen = new JMenuItem("���� ����");
		fileOpen.addActionListener(listener);
		file_menu.add(fileOpen);

		dataInit = new JMenuItem("������ �ʱ�ȭ");
		dataInit.addActionListener(listener);
		file_menu.add(dataInit);

		exit = new JMenuItem("����");
		exit.addActionListener(listener);
		file_menu.add(exit);

		analysis = new JMenu("�м�");
		menubar.add(analysis);
		
		uniAnalysis = new JMenuItem("���� �м�");
		uniAnalysis.addActionListener(listener);
		analysis.add(uniAnalysis);
		
		multiAnalysis = new JMenuItem("��ü �м�");
		multiAnalysis.addActionListener(listener);
		analysis.add(multiAnalysis);
	}
	
	/**
	 * �޴� ������ �̺�Ʈ ó�� Ŭ����
	 * @author HwiYong Chang
	 */
	private class MenuItemActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Object selected = e.getSource();
			if(selected.equals(fileOpen)) {					// ���� ����
				fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileNameExtensionFilter("CSV", "csv"));	// ���� Ȯ���� ����
				fileChooser.setMultiSelectionEnabled(false);							// ���� ���� �Ұ�
				if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)	{	// �������� ������ �����Ͽ��� ��,
					//System.out.println(fileChooser.getSelectedFile().getName());
					ArrayList<ArrayList<Point2D.Double>> result = getData(fileChooser.getSelectedFile());
					if(result != null)		// ����� �����͸� �������� ��,
						for(int i=0; i<result.size(); ++i)
							((GraphDisplayPanel)graphPanel).add_graph(result.get(i));
				}
				else																	// �������� ������ �������� ������ ��,
					JOptionPane.showMessageDialog(null, "csv ������ ���õ��� �ʾҽ��ϴ�.", "���� ���� ����", JOptionPane.ERROR_MESSAGE);	
			} else if(selected.equals(dataInit)) {			// ������ �ʱ�ȭ
				if(JOptionPane.showConfirmDialog(null, "��ϵ� ��� �����͸� �����Ͻðڽ��ϱ�?", "Ȯ��", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					remove(mainPanel);
					fileDataCount = new ArrayList<Integer>();
					initPanel();
					revalidate(); repaint();
				}
			} else if(selected.equals(exit))				// ����
				System.exit(0);
			else if(selected.equals(uniAnalysis)) {			// ���� �м�
				JTextField fileNumberTextField = new JTextField();
				JTextField dataNumberTextField = new JTextField();
				
				Object[] message = {
				    "���� ��ȣ:", fileNumberTextField,
				    "������ ��ȣ:", dataNumberTextField
				};

				ArrayList<ArrayList<Point2D.Double>> data = new ArrayList<ArrayList<Point2D.Double>>();
				int confirm = JOptionPane.showConfirmDialog(null, message, "���� ������ �Է�", JOptionPane.OK_CANCEL_OPTION);
				if (confirm == JOptionPane.OK_OPTION) {				// Ȯ�� ��ư�� ������ ��,
					if(!isNumber(fileNumberTextField.getText()) || !isNumber(dataNumberTextField.getText()))	// ���ڰ� �ƴ� �����Ͱ� ���� ��,	
						JOptionPane.showMessageDialog(null, "���ڷ� �Է��� �ֽʽÿ�!", "�Է� ����", JOptionPane.ERROR_MESSAGE);
					else {
						int fileNumber = Integer.parseInt(fileNumberTextField.getText());
						int dataNumber = Integer.parseInt(dataNumberTextField.getText());
						if(fileNumber < 1 || fileNumber > fileDataCount.size())							// ���� ��ȣ ���� �ʰ�
							JOptionPane.showMessageDialog(null, "���� ��ȣ ���� �ʰ�!", "���� ����", JOptionPane.ERROR_MESSAGE);
						else if(dataNumber < 1 || dataNumber > fileDataCount.get(fileNumber-1))		// ������ ��ȣ ���� �ʰ�
							JOptionPane.showMessageDialog(null, "������ ��ȣ ���� �ʰ�!", "������ ����", JOptionPane.ERROR_MESSAGE);
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
			} else if(selected.equals(multiAnalysis)) {		// ��ü �м�
				JTextField fileNumberTextField = new JTextField();
				JTextField dataNumberTextField = new JTextField();
				
				Object[] message = {
				    "���� ��ȣ:", fileNumberTextField,
				    "������ ��ȣ:", dataNumberTextField
				};

				ArrayList<ArrayList<Point2D.Double>> data = new ArrayList<ArrayList<Point2D.Double>>();
				int conti = 0;
				do {
					int confirm = JOptionPane.showConfirmDialog(null, message, "�߰� ������ �Է�", JOptionPane.OK_CANCEL_OPTION);
					if (confirm == JOptionPane.OK_OPTION) {
						if(!isNumber(fileNumberTextField.getText()) || !isNumber(dataNumberTextField.getText()))	// ���ڰ� �ƴ� �����Ͱ� ���� ��,	
							JOptionPane.showMessageDialog(null, "���ڷ� �Է��� �ֽʽÿ�!", "�Է� ����", JOptionPane.ERROR_MESSAGE);
						else {
							int fileNumber = Integer.parseInt(fileNumberTextField.getText());
							int dataNumber = Integer.parseInt(dataNumberTextField.getText());
							if(fileNumber < 1 || fileNumber > fileDataCount.size())							// ���� ��ȣ ���� �ʰ�
								JOptionPane.showMessageDialog(null, "���� ��ȣ ���� �ʰ�!", "���� ����", JOptionPane.ERROR_MESSAGE);
							else if(dataNumber < 1 || dataNumber > fileDataCount.get(fileNumber-1))		// ������ ��ȣ ���� �ʰ�
								JOptionPane.showMessageDialog(null, "������ ��ȣ ���� �ʰ�!", "������ ����", JOptionPane.ERROR_MESSAGE);
							else {
								int index = 0;
								for(int i=1; i<fileNumber; ++i)
									index += fileDataCount.get(i-1);
								index += dataNumber-1;
								data.add(((GraphDisplayPanel)graphPanel).get_function(index));
							}
						}
					}
					// �����͸� �� �߰��� �������� ���� ���� �����
					conti = JOptionPane.showConfirmDialog(null, "�����͸� �� �߰��Ͻðڽ��ϱ�?", "Ȯ��", JOptionPane.YES_NO_OPTION);
				}while(conti == JOptionPane.OK_OPTION);		// �����͸� �� �߰��� ���̶�� �ݺ��� ����
				if(data.size() > 0)							// �����Ͱ� ���� ��,
					new AnalysisFrame(-1, -1, data);
			}
		}
	}
	
	/**
	 * csv������ �����Ϳ��� ���� ����� �����͸� �����Ѵ�.
	 * @param file	csv����
	 * @return	���� ����� ������
	 */
	private ArrayList<ArrayList<Point2D.Double>> getData(File file) {
		try {
			ArrayList<ArrayList<Point2D.Double>> tempData = new ArrayList<ArrayList<Point2D.Double>>();	
			Scanner input = new Scanner(file);
			String[] words = input.nextLine().split(",");
			
			int columnCount = words.length;
			double[] xPointer = new double[columnCount];
			Arrays.fill(xPointer, 1.0);			// ��� x���� 1�� �ʱ�ȭ
			for(int i=0; i<columnCount; ++i)	// (x, y)��ǥ�� ���� ����Ʈ ����
				tempData.add(new ArrayList<Point2D.Double>());
			
			boolean blankLine = false, scan = false;
			while(input.hasNextLine()) {
				String line = input.nextLine();
				words = line.split(",");
				if(!scan && !blankLine && (line.equals("") || words.length == 0)) {	// ���� (���� ������ ��ĵ ���� ��)
					blankLine = true;
					continue;
				}
				if(blankLine) {													// ��ĵ ���� ��
					scan = true;
					blankLine = false;
					continue;
				}
				if(scan && words.length > 0 && words[0].equals(scanEnd))		// ��ĵ �� ��
					break;
				if(scan) {
					for(int i=0; i<words.length; ++i) {
						if(isNumber(words[i]))		// ������ ��,
							tempData.get(i).add(new Point2D.Double(xPointer[i], Double.parseDouble(words[i])));
						else						// ���ڰ� �ƴ� ��,
							tempData.get(i).add(new Point2D.Double(xPointer[i], default_value));
						++xPointer[i];
					}
				}
			}
			input.close();
			fileDataCount.add(columnCount);
			return tempData;
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "����", JOptionPane.ERROR_MESSAGE);
			return null;
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

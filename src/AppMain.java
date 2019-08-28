import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.TrayIcon.MessageType;
import javax.swing.JTextField;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class AppMain extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private Timer timer;
	private DisplayTrayIcon dti;
	private InformixConnection ifx = new InformixConnection("10.10.10.148","ccx116_citel_uccx", "uccxhruser", "Citel2018");

	//private InformixConnection ifx = new InformixConnection("argosamxccpub", "argosamxccpub_uccx", "uccxhruser", "Arg0s2016");

	private int abandonedCalls;
	private int abortedCalls;
	private int totalCalls;

	private int warningCalls;
	private int dangerCalls;
	private boolean warning = false;
	private boolean danger = false;

	private JTable table;
	private JTable table_1;

	private JScrollPane scrollPane;
	private JScrollPane scrollPane_1;

	private JButton btnStart;
	private JButton btnStop;

	private JLabel lblLlamadasPerdidas;

	private JLayeredPane layeredPane;
	private JPanel contentPane;
	private JTextField txtRepeat;
	private JTextField txtWarning;
	private JTextField txtDanger;
	private JLabel lblPulse;

	public boolean setParameters() {

		if (txtWarning.getText().equals("") || txtDanger.getText().equals("") || txtRepeat.getText().equals("")) {
			JOptionPane.showMessageDialog(new JFrame(), "Debe llenar todos los campos", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		} else {
			if (Integer.parseInt(txtRepeat.getText()) < 1) {
				JOptionPane.showMessageDialog(new JFrame(), "La repetición debe ser mayor a cero", "Error",
						JOptionPane.ERROR_MESSAGE);
				return false;
			} else if (Integer.parseInt(txtWarning.getText()) < 1) {
				JOptionPane.showMessageDialog(new JFrame(), "El valor de la advertencia debe ser mayor a cero", "Error",
						JOptionPane.ERROR_MESSAGE);
				return false;
			} else if (Integer.parseInt(txtDanger.getText()) < 1) {
				JOptionPane.showMessageDialog(new JFrame(), "El valor de la alerta debe ser mayor a cero", "Error",
						JOptionPane.ERROR_MESSAGE);
				return false;
			} else {
				this.warningCalls = Integer.parseInt(txtWarning.getText());
				this.dangerCalls = Integer.parseInt(txtDanger.getText());
				this.timer = new Timer((Integer.parseInt(txtRepeat.getText())) * 1000, this);
				return true;
			}
		}
	}

	public void start() {
		if (setParameters()) {
			if (ifx.connect()) {
				fetchTable();
				timer.start();

				lblPulse.setVisible(true);

				txtWarning.setEnabled(false);
				txtDanger.setEnabled(false);
				txtRepeat.setEnabled(false);

				btnStart.setEnabled(false);
				btnStop.setEnabled(true);

			}
		}
	}

	public void stop() {
		timer.stop();

		lblPulse.setVisible(false);

		txtWarning.setEnabled(true);
		txtDanger.setEnabled(true);
		txtRepeat.setEnabled(true);

		btnStart.setEnabled(true);
		btnStop.setEnabled(false);

		ifx.disconnect();
		
		danger=false;
		warning=false;
	}

	public void fetchTable() {
		if (ifx.isConnected()) {

			table.setModel(ifx.buildTableModel());
			table_1.setModel(ifx.buildTableModelAborted());
			abandonedCalls = table.getRowCount();
			abortedCalls = table_1.getRowCount();
			totalCalls = abandonedCalls + abortedCalls;

			lblLlamadasPerdidas.setText("Llamadas perdidas: " + totalCalls);

			if (totalCalls == 0 && warning == true) {
				lblLlamadasPerdidas.setForeground(new Color(0, 128, 0));
				warning = false;
				danger = false;
			}

			if (totalCalls > warningCalls - 1 && totalCalls < dangerCalls && warning == false) {
				lblLlamadasPerdidas.setForeground(new Color(128, 128, 0));
				System.out.println("Notificación enviada");
				try {
					dti.sendNotification("Advertencia", "Acercándose al límite de llamadas perdidas",
							MessageType.WARNING);
				} catch (Exception e) {
					e.printStackTrace();
				}
				warning = true;
			}

			if (totalCalls > dangerCalls - 1 && danger == false) {
				lblLlamadasPerdidas.setForeground(Color.red);
				System.out.println("Notificación enviada");
				try {
					dti.sendNotification("Alerta", "Límite de llamadas perdidas excedidas", MessageType.ERROR);
				} catch (Exception e) {
					e.printStackTrace();
				}
				danger = true;
				JOptionPane.showMessageDialog(new JFrame(), "Límite de llamadas perdidas alcanzado",
						"Abandoned Calls Counter", JOptionPane.ERROR_MESSAGE);
			}
			// ifx.disconnect();
		} else {

			stop();
			JOptionPane.showMessageDialog(new JFrame(), "Se ha cortado la conexión con el CCX",
					"Abandoned Calls Counter", JOptionPane.ERROR_MESSAGE);
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		fetchTable();
		System.out.print(".");
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AppMain frame = new AppMain();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public AppMain() {
		setTitle("Abandoned Calls Counter");

		setResizable(false);

		dti = new DisplayTrayIcon(this);
		setIconImage(Toolkit.getDefaultToolkit().getImage(AppMain.class.getResource("/images/home-icon.png")));

		URL url = AppMain.class.getResource("/images/pulse-point.gif");
		Icon icon = new ImageIcon(url);

		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 751, 644);

		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		layeredPane = new JLayeredPane();
		layeredPane.setBounds(15, 78, 700, 465);
		contentPane.add(layeredPane);
		layeredPane.setLayout(null);

		lblLlamadasPerdidas = new JLabel("LLamadas Perdidas: 0");
		lblLlamadasPerdidas.setBounds(162, 0, 350, 61);
		layeredPane.add(lblLlamadasPerdidas);
		lblLlamadasPerdidas.setForeground(new Color(0, 128, 0));
		lblLlamadasPerdidas.setHorizontalAlignment(SwingConstants.CENTER);
		lblLlamadasPerdidas.setFont(new Font("Tahoma", Font.PLAIN, 30));

		table = new JTable();
		table.setRowSelectionAllowed(false);

		table_1 = new JTable();
		table_1.setRowSelectionAllowed(false);

		scrollPane = new JScrollPane(table);
		scrollPane.setBounds(15, 77, 670, 168);
		layeredPane.add(scrollPane);

		scrollPane_1 = new JScrollPane(table_1);
		scrollPane_1.setBounds(15, 281, 670, 168);
		layeredPane.add(scrollPane_1);

		btnStart = new JButton("Start");
		btnStart.setBounds(167, 559, 115, 29);
		contentPane.add(btnStart);

		btnStop = new JButton("Stop");
		btnStop.setBounds(493, 559, 115, 29);
		contentPane.add(btnStop);

		btnStop.setEnabled(false);

		JLabel lblRepeat = new JLabel("Repetici\u00F3n");
		lblRepeat.setBounds(38, 26, 80, 20);
		contentPane.add(lblRepeat);

		txtRepeat = new JTextField();
		txtRepeat.setToolTipText("Consultas a la base de datos");
		txtRepeat.setBounds(111, 23, 57, 26);
		contentPane.add(txtRepeat);
		txtRepeat.setColumns(10);

		JLabel lblWarning = new JLabel("Advertencia");
		lblWarning.setBounds(237, 26, 100, 20);
		contentPane.add(lblWarning);

		txtWarning = new JTextField();
		txtWarning.setToolTipText("N\u00FAmero de llamadas perdidas");
		txtWarning.setBounds(334, 23, 57, 26);
		contentPane.add(txtWarning);
		txtWarning.setColumns(10);

		JLabel lblDanger = new JLabel("L\u00EDmite");
		lblDanger.setBounds(467, 26, 69, 20);
		contentPane.add(lblDanger);

		txtDanger = new JTextField();
		txtDanger.setToolTipText("N\u00FAmero de llamadas perdidas");
		txtDanger.setBounds(551, 23, 57, 26);
		contentPane.add(txtDanger);
		txtDanger.setColumns(10);

		JLabel lblNewLabel = new JLabel("(segundos)");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel.setBounds(40, 42, 69, 20);
		contentPane.add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("(Llamadas perdidas)");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel_1.setBounds(224, 42, 115, 20);
		contentPane.add(lblNewLabel_1);

		JLabel label = new JLabel("(Llamadas perdidas)");
		label.setFont(new Font("Tahoma", Font.PLAIN, 12));
		label.setBounds(436, 42, 115, 20);
		contentPane.add(label);

		lblPulse = new JLabel(icon);
		lblPulse.setBounds(615, 0, 100, 79);
		contentPane.add(lblPulse);
		lblPulse.setVisible(false);

		btnStart.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				start();
			}
		});

		btnStop.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
			}
		});

	}
}

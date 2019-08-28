import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;

import javax.swing.JFrame;

public class DisplayTrayIcon {

	private static JFrame frame;
	static TrayIcon trayIcon;

	public DisplayTrayIcon(JFrame jframe) {
		frame = jframe;
		showTrayIcon();
	}
	
	public void sendNotification(String title, String message, TrayIcon.MessageType messageType) throws AWTException, MalformedURLException
	  { 
		trayIcon.displayMessage(title, message, messageType);
	  }

	public static void showTrayIcon() {

		if (!SystemTray.isSupported()) {
			System.err.println("System tray not supported!");
			System.exit(0);
			return;
		}

		final PopupMenu popup = new PopupMenu();
		final SystemTray systemTray = SystemTray.getSystemTray();

		MenuItem exitItem = new MenuItem("Salir");

		popup.add(exitItem);

		trayIcon = new TrayIcon(
				Toolkit.getDefaultToolkit().getImage(AppMain.class.getResource("/images/home-icon.png")),
				"Abandoned Calls Counter",
				popup
				);
		trayIcon.setPopupMenu(popup);
		trayIcon.setImageAutoSize(true);

		exitItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				systemTray.remove(trayIcon);
				System.exit(0);
			}
		});

		trayIcon.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//System.out.println(e);
				
				if (e.getButton() == MouseEvent.BUTTON1) {
					frame.setVisible(true);
				}
			}
		});

		try {
			systemTray.add(trayIcon);
		} catch (AWTException e) {
			e.printStackTrace();
		}

	}
	
	

}

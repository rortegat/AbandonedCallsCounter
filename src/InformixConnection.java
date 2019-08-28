import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class InformixConnection {

	private String user;
	private String password;
	private String url;

	private static Connection con;

	public InformixConnection(String host, String server, String user, String password) {
		this.user = user;
		this.password = password;
		this.url = "jdbc:informix-sqli://" + host +":1504/db_cra:informixserver=" + server;

	}

	public boolean connect() {
		try {
			Class.forName("com.informix.jdbc.IfxDriver");
			con = DriverManager.getConnection(url, user, password);
			System.out.println("Connected");
			return true;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(new JFrame(), e.getMessage(), "Abandoned Calls Counter", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	public boolean disconnect() {
		try {
			con.close();
			System.out.println("Disconnected");
			return true;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(new JFrame(), e.getMessage(), "Abandoned Calls Counter", JOptionPane.ERROR_MESSAGE);
			return false;
		}

	}
	
	public boolean isConnected() {
		try {
			return !con.isClosed();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static String today() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.HOUR_OF_DAY, 5);
		return dateFormat.format(cal.getTime());
	}
	
	private static String tomorrow() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_MONTH, 1);
		cal.add(Calendar.HOUR_OF_DAY, 5);
		return dateFormat.format(cal.getTime());
	}


	public DefaultTableModel buildTableModel() {

		Vector<Vector<String>> data = new Vector<Vector<String>>();
		Vector<String> columns = new Vector<String>();

		DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
		Calendar cal = Calendar.getInstance();
		
		try {
			CallableStatement st = con.prepareCall("call sp_abandoned_calls_activity(?,?,?)");
			st.setString(1, today() + " 13:00:00.000");
			st.setString(2, tomorrow() + " 00:59:59.999");
			st.setInt(3, 0);

			ResultSet rs = st.executeQuery();

			columns.add("Hora inicio");
			columns.add("Hora fin");
			columns.add("Duración (seg)");
			columns.add("Número");
			columns.add("CSQ");
			columns.add("Agente");

			Vector<String> row;

			while (rs.next()) {

				row = new Vector<String>();

				cal.setTime(rs.getDate("call_start_time"));
				cal.add(Calendar.HOUR_OF_DAY, -5);
				row.add(dateFormat.format(cal.getTime()));
				
				cal.setTime(rs.getDate("call_abandon_time"));
				cal.add(Calendar.HOUR_OF_DAY, -5);
				row.add(dateFormat.format(cal.getTime()));
				
				double diferencia = rs.getDate("call_abandon_time").getTime() - rs.getDate("call_start_time").getTime();
			
				row.add(String.valueOf(diferencia/1000));

				row.add(rs.getString("call_ani"));
				row.add(rs.getString("call_routed_csq"));
				//if (rs.getString(6) == null)
					//row.add("Sin agente asignado");
				//else
					row.add(rs.getString("agent_name"));

				data.add(row);
			}

		} catch (SQLException e) {
			e.getMessage();
		}

		return new DefaultTableModel(data, columns);

	}
	
	public DefaultTableModel buildTableModelAborted() {

		Vector<Vector<String>> data = new Vector<Vector<String>>();
		Vector<String> columns = new Vector<String>();

		DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
		Calendar cal = Calendar.getInstance();

		try {
			CallableStatement st = con.prepareCall("call sp_aborted_rejected_call_detail(?,?,?)");
			st.setString(1, today()+" 13:00:00.000");
			st.setString(2, today()+" 19:59:59.999");
			st.setInt(3, 0);

			ResultSet rs = st.executeQuery();

			columns.add("Hora inicio");
			columns.add("Hora fin");
			columns.add("Razón");
			columns.add("Disposición");
			columns.add("dn originador");
			columns.add("Número marcado");
			columns.add("Número original");
			columns.add("CSQ");

			Vector<String> row;

			while (rs.next()) {

				row = new Vector<String>();

				cal.setTime(rs.getDate("start_time"));
				cal.add(Calendar.HOUR_OF_DAY, -5);
				row.add(dateFormat.format(cal.getTime()));
				
				cal.setTime(rs.getDate(2));
				cal.add(Calendar.HOUR_OF_DAY, -5);
				row.add(dateFormat.format(cal.getTime()));

				row.add(rs.getString(4)); // Razón
				row.add(rs.getString(5)); //Disposición
				row.add(rs.getString(6)); //Originator
				
				row.add(rs.getString(8)); //called
				row.add(rs.getString(9)); //original
				row.add(rs.getString(11)); //CSQ
				//if (rs.getString(6) == null)
					//row.add("Sin agente asignado");
				//else
					//row.add(rs.getString(6));

				data.add(row);
			}

		} catch (SQLException e) {
			e.getMessage();
		}

		return new DefaultTableModel(data, columns);

	}

}
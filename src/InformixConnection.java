import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

	private String inicio;
	private String fin;

	private static Connection con;

	public InformixConnection(String host, String server, String user, String password) {
		this.user = user;
		this.password = password;
		this.url = "jdbc:informix-sqli://" + host + ":1504/db_cra:informixserver=" + server;

	}

	public boolean connect() {
		try {
			Class.forName("com.informix.jdbc.IfxDriver");
			con = DriverManager.getConnection(url, user, password);
			System.out.println("Connected");
			return true;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(new JFrame(), e.getMessage(), "Abandoned Calls Counter",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	public boolean disconnect() {
		try {
			con.close();
			System.out.println("Disconnected");
			return true;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(new JFrame(), e.getMessage(), "Abandoned Calls Counter",
					JOptionPane.ERROR_MESSAGE);
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

	private static String hoy() {
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		return dateFormat.format(cal.getTime());
	}

	private static String darFormatoFecha(Date fecha) {
		DateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss a");
		Calendar cal = Calendar.getInstance();
		cal.setTime(fecha);
		return dateFormat.format(cal.getTime());
	}

	public static String convertirFecha(String fecha, String original, String convertir) throws ParseException {

		LocalDateTime ldt = LocalDateTime.parse(fecha, DateTimeFormatter.ofPattern("dd-M-yyyy hh:mm:ss a"));

		ZoneId originalZoneId = ZoneId.of(original);
		ZoneId convertedZoneId = ZoneId.of(convertir);

		ZonedDateTime originalDateTime = ldt.atZone(originalZoneId);
		ZonedDateTime convertedDateTime = originalDateTime.withZoneSameInstant(convertedZoneId);

		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss.SSS");

//        System.out.println("Date (Original) : " + format.format(originalDateTime)+" "+originalZoneId.getId());
//        System.out.println("Date (Converted) : " + format.format(convertedDateTime)+" "+convertedZoneId.getId());

		return format.format(convertedDateTime);

	}

	public static String convertirFechaTime(String fecha, String original, String convertir) throws ParseException {

		LocalDateTime ldt = LocalDateTime.parse(fecha, DateTimeFormatter.ofPattern("dd-M-yyyy hh:mm:ss a"));

		ZoneId originalZoneId = ZoneId.of(original);
		ZoneId convertedZoneId = ZoneId.of(convertir);

		ZonedDateTime originalDateTime = ldt.atZone(originalZoneId);
		ZonedDateTime convertedDateTime = originalDateTime.withZoneSameInstant(convertedZoneId);

		DateTimeFormatter format = DateTimeFormatter.ofPattern("hh:mm:ss a");

//        System.out.println("Date (Original) : " + format.format(originalDateTime)+" "+originalZoneId.getId());
//        System.out.println("Date (Converted) : " + format.format(convertedDateTime)+" "+convertedZoneId.getId());

		return format.format(convertedDateTime);

	}

	public void setFechaStoredProcedure() {
		try {
			this.inicio = convertirFecha(hoy() + " 08:00:00 AM", "America/Mexico_City", "UTC");
			this.fin = convertirFecha(hoy() + " 08:00:00 PM", "America/Mexico_City", "UTC");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public DefaultTableModel buildTableModel() {

		Vector<Vector<String>> data = new Vector<Vector<String>>();
		Vector<String> columns = new Vector<String>();

		try {
			CallableStatement st = con.prepareCall("call sp_abandoned_calls_activity(?,?,?)");
			st.setString(1, inicio);
			st.setString(2, fin);
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

				if (rs.getString("call_routed_csq") != null) {

					row = new Vector<String>();

					try {
						row.add(convertirFechaTime(darFormatoFecha(rs.getDate("call_start_time")), "UTC",
								"America/Mexico_City"));
						row.add(convertirFechaTime(darFormatoFecha(rs.getDate("call_abandon_time")), "UTC",
								"America/Mexico_City"));
					} catch (ParseException e) {
						e.printStackTrace();
					}

					double diferencia = rs.getDate("call_abandon_time").getTime()
							- rs.getDate("call_start_time").getTime();

					row.add(String.valueOf(diferencia / 1000));

					row.add(rs.getString("call_ani"));
					row.add(rs.getString("call_routed_csq"));
					// if (rs.getString(6) == null)
					// row.add("Sin agente asignado");
					// else
					row.add(rs.getString("agent_name"));

					data.add(row);
				}

			}

		} catch (SQLException e) {
			e.getMessage();
		}

		return new DefaultTableModel(data, columns);

	}

	public DefaultTableModel buildTableModelAborted() {

		Vector<Vector<String>> data = new Vector<Vector<String>>();
		Vector<String> columns = new Vector<String>();

		try {
			CallableStatement st = con.prepareCall("call sp_aborted_rejected_call_detail(?,?,?)");
			st.setString(1, inicio);
			st.setString(2, fin);
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

				try {
					row.add(convertirFechaTime(darFormatoFecha(rs.getDate("call_start_time")), "UTC",
							"America/Mexico_City"));
					row.add(convertirFechaTime(darFormatoFecha(rs.getDate("call_abandon_time")), "UTC",
							"America/Mexico_City"));
				} catch (ParseException e) {
					e.printStackTrace();
				}

				row.add(rs.getString(4)); // Razón
				row.add(rs.getString(5)); // Disposición
				row.add(rs.getString(6)); // Originator

				row.add(rs.getString(8)); // called
				row.add(rs.getString(9)); // original
				row.add(rs.getString(11)); // CSQ
				// if (rs.getString(6) == null)
				// row.add("Sin agente asignado");
				// else
				// row.add(rs.getString(6));

				data.add(row);
			}

		} catch (SQLException e) {
			e.getMessage();
		}

		return new DefaultTableModel(data, columns);

	}

}
/*
 * Created on 20-ene-05
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.gvnix.dynamiclist.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;



/**
 * The Class Fecha.
 *
 * @author prey
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Fecha {

	// Gregorian Calendar adopted Oct. 15, 1582 (2299161)
	/** El JGREG. */
	private static int JGREG = 15 + 31 * (10 + 12 * 1582);

	/** El HALFSECOND. */
	private static double HALFSECOND = 0.5;

	/**
	 * Obtener dia mes anyo.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto String
	 */
	public static String obtenerDiaMesAnyo(Date pObjFecha) {

		try {
			Calendar lObjCalendar = new GregorianCalendar();
			lObjCalendar.setTime(pObjFecha);
			int lIntDia = lObjCalendar.get(Calendar.DAY_OF_MONTH);
			int lIntMes = lObjCalendar.get(Calendar.MONTH) + 1;
			String lStrMes = "";
			if (lIntMes < 10) {
				lStrMes = "0" + lIntMes;
			} else {
				lStrMes = Integer.toString(lIntMes);
			}
			String lStrDia = "";
			if (lIntDia < 10) {
				lStrDia = "0" + lIntDia;
			} else {
				lStrDia = Integer.toString(lIntDia);
			}
			int lIntAnyo = lObjCalendar.get(Calendar.YEAR);
			String lStrRes = lStrDia + "/" + lStrMes + "/" + lIntAnyo;

			return lStrRes;

		} catch (Throwable e) {
			return null;
		}

	}

	/**
	 * Obtener dia mes anyo hora minutos.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto String
	 */
	public static String obtenerDiaMesAnyoHoraMinutos(Date pObjFecha) {

		try {
			Calendar lObjCalendar = new GregorianCalendar();
			lObjCalendar.setTime(pObjFecha);
			int lIntDia = lObjCalendar.get(Calendar.DAY_OF_MONTH);
			int lIntMes = lObjCalendar.get(Calendar.MONTH) + 1;
			String lStrMes = "";
			if (lIntMes < 10) {
				lStrMes = "0" + lIntMes;
			} else {
				lStrMes = Integer.toString(lIntMes);
			}
			String lStrDia = "";
			if (lIntDia < 10) {
				lStrDia = "0" + lIntDia;
			} else {
				lStrDia = Integer.toString(lIntDia);
			}
			int lIntAnyo = lObjCalendar.get(Calendar.YEAR);

			int lIntHora = lObjCalendar.get(Calendar.HOUR_OF_DAY);
			int lIntMinutos = lObjCalendar.get(Calendar.MINUTE);
			int lIntSegundos = lObjCalendar.get(Calendar.SECOND);
			String lStrHora = "";
			String lStrMinutos = "";
			String lStrSegundos = "";
			if (lIntHora < 10) {
				lStrHora = "0" + lIntHora;
			} else {
				lStrHora = Integer.toString(lIntHora);
			}
			if (lIntMinutos < 10) {
				lStrMinutos = "0" + lIntMinutos;
			} else {
				lStrMinutos = Integer.toString(lIntMinutos);
			}

			String lStrRes = lStrDia + "/" + lStrMes + "/" + lIntAnyo;
			lStrRes += " " + lStrHora + ":" + lStrMinutos;

			return lStrRes;

		} catch (Throwable e) {
			return null;
		}

	}

	/**
	 * Obtener dia mes anyo hora minutos segundos.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto String
	 */
	public static String obtenerDiaMesAnyoHoraMinutosSegundos(Date pObjFecha) {
		try {
			Calendar lObjCalendar = new GregorianCalendar();
			lObjCalendar.setTime(pObjFecha);
			int lIntDia = lObjCalendar.get(Calendar.DAY_OF_MONTH);
			int lIntMes = lObjCalendar.get(Calendar.MONTH) + 1;
			String lStrMes = "";
			if (lIntMes < 10) {
				lStrMes = "0" + lIntMes;
			} else {
				lStrMes = Integer.toString(lIntMes);
			}
			String lStrDia = "";
			if (lIntDia < 10) {
				lStrDia = "0" + lIntDia;
			} else {
				lStrDia = Integer.toString(lIntDia);
			}
			int lIntAnyo = lObjCalendar.get(Calendar.YEAR);
			String lStrRes = lStrDia + "/" + lStrMes + "/" + lIntAnyo;

			int lIntHora = lObjCalendar.get(Calendar.HOUR_OF_DAY);
			int lIntMinutos = lObjCalendar.get(Calendar.MINUTE);
			int lIntSegundos = lObjCalendar.get(Calendar.SECOND);
			String lStrHora = "";
			String lStrMinutos = "";
			String lStrSegundos = "";
			if (lIntHora < 10) {
				lStrHora = "0" + lIntHora;
			} else {
				lStrHora = Integer.toString(lIntHora);
			}
			if (lIntMinutos < 10) {
				lStrMinutos = "0" + lIntMinutos;
			} else {
				lStrMinutos = Integer.toString(lIntMinutos);
			}
			if (lIntSegundos < 10) {
				lStrSegundos = "0" + lIntSegundos;
			} else {
				lStrSegundos = Integer.toString(lIntSegundos);
			}
			lStrRes += " " + lStrHora + ":" + lStrMinutos + ":" + lStrSegundos;
			return lStrRes;

		}
		catch (Throwable e) {
			return null;
		}


	}

	/**
	 * Obtener anyo mes dia hora minutos segundos.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto String
	 */
	public static String obtenerAnyoMesDiaHoraMinutosSegundos(Date pObjFecha) {

		try {
			Calendar lObjCalendar = new GregorianCalendar();
			lObjCalendar.setTime(pObjFecha);
			int lIntDia = lObjCalendar.get(Calendar.DAY_OF_MONTH);
			int lIntMes = lObjCalendar.get(Calendar.MONTH) + 1;
			String lStrMes = "";
			if (lIntMes < 10) {
				lStrMes = "0" + lIntMes;
			} else {
				lStrMes = Integer.toString(lIntMes);
			}
			String lStrDia = "";
			if (lIntDia < 10) {
				lStrDia = "0" + lIntDia;
			} else {
				lStrDia = Integer.toString(lIntDia);
			}
			int lIntAnyo = lObjCalendar.get(Calendar.YEAR);
			String lStrRes = lIntAnyo + lStrMes + lStrDia;

			int lIntHora = lObjCalendar.get(Calendar.HOUR_OF_DAY);
			int lIntMinutos = lObjCalendar.get(Calendar.MINUTE);
			int lIntSegundos = lObjCalendar.get(Calendar.SECOND);
			String lStrHora = "";
			String lStrMinutos = "";
			String lStrSegundos = "";
			if (lIntHora < 10) {
				lStrHora = "0" + lIntHora;
			} else {
				lStrHora = Integer.toString(lIntHora);
			}
			if (lIntMinutos < 10) {
				lStrMinutos = "0" + lIntMinutos;
			} else {
				lStrMinutos = Integer.toString(lIntMinutos);
			}
			if (lIntSegundos < 10) {
				lStrSegundos = "0" + lIntSegundos;
			} else {
				lStrSegundos = Integer.toString(lIntSegundos);
			}
			lStrRes += " " + lStrHora + ":" + lStrMinutos + ":" + lStrSegundos;
			return lStrRes;

		} catch (Throwable e) {
			return null;
		}

	}

	/**
	 * Obtener anyo mes dia hora minutos segundos mili.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto String
	 */
	public static String obtenerAnyoMesDiaHoraMinutosSegundosMili(Date pObjFecha) {

		Calendar lObjCalendar = new GregorianCalendar();
		lObjCalendar.setTime(pObjFecha);
		int lIntMili = lObjCalendar.get(Calendar.MILLISECOND);
		String lStrFecha = obtenerAnyoMesDiaHoraMinutosSegundos(pObjFecha);
		lStrFecha += "." + lIntMili;
		return lStrFecha;
	}

	/**
	 * Obtener anyo.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto String
	 */
	public static String obtenerAnyo(Date pObjFecha) {

		try {
			Calendar lObjCalendar = new GregorianCalendar();
			lObjCalendar.setTime(pObjFecha);
			int lIntAnyo = lObjCalendar.get(Calendar.YEAR);
			return Integer.toString(lIntAnyo);

		} catch (Throwable e) {
			return null;
		}

	}

	/**
	 * Obtener entero anyo.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto int
	 */
	public static int obtenerEnteroAnyo(Date pObjFecha) {

		try {
			Calendar lObjCalendar = new GregorianCalendar();
			lObjCalendar.setTime(pObjFecha);
			int lIntAnyo = lObjCalendar.get(Calendar.YEAR);
			return lIntAnyo;

		} catch (Throwable e) {
			return -1;
		}
	}

	/**
	 * Obtener dia.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto String
	 */
	public static String obtenerDia(Date pObjFecha) {

		try {
			Calendar lObjCalendar = new GregorianCalendar();
			lObjCalendar.setTime(pObjFecha);
			int lIntDia = lObjCalendar.get(Calendar.DAY_OF_MONTH);
			return Integer.toString(lIntDia);

		} catch (Throwable e) {
			return null;
		}

	}

	/**
	 * Obtener mes.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto String
	 */
	public static String obtenerMes(Date pObjFecha) {

		try {
			Calendar lObjCalendar = new GregorianCalendar();
			lObjCalendar.setTime(pObjFecha);
			int lIntMes = lObjCalendar.get(Calendar.MONTH)+1;
			return Integer.toString(lIntMes);

		} catch (Throwable e) {
			return null;
		}

	}

	/**
	 * Obtener entero mes.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto int
	 */
	public static int obtenerEnteroMes(Date pObjFecha) {

		try {
			Calendar lObjCalendar = new GregorianCalendar();
			lObjCalendar.setTime(pObjFecha);
			return lObjCalendar.get(Calendar.MONTH);
		} catch (Throwable e) {
			return -1;
		}

	}

	/**
	 * Obtener hora minutos.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto String
	 */
	public static String obtenerHoraMinutos(Date pObjFecha) {

		try {
			Calendar lObjCalendar = new GregorianCalendar();
			lObjCalendar.setTime(pObjFecha);
			int lIntHora = lObjCalendar.get(Calendar.HOUR_OF_DAY);
			int lIntMinutos = lObjCalendar.get(Calendar.MINUTE);
			String lStrHora = "";
			String lStrMinutos = "";
			if (lIntHora < 10) {
				lStrHora = "0" + lIntHora;
			} else {
				lStrHora = Integer.toString(lIntHora);
			}
			if (lIntMinutos < 10) {
				lStrMinutos = "0" + lIntMinutos;
			} else {
				lStrMinutos = Integer.toString(lIntMinutos);
			}
			String lStrRes = lStrHora + ":" + lStrMinutos;
			return lStrRes;

		} catch (Throwable e) {
			return null;
		}

	}

	/**
	 * Obtener hora minutos segundos.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto String
	 */
	public static String obtenerHoraMinutosSegundos(Date pObjFecha) {

		try {
			Calendar lObjCalendar = new GregorianCalendar();
			lObjCalendar.setTime(pObjFecha);
			int lIntHora = lObjCalendar.get(Calendar.HOUR_OF_DAY);
			int lIntMinutos = lObjCalendar.get(Calendar.MINUTE);
			int lIntSegundos = lObjCalendar.get(Calendar.SECOND);
			String lStrHora = "";
			String lStrMinutos = "";
			String lStrSegundos = "";
			if (lIntHora < 10) {
				lStrHora = "0" + lIntHora;
			} else {
				lStrHora = Integer.toString(lIntHora);
			}
			if (lIntMinutos < 10) {
				lStrMinutos = "0" + lIntMinutos;
			} else {
				lStrMinutos = Integer.toString(lIntMinutos);
			}
			if (lIntSegundos < 10) {
				lStrSegundos = "0" + lIntSegundos;
			} else {
				lStrSegundos = Integer.toString(lIntSegundos);
			}
			String lStrRes = lStrHora + ":" + lStrMinutos + ":" + lStrSegundos;
			return lStrRes;

		} catch (Throwable e) {
			return null;
		}

	}

	/**
	 * Generar fecha hora minutos.
	 *
	 * @param pStrDiaMesAnyo el string DiaMesAnyo
	 * @param pStrHoraMinutos el string HoraMinutos
	 *
	 * @return un objeto Date
	 */
	public static Date generarFechaHoraMinutos(String pStrDiaMesAnyo, String pStrHoraMinutos) {

		try {
			SimpleDateFormat lObjFormato = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			Date lObjFecha = lObjFormato.parse(pStrDiaMesAnyo + " " + pStrHoraMinutos);
			return lObjFecha;
		} catch (Throwable e) {
			return null;
		}

	}

	/**
	 * Generar fecha hora minutos.
	 *
	 * @param pStrDiaMesAnyoHoraMinutos el string DiaMesAnyoHoraMinutos
	 *
	 * @return un objeto Date
	 */
	public static Date generarFechaHoraMinutos(String pStrDiaMesAnyoHoraMinutos) {

		try {
			SimpleDateFormat lObjFormato = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			Date lObjFecha = lObjFormato.parse(pStrDiaMesAnyoHoraMinutos);
			return lObjFecha;
		} catch (Throwable e) {
			return null;
		}
	}

	/**
	 * Generar fecha hora minutos segundos.
	 *
	 * @param pStrDiaMesAnyoHoraMinutosSegundos el string DiaMesAnyoHoraMinutosSegundos
	 *
	 * @return un objeto Date
	 */
	public static Date generarFechaHoraMinutosSegundos(String pStrDiaMesAnyoHoraMinutosSegundos) {

		try {
			SimpleDateFormat lObjFormato = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Date lObjFecha = lObjFormato.parse(pStrDiaMesAnyoHoraMinutosSegundos);
			return lObjFecha;
		} catch (Throwable e) {
			return null;
		}
	}

	/**
	 * Generar fecha hora minutos segundos mili.
	 *
	 * @param pStrDiaMesAnyoHoraMinutosSegundosMili el string DiaMesAnyoHoraMinutosSegundosMili
	 *
	 * @return un objeto Date
	 */
	public static Date generarFechaHoraMinutosSegundosMili(String pStrDiaMesAnyoHoraMinutosSegundosMili) {

		try {
			SimpleDateFormat lObjFormato = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SS");
			Date lObjFecha = lObjFormato.parse(pStrDiaMesAnyoHoraMinutosSegundosMili);
			return lObjFecha;
		} catch (Throwable e) {
			return null;
		}
	}

	/**
	 * Generar fecha.
	 *
	 * @param pStrDiaMesAnyo el string DiaMesAnyo
	 *
	 * @return un objeto Date
	 */
	public static Date generarFecha(String pStrDiaMesAnyo) {

		try {
			SimpleDateFormat lObjFormato = new SimpleDateFormat("dd/MM/yyyy");
			Date lObjFecha = lObjFormato.parse(pStrDiaMesAnyo);
			return lObjFecha;
		}
		catch (Throwable e) {
			return null;
		}

	}


	/**
	 * Generar fecha inversa.
	 *
	 * @param pStrAnyoMesDia the str anyo mes dia
	 *
	 * @return the date
	 */
	public static Date generarFechaInversa(String pStrAnyoMesDia) {

		try {
			SimpleDateFormat lObjFormato = new SimpleDateFormat("yyyyMMdd");
			Date lObjFecha = lObjFormato.parse(pStrAnyoMesDia);
			return lObjFecha;
		}
		catch (Throwable e) {
			return null;
		}

	}

	/**
	 * Generar fecha precision segundos.
	 *
	 * @param pStrFecha el string Fecha
	 *
	 * @return un objeto Date
	 */
	public static Date generarFechaPrecisionSegundos(String pStrFecha){
		try {
			SimpleDateFormat lObjFormato = new SimpleDateFormat("DD/MM/YYYY HH24:MI:SS");
			Date lObjFecha = lObjFormato.parse(pStrFecha);
			return lObjFecha;
		}
		catch (Throwable e) {
			return null;
		}

	}

	/**
	 * Es fecha1 mayor que fecha2.
	 *
	 * @param lObjFecha1 el l obj fecha1
	 * @param lObjFecha2 el l obj fecha2
	 *
	 * @return true, si satisfactorio
	 */
	public static boolean esFecha1MayorQueFecha2(Date lObjFecha1, Date lObjFecha2) {

		if (lObjFecha1 == null)
			return false;
		return lObjFecha1.after(lObjFecha2);
	}
	
	/**
	 * Es fecha1 mayor que fecha2.
	 *
	 * @param lObjFecha1 el l obj fecha1
	 * @param lObjFecha2 el l obj fecha2
	 *
	 * @return true, si satisfactorio
	 */
	public static boolean esFecha1MayorQueFecha2Nulos(Date lObjFecha1, Date lObjFecha2) {

		if (lObjFecha1 == null)
			return false;
		if (lObjFecha2 == null)
			return false;
		return lObjFecha1.after(lObjFecha2);
	}

	/**
	 * Es fecha1 mayor igual que fecha2.
	 *
	 * @param lObjFecha1 el l obj fecha1
	 * @param lObjFecha2 el l obj fecha2
	 *
	 * @return true, si satisfactorio
	 */
	public static boolean esFecha1MayorIgualQueFecha2(Date lObjFecha1, Date lObjFecha2) {

		if (lObjFecha1.after(lObjFecha2)) {
			return true;
		} else if (lObjFecha2.after(lObjFecha1)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Fraccionar en horas.
	 *
	 * @param lObjFecha1 el l obj fecha1
	 * @param lObjFecha2 el l obj fecha2
	 *
	 * @return un objeto int
	 */
	public static int fraccionarEnHoras(Date lObjFecha1, Date lObjFecha2) {

		double lDoubleResta = (lObjFecha1.getTime() - lObjFecha2.getTime()) / (double) (3600000);
		double lDoubleAbs = Math.floor(lDoubleResta);
		if (lDoubleAbs == lDoubleResta) {
			return (int) lDoubleAbs;
		} else {
			return (int) lDoubleAbs + 1;
		}

	}

	/*
	 * NO GASTAR ESTA FUNCION. NO VA BIEN public int fraccionarEn24Horas(Date
	 * pObjFecha2, Date pObjFecha1) { // Comentado y cambiado por prey el
	 * 15/11/2006
	 *
	 * Calendar calendar2 = Calendar.getInstance();
	 * calendar2.setTime(pObjFecha2); Calendar calendar1 =
	 * Calendar.getInstance(); calendar1.setTime(pObjFecha1); double
	 * lDoubleResta = (calendar2.getTimeInMillis() -
	 * calendar1.getTimeInMillis()) /(double)(3600000*24); double lDoubleAbs =
	 * Math.floor(lDoubleResta); if (lDoubleAbs == lDoubleResta) { return (int)
	 * lDoubleAbs; } else{ return (int) lDoubleAbs + 1; } }
	 */

	/**
	 * Pertenece mes.
	 *
	 * @param mes el mes
	 * @param listameses el listameses
	 *
	 * @return true, si satisfactorio
	 */
	private static boolean perteneceMes(int mes, int[] listameses) {

		boolean pertenece = false;
		for (int i = 0; i < listameses.length; i++) {
			if (listameses[i] == mes) {
				pertenece = true;
			}
		}
		return pertenece;
	}

	/**
	 * Dias por temporada.
	 *
	 * @param pObjFecini el objeto Fecini
	 * @param pObjFecfin el objeto Fecfin
	 *
	 * @return un objeto int[]
	 */
	public static int[] diasPorTemporada(Date pObjFecini, Date pObjFecfin) {

		int[] lObjDias = new int[3];
		int[] lObjMesesA = new int[] { 6, 7 };
		int[] lObjMesesM = new int[] { 5, 8 };
		int[] lObjMesesB = new int[] { 0, 1, 2, 3, 4, 9, 10, 11 };
		Date lObjFecaux = pObjFecini;
		Date lObjFecfinBucle = Fecha.mas24Horas(pObjFecfin);
		while (lObjFecaux.before(lObjFecfinBucle)) {
			int mes = Fecha.obtenerEnteroMes(lObjFecaux);
			if (Fecha.perteneceMes(mes, lObjMesesA)) {
				lObjDias[0]++;
			}
			if (Fecha.perteneceMes(mes, lObjMesesM)) {
				lObjDias[1]++;
			}
			if (Fecha.perteneceMes(mes, lObjMesesB)) {
				lObjDias[2]++;
			}
			lObjFecaux = Fecha.mas24Horas(lObjFecaux);
		}
		return lObjDias;
	}

	// Restar fechas en días
	/**
	 * Restar fechas.
	 *
	 * @param pObjFecha2 el objeto Fecha2
	 * @param pObjFecha1 el objeto Fecha1
	 *
	 * @return un objeto int
	 */
	public static int restarFechas(Date pObjFecha2, Date pObjFecha1) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(pObjFecha2);
		int dia2 = calendar.get(Calendar.DAY_OF_YEAR);
		int anyo2 = calendar.get(Calendar.YEAR);
		calendar.setTime(pObjFecha1);
		int dia1 = calendar.get(Calendar.DAY_OF_YEAR);
		int anyo1 = calendar.get(Calendar.YEAR);
		int diashastafinanyo1 = calendar.getActualMaximum(Calendar.DAY_OF_YEAR) - dia1;

		int dias = 0;
		if (anyo1 == anyo2)
			return (dia2 - dia1);
		else {
			for (int i = 1; i < anyo2 - anyo1; i++) {
				calendar.set(Calendar.YEAR, anyo1 + i);
				dias += calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
			}
			dias += diashastafinanyo1 + dia2;
			return dias;
		}

	}

	// Restar fechas en meses
	/**
	 * Restar fechas mes.
	 *
	 * @param pObjFecha2 el objeto Fecha2
	 * @param pObjFecha1 el objeto Fecha1
	 *
	 * @return un objeto int
	 */
	public static int restarFechasMes(Date pObjFecha2, Date pObjFecha1) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(pObjFecha2);
		int anyo2 = calendar.get(Calendar.YEAR);
		int mes2 = calendar.get(Calendar.MONTH);
		calendar.setTime(pObjFecha1);
		int anyo1 = calendar.get(Calendar.YEAR);
		int mes1 = calendar.get(Calendar.MONTH);
		if (anyo1 == anyo2)
			return (mes2 - mes1) + 1;
		else {
			int meses2 = (anyo2 - 1) * 12 + mes2;
			int meses1 = (anyo1 - 1) * 12 + mes1;
			return (meses2 - meses1) + 1;
		}
	}

	/**
	 * Mas24 horas.
	 *
	 * @param lObjFecha1 el l obj fecha1
	 *
	 * @return un objeto Date
	 */
	public static Date mas24Horas(Date lObjFecha1) {

		return new Date(lObjFecha1.getTime() + (3600000 * 24));
	}

	/**
	 * Convertir a fecha juliana.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto double
	 */
	public static double convertirAFechaJuliana(java.util.Date pObjFecha) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(pObjFecha);

		int year = calendar.get(Calendar.YEAR) - 1900;
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		int julianYear = year;
		if (year < 0)
			julianYear++;
		int julianMonth = month;
		if (month > 2) {
			julianMonth++;
		} else {
			julianYear--;
			julianMonth += 13;
		}

		double julian = (java.lang.Math.floor(365.25 * julianYear) + java.lang.Math.floor(30.6001 * julianMonth) + day + 1720995.0);

		if (day + 31 * (month + 12 * year) >= JGREG) {
			// change over to Gregorian calendar
			int ja = (int) (0.01 * julianYear);
			julian += 2 - ja + (0.25 * ja);
		}
		return java.lang.Math.floor(julian);
	}

	/**
	 * Obtener dia juliano.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto int
	 */
	public static int obtenerDiaJuliano(java.util.Date pObjFecha) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(pObjFecha);
		return calendar.get(Calendar.DAY_OF_YEAR);
	}

	/**
	 * Obtener trimestre.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto int
	 */
	public static int obtenerTrimestre(java.util.Date pObjFecha) {

		GregorianCalendar lObjCalendar = (GregorianCalendar) Calendar.getInstance();
		lObjCalendar.setTime(pObjFecha);
		int lIntMes = lObjCalendar.get(Calendar.MONTH);
		if (lIntMes < 3) {
			return 1;
		} else if (lIntMes < 6) {
			return 2;
		} else if (lIntMes < 9) {
			return 3;
		} else {
			return 4;
		}
	}

	/**
	 * Obtener semestre.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto int
	 */
	public static int obtenerSemestre(java.util.Date pObjFecha) {

		GregorianCalendar lObjCalendar = (GregorianCalendar) Calendar.getInstance();
		lObjCalendar.setTime(pObjFecha);
		int lIntMes = lObjCalendar.get(Calendar.MONTH);
		if (lIntMes < 6) {
			return 1;
		} else {
			return 2;
		}
	}

	/**
	 * Obtener fecha ultimo dia anyo anterior.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto Date
	 */
	public static Date obtenerFechaUltimoDiaAnyoAnterior(java.util.Date pObjFecha) {

		String lStrAnyo = obtenerAnyo(pObjFecha);
		int lIntAnyoAnterior = Integer.parseInt(lStrAnyo) - 1;
		String lStrNuevaFecha = "31/12/" + lIntAnyoAnterior;
		return generarFecha(lStrNuevaFecha);
	}

	/**
	 * Obtener fecha ultimo dia anyo actual.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto Date
	 */
	public static Date obtenerFechaUltimoDiaAnyoActual(java.util.Date pObjFecha) {

		String lStrAnyo = obtenerAnyo(pObjFecha);
		String lStrNuevaFecha = "31/12/" + lStrAnyo;
		return generarFecha(lStrNuevaFecha);
	}

	/**
	 * Obtener fecha ultimo dia semestre anterior.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto Date
	 */
	public static Date obtenerFechaUltimoDiaSemestreAnterior(java.util.Date pObjFecha) {

		int lIntSemestre = obtenerSemestre(pObjFecha);
		String lStrAnyo = obtenerAnyo(pObjFecha);
		if (lIntSemestre == 1) {
			// Hay que ir al año anterior
			int lIntAnyoAnterior = Integer.parseInt(lStrAnyo) - 1;
			String lStrNuevaFecha = "31/12/" + lIntAnyoAnterior;
			return generarFecha(lStrNuevaFecha);
		} else {
			String lStrNuevaFecha = "30/06/" + lStrAnyo;
			return generarFecha(lStrNuevaFecha);
		}
	}

	/**
	 * Obtener fecha ultimo dia semestre actual.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto Date
	 */
	public static Date obtenerFechaUltimoDiaSemestreActual(java.util.Date pObjFecha) {

		int lIntSemestre = obtenerSemestre(pObjFecha);
		String lStrAnyo = obtenerAnyo(pObjFecha);
		if (lIntSemestre == 1) {
			String lStrNuevaFecha = "30/06/" + lStrAnyo;
			return generarFecha(lStrNuevaFecha);
		} else {
			String lStrNuevaFecha = "31/12/" + lStrAnyo;
			return generarFecha(lStrNuevaFecha);
		}
	}

	/**
	 * Obtener fecha ultimo dia trimestre anterior.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto Date
	 */
	public static Date obtenerFechaUltimoDiaTrimestreAnterior(java.util.Date pObjFecha) {

		int lIntTrimestre = obtenerTrimestre(pObjFecha);
		String lStrAnyo = obtenerAnyo(pObjFecha);
		if (lIntTrimestre == 1) {
			// Hay que ir al año anterior
			int lIntAnyoAnterior = Integer.parseInt(lStrAnyo) - 1;
			String lStrNuevaFecha = "31/12/" + lIntAnyoAnterior;
			return generarFecha(lStrNuevaFecha);
		} else if (lIntTrimestre == 2) {
			String lStrNuevaFecha = "31/03/" + lStrAnyo;
			return generarFecha(lStrNuevaFecha);
		} else if (lIntTrimestre == 3) {
			String lStrNuevaFecha = "30/06/" + lStrAnyo;
			return generarFecha(lStrNuevaFecha);
		} else {
			String lStrNuevaFecha = "30/09/" + lStrAnyo;
			return generarFecha(lStrNuevaFecha);
		}
	}

	/**
	 * Obtener fecha ultimo dia trimestre actual.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto Date
	 */
	public static Date obtenerFechaUltimoDiaTrimestreActual(java.util.Date pObjFecha) {

		int lIntTrimestre = obtenerTrimestre(pObjFecha);
		String lStrAnyo = obtenerAnyo(pObjFecha);
		if (lIntTrimestre == 1) {
			String lStrNuevaFecha = "31/03/" + lStrAnyo;
			return generarFecha(lStrNuevaFecha);
		} else if (lIntTrimestre == 2) {
			String lStrNuevaFecha = "30/06/" + lStrAnyo;
			return generarFecha(lStrNuevaFecha);
		} else if (lIntTrimestre == 3) {
			String lStrNuevaFecha = "30/09/" + lStrAnyo;
			return generarFecha(lStrNuevaFecha);
		} else {
			String lStrNuevaFecha = "31/12/" + lStrAnyo;
			return generarFecha(lStrNuevaFecha);
		}
	}

	/*
	 * public Date obtenerFechaUltimoDiaMesAnterior(java.util.Date pObjFecha) {
	 *
	 * int lIntNumDias = Integer.parseInt(obtenerDia(pObjFecha)); return
	 * restarNDias(pObjFecha, lIntNumDias); }
	 */

	/**
	 * Obtener fecha ultimo dia mes anterior.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto Date
	 */
	public static Date obtenerFechaUltimoDiaMesAnterior(java.util.Date pObjFecha) {

		GregorianCalendar lObjCalendar = new GregorianCalendar();
		lObjCalendar.setTime(pObjFecha);
		lObjCalendar.add(Calendar.MONTH, -1);
		lObjCalendar.set(Calendar.DAY_OF_MONTH, lObjCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		return lObjCalendar.getTime();
	}

	/*
	 * public Date obtenerFechaUltimoDiaMesActual(java.util.Date pObjFecha) { //
	 * Para obtener la fecha del último día del mes actual, se suma un mes con
	 * la función add // y luego se invoca a obtenerFechaUltimoDiaMesAnterior
	 * GregorianCalendar lObjCalendar = new GregorianCalendar();
	 * lObjCalendar.setTime(pObjFecha); lObjCalendar.add(Calendar.MONTH, 1);
	 * Date lObjFechaCon1MesMas = lObjCalendar.getTime(); return
	 * obtenerFechaUltimoDiaMesAnterior(lObjFechaCon1MesMas); }
	 */

	/**
	 * Obtener fecha ultimo dia mes actual.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto Date
	 */
	public static Date obtenerFechaUltimoDiaMesActual(java.util.Date pObjFecha) {

		// Para obtener la fecha del último día del mes actual, se suma un mes
		// con la función add
		// y luego se invoca a obtenerFechaUltimoDiaMesAnterior
		GregorianCalendar lObjCalendar = new GregorianCalendar();
		lObjCalendar.setTime(pObjFecha);
		lObjCalendar.set(Calendar.DAY_OF_MONTH, lObjCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		return lObjCalendar.getTime();
	}

	/**
	 * Obtener fecha primer dia mes actual.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto Date
	 */
	public static Date obtenerFechaPrimerDiaMesActual(java.util.Date pObjFecha) {

		// Para obtener la fecha del primer día del mes actual
		GregorianCalendar lObjCalendar = new GregorianCalendar();
		lObjCalendar.setTime(pObjFecha);
		lObjCalendar.set(Calendar.DAY_OF_MONTH, 1);
		return lObjCalendar.getTime();
	}

	/**
	 * Restar n dias.
	 *
	 * @param lObjFecha1 el l obj fecha1
	 * @param pLngNumDias el lng num dias
	 *
	 * @return un objeto Date
	 */
	public static Date restarNDias(Date lObjFecha1, long pLngNumDias) {

		return new Date(lObjFecha1.getTime() - (3600000 * 24 * pLngNumDias));
	}
	
	public static Date sumarNDias(Date lObjFecha1, long pLngNumDias) {

		return new Date(lObjFecha1.getTime() + (3600000 * 24 * pLngNumDias));
	}

	/**
	 * Obtener plazo pago.
	 *
	 * @param pStrFecLiq el string FecLiq
	 * @param pIntPeriodoPago el integer PeriodoPago
	 *
	 * @return un objeto Calendar
	 *
	 * @throws Exception the exception
	 */
	public static Calendar obtenerPlazoPago(String pStrFecLiq, int pIntPeriodoPago) throws Exception {

		Calendar lObjCalendar = Calendar.getInstance();
		lObjCalendar.setTime(generarFecha(pStrFecLiq));
		int lIntDiaMes = lObjCalendar.get(Calendar.DAY_OF_MONTH);
		if (lIntDiaMes > 15) {
			lObjCalendar.set(Calendar.DATE, 5);
			lObjCalendar.set(Calendar.MONTH, lObjCalendar.get(Calendar.MONTH) + pIntPeriodoPago + 2);
		} else {
			lObjCalendar.set(Calendar.DATE, 20);
			lObjCalendar.set(Calendar.MONTH, lObjCalendar.get(Calendar.MONTH) + pIntPeriodoPago + 1);
		}
		// lObjCalendar.set(Calendar.MONTH, lObjCalendar.get(Calendar.MONTH) +
		// 1);
		return lObjCalendar;
	}

	// Modificación prey 11/01/2007 Se crea una función específica para el plazo
	// de pago cuando se manda el ANID
	// (Aceptación por parte del usuario del recibo). Ahi no se suma lo que hay
	// en el properties, sino que se pone
	// lo que dice la ley directamente.

	/**
	 * Obtener plazo pago anid.
	 *
	 * @param pStrFecLiq el string FecLiq
	 *
	 * @return un objeto Calendar
	 *
	 * @throws Exception the exception
	 */
	public Calendar obtenerPlazoPagoANID(String pStrFecLiq) throws Exception {

		Calendar lObjCalendar = Calendar.getInstance();
		lObjCalendar.setTime(generarFecha(pStrFecLiq));
		int lIntDiaMes = lObjCalendar.get(Calendar.DAY_OF_MONTH);
		int lIntMes = lObjCalendar.get(Calendar.MONTH);
		int lIntAnyo = lObjCalendar.get(Calendar.MONTH);
		if (lIntDiaMes > 15) {
			lObjCalendar.set(Calendar.DATE, 5);
			lObjCalendar.set(Calendar.MONTH, lObjCalendar.get(Calendar.MONTH) + 2);
		} else {
			lObjCalendar.set(Calendar.DATE, 20);
			lObjCalendar.set(Calendar.MONTH, lObjCalendar.get(Calendar.MONTH) + 1);
		}
		return lObjCalendar;
	}

	/**
	 * Obtener string plazo pago.
	 *
	 * @param pStrFecha el string Fecha
	 * @param pIntPeriodoPago el integer PeriodoPago
	 *
	 * @return un objeto String
	 *
	 * @throws Exception the exception
	 */
	public static String obtenerStringPlazoPago(String pStrFecha, int pIntPeriodoPago) throws Exception {

		Calendar lObjCalendar = obtenerPlazoPago(pStrFecha, pIntPeriodoPago);
		return obtenerDiaMesAnyo(lObjCalendar.getTime());
	}

	/**
	 * Restar mes.
	 *
	 * @param pStrFecha el string Fecha
	 *
	 * @return un objeto String
	 *
	 * @throws Exception the exception
	 */
	public static String restarMes(String pStrFecha) throws Exception {

		Calendar lObjCalendar = Calendar.getInstance();

		lObjCalendar.setTime(generarFecha(pStrFecha));
		lObjCalendar.set(Calendar.MONTH, lObjCalendar.get(Calendar.MONTH) - 1);
		return obtenerDiaMesAnyo(lObjCalendar.getTime());
	}

	/**
	 * Restar meses.
	 *
	 * @param lObjFecha1  Fecha
	 * @param pLnlObjFecha1gNumMeses
	 *
	 * @return un objeto String
	 *
	 * @throws Exception the exception
	 */
	public static Date restarNMeses(Date lObjFecha1, int pLnlObjFecha1gNumMeses) throws Exception {

		Calendar lObjCalendar = Calendar.getInstance();

		lObjCalendar.setTime(lObjFecha1);
		lObjCalendar.set(Calendar.MONTH, lObjCalendar.get(Calendar.MONTH) - pLnlObjFecha1gNumMeses);
		return lObjCalendar.getTime();
	}


	/**
	 * Sumar años.
	 *
	 * @param pStrFecha el string Fecha
	 *
	 * @return un objeto Date
	 *
	 * @throws Exception the exception
	 */
	public static Date sumarNAnos(Date pObjFecha, Integer pNumanos) throws Exception {

		Calendar lObjCalendar = Calendar.getInstance();

		lObjCalendar.setTime(pObjFecha);
		lObjCalendar.set(Calendar.YEAR, lObjCalendar.get(Calendar.YEAR) + pNumanos);
		return lObjCalendar.getTime();
	}

	/**
	 * Obtener n dias anyo.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto int
	 */
	public static int obtenerNDiasAnyo(Date pObjFecha) {

		try {
			Calendar lObjCalendar = new GregorianCalendar();
			lObjCalendar.setTime(pObjFecha);

			int lIntDias = lObjCalendar.getActualMaximum(Calendar.DAY_OF_YEAR);
			return lIntDias;

		} catch (Throwable e) {
			return 365;
		}
	}

	/**
	 * Es ultimo dia mes.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return true, si satisfactorio
	 */
	public static boolean esUltimoDiaMes(Date pObjFecha) {

		Calendar lObjCalendar = Calendar.getInstance();
		lObjCalendar.setTime(pObjFecha);
		return lObjCalendar.get(Calendar.DAY_OF_MONTH) == lObjCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Obtener n dias mes.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto int
	 */
	public static int obtenerNDiasMes(Date pObjFecha) {

		Calendar lObjCalendar = Calendar.getInstance();
		lObjCalendar.setTime(pObjFecha);
		return lObjCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Es anyo bisiesto.
	 *
	 * @param pIntAnyo el integer Anyo
	 *
	 * @return true, si satisfactorio
	 */
	public static boolean esAnyoBisiesto(int pIntAnyo) {

		if (pIntAnyo % 4 == 0) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Fraccionar en24 horas.
	 *
	 * @param pObjFecha2 el objeto Fecha2
	 * @param pObjFecha1 el objeto Fecha1
	 * @param pObjConexion el objeto Conexion
	 *
	 * @return un objeto int
	 *
	 * @throws SQLException the SQL exception
	 */
	public static int fraccionarEn24Horas(Date pObjFecha2, Date pObjFecha1, Connection pObjConexion)
			throws SQLException {

		PreparedStatement lObjStmt = null;
		ResultSet lObjRes = null;

		String lStrFecha2 = obtenerDiaMesAnyoHoraMinutos(pObjFecha2);
		String lStrFecha1 = obtenerDiaMesAnyoHoraMinutos(pObjFecha1);
		int lIntDiferencia = 0;
		try {
			double lDblDiferencia = 0;
			String lStrSQL = "select TO_DATE('" + lStrFecha2 + "','dd/mm/rr HH24:mi')- TO_DATE('" + lStrFecha1
					+ "','dd/mm/rr HH24:mi') as dif from dual";

			lObjStmt = pObjConexion.prepareStatement(lStrSQL);
			lObjRes = lObjStmt.executeQuery();
			if (lObjRes.next()) {
				lDblDiferencia = lObjRes.getDouble("dif");
				lDblDiferencia = Math.ceil(lDblDiferencia);
				// lIntDiferencia=(int)lDblDiferencia+1;
				lIntDiferencia = (int) lDblDiferencia;
			} else {
				throw new SQLException();
			}

			return lIntDiferencia;
		}

		catch (SQLException e) {
			throw e;
		}

		finally {
			lObjRes.close();
			lObjStmt.close();
		}

	}

	/**
	 * Fraccionar en3 horas.
	 *
	 * @param pObjFecha2 el objeto Fecha2
	 * @param pObjFecha1 el objeto Fecha1
	 * @param pObjConexion el objeto Conexion
	 *
	 * @return un objeto int
	 *
	 * @throws SQLException the SQL exception
	 */
	public static int fraccionarEn3Horas(Date pObjFecha2, Date pObjFecha1, Connection pObjConexion) throws SQLException {

		PreparedStatement lObjStmt = null;
		ResultSet lObjRes = null;

		String lStrFecha2 = obtenerDiaMesAnyoHoraMinutos(pObjFecha2);
		String lStrFecha1 = obtenerDiaMesAnyoHoraMinutos(pObjFecha1);
		int lIntDiferencia = 0;
		try {
			double lDblDiferencia = 0;
			String lStrSQL = "select TO_DATE('" + lStrFecha2 + "','dd/mm/rr HH24:mi')- TO_DATE('" + lStrFecha1
					+ "','dd/mm/rr HH24:mi') as dif from dual";

			lObjStmt = pObjConexion.prepareStatement(lStrSQL);
			lObjRes = lObjStmt.executeQuery();
			if (lObjRes.next()) {
				lDblDiferencia = lObjRes.getDouble("dif");
				lDblDiferencia = lDblDiferencia * 8;
				lDblDiferencia = Math.ceil(lDblDiferencia);
				// lIntDiferencia=(int)lDblDiferencia+1;
				lIntDiferencia = (int) lDblDiferencia;
			} else {
				throw new SQLException();
			}

			return lIntDiferencia;
		}

		catch (SQLException e) {
			throw e;
		}

		finally {
			lObjRes.close();
			lObjStmt.close();
		}

	}

	/**
	 * Obtener mes entre01y12.
	 *
	 * @param pObjFecha el objeto Fecha
	 *
	 * @return un objeto String
	 */
	public static String obtenerMesEntre01y12(Date pObjFecha) {

		try {
			Calendar lObjCalendar = new GregorianCalendar();
			lObjCalendar.setTime(pObjFecha);
			int lIntMes = lObjCalendar.get(Calendar.MONTH);
			String lStrDevolucion = Integer.toString(lIntMes + 1);
			if (lStrDevolucion.length() == 1) {
				lStrDevolucion = "0" + lStrDevolucion;
			}
			return lStrDevolucion;

		} catch (Throwable e) {
			return null;
		}

	}


	/**
	 * Obtener dia de la semana.
	 *
	 * @param pObjFecha la fecha
	 *
	 * @return true, if successful
	 */
	public static int obtenerDiaDelaSemana(Date pObjFecha){
		Calendar lObjCal = Calendar.getInstance();
		lObjCal.setTime(pObjFecha);
		//lObjCal.setFirstDayOfWeek(Calendar.MONDAY);
		//lObjCal.setTimeZone(TimeZone.getTimeZone("GMT+1"));

		int day = lObjCal.get(Calendar.DAY_OF_WEEK);
		return day;

	}

	/**
	 * Obtener fecha del día anterior a la que le pasamos
	 *
	 * @param pObjFecha la fecha
	 *
	 * @return true, if successful
	 */
	public static Date obtenerFechaDiaAnterior(Date pObjFecha){
		Calendar lObjCal = Calendar.getInstance();
		lObjCal.setTime(pObjFecha);
		lObjCal.add(Calendar.DATE, -1);
		return lObjCal.getTime();
	}

	/**
	 * Obtener fecha sin horas y minutos
	 *
	 * @param pObjFecha la fecha
	 *
	 * @return true, if successful
	 */
	public static Date obtenerFechaDiaMesAnyo(Date pObjFecha){
		try{
			SimpleDateFormat lObjFormato = new SimpleDateFormat("dd/MM/yyyy");
			Date lObjFecha = lObjFormato.parse(obtenerDiaMesAnyo(pObjFecha));
			return lObjFecha;
		} catch (Throwable e) {
			return null;
		}
	}
}
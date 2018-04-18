package cn.com.zach.tools.object;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {

	public static final String FORMAT_DATE_ONLY_CH = "yyyy年MM月dd日";

	public static final String FORMAT_DATE_CH = "yyyy年MM月dd日 HH时mm分ss秒";

	public static final String FORMAT_MONTH_ONLY = "yyyy-MM";

	public static final String FORMAT_DATE_ONLY = "yyyy-MM-dd";

	public static final String FORMAT_DATE_HOUR = "yyyy-MM-dd HH:mm";

	public static final String FORMAT_TIME_ONLY = "HH:mm:ss";

	public static final String FORMAT_COMPACT = "yyyyMMddHHmmssSSS";

	public static final String FORMAT_NORMAL = "yyyy-MM-dd HH:mm:ss";

	public static final String FORMAT_DETAIL = "yyyy-MM-dd HH:mm:ss.SSS";

	public static final String FORMAT_UTC = "yyyy-MM-dd'T'HH:mm:ssZ";

	/** 一天有86400秒 */
	public static final long DATE_SECOND = 86400;

	/** 一天有1440分 */
	public static final long DATE_MINUTE = 1440;

	public static final long MINUTE_SECOND = 60;

	/**
	 * 把日期转换date对象
	 * 
	 * @param date 日期
	 * @param format	日期格式
	 * @return date对象
	 */
	public final static Date parse(String date, String format) {
		try {
			SimpleDateFormat sf = new SimpleDateFormat(format);
			return sf.parse(date);
		} catch (ParseException e) {
			// e.printStackTrace();
			return null;
		}
	}

	/**
	 * 自动识别日期格式
	 * 
	 * @param date 日期
	 * @return
	 */
	public final static Date parse(String date) {
		Date answer = null;
		int length = date.length(), end = length - 1;
		char first = date.charAt(0), last = date.charAt(end);
		if (length >= 13 && first >= '0' && first <= '9' && (last == 'l' || last == 'L' || (last >= 48 && last <= 57))) {
			try {
				long time = 0;
				switch (last) {
					case 'l': // long l = 0l;
					case 'L': // long l = 0L;
						Long l = new Long(date.substring(0, end));
						if (l.toString().regionMatches(false, 0, date, 0, end)) {
							time = l;
						}
						break;
					default:
						if (last >= 48 && last <= 57) {// ASCII表中数字范围
							Long myLong = new Long(date);
							if (myLong.toString().regionMatches(false, 0, date, 0, length)) {
								time = myLong;
							}
						}
				}
				if (time > 0) {
					answer = new Date(time);
				}
			} catch (Exception e) {}
		}
		if (answer == null) {
			int feature = dateFeature(date).hashCode();
			String format = null;
			switch (feature) {
				case 1650024186: /* 特征: 812223 */
					format = "yyyy年M月d日";
					break;
				case 1650083768: /* 特征: 814223 */
					format = "yyyy年MM月d日";
					break;
				case 1650024248: /* 特征: 812243 */
					format = "yyyy年M月dd日";
					break;
				case 1650083830: /* 特征: 814243 */
					format = "yyyy年MM月dd日";
					break;
				case 1833350401: /* 特征: 81222301000100244546 */
					format = "yyyy年M月d日  H时mm分ss秒";
					break;
				case 1833290819: /* 特征: 81222301000100242546 */
					format = "yyyy年M月d日  H时m分ss秒";
					break;
				case 1833290757: /* 特征: 81222301000100242526 */
					format = "yyyy年M月d日  H时m分s秒";
					break;
				case 1890549059: /* 特征: 81222301000100442526 */
					format = "yyyy年M月d日  HH时m分s秒";
					break;
				case 1890608641: /* 特征: 81222301000100444526 */
					format = "yyyy年M月d日  HH时mm分s秒";
					break;
				case 1890608703: /* 特征: 81222301000100444546 */
					format = "yyyy年M月d日  HH时mm分ss秒";
					break;
				case -160794305: /* 特征: 81422301000100244546 */
					format = "yyyy年MM月d日  H时mm分ss秒";
					break;
				case -160853887: /* 特征: 81422301000100242546 */
					format = "yyyy年MM月d日  H时m分ss秒";
					break;
				case -160853949: /* 特征: 81422301000100242526 */
					format = "yyyy年MM月d日  H时m分s秒";
					break;
				case -103595647: /* 特征: 81422301000100442526 */
					format = "yyyy年MM月d日  HH时m分s秒";
					break;
				case -103536065: /* 特征: 81422301000100444526 */
					format = "yyyy年MM月d日  HH时mm分s秒";
					break;
				case -103536003: /* 特征: 81422301000100444546 */
					format = "yyyy年MM月d日  HH时mm分ss秒";
					break;
				case 812282047: /* 特征: 81224301000100244546 */
					format = "yyyy年M月dd日  H时mm分ss秒";
					break;
				case 812222465: /* 特征: 81224301000100242546 */
					format = "yyyy年M月dd日  H时m分ss秒";
					break;
				case 812222403: /* 特征: 81224301000100242526 */
					format = "yyyy年M月dd日  H时m分s秒";
					break;
				case 869480705: /* 特征: 81224301000100442526 */
					format = "yyyy年M月dd日  HH时m分s秒";
					break;
				case 869540287: /* 特征: 81224301000100444526 */
					format = "yyyy年M月dd日  HH时mm分s秒";
					break;
				case 869540349: /* 特征: 81224301000100444546 */
					format = "yyyy年M月dd日  HH时mm分ss秒";
					break;
				case -1181862659: /* 特征: 81424301000100244546 */
					format = "yyyy年MM月dd日  H时mm分ss秒";
					break;
				case -1181922241: /* 特征: 81424301000100242546 */
					format = "yyyy年MM月dd日  H时m分ss秒";
					break;
				case -1181922303: /* 特征: 81424301000100242526 */
					format = "yyyy年MM月dd日  H时m分s秒";
					break;
				case -1124664001: /* 特征: 81424301000100442526 */
					format = "yyyy年MM月dd日  HH时m分s秒";
					break;
				case -1124604419: /* 特征: 81424301000100444526 */
					format = "yyyy年MM月dd日  HH时mm分s秒";
					break;
				case -1124604357: /* 特征: 81424301000100444546 */
					format = "yyyy年MM月dd日  HH时mm分ss秒";
					break;
				case 53407410: /* 特征: 87474 */
					format = "yyyy-MM-dd";
					break;
				case 53405488: /* 特征: 87274 */
					format = "yyyy-M-dd";
					break;
				case 53405486: /* 特征: 87272 */
					format = "yyyy-M-d";
					break;
				case 53407408: /* 特征: 87472 */
					format = "yyyy-MM-d";
					break;
				case 2074869767: /* 特征: 8727210028484 */
					format = "yyyy-M-d H:mm:ss";
					break;
				case 2074867845: /* 特征: 8727210028284 */
					format = "yyyy-M-d H:m:ss";
					break;
				case 2074867843: /* 特征: 8727210028282 */
					format = "yyyy-M-d H:m:s";
					break;
				case 2076714885: /* 特征: 8727210048282 */
					format = "yyyy-M-d HH:m:s";
					break;
				case 2076716807: /* 特征: 8727210048482 */
					format = "yyyy-M-d HH:mm:s";
					break;
				case 2076716809: /* 特征: 8727210048484 */
					format = "yyyy-M-d HH:mm:ss";
					break;
				case -1519032951: /* 特征: 8747210028484 */
					format = "yyyy-MM-d H:mm:ss";
					break;
				case -1519034873: /* 特征: 8747210028284 */
					format = "yyyy-MM-d H:m:ss";
					break;
				case -1519034875: /* 特征: 8747210028282 */
					format = "yyyy-MM-d H:m:s";
					break;
				case -1517187833: /* 特征: 8747210048282 */
					format = "yyyy-MM-d HH:m:s";
					break;
				case -1517185911: /* 特征: 8747210048482 */
					format = "yyyy-MM-d HH:mm:s";
					break;
				case -1517185909: /* 特征: 8747210048484 */
					format = "yyyy-MM-d HH:mm:ss";
					break;
				case -1540039159: /* 特征: 8727410028484 */
					format = "yyyy-M-dd H:mm:ss";
					break;
				case -1540041081: /* 特征: 8727410028284 */
					format = "yyyy-M-dd H:m:ss";
					break;
				case -1540041083: /* 特征: 8727410028282 */
					format = "yyyy-M-dd H:m:s";
					break;
				case -1538194041: /* 特征: 8727410048282 */
					format = "yyyy-M-dd HH:m:s";
					break;
				case -1538192119: /* 特征: 8727410048482 */
					format = "yyyy-M-dd HH:mm:s";
					break;
				case -1538192117: /* 特征: 8727410048484 */
					format = "yyyy-M-dd HH:mm:ss";
					break;
				case -838974581: /* 特征: 8747410028484 */
					format = "yyyy-MM-dd H:mm:ss";
					break;
				case -838976503: /* 特征: 8747410028284 */
					format = "yyyy-MM-dd H:m:ss";
					break;
				case -838976505: /* 特征: 8747410028282 */
					format = "yyyy-MM-dd H:m:s";
					break;
				case -837129463: /* 特征: 8747410048282 */
					format = "yyyy-MM-dd HH:m:s";
					break;
				case -837127541: /* 特征: 8747410048482 */
					format = "yyyy-MM-dd HH:mm:s";
					break;
				case -837127539: /* 特征: 8747410048484 */
					format = "yyyy-MM-dd HH:mm:ss";
					break;
				case 49772974: /* 特征: 49498 */
					format = "dd/MM/yyyy";
					break;
				case 47924010: /* 特征: 29298 */
					format = "d/M/yyyy";
					break;
				case 49771052: /* 特征: 49298 */
					format = "dd/M/yyyy";
					break;
				case 47925932: /* 特征: 29498 */
					format = "d/MM/yyyy";
					break;
				case 1561739657: /* 特征: 4949810048484 */
					format = "dd/MM/yyyy HH:mm:ss";
					break;
				case 47896106: /* 特征: 28484 */
					format = "H:mm:ss";
					break;
				case 47894184: /* 特征: 28284 */
					format = "H:m:ss";
					break;
				case 47894182: /* 特征: 28282 */
					format = "H:m:s";
					break;
				case 49741224: /* 特征: 48282 */
					format = "HH:m:s";
					break;
				case 49743146: /* 特征: 48482 */
					format = "HH:mm:s";
					break;
				case 49743148: /* 特征: 48484 */
					format = "HH:mm:ss";
					break;
				case 2008622154: /* 特征: 8747410048484106 */
					format = "yyyy-MM-dd HH:mm:ss.SSS";
					break;
				case -1270120581: /* 特征: 874741048484 */
					format = "yyyy-MM-dd'T'HH:mm:ss";
					break;
				case 499697820: /* 特征: 874741048484106 */
					format = "yyyy-MM-dd'T'HH:mm:ss.SSS";
					break;
			}
			if (format != null) {
				try {
					SimpleDateFormat sf = new SimpleDateFormat(format);
					answer = sf.parse(date);
				} catch (ParseException e) {
					// e.printStackTrace();
				}
			}
		}
		return answer;
	}
	
	/**
	 * 计算字符串特征码
	 * 
	 * @param source
	 * @return
	 */
	private final static String dateFeature(String source) {
		char c = 0;
		int distance = 0;
		StringBuilder feature = new StringBuilder();
		for (int i = 0, size = source.length(); i < size; i++) {
			c = source.charAt(i);
			switch (c) {
				case '年': // 特征值:1
					feature.append(distance).append(1);
					distance = 0;
					break;
				case '月':// 特征值:2
					feature.append(distance).append(2);
					distance = 0;
					break;
				case '日':// 特征值:3
					feature.append(distance).append(3);
					distance = 0;
					break;
				case '时':// 特征值:4
					feature.append(distance).append(4);
					distance = 0;
					break;
				case '分':// 特征值:5
					feature.append(distance).append(5);
					distance = 0;
					break;
				case '秒':// 特征值:6
					feature.append(distance).append(6);
					distance = 0;
					break;
				case '-':// 特征值:7
					feature.append(distance).append(7);
					distance = 0;
					break;
				case ':':// 特征值:8
					feature.append(distance).append(8);
					distance = 0;
					break;
				case '/':// 特征值:9
					feature.append(distance).append(9);
					distance = 0;
					break;
				case 'T':// 特征值:10
					feature.append(distance).append(10);
					distance = 0;
					break;
				case '.':// 特征值:11
					feature.append(distance).append(10);
					distance = 0;
					break;
				case ' ':// 特征值:100
					feature.append(distance).append(100);
					distance = 0;
					break;
				default:
					distance++;
					if (c >= 48 && c <= 57) {
						distance++;
					}
					break;
			}
		}
		if (distance > 0) {
			feature.append(distance);
		}
		return feature.toString();
	}
	
	/**
	 * 当前日期格式化, 格式: yyyy-MM-dd HH:mm:ss
	 * 
	 * @return
	 */
	public final static String format() {
		return format(new Date(), FORMAT_NORMAL);
	}

	/**
	 * 当前日期格式化
	 * 
	 * @param format
	 *            格式
	 * @return
	 */
	public final static String format(String format) {
		return format(new Date(), format);
	}

	/**
	 * 日期格式化
	 * 
	 * @param date
	 *            日期
	 * @param format
	 *            格式
	 * @return
	 */
	public final static String format(Date date, String format) {
		try {
			SimpleDateFormat sf = new SimpleDateFormat(format == null ? FORMAT_NORMAL : format);
			return sf.format(date == null ? new Date() : date);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 比较两个日期的大小，判断是否到期
	 * @param strTime
	 * @param strExpiredTime
	 * @return
	 */
	public final static boolean isExpire(String strTime, String strExpiredTime) {
		Date time = parse(strTime, FORMAT_NORMAL);
		Date expiredTime = parse(strExpiredTime, FORMAT_NORMAL);
		return (time.compareTo(expiredTime) >= 0);
	}

	/**
	 * 生成制定日期的Date对象，从0点开始
	 * 
	 * @param year
	 * @param month
	 * @param days
	 * @return
	 */
	public final static Date createDate(int year, int month, int days) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month - 1, days, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	/**
	 * 计算时间差
	 * 
	 * @param beginTime 开始时间，格式：yyyy-MM-dd
	 * @param endTime   结束时间，格式：yyyy-MM-dd
	 * @return 从开始时间到结束时间之间的时间差（秒）
	 */
	public final static long getTimeDifference(String beginTime, String endTime) {
		long between = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date end = null;
		Date begin = null;
		try {// 将截取到的时间字符串转化为时间格式的字符串
			end = sdf.parse(endTime);
			begin = sdf.parse(beginTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		between = (end.getTime() - begin.getTime()) / 1000;// 除以1000是为了转换成秒
		return between;
	}

	public final static String getTimeDifference(Date beginTime, Date endTime) {
		if (beginTime == null || endTime == null) {
			return "未知";
		}
		long between = endTime.getTime() - beginTime.getTime();
		if (between <= 0) {
			return null;
		}
		between = between / 1000;// 除以1000是为了转换成秒
		long date = between / DATE_SECOND;
		long hour = (between - date * DATE_SECOND) / 3600;
		long minute = (between - date * DATE_SECOND - hour * 3600) / 60;
		long sec = (between - date * DATE_SECOND - hour * 3600 - minute * 60);
		return date + "天" + hour + "小时" + minute + "分钟" + sec + "秒";
	}

	public static String getLastDayOfMonth(int year, int month) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DATE));
		return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
	}

	public static String getFirstDayOfMonth(int year, int month) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DAY_OF_MONTH, cal.getMinimum(Calendar.DATE));
		return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
	}

	public static Integer[] getCurrentYearAndMonth() {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		return new Integer[] { year, month };
	}

	public static Date[] getCurrentMonthDate(int year, int month) {
		String firstDayOfMonth = TimeUtil.getFirstDayOfMonth(year, month);
		String lastDayOfMonth = TimeUtil.getLastDayOfMonth(year, month);
		Date startDate = TimeUtil.parse(firstDayOfMonth, TimeUtil.FORMAT_DATE_ONLY);
		Date endDate = TimeUtil.parse(lastDayOfMonth, TimeUtil.FORMAT_DATE_ONLY);
		return new Date[] { startDate, endDate };
	}

	public static Integer[] getPreviousMonthDate() {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, -1);
		int lastMonthMaxDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), lastMonthMaxDay, 23, 59, 59);
		return new Integer[] { c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1 };
	}

	public static void main(String[] args) {
		Date effectiveTime = null;
		Calendar c = Calendar.getInstance();
		c.set(2018, 2, 2);
		System.out.println(TimeUtil.format(c.getTime(), FORMAT_NORMAL));
		int today = c.get(Calendar.DAY_OF_MONTH);
		if (today == 1) {// 如果是第一天,取上个月的最后一天
			c.add(Calendar.MONTH, -1);
			int month = c.get(Calendar.MONTH);
			int year = c.get(Calendar.YEAR);
			String effectiveTimeStr = TimeUtil.getLastDayOfMonth(year, month);
			effectiveTime = TimeUtil.parse(effectiveTimeStr);
		} else {
			c.add(Calendar.DAY_OF_MONTH, -1);
			effectiveTime = c.getTime();
		}
		System.out.println(TimeUtil.format(effectiveTime, FORMAT_NORMAL));
	}
}

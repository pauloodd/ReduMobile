package br.com.redumobile.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateFormatter {
	private ThreadLocal<SimpleDateFormat> formatter;

	public DateFormatter(final String format) {
		formatter = new ThreadLocal<SimpleDateFormat>() {
			@Override
			protected SimpleDateFormat initialValue() {
				return new SimpleDateFormat(format, Locale.getDefault());
			}
		};
	}

	public String format(Date date) {
		return formatter.get().format(date);
	}

	public Date parse(String date) {
		Date parsedDate = null;
		try {
			parsedDate = formatter.get().parse(date);
		} catch (ParseException e) {
		}

		return parsedDate;
	}
}

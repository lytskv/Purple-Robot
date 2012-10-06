package edu.northwestern.cbits.purple_robot_manager.triggers;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateRange;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import edu.northwestern.cbits.purple_robot_manager.R;

public class DateTrigger extends Trigger
{
	public static final String TYPE_NAME = "datetime";

	private static final String DATETIME_START = "datetime_start";
	private static final String DATETIME_END = "datetime_end";
	private static final String DATETIME_REPEATS = "datetime_repeat";

	private String _start = null;
	private String _end = null;
	private String _repeats = null;
	private Calendar _calendar = null;

	public DateTrigger(Context context, JSONObject object) throws JSONException
	{
		super(context, object);

		this._start = object.getString(DateTrigger.DATETIME_START);
		this._end = object.getString(DateTrigger.DATETIME_END);
		this._repeats = object.getString(DateTrigger.DATETIME_REPEATS);

		if ("null".equals(this._repeats))
			this._repeats = null;

		String repeatString = "";

		if (this._repeats != null)
			repeatString = "\nRRULE:" + this._repeats;

		String icalString = String.format(context.getString(R.string.ical_template), this._start, this._end, this.name(), repeatString);

		StringReader sin = new StringReader(icalString);

		CalendarBuilder builder = new CalendarBuilder();

		try
		{
			this._calendar = builder.build(sin);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ParserException e)
		{
			e.printStackTrace();
		}
	}

	public boolean matches(Context context, Object obj)
	{
		if (obj instanceof Date)
		{
			if (this._calendar != null)
			{
				Date date = (Date) obj;

				// Create the date range which is desired.

				DateTime from = new DateTime(new Date(System.currentTimeMillis() - 5000));
				DateTime to = new DateTime(new Date(System.currentTimeMillis() + 5000));

				Period period = new Period(from, to);

				// For each VEVENT in the ICS
				for (Object o : this._calendar.getComponents("VEVENT"))
				{
					Component c = (Component) o;

					PeriodList list = c.calculateRecurrenceSet(period);

					for (Object po : list)
					{
						if (po instanceof Period)
						{
							Period p = (Period) po;

							DateRange range = new DateRange(p.getStart(), p.getEnd());

							if (range.includes(date, DateRange.INCLUSIVE_END | DateRange.INCLUSIVE_END))
								return true;
						}
					}
				}
			}
		}

		return false;
	}
}

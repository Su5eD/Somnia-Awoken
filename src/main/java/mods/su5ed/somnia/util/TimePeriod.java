package mods.su5ed.somnia.util;

public class TimePeriod
{
	public long start;
	public long end;
	
	public TimePeriod(long start, long end)
	{
		this.start = start;
		this.end = end;
	}

	@SuppressWarnings("unused")
	public boolean isTimeWithin(int time) {
		return this.isTimeWithin((long) time);
	}
	
	public boolean isTimeWithin(long time)
	{
		return (
				time >= start
				&&
				time <= end
				);
	}
}

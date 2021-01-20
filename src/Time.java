
public class Time {
	
	int year;
	int month;
	int day;
	int hour;
	int min;
	double sec;
	
	public Time(int y, int mo, int d, int h, int mi, double s) {
		year = y;
		month = mo;
		day = d;
		hour = h;
		min = mi;
		sec = s;
	}
	
	public Time(Time otherTime) {
		year = otherTime.year;
		month = otherTime.month;
		day = otherTime.day;
		hour = otherTime.hour;
		min = otherTime.min;
		sec = otherTime.sec;
	}

	public void tick(double seconds) {
		sec+=seconds;
		if(sec >= 60) {
			min++;
			sec = 0;
		}
		if(min == 60) {
			hour++;
			min = 0;
		}
		if(hour == 24) {
			day++;
			hour = 0;
		}
		
		if(month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
			if(day == 32) {
				month++;
				day = 1;
			}
		}else if(month == 2) {
			if(year % 4 == 0) {
				if(year % 100 != 0) {
					if(day == 30) {
						month++;
						day = 1;
					}
				}else if(year % 400 == 0) {
					if(day == 30) {
						month++;
						day = 1;
					}
				}else {
					if(day == 29) {
						month++;
						day = 1;
					}
				}
			}else {
				if(day == 29) {
					month++;
					day = 1;
				}
			}
		}else if(day == 31) {
			month++;
			day = 1;
		}
		
		if(month == 13) {
			year++;
			month = 1;
		}
	}
	
	public String toString() {
		return year + ", " + month + ", " + day + " " + hour + ":" + min + ":" + sec;
	}

	public double diff(Time t) {
		return (sec - t.sec) + 60*(min - t.min) + 3600*(hour - t.hour);
	}
	
}

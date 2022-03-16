package utility.datetime;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DatesUtil {

    //returns a List of UNIX UTC Strings, for the configured number of days that we want to get weather for.
    public List<String> retrievePastNDates(int numDays) {

        List<String> dates = new ArrayList<>();
        Instant originalInstant = Instant.now();

        for(int i = 0; i < numDays; i++) {

            long aDayLess = originalInstant.minus(i, ChronoUnit.DAYS).getEpochSecond();
            dates.add(Long.toString(aDayLess));
        }
        return dates;
    }
}

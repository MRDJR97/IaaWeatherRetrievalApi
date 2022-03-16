package service.negative;

import org.junit.Test;
import service.TemperatureService;

import java.math.BigDecimal;

//A small subset of the negative test cases
public class NegativeTemperatureServiceTest {

    String[] args;
    BigDecimal res;
    TemperatureService service;

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidIataCode1() throws Exception {
        args = new String[]{"invalidIataCode"};
        service = new TemperatureService(args);
            res = service.getHighestTemp();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidIataCode2() throws Exception {
        args = new String[]{"INVALID", "Celsius"};
        service = new TemperatureService(args);
        res = service.getHighestTemp();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooManyArgs() throws Exception {
        args = new String[]{"one", "two", "three"};
        service = new TemperatureService(args);
        res = service.getHighestTemp();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooFewArgs() throws Exception {
        args = new String[]{};
        service = new TemperatureService(args);
        res = service.getHighestTemp();
    }

}

package service.positive;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import service.TemperatureService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.fail;

//Using paramaterised test to keep file concise
//Due to limited time I just did e2e positive tests
//Lots of other facets I didnt test - eg mock responses should be utilised to ensure max temp is being correctly calculated
@RunWith(Parameterized.class)
public class ParamaterizedSuccessTemperatureServiceTest {

    String[] args;
    BigDecimal res;
    TemperatureService service;

    public ParamaterizedSuccessTemperatureServiceTest(String[] args) {
        this.args = args;
    }

    @Parameterized.Parameters(name = "Success Test {index}")
    public static Collection<Object> stringArrayProvider() {
        //These are all valid inputs
        return Arrays.asList(new Object[][] {
                { new String[]{"00AK"} },
                { new String[]{"BCJ", "InvalidTempFormat"} },
                { new String[]{"EDRI", "Celsius"} },
                { new String[]{"MYGW", "Fahrenheit"} },
                { new String[]{"OR83", "Kelvin"} }
        });
    }

    @Test
    public void testSuccess(){
        service = new TemperatureService(args);
        try {
            res = service.getHighestTemp();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Encountered exception");
        }
        Assert.assertNotEquals(res, new BigDecimal(-1));
    }

}

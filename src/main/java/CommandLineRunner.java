import service.TemperatureService;

public class CommandLineRunner
{

    public static void main(String[] args) throws Exception {
        System.out.println("Starting...");
        TemperatureService service = new TemperatureService(args);
        service.getHighestTemp();
    }

}
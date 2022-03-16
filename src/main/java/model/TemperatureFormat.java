package model;

//Enum class representing temperatures and the corresponding values that should be used in the URL "units" param
public enum TemperatureFormat {

    CELSIUS, FAHRENHEIT, KELVIN;

    public String getOutput () {
        switch(this) {
            case KELVIN:
                return "standard";
            case CELSIUS:
                return "metric";
            default:
                return "imperial";
        }
    }

}

import java.util.Map;

public class Faculty {
    private Map<String, String> name;
    private double lat;
    private double lng;
    private String description;
    private String image;

    public Faculty() {
        // Default constructor required for calls to DataSnapshot.getValue(Faculty.class)
    }

    public Faculty(Map<String, String> name, double lat, double lng, String description, String image) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.description = description;
        this.image = image;
    }

    public Map<String, String> getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }
}


import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONObject;
import org.apache.commons.csv.*;

import java.io.*;
import java.net.*;
import java.util.List;



public class CreateFromURI {
    JSONObject eventJson = new JSONObject();


    public CreateFromURI(String webcalURL) throws IOException {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
            //para aceitar os acentos
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (webcalURL.startsWith("webcal")) {
            webcalURL = "https" + webcalURL.substring(6);
        }

        List<VEvent> events = getClassroomFromLink(webcalURL);
        String filePathJson = "src/main/java/webCalendar.json";
        String filePathCsv = "src/main/java/webCalendar.csv";
        assert events != null;
        writeEventsToCSV(events, filePathCsv);
        writeEventsToJson(events, filePathJson);

    }
    public static JSONObject createJson(List<VEvent> classes){
        JSONObject json = new JSONObject();
        if (classes != null) {
            for (VEvent classroom : classes) {
                JSONObject jsons = new JSONObject();
                jsons.put("summary", classroom.getSummary().getValue());
                jsons.put("start", classroom.getDateStart().getValue());
                jsons.put("end", classroom.getDateEnd().getValue());
                jsons.put("location", classroom.getLocation().getValue());
                json.append("aulas", jsons);
            }
        }
        return json;
    }

    public static void writeEventsToJson(List<VEvent> events, String filePath){
        String end = createJson(events).toString(5);
        try (BufferedWriter file = new BufferedWriter(new FileWriter(filePath))) {
            file.write(end);
            file.newLine();
            file.flush();
            System.out.println("Successfully saved JSONObject to file!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeEventsToCSV(List<VEvent> events, String filePath) throws IOException {
        CSVPrinter csvPrinter = null;
        try(BufferedWriter file = new BufferedWriter(new FileWriter(filePath))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader("Summary", "Start Time", "End Time", "Location");
            csvPrinter = new CSVPrinter(file, csvFormat);
            for (VEvent event : events) {
                csvPrinter.printRecord(event.getSummary().getValue(),
                        event.getDateStart().getValue(),
                        event.getDateEnd().getValue(),
                        event.getLocation().getValue());
            }
            System.out.println("Successfully saved CsvFormat to file!");
        } finally {
            if (csvPrinter != null) {
                csvPrinter.close();
            }
        }
    }

    public static List<VEvent> getClassroomFromLink(String webcalUrl) {
        try {
            URL url = new URL(webcalUrl);

            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();

            ICalendar ical = Biweekly.parse(inputStream).first();
            return (ical != null) ? ical.getEvents() : null;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static void main(String[] args) throws JsonProcessingException, IOException{
        String uri = "webcal://fenix.iscte-iul.pt/publico/publicPersonICalendar.do?method=iCalendar&username=avsmm@iscte.pt&password=NaneDPf4lWconuF4UuHHptGH9ZfjfjD677VcFhnJTeltafE2JtWwYyvCf4zZ8AyMpIDoOouT2OZNISmi4EAjRfLhHeZwzPzs1BweVlggz8lAvCtNzdo4DfsZBHErCZwf";

        new CreateFromURI(uri);
    }

}

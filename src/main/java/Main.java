import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    public static final Charset encoding = StandardCharsets.UTF_8;

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        String dataJSONStr = "";
        String defJSONStr = "";
        Definition definition = new Definition();

        Map<String, Person> persons = new HashMap<>();
        Map<String, Double> firstResults = new HashMap<>();
        Map<String, Double> lastResults = new HashMap<>();


        String pathToDataFile = "D:\\0_Experiments\\MJakson\\data.json";
        String pathToDefinitionFile = "D:\\0_Experiments\\MJakson\\definition.json";
        String resultsPath = "D:\\0_Experiments\\MJakson\\results.csv";


        System.out.println("Please, enter path to JSON data file (with UTF-8 encoding): ");
        pathToDataFile = scanner.nextLine();

        System.out.println("Please, enter path to JSON report definition file (with UTF-8 encoding): ");
        pathToDefinitionFile = scanner.nextLine();
        resultsPath = pathToDataFile.substring(0, pathToDefinitionFile.lastIndexOf("\\") + 1) + "results.csv";

        try {
            dataJSONStr = readFile(pathToDataFile);
        } catch (IOException e) {
            System.out.println("Requested file can not be read!");
        }

        try {
            defJSONStr = readFile(pathToDefinitionFile);
        } catch (IOException e) {
            System.out.println("Requested file can not be read!");
        }


        persons = mapJSONDataToPerson(dataJSONStr);

        definition = mapJSONDefinitionToDefinition(defJSONStr);

        firstResults = calculatePersonsScore(persons, definition);

        lastResults = calculateLastResults(firstResults, definition, persons);

        writeToCSV(lastResults, resultsPath);

    }
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
    public static String readFile(String path) throws IOException {

        Scanner scanner = new Scanner(Paths.get(path), encoding.name());
        StringBuilder sb = new StringBuilder();

        while(scanner.hasNext()){
            sb.append(scanner.next());
        }
        scanner.close();

        return sb.toString();
    }
//----------------------------------------------------------------------------------------------------------------------
/*
If useExprienceMultiplier is set to true:
Score = totalSales/salesPeriod*experienceMultiplier
If useExprienceMultiplier is set to false:
Score = totalSales/salesPeriod
 */

    public static double calculateScore(Person person, Definition definition){

        if(definition.getUseExperienceMultiplier().equals("true")){
            return person.getTotalSales()/person.getSalesPeriod()*person.getExperienceMultiplier();
        } else {
            return person.getTotalSales()/ person.getSalesPeriod();
        }
    }
//----------------------------------------------------------------------------------------------------------------------
    /*
     have sales period that is equal or less than the periodLimit property;

    have score that is within the top X percent of the results, where X is defined by the
    topPerformersThreshold property of the report definition.
     */
    public static Map<String, Double> calculateLastResults(Map<String, Double> firstResults, Definition definition, Map<String, Person> persons){
        Map<String, Double> results = new HashMap<>();
        double threshold = calculatePerformanceThreshold(firstResults, definition);

        for (var result: firstResults.entrySet()) {
            if(result.getValue() > threshold && (definition.getPeriodLimit() >= persons.get(result.getKey()).getSalesPeriod())){
                results.put(result.getKey(), result.getValue());
            }
        }

        return results;
    }

//----------------------------------------------------------------------------------------------------------------------

    private static double calculatePerformanceThreshold(Map<String, Double> firstResults, Definition definition){
        double sumOfScores = 0;

        for (var firstResult:firstResults.values()) {
            sumOfScores = sumOfScores + (double)firstResult;
        }

        return sumOfScores/firstResults.size() - sumOfScores/firstResults.size() * (definition.getTopPerformersThreshold()/100);
    }
//----------------------------------------------------------------------------------------------------------------------

    public static void writeToCSV(Map<String, Double> lastResults, String resultsPath){

        StringBuilder sb = new StringBuilder();

        try (PrintWriter writer = new PrintWriter(new File(resultsPath))) {

            sb.append("Name , Score");
            sb.append(System.lineSeparator());

            for (var result:lastResults.entrySet()) {
                sb.append(result.getKey());
                sb.append(", ");
                sb.append(result.getValue());
                sb.append(System.lineSeparator());
            }

            writer.write(sb.toString());

            System.out.println("File with results was created.");

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }
//----------------------------------------------------------------------------------------------------------------------
     public static Map<String, Person> mapJSONDataToPerson (String dataString){
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Person> persons = new HashMap<>();
        List<Person> personsList = new ArrayList<>();


            try {
                personsList = Arrays.asList(objectMapper.readValue(dataString, Person[].class));
            } catch (IOException e) {
                System.out.println("Can not parse the JSON data!");
            }

        for (Person person : personsList) {
            persons.put(person.getName(), person);
        }

        return persons;
    }
//----------------------------------------------------------------------------------------------------------------------
    public static Definition mapJSONDefinitionToDefinition(String definitionString){
        ObjectMapper objectMapper = new ObjectMapper();
        Definition definition = new Definition();

        try {
            definition = objectMapper.readValue(definitionString, Definition.class);
        } catch (IOException e) {
            System.out.println("Can not parse the JSON definitions!");
        }


        return definition;
    }
//----------------------------------------------------------------------------------------------------------------------
    public static Map<String, Double> calculatePersonsScore(Map<String, Person> persons, Definition definition){
        Map<String, Double> firstResults = new HashMap<>();

        for (var person:persons.values()) {
            double score = 0;
            score = calculateScore(person, definition);
            firstResults.put(person.getName(), score);

        }

        return firstResults;
    }
//----------------------------------------------------------------------------------------------------------------------

}

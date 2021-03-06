import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;




public class Utils {

    private static final int MAX_FIPS_NUMBER = 72153;
    ArrayList<ElectionInfo> electionResults;
    ArrayList<EducationInfo> eduResults;
    ArrayList<EmploymentInfo> employmentResults;


    public Utils() {

    }

    public ArrayList<County> createCounties(){
        electionResults = parseElectionResults();
        eduResults = parseEducationInfo();
        employmentResults = parseEmploymentInfo();
        sortElectionResults();
        ArrayList<County> counties = new ArrayList<>();
        for (int i = 0; i < MAX_FIPS_NUMBER; i++) {
            ElectionInfo electionInfo = electionResults.get(i);
            County c = new County(electionInfo.getCounty(),i);//add catch for when the fips numbers of the education and employment info exceed the election data
             EducationInfo educationInfo = eduResults.get(i);
            EmploymentInfo employmentInfo = employmentResults.get(i);
            c.setEducationInfo(educationInfo);
            c.setElectionInfo(electionInfo);
            c.setEmploymentInfo(employmentInfo);
            counties.add(c);
        }
        return counties;
    }

    private void sortElectionResults() {
        for (int i = 0; i < electionResults.size(); i++) {

            int j = i;

            while(j>0&&electionResults.get(j-1).getCombinedFips()>electionResults.get(j).getCombinedFips()){
                swap(electionResults,j,j-1);
                j--;

            }

        }

    }

    public void swap(ArrayList<ElectionInfo> electionResults,int index1, int index2){
        ElectionInfo temp = electionResults.get(index1);
        electionResults.set(index1, electionResults.get(index2));
        electionResults.set(index2,temp);
    }

    public ArrayList<State> createStates(){
        ArrayList<State> states =  new ArrayList<>();
        ArrayList<County> counties = createCounties();

        for (int i = 0; i < counties.size(); i++) {
            State state = new State("Name");//change
            state.add(counties.get(i));
        }
        return states;
    }

    public void assignStatesToDataManager(){
        DataManager d = new DataManager();
        ArrayList<State> states = createStates();
        for (int i = 0; i < states.size(); i++) {
             d.addState(states.get(i));

        }
    }

    public ArrayList<ElectionInfo> parseElectionResults() {
        String data = readFileAsString("data/2016_Presidential_Results.csv");

        ArrayList<ElectionInfo> results = new ArrayList<>();

        String[] electionData = data.split("\n");

        for (int i = 1; i < electionData.length; i++) {

            String vals = formatData(electionData[i]);

            vals = removePercentage(vals);

            ElectionInfo e = createElectionResult(vals);
            results.add(e);

        }
        return results;
    }

    public ArrayList<EducationInfo> parseEducationInfo() {
        ArrayList<EducationInfo> totalEducationInfo = new ArrayList<>();
        String[] educationData = readFileAsString("data/Education.csv").split("\n");
        for (int i = 7; i < educationData.length; i++) {
            String vals = formatData(educationData[i]);

            EducationInfo e = createEducationInfo(vals);

            totalEducationInfo.add(e);

        }
        return totalEducationInfo;
    }

    public ArrayList<EmploymentInfo> parseEmploymentInfo() {
        ArrayList<EmploymentInfo> totalEmploymentResults = new ArrayList<>();
        String[] employmentData = readFileAsString("data/Unemployment.csv").split("\n");
        for (int i = 10; i < employmentData.length; i++) {
            String vals = formatData(employmentData[i]);
            EmploymentInfo e = createEmploymentInfo(vals);
            totalEmploymentResults.add(e);
        }
        return totalEmploymentResults;
    }

    private String formatData(String data) {
        String normalizedString = "";
        char[] chars = data.toCharArray();
        boolean inQuotes = false;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '"') {
                inQuotes = !inQuotes;
            } else if (!(inQuotes && chars[i] == ',')) {
                normalizedString += chars[i];
            }
        }
        return normalizedString;
    }

    public static String readFileAsString(String filepath) {
        StringBuilder output = new StringBuilder();

        try (Scanner scanner = new Scanner(new File(filepath))) {

            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                output.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return output.toString();
    }

    private ElectionInfo createElectionResult(String data) {
        String[] vals = data.split(",");
        int demVotes = (int) Double.parseDouble(vals[1]);
        int gopVotes = (int) Double.parseDouble(vals[2]);
        int totalVotes = (int) Double.parseDouble(vals[3]);

        String state = vals[8];
        String county = vals[9];
        int combinedFips = (int) Double.parseDouble(vals[10]);

        ElectionInfo electionResult = new ElectionInfo(demVotes, gopVotes, totalVotes,state,county,combinedFips);
        return electionResult;
    }

    private EducationInfo createEducationInfo(String data) {
        String[] vals = data.split(",");
        try {
            double noHighSchool = Double.parseDouble(vals[43]);
            double onlyHighSchool = Double.parseDouble(vals[44]);
            double someCollege = Double.parseDouble(vals[45]);
            double bachelorsOrMore = Double.parseDouble(vals[46]);
            return new EducationInfo(noHighSchool, onlyHighSchool, someCollege, bachelorsOrMore);
        }catch (Exception e){
            System.out.println("invalid Format");
            return new EducationInfo(-1,-1,-1,-1);
        }
    }

    private EmploymentInfo createEmploymentInfo(String data) {
        String[] vals = data.split(",");

        try {
            int totalLaborForce = Integer.parseInt(vals[42].trim());
            int employedLaborForce = Integer.parseInt(vals[43].trim());
            int unemployedLaborForce = Integer.parseInt(vals[44].trim());
            double unemployedPercent = Double.parseDouble(vals[45].trim());
            return new EmploymentInfo(totalLaborForce, employedLaborForce, unemployedLaborForce, unemployedPercent);
        }catch (Exception e){
            System.out.println("invalid format");

            return new EmploymentInfo(-1,-1,-1,-1);
        }

    }

    private String removePercentage(String data) {

        String dataWithoutPercentage = data.substring(0, data.indexOf("%")) +
                data.substring(data.indexOf("%") + 1);
        return dataWithoutPercentage;
    }


}



package org.cgiar.toucan;

import com.google.common.collect.Lists;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/*
A simple grid-based DSSAT simulation handler.
 */

public class App
{

    // System
    static long timeInitial = System.currentTimeMillis();
    static String OS = System.getProperty("os.name").toLowerCase();
    static int numberOfThreads = 4;
    static DecimalFormat dfSoilProfileID = new DecimalFormat("00000000");

    // Directory pointers
    static String d;
    static String dirSystem;
    static String dirWorking;
    static String dirWeather;
    static String dirInput;
    static String dirOutput;

    // What to simulate, where?
    static String fileMapSPAM = "spam2017V1r1_SSA_gr_YQ_TA.csv";
    static String mapSpamFilterCountryCode = "ETH";
    static String mapSpamColumnName = "maiz_a";
    static int mapSpamValueFilterMin = 1950;
    static int mapSpamValueFilterMax = 2000;

    // Main
    public static void main( String[] args ) throws InterruptedException {

        // OS-dependent
        System.out.println("> OS: "+OS.toUpperCase());
        if (isWindows())
        {
            d = "\\";
            dirWorking = "C:\\Sandbox\\2020-09_ToucanSimple\\";
            dirWeather = "C:\\Sandbox\\2020-09_ToucanSimple\\weather\\";
        }
        else if (isUnix())
        {
            d = "/";
            dirWorking = "/home/ec2-user/toucan/";
            dirWeather = "/home/ec2-user/toucan/weather/";
        }
        dirInput = dirWorking + "input" + d;
        dirOutput = dirWorking + "output" + d;
        dirSystem = dirWorking + "system" + d;

        // Preparing workspace
        File workspaceSource = new File(dirSystem);
        try
        {

            // Making copies of DSSAT files
            for (int t=0; t<numberOfThreads; t++)
            {
                String dirThread = dirWorking+"thread_"+t;
                File workspaceDestination = new File (dirThread);
                FileUtils.copyDirectory(workspaceSource, workspaceDestination);

                // Changing file permissions if Linux
                if (isUnix())
                {

                    // Permission 777
                    Set<PosixFilePermission> perms = new HashSet<>();
                    perms.add(PosixFilePermission.OWNER_READ);
                    perms.add(PosixFilePermission.OWNER_WRITE);
                    perms.add(PosixFilePermission.OWNER_EXECUTE);
                    perms.add(PosixFilePermission.GROUP_READ);
                    perms.add(PosixFilePermission.GROUP_WRITE);
                    perms.add(PosixFilePermission.GROUP_EXECUTE);
                    perms.add(PosixFilePermission.OTHERS_READ);
                    perms.add(PosixFilePermission.OTHERS_WRITE);
                    perms.add(PosixFilePermission.OTHERS_EXECUTE);

                    // Apply to the directory
                    Files.setPosixFilePermissions(Paths.get(dirThread), perms);

                    // Apply to all files
                    String[] workingFileNames = getTxFileNames(dirThread);
                    for (String workingFileName : workingFileNames)
                        Files.setPosixFilePermissions(Paths.get(workspaceDestination + d + workingFileName), perms);

                }

            }
            FileUtils.cleanDirectory(new File(dirOutput));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        // Get Unit Information
        List gridInfo = getGridInfo();
        int numberOfGrids = gridInfo.size();
        int numberOfGridsPerThread = (numberOfGrids / numberOfThreads) + 1;
        List gridInfoPartitions = Lists.partition(gridInfo, numberOfGridsPerThread);
        System.out.println("> Number of grids to simulate: "+numberOfGrids);
        System.out.println("> Number of threads: "+numberOfThreads);
        System.out.println("> Number of grids to simulate per thread: "+numberOfGridsPerThread);

        // Looping through units
        for (int i = 0; i < numberOfGridsPerThread; i++)
        {

            // Multithreading
            ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
            CompletionService completion = new ExecutorCompletionService(executor);

            // Distribute weather files over threads
            for (int t = 0; t < numberOfThreads; t++)
            {

                // Subset
                List<Object> gi = (List<Object>)gridInfoPartitions.get(t);
                if (!gi.isEmpty())
                {
                    try
                    {
                        if (i < gi.size())
                        {
                            Object[] o = (Object[]) gi.get(i);
                            System.out.println("> Thread "+t+", Grid "+(i+1)+"/"+numberOfGridsPerThread);
                            completion.submit(new Thread(o, t));
                        }
                        else
                        {
                            completion.submit(new Thread(null, t));
                        }
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }

            }
            for (int n = 0; n < numberOfThreads; n++)
            {
                completion.take();
            }
            executor.shutdown();

        } // Looping through units

        // Combine output files
        boolean firstFile = true;
        String[] outputFileNames = getCSVFileNames(dirOutput);

        // Write
        try
        {
            String combinedOutput = dirOutput+"combinedOutput_"+mapSpamFilterCountryCode+".csv";
            BufferedWriter writer = new BufferedWriter(new FileWriter(combinedOutput));

            // Looping through the files
            for (String outputFileName: outputFileNames)
            {
                BufferedReader reader = new BufferedReader(new FileReader(dirOutput+outputFileName));

                // To skip the header from the second file
                if (firstFile)
                    firstFile = false;
                else
                    reader.readLine();

                // Reader --> Writer
                String str = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                writer.append(str);

            }
            writer.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        // How long did it take?
        long runningTime = (System.currentTimeMillis() - timeInitial)/(long)1000;
        String rt = String.format("%1$02d:%2$02d:%3$02d", runningTime / (60*60), (runningTime / 60) % 60, runningTime % 60);
        System.out.println("> Done ("+rt+")");
    }

    // List of grid cell IDs to run from MapSPAM
    public static List getGridInfo()
    {
        List gridInfo = Lists.newArrayList();
        try
        {
            Reader in = new FileReader(dirInput + fileMapSPAM);
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
            for (CSVRecord record : records)
            {
                Object[] o = new Object[6];
                String iso3 = record.get("iso3");  // Country code
                int value = (int)Double.parseDouble(record.get(mapSpamColumnName));  // Maize yield from MapSPAM
                if (String.valueOf(iso3).equals(mapSpamFilterCountryCode)
                        && value>mapSpamValueFilterMin
                        && value<mapSpamValueFilterMax)
                {
                    int cell5m = Integer.parseInt(record.get("cell5m"));
                    o[0] = cell5m;
                    o[1] = mapSpamFilterCountryCode.substring(0,2)+dfSoilProfileID.format(cell5m);
                    gridInfo.add(o);
                }
            }
        }
        catch (Exception e) { e.printStackTrace(); }
        return gridInfo;
    }

    // File names
    static String[] getCSVFileNames(String filePath)
    {
        File dir = new File(filePath);
        FilenameFilter filter = (dir1, name) -> (name.toUpperCase().contains("CSV"));
        return dir.list(filter);
    }

    // Get list of file names
    static String[] getTxFileNames(String filePath)
    {
        File dir = new File(filePath);
        return dir.list();
    }

    // OS detection
    public static boolean isWindows()
    {
        return (OS.contains("win"));
    }
    public static boolean isUnix()
    {
        return (OS.contains("nix") || OS.contains("nux") || OS.indexOf("aix") > 0 );
    }

}

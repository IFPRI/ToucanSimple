package org.cgiar.toucan;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.concurrent.Callable;

public class Thread implements Callable<Integer>
{

    int exitCode = 0;
    Object[] o;
    int threadID;

    Thread(Object[] o, int threadID)
    {
        this.o = o;
        this.threadID = threadID;
    }

    @Override
    public Integer call()
    {

        // Only if there's unit information to simulate
        if (o!=null)
        {

            // Modeling unit information
            int cell5m = (int)o[0];
            String weatherFileName = cell5m + ".wth";

            // Copy weather file
            try
            {
                File weatherSource = new File(App.dirWeather+weatherFileName);
                File weatherDestination = new File(App.dirWorking+"thread_"+threadID+App.d+"WEATHERS.WTG");
                FileUtils.copyFile(weatherSource, weatherDestination);
            }
            catch (FileNotFoundException n)
            {
                System.out.println("> Weather file not found: "+weatherFileName);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            // Write SNX file
            try
            {
                SnxWriter.snxWriter(threadID, o);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            // Run it
            exitCode = ExeRunner.dscsm047(threadID);

            // Copy output file
            try
            {
                File outputSource = new File(App.dirWorking+"thread_"+threadID+App.d+"summary.csv");
                File outputDestination = new File(App.dirOutput+App.d+cell5m+".csv");
                FileUtils.copyFile(outputSource, outputDestination);
            }
            catch (FileNotFoundException n)
            {
                System.out.println("> Output file not found at T"+threadID);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }

        // Return
        return exitCode;

    }

}

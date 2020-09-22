package org.cgiar.toucan;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class ExeRunner
{

    // Run DSSAT
    public static int dscsm047(int threadID)
    {

        int exitCode = 0;
        try
        {

            // Execution using ProcessBuilder
            ProcessBuilder pb = new ProcessBuilder();
            pb.directory(new File(App.dirWorking+"thread_"+threadID));

            // OS dependent
            if (App.isWindows())
                pb.command("CMD.EXE", "/C", "DSCSM047.EXE N DSSBatch.v47 >NUL");
            else if (App.isUnix())
                pb.command("bash", "-c", "./DSCSM047.EXE N DSSBatch.v47 >/dev/null");

            Process p = pb.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ( (line = r.readLine()) != null)
            {
                System.out.println(line);
            }

            // Error?
            exitCode = p.waitFor();
            if (exitCode>0)
                System.out.println("> Thread "+threadID+", exited with error code: " + exitCode);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return exitCode;

    }

}

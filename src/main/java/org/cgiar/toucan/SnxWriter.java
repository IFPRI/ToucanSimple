package org.cgiar.toucan;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DecimalFormat;

public class SnxWriter
{

    static DecimalFormat dfTT  = new DecimalFormat("00");

    public static void snxWriter(int threadID, Object[] o)
    {

        // Unit information
        String soilProfileID = (String)o[1];

        // Treatments
        String snxSectionTreatments = "\n" +
                "*TREATMENTS                        -------------FACTOR LEVELS------------\n" +
                "@N R O C TNAME.................... CU FL SA IC MP MI MF MR MC MT ME MH SM\n" +
                "01 1 0 0 MZ                         1  1  0  1 01  0  0  0  0  0  0  0  1\n";

        // Cultivar
        String snxSectionCultivars = "\n" +
                "*CULTIVARS\n" +
                "@C CR INGENO CNAME\n" +
                " 1 MZ ETH001 MELKASS-1_OPV\n";

        // Fields
        String snxSectionFields = "\n*FIELDS\n" +
                "@L ID_FIELD WSTA....  FLSA  FLOB  FLDT  FLDD  FLDS  FLST SLTX  SLDP  ID_SOIL    FLNAME\n" +
                " 1 TOUCAN01 WEATHERS   -99     0 IB000     0     0 00000 -99    180  "+soilProfileID+" -99\n" +
                "@L ...........XCRD ...........YCRD .....ELEV .............AREA .SLEN .FLWR .SLAS FLHST FHDUR\n" +
                " 1               0               0         0                 0     0     0     0   -99   -99\n";

        // Initial Conditions
        String snxSectionInitialConditions = "\n*INITIAL CONDITIONS\n" +
                "@C   PCR ICDAT  ICRT  ICND  ICRN  ICRE  ICWD ICRES ICREN ICREP ICRIP ICRID ICNAME\n" +
                " 1    MZ 19091   100     0     1     1   180  1000    .8     0   100    15 -99\n" +
                "@C  ICBL  SH2O  SNH4  SNO3\n" +
                " 1     5  .001    .6   1.5\n" +
                " 1    15  .001    .6   1.5\n" +
                " 1    30  .001    .6   1.5\n" +
                " 1    45  .001    .6   1.5\n" +
                " 1    60  .001    .6   1.5\n" +
                " 1    90  .001    .6    .6\n" +
                " 1   100  .001    .6    .5\n";

        // Planting Details
        String snxSectionPlantingDetails = "\n*PLANTING DETAILS\n" +
                "@P PDATE EDATE  PPOP  PPOE  PLME  PLDS  PLRS  PLRD  PLDP  PLWT  PAGE  PENV  PLPH  SPRL                        PLNAME\n" +
                "01 19091   -99   3.0   3.0     S     R    61     0     7   -99   -99   -99   -99     0                        -99\n";

        // Simulation controls
        String snxSectionSimulationControls = "\n*SIMULATION CONTROLS\n" +
                "@N GENERAL     NYERS NREPS START SDATE RSEED SNAME.................... SMODEL\n" +
                " 1 GE              2     1     S 19001  1234 TOUCAN SIMPLE\n" +
                "@N OPTIONS     WATER NITRO SYMBI PHOSP POTAS DISES  CHEM  TILL   CO2\n" +
                " 1 OP              Y     Y     N     N     N     N     N     N     D\n" +
                "@N METHODS     WTHER INCON LIGHT EVAPO INFIL PHOTO HYDRO NSWIT MESOM MESEV MESOL\n" +
                " 1 ME              G     M     E     R     S     C     R     1     G     S     2\n" +
                "@N MANAGEMENT  PLANT IRRIG FERTI RESID HARVS\n" +
                " 1 MA              R     N     N     N     M\n" +
                "@N OUTPUTS     FNAME OVVEW SUMRY FROPT GROUT CAOUT WAOUT NIOUT MIOUT DIOUT VBOSE CHOUT OPOUT FMOPT\n" +
                " 1 OU              N     N     Y     3     N     N     N     N     N     N     0     N     N     C\n" +
                "\n" +
                "@  AUTOMATIC MANAGEMENT\n" +
                "@N PLANTING    PFRST PLAST PH2OL PH2OU PH2OD PSTMX PSTMN\n" +
                " 1 PL          19001 19365    40   100    30    40    10\n" +
                "@N IRRIGATION  IMDEP ITHRL ITHRU IROFF IMETH IRAMT IREFF\n" +
                " 1 IR             30    70   100 IB001 IB001    20   .75\n" +
                "@N NITROGEN    NMDEP NMTHR NAMNT NCODE NAOFF\n" +
                " 1 NI             30    50    25 IB001 IB001\n" +
                "@N RESIDUES    RIPCN RTIME RIDEP\n" +
                " 1 RE            100     1    20\n" +
                "@N HARVEST     HFRST HLAST HPCNP HPCNR\n" +
                " 1 HA          19001 19365   100     0";

        // SNX
        String snx = "*EXP.DETAILS: TOUCAN"+dfTT.format(threadID)+"SN\n" +
                "\n" +
                "*GENERAL\n" +
                "\n" +
                snxSectionTreatments +
                snxSectionCultivars +
                snxSectionFields +
                snxSectionInitialConditions +
                snxSectionPlantingDetails +
                snxSectionSimulationControls;

        // Write
        try
        {
            String snxFile = App.dirWorking+"thread_"+threadID+App.d+"TOUCAN"+dfTT.format(threadID)+".SNX";
            BufferedWriter writer = new BufferedWriter(new FileWriter(snxFile));
            writer.write(snx);
            writer.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        // Batch file
        StringBuilder batch = new StringBuilder(
                "$BATCH(SEASONAL)\n" +
                "\n" +
                "@FILEX                                                                                        TRTNO     RP     SQ     OP     CO\n");
        batch.append("TOUCAN").append(dfTT.format(threadID)).append(".SNX                                                                                     01      1      0      0      0\n");

        // Write
        try
        {
            String batchFile = App.dirWorking+"thread_"+threadID+App.d+"DSSBatch.v47";
            BufferedWriter writer = new BufferedWriter(new FileWriter(batchFile));
            writer.write(batch.toString());
            writer.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

}

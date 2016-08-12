package scheduler;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Options
{    
    @Option(name="-s", usage="SMTP host")
    public String SmtpHost;
    
    @Option(name="-p", usage="SMTP port")
    public int SmtpPort;
    
    @Option(name="-u", usage="SMTP user name")
    public String SmtpUser;
    
    @Option(name="-p", usage="SMTP user password")
    public String SmtpPassword;
    
    @Option(name="-f", usage="SMTP from address")
    public String FromAddress;
    
    @Option(name="-t", usage="SMTP to address")
    public String ToAddress;
    
    @Option(name="-d", usage="Destination connection string")
    public String Destination;
    
    @Option(name="-c", usage="Update ranking cron")
    public String UpdateRankingCron = "0 0/60 * * * ?";

    public static Options GetCurrent() {
        return current;
    }
    
    private Options(){};
    
    static boolean Init(String[] args) {
        Options settings = new Options();
        CmdLineParser parser = new CmdLineParser(settings);
        try {
            parser.parseArgument(args);
        }
        catch(CmdLineException e) {
            logger.error("Unable to parse arguments: " + e.getMessage());
            parser.printUsage(System.out);
            return false;
        }
        
        current = settings;
        return true;
    }
    
    private static Options current;
    private static final Logger logger = LoggerFactory.getLogger(Options.class);
}

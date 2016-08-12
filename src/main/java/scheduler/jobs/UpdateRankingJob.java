package scheduler.jobs;

import scheduler.Options;
import scheduler.Utils;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.stream.Stream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.quartz.*;
import org.slf4j.*;

public class UpdateRankingJob implements Job
{    
    enum Sex {
        MAN (1),
        WOMAN (0);
        
        private final int value;
        
        Sex(int value) {
            this.value = value;
        }
    }
    
    public UpdateRankingJob() throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        // Load the Connector/J driver
        Class.forName("com.mysql.jdbc.Driver").newInstance();
    }
    
    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException
    {
        logger.info("Update Ranking started");
        
        try
        {    
            PlayerInfo[] players = ReadSource();
            
            String hash = UpdateLocal(players);
           
            if (!hash.equals(lastHash))
            {
                UpdateDestination(players);
                Notifier.SendPlayersChanged(players);
                lastHash = hash;
            }
            
        } catch (Exception ex) {
            logger.error("Failed to update ranking: " + ex.getMessage());
        }
    }
    
    private static PlayerInfo[] ReadSource() throws IOException
    {
        int timeout = 15 * 1000; // 15 ms
        Document menDoc = Jsoup.connect(SITE_URL).timeout(timeout).get();
        Document womenDoc = Jsoup.connect(menDoc.location() + "?&male=2").timeout(timeout).get();
        
        Stream<PlayerInfo> men = ParsePlayers(menDoc, Sex.MAN);
        Stream<PlayerInfo> women = ParsePlayers(womenDoc, Sex.WOMAN);
        
        return Stream.concat(men, women).toArray(PlayerInfo[]::new);
    }
    
    private static Stream<PlayerInfo> ParsePlayers(Document document, Sex sex)
    {
        return document.select("#sortTable tbody tr").stream().limit(10).map(p -> ParsePlayer(p, sex));
    }
    
    private static PlayerInfo ParsePlayer(Element p, Sex sex)
    {
        Elements columns = p.select("td");
        
        PlayerInfo player = new PlayerInfo();
        player.sex = sex.value;
        player.position = Integer.parseInt(columns.get(0).ownText().replace(" ", ""));
        player.name = columns.get(1).text();
        player.url = SITE_URL + columns.get(1).select("a").get(0).attr("href");
        player.ranking1 = new Double(columns.get(2).text().replace(',', '.'));
        player.ranking2 = new Double(columns.get(3).text().replace(',', '.'));
        
        return player;
    }
    
    private String UpdateLocal(PlayerInfo[] players) throws FileNotFoundException, IOException, NoSuchAlgorithmException
    {    
        String fileName = "players.dat";
        
        try (FileOutputStream fileOut = new FileOutputStream(fileName);
             ObjectOutputStream out = new ObjectOutputStream(fileOut))
        {
            out.writeObject(players);
        }
        
        return Utils.getFileChecksum(fileName);
    }
    
    private static void UpdateDestination(PlayerInfo[] players) throws SQLException
    {
        Options options = Options.GetCurrent();
        Connection connection = DriverManager.getConnection(options.Destination);
        
        String query = "DELETE FROM uttf_ranking;";
        for (PlayerInfo player : players)
        {
            query += String.format(
                    "INSERT INTO uttf_ranking (`sex`, `name`, `position`, `value`, `url`) "
                         + "VALUES ('%d', '%s', '%d', '%f', '%s');",
                    player.sex, player.name, player.position, player.ranking1, player.url);
        }
        
        Statement statement = connection.createStatement();
        statement.executeUpdate(query);
    }
    
    private static String lastHash;
    private static final String SITE_URL = "http://reiting.com.ua";
    private Logger logger = LoggerFactory.getLogger(UpdateRankingJob.class);
}

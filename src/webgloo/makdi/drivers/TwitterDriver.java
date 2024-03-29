package webgloo.makdi.drivers;

import java.util.ArrayList;
import java.util.List;
import twitter4j.Query;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import webgloo.makdi.data.IData;
import webgloo.makdi.data.VanillaList;
import webgloo.makdi.logging.MyTrace;

/**
 *
 * @author rajeevj
 *
 */
public class TwitterDriver implements IDriver {
    
    private int maxResults;
    private Transformer transformer;

    public TwitterDriver(int maxResults) {
        this.maxResults = maxResults;
        this.transformer = new Transformer();
    }

    public TwitterDriver(Transformer transformer, int maxResults) {
        this.maxResults = maxResults;
        this.transformer = transformer;
    }

    @Override
    public String getName() {
        return IDriver.TWITTER_DRIVER;
    }


    @Override
    public long getDelay() {
        //only 150 requests /hour allowed
        return 20000 ;
    }

    @Override
    public List<IData> run(String tag) throws Exception {
        MyTrace.entry("TwitterDriver", "run()");

        tag = this.transformer.transform(tag);
        
        //use twitter4J library
        Twitter client = new TwitterFactory().getInstance();

        //wrap in quotes
        Query q = new Query("\"" + tag + "\"");
        q.setRpp(this.maxResults);
        MyTrace.debug("sending twitter request :: " + q.getQuery());
        
        List<Tweet> tweets = new ArrayList<Tweet>();
        
        try {
            tweets = client.search(q).getTweets();

        } catch (Exception ex) {
            MyTrace.error("Error in twitter client ..",ex);
            
        }

        
        VanillaList list = new VanillaList("tweets for "+ tag);

        //collect data from all feeds and populate one list
        for (Tweet tweet : tweets) {
            //create a string item
            String data = tweet.getText();
            if (tweet.getLocation() != null) {
                data = data + " (" + tweet.getLocation() + ") ";

            }

            //System.out.println(data);
            list.add(data);

        }

        //Add list to return items
        List<IData> items = new ArrayList<IData>();
        items.add(list);
        
        MyTrace.exit("TwitterDriver", "run()");
        return items;

    }

    public static void main(String[] args) throws Exception {

        //TwitterDriver driver = new TwitterDriver(new NullTransformer(),10);
        TwitterDriver driver = new TwitterDriver(new Transformer("watched", null), 10);
        String tag = "despicable me";
        List<IData> items = driver.run(tag);
        for (IData item : items) {
            MyTrace.debug(item.toHtml());
        }

    }
}

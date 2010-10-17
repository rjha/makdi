package webgloo.makdi.drivers;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import webgloo.makdi.data.IData;
import webgloo.makdi.data.Post;
import webgloo.makdi.util.HtmlToText;


import webgloo.makdi.util.MyWriter;
import webgloo.makdi.io.URLReader;

/**
 *
 * @author rajeevj
 *
 */
public class WikipediaDriver implements IDriver {

    public static final String WIKIPEDIA_PRINT_URI = "http://en.wikipedia.org/w/index.php?printable=yes&title=";
    public static final int REQUEST_DELAY = 2000 ;
    
    private Transformer transformer ;

    public WikipediaDriver(Transformer transformer) {
       this.transformer = transformer ;
    }
    
    @Override
    public String getName() {
        return IDriver.WIKIPEDIA_DRIVER;
    }

    @Override
    public List<IData> run(String tag) throws Exception {

        //Transformer is for YAHOO BOSS driver
        //YAHOO BOSS will do its own encoding 
        YahooBossDriver boss = new YahooBossDriver(this.transformer,new String[]{"wikipedia.org"},4);
        List<IData> results = boss.run(tag);

        
        Post result = null;
        if (results.size() > 0) {
            result = (Post) results.get(0);

        }
        
        List<IData> items = new ArrayList<IData>();
        
        //No search result case
        if (result != null) {
            //get the original URL
            String originalUrl = result.getLink();
            //create printable url from original Url
            String token = getWikipediaToken(originalUrl);
            //URLEncode the token
            token = java.net.URLEncoder.encode(token,"UTF-8");
            String address =  WikipediaDriver.WIKIPEDIA_PRINT_URI + token;
            
            //Now fetch content from printUrl
            MyWriter.toConsole("sending request to :: " + address);

            String htmlResponse = URLReader.read(address);
            StringReader reader = new StringReader(htmlResponse);
            HtmlToText parser = new HtmlToText();
            parser.parse(reader);
            reader.close();
            String response = parser.getText();

            //Below method is not very efficient at stripping
            //String response = com.google.gdata.util.common.html.HtmlToText.htmlToPlainText(htmlResponse);

            //prune the response please!
            response = StringUtils.abbreviate(response,1000,2000);
            
            //create a new post
            Post wikipost = new Post();
            wikipost.setTitle(result.getTitle());
            wikipost.setDescription(response);
            wikipost.setLink(originalUrl);

            items.add(wikipost);
            
        }
        
        Thread.sleep(REQUEST_DELAY);
        
        return items;

    }

    private static String getWikipediaToken(String originalUrl) throws Exception {
        //get the token
        int pos = originalUrl.lastIndexOf('/');
        if (pos == -1) {
            throw new Exception("Invalid wikipedia original Url");
        }
        String token = originalUrl.substring(pos + 1);
        return  token;

    }

    public static void main(String[] args) throws Exception {
        WikipediaDriver driver = new WikipediaDriver(new Transformer());
        String tag = "a clockwork orange";
        List<IData> items = driver.run(tag);
        for(IData item : items) {
            System.out.println(item.toHtml());
        }

    }
}

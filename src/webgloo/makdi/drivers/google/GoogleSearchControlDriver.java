package webgloo.makdi.drivers.google;

import java.util.ArrayList;
import java.util.List;
import webgloo.makdi.data.GoogleSearchControl;
import webgloo.makdi.data.IData;
import webgloo.makdi.drivers.IDriver;
import webgloo.makdi.drivers.Transformer;
import webgloo.makdi.logging.MyTrace;

/**
 *
 * @author rajeevj
 *
 */
public class GoogleSearchControlDriver implements IDriver {

    
    private Transformer transformer ;

    public GoogleSearchControlDriver(Transformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public String getName() {
       return IDriver.GOOGLE_AJAX_CONTROL_DRIVER ;
    }


    @Override
    public long getDelay() {
        return 1000 ;
    }

    @Override
    public List<IData> run(String tag) throws Exception {
        MyTrace.entry("GoogleSearchControlDriver", "run()");
        tag = this.transformer.transform(tag);
        //Urlencode the tag
        //tag = java.net.URLEncoder.encode(tag, "UTF-8");

        List<IData> items = new ArrayList<IData>();

        MyTrace.debug("Google Cse :: keyword :: " + tag);
        items.add(new GoogleSearchControl(tag));
        
        MyTrace.exit("GoogleSearchControlDriver", "run()");

        return items;

    }
  
}

package webgloo.makdi.plugin;

import java.util.List;
import org.apache.commons.lang.WordUtils;
import webgloo.makdi.data.Keyword;
import webgloo.makdi.db.GlooDBConnection;
import webgloo.makdi.db.GlooDBManager;

import webgloo.makdi.logging.MyTrace;
import webgloo.makdi.util.MyUtils;

/**
 *
 * @author rajeevj
 */
public abstract class AutoPostPlugin extends BasePlugin {

    private boolean isSummaryInContent = false;

    public boolean isIsSummaryInContent() {
        return isSummaryInContent;
    }
    
    public void setIsSummaryInContent(boolean isSummaryInContent) {
        this.isSummaryInContent = isSummaryInContent;
    }
    
    public abstract List<Keyword> loadNewKeywords() throws Exception;

    public abstract boolean getPageSummary(
            IPlugin profileBean,
            String token,
            StringBuilder title,
            StringBuilder summary) throws Exception;

    public void storeContent(IPlugin pluginBean) throws Exception {

        MyTrace.entry("AutoPostPlugin", "store(plugin bean)");
        java.sql.Connection connection = GlooDBConnection.getConnection();

        //load any new keywords
        List<Keyword> newKeywords = this.loadNewKeywords();

        if (newKeywords != null && newKeywords.size() > 0) {

            for (Keyword keyword : newKeywords) {

                //artificial createdOn values
                String createdOn = keyword.getDate() + " " + MyUtils.now();
                
                GlooDBManager.addAutoPostKeyword(
                        connection,
                        pluginBean.getSiteGuid(),
                        keyword.getToken(),
                        createdOn);

                //wait 1 second before pushing in new keyword
                // This will esnure that keywords are spaced right
                Thread.sleep(1000);

            }

        }


        //Now load back unprocessed keys from database
        List<Keyword> unprocessedKeywords = GlooDBManager.getUnprocessedAutoKeywords(connection, pluginBean.getSiteGuid());
        MyTrace.info("total keywords to process :: " + unprocessedKeywords.size());

        String typeOfWidget = "AUTO_POST";
        String widgetXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <widget> <type>AUTO_POST</type></widget>";
        
        for (Keyword keyword : unprocessedKeywords) {
            MyTrace.info("\n process keyword :: " + keyword.getToken());
            String token = WordUtils.capitalizeFully(keyword.getToken());

            //buffers to hold data
            StringBuilder content = new StringBuilder();
            StringBuilder title = new StringBuilder();
            StringBuilder summary = new StringBuilder();


            if (this.getPageSummary(pluginBean, token, title, summary)) {
                //Add existing summary to  content
                // depending on flag
                if(this.isSummaryInContent) {
                    content.append(summary.toString());
                }
                
                this.getPageContent(pluginBean,token, content);

                //1. create a new gloo_page
                String pageIdentKey = MyUtils.getUUID();
                String pageName = keyword.getToken();
                //mySQL timestamps are in form 2010-10-22 21:14:45
                
                GlooDBManager.addPage(
                        connection,
                        pluginBean.getSiteGuid(),
                        pageIdentKey,
                        pageName);

                //2. store content in newly created page
                GlooDBManager.addPageContent(connection,
                        pluginBean.getSiteGuid(),
                        pageIdentKey,
                        typeOfWidget,
                        widgetXml,
                        title,
                        content);

                //3. store summary in gloo_auto_post table
                // get right date string
                // Trigger will take care of updating
                // gloo_auto_keyword is_processed flag

                GlooDBManager.addAutoPost(connection,
                        pluginBean.getSiteGuid(),
                        pageName,
                        title,
                        summary);


                MyTrace.info("\n stored info for keyword :: " + keyword.getToken());

            } else {
                MyTrace.info("\n No summary for :: " + token);
                //set is_processed flag to 2
                GlooDBManager.updateAutoKeyword(
                        connection,
                        pluginBean.getSiteGuid(),
                        keyword,
                        2);
            }

            Thread.sleep(1000);

        }//loop: keywords

        connection.close();
        MyTrace.exit("AutoPostPlugin", "store(plugin bean)");

    }
}

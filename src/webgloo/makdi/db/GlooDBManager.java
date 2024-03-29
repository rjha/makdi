package webgloo.makdi.db;

import java.sql.CallableStatement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import webgloo.makdi.data.Keyword;
import webgloo.makdi.logging.MyTrace;
import webgloo.makdi.util.MyUtils;

/**
 *
 * @author rajeevj
 *
 */
public class GlooDBManager {

    //@todo - check if content is more than what can fit into column
    public static void addAutoPostKeyword(java.sql.Connection connection,
            String orgId,
            String token,
            String createdOn) throws Exception {

        String pageSeoKey = MyUtils.convertPageNameToId(token);

        CallableStatement cs = connection.prepareCall("{ call addAutoPostKeyword(?,?,?,?) }");
        cs.setString(1, orgId);
        cs.setString(2, token);
        cs.setString(3, pageSeoKey);
        cs.setString(4, createdOn);

        cs.executeUpdate();
        cs.close();

    }

    public static List<Keyword> getUnprocessedAutoKeywords(
            java.sql.Connection connection,
            String orgId) throws Exception {

        MyTrace.entry("GlooDBManager", "getUnprocessedAutoKeywords()");
        //store keyword is not there already
        List<Keyword> keywords = new ArrayList<Keyword>();

        String SQL =
                " select token,seo_key,created_on from gloo_auto_keyword where org_id = "
                + orgId + " and is_processed = 0 ";

        java.sql.Statement stmt = connection.createStatement();
        java.sql.ResultSet rs = stmt.executeQuery(SQL);

        while (rs.next()) {
            Keyword keyword = new Keyword();
            keyword.setToken(rs.getString("token"));
            keyword.setCreatedOn(rs.getString("created_on"));
            keyword.setSeoKey(rs.getString("seo_key"));

            keywords.add(keyword);

        }

        stmt.close();
        rs.close();

        MyTrace.exit("GlooDBManager", "getUnprocessedAutoKeywords()");

        return keywords;

    }

    public static boolean isExistingPage(java.sql.Connection connection,
            String orgId,
            String pageIdentKey) throws Exception {


        String SQL =
                " select count(id) as count from gloo_page where org_id = "
                + orgId + " and ident_key = '" + pageIdentKey + "' ";

        java.sql.Statement stmt = connection.createStatement();
        java.sql.ResultSet rs = stmt.executeQuery(SQL);
        int count = 0;

        while (rs.next()) {
            count = rs.getInt("count");
        }

        stmt.close();
        rs.close();

        boolean flag = (count > 0) ? true : false;
        return flag;

    }

    public static void addAutoPost(java.sql.Connection connection,
            String orgId,
            String pageName,
            StringBuilder title,
            StringBuilder summaryInHtml) throws Exception {


        String GLOO_AUTO_POST_INSERT_SQL =
                "INSERT  INTO gloo_auto_post(org_id,ident_key,seo_key,title,content,created_on,updated_on)"
                + " VALUES(?,?,?,?,?,now(),now()) ";

        String identKey = MyUtils.getUUID();
        java.sql.PreparedStatement pstmt = connection.prepareStatement(GLOO_AUTO_POST_INSERT_SQL);

        pstmt.setString(1, orgId);
        pstmt.setString(2, identKey);
        pstmt.setString(3, MyUtils.convertPageNameToId(pageName));

        pstmt.setString(4, title.toString());
        pstmt.setString(5, summaryInHtml.toString());

        pstmt.executeUpdate();
        pstmt.close();

    }

    public static void addPageContentToBlock(java.sql.Connection connection,
            String orgId,
            String pageIdentKey,
            String typeOfWidget,
            String widgetXml,
            StringBuilder title,
            StringBuilder content) throws Exception {


        String identKey = MyUtils.getUUID();

        //Magic block of Type II
        int typeofBlock = 2;
        int blockNumber = 100;

        String GLOO_BLOCK_INSERT_SQL =
                " INSERT  INTO gloo_block_data(ident_key,org_id,page_key, "
                + " widget_type, title,widget_xml,widget_html, "
                + " created_on,block_no,block_type) "
                + " VALUES(?,?,?,?,?,?,?,now(),?,?) ";

        
            java.sql.PreparedStatement pstmt = connection.prepareStatement(GLOO_BLOCK_INSERT_SQL);
            pstmt.setString(1, identKey);
            pstmt.setString(2, orgId);
            pstmt.setString(3, pageIdentKey);
            pstmt.setString(4, typeOfWidget);

            pstmt.setString(5, title.toString());
            pstmt.setString(6, widgetXml);
            pstmt.setString(7, content.toString());

            pstmt.setInt(8, blockNumber);
            pstmt.setInt(9, typeofBlock);


            pstmt.executeUpdate();
            pstmt.close();
            
        

    }

    /**
     *
     * @param pageName - human readable page name like "The mystery continues"
     * @throws Exception
     */
    public static void addPageMetaData(java.sql.Connection connection,
            String orgId,
            String pageIdentKey,
            String pageName,
            String title,
            String summaryInText) throws Exception {

        String seoKey = MyUtils.convertPageNameToId(pageName);
        //page name should also be cured
        pageName = MyUtils.removeNonAlphaNumeric(pageName);
        pageName = MyUtils.squeeze(pageName);

        //delete existing page on this SEO key
        // page delete - should cascade content delete via triggers
        // clean gloo_block_data (content of gloo_page) as well as
        // gloo_auto_post (summary content)

        String GLOO_PAGE_DELETE_SQL = "delete from gloo_page where org_id = "
                + orgId + " and seo_key ='" + seoKey + "' ";


        //fire delete
        java.sql.Statement stmt = connection.createStatement();
        stmt.executeUpdate(GLOO_PAGE_DELETE_SQL);
        stmt.close();

        String GLOO_PAGE_INSERT_SQL =
                "INSERT  INTO gloo_page(org_id,ident_key,seo_key,page_name,created_on,updated_on, meta_tags,title)"
                + " VALUES(?,?,?,?,now(),now(), ?,?) ";


        //create a description meta tag out of post summary

        String metaTags = "<meta name=\"description\" content=\"" + StringUtils.abbreviate(summaryInText, 300) + " \" />";

        java.sql.PreparedStatement pstmt = connection.prepareStatement(GLOO_PAGE_INSERT_SQL);
        pstmt.setString(1, orgId);
        pstmt.setString(2, pageIdentKey);
        pstmt.setString(3, seoKey);

        pstmt.setString(4, pageName);
        pstmt.setString(5, metaTags);
        pstmt.setString(6, title);

        pstmt.executeUpdate();
        pstmt.close();

    }

    public static void updateAutoKeyword(java.sql.Connection connection,
            String orgId,
            Keyword keyword,
            int value) throws Exception {

        String pageSeoKey = MyUtils.convertPageNameToId(keyword.getToken());

        String GLOO_AUTO_KEYWORD_UPADTE_SQL =
                "update gloo_auto_keyword set is_processed = ? where org_id = ? and seo_key = ? ";

        java.sql.PreparedStatement pstmt = connection.prepareStatement(GLOO_AUTO_KEYWORD_UPADTE_SQL);
        pstmt.setInt(1, value);
        pstmt.setString(2, orgId);
        pstmt.setString(3, pageSeoKey);


        pstmt.executeUpdate();
        pstmt.close();

    }
}

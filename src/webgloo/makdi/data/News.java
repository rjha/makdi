package webgloo.makdi.data;

import webgloo.makdi.html.HtmlGenerator;

/**
 *
 * @author rajeevj
 */
public class News implements IData {

    private String title;
    private String newsId;
    private String source;
    private String description;
    private String link;
    private String author;

    public News() {
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getNewsId() {
        return newsId;
    }

    public void setNewsId(String newsId) {
        this.newsId = newsId;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("title = ").append(this.title).append("\n");
        buffer.append("link = ").append(this.link).append("\n");
        buffer.append("source= ").append(this.source).append("\n");
        return buffer.toString();

    }

    @Override
    public String toHtml() throws Exception {
        return HtmlGenerator.generateNewsCode(this);
    }
}

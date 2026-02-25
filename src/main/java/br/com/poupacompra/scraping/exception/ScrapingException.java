package br.com.poupacompra.scraping.exception;

public class ScrapingException extends RuntimeException {
    
    private final String url;
    
    public ScrapingException(String message) {
        super(message);
        this.url = null;
    }
    
    public ScrapingException(String message, String url) {
        super(message);
        this.url = url;
    }
    
    public ScrapingException(String message, Throwable cause) {
        super(message, cause);
        this.url = null;
    }
    
    public ScrapingException(String message, String url, Throwable cause) {
        super(message, cause);
        this.url = url;
    }
    
    public String getUrl() {
        return url;
    }
}

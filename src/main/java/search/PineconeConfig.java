package search;

public class PineconeConfig {
    // Pinecone API configuration
    public static final String PINECONE_API_KEY = "pcsk_68g65d_Uucih6AFgmeJ14NgnDpWVcHxUW26cGSCfGMaoWZiKF3Ps7XEFaYVfFkFmw8bvRh";
    public static final String INDEX_HOST = "oop-project-r98wed5.svc.aped-4627-b74a.pinecone.io"; // e.g., "your-index-xxxxx.svc.pinecone.io"
    
    // Namespace constants
    public static final String NAMESPACE_SMARTPHONES = "smartphones";
    public static final String NAMESPACE_LAPTOPS = "laptops";
    
    // Search parameters
    public static final int TOP_K = 5; // Number of results to return
    
    private PineconeConfig() {
        // Private constructor to prevent instantiation
    }
} 
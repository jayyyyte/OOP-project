package search;

public class PineconeConfig {
    // Pinecone API configuration
    public static final String PINECONE_API_KEY = "pcsk_68g65d_Uucih6AFgmeJ14NgnDpWVcHxUW26cGSCfGMaoWZiKF3Ps7XEFaYVfFkFmw8bvRh";
    public static final String INDEX_HOST = "oop-project-r98wed5.svc.aped-4627-b74a.pinecone.io"; // e.g., "your-index-xxxxx.svc.pinecone.io"
    public static final String NAMESPACE = "__default__"; // Default namespace
    
    // Search parameters
    public static final int TOP_K = 3; // Number of results to return
    
    private PineconeConfig() {
        // Private constructor to prevent instantiation
    }
} 
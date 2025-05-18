package search;

import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.Map;

public class SearchCLI {
    private final SearchManager searchManager;
    private final RAGSearchEngine ragSearchEngine;
    private final Scanner scanner;

    public SearchCLI(String dataSource) throws IOException {
        this.searchManager = new SearchManager(dataSource);
        this.ragSearchEngine = new RAGSearchEngine(dataSource);
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("=== Search CLI (Development Version) ===");
        System.out.println("Type 'exit' to quit");
        System.out.println("Example queries:");
        System.out.println("- iphone pin trâu giá rẻ");
        System.out.println("- samsung giá rẻ");
        System.out.println("- xiaomi pin trâu");
        System.out.println("=====================================");

        while (true) {
            System.out.println("\nChoose search type:");
            System.out.println("1. Basic Search");
            System.out.println("2. RAG Search (Pinecone)");
            System.out.print("Enter your choice (1 or 2): ");
            
            String choice = scanner.nextLine().trim();
            
            if (choice.equalsIgnoreCase("exit")) {
                break;
            }

            if (!choice.equals("1") && !choice.equals("2")) {
                System.out.println("Invalid choice. Please enter 1 or 2.");
                continue;
            }

            System.out.print("\nEnter search query: ");
            String query = scanner.nextLine().trim();

            if (query.equalsIgnoreCase("exit")) {
                break;
            }

            if (query.isEmpty()) {
                System.out.println("Please enter a valid query");
                continue;
            }

            try {
                List<JSONObject> results;
                if (choice.equals("1")) {
                    results = searchManager.searchProducts(query);
                } else {
                    // For RAG search, we'll use a different approach
                    results = ragSearchEngine.search(Map.of("query", query));
                }
                searchManager.printResults(results);
            } catch (Exception e) {
                System.err.println("Error during search: " + e.getMessage());
            }
        }

        scanner.close();
    }

    public static void main(String[] args) {
        // Update path to point to resources directory
        String dataSource = "src/main/resources/products.json";
        try {
            SearchCLI cli = new SearchCLI(dataSource);
            cli.start();
        } catch (IOException e) {
            System.err.println("Error initializing search system: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 
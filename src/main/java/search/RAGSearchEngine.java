package search;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import okhttp3.*;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RAGSearchEngine extends SearchEngine {
    private final OkHttpClient client;
    
    public RAGSearchEngine(String dataSource) throws IOException {
        super(dataSource);
        this.client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    }

    @Override
    public List<JSONObject> search(Map<String, Object> criteria) {
        if (criteria == null || !criteria.containsKey("query")) {
            System.err.println("Error: Invalid search criteria - query is required");
            return new ArrayList<>();
        }

        String query = (String) criteria.get("query");
        if (query == null || query.trim().isEmpty()) {
            System.err.println("Error: Search query cannot be empty");
            return new ArrayList<>();
        }

        return searchPinecone(query);
    }

    private List<JSONObject> searchPinecone(String query) {
        try {
            // Create the request body according to new Pinecone API format
            JSONObject requestBody = new JSONObject();
            JSONObject queryObj = new JSONObject();
            JSONObject inputs = new JSONObject();
            inputs.put("text", query);
            queryObj.put("inputs", inputs);
            queryObj.put("top_k", PineconeConfig.TOP_K);
            requestBody.put("query", queryObj);
            
            // Specify which fields to return
            JSONArray returnFields = new JSONArray();
            returnFields.put("name");
            returnFields.put("description");
            returnFields.put("price");
            returnFields.put("productUrl");
            returnFields.put("specifications");
            returnFields.put("overallRating");
            returnFields.put("categoryData");
            requestBody.put("fields", returnFields);

            // Create the HTTP request
            String url = "https://" + PineconeConfig.INDEX_HOST + "/records/namespaces/" + PineconeConfig.NAMESPACE + "/search";
            Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("Api-Key", PineconeConfig.PINECONE_API_KEY)
                .addHeader("X-Pinecone-API-Version", "unstable")
                .post(RequestBody.create(
                    MediaType.parse("application/json"),
                    requestBody.toString()
                ))
                .build();

            // Execute the request
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error details available";
                    System.err.println("Pinecone API error: " + response.code());
                    System.err.println("Error details: " + errorBody);
                    
                    // Handle specific error codes
                    switch (response.code()) {
                        case 400:
                            System.err.println("Bad request - Please check your query format and parameters");
                            break;
                        case 401:
                            System.err.println("Unauthorized - Please check your API key");
                            break;
                        case 403:
                            System.err.println("Forbidden - You don't have permission to access this resource");
                            break;
                        case 404:
                            System.err.println("Not found - The requested resource doesn't exist");
                            break;
                        case 429:
                            System.err.println("Too many requests - Please try again later");
                            break;
                        case 500:
                            System.err.println("Internal server error - Please try again later");
                            break;
                        default:
                            System.err.println("Unexpected error occurred");
                    }
                    return new ArrayList<>();
                }

                // Parse the response
                String responseBody = response.body().string();
                if (responseBody == null || responseBody.trim().isEmpty()) {
                    System.err.println("Error: Empty response from Pinecone API");
                    return new ArrayList<>();
                }

                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    if (!jsonResponse.has("result")) {
                        System.err.println("Error: Invalid response format - missing 'result' field");
                        return new ArrayList<>();
                    }

                    JSONObject result = jsonResponse.getJSONObject("result");
                    if (!result.has("hits")) {
                        System.err.println("Error: Invalid response format - missing 'hits' field");
                        return new ArrayList<>();
                    }

                    JSONArray hits = result.getJSONArray("hits");
                    if (hits.length() == 0) {
                        System.out.println("No products found matching your search criteria.");
                        return new ArrayList<>();
                    }

                    // Convert Pinecone results to our format
                    List<JSONObject> results = new ArrayList<>();
                    for (int i = 0; i < hits.length(); i++) {
                        try {
                            JSONObject hit = hits.getJSONObject(i);
                            if (!hit.has("fields")) {
                                System.err.println("Warning: Hit missing 'fields' - skipping");
                                continue;
                            }

                            JSONObject hitFields = hit.getJSONObject("fields");
                            
                            // Create a product object in our format
                            JSONObject product = new JSONObject();
                            product.put("name", hitFields.optString("name", "Unknown"));
                            product.put("description", hitFields.optString("description", ""));
                            product.put("price", hitFields.optDouble("price", 0.0));
                            product.put("productUrl", hitFields.optString("productUrl", ""));
                            
                            // Add specifications if available
                            if (hitFields.has("specifications")) {
                                product.put("specifications", hitFields.get("specifications"));
                            }
                            
                            // Add category data if available
                            if (hitFields.has("categoryData")) {
                                product.put("categoryData", hitFields.get("categoryData"));
                            }
                            
                            // Add rating if available
                            if (hitFields.has("overallRating")) {
                                product.put("overallRating", hitFields.getDouble("overallRating"));
                            }
                            
                            results.add(product);
                        } catch (JSONException e) {
                            System.err.println("Warning: Error processing hit " + i + ": " + e.getMessage());
                            continue;
                        }
                    }
                    
                    return results;
                } catch (JSONException e) {
                    System.err.println("Error parsing Pinecone response: " + e.getMessage());
                    return new ArrayList<>();
                }
            }
        } catch (IOException e) {
            System.err.println("Network error during Pinecone search: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Unexpected error during Pinecone search: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // This method should be implemented to get embeddings for the query
    // You can use a local model or call an embedding API
    private List<Float> getQueryEmbedding(String query) {
        // TODO: Implement query embedding
        // For now, return a dummy embedding
        List<Float> dummyEmbedding = new ArrayList<>();
        for (int i = 0; i < 1536; i++) { // Assuming 1536-dimensional embeddings
            dummyEmbedding.add(0.0f);
        }
        return dummyEmbedding;
    }
} 
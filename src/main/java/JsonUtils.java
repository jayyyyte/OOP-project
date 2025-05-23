//import com.fasterxml.jackson.core.util.DefaultIndenter;
//import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.ObjectWriter;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.List;
//
//public class JsonUtils {
//
//    private static final ObjectMapper objectMapper = new ObjectMapper();
//
//    public static void saveProductsToJson(List<Product> products, String filename) {
//        try {
//            // Configure pretty printing
//            DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
//            prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE); // Indent arrays nicely
//            ObjectWriter writer = objectMapper.writer(prettyPrinter);
//
//            // Write list to file
//            writer.writeValue(new File(filename), products);
//            System.out.println("Successfully saved " + products.size() + " products to " + filename);
//
//        } catch (IOException e) {
//            System.err.println("Error saving products to JSON file: " + filename);
//            e.printStackTrace();
//        }
//    }
//}


//import org.jsoup.*;
//import org.jsoup.nodes.*;
//import org.jsoup.select.*;
//import org.jsoup.Connection;
//import java.io.IOException;
//
//public class Main {
//    public static void main(String[] args) throws IOException {
//        // initializing the HTML Document page variable
//        Document doc;
//
//        try {
//            // fetching the target website
//            doc = Jsoup.connect("https://cellphones.com.vn/mobile.html").get();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        doc = Jsoup
//                .connect("https://cellphones.com.vn/mobile.html")
//                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
//                .header("Accept-Language", "*")
//                .get();
//
//    }
//}

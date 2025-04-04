package main.crawler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvExporter {
    public static void exportPhonesToCsv(List<Phone> phones, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write CSV header
            writer.append("Tên sản phẩm,Giá hiện tại,Giá gốc,Giảm giá,Mô tả,Thông số kỹ thuật,URL ảnh,URL sản phẩm,Đánh giá,Số đánh giá\n");
            
            // Write data rows
            for (Phone phone : phones) {
                writer.append(escapeCsv(phone.getName())).append(",");
                writer.append(escapeCsv(phone.getPrice())).append(",");
                writer.append(escapeCsv(phone.getOriginalPrice())).append(",");
                writer.append(escapeCsv(phone.getDiscount())).append(",");
                writer.append(escapeCsv(phone.getDescription())).append(",");
                writer.append(escapeCsv(phone.getSpecifications())).append(",");
                writer.append(escapeCsv(phone.getImageUrl())).append(",");
                writer.append(escapeCsv(phone.getProductUrl())).append(",");
                writer.append(escapeCsv(phone.getRating())).append(",");
                writer.append(escapeCsv(phone.getReviewCount())).append("\n");
            }
            
            System.out.println("Đã xuất dữ liệu thành công ra file: " + filePath);
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi file CSV: " + e.getMessage());
        }
    }
    
    private static String escapeCsv(String input) {
        if (input == null || input.equals("N/A")) {
            return "";
        }
        // Escape quotes and wrap in quotes if contains comma or newline
        String escaped = input.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
package msa.emaia.tools;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Tools {

    public static String generateOTP() {
        // Define the range for the OTP (100000 to 999999)
        int min = 100000;
        int max = 999999;

        // Create an instance of Random class
        Random random = new Random();

        // Generate a random number within the range
        int otpValue = random.nextInt(max - min + 1) + min;

        // Convert the random number to a string
        return String.valueOf(otpValue);
    }

    public static String toLowerCase(String str) {
        if(str  == null) return null;
        return str.toLowerCase();
    }

    public static String generateFileNameWithDate(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";

        // Extract file extension if it exists
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < originalFilename.length() - 1) {
            fileExtension = originalFilename.substring(dotIndex);
        }

        // Get current date
        LocalDateTime currentDate = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        // Generate new filename
        String dateString = currentDate.format(formatter);
        long ri = generateRandomNumber(10000L);

        return "file_" + dateString+"_"+ri + fileExtension;
    }

    public static void copyProperties(Object source, Object target, String... ignoreProperties) {
        Set<String> ignoreSet = new HashSet<>(Arrays.asList(ignoreProperties));
        ignoreSet.add("createdBy");
        ignoreSet.add("createdAt");
        ignoreSet.add("editedAt");
        ignoreSet.add("editedBy");

        PropertyDescriptor[] sourcePds = BeanUtils.getPropertyDescriptors(source.getClass());
        for (PropertyDescriptor sourcePd : sourcePds) {
            if (ignoreSet.contains(sourcePd.getName())) {
                continue;
            }
            PropertyDescriptor targetPd = BeanUtils.getPropertyDescriptor(target.getClass(), sourcePd.getName());

            if (targetPd != null && targetPd.getWriteMethod() != null) {
                try {
                    Method readMethod = sourcePd.getReadMethod();
                    if (readMethod != null) {
                        if (!readMethod.canAccess(source)) {
                            readMethod.setAccessible(true);
                        }
                        Object value = readMethod.invoke(source);

                        Method writeMethod = targetPd.getWriteMethod();
                        if (!writeMethod.canAccess(target)) {
                            writeMethod.setAccessible(true);
                        }
                        writeMethod.invoke(target, value);
                    }
                } catch (Throwable ex) {
                    throw new BeansException("Could not copy property '" + sourcePd.getName() + "' from source to target", ex) {};
                }
            }
        }
    }

    public static long generateRandomNumber(long min) {
        UUID uuid = UUID.randomUUID();
        // Extract a numeric value from the UUID
        long randomId = Math.abs(uuid.getMostSignificantBits());
        // Ensure the number is within a desired range (e.g., 10 digits)
        return randomId % min;
    }

    public static long generateRandomNumber() {
        return generateRandomNumber(1000000000L);
    }


    public static Cell getMergedRegionCell(Sheet sheet, int rowNum, int colIndex) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            if (region.isInRange(rowNum, colIndex)) {
                return sheet.getRow(region.getFirstRow()).getCell(region.getFirstColumn());
            }
        }
        return null;
    }
}

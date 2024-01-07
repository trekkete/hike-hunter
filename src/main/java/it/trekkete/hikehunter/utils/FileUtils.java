package it.trekkete.hikehunter.utils;

import it.trekkete.hikehunter.data.entity.Trip;
import it.trekkete.hikehunter.data.entity.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class FileUtils {

    private static final String DEFAULT_PATH = "/var/hike-hunter/";
    private static final String USER_PATH = "user/";
    private static final String TRIP_PATH = "trip/";

    private static boolean checkDirectories(User user) {
        return new File(DEFAULT_PATH + USER_PATH + user.getId().toString()).mkdirs();
    }

    private static boolean checkDirectories(Trip trip) {
        return new File(DEFAULT_PATH + TRIP_PATH + trip.getId().toString()).mkdirs();
    }

    public static String saveForUser(byte[] fileContent, String extension, User user, String overwrite) {

        checkDirectories(user);

        String path = DEFAULT_PATH + USER_PATH + user.getId() + "/";

        File output = new File(path, "file-" + String.format("%08d", 0) + extension);

        if (overwrite != null) {
            output = new File(path, overwrite);
        }
        else {
            for (int num = 0; output.exists(); num++) {
                output = new File(path, "file-" + String.format("%08d", num) + extension);
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream(output)) {
            outputStream.write(fileContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return output.getName();
    }

    public static byte[] loadForUser(String name, User user) throws IOException {

        String path = DEFAULT_PATH + USER_PATH + user.getId() + "/" + name;

        File output = new File(path);

        if (!output.exists()) {
            return new byte[0];
        }

        return Files.readAllBytes(output.toPath());
    }
}

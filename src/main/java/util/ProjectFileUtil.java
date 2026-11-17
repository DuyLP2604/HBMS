package util;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ProjectFileUtil {

    public static String saveImageToProject(ServletContext context, Part part, String subFolder, String fileName) {
        try {
            // 1. deploy path of tomcat
            String deployPath = context.getRealPath("") + File.separator + "assets" + File.separator + "images" + File.separator + subFolder;
            File deployDir = new File(deployPath);
            if (!deployDir.exists()) {
                deployDir.mkdirs();
            }

            // 2. Calculate path to project file
            String projectRootPath = context.getRealPath("").split("target")[0] + "src" + File.separator + "main" + File.separator + "webapp";
            String sourcePath = projectRootPath + File.separator + "assets" + File.separator + "images" + File.separator + subFolder;
            File sourceDir = new File(sourcePath);
            if (!sourceDir.exists()) {
                sourceDir.mkdirs();
            }

            // 3. Double Save
            String uniqueFileName = subFolder + "-" + System.currentTimeMillis() + fileName.substring(fileName.lastIndexOf("."));
            File deployFile = new File(deployDir, uniqueFileName);
            File sourceFile = new File(sourceDir, uniqueFileName);

            // Read input stream and write file to Deploy file of tomcat, source folder
            try (InputStream in = part.getInputStream(); OutputStream outDeploy = new FileOutputStream(deployFile); OutputStream outSource = new FileOutputStream(sourceFile)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    outDeploy.write(buffer, 0, length);
                    outSource.write(buffer, 0, length);
                }
            }
            return uniqueFileName;

        } catch (Exception e) {
            return null;
        }
    }

    public static void deleteImageFromProject(ServletContext context, String subFolder, String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return;
        }

        try {
            // 1. Delete file of Deploy / target
            String deployPath = context.getRealPath("") + File.separator + "assets" + File.separator + "images" + File.separator + subFolder;
            File deployFile = new File(deployPath, fileName);
            if (deployFile.exists()) {
                deployFile.delete();
            }

            // 2. delete file of src/main/webapp
            String projectRootPath = context.getRealPath("").split("target")[0] + "src" + File.separator + "main" + File.separator + "webapp";
            String sourcePath = projectRootPath + File.separator + "assets" + File.separator + "images" + File.separator + subFolder;
            File sourceFile = new File(sourcePath, fileName);
            if (sourceFile.exists()) {
                sourceFile.delete();
            }
        } catch (Exception e) {
        }
    }

    public static String saveImageWithExactName(ServletContext context, Part part, String subFolder, String exactFileName) {
        try {
            String deployPath = context.getRealPath("") + File.separator + "assets" + File.separator + "images" + File.separator + subFolder;
            File deployDir = new File(deployPath);
            if (!deployDir.exists()) {
                deployDir.mkdirs();
            }

            String projectRootPath = context.getRealPath("").split("target")[0] + "src" + File.separator + "main" + File.separator + "webapp";
            String sourcePath = projectRootPath + File.separator + "assets" + File.separator + "images" + File.separator + subFolder;
            File sourceDir = new File(sourcePath);
            if (!sourceDir.exists()) {
                sourceDir.mkdirs();
            }

            File deployFile = new File(deployDir, exactFileName);
            File sourceFile = new File(sourceDir, exactFileName);

            // Double save
            try (InputStream in = part.getInputStream(); OutputStream outDeploy = new FileOutputStream(deployFile); OutputStream outSource = new FileOutputStream(sourceFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    outDeploy.write(buffer, 0, length);
                    outSource.write(buffer, 0, length);
                }
            }
            return exactFileName;
        } catch (Exception e) {
            return null;
        }
    }
}

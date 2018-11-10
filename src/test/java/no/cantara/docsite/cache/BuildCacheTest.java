package no.cantara.docsite.cache;

import no.cantara.docsite.util.CommonUtil;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BuildCacheTest {

    @Test
    public void testUnzipTestData() throws IOException {
        File fileZip = CommonUtil.getCurrentPath().resolve("src/test/resources/testdata.zipfile").toFile();
        Path destDir = CommonUtil.getCurrentPath().resolve("target/testdata/");
        Files.createDirectories(destDir);
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
        ZipEntry zipEntry = zis.getNextEntry();
        System.out.println(zipEntry.getName());
        while (zipEntry != null) {
            File newFile = newFile(destDir.toFile(), zipEntry);
            String targetFilenamePath = newFile.getParentFile().getAbsolutePath();
            System.out.println(targetFilenamePath);
            Files.createDirectories(Paths.get(targetFilenamePath));
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }

    static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}

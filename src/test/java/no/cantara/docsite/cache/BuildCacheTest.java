package no.cantara.docsite.cache;

import no.cantara.docsite.domain.github.commits.GitHubCommitRevision;
import no.cantara.docsite.json.JsonbFactory;
import no.cantara.docsite.util.CommonUtil;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BuildCacheTest {

    static final Path destDir = CommonUtil.getCurrentPath().resolve("target/testdata/");

    static void unzipTestData() throws IOException {
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

    /*
    The directories reflect the URL paths:

     RootDirs
      repos
      test
      orgs
      github
      buildStatus
      pages
     RepoDirs
      organziation
          repoName
            commits(file)
            contents (dir)
                pom.xml (file)
            readmefile
    */

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    static List<String> rootDirs() {
        try {
            return Files.walk(destDir, 1).skip(1).map(f -> f.toAbsolutePath().toString().replace(destDir.toString(), "").substring(1)).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static List<String> navigateDirectory(Path subDir) {
        try {
            return Files.walk(subDir, 1).skip(1).map(f -> f.toAbsolutePath().toString().replace(subDir.toString(), "").substring(1)).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    @Test
    public void testNavigate() {
        List<String> rootDirs = rootDirs();
        rootDirs.forEach(rootDir -> {
            System.out.println(rootDir);

            Path reposPath = destDir.resolve(rootDir);
            List<String> orgsDirs = navigateDirectory(reposPath);
            orgsDirs.forEach(orgDir -> {
                System.out.println("  " + orgDir);

                Path orgReposPath = destDir.resolve(rootDir + "/" + orgDir);
                List<String> reposDirs = navigateDirectory(orgReposPath);
                reposDirs.forEach(repoDir -> {
                    System.out.println("    " + repoDir);

                    if (!repoDir.isBlank()) {
                        Path orgRepoPath = destDir.resolve(rootDir + "/" + orgDir + "/" + repoDir);
                        List<String> orgReposDirs = navigateDirectory(orgRepoPath);
                        orgReposDirs.forEach(orgRepoDir -> {
                            System.out.println("      " + orgRepoDir);
                        });
                    }
                });

            });
        });
    }

    static List<CacheKey> repos(String organization) throws IOException {
        Path reposPath = destDir.resolve("repos/"+organization);
        List<CacheKey> repos = Files.walk(reposPath, 1).skip(1).map(f -> f.toAbsolutePath().toString().replace(reposPath.toString(), "").substring(1)).map(f -> CacheKey.of(organization, f, "master")).collect(Collectors.toList());
        return repos;
    }

    static List<GitHubCommitRevision> commitRevisions(CacheKey cacheKey) throws IOException {
        Path reposPath = destDir.resolve(String.format("repos/%s/%s/commits", cacheKey.organization, cacheKey.repoName));
        return Arrays.asList(JsonbFactory.instance().fromJson(new String(Files.readAllBytes(reposPath)), GitHubCommitRevision[].class));
    }

    @Test
    public void testGetRepoCommits() throws IOException {
        for(CacheKey cacheKey : repos("Cantara")) {
        List<GitHubCommitRevision> repos = commitRevisions(CacheKey.of(cacheKey.organization, cacheKey.repoName, cacheKey.branch));
        repos.forEach(repo -> System.out.println(repo));

        }
    }
}

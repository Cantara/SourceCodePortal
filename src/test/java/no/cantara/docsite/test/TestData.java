package no.cantara.docsite.test;

import no.cantara.docsite.cache.CacheHelper;
import no.cantara.docsite.cache.CacheKey;
import no.cantara.docsite.cache.CacheRepositoryKey;
import no.cantara.docsite.cache.CacheShaKey;
import no.cantara.docsite.cache.CacheStore;
import no.cantara.docsite.domain.github.commits.GitHubCommitRevision;
import no.cantara.docsite.domain.github.contents.GitHubRepositoryContents;
import no.cantara.docsite.domain.github.repos.GitHubRepository;
import no.cantara.docsite.domain.maven.FetchMavenPOMTask;
import no.cantara.docsite.domain.maven.MavenPOM;
import no.cantara.docsite.domain.scm.ScmRepository;
import no.cantara.docsite.json.JsonbFactory;
import no.cantara.docsite.util.CommonUtil;
import no.ssb.config.DynamicConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TestData {

    final Path destDir = CommonUtil.getCurrentPath().resolve("target/testdata/");

    TestData() {
        try {
            unzipTestData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static TestData instance() {
        return TestDataSingleton.INSTANCE;
    }

    private static final class TestDataSingleton {
        private static final TestData INSTANCE = new TestData();
    }

    void unzipTestData() throws IOException {
        if (destDir.toFile().exists()) {
            return;
        }
        File fileZip = CommonUtil.getCurrentPath().resolve("src/test/resources/testdata.zipfile").toFile();
        Files.createDirectories(destDir);
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = new File(destDir.toFile(), zipEntry.getName());
            String destDirPath = destDir.toFile().getCanonicalPath();
            String destFilePath = newFile.getCanonicalPath();
            if (!destFilePath.startsWith(destDirPath + File.separator)) {
                throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
            }
            String targetFilenamePath = newFile.getParentFile().getAbsolutePath();
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

    public Path destDir() {
        return destDir;
    }

    public void populateCacheStore(DynamicConfiguration configuration, CacheStore cacheStore) {
        if (CacheHelper.cacheSize(cacheStore.getRepositories()) > 70) {
            return;
        }

        reposToScmRepository(configuration, (key, repo) -> cacheStore.getRepositories().put(key, repo));

        Map<CacheShaKey, GitHubCommitRevision> commitRevisions = commitRevisions("Cantara");
        commitRevisions.entrySet().iterator().forEachRemaining(entry -> cacheStore.getCommits().put(entry.getKey(), entry.getValue().asCommitRevision(entry.getKey())));

        Map<CacheKey, GitHubRepositoryContents> readmeContents = readmeContents("Cantara");
        readmeContents.entrySet().iterator().forEachRemaining(entry -> cacheStore.getReadmeContents().put(entry.getKey(), entry.getValue().asRepositoryContents(entry.getKey())));

        Map<CacheKey, GitHubRepositoryContents> mavenPOMContents = mavenPOMContents("Cantara");
        mavenPOMContents.entrySet().iterator().forEachRemaining(entry -> cacheStore.getMavenProjects().put(entry.getKey(), FetchMavenPOMTask.parse(entry.getValue().content)));
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

    public List<CacheKey> repoKeys(String organization) {
        try {
            Path reposPath = destDir.resolve("repos/" + organization);
            List<CacheKey> repos = Files.walk(reposPath, 1).skip(1).map(f -> f.toAbsolutePath().toString().replace(reposPath.toString(), "").substring(1)).map(f -> CacheKey.of(organization, f, "master")).collect(Collectors.toList());
            return repos;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<GitHubRepository> repos(String organization) {
        try {
            Path reposPath = destDir.resolve("orgs/" + organization + "/repos?type=PUBLIC&per_page=500");
            return Arrays.asList(JsonbFactory.instance().fromJson(new String(Files.readAllBytes(reposPath)), GitHubRepository[].class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void repos(DynamicConfiguration configuration, BiConsumer<CacheRepositoryKey,GitHubRepository> visitor) {
        for (CacheKey cacheKey : repoKeys("Cantara")) {
            CacheRepositoryKey cacheRepositoryKey = CacheRepositoryKey.of(cacheKey, "groupId", false);
            for (GitHubRepository repo : repos("Cantara")) {
                if ((cacheKey.organization+"/"+cacheKey.repoName).equals(repo.fullName) && cacheKey.branch.equals(repo.defaultBranch)) {
                    visitor.accept(cacheRepositoryKey, repo);
                }
            }
        }
    }

    public void reposToScmRepository(DynamicConfiguration configuration, BiConsumer<CacheRepositoryKey,ScmRepository> visitor) {
        for (CacheKey cacheKey : repoKeys("Cantara")) {
            CacheRepositoryKey cacheRepositoryKey = CacheRepositoryKey.of(cacheKey, "groupId", false);
            for (GitHubRepository repo : repos("Cantara")) {
                if ((cacheKey.organization+"/"+cacheKey.repoName).equals(repo.fullName) && cacheKey.branch.equals(repo.defaultBranch)) {
                    ScmRepository scmRepository = createScmRepository(configuration, cacheRepositoryKey, repo);
                    visitor.accept(cacheRepositoryKey, scmRepository);
                }
            }
        }
    }

    ScmRepository createScmRepository(DynamicConfiguration configuration, CacheRepositoryKey cacheRepositoryKey, GitHubRepository githubRepository) {
        return ScmRepository.of(configuration, cacheRepositoryKey, "displayName", "description", new LinkedHashMap<>(),
                                githubRepository.id, githubRepository.description, "groupId", (githubRepository.license != null ? githubRepository.license.spdxId : null), githubRepository.htmlUrl);
    }

    public List<GitHubCommitRevision> commitRevisions(CacheKey cacheKey) {
        try {
            Path reposPath = destDir.resolve(String.format("repos/%s/%s/commits", cacheKey.organization, cacheKey.repoName));
            return Arrays.asList(JsonbFactory.instance().fromJson(new String(Files.readAllBytes(reposPath)), GitHubCommitRevision[].class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<CacheShaKey, GitHubCommitRevision> commitRevisions(String organization) {
        Map<CacheShaKey, GitHubCommitRevision> commitRevisionMap = new LinkedHashMap<>();
        for (CacheKey cacheKey : repoKeys(organization)) {
            List<GitHubCommitRevision> repos = commitRevisions(CacheKey.of(cacheKey.organization, cacheKey.repoName, cacheKey.branch));
            repos.forEach(repo -> commitRevisionMap.put(CacheShaKey.of(cacheKey, "groupId", repo.sha), repo));
        }
        return commitRevisionMap;
    }

    public GitHubRepositoryContents readmeContent(CacheKey cacheKey) {
        try {
            Path reposPath = destDir.resolve(String.format("repos/%s/%s/readme?ref=%s", cacheKey.organization, cacheKey.repoName, cacheKey.branch));
            return JsonbFactory.instance().fromJson(new String(Files.readAllBytes(reposPath)), GitHubRepositoryContents.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<CacheKey, GitHubRepositoryContents> readmeContents(String organization) {
        return repoKeys(organization).stream().collect(Collectors.toMap(key -> key, key -> readmeContent(key)));
    }


    private Path getMavenPOMPath(CacheKey cacheKey) {
        return destDir.resolve(String.format("repos/%s/%s/contents/pom.xml?ref=%s", cacheKey.organization, cacheKey.repoName, cacheKey.branch));
    }

    public MavenPOM mavenPOM(CacheKey cacheKey) {
        try {
            return FetchMavenPOMTask.parse(mavenPOMContent(cacheKey).content);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public GitHubRepositoryContents mavenPOMContent(CacheKey cacheKey) {
        try {
            return JsonbFactory.instance().fromJson(new String(Files.readAllBytes(getMavenPOMPath(cacheKey))), GitHubRepositoryContents.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<CacheKey, GitHubRepositoryContents> mavenPOMContents(String organization) {
        return repoKeys(organization).stream().filter(key -> getMavenPOMPath(key).toFile().exists()).collect(Collectors.toMap(key -> key, key -> mavenPOMContent(key)));
    }

}

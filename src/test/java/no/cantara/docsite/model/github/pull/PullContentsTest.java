package no.cantara.docsite.model.github.pull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyNamingStrategy;
import java.io.IOException;

public class PullContentsTest {

    private static final Logger LOG = LoggerFactory.getLogger(PullContentsTest.class);

    final String CONTENTS_JSON = "{\n" +
            "  \"type\": \"file\",\n" +
            "  \"encoding\": \"base64\",\n" +
            "  \"size\": 5362,\n" +
            "  \"name\": \"README.md\",\n" +
            "  \"path\": \"README.md\",\n" +
            "  \"content\": \"ZW5jb2RlZCBjb250ZW50IC4uLg==\",\n" +
            "  \"sha\": \"3d21ec53a331a6f037a91c368710b99387d012c1\",\n" +
            "  \"url\": \"https://api.github.com/repos/octokit/octokit.rb/contents/README.md\",\n" +
            "  \"git_url\": \"https://api.github.com/repos/octokit/octokit.rb/git/blobs/3d21ec53a331a6f037a91c368710b99387d012c1\",\n" +
            "  \"html_url\": \"https://github.com/octokit/octokit.rb/blob/master/README.md\",\n" +
            "  \"download_url\": \"https://raw.githubusercontent.com/octokit/octokit.rb/master/README.md\",\n" +
            "  \"_links\": {\n" +
            "    \"git\": \"https://api.github.com/repos/octokit/octokit.rb/git/blobs/3d21ec53a331a6f037a91c368710b99387d012c1\",\n" +
            "    \"self\": \"https://api.github.com/repos/octokit/octokit.rb/contents/README.md\",\n" +
            "    \"html\": \"https://github.com/octokit/octokit.rb/blob/master/README.md\"\n" +
            "  }\n" +
            "}";

    @Test
    public void testJson() throws IOException {
        JsonbConfig config = new JsonbConfig().withPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE_WITH_UNDERSCORES);
        Jsonb jsonb = JsonbBuilder.create(config);
        RepositoryContents contents = jsonb.fromJson(CONTENTS_JSON, RepositoryContents.class);
        LOG.trace("contents: {}\ncontents: {}", contents, contents.getDecodedContent());
    }

}

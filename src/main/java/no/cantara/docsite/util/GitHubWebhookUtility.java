/*
 * Licensed under MIT License
 * Copyright (c) 2017 Bernhard Grünewaldt
 * https://github.com/codeclou/jenkins-github-webhook-build-trigger-plugin/blob/master/src/main/java/io/codeclou/jenkins/githubwebhookbuildtriggerplugin/webhooksecret/GitHubWebhookUtility.java
 * https://github.com/codeclou/jenkins-github-webhook-build-trigger-plugin/blob/master/src/main/java/io/codeclou/jenkins/githubwebhookbuildtriggerplugin/GithubWebhookBuildTriggerAction.java
 */
package no.cantara.docsite.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/*
 * Verify HMAC sha1 'x-hub-signature' Header against Request Payload
 * See: https://developer.github.com/webhooks/securing/
 */
public class GitHubWebhookUtility {

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private static final char[] HEX = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    public static boolean verifySignature(String payload, String signature, String secret) {
        if (payload == null || signature == null || secret == null) {
            return false;
        }
        boolean isValid;
        try {
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(payload.getBytes());
            String expected = signature.substring(5);
            String actual = new String(encode(rawHmac));
            isValid = expected.equals(actual);
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException ex) {
            throw new RuntimeException(ex.getLocalizedMessage());
        }
        return isValid;
    }

    private static char[] encode(byte[] bytes) {
        final int amount = bytes.length;
        char[] result = new char[2 * amount];
        int j = 0;
        for (int i = 0; i < amount; i++) {
            result[j++] = HEX[(0xF0 & bytes[i]) >>> 4];
            result[j++] = HEX[(0x0F & bytes[i])];
        }
        return result;
    }

}
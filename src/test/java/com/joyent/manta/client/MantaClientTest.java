/**
 * Copyright (c) 2015, Joyent, Inc. All rights reserved.
 */
package com.joyent.manta.client;

import com.google.api.client.http.HttpHeaders;
import com.joyent.manta.client.config.TestConfigContext;
import com.joyent.manta.config.ConfigContext;
import com.joyent.manta.exception.MantaClientHttpResponseException;
import com.joyent.manta.exception.MantaCryptoException;
import com.joyent.manta.exception.MantaObjectException;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;


/**
 * @author Yunong Xiao
 */
public class MantaClientTest {

    private static final String TEST_DATA = "EPISODEII_IS_BEST_EPISODE";
    private static final String TEST_FILENAME = "Master-Yoda.jpg";

    private MantaClient mantaClient;

    private String testPathPrefix;


    @BeforeClass
    @Parameters({"manta.url", "manta.user", "manta.key_path", "manta.key_id", "manta.timeout"})
    public void beforeClass(@Optional String mantaUrl,
                            @Optional String mantaUser,
                            @Optional String mantaKeyPath,
                            @Optional String mantaKeyId,
                            @Optional Integer mantaTimeout)
            throws IOException, MantaCryptoException {

        // Let TestNG configuration take precedence over environment variables
        ConfigContext config = new TestConfigContext(
                mantaUrl, mantaUser, mantaKeyPath, mantaKeyId, mantaTimeout);

        mantaClient = new MantaClient(config);
        testPathPrefix = String.format("/%s/stor/%s/", config.getMantaUser(), UUID.randomUUID());
        mantaClient.putDirectory(testPathPrefix, null);
    }


    @AfterClass
    public void afterClass() throws IOException, MantaCryptoException {
        if (mantaClient != null) {
            mantaClient.deleteRecursive(testPathPrefix);
        }
    }


    @Test
    public final void testCRUDObject() throws IOException {
        final String name = UUID.randomUUID().toString();
        final String path = testPathPrefix + name;
        mantaClient.put(path, TEST_DATA);

        try (final MantaObjectInputStream gotObject = mantaClient.getAsInputStream(path)) {
            Assert.assertNotNull(gotObject);
            Assert.assertNotNull(gotObject.getContentType());
            Assert.assertNotNull(gotObject.getContentLength());
            Assert.assertNotNull(gotObject.getEtag());
            Assert.assertNotNull(gotObject.getMtime());
            Assert.assertNotNull(gotObject.getPath());

            final String data = MantaUtils.inputStreamToString(gotObject);
            Assert.assertEquals(data, TEST_DATA);
        }

        mantaClient.delete(path);
        boolean thrown = false;
        try {
            mantaClient.get(testPathPrefix + name);
        } catch (final MantaClientHttpResponseException e) {
            Assert.assertEquals(404, e.getStatusCode());
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }


    @Test
    public final void testCRUDWithFileObject() throws IOException {
        final String name = UUID.randomUUID().toString();
        final String path = testPathPrefix + name;

        mantaClient.put(path, TEST_DATA);
        final File file = mantaClient.getToTempFile(path);

        final String data = MantaUtils.readFileToString(file);
        Assert.assertEquals(data, TEST_DATA);
        mantaClient.delete(path);
        boolean thrown = false;
        try {
            mantaClient.get(testPathPrefix + name);
        } catch (final MantaClientHttpResponseException e) {
            Assert.assertEquals(404, e.getStatusCode());
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }


    @Test
    public final void testCRUDObjectWithHeaders() throws IOException {
        final String name = UUID.randomUUID().toString();
        final String path = testPathPrefix + name;
        final HttpHeaders headers = new HttpHeaders();
        headers.set("durability-level", 4);

        mantaClient.put(path, TEST_DATA, headers);
        try (final MantaObjectInputStream gotObject = mantaClient.getAsInputStream(path)) {
            final String data = MantaUtils.inputStreamToString(gotObject);
            Assert.assertEquals(data, TEST_DATA);
            Assert.assertEquals("4", gotObject.getHttpHeaders().getFirstHeaderStringValue("durability-level"));
            mantaClient.delete(gotObject.getPath());
        }

        boolean thrown = false;
        try {
            mantaClient.get(testPathPrefix + name);
        } catch (final MantaClientHttpResponseException e) {
            Assert.assertEquals(404, e.getStatusCode());
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }


    @Test
    public final void testDirectoryIsMarkedAsSuch() throws IOException {
        MantaObject dir = mantaClient.get(testPathPrefix);
        Assert.assertTrue(dir.isDirectory(),
                String.format("Directory should be marked as such [%s]", testPathPrefix));
    }


    @Test
    public final void testRecursiveDeleteObject() throws IOException {
        final String dir1 = String.format("%s1", testPathPrefix);
                mantaClient.putDirectory(testPathPrefix + "1", null);
        mantaClient.putDirectory(dir1, null);
        final String path1 = String.format("%s/%s", dir1, UUID.randomUUID());
        mantaClient.put(path1, TEST_DATA);

        final String dir2 = String.format("%s/2", dir1);
        mantaClient.putDirectory(dir2, null);
        final String path2 = String.format("%s/%s", dir2, UUID.randomUUID());
        mantaClient.put(path2, TEST_DATA);

        final String dir3 = String.format("%s/3", dir2);
        mantaClient.putDirectory(dir3, null);
        final String path3 = String.format("%s/%s", dir3, UUID.randomUUID());
        mantaClient.put(path3, TEST_DATA);

        mantaClient.deleteRecursive(testPathPrefix + "1");

        boolean thrown = false;
        try {
            mantaClient.get(testPathPrefix + "1");
        } catch (final MantaClientHttpResponseException e) {
            Assert.assertEquals(404, e.getStatusCode());
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }


    @Test
    public final void testPutWithStream() throws IOException {
        final String name = UUID.randomUUID().toString();
        final String path = testPathPrefix + name;
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try (InputStream testDataInputStream = classLoader.getResourceAsStream(TEST_FILENAME)) {
            mantaClient.put(path, testDataInputStream);
        }
    }


    @Test
    public final void testHead() throws IOException {
        final String objectName = UUID.randomUUID().toString();
        final String path = testPathPrefix + objectName;

        mantaClient.put(path, TEST_DATA);
        final MantaObjectMetadata mantaObjectHead = mantaClient.head(testPathPrefix + objectName);
        Assert.assertNotNull(mantaObjectHead);
        Assert.assertNotNull(mantaObjectHead.getContentType());
        Assert.assertNotNull(mantaObjectHead.getContentLength());
        Assert.assertNotNull(mantaObjectHead.getEtag());
        Assert.assertNotNull(mantaObjectHead.getMtime());
        Assert.assertNotNull(mantaObjectHead.getPath());

        final String directoryName = UUID.randomUUID().toString();
        mantaClient.putDirectory(testPathPrefix + directoryName, null);
        final MantaObjectMetadata mantaDirectoryHead = mantaClient.head(testPathPrefix + directoryName);
        Assert.assertNotNull(mantaDirectoryHead);
        Assert.assertNotNull(mantaDirectoryHead.getContentType());
        Assert.assertNull(mantaDirectoryHead.getContentLength());
        Assert.assertNull(mantaDirectoryHead.getEtag());
        Assert.assertNotNull(mantaDirectoryHead.getMtime());
        Assert.assertNotNull(mantaDirectoryHead.getPath());

        final String linkName = UUID.randomUUID().toString();
        mantaClient.putSnapLink(testPathPrefix + linkName, testPathPrefix + objectName, null);
        final MantaObjectMetadata mantaLinkHead = mantaClient.head(testPathPrefix + linkName);
        Assert.assertNotNull(mantaLinkHead);
        Assert.assertNotNull(mantaLinkHead.getContentType());
        Assert.assertNotNull(mantaLinkHead.getContentLength());
        Assert.assertNotNull(mantaLinkHead.getEtag());
        Assert.assertNotNull(mantaLinkHead.getMtime());
        Assert.assertNotNull(mantaLinkHead.getPath());

        Assert.assertEquals(mantaObjectHead.getContentType(), mantaLinkHead.getContentType());
        Assert.assertEquals(mantaObjectHead.getContentLength(), mantaLinkHead.getContentLength());
        Assert.assertEquals(mantaObjectHead.getEtag(), mantaLinkHead.getEtag());
    }


    @Test
    public final void testPutLink() throws IOException {
        final String name = UUID.randomUUID().toString();
        final String path = testPathPrefix + name;
        mantaClient.put(path, TEST_DATA);

        final String link = UUID.randomUUID().toString();
        mantaClient.putSnapLink(testPathPrefix + link, testPathPrefix + name, null);
        final String linkContent = mantaClient.getAsString(testPathPrefix + link);
        Assert.assertEquals(linkContent, TEST_DATA);

    }


    @Test
    public final void testList() throws IOException {
        final String pathPrefix = String.format("%s/%s", testPathPrefix, UUID.randomUUID());
        mantaClient.putDirectory(pathPrefix, null);

        mantaClient.put(String.format("%s/%s", pathPrefix, UUID.randomUUID()), "");
        mantaClient.put(String.format("%s/%s", pathPrefix, UUID.randomUUID()), "");
        final String subDir = pathPrefix + "/" + UUID.randomUUID().toString();
        mantaClient.putDirectory(subDir, null);
        mantaClient.put(String.format("%s/%s", subDir, UUID.randomUUID()), "");
        final Collection<MantaObject> objs = mantaClient.listObjects(pathPrefix);
        for (final MantaObject mantaObject : objs) {
            Assert.assertTrue(mantaObject.getPath().startsWith(testPathPrefix));
        }
        Assert.assertEquals(3, objs.size());
    }


    @Test(expectedExceptions = MantaObjectException.class)
    public final void testListNotADir() throws IOException {
        final String name = UUID.randomUUID().toString();
        final String path = testPathPrefix + name;

        mantaClient.put(path, TEST_DATA);
        mantaClient.listObjects(path);
    }


    @Test
    public final void testRFC3986() throws IOException {
        final String name = "spaces in the name of the file";
        final String path = testPathPrefix + name;

        mantaClient.put(path, TEST_DATA);
        final String actual = mantaClient.getAsString(path);

        Assert.assertEquals(actual, TEST_DATA);
        mantaClient.delete(path);
        boolean thrown = false;
        try {
            mantaClient.get(testPathPrefix + name);
        } catch (final MantaClientHttpResponseException e) {
            Assert.assertEquals(404, e.getStatusCode());
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }


    @Test(groups = { "mtime" })
    public final void testGetLastModifiedDate() throws DateParseException {
        final String mtime = "Wed, 11 Nov 2015 18:20:20 GMT";
        final Date expected = DateUtils.parseDate(mtime);
        final MantaObjectMetadata obj = new MantaObjectMetadata(testPathPrefix);
        obj.setMtime(mtime);

        Assert.assertEquals(obj.getLastModifiedTime(), expected,
                "Last modified date should equal input to mtime");
    }


    @Test(groups = { "mtime" })
    public final void testGetNullLastModifiedDate() throws DateParseException {
        final MantaObjectMetadata obj = new MantaObjectMetadata(testPathPrefix);
        obj.setMtime(null);

        Assert.assertNull(obj.getLastModifiedTime(),
                "Last modified date should be null when mtime is null");
    }


    @Test(groups = { "mtime" })
    public final void testGetLastModifiedDateWithUnparseableMtime() throws DateParseException {
        final String mtime = "Bad unparseable string";
        final MantaObjectMetadata obj = new MantaObjectMetadata(testPathPrefix);
        obj.setMtime(mtime);

        Assert.assertNull(obj.getLastModifiedTime(),
                "Last modified date should be null when mtime is null");
    }


    @Test(groups = { "mtime" })
    public final void testSetLastModifiedDate() throws DateParseException {
        final String mtime = "Wed, 11 Nov 2015 18:20:20 GMT";
        final Date input = DateUtils.parseDate(mtime);
        final MantaObjectMetadata obj = new MantaObjectMetadata(testPathPrefix);

        obj.setLastModifiedTime(input);

        Assert.assertEquals(obj.getLastModifiedTime(), input,
                "Last modified date should equal input to mtime");
        Assert.assertEquals(obj.getMtime(), mtime,
                "Mtime should equal input to Last modified date");

    }


}

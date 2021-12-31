package de.qStivi.apis;

import org.json.JSONObject;
import org.junit.jupiter.api.RepeatedTest;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class YouTubeTest {

    JSONObject json = new JSONObject("""
              {"web-app": {
              "servlet": [  \s
                {
                  "servlet-name": "cofaxCDS",
                  "servlet-class": "org.cofax.cds.CDSServlet",
                  "init-param": {
                    "configGlossary:installationAt": "Philadelphia, PA",
                    "configGlossary:adminEmail": "ksm@pobox.com",
                    "configGlossary:poweredBy": "Cofax",
                    "configGlossary:poweredByIcon": "/images/cofax.gif",
                    "configGlossary:staticPath": "/content/static",
                    "templateProcessorClass": "org.cofax.WysiwygTemplate",
                    "templateLoaderClass": "org.cofax.FilesTemplateLoader",
                    "templatePath": "templates",
                    "templateOverridePath": "",
                    "defaultListTemplate": "listTemplate.htm",
                    "defaultFileTemplate": "articleTemplate.htm",
                    "useJSP": false,
                    "jspListTemplate": "listTemplate.jsp",
                    "jspFileTemplate": "articleTemplate.jsp",
                    "cachePackageTagsTrack": 200,
                    "cachePackageTagsStore": 200,
                    "cachePackageTagsRefresh": 60,
                    "cacheTemplatesTrack": 100,
                    "cacheTemplatesStore": 50,
                    "cacheTemplatesRefresh": 15,
                    "cachePagesTrack": 200,
                    "cachePagesStore": 100,
                    "cachePagesRefresh": 10,
                    "cachePagesDirtyRead": 10,
                    "searchEngineListTemplate": "forSearchEnginesList.htm",
                    "searchEngineFileTemplate": "forSearchEngines.htm",
                    "searchEngineRobotsDb": "WEB-INF/robots.db",
                    "useDataStore": true,
                    "dataStoreClass": "org.cofax.SqlDataStore",
                    "redirectionClass": "org.cofax.SqlRedirection",
                    "dataStoreName": "cofax",
                    "dataStoreDriver": "com.microsoft.jdbc.sqlserver.SQLServerDriver",
                    "dataStoreUrl": "jdbc:microsoft:sqlserver://LOCALHOST:1433;DatabaseName=goon",
                    "dataStoreUser": "sa",
                    "dataStorePassword": "dataStoreTestQuery",
                    "dataStoreTestQuery": "SET NOCOUNT ON;select test='test';",
                    "dataStoreLogFile": "/usr/local/tomcat/logs/datastore.log",
                    "dataStoreInitConns": 10,
                    "dataStoreMaxConns": 100,
                    "dataStoreConnUsageLimit": 100,
                    "dataStoreLogLevel": "debug",
                    "maxUrlLength": 500}},
                {
                  "servlet-name": "cofaxEmail",
                  "servlet-class": "org.cofax.cds.EmailServlet",
                  "init-param": {
                  "mailHost": "mail1",
                  "mailHostOverride": "mail2"}},
                {
                  "servlet-name": "cofaxAdmin",
                  "servlet-class": "org.cofax.cds.AdminServlet"},
            \s
                {
                  "servlet-name": "fileServlet",
                  "servlet-class": "org.cofax.cds.FileServlet"},
                {
                  "servlet-name": "cofaxTools",
                  "servlet-class": "org.cofax.cms.CofaxToolsServlet",
                  "init-param": {
                    "templatePath": "toolstemplates/",
                    "log": 1,
                    "logLocation": "/usr/local/tomcat/logs/CofaxTools.log",
                    "logMaxSize": "",
                    "dataLog": 1,
                    "dataLogLocation": "/usr/local/tomcat/logs/dataLog.log",
                    "dataLogMaxSize": "",
                    "removePageCache": "/content/admin/remove?cache=pages&id=",
                    "removeTemplateCache": "/content/admin/remove?cache=templates&id=",
                    "fileTransferFolder": "/usr/local/tomcat/webapps/content/fileTransferFolder",
                    "lookInContext": 1,
                    "adminGroupID": 4,
                    "betaServer": true}}],
              "servlet-mapping": {
                "cofaxCDS": "/",
                "cofaxEmail": "/cofaxutil/aemail/*",
                "cofaxAdmin": "/admin/*",
                "fileServlet": "/static/*",
                "cofaxTools": "/tools/*"},
            \s
              "taglib": {
                "taglib-uri": "cofax.tld",
                "taglib-location": "/WEB-INF/tlds/cofax.tld"}}}""");

    //region getVideoIdBySearchQuery() tests
    @RepeatedTest(10)
    void getVideoIdBySearchQuery() {
        assertEquals("KHIzQ_NobAk", YouTube.getVideoIdBySearchQuery("qstivi dev"));
    }

    @RepeatedTest(10)
    void getVideoIdBySearchQueryEmpty() {
        assertNull(YouTube.getVideoIdBySearchQuery(""));
    }

    @RepeatedTest(10)
    void getVideoIdBySearchQueryNull() {
        assertNull(YouTube.getVideoIdBySearchQuery(null));
    }

    @RepeatedTest(10)
    void getVideoIdBySearchQuerySpace() {
        assertNull(YouTube.getVideoIdBySearchQuery(" "));
    }
    //endregion

    //region getPlaylistItemsByLink() tests
    @RepeatedTest(10)
    void getPlaylistItemsByLinkEmptyList() {
        var items = YouTube.getPlaylistItemsByLink("https://www.youtube.com/playlist?list=PLPoS_0I9SFONyANDxfvk-HASIUpTw2eqM");
        assertNull(items);
    }

    @RepeatedTest(10)
    void getPlaylistItemsByLinkOneItem() {
        var items = YouTube.getPlaylistItemsByLink("https://www.youtube.com/playlist?list=PLPoS_0I9SFOP9S6-XBphzT43ZSCEwmKDX");
        assertNotNull(items);
        assertEquals("KHIzQ_NobAk", items.get(0));
        assertThrowsExactly(IndexOutOfBoundsException.class, () -> System.out.println(items.get(1)));
    }

    @RepeatedTest(10)
    void getPlaylistItemsByLinkThreeItems() {
        var items = YouTube.getPlaylistItemsByLink("https://www.youtube.com/playlist?list=PLPoS_0I9SFOML7F4yoEflduzK65Xzqfd-");
        assertNotNull(items);
        assertEquals("KHIzQ_NobAk", items.get(0));
        assertEquals("9bZkp7q19f0", items.get(1));
        assertEquals("4hpEnLtqUDg", items.get(2));
        assertThrowsExactly(IndexOutOfBoundsException.class, () -> System.out.println(items.get(3)));
    }

    @RepeatedTest(10)
    void getPlaylistItemsByLinkPrivateList() {
        var items = YouTube.getPlaylistItemsByLink("https://www.youtube.com/playlist?list=PLPoS_0I9SFOP8lzaAf9JsOkntEJZSOZCP");
        assertNull(items);
    }

    @RepeatedTest(10)
    void getPlaylistItemsByLinkEmptyLink() {
        var items = YouTube.getPlaylistItemsByLink("");
        assertNull(items);
    }

    @RepeatedTest(10)
    void getPlaylistItemsByLinkNullLink() {
        var items = YouTube.getPlaylistItemsByLink(null);
        assertNull(items);
    }

    @RepeatedTest(10)
    void getPlaylistItemsByLinkDoesNotExistLink() {
        var items = YouTube.getPlaylistItemsByLink("https://www.youtube.com/playlist?list=yee");
        assertNull(items);
    }
    //endregion

    //region readJsonFromURL() test
    @RepeatedTest(10)
    void readJsonFromUrl() {
        assertEquals(json.toString(), Objects.requireNonNull(YouTube.readJsonFromUrl("https://qstivi.de/json.json")).toString());
    }
    //endregion
}
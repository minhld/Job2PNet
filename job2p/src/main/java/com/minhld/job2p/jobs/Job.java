package com.minhld.job2p.jobs;

import com.minhld.job2p.com.minhld.extra.WebPart;
import com.minhld.job2p.supports.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by minhld on 1/6/2015.
 */
public class Job {
    /**
     * this function will partially load a web page and copy all the resources to
     * the local depending on its index and number of parts, the piece of data it
     * copies will be different
     *
     * @param input
     * @return
     */
    public Object exec(Object input) {
        WebPart webPart = (WebPart) input;
        byte[] result = new byte[0];

        // create a zip output
        String anyFileName = UUID.randomUUID().toString() + ".zip";
        String anyPath = Utils.getDownloadPath() + "/" + anyFileName;
        ZipOutputStream zipOut = null;

        try {
            zipOut = new ZipOutputStream(new FileOutputStream(anyPath));

            HashMap<String, String> links = updateLinks(webPart.url);

            if (webPart.index == 0) {
                writeZip(zipOut, "index.html", links.get("html"));
            }
            links.remove("html");

            int linksSize = links.size();
            int step = webPart.numOfParts == 1 ? linksSize : linksSize / webPart.numOfParts + 1;
            int offs = step * webPart.index;

            int cnt = 0;
            for (String lnkKey : links.keySet()) {
                if (cnt < offs) {
                    cnt++;
                    continue;
                }

                if (cnt < offs + step) {
                    // download the link
                    try {
                        writeZip(zipOut, lnkKey, new URL(links.get(lnkKey)).openStream());
                    } catch(Exception e) {
                        // skip one resource, no problem
                    }
                    cnt++;
                }else {
                    break;
                }
            }

            zipOut.flush();
            zipOut.close();

            result = Utils.readFile(new File(anyPath));

        } catch (IOException e) {
            e.printStackTrace();

        }

        return result;
    }

    /**
     * this function loads a web page and return data & all the related links
     * using within that page including css, js, images & other multimedia
     * resources
     *
     * @param url
     * @return
     * @throws IOException
     */
    private HashMap<String, String> updateLinks(String url) throws IOException {
        HashMap<String, String> links = new HashMap<>();

        Document htmlDoc = Jsoup.connect(url).get();
        Elements elements = htmlDoc.getAllElements();

        String orgSrcUrl = "", srcName = "";
        for (Element e : elements) {
            //srcUrl = getSrc(e);
            if (e.attr("href") != null && !e.attr("href").isEmpty() && !e.tagName().equalsIgnoreCase("a")) {
                orgSrcUrl = e.attr("href");
                srcName = getFilename(orgSrcUrl);
                links.put(srcName, orgSrcUrl);
                htmlDoc.getElementsByAttributeValue("href", orgSrcUrl).attr("href", srcName);
            }

            if (e.attr("src") != null && !e.attr("src").isEmpty()) {
                orgSrcUrl = e.attr("src");
                srcName = getFilename(orgSrcUrl);
                links.put(srcName, orgSrcUrl);
                htmlDoc.getElementsByAttributeValue("src", orgSrcUrl).attr("src", srcName);
            }

        }

        String htmlDocStr = htmlDoc.html();
        links.put("html", htmlDocStr);

        return links;
    }

    /**
     * write out (string) data to a zip entry
     *
     * @param zipOut
     * @param name
     * @param data
     * @throws IOException
     */
    private void writeZip(ZipOutputStream zipOut, String name, String data) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zipOut.putNextEntry(entry);
        BufferedWriter zipWriter = new BufferedWriter(new OutputStreamWriter(
                zipOut, Charset.forName("utf-8")));
        zipWriter.write(data);
        zipWriter.flush();

        zipOut.closeEntry();
    }

    /**
     * write out binary data to a zip entry
     * @param zipOut
     * @param name
     * @param in
     * @throws IOException
     */
    private void writeZip(ZipOutputStream zipOut, String name, InputStream in) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zipOut.putNextEntry(entry);

        byte[] buffer = new byte[2048];
        int length = 0;
        while((length = in.read(buffer)) > 0) {
            zipOut.write(buffer, 0, length);
        }

        zipOut.closeEntry();
    }

    /**
     * only get the file name of an URL
     *
     * @param src
     * @return
     */
    private String getFilename(String src) {
        return new File(src).getName();
    }
}

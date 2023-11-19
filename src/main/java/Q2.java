import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;

public class Q2 {
    // Set to store visited URLs
    private Set<String> visitedUrls = new HashSet<>();

    // Graph to visualize the web crawl
    private Graph graph = new MultiGraph("Web Crawler Graph");
    private long startTime;

    public Q2() {
        this.startTime = System.currentTimeMillis();
        graph.addAttribute("ui.stylesheet",
                        "node.seed { fill-color: red; text-mode: truncated; text-color: red; }");
    }

    // Method to crawl the website starting from a seed URL and up to a certain depth
    // maxLinks limits the number of links to follow on each page
    public void crawl(String seedUrl, int depth, int maxLinks) {
        if (depth == 0 || visitedUrls.contains(seedUrl)) {
            System.out.println("Stopped crawling at: " + seedUrl + " due to depth: " + depth);
            return;
        }

        try {
            URL url = new URL(seedUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setFollowRedirects(true);  // IMPORTANT!!!!! Ensure redirects are followed

            int status = connection.getResponseCode();
            if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER) {
                // It's a redirect
                String newUrl = connection.getHeaderField("Location");
                crawl(newUrl, depth, maxLinks);  // Recursively call crawl with the new URL
            } else {
                Document doc = Jsoup.parse(connection.getInputStream(), null, seedUrl);
                visitedUrls.add(seedUrl);

                System.out.println("Visited URL: " + seedUrl);

                Elements links = doc.select("a[href]");
                System.out.println("Found " + links.size() + " links on: " + seedUrl);

                int linkCount = 0;

                for (Element link : links) {
                    if(linkCount >= maxLinks) {
                        System.out.println("Max links reached for: " + seedUrl);
                        break;
                    }

                    String absUrl = link.attr("abs:href");

                    if (isValidUrl(seedUrl, absUrl)) {
                        System.out.println("Valid link found: " + absUrl);

                        // Add the absUrl as a node if it is not already in the graph
                        if(graph.getNode(absUrl) != null || visitedUrls.contains(absUrl)) {
                            System.out.println("Link already crawled: " + absUrl);
                            continue;
                        }
                        Node node = graph.addNode(absUrl);
                        node.addAttribute("ui.label", absUrl);

                        // Create a unique edge ID based on the source and destination URLs
                        String edgeId = seedUrl + " -> " + absUrl;

                        // Only add the edge if it does not already exist
                        if (graph.getEdge(edgeId) == null) {
                            graph.addEdge(edgeId, seedUrl, absUrl, true);
                        }

                        crawl(absUrl, depth - 1, maxLinks);

                        linkCount++;
                    } else {
                        System.out.println("Invalid link found: " + absUrl);
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("IOException occurred at URL: " + seedUrl);
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception occurred at URL: " + seedUrl);
            e.printStackTrace();
        }
    }

    // Method to check if a URL is valid
    private boolean isValidUrl(String seedUrl, String absUrl) {
        if (absUrl.equals(seedUrl)) {
            return false;
        }
        String seedDomain = getDomainName(seedUrl);
        String absDomain = getDomainName(absUrl);
        if (seedDomain.equals(absDomain)) {
            return false;
        }
        String[] seedSubDomains = seedDomain.split("\\.");
        String[] absSubDomains = absDomain.split("\\.");
        int commonSubDomains = 0;
        for (int i = 0; i < Math.min(seedSubDomains.length, absSubDomains.length); i++) {
            if (seedSubDomains[seedSubDomains.length - i - 1].equals(absSubDomains[absSubDomains.length - i - 1])) {
                commonSubDomains++;
            } else {
                break;
            }
        }
        if (commonSubDomains > 2) {
            return false;
        }
        return true;
    }

    private String getDomainName(String url) {
        try {
            URL netUrl = new URL(url);
            return netUrl.getHost();
        } catch (MalformedURLException e) {
            return "";
        }
    }

    private void printStatistics() {
        System.out.println("Number of nodes: " + graph.getNodeCount());
        System.out.println("Number of edges: " + graph.getEdgeCount());

        int maxInDegree = 0;
        Node maxInDegreeNode = null;
        for (Node node : graph) {
            int inDegree = node.getInDegree();
            if (inDegree > maxInDegree) {
                maxInDegree = inDegree;
                maxInDegreeNode = node;
            }
        }

        System.out.println("Node with maximum in-degree: " + maxInDegreeNode.getId());
        System.out.println("Maximum in-degree: " + maxInDegree);

        double totalTime = (System.currentTimeMillis() - startTime) / 1000.0;
        System.out.println("Total time taken: " + totalTime + " seconds");
    }

    // Main method
    public static void main(String[] args) throws IOException {
        // Set system property for GraphStream
        System.setProperty("org.graphstream.ui", "swing");

        // Instantiate the web crawler
        Q2 crawler = new Q2();

        // Add the seed URLs as nodes
        String[] seedUrls = {
                "https://www.tongji.edu.cn",
                "https://www.pku.edu.cn",
                "https://www.sina.com.cn",
                "https://www.mit.edu"};
        for (String url : seedUrls) {
            Node node = crawler.graph.getNode(url);
            if(node == null) {
                node = crawler.graph.addNode(url);
            }
            node.addAttribute("ui.class", "seed");
            node.addAttribute("ui.label", url);
        }

        // Start crawling from four different seed URLs
        for (String url : seedUrls) {
            crawler.crawl(url, 3, 6);
        }

        // Display the resulting graph
        crawler.printStatistics();
        crawler.graph.display();
    }
}
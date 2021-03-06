package io.bloc.android.blocly.api.network;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Mike on 8/20/2015.
 */
public class GetFeedsNetworkRequest extends NetworkRequest<List<GetFeedsNetworkRequest.FeedResponse>> {
    public static final int ERROR_PARSING = 3;
    private static final String XML_TAG_TITLE = "title";
    private static final String XML_TAG_DESCRIPTION = "description";
    private static final String XML_TAG_LINK = "link";
    private static final String XML_TAG_ITEM = "item";
    private static final String XML_TAG_PUB_DATE = "pubDate";
    private static final String XML_TAG_GUID = "guid";
    private static final String XML_TAG_ENCLOSURE = "enclosure";
    private static final String XML_TAG_CONTENT_ENCODED = "content:encoded";
    private static final String XML_TAG_MEDIA_CONTENT = "media:content";
    private static final String XML_ATTRIBUTE_URL = "url";
    private static final String XML_ATTRIBUTE_TYPE = "type";
    //#51 Added string references for the tags and attributes.
    //#57 Added in the media:content and content:encoded tags.

    String[] feedUrls;

    public GetFeedsNetworkRequest(String... feedUrls) {
        this.feedUrls = feedUrls;
    }

    @Override
    public List<FeedResponse> performRequest() {
        List<FeedResponse> responseFeeds = new ArrayList<FeedResponse>(feedUrls.length);
        for (String feedUrlString : feedUrls) {
            InputStream inputStream = openStream(feedUrlString);
            if (inputStream == null) {
                return null;
            }
            try {
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document xmlDocument = documentBuilder.parse(inputStream);
                //#51 the XMLdocument is put into the Document object
                String channelTitle = optFirstTagFromDocument(xmlDocument, XML_TAG_TITLE);
                String channelDescription = optFirstTagFromDocument(xmlDocument, XML_TAG_DESCRIPTION);
                String channelURL = optFirstTagFromDocument(xmlDocument, XML_TAG_LINK);
                //#51 The method recovers the tag title, description and the link.
                NodeList allItemNodes = xmlDocument.getElementsByTagName(XML_TAG_ITEM);
                List<ItemResponse> responseItems = new ArrayList<ItemResponse>(allItemNodes.getLength());
                for (int itemIndex = 0; itemIndex < allItemNodes.getLength(); itemIndex++) {
                    String itemURL = null;
                    String itemTitle = null;
                    String itemImageURL = null;
                    String itemContentEncodedText = null;
                    String itemMediaURL = null;
                    String itemMediaMIMEType = null;
                    String itemDescription = null;
                    String itemGUID = null;
                    String itemPubDate = null;
                    String itemEnclosureURL = null;
                    String itemEnclosureMIMEType = null;
                    Node itemNode = allItemNodes.item(itemIndex);
                    NodeList tagNodes = itemNode.getChildNodes();
                    for (int tagIndex = 0; tagIndex < tagNodes.getLength(); tagIndex++) {
                        Node tagNode = tagNodes.item(tagIndex);
                        String tag = tagNode.getNodeName();
                        if (XML_TAG_LINK.equalsIgnoreCase(tag)) {
                            itemURL = tagNode.getTextContent();
                        } else if (XML_TAG_TITLE.equalsIgnoreCase(tag)) {
                            itemTitle = tagNode.getTextContent();
                        } else if (XML_TAG_DESCRIPTION.equalsIgnoreCase(tag)) {
                            String descriptionText = tagNode.getTextContent();
                            itemImageURL = parseImageFromHTML(descriptionText);
                            itemDescription = parseTextFromHTML(descriptionText);
                        } else if (XML_TAG_ENCLOSURE.equalsIgnoreCase(tag)) {
                            NamedNodeMap enclosureAttributes = tagNode.getAttributes();
                            itemEnclosureURL = enclosureAttributes.getNamedItem(XML_ATTRIBUTE_URL).getTextContent();
                            itemEnclosureMIMEType = enclosureAttributes.getNamedItem(XML_ATTRIBUTE_TYPE).getTextContent();
                        } else if (XML_TAG_PUB_DATE.equalsIgnoreCase(tag)) {
                            itemPubDate = tagNode.getTextContent();
                        } else if (XML_TAG_GUID.equalsIgnoreCase(tag)) {
                            itemGUID = tagNode.getTextContent();

                        } else if (XML_TAG_CONTENT_ENCODED.equalsIgnoreCase(tag)){
                            String contentEncoded = tagNode.getTextContent();
                            itemImageURL = parseImageFromHTML(contentEncoded);
                            itemContentEncodedText = parseTextFromHTML(contentEncoded);
                        } else if(XML_TAG_MEDIA_CONTENT.equalsIgnoreCase(tag)){
                            NamedNodeMap mediaAttributes = tagNode.getAttributes();
                            itemMediaURL = mediaAttributes.getNamedItem(XML_ATTRIBUTE_URL).getTextContent();
                            itemMediaMIMEType = mediaAttributes.getNamedItem(XML_ATTRIBUTE_TYPE).getTextContent();
                        }
                    }

                    if(itemEnclosureURL == null){
                        itemEnclosureURL = itemImageURL;
                    }

                    if (itemEnclosureURL == null){
                        itemEnclosureURL = itemImageURL;
                        itemEnclosureMIMEType = itemMediaMIMEType;
                    }

                    if (itemContentEncodedText != null){
                        itemDescription = itemContentEncodedText;
                    }
                    /*#57itemImageURL will hold the first image if there's one from HTML. parseTextFromHTML is used
                     *to take out HTML tags and attrs, leaving just the text. Parsed the text and image of content:encoded
                     *This catches the case where an image was not taken out of description or content. Assigned values within
                     * the media:content tag. Placed the description with the content encoded text.
                     */
                    responseItems.add(new ItemResponse(itemURL, itemTitle, itemDescription,
                            itemGUID, itemPubDate, itemEnclosureURL, itemEnclosureMIMEType));
                    /*#51 getElementsByTagName recovers a NodeList for the item tags in the feed. Set up variables
                     * for each field to recover. Parsed the item tags, recover the Node respresenting the Rss item
                      * and extract the list of child Nodes. All the tags to parse are added.
                      * Add a map of all attributes and recover the url and type of entries. */
                }
                responseFeeds.add(new FeedResponse(feedUrlString, channelTitle, channelURL, channelDescription, responseItems));
                inputStream.close();
                //#51 added the items to the responseFeed and closed the inputStream
            } catch (IOException e) {
                e.printStackTrace();
                setErrorCode(ERROR_IO);
                return null;
            } catch (SAXException e) {
                e.printStackTrace();
                setErrorCode(ERROR_PARSING);
                return null;
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
                setErrorCode(ERROR_PARSING);
                return null;
            }
            //#51 error codes added related to parsing
        }
        return responseFeeds;
        //#51 Created the variable that performRequest returns and added in the return
    }
    private String optFirstTagFromDocument(Document document, String tagName) {
        NodeList elementsByTagName = document.getElementsByTagName(tagName);
        if (elementsByTagName.getLength() > 0) {
            return elementsByTagName.item(0).getTextContent();
        }
        return null;
    }

    static String parseTextFromHTML(String htmlString) {
        org.jsoup.nodes.Document document = Jsoup.parse(htmlString);
        return document.body().text();
    }
    static String parseImageFromHTML(String htmlString) {
        org.jsoup.nodes.Document document = Jsoup.parse(htmlString);
        Elements imgElements = document.select("img");
        if (imgElements.isEmpty()) {
            return null;
        }
        return imgElements.attr("src");
    }
    /*#57 Incorporating the jSoup methods. HTML has everything removed but the remaining text.
     *parseImageFromHTML takes out the image from the HTML, then returns the source of the first image.
     */
    //#51 Created a method to recover the tags from a Document
    public static class FeedResponse {
        public final String channelFeedURL;
        public final String channelTitle;
        public final String channelURL;
        public final String channelDescription;
        public final List<ItemResponse> channelItems;

        FeedResponse(String channelFeedURL, String channelTitle, String channelURL,
                     String channelDescription, List<ItemResponse> channelItems) {
            this.channelFeedURL = channelFeedURL;
            this.channelTitle = channelTitle;
            this.channelURL = channelURL;
            this.channelDescription = channelDescription;
            this.channelItems = channelItems;
        }
    }

    public static class ItemResponse {
        public final String itemURL;
        public final String itemTitle;
        public final String itemDescription;
        public final String itemGUID;
        public final String itemPubDate;
        public final String itemEnclosureURL;
        public final String itemEnclosureMIMEType;

        ItemResponse(String itemURL, String itemTitle, String itemDescription,
                     String itemGUID, String itemPubDate, String itemEnclosureURL,
                     String itemEnclosureMIMEType) {
            this.itemURL = itemURL;
            this.itemTitle = itemTitle;
            this.itemDescription = itemDescription;
            this.itemGUID = itemGUID;
            this.itemPubDate = itemPubDate;
            this.itemEnclosureURL = itemEnclosureURL;
            this.itemEnclosureMIMEType = itemEnclosureMIMEType;
        }
    }
}
    /*#50 the multiple feeds will be stored in the new field declared. Override the abstract method declared
     * inside NetworkRequest. The BufferedReader is added for the URL's inside feedUrls. The lines are able
     * to be read one line at a time. The reader is closed at the end along with it's stream
     */
    /*#51 Added in FeedResponse which consists of the channels, the feed URL and ItemResponse objects. */


package com.zanol.rssreader;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

public class ParseApplications {

    private static final String TAG = "ParseApplications";
    private ArrayList<FeedEntry> applications;

    public ParseApplications() {
        applications = new ArrayList<>();
    }

    public ArrayList<FeedEntry> getApplications() {
        return applications;
    }

    public boolean parse(String xmlText) {
        boolean status = true;
        FeedEntry entry = null;
        boolean isEntry = false;
        String textValue = "";

        XmlPullParserFactory factory;

        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser pullParser = factory.newPullParser();
            pullParser.setInput(new StringReader(xmlText));

            int eventType = pullParser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tag = pullParser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        Log.d(TAG, "parse: Começando a tag: " + tag);
                        if (tag.equalsIgnoreCase("entry")) {
                            isEntry = true;
                            entry = new FeedEntry();
                        }
                        break;
                    case XmlPullParser.TEXT:
                        textValue = pullParser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        Log.d(TAG, "parse: Terminando a tag: " + tag);
                        if (isEntry) {
                            if (tag.equalsIgnoreCase("entry")) {
                                applications.add(entry);
                                isEntry= false;
                            } else if (tag.equalsIgnoreCase("name")) {
                                entry.setName(textValue);
                            } else if (tag.equalsIgnoreCase("artist")) {
                                entry.setArtist(textValue);
                            } else if (tag.equalsIgnoreCase("summary")) {
                                entry.setSummary(textValue);
                            } else if (tag.equalsIgnoreCase("image")) {
                                entry.setImgURL(textValue);
                            } else if (tag.equalsIgnoreCase("releaseDate")) {
                                entry.setReleaseDate(textValue);
                            }
                        }
                        break;
                }

                eventType = pullParser.next();
            }

            for (FeedEntry feedEntry : applications) {
                Log.d(TAG, "parse: ****************************");
                Log.d(TAG, feedEntry.toString());
                Log.d(TAG, "parse: ****************************");
            }
        } catch (Exception e) {
            Log.e(TAG, "parse: Erro ao fazer parse:" + e.getLocalizedMessage());
            status = false;
        }

        return status;
    }
}
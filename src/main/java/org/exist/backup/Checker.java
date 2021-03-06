/*
 * Copyright (C) 2015 The Animo Project
 * http://animotron.org
 *
 * This file is part of Outrunner.
 *
 * Outrunner is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.exist.backup;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class Checker {

    static SAXParserFactory factory = SAXParserFactory.newInstance();
    static DefaultHandler handler = new DefaultHandler();

    static class MetadataHandler extends  DefaultHandler {

        Path location;

        MetadataHandler(Path location) {
            this.location = location;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if ("resource".equals(qName)) {
                String filename = attributes.getValue("filename");
                if (filename == null) {
                    System.out.println("'resource' without 'filename' at " + location);

                } else {
                    Path file = location.resolve(filename);

                    String type = attributes.getValue("type");
                    if ("XMLResource".equals(type)) {
                        checkXml(file);
                    } else {
                        if (!Files.isRegularFile(file)) {
                            System.out.println(file+" is missing.");
                        }
                    }
                }
            } else if ("subcollection".equals(qName)) {
                String filename = attributes.getValue("filename");
                if (filename != null) {
                    Path folder = location.resolve(filename);
                    if (Files.isDirectory(folder)) {
                        Path metaFile = folder.resolve("__contents__.xml");
                        if (!Files.isRegularFile(metaFile)) {
                            System.out.println("'subcollection' "+folder+" have no metadata.");
                        }
                    } else {
                        System.out.println("'subcollection' "+folder+" does not exist, but present at metadata.");
                    }
                } else {
                    System.out.println("'subcollection' without 'filename' at "+location);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        //do not do DTD validation
        factory.setValidating(false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        Checker.check(Paths.get(args[0]));
    }

    private static void checkXml(Path file) {
        SAXParser saxParser;
        try {
            saxParser = factory.newSAXParser();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try (InputStream in = Files.newInputStream(file)) {
            saxParser.parse(in, handler);
        } catch (Exception e) {
            System.out.println(file.toString()+" "+e.getMessage());
        }
    }

    private static void checkContents(Path file) {
        SAXParser saxParser;
        try {
            saxParser = factory.newSAXParser();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try (InputStream in = Files.newInputStream(file)) {
            saxParser.parse(in, new MetadataHandler(file.getParent()));
        } catch (Exception e) {
            System.out.println(file.toString()+" "+e.getMessage());
        }
    }

    private static void check(final Path path) throws Exception {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
                if (attr.isRegularFile()) {
                    if (file.getFileName().toString().equals("__contents__.xml")) {
                        checkContents(file);
                    }
                } else if (attr.isSymbolicLink()) {
                } else {
                }

                return CONTINUE;
            }
        });
    }
}

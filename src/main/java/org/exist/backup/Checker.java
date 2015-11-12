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

    public static void main(String[] args) throws Exception {
        Checker.check(Paths.get(args[0]));
    }

    private static void checkContents(Path file) {
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

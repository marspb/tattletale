/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.tattletale.reporting;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.Location;
import org.jboss.tattletale.core.NestableArchive;

/**
 * Eliminate JAR files with multiple versions
 *
 * @author Jesper Pedersen <jesper.pedersen@jboss.org>
 * @author <a href="mailto:torben.jaeger@jit-consulting.de">Torben Jaeger</a>
 */
public class EliminateJarsReport extends AbstractReport {
	/** NAME */
	private static final String NAME = "Eliminate Jar files with different versions";

	/** DIRECTORY */
	private static final String DIRECTORY = "eliminatejars";

	/** Constructor */
	public EliminateJarsReport() {
		super(DIRECTORY, ReportSeverity.WARNING, NAME, DIRECTORY);
	}

	/**
	 * write out the report's content
	 *
	 * @param bw
	 *            the writer to use
	 * @throws IOException
	 *             if an error occurs
	 */
	public void writeHtmlBodyContent(BufferedWriter bw) throws IOException {
		bw.write("<table>" + Dump.newLine());

		bw.write("  <tr>" + Dump.newLine());
		bw.write("     <th>Archive</th>" + Dump.newLine());
		bw.write("     <th>Location</th>" + Dump.newLine());
		bw.write("  </tr>" + Dump.newLine());

		Set<Archive> includingSubarchives = new TreeSet<Archive>(archives);

		for (Archive archive : archives) {
			if (archive instanceof NestableArchive) {
				for (Archive nested : ((NestableArchive) archive).getSubArchives()) {
					if (!includingSubarchives.contains(nested)) {
						includingSubarchives.add(nested);
					}
				}
			}
		}

		boolean odd = true;

		for (Archive archive : includingSubarchives) {
			String archiveName = archive.getName();
			int finalDot = archiveName.lastIndexOf(".");
			String extension = archiveName.substring(finalDot + 1);

			SortedSet<Location> locations = archive.getLocations();
			Iterator<Location> lit = locations.iterator();

			Location location = lit.next();

			boolean include = false;
			String version = location.getVersion();
			boolean filtered = isFiltered(archive.getName());

			while (!include && lit.hasNext()) {
				location = lit.next();

				// noinspection StringEquality
				if (version == location.getVersion() || (version != null && version.equals(location.getVersion()))) {
					// Same version identifier - just continue
				} else {
					include = true;

					if (!filtered) {
						status = ReportStatus.RED;
					}
				}
			}

			if (include) {
				if (odd) {
					bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
				} else {
					bw.write("  <tr class=\"roweven\">" + Dump.newLine());
				}
				bw.write("     <td><a href=\"../" + extension + "/" + archiveName + ".html\">" + archiveName
						+ "</a></td>" + Dump.newLine());
				bw.write("     <td>");

				bw.write("       <table>" + Dump.newLine());

				lit = locations.iterator();
				while (lit.hasNext()) {
					location = lit.next();

					bw.write("      <tr>" + Dump.newLine());

					bw.write("        <td>" + location.getFilename() + "</td>" + Dump.newLine());
					if (!filtered) {
						bw.write("        <td>");
					} else {
						bw.write("        <td style=\"text-decoration: line-through;\">");
					}
					if (location.getVersion() != null) {
						bw.write(location.getVersion());
					} else {
						bw.write("<i>Not listed</i>");
					}
					bw.write("</td>" + Dump.newLine());

					bw.write("      </tr>" + Dump.newLine());
				}

				bw.write("       </table>" + Dump.newLine());

				bw.write("</td>" + Dump.newLine());
				bw.write("  </tr>" + Dump.newLine());

				odd = !odd;
			}

		}

		bw.write("</table>" + Dump.newLine());
	}

	/**
	 * write out the header of the report's content
	 *
	 * @param bw
	 *            the writer to use
	 * @throws IOException
	 *             if an errror occurs
	 */
	public void writeHtmlBodyHeader(BufferedWriter bw) throws IOException {
		bw.write("<body>" + Dump.newLine());
		bw.write(Dump.newLine());

		bw.write("<h1>" + NAME + "</h1>" + Dump.newLine());

		bw.write("<a href=\"../index.html\">Main</a>" + Dump.newLine());
		bw.write("<p>" + Dump.newLine());
	}

	/**
	 * Create filter
	 *
	 * @return The filter
	 */
	@Override
	protected Filter createFilter() {
		return new KeyFilter();
	}
}

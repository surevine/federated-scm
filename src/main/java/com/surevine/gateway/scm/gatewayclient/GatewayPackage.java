/*
 * Copyright (C) 2008-2014 Surevine Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package com.surevine.gateway.scm.gatewayclient;

import com.surevine.gateway.scm.util.PropertyUtil;
import com.surevine.gateway.scm.util.StringUtil;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

/**
 * @author nick.leaver@surevine.com
 */
public class GatewayPackage {
    private Path bundlePath;
    private Map<String, String> metadata;
    private Path archivePath;
    private boolean archiveWritten;
    
    public GatewayPackage(final Path bundlePath, final Map<String, String> metadata) {
        this.bundlePath = bundlePath;
        this.metadata = metadata;
    }
    
    public Path getArchive() {
        return archivePath;
    }
    
    public String getDerivedFilename() {
        StringBuilder sb = new StringBuilder();
        sb.append("scm_")
                .append(metadata.get(MetadataUtil.KEY_ORGANISATION))
                .append("_").append(metadata.get(MetadataUtil.KEY_PROJECT))
                .append("_").append(metadata.get(MetadataUtil.KEY_REPO));
        return StringUtil.cleanStringForFilePath(sb.toString()) + ".tar.gz";
    }
    
    public void createArchive() throws IOException, ArchiveException, CompressorException {
        if (!archiveWritten) {
            String uuid = UUID.randomUUID().toString();
            archivePath = Paths.get(PropertyUtil.getTempDir(), uuid + ".tar.gz");
            Path metadataPath = Paths.get(PropertyUtil.getTempDir(), ".metadata.json");
            Path tarPath = Paths.get(PropertyUtil.getTempDir(), uuid + ".tar");

            writeMetadata(metadataPath);
            createTar(tarPath, metadataPath, bundlePath);
            writeGZ(archivePath, tarPath);

            // remove everything except the tar.gz
            Files.deleteIfExists(metadataPath);
            Files.deleteIfExists(tarPath);
            Files.deleteIfExists(bundlePath);

            archiveWritten = true;
        }
    }
    
    void writeGZ(final Path gzPath, final Path tarPath) throws IOException, CompressorException {
        CompressorOutputStream cos = new CompressorStreamFactory()
                .createCompressorOutputStream("gz", Files.newOutputStream(gzPath));

        try {
            Files.copy(tarPath, cos);
        } finally {
            cos.close();
        }
    }
    
    void createTar(final Path tarPath, final Path ... paths) throws IOException, ArchiveException {
        ArchiveOutputStream os = new ArchiveStreamFactory()
                .createArchiveOutputStream("tar", Files.newOutputStream(tarPath));
        try {
            for (Path path:paths) {
                TarArchiveEntry entry = new TarArchiveEntry(path.toFile());
                entry.setName(path.getFileName().toString());
                os.putArchiveEntry(entry);
                Files.copy(path, os);
                os.closeArchiveEntry();
            }
        } finally {
            os.close();
        }
    }
        
    void writeMetadata(final Path metadataPath) throws IOException {
        JSONObject metadataJSON = new JSONObject(metadata);
        Files.write(metadataPath, metadataJSON.toString().getBytes(Charset.forName("UTF-8")));
    }
}

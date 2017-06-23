package org.opentripplanner.util;

import com.google.common.io.Files;

import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

// TODO Extract this to utility package?
public class ZipUtils implements StreamingOutput {
    private File directory;

    public ZipUtils(File directory) {
        this.directory = directory;
    }

    @Override
    public void write(OutputStream outStream) throws IOException {
        ZipOutputStream zip = new ZipOutputStream(outStream);
        for (File f : directory.listFiles()) {
            zip.putNextEntry(new ZipEntry(f.getName()));
            Files.copy(f, zip);
            zip.closeEntry();
            zip.flush();
        }
        zip.close();
    }
}

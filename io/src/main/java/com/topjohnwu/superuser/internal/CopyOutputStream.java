/*
 * Copyright 2021 John "topjohnwu" Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.topjohnwu.superuser.internal;

import android.content.Context;

import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.io.SuFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

class CopyOutputStream extends BaseSuOutputStream {

    private final File tmp;
    private final SuFile outFile;

    CopyOutputStream(SuFile file, boolean append) throws FileNotFoundException {
        super(file, append);
        outFile = file;

        Context c = Utils.getDeContext(Utils.getContext());
        try {
            tmp = File.createTempFile("output", null, c.getCacheDir());
            tmp.deleteOnExit();
        } catch (IOException e) {
            throw new FileNotFoundException("Cannot create cache file");
        }

        out = new FileOutputStream(tmp);
    }

    @Override
    public void close() throws IOException {
        try {
            out.flush();
            out.close();
            if (!Shell.su("cat " + tmp + op() + outFile).to(null).exec().isSuccess())
                throw new IOException("Cannot write to target file");
        } finally {
            tmp.delete();
        }
    }
}

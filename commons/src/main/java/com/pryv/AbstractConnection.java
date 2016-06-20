package com.pryv;

import com.pryv.model.Stream;

import org.joda.time.DateTime;

import java.util.Map;

public abstract class AbstractConnection {
    public abstract DateTime serverTimeInSystemDate(double time);

    public abstract String generateCacheFolderName();

    public abstract Map<String, Stream> getRootStreams();

    public abstract boolean isCacheActive();

    public abstract void updateRootStreams(Map<String, Stream> streams);
}

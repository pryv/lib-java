package com.pryv;

import com.pryv.model.Stream;

import org.joda.time.DateTime;

import java.util.Map;

public interface AbstractConnection {
    DateTime serverTimeInSystemDate(double time);

    String generateCacheFolderName();

    Map<String, Stream> getRootStreams();

    boolean isCacheActive();

    void updateRootStreams(Map<String, Stream> streams);
}

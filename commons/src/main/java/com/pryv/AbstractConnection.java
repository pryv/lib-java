package com.pryv;

import com.pryv.model.Stream;

import org.joda.time.DateTime;

import java.util.Map;

public interface AbstractConnection {
    DateTime serverTimeInSystemDate(double time);

    Map<String, Stream> getRootStreams();

    void updateRootStreams(Map<String, Stream> streams);
}

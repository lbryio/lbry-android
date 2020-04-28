package io.lbry.browser.model;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;

import lombok.Data;

@Data
public class File {
    private Claim.StreamMetadata metadata;
    private long addedOn;
    private int blobsCompleted;
    private int blobsInStream;
    private int blobsRemaining;
    private String channelClaimId;
    private String channelName;
    private String claimId;
    private String claimName;
    private boolean completed;
    private String downloadDirectory;
    private String downloadPath;
    private String fileName;
    private String key;
    private String mimeType;
    private int nout;
    private String outpoint;
    private int pointsPaid;
    private String protobuf;
    private String sdHash;
    private String status;
    private boolean stopped;
    private String streamHash;
    private String streamName;
    private String streamingUrl;
    private String suggestedFileName;
    private long totalBytes;
    private long totalBytesLowerBound;
    private String txid;
    private long writtenBytes;

    public static File fromJSONObject(JSONObject fileObject) {
        String fileJson = fileObject.toString();
        Type type = new TypeToken<File>(){}.getType();
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        File file = gson.fromJson(fileJson, type);
        return file;
    }
}

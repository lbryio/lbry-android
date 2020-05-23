package io.lbry.browser.model;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;

import io.lbry.browser.utils.LbryUri;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class LbryFile {
    private Claim.StreamMetadata metadata;
    private long addedOn;
    private int blobsCompleted;
    private int blobsInStream;
    private int blobsRemaining;
    private String channelClaimId;
    private String channelName;
    @EqualsAndHashCode.Include
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
    private long timestamp;
    private long totalBytes;
    private long totalBytesLowerBound;
    private String txid;
    private long writtenBytes;

    private Claim generatedClaim;

    public Claim getClaim() {
        if (generatedClaim != null) {
            return generatedClaim;
        }

        generatedClaim = new Claim();
        generatedClaim.setValueType(Claim.TYPE_STREAM);
        generatedClaim.setPermanentUrl(LbryUri.tryParse(String.format("%s#%s", claimName, claimId)).toString());
        generatedClaim.setClaimId(claimId);
        generatedClaim.setName(claimName);
        generatedClaim.setValue(metadata);
        generatedClaim.setConfirmations(1);
        generatedClaim.setTxid(txid);
        generatedClaim.setNout(nout);
        generatedClaim.setFile(this);

        if (channelClaimId != null) {
            Claim signingChannel = new Claim();
            signingChannel.setClaimId(channelClaimId);
            signingChannel.setName(channelName);
            signingChannel.setPermanentUrl(LbryUri.tryParse(String.format("%s#%s", claimName, claimId)).toString());
            generatedClaim.setSigningChannel(signingChannel);
        }

        return generatedClaim;
    }

    public static LbryFile fromJSONObject(JSONObject fileObject) {
        String fileJson = fileObject.toString();
        Type type = new TypeToken<LbryFile>(){}.getType();
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        LbryFile file = gson.fromJson(fileJson, type);

        if (file.getMetadata() != null && file.getMetadata().getReleaseTime() == 0) {
            file.getMetadata().setReleaseTime(file.getTimestamp());
        }
        return file;
    }
}

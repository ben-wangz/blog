package tech.geekcity.flink.connectors.s3.multiple;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.apache.flink.annotation.Internal;
import org.apache.flink.api.connector.source.Boundedness;
import org.apache.flink.api.connector.source.Source;
import org.apache.flink.api.connector.source.SourceOutput;
import org.apache.flink.api.connector.source.SourceReader;
import org.apache.flink.api.connector.source.SourceReaderContext;
import org.apache.flink.api.connector.source.SourceSplit;
import org.apache.flink.api.connector.source.SplitEnumerator;
import org.apache.flink.api.connector.source.SplitEnumeratorContext;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.base.source.reader.RecordEmitter;
import org.apache.flink.connector.base.source.reader.RecordsWithSplitIds;
import org.apache.flink.connector.base.source.reader.SingleThreadMultiplexSourceReaderBase;
import org.apache.flink.connector.base.source.reader.splitreader.SplitReader;
import org.apache.flink.connector.base.source.reader.splitreader.SplitsChange;
import org.apache.flink.connector.file.src.ContinuousEnumerationSettings;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.core.memory.DataOutputSerializer;

@Getter
@EqualsAndHashCode
@ToString
public class S3ParquetSource
    implements Source<
        String, S3ParquetSource.S3ParquetSplit, S3ParquetSource.S3ParquetEnumeratorState> {
  @Getter
  @EqualsAndHashCode
  @ToString
  public static class S3Connection {
    private final String endpoint;
    private final String accessKey;
    private final String accessSecret;

    @Builder
    @Jacksonized
    public S3Connection(String endpoint, String accessKey, String accessSecret) {
      this.endpoint = endpoint;
      this.accessKey = accessKey;
      this.accessSecret = accessSecret;
    }
  }

  @Getter
  @EqualsAndHashCode
  @ToString
  public static class S3ParquetSplit implements SourceSplit, Serializable {
    private final String path;
    private final long offset;

    @Builder
    @Jacksonized
    public S3ParquetSplit(String path, long offset) {
      this.path = path;
      this.offset = offset;
    }

    @Override
    public String splitId() {
      return String.format("(%s,%s)", path, offset);
    }
  }

  @Getter
  @EqualsAndHashCode
  @ToString
  public static class S3ParquetSplitState implements Serializable {
    private final S3ParquetSplit split;
    private final long currentOffset;

    @Builder
    @Jacksonized
    public S3ParquetSplitState(S3ParquetSplit split, long currentOffset) {
      this.split = split;
      this.currentOffset = currentOffset;
    }
  }

  @Getter
  @EqualsAndHashCode
  @ToString
  public static class S3ParquetEnumeratorState implements Serializable {
    private final Collection<S3ParquetSplit> splits;
    private final Collection<S3ParquetSplit> alreadyProcessedSplits;

    @Builder
    public S3ParquetEnumeratorState(
        Collection<S3ParquetSplit> splits, Collection<S3ParquetSplit> alreadyProcessedSplits) {
      this.splits = null == splits ? new ArrayList<>() : splits;
      this.alreadyProcessedSplits =
          null == alreadyProcessedSplits ? new ArrayList<>() : alreadyProcessedSplits;
    }
  }

  @Getter
  public static class S3ParquetSplitReader implements SplitReader<String, S3ParquetSplit> {
    @Builder
    public S3ParquetSplitReader() {}

    @Override
    public void close() throws Exception {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'close'");
    }

    @Override
    public RecordsWithSplitIds<String> fetch() throws IOException {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'fetch'");
    }

    @Override
    public void handleSplitsChanges(SplitsChange<S3ParquetSplit> arg0) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'handleSplitsChanges'");
    }

    @Override
    public void wakeUp() {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'wakeUp'");
    }
  }

  @Getter
  @EqualsAndHashCode
  @ToString
  public static class S3ParquetRecordEmitter
      implements RecordEmitter<String, String, S3ParquetSplitState> {
    @Builder
    public S3ParquetRecordEmitter() {}

    @Override
    public void emitRecord(
        String element, SourceOutput<String> sourceOutput, S3ParquetSplitState s3ParquetSplit)
        throws Exception {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'emitRecord'");
    }
  }

  @Getter
  public static class S3ParquetReader
      extends SingleThreadMultiplexSourceReaderBase<
          String, String, S3ParquetSplit, S3ParquetSplitState> {
    @Builder
    public S3ParquetReader(Configuration configuration, SourceReaderContext sourceReaderContext) {
      super(
          () -> S3ParquetSplitReader.builder().build(),
          S3ParquetRecordEmitter.builder().build(),
          configuration,
          sourceReaderContext);
    }

    @Override
    protected S3ParquetSplitState initializedState(S3ParquetSplit s3ParquetSplit) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'initializedState'");
    }

    @Override
    protected void onSplitFinished(Map<String, S3ParquetSplitState> finishedSplitIds) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'onSplitFinished'");
    }

    @Override
    protected S3ParquetSplit toSplitType(String splitId, S3ParquetSplitState s3ParquetSplitState) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'toSplitType'");
    }
  }

  @Getter
  @ToString
  public static class S3ParquetEnumerator
      implements SplitEnumerator<S3ParquetSplit, S3ParquetEnumeratorState> {
    private final SplitEnumeratorContext<S3ParquetSplit> enumeratorContext;
    private final S3ParquetEnumeratorState s3ParquetEnumeratorState;

    @Builder
    public S3ParquetEnumerator(
        SplitEnumeratorContext<S3ParquetSplit> enumeratorContext,
        S3ParquetEnumeratorState s3ParquetEnumeratorState) {
      this.enumeratorContext = enumeratorContext;
      this.s3ParquetEnumeratorState =
          null == s3ParquetEnumeratorState
              ? S3ParquetEnumeratorState.builder().build()
              : s3ParquetEnumeratorState;
    }

    @Override
    public void start() {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'start'");
    }

    @Override
    public void handleSplitRequest(int subtaskId, @Nullable String requesterHostname) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'handleSplitRequest'");
    }

    @Override
    public void addSplitsBack(List<S3ParquetSplit> splits, int subtaskId) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'addSplitsBack'");
    }

    @Override
    public void addReader(int subtaskId) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'addReader'");
    }

    @Override
    public S3ParquetEnumeratorState snapshotState(long checkpointId) throws Exception {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'snapshotState'");
    }

    @Override
    public void close() throws IOException {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'close'");
    }
  }

  public static class S3ParquetSplitSerializer
      implements SimpleVersionedSerializer<S3ParquetSplit> {
    public static final S3ParquetSplitSerializer INSTANCE = new S3ParquetSplitSerializer();
    private static final ThreadLocal<DataOutputSerializer> SERIALIZER_CACHE =
        ThreadLocal.withInitial(() -> new DataOutputSerializer(64));
    public static final int CURRENT_VERSION = 0;

    @Override
    public int getVersion() {
      return CURRENT_VERSION;
    }

    @Override
    public byte[] serialize(S3ParquetSplit obj) throws IOException {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'serialize'");
    }

    @Override
    public S3ParquetSplit deserialize(int version, byte[] serialized) throws IOException {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'deserialize'");
    }
  }

  public static class S3ParquetEnumeratorStateSerializer
      implements SimpleVersionedSerializer<S3ParquetEnumeratorState> {
    public static final S3ParquetEnumeratorStateSerializer INSTANCE =
        new S3ParquetEnumeratorStateSerializer();
    private static final ThreadLocal<DataOutputSerializer> SERIALIZER_CACHE =
        ThreadLocal.withInitial(() -> new DataOutputSerializer(64));
    public static final int CURRENT_VERSION = 0;

    @Override
    public int getVersion() {
      return CURRENT_VERSION;
    }

    @Override
    public byte[] serialize(S3ParquetEnumeratorState obj) throws IOException {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'serialize'");
    }

    @Override
    public S3ParquetEnumeratorState deserialize(int version, byte[] serialized) throws IOException {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'deserialize'");
    }
  }

  private final Configuration configuration;
  private final S3Connection s3Connection;
  private final String bucket;
  private final String path;
  private ContinuousEnumerationSettings continuousEnumerationSettings;

  @Builder
  public S3ParquetSource(
      Configuration configuration,
      ContinuousEnumerationSettings ContinuousEnumerationSettings,
      S3Connection s3connConnection,
      String bucket,
      String path) {
    this.configuration = configuration;
    this.s3Connection = s3connConnection;
    this.bucket = bucket;
    this.path = path;
    this.continuousEnumerationSettings = ContinuousEnumerationSettings;
  }

  @Override
  public Boundedness getBoundedness() {
    if (null == continuousEnumerationSettings) {
      return Boundedness.BOUNDED;
    }
    return Boundedness.CONTINUOUS_UNBOUNDED;
  }

  @Internal
  @Override
  public SourceReader<String, S3ParquetSplit> createReader(
      SourceReaderContext sourceReaderContext) {
    return S3ParquetReader.builder().sourceReaderContext(sourceReaderContext).build();
  }

  @Internal
  @Override
  public SplitEnumerator<S3ParquetSplit, S3ParquetEnumeratorState> createEnumerator(
      SplitEnumeratorContext<S3ParquetSplit> enumeratorContext) {
    return S3ParquetEnumerator.builder().enumeratorContext(enumeratorContext).build();
  }

  @Internal
  @Override
  public SplitEnumerator<S3ParquetSplit, S3ParquetEnumeratorState> restoreEnumerator(
      SplitEnumeratorContext<S3ParquetSplit> enumContext, S3ParquetEnumeratorState checkpointState)
      throws IOException {
    return S3ParquetEnumerator.builder()
        .enumeratorContext(enumContext)
        .s3ParquetEnumeratorState(checkpointState)
        .build();
  }

  @Internal
  @Override
  public SimpleVersionedSerializer<S3ParquetSplit> getSplitSerializer() {
    return S3ParquetSplitSerializer.INSTANCE;
  }

  @Internal
  @Override
  public SimpleVersionedSerializer<S3ParquetEnumeratorState> getEnumeratorCheckpointSerializer() {
    return S3ParquetEnumeratorStateSerializer.INSTANCE;
  }
}

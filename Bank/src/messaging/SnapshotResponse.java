package messaging;

public class SnapshotResponse extends Response {

    private Snapshot snapshot;

    public SnapshotResponse(String failure_reason) {
        this.success = false;
        this.failure_reason = failure_reason;
    }

    public SnapshotResponse(Snapshot snapshot) {
        this.success = true;
        this.snapshot = snapshot;
    }

    public Snapshot getSnapshot() {
        return this.snapshot;
    }
}


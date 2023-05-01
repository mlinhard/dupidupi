package sk.linhard.dupidupi.report;

public record Edge(Path from, Path to) {

    public Edge parents() {
        var fromParent = from.getParent();
        var toParent = to.getParent();
        return fromParent != null && toParent != null
                ? new Edge(fromParent, toParent)
                : null;
    }
}
